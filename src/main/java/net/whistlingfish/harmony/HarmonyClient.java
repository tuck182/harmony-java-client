package net.whistlingfish.harmony;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import net.whistlingfish.harmony.config.Activity;
import net.whistlingfish.harmony.config.HarmonyConfig;
import net.whistlingfish.harmony.protocol.AuthService;
import net.whistlingfish.harmony.protocol.EmptyIncrementedIdReplyFilter;
import net.whistlingfish.harmony.protocol.LoginToken;
import net.whistlingfish.harmony.protocol.MessageAuth.AuthReply;
import net.whistlingfish.harmony.protocol.MessageAuth.AuthRequest;
import net.whistlingfish.harmony.protocol.MessageGetConfig.GetConfigReply;
import net.whistlingfish.harmony.protocol.MessageGetConfig.GetConfigRequest;
import net.whistlingfish.harmony.protocol.MessageGetCurrentActivity.GetCurrentActivityReply;
import net.whistlingfish.harmony.protocol.MessageGetCurrentActivity.GetCurrentActivityRequest;
import net.whistlingfish.harmony.protocol.MessageHoldAction.HoldActionRequest;
import net.whistlingfish.harmony.protocol.MessagePing.PingReply;
import net.whistlingfish.harmony.protocol.MessagePing.PingRequest;
import net.whistlingfish.harmony.protocol.MessageStartActivity.StartActivityReply;
import net.whistlingfish.harmony.protocol.MessageStartActivity.StartActivityRequest;
import net.whistlingfish.harmony.protocol.OAPacket;
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

import com.google.inject.Guice;
import com.google.inject.Injector;

import static net.whistlingfish.harmony.protocol.MessageHoldAction.HoldStatus.PRESS;
import static net.whistlingfish.harmony.protocol.MessageHoldAction.HoldStatus.RELEASE;

public class HarmonyClient {
    private static Logger logger = LoggerFactory.getLogger(HarmonyClient.class);


    private static final int DEFAULT_PORT = 5222;
    private static final String DEFAULT_XMPP_USER = "guest@connect.logitech.com/gatorade.";
    private static final String DEFAULT_XMPP_PASSWORD = "gatorade.";

    private XMPPTCPConnection connection;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> heartbeat;

    @Inject
    private AuthService authService;

    private HarmonyConfig config;

    public HarmonyClient() {
        scheduler = Executors.newScheduledThreadPool(1);
    }

    public static HarmonyClient getInstance() {
        Injector injector = Guice.createInjector(new HarmonyClientModule());
        return injector.getInstance(HarmonyClient.class);
    }

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

            authConnection.disconnect();

            connection = new XMPPTCPConnection(connectionConfig);
            addPacketLogging(connection, "main");
            connection.connect();
            connection.login(oaResponse.getUsername(), oaResponse.getPassword(), "main");
            connection.setFromMode(FromMode.USER);

            heartbeat = scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!connection.isConnected()) {
                            if (heartbeat != null) {
                                heartbeat.cancel(false);
                            }
                            return;
                        }
                        sendPing();
                    } catch (Exception e) {
                        logger.warn("Send heartbeat failed", e);
                    }
                }
            }, 30, 30, TimeUnit.SECONDS);

        } catch (XMPPException | SmackException | IOException e) {
            throw new RuntimeException("Failed communicating with Harmony Hub", e);
        }
    }

    private Packet sendOAPacket(XMPPTCPConnection authConnection, OAPacket packet) {
        PacketCollector collector = authConnection.createPacketCollector(new EmptyIncrementedIdReplyFilter(packet,
                authConnection));
        try {
            authConnection.sendPacket(packet);
            return getNextPacketSkipContinues(collector);
        } catch (/* XMPPException | */SmackException e) {
            throw new RuntimeException("Failed communicating with Harmony Hub", e);
        } finally {
            collector.cancel();
        }
    }

    private <R extends OAPacket> R sendOAPacket(XMPPTCPConnection authConnection, OAPacket packet, Class<R> replyClass) {
        PacketCollector collector = authConnection.createPacketCollector(new OAReplyFilter(packet, authConnection));
        try {
            authConnection.sendPacket(packet);
            return replyClass.cast(getNextPacketSkipContinues(collector));
        } catch (SmackException e) {
            throw new RuntimeException("Failed communicating with Harmony Hub", e);
        } finally {
            collector.cancel();
        }
    }

    private Packet getNextPacketSkipContinues(PacketCollector collector) {
        while (true) {
            // Packet reply = collector.nextResultOrThrow();
            Packet reply = collector.nextResultBlockForever();
            if (!(reply instanceof OAPacket)) {
                continue;
            }
            OAPacket oaReply = (OAPacket) reply;
            if (!oaReply.isContinuePacket())
                return reply;
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
                logger.trace("{}>>> {}", prefix, packet.toXML().toString().replaceAll("\n", ""));
            }
        }, allPacketsFilter);
        authConnection.addPacketListener(new PacketListener() {
            @Override
            public void processPacket(Packet packet) throws NotConnectedException {
                logger.trace("<<<{} {}", prefix, packet.toXML().toString().replaceAll("\n", ""));
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

    public void sendPing() {
        sendOAPacket(connection, new PingRequest(), PingReply.class);
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
        GetCurrentActivityReply reply = sendOAPacket(connection, new GetCurrentActivityRequest(),
                GetCurrentActivityReply.class);
        HarmonyConfig config = getConfig();
        return config.getActivityById(reply.getResult());
    }

    public void startActivity(int activityId) {
        sendOAPacket(connection, new StartActivityRequest(activityId), StartActivityReply.class);
    }

    public void startActivityByName(String label) {
        startActivity(getConfig().getActivityByName(label).getId());
    }
}
