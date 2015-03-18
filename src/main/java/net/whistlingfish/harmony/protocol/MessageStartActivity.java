package net.whistlingfish.harmony.protocol;

import java.util.Map;

import org.jivesoftware.smack.packet.IQ;

import com.google.common.collect.ImmutableMap;

import static net.whistlingfish.harmony.Jackson.OBJECT_MAPPER;

public class MessageStartActivity {
    public static final String MIME_TYPE = "vnd.logitech.harmony/vnd.logitech.harmony.engine?startactivity";
    public static final String MIME_TYPE2 = "harmony.engine?startActivity";

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
                    .put("timestamp", generateTimestamp())
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
     * Parser
     */
    public static class StartActivityReplyParser extends OAReplyParser {
        @Override
        public IQ parseReplyContents(String statusCode, String errorString, String contents) {
            return OBJECT_MAPPER.convertValue(parseKeyValuePairs(statusCode, errorString, contents),
                    StartActivityReply.class);
        }
        
        @Override
        public boolean validResponseCode(String code){
        	//sometimes the start activity will return a 401 if a device is not setup correctly
        	return super.validResponseCode(code) || code.equals("401");
        }
    }

}
