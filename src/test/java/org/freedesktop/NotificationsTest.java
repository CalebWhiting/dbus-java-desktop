package org.freedesktop;

import lombok.extern.slf4j.Slf4j;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.types.UInt32;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NotificationsTest
{

    private static final String APP_NAME = "org.freedesktop.TestNotifications";

    private DBusConnection connection;

    private Notifications notifications;

    private List<String> caps;

    @Before
    public void openConnection() throws DBusException
    {
        this.connection = DBusConnectionBuilder.forSessionBus().build();
        this.notifications = this.connection.getRemoteObject(Notifications.NAME,
                Notifications.OBJECT_PATH,
                Notifications.class);
        this.caps = Arrays.asList(this.notifications.getCapabilities());
        this.caps.forEach(cap -> log.info("Server capability: {}", cap));
    }

    @After
    public void closeConnection()
    {
        this.connection.disconnect();
    }

    @Test
    public void testNotifications() throws DBusException, InterruptedException
    {
        Notifications.Hints hints = new Notifications.Hints();

        StringBuilder summary = new StringBuilder();
        StringBuilder body = new StringBuilder();
        Notifications.Actions actions = new Notifications.Actions();

        hints.set(Notifications.HintKey.X_KDE_DISPLAY_APPNAME, "(KDE) Notification Test");
        hints.set(Notifications.HintKey.X_KDE_ORIGIN_NAME, "Origin Name");

        if (caps.contains(Notifications.Capabilities.X_KDE_INLINE_REPLY))
        {
            actions.addAction("inline-reply", "Inline Reply");
        }

        if (caps.contains(Notifications.Capabilities.BODY))
        {
            summary.append("This is a summary");
            body.append("This is the body of the notification.");

            if (caps.contains(Notifications.Capabilities.BODY_MARKUP))
            {
                body.append("\nThe server <b>does</b> support markup.");
            } else
            {
                body.append("\nThe server doesn't support markup.");
            }

            if (caps.contains(Notifications.Capabilities.ACTIONS))
            {
                hints.set(Notifications.HintKey.RESIDENT, true);
                actions.addAction("verify", "Verify");

                if (caps.contains(Notifications.Capabilities.ACTION_ICONS))
                {
                    hints.set(Notifications.HintKey.ACTION_ICONS, true);
                    body.append("\nThe server supports action icons.");
                } else
                {
                    body.append("\nThe server doesn't support action icons.");
                }
            } else
            {
                hints.set(Notifications.HintKey.RESIDENT, false);
                body.append("\nThe server doesn't support actions. The test will be incomplete.");
            }
        } else
        {
            summary.append("The server lacks basic features");
        }

        UInt32 id = notifications.open(APP_NAME,
                new UInt32(0),
                "debug-run",
                summary.toString(),
                body.toString(),
                actions,
                hints,
                caps.contains(Notifications.Capabilities.ACTIONS) ? 0 : 5000);

        CountDownLatch latch = new CountDownLatch(1);
        connection.addSigHandler(Notifications.NotificationClosed.class, notifications, evt -> {
            if (Objects.equals(evt.getId(), id))
            {
                log.info("Notification closed");
                latch.countDown();
            }
        });
        connection.addSigHandler(Notifications.ActivationToken.class, notifications, evt -> {
            if (Objects.equals(evt.getId(), id))
            {
                log.info("Activation: {}", evt.getToken());
            }
        });
        connection.addSigHandler(Notifications.ActionInvoked.class, notifications, evt -> {
            if (Objects.equals(evt.getId(), id))
            {
                if (evt.getAction().equals("verify"))
                {
                    log.info("Reply action invoked");
                    notifications.close(id);
                }
            }
        });
        connection.addSigHandler(Notifications.KdeNotificationReplied.class, notifications, evt -> {
            if (Objects.equals(evt.getId(), id))
            {
                log.info("Replied with a message! How fancy!");
                log.info("[{}]", evt.getMessage());
                notifications.close(id);
            }
        });
        assert latch.await(30, TimeUnit.SECONDS) : "Received no close signal";
    }

    @Test
    public void testInhibit() throws DBusException, InterruptedException
    {
        UInt32 cookie = notifications.inhibit(getClass().getName(), "For testing", new Notifications.Hints());

        UInt32 id = notifications.open(getClass().getName(),
                new UInt32(0),
                "error",
                "This should be inhibited",
                "This should be inhibited",
                new Notifications.Actions(),
                new Notifications.Hints(),
                1000);

        log.info("Inhibited Notification ID: {}", id);

        CountDownLatch latch = new CountDownLatch(1);

        connection.addSigHandler(Notifications.NotificationClosed.class, notifications, evt -> {
            if (evt.getId().equals(id))
            {
                log.info("Notification closed: {}", evt.getId());
                latch.countDown();
            }
        });

        assert !latch.await(3, TimeUnit.SECONDS) : "Received close signal";

        notifications.close(id);
        assert latch.await(1, TimeUnit.SECONDS) : "Received no close signal";

        // give it time to close before releasing the inhibitor
        Thread.sleep(500);

        notifications.release(cookie);
    }

}
