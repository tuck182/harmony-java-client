package net.whistlingfish.harmony.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jivesoftware.smack.packet.IQ;

import com.google.common.base.Joiner;

public abstract class OAPacket extends IQ {
    private static final long CREATION_TIME = System.currentTimeMillis();

    private final String mimeType;
    private String statusCode;
    private String errorString;

    public OAPacket(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getErrorString() {
        return errorString;
    }

    public void setErrorString(String errorString) {
        this.errorString = errorString;
    }

    @Override
    public CharSequence getChildElementXML() {
        StringBuilder sb = new StringBuilder() //
                .append("\n<oa xmlns=\"connect.logitech.com\"");
        if (statusCode != null)
            sb.append(" errorcode=\"").append(statusCode).append("\"");
        if (errorString != null)
            sb.append(" errorstring=\"").append(errorString).append("\"");
        sb.append(" mime=\"").append(getMimeType()).append("\">") //
                .append(joinChildElementPairs(getChildElementPairs()))
                .append("</oa>\n");
        return sb.toString();
    }

    protected String getMimeType() {
        return mimeType;
    }

    private String joinChildElementPairs(Map<String, Object> pairs) {
        List<String> parts = new ArrayList<>();
        for (Entry<String, Object> pair : pairs.entrySet()) {
            parts.add(pair.getKey() + "=" + pair.getValue());
        }
        return Joiner.on(":").join(parts);
    }

    protected abstract Map<String, Object> getChildElementPairs();

    public boolean isContinuePacket() {
        return "100".equals(statusCode);
    }

    protected long generateTimestamp() {
        return System.currentTimeMillis() - CREATION_TIME;
    }
}
