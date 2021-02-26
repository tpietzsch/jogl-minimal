package tpietzsch.example2;

import bdv.TransformEventHandler;
import bdv.viewer.InteractiveDisplay;
import bdv.viewer.InteractiveDisplayCanvas;
import bdv.viewer.OverlayRenderer;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLCapabilitiesImmutable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.awt.GLJPanel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.util.function.Consumer;
import org.scijava.listeners.Listeners;

/**
 * Wraps a {@code Component} (either {@link GLJPanel} or {@link GLCanvas}) that
 * is used to display rendered images using JOGL.
 * <p>
 * {@code InteractiveGLDisplay} has a {@code TransformEventHandler} that is
 * notified when the component size is changed.
 * <p>
 * {@link #addHandler}/{@link #removeHandler} provide simplified adding/removing
 * of handlers that implement {@code MouseListener}, {@code KeyListener}, etc.
 *
 * @author Tobias Pietzsch
 */
public class InteractiveGLDisplayCanvas< C extends Component & GLAutoDrawable > implements InteractiveDisplay
{
	/**
	 * Receive notifications about changes of the canvas size.
	 *
	 * @author Tobias Pietzsch
	 */
	public interface CanvasSizeListener
	{
		/**
		 * This is called, when the screen size of the canvas (the component
		 * displaying the image and generating mouse events) changes. This can be
		 * used to determine scale of overlay or screen coordinates relative to the
		 * border.
		 *
		 * @param width
		 *            the new canvas width.
		 * @param height
		 *            the new canvas height.
		 */
		void setCanvasSize( int width, int height );
	}

	/**
	 * Mouse/Keyboard handler that manipulates the view transformation.
	 */
	private TransformEventHandler handler;

	/**
	 * To draw this component, {@link OverlayRenderer#drawOverlays} is invoked for each renderer.
	 */
	private final Listeners.List< OverlayRenderer > overlayRenderers;

	private final C canvas;

	private final boolean yAxisFlipped;

	private final Listeners.List< CanvasSizeListener > canvasSizeListeners;

	/**
	 * Create a new {@code InteractiveDisplayCanvas} with a {@link GLCanvas}.
	 *
	 * @param width
	 *            preferred component width.
	 * @param height
	 *            preferred component height.
	 */
	public static InteractiveGLDisplayCanvas< GLCanvas > createGLCanvas( final int width, final int height )
	{
		final GLCanvas canvas = new GLCanvas( new GLCapabilities( GLProfile.getMaxProgrammableCore( true ) ) );
		return new InteractiveGLDisplayCanvas<>( canvas, width, height, false );
	}

	static class MyGLJPanel extends GLJPanel
	{
		private Consumer< Graphics > onPaintComponent;

		public MyGLJPanel( final GLCapabilitiesImmutable userCapsRequest ) throws GLException
		{
			super( userCapsRequest );

			// Set surface scale to 1, for performance reasons.
			// This means we won't paint full resolution on highDPI displays.
			setSurfaceScale( new float[] { 1, 1 } );

			// We flip the Y axis ourselves. Don't make the GLJPanel do it.
			setSkipGLOrientationVerticalFlip( true );
		}

		@Override
		public void paintComponent( final Graphics g )
		{
			super.paintComponent( g );
			onPaintComponent.accept( g );
		}
	}

	/**
	 * Create a new {@code InteractiveDisplayCanvas} with a {@link GLJPanel}.
	 *
	 * @param width
	 *            preferred component width.
	 * @param height
	 *            preferred component height.
	 */
	public static InteractiveGLDisplayCanvas< GLJPanel > createGLJPanel( final int width, final int height )
	{
		final MyGLJPanel panel = new MyGLJPanel( new GLCapabilities( GLProfile.getMaxProgrammableCore( true ) ) );

		final InteractiveGLDisplayCanvas< GLJPanel > canvas = new InteractiveGLDisplayCanvas<>( panel, width, height, true );
		panel.onPaintComponent = canvas::paintOverlays;
		return canvas;
	}

