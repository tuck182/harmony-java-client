package net.whistlingfish.harmony.protocol;

import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.packet.IQ;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;

import static java.util.Collections.emptyMap;

public class MessageHoldAction {
    public static final String MIME_TYPE = "vnd.logitech.harmony/vnd.logitech.harmony.engine?holdAction";

    /*
     * Request
     */
    public static class HoldActionRequest extends IrCommand {
        private int deviceId;
        private String button;
        private HoldStatus status;

        public HoldActionRequest(int deviceId, String button, HoldStatus status) {
            super(MIME_TYPE);
            this.deviceId = deviceId;
            this.button = button;
            this.status = status;
        }

        @Override
        protected Map<String, Object> getChildElementPairs() {
            return ImmutableMap.<String, Object> builder() //
                    .put("action", generateAction(deviceId, button))
                    .put("status", status)
                    .put("timestamp", generateTimestamp())
                    .build();
        }
    }

    /*
     * Reply (unused)
     */
    public static class HoldActionReply extends OAPacket {
        public HoldActionReply() {
            super(MIME_TYPE);
        }

        @Override
        protected Map<String, Object> getChildElementPairs() {
            return emptyMap();
        }
    }

    /*
     * Parser (unused)
     */
    public static class HoldActionReplyParser extends OAReplyParser {
        @Override
        public IQ parseReplyContents(String statusCode, String errorString, String contents) {
            return new HoldActionReply();
        }
    }

    public enum HoldStatus {
        PRESS("press"), RELEASE("release");

        private static Map<String, HoldStatus> valueMap;

        private final String description;

        private HoldStatus(String description) {
            this.description = description;
            storeInValueMap(this);
        }

        private void storeInValueMap(HoldStatus holdStatus) {
            if (valueMap == null)
                valueMap = new HashMap<String, HoldStatus>();
            valueMap.put(description, this);
        }

        @JsonValue
        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return description;
        }

        @JsonCreator
        public static HoldStatus forValue(String description) {
            HoldStatus result = valueMap.get(description);
            if (result != null)
                return result;
            return valueOf(description);
        }
    }
}
