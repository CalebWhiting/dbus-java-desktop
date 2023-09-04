package org.freedesktop;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.freedesktop.dbus.Struct;
import org.freedesktop.dbus.Tuple;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusMemberName;
import org.freedesktop.dbus.annotations.Position;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.interfaces.DBusSerializable;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.Variant;

import java.util.*;

/**
 * <a href="https://specifications.freedesktop.org/notification-spec/latest/">...</a>
 */
@DBusInterfaceName("org.freedesktop.Notifications")
interface Notifications extends DBusInterface
{

    String NAME = "org.freedesktop.Notifications";
    String OBJECT_PATH = "/org/freedesktop/Notifications";

    @DBusMemberName("Notify")
    UInt32 open(
            String app_name,
            UInt32 replaces,
            String app_icon,
            String summary,
            String body,
            Actions actions,
            Hints hints,
            int timeout);

    @DBusMemberName("CloseNotification")
    void close(UInt32 id);

    @DBusMemberName("GetCapabilities")
    String[] getCapabilities();

    @DBusMemberName("GetServerInformation")
    ServerInformation getServerInformation();

    @DBusMemberName("Inhibit")
    UInt32 inhibit(String desktop_entry, String reason, Hints hints);

    @DBusMemberName("UnInhibit")
    void release(UInt32 cookie);

    @Getter
    @AllArgsConstructor
    enum Urgency
    {
        LOW((byte) 0),
        NORMAL((byte) 1),
        CRITICAL((byte) 2);

        private final byte tag;

    }

    interface Capabilities
    {
        /**
         * Supports using icons instead of text for displaying actions. Using icons for actions must be enabled on a per-notification basis using the "action-icons" hint.
         */
        String ACTION_ICONS = "action-icons";

        /**
         * The server will provide the specified actions to the user. Even if this cap is missing, actions may still be specified by the client, however the server is free to ignore them.
         */
        String ACTIONS = "actions";

        /**
         * Supports body text. Some implementations may only show the summary (for instance, onscreen displays, marquee/scrollers)
         */
        String BODY = "body";

        /**
         * The server supports hyperlinks in the notifications.
         */
        String BODY_HYPERLINKS = "body-hyperlinks";

        /**
         * The server supports images in the notifications.
         */
        String BODY_IMAGES = "body-images";

        /**
         * Supports markup in the body text. If marked up text is sent to a server that does not give this cap, the markup will show through as regular text so must be stripped clientside.
         */
        String BODY_MARKUP = "body-markup";

        /**
         * The server will render an animation of all the frames in a given image array. The client may still specify multiple frames even if this cap and/or "icon-static" is missing, however the server is free to ignore them and use only the primary frame.
         */
        String ICON_MULTI = "icon-multi";

        /**
         * Supports display of exactly 1 frame of any given image array. This value is mutually exclusive with "icon-multi", it is a protocol error for the server to specify both.
         */
        String ICON_STATIC = "icon-static";

        /**
         * The server supports persistence of notifications. Notifications will be retained until they are acknowledged or removed by the user or recalled by the sender. The presence of this capability allows clients to depend on the server to ensure a notification is seen and eliminate the need for the client to display a reminding function (such as a status icon) of its own.
         */
        String PERSISTENCE = "persistence";

        /**
         * The server supports sounds on notifications. If returned, the server must support the "sound-file" and "suppress-sound" hints.
         */
        String SOUND = "sound";
        /**
         * Undocumented
         */
        String X_KDE_URLS = "x-kde-urls";

        /**
         * Undocumented
         */
        String X_KDE_ORIGIN_NAME = "x-kde-origin-name";

        /**
         * Undocumented
         */
        String X_KDE_DISPLAY_APPNAME = "x-kde-display-appname";

        /**
         * Actions with the identifier "inline-reply" display input fields which trigger the InlineReply signal.
         * <p/>
         * As of the time of writing, this appears to be exclusive to KDE due to reluctance to changing the spec.
         * <p/>
         * GNOME won't merge the feature unless it's part of the spec.
         * <a href="https://gitlab.gnome.org/GNOME/libnotify/-/merge_requests/12">GNOME merge request</a>
         * <p/>
         * Discussions have recently resurfaced about changing the spec.
         * However, it seems it might not be codified in this form.
         * <a href="https://github.com/flatpak/xdg-desktop-portal/issues/983">Notification Portal V2 Discussion</a>
         */
        String X_KDE_INLINE_REPLY = "inline-reply";

    }

