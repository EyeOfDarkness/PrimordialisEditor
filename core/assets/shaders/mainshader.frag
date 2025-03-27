#define HIGHP

#define MAX_TYPES 128

varying highp vec2 v_texCoords;
varying vec2 v_ipos;

uniform highp sampler2D u_color;
uniform sampler2D u_type;
uniform sampler2D u_texture;

uniform int u_showtype;
uniform highp float u_resolution;
uniform highp float u_ires;
uniform vec2 u_camTrns;

uniform vec4 u_regions[MAX_TYPES];

const vec2 dir[4] = vec2[4](vec2(0.0, 0.0), vec2(1.0, 0.0), vec2(0.0, 1.0), vec2(1.0, 1.0));
//const vec2 dir2[9] = vec2[9](vec2(1.0, -1.0), vec2(0.0, -1.0), vec2(1.0, -1.0), vec2(1.0, 0.0), vec2(0.0, 0.0), vec2(1.0, 0.0), vec2(1.0, 1.0), vec2(0.0, 1.0), vec2(1.0, 1.0));

vec3 lintosrgb(vec3 cin){
    bvec3 cutoff = lessThan(cin, vec3(0.0031308));
    vec3 higher = vec3(1.055)*pow(cin, vec3(1.0/2.4)) - vec3(0.055);
    vec3 lower = cin * vec3(12.92);

    return mix(higher, lower, cutoff);
}

int regionIdx(vec3 col){
    int idx = int(col.r * 8.4) + int(col.g * 8.4) * 8 + int(col.b * 8.4) * 64;
    idx = min(idx, MAX_TYPES - 1);

    return idx;
}

void main(){
    bool found = false;
    float cdst = 999.0;
    vec4 oc = vec4(0.0);
    vec4 ot = vec4(0.0);
    float shdw = 0.0;
    vec2 uv = vec2(0.0);
    vec2 icor = floor(v_texCoords * u_resolution);
    vec2 fcor = v_texCoords * u_resolution;
    
    vec2 corout = vec2(0.0);
    for(int i = 0; i < 4; i++){
        vec2 loc = dir[i] + icor;
        vec2 dif = (v_texCoords * u_resolution) - loc;
        //vec2 ndif = vec2(dif.x + dif.y * 0.5 * 0.866025403, dif.y * 0.866025403);
        vec2 ndif = vec2(dif.x + dif.y * 0.5, dif.y * 0.866025403);
        
        vec4 typ = texture2D(u_type, loc * u_ires);
        
        float len = length(ndif);
        if(typ.a > 0.5 && len < 0.6 && len < cdst){
            float f = 1.0 - (len * 1.6666667);
            float f2 = min(1.0, f * 2.75 + 0.5);
            oc = texture2D(u_color, loc * u_ires);
            shdw = f2;
            
            uv = ndif;
            
            ot = typ;
            cdst = len;
            corout = loc;
            found = true;
        }
    }
    //oc = texture2D(u_color, v_texCoords);
    //ot = texture2D(u_type, v_texCoords);
    vec3 nrgb = lintosrgb(oc.rgb);
    float lum = (nrgb.r + nrgb.g + nrgb.b) * 0.333333;
    oc.rgb = lintosrgb(oc.rgb * shdw);
    //oc.rgb = nrgb * shdw;
    if(fcor.x < 1.0 || fcor.y < 1.0 || fcor.x > (u_resolution - 1.0) || fcor.y > (u_resolution - 1.0)){
        oc = vec4(0.0);
        ot = vec4(0.0);
    }

    float colA = oc.a;
    if(u_showtype != 2){
        //c1.a = c2.a;
        oc.a = ot.a;
        if(colA < 0.999){
            //vec2 wv = v_screenCoords * 60.0;
            //vec2 wv = v_ipos * 512.0 * 2.0;
            vec2 wv = v_ipos * 48.0;
            float ti = (floor(wv.x) + floor(wv.y)) * 0.5;
            float ti2 = ti - floor(ti);

            float gr = 0.75;
            if(ti2 < 0.5){
                gr = 0.5;
            }
            oc.rgb = mix(oc.rgb, vec3(gr), 1.0 - colA);
        }
        if(u_showtype == 1 && ot.a > 0.5){
            uv = vec2(uv.x * u_camTrns.x - uv.y * u_camTrns.y, uv.y * u_camTrns.x + uv.x * u_camTrns.y);
            uv.y = -uv.y;

            if(uv.x >= -0.5 && uv.x <= 0.5 && uv.y >= -0.5 && uv.y <= 0.5){
                int rd = regionIdx(ot.rgb);
                vec4 reg = u_regions[rd];
                vec2 uv2 = (uv + vec2(0.5)) * reg.zw + reg.xy;

                vec4 c3 = texture2D(u_texture, uv2);
                if(lum > 0.75){
                    c3.rgb = vec3(0.1);
                }
                oc.rgb = mix(oc.rgb, c3.rgb, c3.a);
            }
        }
    }else{
        if(ot.a < 0.5){
            oc.a = 0.0;
        }else{
            oc.a = min(1.0, oc.a * (1.0039215));
        }
    }

    gl_FragColor = oc;
}
