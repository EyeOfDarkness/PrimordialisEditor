package primeditor.io;

import arc.util.io.*;

public class EditorReads{
    //int[] res = new int[4];
    Reads r;

    public EditorReads(Reads reads){
        r = reads;
    }

    public void skip(int a){
        r.skip(a);
    }

    public byte b(){
        return r.b();
    }
    public int ub(){
        return r.ub();
    }

    public int i(){
        int b3 = r.ub();
        int b2 = r.ub();
        int b1 = r.ub();
        int b0 = r.ub();

        return ((b0 & 0xff) << 24) | ((b1 & 0xff) << 16) | ((b2 & 0xff) << 8) | (b3 & 0xff);
    }

    public float f(){
        return Float.intBitsToFloat(i());
    }

    public void close(){
        r.close();
    }
}
