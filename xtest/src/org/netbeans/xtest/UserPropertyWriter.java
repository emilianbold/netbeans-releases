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

/**
 * @author lm97939
 */
public class UserPropertyWriter extends Task {

    private String prefix;
    private File file;
    private String delim;
    
    public void setPropertyPrefix(String p) {
        prefix = p;
    }
    
    public void setFile(File f) {
        file = f;
    }
    
    public void setDelimiter(String d) {
        delim = d ;
    }

    public void execute() throws BuildException {
        final String HEADER = "Properties passed to test";
        Properties properties = new Properties();
        
        if (file == null) throw new BuildException("Attribute 'file' is empty.", location);
        if (prefix == null) 
            log("No propertyPrefix set. All properties will be written to file.");
        if (delim == null)
            log("No delim set. Property will be written without cutting.");
        
        Hashtable table = project.getProperties();
        Enumeration enum = table.keys();
        while (enum.hasMoreElements()) {
            String key = (String) enum.nextElement();
            if (prefix == null || key.startsWith(prefix)) {
                if (delim == null) 
                    properties.setProperty(key,project.getProperty(key));
                else {
                    int i = key.indexOf(delim);
                    if (i != -1) 
                        properties.setProperty(key.substring(i+1),project.getProperty(key));
                    else 
                        log("WARNING: Property name "+key+" doesn't contain required delimiter "+delim, Project.MSG_WARN);
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
