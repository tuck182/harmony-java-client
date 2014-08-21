package net.whistlingfish.harmony.protocol;

import java.util.Map;

import org.jivesoftware.smack.packet.IQ;

import static java.util.Collections.emptyMap;

public class MessageGetConfig {
    public static final String MIME_TYPE = "vnd.logitech.harmony/vnd.logitech.harmony.engine?config";

    /*
     * Request
     */
    public static class GetConfigRequest extends OAPacket {
        public GetConfigRequest() {
            super(MIME_TYPE);
        }

        @Override
        protected Map<String, Object> getChildElementPairs() {
            return emptyMap();
        }
    }

    /*
     * Reply
     */
    public static class GetConfigReply extends OAPacket {
        private String contents;

        public GetConfigReply(String contents) {
            super(MIME_TYPE);
            this.contents = contents;
        }

        @Override
        protected Map<String, Object> getChildElementPairs() {
            return emptyMap();
        }

        public String getConfig() {
            return contents;
        }
    }

    /*
     * Parser
     */
    public static class GetConfigReplyParser extends OAReplyParser {

        @Override
        public IQ parseReplyContents(String statusCode, String errorString, String contents) {
            return new GetConfigReply(contents);
        }
    }
}
