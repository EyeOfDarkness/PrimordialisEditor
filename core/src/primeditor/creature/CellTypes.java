package primeditor.creature;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.struct.*;
import arc.util.*;
import primeditor.*;

import java.util.*;

public class CellTypes{
    public static Seq<CellType> types = new Seq<>(), combos = new Seq<>();
    private final static IntMap<CellType> typeMap = new IntMap<>();
    public static CellType unknown, combo, def;

    private static int gid = 0;
    private final static Seq<CellType> allSeq = new Seq<>();

    public static void load(){
        Fi list = Core.files.internal("celltypes.txt");
        String[] str = list.readString().split("\n");
        for(String st : str){
            //s = s.substring(0, 4);
            //Log.info(s + ":" + s.toLowerCase() + ":" + s.length());
            //new CellType(s);
            String[] s = st.split(" ");
            CellType cell = new CellType(s[0]);
            try{
                float r = Float.parseFloat(s[1]);
                float g = Float.parseFloat(s[2]);
                float b = Float.parseFloat(s[3]);

                cell.defaultColor.set(r, g, b, 1f);
            }catch(Exception e){
                cell.defaultColor.set(0.5f, 0.5f, 0.5f, 1f);
                Log.info(e);
            }
            if(s[0].equals("BODY")){
                def = cell;
            }
            //Log.info(cell.name + ": " + cell.defaultColor.r + "," + cell.defaultColor.g + "," + cell.defaultColor.b);
        }

        /*
        unknown = new CellType("BODY");
        new CellType("HART");
        new CellType("SPIK");
        new CellType("SEEK");
        new CellType("TRG0");
        new CellType("HARD");
        */
        //unknown = types.get(0);
        unknown = new CellType("UNKN");
        unknown.hidden = true;
        combo = new CellType("CMBO");
        combo.hidden = true;
        //defaultTypes.add(unknown);
    }

    public static Seq<CellType> all(){
        allSeq.clear();
        allSeq.addAll(types);
        if(EditorMain.creature != null){
            allSeq.add(EditorMain.creature.combos);
        }

        return allSeq;
    }

    public static void reset(){
        for(CellType com : combos){
            typeMap.remove(com.type);
        }
        combos.clear();
        gid = types.size;
    }

    public static CellType get(int type){
        return typeMap.get(type, unknown);
    }

    public static class CellType{
        public int id;
        int type;
        public boolean hidden = false;

        public TextureRegion region;
        public String name;
        public Color defaultColor = new Color();

        CellType(String name){
            this(name, true);
        }

        CellType(String name, boolean shoudAdd){
            id = gid++;

            for(int i = 0; i < 4; i++){
                int offset = i * 8;
                char n = name.charAt(i);
                int d = (n & 255) << offset;
                type |= d;
            }

            this.name = name;
            //this.region = Core.atlas.find("cells/" + name.toLowerCase(), Core.atlas.find("cells/no-texture"));
            this.region = Core.atlas.find(name.toLowerCase(), Core.atlas.find("no-texture"));
            if(shoudAdd){
                types.add(this);
                typeMap.put(type, this);
            }

            //type = d;
            //Log.info("Cell Type: " + name.toLowerCase() + " : " + Integer.toHexString(type));
        }

        public Color getType(){
            float f1 = (id / 8f) % 1f;
            float f2 = ((int)(id / 8f) / 8f) % 1f;
            float f3 = (int)(id / (8f * 8f)) / 8f;

            //idx = int(f1 * 16f) + int(f2 * 16f) * 16 + int(f3 * 16f) * 256;
            //error correction?
            //idx = int(f1 * 16.05f) + int(f2 * 16.05f) * 16 + int(f3 * 16.05f) * 256;
            //should total 4096 cells, assuming no precision loss.
            //a=0: no cell, a=0.5 delete, a=1 replace

            return Tmp.c2.set(f1, f2, f3, 1f);
        }
    }

    public static class ComboCellType extends CellType{
        public CellType a, b;
        public int ia, ib;
        public String desc;
        boolean loaded = false;

        public ComboCellType(int type){
            super("CMBO", false);
            this.type = type;
            this.region = Core.atlas.find("cmbo", Core.atlas.find("no-texture"));
        }

        public void addSet(ObjectSet<ComboCellType> set){
            if(set.contains(this)) return;
            set.add(this);
            if(a instanceof ComboCellType ac) ac.addSet(set);
            if(b instanceof ComboCellType bc) bc.addSet(set);
        }

        @Override
        public Color getType(){
            return combo.getType();
        }

        public void loadDescription(){
            if(loaded) return;
            loaded = true;

            String na;
            if(a instanceof ComboCellType cm){
                cm.loadDescription();
                na = cm.desc;
            }else{
                na = a.name;
            }

            String nb;
            if(b instanceof ComboCellType cm){
                cm.loadDescription();
                nb = cm.desc;
            }else{
                nb = b.name;
            }

            defaultColor.a = 1f;
            defaultColor.set(a.defaultColor).lerp(b.defaultColor, 0.5f);
            desc = "(" + na + ", " + nb + ")";
        }
    }
}
