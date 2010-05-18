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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
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
    
    final private Logger logger = Logger.getLogger(PackageCatalogArtifacts.class.getName());
    final private String retrieverPathPrefix = "nbproject/private/cache/retriever/"; // NOI18N
    final private String retrieverPathPrefix2 = "retrieved/"; // NOI18N
    
    public void doCopy(File sourceDirectory, File buildDirectory) throws SAXException, IOException {
//out();
//out("doCopy: " + sourceDirectory);
//out("      : " + buildDirectory);
//out();
        CommandlineBpelProjectXmlCatalogProvider.getInstance().setSourceDirectory(sourceDirectory.getAbsolutePath());
//out("      1");
        File catalogFile = new File(CommandlineBpelProjectXmlCatalogProvider.getInstance().getProjectCatalogUri());
//out("      2");
        
        if (!catalogFile.exists() || (catalogFile.length() == 0)) {
            return;
        }
//out("      3");
        myVisitedXSDList = new ArrayList<String>();
//out("      4");
        doCopy(sourceDirectory, buildDirectory, new CatalogReader(catalogFile.getCanonicalPath()));
//out("      5");
    }
    
    private void doCopy(File sourceDirectory, File buildDirectory, CatalogReader catalogReader) throws IOException {
//out();
//out("doCopy: " + sourceDirectory);
//out("      : " + buildDirectory);
        List<String> systemIds = catalogReader.getSystemIds();
        List<String> locations = catalogReader.getLocations();

//out("      1");
        File metaInfDirectory = new File(buildDirectory, "META-INF"); // NOI18N
        metaInfDirectory.mkdirs();
        PrintWriter writer = new PrintWriter(new FileWriter(new File(metaInfDirectory, "catalog.xml")));
//out("      2");

        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"); // NOI18N
        writer.println("<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\" prefer=\"system\">"); // NOI18N
//out("      3");
        for (int i = 0; i < systemIds.size(); i++) {
            String systemId = systemIds.get(i);
            String location = locations.get(i);

            try {
//out("see: " + systemId);
//out("   : " + location);
                handleEntry(systemId, location, sourceDirectory, buildDirectory, writer);
            }
            catch (CatalogPackagingException e) {
                System.err.println("Failed to package '" + systemId + "', an exception was thrown: " + e.getMessage());
                e.printStackTrace();
            }
        }
//out("      4");
        writer.println("</catalog>");
        writer.flush();
        writer.close();
//out("      5");
//out();
    }
    
    private void handleEntry(final String systemId, final String location, final File sourceDirectory, final File buildDirectory, final PrintWriter catalogWriter) throws CatalogPackagingException {
//out();
//out("handle: " + systemId);
//out("      : " + location);
//out("      : " + sourceDirectory);
//out("      : " + buildDirectory);
        final File projectDirectory = sourceDirectory.getParentFile();
        
        try {
            final URI uri = new URI(location);
//out();
//out(" uri.getScheme(): " + uri.getScheme());
//out();
            // The location is a relative path, i.e. the URI scheme is null
            if (uri.getScheme() == null) {
//out("      : 111");
                // # 166953
                if (location.startsWith("src/")) { // The URI leads us to the sources directory -- just correct it and proceed
                    // # 179702
                    String localUri = ".." + location.substring(3);
//                  Util.copyFile(new File(projectDirectory, location), new File(buildDirectory, METAINF + localUri));
                    printToCatalog(catalogWriter, systemId, localUri);
//out("      : " + localUri);
                }
                else 

                // The URI leads to the nbproject directory -- it is a
                // resource fetched by the retriever -- copy it
                if (location.startsWith(retrieverPathPrefix)) {
                    String localUri = "src/_references/_cache/" + location.substring(retrieverPathPrefix.length());
                    localUri = localUri.replace("../", "__/").replace("//", "/");
                    Util.copyFile(new File(projectDirectory, location), new File(buildDirectory, METAINF + localUri));
                    printToCatalog(catalogWriter, systemId, localUri);
                } else 

                // The URI leads to the retrieved directory -- it is a
                // resource fetched by the retriever -- copy it
                if (location.startsWith(retrieverPathPrefix2)) {
                    String localUri = "src/_references/_retrieved/" + location.substring(retrieverPathPrefix2.length());
                    localUri = localUri.replace("../", "__/").replace("//", "/");
                    Util.copyFile(new File(projectDirectory, location), new File(buildDirectory, METAINF + localUri));
                    printToCatalog(catalogWriter, systemId, localUri);
                } else {
                    String localUri = "src/_references/_relative/" + location;
                    localUri = localUri.replace("../", "__/").replace("//", "/");
                    Util.copyFile(new File(projectDirectory, location), new File(buildDirectory, METAINF + localUri));
                    printToCatalog(catalogWriter, systemId, localUri);
                }
            } else 

            // If the location is a NetBeans URI (URI scheme is 'nb-uri')
            if (uri.getScheme().equals("nb-uri")) {
//out("      : NB URI");
                FileObject src = FileUtil.toFileObject(sourceDirectory);
//out("   src: " + src);
                Project project = FileOwnerQuery.getOwner(src);
                processNbUriLocation(project, systemId, location, buildDirectory, catalogWriter);
            }
            else if (uri.getScheme().equals("file")) {
//out();
//out("getScheme(): " + uri.getScheme());
                    String locator = location.substring("file:/".length());
                    String localUri = "src/_references/_loocalee/" + locator;
                    localUri = localUri.replace("../", "__/").replace("//", "/").replace(":", "_$$");
//out("    locator: " + locator);
//out("   localUri: " + localUri);
                    Util.copyFile(new File(locator), new File(buildDirectory, METAINF + localUri));
                    printToCatalog(catalogWriter, systemId, localUri);
            }
            else {
                // Otherwise we do not know how to handle this location -- fail the build
                throw new CatalogPackagingException("This URI is not supported: " + location);
            }
        }
        catch (IOException e) {
            throw new CatalogPackagingException(e);
        }
        catch (URISyntaxException e) {
            throw new CatalogPackagingException(e);
        }
    }
    
    private void processNbUriLocation(final Project project, final String namespace, final String location, final File buildDirectory, final PrintWriter catalogWriter) throws CatalogPackagingException {
        if (project == null) {
            return;
        }
        try { // 'refd' stands for 'referenced'
            final FileObject refdFileObject = getCatalogSupport(project).resolveProjectProtocol(new URI(location));
      
            if (refdFileObject == null) {
                throw new CatalogPackagingException("Can't resolve referenced file object. Check the status of the referenced resource: "+location);
            }
            final File refdFile = FileUtil.toFile(refdFileObject);
            FileObject refdProjectDirectory = getProjectDirectory(refdFileObject);
//out();
//out("file: " + refdFileObject);
//out();
//out(" prj: " + refdProjectDirectory);

            String refdProjectDirName = refdProjectDirectory.getName();
            String refdFilePath = Util.getRelativePath(FileUtil.toFile(refdProjectDirectory), refdFile);
            String localUri = "src/_references/_projects/" + refdProjectDirName + "/" + refdFilePath;
            localUri = localUri.replace("../", "__/").replace("//", "/");

            Util.copyFile(refdFile, new File(buildDirectory, METAINF + localUri));
            printToCatalog(catalogWriter, namespace, localUri);
            processImports(refdFile, FileUtil.toFile(refdProjectDirectory), buildDirectory, catalogWriter);
        }
        catch (IOException e) {
            throw new CatalogPackagingException(e);
        }
        catch (URISyntaxException e) {
            throw new CatalogPackagingException(e);
        }
    }

    private FileObject getProjectDirectory(FileObject file) {
        Project project = FileOwnerQuery.getOwner(file);

        if (project != null) {
            return project.getProjectDirectory();
        }
        while (file != null) {
            if (isProjectDirectory(file)) {
                return file;
            }
            file = file.getParent();
        }
        return null;
    }

    private boolean isProjectDirectory(FileObject folder) {
        if (folder == null) {
            return false;
        }
        if ( !folder.isFolder()) {
            return false;
        }
        if (folder.getFileObject("nbproject") == null) { // NOI18N
            return false;
        }
        if (folder.getFileObject("src") == null) { // NOI18N
            return false;
        }
        if (folder.getFileObject("build.xml") == null) { // NOI18N
            return false;
        }
        return true;
    }

    private void processImports(final File file, final File projectDirectory, final File buildDirectory, final PrintWriter catalogWriter) throws CatalogPackagingException {
//out();
//out("see: " + file);
//out("     imports: ");
        try {
            final String projectDirname = projectDirectory.getName();
            final String catalogPath = new File(projectDirectory, "catalog.xml").getAbsolutePath();
            final CatalogReader catalogReader = new CatalogReader(catalogPath);
            final List<String> systemIds = catalogReader.getSystemIds();
            final List<String> locations = catalogReader.getLocations();
            final List<String> imports = parseImports(file);
            
            for (String key : imports) {
//out("            : " + key);
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
                            String localUri = "src/_references/_projects/" + projectDirname + "/" + location;
                            localUri = localUri.replace("../", "__/").replace("//", "/");
                            File refdFile = new File(projectDirectory, location);
                            Util.copyFile(refdFile, new File(buildDirectory, METAINF + localUri));
                            printToCatalog(catalogWriter, systemId, localUri);
                            processImports(refdFile, projectDirectory, buildDirectory, catalogWriter);
                        }
                        // The URI leads to the nbproject directory -- it is a
                        // resource fetched by the retriever -- copy it
                        else if (location.startsWith(retrieverPathPrefix)) {
                            String localUri = "src/_references/_projects/" + projectDirname + "/_references/_cache/" + location.substring(retrieverPathPrefix.length());
                            localUri = localUri.replace("../", "__/").replace("//", "/");
                            File refdFile = new File(projectDirectory, location);
                            Util.copyFile(refdFile, new File(buildDirectory, METAINF + localUri));
                            printToCatalog(catalogWriter, systemId, localUri);
                            processImports(refdFile, projectDirectory, buildDirectory, catalogWriter);
                        }
                        // The URI leads to the retrieved directory -- it is a
                        // resource fetched by the retriever -- copy it
                        else if (location.startsWith(retrieverPathPrefix2)) {
                            String localUri = "src/_references/_projects/" + projectDirname + "/_references/_retrieved/" + location.substring(retrieverPathPrefix2.length());
                            localUri = localUri.replace("../", "__/").replace("//", "/");
                            File refdFile = new File(projectDirectory, location);
                            Util.copyFile(refdFile, new File(buildDirectory, METAINF + localUri));
                            printToCatalog(catalogWriter, systemId, localUri);
                            processImports(refdFile, projectDirectory, buildDirectory, catalogWriter);
                        } else {
                            // Any other relative path
                            String localUri = "src/_references/_projects/" + projectDirname + "/_references/_relative/" + location;
                            localUri = localUri.replace("../", "__/").replace("//", "/");
                            File refdFile = new File(projectDirectory, location);
                            Util.copyFile(refdFile, new File(buildDirectory, METAINF + localUri));
                            printToCatalog(catalogWriter, systemId, localUri);
                            processImports(refdFile, projectDirectory, buildDirectory, catalogWriter);
                        }
                    }
                    // If the location is a NetBeans URI (URI scheme is 'nb-uri')
                    else if (uri.getScheme().equals("nb-uri")) {
                        processNbUriLocation(FileOwnerQuery.getOwner(FileUtil.toFileObject(projectDirectory)), systemId, location, buildDirectory, catalogWriter);
                    }
                    // Otherwise we do not know how to handle this location -- fail the build
                    else {
                        throw new CatalogPackagingException("This URI is not supported: " + location);
                    }
                } else {
                    String systemId = key;
                    String location = key;
                    URI uri = new URI(location);
                    
                    if (myVisitedXSDList.contains(key)) {
                        continue;
                    }
                    myVisitedXSDList.add(key);

                    // We know how to handle relative URIs, but nothign else, if it's not 
                    // in the project catalog
                    if (uri.getScheme() == null) {
                        final File refdFile = new File(file.getParentFile(), location);
                        
                        // If this file is part of the project's file structure, we 
                        // should simply copy it to the corresponding location, without
                        // any additional handling
                        String refdFileRelativePath = Util.getRelativePath(projectDirectory, refdFile);
                        String localUri;

                        if (refdFileRelativePath.startsWith("..")) {
                            localUri = "src/_references/_projects/" + projectDirname + "/_references/_relative/" + location;
                            localUri = localUri.replace("../", "__/").replace("//", "/");
                            Util.copyFile(refdFile, new File(buildDirectory, METAINF + localUri));
                        } else {
                            localUri = "src/_references/_projects/" + projectDirname + "/" + refdFileRelativePath;
                            Util.copyFile(refdFile, new File(buildDirectory, METAINF + localUri));
                        }
                        printToCatalog(catalogWriter, systemId, localUri);
                        processImports(refdFile, projectDirectory, buildDirectory, catalogWriter);
                    } else {
                        throw new CatalogPackagingException("This URI is not supported: " + location);
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
    
    private List<String> parseImports(final File file) throws CatalogPackagingException {
        final MyHandler handler = new MyHandler();
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(file, handler);
        } catch (IOException e) {
            throw new CatalogPackagingException(e);
        } catch (SAXException e) {
            // ignore
        } catch (ParserConfigurationException e) {
            throw new CatalogPackagingException(e);
        }
        return handler.getLocations();
    }
    
    private void printToCatalog(final PrintWriter writer, final String systemId, final String uri) {
        writer.println("    <system " + "systemId=\"" + systemId + "\" " + "uri=\"" + uri.replace("\\", "/") + "\"/>"); // NOI18N
    }

    public static DefaultProjectCatalogSupport getCatalogSupport(Project project) {
        AntProjectHelper antHelper = project.getLookup().lookup(AntProjectHelper.class);
        ReferenceHelper referenceHelper = project.getLookup().lookup(ReferenceHelper.class);
        return new DefaultProjectCatalogSupport(project, antHelper, referenceHelper);
    }

    public static void out() {
        System.out.println();
    }

    public static void out(Object object) {
        System.out.println("*** " + object); // NOI18N
    }

    // ----------------------------------------------------
    private static class MyHandler extends DefaultHandler {
        
        private List<String> locations = new ArrayList<String>();
        
        public List<String> getLocations() {
            return locations;
        }
        
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.endsWith("import") || qName.endsWith("include")) { // NOI18N
                String wsdlLocation = attributes.getValue("location"); // NOI18N
                String schemaLocation = attributes.getValue("schemaLocation"); // NOI18N
                locations.add(wsdlLocation == null ? schemaLocation : wsdlLocation);
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

    private static final String METAINF = "META-INF/";
    private ArrayList<String> myVisitedXSDList;
}
