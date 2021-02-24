package tpietzsch.offscreen;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collections;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;
import tpietzsch.backend.jogl.JoglGpuContext;
import tpietzsch.shadergen.DefaultShader;
import tpietzsch.shadergen.generate.Segment;
import tpietzsch.shadergen.generate.SegmentTemplate;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_CLAMP_TO_EDGE;
import static com.jogamp.opengl.GL.GL_COLOR_ATTACHMENT0;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH24_STENCIL8;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER_BINDING;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER_COMPLETE;
import static com.jogamp.opengl.GL.GL_LINEAR;
import static com.jogamp.opengl.GL.GL_RENDERBUFFER;
import static com.jogamp.opengl.GL.GL_RGB;
import static com.jogamp.opengl.GL.GL_RGB32F;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_S;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_T;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;
import static com.jogamp.opengl.GL.GL_VIEWPORT;
import static com.jogamp.opengl.GL2ES3.GL_DEPTH_STENCIL_ATTACHMENT;

/**
 * Render to texture. Print values. For debugging shaders.
 */
public class OffScreenFrameBuffer
{
	private final boolean withDepthAndStencil;

	private int vaoQuad;

	private final DefaultShader progQuad;

	private int framebuffer;

	private int texColorBuffer;

	private final int fbWidth;

	private final int fbHeight;

	// texture format for color attachment
	private final int internalFormat;

	// back up window viewport when binding this OffScreenFrameBuffer
	private int[] viewport = new int[ 4 ];

	// downloaded texture data
	private float[] rgb;

	// downloaded texture data as 3 * fwWidth * fbHeight image
	private Img< FloatType > img;

	private boolean framebufferInitialized;

	private boolean quadInitialized;

	private boolean imgInitialized;

	private boolean imgValid;

	private int restoreFramebuffer;

	/**
	 * Use {@code GL_RGB32F} as internalFormat.
	 * @param fbWidth width of offscreen framebuffer
	 * @param fbHeight height of offscreen framebuffer
	 */
	public OffScreenFrameBuffer( final int fbWidth, final int fbHeight )
	{
		this( fbWidth, fbHeight, GL_RGB32F );
	}

	/**
	 * @param fbWidth width of offscreen framebuffer
	 * @param fbHeight height of offscreen framebuffer
	 * @param internalFormat internal texture format
	 */
	public OffScreenFrameBuffer( final int fbWidth, final int fbHeight, final int internalFormat )
	{
		this( fbWidth, fbHeight, internalFormat, false );
	}

	/**
	 * @param fbWidth width of offscreen framebuffer
	 * @param fbHeight height of offscreen framebuffer
	 * @param internalFormat internal texture format
	 * @param withDepthAndStencil whether to create a render buffer for depth and stencil
	 */
	public OffScreenFrameBuffer( final int fbWidth, final int fbHeight, final int internalFormat, final boolean withDepthAndStencil )
	{
		this.fbWidth = fbWidth;
		this.fbHeight = fbHeight;
		this.internalFormat = internalFormat;
		this.withDepthAndStencil = withDepthAndStencil;

		final Segment quadvp = new SegmentTemplate( OffScreenFrameBuffer.class, "osfbquad.vp" ).instantiate();
		final Segment quadfp = new SegmentTemplate( OffScreenFrameBuffer.class, "osfbquad.fp" ).instantiate();
		progQuad = new DefaultShader( quadvp.getCode(), quadfp.getCode() );
	}

	private void initFrameBuffer( GL3 gl )
	{
		if ( framebufferInitialized )
			return;
		framebufferInitialized = true;

		final int[] tmp = new int[ 1 ];
		gl.glGenFramebuffers( 1, tmp, 0 );
		framebuffer = tmp[ 0 ];

		gl.glGetIntegerv( GL_FRAMEBUFFER_BINDING, tmp, 0 );
		restoreFramebuffer = tmp[ 0 ];
		gl.glBindFramebuffer( GL_FRAMEBUFFER, framebuffer );

		// generate texture
		gl.glGenTextures( 1, tmp, 0 );
		texColorBuffer = tmp[ 0 ];
		gl.glBindTexture( GL_TEXTURE_2D, texColorBuffer );
		gl.glTexStorage2D( GL_TEXTURE_2D, 1, internalFormat, fbWidth, fbHeight );
		gl.glBindTexture( GL_TEXTURE_2D, 0 );

		// attach it to currently bound framebuffer object
		gl.glFramebufferTexture2D( GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texColorBuffer, 0 );

		if ( withDepthAndStencil )
		{
			// create depth & stencil renderbuffer
			int rbo;
			gl.glGenRenderbuffers( 1, tmp, 0 );
			rbo = tmp[ 0 ];
			gl.glBindRenderbuffer( GL_RENDERBUFFER, rbo );
			gl.glRenderbufferStorage( GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, fbWidth, fbHeight );
			gl.glBindRenderbuffer( GL_RENDERBUFFER, 0 );

			// attach depth & stencil renderbuffer
			gl.glFramebufferRenderbuffer( GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, rbo );
		}

		if ( gl.glCheckFramebufferStatus( GL_FRAMEBUFFER ) != GL_FRAMEBUFFER_COMPLETE )
			System.err.println( "ERROR::FRAMEBUFFER:: Framebuffer is not complete!" );
		gl.glBindFramebuffer( GL_FRAMEBUFFER, restoreFramebuffer );
	}

