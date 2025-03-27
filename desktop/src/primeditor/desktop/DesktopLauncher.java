package primeditor.desktop;

import arc.backend.sdl.*;
import arc.util.*;
import primeditor.*;

public class DesktopLauncher{
	public static void main(String[] arg){
		try{
			new SdlApplication(new EditorMain(), new SdlConfig(){{
				title = "Primordialis Creature Editor";
				maximized = false;
				disableAudio = false;
				vSyncEnabled = true;
				disableAudio = true;
				gl30 = true;
				width = 1200;
				height = 800;
			}});
		}catch(Throwable th){
			handleCrash(th);
		}
	}

	static void handleCrash(Throwable err){
		try{
			Log.err(err);
		}catch(Throwable no){
			err.printStackTrace();
		}
		System.exit(1);
	}
}