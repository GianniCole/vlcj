/*
 * This file is part of VLCJ.
 *
 * VLCJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VLCJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VLCJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2009-2019 Caprica Software Limited.
 */

package uk.co.caprica.vlcj.component;

import uk.co.caprica.vlcj.component.callback.CallbackImagePainter;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.medialist.MediaList;
import uk.co.caprica.vlcj.player.embedded.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.callback.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.fullscreen.FullScreenStrategy;
import uk.co.caprica.vlcj.player.list.MediaListPlayer;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class CallbackMediaListPlayerComponent extends CallbackMediaListPlayerComponentBase {

    /**
     * Media list player.
     */
    private final MediaListPlayer mediaListPlayer;

    /**
     * Media list.
     */
    private final MediaList mediaList;

    public CallbackMediaListPlayerComponent(MediaPlayerFactory mediaPlayerFactory, FullScreenStrategy fullScreenStrategy, InputEvents inputEvents, BufferFormatCallback bufferFormatCallback, boolean lockBuffers, Dimension size, CallbackImagePainter imagePainter, JComponent videoSurfaceComponent, RenderCallback renderCallback) {
        super(mediaPlayerFactory, fullScreenStrategy, inputEvents, bufferFormatCallback, lockBuffers, size, imagePainter, videoSurfaceComponent, renderCallback);

        this.mediaListPlayer = getMediaPlayerFactory().mediaPlayers().newMediaListPlayer();
        this.mediaListPlayer.mediaPlayer().setMediaPlayer(getMediaPlayer());
        this.mediaListPlayer.events().addMediaListPlayerEventListener(this);

        this.mediaList = getMediaPlayerFactory().media().newMediaList();
        this.mediaList.events().addMediaListEventListener(this);

        this.mediaListPlayer.list().setMediaList(this.mediaList);

        onAfterConstruct();
    }

    public CallbackMediaListPlayerComponent(MediaPlayerFactory mediaPlayerFactory, FullScreenStrategy fullScreenStrategy, InputEvents inputEvents, BufferFormatCallback bufferFormatCallback, boolean lockBuffers, Dimension size, CallbackImagePainter imagePainter) {
        this(mediaPlayerFactory, fullScreenStrategy, inputEvents, bufferFormatCallback, lockBuffers, size, imagePainter, null, null);
    }

    public CallbackMediaListPlayerComponent(MediaPlayerFactory mediaPlayerFactory, FullScreenStrategy fullScreenStrategy, InputEvents inputEvents, BufferFormatCallback bufferFormatCallback, boolean lockBuffers, JComponent videoSurfaceComponent, RenderCallback renderCallback) {
        this(mediaPlayerFactory, fullScreenStrategy, inputEvents, bufferFormatCallback, lockBuffers, null, null, videoSurfaceComponent, renderCallback);
    }

    public CallbackMediaListPlayerComponent(MediaPlayerSpecs.CallbackMediaPlayerSpec spec) {
        this(spec.factory, spec.fullScreenStrategy, spec.inputEvents, spec.bufferFormatCallback, spec.lockedBuffers, spec.size, spec.imagePainter, spec.videoSurfaceComponent, spec.renderCallback);
    }

    /**
     * Construct a media list player component.
     */
    public CallbackMediaListPlayerComponent() {
        this(null, null, null, null, true, null, null, null, null);
    }

    /**
     * Get the embedded media list player reference.
     * <p>
     * An application uses this handle to control the media player, add listeners and so on.
     *
     * @return media list player
     */
    public final MediaListPlayer getMediaListPlayer() {
        return mediaListPlayer;
    }

    /**
     * Get the embedded media list reference.
     *
     * @return media list
     */
    public final MediaList getMediaList() {
        return mediaList;
    }

    @Override
    protected final void onBeforeRelease() {
        mediaListPlayer.release();
        mediaList.release();
    }

}
