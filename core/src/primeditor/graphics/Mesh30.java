package primeditor.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.gl.*;

public class Mesh30 extends Mesh{
    public Mesh30(boolean useVertexArray, boolean isStatic, int maxVertices, int maxIndices, VertexAttribute... attributes){
        super(useVertexArray, isStatic, maxVertices, maxIndices, attributes);
    }

    @Override
    public void render(Shader shader, int primitiveType, int offset, int count, boolean autoBind){
        if(count == 0) return;

        if(autoBind) bind(shader);
        if(shader instanceof Shader30 ns && ns.outputs != null){
            Core.gl30.glDrawBuffers(ns.outputs.length, ns.buffer);
        }

        vertices.render(indices, primitiveType, offset, count);

        if(autoBind) unbind(shader);
    }
}
