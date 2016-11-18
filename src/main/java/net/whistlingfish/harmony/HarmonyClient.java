package net.whistlingfish.harmony;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import net.whistlingfish.harmony.config.Activity;
import net.whistlingfish.harmony.config.Device;
import net.whistlingfish.harmony.config.HarmonyConfig;
import net.whistlingfish.harmony.protocol.EmptyIncrementedIdReplyFilter;
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
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection.FromMode;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

import static java.lang.String.format;
import static net.whistlingfish.harmony.protocol.MessageHoldAction.HoldStatus.PRESS;
import static net.whistlingfish.harmony.protocol.MessageHoldAction.HoldStatus.RELEASE;

public class HarmonyClient {
    private static final Logger logger = LoggerFactory.getLogger(HarmonyClient.class);

    public static final int DEFAULT_REPLY_TIMEOUT = 30_000;
    public static final int START_ACTIVITY_REPLY_TIMEOUT = 30_000;

    private static final int DEFAULT_PORT = 5222;
    private static final String DEFAULT_XMPP_USER = "guest@connect.logitech.com/gatorade.";
    private static final String DEFAULT_XMPP_PASSWORD = "gatorade.";

    private XMPPTCPConnection connection;
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

    public void disconnect(){
    	if(connection != null){
	    	try {
				connection.disconnect();
			} catch (NotConnectedException ignored) {
				logger.debug("Connection is already closed.");
			}
    	}
    	if (heartbeat != null) {
            heartbeat.cancel(false);
        }
    }

    // This method is for backwards compatibility
    public void connect(String host, String username, String password) {
    	this.connect(host);
    }
    
    // No need for username password with pair method
    public void connect(String host) {
        ConnectionConfiguration connectionConfig = createConnectionConfig(host, DEFAULT_PORT);
        XMPPTCPConnection authConnection = new XMPPTCPConnection(connectionConfig);
        try {
            addPacketLogging(authConnection, "auth");

            // Pair with the local hub and get the token
            authConnection.connect();
            authConnection.login(DEFAULT_XMPP_USER, DEFAULT_XMPP_PASSWORD, "auth");
            authConnection.setFromMode(FromMode.USER);

            AuthRequest sessionRequest = createPairSessionRequest();
            AuthReply oaResponse = sendOAPacket(authConnection, sessionRequest, AuthReply.class);

            authConnection.disconnect();

            connection = new XMPPTCPConnection(connectionConfig);
            addPacketLogging(connection, "main");
            connection.connect();
            connection.login(oaResponse.getUsername(), oaResponse.getPassword(), "main");
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
				public void authenticated(XMPPConnection connection) {
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
                    	if(connection.isConnected()){
                    		sendPing();
                    	}
                    } catch (Exception e) {
                        logger.warn("Send heartbeat failed", e);
                    }
                }
            }, 30, 30, TimeUnit.SECONDS);

            monitorActivityChanges();
            getCurrentActivity();

        } catch (XMPPException | SmackException | IOException e) {
            throw new RuntimeException("Failed communicating with Harmony Hub", e);
        }
    }

    private void monitorActivityChanges() {
        connection.addPacketListener(new PacketListener() {
            @Override
            public void processPacket(Packet packet) throws NotConnectedException {
                updateCurrentActivity(getCurrentActivity());
            }
        }, new PacketFilter() {
            @Override
            public boolean accept(Packet packet) {
                PacketExtension event = packet.getExtension("event", "connect.logitech.com");
                if (event == null)
                    return false;
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

    private Packet sendOAPacket(XMPPTCPConnection authConnection, OAPacket packet) {
        return sendOAPacket(authConnection, packet, DEFAULT_REPLY_TIMEOUT);
    }

    private Packet sendOAPacket(XMPPTCPConnection authConnection, OAPacket packet, long replyTimeout) {
        PacketCollector collector = authConnection.createPacketCollector(new EmptyIncrementedIdReplyFilter(packet,
                authConnection));
        messageLock.lock();
        try {
            authConnection.sendPacket(packet);
            return getNextPacketSkipContinues(collector, replyTimeout);
        } catch (SmackException | XMPPErrorException e) {
            throw new RuntimeException("Failed communicating with Harmony Hub", e);
        } finally {
            messageLock.unlock();
            collector.cancel();
        }
    }

    private <R extends OAPacket> R sendOAPacket(XMPPTCPConnection authConnection, OAPacket packet, Class<R> replyClass) {
        return sendOAPacket(authConnection, packet, replyClass, DEFAULT_REPLY_TIMEOUT);
    }

    private <R extends OAPacket> R sendOAPacket(XMPPTCPConnection authConnection, OAPacket packet, Class<R> replyClass,
            long replyTimeout) {
        PacketCollector collector = authConnection.createPacketCollector(new OAReplyFilter(packet, authConnection));
        messageLock.lock();
        try {
            authConnection.sendPacket(packet);
            return replyClass.cast(getNextPacketSkipContinues(collector, replyTimeout));
        } catch (SmackException | XMPPErrorException e) {
            throw new RuntimeException("Failed communicating with Harmony Hub", e);
        } finally {
            messageLock.unlock();
            collector.cancel();
        }
    }

    private Packet getNextPacketSkipContinues(PacketCollector collector, long replyTimeout) throws NoResponseException,
            XMPPErrorException {
        while (true) {
            Packet reply = collector.nextResult(replyTimeout);
            if (reply == null) {
                throw new NoResponseException();
            }
            if (reply instanceof OAPacket && ((OAPacket) reply).isContinuePacket()) {
                continue;
            }
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
        authConnection.addPacketSendingListener(new PacketListener() {
            @Override
            public void processPacket(Packet packet) {
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

    private AuthRequest createPairSessionRequest() {
        return new AuthRequest();
    }

    public void sendPing() {
        sendOAPacket(connection, new PingRequest(), PingReply.class);
    }

    public void pressButton(int deviceId, String button) {
        sendOAPacket(connection, new HoldActionRequest(deviceId, button, PRESS));
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        sendOAPacket(connection, new HoldActionRequest(deviceId, button, RELEASE));
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
        GetCurrentActivityReply reply = sendOAPacket(connection, new GetCurrentActivityRequest(),
                GetCurrentActivityReply.class);
        HarmonyConfig config = getConfig();
        return updateCurrentActivity(config.getActivityById(reply.getResult()));
    }

    public void startActivity(int activityId) {
        if (getConfig().getActivityById(activityId) == null) {
            throw new IllegalArgumentException(format("Unknown activity '%d'", activityId));
        }
        sendOAPacket(connection, new StartActivityRequest(activityId), StartActivityReply.class,
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
