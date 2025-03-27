## Primordialis Editor
Basic creature editor for Primordialis, held by duct tape and hot glue.
Using the "Arc" framework that the author specifically said not to use.
Using someone elses copy of the repo because Jitpack is dumb and erases the files of the original.

# Controls
F1: Grid Mode. Shows the creature in unskewed coordinates. used for testing.
F2: Create an image of the creature currently in the editor.

WASD and Arrow keys to move the camera.
Q and E to rotate the camera.
ScrollWheel to zoom and change values in the color sliders.
`+` and `-` to change brush size.
When using the Fill tool, hold Shift to replace all cells that matches the cursor, Control to replace cells that matches the color, and Alt to replace cells without replacing the color.

# Limitations
- Theres no selection tool.
- This Editor currently can't load/create Combo Cells. It gets converted to basic cells if you save the creature.
- Saved creature will have a placeholder name, and cannot be changed in the application.
- Undo History Limit is currently 8.
- Fill tool has a range of 250 if used on an empty cell, 800 otherwise.
- Center pivot used for mirroring cannot be changed.

# Data Directory
All your creatures, images, and other things are stored in your app data directory

On Windows:
`C:/Users/USER/AppData/Roaming/Primordialis Editor`

On Linux:
`~/.local/share/`

## Building

Make sure you have [JDK 16-17](https://adoptium.net/archive.html?variant=openjdk17&jvmVariant=hotspot) installed. Open a terminal in the repo directory and run the following commands:

### Windows

_Running:_ `gradlew desktop:run`  
_Building:_ `gradlew desktop:dist`  
_Sprite Packing:_ `gradlew tools:pack`

### Linux

_Running:_ `./gradlew desktop:run`  
_Building:_ `./gradlew desktop:dist`  
_Sprite Packing:_ `./gradlew tools:pack`