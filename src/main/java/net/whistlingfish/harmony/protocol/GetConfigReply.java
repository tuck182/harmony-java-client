package net.whistlingfish.harmony.protocol;

import java.util.Map;

import static java.util.Collections.emptyMap;


public class GetConfigReply extends OA {
    public static String MIME_TYPE = "vnd.logitech.harmony/vnd.logitech.harmony.engine?config";
    private String contents;

    public GetConfigReply(String contents) {
        super(MIME_TYPE);
        this.contents = contents;
    }

    @Override
    protected Map<String, Object> getChildElementPairs() {
        return emptyMap();
    }

    public String getConfig() {
        return contents;
    }
}
