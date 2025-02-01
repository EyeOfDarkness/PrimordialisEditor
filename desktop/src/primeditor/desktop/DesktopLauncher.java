package primeditor.desktop;

import arc.backend.sdl.*;
import primeditor.*;

public class DesktopLauncher{
	public static void main(String[] arg){
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
	}

}