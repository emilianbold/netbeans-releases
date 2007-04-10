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
import java.util.HashSet;
import java.util.Set;

import java.util.logging.Logger;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.types.FileSet;

import org.netbeans.modules.xslt.project.CommandlineXsltProjectXmlCatalogProvider;

public class PackageCatalogArtifacts extends org.apache.tools.ant.Task {

    private static final String dirSep = "/";
    public static HashSet mValidRetrievedUriSet = new HashSet();
    // Member variable representing build directory
     /**
      * Logger instance
      */
     private Logger logger = Logger.getLogger(PackageCatalogArtifacts.class.getName());    

    /**
     * Build directory
     */
    private String mBuildDirectoryPath = null;

    public PackageCatalogArtifacts() {
    }



    
    public void doCopy(String projectCatalogPath, File buildDir) {
        projectCatalogPath = projectCatalogPath.replace('\\','/');
        File localCatalogFile = new File(CommandlineXsltProjectXmlCatalogProvider.getInstance().getProjectWideCatalog());
        String localCatalogPath = null;
        if (!localCatalogFile.exists() ) {
            return;
        } else {
            localCatalogPath =  localCatalogFile.getAbsolutePath().replace('\\','/');
        }

        File catalogFile = new File(projectCatalogPath);
        if (catalogFile.exists() && catalogFile.length() > 0) {
            Set<String> setOfURIs = null;
            XsltProjectCatalogReader bpProjectCatRdr = null;
            XsltProjectCatalogReader bpLocalCatRdr = null;
            try {
                bpProjectCatRdr = new XsltProjectCatalogReader(projectCatalogPath);
                bpLocalCatRdr = new XsltProjectCatalogReader(localCatalogPath);
                doCopy(buildDir,bpProjectCatRdr,  bpLocalCatRdr);
            } catch (Throwable ex) {
                logger.fine(ex.getMessage());
            }
        }
        
    }

    private void doCopy(File mBuildDir,XsltProjectCatalogReader projectCtlg, XsltProjectCatalogReader localCtlg ) throws Exception {
        
        ArrayList <String> listOfProjectNS = projectCtlg.getListOfNamespaces();
        ArrayList <String> listOfProjoectURIs = projectCtlg.getListOfLocalURIs();
        
        ArrayList <String> listOfLocalURIs = localCtlg.getListOfLocalURIs();
        ArrayList <String> listOfLocalNSs = localCtlg.getListOfNamespaces();

        Copy copyOp = new Copy(); 
        String metaInfDir = mBuildDir.getAbsolutePath()+ dirSep +"META-INF";
        File metaInfFile = new File(metaInfDir);
        if (!metaInfFile.exists()) {
            metaInfFile.mkdirs();
        }
        org.apache.tools.ant.Project packProject = new org.apache.tools.ant.Project();
        packProject.init();             
        copyOp.setProject(packProject);
        int localIndex = -1;
        int projIndx = -1;
        int projectIndex = -1;
        String localURILoc = null;
        String prjURILoc = null;
        File localURIFile = null;
        String projURLoc = null;
        for (String ns: listOfLocalNSs) {
            //Get the index of the NS in Project catalog
            projIndx =  projectCtlg.locateNS(ns);
            //If the entry is not found in project catalog leave it
            if ( projIndx == -1) {
                continue;
            }
           //Check the Namespace entry in Local Catalog (retreived\catalog.xml)
            localIndex = localCtlg.locateNS(ns);
            //If found, get the URI Location from local catalog
            localURILoc= (String)listOfLocalURIs.get(localIndex);
            prjURILoc = (String) listOfProjoectURIs.get(projIndx);
            prjURILoc = prjURILoc.replace('\\','/');
            
            localURIFile = new File(localURILoc);
            int delimIndx = -1;
            String localURIFileParentDir = localURIFile.getParent();
            if (localURIFileParentDir != null) {
                localURILoc = localURIFileParentDir.replace('\\','/');
            } else {
                localURILoc = localURIFile.getAbsolutePath().replace('\\','/');
                delimIndx = localURILoc.lastIndexOf("/");
                if (delimIndx > 0) {
                    localURILoc = localURILoc.substring(0,delimIndx);
                } else {
                    continue;
                }
            }
            //Set the destination Dir
            copyOp.setTodir(new File(metaInfDir+dirSep+localURILoc));      
            copyOp.setOverwrite(true);
    
            FileSet fs = null;
            File projectDir = mBuildDir.getParentFile();
            if (projectDir == null) {
                projectDir = new File(mBuildDir.getAbsolutePath()+dirSep+"..");
            }
            File deleteDir =null;
            boolean bDelete = true;
            copyOp.setFile(new File(projectDir+dirSep+prjURILoc));
            copyOp.execute();
            
        }

    }
    

}
