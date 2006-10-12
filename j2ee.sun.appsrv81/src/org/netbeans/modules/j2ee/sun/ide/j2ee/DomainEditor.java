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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * DomainEditor.java
 *
 * Created on April 14, 2006, 10:33 AM
 *
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Parses and edits domain.xml
 * Used for Profiler, HTTP Proxy, DataSources
 * @author Nitya Doraisamy
 */
public class DomainEditor {
    
    private static DeploymentManager dm;
    private static String HTTP_PROXY_HOST = "-Dhttp.proxyHost="; //NOI18N
    private static String HTTP_PROXY_PORT = "-Dhttp.proxyPort="; //NOI18N
    private static String HTTPS_PROXY_HOST = "-Dhttps.proxyHost="; //NOI18N
    private static String HTTPS_PROXY_PORT = "-Dhttps.proxyPort="; //NOI18N
    private static String HTTP_PROXY_NO_HOST = "-Dhttp.nonProxyHosts="; //NOI18N
    
    private static String SAMPLE_DATASOURCE = "jdbc/sample"; //NOI18N
    private static String SAMPLE_CONNPOOL = "SamplePool"; //NOI18N
    
    private static String NBPROFILERNAME = "NetBeansProfiler"; //NOI18N
    
    /**
     * Creates a new instance of DomainEditor
     * @param dm Deployment Manager of Target Server
     */
    public DomainEditor(DeploymentManager dm) {
        this.dm = dm;
    }
    
    /**
     * Get the location of the server's domain.xml
     * @return String representing path to domain.xml
     */
    public String getDomainLocation(){
        DeploymentManagerProperties dmProps = new DeploymentManagerProperties(this.dm);
        String domainScriptFilePath = dmProps.getLocation()+"/" + dmProps.getDomainName() + //NOI18N
                "/config/domain.xml"; //NOI18N
        return domainScriptFilePath;
    }
    
    /**
     * Get Document Object representing the domain.xml    
     * @return Document object representing given domain.xml
     */
    public Document getDomainDocument(){
        String domainLoc = getDomainLocation();
        
        // Load domain.xml
        Document domainScriptDocument = getDomainDocument(domainLoc);
        return domainScriptDocument;
    }
    
    /**
     * Get Document Object representing the domain.xml
     * @param domainLoc Location of domain.xml
     * @return Document object representing given domain.xml
     */
    public Document getDomainDocument(String domainLoc){
        File domainScriptFile = new File(domainLoc);
        
        // Load domain.xml
        Document domainScriptDocument = loadDomainScriptFile(domainLoc);
        return domainScriptDocument;
    }
    
    /**
     * Perform server instrumentation for profiling
     * @param domainDoc Document object representing domain.xml
     * @param nativeLibraryPath Native Library Path
     * @param jvmOptions Values for jvm-options to enable profiling
     * @return returns true if server is ready for profiling
     */
    public boolean addProfilerElements(Document domainDoc, String nativeLibraryPath, String[] jvmOptions){
        String domainPath = getDomainLocation();
        
        // Remove any previously defined 'profiler' element(s)
        removeProfiler(domainDoc);
        
        // If no 'profiler' element needs to be defined, the existing one is simply removed (by the code above)
        // (This won't happen for NetBeans Profiler, but is a valid scenario)
        // Otherwise new 'profiler' element is inserted according to provided parameters
        if (nativeLibraryPath != null || jvmOptions != null) {
            
            // Create "profiler" element
            Element profilerElement = domainDoc.createElement("profiler");//NOI18N
            profilerElement.setAttribute("enabled", "true");//NOI18N
            profilerElement.setAttribute("name", NBPROFILERNAME);//NOI18N
            if (nativeLibraryPath != null) 
                profilerElement.setAttribute("native-library-path", nativeLibraryPath);//NOI18N
            
            File appServerLocation = ((SunDeploymentManagerInterface)getDeploymentManager()).getPlatformRoot();
            // Create "jvm-options" element
            if (jvmOptions != null) {
                for (int i = 0; i < jvmOptions.length; i++) {
                    Element jvmOptionsElement = domainDoc.createElement("jvm-options");
                    Text tt = domainDoc.createTextNode(formatJvmOption(jvmOptions[i] ,  appServerLocation));
                    jvmOptionsElement.appendChild(tt);
                    profilerElement.appendChild(jvmOptionsElement);
                }
            }
            
            // Find the "java-config" element
            NodeList javaConfigNodeList = domainDoc.getElementsByTagName("java-config");
            if (javaConfigNodeList == null || javaConfigNodeList.getLength() == 0) {
                System.err.println("ConfigFilesUtils: cannot find 'java-config' section in domain config file " + domainPath);
                return false;
            }
            
            // Insert the "profiler" element as a first child of "java-config" element
            Node javaConfigNode = javaConfigNodeList.item(0);
            if (javaConfigNode.getFirstChild() != null) 
                javaConfigNode.insertBefore(profilerElement, javaConfigNode.getFirstChild());
            else 
                javaConfigNode.appendChild(profilerElement);
            
        }
        // Save domain.xml
        return saveDomainScriptFile(domainDoc, domainPath);
    }
    
