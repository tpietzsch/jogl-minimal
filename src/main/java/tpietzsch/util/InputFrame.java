package tpietzsch.util;

import bdv.viewer.render.PainterThread;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.scijava.ui.behaviour.MouseAndKeyHandler;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.Behaviours;
import org.scijava.ui.behaviour.util.InputActionBindings;
import org.scijava.ui.behaviour.util.TriggerBehaviourBindings;
import tpietzsch.example2.InteractiveGLDisplayCanvas;

public class InputFrame
{
	private GLEventListener glEventListener;

	private final PainterThread painterThread;

	private final InteractiveGLDisplayCanvas< GLCanvas > display;

	private long t0;

	private final InputActionBindings keybindings;

	private final TriggerBehaviourBindings triggerbindings;

	private final Actions actions;

	private final Behaviours behaviours;

	private final JFrame frame;

	private final GLEventListener wrapper = new GLEventListener()
	{
		@Override
		public void init( final GLAutoDrawable drawable )
		{
			dbgln( "InputFrame.init" );
			if ( glEventListener != null )
				glEventListener.init( drawable );
			else
				dbgln( "glEventListener == null" );
		}

		@Override
		public void dispose( final GLAutoDrawable drawable )
		{
			dbgln( "InputFrame.dispose" );
			if ( glEventListener != null )
				glEventListener.dispose( drawable );
			else
				dbgln( "glEventListener == null" );
		}

		@Override
		public void display( final GLAutoDrawable drawable )
		{
			dbgln( "InputFrame.display" );
			if ( glEventListener != null )
				glEventListener.display( drawable );
			else
				dbgln( "glEventListener == null" );
		}

		@Override
		public void reshape( final GLAutoDrawable drawable, final int x, final int y, final int width, final int height )
		{
			dbgln( "InputFrame.reshape" );
			dbgln( "x = [" + x + "], y = [" + y + "], width = [" + width + "], height = [" + height + "]" );
			if ( glEventListener != null )
				glEventListener.reshape( drawable, x, y, width, height );
			else
				dbgln( "glEventListener == null" );
		}
	};

	public InputFrame(
			final String title,
			final int width,
			final int height )
	{
//		final GLCapabilities capsReqUser = new GLCapabilities( GLProfile.getGL2GL3() );
//		final GLCapabilities capsReqUser = new GLCapabilities( GLProfile.getMaxProgrammableCore( true ) );
		display = InteractiveGLDisplayCanvas.createGLCanvas( width, height );
		display.addGLEventListener( wrapper );

		painterThread = new PainterThread( display::display );

		frame = new JFrame( title );
		frame.getRootPane().setDoubleBuffered( true );
		frame.getContentPane().add( display.getComponent(), BorderLayout.CENTER );
		frame.pack();
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		frame.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				dbgln( "stopping painterThread..." );
				painterThread.interrupt();
			}
		} );

		keybindings = new InputActionBindings();
		triggerbindings = new TriggerBehaviourBindings();

		final InputTriggerConfig keyConfig = new InputTriggerConfig();
		actions = new Actions( keyConfig, "all" );
		actions.install( keybindings, "default" );
		behaviours = new Behaviours( keyConfig, "all" );
		behaviours.install( triggerbindings, "default" );

		SwingUtilities.replaceUIActionMap( frame.getRootPane(), getKeybindings().getConcatenatedActionMap() );
		SwingUtilities.replaceUIInputMap( frame.getRootPane(), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, getKeybindings().getConcatenatedInputMap() );

		final MouseAndKeyHandler mouseAndKeyHandler = new MouseAndKeyHandler();
		mouseAndKeyHandler.setInputMap( getTriggerbindings().getConcatenatedInputTriggerMap() );
		mouseAndKeyHandler.setBehaviourMap( getTriggerbindings().getConcatenatedBehaviourMap() );
		display.addHandler( mouseAndKeyHandler );

		t0 = -1;
	}

	public void setGlEventListener( final GLEventListener glEventListener )
	{
		this.glEventListener = glEventListener;
	}

	public void show()
	{
		t0 = System.currentTimeMillis();
		frame.setVisible( true );
		display.requestFocusInWindow();
		painterThread.start();
	}

	public void requestRepaint()
	{
		painterThread.requestRepaint();
	}

	public long dTimeMillis()
	{
		return t0 < 0
				? 0
				: ( System.currentTimeMillis() - t0 );
	}

	public InputActionBindings getKeybindings()
	{
		return keybindings;
	}

	public TriggerBehaviourBindings getTriggerbindings()
	{
		return triggerbindings;
	}

	public Actions getDefaultActions()
	{
		return actions;
	}

	public Behaviours getDefaultBehaviours()
	{
		return behaviours;
	}

	public GLCanvas getDisplay()
	{
		return display.getComponent();
	}

	public JFrame getFrame()
	{
		return frame;
	}

	/*
	 *
	 *
	 */

	public static boolean DEBUG = true;

	public static void dbgln( final String string )
	{
		if ( DEBUG )
			System.out.println( string );
	}
}
