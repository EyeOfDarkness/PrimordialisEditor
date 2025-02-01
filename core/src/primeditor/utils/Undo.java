package primeditor.utils;

import arc.math.*;
import arc.struct.*;
import primeditor.creature.*;
import primeditor.creature.CellTypes.*;
import primeditor.creature.Creature.*;

public class Undo{
    //Seq<ShortSeq> history = new Seq<>(ShortSeq.class);
    IntSeq[] history = new IntSeq[9];
    int idx = 0;

    int uid = 0;

    public boolean active;
    Cell reference = new Cell();

    public Undo(){
        for(int i = 0; i < history.length; i++){
            history[i] = new IntSeq();
        }
    }

    public void flush(){
        /*
        if(idx >= history.length){
            IntSeq first = history[0];
            first.clear();

            System.arraycopy(history, 1, history, 0, history.length - 1);

            history[history.length - 1] = first;
            idx = history.length;

            return;
        }
        */
        IntSeq sq = history[Mathf.mod(idx, history.length)];
        if(sq.isEmpty()) return;
        idx++;
        history[Mathf.mod(idx, history.length)].clear();
    }

    public void clear(){
        for(IntSeq s : history){
            s.clear();
        }
        idx = 0;
    }

    public void undo(Creature creature){
        //idx = Mathf.clamp(idx - 1, 0, history.length - 1);
        idx--;
        IntSeq seq = history[Mathf.mod(idx, history.length)];
        if(!seq.isEmpty()){
            uid = 0;
            while(uid < seq.size){
                read(seq, creature);
            }
            seq.clear();
            uid = 0;
        }
    }

    public void register(Creature creature){
        IntSeq seq = history[Mathf.mod(idx, history.length)];
        for(Cell c : creature.changed){
            //format 4 = 1 color, 2 no cell
            //type 4
            //x 4, y 4
            //r 8
            //g 8
            //b 8
            //a 8
            Cell c2 = creature.cellGrid[c.getGPos()];

            write(seq, c2, c);
        }
        flush();
    }

    public void registerIndividual(Cell next){
        IntSeq seq = history[Mathf.mod(idx, history.length)];
        /*
        if(last == null || (last.deleted || !last.equals(next))){
            write(seq, last, next);
        }
        */
        if(!reference.equals(next) || (next.deleted && reference.type != null)){
            write(seq, reference.type == null ? null : reference, next);
        }
    }

    public void setReference(Cell last){
        if(last == null){
            reference.type = null;
            return;
        }
        reference.type = last.type;
        reference.x = last.x;
        reference.y = last.y;
        reference.r = last.r;
        reference.g = last.g;
        reference.b = last.b;
        reference.a = last.a;
        reference.deleted = last.deleted;
    }

    void read(IntSeq seq, Creature creature){
        int[] items = seq.items;

        int format = items[uid++];
        int type = (format >>> 16) & 0xffff;
        int fr = format & 0xffff;
        int pformat = items[uid++];

        int ix = (pformat & 0xffff) - 1024;
        int iy = ((pformat >>> 16) & 0xffff) - 1024;

        //return ((x + 1024) % 2048) + ((y * 2048) + 1024 * 2048);
        int j = ((ix + 1024) % 2048) + ((iy * 2048) + 1024 * 2048);

        if(fr == 2){
            //int ix = next.x + 1024, iy = next.y + 1024;
            //int pformat = (ix & 0xffff) + (iy & 0xffff) << 16;
            //((y * 2048) + 1024 * 2048)
            Cell c = creature.cellGrid[j];
            if(c != null){
                c.deleted = true;
                creature.changed.add(c);
            }
        }else{
            CellType ct = CellTypes.types.get(type);

            Cell c = creature.cellGrid[j];
            if(c == null){
                c = new Cell();
                c.added = true;
                c.x = ix;
                c.y = iy;
            }
            c.type = ct;

            if(fr == 1){
                c.r = Float.intBitsToFloat(items[uid++]);
                c.g = Float.intBitsToFloat(items[uid++]);
                c.b = Float.intBitsToFloat(items[uid++]);
                c.a = Float.intBitsToFloat(items[uid++]);
            }
            creature.changed.add(c);
        }
    }

    void write(IntSeq seq, Cell last, Cell next){
        if(last != null){
            int type = last.type.id;
            boolean color = false;
            int format = type << 16;
            if(last.r != next.r || last.g != next.g || last.b != next.b  || last.a != next.a || next.deleted){
                color = true;
                format |= 1;
            }
            seq.add(format);
            int ix = last.x + 1024, iy = last.y + 1024;
            int pformat = (ix & 0xffff) | (iy & 0xffff) << 16;
            seq.add(pformat);

            if(color){
                seq.add(Float.floatToRawIntBits(last.r));
                seq.add(Float.floatToRawIntBits(last.g));
                seq.add(Float.floatToRawIntBits(last.b));
                seq.add(Float.floatToRawIntBits(last.a));
            }
        }else{
            seq.add(2);
            int ix = next.x + 1024, iy = next.y + 1024;
            int pformat = (ix & 0xffff) | ((iy & 0xffff) << 16);
            seq.add(pformat);
        }
    }
}
