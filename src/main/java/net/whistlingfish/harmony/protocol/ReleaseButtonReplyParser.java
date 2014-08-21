package net.whistlingfish.harmony.protocol;

import java.util.Map;

import org.jivesoftware.smack.packet.IQ;

import com.google.common.collect.ImmutableMap;

import static java.util.Collections.emptyMap;

public class ReleaseButtonReplyParser extends OAReplyParser {
    public static final String MIME_TYPE = "vnd.logitech.harmony/vnd.logitech.harmony.engine?holdAction";

    @Override
    public IQ parseReplyContents(String contents) {
        return new ReleaseButtonReply();
    }

    /*
     * Request
     */
    public static class ReleaseButtonRequest extends IrCommand {
        private String button;

        public ReleaseButtonRequest(String button) {
            super(MIME_TYPE);
            this.button = button;
        }

        @Override
        protected Map<String, Object> getChildElementPairs() {
            return ImmutableMap.<String, Object> builder() //
                    .put("action", generateAction("16267476", button))
                    .put("status", "release")
                    .build();
        }
    }

    /*
     * Reply
     */
    public class ReleaseButtonReply extends OA {
        public ReleaseButtonReply() {
            super(MIME_TYPE);
        }

        @Override
        protected Map<String, Object> getChildElementPairs() {
            return emptyMap();
        }
    }
}
