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
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.zip.ZipFile;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

/** Invokes signature tests.
 * @author Michal Zlámal
 */
public class Sigtest extends Task {

    File fileName;
    Path classpath;
    String packages;
    ActionType action;
    File sigtestJar;
    boolean failOnError = true;
    
    public void setFileName(File f) {
        fileName = f;
    }
    
    public void setPackages(String s) {
        packages = s;
    }

    public void setAction(ActionType s) {
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

    @Override
    public void execute() throws BuildException {
        if (fileName == null) {
            throw new BuildException("FileName has to filed", getLocation());
        }
        if (packages == null) {
            throw new BuildException("Packages has to filed", getLocation());
        }
        if (action == null) {
            throw new BuildException("Action has to filed", getLocation());
        }
        if (classpath == null) {
            throw new BuildException("Classpath has to filed", getLocation());
        }
        if (sigtestJar == null) {
            throw new BuildException("SigtestJar has to filed", getLocation());
        }
        
        if (packages.equals("-")) {
            log("No public packages, skipping");
            return;
        }
        
        if (!sigtestJar.exists()) {
            throw new BuildException("Cannot find JAR with testing infrastructure: " + sigtestJar);
        }
        
        try {
            ZipFile zip = new ZipFile(sigtestJar);
            String c1 = "com/sun/tdk/signaturetest/Setup.class";
            if (zip.getEntry(c1) != null) {
                zip.close();
                tdk();
                return;
            }
            String c2 = "org/netbeans/apitest/Main.class";
            if (zip.getEntry(c2) != null) {
                zip.close();
                apitest();
                return;
            }
            zip.close();
            
            throw new BuildException("Cannot find " + c1 + " nor " + c2 + " in " + sigtestJar);
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
        
    }
    
    private void tdk() {
        Java java = (Java) getProject().createTask("java");
        Path sigtestPath = new Path(getProject());
        sigtestPath.setLocation(sigtestJar);
        
        java.setClasspath(sigtestPath);
        String a = null;
        if ("strictcheck".equals(action.getValue())) { // NOI18N
            a = "SignatureTest"; // NOI18N
        }
        if ("generate".equals(action.getValue())) { // NOI18N
            a = "Setup"; // NOI18N
        }
        if (a == null) {
            throw new BuildException("Unsupported action " + action);
        }
        java.setClassname("com.sun.tdk.signaturetest." + a);
        Commandline.Argument arg;
        arg = java.createArg();
        arg.setValue("-FileName");
        arg = java.createArg();
        arg.setValue(fileName.getAbsolutePath());
        arg = java.createArg();
        arg.setValue("-Classpath");
        arg = java.createArg();
        arg.setPath(classpath);
        
        File outputFile = null;
        String s = getProject().getProperty("sigtest.output.dir");
        if (s != null) {
            File dir = getProject().resolveFile(s);
            dir.mkdirs();
            outputFile = new File(dir, fileName.getName().replace(".sig", "").replace("-", "."));
            log(outputFile.toString());
            java.setOutput(outputFile);
            java.setFork(true);
        }
        
        
        arg = java.createArg();
        arg.setLine("-static");
        log("Packages: " + packages);
        StringTokenizer packagesTokenizer = new StringTokenizer(packages,",");
        while (packagesTokenizer.hasMoreTokens()) {
            String p = packagesTokenizer.nextToken().trim();
            String prefix = "-PackageWithoutSubpackages "; // NOI18N
            //Strip the ending ".*"
            int idx = p.lastIndexOf(".*");
            if (idx > 0) {
                p = p.substring(0, idx);
            } else {
                idx = p.lastIndexOf(".**");
                if (idx > 0) {
                    prefix = "-Package "; // NOI18N
                    p = p.substring(0, idx);
                }
            }
            
            arg = java.createArg();
            arg.setLine(prefix + p);
        }
        int returnCode = java.executeJava();
        if (returnCode != 95) {
            if (failOnError && outputFile == null) {
                throw new BuildException("Signature tests return code is wrong (" + returnCode + "), check the messages above", getLocation());
            }
            else {
                log("Signature tests return code is wrong (" + returnCode + "), check the messages above");
            }
        } else {
            if (outputFile != null) {
                outputFile.delete();
            }
        }
    }
    
    private void apitest() {
        Java java = (Java) getProject().createTask("java");
        java.setFork(true);
        Path sigtestPath = new Path(getProject());
        sigtestPath.setLocation(sigtestJar);
        
        
        java.setClasspath(sigtestPath);
        java.setClassname("org.netbeans.apitest.Main");
        Commandline.Argument arg;
        arg = java.createArg();
        arg.setValue("-FileName");
        arg = java.createArg();
        arg.setValue(fileName.getAbsolutePath());
        arg = java.createArg();
        arg.setValue("-Classpath");
        arg = java.createArg();
        arg.setPath(classpath);
        if (action.getValue().equals("generate")) {
            arg = java.createArg();
            arg.setValue("-setup");
        } else if (action.getValue().equals("check")) {
            // no special arg for check
        } else if (action.getValue().equals("strictcheck")) {
            arg = java.createArg();
            arg.setValue("-maintenance");
        } else {
            throw new BuildException("Unknown action: " + action);
        }
        
        
        arg = java.createJvmarg();
        arg.setValue("-XX:PermSize=32m");
        arg = java.createJvmarg();
        arg.setValue("-XX:MaxPermSize=200m");
            
        File outputFile = null;
        String s = getProject().getProperty("sigtest.output.dir");
        if (s != null) {
            File dir = getProject().resolveFile(s);
            dir.mkdirs();
            outputFile = new File(dir, fileName.getName().replace(".sig", "").replace("-", "."));
            log(outputFile.toString());
            java.setOutput(outputFile);
        }
        
        
        log("Packages: " + packages);
        StringTokenizer packagesTokenizer = new StringTokenizer(packages,",");
        while (packagesTokenizer.hasMoreTokens()) {
            String p = packagesTokenizer.nextToken().trim();
            String prefix = "-PackageWithoutSubpackages "; // NOI18N
            //Strip the ending ".*"
            int idx = p.lastIndexOf(".*");
            if (idx > 0) {
                p = p.substring(0, idx);
            } else {
                idx = p.lastIndexOf(".**");
                if (idx > 0) {
                    prefix = "-Package "; // NOI18N
                    p = p.substring(0, idx);
                }
            }
            
            arg = java.createArg();
            arg.setLine(prefix + p);
        }
        int returnCode = java.executeJava();
        if (returnCode != 0) {
            if (failOnError && outputFile == null) {
                throw new BuildException("Signature tests return code is wrong (" + returnCode + "), check the messages above", getLocation());
            }
            else {
                log("Signature tests return code is wrong (" + returnCode + "), check the messages above");
            }
        } else {
            if (outputFile != null) {
                outputFile.delete();
            }
        }
    }

    public static final class ActionType extends EnumeratedAttribute {
        public String[] getValues () {
            return new String[] { 
                "generate",
                "check",
                "strictcheck",
            };
        }
    }

}