	private InteractiveGLDisplayCanvas( final C canvas, final int width, final int height, final boolean yAxisFlipped )
	{
		this.canvas = canvas;
		this.yAxisFlipped = yAxisFlipped;

		canvas.setPreferredSize( new Dimension( width, height ) );
		canvas.setFocusable( true );

		canvasSizeListeners = new Listeners.SynchronizedList<>( r -> r.setCanvasSize( canvas.getWidth(), canvas.getHeight() ) );
		overlayRenderers = new Listeners.SynchronizedList<>( r -> r.setCanvasSize( canvas.getWidth(), canvas.getHeight() ) );

		canvas.addComponentListener( new ComponentAdapter()
		{
			@Override
			public void componentResized( final ComponentEvent e )
			{
				final int w = canvas.getWidth();
				final int h = canvas.getHeight();
				// NB: Update of overlayRenderers needs to happen before update of handler
				// Otherwise repaint might start before the render target receives the size change.
				overlayRenderers.list.forEach( r -> r.setCanvasSize( w, h ) );
				canvasSizeListeners.list.forEach( r -> r.setCanvasSize( w, h ) );
				if ( handler != null )
					handler.setCanvasSize( w, h, true );
				// enableEvents( AWTEvent.MOUSE_MOTION_EVENT_MASK );
			}
		} );

		canvas.addMouseListener( new MouseAdapter()
		{
			@Override
			public void mousePressed( final MouseEvent e )
			{
				canvas.requestFocusInWindow();
			}
		} );
	}

	/**
	 * CanvasSizeListeners can be added/removed here.
	 */
	public Listeners< CanvasSizeListener > canvasSizeListeners()
	{
		return canvasSizeListeners;
	}

	@Override
	public Listeners< OverlayRenderer > overlays()
	{
		return overlayRenderers;
	}

	private void paintOverlays( final Graphics g )
	{
		overlayRenderers.list.forEach( r -> r.drawOverlays( g ) );
	}

	/**
	 * Add new event handler. Depending on the interfaces implemented by
	 * <code>handler</code> calls {@link Component#addKeyListener(KeyListener)},
	 * {@link Component#addMouseListener(MouseListener)},
	 * {@link Component#addMouseMotionListener(MouseMotionListener)},
	 * {@link Component#addMouseWheelListener(MouseWheelListener)}.
	 *
	 * @param h handler to remove
	 */
	public void addHandler( final Object h )
	{
		if ( h instanceof KeyListener )
			canvas.addKeyListener( ( KeyListener ) h );

		if ( h instanceof MouseMotionListener )
			canvas.addMouseMotionListener( ( MouseMotionListener ) h );

		if ( h instanceof MouseListener )
			canvas.addMouseListener( ( MouseListener ) h );

		if ( h instanceof MouseWheelListener )
			canvas.addMouseWheelListener( ( MouseWheelListener ) h );

		if ( h instanceof FocusListener )
			canvas.addFocusListener( ( FocusListener ) h );
	}

	/**
	 * Remove an event handler. Add new event handler. Depending on the
	 * interfaces implemented by <code>handler</code> calls
	 * {@link Component#removeKeyListener(KeyListener)},
	 * {@link Component#removeMouseListener(MouseListener)},
	 * {@link Component#removeMouseMotionListener(MouseMotionListener)},
	 * {@link Component#removeMouseWheelListener(MouseWheelListener)}.
	 *
	 * @param h handler to remove
	 */
	public void removeHandler( final Object h )
	{
		if ( h instanceof KeyListener )
			canvas.removeKeyListener( ( KeyListener ) h );

		if ( h instanceof MouseMotionListener )
			canvas.removeMouseMotionListener( ( MouseMotionListener ) h );

		if ( h instanceof MouseListener )
			canvas.removeMouseListener( ( MouseListener ) h );

		if ( h instanceof MouseWheelListener )
			canvas.removeMouseWheelListener( ( MouseWheelListener ) h );

		if ( h instanceof FocusListener )
			canvas.removeFocusListener( ( FocusListener ) h );
	}

	/**
	 * Set the {@link TransformEventHandler} that will be notified when component is resized.
	 *
	 * @param transformEventHandler
	 *            handler to use
	 */
	public void setTransformEventHandler( final TransformEventHandler transformEventHandler )
	{
		if ( handler != null )
			removeHandler( handler );
		handler = transformEventHandler;
		int w = canvas.getWidth();
		int h = canvas.getHeight();
		if ( w <= 0 || h <= 0 )
		{
			final Dimension preferred = canvas.getPreferredSize();
			w = preferred.width;
			h = preferred.height;
		}
		handler.setCanvasSize( w, h, false );
		addHandler( handler );
	}

	/**
	 * Implementing classes extend {@link Component} and return {@code this}.
	 */
	public C getComponent()
	{
		return canvas;
	}

	public boolean yAxisFlipped()
	{
		return yAxisFlipped;
	}

	// -- forwarding some panel methods for convenience --

	public void addGLEventListener( GLEventListener listener)
	{
		canvas.addGLEventListener( listener );
	}

	public void display()
	{
		canvas.display();
	}

	public boolean requestFocusInWindow()
	{
		return canvas.requestFocusInWindow();
	}

	public int getWidth()
	{
		return canvas.getWidth();
	}

	public int getHeight()
	{
		return canvas.getHeight();
	}

	public Dimension getSize()
	{
		return canvas.getSize();
	}
}
