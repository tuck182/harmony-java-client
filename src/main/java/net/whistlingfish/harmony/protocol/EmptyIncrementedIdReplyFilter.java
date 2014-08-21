package net.whistlingfish.harmony.protocol;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import static java.lang.String.format;

public class EmptyIncrementedIdReplyFilter implements PacketFilter {

    private final AndFilter filter;

    public EmptyIncrementedIdReplyFilter(OAPacket request, XMPPTCPConnection connection) {
        PacketFilter iqFilter = new OrFilter(new IQTypeFilter(IQ.Type.ERROR), new IQTypeFilter(IQ.Type.GET));
        PacketFilter idFilter = new PacketIDFilter(incrementPacketId(request));
        filter = new AndFilter(iqFilter, idFilter);
    }

    @Override
    public boolean accept(Packet packet) {
        return filter.accept(packet);
    }

    private Pattern numericIdRE = Pattern.compile("^(.*?)(\\d+)$");

    private String incrementPacketId(OAPacket request) {
        String packetId = request.getPacketID();
        Matcher matcher = numericIdRE.matcher(packetId);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(format("Can't handle non-numeric packet id %s", packetId));
        }
        String beginning = matcher.group(1);
        Long packetNum = Long.parseLong(matcher.group(2));
        return format("%s%d", beginning, (packetNum + 1));
    }
}
