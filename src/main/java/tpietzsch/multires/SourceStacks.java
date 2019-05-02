package tpietzsch.multires;

import bdv.viewer.Source;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.cell.AbstractCellImg;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.PrimitiveType;
import net.imglib2.util.Fraction;

import static net.imglib2.type.PrimitiveType.SHORT;
import static tpietzsch.multires.SourceStacks.SourceStackType.MULTIRESOLUTION;
import static tpietzsch.multires.SourceStacks.SourceStackType.SIMPLE;
import static tpietzsch.multires.SourceStacks.SourceStackType.UNDEFINED;

public class SourceStacks
{
	public enum SourceStackType
	{
		SIMPLE,
		MULTIRESOLUTION,
		UNDEFINED
	}

	private static final Map< Source< ? >, SourceStackType > sourceStackTypes = new WeakHashMap<>();

	public static void setSourceStackType( Source< ? > source, SourceStackType stack )
	{
		sourceStackTypes.put( source, stack );
	}

	public static SourceStackType getSourceStackType( Source< ? > source )
	{
		return sourceStackTypes.getOrDefault( source, UNDEFINED );
	}

	public static < T > Stack3D< T > getStack3D( final Source< T > source, final int timepoint )
	{
		if ( !source.isPresent( timepoint ) )
			return null;

		SourceStackType stackType = getSourceStackType( source );
		if ( stackType == UNDEFINED )
		{
			stackType = inferSourceStackType( source, timepoint );
			setSourceStackType( source, stackType );
		}

		if ( stackType == SIMPLE )
			return new SimpleStack3DImp<>( source, timepoint );
		else if ( stackType == MULTIRESOLUTION )
			return new MultiResolutionStack3DImp<>( source, timepoint );
		else
			return null;
	}

	/*
	 * Decide whether to use a cached multiresolution stack or a simple stack.
	 * Depends on properties of source and what is currently implemented in BVV...
	 */
	private static SourceStackType inferSourceStackType( Source< ? > source, final int timepoint )
	{
		final Object type = source.getType();

		// Currently only [Volatile]UnsignedShortType CellImgs are handled by GPU cache
		if ( type instanceof NativeType )
		{
			final PrimitiveType primitive = ( ( NativeType ) type ).getNativeTypeFactory().getPrimitiveType();
			final Fraction epp = ( ( NativeType ) type ).getEntitiesPerPixel();
			final boolean cellimg = source.getSource( timepoint, 0 ) instanceof AbstractCellImg;
			if ( primitive == SHORT && cellimg && epp.getNumerator() == epp.getDenominator() )
				return MULTIRESOLUTION;
		}

		return SIMPLE;
	}

	static abstract class Stack3DImp< T > implements Stack3D< T >
	{
		final int timepoint;

		protected final Source< T > source;

		private final AffineTransform3D sourceTransform;

		Stack3DImp( final Source< T > source, final int timepoint )
		{
			this.timepoint = timepoint;
			this.source = source;
			this.sourceTransform = new AffineTransform3D();
			source.getSourceTransform( timepoint, 0, sourceTransform );
		}

		@Override
		public AffineTransform3D getSourceTransform()
		{
			return sourceTransform;
		}

		@Override
		public T getType()
		{
			return source.getType();
		}

		@Override
		public boolean equals( final Object o )
		{
			if ( this == o )
				return true;
			if ( o == null || getClass() != o.getClass() )
				return false;

			final Stack3DImp< ? > that = ( Stack3DImp< ? > ) o;

			if ( timepoint != that.timepoint )
				return false;
			return source.equals( that.source );
		}

		@Override
		public int hashCode()
		{
			int result = timepoint;
			result = 31 * result + source.hashCode();
			return result;
		}
	}

	static class SimpleStack3DImp< T > extends Stack3DImp< T > implements SimpleStack3D< T >
	{
		SimpleStack3DImp( final Source< T > source, final int timepoint )
		{
			super( source, timepoint );
		}

		@Override
		public RandomAccessibleInterval< T > getImage()
		{
			return source.getSource( timepoint, 0 );
		}
	}

	static class MultiResolutionStack3DImp< T > extends Stack3DImp< T > implements MultiResolutionStack3D< T >
	{
		private final ArrayList< ResolutionLevel3DImp< T > > resolutions;