    /**
     * Remove server instrumentation to disable profiling
     * @param domainDoc Document object representing domain.xml
     * @return true if profiling support has been removed
     */
    public boolean removeProfilerElements(Document domainDoc){
        boolean eleRemoved = removeProfiler(domainDoc);
        if(eleRemoved){
            // Save domain.xml
            return saveDomainScriptFile(domainDoc, getDomainLocation());
        }else{
            //no need to save.
            return true;
        }    
    }
    
    private boolean removeProfiler(Document domainDoc){
        // Remove any previously defined 'profiler' element(s)
        NodeList profilerElementNodeList = domainDoc.getElementsByTagName("profiler");//NOI18N
        if (profilerElementNodeList != null && profilerElementNodeList.getLength() > 0){
            Vector nodes = new Vector(); //temp storage for the nodes to delete
            //we only want to delete the NBPROFILERNAME nodes.
            // otherwise, see bug # 77026
            for (int i = 0; i < profilerElementNodeList.getLength(); i++) {
                Node n= profilerElementNodeList.item(i);                
                Node a= n.getAttributes().getNamedItem("name");//NOI18N
                if ((a!=null)&&(a.getNodeValue().equals(NBPROFILERNAME))){//NOI18N
                    nodes.add(n);
                }                              
            }
            for(int i=0; i<nodes.size(); i++){
                Node nd = (Node)nodes.get(i);
                nd.getParentNode().removeChild(nd);
            }
            return true;
        }
            
        return false;
    }
       
    public String[] getHttpProxyOptions(){
        ArrayList httpProxyOptions = new ArrayList();       
        Document domainDoc = getDomainDocument();
        NodeList javaConfigNodeList = domainDoc.getElementsByTagName("java-config");
        if (javaConfigNodeList == null || javaConfigNodeList.getLength() == 0) {
            return (String[])httpProxyOptions.toArray();
        }
        
        NodeList jvmOptionNodeList = domainDoc.getElementsByTagName("jvm-options");
        for(int i=0; i<jvmOptionNodeList.getLength(); i++){
            Node nd = jvmOptionNodeList.item(i);
            if(nd.hasChildNodes())  {
                Node childNode = nd.getFirstChild();
                String childValue = childNode.getNodeValue();
                if(childValue.indexOf(HTTP_PROXY_HOST) != -1
                        || childValue.indexOf(HTTP_PROXY_PORT) != -1
                        || childValue.indexOf(HTTPS_PROXY_HOST) != -1
                        || childValue.indexOf(HTTPS_PROXY_PORT) != -1
                        || childValue.indexOf(HTTP_PROXY_NO_HOST) != -1){
                    httpProxyOptions.add(childValue);
                }
            }
        }

        String[] opts = new String[httpProxyOptions.size()];
        return (String[])httpProxyOptions.toArray(opts);
        
    }
    
