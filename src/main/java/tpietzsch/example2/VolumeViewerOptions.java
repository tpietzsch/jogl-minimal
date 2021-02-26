/*
 * #%L
 * BigDataViewer core classes with minimal dependencies
 * %%
 * Copyright (C) 2012 - 2015 BigDataViewer authors
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package tpietzsch.example2;

import bdv.TransformEventHandler3D;
import bdv.TransformEventHandlerFactory;
import bdv.viewer.ViewerOptions;
import bdv.viewer.animate.MessageOverlayAnimator;
import java.awt.event.KeyListener;
import org.scijava.ui.behaviour.KeyPressedManager;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

/**
 * Optional parameters for {@link VolumeViewerPanel}.
 *
 * @author Tobias Pietzsch
 */
public class VolumeViewerOptions
{
	public final Values values = new Values();

	/**
	 * Create default {@link VolumeViewerOptions}.
	 * @return default {@link VolumeViewerOptions}.
	 */
	public static VolumeViewerOptions options()
	{
		return new VolumeViewerOptions();
	}

	/**
	 * Set width of {@link VolumeViewerPanel} canvas.
	 */
	public VolumeViewerOptions width( final int w )
	{
		values.width = w;
		return this;
	}

	/**
	 * Set height of {@link VolumeViewerPanel} canvas.
	 */
	public VolumeViewerOptions height( final int h )
	{
		values.height = h;
		return this;
	}

	/**
	 * Sets the width of offscreen canvas.
	 *
	 * @param w
	 *            the width.
	 * @return this instance.
	 */
	public VolumeViewerOptions renderWidth( final int w )
	{
		values.renderWidth = w;
		return this;
	}

	/**
	 * Sets the height of offscreen canvas.
	 *
	 * @param h
	 *            the height.
	 * @return this instance.
	 */
	public VolumeViewerOptions renderHeight( final int h )
	{
		values.renderHeight = h;
		return this;
	}

	/**
	 * Set target per-frame time in milliseconds.
	 *
	 * @param t
	 *            Target rendering time in milliseconds.
	 */
	public VolumeViewerOptions maxRenderMillis( final int t )
	{
		values.maxRenderMillis = t;
		return this;
	}

	/**
	 * Sets the dither window width.
	 * E.g., {@code w=3} means {@code 3x3} dither window size.
	 *
	 * @param w
	 * 		dither window width.
	 * @return this instance.
	 */
	public VolumeViewerOptions ditherWidth( final int w )
	{
		values.ditherWidth = w;
		return this;
	}

	/**
	 * Sets the number of dither samples.
	 * Pixels are interpolated from this many nearest neighbors when dithering.
	 *
	 * @param n
	 * 		the number of dither samples.
	 *
	 * @return this instance.
	 */
	public VolumeViewerOptions numDitherSamples( final int n )
	{
		values.numDitherSamples = n;
		return this;
	}

	/**
	 * Sets the GPU cache tile size.
	 * (not padded)
	 *
	 * @param s
	 * 		the cache tile size.
	 * @return this instance
	 */
	public VolumeViewerOptions cacheBlockSize( final int[] s )
	{
		assert s.length == 3;
		values.cacheBlockSize[ 0 ] = s[ 0 ];
		values.cacheBlockSize[ 1 ] = s[ 1 ];
		values.cacheBlockSize[ 2 ] = s[ 2 ];
		return this;
	}

	/**
	 * Sets the GPU cache tile size.
	 * (not padded)
	 *
	 * @param s
	 * 		the cache tile size.
	 * @return this instance
	 */
	public VolumeViewerOptions cacheBlockSize( final int s )
	{
		values.cacheBlockSize[ 0 ] = s;
		values.cacheBlockSize[ 1 ] = s;
		values.cacheBlockSize[ 2 ] = s;
		return this;
	}

	/**
	 * Sets the max memory to use for the GPU cache texture in MB.
	 * The size of the GPU cache texture will match this as close as possible with the given tile size.
	 *
	 * @param s
	 * 		the GPU cache size in MB.
	 * @return this instance.
	 */
	public VolumeViewerOptions maxCacheSizeInMB( final int s )
	{
		values.maxCacheSizeInMB = s;
		return this;
	}

	/**
	 * Sets the distance from the camera to the z=0 plane. In units of screen pixel width.
	 *
	 * @param d
	 *            camera distance.
	 * @return this instance.
	 */
	public VolumeViewerOptions dCam( final double d )
	{
		values.dCam = d;
		return this;
	}

	/**
	 * Sets the visible distance from the z=0 plane towards the camera. In units of screen pixel width.
	 * (Should be smaller than camera distance.)
	 *
	 * @param d
	 *            visible distance from z=0 towards camera.
	 * @return this instance.
	 */
	public VolumeViewerOptions dClipNear( final double d )
	{
		values.dClipNear = d;
		return this;
	}

	/**
	 * Sets the visible distance from the z=0 plane away from the camera. In units of screen pixel width.
	 *
	 * @param d
	 *            visible distance from z=0 away from camera.
	 * @return this instance.
	 */
	public VolumeViewerOptions dClipFar( final double d )
	{
		values.dClipFar = d;
		return this;
	}

