/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.compapp.projects.jbi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.compapp.projects.jbi.descriptor.XmlUtil;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.VisualClassPathItem;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static org.netbeans.modules.compapp.projects.jbi.CasaConstants.*;

/**
 * A helper class to handle CASA related functions.
 * 
 * @author jqian
 */
public class CasaHelper {
    public static String CASA_DIR_NAME = "/src/conf/";  // NOI18N 
    public static String CASA_EXT = ".casa";  // NOI18N 
    private static String WSDL_EXT = ".wsdl";  // NOI18N 
    private static String LOCK_FILE_PREFIX = ".LCK";  // NOI18N 
    private static String LOCK_FILE_SUFFIX = "~";  // NOI18N

    // WSIT Callback Java project support
    private static final String WSIT_CALLBACK_NAMESPACE = "http://www.sun.com/jbi/wsit/callbackproject"; // NOI18N
    private static final String WSIT_CALLBACK_ELEMENT_NAME = "WsitCallback"; // NOI18N
    private static final String WSIT_CALLBACK_PROJECT_ATTR_NAME = "CallbackProject"; // NOI18N

    // JAX-WS Handlers
    public static final String JAXWS_HANDLER_NAMESPACE = "http://www.sun.com/jbi/httpbc/jaxws_handlers"; // NOI18N
    public static final String JAXWS_HANDLER_ELEMENT_NAME = "handler"; // NOI18N
    public static final String JAXWS_HANDLER_CHAIN_ELEMENT_NAME = "handler-chain"; // NOI18N
    public static final String JAXWS_HANDLER_PROJECT_PATH_ATTR_NAME = "projectpath"; // NOI18N
    public static final String JAXWS_HANDLER_JAR_PATHS_ATTR_NAME = "jarpaths"; // NOI18N

    // JAX-RS Handlers
    public static final String JAXRS_HANDLER_NAMESPACE = "http://www.sun.com/jbi/restbc/jaxrs_filters"; // NOI18N
    public static final String JAXRS_HANDLER_ELEMENT_NAME = "filter"; // NOI18N
    public static final String JAXRS_HANDLER_CHAIN_ELEMENT_NAME = "filter-chain"; // NOI18N
    public static final String JAXRS_HANDLER_PROJECT_PATH_ATTR_NAME = "projectpath"; // NOI18N
    public static final String JAXRS_HANDLER_JAR_PATHS_ATTR_NAME = "jarpaths"; // NOI18N


    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.compapp.projects.jbi.CasaHelper"); // NOI18N
    
    /**
     * Gets the name of the CASA file in the given project.
     * 
     * @param project   a JBI project
     * 
     * @return  CASA file name
     */
    public static String getCasaFileName(Project project) {
        ProjectInformation projInfo = 
                project.getLookup().lookup(ProjectInformation.class);    
        String projName = projInfo.getName();
                            
        File pf = FileUtil.toFile(project.getProjectDirectory());
        return pf.getPath() + CASA_DIR_NAME + projName + CASA_EXT;
    }
    
    /**
     * Gets the CASA file object in the given JBI project.
     * 
     * @param project   a JBI project
     * @param create    if <code>true</code> and the CASA file doesn't exist in
     *                  the project, then an empty CASA file will be created
     * 
     * @return  CASA file object
     */
    public static FileObject getCasaFileObject(JbiProject project, boolean create) {
        ProjectInformation projInfo = 
                project.getLookup().lookup(ProjectInformation.class);
        assert projInfo != null;
        
        String projName = projInfo.getName();
        
        FileObject confFO = project.getProjectDirectory().getFileObject(CASA_DIR_NAME);
        if (confFO == null) {
            // This could happen during compapp rename with directory name change.
            return null;
        }
        
        FileObject casaFO = confFO.getFileObject(projName + CASA_EXT);   
        
        if (casaFO == null && create) {
            casaFO = createDefaultCasaFileObject(project);
            updateCasaWithJBIModules(project);
        }
        
        return casaFO;
    }    
        
