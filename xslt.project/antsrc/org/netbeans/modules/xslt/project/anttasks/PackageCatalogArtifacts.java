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
import org.netbeans.modules.xslt.project.CommandlineXsltProjectXmlCatalogProvider;
import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PackageCatalogArtifacts extends org.apache.tools.ant.Task {

    private static final String dirSep = "/";
    private Logger logger = Logger.getLogger(PackageCatalogArtifacts.class.getName());
    final private String retrieverPathPrefix = "nbproject/private/cache/retriever/"; // NOI18N
    final private String retrieverPathPrefix2 = "retrieved/"; // NOI18N

    /**
     * Build directory
     */
    private String mBuildDirectoryPath = null;

    public PackageCatalogArtifacts() {
    }

    public void doCopy(final File sourceDirectory, final File buildDirectory) throws SAXException, IOException {
//out();
//out("doCopy: " + sourceDirectory);
//out("      : " + buildDirectory);
//out();
        CommandlineXsltProjectXmlCatalogProvider.getInstance().setSourceDirectory(sourceDirectory.getAbsolutePath());
        File catalogFile = new File(CommandlineXsltProjectXmlCatalogProvider.getInstance().getProjectCatalogUri());

        if (!catalogFile.exists() || (catalogFile.length() == 0)) {
            return;
        }
        myVisitedXSDList = new ArrayList<String>();
        doCopy(sourceDirectory, buildDirectory, new XsltProjectCatalogReader(catalogFile.getCanonicalPath()));
    }

    private void doCopy(final File sourceDirectory, final File buildDirectory, final XsltProjectCatalogReader catalogReader) throws IOException {
//out();
//out("doCopy: " + sourceDirectory);
//out("      : " + buildDirectory);
        final List<String> systemIds = catalogReader.getSystemIds();
        final List<String> locations = catalogReader.getLocations();

        final File metaInfDirectory = new File(buildDirectory, "META-INF"); // NOI18N
        metaInfDirectory.mkdirs();
        final PrintWriter writer = new PrintWriter(new FileWriter(new File(metaInfDirectory, "catalog.xml")));

        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"); // NOI18N
        writer.println("<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\" prefer=\"system\">"); // NOI18N
//out();
        for (int i = 0; i < systemIds.size(); i++) {
            final String systemId = systemIds.get(i);
            final String location = locations.get(i);

            try {
//out("see: " + systemId);
//out("   : " + location);
                handleEntry(systemId, location, sourceDirectory, buildDirectory, writer);
            } catch (CatalogPackagingException e) {
                System.err.println("Failed to package '" + systemId + "', an exception was thrown: " + e.getMessage());
            }
        }
        writer.println("</catalog>");
        writer.flush();
        writer.close();
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
            // The location is a relative path, i.e. the URI scheme is null
            if (uri.getScheme() == null) {
//out("      : 111");
                // The URI leads us to the sources directory -- just correct it and proceed
                if (location.startsWith("src/")) {
                    String localUri = location;
                    Util.copyFile(new File(projectDirectory, location), new File(buildDirectory, METAINF + localUri));
                    printToCatalog(catalogWriter, systemId, localUri);
                } else

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
            // Otherwise we do not know how to handle this location -- fail the build
            else {
                throw new CatalogPackagingException("This URI is not supported: " + location);
            }
        } catch (IOException e) {
            throw new CatalogPackagingException(e);
        } catch (URISyntaxException e) {
            throw new CatalogPackagingException(e);
        }
    }

    private void processNbUriLocation(final Project project, final String namespace, final String location, final File buildDirectory, final PrintWriter catalogWriter) throws CatalogPackagingException {
        try { // 'refd' stands for 'referenced'
            final FileObject refdFileObject = getCatalogSupport(project).resolveProjectProtocol(new URI(location));

            if (refdFileObject == null) {
                throw new CatalogPackagingException("Can't resolve referenced file object. Check the status of the referenced resource: "+location);
            }
            final File refdFile = FileUtil.toFile(refdFileObject);
            final Project refdProject = FileOwnerQuery.getOwner(refdFileObject);
            final String refdProjectDirName = refdProject.getProjectDirectory().getName();
            final String refdFilePath = Util.getRelativePath(FileUtil.toFile(refdProject.getProjectDirectory()), refdFile);
            String localUri = "src/_references/_projects/" + refdProjectDirName + "/" + refdFilePath;
            localUri = localUri.replace("../", "__/").replace("//", "/");

            Util.copyFile(refdFile, new File(buildDirectory, METAINF + localUri));
            printToCatalog(catalogWriter, namespace, localUri);
            processImports(refdFile, refdProject, buildDirectory, catalogWriter);
        } catch (IOException e) {
            throw new CatalogPackagingException(e);
        } catch (URISyntaxException e) {
            throw new CatalogPackagingException(e);
        }
    }

    private void processImports(final File file, final Project project, final File buildDirectory, final PrintWriter catalogWriter) throws CatalogPackagingException {
//out();
//out("see: " + file);
//out("     imports: ");
        try {
            final File projectDirectory = FileUtil.toFile(project.getProjectDirectory());
            final String projectDirname = projectDirectory.getName();
            final String catalogPath = new File(FileUtil.toFile(project.getProjectDirectory()), "catalog.xml").getAbsolutePath();
            final XsltProjectCatalogReader catalogReader = new XsltProjectCatalogReader(catalogPath);
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
                            final File refdFile = new File(projectDirectory, location);
                            Util.copyFile(refdFile, new File(buildDirectory, METAINF + localUri));
                            printToCatalog(catalogWriter, systemId, localUri);
                            processImports(refdFile, project, buildDirectory, catalogWriter);
                        }
                        // The URI leads to the nbproject directory -- it is a
                        // resource fetched by the retriever -- copy it
                        else if (location.startsWith(retrieverPathPrefix)) {
                            String localUri = "src/_references/_projects/" + projectDirname + "/_references/_cache/" + location.substring(retrieverPathPrefix.length());
                            localUri = localUri.replace("../", "__/").replace("//", "/");
                            final File refdFile = new File(projectDirectory, location);
                            Util.copyFile(refdFile, new File(buildDirectory, METAINF + localUri));
                            printToCatalog(catalogWriter, systemId, localUri);
                            processImports(refdFile, project, buildDirectory, catalogWriter);
                        }
                        // The URI leads to the retrieved directory -- it is a
                        // resource fetched by the retriever -- copy it
                        else if (location.startsWith(retrieverPathPrefix2)) {
                            String localUri = "src/_references/_projects/" + projectDirname + "/_references/_retrieved/" + location.substring(retrieverPathPrefix2.length());
                            localUri = localUri.replace("../", "__/").replace("//", "/");
                            final File refdFile = new File(projectDirectory, location);
                            Util.copyFile(refdFile, new File(buildDirectory, METAINF + localUri));
                            printToCatalog(catalogWriter, systemId, localUri);
                            processImports(refdFile, project, buildDirectory, catalogWriter);
                        } else {
                            // Any other relative path
                            String localUri = "src/_references/_projects/" + projectDirname + "/_references/_relative/" + location;
                            localUri = localUri.replace("../", "__/").replace("//", "/");
                            final File refdFile = new File(projectDirectory, location);
                            Util.copyFile(refdFile, new File(buildDirectory, METAINF + localUri));
                            printToCatalog(catalogWriter, systemId, localUri);
                            processImports(refdFile, project, buildDirectory, catalogWriter);
                        }
                    }
                    // If the location is a NetBeans URI (URI scheme is 'nb-uri')
                    else if (uri.getScheme().equals("nb-uri")) {
                        processNbUriLocation(project, systemId, location, buildDirectory, catalogWriter);
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

                        if (!refdFileRelativePath.startsWith("..")) {
                            String localUri = "src/_references/_projects/" + projectDirname + "/" + refdFileRelativePath;
                            Util.copyFile(refdFile, new File(buildDirectory, METAINF + localUri));
                        } else {
                            String localUri = "src/_references/_projects/" + projectDirname + "/_references/_relative/" + location;
                            localUri = localUri.replace("../", "__/").replace("//", "/");
                            Util.copyFile(refdFile, new File(buildDirectory, METAINF + localUri));
                            printToCatalog(catalogWriter, systemId, localUri);
                        }
                        processImports(refdFile, project, buildDirectory, catalogWriter);
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
            throw new CatalogPackagingException(e);
        } catch (ParserConfigurationException e) {
            throw new CatalogPackagingException(e);
        }
        return handler.getLocations();
    }

    private void printToCatalog(final PrintWriter writer, final String systemId, final String uri) {
        writer.println("    <system " + "systemId=\"" + systemId + "\" " + "uri=\"" + uri.replace("\\", "/") + "\"/>"); // NOI18N
    }

    public static DefaultProjectCatalogSupport getCatalogSupport(final Project project) {
        final AntProjectHelper antHelper = project.getLookup().lookup(AntProjectHelper.class);
        final ReferenceHelper referenceHelper = project.getLookup().lookup(ReferenceHelper.class);
        return new DefaultProjectCatalogSupport(project, antHelper, referenceHelper);
    }

    public static void out() {
        System.out.println();
    }

    public static void out(Object object) {
        System.out.println("** XSLT ** " + object); // NOI18N
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

//    public void doCopy(String projectCatalogPath, File buildDir) {
//        projectCatalogPath = projectCatalogPath.replace('\\', '/');
//        File localCatalogFile = new File(CommandlineXsltProjectXmlCatalogProvider.getInstance().getProjectWideCatalog());
//        String localCatalogPath = null;
//        if (!localCatalogFile.exists()) {
//            return;
//        } else {
//            localCatalogPath = localCatalogFile.getAbsolutePath().replace('\\', '/');
//        }
//
//        File catalogFile = new File(projectCatalogPath);
//        if (catalogFile.exists() && catalogFile.length() > 0) {
//            Set<String> setOfURIs = null;
//            XsltProjectCatalogReader bpProjectCatRdr = null;
//            XsltProjectCatalogReader bpLocalCatRdr = null;
//            try {
//                bpProjectCatRdr = new XsltProjectCatalogReader(projectCatalogPath);
//                bpLocalCatRdr = new XsltProjectCatalogReader(localCatalogPath);
//                doCopy(buildDir, bpProjectCatRdr, bpLocalCatRdr);
//            } catch (Throwable ex) {
//                logger.fine(ex.getMessage());
//            }
//        }
//
//    }
//
//    private void doCopy(File mBuildDir, XsltProjectCatalogReader projectCtlg, XsltProjectCatalogReader localCtlg) throws Exception {
//
//        ArrayList<String> listOfProjectNS = projectCtlg.getListOfNamespaces();
//        ArrayList<String> listOfProjoectURIs = projectCtlg.getListOfLocalURIs();
//
//        ArrayList<String> listOfLocalURIs = localCtlg.getListOfLocalURIs();
//        ArrayList<String> listOfLocalNSs = localCtlg.getListOfNamespaces();
//
//        Copy copyOp = new Copy();
//        String metaInfDir = mBuildDir.getAbsolutePath() + dirSep + "META-INF";
//        File metaInfFile = new File(metaInfDir);
//        if (!metaInfFile.exists()) {
//            metaInfFile.mkdirs();
//        }
//        org.apache.tools.ant.Project packProject = new org.apache.tools.ant.Project();
//        packProject.init();
//        copyOp.setProject(packProject);
//        int localIndex = -1;
//        int projIndx = -1;
//        int projectIndex = -1;
//        String localURILoc = null;
//        String prjURILoc = null;
//        File localURIFile = null;
//        String projURLoc = null;
//        for (String ns : listOfLocalNSs) {
//            //Get the index of the NS in Project catalog
//            projIndx = projectCtlg.locateNS(ns);
//            //If the entry is not found in project catalog leave it
//            if (projIndx == -1) {
//                continue;
//            }
//            //Check the Namespace entry in Local Catalog (retreived\catalog.xml)
//            localIndex = localCtlg.locateNS(ns);
//            //If found, get the URI Location from local catalog
//            localURILoc = (String) listOfLocalURIs.get(localIndex);
//            prjURILoc = (String) listOfProjoectURIs.get(projIndx);
//            prjURILoc = prjURILoc.replace('\\', '/');
//
//            localURIFile = new File(localURILoc);
//            int delimIndx = -1;
//            String localURIFileParentDir = localURIFile.getParent();
//            if (localURIFileParentDir != null) {
//                localURILoc = localURIFileParentDir.replace('\\', '/');
//            } else {
//                localURILoc = localURIFile.getAbsolutePath().replace('\\', '/');
//                delimIndx = localURILoc.lastIndexOf("/");
//                if (delimIndx > 0) {
//                    localURILoc = localURILoc.substring(0, delimIndx);
//                } else {
//                    continue;
//                }
//            }
//            //Set the destination Dir
//            copyOp.setTodir(new File(metaInfDir + dirSep + localURILoc));
//            copyOp.setOverwrite(true);
//
//            FileSet fs = null;
//            File projectDir = mBuildDir.getParentFile();
//            if (projectDir == null) {
//                projectDir = new File(mBuildDir.getAbsolutePath() + dirSep + "..");
//            }
//            File deleteDir = null;
//            boolean bDelete = true;
//            copyOp.setFile(new File(projectDir + dirSep + prjURILoc));
//            copyOp.execute();
//
//        }
//
//    }
}
