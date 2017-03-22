package net.whistlingfish.harmony.protocol;

import org.jivesoftware.smack.UnparseableStanza;
import org.jivesoftware.smack.parsing.ParsingExceptionCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HarmonyExceptionLoggingCallback implements ParsingExceptionCallback {

    private static final Logger logger = LoggerFactory.getLogger(HarmonyExceptionLoggingCallback.class);

    @Override
    public void handleUnparsableStanza(UnparseableStanza unparsed) throws Exception {
        if ("<iq/>".equals(unparsed.getContent().toString())) {
            logger.trace("Skipping <iq/> Stanza");
        } else {
            logger.warn("Smack message parsing exception. Content: '" + unparsed.getContent() + "'",
                    unparsed.getParsingException());
        }
    }
}
