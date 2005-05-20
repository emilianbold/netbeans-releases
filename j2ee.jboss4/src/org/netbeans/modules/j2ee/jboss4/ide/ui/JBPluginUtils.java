/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.jboss4.ide.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Ivan Sidorkin
 */
public class JBPluginUtils {
    
    //------------  getting exists servers---------------------------
    /**
     * returns Hashmap
     * key = server name
     * value = server folder full path
     */
    public static Hashtable getRegisteredDomains(){
        Hashtable result = new Hashtable();
        //  String domainListFile = File.separator+"common"+File.separator+"nodemanager"+File.separator+"nodemanager.domains";  // NOI18N
        
        String serverLoc = JBPluginProperties.getInstance().getInstallLocation();
        if (isGoodJBServerLocation(new File(serverLoc))){
            File file = new File(serverLoc+File.separator + "server");  // NOI18N
            
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
        serverFileColl.add("lib/jboss-management.jar");      // NOI18N
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
            // it is ok
            // it optional functionality so we don't need to look at any exception
        }
        return "";
    }
}
