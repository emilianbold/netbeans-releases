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
package org.netbeans.modules.xslt.project.anttasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.types.Reference;

import java.util.StringTokenizer;

import java.util.logging.Logger;

import org.netbeans.modules.xslt.project.CommandlineXsltProjectXmlCatalogProvider;

/**
 * Ant task wrapper which invokes the JBI Generation task
 * @author Vitaly Bychkov
 * @author Sreenivasan Genipudi
 */
public class GenerateJBIDescriptor extends org.apache.tools.ant.Task {
 
    /**
     * Source directory
     */
    private String mSourceDirectory = null;
    /**
     * Build directory
     */
    private String mBuildDirectory = null;    
    /**
     * Project classpath
     */
    private String mProjectClassPath= null;
    /**
     * Classpath Reference
     */
    private Reference m_ref = null;
    
    /**
     * Logger instance
     */
    private Logger logger = Logger.getLogger(GenerateJBIDescriptor.class.getName());    

    public GenerateJBIDescriptor() {
    }
    
    /**
     * Set the classpath reference
     * @param ref Classpath Reference
     */
    public void setClasspathRef(Reference ref) {
        this.m_ref = ref;
    }
    
    /**
     * Set the build directory
     * @param buildDir build directory
     */
    public void setBuildDirectory(String buildDir) {
        mBuildDirectory = buildDir;
    }

    /**
     * Get the build directory
     * @return String value of the build directory
     */
    public String getBuildDirectory() {
        return mBuildDirectory;
    }

    /**
     * Set the source directory
     * @param srcDir source directory
     */
    public void setSourceDirectory(String srcDir) {
        this.mSourceDirectory = srcDir;
    }
    
    /**
     * Get the source directory
     * @return String value of the source directory
     */
    public String getSourceDirectory() {
        return this.mSourceDirectory;
    }
    
    /**
     * Set the project classpath
     * @param projectClassPath Project classpath
     */
    public void setProjectClassPath(String projectClassPath) {
        this.mProjectClassPath = projectClassPath;
    }
        
    
    /**
     * Invoke the task that generates the JBI.xml
     */
    public void execute() throws BuildException { 
        if(this.mSourceDirectory == null) {
            throw new BuildException("No directory is set for source files.");
        }

        File sourceDirectory = new File(this.mSourceDirectory);
        File buildDirectory = new File(this.mBuildDirectory);

        //read project classpath
        //TODO: refactor this to use wsdl classpath
        List<File> projectDirs = new ArrayList<File>();
        if(this.mProjectClassPath != null
                && !this.mProjectClassPath.trim().equals("")
                && !this.mProjectClassPath.trim().equals("${javac.classpath}")) {
            StringTokenizer st = new StringTokenizer(this.mProjectClassPath, ";");
            while (st.hasMoreTokens()) {
                String spath = st.nextToken();
                try {

                    File sFile =  new File(sourceDirectory.getParentFile().getCanonicalPath() + File.separator + spath);

                    File srcFolder = new File(sFile.getParentFile().getParentFile().getCanonicalFile(), "src");
                    projectDirs.add(srcFolder);
                } catch(Exception ex) {
                    throw new BuildException("Failed to create File object for dependent project path "+ spath);
                }
            }
        }

        //find the owner project
        if(sourceDirectory != null) {
            List<File> srcList = new ArrayList<File>();
            srcList.add(sourceDirectory);
            if (buildDirectory != null) {
                srcList.add(buildDirectory);
            }

            CommandlineXsltProjectXmlCatalogProvider.getInstance().setSourceDirectory(this.mSourceDirectory);
            JBIGenerator generator = new JBIGenerator(projectDirs, srcList, mSourceDirectory, mBuildDirectory);
            generator.generate();
        }

    }
    
    public static void main(String[] args) {
        GenerateJBIDescriptor dd = new GenerateJBIDescriptor();

    }
}
