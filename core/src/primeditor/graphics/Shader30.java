package primeditor.graphics;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.gl.*;
import arc.util.*;
import primeditor.utils.*;

import java.nio.*;

public class Shader30 extends Shader{
    public IntBuffer buffer;
    public String[] outputs;

    public Shader30(String vert, String frag, String[] outs){
        super(vert, frag);
        outputs = outs;
        generateBuffer();
    }
    public Shader30(String vert, String frag){
        super(vert, frag);
        generateBuffer();
    }
    public Shader30(Fi vert, Fi frag){
        super(vert, frag);
    }

    void generateBuffer(){
        int program = ReflectUtils.getInt(Shader.class, this, "program");
        if(program != -1 && outputs != null){
            //int color = Core.gl30.glGetFragDataLocation(program, "o_color");
            IntBuffer buffer = Buffers.newIntBuffer(outputs.length);
            for(String out : outputs){
                int loc = Core.gl30.glGetFragDataLocation(program, out);
                //Log.info(out + ":" + (GL30.GL_COLOR_ATTACHMENT0 + loc));
                buffer.put(GL30.GL_COLOR_ATTACHMENT0 + loc);
            }
            //Log.info("Attachment:" + GL30.GL_COLOR_ATTACHMENT0);
            buffer.position(0);
            this.buffer = buffer;
        }
    }

    @Override
    protected String preprocess(String source, boolean fragment){
        if(fragment){
            source =
                    "#ifdef GL_ES\n" +
                            "precision " + (source.contains("#define HIGHP") && !source.contains("//#define HIGHP") ? "highp" : "mediump") + " float;\n" +
                            "precision mediump int;\n" +
                            "#else\n" +
                            "#define lowp  \n" +
                            "#define mediump \n" +
                            "#define highp \n" +
                            "#endif\n" + source;
        }else{
            source =
                    "#ifndef GL_ES\n" +
                            "#define lowp  \n" +
                            "#define mediump \n" +
                            "#define highp \n" +
                            "#endif\n" + source;
        }

        if(Core.gl30 != null){
            String version =
                    source.contains("#version ") ? "" :
                            Core.app.isDesktop() ? (Core.graphics.getGLVersion().atLeast(3, 2) ? "150" : "130") :
                                    "300 es";

            return
                    "#version " + version + "\n"
                            + source
                            .replace("varying", fragment ? "in" : "out")
                            .replace("attribute", fragment ? "???" : "in")
                            .replace("texture2D(", "texture(")
                            .replace("textureCube(", "texture(");
        }
        return source;
    }
}
