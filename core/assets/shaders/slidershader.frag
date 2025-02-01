uniform vec3 u_hsl;
uniform float u_alpha;
uniform vec2 u_rectSize;
uniform int u_mode;

varying lowp vec4 v_color;
varying lowp vec4 v_mix_color;
varying highp vec2 v_texCoords;
//uniform highp sampler2D u_texture;

vec3 hslToRgb(vec3 hsl){
    float s = hsl.g;
    float v = hsl.b;
    float x = mod(hsl.r / 60.0 + 6.0, 6.0);
    int i = int(x);
    float f = x - i;

    float p = v * (1.0 - s);
    float q = v * (1.0 - s * f);
    float t = v * (1.0 - s * (1.0 - f));

    vec3 c = vec3(0.0);
    switch(i){
        case 0: {
            c.r = v;
            c.g = t;
            c.b = p;
        } break;
        case 1: {
            c.r = q;
            c.g = v;
            c.b = p;
        } break;
        case 2: {
            c.r = p;
            c.g = v;
            c.b = t;
        } break;
        case 3: {
            c.r = p;
            c.g = q;
            c.b = v;
        } break;
        case 4: {
            c.r = t;
            c.g = p;
            c.b = v;
        } break;
        default: {
            c.r = v;
            c.g = p;
            c.b = q;
        }
    }

    return c;
}
vec3 lintosrgb(vec3 cin){
    bvec3 cutoff = lessThan(cin, vec3(0.0031308));
    vec3 higher = vec3(1.055)*pow(cin, vec3(1.0/2.4)) - vec3(0.055);
    vec3 lower = cin * vec3(12.92);

    return mix(higher, lower, cutoff);
}

void main(){
    vec4 oc = vec4(1.0);
    float shift = v_texCoords.x;

    switch(u_mode){
        case 0: {
            oc.rgb = hslToRgb(vec3(shift * 360.0, 1.0, 1.0));
        } break;
        case 1: {
            oc.rgb = hslToRgb(vec3(u_hsl.r, shift, 1.0));
        } break;
        case 2: {
            //oc.rgb = hslToRgb(vec3(u_hsl.r, u_hsl.g, shift));
            oc.rgb = hslToRgb(vec3(u_hsl.r, u_hsl.g, shift));
        } break;
        case 3: {
            oc.rgb = hslToRgb(u_hsl);
            oc.a = shift;
        } break;
        default: {
            oc.rgb = hslToRgb(u_hsl);
            oc.a = u_alpha;
        }
    }
    oc.rgb = lintosrgb(oc.rgb);
    if(oc.a < 1.0){
        vec2 wv = (v_texCoords * u_rectSize) / 8.0;
        float ti = (floor(wv.x) + floor(wv.y)) * 0.5;
        float ti2 = ti - floor(ti);

        float gr = 0.75;
        if(ti2 < 0.5){
            gr = 0.5;
        }
        oc.rgb = mix(oc.rgb, vec3(gr), 1.0 - oc.a);
        oc.a = 1.0;
    }

    gl_FragColor = oc;
}
