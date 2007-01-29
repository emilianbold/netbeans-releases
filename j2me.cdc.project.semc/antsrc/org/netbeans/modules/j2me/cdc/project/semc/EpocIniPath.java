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

package org.netbeans.modules.j2me.cdc.project.semc;

// IMPORTANT! You need to compile this class against ant.jar. So add the
// JAR ide5/ant/lib/ant.jar from your IDE installation directory (or any
// other version of Ant you wish to use) to your classpath. Or if
// writing your own build target, use e.g.:
// <classpath>
//     <pathelement location="${ant.home}/lib/ant.jar"/>
// </classpath>

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

/**
 * @author suchys
 */
public class EpocIniPath extends Task {
    
    private File file;
    private File workingdir;    
    
    public void execute() throws BuildException {
        if (file == null){
            throw new BuildException("File is null");
        }
        if (!file.exists()){
            throw new BuildException("File epoc.ini does not exists");            
        }
        BufferedReader br = null;
        StringBuffer newFileContent = new StringBuffer();
        boolean found = false;
        String lineOrig = "";
        try {            
            Pattern patternDriveRemapping = Pattern.compile("^\\s*_epoc_drive_d +.*$"); 
            br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null){
                if ( patternDriveRemapping.matcher(line).matches()){
                    found = true;
                    lineOrig = line;
                    line = "#" + line; //comment out 
                }
                newFileContent.append(line);
                newFileContent.append("\r\n");
            }
        } catch (FileNotFoundException ex) {
            throw new BuildException(ex);
        } catch (IOException ex) {
            throw new BuildException(ex);
        } finally {
            if (br != null)                
                try {
                    br.close();
                } catch (IOException ex) {
                    throw new BuildException(ex);
                }
        }
        
        if (found && !file.canWrite()){
            log("epoc.ini can not be written! Make it writable or comment out line \'" + lineOrig + "\'", Project.MSG_WARN);
            throw new BuildException("epoc.ini can not be written!");
        }
        
        if (found){
            log("epoc.ini includes \'" + lineOrig + "\' remapping of epoc system path! Commenting out as it can cause execution trouble for certain application types", Project.MSG_WARN);
            BufferedOutputStream bos = null;
            try {            
                bos = new BufferedOutputStream(new FileOutputStream(new File(file.getAbsolutePath())));
                bos.write(newFileContent.toString().getBytes());
            } catch (FileNotFoundException ex) {
                throw new BuildException(ex);
            } catch (IOException ex) {
                throw new BuildException(ex);
            } finally {
                if (bos != null)                
                    try {
                        bos.close();
                    } catch (IOException ex) {
                        throw new BuildException(ex);
                    }
            }
        }
   }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getWorkingdir() {
        return workingdir;
    }

    public void setWorkingdir(File workingdir) {
        this.workingdir = workingdir;
    }
    
}
