/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.jspcompiler;

import java.io.*;

/**
 * This class reads SMAP information from files.
 * @author  mg116726
 */
public class SmapFileReader implements SmapReader {
        
    private File file;
    
    public SmapFileReader(java.io.File file) {
        this.file = file;
    }
    
    public String toString() {
        if (file != null) return file.toString();
        return null;
    }
    
    public String readSmap() {
        if (file != null) {
            try {
                FileReader fr = new FileReader(file);
                LineNumberReader lnr = new LineNumberReader(fr);
                String line = "";
                String out = "";
                while ((line = lnr.readLine()) != null) {
                    out = out.concat(line);
                    out = out.concat("\n");
                }
                return out;
            } catch (FileNotFoundException fne) {
                return null;
            } catch (IOException ioe) {
                return null;
            }
        }
        return null;
    }
 

}
