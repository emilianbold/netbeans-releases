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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.logging.Logger;
import javax.xml.namespace.QName;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.netbeans.modules.compapp.projects.jbi.MigrationHelper;
import org.netbeans.modules.compapp.projects.jbi.descriptor.XmlUtil;
import org.netbeans.modules.compapp.projects.jbi.descriptor.endpoints.model.Connection;
import org.netbeans.modules.compapp.projects.jbi.descriptor.endpoints.model.ConnectionContainer;
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
import org.xml.sax.SAXException;

/**
 * Ant task to build jbiserver service assembly
 *
 * @author tli
 * @author jqian
 */
public class BuildServiceAssembly extends Task {
    
    /**
     * DOCUMENT ME!
     */
    String showLogOption = "false";
    
    private boolean showLog = false;
    
    private wsdlRepository mRepo;
    
    private Document jbiDocument;
    
    private byte[] endpoints;
    
    private Logger logger = Logger.getLogger(getClass().getName());
    
    private File catalogFile;
    
    // private boolean jbiRouting = true;
    private String serviceUnitsDirLoc;
    private String jbiasaDirLoc;
    
    private boolean saInternalRouting = true;
    
    // 03/26/07 Ignore Concreate WSDL ports in J2EE projects, T. Li
    private boolean ignoreJ2EEPorts = true;
    
    // binding component name list in BindingComponentInformation.xml
    private List<String> bcNameList;
    
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
            MigrationHelper.migrateCasaWSDL(jbiasaDirLoc, getProjectName());
                        
            Project p = this.getProject();
            String confDir = p.getProperty((JbiProjectProperties.META_INF));
            if ((confDir == null) || (confDir.length() < 1)) {
                return;
            }
            
            String javaeeJars = p.getProperty(JbiProjectProperties.JBI_JAVAEE_JARS);
            String jars = p.getProperty((JbiProjectProperties.JBI_CONTENT_ADDITIONAL));
            
            String projPath = p.getProperty("basedir") + File.separator;            
            String srcDirLoc = projPath + "src" + File.separator;
            String confDirLoc = srcDirLoc + "conf" + File.separator;
            
            serviceUnitsDirLoc = srcDirLoc + "jbiServiceUnits";            
            jbiasaDirLoc = srcDirLoc + "jbiasa";
            
            String catalogDirLoc = serviceUnitsDirLoc
                    + File.separator + "META-INF"
                    + File.separator + "catalogData";
            
            // String mapFileLoc = projPath + confDir + File.separator + "portmap.xml";
            String connectionsFileLoc = confDirLoc + "connections.xml";
            String buildDir = projPath + p.getProperty(JbiProjectProperties.BUILD_DIR);
            
            // create confDir if needed..
            File buildMetaInfDir = new File(buildDir + "/META-INF");
            if (!buildMetaInfDir.exists()) {
                buildMetaInfDir.mkdirs();
            }
            
            // todo: set the default to false for now... 03/15/06
            // jbiRouting = getBooleanProperty(p.getProperty((JbiProjectProperties.JBI_ROUTING)), true);
            saInternalRouting = getBooleanProperty(p.getProperty((JbiProjectProperties.JBI_SA_INTERNAL_ROUTING)), true);
            
            // create project wsdl repository...
            mRepo = new wsdlRepository(p);
            
            String jbiFileLoc = buildDir + "/META-INF/jbi.xml";
            String genericBCJarFileLoc = buildDir + "/BCDeployment.jar";            
            
            File bDir = new File(buildDir);
            if (!bDir.exists()) {
                bDir.mkdirs();
            }
            
            // Get all the bc names into bcNameList.
            // Use ComponentInformation.xml instead of BindingComponentInformatino.xml
            // to get all the binding components!
            String ciFileLoc = confDirLoc + "ComponentInformation.xml";
            loadBindingComponentInfo(ciFileLoc);
            
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

                String jarName = getShortName(srcJarPath); // e.x.: SynchronousSample.jar
                
                if ((srcJarPath.indexOf(':') < 0) && (!srcJarPath.startsWith("/"))) { // i.e., relative path
                    srcJarPath = projPath + srcJarPath;
                }
                
                File srcJarFile = new File(srcJarPath);
                
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
            