    public boolean setHttpProxyOptions(String[] httpProxyOptions){
        Document domainDoc = getDomainDocument();
        NodeList javaConfigNodeList = domainDoc.getElementsByTagName("java-config");
        if (javaConfigNodeList == null || javaConfigNodeList.getLength() == 0) {
            return false;
        }
        
        //Iterates through the existing proxy attributes and deletes them
        removeProxyOptions(domainDoc, javaConfigNodeList.item(0));
                
        //Add new set of proxy options
        for(int j=0; j<httpProxyOptions.length; j++){
            String option = httpProxyOptions[j];
            Element jvmOptionsElement = domainDoc.createElement("jvm-options");
            Text proxyOption = domainDoc.createTextNode(option);
            jvmOptionsElement.appendChild(proxyOption);
            javaConfigNodeList.item(0).appendChild(jvmOptionsElement);
        }
        
        return saveDomainScriptFile(domainDoc, getDomainLocation(), false);
    }
      
    private boolean removeProxyOptions(Document domainDoc, Node javaConfigNode){
        NodeList jvmOptionNodeList = domainDoc.getElementsByTagName("jvm-options");
        
        Vector nodes = new Vector();
        for(int i=0; i<jvmOptionNodeList.getLength(); i++){
            Node nd = jvmOptionNodeList.item(i);
            if(nd.hasChildNodes())  {
                Node childNode = nd.getFirstChild();
                String childValue = childNode.getNodeValue();
                if(childValue.indexOf(HTTP_PROXY_HOST) != -1
                        || childValue.indexOf(HTTP_PROXY_PORT) != -1
                        || childValue.indexOf(HTTPS_PROXY_HOST) != -1
                        || childValue.indexOf(HTTPS_PROXY_PORT) != -1
                        || childValue.indexOf(HTTP_PROXY_NO_HOST) != -1){
                   nodes.add(nd);
                }
            }
        }
        for(int i=0; i<nodes.size(); i++){
            Node nd = (Node)nodes.get(i);
            javaConfigNode.removeChild((Node)nodes.get(i));
        }
        
        return saveDomainScriptFile(domainDoc, getDomainLocation(), false);
    }
    
