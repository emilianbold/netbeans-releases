package org.netbeans.qa.form;

import java.io.*;

public class VisualDevelopmentUtil {
    
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
    
}
