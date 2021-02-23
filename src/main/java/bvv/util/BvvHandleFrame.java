package bvv.util;

import bdv.cache.CacheControl.CacheControls;
import bdv.tools.brightness.ConverterSetup;
import bdv.tools.transformation.ManualTransformationEditor;
import bdv.viewer.DisplayMode;
import bdv.viewer.SourceAndConverter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import org.scijava.ui.behaviour.util.InputActionBindings;
import org.scijava.ui.behaviour.util.TriggerBehaviourBindings;
import tpietzsch.example2.BigVolumeViewer;
import tpietzsch.example2.VolumeViewerFrame;
import tpietzsch.example2.VolumeViewerOptions;

public class BvvHandleFrame extends BvvHandle
{
	private BigVolumeViewer bvv;

	private final String frameTitle;

	BvvHandleFrame( final BvvOptions options )
	{
		super( options );
		frameTitle = options.values.getFrameTitle();
		bvv = null;
		cacheControls = new CacheControls();
	}

	public BigVolumeViewer getBigVolumeViewer()
	{
		return bvv;
	}

	@Override
	public void close()
	{
		if ( bvv != null )
		{
			final VolumeViewerFrame frame = bvv.getViewerFrame();
			frame.dispatchEvent( new WindowEvent( frame, WindowEvent.WINDOW_CLOSING ) );
			bvv = null;
		}
		super.close();
	}

	@Override
	public ManualTransformationEditor getManualTransformEditor()
	{
		return bvv.getManualTransformEditor();
	}

	@Override
	public InputActionBindings getKeybindings()
	{
		return bvv.getViewerFrame().getKeybindings();
	}

	@Override
	public TriggerBehaviourBindings getTriggerbindings()
	{
		return bvv.getViewerFrame().getTriggerbindings();
	}

	@Override
	boolean createViewer(
			final List< ? extends ConverterSetup > converterSetups,
			final List< ? extends SourceAndConverter< ? > > sources,
			final int numTimepoints )
	{
		final VolumeViewerOptions viewerOptions = bvvOptions.values.getVolumeViewerOptions();
		bvv = new BigVolumeViewer(
				new ArrayList<>( converterSetups ),
				new ArrayList<>( sources ),
				numTimepoints,
				cacheControls,
				frameTitle,
				viewerOptions );
		viewer = bvv.getViewer();
		setupAssignments = bvv.getSetupAssignments();
		setups = bvv.getConverterSetups();

		viewer.setDisplayMode( DisplayMode.FUSED );
		bvv.getViewerFrame().setVisible( true );

		final boolean initTransform = !sources.isEmpty();
		return initTransform;
	}
}