            // resolve connections... and write out to connections.xml
            // Todo: load this from connections.xml
            ConnectionContainer cc = new ConnectionContainer();
            Map<String, List[]> bcConnections = ResloveConnections(mRepo, cc);
            
            // todo: 02/08/07 need to merge Casa Connections in...
            String casaFileLoc = confDirLoc + getCASAFileName();
            CasaBuilder casaBuilder = new CasaBuilder(project, mRepo, this, casaFileLoc);
            casaBuilder.mergeCasaConnection(cc, bcConnections);
            
            // System.out.println("bcs: " + bcs);
            CreateSAConnections dd = new CreateSAConnections();
            dd.buildDOMTree(cc, mRepo, p);
            dd.writeToFile(connectionsFileLoc);
            
            // Generate SA jbi.xml,
            generateServiceAssemblyDescriptor(
                    cc, bcConnections, connectionsFileLoc, jbiFileLoc);
            
            // Todo: 08/22/06 merge catalogs
            MergeSeJarCatalogs(catalogDirLoc);
            
            genericBCJar = new JarFile(genericBCJarFileLoc);
                       
            List<String> bcsUsingCompAppWsdl = 
                    getBindingComponentsUsingCompAppWsdl(bcConnections);
                    
            for (String bcName : bcConnections.keySet()) {
                List<Connection>[] clist = bcConnections.get(bcName);
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
                    createBCJBIDescriptor(bcJarName, clist, cc);
                    
                    // Create build/<bcName>.jar
                    // ToDo: update bc jar endpoints.xml
                    // 1. copy BCjars for each needed BC
                    // 2. create jbi.xml
                    boolean isCompAppWSDLNeeded = bcsUsingCompAppWsdl.contains(bcName);
                    createBCJar(bcJarName, clist, genericBCJar, cc, isCompAppWSDLNeeded);                    
                } else {
                    log("ERROR: Cannot create binding component jar for " + bcName);
                }
            }
            
            Document casaDocument = casaBuilder.createCasaDocument(jbiDocument);  
            XmlUtil.writeToFile(casaFileLoc, casaDocument);
            
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
    
    /*
    private void clearDeletedBCEndpoints(List<Connection>[] clist, 
            List<Endpoint> deletedBCEndpoints) {
        for (int i = clist[0].size() - 1; i >= 0; i--) {
            Connection connection = clist[0].get(i);
            Endpoint consumes = connection.getConsume();
            for (Endpoint e : deletedBCEndpoints) {
                if (e.equals(consumes)) {
                    clist[0].remove(connection);
                    break;
                }
            }
        }
        for (int i = clist[1].size() - 1; i >= 0; i--) {
            Connection connection = clist[1].get(i);
            Endpoint provides = connection.getProvide();
            for (Endpoint e : deletedBCEndpoints) {
                if (e.equals(provides)) {
                    clist[1].remove(connection);
                    break;
                }
            }
        }
    }*/
    
    private List<String> getJarList(String commaSeparatedList){
        List<String>  ret = new ArrayList<String>();
        if (commaSeparatedList != null) {
            StringTokenizer st = new StringTokenizer(commaSeparatedList, ";");
            String tkn = null;
            while (st.hasMoreTokens()){
                tkn = (String) st.nextToken();
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
    
    private void MergeSeJarCatalogs(String catalogDirLoc) {
        File catalogDir = new File(catalogDirLoc);
        if (!catalogDir.exists()) {  // no catalog...
            return;
        }
        
        // 1. loop thru project subdirs
        List<File> cats = new ArrayList<File>();
        File[] children = catalogDir.listFiles();
        if (children == null) {
            return; // no children...
        }
        for(int i = 0; i < children.length; i++) {
            File child = children[i];
            File catFile = new File(child, SU_CATALOGXML_PATH);
            if (catFile.exists()) {
                cats.add(catFile);
            }
        }
        
        // 2. merge catalog.xml
        if (cats.size() < 1) {
            return; // no catalog..
        }
        int ncat = 0;
        Document catlogDoc = null;
        Node catlogRoot = null;
        catalogFile = new File(catalogDir.getParentFile(), "catalog.xml"); // NOI18N
        try {
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = fact.newDocumentBuilder();
            
            for(File catalog: cats) {
                ncat++;
                if (ncat == 1) { // copy the first one...
                    catalogFile.createNewFile();
                    InputStream in = new FileInputStream(catalog);
                    OutputStream out = new FileOutputStream(catalogFile);
                    
                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                } else if (ncat == 2) {
                    catlogDoc = builder.parse(catalogFile);
                    catlogRoot = catlogDoc.getDocumentElement();
                    importCatalog(builder, catalog, catlogRoot);
                } else { // merge next..
                    importCatalog(builder, catalog, catlogRoot);
                }
            }
            
            if (ncat > 1) {
                DOMSource src = new DOMSource(catlogRoot);
                FileOutputStream fos = new FileOutputStream(catalogFile);
                StreamResult rest = new StreamResult(fos);
                TransformerFactory transFact = TransformerFactory.newInstance();
                Transformer transformer = transFact.newTransformer();
                transformer.transform(src, rest);
            }
        } catch (Exception ex) {
            log("Exception: A processing error occurred; " + ex);
        }
        
    }
    
    private void importCatalog(DocumentBuilder builder, File catFile, Node catRoot) {
        try {
            Document doc = builder.parse(catFile);
            Element root = doc.getDocumentElement();
            NodeList nl = root.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                
                // todo: Need to check for duplicated entries...
                
                n = catRoot.getOwnerDocument().importNode(n, true);
                catRoot.appendChild(n);
            }
        } catch (Exception ex) {
            log("Exception: A processing error occurred; " + ex);
        }
    }
    
    /**
     * Generates service assembly jbi.xml (based on the ASI.xml document).
     */
    private void generateServiceAssemblyDescriptor(ConnectionContainer cc,
            Map<String, List[]> bcs, 
            String conFileLoc,
            String jbiFileLoc) {        
        try {
            CreateSAConnections dd = new CreateSAConnections();
            dd.buildDOMTree(cc, mRepo, getProject());
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
                        if (bcNameList.contains(compName) &&  // is bc
                                !bcs.containsKey(compName)) { // not being used
                            sa.removeChild(su);
                        }
                    }
                }
                
                // 2. add connections
                sa.appendChild(dd.createConnections(cc, jbiDocument));
                
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
                Map<String, String> map = cc.getNamespaceMap();
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
     * @return  a map mapping binding component name (say, "sun-http-binding")
     *          to a list of two connection lists.
     */
    private Map<String, List[]> ResloveConnections(wsdlRepository repo,
            ConnectionContainer cc) {
        Map<String, PtConnection> ptConnectionMap = repo.getConnections(); // key is portType
        HashMap<String, List[]> bcConnections = new HashMap<String, List[]>();
        
        // loop thru the Pt connections
        for (String pt : ptConnectionMap.keySet()) {
            PtConnection ptConnection = ptConnectionMap.get(pt);
                if (showLog) {
                log(ptConnection.dump());
            }
            
            // check the number of ports
            List<Port> ports = ptConnection.getPorts();
            int numPorts = ports.size();
            if (numPorts == 1) { // OK, only 1 external port...
                Port p = ports.get(0);
                List<Endpoint> providers = ptConnection.getProvides();
                
                // check the number of providers
                int numProviders = providers.size();
                if (numProviders > 1) { // report error... more than 1 providers
                    log("***Warning: 1 port with " + numProviders + " providers ["
                            + pt + "]");
                    // todo: OK, we will just add the first one in...
                }
                
                // Is there a binding for the port
                String bcName = repo.getBindingComponentName(p);
                QName ptQName = p.getBinding().get().getType().getQName();
                if (bcName == null) { // report error.. no binding
                    log("***Warning: PORT w/o address: " + ptQName);
                } else {
                    Object cmap = bcConnections.get(bcName);
                    ArrayList[] clist = new ArrayList[2];
                    if (cmap != null) {
                        clist = ((ArrayList[]) cmap);
                    } else {
                        clist[0] = new ArrayList<Connection>();
                        clist[1] = new ArrayList<Connection>();
                    }
                    
                    // Create the endpoint for port
                    Service sv = (Service) p.getParent();
                    String tns = ((Definitions) sv.getParent()).getTargetNamespace();
                    Endpoint port = new Endpoint(//"either",
                            p.getName(),
                            new QName(tns, sv.getName()),
                            ptQName);
                    
                    // create a connection port -> provider
                    Endpoint provide = null;
                    if (numProviders > 0) {
                        // 03/08/06 use the first one only...
                        provide = providers.get(0);
                        Connection c = new Connection(port, provide);
                        cc.addConnection(c);
                        clist[0].add(c);
                        if (numProviders > 1) {
                            dumpEndpoints(providers, "Provider");
                        }
                    }
                    // loop thru consumers
                    List<Endpoint> consumers = ptConnection.getConsumes();
                    for (Endpoint consume : consumers) {
                        // create a connection consumer -> port
                        // todo: 03/23/06.. replace with consumer -> provider
                        // instead
                        Connection c = null;
                        if ((provide != null) && saInternalRouting) {
                            // create a direct connection consumer -> provide
                            c = new Connection(consume, provide);
                        } else {
                            c = new Connection(consume, port);
                            clist[1].add(c);
                        }
                        cc.addConnection(c);
                    }
                    
                    bcConnections.put(bcName, clist);
                }
                
            } else if (numPorts == 0) { // no external port...
                // report unused wsdl port, or internal connection
                log("***Warning: no wsdl port implementing portType [" + pt + "]");
                
                // resolve internal connections...
                List<Endpoint> consumers = ptConnection.getConsumes();
                List<Endpoint> providers = ptConnection.getProvides();
                int numProviders = providers.size();
                int numConsumers = consumers.size();
                if ((numConsumers < 1) || (numProviders < 1)) {
                    // no connection needed...
                    
                    // todo: 09/05/06 Create default binding/serivce/port for sole provider...
                    // todo: 1. add port w/default binding/serverice
                    // todo: 2. add connection port -> provider
                    
                } else if (numProviders == 1) { // OK...
                    Endpoint provide = providers.get(0);
                    for (Endpoint consume : consumers) {
                        // create a connection consumer -> provider
                        Connection c = new Connection(consume, provide);
                        cc.addConnection(c);
                    }
                } else if (numProviders > 1) {
                    // report error... more than 1 providers
                    log("***Warning: " + numPorts + " ports, " +
                            numConsumers + " consumers, " +
                            numProviders + " providers. [" + pt + "]");
                    dumpPorts(ports);
                    dumpEndpoints(consumers, "Consumer");
                    dumpEndpoints(providers, "Provider");
                }
                
            } else { // more than 1 external port
                // OK, if multiple ports, but only 1 provider 0 consumer...
                // Q: Why do we have to have 0 consumer? If we can connect multiple
                // ports to the sole provider, why can't we connect multiple
                // consumers to the sole provider at the same time?
                
                List<Endpoint> consumers = ptConnection.getConsumes();
                List<Endpoint> providers = ptConnection.getProvides();
                int numProviders = providers.size();
                int numConsumers = consumers.size();
                if (numProviders > 1) {
                    // report error... more than 1 providers
                    log("***Warning: " + numPorts + " ports and " + numProviders
                            + " providers [" + pt + "]");
                    dumpPorts(ports);
                    dumpEndpoints(providers, "Provider");
                } else if (numConsumers > 0) {
                    // report error... more than 1 consumers
                    log("***Warning: 1 or more consumers with " + numPorts
                            + " ports [" + pt + "]");
                    dumpPorts(ports);
                    dumpEndpoints(consumers, "Consumer");
                } else if (numProviders == 1) {
                    Endpoint provide = providers.get(0);
                    for (Port p : ports) {
                        String bcName = repo.getBindingComponentName(p);
                        QName ptQName = p.getBinding().get().getType().getQName();
                        if (bcName == null) {
                            log("***Warning: PORT w/o address: " + ptQName);
                        } else {
                            Object cmap = bcConnections.get(bcName);
                            ArrayList[] clist = new ArrayList[2];
                            if (cmap != null) {
                                clist = ((ArrayList[]) cmap);
                            } else {
                                clist[0] = new ArrayList<Connection>();
                                clist[1] = new ArrayList<Connection>();
                            }
                            
                            Service sv = (Service) p.getParent();
                            String tns = ((Definitions) sv.getParent()).getTargetNamespace();
                            Endpoint port = new Endpoint(p.getName(),
                                    new QName(tns, sv.getName()),
                                    ptQName);
                            Connection c = new Connection(port, provide);
                            cc.addConnection(c);
                            clist[0].add(c);
                            bcConnections.put(bcName, clist);
                        }
                    }
                }
            }
        }
        
        if (showLog) {
            log("\n-----------------------------------\n");
            log(cc.dump());
        }
        
        return bcConnections;
    }
    
    private void dumpEndpoints(List<Endpoint> endpoints, String name) {
        int psize = endpoints.size();
        if (psize > 1) {
            for (int k = 0; k < psize; k++) {
                Endpoint pn = endpoints.get(k);
                log("\t" + name + "[" + k + "]: : " + pn.getServiceQName() + ", "
                        + pn.getEndpointName());
            }
        }
    }
    
    private void dumpPorts(List<Port> ports) {
        int psize = ports.size();
        if (psize > 1) {
            for (int k = 0; k < psize; k++) {
                Port port = ports.get(k);
                log("\tPort[" + k + "]: : " + port.getName() + ", "
                        + port.getBinding().getQName());
            }
        }
    }
    
    /**
     * Loads binding component names from ComponentInformation.xml.
     *
     * @param ciFileLoc    file location for ComponentInformation.xml
     */
    private void loadBindingComponentInfo(String ciFileLoc) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            
            Document document =
                    factory.newDocumentBuilder().parse(new File(ciFileLoc));
            NodeList compInfoNodeList = document.getElementsByTagName("component-info");
            
            bcNameList = new ArrayList<String>();
            for (int i = 0, isize = compInfoNodeList.getLength(); i < isize; i++) {
                Element compInfo = (Element) compInfoNodeList.item(i);
                Element typeElement = (Element) compInfo.getElementsByTagName("type").item(0);                
                String compType = typeElement.getFirstChild().getNodeValue();
                if (compType.equalsIgnoreCase("binding")) {
                    Element nameElement = (Element) compInfo.getElementsByTagName("name").item(0);
                    String compName = nameElement.getFirstChild().getNodeValue();
                    bcNameList.add(compName);
                }
            }
        } catch (IOException e) {
            log("IOException: A parsing error occurred; the xml input is not valid: " + ciFileLoc);
        } catch (SAXException e) {
            log("SAXException: A parsing error occurred; the xml input is not valid: " + ciFileLoc);
        } catch (ParserConfigurationException e) {
            log("ParserConfigurationException: A parsing error occurred; the xml input is not valid: " + ciFileLoc);
        }
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
        } catch (IOException e) {
            log("IOException: A parsing error occurred; the xml input is not valid: " + asiFileLoc);
        } catch (SAXException e) {
            log("SAXException: A parsing error occurred; the xml input is not valid: " + asiFileLoc);
        } catch (ParserConfigurationException e) {
            log("ParserConfigurationException: A parsing error occurred; the xml input is not valid: " + asiFileLoc);
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
        int index = 0;
        if (subProjectJar != null){
            sJar = new File(subProjectJar);
            ret = sJar.getName();
            index = ret.lastIndexOf(".");
            if (index > -1){
                ret = dir + "/" + ret.substring(0, index + 1);
                ret = ret + "jar" ; // No I18N
            }
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
        
    private void createBCJBIDescriptor(String outFile, List[] clist,
            ConnectionContainer cc) {
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
            dd.buildDOMTree(cc, clist, mRepo);
            dd.writeToFile(bcSUDirLoc);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private String getProjectName() {
        return getProject().getProperty(JbiProjectProperties.ASSEMBLY_UNIT_UUID);
    }
    
    private String getCasaWSDLFileName() {
        return getProjectName() + ".wsdl";
    }
    
    private String getCASAFileName() {
        return getProjectName() + ".casa";
    }
    
    private void createBCJar(String outFile, List[] clist, 
            JarFile genericBCJar, ConnectionContainer cc,
            boolean isCompAppWSDLNeeded) 
            throws Exception {
        byte[] buffer = new byte[1024];
        int bytesRead;
        
        String casaWSDLFileName = getCasaWSDLFileName();
        
        JarOutputStream newJar = new JarOutputStream(new FileOutputStream(outFile));
        
        try {
            Enumeration entries = genericBCJar.entries();
            
            while (entries.hasMoreElements()) {
                JarEntry entry = (JarEntry) entries.nextElement();
                InputStream is = genericBCJar.getInputStream(entry);
                
                // TODO: update casa wsdl entry in generic bc jar file.
                if (entry.getName().equals(casaWSDLFileName)) {
                    // Quick fix for J1: If the casa wsdl file doesn't contain 
                    // active endpoints, then we skip packaging the casa wsdl entry.
                    // (Future improvement: This rule should apply to all the 
                    // wsdl files.)
                    if (isCompAppWSDLNeeded) {
                        
                        newJar.putNextEntry(new JarEntry(casaWSDLFileName));
                        
                        // HACK: remove "../jbiServiceUnits/" from import elements' location
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8")); // FIXME: encoding
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.indexOf("location=\"../jbiServiceUnits/") != -1) {
                                line = line.replace("../jbiServiceUnits/", "");
                            }
                            newJar.write(line.getBytes("UTF-8"));
                            newJar.write(System.getProperty("line.separator").getBytes("UTF-8"));
                        }
                        reader.close();
                    }
                    
                } else {
                    
                    newJar.putNextEntry(entry);
                    
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
            dd.buildDOMTree(cc, clist, mRepo);
            newJar.write(dd.writeToBytes());
            
            // create the catalog.xml
            if (catalogFile != null) {
                JarEntry catalogentry = new JarEntry(SU_CATALOGXML_PATH);
                newJar.putNextEntry(catalogentry);
                InputStream in = new FileInputStream(catalogFile);
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
    
    /*
    private boolean checkWSDLEndpointUsage(InputStream wsdlInputStream, 
            Map<String, List[]> bcConnections, String bcName) {
        
        try {
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = fact.newDocumentBuilder();
            Document document = builder.parse(wsdlInputStream);
            
            String tns = document.getDocumentElement().getAttribute("targetNamespace");
            
            NodeList portNodeList = document.getElementsByTagName("port");
            for (int i = 0; i < portNodeList.getLength(); i++) {
                Element port = (Element) portNodeList.item(i);
                String portName = port.getAttribute("name");
                Element service = (Element) port.getParentNode();
                
                String serviceName = service.getAttribute("name");
                QName serviceQName = new QName(tns, serviceName);
                
                List<Connection>[] clist = (List<Connection>[]) bcConnections.get(bcName);
                for (Connection connection : clist[0]) {
                    Endpoint e = connection.getConsume();              
                    // we don't need to check interface name.
                    if (e.getEndpointName().equals(portName) &&
                            e.getServiceQName().equals(serviceQName)) {
                        return true;
                    }
                }
                
                for (Connection connection : clist[1]) {
                    Endpoint e = connection.getProvide();              
                    // we don't need to check interface name.
                    if (e.getEndpointName().equals(portName) &&
                            e.getServiceQName().equals(serviceQName)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return true;        
    }*/
    
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
    /*
    private List<Endpoint> getEndpointsInServiceAssemblyDescriptor(String saJBIFile) {
        
        List<Endpoint> ret = new ArrayList<Endpoint>();
        
        try {
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = fact.newDocumentBuilder();
            Document document = builder.parse(new File(saJBIFile));
            
            NodeList consumerNodeList = document.getElementsByTagName("consumer");
            for (int i = 0; i < consumerNodeList.getLength(); i++) {
                Element consumer = (Element) consumerNodeList.item(i);
                
                QName serviceQName = XmlUtil.getAttributeNSName(consumer, "service-name"); // REFACTOR ME
                String endpointName = consumer.getAttribute("endpoint-name");
                Endpoint endpoint = new Endpoint(endpointName, serviceQName, null); // we don't care about interface name in the current use case
                ret.add(endpoint);
            }
            
            NodeList providerNodeList = document.getElementsByTagName("provider");
            for (int i = 0; i < providerNodeList.getLength(); i++) {
                Element provider = (Element) providerNodeList.item(i);
                
                QName serviceQName = XmlUtil.getAttributeNSName(provider, "service-name");
                String endpointName = provider.getAttribute("endpoint-name");
                Endpoint endpoint = new Endpoint(endpointName, serviceQName, null);
                ret.add(endpoint);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return ret;
    }*/
    
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
            Map<String, List[]> bcConnections) {
        
        List<String> ret = new ArrayList<String>();
        
        WSDLModel compAppWsdlModel = getCompAppWsdlModel();           
        List<Endpoint> endpoints = getEndpointsInWsdlModel(compAppWsdlModel);
            
        if (endpoints.size() > 0) {
            for (String bcName : bcConnections.keySet()) {
                List<Endpoint> bcEndpoints = new ArrayList<Endpoint>();
                
                List<Connection>[] clist = (List<Connection>[]) bcConnections.get(bcName);
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
