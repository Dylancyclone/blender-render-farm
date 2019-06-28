# blender-render-farm

blender-render-farm is a Java CLI built to manage rendering blender projects on multiple computers at once. One folder is selected as a working directory where blender files are stored as jobs and any number of computers can directly connect to it to render frames.

The jobs are stored in a simple json file, meaning any computer than has file access to the computer that hosts the project files can contribute to rendering.

The application supports all Windows and Unix platforms, and supports the following output formats: TGA, RAWTGA, JPEG, IRIS, IRIZ, AVIRAW, AVIJPEG, PNG, BMP

## How to Run

Either grab the latest [release](https://github.com/Dylancyclone/blender-render-farm/releases), or compile it for yourself using `mvn package` from the root folder.

To instantly run it, use:

`mvn package;java -jar ./target/blender-render-farm-1.0.0-jar-with-dependencies.jar [WORKING_DIRECTORY] [BLENDER_EXECUTABLE]`

## Usage and Examples

```
usage: blender-render-farm [WORKING_DIRECTORY] [BLENDER_EXECUTABLE] [args...]

 -b,--basic    Only show basic output
 -c,--client   Instantly become a client
 -e,--extra    Show extra output
 -h,--help     Show this message
 -m,--master   Instantly become a master
```

For example running from a remote computer connecting to a windows computer over a UNC path:

`java -jar blender-render-farm.jar \\computer-name\BlenderFiles path/to/blender/executable`

Note on Linux systems (or if you've got blender in your windows PATH), it may be sufficient to just write 'blender' for the executable and it will be found.

After creating a job that points to a .blend file, all rendered frames will be placed in a subdirectory next to the blend file with the same name.

## Future Updates

While the current version works perfectly for a LAN network, there may come a time where I need rendering across networks. To do this, I would update this to be closer to a true master/client application, where the client connects to a master instance and asks directly for work to do. The master would choose a job and a frame that needs rendering and send the info to the client. The client would download and cache the job files to minimize network usage, render the frame locally, then send it back to the master and ask what to do next.