	public VolumeViewerOptions dClip( final double d )
	{
		values.dClipNear = d;
		values.dClipFar = d;
		return this;
	}

	public VolumeViewerOptions maxAllowedStepInVoxels( final double s )
	{
		values.maxAllowedStepInVoxels = s;
		return this;
	}

	/**
	 * Set how many source groups there are initially.
	 *
	 * @param n
	 *            How many source groups to create initially.
	 */
	public VolumeViewerOptions numSourceGroups( final int n )
	{
		values.numSourceGroups = n;
		return this;
	}

	public VolumeViewerOptions msgOverlay( final MessageOverlayAnimator o )
	{
		values.msgOverlay = o;
		return this;
	}

	public VolumeViewerOptions transformEventHandlerFactory( final TransformEventHandlerFactory f )
	{
		values.transformEventHandlerFactory = f;
		return this;
	}

	/**
	 * Set the {@link InputTriggerConfig} from which keyboard and mouse action mapping is loaded.
	 *
	 * @param c the {@link InputTriggerConfig} from which keyboard and mouse action mapping is loaded
	 */
	public VolumeViewerOptions inputTriggerConfig( final InputTriggerConfig c )
	{
		values.inputTriggerConfig = c;
		return this;
	}

	/**
	 * Set the {@link KeyPressedManager} to share
	 * {@link KeyListener#keyPressed(java.awt.event.KeyEvent)} events with other
	 * ui-behaviour windows.
	 * <p>
	 * The goal is to make keyboard click/drag behaviours work like mouse
	 * click/drag: When a behaviour is initiated with a key press, the window
	 * under the mouse receives focus and the behaviour is handled there.
	 * </p>
	 *
	 * @param manager
	 * @return
	 */
	public VolumeViewerOptions shareKeyPressedEvents( final KeyPressedManager manager )
	{
		values.keyPressedManager = manager;
		return this;
	}

	/**
	 * Read-only {@link VolumeViewerOptions} values.
	 */
	public static class Values
	{
		private int width = 800;
		private int height = 600;
		private int maxRenderMillis = 30;

		private int renderWidth = 512;
		private int renderHeight = 512;
		private int ditherWidth = 3;
		private int numDitherSamples = 8;
		private final int[] cacheBlockSize = new int[] { 32, 32, 32 };
		private int maxCacheSizeInMB = 300;
		private double dCam = 2000;
		private double dClipNear = 1000;
		private double dClipFar = 1000;
		private double maxAllowedStepInVoxels = 1.0;

		private int numSourceGroups = 10;
		private MessageOverlayAnimator msgOverlay = new MessageOverlayAnimator( 800 );
		private TransformEventHandlerFactory transformEventHandlerFactory = TransformEventHandler3D::new;
		private InputTriggerConfig inputTriggerConfig = null;
		private KeyPressedManager keyPressedManager = null;

		public VolumeViewerOptions optionsFromValues()
		{
			return new VolumeViewerOptions().
					width( width ).
					height( height ).
					renderWidth( renderWidth ).
					renderHeight( renderHeight ).
					maxRenderMillis( maxRenderMillis ).
					ditherWidth( ditherWidth ).
					numDitherSamples( numDitherSamples ).
					cacheBlockSize( cacheBlockSize ).
					maxCacheSizeInMB( maxCacheSizeInMB ).
					dCam( dCam ).
					dClipNear( dClipNear ).
					dClipFar( dClipFar ).
					maxAllowedStepInVoxels( maxAllowedStepInVoxels ).
					numSourceGroups( numSourceGroups ).
					msgOverlay( msgOverlay ).
					transformEventHandlerFactory( transformEventHandlerFactory ).
					inputTriggerConfig( inputTriggerConfig ).
					shareKeyPressedEvents( keyPressedManager );
		}

		public int getWidth()
		{
			return width;
		}

		public int getHeight()
		{
			return height;
		}

		public int getRenderWidth()
		{
			return renderWidth;
		}

		public int getRenderHeight()
		{
			return renderHeight;
		}

		public int getMaxRenderMillis()
		{
			return maxRenderMillis;
		}

		public int getDitherWidth()
		{
			return ditherWidth;
		}

		public int getNumDitherSamples()
		{
			return numDitherSamples;
		}

		public int[] getCacheBlockSize()
		{
			return cacheBlockSize.clone();
		}

		public int getMaxCacheSizeInMB()
		{
			return maxCacheSizeInMB;
		}

		public double getDCam()
		{
			return dCam;
		}

		public double getDClipNear()
		{
			return dClipNear;
		}

		public double getDClipFar()
		{
			return dClipFar;
		}

		public double getMaxAllowedStepInVoxels()
		{
			return maxAllowedStepInVoxels;
		}

		public int getNumSourceGroups()
		{
			return numSourceGroups;
		}

		public MessageOverlayAnimator getMsgOverlay()
		{
			return msgOverlay;
		}

		public TransformEventHandlerFactory getTransformEventHandlerFactory()
		{
			return transformEventHandlerFactory;
		}

		public InputTriggerConfig getInputTriggerConfig()
		{
			return inputTriggerConfig;
		}

		public KeyPressedManager getKeyPressedManager()
		{
			return keyPressedManager;
		}
	}
}
