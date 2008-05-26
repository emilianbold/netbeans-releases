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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.bpel.project.CommandlineBpelProjectXmlCatalogProvider;
import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

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
                getProjectCatalogUri());
        
        final boolean projectCatalogExists = projectCatalogFile.exists() && 
                (projectCatalogFile.length() > 0);
        
        if (!projectCatalogExists) {
            return;
        }
        
        final CatalogReader projectCatalogReader;
        
        try {
            projectCatalogReader = projectCatalogExists ? 
                new CatalogReader(
                projectCatalogFile.getCanonicalPath()) : null;
            
            doCopy(sourceDirectory, 
                    buildDirectory,
                    projectCatalogReader);
        } catch (Throwable ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
        }
    }
    
    private void doCopy(
            final File sourceDirectory,
            final File buildDirectory,
            final CatalogReader projectCatalog) throws Exception {
        
        final List<String> listOfProjectNSs = projectCatalog == null ? 
            new ArrayList<String>() : projectCatalog.getNamespaces();
        final List<String> listOfProjectURIs = projectCatalog == null ? 
            new ArrayList<String>() : projectCatalog.getLocations();
        
        final File metaInfDirectory = new File(buildDirectory, "META-INF");
        metaInfDirectory.mkdirs();
        
        final File projectDirectory = buildDirectory.getParentFile();
        final File catalogFile = new File(metaInfDirectory, "catalog.xml");
        
        final String retrieverPathPrefix = "nbproject/private/cache/";
        final String retrieverPathPrefix2 = "retrieved/";
        
        final PrintWriter catalogWriter = 
                new PrintWriter(new FileWriter(catalogFile));
        
        catalogWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
        catalogWriter.println("<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\" prefer=\"system\">");
        
        for (int i = 0; i < listOfProjectNSs.size(); i++) {
            final String ns = listOfProjectNSs.get(i);
            String localUri = listOfProjectURIs.get(i);
            
            final URI realUri = new URI(localUri);
            
            if (realUri.getScheme() == null) {
                // The URI leads us to the sources directory -- just correct 
                // it and proceed
                if (localUri.startsWith(sourceDirectory.getName() + "/")) {
                    localUri = "../" + localUri.substring(
                            sourceDirectory.getName().length() + 1);
                } 
                
                // The URI leads to the nbproject directory -- it is a
                // resource fetched by the retriever -- copy it
                if (localUri.startsWith(retrieverPathPrefix)) {
                    localUri = localUri.substring(retrieverPathPrefix.length());
                    
                    Util.copyFile(
                            new File(projectDirectory, retrieverPathPrefix + localUri), 
                            new File(buildDirectory, "_" + localUri));
                    
                    localUri = "../_" + localUri;
                }
                
                // The URI leads to the retrieved directory -- it is a
                // resource fetched by the retriever -- copy it
                if (localUri.startsWith(retrieverPathPrefix2)) {
                    Util.copyFile(
                            new File(projectDirectory, localUri), 
                            new File(buildDirectory, "_" + localUri));
                    
                    localUri = "../_" + localUri;
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
                            "_dependent/" + 
                            referencedProject.getProjectDirectory().getName() + 
                            "/" + Util.getRelativePath(FileUtil.toFile(
                            referencedProject.getProjectDirectory()), file);
                    final File target = new File(buildDirectory, uri);
                    
                    Util.copyFile(file, target);
                    
                    processImports(file, target);
                    
                    localUri = "../" + uri;
                } catch (Exception e) {
                    File[] resolved = resolveProjectUri(projectDirectory, localUri);
                    
                    if ((resolved != null) && resolved[0].exists()) {
                        final String uri = 
                                "_dependent/" + 
                                resolved[1] + 
                                "/" + Util.getRelativePath(resolved[1], resolved[0]);
                        final File target = new File(buildDirectory, uri);
                        
                        Util.copyFile(resolved[0], target);
                        
                        processImports(resolved[0], target);
                        
                        localUri = "../" + uri;
                    } else {
                        localUri = null;
                    }
                }
            }
            if (localUri != null) {
                // # 130092
                localUri = localUri.replace("\\", "/"); // NOI18N
                catalogWriter.println("    <system systemId=\"" + ns + "\" uri=\"" + localUri + "\"/>");
            }
        }
        catalogWriter.println("</catalog>");
        catalogWriter.flush();
        catalogWriter.close();
    }
    
    private File[] resolveProjectUri(
            final File projectDirectory, 
            final String uri) throws IOException {
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
    
    private void processImports(
            final File source,
            final File target) {
        
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        
        try {
            SAXParserFactory.
                    newInstance().
                    newSAXParser().
                    parse(source, new MyHandler(source, target));
        } catch (IOException e) {
            // just ignore, as we cannot handle it in any way
        } catch (ParserConfigurationException e) {
            // just ignore, as we cannot handle it in any way
        } catch (SAXException e) {
            // just ignore, as we cannot handle it in any way
        }
    }
    
    private class MyHandler extends DefaultHandler {
        
        private File source;
        private File target;
        
        public MyHandler(
                final File source,
                final File target) {
            
            this.source = source;
            this.target = target;
        }

        @Override
        public void startElement(
                final String uri, 
                final String localName, 
                final String qName, 
                final Attributes attributes) throws SAXException {
            
            if ("import".equals(qName) || "xsd:import".equals(qName)) {
                final String wsdlLocation = attributes.getValue("location");
                final String schemaLocation = attributes.getValue("schemaLocation");
                
                final String location = 
                        wsdlLocation == null ? schemaLocation : wsdlLocation;
                
                final File referenced = new File(source.getParentFile(), location);
                final File dereferenced = new File(target.getParentFile(), location);
                
                if ((location != null) && 
                        referenced.exists() && !referenced.isDirectory()) {
                    try {
                        Util.copyFile(referenced, dereferenced);
                    } catch (IOException e) {
                        throw new SAXException(e);
                    }
                    
                    processImports(referenced, dereferenced);
                }
            }
        }
    }
}
