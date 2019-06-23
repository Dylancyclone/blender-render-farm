package com.lathrum.blender_render_farm;

public class Job {
	public String file = "";
	public String startFrame = "";
	public String endFrame = "";
	public String format = "";
	
	public Job(String file, String startFrame, String endFrame, String format)
	{
		this.file = file;
		this.startFrame = startFrame;
		this.endFrame = endFrame;
		this.format = format;
	}
	
	public String toString()
	{
		return "File: " + this.file + ", Starting Frame: " + this.startFrame +", Ending Frame: " + this.endFrame+", Format: " + this.format;
	}
}
