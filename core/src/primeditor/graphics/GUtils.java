package primeditor.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;

public class GUtils{
    static float[] verts = new float[24];
    static ScreenQuad30 quad;

    static ScreenQuad30 getQuad(){
        if(quad == null) quad = new ScreenQuad30();
        return quad;
    }

    public static void blit(Shader30 shader){
        var q = getQuad();
        /*
        q.mesh.setAutoBind(false);
        q.mesh.bind(shader);
        Core.gl30.glDrawBuffers(shader.outputs.length, shader.buffer);
        q.render(shader);

        q.mesh.unbind(shader);
        q.mesh.setAutoBind(true);
        */
        shader.bind();
        shader.apply();
        q.render(shader);
    }

    public static void srect(float x, float y, float width, float height){
        float mc = Color.clearFloatBits;
        float c = Color.whiteFloatBits;
        float x2 = x + width, y2 = y + height;
        verts[0] = x;
        verts[1] = y;
        verts[2] = c;
        verts[3] = 0;
        verts[4] = 0;
        verts[5] = mc;

        verts[6] = x2;
        verts[7] = y;
        verts[8] = c;
        verts[9] = 1;
        verts[10] = 0;
        verts[11] = mc;

        verts[12] = x2;
        verts[13] = y2;
        verts[14] = c;
        verts[15] = 1;
        verts[16] = 1;
        verts[17] = mc;

        verts[18] = x;
        verts[19] = y2;
        verts[20] = c;
        verts[21] = 0;
        verts[22] = 1;
        verts[23] = mc;

        Draw.vert(Core.atlas.white().texture, verts, 0, 24);
    }
}
