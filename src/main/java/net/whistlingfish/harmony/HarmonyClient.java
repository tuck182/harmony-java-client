package net.whistlingfish.harmony;

import java.io.IOException;

import javax.inject.Inject;

import net.whistlingfish.harmony.protocol.AuthReply;
import net.whistlingfish.harmony.protocol.AuthRequest;
import net.whistlingfish.harmony.protocol.AuthService;
import net.whistlingfish.harmony.protocol.GetConfigReply;
import net.whistlingfish.harmony.protocol.GetConfigRequest;
import net.whistlingfish.harmony.protocol.LoginToken;
import net.whistlingfish.harmony.protocol.OA;
import net.whistlingfish.harmony.protocol.OAReplyFilter;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection.FromMode;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HarmonyClient {
    private static Logger logger = LoggerFactory.getLogger(HarmonyClient.class);

    private static final int DEFAULT_PORT = 5222;
    private static final String DEFAULT_XMPP_USER = "guest@connect.logitech.com/gatorade.";
    private static final String DEFAULT_XMPP_PASSWORD = "gatorade.";

    private XMPPTCPConnection connection;

    @Inject
    private AuthService authService;

    /*
     * FIXME: Wrap Smack exceptions
     */
    public void connect(String host, String username, String password) {
        // First get a login token from Logitech
        LoginToken loginToken = authService.getLoginToken(username, password);

        ConnectionConfiguration connectionConfig = createConnectionConfig(host, DEFAULT_PORT);
        XMPPTCPConnection authConnection = new XMPPTCPConnection(connectionConfig);
        try {
            addPacketLogging(authConnection, "auth");

            authConnection.connect();
            authConnection.login(DEFAULT_XMPP_USER, DEFAULT_XMPP_PASSWORD);
            authConnection.setFromMode(FromMode.USER);

            AuthRequest sessionRequest = createSessionRequest(loginToken);
            AuthReply oaResponse = sendOAPacket(authConnection, sessionRequest, AuthReply.class);

            logger.debug("Got session response: {}", oaResponse);
            authConnection.disconnect();

            connection = new XMPPTCPConnection(connectionConfig);
            addPacketLogging(connection, "main");
            connection.connect();
            connection.login(oaResponse.getUsername(), oaResponse.getPassword());

        } catch (XMPPException | SmackException | IOException e) {
            throw new RuntimeException("Failed communicating with Harmony Hub", e);
        }
    }

    private <R extends OA> R sendOAPacket(XMPPTCPConnection authConnection, OA packet, Class<R> replyClass) {
        try {
            PacketCollector collector = authConnection.createPacketCollector(new OAReplyFilter(packet, authConnection));
            authConnection.sendPacket(packet);
            // Packet = collector.nextResultOrThrow();
            Packet reply = collector.nextResultBlockForever();
            return replyClass.cast(reply);
        } catch (/* XMPPException | */SmackException e) {
            throw new RuntimeException("Failed communicating with Harmony Hub", e);
        }
    }

    private void addPacketLogging(XMPPTCPConnection authConnection, final String prefix) {
        PacketFilter allPacketsFilter = new PacketFilter() {
            @Override
            public boolean accept(Packet packet) {
                return true;
            }
        };
        authConnection.addPacketInterceptor(new PacketInterceptor() {
            @Override
            public void interceptPacket(Packet packet) {
                logger.debug("{}>>> {}", prefix, packet);
            }
        }, allPacketsFilter);
        authConnection.addPacketListener(new PacketListener() {
            @Override
            public void processPacket(Packet packet) throws NotConnectedException {
                logger.debug("<<<{} {}", prefix, packet);
            }
        }, allPacketsFilter);
    }

    private ConnectionConfiguration createConnectionConfig(String host, int port) {
        SASLAuthentication.supportSASLMechanism("PLAIN");

        ConnectionConfiguration config = new ConnectionConfiguration(host, port);
        return config;
    }

    public String getConfig() {
        GetConfigReply reply = sendOAPacket(connection, new GetConfigRequest(), GetConfigReply.class);
        return reply.getConfig();
    }

    private AuthRequest createSessionRequest(LoginToken loginToken) {
        return new AuthRequest(loginToken);
    }
}
