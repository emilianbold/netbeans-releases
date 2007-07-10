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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A helper class to handle CASA related functions.
 * 
 * @author jqian
 */
public class CasaHelper {
    public static String CASA_DIR_NAME = "/src/conf/";  // NOI18N for now..
    public static String CASA_EXT = ".casa";  // NOI18N for now..
    
    public static String getCasaFileName(Project project) {
        ProjectInformation projInfo = 
                project.getLookup().lookup(ProjectInformation.class);    
        String projName = projInfo.getName();
                            
        File pf = FileUtil.toFile(project.getProjectDirectory());
        return pf.getPath() + CASA_DIR_NAME + projName + CASA_EXT;
    }
    
    public static FileObject getCasaFileObject(Project project, boolean create) {
        ProjectInformation projInfo = 
                project.getLookup().lookup(ProjectInformation.class);
        assert projInfo != null;
        
        String projName = projInfo.getName();
        
        FileObject confFO = project.getProjectDirectory().getFileObject(CASA_DIR_NAME); // "src/conf"
        if (confFO == null) {
            // This could happen during compapp rename with directory name change.
            return null;
        }
        
        FileObject casaFO = confFO.getFileObject(projName + CASA_EXT);   
        
        if (casaFO == null && create) {
            casaFO = createDefaultCasaFileObject(project);
        }
        
        return casaFO;
    }    
    
    public static FileObject createDefaultCasaFileObject(Project project) {
        ProjectInformation projInfo = 
                project.getLookup().lookup(ProjectInformation.class);
        assert projInfo != null;        
        String projName = projInfo.getName();        
        FileObject confFO = project.getProjectDirectory().getFileObject(CASA_DIR_NAME); // "src/conf"
        
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
    
    public static boolean containsWSDLPort(Project project) {
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
                        ;
                    }
                }
            }
        }
        return false;
    }
    
    public static void registerCasaFileListener(Project project) {        
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
        
    public static void saveCasa(Project project) {
        FileObject casaFO = getCasaFileObject(project, false);
        if (casaFO != null) {
            try {
                DataObject casaDO = DataObject.find(casaFO);
                
                SaveCookie saveCookie = 
                        (SaveCookie) casaDO.getCookie(SaveCookie.class);
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
}