    @Getter
    @ToString
    class NotificationClosed extends DBusSignal
    {
        private final String objectPath;

        private final UInt32 id;

        private final UInt32 reason;

        public NotificationClosed(String _objectPath, UInt32 id, UInt32 reason) throws DBusException
        {
            super(_objectPath, id, reason);
            this.objectPath = _objectPath;
            this.id = id;
            this.reason = reason;
        }

    }

    @Getter
    @ToString
    class ActionInvoked extends DBusSignal
    {
        private final String objectPath;

        private final UInt32 id;

        private final String action;

        public ActionInvoked(String _objectPath, UInt32 id, String action) throws DBusException
        {
            super(_objectPath, id, action);
            this.objectPath = _objectPath;
            this.id = id;
            this.action = action;
        }

    }

    @Getter
    @ToString
    class ActivationToken extends DBusSignal
    {
        private final String objectPath;

        private final UInt32 id;

        private final String token;

        public ActivationToken(String _objectPath, UInt32 id, String token) throws DBusException
        {
            super(_objectPath, id, token);
            this.objectPath = _objectPath;
            this.id = id;
            this.token = token;
        }

    }

    /**
     * @see Capabilities#X_KDE_INLINE_REPLY
     */
    @Getter
    @ToString
    @DBusMemberName("NotificationReplied")
    class KdeNotificationReplied extends DBusSignal
    {
        private final String objectPath;

        private final UInt32 id;

        private final String message;

        public KdeNotificationReplied(String _objectPath, UInt32 id, String message) throws DBusException
        {
            super(_objectPath, id, message);
            this.objectPath = _objectPath;
            this.id = id;
            this.message = message;
        }

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    class Action
    {
        private String identifier;

        private String text;
    }

    @Getter
    @NoArgsConstructor
    class Actions implements DBusSerializable
    {
        private final List<Action> actions = new LinkedList<>();

        public Actions addAction(Action action)
        {
            this.actions.add(action);
            return this;
        }

        public Actions addAction(String identifier, String text)
        {
            return addAction(new Action(identifier, text));
        }

        @Override
        public Object[] serialize()
        {
            int entries = this.actions.size();
            String[] serializable = new String[entries * 2];
            for (int i = 0; i < actions.size(); i++)
            {
                Action action = actions.get(i);
                serializable[(i * 2)] = action.getIdentifier();
                serializable[(i * 2) + 1] = action.getText();
            }
            return new Object[]{ serializable };
        }

        public void deserialize(String[] args)
        {
            if (args.length % 2 != 0)
            {
                throw new IllegalArgumentException("args must be an array of string pairs");
            }
            for (int i = 0; i < args.length - 1; i += 2)
            {
                addAction(args[i], args[i + 1]);
            }
        }

    }

    @Getter
    @AllArgsConstructor
    class ServerInformation extends Tuple
    {

        @Position(0)
        private final String name;

        @Position(1)
        private final String vendor;

        @Position(2)
        private final String version;

        @Position(3)
        private final String specVersion;

    }

    class Hints implements DBusSerializable
    {

        private final Map<String, Variant<?>> hints;

        public Hints()
        {
            this.hints = new HashMap<>();
        }

        public <T> void set(HintKey<T> key, T value)
        {
            this.hints.put(key.getKey(), new Variant<>(value, key.type));
        }

        public <T> T get(HintKey<T> key)
        {
            Variant<?> variant = this.hints.get(key.getKey());
            if (variant == null)
            {
                return null;
            }
            return key.type.cast(variant.getValue());
        }

        public Set<String> keys()
        {
            return this.hints.keySet();
        }

        public void setAll(Hints other)
        {
            this.hints.putAll(other.hints);
        }

        @Override
        public Object[] serialize()
        {
            return new Object[]{ hints };
        }

        public void deserialize(Map<String, Variant<?>> serializable)
        {
            this.hints.putAll(serializable);
        }

    }

