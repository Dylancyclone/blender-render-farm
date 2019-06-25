import java.util.regex.*;
import org.apache.commons.cli.*;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import me.tongfei.progressbar.*;
import com.lathrum.blender_render_farm.Job;

import java.util.*;

public class App {

	public static String workingDirectory;
	public static String blenderPath;
	public static String currFrame = "0";
	public static boolean basicMode = false;
	public static Process proc;
	public static boolean isRendering = false;
	public static String currentJobFile;
	public static String currentJobFormat;
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
	
	public static String formatPath(String res) {
	    if (res==null) return null;
	    if (File.separatorChar=='\\') {
	        // From Windows to Linux/Mac
	        return res.replace('/', File.separatorChar);
	    } else {
	        // From Linux/Mac to Windows
	        return res.replace('\\', File.separatorChar);
	    }
	}

	
	public static String normalizeFileFormat(String format) {
		format = format.toLowerCase();

		if (format.equals("jpeg")) //While blender takes "JPEG" as the render format, it still outputs a .jpg :(
		{
			format="jpg";
		}
		return format;
	}

	public static void main(String[] args) throws IOException, InterruptedException {

		//Prepare Arguments
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();

		options.addOption("h", "help", false, "Show this message");
		options.addOption("c", "client", false, "Instantly become a client");
		options.addOption("m", "master", false, "Instantly become a master");
		options.addOption("b", "basic", false, "Only show basic output (functionally identical)");
		//options.addOption("e", "extra", false, "Show extra output (may be slower)");
	    try {
	        // parse the command line arguments
	        CommandLine cmd = parser.parse(options, args);
		    if(cmd.hasOption("h") || args[0].charAt(0) =='-' || args[1].charAt(0) =='-') {
		    	HelpFormatter formatter = new HelpFormatter();
		    	formatter.printHelp("blender-render-farm [WORKING_DIRECTORY] [BLENDER_EXECUTABLE] [args...]", options, false);
				System.exit(0);
		    }
			workingDirectory = args[0];
			blenderPath = args[1];
		    if(cmd.hasOption("b")) {
				basicMode = true;
		    }
		    if(cmd.hasOption("c")) {
		        client();
		    }
		    if(cmd.hasOption("m")) {
				master();
		    }
	    }
	    catch( ParseException e ) {
	        System.err.println(e.getMessage());
	    	HelpFormatter formatter = new HelpFormatter();
	    	formatter.printHelp("blender-render-farm [WORKING_DIRECTORY] [BLENDER_EXECUTABLE] [args...]", options, false);
			System.exit(0);
	    }
	    catch( Exception e ) {
	    	HelpFormatter formatter = new HelpFormatter();
	    	formatter.printHelp("blender-render-farm [WORKING_DIRECTORY] [BLENDER_EXECUTABLE] [args...]", options, false);
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
					client();
					break;
				case 3:
					System.exit(0);
		        default:
		            System.out.println("Invalid choice; try again.");
		            break;
			}
		} while (choice != 3);
		
