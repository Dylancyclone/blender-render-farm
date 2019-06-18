import java.util.regex.*;
import java.io.*;
import java.nio.file.*;
import com.google.gson.*;

public class App {

	public static String filename = "placeholder";
	public static String startFrame = "1";
	public static String endFrame = "2";
	public static String currFrame = "0";
	public static String currFrameRegex = "Fra:([0-9]+)";
	public static Process proc;
	public static boolean isRendering = false;

	public static void main(String[] args) throws IOException, InterruptedException {


	JsonObject jsonObject = new JsonParser().parse("{\"name\": \"John\"}").getAsJsonObject();

	System.out.println(jsonObject.get("name").getAsString()); //John

	Runtime.getRuntime().addShutdownHook(new Thread() {
		public void run() {
			System.out.println("\n\nShutting Down...");
			if (proc != null) {proc.destroy();}
			if (currFrame != "-1" && isRendering)
			{
				try
				{
					Files.deleteIfExists(Paths.get("D:\\Documents\\BlenderFiles\\"+filename+"\\"+currFrame+".png")); 
				} 
				catch(NoSuchFileException e) 
				{ 
					System.out.println("Error deleting unfinished frame: No such file exists.");
				}
				catch(IOException e) 
				{ 
					System.out.println("Error deleting unfinished frame: Invalid permissions.");
				} 

				System.out.println("Deleted unfinshed frame "+currFrame+" successfully.");
			}
		}
	});

	long start = System.currentTimeMillis();


	isRendering = false;
	for (int i = Integer.parseInt(startFrame);i <= Integer.parseInt(endFrame);i++)
	{
		currFrame = Integer.toString(i);
		if (new File("D:\\Documents\\BlenderFiles\\"+filename+"\\"+currFrame+".png").exists())
		{
			isRendering = false;
			System.out.println("Skipping existing frame: "+currFrame);
			continue;
		}
		isRendering = true;
		String[] command = new String[] {
		"C:\\Program Files\\Blender Foundation\\Blender\\blender.exe",
		"-b", "D:\\Documents\\BlenderFiles\\"+filename+".blend",
		"-o", "D:\\Documents\\BlenderFiles\\"+filename+"\\#",
		"-F", "JPEG",
		"-x", "1",
		"-f", currFrame};

		proc = Runtime.getRuntime().exec(command);

		// Read the output

		BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

		String line = "";
		Pattern pattern = Pattern.compile(currFrameRegex);
		Matcher m;

		while((line = reader.readLine()) != null) {
			//System.out.println(currFrame);
			m = pattern.matcher(line);
			if (m.find( )) {
				System.out.print("\r"+line+"\n");
				System.out.print("As of xx:xx:xx, X jobs containing X frames remaining"); // TODO: isolation mode
				continue;
			}
			System.out.print("\n"+line);
		}

		proc.waitFor();
	}

	currFrame = "-1";
	System.out.println("Took " + (System.currentTimeMillis()-start) +" ms.");
	}

}
