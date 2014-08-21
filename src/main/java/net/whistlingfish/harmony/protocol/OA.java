package net.whistlingfish.harmony.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jivesoftware.smack.packet.IQ;

import com.google.common.base.Joiner;

import static java.lang.String.format;

public abstract class OA extends IQ {
    private String mimeType;

    public OA(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public CharSequence getChildElementXML() {
        return format("\n<oa xmlns=\"connect.logitech.com\" mime=\"%s\">"
                + "%s" //
                + "</oa>\n", //
                getMimeType(),
                joinChildElementPairs(getChildElementPairs()));
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
}
