import java.util.regex.*;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.lathrum.blender_render_farm.Job;
import java.util.*;

public class App {

	public static String workingDirectory;
	public static String filename;
	public static String startFrame;
	public static String endFrame;
	public static String currFrame = "0";
	public static String currFrameRegex = "Fra:([0-9]+)";
	public static Process proc;
	public static boolean isRendering = false;
	public static final Scanner Scan = new Scanner(System.in);
	
	public static boolean isNumeric(String strNum) {
	    try {
	        Integer.parseInt(strNum);
	    } catch (Exception e) {
	        return false;
	    }
	    return true;
	}
	
	public static Object[] removeElement(Object[] arr, int index) 
	{
		if (arr == null || index < 0 || index >= arr.length)
		{ 
			return arr; 
		} 
		
		Object[] anotherArray = new Object[arr.length - 1]; 
		
		System.arraycopy(arr, 0, anotherArray, 0, index); 
		System.arraycopy(arr, index + 1, anotherArray, index, arr.length - index - 1); 
		
		return anotherArray; 
	}

	public static void main(String[] args) throws IOException, InterruptedException {

		//Prepare Arguments
		try
		{
			//Correct number of args
			workingDirectory = args[0];
		}
		catch(Exception e)
		{
			System.out.println("\nUsage: blender-render-farm working_directory\n");
			System.exit(0);
		}


		int choice;
		do {
			System.out.println();
			System.out.println();
			System.out.println("[1] Master");
			System.out.println("[2] Client");
			System.out.println("[3] Quit");
			System.out.println("Which mode?");
		    while (!Scan.hasNextInt()) {
		        System.out.println("Error: Not a number");
		        Scan.next();
		    }
		    choice = Scan.nextInt();
			
			switch (choice)
			{
				case 1:
					master();
					break;
				case 2:
					System.out.println("Client Selected; returning");
					break;
				case 3:
					System.exit(0);
		        default:
		            System.out.println("Invalid choice; try again.");
		            break;
			}
		} while (choice != 3);
		
		System.exit(0);
		

		//Prepare Arguments
		try
		{
			//Correct number of args
			filename = args[0];
			startFrame = args[1];
			endFrame = args[2];
	
			//Correct arg types
			Integer.parseInt(args[1]);
			Integer.parseInt(args[2]);
		}
		catch(Exception e)
		{
			System.out.println("\nUsage: blender-render-farm file_name start_frame end_frame\n");
			//System.out.println("Options:");
			//System.out.println("");
			System.exit(0);
		}
		
	
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
			System.out.println();
		}
	
