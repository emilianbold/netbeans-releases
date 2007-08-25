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
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
//import java.util.HashSet;
//import java.util.Set;
//import org.netbeans.api.project.FileOwnerQuery;
//import org.netbeans.api.project.Project;
//import org.netbeans.api.project.ProjectUtils;
//import org.netbeans.api.project.ui.OpenProjects;
//import org.openide.DialogDisplayer;
//import org.openide.DialogDisplayer;
//import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.XMLFileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

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
            FileObject f = activatedNodes[0].getCookie(DataObject.class).getPrimaryFile();
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
                                        
                                        // Find the resource using the file path
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
            LineCookie lineCookie = layerDataObject.getCookie(LineCookie.class);
            if (lineCookie != null) {
                List<FileObject> lineage = new LinkedList<FileObject>();
                FileObject parent = originalF;
                while (parent != null) {
                    if (parent.getParent() != null) {
                        lineage.add(0, parent);
                    }
                    parent = parent.getParent();
                }
                try {
                    InputSource in = new InputSource(layerDataObject.getPrimaryFile().getURL().toExternalForm());
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    SAXParser parser = factory.newSAXParser();
                    final int[] line = new int[1];
                    //final int[] col = new int[1];
                    final String name = originalF.getNameExt();
                    class Handler extends DefaultHandler {
                        private Locator locator;
                        private Iterator<FileObject> lineageIterator;
                        private FileObject lookingFor;
                        Handler(List<FileObject> lineage) {
                            lineageIterator = lineage.iterator();
                            lookingFor = lineageIterator.next(); 
                        }
                        @Override
                        public void setDocumentLocator(Locator l) {
                            locator = l;
                        }
                        @Override
                        public void startElement(String uri, String localname, String qname, Attributes attr) throws SAXException {
                            if (line[0] == 0) {
                                if (((lookingFor.isFolder() ? "folder".equals(qname) : "file".equals(qname)) &&
                                    lookingFor.getNameExt().equals(attr.getValue("name")))) { // NOI18N
                                    if (lineageIterator.hasNext()) {
                                        lookingFor = lineageIterator.next();
                                    } else {
                                        line[0] = locator.getLineNumber();
                                        // col[0] = locator.getColumnNumber();
                                    }
                                }
                            }
                        }
                    }
                    parser.parse(in, new Handler(lineage));
                    if (line[0] < 1) {
                        return;
                    }
                    lineCookie.getLineSet().getCurrent(line[0] - 1).show(Line.SHOW_GOTO /*, Math.max(0, col[0])*/);
                } catch (Exception e) {
                    return;
                }
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
