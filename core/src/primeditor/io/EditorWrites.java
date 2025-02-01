package primeditor.io;

import arc.util.io.*;

public class EditorWrites{
    Writes w;

    public EditorWrites(Writes w){
        this.w = w;
    }

    public void b(byte v){
        w.b(v);
    }

    public void i(int v){
        //int b3 = r.ub();
        //int b2 = r.ub();
        //int b1 = r.ub();
        //int b0 = r.ub();
        //
        //return ((b0 & 0xff) << 24) | ((b1 & 0xff) << 16) | ((b2 & 0xff) << 8) | (b3 & 0xff);

        int b0 = (v >>> 24) & 0xff;
        int b1 = (v >>> 16) & 0xff;
        int b2 = (v >>> 8) & 0xff;
        int b3 = v & 0xff;

        int out = (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
        w.i(out);
    }

    public void f(float v){
        i(Float.floatToRawIntBits(v));
    }

    public void close(){
        w.close();
    }
}