	private void initQuad( GL3 gl )
	{
		if ( quadInitialized )
			return;
		quadInitialized = true;

		final float verticesQuad[] = {
				//    pos      texture
				 1,  1, 0,     1, 1,   // top right
				 1, -1, 0,     1, 0,   // bottom right
				-1, -1, 0,     0, 0,   // bottom left
				-1,  1, 0,     0, 1    // top left
		};

		final float verticesQuadFlipY[] = {
				//    pos      texture
				 1,  1, 0,     1, 0,   // top right
				 1, -1, 0,     1, 1,   // bottom right
				-1, -1, 0,     0, 1,   // bottom left
				-1,  1, 0,     0, 0    // top left
		};

		final int[] tmp = new int[ 1 ];
		gl.glGenBuffers( 1, tmp, 0 );
		final int vboQuad = tmp[ 0 ];
		gl.glBindBuffer( GL_ARRAY_BUFFER, vboQuad );
		gl.glBufferData( GL_ARRAY_BUFFER, verticesQuad.length * Float.BYTES, FloatBuffer.wrap( verticesQuad ), GL.GL_STATIC_DRAW );
		gl.glBindBuffer( GL_ARRAY_BUFFER, 0 );

		final int indices[] = {
				0, 3, 1,   // first triangle
				1, 3, 2    // second triangle
		};
		gl.glGenBuffers( 1, tmp, 0 );
		final int eboQuad = tmp[ 0 ];
		gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, eboQuad );
		gl.glBufferData( GL_ELEMENT_ARRAY_BUFFER, indices.length * Integer.BYTES, IntBuffer.wrap( indices ), GL.GL_STATIC_DRAW );
		gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, 0 );

		gl.glGenVertexArrays( 1, tmp, 0 );
		vaoQuad = tmp[ 0 ];
		gl.glBindVertexArray( vaoQuad );
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, vboQuad );
		gl.glVertexAttribPointer( 0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0 );
		gl.glEnableVertexAttribArray( 0 );
		gl.glVertexAttribPointer( 1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES );
		gl.glEnableVertexAttribArray( 1 );
		gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, eboQuad );
		gl.glBindVertexArray( 0 );
	}

	private void initImg()
	{
		if ( imgInitialized )
			return;
		imgInitialized = true;

		rgb = new float[ fbWidth * fbHeight * 3 ];
		img = ArrayImgs.floats( rgb, 3, fbWidth, fbHeight );
	}

	public Img< FloatType > getImg()
	{
		return img;
	}

	public int getTexColorBuffer()
	{
		return texColorBuffer;
	}

	/**
	 * Bind this framebuffer and clear it.
	 * Call before rendering.
	 */
	public void bind( GL3 gl )
	{
		bind( gl, true );
	}

	/**
	 * Unbind this framebuffer (bind framebuffer 0) and download the texture into a float array.
	 * Call after rendering.
	 */
	public void unbind( GL3 gl )
	{
		unbind( gl, true );
	}

	/**
	 * Get a value from the texture (downloaded by {@link #unbind(GL3)} or {@link #getTexture(GL3)})
	 * @param c channel (rgb)
	 * @param x
	 * @param y
	 * @return
	 */
	public float get( int c, int x, int y )
	{
		if ( !imgValid )
			System.err.println( "Img not valid. Call getTexture() first." );

		final RandomAccess< FloatType > a = img.randomAccess();
		a.setPosition( new long[] { c, x, y } );
		return a.get().get();
	}

	public void bind( GL3 gl, boolean clear )
	{
		initFrameBuffer( gl );

		final int[] tmp = new int[ 1 ];
		gl.glGetIntegerv( GL_FRAMEBUFFER_BINDING, tmp, 0 );
		restoreFramebuffer = tmp[ 0 ];

		gl.glBindFramebuffer( GL_FRAMEBUFFER, framebuffer );
		gl.glGetIntegerv( GL_VIEWPORT, viewport, 0 );
		gl.glViewport( 0, 0, fbWidth, fbHeight );
		if ( clear )
		{
			gl.glClearColor( 0, 0, 0, 0 );
			gl.glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );
		}
	}

	public void unbind( GL3 gl, boolean getTexture )
	{
		gl.glBindFramebuffer( GL_FRAMEBUFFER, restoreFramebuffer );
		gl.glViewport( viewport[ 0 ], viewport[ 1 ], viewport[ 2 ], viewport[ 3 ] );
		imgValid = false;

		if ( getTexture )
			getTexture( gl );
	}

	/**
	 * Render fullscreen quad with the texture.
	 */
	public void drawQuad( GL3 gl )
	{
		drawQuad( gl, GL_LINEAR, GL_LINEAR );
	}

	public void drawQuad( GL3 gl, int minFilter, int magFilter )
	{
		initQuad( gl );

		progQuad.use( JoglGpuContext.get( gl ) );
		gl.glActiveTexture( GL_TEXTURE0 );
		gl.glBindTexture( GL_TEXTURE_2D, texColorBuffer );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
		gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
		gl.glBindVertexArray( vaoQuad );
		gl.glDrawElements( GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0 );
		gl.glBindVertexArray( 0 );
		gl.glBindTexture( GL_TEXTURE_2D, 0 );
	}

	public void getTexture( GL3 gl )
	{
		if ( imgValid )
			return;
		imgValid = true;

		initImg();

		gl.glBindTexture( GL_TEXTURE_2D, texColorBuffer );
		gl.glGetTexImage( GL_TEXTURE_2D, 0, GL_RGB, GL_FLOAT, FloatBuffer.wrap( rgb ) );
		gl.glBindTexture( GL_TEXTURE_2D, 0 );
	}

	public int getWidth()
	{
		return fbWidth;
	}

	public int getHeight()
	{
		return fbHeight;
	}
}
