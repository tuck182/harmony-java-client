package net.whistlingfish.harmony.protocol;

import org.jivesoftware.smack.packet.IQ;

public class GetConfigReplyParser extends OAReplyParser {

    @Override
    public IQ parseReplyContents(String contents) {
        return new GetConfigReply(contents);
    }

}
