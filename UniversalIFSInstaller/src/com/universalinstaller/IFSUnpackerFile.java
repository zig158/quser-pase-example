package com.universalinstaller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class IFSUnpackerFile
{
	public File file;
	public File ifsFile;
	public Boolean isZip;
	public Boolean replace;
	public Boolean remove;

	protected IFSUnpackerFile(){}

	public IFSUnpackerFile(File input, File ifsfile, boolean zip, boolean replace, boolean remove)
	{
		this.file = input;
		this.ifsFile = ifsfile;
		this.isZip = zip;
		this.replace = replace;
		this.remove = remove;
	}

	public static List<IFSUnpackerFile> load() throws IFSUnpackerException
	{
		File configFile = new File("files.json");
		if(!configFile.isFile())
			throw new IFSUnpackerException(String.format("Config File %s Not Found", configFile.getAbsolutePath()));

		try{
			String json = new String(Files.readAllBytes(configFile.toPath()),"UTF8");
			return new Gson().fromJson(json, new TypeToken<List<IFSUnpackerFile>>(){}.getType());
		} catch (IOException | JsonSyntaxException e){
			throw new IFSUnpackerException(String.format("Error Reading Config File %s", configFile.getAbsolutePath()),e);
		}
	}
}
