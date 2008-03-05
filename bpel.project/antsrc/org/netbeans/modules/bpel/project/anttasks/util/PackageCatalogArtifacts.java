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
package org.netbeans.modules.bpel.project.anttasks.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.bpel.project.CommandlineBpelProjectXmlCatalogProvider;
import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class PackageCatalogArtifacts {
    
    private Logger logger = Logger.getLogger(
            PackageCatalogArtifacts.class.getName());    
    
    public PackageCatalogArtifacts() {
        // Does nothing
    }
    
    public void doCopy(
            final File sourceDirectory,
            final File buildDirectory) {
        
        CommandlineBpelProjectXmlCatalogProvider.getInstance().
                setSourceDirectory(sourceDirectory.getAbsolutePath());
        
        final File projectCatalogFile = new File(
                CommandlineBpelProjectXmlCatalogProvider.getInstance().
                getProjectWideCatalogForWizard());
        final File localCatalogFile = new File(
                CommandlineBpelProjectXmlCatalogProvider.getInstance().
                getProjectWideCatalog());
        
        final boolean projectCatalogExists = projectCatalogFile.exists() && 
                (projectCatalogFile.length() > 0);
        final boolean localCatalogExists = localCatalogFile.exists() && 
                (localCatalogFile.length() > 0);
        
        if (!projectCatalogExists && !localCatalogExists) {
            return;
        }
        
        final CatalogReader projectCatalogReader;
        final CatalogReader localCatalogReader;
        
        try {
            projectCatalogReader = projectCatalogExists ? 
                new CatalogReader(
                projectCatalogFile.getCanonicalPath()) : null;
            localCatalogReader = localCatalogExists ? 
                new CatalogReader(
                localCatalogFile.getCanonicalPath()) : null;
            
            doCopy(sourceDirectory, 
                    buildDirectory, 
                    projectCatalogReader, 
                    localCatalogReader);
        } catch (Throwable ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
        }
    }
    
    private void doCopy(
            final File sourceDirectory,
            final File buildDirectory,
            final CatalogReader projectCatalog, 
            final CatalogReader localCatalog) throws Exception {
        
        final List<String> listOfProjectNSs = projectCatalog == null ? 
            new ArrayList<String>() : 
            projectCatalog.getListOfNamespaces();
        final List<String> listOfProjectURIs = projectCatalog == null ? 
            new ArrayList<String>() : 
            projectCatalog.getListOfLocalURIs();
        
        final List<String> listOfLocalNSs = localCatalog == null ? 
            new ArrayList<String>() : 
            localCatalog.getListOfNamespaces();
        final List<String> listOfLocalURIs = localCatalog == null ? 
            new ArrayList<String>() : 
            localCatalog.getListOfLocalURIs();
        
        // Now we have constructed the lists of system id -> local uri mappings
        // for both local (./retrieved/catalog.xml) and project (./catalog.xml)
        // catalogs. We will run through the local one first, then through the
        // project one. Note that the project catalog takes precedence of the 
        // local one, i.e. if the same system id exists in both the local 
        // catalog and the project one, the resource will be taken from the 
        // uri in the project catalog.
        
        // Some additional initialization (helper stuff)
        final File metaInfDirectory = new File(buildDirectory, "META-INF");
        metaInfDirectory.mkdirs();
        
        final File projectDirectory = buildDirectory.getParentFile();
        final File catalogFile = new File(metaInfDirectory, "catalog.xml");
        
        final PrintWriter catalogWriter = 
                new PrintWriter(new FileWriter(catalogFile));
        
        catalogWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
        catalogWriter.println("<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\" prefer=\"system\">");
        
        // For local catalog we always copy the resources referenced in it. For
        // project catalog the behavior is a little bit more complicated.
        for (int i = 0; i < listOfLocalNSs.size(); i++) {
            final String ns = listOfLocalNSs.get(i);
            
            // Check whether the same system id exists in the project catalog
            final int indexInProject = projectCatalog.locateNS(ns);
            
            if (indexInProject == -1) {
                final String localUri = listOfLocalURIs.get(i);
                
                Util.copyFile(
                        new File(projectDirectory, localUri), 
                        new File(metaInfDirectory, localUri));
                
                //catalogModel.addURI(new URI(ns), new URI(localUri));
                catalogWriter.println("    <system systemId=\"" + ns + "\" uri=\"" + localUri + "\"/>");
            }
        }
        
        for (int i = 0; i < listOfProjectNSs.size(); i++) {
            final String ns = listOfProjectNSs.get(i);
            String localUri = listOfProjectURIs.get(i);
            
            URI realUri = new URI(localUri);
            
            // If the URI does not have a scheme, it is a path relative to the
            // project catalog file (i.e. the project directory). If it is 
            // contained in the sources directory, it will be copied to the 
            // build directory automatically. Otherwise, we need to copy it 
            // manually (just like for local catalog resources)
            if (realUri.getScheme() == null) {
                if (localUri.startsWith(sourceDirectory.getName() + "/")) {
                    localUri = "../" + localUri.substring(
                            sourceDirectory.getName().length() + 1);
                } else {
                    // TODO
                }
            } else if (realUri.getScheme().equals("nb-uri")) {
                try {
                    Project project = FileOwnerQuery.getOwner(
                            FileUtil.toFileObject(sourceDirectory));

                    final AntProjectHelper antHelper = project.getLookup().
                            lookup(AntProjectHelper.class);
                    final ReferenceHelper referenceHelper = project.getLookup().
                            lookup(ReferenceHelper.class);

                    final DefaultProjectCatalogSupport catalogSupport = 
                            new DefaultProjectCatalogSupport(
                                    project, antHelper, referenceHelper);

                    final FileObject fo = catalogSupport.resolveProjectProtocol(
                            new URI(localUri));
                    final File file = FileUtil.toFile(fo);

                    final Project referencedProject = FileOwnerQuery.getOwner(fo);

                    final String uri = 
                            "dependentProjectsFiles/" + 
                            referencedProject.getProjectDirectory().getName() + 
                            "/" + Util.getRelativePath(FileUtil.toFile(
                            referencedProject.getProjectDirectory()), file);
                    final File target = new File(buildDirectory, uri);

                    Util.copyFile(file, target);

                    localUri = "../" + uri;
                } catch (Exception e) {
                    File[] resolved = resolveProjectUri(projectDirectory, localUri);
                    
                    if ((resolved != null) && resolved[0].exists()) {
                        final String uri = 
                                "dependentProjectsFiles/" + 
                                resolved[1] + 
                                "/" + Util.getRelativePath(resolved[1], resolved[0]);
                        final File target = new File(buildDirectory, uri);

                        Util.copyFile(resolved[0], target);
                        
                        localUri = "../" + uri;
                    } else {
                        localUri = null;
                    }
                }
            }
            
            if (localUri != null) {
                catalogWriter.println("    <system systemId=\"" + ns + "\" uri=\"" + localUri + "\"/>");
            }
        }
        
        catalogWriter.println("</catalog>");
        catalogWriter.flush();
        catalogWriter.close();
    }
    
    private File[] resolveProjectUri(File projectDirectory, String uri) throws IOException {
        //nb-uri:IZ107908_Module2#src/DummySchema.xsd
        String corrected = uri.substring("nb-uri:".length());
        
        String projectName = corrected.substring(0, corrected.indexOf("#"));
        String path = corrected.substring(corrected.indexOf("#") + 1);
        String projectLocation = null;
        
        File properties = new File(projectDirectory, "nbproject/project.properties");
        BufferedReader reader = new BufferedReader(new FileReader(properties));
        
        String key = "project." + projectName + "=";
        
        String line = null;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith(key)) {
                projectLocation = line.substring(key.length());
                break;
            }
        }
        
        reader.close();
        
        if (projectLocation == null) {
            return null;
        } else {
            return new File[] {
                new File(projectDirectory, projectLocation + "/" + path),
                new File(projectDirectory, projectLocation)
            };
        }
    }
}
