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

package org.netbeans.modules.compapp.projects.jbi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.compapp.projects.jbi.descriptor.XmlUtil;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.VisualClassPathItem;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A helper class to handle CASA related functions.
 * 
 * @author jqian
 */
public class CasaHelper {
    public static String CASA_DIR_NAME = "/src/conf/";  // NOI18N 
    public static String CASA_EXT = ".casa";  // NOI18N 
        
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
            casaFO = FileUtil.copyFile(
                    Repository.getDefault().getDefaultFileSystem().findResource(
                    "org-netbeans-modules-compapp-projects-jbi/project.casa" // NOI18N
                    ), confFO, projName
                    );        
            registerCasaFileListener(project);
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
        return srcDirFO == null ? null : srcDirFO.getFileObject(projName + ".wsdl"); // NOI18N
    }
    
    public static boolean containsWSDLPort(JbiProject project) {
        FileObject casaFO = getCasaFileObject(project, false);
        if (casaFO != null) {
            InputStream is = null;
            try {
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                
                is = casaFO.getInputStream();
                Document doc = builder.parse(is);
                NodeList portNodeList = doc.getElementsByTagName("port"); // NOI18N
                for (int i = 0; i < portNodeList.getLength(); i++) {
                    Node portNode = portNodeList.item(i);
                    NamedNodeMap attrMap = portNode.getAttributes();
                    Node stateNode = attrMap.getNamedItem("state"); // NOI18N
                    if (stateNode == null || 
                            ! ("deleted".equals(stateNode.getNodeValue()))) { // NOI18N
                        return true;
                    }
                }
            } catch (Exception e) {
                System.out.println("Error parsing XML: " + e);
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
    
    public static void registerCasaFileListener(JbiProject project) {        
        FileObject casaFO = CasaHelper.getCasaFileObject(project, false);
        if (casaFO != null) {
            FileChangeListener listener =
                    project.getLookup().lookup(FileChangeListener.class);
            if (listener != null) {
                casaFO.removeFileChangeListener(listener);
                casaFO.addFileChangeListener(listener);
            }
        }
    }
        
    public static void saveCasa(JbiProject project) {
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
     *
     * @return      the updated CASA file object
     */
    public static FileObject updateCasaWithJBIModules(JbiProject project) { 
        JbiProjectProperties properties = project.getProjectProperties();
        return updateCasaWithJBIModules(project, properties);
    }

    /**
     * Updates CASA FileObject with service engine service unit info
     * defined in the project properties.
     *
     * @param project       a JBI project
     * @param properties    project properties (may not been persisted yet)
     *
     * @return      the updated CASA file object
     */   
    public static FileObject updateCasaWithJBIModules(JbiProject project, 
            JbiProjectProperties properties) { 
         
        FileObject casaFO = CasaHelper.getCasaFileObject(project, true);
        if (casaFO == null) {
            return null;
        }
           
        File casaFile = FileUtil.toFile(casaFO);
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document casaDocument = builder.parse(casaFile);

            Element sus = (Element) casaDocument.getElementsByTagName(
                    CasaConstants.CASA_SERVICE_UNITS_ELEM_NAME).item(0);
            NodeList seSUs = sus.getElementsByTagName(
                    CasaConstants.CASA_SERVICE_ENGINE_SERVICE_UNIT_ELEM_NAME);
            
            List<VisualClassPathItem> newContentList = 
                    (List<VisualClassPathItem>) properties.get(JbiProjectProperties.JBI_CONTENT_ADDITIONAL);
            List<String> newTargetIDs = 
                    (List<String>) properties.get(JbiProjectProperties.JBI_CONTENT_COMPONENT);

            List<String> newArtifactsList = new ArrayList<String>();
            for (VisualClassPathItem newContent : newContentList) {
                newArtifactsList.add(newContent.toString());
            }

            // Remove deleted service units from casa
            for (int i = 0; i < seSUs.getLength(); i++) {
                Element seSU = (Element) seSUs.item(i);
                String zipName = seSU.getAttribute(CasaConstants.CASA_ARTIFACTS_ZIP_ATTR_NAME);
                if (!newArtifactsList.contains(zipName)) {
                    sus.removeChild(seSU);
                }
            }

            // Add new service units to casa
            for (String artifactName: newArtifactsList) {
                boolean found = false;
                for (int i = 0; i < seSUs.getLength(); i++) {
                    Element seSU = (Element) seSUs.item(i);
                    if (seSU.getAttribute(CasaConstants.CASA_ARTIFACTS_ZIP_ATTR_NAME).
                            equals(artifactName)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    String targetCompID = "unknown"; // NOI18N
                    for (int j = 0; j < newContentList.size(); j++) {
                        if (newContentList.get(j).toString().equals(artifactName)) {
                            targetCompID = newTargetIDs.get(j);
                            break;
                        }
                    }
                    Element seSU = casaDocument.createElement(
                            CasaConstants.CASA_SERVICE_ENGINE_SERVICE_UNIT_ELEM_NAME);
                    String compProjName = artifactName.substring(0, artifactName.length() - 4);
                    seSU.setAttribute(CasaConstants.CASA_X_ATTR_NAME, "-1"); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_Y_ATTR_NAME, "-1"); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_INTERNAL_ATTR_NAME, "true"); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_DEFINED_ATTR_NAME, "false"); // NOI18N 
                    seSU.setAttribute(CasaConstants.CASA_UNKNOWN_ATTR_NAME, "false"); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_NAME_ATTR_NAME, compProjName); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_UNIT_NAME_ATTR_NAME, compProjName); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_COMPONENT_NAME_ATTR_NAME, targetCompID); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_DESCRIPTION_ATTR_NAME, "some description"); // NOI18N
                    seSU.setAttribute(CasaConstants.CASA_ARTIFACTS_ZIP_ATTR_NAME, artifactName);

                    sus.appendChild(seSU);
                }
            }
                        
            XmlUtil.writeToFile(casaFile.getPath(), casaDocument);
            
            casaFO = FileUtil.toFileObject(casaFile);
            casaFO.refresh();
            
            
            System.out.println(FileUtil.getMIMEType(casaFO));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return casaFO;
    }
}
