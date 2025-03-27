attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;
attribute vec4 a_mix_color;
uniform mat4 u_projTrans;
uniform vec2 u_aspectRatio;
varying vec4 v_color;
varying vec4 v_mix_color;
varying vec2 v_texCoords;
varying vec2 v_ipos;

void main(){
    v_color = a_color;
    v_mix_color = a_mix_color;
    v_texCoords = a_texCoord0;
    vec4 ip = u_projTrans * a_position;
    v_ipos = (ip.xy - vec2(0.5)) * u_aspectRatio + vec2(0.5);
    
    gl_Position = u_projTrans * a_position;
}
