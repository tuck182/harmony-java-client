package net.whistlingfish.harmony;

import static java.lang.String.format;
import static net.whistlingfish.harmony.protocol.MessageHoldAction.HoldStatus.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.StanzaCollector;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPConnection.FromMode;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Bind;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.sasl.SASLMechanism;
import org.jivesoftware.smack.sm.predicates.ForEveryStanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

import net.whistlingfish.harmony.config.Activity;
import net.whistlingfish.harmony.config.Device;
import net.whistlingfish.harmony.config.HarmonyConfig;
import net.whistlingfish.harmony.protocol.EmptyIncrementedIdReplyFilter;

import net.whistlingfish.harmony.protocol.HarmonyBindIQProvider;
import net.whistlingfish.harmony.protocol.HarmonyXMPPTCPConnection;
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
import net.whistlingfish.harmony.protocol.OAReplyFilter;
import net.whistlingfish.harmony.protocol.OAStanza;

public class HarmonyClient {

    private static final Logger logger = LoggerFactory.getLogger(HarmonyClient.class);

    public static final int DEFAULT_REPLY_TIMEOUT = 30_000;
    public static final int START_ACTIVITY_REPLY_TIMEOUT = 30_000;

    private static final int DEFAULT_PORT = 5222;
    private static final String DEFAULT_XMPP_USER = "guest@connect.logitech.com/gatorade.";
    private static final String DEFAULT_XMPP_PASSWORD = "gatorade.";

    private boolean smackConfigured;
    private HarmonyXMPPTCPConnection connection;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> heartbeat;

    /*
     * To prevent timeouts when different threads send a message and expect a response, create a lock that only allows a
     * single thread at a time to perform a send/receive action.
     */
    private ReentrantLock messageLock = new ReentrantLock();

    private HarmonyConfig config;

    private Activity currentActivity;

    private Set<ActivityChangeListener> activityChangeListeners = new HashSet<>();

    public static HarmonyClient getInstance() {
        Injector injector = Guice.createInjector(new HarmonyClientModule());
        return injector.getInstance(HarmonyClient.class);
    }

    private void configureSmack() {
        if (!smackConfigured) {
            ProviderManager.addIQProvider(Bind.ELEMENT, Bind.NAMESPACE, new HarmonyBindIQProvider());
            smackConfigured = true;
        }
    }

    public void disconnect() {
        if (connection != null) {
            connection.disconnect();
        }
        if (heartbeat != null) {
            heartbeat.cancel(false);
        }
    }

    public void connect(String host) {
        connect(host, null);
    }