		currFrame = "-1";
		System.out.println("Took " + (System.currentTimeMillis()-start) +" ms.");
	}

	private static void master() {

		int choice;
		do {
			System.out.println();
			System.out.println();
			System.out.println("[1] Create Job");
			System.out.println("[2] List Jobs");
			System.out.println("[3] Delete Job");
			System.out.println("[4] Back");
			System.out.println("Which Action?");
		    while (!Scan.hasNextInt()) {
		        System.out.println("Error: Not a number");
		        Scan.nextLine();
		    }
		    choice = Scan.nextInt();
			
			
			switch (choice)
			{
				case 1:
					createJob();
					break;
				case 2:
					listJobs();
					break;
				case 3:
					deleteJob();
					break;
				case 4:
					break;
		        default:
		            System.out.println("Invalid choice; try again.");
		            break;
			}

		} while (choice != 4);
		
	}

	private static void createJob() {

		String file = "";
		String startFrame = "";
		String endFrame = "";
		
		while (true) //Loop until we get what we want
		{
			System.out.print("\nEnter the file name without extension relative to "+workingDirectory+": ");
			file = Scan.nextLine();
			
			if (file.length() > 0)
			{
				break; //Break the loop, we got what we want.
			}
		}
		
		while (true) //Loop until we get what we want
		{
			System.out.print("Enter the starting frame: ");
			startFrame = Scan.nextLine();
			
			if (startFrame.length() > 0 && isNumeric(startFrame))
			{
				break; //Break the loop, we got what we want.
			}
		}
		
		while (true) //Loop until we get what we want
		{
			System.out.print("Enter the ending frame: ");
			endFrame = Scan.nextLine();

			if (endFrame.length() > 0 && isNumeric(endFrame))
			{
				break; //Break the loop, we got what we want.
			}
		}

		Job job = new Job(file, startFrame, endFrame);
		Gson gson = new Gson();
		
		Type collectionType = new TypeToken<Collection<Job>>(){}.getType();
		Collection<Job> jobs = new LinkedList<Job>();
		
		//Open file
		File workingFile = new File(workingDirectory+"/render-farm/data.txt");
		if (!workingFile.exists()) {
			try {
				File directory = new File(workingFile.getParent());
				if (!directory.exists()) {
					directory.mkdirs();
				}
				workingFile.createNewFile();
			} catch (IOException e) {
				System.out.println("Excepton Occured: " + e.toString());
			}
		}
		
		//Read File
		InputStreamReader isReader;
		try {
			isReader = new InputStreamReader(new FileInputStream(workingFile), "UTF-8");

			JsonReader myReader = new JsonReader(isReader);
			Collection<Job> data = gson.fromJson(myReader, collectionType);
			if (data != null) {jobs = data;}

		} catch (Exception e) {
			System.out.println("Error load cache from file " + e.toString());
		}

		//System.out.println("\nData loaded successfully from file " + workingDirectory+"/render-farm/data.txt");
		//System.out.println(gson.toJson(jobs));
		
		jobs.add(job);

		//Write file
		try {
			FileWriter writer = new FileWriter(workingFile.getAbsoluteFile(), false);
			writer.write(gson.toJson(jobs));
			writer.close();

			//System.out.println("\nData saved at file location: " + workingDirectory+"/render-farm/data.txt" + " Data: " + gson.toJson(jobs) + "\n");
			System.out.println("Job sucessfully created");
		} catch (IOException e) {
			System.out.println("Error while saving data to file " + e.toString());
		}
		
	}

	private static void listJobs() {

		Gson gson = new Gson();
		Type collectionType = new TypeToken<Collection<Job>>(){}.getType();
		Collection<Job> jobs = new LinkedList<Job>();
		
		//Open file
		File workingFile = new File(workingDirectory+"/render-farm/data.txt");
		if (!workingFile.exists()) {
			try {
				File directory = new File(workingFile.getParent());
				if (!directory.exists()) {
					directory.mkdirs();
				}
				workingFile.createNewFile();
			} catch (IOException e) {
				System.out.println("Excepton Occured: " + e.toString());
			}
		}
		
		//Read File
		InputStreamReader isReader;
		try {
			isReader = new InputStreamReader(new FileInputStream(workingFile), "UTF-8");

			JsonReader myReader = new JsonReader(isReader);
			Collection<Job> data = gson.fromJson(myReader, collectionType);
			if (data != null) {jobs = data;}

		} catch (Exception e) {
			System.out.println("Error load cache from file " + e.toString());
		}

		//System.out.println("\nData loaded successfully from file " + workingDirectory+"/render-farm/data.txt");
		System.out.println();
		//System.out.println(gson.toJson(jobs));
		for (Job item : jobs)
		{
			System.out.println(item);
		}
		
	}

	private static void deleteJob() {

		Gson gson = new Gson();
		Type collectionType = new TypeToken<Collection<Job>>(){}.getType();
		Collection<Job> jobs = new LinkedList<Job>();
		
		//Open file
		File workingFile = new File(workingDirectory+"/render-farm/data.txt");
		if (!workingFile.exists()) {
			try {
				File directory = new File(workingFile.getParent());
				if (!directory.exists()) {
					directory.mkdirs();
				}
				workingFile.createNewFile();
			} catch (IOException e) {
				System.out.println("Excepton Occured: " + e.toString());
			}
		}
		
		//Read File
		InputStreamReader isReader;
		try {
			isReader = new InputStreamReader(new FileInputStream(workingFile), "UTF-8");

			JsonReader myReader = new JsonReader(isReader);
			Collection<Job> data = gson.fromJson(myReader, collectionType);
			if (data != null) {jobs = data;}

		} catch (Exception e) {
			System.out.println("Error load cache from file " + e.toString());
		}

		//System.out.println("\nData loaded successfully from file " + workingDirectory+"/render-farm/data.txt");
		System.out.println();
		//System.out.println(gson.toJson(jobs));
		Object[] jobArray = jobs.toArray();
		
		for (int i = 0; i < jobArray.length; i++)
		{
			System.out.println("["+i+"] "+jobArray[i]);
		}

		int choice;
		
		while (true) //Loop until we get what we want
		{
			try
			{
				System.out.println("Which Job would you like to delete?");
				choice = Scan.nextInt();
	
				if (choice > 0 && choice < jobArray.length)
				{
					break; //Break the loop, we got what we want.
				}
			}
			catch(Exception e){/*NaN?*/}
		}
		Object[] newJobArray = removeElement(jobArray,choice);
		
		//Write file
		try {
			FileWriter writer = new FileWriter(workingFile.getAbsoluteFile(), false);
			writer.write(gson.toJson(newJobArray));
			writer.close();

			//System.out.println("\nData saved at file location: " + workingDirectory+"/render-farm/data.txt" + " Data: " + gson.toJson(jobs) + "\n");
			System.out.println("Job sucessfully deleted");
		} catch (IOException e) {
			System.out.println("Error while saving data to file " + e.toString());
		}
		
	}

}
