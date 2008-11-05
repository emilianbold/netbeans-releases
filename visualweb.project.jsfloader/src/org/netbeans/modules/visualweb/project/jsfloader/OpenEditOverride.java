/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.visualweb.project.jsfloader;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.WeakHashMap;
import javax.swing.Action;
import javax.swing.JComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Workaround for http://www.netbeans.org/issues/show_bug.cgi?id=58136.  Saves the
 * currently selected tab for JSF multiviews that are still opened when a project is closed.
 * 
 * @author quynguyen
 */
@LookupProvider.Register(projectType="org-netbeans-modules-web-project")
public final class OpenEditOverride  implements LookupProvider {
    // Taken from org.netbeans.modules.project.ui.ProjectUtilities
    private static final String OPEN_FILES_NS = "http://www.netbeans.org/ns/projectui-open-files/1"; // NOI18N
    private static final String OPEN_FILES_ELEMENT = "open-files"; // NOI18N
    private static final String FILE_ELEMENT = "file"; // NOI18N
    
    static final String MULTIVIEW_ATTRIBUTE = "selected-multiview";
    
    private static WeakHashMap<Project, HashMap<FileObject,String>> multiViewsByProject = new WeakHashMap<Project, HashMap<FileObject,String>>();
    
    public Lookup createAdditionalLookup(Lookup baseContext) {
        Project proj = baseContext.lookup(Project.class);
        return Lookups.singleton(new ProjectOpenedHookImpl(proj));
    }
    
    private static void multiViewChanged(Project fromProject, DataObject multiViewDO, String multiViewId) {
        HashMap<FileObject,String> projectMultiViews = multiViewsByProject.get(fromProject);
        
        if (projectMultiViews == null) {
            projectMultiViews = new HashMap<FileObject,String>();
            multiViewsByProject.put(fromProject, projectMultiViews);
        }
        projectMultiViews.put(multiViewDO.getPrimaryFile(), multiViewId);
    }
    
    private static void unregisterProject(Project proj) {
        multiViewsByProject.remove(proj);
    }
    
    private static final class ProjectOpenedHookImpl extends ProjectOpenedHook {
        private WeakReference<Project> projectRef;
        
        public ProjectOpenedHookImpl(Project project) {
            this.projectRef = new WeakReference<Project>(project);
        }
        
        @Override
        protected void projectOpened() {
            // NO-OP
        }

        @Override
        protected void projectClosed() {
            Project project = projectRef.get();
            if (project != null) {
                AuxiliaryConfiguration aux = ProjectUtils.getAuxiliaryConfiguration(project);
                HashMap<FileObject,String> multiviews = multiViewsByProject.get(project);
                multiviews = (multiviews == null) ? new HashMap<FileObject,String>(1) : multiviews;
                
                Element openFiles = aux.getConfigurationFragment(OPEN_FILES_ELEMENT, OPEN_FILES_NS, false);

                if (openFiles == null)
                    return;

                NodeList fileNodeList = openFiles.getElementsByTagName(FILE_ELEMENT);
                for (int i = 0; i < fileNodeList.getLength(); i++) {
                    String url = fileNodeList.item(i).getChildNodes().item(0).getNodeValue();
                    FileObject fo;
                    try {
                        fo = URLMapper.findFileObject(new URL(url));
                    } catch (MalformedURLException mue) {
                        assert false : "MalformedURLException in " + url;
                        continue;
                    }
                    if (fo == null) {
                        continue;
                    }

                    if (FileOwnerQuery.getOwner(fo) != project) {
                        continue;
                    }

                    try {
                        DataObject dobj = DataObject.find(fo);
                        if (dobj instanceof JsfJspDataObject) {
                            FileObject primaryFile = dobj.getPrimaryFile();
                            String mvId = multiviews.get(primaryFile);

                            primaryFile.setAttribute(MULTIVIEW_ATTRIBUTE, mvId);
                        }
                    }catch (IOException ex) {
                        assert false : "IOException for FileObject: " + fo.getPath();
                        continue;
                    }
                }
                
                unregisterProject(project);
            }
        }
    }
            
    static final class MultiViewDelegate implements MultiViewElement {
        private final MultiViewElement originalElement;
        private final String mvIdentifier;
        private final DataObject dobj;
        private WeakReference<Project> associatedProject;
        
        public MultiViewDelegate(MultiViewElement originalElement, String mvIdentifier, DataObject dobj) {
            this.originalElement = originalElement;
            this.mvIdentifier = mvIdentifier;
            this.dobj = dobj;
            
            associatedProject = new WeakReference<Project>(FileOwnerQuery.getOwner(dobj.getPrimaryFile()));
        }

        public MultiViewElement getOriginal() {
            return originalElement;
        }
        
        public void componentShowing() {
            Project projectRef = associatedProject.get();
            if (projectRef != null) {
                multiViewChanged(projectRef, dobj, mvIdentifier);
            }
            
            originalElement.componentShowing();
        }
        
        //// MultiViewElement implementation
        public JComponent getVisualRepresentation() {
            return originalElement.getVisualRepresentation();
        }

        public JComponent getToolbarRepresentation() {
            return originalElement.getToolbarRepresentation();
        }

        public Action[] getActions() {
            return originalElement.getActions();
        }

        public Lookup getLookup() {
            return originalElement.getLookup();
        }

        public void componentOpened() {
            originalElement.componentOpened();
        }

        public void componentClosed() {
            originalElement.componentClosed();
        }

        public void componentHidden() {
            originalElement.componentHidden();
        }

        public void componentActivated() {
            originalElement.componentActivated();
        }

        public void componentDeactivated() {
            originalElement.componentDeactivated();
        }

        public UndoRedo getUndoRedo() {
            return originalElement.getUndoRedo();
        }

        public void setMultiViewCallback(MultiViewElementCallback callback) {
            originalElement.setMultiViewCallback(callback);
        }

        public CloseOperationState canCloseElement() {
            return originalElement.canCloseElement();
        }
        
    }
}
     