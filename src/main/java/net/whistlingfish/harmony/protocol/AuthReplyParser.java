package net.whistlingfish.harmony.protocol;

import org.jivesoftware.smack.packet.IQ;

import static net.whistlingfish.harmony.Jackson.OBJECT_MAPPER;

public class AuthReplyParser extends OAReplyParser {
    @Override
    public IQ parseReplyContents(String contents) {
        return OBJECT_MAPPER.convertValue(parseKeyValuePairs(contents), AuthReply.class);
    }
}
