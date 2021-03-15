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
package tpietzsch.shadergen.generate;

import java.util.Map;

import tpietzsch.shadergen.AbstractShader;
import tpietzsch.shadergen.Uniform1f;
import tpietzsch.shadergen.Uniform1fv;
import tpietzsch.shadergen.Uniform1i;
import tpietzsch.shadergen.Uniform1iv;
import tpietzsch.shadergen.Uniform2f;
import tpietzsch.shadergen.Uniform2fv;
import tpietzsch.shadergen.Uniform2i;
import tpietzsch.shadergen.Uniform2iv;
import tpietzsch.shadergen.Uniform3f;
import tpietzsch.shadergen.Uniform3fv;
import tpietzsch.shadergen.Uniform3i;
import tpietzsch.shadergen.Uniform3iv;
import tpietzsch.shadergen.Uniform4f;
import tpietzsch.shadergen.Uniform4fv;
import tpietzsch.shadergen.Uniform4i;
import tpietzsch.shadergen.Uniform4iv;
import tpietzsch.shadergen.UniformMatrix3f;
import tpietzsch.shadergen.UniformMatrix4f;
import tpietzsch.shadergen.UniformSampler;

public class SegmentedShader extends AbstractShader
{
	private final Map< String, Object > uniforms;

	SegmentedShader( final StringBuilder vpCode, final StringBuilder fpCode, final Map< String, Object > uniforms )
	{
		super( vpCode, fpCode );
		this.uniforms = uniforms;
	}

	@Override
	protected String getUniqueName( final String key )
	{
		final Object o = uniforms.get( key );
		if ( o == SegmentedShaderBuilder.NOT_UNIQUE )
			throw new IllegalArgumentException( "uniform name '" + key + "' is not unique across segments." );
		else if ( o == null )
			return key;
		else
			return ( String ) o;
	}

	public Uniform1i getUniform1i( final Segment segment, final String key )
	{
		return getUniform1i( segment.getSingleIdentifier( key ) );
	}

	public Uniform2i getUniform2i( final Segment segment, final String key )
	{
		return getUniform2i( segment.getSingleIdentifier( key ) );
	}

	public Uniform3i getUniform3i( final Segment segment, final String key )
	{
		return getUniform3i( segment.getSingleIdentifier( key ) );
	}

	public Uniform4i getUniform4i( final Segment segment, final String key )
	{
		return getUniform4i( segment.getSingleIdentifier( key ) );
	}

	public Uniform1iv getUniform1iv(final Segment segment, final String key )
	{
		return getUniform1iv( segment.getSingleIdentifier( key ) );
	}

	public Uniform2iv getUniform2iv(final Segment segment, final String key )
	{
		return getUniform2iv( segment.getSingleIdentifier( key ) );
	}

	public Uniform3iv getUniform3iv(final Segment segment, final String key )
	{
		return getUniform3iv( segment.getSingleIdentifier( key ) );
	}

	public Uniform4iv getUniform4iv(final Segment segment, final String key )
	{
		return getUniform4iv( segment.getSingleIdentifier( key ) );
	}

	public Uniform1f getUniform1f( final Segment segment, final String key )
	{
		return getUniform1f( segment.getSingleIdentifier( key ) );
	}

	public Uniform2f getUniform2f( final Segment segment, final String key )
	{
		return getUniform2f( segment.getSingleIdentifier( key ) );
	}

	public Uniform3f getUniform3f( final Segment segment, final String key )
	{
		return getUniform3f( segment.getSingleIdentifier( key ) );
	}

	public Uniform4f getUniform4f( final Segment segment, final String key )
	{
		return getUniform4f( segment.getSingleIdentifier( key ) );
	}

	public Uniform1fv getUniform1fv(final Segment segment, final String key )
	{
		return getUniform1fv( segment.getSingleIdentifier( key ) );
	}

	public Uniform2fv getUniform2fv(final Segment segment, final String key )
	{
		return getUniform2fv( segment.getSingleIdentifier( key ) );
	}

	public Uniform3fv getUniform3fv( final Segment segment, final String key )
	{
		return getUniform3fv( segment.getSingleIdentifier( key ) );
	}

	public Uniform4fv getUniform4fv(final Segment segment, final String key )
	{
		return getUniform4fv( segment.getSingleIdentifier( key ) );
	}

	public UniformMatrix3f getUniformMatrix3f( final Segment segment, final String key )
	{
		return getUniformMatrix3f( segment.getSingleIdentifier( key ) );
	}

	public UniformMatrix4f getUniformMatrix4f( final Segment segment, final String key )
	{
		return getUniformMatrix4f( segment.getSingleIdentifier( key ) );
	}

	public UniformSampler getUniformSampler( final Segment segment, final String key )
	{
		return getUniformSampler( segment.getSingleIdentifier( key ) );
	}
}
