package com.riktamtech.android.ratethisstc.dao;

public class AchievementDAO
{
	public String badge;
	public int winningRate,drawRate,totalRated,totalUploaded;
	public AchievementDAO(String badge, int winningRate, int drawRate, int totalRated, int totalUploaded)
	{
		super();
		this.badge = badge;
		this.winningRate = winningRate;
		this.drawRate = drawRate;
		this.totalRated = totalRated;
		this.totalUploaded = totalUploaded;
	}
	public AchievementDAO()
	{
	}
}
