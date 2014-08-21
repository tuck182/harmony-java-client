package net.whistlingfish.harmony.protocol;

import java.util.Map;

import static java.util.Collections.emptyMap;


public class GetConfigRequest extends OA {
    private static final String MIME_TYPE = "vnd.logitech.harmony/vnd.logitech.harmony.engine?config";

    public GetConfigRequest() {
        super(MIME_TYPE);
    }

    @Override
    protected Map<String, Object> getChildElementPairs() {
        return emptyMap();
    }
}