		MultiResolutionStack3DImp( final Source< T > source, final int timepoint )
		{
			super( source, timepoint );

			final SourceStackResolutions ssr = SourceStacks.sourceStackResolutions.computeIfAbsent( source, s -> new SourceStackResolutions( source, timepoint ) );

			resolutions = new ArrayList<>();
			for ( int level = 0; level < source.getNumMipmapLevels(); level++ )
				resolutions.add( new ResolutionLevel3DImp<>( source, timepoint, level, ssr ) );
		}

		@Override
		public List< ResolutionLevel3DImp< T > > resolutions()
		{
			return resolutions;
		}
	}

	static class ResolutionLevel3DImp< T > implements ResolutionLevel3D< T >
	{
		private final int level;

		private final int timepoint;

		private final Source< T > source;

		private final int[] resolution;

		private final double[] scale;

		private final AffineTransform3D levelt;

		ResolutionLevel3DImp( final Source< T > source, final int timepoint, final int level, final SourceStackResolutions sourceStackResolutions )
		{
			this.level = level;
			this.timepoint = timepoint;
			this.source = source;

			resolution = sourceStackResolutions.resolutions[ level ];
			scale = sourceStackResolutions.scales[ level ];
			levelt = sourceStackResolutions.levelts[ level ];
		}

		@Override
		public int getLevel()
		{
			return level;
		}

		@Override
		public int[] getR()
		{
			return resolution;
		}

		@Override
		public double[] getS()
		{
			return scale;
		}

		@Override
		public AffineTransform3D getLevelTransform()
		{
			return levelt;
		}

		@Override
		public RandomAccessibleInterval< T > getImage()
		{
			return source.getSource( timepoint, level );
		}

		@Override
		public T getType()
		{
			return source.getType();
		}

		@Override
		public boolean equals( final Object o )
		{
			if ( this == o )
				return true;
			if ( o == null || getClass() != o.getClass() )
				return false;

			final ResolutionLevel3DImp< ? > that = ( ResolutionLevel3DImp< ? > ) o;

			if ( timepoint != that.timepoint )
				return false;
			if ( level != that.level )
				return false;
			return source.equals( that.source );
		}

		@Override
		public int hashCode()
		{
			int result = level;
			result = 31 * result + timepoint;
			result = 31 * result + source.hashCode();
			return result;
		}
	}

	/**
	 * Caches resolution level parameters extracted from Sources
	 */
	private static final Map< Source< ? >, SourceStackResolutions > sourceStackResolutions = new WeakHashMap<>();

	/**
	 * Extract resolution level parameters from a Source
	 */
	static class SourceStackResolutions
	{
		final int[][] resolutions;

		final double[][] scales;

		final AffineTransform3D[] levelts;

		SourceStackResolutions( final Source< ? > source, final int timepoint )
		{
			final int numLevels = source.getNumMipmapLevels();

			resolutions = new int[ numLevels ][];
			scales = new double[ numLevels ][];
			levelts = new AffineTransform3D[ numLevels ];

			resolutions[ 0 ] = new int[] { 1, 1, 1 };
			scales[ 0 ] = new double[] { 1, 1, 1 };
			levelts[ 0 ] = new AffineTransform3D();

			final AffineTransform3D sourceTransform = new AffineTransform3D();
			source.getSourceTransform( timepoint, 0, sourceTransform );
			for ( int level = 1; level < numLevels; level++ )
			{
				final int[] resolution = resolutions[ level ] = new int[ 3 ];
				final double[] scale = scales[ level ] = new double[ 3 ];
				final AffineTransform3D levelt = levelts[ level ] = new AffineTransform3D();

				final AffineTransform3D levelTransform = new AffineTransform3D();
				source.getSourceTransform( timepoint, level, levelTransform );
				levelTransform.preConcatenate( sourceTransform.inverse() );
				for ( int d = 0; d < 3; ++d )
				{
					resolution[ d ] = ( int ) Math.round( levelTransform.get( d, d ) );
					scale[ d ] = 1.0 / resolution[ d ];
					levelt.set( resolution[ d ], d, d );
					levelt.set( 0.5 * ( resolution[ d ] - 1 ), d, 3 );
				}

				// TODO: sanity check: levelt * levelTransform^-1 ~= identity
			}
		}
	}
}