    /**
     * Creates the default CASA file object in the JBI project.
     * 
     * @param project   a JBI project
     * 
     * @return  the newly created CASA file object
     */
    public static FileObject createDefaultCasaFileObject(JbiProject project) {
        ProjectInformation projInfo = 
                project.getLookup().lookup(ProjectInformation.class);
        assert projInfo != null;        
        String projName = projInfo.getName();        
        FileObject confFO = project.getProjectDirectory().getFileObject(CASA_DIR_NAME); 
        FileObject casaFO = null;
        try {
            FileObject casaTemplateFO =
                    FileUtil.getConfigFile(
                    "org-netbeans-modules-compapp-projects-jbi/project.casa" // NOI18N
                    );
            casaFO = FileUtil.copyFile(casaTemplateFO, confFO, projName);
//            registerCasaFileListener(project);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return casaFO;
    }
    
    public static FileObject getCompAppWSDLFileObject(Project project) {
        ProjectInformation projInfo = 
                project.getLookup().lookup(ProjectInformation.class);
        String projName = projInfo.getName();
        FileObject srcDirFO = ((JbiProject)project).getSourceDirectory();
        return srcDirFO == null ? null : srcDirFO.getFileObject(projName + WSDL_EXT); // NOI18N
    }
        
    /**
     * Checks whether the CASA file contains any non-deleted defined WSDL Port.
     */
    public static boolean containsWSDLPort(JbiProject project) {
        FileObject casaFO = getCasaFileObject(project, false);
        if (casaFO != null) {
            InputStream is = null;
            try {
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                
                is = casaFO.getInputStream();
                Document doc = builder.parse(is);
                
                NodeList endpointNodeList = doc.getElementsByTagName(CASA_ENDPOINT_ELEM_NAME);
                NodeList portNodeList = doc.getElementsByTagName(CASA_PORT_ELEM_NAME); 
                for (int i = 0; i < portNodeList.getLength(); i++) {
                    Element port = (Element) portNodeList.item(i);
                    String state = port.getAttribute(CASA_STATE_ATTR_NAME);
                    if (!state.equals(CASA_DELETED_ATTR_VALUE)) { 
                        Element consumes = (Element) port.getElementsByTagName(CASA_CONSUMES_ELEM_NAME).item(0);
                        String endpointName = consumes.getAttribute(CASA_ENDPOINT_ATTR_NAME);
                        
                        for (int j = 0; j < endpointNodeList.getLength(); j++) {
                            Element endpoint = (Element) endpointNodeList.item(j);
                            if (endpoint.getAttribute(CASA_NAME_ATTR_NAME).equals(endpointName)) {
                                String interfaceName = endpoint.getAttribute(CASA_INTERFACE_NAME_ATTR_NAME);
                                if (interfaceName.endsWith(":" + CASA_DUMMY_PORTTYPE)) { // NOI18N
                                    break;
                                } else {
                                    return true;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error parsing CASA file: " + e); // NOI18N
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception ignore) {
                    }
                }
            }
        }
        return false;
    }
    
//    public static void registerCasaFileListener(JbiProject project) {        
//        FileObject casaFO = CasaHelper.getCasaFileObject(project, false);
//        if (casaFO != null) {
//            FileChangeListener listener =
//                    project.getLookup().lookup(FileChangeListener.class);
//            if (listener != null) {
//                casaFO.removeFileChangeListener(listener);
//                casaFO.addFileChangeListener(listener);
//            }
//        }
//    }
        
    public static void saveCasa(JbiProject project) {
        //System.out.println("CasaHelper.saveCasa()  (" + Thread.currentThread().getName()  + ")");
        
        FileObject casaFO = getCasaFileObject(project, false);
        if (casaFO != null) {
            try {
                DataObject casaDO = DataObject.find(casaFO);
                
                SaveCookie saveCookie = casaDO.getCookie(SaveCookie.class);
                if (saveCookie != null) {
                    try {
                        saveCookie.save();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (DataObjectNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Updates CASA FileObject with service engine service unit info
     * defined in the project properties.
     *
     * @param project   a JBI project
     */
    public static void updateCasaWithJBIModules(JbiProject project) { 
        JbiProjectProperties properties = project.getProjectProperties();
        updateCasaWithJBIModules(project, properties);
    }

    /**
     * Updates CASA FileObject with service engine service unit info
     * defined in the project properties.
     *
     * @param project       a JBI project
     * @param properties    project properties (may not been persisted yet)
     */   
    public static void updateCasaWithJBIModules(JbiProject project, 
            JbiProjectProperties properties) {
         
        FileObject casaFO = CasaHelper.getCasaFileObject(project, true);
        if (casaFO == null) {
            return;
        }
                   
        try {
            boolean modified = false; // whether casa is modified
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document casaDocument = builder.parse(casaFO.getInputStream());

            Element sus = (Element) casaDocument.getElementsByTagName(
                    CasaConstants.CASA_SERVICE_UNITS_ELEM_NAME).item(0);
            NodeList seSUs = sus.getElementsByTagName(
                    CasaConstants.CASA_SERVICE_ENGINE_SERVICE_UNIT_ELEM_NAME);
                        
            @SuppressWarnings("unchecked")
            List<VisualClassPathItem> newContentList = 
                    (List) properties.get(JbiProjectProperties.JBI_CONTENT_ADDITIONAL);
            @SuppressWarnings("unchecked")
            List<String> newTargetIDs = 
                    (List) properties.get(JbiProjectProperties.JBI_CONTENT_COMPONENT);

            List<String> newProjectNameList = new ArrayList<String>();
            for (VisualClassPathItem newContent : newContentList) {
                newProjectNameList.add(newContent.getProjectName());
            }
            
            List<String> sesuNames = new ArrayList<String>();
            for (int i = 0; i < seSUs.getLength(); i++) {
                Element seSU = (Element) seSUs.item(i);
                String unitName = seSU.getAttribute(CasaConstants.CASA_UNIT_NAME_ATTR_NAME);
                sesuNames.add(unitName);
            }

            // Remove deleted service units from casa
            for (int i = 0; i < seSUs.getLength(); i++) {
                Element seSU = (Element) seSUs.item(i);

                // skip external unknown SUs
                String internal = seSU.getAttribute(CasaConstants.CASA_INTERNAL_ATTR_NAME);
                String unknown = seSU.getAttribute(CasaConstants.CASA_UNKNOWN_ATTR_NAME);
                if ("false".equalsIgnoreCase(internal) && "true".equals(unknown)) { // NOI18N
                    continue;
                }

                String projName = seSU.getAttribute(CasaConstants.CASA_UNIT_NAME_ATTR_NAME);
                if (!newProjectNameList.contains(projName)) {
                    sus.removeChild(seSU);
                    modified = true;
                    //System.out.println("removing old su: " + projName);
                }
            }

            // Add new service units to casa
            List<String> externalSuNames = project.getExternalServiceUnitNames();
            for (VisualClassPathItem artifact: newContentList) {
                String projName = artifact.getProjectName();
                String artifactName = artifact.toString();
                
                if (!sesuNames.contains(projName)) {
                    String targetCompID = "unknown"; // NOI18N
                    for (int j = 0; j < newContentList.size(); j++) {
                        if (newContentList.get(j).toString().equals(artifactName)) {
                            targetCompID = newTargetIDs.get(j);
                            break;
                        }
                    }
                    Element seSU = casaDocument.createElement(
                            CasaConstants.CASA_SERVICE_ENGINE_SERVICE_UNIT_ELEM_NAME);
                    seSU.setAttribute(CasaConstants.CASA_X_ATTR_NAME, "-1"); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_Y_ATTR_NAME, "-1"); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_INTERNAL_ATTR_NAME, 
                            externalSuNames.contains(projName) ? "false" : "true"); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_DEFINED_ATTR_NAME, "false"); // NOI18N 
                    seSU.setAttribute(CasaConstants.CASA_UNKNOWN_ATTR_NAME, "false"); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_NAME_ATTR_NAME, projName); // NOI18N  // FIXME
                    seSU.setAttribute(CasaConstants.CASA_UNIT_NAME_ATTR_NAME, projName); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_COMPONENT_NAME_ATTR_NAME, targetCompID); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_DESCRIPTION_ATTR_NAME, "some description"); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_ARTIFACTS_ZIP_ATTR_NAME, artifactName);

                    sus.appendChild(seSU);
                    modified = true;
                    //System.out.println("Adding new su: " + projName);
                }
            }
                        
            if (modified) {
                //System.out.println("CasaHelper: starting writing to CASA (Thread:" + Thread.currentThread().getName() + ")");
                XmlUtil.writeToFileObject(casaFO, casaDocument);
                //System.out.println("CasaHelper: finished writing to CASA (Thread:" + Thread.currentThread().getName() + ")");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the set of WSIT callback handler Java Projects.
     *
     * @param project   the compapp project
     * @return the set of WSIT callback handler Java Projects
     */
    public static Set<String> getWSITCallbackProjects(JbiProject project) {
        return collectCasaPortExtensionAttributes(project,
                WSIT_CALLBACK_NAMESPACE,
                WSIT_CALLBACK_ELEMENT_NAME,
                WSIT_CALLBACK_PROJECT_ATTR_NAME);
    }

    /**
     * Get the set of JAX-WS handler Jar files.
     *
     * @param project   the compapp project
     * @return the set of JAX-WS handler Jar files
     */
    public static Set<String> getJAXWSHandlerJars(JbiProject project) {
        Set<String> jarPaths = collectCasaPortExtensionAttributes(project,
                JAXWS_HANDLER_NAMESPACE,
                JAXWS_HANDLER_ELEMENT_NAME,
                JAXWS_HANDLER_JAR_PATHS_ATTR_NAME);
        
        Set<String> ret = new HashSet<String>();
        for (String jarPath : jarPaths) {
            ret.addAll(Arrays.asList(jarPath.split(";"))); // NOI18N
        }

        return jarPaths;
    }

    /**
     * Get the set of JAX-RS handler Jar files.
     *
     * @param project   the compapp project
     * @return the set of JAX-RS handler Jar files
     */
    public static Set<String> getJAXRSHandlerJars(JbiProject project) {
        Set<String> jarPaths = collectCasaPortExtensionAttributes(project,
                JAXRS_HANDLER_NAMESPACE,
                JAXRS_HANDLER_ELEMENT_NAME,
                JAXRS_HANDLER_JAR_PATHS_ATTR_NAME);

        Set<String> ret = new HashSet<String>();
        for (String jarPath : jarPaths) {
            ret.addAll(Arrays.asList(jarPath.split(";"))); // NOI18N
        }

        return jarPaths;
    }

    /**
     * Get the set of Java Projects
     *
     * @param project               the compapp project
     * @param extensionNamespace
     * @param elementName
     * @param attributeName
     *
     * @return the set of Java Projects
     */
    private static Set<String> collectCasaPortExtensionAttributes(JbiProject project,
            String extensionNamespace,
            String elementName,
            String attributeName) {

        Set<String> ret = new HashSet<String>();

        FileObject casaFO = CasaHelper.getCasaFileObject(project, true);
        if (casaFO == null) {
            return ret;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document casaDocument = builder.parse(casaFO.getInputStream());

            NodeList casaPorts = casaDocument.getElementsByTagName(CASA_PORT_ELEM_NAME);

            for (int i = 0; i < casaPorts.getLength(); i++) {
                Element casaPort = (Element) casaPorts.item(i);

                NodeList extElements = casaPort.getElementsByTagNameNS(
                        extensionNamespace, elementName);
                for (int j = 0; j < extElements.getLength(); j++) {
                    Element extElement = (Element) extElements.item(j);
                    String attributeValue = extElement.getAttribute(attributeName);
                    ret.add(attributeValue);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }
    
    /**
     * Deletes any left-over lock file on CASA and CompApp.wsdl.
     */
    public static void cleanupLocks(Project p) {
        File casaLockFile = getCasaLockFile(p);

        if (casaLockFile != null && casaLockFile.exists()) {
            String msg = NbBundle.getMessage(CasaHelper.class,
                    "CASA_LOCK_EXISTS"); // NOI18N                    
            NotifyDescriptor d =
                    new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);

            if (!casaLockFile.delete()) {
                msg = NbBundle.getMessage(CasaHelper.class,
                        "FAIL_TO_DELETE_FILE", casaLockFile); // NOI18N
                d = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        }

        File compappWSDLLockFile = getCompAppWSDLLockFile(p);

        if (compappWSDLLockFile != null && compappWSDLLockFile.exists()) {
            String msg = NbBundle.getMessage(CasaHelper.class,
                    "COMPAPP_WSDL_LOCK_EXISTS"); // NOI18N       
            NotifyDescriptor d =
                    new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);

            if (!casaLockFile.delete()) {
                msg = NbBundle.getMessage(CasaHelper.class,
                        "FAIL_TO_DELETE_FILE", compappWSDLLockFile); // NOI18N
                d = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        }
    }
        
    private static File getCasaLockFile(Project project) {
        ProjectInformation projInfo = 
                project.getLookup().lookup(ProjectInformation.class);
        assert projInfo != null;
        
        String projName = projInfo.getName();
        
        FileObject confFO = project.getProjectDirectory().getFileObject(CASA_DIR_NAME);
        if (confFO != null) {
            // FileObject doesn't work:
            // confFO.getFileObject(
            //        LOCK_FILE_PREFIX + projName + CASA_EXT + LOCK_FILE_SUFFIX);
            File lockFile = new File(FileUtil.toFile(confFO), 
                    LOCK_FILE_PREFIX + projName + CASA_EXT + LOCK_FILE_SUFFIX);
            return lockFile;
        }
        
        return null;
    }   
    
    private static File getCompAppWSDLLockFile(Project project) {
        ProjectInformation projInfo = 
                project.getLookup().lookup(ProjectInformation.class);
        String projName = projInfo.getName();
        FileObject srcDirFO = ((JbiProject)project).getSourceDirectory();
        if (srcDirFO != null) {
            // FileObject doesn't work:
            // srcDirFO.getFileObject(
            //     LOCK_FILE_PREFIX + projName + WSDL_EXT + LOCK_FILE_SUFFIX); 
            File lockFile = new File(FileUtil.toFile(srcDirFO), 
                    LOCK_FILE_PREFIX + projName + WSDL_EXT + LOCK_FILE_SUFFIX);
            return lockFile;
        }
        
        return null;
    }    
}
