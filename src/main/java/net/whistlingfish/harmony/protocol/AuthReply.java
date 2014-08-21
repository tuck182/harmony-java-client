package net.whistlingfish.harmony.protocol;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import static java.lang.String.format;

public class AuthReply extends OA {
    public static String MIME_TYPE = "vnd.logitech.connect/vnd.logitech.pair";

    private String serverIdentity;
    private String hubId;
    private String identity;
    private String status;
    private Map<String, String> protocolVersion;
    private Map<String, String> hubProfiles;
    private String productId;
    private String friendlyName;

    public AuthReply() {
        super(MIME_TYPE);
    }

    @Override
    protected Map<String, Object> getChildElementPairs() {
        return ImmutableMap.<String, Object> builder() //
                .put("serverIdentity", serverIdentity)
                .put("hubId", hubId)
                .put("identity", identity)
                .put("status", status)
                .put("protocolVersion", protocolVersion)
                .put("hubProfiles", hubProfiles)
                .put("productId", productId)
                .put("friendlyName", friendlyName)
                .build();
    }

    public String getUsername() {
        return format("%s@connect.logitech.com/gatorade", identity);
    }

    public String getPassword() {
        return identity;
    }

    public String getServerIdentity() {
        return serverIdentity;
    }

    public String getHubId() {
        return hubId;
    }

    public String getIdentity() {
        return identity;
    }

    public String getStatus() {
        return status;
    }

    public Map<String, String> getProtocolVersion() {
        return protocolVersion;
    }

    public Map<String, String> getHubProfiles() {
        return hubProfiles;
    }

    public String getProductId() {
        return productId;
    }

    public String getFriendlyName() {
        return friendlyName;
    }
}
