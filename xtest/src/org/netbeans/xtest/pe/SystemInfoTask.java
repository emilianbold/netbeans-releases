/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * SystemInfoTask.java
 *
 * Created on November 13, 2001, 12:23 PM
 */

package org.netbeans.xtest.pe;

import org.apache.tools.ant.*;
import org.netbeans.xtest.pe.xmlbeans.*;
import java.io.*;
import org.netbeans.xtest.util.SerializeDOM;

// for getting hostname 
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author  mb115822
 * @version 
 */
public class SystemInfoTask extends Task {

    /** Creates new SystemInfoTask */
    public SystemInfoTask() {
    }    
    
    private File outfile;
    
    public void setOutFile(File outfile) {
        this.outfile = outfile;
    }
    
    
   

    public void execute () throws BuildException {
        log("Generating system info xml");
        SystemInfo si = new SystemInfo();
        try {
            FileOutputStream outStream = new FileOutputStream(this.outfile);            
            SerializeDOM.serializeToStream(si.toDocument(),outStream);
            outStream.close();
        } catch (IOException ioe) {
            log("Cannot save systeminfo:"+ioe);
            ioe.printStackTrace(System.err);
        } catch (Exception e) {
            log("XMLBean exception?:"+e);
            e.printStackTrace(System.err);           
        }
    }
}
