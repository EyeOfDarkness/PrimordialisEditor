package primeditor.io;

import arc.graphics.*;
import arc.struct.*;
import arc.util.*;
import net.jpountz.lz4.*;
import primeditor.*;
import primeditor.creature.*;
import primeditor.creature.CellTypes.*;
import primeditor.creature.Creature.*;

import java.util.*;

import static primeditor.EditorMain.canvasRes;
import static primeditor.EditorMain.hRes;

public class Version5{
    final static IntMap<CellType> typeMap = new IntMap<>();
    final static IntMap<Color> colorMap = new IntMap<>();

    public static void load(Creature creature, EditorReads r){
        typeMap.clear();
        colorMap.clear();

        long comp = r.l();
        long uncm = r.l();
        var data = r.getData();
        byte[] bits = new byte[(int)comp];

        try{
            data.readFully(bits);
            var ins = LZ4Factory.safeInstance();
            PseudoRead cr = new PseudoRead(ins.fastDecompressor().decompress(bits, (int)uncm));

            creature.orientation = (byte)cr.i();
            Control.setOrientation(creature.orientation - 3);

            int ctypes = cr.i();
            int ccombo = cr.i();
            int colors = cr.i();
            int minX = cr.i();
            int minY = cr.i();
            int maxX = cr.i();
            int maxY = cr.i();

            Log.info("compressed:" + comp);
            Log.info("uncompressed:" + uncm);
            Log.info("orientation:" + creature.orientation);
            Log.info("types:" + ctypes);
            Log.info("combos:" + ccombo);
            Log.info("colors:" + colors);
            Log.info("minX:" + minX);
            Log.info("minY:" + minY);
            Log.info("maxX:" + maxX);
            Log.info("maxY:" + maxY);
            
            for(int i = 0; i < ctypes; i++){
                int type = cr.i();
                //typeMap.put(i + 1, CellTypes.get(type));
                var cell = CellTypes.get(type);
                Log.info("cell:" + (cell == CellTypes.unknown ? Integer.toHexString(type) : cell.name));
                typeMap.put(i + 1, cell);
            }
            for(int i = 0; i < ccombo; i++){
                //int idx = 0x08000000 | i;
                int idx = 0x80000000 | i;
                int a = cr.i();
                int b = cr.i();
                ComboCellType cmb = new ComboCellType(idx);
                cmb.ia = a;
                cmb.ib = b;
                creature.combos.add(cmb);
                creature.comboMap.put(idx, cmb);
                typeMap.put(ctypes + i + 1, cmb);
                Log.info("combo " + Integer.toHexString(idx) + ": " + Integer.toHexString(a) + ", " + Integer.toHexString(b));
            }
            for(int i = 0; i < colors; i++){
                float rc = cr.f();
                float gc = cr.f();
                float bc = cr.f();
                float ac = cr.f();

                colorMap.put(i + 1, new Color(rc, gc, bc, ac));
                Log.info("color:" + rc + "," + gc + "," + bc + "," + ac);
            }

            creature.processCombos();

            int width = maxX - minX;
            int height = maxY - minY;
            int len = width * height;
            for(int i = 0; i < len; i++){
                int ix = (i % width) + minX, iy = (i / width) + minY;
                int ct = cr.i();
                if(ct == 0) continue;

                int oix = ix + hRes, oiy = iy + hRes;
                if(oix < 0 || oix >= canvasRes || oiy < 0 || oiy >= canvasRes){
                    continue;
                }

                var ty = typeMap.get(ct, CellTypes.unknown);
                Cell c = new Cell();
                c.type = ty;
                c.arrayIdx = creature.cells2.size;
                c.x = ix;
                c.y = iy;
                /*
                Color def = ty.defaultColor;
                c.r = def.r;
                c.g = def.g;
                c.b = def.b;
                c.a = def.a;
                */
                creature.cells2.add(c);
                creature.cellGrid[c.getGPos()] = c;
            }
            for(int i = 0; i < len; i++){
                int ix = (i % width) + minX, iy = (i / width) + minY;
                int ct = cr.i();
                //if(ct == 0) continue;

                int oix = ix + hRes, oiy = iy + hRes;
                if(oix < 0 || oix >= canvasRes || oiy < 0 || oiy >= canvasRes){
                    continue;
                }
                int gid = (oix % canvasRes) + (oiy * canvasRes);
                Cell c = creature.cellGrid[gid];

                if(c != null){
                    if(ct == 0){
                        Color def = c.type.defaultColor;
                        c.r = def.r;
                        c.g = def.g;
                        c.b = def.b;
                        c.a = def.a;
                    }else{
                        Color col = colorMap.get(ct, Color.magenta);
                        c.r = col.r;
                        c.g = col.g;
                        c.b = col.b;
                        c.a = col.a;
                    }
                }
            }

        }catch(Exception e){
            creature.cells2.clear();
            creature.combos.clear();
            creature.comboMap.clear();
            Arrays.fill(creature.cellGrid, null);
            Log.err(e);
        }

        typeMap.clear();
        colorMap.clear();
    }
    public static void save(Creature creature, EditorWrites r){
        //
    }

    static class PseudoRead{
        byte[] data;
        private int i = 0;

        PseudoRead(byte[] d){
            data = d;
        }

        int ub(){
            //int b = data[i];
            int b = (256 | data[i]) & 0xff;
            i++;
            return b;
        }

        int i(){
            int b3 = ub();
            int b2 = ub();
            int b1 = ub();
            int b0 = ub();

            return ((b0 & 0xff) << 24) | ((b1 & 0xff) << 16) | ((b2 & 0xff) << 8) | (b3 & 0xff);
        }

        float f(){
            return Float.intBitsToFloat(i());
        }
    }
}
