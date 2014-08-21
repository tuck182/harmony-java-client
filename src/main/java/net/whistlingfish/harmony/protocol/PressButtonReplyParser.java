package net.whistlingfish.harmony.protocol;

import java.util.Map;

import org.jivesoftware.smack.packet.IQ;

import com.google.common.collect.ImmutableMap;

import static java.util.Collections.emptyMap;

public class PressButtonReplyParser extends OAReplyParser {
    public static final String MIME_TYPE = "vnd.logitech.harmony/vnd.logitech.harmony.engine?holdAction";

    @Override
    public IQ parseReplyContents(String contents) {
        return new PressButtonReply();
    }

    /*
     * Request
     */
    public static class PressButtonRequest extends IrCommand {
        private String button;

        public PressButtonRequest(String button) {
            super(MIME_TYPE);
            this.button = button;
        }

        @Override
        protected Map<String, Object> getChildElementPairs() {
            return ImmutableMap.<String, Object> builder() //
                    .put("action", generateAction("16267476", button))
                    .put("status", "press")
                    .build();
        }
    }

    /*
     * Reply
     */
    public static class PressButtonReply extends OA {
        public PressButtonReply() {
            super(MIME_TYPE);
        }

        @Override
        protected Map<String, Object> getChildElementPairs() {
            return emptyMap();
        }
    }
}
