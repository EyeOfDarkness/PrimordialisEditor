#define HIGHP

uniform highp sampler2D u_color;
uniform highp sampler2D u_type;

in vec2 v_texCoords;

out vec4 o_color;
out vec4 o_type;

void main(){
    vec4 typ = texture(u_type, v_texCoords);
    typ.rgb = floor(typ.rgb * 8.4) / 8.0;
    o_color = texture(u_color, v_texCoords);
    o_type = typ;
}
