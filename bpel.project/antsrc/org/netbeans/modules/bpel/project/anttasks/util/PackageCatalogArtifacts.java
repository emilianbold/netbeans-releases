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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
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
    
    final private Logger logger = 
            Logger.getLogger(PackageCatalogArtifacts.class.getName());
    
    final private String retrieverPathPrefix = 
            "nbproject/private/cache/retriever/"; // NOI18N
    final private String retrieverPathPrefix2 = 
            "retrieved/"; // NOI18N
    
    public void doCopy(
            final File sourceDirectory,
            final File buildDirectory) {
        
        CommandlineBpelProjectXmlCatalogProvider.getInstance().
                setSourceDirectory(sourceDirectory.getAbsolutePath());
        
        final File catalogFile = new File(
                CommandlineBpelProjectXmlCatalogProvider.getInstance().
                getProjectCatalogUri());
        
        if (!catalogFile.exists() || (catalogFile.length() == 0)) return;
        
        try {
            final CatalogReader catalogReader = 
                    new CatalogReader(catalogFile.getCanonicalPath());
            
            doCopy(sourceDirectory, buildDirectory, catalogReader);
        } catch (CatalogPackagingException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } catch (SAXException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    private void doCopy(
            final File sourceDirectory,
            final File buildDirectory,
            final CatalogReader catalogReader) throws CatalogPackagingException {
        
        try {
            final List<String> systemIds = catalogReader.getSystemIds();
            final List<String> locations = catalogReader.getLocations();

            final File metaInfDirectory = new File(buildDirectory, "META-INF"); // NOI18N
            metaInfDirectory.mkdirs();

            final PrintWriter writer = new PrintWriter(
                    new FileWriter(new File(metaInfDirectory, "catalog.xml")));

            writer.println("<?xml " + // NOI18N
                    "version=\"1.0\" " + // NOI18N
                    "encoding=\"UTF-8\" " + // NOI18N
                    "standalone=\"no\"?>"); // NOI18N
            writer.println("<catalog " + // NOI18N
                    "xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\" " + // NOI18N
                    "prefer=\"system\">"); // NOI18N

            for (int i = 0; i < systemIds.size(); i++) {
                final String systemId = systemIds.get(i);
                String location = locations.get(i);

                handleEntry(
                        systemId,
                        location, 
                        sourceDirectory, 
                        buildDirectory, 
                        writer);
            }

            writer.println("</catalog>");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new CatalogPackagingException(e);
        }
    }
    
    private void handleEntry(
            final String systemId,
            final String location,
            final File sourceDirectory,
            final File buildDirectory,
            final PrintWriter catalogWriter) throws CatalogPackagingException {
        
        final File projectDirectory = sourceDirectory.getParentFile();
        
        try {
            final URI uri = new URI(location);
            // The location is a relative path, i.e. the URI scheme is null
            if (uri.getScheme() == null) {
                // The URI leads us to the sources directory -- just correct 
                // it and proceed
                if (location.startsWith("src/")) {
                    printToCatalog(
                            catalogWriter, 
                            systemId, 
                            "../" + location.substring("src/".length()));
                } else 

                // The URI leads to the nbproject directory -- it is a
                // resource fetched by the retriever -- copy it
                if (location.startsWith(retrieverPathPrefix)) {
                    String localUri = "_references/_cache/" + 
                            location.substring(retrieverPathPrefix.length());

                    localUri = localUri.replace("../", "__/").replace("//", "/");

                    Util.copyFile(
                            new File(projectDirectory, location), 
                            new File(buildDirectory,  localUri));

                    printToCatalog(
                            catalogWriter, 
                            systemId, 
                            "../" + localUri);
                } else 

                // The URI leads to the retrieved directory -- it is a
                // resource fetched by the retriever -- copy it
                if (location.startsWith(retrieverPathPrefix2)) {
                    String localUri = "_references/_retrieved/" + 
                            location.substring(retrieverPathPrefix2.length());

                    localUri = localUri.replace("../", "__/").replace("//", "/");

                    Util.copyFile(
                            new File(projectDirectory, location), 
                            new File(buildDirectory, localUri));

                    printToCatalog(
                            catalogWriter, 
                            systemId, 
                            "../" + localUri);
                } else

                // We cannot hanle any other relative paths, throw an exception
                {
                    String localUri = "_references/_relative/" + location;

                    localUri = localUri.replace("../", "__/").replace("//", "/");

                    Util.copyFile(
                            new File(projectDirectory, location), 
                            new File(buildDirectory, localUri));

                    printToCatalog(
                            catalogWriter, 
                            systemId, 
                            "../" + localUri);
                }
            } else 

            // If the location is a NetBeans URI (URI scheme is 'nb-uri')
            if (uri.getScheme().equals("nb-uri")) {
                final Project project = FileOwnerQuery.getOwner(
                        FileUtil.toFileObject(sourceDirectory));

                processNbUriLocation(
                        project, systemId, location, buildDirectory, catalogWriter);
            }

            // Otherwise we do not know how to handle this location -- fail the build
            else {
                throw new CatalogPackagingException(
                        "This URI is not supported: " + location);
            }
        } catch (IOException e) {
            throw new CatalogPackagingException(e);
        } catch (URISyntaxException e) {
            throw new CatalogPackagingException(e);
        }
    }
    
    private void processNbUriLocation(
            final Project project, 
            final String namespace,
            final String location, 
            final File buildDirectory,
            final PrintWriter catalogWriter) throws CatalogPackagingException {
        
        /* 'refd' stands for 'referenced' */
        
        try {
            final FileObject refdFileObject = 
                    getCatalogSupport(project).resolveProjectProtocol(new URI(location));
            final File refdFile = 
                    FileUtil.toFile(refdFileObject);
            final Project refdProject = 
                    FileOwnerQuery.getOwner(refdFileObject);

            final String refdProjectDirName = 
                    refdProject.getProjectDirectory().getName();
            final String refdFilePath = Util.getRelativePath(
                    FileUtil.toFile(refdProject.getProjectDirectory()), refdFile);

            String localUri = 
                    "_references/_projects/" + refdProjectDirName + "/" + refdFilePath;

            localUri = localUri.replace("../", "__/").replace("//", "/");

            Util.copyFile(
                    refdFile, 
                    new File(buildDirectory, localUri));

            printToCatalog(
                    catalogWriter, 
                    namespace, 
                    "../" + localUri);

            processImports(refdFile, refdProject, buildDirectory, catalogWriter);
        } catch (IOException e) {
            throw new CatalogPackagingException(e);
        } catch (URISyntaxException e) {
            throw new CatalogPackagingException(e);
        }
    }
    
    private void processImports(
            final File file, 
            final Project project, 
            final File buildDirectory, 
            final PrintWriter catalogWriter) throws CatalogPackagingException {
        
        try {
            final File projectDirectory = FileUtil.toFile(project.getProjectDirectory());
            final String projectDirname = projectDirectory.getName();
            
            final String catalogPath = new File(FileUtil.toFile(
                    project.getProjectDirectory()), "catalog.xml").getAbsolutePath();
            final CatalogReader catalogReader = new CatalogReader(catalogPath);
            
            final List<String> systemIds = catalogReader.getSystemIds();
            final List<String> locations = catalogReader.getLocations();
            
            final List<String> imports = parseImports(file);
            
            for (String key: imports) {
                final int index = systemIds.indexOf(key);
                
                if (index != -1) {
                    final String systemId = systemIds.get(index);
                    final String location = locations.get(index);
                    
                    final URI uri = new URI(location);
                    // The location is a relative path, i.e. the URI scheme is null
                    if (uri.getScheme() == null) {
                        // The URI leads us to the sources directory -- just correct 
                        // it and proceed
                        if (location.startsWith("src/")) {
                            String localUri = 
                                    "_references/_projects/" + projectDirname + 
                                    "/" + location;
                                    
                            localUri = localUri.replace("../", "__/").replace("//", "/");
                            
                            final File refdFile = new File(projectDirectory, location);
                            
                            Util.copyFile(
                                    refdFile, 
                                    new File(buildDirectory,  localUri));
                                    
                            printToCatalog(
                                    catalogWriter, 
                                    systemId, 
                                    "../" + localUri);
                            
                            processImports(
                                    refdFile, project, buildDirectory, catalogWriter);
                        } else 
                        
                        // The URI leads to the nbproject directory -- it is a
                        // resource fetched by the retriever -- copy it
                        if (location.startsWith(retrieverPathPrefix)) {
                            String localUri = 
                                    "_references/_projects/" + projectDirname + 
                                    "/_references/_cache/" + 
                                    location.substring(retrieverPathPrefix.length());
                                    
                            localUri = localUri.replace("../", "__/").replace("//", "/");
                            
                            final File refdFile = new File(projectDirectory, location);
                            
                            Util.copyFile(
                                    refdFile, 
                                    new File(buildDirectory,  localUri));
                                    
                            printToCatalog(
                                    catalogWriter, 
                                    systemId, 
                                    "../" + localUri);
                            
                            processImports(
                                    refdFile, project, buildDirectory, catalogWriter);
                        } else 
                        
                        // The URI leads to the retrieved directory -- it is a
                        // resource fetched by the retriever -- copy it
                        if (location.startsWith(retrieverPathPrefix2)) {
                            String localUri = 
                                    "_references/_projects/" + projectDirname + 
                                    "/_references/_retrieved/" + 
                                    location.substring(retrieverPathPrefix2.length());
                                    
                            localUri = localUri.replace("../", "__/").replace("//", "/");
                            
                            final File refdFile = new File(projectDirectory, location);
                            
                            Util.copyFile(
                                    refdFile, 
                                    new File(buildDirectory, localUri));
                                    
                            printToCatalog(
                                    catalogWriter, 
                                    systemId, 
                                    "../" + localUri);
                            
                            processImports(
                                    refdFile, project, buildDirectory, catalogWriter);
                        } else
                        
                        // Any other relative path
                        {
                            String localUri = 
                                    "_references/_projects/" + projectDirname + 
                                    "/_references/_relative/" + location;
                                    
                            localUri = localUri.replace("../", "__/").replace("//", "/");
                            
                            final File refdFile = new File(projectDirectory, location);
                            
                            Util.copyFile(
                                    refdFile, 
                                    new File(buildDirectory, localUri));
                                    
                            printToCatalog(
                                    catalogWriter, 
                                    systemId, 
                                    "../" + localUri);
                            
                            processImports(
                                    refdFile, project, buildDirectory, catalogWriter);
                        }
                    } else 

                    // If the location is a NetBeans URI (URI scheme is 'nb-uri')
                    if (uri.getScheme().equals("nb-uri")) {
                        processNbUriLocation(
                                project, systemId, location, buildDirectory, catalogWriter);
                    }
                    
                    // Otherwise we do not know how to handle this location -- fail the 
                    // build
                    else {
                        throw new CatalogPackagingException(
                                "This URI is not supported: " + location);
                    }
                } else {
                    final String systemId = key;
                    final String location = key;
                    
                    final URI uri = new URI(location);
                    
                    // We know how to handle relative URIs, but nothign else, if it's not 
                    // in the project catalog
                    if (uri.getScheme() == null) {
                        final File refdFile = new File(file.getParentFile(), location);
                        
                        // If this file is part of the project's file structure, we 
                        // should simply copy it to the corresponding location, without
                        // any additional handling
                        final String refdFileRelativePath = 
                                Util.getRelativePath(projectDirectory, refdFile);
                        if (!refdFileRelativePath.startsWith("..")) {
                            String localUri = 
                                    "_references/_projects/" + projectDirname + "/" +
                                    refdFileRelativePath;
                            
                            Util.copyFile(
                                    refdFile, 
                                    new File(buildDirectory, localUri));
                        } else {
                            String localUri = 
                                    "_references/_projects/" + projectDirname + 
                                    "/_references/_relative/" + location;
                                    
                            localUri = localUri.replace("../", "__/").replace("//", "/");
                            
                            Util.copyFile(
                                    refdFile, 
                                    new File(buildDirectory, localUri));
                                    
                            printToCatalog(
                                    catalogWriter, 
                                    systemId, 
                                    "../" + localUri);
                        }
                        
                        processImports(refdFile, project, buildDirectory, catalogWriter);
                    } else {
                        throw new CatalogPackagingException(
                                "This URI is not supported: " + location);
                    }
                }
            }
        } catch (IOException e) {
            throw new CatalogPackagingException(e);
        } catch (URISyntaxException e) {
            throw new CatalogPackagingException(e);
        } catch (SAXException e) {
            throw new CatalogPackagingException(e);
        }
    }
    
    private List<String> parseImports(
            final File file) throws CatalogPackagingException {
        
        final MyHandler handler = new MyHandler();
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        
        try {
            SAXParserFactory.
                    newInstance().
                    newSAXParser().
                    parse(file, handler);
        } catch (IOException e) {
            throw new CatalogPackagingException(e);
        } catch (SAXException e) {
            throw new CatalogPackagingException(e);
        } catch (ParserConfigurationException e) {
            throw new CatalogPackagingException(e);
        }
        
        return handler.getLocations();
    }
    
    private void printToCatalog(
            final PrintWriter writer, 
            final String systemId, 
            final String uri) {
        writer.println("    <system " +
                "systemId=\"" + systemId + "\" " +
                "uri=\"" + uri.replace("\\", "/") + "\"/>");
    }
    
    private DefaultProjectCatalogSupport getCatalogSupport(
            final Project project) {
        final AntProjectHelper antHelper = 
                project.getLookup().lookup(AntProjectHelper.class);
        final ReferenceHelper referenceHelper = 
                project.getLookup().lookup(ReferenceHelper.class);
                
        return new DefaultProjectCatalogSupport(
                project, antHelper, referenceHelper);
    }
    
    //////////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    private static class MyHandler extends DefaultHandler {
        
        private List<String> locations = new LinkedList<String>();
        
        public List<String> getLocations() {
            return locations;
        }
        
        @Override
        public void startElement(
                final String uri, 
                final String localName, 
                final String qName, 
                final Attributes attributes) throws SAXException {
            
            if (qName.endsWith("import")) {
                final String wsdlLocation = attributes.getValue("location");
                final String schemaLocation = attributes.getValue("schemaLocation");
                
                final String location = 
                        wsdlLocation == null ? schemaLocation : wsdlLocation;
                
                locations.add(location);
            }
        }
    }
    
    public static class CatalogPackagingException extends Exception {
        
        public CatalogPackagingException(final String message) {
            super(message);
        }
        
        public CatalogPackagingException(final Throwable cause) {
            super(cause);
        }
        
    }
    
}
