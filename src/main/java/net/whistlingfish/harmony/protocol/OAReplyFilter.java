package net.whistlingfish.harmony.protocol;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.FromMatchesFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaIdFilter;
import org.jivesoftware.smack.packet.Stanza;
import org.jxmpp.jid.Jid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Copied from IQReplyFilter, but tweaked to support the Harmony's response pattern
 */
public class OAReplyFilter implements StanzaFilter {
    private static Logger logger = LoggerFactory.getLogger(OAReplyFilter.class);

    private final StanzaFilter iqAndIdFilter;
    private final OrFilter fromFilter;
    private final Jid to;
    private final Jid local;
    private final Jid server;
    private final String stanzaId;

    public OAReplyFilter(OAStanza request, XMPPConnection connection) {
        to = request.getTo();
        if (connection.getUser() == null) {
            // We have not yet been assigned a username, this can happen if the connection is
            // in an early stage, i.e. when performing the SASL auth.
            local = null;
        } else {
            local = connection.getUser();
        }
        server = connection.getServiceName();
        stanzaId = request.getStanzaId();

        StanzaFilter iqFilter = new OrFilter(IQTypeFilter.ERROR, IQTypeFilter.GET);
        StanzaFilter idFilter = new StanzaIdFilter(request.getStanzaId());
        iqAndIdFilter = new AndFilter(iqFilter, idFilter);
        fromFilter = new OrFilter();
        fromFilter.addFilter(FromMatchesFilter.createFull(to));
        if (to == null) {
            if (local != null) {
                fromFilter.addFilter(FromMatchesFilter.createBare(local));
            }
            fromFilter.addFilter(FromMatchesFilter.createFull(server));
        } else if (local != null && to.equals(local.asBareJid())) {
            fromFilter.addFilter(FromMatchesFilter.createFull(null));
        }
    }

    @Override
    public boolean accept(Stanza stanza) {
        // First filter out everything that is not an IQ stanza and does not have the correct ID set.
        if (!iqAndIdFilter.accept(stanza)) {
            return false;
        }

        // Second, check if the from attributes are correct and log potential IQ spoofing attempts
        if (fromFilter.accept(stanza)) {
            return true;
        } else {
            logger.warn(String.format(
                    "Rejected potentially spoofed reply to IQ-stanza. Filter settings: "
                            + "stanzaId=%s, to=%s, local=%s, server=%s. Received stanza with from=%s",
                    stanzaId, to, local, server, stanza.getFrom()), stanza);
            return false;
        }
    }

}
