package net.whistlingfish.harmony.protocol;

import java.util.Map;

import org.jivesoftware.smack.packet.IQ;

import com.google.common.collect.ImmutableMap;

import static java.util.Collections.emptyMap;
import static net.whistlingfish.harmony.Jackson.OBJECT_MAPPER;

public class MessageStartActivity {
    public static final String MIME_TYPE = "vnd.logitech.harmony/vnd.logitech.harmony.engine?startactivity";

    /*
     * Request
     */
    public static class StartActivityRequest extends IrCommand {
        public StartActivityRequest() {
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
    public static class StartActivityReply extends OA {
        public StartActivityReply() {
            super(MIME_TYPE);
        }

        @Override
        protected Map<String, Object> getChildElementPairs() {
            return ImmutableMap.<String, Object>builder() //
                    .build();
        }
    }

    /*
     * Parser
     */
    public static class StartActivityReplyParser extends OAReplyParser {
        @Override
        public IQ parseReplyContents(String contents) {
            return OBJECT_MAPPER.convertValue(parseKeyValuePairs(contents), StartActivityReply.class);
        }

    }

}
