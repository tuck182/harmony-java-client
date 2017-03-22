package net.whistlingfish.harmony.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.SimpleIQ;
import org.jivesoftware.smack.packet.XMPPError;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Joiner;

public abstract class OAStanza extends IQ {
    private static final long CREATION_TIME = System.currentTimeMillis();

    private final String mimeType;
    private String statusCode;
    private String errorString;

    public OAStanza(String mimeType) {
        super(new SimpleIQ("oa", "connect.logitech.com") {
        });
        this.mimeType = mimeType;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    @JsonIgnore // Subclasses use a Jackson object mapper that throws an exception for properties with multiple setters
    public void setError(XMPPError error) {
        super.setError(error);
    }

    public String getErrorString() {
        return errorString;
    }

    public void setErrorString(String errorString) {
        this.errorString = errorString;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        if (statusCode != null) {
            xml.attribute("errorcode", statusCode);
        }
        if (errorString != null) {
            xml.attribute("errorstring", errorString);
        }

        xml.attribute("mime", getMimeType());
        xml.rightAngleBracket();
        xml.append(joinChildElementPairs(getChildElementPairs()));
        return xml;
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
