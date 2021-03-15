/*-
 * #%L
 * Volume rendering of bdv datasets
 * %%
 * Copyright (C) 2018 - 2021 Tobias Pietzsch
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
package tpietzsch.backend.jogl;

import com.jogamp.opengl.GL2ES2;
import java.nio.FloatBuffer;
import tpietzsch.backend.SetUniforms;

public class JoglSetUniforms implements SetUniforms
{
	private final GL2ES2 gl;

	private final int program;

	JoglSetUniforms( final GL2ES2 gl, final int program )
	{
		this.gl = gl;
		this.program = program;
	}

	@Override
	public boolean shouldSet( final boolean modified )
	{
		return modified;
	}

	@Override
	public void setUniform1i( final String name, final int v0 )
	{
		gl.glProgramUniform1i( program, location( name ), v0 );
	}

	@Override
	public void setUniform2i( final String name, final int v0, final int v1 )
	{
		gl.glProgramUniform2i( program, location( name ), v0, v1 );
	}

	@Override
	public void setUniform3i( final String name, final int v0, final int v1, final int v2 )
	{
		gl.glProgramUniform3i( program, location( name ), v0, v1, v2 );
	}

	@Override
	public void setUniform4i( final String name, final int v0, final int v1, final int v2, final int v3 )
	{
		gl.glProgramUniform4i( program, location( name ), v0, v1, v2, v3 );
	}

	@Override
	public void setUniform1iv( final String name, final int count, final int[] value )
	{
		gl.glProgramUniform1iv( program, location( name ), count, value, 0 );
	}

	@Override
	public void setUniform2iv( final String name, final int count, final int[] value )
	{
		gl.glProgramUniform2iv( program, location( name ), count, value, 0 );
	}

	@Override
	public void setUniform3iv( final String name, final int count, final int[] value )
	{
		gl.glProgramUniform3iv( program, location( name ), count, value, 0 );
	}

	@Override
	public void setUniform4iv( final String name, final int count, final int[] value )
	{
		gl.glProgramUniform4iv( program, location( name ), count, value, 0 );
	}

	@Override
	public void setUniform1f( final String name, final float v0 )
	{
		gl.glProgramUniform1f( program, location( name ), v0 );
	}

	@Override
	public void setUniform2f( final String name, final float v0, final float v1 )
	{
		gl.glProgramUniform2f( program, location( name ), v0, v1 );
	}

	@Override
	public void setUniform3f( final String name, final float v0, final float v1, final float v2 )
	{
		gl.glProgramUniform3f( program, location( name ), v0, v1, v2 );
	}

	@Override
	public void setUniform4f( final String name, final float v0, final float v1, final float v2, final float v3 )
	{
		gl.glProgramUniform4f( program, location( name ), v0, v1, v2, v3 );
	}

	@Override
	public void setUniform1fv( final String name, final int count, final float[] value )
	{
		gl.glProgramUniform1fv( program, location( name ), count, value, 0 );
	}

	@Override
	public void setUniform2fv( final String name, final int count, final float[] value )
	{
		gl.glProgramUniform2fv( program, location( name ), count, value, 0 );
	}

	@Override
	public void setUniform3fv( final String name, final int count, final float[] value )
	{
		gl.glProgramUniform3fv( program, location( name ), count, value, 0 );
	}

	@Override
	public void setUniform4fv( final String name, final int count, final float[] value )
	{
		gl.glProgramUniform4fv( program, location( name ), count, value, 0 );
	}

	@Override
	public void setUniformMatrix4f( final String name, final boolean transpose, final FloatBuffer value )
	{
		gl.glProgramUniformMatrix4fv( program, location( name ), 1, transpose, value );
	}

	@Override
	public void setUniformMatrix3f( final String name, final boolean transpose, final FloatBuffer value )
	{
		gl.glProgramUniformMatrix3fv( program, location( name ), 1, transpose, value );
	}

	private int location( final String name )
	{
		return gl.glGetUniformLocation( program, name );
	}
}
