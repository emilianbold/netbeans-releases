/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * UnzipExt.java
 *
 * Created on February 11, 2002, 6:25 PM
 */

package org.netbeans.xtest.driver;


import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import java.io.*;
import java.util.zip.*;


/**
 * @author lm97939
 */
public class UnzipExt extends Task {

    /** Holds value of property setSrc. */
    private File src;

    /** Holds value of property dest. */
    private File dest;

    /** Holds value of property files. */
    private String files = "";

    /** Holds value of property shift. */
    private boolean shift = false;
    
    public void execute() throws BuildException {
        unpackZip(src,dest,files);
    }

        
    public void unpackZip(File zipFile, File destFile, String fileToUnpack) {
        
        if (!destFile.exists()) {
            destFile.mkdirs();
        }        
        if (!zipFile.exists()) {
            throw new BuildException("ZipFile "+zipFile.getName()+" does not exist");
        }
        FileInputStream fis = null;
        ZipInputStream zis = null;
        FileOutputStream out = null;
        try {
            fis = new FileInputStream(zipFile);
            zis = new ZipInputStream(fis);
            ZipEntry entry = zis.getNextEntry();
                
            log("Expanding: " + zipFile + " into " + destFile, Project.MSG_INFO);
            while (entry != null) {
                String entryName = entry.getName();
                if (entryName.startsWith(fileToUnpack)) {
                    String outFilename = entryName; 
                    if (shift) {
                        if (entryName.length() > fileToUnpack.length()) 
                            outFilename = entryName.substring(fileToUnpack.length());
                        else 
                            outFilename = "";
                        if (outFilename.startsWith("/")) 
                            if (outFilename.length() > 1)
                                outFilename = outFilename.substring(1);
                            else 
                                outFilename = "";
                    }
                    File outFile = new File(destFile.getAbsolutePath()+File.separator+outFilename);
                    log("Extracting "+outFile.getAbsolutePath(),Project.MSG_VERBOSE);
                    if (entry.isDirectory()) {
                       if (!outFile.exists()) {
                            boolean result = outFile.mkdirs();
                            log("Making directory",Project.MSG_VERBOSE);
                            if (result != true) {
                                // we have problem ---
                                throw new IOException("Directory cannot be created:"+outFilename);
                            }
                       }
                    } else {
                        log("Extracting file",Project.MSG_DEBUG);
                        try {
                            
                            if (!outFile.getParentFile().exists())
                                outFile.getParentFile().mkdirs();
                            out = new FileOutputStream(outFile);
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = zis.read(buffer)) != -1) {
                                out.write(buffer,0,bytesRead);
                            }
                            out.close();
                        } catch (IOException ioe) {
                            // we have problem ...
                            if (out!=null) out.close();
                            if (zis!=null) zis.close();
                            if (fis!=null) fis.close();
                            throw new BuildException(ioe);
                        }
                    }
                }
                // next entry
                entry = zis.getNextEntry();
                
            }
            zis.close();
            fis.close();
            
        } catch (IOException ioe) {
            try {
                if (zis!=null) zis.close();
                if (fis!=null) fis.close();
            } catch (Exception e) {
            }
            throw new BuildException(ioe);
        }
    }
    
    /** Setter for property setSrc.
     * @param setSrc New value of property setSrc.
     */
    public void setSrc(File src) {
        this.src = src;
    }
    
    /** Setter for property dest.
     * @param dest New value of property dest.
     */
    public void setDest(File dest) {
        this.dest = dest;
    }
    
    /** Setter for property files.
     * @param files New value of property files.
     */
    public void setFiles(String files) {
        this.files = files.replace('\\','/');
    }
    
    /** Setter for property shift.
     * @param shift New value of property shift.
     */
    public void setShift(boolean shift) {
        this.shift = shift;
    }
    
}
