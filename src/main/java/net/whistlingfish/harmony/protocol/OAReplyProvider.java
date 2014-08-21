package net.whistlingfish.harmony.protocol;

import java.util.HashMap;
import java.util.Map;

import net.whistlingfish.harmony.protocol.GetCurrentActivity.GetCurrentActivityReplyParser;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

import static java.lang.String.format;

public class OAReplyProvider implements IQProvider {
    private static Map<String, OAReplyParser> replyParsers = new HashMap<>();
    static {
        replyParsers.put(AuthReply.MIME_TYPE, new AuthReplyParser());
        replyParsers.put(GetConfigReply.MIME_TYPE, new GetConfigReplyParser());
        replyParsers.put(PressButtonReplyParser.MIME_TYPE, new PressButtonReplyParser());
        replyParsers.put(GetCurrentActivity.MIME_TYPE, new GetCurrentActivityReplyParser());
    }

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
        if (!"200".equals(attrs.get("errorcode"))) {
            throw new HarmonyProtocolException(format("Got error response [%s]: %s", attrs.get("errorcode"),
                    attrs.get("errorstring")));
        }

        String mimeType = parser.getAttributeValue(null, "mime");
        OAReplyParser replyParser = replyParsers.get(mimeType);
        if (replyParser == null) {
            throw new HarmonyProtocolException(format("Unable to handle reply type '%s'", mimeType));
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
        return replyParser.parseReplyContents(contents.toString());
    }
}
