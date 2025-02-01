attribute vec4 a_position;
attribute vec2 a_texCoord0;

uniform vec2 u_aspectRatio;
uniform float u_camScl;
uniform vec2 u_camTrns;
uniform vec2 u_camPos;

varying vec2 v_texCoords;
varying vec2 v_screenCoords;

void main(){
    vec2 v = ((a_texCoord0 - vec2(0.5)) * u_camScl - u_camPos) * u_aspectRatio;
    //v.y = v.y * 2.0;
    //v.y = v.y * 1.2;
    vec2 v2 = vec2(v.x * u_camTrns.x - v.y * u_camTrns.y, (v.y * u_camTrns.x + v.x * u_camTrns.y));
    //v2.y = v2.y * 1.1525;
    v2.y = v2.y * 1.155501302;
    
    v_screenCoords = (a_texCoord0 - vec2(0.5)) * u_aspectRatio + vec2(0.5);

    v_texCoords = v2 + vec2(0.5);
    gl_Position = a_position;
}