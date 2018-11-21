package com.shiyou.tryapp2.app.photo.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.extend.data.BaseData;

public class PhotoAlbumData extends BaseData implements Serializable
{
	private static final long serialVersionUID = 2838135570871922734L;

	public String id;
	public String name;
	public String path;
	public List<PhotoData> photoList = new ArrayList<PhotoData>();
}