    public void connect(String host, LoginToken loginToken) {
        configureSmack();

        XMPPTCPConnectionConfiguration connectionConfig = createConnectionConfig(host, DEFAULT_PORT);
        HarmonyXMPPTCPConnection authConnection = new HarmonyXMPPTCPConnection(connectionConfig);

        try {
            addPacketLogging(authConnection, "auth");

            // Pair with the local hub and get the token
            authConnection.connect();
            authConnection.login(DEFAULT_XMPP_USER, DEFAULT_XMPP_PASSWORD, Resourcepart.from("auth"));
            authConnection.setFromMode(FromMode.USER);

            AuthRequest sessionRequest = createSessionRequest(loginToken);
            AuthReply oaResponse = sendOAStanza(authConnection, sessionRequest, AuthReply.class);

            authConnection.disconnect();

            connection = new HarmonyXMPPTCPConnection(connectionConfig);
            addPacketLogging(connection, "main");
            connection.connect();
            connection.login(oaResponse.getUsername(), oaResponse.getPassword(), Resourcepart.from("main"));
            connection.setFromMode(FromMode.USER);
            connection.addConnectionListener(new ConnectionListener() {

                @Override
                public void reconnectionSuccessful() {
                    getCurrentActivity();
                }

                @Override
                public void connected(XMPPConnection connection) {
                }

                @Override
                public void authenticated(XMPPConnection connection, boolean resumed) {
                }

                @Override
                public void connectionClosed() {
                }

                @Override
                public void connectionClosedOnError(Exception e) {
                }

                @Override
                public void reconnectingIn(int seconds) {
                }

                @Override
                public void reconnectionFailed(Exception e) {
                }

            });

            heartbeat = scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (connection.isConnected()) {
                            sendPing();
                        }
                    } catch (Exception e) {
                        logger.warn("Send heartbeat failed", e);
                    }
                }
            }, 30, 30, TimeUnit.SECONDS);

            monitorActivityChanges();
            getCurrentActivity();

        } catch (InterruptedException | XMPPException | SmackException | IOException e) {
            throw new RuntimeException("Failed communicating with Harmony Hub", e);
        }
    }

    private void monitorActivityChanges() {
        connection.addSyncStanzaListener(new StanzaListener() {
            @Override
            public void processStanza(Stanza stanza) throws NotConnectedException {
                updateCurrentActivity(getCurrentActivity());
            }
        }, new StanzaFilter() {
            @Override
            public boolean accept(Stanza stanza) {
                ExtensionElement event = stanza.getExtension("event", "connect.logitech.com");
                if (event == null) {
                    return false;
                }
                return true;
            }
        });
    }

    private synchronized Activity updateCurrentActivity(Activity activity) {
        if (currentActivity != activity) {
            currentActivity = activity;
            for (ActivityChangeListener listener : activityChangeListeners) {
                logger.debug("listener[{}] notified: {}", listener, currentActivity);
                listener.activityStarted(currentActivity);
            }
        }
        return currentActivity;
    }

    public void addListener(HarmonyHubListener listener) {
        listener.addTo(this);
    }

    public synchronized void addListener(ActivityChangeListener listener) {
        logger.debug("listener[{}] added", listener);
        activityChangeListeners.add(listener);
        if (currentActivity != null) {
            logger.debug("listener[{}] notified: {}", listener, currentActivity);
            listener.activityStarted(currentActivity);
        }
    }

    public void removeListener(HarmonyHubListener listener) {
        listener.removeFrom(this);
    }

    public void removeListener(ActivityChangeListener activityChangeListener) {
        activityChangeListeners.remove(activityChangeListener);
    }

    private Stanza sendOAStanza(XMPPTCPConnection authConnection, OAStanza stanza) {
        return sendOAStanza(authConnection, stanza, DEFAULT_REPLY_TIMEOUT);
    }

    private Stanza sendOAStanza(XMPPTCPConnection authConnection, OAStanza stanza, long replyTimeout) {
        StanzaCollector collector = authConnection
                .createStanzaCollector(new EmptyIncrementedIdReplyFilter(stanza, authConnection));
        messageLock.lock();
        try {
            authConnection.sendStanza(stanza);
            return getNextStanzaSkipContinues(collector, replyTimeout, authConnection);
        } catch (InterruptedException | SmackException | XMPPErrorException e) {
            throw new RuntimeException("Failed communicating with Harmony Hub", e);
        } finally {
            messageLock.unlock();
            collector.cancel();
        }
    }

    private <R extends OAStanza> R sendOAStanza(XMPPTCPConnection authConnection, OAStanza stanza,
            Class<R> replyClass) {
        return sendOAStanza(authConnection, stanza, replyClass, DEFAULT_REPLY_TIMEOUT);
    }

    private <R extends OAStanza> R sendOAStanza(XMPPTCPConnection authConnection, OAStanza stanza, Class<R> replyClass,
            long replyTimeout) {
        StanzaCollector collector = authConnection.createStanzaCollector(new OAReplyFilter(stanza, authConnection));
        messageLock.lock();
        try {
            authConnection.sendStanza(stanza);
            return replyClass.cast(getNextStanzaSkipContinues(collector, replyTimeout, authConnection));
        } catch (InterruptedException | SmackException | XMPPErrorException e) {
            throw new RuntimeException("Failed communicating with Harmony Hub", e);
        } finally {
            messageLock.unlock();
            collector.cancel();
        }
    }

    private Stanza getNextStanzaSkipContinues(StanzaCollector collector, long replyTimeout,
            XMPPTCPConnection authConnection) throws InterruptedException, NoResponseException, XMPPErrorException {
        while (true) {
            Stanza reply = collector.nextResult(replyTimeout);
            if (reply == null) {
                throw NoResponseException.newWith(authConnection, collector);
            }
            if (reply instanceof OAStanza && ((OAStanza) reply).isContinuePacket()) {
                continue;
            }
            return reply;
        }
    }

    private void addPacketLogging(XMPPTCPConnection authConnection, final String prefix) {
        authConnection.addPacketSendingListener(new StanzaListener() {
            @Override
            public void processStanza(Stanza stanza) {
                logger.trace("{}>>> {}", prefix, stanza.toXML().toString().replaceAll("\n", ""));
            }
        }, ForEveryStanza.INSTANCE);
        authConnection.addSyncStanzaListener(new StanzaListener() {
            @Override
            public void processStanza(Stanza stanza) throws NotConnectedException, InterruptedException {
                logger.trace("<<<{} {}", prefix, stanza.toXML().toString().replaceAll("\n", ""));
            }
        }, ForEveryStanza.INSTANCE);
    }

    private XMPPTCPConnectionConfiguration createConnectionConfig(String host, int port) {
        try {
            return XMPPTCPConnectionConfiguration.builder().setHost(host).setPort(port).setXmppDomain(host)
                    .addEnabledSaslMechanism(SASLMechanism.PLAIN).build();
        } catch (XmppStringprepException e) {
            throw new RuntimeException(e);
        }
    }

    public HarmonyConfig getConfig() {
        if (config == null) {
            config = HarmonyConfig
                    .parse(sendOAStanza(connection, new GetConfigRequest(), GetConfigReply.class).getConfig());
        }
        return config;
    }

    private AuthRequest createPairSessionRequest() {
        return new AuthRequest();
    }

    public void sendPing() {
        sendOAStanza(connection, new PingRequest(), PingReply.class);
    }

    public void pressButton(int deviceId, String button) {
        sendOAStanza(connection, new HoldActionRequest(deviceId, button, PRESS));
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        sendOAStanza(connection, new HoldActionRequest(deviceId, button, RELEASE));
    }

    public void pressButton(String deviceName, String button) {
        Device device = getConfig().getDeviceByName(deviceName);
        if (device == null) {
            throw new IllegalArgumentException(format("Unknown device '%s'", deviceName));
        }
        pressButton(device.getId(), button);
    }

    public Map<Integer, String> getDeviceLabels() {
        return getConfig().getDeviceLabels();
    }

    public Activity getCurrentActivity() {
        GetCurrentActivityReply reply = sendOAStanza(connection, new GetCurrentActivityRequest(),
                GetCurrentActivityReply.class);
        HarmonyConfig config = getConfig();
        return updateCurrentActivity(config.getActivityById(reply.getResult()));
    }

    public void startActivity(int activityId) {
        if (getConfig().getActivityById(activityId) == null) {
            throw new IllegalArgumentException(format("Unknown activity '%d'", activityId));
        }
        sendOAStanza(connection, new StartActivityRequest(activityId), StartActivityReply.class,
                START_ACTIVITY_REPLY_TIMEOUT);
    }

    public void startActivityByName(String label) {
        Activity activity = getConfig().getActivityByName(label);
        if (activity == null) {
            throw new IllegalArgumentException(format("Unknown activity '%s'", label));
        }
        startActivity(activity.getId());
    }
}
