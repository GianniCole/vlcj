package uk.co.caprica.vlcj.media;

import com.sun.jna.CallbackThreadInitializer;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import uk.co.caprica.vlcj.binding.internal.libvlc_callback_t;
import uk.co.caprica.vlcj.binding.internal.libvlc_event_e;
import uk.co.caprica.vlcj.binding.internal.libvlc_event_manager_t;
import uk.co.caprica.vlcj.binding.internal.libvlc_event_t;
import uk.co.caprica.vlcj.player.events.media.MediaEvent;
import uk.co.caprica.vlcj.player.events.media.MediaEventFactory;
import uk.co.caprica.vlcj.player.events.media.MediaEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventService extends BaseService {

    /**
     * Collection of media event listeners.
     * <p>
     * A {@link CopyOnWriteArrayList} is used defensively so as not to interfere with the processing of any existing
     * events that may be being generated by the native callback in the unlikely case that a listeners is being added or
     * removed.
     */
    private final List<MediaEventListener> eventListenerList = new CopyOnWriteArrayList<MediaEventListener>();

    private final MediaEventCallback callback = new MediaEventCallback();

    EventService(Media media) {
        super(media);

        registerNativeEventListener();
    }

    /**
     * Add a component to be notified of media events.
     *
     * @param listener component to notify
     */
    public void addMediaEventListener(MediaEventListener listener) {
        eventListenerList.add(listener);
    }

    /**
     * Remove a component that was previously interested in notifications of media events.
     *
     * @param listener component to stop notifying
     */
    public void removeMediaEventListener(MediaEventListener listener) {
        eventListenerList.remove(listener);
    }

    /**
     * Register a call-back to receive media native events.
     */
    private void registerNativeEventListener() {
        libvlc_event_manager_t mediaEventManager = libvlc.libvlc_media_event_manager(mediaInstance);
        for (libvlc_event_e event : libvlc_event_e.values()) {
            if (event.intValue() >= libvlc_event_e.libvlc_MediaMetaChanged.intValue() && event.intValue() <= libvlc_event_e.libvlc_MediaThumbnailGenerated.intValue()) {
                libvlc.libvlc_event_attach(mediaEventManager, event.intValue(), callback, null);
            }
        }
    }

    /**
     * De-register the call-back used to receive native media events.
     */
    private void deregisterNativeEventListener() {
        libvlc_event_manager_t mediaEventManager = libvlc.libvlc_media_event_manager(mediaInstance);
        for (libvlc_event_e event : libvlc_event_e.values()) {
            if (event.intValue() >= libvlc_event_e.libvlc_MediaMetaChanged.intValue() && event.intValue() <= libvlc_event_e.libvlc_MediaThumbnailGenerated.intValue()) {
                libvlc.libvlc_event_detach(mediaEventManager, event.intValue(), callback, null);
            }
        }
    }

    /**
     * Raise a new event (dispatch it to listeners).
     * <p>
     * Events are processed on the <em>native</em> callback thread, so must execute quickly and certainly must never
     * block.
     * <p>
     * It is also generally <em>forbidden</em> for an event handler to call back into LibVLC.
     *
     * @param mediaEvent event to raise, may be <code>null</code> and if so will be ignored
     */
    private void raiseEvent(MediaEvent mediaEvent) {
        if (mediaEvent != null) {
            for (MediaEventListener listener : eventListenerList) {
                mediaEvent.notify(listener);
            }
        }
    }

    private class MediaEventCallback implements libvlc_callback_t {

        private MediaEventCallback() {
            Native.setCallbackThreadInitializer(this, new CallbackThreadInitializer(true, false, "media-events"));
        }

        @Override
        public void callback(libvlc_event_t event, Pointer userData) {
            raiseEvent(MediaEventFactory.createEvent(media, event));
        }
    }

    @Override
    protected void release() {
        eventListenerList.clear();

        deregisterNativeEventListener();
    }

}