    /*
     * Creates Document instance from domain.xml
     * @param domainScriptFilePath Path to domain.xml
     */
    private Document loadDomainScriptFile(String domainScriptFilePath) {
        Document document = null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setValidating(false);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            
            dBuilder.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    StringReader reader = new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); // NOI18N
                    InputSource source = new InputSource(reader);
                    source.setPublicId(publicId);
                    source.setSystemId(systemId);
                    return source;
                }
            });
            
            return dBuilder.parse(new File(domainScriptFilePath));
        } catch (Exception e) {
            System.err.println("ConfigFilesUtils: unable to parse domain config file " + domainScriptFilePath);
            return null;
        }
    }
    
    private boolean saveDomainScriptFile(Document domainScriptDocument, String domainScriptFilePath) {
        return saveDomainScriptFile(domainScriptDocument, domainScriptFilePath, true);
    }
    /*
     * Saves Document instance to domain.xml
     * @param domainScriptDocument Document representing the xml
     * @param domainScriptFilePath Path to domain.xml
     */
    private boolean saveDomainScriptFile(Document domainScriptDocument, String domainScriptFilePath, boolean indent) {
        boolean result = false;
        FileWriter domainScriptFileWriter = null;
        try {
            domainScriptFileWriter = new FileWriter(domainScriptFilePath);
            try {
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                if(indent)
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, domainScriptDocument.getDoctype().getPublicId());
                transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, domainScriptDocument.getDoctype().getSystemId());
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                
                DOMSource domSource = new DOMSource(domainScriptDocument);
                StreamResult streamResult = new StreamResult(domainScriptFileWriter);
                
                transformer.transform(domSource, streamResult);
                result = true;
            } catch (Exception e) {
                System.err.println("ConfigFilesUtils: Unable to save domain config file " + domainScriptFilePath);
                result = false;
            }
        } catch (IOException ioex) {
            System.err.println("ConfigFilesUtils: cannot create output stream for domain config file " + domainScriptFilePath);
            result = false;
        } finally {
            try { 
                if (domainScriptFileWriter != null) 
                    domainScriptFileWriter.close(); 
            } catch (IOException ioex2) {
                System.err.println("SunAS8IntegrationProvider: cannot close output stream for " + domainScriptFilePath); 
            };
        }
        
        return result;
    }
    
    // Converts -agentpath:"C:\Program Files\lib\profileragent.dll=\"C:\Program Files\lib\"",5140
    // to -agentpath:C:\Program Files\lib\profileragent.dll="C:\Program Files\lib",5140 (AS 8.1 and AS 8.2)
    // or to  "-agentpath:C:\Program Files\lib\profileragent.dll=\"C:\Program Files\lib\",5140" (GlassFish or AS 9.0)
    private String formatJvmOption(String jvmOption, File appServerLocation) {
        // only jvmOption containing \" needs to be formatted
        if (jvmOption.indexOf("\\\"") != -1) {

            // Modification for AS 8.1, 8.2, initial modification for AS 9.0, GlassFish
            // Converts -agentpath:"C:\Program Files\lib\profileragent.dll=\"C:\Program Files\lib\"",5140
            // to -agentpath:C:\Program Files\lib\profileragent.dll="C:\Program Files\lib",5140
            String modifiedOption = jvmOption.replaceAll("\\\\\"", "#"); // replace every \" by #
            modifiedOption = modifiedOption.replaceAll("\\\"", ""); // delete all "
            modifiedOption = modifiedOption.replaceAll("#", "\""); // replace every # by "

            // Modification for AS 9.0, GlassFish should be done only if native launcher isn't used,
            // otherwise will cause server startup failure. It seems that currently native launcher is used
            // for starting the servers from the IDE.
   //         boolean usingNativeLauncher = false;
            String osType=System.getProperty("os.name");//NOI18N
            if ((osType.startsWith("Mac OS"))||(ServerLocationManager.isGlassFish(appServerLocation))){//no native for mac of glassfish
  //          if (!usingNativeLauncher) {

                // Modification for AS 9.0, GlassFish
                // Converts -agentpath:C:\Program Files\lib\profileragent.dll="C:\Program Files\lib",5140
                // "-agentpath:C:\Program Files\lib\profileragent.dll=\"C:\Program Files\lib\",5140"

                    modifiedOption = "\"" + modifiedOption.replaceAll("\\\"", "\\\\\"") + "\"";

            }

            // return correctly formatted jvmOption
            return modifiedOption;
        }
        // return original jvmOption
        return jvmOption;
     }
    
    static final String[] sysDatasources = {"jdbc/__TimerPool", "jdbc/__CallFlowPool", "jdbc/__default"}; //NOI18N
    
    
            
    public HashMap getSunDatasourcesFromXml(){
        HashMap dSources = new HashMap();
        Document domainDoc = getDomainDocument();
        HashMap dsMap = getDataSourcesAttrMap(domainDoc);
        HashMap cpMap = getConnPoolsNodeMap(domainDoc);
        dsMap.keySet().removeAll(Arrays.asList(sysDatasources));    
        String[] ds = (String[])dsMap.keySet().toArray(new String[dsMap.size()]);
        
        for(int i=0; i<ds.length; i++){
            String jndiName = ds[i];
            HashMap pValues = new HashMap();
            NamedNodeMap dsAttrMap = (NamedNodeMap)dsMap.get(jndiName);
            String poolName = dsAttrMap.getNamedItem("pool-name").getNodeValue();
            
            //Get the Connection Pool used by this jdbc-resource
            Node cpNode = (Node)cpMap.get(poolName);
            NamedNodeMap cpAttrMap = cpNode.getAttributes();
            String dsClassName = cpAttrMap.getNamedItem("datasource-classname").getNodeValue();
            
            //Get property values
            Element cpElement = (Element) cpNode;
            NodeList propsNodeList = cpElement.getElementsByTagName("property");
                        
            //Cycle through each property element
            HashMap map = new HashMap();
            for(int j=0; j<propsNodeList.getLength(); j++){
                Node propNode = propsNodeList.item(j);
                NamedNodeMap propsMap = propNode.getAttributes();
                
                for(int m=0; m<propsMap.getLength(); m++){
                    String mkey = propsMap.getNamedItem("name").getNodeValue();
                    String mkeyValue = propsMap.getNamedItem("value").getNodeValue();
                    map.put(mkey, mkeyValue);
                }
            } // connection-pool properties

            pValues.put("User", (String)map.get("User"));
            pValues.put("Password", (String)map.get("Password"));
            pValues.put("URL", (String)map.get("URL"));
            pValues.put("databaseName", (String)map.get("databaseName"));
            pValues.put("serverName", (String)map.get("serverName"));
            pValues.put("PortNumber", (String)map.get("PortNumber"));
            pValues.put("portNumber", (String)map.get("portNumber"));
            pValues.put("DatabaseName", (String)map.get("DatabaseName"));
            pValues.put("SID", (String)map.get("SID"));
            pValues.put("dsClassName", dsClassName);
            
            dSources.put(jndiName, pValues);
        } // for each jdbc-resource
        return dSources;
    }

    public HashMap getConnPoolsFromXml(){
        HashMap pools = new HashMap();
        Document domainDoc = getDomainDocument();
        HashMap cpMap = getConnPoolsNodeMap(domainDoc);
        
        String[] cp = (String[])cpMap.keySet().toArray(new String[cpMap.size()]);
        for(int i=0; i<cp.length; i++){
            String name = cp[i];
            HashMap pValues = new HashMap();             
            Node cpNode = (Node)cpMap.get(name);
            NamedNodeMap cpAttrMap = cpNode.getAttributes();
            String dsClassName = cpAttrMap.getNamedItem("datasource-classname").getNodeValue();
            
            //Get property values
            Element cpElement = (Element) cpNode;
            NodeList propsNodeList = cpElement.getElementsByTagName("property");
                        
            //Cycle through each property element
            HashMap map = new HashMap();
            for(int j=0; j<propsNodeList.getLength(); j++){
                Node propNode = propsNodeList.item(j);
                NamedNodeMap propsMap = propNode.getAttributes();
                
                for(int m=0; m<propsMap.getLength(); m++){
                    String mkey = propsMap.getNamedItem("name").getNodeValue();
                    String mkeyValue = propsMap.getNamedItem("value").getNodeValue();
                    map.put(mkey, mkeyValue);
                }
            } // connection-pool properties
            
            pValues.put("User", (String)map.get("User"));
            pValues.put("Password", (String)map.get("Password"));
            pValues.put("URL", (String)map.get("URL"));
            pValues.put("databaseName", (String)map.get("databaseName"));
            pValues.put("serverName", (String)map.get("serverName"));
            pValues.put("PortNumber", (String)map.get("PortNumber"));
            pValues.put("portNumber", (String)map.get("portNumber"));
            pValues.put("DatabaseName", (String)map.get("DatabaseName"));
            pValues.put("SID", (String)map.get("SID"));
            pValues.put("dsClassName", dsClassName);
            
            pools.put(name, pValues);
        }
      
        return pools;
    }
    
    private HashMap getDataSourcesAttrMap(Document domainDoc){
        HashMap dataSourceMap = new HashMap();
        updateWithSampleDataSource(domainDoc);
        NodeList dataSourceNodeList = domainDoc.getElementsByTagName("jdbc-resource");
        for(int i=0; i<dataSourceNodeList.getLength(); i++){
            Node dsNode = dataSourceNodeList.item(i);
            NamedNodeMap dsAttrMap = dsNode.getAttributes();
            String jndiName = dsAttrMap.getNamedItem("jndi-name").getNodeValue();
            dataSourceMap.put(jndiName, dsAttrMap);
        }    
        return dataSourceMap;
    }
    
    private boolean updateWithSampleDataSource(Document domainDoc){
        boolean sampleExists = false;
        NodeList dataSourceNodeList = domainDoc.getElementsByTagName("jdbc-resource");
        for(int i=0; i<dataSourceNodeList.getLength(); i++){
            Node dsNode = dataSourceNodeList.item(i);
            NamedNodeMap dsAttrMap = dsNode.getAttributes();
            String jndiName = dsAttrMap.getNamedItem("jndi-name").getNodeValue();
            if(jndiName.equals(SAMPLE_DATASOURCE))
                sampleExists = true;
        }
        if(!sampleExists)
            return createSampleDatasource(domainDoc);
        return true;
    }
    
    private HashMap getConnPoolsNodeMap(Document domainDoc){
        HashMap connPoolMap = new HashMap();
        NodeList connPoolNodeList = domainDoc.getElementsByTagName("jdbc-connection-pool");
        for(int i=0; i<connPoolNodeList.getLength(); i++){
            Node cpNode = connPoolNodeList.item(i);
            NamedNodeMap cpAttrMap = cpNode.getAttributes();
            String cpName = cpAttrMap.getNamedItem("name").getNodeValue();
            connPoolMap.put(cpName, cpNode);
        }    
        return connPoolMap;
    }
        
    public boolean createSampleDatasource(Document domainDoc){
        NodeList resourcesNodeList = domainDoc.getElementsByTagName("resources");
        NodeList serverNodeList = domainDoc.getElementsByTagName("server");
        if (resourcesNodeList == null || resourcesNodeList.getLength() == 0 || 
                serverNodeList == null || serverNodeList.getLength() == 0) {
            return true;
        }
        Node resourcesNode = resourcesNodeList.item(0);
        
        HashMap cpMap = getConnPoolsNodeMap(domainDoc);
        if(! cpMap.containsKey(SAMPLE_CONNPOOL)){
            Node oldNode = (Node)cpMap.get("DerbyPool");
            Node cpNode = oldNode.cloneNode(false);
            NamedNodeMap cpAttrMap = cpNode.getAttributes();
            cpAttrMap.getNamedItem("name").setNodeValue(SAMPLE_CONNPOOL);
            HashMap poolProps = new HashMap();
            poolProps.put("serverName", "localhost");
            poolProps.put("Password", "app");
            poolProps.put("User", "app");
            poolProps.put("DatabaseName", "sample");
            poolProps.put("PortNumber", "1527");
            
            Object[] propNames = poolProps.keySet().toArray();
            for(int i=0; i<propNames.length; i++){
                String keyName = (String)propNames[i];
                Element propElement = domainDoc.createElement("property");
                propElement.setAttribute("name", keyName);
                propElement.setAttribute("value", (String)poolProps.get(keyName));
                cpNode.appendChild(propElement);
            }
            resourcesNode.appendChild(cpNode);
        }
                
        Element dsElement = domainDoc.createElement("jdbc-resource");
        dsElement.setAttribute("jndi-name", SAMPLE_DATASOURCE);
        dsElement.setAttribute("pool-name", SAMPLE_CONNPOOL);
        dsElement.setAttribute("object-type", "user");
        dsElement.setAttribute("enabled", "true");
        
        // Insert the ds __Sample as a first child of "resources" element
        if (resourcesNode.getFirstChild() != null)
            resourcesNode.insertBefore(dsElement, resourcesNode.getFirstChild());
        else
            resourcesNode.appendChild(dsElement);
        
        //<resource-ref enabled="true" ref="jdbc/__default"/>
        Element dsResRefElement = domainDoc.createElement("resource-ref");
        dsResRefElement.setAttribute("ref", SAMPLE_DATASOURCE);
        dsResRefElement.setAttribute("enabled", "true");
        // Insert the ds reference __Sample as last child of "server" element
        Node serverNode = serverNodeList.item(0);
        if (serverNode.getLastChild() != null)
            serverNode.insertBefore(dsResRefElement, serverNode.getLastChild());
        else
            serverNode.appendChild(dsResRefElement);
        
        return saveDomainScriptFile(domainDoc, getDomainLocation());
    }
        
    /**
     * 
     */
    public DeploymentManager getDeploymentManager() {
        return this.dm;
    }
    
}
