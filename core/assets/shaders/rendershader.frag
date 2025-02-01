#define HIGHP

#define MAX_TYPES 128

uniform highp sampler2D u_texture;
uniform sampler2D u_type;

uniform sampler2D u_main;

uniform int u_showtype;

uniform vec2 u_camTrns;

uniform vec4 u_regions[MAX_TYPES];

varying vec2 v_texCoords;
varying vec2 v_screenCoords;

vec3 lintosrgb(vec3 cin){
    bvec3 cutoff = lessThan(cin, vec3(0.0031308));
    vec3 higher = vec3(1.055)*pow(cin, vec3(1.0/2.4)) - vec3(0.055);
    vec3 lower = cin * vec3(12.92);

    return mix(higher, lower, cutoff);
}

vec2 toIdx(float wx, float wy){
    float offsetX = (floor(wy) - 1024.0) * 0.5;
    float ix = wx - offsetX;
    float iy = wy;
    
    return vec2(ix, iy);
}

int regionIdx(vec3 col){
    int idx = int(col.r * 8.4) + int(col.g * 8.4) * 8 + int(col.b * 8.4) * 64;
    idx = min(idx, MAX_TYPES - 1);

    return idx;
}

void main(){
    float offsetX = (floor(v_texCoords.y * 2048.0) - 1024.0) / (2048.0 * 2.0);
    vec2 v = v_texCoords;
    
    vec2 wv = v_texCoords * 2048.0;
    vec2 iw = floor(wv);
    //vec2 wv2 = v * 2048.0;
    
    vec2 h1 = toIdx(wv.x, wv.y);
    vec2 hf = floor(h1);
    
    vec2 testh = h1;
    
    vec2 lp = (h1 - hf) - vec2(0.5);
    vec2 uv = (h1 - hf);
    
    //float lx = 0.5 - max(0.0, abs(lp.x) - 0.25) * 0.577350 * 1.13;
    //possibly off by some decimal
    //float lx = 0.5 - max(0.0, abs(lp.x) - 0.25) * 0.577350 * 1.1525;
    float lx = 0.5 - max(0.0, abs(lp.x) - 0.25) * 0.577350 * 1.155501302;
    float ly = abs(lp.y);
    if(lp.y > 0.0){
        vec2 h2 = toIdx(wv.x, wv.y + 1.0);
        if(ly > lx){
            h1 = h2;
            uv = (h1 - floor(h1)) - vec2(0.0, 1.0);
        }
    }else{
        vec2 h2 = toIdx(wv.x, wv.y - 1.0);
        if(ly > lx){
            h1 = h2;
            uv = (h1 - floor(h1)) + vec2(0.0, 1.0);
        }
    }

    vec4 c1 = texture2D(u_texture, h1 / 2048.0);
    vec4 c2 = texture2D(u_type, h1 / 2048.0);

    float colA = c1.a;

    c1.rgb = lintosrgb(c1.rgb);
    if(u_showtype != 2){
        c1.a = c2.a;
        if(colA < 0.999){
            vec2 wv = v_screenCoords * 60.0;
            float ti = (floor(wv.x) + floor(wv.y)) * 0.5;
            float ti2 = ti - floor(ti);
            
            float gr = 0.75;
            if(ti2 < 0.5){
                gr = 0.5;
            }
            c1.rgb = mix(c1.rgb, vec3(gr), 1.0 - colA);
        }
    }else{
        //c1.a *= c2.a;
        if(c2.a < 0.5){
            c1.a = 0.0;
        }else{
            c1.a = min(1.0, c1.a * (1.0039215));
        }
    }
    if(u_showtype == 1 && c2.a > 0.5){
        uv = (uv - vec2(0.5)) * 0.75;
        uv.x = uv.x * 1.155501302;
        uv.y = -uv.y;
        uv = vec2(uv.x * u_camTrns.x - uv.y * u_camTrns.y, uv.y * u_camTrns.x + uv.x * u_camTrns.y);
        uv = uv + vec2(0.5);
        //vec4 c2 = texture2D(u_type, v_texCoords);
        int rd = regionIdx(c2.rgb);
        vec4 reg = u_regions[rd];
        vec2 uv2 = uv * reg.zw + reg.xy;

        vec4 c3 = texture2D(u_main, uv2);
        float trgb = (c1.r + c1.g + c1.b) * 0.333333;
        if(trgb > 0.75){
            c3.rgb = vec3(0.1);
        }

        c1.rgb = mix(c1.rgb, c3.rgb, c3.a);
        //c1.rg = uv;
    }
    //vec2 uv = (h1 - hf);
    if(u_showtype != 2 && (h1.x < 0.0 || h1.y < 0.0 || h1.x > 2048.0 || h1.y > 2048.0)){
        c1 = vec4(0.5, 0.1, 0.1, 1.0);
    }
    
    //c1.rg = uv;
    //c1 = mix(c1, texture2D(u_main, v_screenCoords), 0.5);
    //c1 = texture2D(u_main, v_screenCoords);

    gl_FragColor = c1;
}
