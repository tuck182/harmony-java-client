package net.whistlingfish.harmony.protocol;

import org.jivesoftware.smack.packet.IQ;

public abstract class OAReplyParser {
    public abstract IQ parseReplyContents(String contents);
}
