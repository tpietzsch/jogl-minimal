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
package tpietzsch.backend;

import java.nio.FloatBuffer;

public interface SetUniforms
{
	boolean shouldSet( boolean modified );

	void setUniform1i( final String name, final int v0 );

	void setUniform2i( final String name, final int v0, final int v1 );

	void setUniform3i( final String name, final int v0, final int v1, final int v2 );

	void setUniform4i( final String name, final int v0, final int v1, final int v2, final int v3 );

	void setUniform1iv( final String name, final int count, final int[] value );

	void setUniform2iv( final String name, final int count, final int[] value );

	void setUniform3iv( final String name, final int count, final int[] value );

	void setUniform4iv( final String name, final int count, final int[] value );

	void setUniform1f( final String name, final float v0 );

	void setUniform2f( final String name, final float v0, final float v1 );

	void setUniform3f( final String name, final float v0, final float v1, final float v2 );

	void setUniform4f( final String name, final float v0, final float v1, final float v2, final float v3 );

	void setUniform1fv( final String name, final int count, final float[] value );

	void setUniform2fv( final String name, final int count, final float[] value );

	void setUniform3fv( final String name, final int count, final float[] value );

	void setUniform4fv( final String name, final int count, final float[] value );

	// transpose==true: data is in row-major order
	void setUniformMatrix3f( final String name, final boolean transpose, final FloatBuffer value );

	// transpose==true: data is in row-major order
	void setUniformMatrix4f( final String name, final boolean transpose, final FloatBuffer value );
}
