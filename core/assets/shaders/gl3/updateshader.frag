#define HIGHP

uniform highp sampler2D u_color;
uniform highp sampler2D u_type;

uniform highp sampler2D u_changeColor;
uniform highp sampler2D u_changeType;

in vec2 v_texCoords;

out vec4 o_color;
out vec4 o_type;

void main(){
    vec4 col = texture(u_color, v_texCoords);
    vec4 typ = texture(u_type, v_texCoords);

    vec4 ch = texture(u_changeType, v_texCoords);

    int type = int(ch.a * 2.2);
    if(type == 1){
        col = vec4(0.0);
        typ = vec4(0.0);
    }
    if(type == 2){
        col = texture(u_changeColor, v_texCoords);
        typ = ch;
    }
    typ.rgb = floor(typ.rgb * 8.4) / 8.0;

    //gl_FragColor = c1;
    o_color = col;
    o_type = typ;
}
