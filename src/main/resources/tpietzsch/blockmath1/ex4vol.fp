out vec4 FragColor;

uniform vec2 viewportSize;

uniform mat4 ipvm;

uniform vec3 sourcemin;
uniform vec3 sourcemax;

uniform sampler3D scaleLut;
uniform sampler3D offsetLut;
uniform sampler3D volumeCache;

uniform vec3 blockSize;
uniform vec3 lutSize;
uniform vec3 padSize;

uniform float intensity_offset;
uniform float intensity_scale;


// intersect ray with a box
// http://www.siggraph.org/education/materials/HyperGraph/raytrace/rtinter3.htm
void intersectBox( vec3 r_o, vec3 r_d, vec3 boxmin, vec3 boxmax, out float tnear, out float tfar )
{
	// compute intersection of ray with all six bbox planes
	vec3 invR = 1 / r_d;
	vec3 tbot = invR * ( boxmin - r_o );
	vec3 ttop = invR * ( boxmax - r_o );

	// re-order intersections to find smallest and largest on each axis
	vec3 tmin = min(ttop, tbot);
	vec3 tmax = max(ttop, tbot);

	// find the largest tmin and the smallest tmax
	tnear = max( max( tmin.x, tmin.y ), max( tmin.x, tmin.z ) );
	tfar = min( min( tmax.x, tmax.y ), min( tmax.x, tmax.z ) );
}

void main()
{
    // frag coord in NDC
    vec2 uv = 2 * gl_FragCoord.xy / viewportSize - 1;

    // NDC of frag on near and far plane
    vec4 front = vec4( uv, -1, 1 );
    vec4 back = vec4( uv, 1, 1 );

    // calculate eye ray in source space
    vec4 mfront = ipvm * front;
    mfront *= 1 / mfront.w;
    vec4 mback = ipvm * back;
    mback *= 1 / mback.w;
    vec3 frontToBack = (mback - mfront).xyz;
    vec3 step = normalize( frontToBack );

    // find intersection with box
    float tnear, tfar;
    intersectBox( mfront.xyz, step, sourcemin, sourcemax, tnear, tfar );
    tnear = max( 0, tnear );
    tfar = min( length( frontToBack ), tfar );
    const float sub = 1;
    const vec3 oobcol = vec3( 1, 1, 1 );
    const vec3 l0col = vec3( 0.7, 1, 0.7 );
    const vec3 l1col = vec3( 1, 0.7, 0.7 );
    const vec3 l2col = vec3( 0.7, 0.7, 1 );
    if ( tnear < tfar )
    {
        vec3 pos = mfront.xyz + tnear * step + 0.5;
        vec3 q = ( pos + blockSize * padSize ) / ( blockSize * lutSize );
        vec3 qstep = ( 1 / sub ) * step / ( blockSize * lutSize );
        int numSteps = int( sub * trunc( tfar - tnear ) + 1 );
        float v = 0;
        float qv = 0;
        for ( int i = 0; i < numSteps; ++i, q += qstep )
        {
	        vec3 qs = texture( scaleLut, q ).xyz;
	        vec3 qd = texture( offsetLut, q ).xyz;
	        float vi = texture( volumeCache, q * qs + qd ).r;
	        if ( vi > v )
	            qv = qs.x;
	        v = max( v, vi );
        }
        vec3 lcol;
        if ( qv > 3 )
            lcol = l0col;
        else if ( qv > 2 )
            lcol = l1col;
        else if ( qv > 0.1 )
            lcol = l2col;
        else
            lcol = oobcol;
        FragColor = vec4( lcol * vec3( intensity_offset + intensity_scale * v ), 1 );
    }
    else
        discard;
}