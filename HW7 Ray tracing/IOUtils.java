package mchhui.raytracing;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class IOUtils {
	public static InputStream getInputStream(String path) {
		return ClassLoader.getSystemResourceAsStream(path);
	}
	
	public static String readText(String path) {
		InputStream input=getInputStream(path);
		BufferedReader reader=new BufferedReader(new InputStreamReader(input,Charset.forName("UTF-8")));
		String result="";
		try {
			while(reader.ready()) {
				result+=reader.readLine()+"\n";
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}
}
