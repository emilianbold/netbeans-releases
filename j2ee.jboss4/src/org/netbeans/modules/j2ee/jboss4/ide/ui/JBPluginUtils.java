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
package org.netbeans.modules.j2ee.jboss4.ide.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilderFactory;
import org.openide.ErrorManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Ivan Sidorkin
 */
public class JBPluginUtils {
    
    public static final String SERVER_XML = File.separator + "deploy" + File.separator +
                "jbossweb-tomcat55.sar" + File.separator + "server.xml";
    
    
    //------------  getting exists servers---------------------------
    /**
     * returns Hashmap
     * key = server name
     * value = server folder full path
     */
    public static Hashtable getRegisteredDomains(String serverLocation){
        Hashtable result = new Hashtable();
        //  String domainListFile = File.separator+"common"+File.separator+"nodemanager"+File.separator+"nodemanager.domains";  // NOI18N
        
        if (isGoodJBServerLocation(new File(serverLocation))){
            File file = new File(serverLocation + File.separator + "server");  // NOI18N
            
            String[] files = file.list(new FilenameFilter(){
                public boolean accept(File dir, String name){
                    if ((new File(dir.getAbsolutePath()+File.separator+name)).isDirectory()) return true;
                    return false;
                }
            });
            
            for(int i =0; i<files.length; i++){
                String path = file.getAbsolutePath() + File.separator + files[i];
                
                if (isGoodJBInstanceLocation(new File(path))){
                    result.put(files[i], path);
                }
            }
        }
        return result;
    }
    
    
    //--------------- checking for possible server directory -------------
    private static Collection serverFileColl = new java.util.ArrayList();
    static {
        serverFileColl.add("conf");              // NOI18N
        serverFileColl.add("deploy");               // NOI18N
        serverFileColl.add("lib");    // NOI18N
        serverFileColl.add("conf/jboss-service.xml");      // NOI18N
    }
    
    public static boolean isGoodJBInstanceLocation(File candidate){
        if (null == candidate ||
                !candidate.exists() ||
                !candidate.canRead() ||
                !candidate.isDirectory()  ||
                !hasRequiredChildren(candidate, serverFileColl)) {
            return false;
        }
        return true;
    }
    
    //--------------- checking for possible server directory -------------
    private static Collection fileColl = new java.util.ArrayList();
    
    static {
        fileColl.add("bin");        // NOI18N
        fileColl.add("client");       // NOI18N
        fileColl.add("lib");     // NOI18N
        fileColl.add("server");    // NOI18N
        fileColl.add("lib/jboss-common.jar"); // NOI18N
        fileColl.add("lib/endorsed/resolver.jar"); // NOI18N
    }
    
    public static boolean isGoodJBServerLocation(File candidate){
        if (null == candidate ||
                !candidate.exists() ||
                !candidate.canRead() ||
                !candidate.isDirectory()  ||
                !hasRequiredChildren(candidate, fileColl)) {
            return false;
        }
        return true;
    }
    
    private static boolean hasRequiredChildren(File candidate, Collection requiredChildren) {
        if (null == candidate)
            return false;
        String[] children = candidate.list();
        if (null == children)
            return false;
        if (null == requiredChildren)
            return true;
        Iterator iter = requiredChildren.iterator();
        while (iter.hasNext()){
            String next = (String)iter.next();
            File test = new File(candidate.getPath()+File.separator+next);
            if (!test.exists())
                return false;
        }
        return true;
    }
    
  
    /**
     *
     *
     */
    public static String getDeployDir(String domainDir){
        String result="";
        result = domainDir + File.separator + "deploy"; //NOI18N
        return result;
        //todo: get real deploy path
    }
    
