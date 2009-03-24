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

package org.netbeans.modules.compapp.projects.jbi.anttasks;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.logging.Logger;
import javax.xml.namespace.QName;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectConstants;
import org.netbeans.modules.compapp.projects.jbi.descriptor.XmlUtil;
import org.netbeans.modules.compapp.projects.jbi.descriptor.endpoints.model.Endpoint;
import org.netbeans.modules.compapp.projects.jbi.descriptor.endpoints.model.PtConnection;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;
import org.netbeans.modules.compapp.javaee.codegen.model.AbstractProject;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.netbeans.modules.compapp.projects.jbi.JbiConstants.*;


/**
 * Ant task to build jbiserver service assembly
 *
 * @author tli
 * @author jqian
 */
public class BuildServiceAssembly extends Task {
    
    private String showLogOption = "false";
    
    private boolean showLog = false;
    
    private wsdlRepository mRepo;
    
    private Document jbiDocument;
    
    private Logger logger = Logger.getLogger(getClass().getName());
    
    private File mergedCatalogFile;
    
    // private boolean jbiRouting = true;
    private String projDirLoc;
    private String serviceUnitsDirLoc;
    private String jbiasaDirLoc;

    // IZ#126214 Soap bc wsit callback handler projects
    private String wsitCallbackProjects;
    private static final String SOAP_BC = "sun-http-binding";
    
    private boolean saInternalRouting = true;
    private boolean bcAutoConnect = true;
    
    // 03/26/07 Ignore Concreate WSDL ports in J2EE projects, T. Li
    private boolean ignoreJ2EEPorts = true;
    
    // binding component name list in BindingComponentInformation.xml
    private List<String> bcNames;
    
    // service unit artifact zip file name list in AssemblyInformation.xml
    private List<String> suJarNames;
    
    public static final String BC_JARNAME = "BCDeployment.jar"; // NOI18N
    
    public static final String ENDPOINT_XML = "endpoints.xml"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String SU_JBIXML_PATH = "META-INF/jbi.xml"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String SU_CATALOGXML_PATH = "META-INF/catalog.xml"; // NOI18N

    public static final String XML_CATALOG_CATALOG = "catalog"; // NOI18N
    public static final String XML_CATALOG_URN = "urn:oasis:names:tc:entity:xmlns:xml:catalog"; // NOI18N
    public static final String XML_CATALOG_XMLNS = "xmlns"; // NOI18N
    public static final String XML_CATALOG_PREFER = "prefer"; // NOI18N
    public static final String XML_CATALOG_SYSTEM = "system"; // NOI18N
    public static final String XML_CATALOG_NEXTCATALOG = "nextCatalog"; // NOI18N

    
    /**
     * Getter for the show log option
     *
     * @return show log option
     */
    public String getShowLogOption() {
        return showLogOption;
    }
    
