package primeditor.graphics;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import primeditor.graphics.Shaders.*;

import static primeditor.EditorMain.canvasRes;

public class BlankBatch extends Batch{
    public static final int VERTEX_SIZE = 2 + 4 + 1;
    public static final int SPRITE_SIZE = 4 * VERTEX_SIZE;

    protected final float[] vertices;

    protected Color typeColor = new Color();
    protected float type;

    public BlankBatch(int size){
        if(size > 8191) throw new IllegalArgumentException("Can't have more than 8191 sprites per batch: " + size);

        if(size > 0){
            projectionMatrix.setOrtho(0, 0, canvasRes, canvasRes);

            VertexAttribute pcolor = new VertexAttribute(4, GL20.GL_FLOAT, true, "a_color");
            VertexAttribute ptype = new VertexAttribute(4, GL20.GL_UNSIGNED_BYTE, true, "a_type");

            mesh = new Mesh30(true, false, size * 4, size * 6,
                    VertexAttribute.position,
                    pcolor,
                    ptype
            );

            vertices = new float[size * SPRITE_SIZE];

            int len = size * 6;
            short[] indices = new short[len];
            short j = 0;
            for(int i = 0; i < len; i += 6, j += 4){
                indices[i] = j;
                indices[i + 1] = (short)(j + 1);
                indices[i + 2] = (short)(j + 2);
                indices[i + 3] = (short)(j + 2);
                indices[i + 4] = (short)(j + 3);
                indices[i + 5] = j;
            }
            mesh.setIndices(indices);

            shader = createShader();
        }else{
            vertices = new float[0];
            shader = null;
        }
    }

    public void setType(Color c){
        typeColor.set(c);
        type = typeColor.toFloatBits();
    }
    public void setTypeDelete(){
        typeColor.set(0f, 0f, 0f, 0.5f);
        type = typeColor.toFloatBits();
    }

    @Override
    protected void draw(Texture texture, float[] spriteVertices, int offset, int count){
        //
    }

    @Override
    protected void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float rotation){
        if(idx == vertices.length){
            flush();
        }

        float[] vertices = this.vertices;
        int idx = this.idx;
        this.idx += SPRITE_SIZE;

        float fx2 = x + width;
        float fy2 = y + height;

        //rendering breaks because opengl
        float minA = Math.max(0.001f, color.a);

        vertices[idx] = x;
        vertices[idx + 1] = y;
        vertices[idx + 2] = color.r;
        vertices[idx + 3] = color.g;
        vertices[idx + 4] = color.b;
        vertices[idx + 5] = minA;
        vertices[idx + 6] = type;

        vertices[idx + 7] = x;
        vertices[idx + 8] = fy2;
        vertices[idx + 9] = color.r;
        vertices[idx + 10] = color.g;
        vertices[idx + 11] = color.b;
        vertices[idx + 12] = minA;
        vertices[idx + 13] = type;

        vertices[idx + 14] = fx2;
        vertices[idx + 15] = fy2;
        vertices[idx + 16] = color.r;
        vertices[idx + 17] = color.g;
        vertices[idx + 18] = color.b;
        vertices[idx + 19] = minA;
        vertices[idx + 20] = type;

        vertices[idx + 21] = fx2;
        vertices[idx + 22] = y;
        vertices[idx + 23] = color.r;
        vertices[idx + 24] = color.g;
        vertices[idx + 25] = color.b;
        vertices[idx + 26] = minA;
        vertices[idx + 27] = type;
    }

    @Override
    protected void flush(){
        if(idx == 0) return;

        getShader().bind();
        setupMatrices();

        if(customShader != null && apply){
            customShader.apply();
        }

        Gl.depthMask(false);
        //totalRenderCalls++;
        int spritesInBatch = idx / SPRITE_SIZE;
        //if(spritesInBatch > maxSpritesInBatch) maxSpritesInBatch = spritesInBatch;
        int count = spritesInBatch * 6;

        blending.apply();

        //lastTexture.bind();
        Mesh mesh = this.mesh;
        mesh.setVertices(vertices, 0, idx);
        mesh.getIndicesBuffer().position(0);
        mesh.getIndicesBuffer().limit(count);
        mesh.render(getShader(), Gl.triangles, 0, count);

        idx = 0;
    }

    Shader createShader(){
        return new BlankBatchShader();
    }
}
