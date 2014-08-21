package net.whistlingfish.harmony.protocol;

import java.util.Map;

import org.jivesoftware.smack.packet.IQ;

import com.google.common.collect.ImmutableMap;

import static net.whistlingfish.harmony.Jackson.OBJECT_MAPPER;

public class MessageStartActivity {
    public static final String MIME_TYPE = "vnd.logitech.harmony/vnd.logitech.harmony.engine?startactivity";

    /*
     * Request
     */
    public static class StartActivityRequest extends IrCommand {
        private int activityId;

        public StartActivityRequest(int activityId) {
            super(MIME_TYPE);
            this.activityId = activityId;
        }

        @Override
        protected Map<String, Object> getChildElementPairs() {
            return ImmutableMap.<String, Object> builder() //
                    .put("activityId", activityId)
                    .put("timestamp", System.currentTimeMillis())
                    .build();
        }
    }

    /*
     * Reply (unused)
     */
    public static class StartActivityReply extends OAPacket {
        public StartActivityReply() {
            super(MIME_TYPE);
        }

        @Override
        protected Map<String, Object> getChildElementPairs() {
            return ImmutableMap.<String, Object> builder() //
                    .build();
        }
    }

    /*
     * Parser (unused)
     */
    public static class StartActivityReplyParser extends OAReplyParser {
        @Override
        public IQ parseReplyContents(String statusCode, String errorString, String contents) {
            return OBJECT_MAPPER.convertValue(parseKeyValuePairs(statusCode, errorString, contents),
                    StartActivityReply.class);
        }

    }

}
