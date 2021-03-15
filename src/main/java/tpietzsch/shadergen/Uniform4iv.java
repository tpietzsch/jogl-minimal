package tpietzsch.shadergen;

public interface Uniform4iv
{
    void set( int[] value );

    /*
     * DEFAULT METHODS
     */

    default void set( final int[][] v )
    {
        final int elemSize = 4;
        final int[] data = new int[ elemSize * v.length ];
        int j = 0;
        for ( int i = 0; i < v.length; ++i )
            for ( int d = 0; d < elemSize; ++d )
                data[ j++ ] = v[ i ][ d ];
        set( data );
    }
//	default void set( final Vector3fc... v )
}
