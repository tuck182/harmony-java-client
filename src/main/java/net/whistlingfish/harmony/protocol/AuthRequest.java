package net.whistlingfish.harmony.protocol;

import java.util.Map;
import java.util.UUID;

import org.jivesoftware.smack.packet.IQ;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.BaseEncoding;

public class AuthRequest extends OA {
    public static String MIME_TYPE = "vnd.logitech.connect/vnd.logitech.pair";

    private LoginToken loginToken;

    public AuthRequest(LoginToken loginToken) {
        super(MIME_TYPE);
        this.loginToken = loginToken;
        setType(IQ.Type.GET);
    }

    @Override
    protected Map<String, Object> getChildElementPairs() {
        return ImmutableMap.<String, Object> builder() //
                .put("token", loginToken.getUserAuthToken())
                .put("name", generateUniqueId() + "#" + getDeviceIdentifier())
                .build();
    }

    private String generateUniqueId() {
        return BaseEncoding.base64().encode(UUID.randomUUID().toString().getBytes());
    }

    private String getDeviceIdentifier() {
        return "iOS6.0.1#iPhone";
    }
}
