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

package org.netbeans.modules.apisupport.project.layers;

import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.Set;
//import org.netbeans.api.project.FileOwnerQuery;
//import org.netbeans.api.project.Project;
//import org.netbeans.api.project.ProjectUtils;
//import org.netbeans.api.project.ui.OpenProjects;
//import org.openide.DialogDisplayer;
//import org.openide.DialogDisplayer;
//import org.openide.NotifyDescriptor;
import org.netbeans.editor.FindSupport;
import org.netbeans.editor.SettingsNames;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.XMLFileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.xml.sax.SAXException;

/**
 * Open the layer file(s) declaring the SFS node.
 * 
 * @author Sandip Chitale
 */
public class OpenLayerFilesAction extends CookieAction {

    // HACK Tunnel through the MultiFileSystem to get to the delegates
    private static Method getDelegatesMethod;
    static {
        try {
            getDelegatesMethod = MultiFileSystem.class.getDeclaredMethod("getDelegates");
            getDelegatesMethod.setAccessible(true);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    protected void performAction(Node[] activatedNodes) {
        if (getDelegatesMethod == null) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        try {
//            Set<Project> projectsSet = new HashSet<Project>();
            FileObject f = activatedNodes[0].getCookie(org.openide.loaders.DataObject.class).getPrimaryFile();
            FileSystem fs = f.getFileSystem();
            while (fs instanceof MultiFileSystem) {
                try {
                    FileSystem[] delegates = (FileSystem[]) getDelegatesMethod.invoke(fs);
                    if (delegates != null && delegates.length > 0) {
                        fs = delegates[0];
                        if (fs instanceof MultiFileSystem) {
                            // keep going
                            continue;
                        } else if (fs instanceof WritableXMLFileSystem) {
                            // try current module project's layer file.
                            FileObject originalF = fs.findResource(f.getPath());
                            if (originalF != null) {
                                URL url = (URL) f.getAttribute("WritableXMLFileSystem.location"); // NOI18N
                                FileObject layerFileObject = URLMapper.findFileObject(url);
                                if (layerFileObject != null) {
                                    try {
                                        DataObject layerDataObject = DataObject.find(layerFileObject);
                                        openLayerFileAndFind(layerDataObject, originalF);
//                                        Project project = FileOwnerQuery.getOwner(layerFileObject);
//                                        if (project != null) {
//                                            projectsSet.add(project);
//                                        }
                                    } catch (DataObjectNotFoundException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                }
                            }
                            // Now, try other layer files visible to the module project
                            for (int i = 1; i < delegates.length; i++) {
                                XMLFileSystem xMLFileSystem = (XMLFileSystem) delegates[i];
                                
                                // Have to use deprecated API to get all the Xml URLs
                                URL[] urls = xMLFileSystem.getXmlUrls();
                                for (URL url : urls) {
                                    try {
                                        // Build an XML FS for the given URL
                                        XMLFileSystem aXMLFileSystem = new XMLFileSystem(url);
                                        
                                        // Find the resource usiung the file path
                                        originalF = aXMLFileSystem.findResource(f.getPath());
                                        
                                        // Found?
                                        if (originalF != null) {
                                            // locate the layer's file object and open it
                                            FileObject layerFileObject = URLMapper.findFileObject(url);
                                            if (layerFileObject != null) {
                                                try {
                                                    DataObject layerDataObject = DataObject.find(layerFileObject);
                                                    openLayerFileAndFind(layerDataObject, originalF);
                                                    
//                                                    Project project = FileOwnerQuery.getOwner(layerFileObject);
//                                                    if (project != null) {
//                                                        projectsSet.add(project);
//                                                    }
                                                } catch (DataObjectNotFoundException ex) {
                                                    Exceptions.printStackTrace(ex);
                                                }
                                            }
                                        }
                                    }catch (SAXException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                }
                            }
                        }
                    }

//                    projectsSet.removeAll(Arrays.asList(OpenProjects.getDefault().getOpenProjects()));
//                    if (projectsSet.size() > 0) {
//                        Set<String> projectNames = new HashSet<String>(projectsSet.size());
//                        for(Project project:projectsSet) {
//                            projectNames.add(ProjectUtils.getInformation(project).getDisplayName());
//                        }
//                        if (DialogDisplayer.getDefault().notify(
//                                new NotifyDescriptor.Confirmation(
//                                    projectNames.toArray(new String[0]),
//                                    NbBundle.getMessage(OpenLayerFilesAction.class, "MSG_open_layer_projects"),
//                                    NotifyDescriptor.YES_NO_OPTION,
//                                    NotifyDescriptor.QUESTION_MESSAGE)) == NotifyDescriptor.YES_OPTION) {
//                            OpenProjects.getDefault().open(projectsSet.toArray(new Project[0]), false);
//                        } 
//                    }
                }catch (IllegalAccessException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IllegalArgumentException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private static void openLayerFileAndFind(DataObject layerDataObject, FileObject originalF) {
        EditorCookie editorCookie = layerDataObject.getCookie(EditorCookie.class);
        if (editorCookie != null) {
            editorCookie.open();
            FindSupport findSupport = FindSupport.getFindSupport();
            Map findProps = new HashMap();
            findProps.put(SettingsNames.FIND_HIGHLIGHT_SEARCH, Boolean.FALSE);
            findProps.put(SettingsNames.FIND_REG_EXP, Boolean.TRUE);
            findProps.put(SettingsNames.FIND_WHOLE_WORDS, Boolean.FALSE);
            findProps.put(SettingsNames.FIND_MATCH_CASE, Boolean.FALSE);
            findProps.put(SettingsNames.FIND_WRAP_SEARCH, Boolean.TRUE);
            findProps.put(SettingsNames.FIND_INC_SEARCH, Boolean.FALSE);
            findProps.put(SettingsNames.FIND_BACKWARD_SEARCH, Boolean.FALSE);
            findProps.put(SettingsNames.FIND_WHAT,
                "<" + ".*" + (originalF.isFolder() ? "folder" : "file") + ".*" + "name=\"" +  // NOI18N
                Pattern.quote(originalF.getNameExt()) // quote the pattern
                + "\""); // NOI18N
            if (findSupport.find(findProps, false)) {
                findProps.put(SettingsNames.FIND_REG_EXP, Boolean.FALSE);
                findProps.put(SettingsNames.FIND_WRAP_SEARCH, Boolean.FALSE);
                findProps.put(SettingsNames.FIND_WHAT, originalF.getNameExt());
                findSupport.find(findProps, true);
            }
        }
    }

    public String getName() {
         return NbBundle.getMessage(OpenLayerFilesAction.class, "LBL_open_layer_files_action");
    }
    
    protected Class[] cookieClasses() {
        return new Class[] {DataObject.class};
    }
    
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }

}