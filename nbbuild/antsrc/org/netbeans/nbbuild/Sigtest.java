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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.File;
import java.util.StringTokenizer;
import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.*;

/**
 * @author michal
 */
public class Sigtest extends Task {

    File fileName;
    Path classpath;
    String packages;
    String additionArgs;
    String action;
    File sigtestJar;
    boolean failOnError = true;
    
    public void setFileName(File f) {
        fileName = f;
    }
    
    public void setPackages(String s) {
        packages = s;
    }

    public void setAdditionalArgs(String s) {
        additionArgs = s;
    }

    public void setAction(String s) {
        action = s;
    }

    public void setClasspath(Path p) {
        if (classpath == null) {
            classpath = p;
        } else {
            classpath.append(p);
        }
    }
    public Path createClasspath () {
        if (classpath == null) {
            classpath = new Path(getProject());
        }
        return classpath.createPath();
    }
    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }

    public void setSigtestJar(File f) {
        sigtestJar = f;
    }

    public void setFailOnError(boolean b) {
        failOnError = b;
    }

    public void execute() throws BuildException {
        if (fileName == null) throw new BuildException("FileName has to filed", getLocation());
        if (packages == null) throw new BuildException("Packages has to filed", getLocation());
        if (action == null) throw new BuildException("Action has to filed", getLocation());
        if (classpath == null) throw new BuildException("Classpath has to filed", getLocation());
        if (sigtestJar == null) throw new BuildException("SigtestJar has to filed", getLocation());
        
        if (packages.equals("-")) {
            log("No public packages, skipping");
            return;
        }
        
        Java java = (Java) getProject().createTask("java");
        Path sigtestPath = new Path(getProject());
        sigtestPath.setLocation(sigtestJar);
        
        java.setClasspath(sigtestPath);
        java.setClassname("com.sun.tdk.signaturetest." + action);
        Commandline.Argument arg;
        arg = java.createArg();
        arg.setValue("-FileName");
        arg = java.createArg();
        arg.setValue(fileName.getAbsolutePath());
        arg = java.createArg();
        arg.setValue("-Classpath");
        arg = java.createArg();
        arg.setPath(classpath);
        if (additionArgs != null) {
            arg = java.createArg();
            arg.setLine(additionArgs);
        }
        log("Packages: " + packages);
        StringTokenizer packagesTokenizer = new StringTokenizer(packages,",");
        while (packagesTokenizer.hasMoreTokens()) {
            String p = packagesTokenizer.nextToken().trim();
            //Strip the ending ".*"
            if (p.lastIndexOf(".*") > 0 )
                p = p.substring(0,p.lastIndexOf(".*"));
            
            arg = java.createArg();
            arg.setLine("-PackageWithoutSubpackages " + p);
        }
        int returnCode = java.executeJava();
        if (returnCode != 95) {
            if (failOnError) throw new BuildException("Signature tests return code is wrong (" + returnCode + "), check the messages above",getLocation());
            else log("Signature tests return code is wrong (" + returnCode + "), check the messages above");
        }
    }

}
