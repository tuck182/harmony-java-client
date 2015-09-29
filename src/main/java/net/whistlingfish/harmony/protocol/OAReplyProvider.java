package net.whistlingfish.harmony.protocol;

import java.util.HashMap;
import java.util.Map;

import net.whistlingfish.harmony.protocol.MessageAuth.AuthReplyParser;
import net.whistlingfish.harmony.protocol.MessageGetConfig.GetConfigReplyParser;
import net.whistlingfish.harmony.protocol.MessageGetCurrentActivity.GetCurrentActivityReplyParser;
import net.whistlingfish.harmony.protocol.MessageHoldAction.HoldActionReplyParser;
import net.whistlingfish.harmony.protocol.MessagePing.PingReplyParser;
import net.whistlingfish.harmony.protocol.MessageStartActivity.StartActivityReplyParser;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

import static java.lang.String.format;

public class OAReplyProvider implements IQProvider {
    private static Map<String, OAReplyParser> replyParsers = new HashMap<>();
    static {
        replyParsers.put(MessageAuth.MIME_TYPE, new AuthReplyParser());
        replyParsers.put(MessageGetConfig.MIME_TYPE, new GetConfigReplyParser());
        replyParsers.put(MessageHoldAction.MIME_TYPE, new HoldActionReplyParser());
        replyParsers.put(MessageGetCurrentActivity.MIME_TYPE, new GetCurrentActivityReplyParser());
        replyParsers.put(MessageStartActivity.MIME_TYPE, new StartActivityReplyParser());
        replyParsers.put(MessageStartActivity.MIME_TYPE2, new StartActivityReplyParser());
        replyParsers.put(MessagePing.MIME_TYPE, new PingReplyParser());
    }

//    private static Set<String> validResponses = new HashSet<>();
//    static {
//        validResponses.add("100");
//        validResponses.add("200");
//    }

    @Override
    public IQ parseIQ(XmlPullParser parser) throws Exception {
        String elementName = parser.getName();

        Map<String, String> attrs = new HashMap<>();
        for (int i = 0; i < parser.getAttributeCount(); i++) {
            String prefix = parser.getAttributePrefix(i);
            if (prefix != null) {
                attrs.put(prefix + ":" + parser.getAttributeName(i), parser.getAttributeValue(i));
            } else {
                attrs.put(parser.getAttributeName(i), parser.getAttributeValue(i));
            }
        }
        String statusCode = attrs.get("errorcode");
        String errorString = attrs.get("errorstring");

        String mimeType = parser.getAttributeValue(null, "mime");
        OAReplyParser replyParser = replyParsers.get(mimeType);
        if (replyParser == null) {
            throw new HarmonyProtocolException(format("Unable to handle reply type '%s'", mimeType));
        }
        if (!replyParser.validResponseCode(statusCode)) {
            throw new HarmonyProtocolException(format("Got error response [%s]: %s", statusCode,
                    attrs.get("errorstring")));
        }

        StringBuilder contents = new StringBuilder();
        boolean done = false;
        while (!done) {
            switch (parser.next()) {
            case XmlPullParser.END_TAG:
                if (parser.getName().equals(elementName)) {
                    done = true;
                    break;
                }
                // otherwise fall through to default
            default:
                contents.append(parser.getText());
                break;
            }
        }
        return replyParser.parseReplyContents(statusCode, errorString, contents.toString());
    }
}
