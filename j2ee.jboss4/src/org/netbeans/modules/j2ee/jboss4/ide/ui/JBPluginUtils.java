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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
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

    //--------------- checking for possible domain directory -------------
    private static List<String> domainRequirements4x;

    private static synchronized List<String> getDomainRequirements4x() {
        if (domainRequirements4x == null) {
            domainRequirements4x = new ArrayList<String>(13);
            Collections.addAll(domainRequirements4x,
                    "conf", // NOI18N
                    "deploy", // NOI18N
                    "lib", // NOI18N
                    "conf/jboss-service.xml", // NOI18N
                    "lib/jboss-j2ee.jar", // NOI18N
                    "lib/jboss.jar", // NOI18N
                    "lib/jbosssx.jar", // NOI18N
                    "lib/jboss-transaction.jar", // NOI18N
                    "lib/jmx-adaptor-plugin.jar", // NOI18N
                    "lib/jnpserver.jar", // NOI18N
                    "lib/log4j.jar", // NOI18N
                    "lib/xmlentitymgr.jar", // NOI18N
                    "deploy/jmx-invoker-service.xml"); // NOI18N
        }
        return domainRequirements4x;
    }

    private static List<String> domainRequirements5x;

    private static synchronized List<String> getDomainRequirements5x() {
        if (domainRequirements5x == null) {
            domainRequirements5x = new ArrayList<String>(12);
            Collections.addAll(domainRequirements5x,
                    "conf", // NOI18N
                    "deploy", // NOI18N
                    "lib", // NOI18N
                    "conf/jboss-service.xml", // NOI18N
                    "lib/jboss-j2ee.jar", // NOI18N
                    "lib/jboss.jar", // NOI18N
                    "lib/jbosssx.jar", // NOI18N
                    "lib/jboss-transaction.jar", // NOI18N
                    "lib/jmx-adaptor-plugin.jar", // NOI18N
                    "lib/jnpserver.jar", // NOI18N
                    "lib/log4j.jar", // NOI18N
                    "deploy/jmx-invoker-service.xml"); // NOI18N
        }
        return domainRequirements5x;
    }

    //--------------- checking for possible server directory -------------
    private static List<String> serverRequirements4x;

    private static synchronized List<String> getServerRequirements4x() {
        if (serverRequirements4x == null) {
            serverRequirements4x = new ArrayList<String>(6);
            Collections.addAll(serverRequirements4x,
                    "bin", // NOI18N
                    "client", // NOI18N
                    "lib", // NOI18N
                    "server", // NOI18N
                    "lib/jboss-common.jar", // NOI18N
                    "lib/endorsed/resolver.jar"); // NOI18N
        }
        return serverRequirements4x;
    }

    private static List<String> serverAlterRequirements4x;

    private static synchronized List<String> getServerAlterRequirements4x() {
        if (serverAlterRequirements4x == null) {
            serverAlterRequirements4x = new ArrayList<String>(8);
            Collections.addAll(serverAlterRequirements4x,
                    "bin", // NOI18N
                    "client", // NOI18N
                    "lib", // NOI18N
                    "server", // NOI18N
                    "lib/jboss-common.jar", // NOI18N
                    "client/jaxb-xjc.jar", // NOI18N
                    "client/jaxb-impl.jar", // NOI18N
                    "client/jaxb-api.jar"); // NOI18N
        }
        return serverAlterRequirements4x;
    }

    private static List<String> serverRequirements5x;

    private static synchronized List<String> getServerRequirements5x() {
        if (serverRequirements5x == null) {
            serverRequirements5x = new ArrayList<String>(6);
            Collections.addAll(serverRequirements5x,
                    "bin", // NOI18N
                    "client", // NOI18N
                    "lib", // NOI18N
                    "server", // NOI18N
                    "lib/jboss-common-core.jar", // NOI18N
                    "lib/endorsed/resolver.jar"); // NOI18N
        }
        return serverRequirements5x;
    }

    //------------  getting exists servers---------------------------
    /**
     * returns Hashmap
     * key = server name
     * value = server folder full path
     */
    public static Hashtable getRegisteredDomains(String serverLocation){
        Hashtable result = new Hashtable();
        //  String domainListFile = File.separator+"common"+File.separator+"nodemanager"+File.separator+"nodemanager.domains";  // NOI18N

        if (isGoodJBServerLocation4x(new File(serverLocation)) ||
            isGoodJBServerLocation5x(new File(serverLocation)))
        {
           File file = new File(serverLocation + File.separator + "server");  // NOI18N

            String[] files = file.list(new FilenameFilter(){
                public boolean accept(File dir, String name){
                    if ((new File(dir.getAbsolutePath()+File.separator+name)).isDirectory()) return true;
                    return false;
                }
            });

            for(int i =0; i<files.length; i++){
                String path = file.getAbsolutePath() + File.separator + files[i];

                if (isGoodJBInstanceLocation4x(new File(path)) ||
                    isGoodJBInstanceLocation5x(new File(path)))
                {
                    result.put(files[i], path);
                }
            }
        }
        return result;
    }

    private static boolean isGoodJBInstanceLocation(File candidate, List<String> requirements){
        if (null == candidate ||
                !candidate.exists() ||
                !candidate.canRead() ||
                !candidate.isDirectory()  ||
                !hasRequiredChildren(candidate, requirements)) {
            return false;
        }
        return true;
    }

    public static boolean isGoodJBInstanceLocation4x(File candidate){
        return isGoodJBInstanceLocation(candidate, getDomainRequirements4x());
    }

    public static boolean isGoodJBInstanceLocation5x(File candidate){
        return isGoodJBInstanceLocation(candidate, getDomainRequirements5x());
    }

    private static boolean isGoodJBServerLocation(File candidate, List<String> requirements){
        if (null == candidate ||
                !candidate.exists() ||
                !candidate.canRead() ||
                !candidate.isDirectory()  ||
                !hasRequiredChildren(candidate, requirements)) {
            return false;
        }
        return true;
    }

    public static boolean isGoodJBServerLocation4x(File candidate){
        return isGoodJBServerLocation(candidate, getServerRequirements4x()) ||
                isGoodJBServerLocation(candidate, getServerAlterRequirements4x());
    }

    public static boolean isGoodJBServerLocation4x(JBDeploymentManager dm){
        String installDir = dm.getInstanceProperties().getProperty(JBPluginProperties.PROPERTY_ROOT_DIR);
        return isGoodJBServerLocation4x(new File(installDir));
    }

    public static boolean isGoodJBServerLocation5x(File candidate){
        return isGoodJBServerLocation(candidate, getServerRequirements5x());
    }

    /**
     * Checks whether the given candidate has all required childrens. Children
     * can be both files and directories. Method does not distinguish between them.
     *
     * @return true if the candidate has all files/directories named in requiredChildren,
     *             false otherwise
     */
    private static boolean hasRequiredChildren(File candidate, List<String> requiredChildren) {
        if (null == candidate || null == candidate.list()) {
            return false;
        }
        if (null == requiredChildren) {
            return true;
        }

        for (String next : requiredChildren) {
            File test = new File(candidate.getPath() + File.separator + next);
            if (!test.exists()) {
                return false;
            }
        }
        return true;
    }

    //--------------------------------------------------------------------

    /**
     *
     *
     */
    public static String getDeployDir(String domainDir){
        return domainDir + File.separator + "deploy"; //NOI18N
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
        try {
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
        } catch (Exception e) {
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
        try {
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
        } catch (Exception e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }
        return "";
    }

    public static String getRMINamingServicePort(String domainDir){

        String serviceXml = domainDir+File.separator+"conf"+File.separator+"jboss-service.xml"; //NOI18N
        File xmlFile = new File(serviceXml);
        if (!xmlFile.exists()) return "";

        InputStream inputStream = null;
        Document document = null;
        try {
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
        } catch (Exception e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }
        return "";
    }

    public static String getRMIInvokerPort(String domainDir){

        String serviceXml = domainDir+File.separator+"conf"+File.separator+"jboss-service.xml"; //NOI18N
        File xmlFile = new File(serviceXml);
        if (!xmlFile.exists()) return "";

        InputStream inputStream = null;
        Document document = null;
        try {
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
        } catch (Exception e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
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
