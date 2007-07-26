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
package org.netbeans.modules.bpel.project.anttasks;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.netbeans.modules.bpel.project.CommandlineBpelProjectXmlCatalogProvider;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Reference;

/**
 * Generates JBI Descriptor
 * @author Sreenivasan Genipudi
 */
public class GenerateJBIDescriptor extends Task {
    private String mSourceDirectory = null;
    private String mBuildDirectory = null;    
    private String mProjectClassPath= null;
    
    public GenerateJBIDescriptor() {}
    
    public void setBuildDirectory(String buildDir) {
        mBuildDirectory = buildDir;
    }

    public void setSourceDirectory(String srcDir) {
        this.mSourceDirectory = srcDir;
    }

    public void setClasspathRef(Reference ref) {
    }    
    
    public String getSourceDirectory() {
        return this.mSourceDirectory;
    }
    
    public void setProjectClassPath(String projectClassPath) {
        this.mProjectClassPath = projectClassPath;
    }
    
    public void execute() throws BuildException {
        if (this.mSourceDirectory == null) {
            throw new BuildException("No directory is set for source files.");
        }
        File sourceDirectory = new File(this.mSourceDirectory);
        ArrayList projectDirs = new ArrayList();

        if (this.mProjectClassPath != null && !this.mProjectClassPath.trim().equals("") && !this.mProjectClassPath.trim().equals("${javac.classpath}")) {
            StringTokenizer st = new StringTokenizer(this.mProjectClassPath, ";");
           
            while (st.hasMoreTokens()) {
                String spath = st.nextToken();
                try {
                    File sFile =  new File(sourceDirectory.getParentFile().getCanonicalPath() + File.separator + spath);
                    File srcFolder = new File(sFile.getParentFile().getParentFile().getCanonicalFile(), "src");
                    projectDirs.add(srcFolder);
                } 
                catch(Exception e) {
                    throw new BuildException("Failed to create File object for dependent project path "+ spath);
                }
            }
        }
        if(sourceDirectory != null) {
            ArrayList srcList = new ArrayList();
            srcList.add(sourceDirectory);
            CommandlineBpelProjectXmlCatalogProvider.getInstance().setSourceDirectory(this.mSourceDirectory);
            JBIGenerator generator = new JBIGenerator(projectDirs, srcList);
            generator.generate(new File(mBuildDirectory));
        }
    }
}
