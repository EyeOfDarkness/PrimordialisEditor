package primeditor.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import primeditor.*;
import primeditor.creature.*;

public class Shaders{
    static String ds, ds2, ws;
    public static Shader screen;
    public static TypeShader ts;
    public static ColorShader cs;
    public static RenderShader render;
    public static ColorWheelShader colwheel;

    public static UpdateShader update;
    public static UploadShader upload;

    public static TestShader test;

    public static void load(){
        ds = Core.files.internal("shaders/drawshader.vert").readString();
        ds2 = """
                in vec4 a_position;
                in vec2 a_texCoord0;
                
                out vec2 v_texCoords;
                
                void main(){
                    v_texCoords = a_texCoord0;
                    gl_Position = a_position;
                }
                """;
        ws = """
                attribute vec4 a_position;
                attribute vec4 a_color;
                attribute vec2 a_texCoord0;
                attribute vec4 a_mix_color;
                uniform mat4 u_projTrans;
                varying vec4 v_color;
                varying vec4 v_mix_color;
                varying vec2 v_texCoords;
                
                void main(){
                    v_color = a_color;
                    v_mix_color = a_mix_color;
                    v_texCoords = a_texCoord0;
                    gl_Position = u_projTrans * a_position;
                }
                """;

        ts = new TypeShader();
        cs = new ColorShader();
        render = new RenderShader();
        test = new TestShader();
        colwheel = new ColorWheelShader();

        update = new UpdateShader();
        upload = new UploadShader();

        screen = new Shader("""
                attribute vec4 a_position;
                attribute vec2 a_texCoord0;
                
                varying vec2 v_texCoords;
                
                void main(){
                    v_texCoords = a_texCoord0;
                    gl_Position = a_position;
                }
                """, """
                uniform sampler2D u_texture;
                                        
                varying vec2 v_texCoords;
                                    
                void main(){
                    gl_FragColor = texture2D(u_texture, v_texCoords);
                }
                """);
    }

    public static class TypeShader extends Shader{
        public Texture t1, t2;

        TypeShader(){
            super(ds, Core.files.internal("shaders/typesshader.frag").readString());
        }

        @Override
        public void apply(){
            t2.bind(1);
            t1.bind(0);

            setUniformi("u_texture", 0);
            setUniformi("u_type", 1);
        }
    }
    public static class ColorShader extends Shader{
        public Texture t1, t2, t3;

        ColorShader(){
            super(ds, Core.files.internal("shaders/colorsshader.frag").readString());
        }

        @Override
        public void apply(){
            t3.bind(2);
            t2.bind(1);
            t1.bind(0);

            setUniformi("u_texture", 0);
            setUniformi("u_type", 1);
            setUniformi("u_color", 2);
        }
    }
    public static class RenderShader extends Shader{
        public Texture color, type;
        public boolean screenshotMode = false;
        public float scWidth, scHeight, scX, scY;

        float[] types;

        RenderShader(){
            super(Core.files.internal("shaders/rendershader.vert"), Core.files.internal("shaders/rendershader.frag"));
        }

        public void updateCells(){
            types = new float[128 * 4];
            int i2 = 0;
            for(int i = 0; i < types.length; i += 4){
                if(i2 < CellTypes.types.size){
                    TextureRegion rg = CellTypes.types.get(i2).region;
                    types[i] = rg.u;
                    types[i + 1] = rg.v;
                    types[i + 2] = rg.u2 - rg.u;
                    types[i + 3] = rg.v2 - rg.v;
                }
                i2++;
            }
            //bind();
            //setUniform4fv("u_regions", types, 0, types.length);
        }

