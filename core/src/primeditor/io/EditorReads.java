package primeditor.io;

import arc.util.io.*;

import java.io.*;

public class EditorReads{
    //int[] res = new int[4];
    Reads r;

    public EditorReads(Reads reads){
        r = reads;
    }

    public void skip(int a){
        r.skip(a);
    }

    public DataInput getData(){
        return r.input;
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
    public long l(){
        long b7 = r.ub();
        long b6 = r.ub();
        long b5 = r.ub();
        long b4 = r.ub();
        long b3 = r.ub();
        long b2 = r.ub();
        long b1 = r.ub();
        long b0 = r.ub();

        return ((b0 & 0xff) << 56L) | ((b1 & 0xff) << 48L) | ((b2 & 0xff) << 40L) | ((b3 & 0xff) << 32L) | ((b4 & 0xff) << 24L) | ((b5 & 0xff) << 16L) | ((b6 & 0xff) << 8) | (b7 & 0xff);
    }

    public float f(){
        return Float.intBitsToFloat(i());
    }

    public void close(){
        r.close();
    }
}
