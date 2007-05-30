package org.netbeans.qa.form;

import java.io.*;

public class VisualDevelopmentUtil {
    public static String JAVA_VERSION = System.getProperty("java.version");
    
    public static String readFromFile(String filename) throws IOException   {
        File f = new File(filename); 
        int size = (int) f.length();        
        int bytes_read = 0;
        FileInputStream in = new FileInputStream(f);
        byte[] data = new byte [size];
        while(bytes_read < size)
            bytes_read += in.read(data, bytes_read, size-bytes_read);
        return new String(data);
    }
    
    public static void copy(File src, File dst) throws IOException {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }

    
    
}
