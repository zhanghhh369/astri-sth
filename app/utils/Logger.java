package utils;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class Logger {
	public static void write(String log) {
		String filepath = "/home/data/streamsetlog/streamset.log";
		File file = new File(filepath);
		Writer out = null;
		try {
			out = new FileWriter(file, true);
			out.write("\n" + log);
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