    @Getter
    @AllArgsConstructor
    class HintKey<T>
    {
        /**
         * When set, a server that has the "action-icons" capability will attempt to interpret any action identifier as a named icon. The localized display name will be used to annotate the icon for accessibility purposes. The icon name should be compliant with the Freedesktop.org Icon Naming Specification.
         */
        public static final HintKey<Boolean> ACTION_ICONS = new HintKey<>("action-icons", boolean.class, 1.2);

        /**
         * The type of notification this is
         */
        public static final HintKey<String> CATEGORY = new HintKey<>("category", String.class, 1.0);

        /**
         * This specifies the name of the desktop filename representing the calling program. This should be the same as the prefix used for the application's .desktop file. An example would be "rhythmbox" from "rhythmbox.desktop". This can be used by the daemon to retrieve the correct icon for the application, for logging purposes, etc
         */
        public static final HintKey<String> DESKTOP_ENTRY = new HintKey<>("desktop-entry", String.class, 1.0);

        /**
         * This is a raw data image format which describes the width, height, rowstride, has alpha, bits per sample, channels and image data respectively.
         */
        public static final HintKey<RawImage> IMAGE_DATA = new HintKey<>("image-data", RawImage.class, 1.2);

        /**
         * Alternative way to define the notification image. See Icons and Images.
         */
        public static final HintKey<String> IMAGE_PATH = new HintKey<>("image-path", String.class, 1.2);

        /**
         * When set the server will not automatically remove the notification when an action has been invoked. The notification will remain resident in the server until it is explicitly removed by the user or by the sender. This hint is likely only useful when the server has the "persistence" capability.
         */
        public static final HintKey<Boolean> RESIDENT = new HintKey<>("resident", boolean.class, 1.2);

        /**
         * The path to a sound file to play when the notification pops up
         */
        public static final HintKey<String> SOUND_FILE = new HintKey<>("sound-file", String.class, 1.0);

        /**
         * A themeable named sound from the freedesktop.org sound naming specification to play when the notification pops up. Similar to icon-name, only for sounds. An example would be "message-new-instant"
         */
        public static final HintKey<String> SOUND_NAME = new HintKey<>("sound-name", String.class, 1.0);

        /**
         * Causes the server to suppress playing any sounds, if it has that ability. This is usually set when the client itself is going to play its own sound
         */
        public static final HintKey<Boolean> SUPPRESS_SOUND = new HintKey<>("suppress-sound", boolean.class, 1.0);

        /**
         * When set the server will treat the notification as transient and by-pass the server's persistence capability, if it should exist.
         */
        public static final HintKey<Boolean> TRANSIENT = new HintKey<>("transient", boolean.class, 1.2);

        /**
         * Specifies the X location on the screen that the notification should point to. The "y" hint must also be specified
         */
        public static final HintKey<Long> X = new HintKey<>("x", long.class, 1.0);

        /**
         * Specifies the Y location on the screen that the notification should point to. The "x" hint must also be specified
         */
        public static final HintKey<Long> Y = new HintKey<>("y", long.class, 1.0);

        /**
         * The urgency level
         */
        public static final HintKey<Urgency> URGENCY = new HintKey<>("urgency", Urgency.class, 1.0);

        /**
         * Undocumented
         */
        public static final HintKey<String[]> X_KDE_URLS = new HintKey<>(Capabilities.X_KDE_URLS, String[].class, -1);

        /**
         * Undocumented
         */
        public static final HintKey<String> X_KDE_ORIGIN_NAME = new HintKey<>(Capabilities.X_KDE_ORIGIN_NAME, String.class, -1);

        /**
         * Undocumented
         */
        public static final HintKey<String> X_KDE_DISPLAY_APPNAME = new HintKey<>(Capabilities.X_KDE_DISPLAY_APPNAME, String.class, -1);

        private final String key;

        private final Class<T> type;

        private final double version;

    }

    class RawImage extends Struct
    {

        private final int width;

        private final int height;

        private final int stride;

        private final boolean hasAlpha;

        private final int bitsPerSamble;

        private final int channels;

        private final long[] data;

        public RawImage(
                int width, int height, int stride, boolean hasAlpha, int bitsPerSamble, int channels, long[] data)
        {
            this.width = width;
            this.height = height;
            this.stride = stride;
            this.hasAlpha = hasAlpha;
            this.bitsPerSamble = bitsPerSamble;
            this.channels = channels;
            this.data = data;
        }

    }


}
