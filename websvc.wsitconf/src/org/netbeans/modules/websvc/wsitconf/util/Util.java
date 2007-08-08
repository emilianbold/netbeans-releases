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

package org.netbeans.modules.websvc.wsitconf.util;

import java.util.logging.Level;

import java.util.logging.Logger;
import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import java.awt.Container;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.swing.JComponent;
import java.util.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.websvc.jaxwsruntimemodel.JavaWsdlMapper;
import org.netbeans.modules.websvc.wsitconf.ui.service.profiles.UsernameAuthenticationProfile;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.KeystorePanel;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Util {

    public enum ProjectType {
        WEB, EJB, CLIENT, UNKNOWN
    };
    
    private static final Logger logger = Logger.getLogger(Util.class.getName());

    /*
     * Changes the text of a JLabel in component from oldLabel to newLabel
     */
    public static void changeLabelInComponent(JComponent component, String oldLabel, String newLabel) {
        JLabel label = findLabel(component, oldLabel);
        if(label != null) {
            label.setText(newLabel);
        }
    }
    
    /*
     * Hides a JLabel and the component that it is designated to labelFor, if any
     */
    public static void hideLabelAndLabelFor(JComponent component, String lab) {
        JLabel label = findLabel(component, lab);
        if(label != null) {
            label.setVisible(false);
            Component c = label.getLabelFor();
            if(c != null) {
                c.setVisible(false);
            }
        }
    }
    
    /*
     * Recursively gets all components in the components array and puts it in allComponents
     */
    public static void getAllComponents( Component[] components, Collection<Component> allComponents ) {
        for( int i = 0; i < components.length; i++ ) {
            if( components[i] != null ) {
                allComponents.add( components[i] );
                if( ( ( Container )components[i] ).getComponentCount() != 0 ) {
                    getAllComponents( ( ( Container )components[i] ).getComponents(), allComponents );
                }
            }
        }
    }
    
    /*
     *  Recursively finds a JLabel that has labelText in comp
     */
    public static JLabel findLabel(JComponent comp, String labelText) {
        Vector<Component> allComponents = new Vector<Component>();
        getAllComponents(comp.getComponents(), allComponents);
        Iterator iterator = allComponents.iterator();
        while(iterator.hasNext()) {
            Component c = (Component)iterator.next();
            if(c instanceof JLabel) {
                JLabel label = (JLabel)c;
                if(label.getText().equals(labelText)) {
                    return label;
                }
            }
        }
        return null;
    }
    
    /**
     * Returns Java source groups for all source packages in given project.<br>
     * Doesn't include test packages.
     *
     * @param project Project to search
     * @return Array of SourceGroup. It is empty if any probelm occurs.
     */
    public static SourceGroup[] getJavaSourceGroups(Project project) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                                    JavaProjectConstants.SOURCES_TYPE_JAVA);
        Set testGroups = getTestSourceGroups(sourceGroups);
        List<SourceGroup> result = new ArrayList<SourceGroup>();
        for (int i = 0; i < sourceGroups.length; i++) {
            if (!testGroups.contains(sourceGroups[i])) {
                result.add(sourceGroups[i]);
            }
        }
        return result.toArray(new SourceGroup[result.size()]);
    }

    private static Set<SourceGroup> getTestSourceGroups(SourceGroup[] sourceGroups) {
        Map foldersToSourceGroupsMap = createFoldersToSourceGroupsMap(sourceGroups);
        Set<SourceGroup> testGroups = new HashSet<SourceGroup>();
        for (int i = 0; i < sourceGroups.length; i++) {
            testGroups.addAll(getTestTargets(sourceGroups[i], foldersToSourceGroupsMap));
        }
        return testGroups;
    }
    
    private static Map createFoldersToSourceGroupsMap(final SourceGroup[] sourceGroups) {
        Map<FileObject, SourceGroup> result;
        if (sourceGroups.length == 0) {
            result = Collections.emptyMap();
        } else {
            result = new HashMap<FileObject, SourceGroup>(2 * sourceGroups.length, .5f);
            for (int i = 0; i < sourceGroups.length; i++) {
                SourceGroup sourceGroup = sourceGroups[i];
                result.put(sourceGroup.getRootFolder(), sourceGroup);
            }
        }
        return result;
    }

    private static List<FileObject> getFileObjects(URL[] urls) {
        List<FileObject> result = new ArrayList<FileObject>();
        for (int i = 0; i < urls.length; i++) {
            FileObject sourceRoot = URLMapper.findFileObject(urls[i]);
            if (sourceRoot != null) {
                result.add(sourceRoot);
            } else {
                if (logger.isLoggable(Level.INFO)) {
                    logger.log(Level.INFO, "No FileObject found for the following URL: " + urls[i]); //NOI18N
                }
            }
        }
        return result;
    }
    
    private static List<SourceGroup> getTestTargets(SourceGroup sourceGroup, Map foldersToSourceGroupsMap) {
        final URL[] rootURLs = UnitTestForSourceQuery.findUnitTests(sourceGroup.getRootFolder());
        if (rootURLs.length == 0) {
            return Collections.emptyList();
        }
        List<SourceGroup> result = new ArrayList<SourceGroup>();
        List<FileObject> sourceRoots = getFileObjects(rootURLs);
        for (int i = 0; i < sourceRoots.size(); i++) {
            FileObject sourceRoot = sourceRoots.get(i);
            SourceGroup srcGroup = (SourceGroup) foldersToSourceGroupsMap.get(sourceRoot);
            if (srcGroup != null) {
                result.add(srcGroup);
            }
        }
        return result;
    }

    /* Used to validate number inputs
     */
    public static boolean isPositiveNumber(String s, boolean zeroAllowed, boolean allowEmptyValue) {
        Integer i = null;
        if ((s == null) || ("".equals(s))) {
            return allowEmptyValue ? true : false;
        }
        try {
            i = Integer.parseInt(s);
            if (i != null) {
                if (zeroAllowed) {
                    return i.intValue() >= 0;
                }
                return i.intValue() > 0;
            }
        } catch (NumberFormatException nfe) {}
        return false;
    }

    public static String getServerStoreLocation(Project project, boolean trust) {
        String storeLocation = null;
        J2eeModuleProvider mp = project.getLookup().lookup(J2eeModuleProvider.class);
        if (mp != null) {
            InstanceProperties ip = mp.getInstanceProperties();
            if ("".equals(ip.getProperty("LOCATION"))) {    //NOI18N
                return null;
            }
            
            J2eePlatform j2eePlatform = getJ2eePlatform(project);
            if (j2eePlatform != null) {
                File[] keyLocs = null;
                keyLocs = trust ? j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_TRUSTSTORE_CLIENT) :
                                  j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_KEYSTORE_CLIENT);
                if ((keyLocs != null) && (keyLocs.length > 0)) {
                    storeLocation = keyLocs[0].getAbsolutePath();
                }
            }
        }
        return storeLocation;
    }
    
    public static List<String> getAliases(String storePath, char[] password, String type) throws IOException {
        if ((storePath == null) || (type == null)) return null;
        FileInputStream iStream = null;
        try {
            File f = new File(storePath);
            if ((f == null) || (!f.exists())) {
                throw new IOException();
            }
            iStream = new FileInputStream(f);
            java.security.KeyStore keyStore;
            keyStore = java.security.KeyStore.getInstance(type);
            keyStore.load(iStream, password); 
            Enumeration<String> e = keyStore.aliases();
            ArrayList<String> arr = new ArrayList<String>(keyStore.size());
            while (e.hasMoreElements()) {
                String key = e.nextElement();
                arr.add(key);
            }
            Collections.sort(arr);
            return arr;
        } catch (FileNotFoundException ex) {
            logger.log(Level.INFO, null, ex);
        } catch (KeyStoreException ex) {
            logger.log(Level.INFO, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            logger.log(Level.INFO, null, ex);
        } catch (CertificateException ex) {
            logger.log(Level.INFO, null, ex);
        } finally {
            if (iStream != null) iStream.close();
        }
        return null;
    }

    public static final String getPassword(Project p) {
        J2eeModuleProvider mp = p.getLookup().lookup(J2eeModuleProvider.class);
        if (mp != null) {
            InstanceProperties ip = mp.getInstanceProperties();
            return ip.getProperty(InstanceProperties.PASSWORD_ATTR);
        }
        return "";
    }

    private static String getServerInstanceID(Project p) {
        if (p != null) {
            J2eeModuleProvider mp = p.getLookup().lookup(J2eeModuleProvider.class);
            if (mp != null) {
                return mp.getServerInstanceID();
            }
        }
        return null;
    }
    
    public static final boolean isWsitSupported(Project p) {

        // check if the wsimport class is already present - this means we don't need to add the library
        SourceGroup[] sgs = ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        ClassPath classPath = ClassPath.getClassPath(sgs[0].getRootFolder(),ClassPath.COMPILE);
        FileObject wsimportFO = classPath.findResource("com/sun/xml/ws/policy/Policy.class"); // NOI18N
        
        if (wsimportFO == null) {
            J2eePlatform j2eePlatform = getJ2eePlatform(p);
            if (j2eePlatform != null) {
                return j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSIT); //NOI18N
            }
        }
        return true;
    }

    public static J2eePlatform getJ2eePlatform(Project project) {
        String serverInstanceID = getServerInstanceID(project);
        if ((serverInstanceID != null) && (serverInstanceID.length() > 0)) {
            return Deployment.getDefault().getJ2eePlatform(serverInstanceID);
        }
        return null;
    }
    
    /**
     * Is J2EE version of a given project JavaEE 5 or higher?
     *
     * @param project J2EE project
     * @return true if J2EE version is JavaEE 5 or higher; otherwise false
     */
    public static boolean isJavaEE5orHigher(Project project) {
        if (project == null) {
            return false;
        }
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider != null) {
            J2eeModule j2eeModule = j2eeModuleProvider.getJ2eeModule();
            if (j2eeModule != null) {
                Object type = j2eeModule.getModuleType();
                double version = Double.parseDouble(j2eeModule.getModuleVersion());
                if (J2eeModule.EJB.equals(type) && (version > 2.1)) {
                    return true;
                }
                if (J2eeModule.WAR.equals(type) && (version > 2.4)) {
                    return true;
                }
                if (J2eeModule.CLIENT.equals(type) && (version > 1.4)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static String getServerName(Project p) {
        String sID = getServerInstanceID(p);
        if (sID != null) {
            J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(sID);
            return j2eePlatform.getDisplayName();
        }
        return null;
    }

    public static String getStoreLocation(Project project, boolean trust, boolean client) {
        String keystoreLocation = null;
        J2eeModuleProvider mp = project.getLookup().lookup(J2eeModuleProvider.class);
        if (mp != null) {
            String sID = mp.getServerInstanceID();
            
            InstanceProperties ip = mp.getInstanceProperties();
            if ("".equals(ip.getProperty("LOCATION"))) {
                return "";
            }
            
            J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(sID);
            File[] keyLocs = null;
            String store = null;
            if (client) {
                store = trust ? J2eePlatform.TOOL_TRUSTSTORE_CLIENT : J2eePlatform.TOOL_KEYSTORE_CLIENT;
            } else {
                store = trust ? J2eePlatform.TOOL_TRUSTSTORE : J2eePlatform.TOOL_KEYSTORE;
            }
            keyLocs = j2eePlatform.getToolClasspathEntries(store);
            if ((keyLocs != null) && (keyLocs.length > 0)) {
                keystoreLocation = keyLocs[0].getAbsolutePath();
            }
        }
        return keystoreLocation;
    }

    public static FileObject getTomcatLocation(Project project) {
        J2eeModuleProvider mp = project.getLookup().lookup(J2eeModuleProvider.class);
        FileObject folder = null;
        if (mp != null) { 
            try {
                String id = mp.getServerInstanceID();
                int begin = id.indexOf("home=") + 5;
                int end = id.indexOf(":", id.indexOf("home=") + 1);
                if (end <= begin) {
                    end = id.length();
                }
                String location = id.substring(begin, end);
                File f = new File(location);
                if (f != null) {
                    folder = FileUtil.toFileObject(f);
                }
            } catch (Exception ex) {
                Logger.getLogger("global").log(Level.INFO, null, ex);
            }    
        }
        return folder;
    }

    public static String getDefaultPassword(Project p) {
        String password = KeystorePanel.DEFAULT_PASSWORD;
        if (isGlassfish(p)) {
            String storeLoc = getStoreLocation(p, false, false);
            try {
                getAliases(storeLoc, password.toCharArray(), KeystorePanel.JKS);
            } catch (IOException ioe) {
                password = Util.getPassword(p);
                try {
                    getAliases(storeLoc, password.toCharArray(), KeystorePanel.JKS);
                } catch (IOException ioe2) {
                    password = "";
                }
            }
        }        
        return password;
    }
    
    public static final void fillDefaults(Project project, boolean client) {

        final String STORE_FOLDER_NAME = "certs";
        
        boolean tomcat = isTomcat(project);
        boolean glassfish = isGlassfish(project);
        
        String serverKeyStorePath = getStoreLocation(project, false, false);
        String serverTrustStorePath = getStoreLocation(project, true, false);
        String clientKeyStorePath = getStoreLocation(project, false, true);
        String clientTrustStorePath = getStoreLocation(project, true, true);

        String serverKeyStoreBundled = "/org/netbeans/modules/websvc/wsitconf/resources/server-keystore.jks"; //NOI18N
        String serverTrustStoreBundled = "/org/netbeans/modules/websvc/wsitconf/resources/server-truststore.jks"; //NOI18N
        String clientKeyStoreBundled = "/org/netbeans/modules/websvc/wsitconf/resources/client-keystore.jks"; //NOI18N
        String clientTrustStoreBundled = "/org/netbeans/modules/websvc/wsitconf/resources/client-truststore.jks"; //NOI18N
        
        String srcPasswd = "changeit";
        
        if (tomcat) {
            if (project != null) {
                FileObject tomcatLocation = getTomcatLocation(project);
                try {
                    FileObject targetFolder = FileUtil.createFolder(tomcatLocation, STORE_FOLDER_NAME);
                    DataFolder folderDO = (DataFolder) DataObject.find(targetFolder);
                    FileSystem fs = Repository.getDefault().getDefaultFileSystem();
                    FileObject foClientKey = fs.findResource("Templates/WebServices/client-keystore.jks"); // NOI18N
                    FileObject foClientTrust = fs.findResource("Templates/WebServices/client-truststore.jks"); // NOI18N
                    FileObject foServerKey = fs.findResource("Templates/WebServices/server-keystore.jks"); // NOI18N
                    FileObject foServerTrust = fs.findResource("Templates/WebServices/server-truststore.jks"); // NOI18N
                    FileObject[] filesToCreate = {foClientKey, foClientTrust, foServerKey, foServerTrust };
                    for (FileObject fo : filesToCreate) {
                        if (fo != null) {
                            DataObject template = DataObject.find(fo);
                            if (template != null) {
                                if (targetFolder.getFileObject(fo.getName(), fo.getExt()) == null) {
                                    template.createFromTemplate(folderDO, fo.getNameExt());
                                }
                            }
                        }
                    }

                    if (!client) {
                        FileObject tomcatUsers = tomcatLocation.getFileObject("conf/tomcat-users.xml");

                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = dbf.newDocumentBuilder();
                        Document document = builder.parse(FileUtil.toFile(tomcatUsers));

                        NodeList nodes = document.getElementsByTagName("tomcat-users");
                        if ((nodes != null) && (nodes.getLength() > 0)) {
                            Node n = nodes.item(0);
                            NodeList users = document.getElementsByTagName("user");
                            boolean foundUser = false;
                            for (int i=0; i < users.getLength(); i++) {
                                Node node = users.item(i);
                                if (node instanceof Element) {
                                    Element u = (Element)node;
                                    String userAttr = u.getAttribute("name");
                                    if (UsernameAuthenticationProfile.DEFAULT_USERNAME.equals(userAttr)) {
                                        foundUser = true;
                                        break;
                                    }
                                }
                            }
                            if (!foundUser) {
                                if (tomcatUsers.getParent().getFileObject("tomcat-users.backup", "xml") == null) {
                                    FileUtil.copyFile(tomcatUsers, tomcatUsers.getParent(), "tomcat-users.backup");
                                }

                                Element user = document.createElement("user");
                                user.setAttribute("name", UsernameAuthenticationProfile.DEFAULT_USERNAME);
                                user.setAttribute("password", UsernameAuthenticationProfile.DEFAULT_PASSWORD);
                                user.setAttribute("roles", "tomcat");
                                n.appendChild(user);

                                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                                transformer.setOutputProperty(OutputKeys.INDENT, "yes");

                                //initialize StreamResult with File object to save to file
                                StreamResult result = new StreamResult(FileUtil.toFile(tomcatUsers));
                                DOMSource source = new DOMSource(document);
                                transformer.transform(source, result);
                            }
                        }
                    }
                } catch (Exception ex) {
                    Logger.getLogger("global").log(Level.INFO, null, ex);
                }
            }
            return;
        }

        String dstPasswd = getDefaultPassword(project);
        
        if (glassfish) {
            try {
                if (!client) refreshBuildScript(project);
                copyKey(serverKeyStoreBundled, "xws-security-server", srcPasswd, srcPasswd, serverKeyStorePath, "xws-security-server", dstPasswd, false);
                copyKey(serverKeyStoreBundled, "wssip", srcPasswd, srcPasswd, serverKeyStorePath, "wssip", dstPasswd, false);
                copyKey(serverTrustStoreBundled, "certificate-authority", srcPasswd, srcPasswd, serverTrustStorePath, "xwss-certificate-authority", dstPasswd, true);
                copyKey(serverTrustStoreBundled, "xws-security-client", srcPasswd, srcPasswd, serverTrustStorePath, "xws-security-client", dstPasswd, true);
                copyKey(clientKeyStoreBundled, "xws-security-client", srcPasswd, srcPasswd, clientKeyStorePath, "xws-security-client", dstPasswd, false);
                copyKey(clientTrustStoreBundled, "xws-security-server", srcPasswd, srcPasswd, clientTrustStorePath, "xws-security-server", dstPasswd, true);
                copyKey(clientTrustStoreBundled, "wssip", srcPasswd, srcPasswd, clientTrustStorePath, "wssip", dstPasswd, true);
            } catch (Exception ex) {
                Logger.getLogger("global").log(Level.INFO, null, ex);
            }
        }
        
    }
    
    public static void copyKey(String srcPath, String srcAlias, String srcPasswd, String srcKeyPasswd, 
                               String dstPath, String dstAlias, String dstPasswd,  
                               boolean trustedCertEntry) throws Exception {
        KeyStore srcStore = KeyStore.getInstance("JKS");
        KeyStore dstStore = KeyStore.getInstance("JKS");
        srcStore.load(Util.class.getResourceAsStream(srcPath), srcPasswd.toCharArray());
        InputStream is = new FileInputStream(dstPath);
        try {
            dstStore.load(is, dstPasswd.toCharArray());
            Key privKey = srcStore.getKey(srcAlias, srcKeyPasswd.toCharArray());

            if (is != null) is.close();
            
            OutputStream os = new FileOutputStream(dstPath);
            try {
                if (privKey == null || trustedCertEntry) {
                    //this is a cert-entry
                    dstStore.setCertificateEntry(dstAlias, srcStore.getCertificate(srcAlias));
                } else {              
                    Certificate cert = srcStore.getCertificate(srcAlias);
                    Certificate[] chain = new Certificate[] {cert};
                    dstStore.setKeyEntry(dstAlias, privKey, srcKeyPasswd.toCharArray(), chain);
                }
                dstStore.store(os, dstPasswd.toCharArray());
            } finally {
                if (os != null) os.close();
            }
        } finally {
            if (is != null) is.close();
        }
    }
    
    public static final boolean isTomcat(Project project) {
        String sID = getServerInstanceID(project);
        if (sID != null) {
            if ((sID != null) && (sID.toLowerCase().contains("tomcat"))) {     //NOI18N
                return true;
            }
        }
        return false;
    }

    public static final boolean isGlassfish(Project project) {
        if (project != null) {
            J2eeModuleProvider mp = project.getLookup().lookup(J2eeModuleProvider.class);
            if (mp != null) {
                String instid = mp.getServerInstanceID();
                if ((instid != null) && (instid.toLowerCase().contains("appserv"))) {     //NOI18N
                    return true;
                }
            }
        }
        return false;
    }
    
    public static final boolean isWebProject(Project project) {
        if (getProjectType(project) == ProjectType.WEB) {
            return true;
        }
        return false;
    }

    public static final boolean isJsr109Project(Project project) {
        if ((getProjectType(project) == ProjectType.WEB) || 
            (getProjectType(project) == ProjectType.EJB)) {
            return true;
        }
        return false;
    }
    
    private static J2eeModuleProvider getProvider(Project p) {
        if (p != null) {
            return p.getLookup().lookup(J2eeModuleProvider.class);
        }
        return null;
    }
    
    public static final ProjectType getProjectType(Project project) {
        J2eeModuleProvider mp = getProvider(project);
        if (mp != null) {
            J2eeModule jm = mp.getJ2eeModule();
            if (jm != null) {
                Object moduleType = jm.getModuleType();
                if (J2eeModule.WAR.equals(moduleType)) {
                    return ProjectType.WEB;
                } else if (J2eeModule.EJB.equals(moduleType)) {
                    return ProjectType.EJB;
                } else if (J2eeModule.CLIENT.equals(moduleType)) {
                    return ProjectType.CLIENT;
                }
            }
        }
        return ProjectType.UNKNOWN;
    }

    private static boolean isOperationInList(String operName, Collection<BindingOperation> operations) {
        Iterator<BindingOperation> i = operations.iterator();
        while (i.hasNext()) {
            BindingOperation bo = i.next();
            if ((bo != null) && (operName.equals(bo.getName()))) {
                return true;
            }
        }
        return false;
    }
    
    public static Collection<BindingOperation> refreshOperations(Binding binding, FileObject jc) {
        
        if (binding == null) {
            return null;
        }
        
        Collection<BindingOperation> operations = binding.getBindingOperations();
        if (jc == null) {
            return operations;
        }
        
        PortType pt = getPortType(binding);
        
        // create operations and add them to the binding element
        List<String> bindingOperationNames = JavaWsdlMapper.getOperationNames(jc);
        for (String name : bindingOperationNames) {
            if (!isOperationInList(name, operations)) {
                generateOperation(binding, pt, name, jc);
            }
        }
        
        return binding.getBindingOperations();
    }

    public static BindingOperation generateOperation(Binding binding, PortType portType, String operationName, FileObject implClass) {
        WSDLModel model = binding.getModel();
        WSDLComponentFactory wcf = model.getFactory();
        Definitions d = (Definitions) binding.getParent();

        BindingOperation bindingOperation;
        
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }

        try {
            bindingOperation = wcf.createBindingOperation();
            bindingOperation.setName(operationName);
            binding.addBindingOperation(bindingOperation);

            // add input/output messages
            org.netbeans.modules.xml.wsdl.model.Message inputMsg = wcf.createMessage();
            inputMsg.setName(operationName);
            d.addMessage(inputMsg);

            org.netbeans.modules.xml.wsdl.model.Message outMsg = wcf.createMessage();
            outMsg.setName(operationName + "Response");                  //NOI18N
            d.addMessage(outMsg);

            org.netbeans.modules.xml.wsdl.model.RequestResponseOperation oper = wcf.createRequestResponseOperation();
            oper.setName(operationName);
            portType.addOperation(oper);

            org.netbeans.modules.xml.wsdl.model.Input input = wcf.createInput();
            oper.setInput(input);
            input.setMessage(input.createReferenceTo(inputMsg, org.netbeans.modules.xml.wsdl.model.Message.class));

            org.netbeans.modules.xml.wsdl.model.Output out = wcf.createOutput();
            oper.setOutput(out);
            out.setMessage(out.createReferenceTo(outMsg, org.netbeans.modules.xml.wsdl.model.Message.class));

            org.netbeans.modules.xml.wsdl.model.BindingOutput bindingOutput = wcf.createBindingOutput();
            bindingOperation.setBindingOutput(bindingOutput);
            org.netbeans.modules.xml.wsdl.model.BindingInput bindingInput = wcf.createBindingInput();
            bindingOperation.setBindingInput(bindingInput);

            //add faults
            List<String> operationFaults = JavaWsdlMapper.getOperationFaults(implClass, operationName);
            for (String fault : operationFaults) {
                org.netbeans.modules.xml.wsdl.model.BindingFault bindingFault = wcf.createBindingFault();
                bindingOperation.addBindingFault(bindingFault);
            }
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
        
        return bindingOperation;
    }

    public static PortType getPortType(Binding binding) {
        Definitions d = (Definitions) binding.getParent();

        QName portTypeQName = binding.getType().getQName();
        PortType portType = null;
        
        Collection<PortType> portTypes = d.getPortTypes();
        Iterator<PortType> i = portTypes.iterator();
        while (i.hasNext()) {
            PortType pt = i.next();
            if (pt != null) {
                if (portTypeQName.getLocalPart().equals(pt.getName())) {
                    portType = pt;
                    break;
                }
            }
        }
        return portType;
    }
    
    public static FileObject getFOForModel(WSDLModel model) {
        if (model == null) return null;
        ModelSource ms = model.getModelSource();
        return Utilities.getFileObject(ms);
    }

    private static final String BUILD_SCRIPT = "/build.xml";       //NOI18N
    private static final String BACKUP_EXT = ".bak";        //NOI18N
    private static final String IMPORT_WSIT_DEPLOY_XML = "<import file=\"nbproject/wsit-deploy.xml\"/>";    //NOI18N
    private static final String IMPORT_TAG = "<import"; //NOI18N
    private static final String WSIT_DEPLOY_XML_PATH = "nbproject/wsit-deploy.xml"; //NOI18N
    private static final String WSIT_DEPLOY_XSL = "org/netbeans/modules/websvc/wsitconf/resources/wsit-deploy.xsl";  //NOI18N
    
    public static void refreshBuildScript(Project p) {
        String buildScript = FileUtil.toFile(p.getProjectDirectory()).getPath() + BUILD_SCRIPT;
        
        BufferedReader reader = null;
        BufferedWriter writer = null;
        String line = null;
        boolean added = false;
        
        // First check to see if our import statement has already been added.
        try {
            reader = new BufferedReader(new FileReader(buildScript));
            while ((line = reader.readLine()) != null) {
                if (line.indexOf(IMPORT_WSIT_DEPLOY_XML) != -1) {
                    added = true;
                    break;
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        } catch (IOException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    Logger.getLogger("global").log(Level.INFO, null, ex);
                }
            }
        }
        
        // If our import statement has not been added, add it now.
        if (!added) {
            try {
                // Rename the original to build.xml.bak
                File backupBuildScript = new File(buildScript);
                backupBuildScript.renameTo(new File(buildScript + BACKUP_EXT));
                
                reader = new BufferedReader(new FileReader(buildScript + BACKUP_EXT));
                writer = new BufferedWriter(new FileWriter(buildScript));
                added = false;
                int index = 0;
                
                while ((line = reader.readLine()) != null) {
                    if (!added && (index = line.indexOf(IMPORT_TAG)) != -1) {
                        StringBuffer buf = new StringBuffer(line);
                        buf = buf.replace(index, line.length(), IMPORT_WSIT_DEPLOY_XML);
                        writer.write(buf.toString());
                        writer.newLine();
                        added = true;
                    }
                    
                    writer.write(line);
                    writer.newLine();
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger("global").log(Level.INFO, null, ex);
            } catch (IOException ex) {
                Logger.getLogger("global").log(Level.INFO, null, ex);
            } finally {
                try {
                    if (writer != null) {
                        writer.flush();
                        writer.close();
                    }
                    
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger("global").log(Level.INFO, null, ex);
                }
            }
        }
        
        // Now refresh the wsit-deploy.xml itself.
        GeneratedFilesHelper genFilesHelper = new GeneratedFilesHelper(p.getProjectDirectory());
        
        try {
            genFilesHelper.refreshBuildScript(
                    WSIT_DEPLOY_XML_PATH,
                    Util.class.getClassLoader().getResource(WSIT_DEPLOY_XSL),
                    false);
        } catch (IOException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        }
    }
    
    public static void unfillDefaults(Project p) {
        String buildScript = FileUtil.toFile(p.getProjectDirectory()).getPath() + BUILD_SCRIPT;
        
        BufferedReader reader = null;
        BufferedWriter writer = null;
        String line = null;
        boolean added = false;
        
        // First check to see if our import statement has already been added.
        try {
            reader = new BufferedReader(new FileReader(buildScript));
            while ((line = reader.readLine()) != null) {
                if (line.indexOf(IMPORT_WSIT_DEPLOY_XML) != -1) {
                    added = true;
                    break;
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        } catch (IOException ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    Logger.getLogger("global").log(Level.INFO, null, ex);
                }
            }
        }
        
        // If our import statement has not been added, add it now.
        if (added) {
            try {
                // Rename the original to build.xml.bak
                File backupBuildScript = new File(buildScript);
                backupBuildScript.renameTo(new File(buildScript + BACKUP_EXT));
                
                reader = new BufferedReader(new FileReader(buildScript + BACKUP_EXT));
                writer = new BufferedWriter(new FileWriter(buildScript));
                added = false;
                int index = 0;
                
                while ((line = reader.readLine()) != null) {
                    if ((index = line.indexOf(IMPORT_WSIT_DEPLOY_XML)) == -1) {
                        writer.write(line);
                        writer.newLine();
                    }
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger("global").log(Level.INFO, null, ex);
            } catch (IOException ex) {
                Logger.getLogger("global").log(Level.INFO, null, ex);
            } finally {
                try {
                    if (writer != null) {
                        writer.flush();
                        writer.close();
                    }
                    
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger("global").log(Level.INFO, null, ex);
                }
            }
        }
    }

    public static FileObject getSunDDFO(Project p) {
        FileObject[] fobjs = getProvider(p).getConfigurationFiles();
        if (fobjs.length > 0) {
            return fobjs[0];
        }
        return null;
    }    
}
