package primeditor;

import arc.*;
import arc.assets.*;
import arc.files.*;
import arc.graphics.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.Log.*;
import primeditor.creature.*;
import primeditor.graphics.*;
import primeditor.io.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

import static arc.Core.*;

public class EditorMain extends ApplicationCore{
    public static UI ui;
    public static Renderer renderer;
    public static Control control;

    boolean loadedFileLogger = false;
    String appName = "Primordialis Editor";

    public static Fi source;
    public static Creature creature;

    public static int undoHistoryLimit = 8;

    public static void loadCreature(File file){
        if(creature != null) creature.dispose();
        Control.undo.clear();

        Fi f = new Fi(file);

        source = f;
        creature = new Creature();
        creature.load(new EditorReads(f.reads()));
    }
    public static void saveCreature(Fi file){
        if(creature == null) return;

        creature.save(new EditorWrites(file.writes()));
    }
    public static void newCreature(){
        if(creature != null) creature.dispose();
        Control.undo.clear();

        source = null;
        creature = new Creature();
    }
    
	@Override
	public void setup(){
        loadLogger();
        /*
		add(control = new Control());
		add(renderer = new Renderer());
		add(world = new World());
		add(ui = new UI());
        */
        
        add(renderer = new Renderer());
        add(ui = new UI());
        add(control = new Control());

        CellTypes.load();
        Control.loadBrush();
        Shaders.load();
        Shaders.render.updateCells();

        keybinds.setDefaults(Binding.values());
        //ui.setup();
	}
    
    void loadLogger(){
        if(loadedFileLogger) return;
        
        settings.setAppName(appName);

        try{
            Writer writer = settings.getDataDirectory().child("last_log.txt").writer(false);
            LogHandler log = Log.logger;
            //ignore it
            Log.logger = (level, text) -> {
                log.log(level, text);

                try{
                    writer.write("[" + Character.toUpperCase(level.name().charAt(0)) + "] " + Log.removeColors(text) + "\n");
                    writer.flush();
                }catch(IOException e){
                    e.printStackTrace();
                    //ignore it
                }
            };
        }catch(Exception e){
            //handle log file not being found
            //Log.err(e);
            e.printStackTrace();
        }
        
        loadedFileLogger = true;
    }
}