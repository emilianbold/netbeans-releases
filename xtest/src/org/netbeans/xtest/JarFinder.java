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
 * JarFinder.java
 *
 * Created on November 26, 2001, 3:46 PM
 */

package org.netbeans.xtest;


import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import java.util.StringTokenizer;
import java.io.File;

/** This task finds files from attribute 'files' in directories from attribute
 * 'dirs' and creates path which writes to property which name is in attribute 'property'.
 *
 * Example:
 *
 * Suppose directory c:\\jemmy contains files jemmy.jar and jelly.jar and
 * directory c:\\jdbcdrivers contains file driver.zip
 *
 * <jar-finder dirs="c:\\jemmy;c:\\jdbcdrivers" files="driver.zip;jemmy.jar;jelly.jar" 
 *             property="myclasspath"/>
 *
 * property myclasspath will contain "c:\\jdbcdrivers\\driver.zip;c:\\jemmy\\jemmy.jar;c:\\jemmy\\jelly.jar"
 *
 * @author lm97939
 */
public class JarFinder extends Task {

    private String dirlist,filelist,property;
    
    public void setDirs(String d) {
        dirlist = d;
    }
    
    public void setFiles(String f) {
        filelist=f;
    }
    
    public void setProperty(String p) {
        property = p;
    }

    public void execute() throws BuildException {
        if (dirlist == null) throw new BuildException("Attribute dirs is empty.");
        if (filelist == null) throw new BuildException("Attribute files is empty.");
        if (property == null) throw new BuildException("Attribute property is empty.");
        
        StringBuffer buffer = new StringBuffer();
        StringTokenizer filetokens = new StringTokenizer(filelist,",;"+File.pathSeparator);
        boolean found = false;
        while (filetokens.hasMoreTokens()) {
            found = false;
            String file = filetokens.nextToken();
            File ffile = getProject().resolveFile(file);
            if (ffile.exists()) {
                found = true;
                if (buffer.length() > 0) buffer.append(File.pathSeparator);
                buffer.append(ffile.getAbsolutePath());
                continue;
            }
            StringTokenizer dirtokens = new StringTokenizer(dirlist,",;"+File.pathSeparator);
            while (dirtokens.hasMoreTokens()) {
                String dir = dirtokens.nextToken();
                File fdir = getProject().resolveFile(dir);
                if (!fdir.exists() || !fdir.isDirectory()) 
                    throw new BuildException("Directory " + fdir.getAbsolutePath() + " not found (from '" + dirlist + "').");
                ffile = new File(fdir,file);
                if (ffile.exists()) {
                    found = true;
                    if (buffer.length() > 0) buffer.append(File.pathSeparator);
                    buffer.append(ffile.getAbsolutePath());
                    break;
                }
            }
            if (found == false) 
                throw new BuildException("File "+file+" was not found in any directory of "+dirlist+".");
        }
        log ("Setting property " + property + " to " + buffer.toString());
        getProject().setProperty(property,buffer.toString());
    }

}
