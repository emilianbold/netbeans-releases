/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.bpel.project.anttasks;

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

import org.netbeans.modules.bpel.project.ApacheResolverHelper;
import org.netbeans.modules.bpel.project.CommandlineBpelProjectXmlCatalogProvider;

public class PackageCatalogArtifacts extends org.apache.tools.ant.Task {

    private static final String dirSep = "/";
    public static HashSet mValidRetrievedUriSet = new HashSet();
    private Logger logger = Logger.getLogger(PackageCatalogArtifacts.class.getName());    
    private String mBuildDirectoryPath = null;

    public PackageCatalogArtifacts() {}
    
    public void doCopy(String projectCatalogPath, File buildDir) {
        projectCatalogPath = projectCatalogPath.replace('\\','/');
        File localCatalogFile = new File(CommandlineBpelProjectXmlCatalogProvider.getInstance().getProjectWideCatalog());
        String localCatalogPath = null;
        if (!localCatalogFile.exists() ) {
            return;
        } else {
            localCatalogPath =  localCatalogFile.getAbsolutePath().replace('\\','/');
        }

        File catalogFile = new File(projectCatalogPath);
        if (catalogFile.exists() && catalogFile.length() > 0) {
            Set<String> setOfURIs = null;
            BpelProjectCatalogReader bpProjectCatRdr = null;
            BpelProjectCatalogReader bpLocalCatRdr = null;
            try {
                bpProjectCatRdr = new BpelProjectCatalogReader(projectCatalogPath);
                bpLocalCatRdr = new BpelProjectCatalogReader(localCatalogPath);
                doCopy(buildDir,bpProjectCatRdr,  bpLocalCatRdr);
            } catch (Throwable ex) {
                logger.fine(ex.getMessage());
            }
        }
        
    }

    private void doCopy(File mBuildDir,BpelProjectCatalogReader projectCtlg, BpelProjectCatalogReader localCtlg ) throws Exception {
        
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
