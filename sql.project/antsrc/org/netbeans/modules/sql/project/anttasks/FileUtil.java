package org.netbeans.modules.sql.project.anttasks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



/**
 * @author Sujit Biswas
 *
 */
public class FileUtil {

	public static void copy(byte[] input, OutputStream output) throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(input);
		FileUtil.copy(in, output);
	}

	public static void copy(InputStream input, OutputStream output) throws IOException {
		byte[] buf = new byte[1024 * 4];
		int n = 0;
		while ((n = input.read(buf)) != -1) {
			output.write(buf, 0, n);
		}
		output.flush();
	}

}
