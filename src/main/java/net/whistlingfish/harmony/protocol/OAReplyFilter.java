package net.whistlingfish.harmony.protocol;

import java.util.Locale;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.FromMatchesFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Copied from IQReplyFilter, but tweaked to support the Harmony's response pattern
 */
public class OAReplyFilter implements PacketFilter {
    private static Logger logger = LoggerFactory.getLogger(OAReplyFilter.class);

    private final PacketFilter iqAndIdFilter;
    private final OrFilter fromFilter;
    private final String to;
    private final String local;
    private final String server;
    private final String packetId;

    public OAReplyFilter(OAPacket request, XMPPConnection connection) {
        to = request.getTo();
        if (connection.getUser() == null) {
            // We have not yet been assigned a username, this can happen if the connection is
            // in an early stage, i.e. when performing the SASL auth.
            local = null;
        } else {
            local = connection.getUser().toLowerCase(Locale.US);
        }
        server = connection.getServiceName().toLowerCase(Locale.US);
        packetId = request.getPacketID();

        PacketFilter iqFilter = new OrFilter(new IQTypeFilter(IQ.Type.ERROR), new IQTypeFilter(IQ.Type.GET));
        PacketFilter idFilter = new PacketIDFilter(request);
        iqAndIdFilter = new AndFilter(iqFilter, idFilter);
        fromFilter = new OrFilter();
        fromFilter.addFilter(FromMatchesFilter.createFull(to));
        if (to == null) {
            if (local != null)
                fromFilter.addFilter(FromMatchesFilter.createBare(local));
            fromFilter.addFilter(FromMatchesFilter.createFull(server));
        } else if (local != null && to.toLowerCase(Locale.US).equals(StringUtils.parseBareAddress(local))) {
            fromFilter.addFilter(FromMatchesFilter.createFull(null));
        }
    }

    @Override
    public boolean accept(Packet packet) {
        // First filter out everything that is not an IQ stanza and does not have the correct ID set.
        if (!iqAndIdFilter.accept(packet))
            return false;

        // Second, check if the from attributes are correct and log potential IQ spoofing attempts
        if (fromFilter.accept(packet)) {
            return true;
        } else {
            logger.warn(String.format("Rejected potentially spoofed reply to IQ-packet. Filter settings: "
                    + "packetId=%s, to=%s, local=%s, server=%s. Received packet with from=%s", packetId, to, local,
                    server, packet.getFrom()), packet);
            return false;
        }
    }

}
