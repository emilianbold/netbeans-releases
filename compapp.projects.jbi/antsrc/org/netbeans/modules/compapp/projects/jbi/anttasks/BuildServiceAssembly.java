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

package org.netbeans.modules.compapp.projects.jbi.anttasks;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
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
import org.netbeans.modules.compapp.projects.jbi.MigrationHelper;
import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectConstants;
import org.netbeans.modules.compapp.projects.jbi.descriptor.XmlUtil;
import org.netbeans.modules.compapp.projects.jbi.descriptor.endpoints.model.Connection;
import org.netbeans.modules.compapp.projects.jbi.descriptor.endpoints.model.Endpoint;
import org.netbeans.modules.compapp.projects.jbi.descriptor.endpoints.model.PtConnection;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
    
    private boolean saInternalRouting = true;
    
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
    public void execute() throws BuildException {
        showLog = showLogOption.equalsIgnoreCase("true");
        JarFile genericBCJar = null;
        try {                        
            Project p = this.getProject();
            String confDir = p.getProperty((JbiProjectProperties.META_INF));
            if ((confDir == null) || (confDir.length() < 1)) {
                return;
            }
            
            String javaeeJars = p.getProperty(JbiProjectProperties.JBI_JAVAEE_JARS);
            String jars = p.getProperty((JbiProjectProperties.JBI_CONTENT_ADDITIONAL));
            
            projDirLoc = p.getProperty("basedir") + File.separator;            
            String srcDirLoc = projDirLoc + "src" + File.separator;
            String confDirLoc = srcDirLoc + "conf" + File.separator;
            
            serviceUnitsDirLoc = srcDirLoc + JbiProjectConstants.FOLDER_JBISERVICEUNITS;            
            jbiasaDirLoc = srcDirLoc + JbiProjectConstants.FOLDER_JBIASA;               
            
            // Command-line support
            MigrationHelper.migrateCasaWSDL(jbiasaDirLoc, getProjectName());
            
            // Command-line support
//            MigrationHelper.migrateCompAppProperties(projDirLoc, null);
            
            
            String catalogDirLoc = serviceUnitsDirLoc
                    + File.separator + "META-INF";
                    //+ File.separator + "catalogData";
            
            // String mapFileLoc = projPath + confDir + File.separator + "portmap.xml";
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
            
            // create project wsdl repository...
            mRepo = new wsdlRepository(p, this);
            
            String jbiFileLoc = buildDir + "/META-INF/jbi.xml";
            String genericBCJarFileLoc = buildDir + "/BCDeployment.jar";            
            
            File bDir = new File(buildDir);
            if (!bDir.exists()) {
                bDir.mkdirs();
            }
            
            // Get all the bc names
            // Use ComponentInformation.xml instead of BindingComponentInformatino.xml
            // to get all the binding component names!
            String ciFileLoc = confDirLoc + "ComponentInformation.xml";
            bcNames = loadBindingComponentNames(ciFileLoc);
            
            String asiFileLoc = confDirLoc + "AssemblyInformation.xml";
            loadAssemblyInfo(asiFileLoc);
            
            // generate the SE jar file list
            // loop thru SE suprojects and copying SE deployment jars            
            List<String> srcJarPaths = getJarList(jars);
            List<String> javaEEJarPaths = getJarList(javaeeJars);
            
            for (String srcJarPath : srcJarPaths) {
                if ((javaEEJarPaths != null) && (javaEEJarPaths.contains(srcJarPath))){
                    srcJarPath = getLocalJavaEEJarPath(buildDir, srcJarPath);
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
                    log(" copying Sub-Assembly: " + destJarPath);
                    copyJarFile(srcJarPath, destJarPath);
                }
            }
            
            CasaBuilder casaBuilder = new CasaBuilder(project, mRepo, this);
            final Document oldCasaDocument = casaBuilder.getOldCasaDocument();
            
            // Resolve connections
            ConnectionResolver connectionResolver = 
                    new ConnectionResolver(this, showLog, saInternalRouting);          
            connectionResolver.resolveConnections(mRepo, oldCasaDocument);
            
            
            // Write connections to connections.xml
            CreateSAConnections dd = new CreateSAConnections();
            dd.buildDOMTree(connectionResolver, mRepo, p);
            dd.writeToFile(connectionsFileLoc);
            
            // Generate SA jbi.xml,
            generateServiceAssemblyDescriptor(
                    connectionResolver, connectionsFileLoc, jbiFileLoc);
            
            // Todo: 08/22/06 merge catalogs
            MergeSeJarCatalogs(catalogDirLoc);
            
            // Generate BC SU jbi.xml and BC SU jar file
            genericBCJar = new JarFile(genericBCJarFileLoc);                                         
            
            // a map mapping binding component name (say, "sun-http-binding")
            // to a list of two connection lists.  
            Map<String, List<Connection>[]> bcConnections = 
                    connectionResolver.getBCConnections();
            
//            List<String> bcsUsingCompAppWsdl = 
//                    getBindingComponentsUsingCompAppWsdl(bcConnections);            
            for (String bcName : connectionResolver.getBCNames()) {
                String bcJarName = null;
                for (int k = 0; k < suJarNames.size(); k++) {
                    String name = suJarNames.get(k);
                    if (name.indexOf(bcName) != -1) {
                        // Use the name defined in ASI.xml
                        bcJarName = buildDir + "/" + name;
                        break;
                    }
                }
                if (bcJarName != null) {
                    log("Create " + bcJarName + " for binding component " + bcName);
                    
                    // Create standalone BC jbi.xml file under src/jbiServiceUnits/<bcName>/.
                    createBCJBIDescriptor(bcJarName, bcName, connectionResolver);
                    
                    // Create build/<bcName>.jar
                    // ToDo: update bc jar endpoints.xml
                    // 1. copy BCjars for each needed BC
                    // 2. create jbi.xml
//                    boolean isCompAppWSDLNeeded = bcsUsingCompAppWsdl.contains(bcName);
                    createBCJar(bcJarName, bcName, genericBCJar, connectionResolver/*, isCompAppWSDLNeeded*/);                    
                } else {
                    log("ERROR: Cannot create binding component jar for " + bcName);
                }
            }
            
            casaBuilder.createCasaDocument(jbiDocument);  
            
        } catch (Exception e) {
            e.printStackTrace();
            log("Build SA Failed: " + e.toString());
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
                catalogFiles.add(catalogFile);
            }
        }
        
        if (catalogFiles.size() < 1) {
            return; // no catalog..
        }
        
        // 2. merge catalog.xml
        Document mergedCatalogDoc = null;
        
        // src/jbiServiceUnits/catalog.xml
        mergedCatalogFile = new File(catalogDir.getParentFile(), "catalog.xml"); // NOI18N
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
            if (uri != null && 
                    // make sure the catalog data is along with the catalog.xml
                    new File(sesuDir, uri).exists()) {
                uri = sesuName + "/" + uri;
                systemNode.setAttribute("uri", uri);
            }
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
            String jbiFileLoc) {  
        
        Set<String> outputBCNames = connectionResolver.getBCNames();
        
        try {
            
            CreateSAConnections dd = new CreateSAConnections();
            dd.buildDOMTree(connectionResolver, mRepo, getProject());
            dd.writeToFile(conFileLoc);
            
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
                
                // 2. add connections
                sa.appendChild(dd.createConnections(connectionResolver, jbiDocument));
                
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
    private List<String> loadBindingComponentNames(String ciFileLoc) {
        List<String> ret = new ArrayList<String>();
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            
            Document document =
                    factory.newDocumentBuilder().parse(new File(ciFileLoc));
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
        } catch (Exception e) {
            log(e.getMessage() + " : " + ciFileLoc);
        }
        
        return ret;
    }
    
    private void loadAssemblyInfo(String asiFileLoc) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            
            jbiDocument = factory.newDocumentBuilder().parse(new File(asiFileLoc));
            
            // Load service unit jar names
            suJarNames = new ArrayList<String>();
            NodeList jarNodeList = jbiDocument.getElementsByTagName("artifacts-zip");
            for (int i = 0, isize = jarNodeList.getLength(); i < isize; i++) {
                Node jarNode = jarNodeList.item(i);
                String jarName = jarNode.getFirstChild().getNodeValue();
                suJarNames.add(jarName);
            }
        } catch (Exception e) {
            log(e.getMessage() + " : " + asiFileLoc);
        }
    }
    
    private String getShortName(String source) {
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
    
    private void copyJarFile(String inFile, String outFile)
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
                newJar.putNextEntry(entry);
                
                if (fileName.equalsIgnoreCase(SU_JBIXML_PATH)) {
                    // found existing jbi.xml
                    hasJbiXml = true;
                    copyJbiXml = true;
                }
                
                while ((bytesRead = is.read(buffer)) != -1) {
                    newJar.write(buffer, 0, bytesRead);
                    if (copyJbiXml) {
                        jbiXml += new String(buffer, 0, bytesRead, "UTF-8");
                    }
                }
                
                is.close();
                is = null;
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
            log("Operation aborted due to : " + ex);
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
        
    private void createBCJBIDescriptor(String outFile, String bcName,
            ConnectionResolver connectionResolver) {
        Project p = this.getProject();
        String projPath = p.getProperty("basedir") + File.separator;
        
        String suName = outFile.substring(outFile.lastIndexOf("/") + 1); //TMP
        suName = suName.substring(0, suName.length() - 4);
        String bcSUDirLoc = serviceUnitsDirLoc + File.separator + suName;
        File bcSUDir = new File(bcSUDirLoc);
        if (!bcSUDir.exists()) {
            boolean success = bcSUDir.mkdir();
        }
        if (!bcSUDir.isDirectory()) {
            throw new RuntimeException(bcSUDirLoc + " is not a directory");
        }
        
        CreateSUDescriptor dd = new CreateSUDescriptor();
        try {
            dd.buildDOMTree(connectionResolver, bcName, mRepo);
            dd.writeToFile(bcSUDirLoc);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private String getProjectName() {
        Project proj = getProject();
        return AntProjectHelper.getServiceAssemblyID(proj);
    }
    
    private String getCasaWSDLFileName() {
        return getProjectName() + ".wsdl";
    }
        
    private void createBCJar(String outFile, String bcName,
            JarFile genericBCJar, ConnectionResolver connectionResolver/*,
            boolean isCompAppWSDLNeeded*/) 
            throws Exception {
        byte[] buffer = new byte[1024];
        int bytesRead;
        
        String casaWSDLFileName = getCasaWSDLFileName();
        
        JarOutputStream newJar = new JarOutputStream(new FileOutputStream(outFile));
        
        try {
            Enumeration<JarEntry> jarEntries = genericBCJar.entries();
            
            while (jarEntries.hasMoreElements()) {
                JarEntry jarEntry = jarEntries.nextElement();
                InputStream is = genericBCJar.getInputStream(jarEntry);
                
                // TODO: update casa wsdl entry in generic bc jar file.
                if (jarEntry.getName().equals(casaWSDLFileName)) {
//                    // Quick fix for J1: If the casa wsdl file doesn't contain 
//                    // active endpoints, then we skip packaging the casa wsdl entry.
//                    // (Future improvement: This rule should apply to all the 
//                    // wsdl files.)
//                    if (isCompAppWSDLNeeded) {
                    
                    newJar.putNextEntry(new JarEntry(casaWSDLFileName));
                    
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
                    
                } else {
                    
                    newJar.putNextEntry(jarEntry);
                    
                    while ((bytesRead = is.read(buffer)) != -1) {
                        newJar.write(buffer, 0, bytesRead);
                    }
                }
                is.close();
                is = null;
            }
            
            // create a jbi.xml
            JarEntry jbientry = new JarEntry(SU_JBIXML_PATH);
            newJar.putNextEntry(jbientry);
            CreateSUDescriptor dd = new CreateSUDescriptor();
            dd.buildDOMTree(connectionResolver, bcName, mRepo);
            newJar.write(dd.writeToBytes());
            
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
      
    private WSDLModel getCompAppWsdlModel() {
        String casaWsdlName = getCasaWSDLFileName();
        for (WSDLModel wsdlModel : mRepo.getWsdlCollection()) {
            String wsdlPath = wsdlRepository.getWsdlFilePath(wsdlModel);
            if (wsdlPath.endsWith(casaWsdlName)) { // FIXME
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
}
