/*
 * UserPropertyWriter.java
 *
 * Created on November 12, 2001, 4:30 PM
 */

package org.netbeans.xtest;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import java.util.Properties;
import java.io.File;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;

/**
 * @author lm97939
 */
public class UserPropertyWriter extends Task {

    private String prefix;
    private File file;
    private String attribs;
    
    public void setPropertyPrefix(String p) {
        prefix = p;
    }
    
    public void setFile(File f) {
        file = f;
    }
    
    public void setAttribs(String a) {
        attribs = a;
    }

    public void execute() throws BuildException {
        final String HEADER = "Properties passed to test";
        final String PREFIX = "xtest.userdata";
        Properties properties = new Properties();
        
        if (file == null) throw new BuildException("Attribute 'file' is empty.", location);
        if (attribs == null) throw new BuildException("Attribute 'attribs' is empty.", location);
        if (prefix == null) 
            log("No propertyPrefix set. All properties will be written to file.");
        
        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();
        
        Hashtable table = project.getProperties();
        Enumeration enum = table.keys();
        while (enum.hasMoreElements()) {
            String key = (String) enum.nextElement();
            StringTokenizer attrtokens = new StringTokenizer(attribs,","); 
            while (attrtokens.hasMoreTokens()) {
                String attr = attrtokens.nextToken();
                if (prefix == null || key.startsWith(prefix+"|") || key.startsWith(PREFIX+"("+attr+")|")) {
                      int i = key.indexOf("|");
                      if (prefix == null || i == -1)
                          properties.setProperty(key,project.getProperty(key));
                      else 
                          properties.setProperty(key.substring(i+1),project.getProperty(key));
                      break;
                }
            }
        }
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            properties.store(bos,HEADER);
            bos.close();
        }
        catch (java.io.IOException e) { throw new BuildException(e,location); }

    }

}
