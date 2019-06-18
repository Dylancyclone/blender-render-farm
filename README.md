# blender-render-farm

A work-in-progress Java CLI to manage rendering blender projects on multiple computers at once. This project is no where near a completed state, but updates are slowly being made.

## Current best way to run

This is my first Maven project, so I'm still trying to work out the best way to compile a standalone build in the best way. For now, this is what I'm using. This will probably change in the near future.

From the root folder, run `mvn package;java -jar .\target\blender-render-farm-0.0.1-SNAPSHOT-jar-with-dependencies.jar [file_name] [start_frame] [end_frame]`