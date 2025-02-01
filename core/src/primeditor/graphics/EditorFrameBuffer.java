package primeditor.graphics;

import arc.graphics.*;
import arc.graphics.Pixmap.*;
import arc.graphics.Texture.*;
import arc.graphics.gl.*;
import arc.util.*;

public class EditorFrameBuffer extends FrameBuffer{
    int tmpWidth, tmpHeight;
    /*
    protected FloatFrameBuffer(GLFrameBufferBuilder<? extends GLFrameBuffer<Texture>> bufferBuilder){
        super(bufferBuilder);
    }

    public FloatFrameBuffer(int width, int height, boolean hasDepth){
        FloatFrameBufferBuilder bufferBuilder = new FloatFrameBufferBuilder(width, height);
        bufferBuilder.addFloatAttachment(GL30.GL_RGBA32F, GL30.GL_RGBA, GL30.GL_FLOAT, false);
        if(hasDepth) bufferBuilder.addBasicDepthRenderBuffer();
        this.bufferBuilder = bufferBuilder;

        build();
    }
     */
    protected EditorFrameBuffer(GLFrameBufferBuilder<? extends GLFrameBuffer<Texture>> bufferBuilder){
        super(bufferBuilder);
    }
    public EditorFrameBuffer(int width, int height){
        tmpWidth = width;
        tmpHeight = height;
        EditorFrameBufferBuilder bufferBuilder = new EditorFrameBufferBuilder(width, height);
        bufferBuilder.addFloatAttachment(GL30.GL_RGBA32F, GL30.GL_RGBA, GL30.GL_FLOAT, true);
        bufferBuilder.addBasicColorTextureAttachment(Format.rgba4444);
        this.bufferBuilder = bufferBuilder;

        build();
    }

    @Override
    protected void create(Format format, int width, int height, boolean hasDepth, boolean hasStencil){
        //super.create(format, width, height, hasDepth, hasStencil);
    }

    @Override
    protected Texture createTexture(FrameBufferTextureAttachmentSpec attachmentSpec){
        try{
            var spec = FrameBufferTextureAttachmentSpec.class;

            var f = spec.getDeclaredField("isFloat");
            f.setAccessible(true);
            if(f.getBoolean(attachmentSpec)){
                //GLOnlyTextureData data = new GLOnlyTextureData(tmpWidth, tmpHeight, 0, inf.getInt(attachmentSpec), frm.getInt(attachmentSpec), typ.getInt(attachmentSpec));
                FloatTextureData data = new FloatTextureData(tmpWidth, tmpHeight, GL30.GL_RGBA32F, GL30.GL_RGBA, GL30.GL_FLOAT, true);
                Texture result = new Texture(data);
                result.setFilter(TextureFilter.nearest, TextureFilter.nearest);
                result.setWrap(TextureWrap.clampToEdge, TextureWrap.clampToEdge);

                return result;
            }
        }catch(Exception e){
            throw new ArcRuntimeException(e);
        }
        Texture t = super.createTexture(attachmentSpec);
        t.setFilter(TextureFilter.nearest);

        return t;
    }

    static class EditorFrameBufferBuilder extends GLFrameBufferBuilder<EditorFrameBuffer>{
        EditorFrameBufferBuilder(int width, int height){
            super(width, height);
        }

        @Override
        public EditorFrameBuffer build(){
            return new EditorFrameBuffer(this);
        }
    }
}