    public static String getHTTPConnectorPort(String domainDir){
        String defaultPort = "8080";
        String serverXml = domainDir + SERVER_XML; //NOI18N
        
        File serverXmlFile = new File(serverXml);
        if(!serverXmlFile.exists()){
            return defaultPort;
        }
        
        InputStream inputStream = null;
        Document document = null;
        try{
            inputStream = new FileInputStream(serverXmlFile);
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
            
            // get the root element
            Element root = document.getDocumentElement();
            
            NodeList children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeName().equals("Service")) {  // NOI18N
                    NodeList nl = child.getChildNodes();
                    for (int j = 0; j < nl.getLength(); j++){
                        Node ch = nl.item(j);
                        
                        if (ch.getNodeName().equals("Connector")) {  // NOI18N
                            return ch.getAttributes().getNamedItem("port").getNodeValue();
                        }
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            // it is ok
            // it optional functionality so we don't need to look at any exception
        }
        
        return defaultPort;
    }


    public static String getJnpPort(String domainDir){
        
        String serviceXml = domainDir+File.separator+"conf"+File.separator+"jboss-service.xml"; //NOI18N
        File xmlFile = new File(serviceXml);
        if (!xmlFile.exists()) return "";
        
        InputStream inputStream = null;
        Document document = null;
        try{
            inputStream = new FileInputStream(xmlFile);
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
            
            // get the root element
            Element root = document.getDocumentElement();
            
            // get the child nodes
            NodeList children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeName().equals("mbean")) {  // NOI18N
                    NodeList nl = child.getChildNodes();
                    if (!child.getAttributes().getNamedItem("name").getNodeValue().equals("jboss:service=Naming")) //NOI18N
                        continue;
                    for (int j = 0; j < nl.getLength(); j++){
                        Node ch = nl.item(j);
                        
                        if (ch.getNodeName().equals("attribute")) {  // NOI18N
                            if (!ch.getAttributes().getNamedItem("name").getNodeValue().equals("Port")) //NOI18N
                                continue;
                             return ch.getFirstChild().getNodeValue();
                        }
                    }
                }
            }
        }catch(Exception e){
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        return "";
    }
  
    public static String getRMINamingServicePort(String domainDir){
        
        String serviceXml = domainDir+File.separator+"conf"+File.separator+"jboss-service.xml"; //NOI18N
        File xmlFile = new File(serviceXml);
        if (!xmlFile.exists()) return "";
        
        InputStream inputStream = null;
        Document document = null;
        try{
            inputStream = new FileInputStream(xmlFile);
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
            
            // get the root element
            Element root = document.getDocumentElement();
            
            // get the child nodes
            NodeList children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeName().equals("mbean")) {  // NOI18N
                    NodeList nl = child.getChildNodes();
                    if (!child.getAttributes().getNamedItem("name").getNodeValue().equals("jboss:service=Naming")) //NOI18N
                        continue;
                    for (int j = 0; j < nl.getLength(); j++){
                        Node ch = nl.item(j);
                        
                        if (ch.getNodeName().equals("attribute")) {  // NOI18N
                            if (!ch.getAttributes().getNamedItem("name").getNodeValue().equals("RmiPort")) //NOI18N
                                continue;
                             return ch.getFirstChild().getNodeValue();
                        }
                    }
                }
            }
        }catch(Exception e){
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        return "";
    }
  
    public static String getRMIInvokerPort(String domainDir){
        
        String serviceXml = domainDir+File.separator+"conf"+File.separator+"jboss-service.xml"; //NOI18N
        File xmlFile = new File(serviceXml);
        if (!xmlFile.exists()) return "";
        
        InputStream inputStream = null;
        Document document = null;
        try{
            inputStream = new FileInputStream(xmlFile);
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
            
            // get the root element
            Element root = document.getDocumentElement();
            
            // get the child nodes
            NodeList children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeName().equals("mbean")) {  // NOI18N
                    NodeList nl = child.getChildNodes();
                    if (!child.getAttributes().getNamedItem("name").getNodeValue().equals("jboss:service=invoker,type=jrmp")) //NOI18N
                        continue;
                    for (int j = 0; j < nl.getLength(); j++){
                        Node ch = nl.item(j);
                        
                        if (ch.getNodeName().equals("attribute")) {  // NOI18N
                            if (!ch.getAttributes().getNamedItem("name").getNodeValue().equals("RMIObjectPort")) //NOI18N
                                continue;
                             return ch.getFirstChild().getNodeValue();
                        }
                    }
                }
            }
        }catch(Exception e){
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        return "";
    }
  
      /** Return true if the specified port is free, false otherwise. */
    public static boolean isPortFree(int port) {
        ServerSocket soc = null;
        try {
            soc = new ServerSocket(port);
        } catch (IOException ioe) {
            return false;
        } finally {
            if (soc != null)
                try { soc.close(); } catch (IOException ex) {} // noop
        }
        
        return true;
    }
    

}
