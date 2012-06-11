package info.iamkebo.somniloquy;

import java.io.File;

import android.content.Context;

public class Utils {
	public static String[] getFileList(String dir){
		String[] result = null;
		File file = new File(dir);
		if(file.isDirectory()){
			result = file.list();
		}
		
		return result;
	}
	
	public static boolean removeFile(String filepath){
		File f = new File(filepath);
		if(f.isFile()){
			return f.delete();
		}
		return false;
	}
}