		System.exit(0);
	}

	//
	// MASTER
	//
	
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
	
	private static Collection<Job> readDataFile() {

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
		return jobs;
	}

	private static void writeDataFile(Collection<Job> jobs) {

		Gson gson = new Gson();
		
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
	private static void writeDataFile(Object[] jobs) {

		Gson gson = new Gson();
		
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

	private static void createJob() {

		String file = "";
		String startFrame = "";
		String endFrame = "";
		String format = "";
		
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
		
		while (true) //Loop until we get what we want
		{
			List<String> formats = Arrays.asList("TGA","RAWTGA","JPEG","IRIS","IRIZ","AVIRAW","AVIJPEG","PNG","BMP");
			System.out.print("Enter the File format [TGA,RAWTGA,JPEG,IRIS,IRIZ,AVIRAW,AVIJPEG,PNG,BMP] (Default: JPEG): ");
			format = Scan.nextLine().toUpperCase();

			if (format.length() == 0 || format.equals("JPG")) //Default or common mistake
			{
				format="JPEG";
			}
			if (formats.contains(format))
			{
				break; //Break the loop, we got what we want.
			}
		}

		Job job = new Job(file, startFrame, endFrame, format);
		
		Collection<Job> jobs = readDataFile();

		//System.out.println(gson.toJson(jobs));
		
		jobs.add(job);
		writeDataFile(jobs);
		
	}

	private static void listJobs() {

		Collection<Job> jobs = readDataFile();

		//System.out.println(gson.toJson(jobs));
		System.out.println();
		for (Job item : jobs)
		{
			System.out.println(item);
		}
		
	}

	private static void deleteJob() {

		Collection<Job> jobs = readDataFile();
		System.out.println();
		//System.out.println(gson.toJson(jobs));
		Object[] jobArray = jobs.toArray();

		System.out.println("[0] Cancel");
		for (int i = 1; i <= jobArray.length; i++)
		{
			System.out.println("["+i+"] "+jobArray[i-1]);
		}

		int choice;
		
		while (true) //Loop until we get what we want
		{
			try
			{
				System.out.println("Which Job would you like to delete?");
				choice = Scan.nextInt()-1;
	
				if (choice >= -1 && choice < jobArray.length)
				{
					break; //Break the loop, we got what we want.
				}
			}
			catch(Exception e){Scan.nextLine();/*NaN?*/}
		}
		if (choice != -1)
		{
			Object[] newJobArray = removeElement(jobArray,choice);
	
			writeDataFile(newJobArray);
		}
		//Else just return without doing anything
		
	}

	//
	// CLIENT
	//
	
	private static void client() throws IOException, InterruptedException {

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("\n\nShutting Down...");
				if (proc != null) {proc.destroy();}
				if (currFrame != "-1" && isRendering)
				{
					try
					{
						Files.delete(Paths.get(formatPath(workingDirectory+File.separator+currentJobFile+File.separator+currFrame+"."+currentJobFormat))); 
						System.out.println("Deleted unfinshed frame "+currFrame+" successfully.");
					}
					catch(NoSuchFileException e) 
					{ 
						System.out.println("Error deleting unfinished frame: No such file exists. " + formatPath(workingDirectory+File.separator+currentJobFile+File.separator+currFrame+"."+currentJobFormat));
					}
					catch(IOException e) 
					{ 
						System.out.println("Error deleting unfinished frame: Invalid permissions. " + formatPath(workingDirectory+File.separator+currentJobFile+File.separator+currFrame+"."+currentJobFormat));
					}
	
				}
			}
		});
	
		isRendering = false;
		while (true)
		{
			Collection<Job> jobs = readDataFile();
			int renderedFrames = renderJobs(jobs);
			System.out.println("Rendered "+renderedFrames+" Frame(s)");
			
			if (renderedFrames == 0) //If we didn't render anything
			{
				System.out.println();
				System.out.println("Setting up watchers...");
				
		        final WatchService watchService = FileSystems.getDefault().newWatchService();

	            final Path workingDir = Paths.get(formatPath(workingDirectory+"/render-farm/"));
	            workingDir.register(watchService, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
	            //System.out.println("Watching directory: " + workingDir.toAbsolutePath());
		        for (Job job : jobs) {
		            final Path watchedDir = Paths.get(formatPath(workingDirectory+File.separator+job.file+File.separator));
		            watchedDir.register(watchService, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
		            //System.out.println("Watching directory: " + watchedDir.toAbsolutePath());
		        }

				System.out.println();
				System.out.println();
				System.out.println("Waiting for something to do...");
		        while (true) {
		            final WatchKey watchKey = watchService.take();
		            final Watchable watchable = watchKey.watchable();

		            if (!(watchable instanceof Path)) {
		                throw new AssertionError("The watchable should have been a Path");
		            }

		            final Path directory = (Path) watchable;
		            System.out.println("New event for directory: " + directory);
		            System.out.println("Checking to rerender");
		            break;
		        }
			}
		}
		
	}
	

	private static int renderJobs(Collection<Job> jobs) throws IOException, InterruptedException {
		
		int renderedFrames = 0;
		isRendering = false;
		for (Job job : jobs)
		{
			currentJobFile = job.file;
			currentJobFormat = normalizeFileFormat(job.format);

			boolean needToRender = false;
			for (int i = Integer.parseInt(job.startFrame);i <= Integer.parseInt(job.endFrame);i++)
			{
				currFrame = Integer.toString(i);
				if (!new File(formatPath(workingDirectory+File.separator+job.file+File.separator+currFrame+"."+currentJobFormat)).exists())
				{
					needToRender = true; //If a file does not exist, we need to render it
					break;
				}
			}
			if (!needToRender)
			{
				System.out.println("Skipping job: "+job.file+", no frames need rendering");
				continue;
			}
			System.out.println("\nRendering job: "+job.file);
			
			for (int i = Integer.parseInt(job.startFrame);i <= Integer.parseInt(job.endFrame);i++)
			{
				currFrame = Integer.toString(i);
				if (new File(formatPath(workingDirectory+File.separator+job.file+File.separator+currFrame+"."+currentJobFormat)).exists())
				{
					isRendering = false;
					System.out.println("Skipping existing frame: "+currFrame);
					continue;
				}
				renderedFrames++;
				isRendering = true;
				String[] command = new String[] {
				blenderPath,
				"-b", formatPath(workingDirectory+File.separator+job.file+".blend"),
				"-o", formatPath(workingDirectory+File.separator+job.file+File.separator+"#"),
				"-F", job.format,
				"-x", "1",
				"-f", currFrame};
		
				proc = Runtime.getRuntime().exec(command);
		
				// Read the output
		
				BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		
				String line = "";
				String currFrameRegex = "Fra:([0-9]+)";
				Pattern currFramePattern = Pattern.compile(currFrameRegex);
				Matcher m;

				if (!basicMode)
				{
					while((line = reader.readLine()) != null) {
						//System.out.println(currFrame);
						m = currFramePattern.matcher(line);
						if (m.find()) {
							System.out.print("\r"+line+"\n");
							System.out.print("As of xx:xx:xx, X jobs containing X frames remaining"); // TODO: extra mode
						}
						else
						{
							System.out.print("\n"+line);
						}
					}
				}
				else
				{

					String blenderRenderRegex = "Part ([0-9]+)-([0-9]+)";
					Pattern blenderRenderPattern = Pattern.compile(blenderRenderRegex);
					Matcher blenderRenderMatch;
					
					String cyclesRenderRegex = "Tile ([0-9]+)/([0-9]+), Sample ([0-9]+)/([0-9]+)";
					Pattern cyclesRenderPattern = Pattern.compile(cyclesRenderRegex);
					Matcher cyclesRenderMatch;
					
					try (ProgressBar pb = new ProgressBar("Rendering frame "+currFrame, -1, ProgressBarStyle.ASCII)) {
						while((line = reader.readLine()) != null) {
							//System.out.println(currFrame);
							m = currFramePattern.matcher(line);
							if (m.find()) {
								blenderRenderMatch = blenderRenderPattern.matcher(line);
								cyclesRenderMatch = cyclesRenderPattern.matcher(line);
								if (blenderRenderMatch.find()) {
									pb.stepTo(Long.parseLong(blenderRenderMatch.group(1)));
								    pb.maxHint(Long.parseLong(blenderRenderMatch.group(2)));
								}
								else if (cyclesRenderMatch.find()) {
									pb.stepTo(Long.parseLong(cyclesRenderMatch.group(1))*Long.parseLong(cyclesRenderMatch.group(4))+Long.parseLong(cyclesRenderMatch.group(3)));
								    pb.maxHint(Long.parseLong(cyclesRenderMatch.group(2))*Long.parseLong(cyclesRenderMatch.group(4)));
								}
							}
						}
					}
				}
		
				proc.waitFor();
			}
		}
		currFrame = "-1";
		return renderedFrames;
	}

}
