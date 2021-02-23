package bvv.util;

import bdv.cache.CacheControl.CacheControls;
import bdv.tools.InitializeViewerState;
import bdv.tools.brightness.ConverterSetup;
import bdv.tools.brightness.SetupAssignments;
import bdv.tools.transformation.ManualTransformationEditor;
import bdv.ui.CardPanel;
import bdv.ui.splitpanel.SplitPanel;
import bdv.util.BdvFunctions;
import bdv.viewer.ConverterSetups;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.TimePointListener;
import bdv.viewer.TransformListener;
import bdv.viewer.ViewerStateChangeListener;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import net.imglib2.realtransform.AffineTransform3D;
import org.scijava.ui.behaviour.util.InputActionBindings;
import org.scijava.ui.behaviour.util.TriggerBehaviourBindings;
import tpietzsch.example2.VolumeViewerPanel;

/**
 * Represents a BigVolumeViewer frame or panel and can be used to get to the bvv
 * internals.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public abstract class BvvHandle implements Bvv
{
	protected VolumeViewerPanel viewer;

	protected CardPanel cards;

	protected SplitPanel splitPanel;

	protected ConverterSetups setups;

	// TODO: Remove
	protected SetupAssignments setupAssignments;

	protected final ArrayList< BvvSource > bvvSources;

	protected final BvvOptions bvvOptions;

	protected boolean hasPlaceHolderSources;

	protected final int origNumTimepoints;

	protected CacheControls cacheControls;

	public BvvHandle( final BvvOptions options )
	{
		bvvOptions = options;
		bvvSources = new ArrayList<>();
		origNumTimepoints = 1;
	}

	@Override
	public BvvHandle getBvvHandle()
	{
		return this;
	}

	public VolumeViewerPanel getViewerPanel()
	{
		return viewer;
	}

	public CardPanel getCardPanel()
	{
		return cards;
	}

	public SplitPanel getSplitPanel()
	{
		return splitPanel;
	}

	public ConverterSetups getConverterSetups()
	{
		return setups;
	}

	// TODO: REMOVE
	@Deprecated
	public SetupAssignments getSetupAssignments()
	{
		return setupAssignments;
	}

	CacheControls getCacheControls()
	{
		return cacheControls;
	}

	@Deprecated
	int getUnusedSetupId()
	{
		return BdvFunctions.getUnusedSetupId( setupAssignments );
	}

	@Override
	public void close()
	{
		if ( viewer != null )
		{
			viewer.stop();
			bvvSources.clear();
			cacheControls.clear();

			viewer = null;
			cards = null;
			splitPanel = null;
			setups = null;
			setupAssignments = null;
			cacheControls = null;
		}
	}

	public abstract ManualTransformationEditor getManualTransformEditor();

	public abstract InputActionBindings getKeybindings();

	public abstract TriggerBehaviourBindings getTriggerbindings();

	abstract boolean createViewer(
			final List< ? extends ConverterSetup > converterSetups,
			final List< ? extends SourceAndConverter< ? > > sources,
			final int numTimepoints );

	void add(
			final List< ? extends ConverterSetup > converterSetups,
			final List< ? extends SourceAndConverter< ? > > sources,
			final int numTimepoints )
	{
		final boolean initTransform;
		if ( viewer == null )
		{
			initTransform = createViewer( converterSetups, sources, numTimepoints );
		}
		else
		{
			initTransform = viewer.state().getSources().isEmpty() && sources != null && !sources.isEmpty();

			if ( converterSetups != null && sources != null && converterSetups.size() != sources.size() )
				System.err.println( "WARNING! Adding sources to BdvHandle with converterSetups.size() != sources.size()." );

			if ( converterSetups != null )
			{
				final int numSetups = Math.min( converterSetups.size(), sources.size() );
				for ( int i = 0; i < numSetups; ++i )
				{
					final SourceAndConverter< ? > source = sources.get( i );
					final ConverterSetup setup = converterSetups.get( i );
					if ( setup != null )
						setups.put( source, setup );
				}

				// TODO: REMOVE
				converterSetups.forEach( setupAssignments::addSetup );
			}

			if ( sources != null )
				for ( final SourceAndConverter< ? > soc : sources )
				{
					viewer.state().addSource( soc );
					viewer.state().setSourceActive( soc, true );
				}
		}

		if ( initTransform )
		{
			synchronized ( this )
			{
				initTransformPending = true;
				tryInitTransform();
			}
		}
	}

	private boolean initTransformPending;

	protected synchronized void tryInitTransform()
	{
		if ( viewer.getDisplay().getWidth() <= 0 || viewer.getDisplay().getHeight() <= 0 )
			return;

		if ( initTransformPending )
		{
			initTransformPending = false;

			final Dimension dim = viewer.getDisplay().getSize();
			final AffineTransform3D viewerTransform = InitializeViewerState.initTransform( dim.width, dim.height, false, viewer.state().snapshot() );
			viewer.state().setViewerTransform( viewerTransform );
		}
	}

	void remove(
			final List< ? extends ConverterSetup > converterSetups,
			final List< ? extends SourceAndConverter< ? > > sources,
			final List< TransformListener< AffineTransform3D > > transformListeners,
			final List< TimePointListener > timepointListeners,
			final List< ViewerStateChangeListener > viewerStateChangeListeners )
	{
		if ( viewer == null )
			return;

		if ( converterSetups != null )
			for ( final ConverterSetup setup : converterSetups )
				setupAssignments.removeSetup( setup );

		if ( transformListeners != null )
			viewer.transformListeners().removeAll( transformListeners );

		if ( timepointListeners != null )
			for ( final TimePointListener l : timepointListeners )
				viewer.removeTimePointListener( l );

		if ( viewerStateChangeListeners != null )
			viewer.state().changeListeners().removeAll( viewerStateChangeListeners );

		if ( sources != null )
			viewer.state().removeSources( sources );
	}

	void addBvvSource( final BvvSource bvvSource )
	{
		bvvSources.add( bvvSource );
		updateHasPlaceHolderSources();
		updateNumTimepoints();
	}

	void removeBvvSource( final BvvSource bvvSource )
	{
		bvvSources.remove( bvvSource );
		updateHasPlaceHolderSources();
		updateNumTimepoints();
	}

	void updateHasPlaceHolderSources()
	{
		for ( final BvvSource s : bvvSources )
			if ( s.isPlaceHolderSource() )
			{
				hasPlaceHolderSources = true;
				return;
			}
		hasPlaceHolderSources = false;
	}

	void updateNumTimepoints()
	{
		int numTimepoints = origNumTimepoints;
		for ( final BvvSource s : bvvSources )
			numTimepoints = Math.max( numTimepoints, s.getNumTimepoints() );
		if ( viewer != null )
			viewer.setNumTimepoints( numTimepoints );
	}
}