        @Override
        public void apply(){
            CellTypes.unknown.region.texture.bind(2);
            type.bind(1);
            color.bind(0);

            float fs = Math.min(Core.graphics.getWidth(), Core.graphics.getHeight());

            setUniformi("u_texture", 0);
            setUniformi("u_type", 1);
            setUniformi("u_main", 2);

            if(!screenshotMode){
                setUniformf("u_aspectRatio", Core.graphics.getWidth() / fs, Core.graphics.getHeight() / fs);
                setUniformf("u_camScl", Renderer.cs);
                setUniformf("u_camPos", Renderer.cx, Renderer.cy);
                setUniformf("u_camTrns", Mathf.cosDeg(-Renderer.cr), Mathf.sinDeg(-Renderer.cr));

                setUniformi("u_showtype", Renderer.showtype ? 1 : 0);
            }else{
                setUniformf("u_aspectRatio", scWidth / 2048f, scHeight / 2048f);
                setUniformf("u_camScl", 1f);
                setUniformf("u_camPos", scX, scY);
                setUniformf("u_camTrns", Mathf.cosDeg(-Renderer.cr), Mathf.sinDeg(-Renderer.cr));
                setUniformi("u_showtype", 2);
            }

            setUniform4fv("u_regions", types, 0, types.length);
        }
    }
    public static class ColorWheelShader extends Shader{
        public int mode;
        public float h, s, l, a;
        public float rectX, rectY;

        ColorWheelShader(){
            super(ws, Core.files.internal("shaders/slidershader.frag").readString());
        }

        @Override
        public void apply(){
            setUniformf("u_hsl", h, s, l);
            setUniformf("u_alpha", a);
            setUniformf("u_rectSize", rectX, rectY);
            setUniformi("u_mode", mode);
        }
    }

    public static class UpdateShader extends Shader30{
        public Texture color, type;
        public Texture changeColor, changeType;

        UpdateShader(){
            super(ds2, Core.files.internal("shaders/gl3/updateshader.frag").readString(), new String[]{"o_color", "o_type"});
        }

        @Override
        public void apply(){
            changeType.bind(3);
            changeColor.bind(2);
            type.bind(1);
            color.bind(0);

            setUniformi("u_color", 0);
            setUniformi("u_type", 1);

            setUniformi("u_changeColor", 2);
            setUniformi("u_changeType", 3);
        }
    }
    public static class UploadShader extends Shader30{
        public Texture color, type;

        UploadShader(){
            super(ds2, Core.files.internal("shaders/gl3/uploadshader.frag").readString(), new String[]{"o_color", "o_type"});
        }

        @Override
        public void apply(){
            type.bind(1);
            color.bind(0);

            setUniformi("u_color", 0);
            setUniformi("u_type", 1);
        }
    }

    public static class BlankBatchShader extends Shader30{
        BlankBatchShader(){
            super("""
                in vec4 a_position;
                in vec4 a_color;
                in vec4 a_type;
                uniform mat4 u_projTrans;
                out vec4 v_color;
                out vec4 v_type;
                
                void main(){
                    v_color = a_color;
                    v_type = a_type;
                    gl_Position = u_projTrans * a_position;
                }
                """, """
                in vec4 v_color;
                in vec4 v_type;
                
                out vec4 o_color;
                out vec4 o_type;
                
                void main(){
                    o_color = v_color;
                    o_type = v_type;
                }
                """, new String[]{"o_color", "o_type"});
        }
    }
    public static class TestShader extends Shader{
        TestShader(){
            super("""
                    attribute vec4 a_position;
                    attribute vec2 a_texCoord0;
                    
                    uniform vec2 u_aspectRatio;
                    uniform float u_camScl;
                    
                    varying vec2 v_texCoords;
                    
                    void main(){
                        vec2 v2 = ((a_texCoord0 - vec2(0.5)) * u_camScl) * u_aspectRatio;
                        v_texCoords = v2 + vec2(0.5);
                        gl_Position = a_position;
                    }
                    """, """
                    uniform sampler2D u_texture;
                    
                    varying vec2 v_texCoords;
                    void main(){
                        vec4 c1 = texture2D(u_texture, v_texCoords);
                        gl_FragColor = c1;
                    }
                    """);
        }
        
        @Override
        public void apply(){
            float fs = Math.min(Core.graphics.getWidth(), Core.graphics.getHeight());
            setUniformf("u_aspectRatio", Core.graphics.getWidth() / fs, Core.graphics.getHeight() / fs);
            setUniformf("u_camScl", Renderer.cs);
        }
    }
}
