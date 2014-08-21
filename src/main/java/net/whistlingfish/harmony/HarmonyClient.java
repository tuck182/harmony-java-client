package net.whistlingfish.harmony;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;

import net.whistlingfish.harmony.config.Activity;
import net.whistlingfish.harmony.config.HarmonyConfig;
import net.whistlingfish.harmony.protocol.AuthReply;
import net.whistlingfish.harmony.protocol.AuthRequest;
import net.whistlingfish.harmony.protocol.AuthService;
import net.whistlingfish.harmony.protocol.EmptyIncrementedIdReplyFilter;
import net.whistlingfish.harmony.protocol.GetConfig.GetConfigReply;
import net.whistlingfish.harmony.protocol.GetConfig.GetConfigRequest;
import net.whistlingfish.harmony.protocol.GetCurrentActivity.GetCurrentActivityReply;
import net.whistlingfish.harmony.protocol.GetCurrentActivity.GetCurrentActivityRequest;
import net.whistlingfish.harmony.protocol.HoldAction.HoldActionRequest;
import net.whistlingfish.harmony.protocol.LoginToken;
import net.whistlingfish.harmony.protocol.OA;
import net.whistlingfish.harmony.protocol.OAReplyFilter;
import net.whistlingfish.harmony.protocol.StartActivity.StartActivityReply;
import net.whistlingfish.harmony.protocol.StartActivity.StartActivityRequest;

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

import static net.whistlingfish.harmony.protocol.HoldAction.HoldStatus.PRESS;
import static net.whistlingfish.harmony.protocol.HoldAction.HoldStatus.RELEASE;

public class HarmonyClient {
    private static Logger logger = LoggerFactory.getLogger(HarmonyClient.class);

    private static final int DEFAULT_PORT = 5222;
    private static final String DEFAULT_XMPP_USER = "guest@connect.logitech.com/gatorade.";
    private static final String DEFAULT_XMPP_PASSWORD = "gatorade.";

    private XMPPTCPConnection connection;

    @Inject
    private AuthService authService;

    private HarmonyConfig config;

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
            authConnection.login(DEFAULT_XMPP_USER, DEFAULT_XMPP_PASSWORD, "auth");
            authConnection.setFromMode(FromMode.USER);

            AuthRequest sessionRequest = createSessionRequest(loginToken);
            AuthReply oaResponse = sendOAPacket(authConnection, sessionRequest, AuthReply.class);

            logger.debug("Got session response: {}", oaResponse);
            authConnection.disconnect();

            connection = new XMPPTCPConnection(connectionConfig);
            addPacketLogging(connection, "main");
            connection.connect();
            connection.login(oaResponse.getUsername(), oaResponse.getPassword(), "main");
            connection.setFromMode(FromMode.USER);

        } catch (XMPPException | SmackException | IOException e) {
            throw new RuntimeException("Failed communicating with Harmony Hub", e);
        }
    }

    private Packet sendOAPacket(XMPPTCPConnection authConnection, OA packet) {
        PacketCollector collector = authConnection.createPacketCollector(new EmptyIncrementedIdReplyFilter(packet,
                authConnection));
        try {
            authConnection.sendPacket(packet);
            // Packet reply = collector.nextResultOrThrow();
            Packet reply = collector.nextResultBlockForever();
            return reply;
        } catch (/* XMPPException | */SmackException e) {
            throw new RuntimeException("Failed communicating with Harmony Hub", e);
        } finally {
            collector.cancel();
        }
    }

    private <R extends OA> R sendOAPacket(XMPPTCPConnection authConnection, OA packet, Class<R> replyClass) {
        PacketCollector collector = authConnection.createPacketCollector(new OAReplyFilter(packet, authConnection));
        try {
            authConnection.sendPacket(packet);
            Packet reply = collector.nextResultOrThrow();
            // Packet reply = collector.nextResultBlockForever();
            return replyClass.cast(reply);
        } catch (XMPPException | SmackException e) {
            throw new RuntimeException("Failed communicating with Harmony Hub", e);
        } finally {
            collector.cancel();
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

    public HarmonyConfig getConfig() {
        if (config == null) {
            config = HarmonyConfig.parse(sendOAPacket(connection, new GetConfigRequest(), GetConfigReply.class)
                    .getConfig());
        }
        return config;
    }

    private AuthRequest createSessionRequest(LoginToken loginToken) {
        return new AuthRequest(loginToken);
    }

    public void pressButton(String deviceId, String button) {
        sendOAPacket(connection, new HoldActionRequest(deviceId, button, PRESS));
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        sendOAPacket(connection, new HoldActionRequest(deviceId, button, RELEASE));
    }

    public Map<String, String> getDeviceLabels() {
        return getConfig().getDeviceLabels();
    }

    public Activity getCurrentActivity() {
        GetCurrentActivityReply reply = sendOAPacket(connection, new GetCurrentActivityRequest(), GetCurrentActivityReply.class);
        HarmonyConfig config = getConfig();
        return config.getActivityById(reply.getResult());
    }

    public void startActivity(int parseInt) {
        sendOAPacket(connection, new StartActivityRequest(), StartActivityReply.class);
    }

    public void startActivityByName(String label) {
        startActivity(getConfig().getActivityByName(label).getId());
    }
}
