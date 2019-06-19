package com.lathrum.blender_render_farm;

public class Job {
	private String file = "";
	private String startFrame = "";
	private String endFrame = "";
	
	public Job(String file, String startFrame, String endFrame)
	{
		this.file = file;
		this.startFrame = startFrame;
		this.endFrame = endFrame;
	}
	
	public String toString()
	{
		return "File: " + this.file + ", Starting Frame: " + this.startFrame +", Ending Frame:  " + this.endFrame;
	}
}
