package com.shiyou.tryapp2.app.photo.data;

import java.io.Serializable;

import android.extend.data.BaseData;

public class PhotoData extends BaseData implements Serializable
{
	private static final long serialVersionUID = -2822907769742108524L;

	public int id;
	public String path;

	public PhotoData()
	{
	}

	public PhotoData(int id, String path)
	{
		this.id = id;
		this.path = path;
	}

	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("PhotoData: ");
		sb.append("id=").append(id).append("; ");
		sb.append("path=").append(path);
		return sb.toString();
	}
}
