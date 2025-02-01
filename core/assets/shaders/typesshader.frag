#define HIGHP

uniform sampler2D u_texture;

uniform sampler2D u_type;

varying vec2 v_texCoords;

void main(){
    vec4 c1 = texture2D(u_texture, v_texCoords);
    vec4 c2 = texture2D(u_type, v_texCoords);

    int type = int(c2.a * 2.0);

    if(type == 1){
        c1 = vec4(0.0);
    }
    if(type == 2){
        c1 = c2;
    }

    gl_FragColor = c1;
}
