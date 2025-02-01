package primeditor.graphics;

import arc.graphics.*;

public class ScreenQuad30{
    public final Mesh30 mesh;

    public ScreenQuad30(){
        mesh = new Mesh30(false, true, 4, 0, VertexAttribute.position, VertexAttribute.texCoords);
        mesh.setVertices(new float[]{-1f, -1f, 0f, 0f, 1f, -1f, 1f, 0f, 1f, 1f, 1f, 1f, -1f, 1f, 0f, 1f});
    }

    public void render(Shader30 shader){
        mesh.render(shader, Gl.triangleFan, 0, 4);
    }
}
