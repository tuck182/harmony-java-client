package net.whistlingfish.harmony.protocol;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.packet.EmptyResultIQ;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.packet.XMPPError.Condition;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.PacketParserUtils;
import org.jivesoftware.smack.util.ParserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParser;

public class HarmonyXMPPTCPConnection extends XMPPTCPConnection {

    private static final Logger logger = LoggerFactory.getLogger(HarmonyXMPPTCPConnection.class);

    public HarmonyXMPPTCPConnection(XMPPTCPConnectionConfiguration config) {
        super(config);
    }

    @Override
    protected void parseAndProcessStanza(XmlPullParser parser) throws Exception {
        ParserUtils.assertAtStartTag(parser);
        int parserDepth = parser.getDepth();
        Stanza stanza = null;
        try {
            if (IQ.IQ_ELEMENT.equals(parser.getName()) && parser.getAttributeValue("", "type") == null) {
                // Acknowledgement IQs don't contain a type so an empty result is created here to prevent a parsing NPE
                stanza = new EmptyResultIQ();
            } else {
                stanza = PacketParserUtils.parseStanza(parser);
            }
        } catch (Exception e) {
            CharSequence content = PacketParserUtils.parseContentDepth(parser, parserDepth);
            logger.warn("Smack message parsing exception. Content: '{}'", content, e);
        }
        ParserUtils.assertAtEndTag(parser);
        if (stanza != null) {
            processStanza(stanza);
        }
    }

    @Override
    public void sendStanza(Stanza stanza) throws NotConnectedException, InterruptedException {
        if (stanza.getError() == null || stanza.getError().getCondition() != Condition.service_unavailable) {
            super.sendStanza(stanza);
        }
    }

}