    /**
     * Setter for the show log option
     *
     * @param showLogOption showing log
     */
    public void setShowLogOption(String showLogOption) {
        this.showLogOption = showLogOption;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @throws BuildException DOCUMENT ME!
     */
    @Override
    public void execute() throws BuildException {
        showLog = showLogOption.equalsIgnoreCase("true");
        JarFile genericBCJar = null;

        Project p = this.getProject();
        String confDir = p.getProperty((JbiProjectProperties.META_INF));
        if ((confDir == null) || (confDir.length() < 1)) {
            return;
        }

        wsitCallbackProjects = p.getProperty("WsitCallbackProjects"); // NOI18N

        String javaeeJars = p.getProperty(JbiProjectProperties.JBI_JAVAEE_JARS);
        String jars = p.getProperty((JbiProjectProperties.JBI_CONTENT_ADDITIONAL));

        projDirLoc = p.getProperty("basedir") + File.separator;
        String srcDirLoc = projDirLoc + "src" + File.separator;
        String confDirLoc = srcDirLoc + "conf" + File.separator;

        serviceUnitsDirLoc = srcDirLoc + JbiProjectConstants.FOLDER_JBISERVICEUNITS;            
        jbiasaDirLoc = srcDirLoc + JbiProjectConstants.FOLDER_JBIASA;               

        // Command-line support
//        MigrationHelper.migrateCasaWSDL(jbiasaDirLoc, getProjectName());

        // Command-line support
//        MigrationHelper.migrateCompAppProperties(projDirLoc, null);


        String catalogDirLoc = serviceUnitsDirLoc
                + File.separator + "META-INF"; // NOI18N

        String connectionsFileLoc = confDirLoc + "connections.xml";
        String buildDir = projDirLoc + p.getProperty(JbiProjectProperties.BUILD_DIR);

        // create confDir if needed..
        File buildMetaInfDir = new File(buildDir + "/META-INF");
        if (!buildMetaInfDir.exists()) {
            buildMetaInfDir.mkdirs();
        }

        // todo: set the default to false for now... 03/15/06
        // jbiRouting = getBooleanProperty(p.getProperty((JbiProjectProperties.JBI_ROUTING)), true);
        saInternalRouting = getBooleanProperty(p.getProperty((JbiProjectProperties.JBI_SA_INTERNAL_ROUTING)), true);
        bcAutoConnect = getBooleanProperty(p.getProperty((JbiProjectProperties.JBI_ROUTING_BC_AUTOCONNECT)), true);

        // 09.29.09, IZ#145136 update project catalog before validation of wsdls...
        updateFromSUCatalog(catalogDirLoc, projDirLoc + "Catalog.xml");

        // create project wsdl repository...
        try {
            FileObject baseDirFO = FileUtil.toFileObject(p.getBaseDir());
            if (baseDirFO != null) {
                FileSystem fs = baseDirFO.getFileSystem();
                fs.refresh(true);
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        mRepo = new wsdlRepository(p, this);

        log("Validating CompApp project...");
        validateCompAppProject();

        try {   
            String jbiFileLoc = buildDir + "/META-INF/jbi.xml"; 
            String genericBCJarFileLoc = buildDir + "/BCDeployment.jar";       
            
            File bDir = new File(buildDir);
            if (!bDir.exists()) {
                bDir.mkdirs();
            }
            
            // Get all the bc names
            // Use ComponentInformation.xml instead of BindingComponentInformatino.xml
            // to get all the binding component names!
            String ciFileLoc = confDirLoc + JbiProject.COMPONENT_INFO_FILE_NAME;
            bcNames = loadBindingComponentNames(ciFileLoc);
            
            String asiFileLoc = confDirLoc + JbiProject.ASSEMBLY_INFO_FILE_NAME;
            loadAssemblyInfo(asiFileLoc);
                      
            CasaBuilder casaBuilder = new CasaBuilder(project, mRepo, this);
            final Document oldCasaDocument = casaBuilder.getOldCasaDocument();
            
            // Generate the SE jar file list
            // Loop thru SE suprojects and copying/updating SE deployment jars            
            log("Generating Service Engine Service Units...");
            List<String> srcJarPaths = getJarList(jars);
            List<String> javaEEJarPaths = getJarList(javaeeJars);
            List<String> saEEJarPaths = new ArrayList<String>();

            for (String srcJarPath : srcJarPaths) {
                if ((javaEEJarPaths != null) && (javaEEJarPaths.contains(srcJarPath))){
                    srcJarPath = getLocalJavaEEJarPath(buildDir, srcJarPath);
                    saEEJarPaths.add(srcJarPath);
                    createEndpointsFrom(srcJarPath);
                    continue;
                }
                
                if ((srcJarPath.indexOf(':') < 0) && (!srcJarPath.startsWith("/"))) { // i.e., relative path
                    srcJarPath = projDirLoc + srcJarPath;
                }                
                File srcJarFile = new File(srcJarPath);
                
                String jarName = getShortName(srcJarPath); // e.x.: SynchronousSample.jar
                if (!srcJarFile.exists()) {
                    log(" Error: Missing project Sub-Assembly: " + srcJarPath);
                } else if (! suJarNames.contains(jarName)) {
                    log(" Error: Cannot locate service unit for " + jarName);
                } else {
                    String destJarPath = buildDir + File.separator + jarName;
                    //log("  copying Sub-Assembly: " + destJarPath);
                    copyJarFileWithEndpointDecoration(srcJarPath, destJarPath,
                            oldCasaDocument);
                }
            }
            
            // Resolve connections
            log("Resolving connections...");
            ConnectionResolver connectionResolver = 
                    new ConnectionResolver(this, showLog, saInternalRouting);
            connectionResolver.resolveConnections(mRepo, !bcAutoConnect, oldCasaDocument);
            
            // Write connections to connections.xml
            log("Writing connections out to connections.xml...");
            SAConnectionsBuilder dd = new SAConnectionsBuilder();
            String saName = AntProjectHelper.getServiceAssemblyID(p);
            String saDescription = AntProjectHelper.getServiceAssemblyDescription(p);
            dd.buildDOMTree(connectionResolver, saName, saDescription, oldCasaDocument);
            dd.writeToFile(connectionsFileLoc);
            
            // Generate SA jbi.xml,
            log("Generating Service Assembly jbi.xml...");
            generateServiceAssemblyDescriptor(
                    connectionResolver, connectionsFileLoc, jbiFileLoc, oldCasaDocument);
            
            // Todo: 08/22/06 merge catalogs
            log("Merging component projects' catalogs...");
            MergeSeJarCatalogs(catalogDirLoc);
            
            // Generate BC SU jbi.xml and BC SU jar file
            log("Generating Binding Component Service Units...");
            genericBCJar = new JarFile(genericBCJarFileLoc);  
            
            // Create undecorated BC SU jbi.xml    
            Map<String, BCSUDescriptorBuilder> bcsuDescriptorBuilderMap = 
                    new HashMap<String, BCSUDescriptorBuilder>();
            Map<String, String> bcJarMap = new HashMap<String, String>();
            
            for (String bcName : connectionResolver.getBCNames()) {
                String bcJarName = null;
                for (int k = 0; k < suJarNames.size(); k++) {
                    String name = suJarNames.get(k);
                    if (name.indexOf(bcName) != -1) {
                        // Use the name defined in ASI.xml
                        bcJarName = buildDir + File.separator + name;
                        log("  creating " + name);
                        break;
                    }
                }
                if (bcJarName != null) {
                    
                    // Create standalone BC jbi.xml file under src/jbiServiceUnits/<bcName>/.
                    BCSUDescriptorBuilder bcsuDescriptorBuilder = createBCSUDescriptor(
                            //bcJarName, 
                            bcName, connectionResolver);
                    bcsuDescriptorBuilderMap.put(bcName, bcsuDescriptorBuilder);
                    bcJarMap.put(bcName, bcJarName);
                } else {
                    log("ERROR: Cannot create binding component jar file for " + bcName);
                }
            }
            
            // Create the new CASA file (This depends on BC SU jbi.xml)
            log("Creating/Updating CASA...");
            Document newCasaDocument = casaBuilder.createCasaDocument(jbiDocument);
            //String s = new String(XmlUtil.writeToBytes(newCasaDocument));
            //System.out.println("---------------\n"+s+"------------------\n");
            
            // Decorate BC SU jbi.xml and generate BC jar.
            for (String bcName : bcsuDescriptorBuilderMap.keySet()) {
                BCSUDescriptorBuilder bcsuDescriptorBuilder = 
                        bcsuDescriptorBuilderMap.get(bcName);
                bcsuDescriptorBuilder.decorateEndpoints(newCasaDocument);
                
                // Create build/<bcName>.jar
                // ToDo: update bc jar endpoints.xml
                // 1. copy BCjars for each needed BC
                // 2. create jbi.xml
                // boolean isCompAppWSDLNeeded = bcsUsingCompAppWsdl.contains(bcName);

                // IZ#126214, soap bc wist callback handler...
                createBCJar(bcJarMap.get(bcName), genericBCJar,
                        /*isCompAppWSDLNeeded,*/ bcsuDescriptorBuilder, bcName);
            }            
              
            /*
            // Generate the SE jar file list
            // Loop thru SE suprojects and copying/updating SE deployment jars            
            log("Generating Service Engine Service Units...");
            List<String> srcJarPaths = getJarList(jars);
            List<String> javaEEJarPaths = getJarList(javaeeJars);
            List<String> saEEJarPaths = new ArrayList<String>();

            for (String srcJarPath : srcJarPaths) {
                if ((javaEEJarPaths != null) && (javaEEJarPaths.contains(srcJarPath))){
                    srcJarPath = getLocalJavaEEJarPath(buildDir, srcJarPath);
                    saEEJarPaths.add(srcJarPath);
                    createEndpointsFrom(srcJarPath);
                    continue;
                }
                
                if ((srcJarPath.indexOf(':') < 0) && (!srcJarPath.startsWith("/"))) { // i.e., relative path
                    srcJarPath = projDirLoc + srcJarPath;
                }                
                File srcJarFile = new File(srcJarPath);
                
                String jarName = getShortName(srcJarPath); // e.x.: SynchronousSample.jar
                if (!srcJarFile.exists()) {
                    log(" Error: Missing project Sub-Assembly: " + srcJarPath);
                } else if (! suJarNames.contains(jarName)) {
                    log(" Error: Cannot locate service unit for " + jarName);
                } else {
                    String destJarPath = buildDir + File.separator + jarName;
                    //log("  copying Sub-Assembly: " + destJarPath);
                    copyJarFileWithEndpointDecoration(srcJarPath, destJarPath,
                            newCasaDocument);
                }
            }
            */
            // )4/03/08, generated OSGi supported manifest.mf (minimum entries)
            String osgisupport = p.getProperty(JbiProjectProperties.OSGI_SUPPORT);
            String projName = p.getProperty(JbiProjectProperties.SERVICE_ASSEMBLY_ID);

            // 02/04/09, IZ#153580, always generated OSGi manifest
            //  if ((osgisupport != null) && osgisupport.equalsIgnoreCase("true")) {
                generateOSGiManifest(buildMetaInfDir, projName);
            // }

            // 9/12/07, filter out unconnected JavaEE endpoints
            log("Filtering Java EE Endpoints...");

            // 01/25/08, disabled, see IZ#115609 and 113026
            // filterJavaEEEndpoints(connectionResolver, saEEJarPaths, serviceUnitsDirLoc);
        } catch (Exception e) {
            log("ERROR: " + e.toString(), Project.MSG_ERR);
        } finally {
            try {
                if (genericBCJar != null) {
                    genericBCJar.close();
                }
                // jar.close();
            } catch (IOException ignored) {
                // ignore this..
            }
        }
    }

    private void generateOSGiManifest(File buildDir, String projName) {
        if (buildDir == null) {
            return;
        }

        if (projName == null) {
            projName = "UnKnownApp";  // NOI18N
        }
        try {
            String manText = "Bundle-Name: " + projName + "\n" +     // NOI18N
                             "Bundle-SymbolicName: " + projName + "\n" +    // NOI18N
                             "Bundle-ManifestVersion: 2\n" +      // NOI18N
                             "Bundle-Version: 1.0.0";     // NOI18N

            File file = new File(buildDir, "MANIFEST.MF");    // NOI18N
            boolean success = file.createNewFile();
            if (success) {
                BufferedWriter out = new BufferedWriter(new FileWriter(file));
                out.write(manText);
                out.close();
            } else {
                // File already exists
            }
        } catch (IOException ex) {
            log("Exception: A processing error occurred; " + ex);     // NOI18N
        }


    }

    private List<String> getJarList(String commaSeparatedList){
        List<String>  ret = new ArrayList<String>();
        if (commaSeparatedList != null) {
            StringTokenizer st = new StringTokenizer(commaSeparatedList, ";");
            String tkn = null;
            while (st.hasMoreTokens()){
                tkn = st.nextToken();
                ret.add(tkn);
            }
        }
        return ret;
    }
    
    private boolean getBooleanProperty(String str, boolean def) {
        boolean bFlag = def; // the default value
        if (str == null) {
            bFlag = def;
        } else {
            bFlag = str.equalsIgnoreCase("true");
        }
        
        return bFlag;
    }

    /**
     * Update the CompApp project catalog to add/remove SE SU catalog entries
     *
     * @param suCatalogDirLoc
     * @param prjCatalogFileLoc
     */
    private void updateFromSUCatalog(String suCatalogDirLoc, String prjCatalogFileLoc) {
        File catalogDir = new File(suCatalogDirLoc);
        if (!catalogDir.exists()) {  // no catalog...
            return;
        }

        // 1. loop thru project subdirs
        List<String> catalogFiles = new ArrayList<String>();
        File[] children = catalogDir.listFiles();
        if (children == null) {
            return; // no children...
        }

        // updating catalog...
        try {
            for (File child : children) {
                File catalogFile = new File(child, "catalog.xml"); // NOI18N;
                if (catalogFile.exists()) {
                    String catalogLoc = catalogFile.getAbsolutePath();
                    catalogLoc = catalogLoc.replaceAll("\\\\", "/");
                    int idx = catalogLoc.indexOf("src/jbiServiceUnits/META-INF"); // NOI18N
                    catalogFiles.add(catalogLoc.substring(idx));
                    // System.out.println("SU Catalog: "+catalogLoc);
                }
            }

            if (catalogFiles.size() < 1) {
                return; // no catalog..
            }

            boolean brandNewPrjCatalog = false;
            File prjCatalog = new File(prjCatalogFileLoc);
            if (!prjCatalog.exists()) { // create one...
                prjCatalog.createNewFile();
                brandNewPrjCatalog = true;
            }

            boolean fileUpdate = false;
            // parse content one...
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = fact.newDocumentBuilder();
            Document doc = null;
            if (brandNewPrjCatalog) {
                doc = createNewCatalogDocument(builder);
            } else {
                try {
                    doc = builder.parse(prjCatalog);
                } catch (Exception ex) {
                    doc = createNewCatalogDocument(builder);
                }
            }

            Element root = doc.getDocumentElement();
            NodeList catalogNodes = doc.getElementsByTagName(XML_CATALOG_NEXTCATALOG); // NOI18N
            for (int i = 0; i < catalogNodes.getLength(); i++) {
                Element catalogNode = (Element) catalogNodes.item(i);
                String catalog = catalogNode.getAttribute(XML_CATALOG_CATALOG); // NOI18N
                //System.out.println("next Catalog: " + catalogFiles.contains(catalog) + ", " + catalog);
                if (catalogFiles.contains(catalog)) { // OK, remove from List
                    catalogFiles.remove(catalog);
                } else { // remove from project catalog
                    root.removeChild(catalogNode);
                    fileUpdate = true;
                }
            }

            // add new su catalogs...
            for (String catalog : catalogFiles) {
                Element elm = doc.createElement(XML_CATALOG_NEXTCATALOG);
                elm.setAttribute(XML_CATALOG_CATALOG, catalog);
                root.appendChild(elm);
                fileUpdate = true;
                //System.out.println("add Catalog: " + catalog);
            }

            if (fileUpdate) {
                DOMSource src = new DOMSource(doc);
                FileOutputStream fos = new FileOutputStream(prjCatalog);
                StreamResult rest = new StreamResult(fos);
                TransformerFactory transFact = TransformerFactory.newInstance();
                Transformer transformer = transFact.newTransformer();
                transformer.transform(src, rest);
                fos.flush();
                fos.close();
            }
        } catch (Exception ex) {
            log("Exception: A processing error occurred; " + ex);
        }
    }

    private Document createNewCatalogDocument(DocumentBuilder builder) {
        Document doc = builder.newDocument();
        Element elm = doc.createElement(XML_CATALOG_CATALOG);
        elm.setAttribute(XML_CATALOG_XMLNS, XML_CATALOG_URN);
        elm.setAttribute(XML_CATALOG_PREFER, XML_CATALOG_SYSTEM);
        doc.appendChild(elm);
        return doc;
    }

    private boolean isValid(File file) {
        boolean validCatalog = false;
        // parse content one...
        try {
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = fact.newDocumentBuilder();
            Document doc = builder.parse(file);
            validCatalog = true;
        } catch (Exception ex) {
            // invalid catalog...
            log("Skip Invalid SU Catalog:\n    "+file.getAbsolutePath()); 
        }
        return validCatalog;
    }
    
    // catalogDirLoc: <compapp>/src/jbiServiceUnits/META-INF
    private void MergeSeJarCatalogs(String catalogDirLoc) { 
        File catalogDir = new File(catalogDirLoc);
        if (!catalogDir.exists()) {  // no catalog...
            return;
        }
        
        // 1. loop thru project subdirs
        List<File> catalogFiles = new ArrayList<File>();
        File[] children = catalogDir.listFiles();
        if (children == null) {
            return; // no children...
        }
        
        for (File child : children) {
            File catalogFile = new File(child, "catalog.xml"); //SU_CATALOGXML_PATH);
            if (catalogFile.exists()) {
                // check for valid catalog format...
                if (isValid(catalogFile)) {
                    catalogFiles.add(catalogFile);
                }
            }
        }
        
        if (catalogFiles.size() < 1) {
            return; // no catalog..
        }
        
        // 2. merge catalog.xml
        Document mergedCatalogDoc = null;
        
        // src/jbiServiceUnits/catalog.xml
        mergedCatalogFile = new File(catalogDir.getParentFile(), "catalog.xml"); 
        try {
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = fact.newDocumentBuilder();
            
            for (int i = 0; i < catalogFiles.size(); i++) {
                File catalogFile = catalogFiles.get(i);
                if (i == 0) {                    
                    mergedCatalogDoc = 
                            getUpdatedCatalogDocument(catalogFile, builder);
                } else {
                    Document catalogDoc = 
                            getUpdatedCatalogDocument(catalogFile, builder);                    
                    importCatalog(mergedCatalogDoc, catalogDoc);
                }
            }
            
            mergedCatalogFile.createNewFile();       
            
            DOMSource src = new DOMSource(mergedCatalogDoc);
            FileOutputStream fos = new FileOutputStream(mergedCatalogFile);
            StreamResult rest = new StreamResult(fos);
            TransformerFactory transFact = TransformerFactory.newInstance();
            Transformer transformer = transFact.newTransformer();
            transformer.transform(src, rest);
            fos.flush();
            fos.close();
        } catch (Exception ex) {
            log("Exception: A processing error occurred; " + ex);
        }        
    }
    
    // Before merging the catalog file, we need to update the catalog to 
    // include the extra SE SU directory under src/jbiServiceUnits/META-INF/ 
    // to avoid conflict from multiple SE SUs.
    private Document getUpdatedCatalogDocument(File catalogFile, 
            DocumentBuilder builder)
            throws Exception {
        
        File sesuDir = catalogFile.getParentFile();
        String sesuName = sesuDir.getName();
        
        Document doc = builder.parse(catalogFile);
        
        // TODO: this might not be enough
        NodeList systemNodes = doc.getElementsByTagName("system");
        for (int i = 0; i < systemNodes.getLength(); i++) {
            Element systemNode = (Element) systemNodes.item(i);
            String uri = systemNode.getAttribute("uri");
            
            if (uri != null) {
//                // Tmp fix to make valid URI (#130092)
//                uri = uri.replaceAll("\\\\", "/");
                URI realUri = new URI(uri);
                
                if (realUri.getScheme() == null) {
                    /*
                    uri = "../" + sesuName + "/META-INF/" + uri;
                    
                    // correct the URI (get rid of "META-INF/../")
                    uri = uri.replace("/META-INF/..", "");                    
                    */
                    
                    uri = sesuName + "/" + uri;
                    systemNode.setAttribute("uri", uri);
                }
            }
            
//            if (uri != null && 
//                    // make sure the catalog data is along with the catalog.xml
//                    new File(sesuDir, uri).exists()) {
//                uri = sesuName + "/" + uri;
//                systemNode.setAttribute("uri", uri);
//            }
        }
        
        return doc;
    }
    
    private void importCatalog(Document mergedDoc, Document doc) {
        try {
            Element mergedRoot = mergedDoc.getDocumentElement();
            Element root = doc.getDocumentElement();
            NodeList childNodes = root.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node childNode = childNodes.item(i);
                
                // todo: Need to check for duplicated entries...
                
                childNode = mergedDoc.importNode(childNode, true);
                mergedRoot.appendChild(childNode);
            }
        } catch (Exception ex) {
            log("Exception: A processing error occurred; " + ex);
        }
    }
    
    /**
     * Generates service assembly jbi.xml (based on the ASI.xml document).
     * 
     * @param cc
     * @param outputBCNames   a set of BC names which exist in the SA jbi.xml
     * @param conFileLoc
     * @param jbiFileLoc
     */
    private void generateServiceAssemblyDescriptor(ConnectionResolver connectionResolver,
            String conFileLoc,
            String jbiFileLoc,
            Document casaDocument) {  
        
        Set<String> outputBCNames = connectionResolver.getBCNames();
        
        try {
            
            SAConnectionsBuilder dd = new SAConnectionsBuilder();
            Project p = getProject();
            String saName = AntProjectHelper.getServiceAssemblyID(p);
            String saDescription = AntProjectHelper.getServiceAssemblyDescription(p);
            dd.buildDOMTree(connectionResolver, saName, saDescription, casaDocument);
            dd.writeToFile(conFileLoc);
            
            if (jbiDocument == null) {
              return;
            }
            NodeList sas = jbiDocument.getElementsByTagName("service-assembly");
            if ((sas != null) && (sas.getLength() > 0)) {
                Element sa = (Element) sas.item(0);
                
                // 1. loop through sus and remove unused bc components
                NodeList suList = jbiDocument.getElementsByTagName("service-unit");
                for (int i = suList.getLength() - 1; i >= 0; i--) {
                    Element su = (Element) suList.item(i);
                    NodeList cns = su.getElementsByTagName("component-name");
                    if (cns != null && cns.getLength() > 0) {
                        Element cn = (Element) cns.item(0);
                        String compName = cn.getFirstChild().getNodeValue();
                        if (bcNames.contains(compName) &&  // is bc
                                !outputBCNames.contains(compName)) { // not being used
                            sa.removeChild(su);
                        }
                    }
                }
                
                // 2. add connections (and qos:connections)
                List<Element> connectionsElements = 
                    dd.createConnections(connectionResolver, jbiDocument, casaDocument);
                for (Element connections : connectionsElements) {
                    sa.appendChild(connections);
                }               
                
                // normalize the document
                sa.normalize();
                NodeList children = sa.getChildNodes();
                for (int i = children.getLength() - 1; i >= 0; i--) {
                    Node child = children.item(i);
                    String nodeValue = child.getNodeValue();
                    if (nodeValue != null && nodeValue.trim().length() == 0) {
                        sa.removeChild(child);
                    }
                }
            }
            
            // 3. update namespaces
            NodeList rs = jbiDocument.getElementsByTagName("jbi");
            if ((rs != null) && (rs.getLength() > 0)) {
                Element root = (Element) rs.item(0);
                Map<String, String> map = connectionResolver.getNamespaceMap();
                for (String key : map.keySet()) {
                    if (key != null) {
                        String value = map.get(key);
                        root.setAttribute("xmlns:" + key, value);
                    }
                }
            }
                        
            XmlUtil.writeToFile(jbiFileLoc, jbiDocument);
        } catch (Exception e) {
            e.printStackTrace();
            log(" Build SA Descriptor Failed: " + e.toString());
        }
    }
        
    /**
     * Loads all the binding component names from ComponentInformation.xml.
     *
     * @param ciFileLoc    file location for ComponentInformation.xml
     */
    private List<String> loadBindingComponentNames(String ciFileLoc) throws Exception {
        List<String> ret = new ArrayList<String>();
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);

        File ciFile = new File(ciFileLoc);
        if (!ciFile.exists()) {
            throw new FileNotFoundException(ciFileLoc + " is missing.");
        }

        Document document =
                factory.newDocumentBuilder().parse(ciFile);
        NodeList compInfoNodeList = document.getElementsByTagName("component-info");

        for (int i = 0, isize = compInfoNodeList.getLength(); i < isize; i++) {
            Element compInfo = (Element) compInfoNodeList.item(i);
            Element typeElement = (Element) compInfo.getElementsByTagName("type").item(0);
            String compType = typeElement.getFirstChild().getNodeValue();
            if (compType.equalsIgnoreCase("binding")) {
                Element nameElement = (Element) compInfo.getElementsByTagName("name").item(0);
                String compName = nameElement.getFirstChild().getNodeValue();
                ret.add(compName);
            }
        }
        
        return ret;
    }
    
    private void loadAssemblyInfo(String asiFileLoc) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);

        File asiFile = new File(asiFileLoc);
        if (!asiFile.exists()) {
            throw new FileNotFoundException(asiFileLoc + " is missing.");
        }

        jbiDocument = factory.newDocumentBuilder().parse(new File(asiFileLoc));

        // Load service unit jar names
        suJarNames = new ArrayList<String>();
        NodeList jarNodeList = jbiDocument.getElementsByTagName("artifacts-zip");
        for (int i = 0, isize = jarNodeList.getLength(); i < isize; i++) {
            Node jarNode = jarNodeList.item(i);
            String jarName = jarNode.getFirstChild().getNodeValue();
            suJarNames.add(jarName);
        }
    }
    
    private String getShortName_Fallback(String source) {
        // source: ../SynchronousSample/build/SEDeployment.jar
        //           2                 1
        // shortName: SynchronousSample.jar
        String name = new String(source);
        int idx1 = source.indexOf("/build/SEDeployment.jar");
        if (idx1 > 0) {
            int idx2 = source.lastIndexOf('/', idx1 - 1);
            if (idx2 < 0) {
                idx2 = source.lastIndexOf('\\', idx1 - 1); // try other..
            }
            
            if ((idx1 > 0) && (idx2 > 0)) {
                //return source.substring(idx2 + 1, idx1) + "@SEDeployment.jar";
                return source.substring(idx2 + 1, idx1) + ".jar";
            }
        } else {
            // source: ../SynchronousSample/build/SynchronousSample.jar
            //                                   3
            int idx3 = source.lastIndexOf('/');
            return source.substring(idx3 + 1);
        }
        
        return name;
    }   
    
    private String getShortName(String source) {
        
        File file = new File(source);

        // The following doesn't work outside of IDE.
        /*
        FileObject projDirFO = FileUtil.toFileObject(file.getParentFile());
        ProjectManager projectManager = ProjectManager.getDefault();
        while (!projectManager.isProject(projDirFO)) {
            projDirFO = projDirFO.getParent();
        }
        org.netbeans.api.project.Project proj = 
                projectManager.findProject(projDirFO);
        ProjectInformation projInfo = 
                proj.getLookup().lookup(ProjectInformation.class);
        String projName = projInfo.getDisplayName();
        return projName + source.substring(source.lastIndexOf("."));
        */

        // get project display name from nbproject/project.xml
        // /project/configuration/data/name
        File projDir = file.getParentFile();

        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.equals("nbproject");
            }
        };

        try {
            while (true) {
                File[] files = projDir.listFiles(filter);
                if (files != null && files.length > 0) {
                    File projectXmlFile = new File(files[0], "project.xml");
                    DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = fact.newDocumentBuilder();
                    Document document = builder.parse(projectXmlFile);
                    Element nameElement = 
                            (Element) document.getElementsByTagName("name").item(0);                
                    String projDisplayName = nameElement.getTextContent();
                    return projDisplayName + source.substring(source.lastIndexOf(".")); //?
                }

                projDir = projDir.getParentFile();
            }
        } catch (Exception e) {
            log(e.getMessage());
            return getShortName_Fallback(source);
        }
    }
    
    private String getLocalJavaEEJarPath(String dir, String subProjectJar){
        File sJar = null;
        String ret = null;
        if (subProjectJar != null){
            sJar = new File(subProjectJar);
            ret = dir + "/" + sJar.getName(); //NOI18N
        }
        return ret;
    }

    private void createEndpointsFrom(String jarFile)  throws Exception {
        JarFile jar = new JarFile(jarFile);
        byte[] buffer = new byte[1024];
        int bytesRead;
        
        try {
            Enumeration entries = jar.entries();
            StringBuffer jbiXml = new StringBuffer();
            
            while (entries.hasMoreElements()) {
                JarEntry entry = (JarEntry) entries.nextElement();
                String fileName = entry.getName().toLowerCase();
                
                if (fileName.equalsIgnoreCase(SU_JBIXML_PATH)) {
                    InputStream is = jar.getInputStream(entry);
                    while ((bytesRead = is.read(buffer)) != -1) {
                        jbiXml.append(new String(buffer, 0, bytesRead, "UTF-8"));
                    }
                    
                    is.close();
                    is = null;
                    
                    Document jbiDoc = XmlUtil.createDocumentFromXML(true, jbiXml.toString());
                    createEndpoints(jbiDoc.getElementsByTagName("provides"), true);
                    createEndpoints(jbiDoc.getElementsByTagName("consumes"), false);
                    break;
                }
            }
        } catch (IOException ex) {
            log("Operation aborted due to : " + ex);
        } finally {
            try {
                jar.close();
            } catch (IOException ignored) {
                // ignore this..
            }
        }
        
    }
    
    private void copyJarFileWithEndpointDecoration(String inFile, String outFile,
            Document casaDocument)
            throws Exception {
        byte[] buffer = new byte[1024];
        int bytesRead;
        
        JarFile jar = new JarFile(inFile);
        JarOutputStream newJar = new JarOutputStream(new FileOutputStream(
                outFile));
        
        try {
            Enumeration entries = jar.entries();
            // PortMapContainer pc = null;
            boolean hasJbiXml = false;
            boolean copyJbiXml = false;
            String jbiXml = "";
            
            while (entries.hasMoreElements()) {
                JarEntry entry = (JarEntry) entries.nextElement();
                
                String fileName = entry.getName().toLowerCase();
                InputStream is = jar.getInputStream(entry);
                copyJbiXml = false;
                
                if (fileName.equalsIgnoreCase(SU_JBIXML_PATH)) {
                    // found existing jbi.xml
                    hasJbiXml = true;
                    copyJbiXml = true;
                    newJar.putNextEntry(new JarEntry(entry.getName()));
                } else { 
                    newJar.putNextEntry(entry);
                }
                
                while ((bytesRead = is.read(buffer)) != -1) {
                    if (copyJbiXml) {
                        jbiXml += new String(buffer, 0, bytesRead, "UTF-8");
                    } else {
                        newJar.write(buffer, 0, bytesRead);                        
                    }
                }
                
                is.close();
                is = null;
                
                if (copyJbiXml) {
                    Document jbiDoc = XmlUtil.createDocumentFromXML(true, jbiXml);
                    ServiceUnitDescriptorEnhancer.decorateEndpoints(jbiDoc, casaDocument);
                    newJar.write(XmlUtil.writeToBytes(jbiDoc));
                }
            }
            
            // create a temp jbi.xml
            if (hasJbiXml) {
                // todo: load into PtConnections...
                // log("JBI.XML:\n"+jbiXml);
                Document jbiDoc = XmlUtil.createDocumentFromXML(true, jbiXml);
                createEndpoints(jbiDoc.getElementsByTagName("provides"), true);
                createEndpoints(jbiDoc.getElementsByTagName("consumes"), false);
            }
            
        } catch (IOException ex) {
            log("ERROR: Operation aborted due to : " + ex, Project.MSG_ERR);
        } finally {
            try {
                newJar.close();
                jar.close();
            } catch (IOException ignored) {
                // ignore this..
            }
        }
    }
    
    private void createEndpoints(NodeList nodeList, boolean isProvide) {
        if (nodeList != null) {
            for (int i = 0, node = nodeList.getLength(); i < node; i++) {
                Element pe = (Element) nodeList.item(i);
                String endpointName = pe.getAttribute("endpoint-name");
                QName serviceQName = getNSName(pe, pe.getAttribute("service-name"));
                QName interfaceQName = getNSName(pe, pe.getAttribute("interface-name"));
                Endpoint p = new Endpoint(//"either",
                        endpointName, serviceQName, interfaceQName);
                if (p != null) {
                    String PT = p.getInterfaceQName().toString();
                    PtConnection con = mRepo.getPtConnection(PT);
                    if (con != null) {
                        if (isProvide) {
                            con.addProvide(p);
                        } else {
                            con.addConsume(p);
                        }
                    }
                }
            }
        }
    }
    
    private static QName getNSName(Element e, String qname) {
        if (qname == null) {
            return null;
        }
        int i = qname.indexOf(':');
        if (i > 0) {
            String name = qname.substring(i + 1);
            String prefix = qname.substring(0, i);
            return new QName(getNamespace(e, prefix), name);
        } else {
            return new QName(qname);
        }
    }
    
    /**
     * Gets the namespace from the qname.
     *
     * @param el 
     * @param prefix name prefix of service
     *
     * @return namespace namespace of service
     */
    public static String getNamespace(Element el, String prefix) {
        if ((prefix == null) || (prefix.length() < 1)) {
            return "";
        }
        try {
            NamedNodeMap map = el.getOwnerDocument().getDocumentElement().getAttributes();
            for (int j = 0; j < map.getLength(); j++) {
                Node n = map.item(j);
                String localName = n.getLocalName();
                if (localName != null) {
                    if (n.getLocalName().trim().equals(prefix.trim())) {
                        return n.getNodeValue();
                    }
                }
            }
        } catch (Exception e) {
        }
        
        return "";
    }
        
    private BCSUDescriptorBuilder createBCSUDescriptor(//String outFile, 
            String bcName, ConnectionResolver connectionResolver) {
        
//        String suName = outFile.substring(outFile.lastIndexOf(File.separator) + 1); //TMP
//        suName = suName.substring(0, suName.length() - 4);
        String bcSUDirLoc = serviceUnitsDirLoc + File.separator + bcName;
        File bcSUDir = new File(bcSUDirLoc);
        if (!bcSUDir.exists()) {
            bcSUDir.mkdir();
        }
        if (!bcSUDir.isDirectory()) {
            throw new RuntimeException(bcSUDirLoc + " is not a directory");
        }
        
        BCSUDescriptorBuilder dd = new BCSUDescriptorBuilder();
        try {
            dd.buildDOMTree(connectionResolver, bcName);
            dd.writeToFile(bcSUDirLoc);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return dd;
    }
    
    private String getProjectName() {
        Project proj = getProject();
        return AntProjectHelper.getServiceAssemblyID(proj);
    }
    
    private String getCompAppWSDLFileName() {
        return getProjectName() + ".wsdl";
    }
        
    private void createBCJar(String outFile,
            JarFile genericBCJar,
            /*boolean isCompAppWSDLNeeded,*/ 
            BCSUDescriptorBuilder bcsuDescriptorBuilder,
            String bcName)
            throws Exception {
        byte[] buffer = new byte[1024];
        int bytesRead;
        
        //String compAppWSDLFileName = getCompAppWSDLFileName();
        
        JarOutputStream newJar = new JarOutputStream(new FileOutputStream(outFile));
        
        try {
            Enumeration<JarEntry> jarEntries = genericBCJar.entries();
            
            while (jarEntries.hasMoreElements()) {
                JarEntry jarEntry = jarEntries.nextElement();
                InputStream is = genericBCJar.getInputStream(jarEntry);
                
                String jarEntryName = jarEntry.getName();
                
                // TODO: update casa wsdl entry in generic bc jar file.
                //if (jarEntryName.equals(compAppWSDLFileName)) {
                if (jarEntryName.toLowerCase().endsWith(".wsdl")) {
//                    // Quick fix for J1: If the casa wsdl file doesn't contain 
//                    // active endpoints, then we skip packaging the casa wsdl entry.
//                    // (Future improvement: This rule should apply to all the 
//                    // wsdl files.)
//                    if (isCompAppWSDLNeeded) {
                    
                    newJar.putNextEntry(new JarEntry(jarEntryName));
                    
                    // HACK: remove "../jbiServiceUnits/" and "../jbiasa" from 
                    // import elements' location
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8")); // FIXME: encoding
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.indexOf("import") != -1) {
                            if (line.indexOf("location=\"../" + JbiProjectConstants.FOLDER_JBISERVICEUNITS + "/") != -1) {
                                line = line.replace("../" + JbiProjectConstants.FOLDER_JBISERVICEUNITS + "/", "");
                            }
                            if (line.indexOf("location=\"../" + JbiProjectConstants.FOLDER_JBIASA + "/") != -1) {
                                line = line.replace("../" + JbiProjectConstants.FOLDER_JBIASA + "/", "");
                            }
                        }
                        newJar.write(line.getBytes("UTF-8"));
                        newJar.write(System.getProperty("line.separator").getBytes("UTF-8"));
                    }
                    reader.close();
//                    }
                } else if (bcNames.contains(jarEntryName.substring(0, jarEntryName.length() - 1))) {
                    // Skip (empty) BC SU directory, (for example, "sun-http-binding/")
                    // which shouldn't go into the generic bc jar file in the 
                    // first place.
                } else if (jarEntryName.equals(".ignore")) {
                    // Ignore ".ignore" entry.
                } else {
                    
                    newJar.putNextEntry(jarEntry);
                    
                    while ((bytesRead = is.read(buffer)) != -1) {
                        newJar.write(buffer, 0, bytesRead);
                    }
                }
                is.close();
                is = null;
            }
            
            // create BC SU's jbi.xml
            JarEntry jbientry = new JarEntry(SU_JBIXML_PATH);
            newJar.putNextEntry(jbientry);            
            newJar.write(bcsuDescriptorBuilder.writeToBytes());
            
            // create the catalog.xml
            if (mergedCatalogFile != null) {
                JarEntry catalogentry = new JarEntry(SU_CATALOGXML_PATH);
                newJar.putNextEntry(catalogentry);
                InputStream in = new FileInputStream(mergedCatalogFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    newJar.write(buf, 0, len);
                }
                in.close();
            }

            // IZ#126214... add WSIT Callback java class to http-soap bc
            if (SOAP_BC.equals(bcName) && (wsitCallbackProjects != null)) {
                StringTokenizer st = new StringTokenizer(wsitCallbackProjects, ";"); // NOI18N
                while (st.hasMoreTokens()) {
                    String projLoc = st.nextToken();
                    File buildDir = new File(projLoc + "/build/classes/"); // NOI18N
                    if (buildDir.exists()) {
                        String path = "";
                        copyWsitCallbackClass(buildDir, newJar, path);
                    }
                }

            }
            
        } catch (IOException ex) {
            log("Operation aborted due to : " + ex);
        } finally {
            try {
                newJar.close();
                // jar.close();
            } catch (IOException ignored) {
                // ignore this..
            }
        }
    }

    private void copyWsitCallbackClass(File file, JarOutputStream newJar, String path) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            String fname = file.getName();
            String pname = fname.equals("classes") ? "" : path+fname+"/"; // NOI18N
            for (int i=0; i<children.length; i++) {
                copyWsitCallbackClass(children[i], newJar, pname);
            }
        } else {
            String fname = path+file.getName();
            try {
                // System.out.println("COPYING: "+fname);
                JarEntry classEntry = new JarEntry(fname);
                newJar.putNextEntry(classEntry);
                InputStream in = new FileInputStream(file);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    newJar.write(buf, 0, len);
                }
                in.close();
            } catch (IOException ex) {
                log("Copying " + fname + " failed due to :\n" + ex); // NOI18N
            }
        }

    }
    
    // TODO: Move me to a separate ant task when some mandatory change is 
    // required for ATS.
    public void validateCompAppProject() throws BuildException {
                
        boolean isError = false;
        
        Validation validation = new MyValidation();
        
        for (WSDLModel wsdlModel : mRepo.getWsdlCollection()) {   
            String wsdlPath = wsdlRepository.getWsdlFilePath(wsdlModel);
//            System.out.println("wsdlPath: " + wsdlPath);
//            System.out.println("serviceUnitsDirLoc: " + serviceUnitsDirLoc);
         
            // skip validation of WSDLs defined in the SU projects
            if (wsdlPath.startsWith(serviceUnitsDirLoc)) {
                continue;
            }  
            
            log("  validating " + wsdlPath);
            validation.validate(wsdlModel, ValidationType.COMPLETE);        

            for (ResultItem resultItem : validation.getValidationResult()) {
               logValidationErrors(new File(wsdlPath), resultItem);

               if(resultItem.getType() == Validator.ResultType.ERROR) {
                   isError = true;
               }
            }
        }
        
        if (isError) {
            throw new BuildException("Found validation error(s).");
        }
    }
       
    private void logValidationErrors(File file, ResultItem resultItem) {
        int lineNumber = 0;
        int columnNumber = 0;
        String errorDescription = resultItem.getDescription();
        
        // FIXME: getting null errorDescription when building from command line.
        // It seems such "error" can be ignored. Needs further investigation.
        // On the other hand, debbuging ant task with classpath set from 
        // command line works fine.
        if (errorDescription == null) {
            return;
        }
        
        String msgType = resultItem.getType().name();
        Component component = resultItem.getComponents();

        if (component != null) {
            lineNumber = ModelUtil.getLineNumber(component);
            columnNumber = ModelUtil.getColumnNumber(component);
            showError(file, columnNumber, lineNumber, errorDescription, msgType);
        } else {
            columnNumber = resultItem.getColumnNumber();
            lineNumber = resultItem.getLineNumber();
            showError(file, columnNumber, lineNumber, errorDescription, msgType);
        }
    }

    private void showError(File file, int columnNumber, int lineNumber, 
            String errorDescription, String msgType) {
        StringBuffer lineNumStr = new StringBuffer(5);
        StringBuffer columnNumStr = new StringBuffer(5);

        if (lineNumber != -1) {
            lineNumStr.append(":");
            lineNumStr.append(lineNumber);
            lineNumStr.append(",");
        }
        if (columnNumber != -1) {
            columnNumStr.append(" column:");
            columnNumStr.append(columnNumber);
            columnNumStr.append(" ");
        }
        msgType = msgType + ": ";
        StringBuffer msg = new StringBuffer(100);
        msg.append(msgType);

        if (file != null) {
            msg.append(file.getPath());
        }
        msg.append(lineNumStr);
        msg.append(columnNumStr);
        msg.append(System.getProperty("line.separator"));        
        msg.append(errorDescription);
        msg.append(System.getProperty("line.separator"));
        
        log(msg.toString());
    }

    /*
    private Document decorateSuJbiDocument(Document suJbiDocument, 
            Document casaDocument, String bcName) {
        
        Document ret = suJbiDocument; //(Document) suJbiDocument.cloneNode(true);
        
        NodeList consumesNodeList = casaDocument.getElementsByTagName(
                JBI_CONSUMES_ELEM_NAME);
        for (int i = 0; i < consumesNodeList.getLength(); i++) {
            Element consumes = (Element) consumesNodeList.item(i);
            
        }
        
        
        
        NodeList bcsuNodeList = casaDocument.getElementsByTagName(
                CASA_BINDING_COMPONENT_SERVICE_UNIT_ELEM_NAME);
        for (int i = 0; i < bcsuNodeList.getLength(); i++) {
            Element bcsu = (Element) bcsuNodeList.item(i);
            if (!bcsu.getAttribute(CASA_COMPONENT_NAME_ATTR_NAME).equals(bcName)) {
                continue;
            }
            
            NodeList consumesNodeList = casaDocument.getElementsByTagName(
                CASA_CONSUMES_ELEM_NAME);
            for (int j = 0; j < bcsuNodeList.getLength(); j++) {
                Element consumes = (Element) bcsuNodeList.item(j);
                NodeList consumesChildren = consumes.getChildNodes();
                for (int k = 0; k < consumesChildren.getLength(); k++) {
                    Node consumesChild = consumesChildren.item(k);
                    if (consumesChild instanceof Element) {
                        Element 
                    }
                }
            }
        }
        
        return ret;
    }
    */
    
    /*
    private WSDLModel getCompAppWsdlModel() {
        String compappWSDLFileName = getCompAppWSDLFileName();
        for (WSDLModel wsdlModel : mRepo.getWsdlCollection()) {
            String wsdlPath = wsdlRepository.getWsdlFilePath(wsdlModel);
            if (wsdlPath.endsWith(compappWSDLFileName)) { // FIXME
                return wsdlModel;
            }
        }
        return null;
    }    
    
    private List<Endpoint> getEndpointsInWsdlModel(WSDLModel wsdlModel) {
        List<Endpoint> ret = new ArrayList<Endpoint>();
        
        if (wsdlModel != null) {
            Definitions defs = wsdlModel.getDefinitions();
            String tns = defs.getTargetNamespace();
            for (Service service : defs.getServices()) {
                String serviceName = service.getName();
                for (Port port : service.getPorts()) {
                    String portName = port.getName();
                    Endpoint endpoint = new Endpoint(
                            portName, new QName(tns, serviceName), null); // don't care about interface name for now
                    ret.add(endpoint);
                }
            }
        }
        
        return ret;
    }
    
    private List<String> getBindingComponentsUsingCompAppWsdl(
            Map<String, List<Connection>[]> bcConnections) {
        
        List<String> ret = new ArrayList<String>();
        
        WSDLModel compAppWsdlModel = getCompAppWsdlModel();           
        List<Endpoint> endpoints = getEndpointsInWsdlModel(compAppWsdlModel);
            
        if (endpoints.size() > 0) {
            for (String bcName : bcConnections.keySet()) {
                List<Endpoint> bcEndpoints = new ArrayList<Endpoint>();
                
                List<Connection>[] clist = bcConnections.get(bcName);
                for (Connection connection : clist[0]) {
                    Endpoint e = connection.getConsume();
                    bcEndpoints.add(e);
                }
                
                for (Connection connection : clist[1]) {
                    Endpoint e = connection.getProvide();
                    bcEndpoints.add(e);
                }
                
                for (Endpoint e : endpoints) {
                    String endpointName = e.getEndpointName();
                    QName serviceQName = e.getServiceQName();
                    for (Endpoint bce : bcEndpoints) {
                        // we don't need to check interface name.
                        if (bce.getEndpointName().equals(endpointName) &&
                                bce.getServiceQName().equals(serviceQName)) {
                            ret.add(bcName);
                            break;
                        }
                    }
                    
                    if (ret.contains(bcName)) {
                        break;
                    }
                }
            }
        }
        
        return ret;
    }
    */
    
    //--------------------------------------------------------------------
    // 09/12/07, T.Li, code add to support JavaEE endpoint filtering
    //--------------------------------------------------------------------

    private void filterJavaEEEndpoints(ConnectionResolver cR, List<String> saEEJarPaths,
                String serviceUnitsDirLoc) {
        String NS = AbstractProject.MAPPING_NS;
        for (String path : saEEJarPaths) {
            File jbixml = new File(serviceUnitsDirLoc + "/" + getJavaEEProjName(path) + "/jbi.xml");
            try {
                // 1. get the EE proj jbi.xml from jbiServiceUnits
                Document jbiDoc = XmlUtil.createDocument(true, jbixml);
                NodeList nodeList = jbiDoc.getElementsByTagName(JBI_PROVIDES_ELEM_NAME);
                List<Element> uList = new ArrayList<Element>();
                getUnconnectedNodes(uList, nodeList, cR);
                nodeList = jbiDoc.getElementsByTagName(JBI_CONSUMES_ELEM_NAME);
                getUnconnectedNodes(uList, nodeList, cR);

                if (uList.size() > 0) {
                    // 2. remove unconnected endpoint from jbi.xml
                    Node srvNode = jbiDoc.getElementsByTagName(JBI_SERVICES_ELEM_NAME).item(0);
                    Node mapNode = jbiDoc.getElementsByTagNameNS(NS, AbstractProject.MAPPING_ELEMS).item(0);
                    NodeList mapList = jbiDoc.getElementsByTagNameNS(NS, AbstractProject.MAPPING_JAVA_ELEM);
                    Hashtable eptMap = new Hashtable();
                    for (int i=0; i<mapList.getLength(); i++) {
                        Element map = (Element) mapList.item(i);
                        String eptName = map.getAttribute(JBI_ENDPOINT_NAME_ATTR_NAME);
                        eptMap.put(eptName, map.getParentNode());
                    }
                    for (Element elm : uList) {
                        String eptName = elm.getAttribute(JBI_ENDPOINT_NAME_ATTR_NAME);
                        srvNode.removeChild(elm);
                        if (mapNode != null) {
                            mapNode.removeChild((Node) eptMap.get(eptName));
                        }
                    }
                    byte[] jbiNew = XmlUtil.writeToBytes(jbiDoc);

                    // 3. update EE proj jar in the build dir
                    copyJavaEEJarFile(path, jbiNew);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void copyJavaEEJarFile(String inFile, byte[] jbixml) {
        byte[] buffer = new byte[1024];
        int bytesRead;
        File jarFile = null;
        File tempJarFile = null;
        JarFile jar = null;
        JarOutputStream newJar = null;
        boolean jarUpdated = false;
        try {
            jarFile = new File(inFile);
            tempJarFile = new File(inFile + ".new");
            jar = new JarFile(jarFile);
            newJar = new JarOutputStream(new FileOutputStream(tempJarFile));
            Enumeration entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = (JarEntry) entries.nextElement();

                String fileName = entry.getName().toLowerCase();
                if (fileName.equalsIgnoreCase(SU_JBIXML_PATH)) {
                    // replace only if there is already a jbi.xml
                    JarEntry jbiEntry = new JarEntry(SU_JBIXML_PATH);
                    newJar.putNextEntry(jbiEntry);
                    newJar.write(jbixml);
                } else { // just copying...
                    newJar.putNextEntry(entry);
                    InputStream is = jar.getInputStream(entry);
                    while ((bytesRead = is.read(buffer)) != -1) {
                        newJar.write(buffer, 0, bytesRead);
                    }
                    is.close();
                    is = null;
                }
            }
            jarUpdated = true;
        } catch (Exception ex) {
            log("Copying EE jar aborted due to : " + ex);
            ex.printStackTrace();
        } finally {
            try {
                if (newJar != null) {
                    newJar.close();
                }
                if (jar != null) {
                    jar.close();
                }
                if (jarUpdated) {
                    jarFile.delete();
                    tempJarFile.renameTo(jarFile);
                } else {
                    if (tempJarFile != null) {
                        tempJarFile.delete();
                    }
                }
            } catch (IOException ignored) {
                log("Copying EE jar aborted finally due to : " + ignored);
            }
        }
    }

    private String getJavaEEProjName(String subProjectJar){
        File sJar = null;
        String ret = null;
        if (subProjectJar != null){
            sJar = new File(subProjectJar);
            String sJarName = sJar.getName();
            int ext = sJarName.indexOf('.');
            if (ext > 0) {
                sJarName = sJarName.substring(0, ext);
            }
            ret = sJarName; //NOI18N
        }
        return ret;
    }

    private void getUnconnectedNodes(List<Element> uList, NodeList nodeList, ConnectionResolver cR) {
        for (int i = 0, node = nodeList.getLength(); i < node; i++) {
            Element pe = (Element) nodeList.item(i);
            String endpointName = pe.getAttribute(JBI_ENDPOINT_NAME_ATTR_NAME);
            QName serviceQName = getNSName(pe, pe.getAttribute(JBI_SERVICE_NAME_ATTR_NAME));
            QName interfaceQName = getNSName(pe, pe.getAttribute(JBI_INTERFACE_NAME_ATTR_NAME));
            Endpoint p = new Endpoint(endpointName, serviceQName, interfaceQName);
            boolean connected = cR.isConnected(p);
            //System.out.println("\tEntPt: "+endpointName+ ", connected: "+connected);
            if (! connected) {
                uList.add(pe);
            }
        }
    }

}
