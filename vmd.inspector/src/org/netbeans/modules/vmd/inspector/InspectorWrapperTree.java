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

package org.netbeans.modules.vmd.inspector;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.Action;

import org.netbeans.modules.vmd.api.inspector.InspectorFolder;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderPath;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderPresenter;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingController;
import org.netbeans.modules.vmd.api.inspector.InspectorRegistry;
import org.netbeans.modules.vmd.api.io.ActiveViewSupport;
import org.netbeans.modules.vmd.api.io.DataEditorView;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.DesignDocumentAwareness;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.openide.util.WeakSet;
import org.openide.util.datatransfer.NewType;

/**
 *
 * @author Karol Harezlak
 */
public final class InspectorWrapperTree implements FolderRegistry.Listener, ActiveViewSupport.Listener, DesignDocumentAwareness {
    
    private Collection<InspectorFolder> globalFolders;
    private FolderRegistry registry;
    private DesignDocument document;
    private InspectorFolderWrapper rootFolderWrapper;
    private Set<DesignComponent> componentsToDelete;
    private Set<DesignComponent> componentsToAdd;
    //TODO remove private volatile boolean lock = false;
    private Collection<InspectorFolderWrapper> foldersToUpdate;
    private WeakHashMap<DesignDocument, InspectorFolderWrapper> rootFolderWrapperMap = new WeakHashMap<DesignDocument, InspectorFolderWrapper>();
    private WeakSet<DataObjectContext> contexts = new WeakSet<DataObjectContext>();
    
    InspectorWrapperTree() {
        ActiveViewSupport.getDefault().addActiveViewListener(this);
        foldersToUpdate = new HashSet<InspectorFolderWrapper>();
        componentsToAdd = new HashSet<DesignComponent>();
        componentsToDelete = new HashSet<DesignComponent>();
    }
    
    synchronized void buildTree(final Collection<DesignComponent> createdComponents,final Collection<DesignComponent> affectedComponents) {
        foldersToUpdate.clear();
        updateChangedDescriptors(createdComponents, affectedComponents);
//        System.out.println("Start");
        long start = System.currentTimeMillis();
        dive(InspectorFolderPath.createInspectorPath().add(rootFolderWrapper.getFolder()), rootFolderWrapper);
        updateViewChildren(rootFolderWrapper);
        long stop = System.currentTimeMillis();
//        System.out.println("Time to build and refresh navigator tree "+ ((stop-start) * 0.001)+" s"); //NOI18N //Remove
        //DebugInspector.printFoldersTree(rootFolder);
    }
    
    private void dive(InspectorFolderPath path, InspectorFolderWrapper parentWrapper) {
        List<InspectorFolderWrapper> wrapperChildren = null;
        
        if (parentWrapper.getFolder() instanceof RootFolder)
            wrapperChildren = componentsRecursion(path , parentWrapper, document.getRootComponent());
        else
            wrapperChildren = componentsChildrenDive(path, parentWrapper, document.getRootComponent());
        
        if (wrapperChildren == null)
            wrapperChildren = registryDescriptorsDive(path, parentWrapper);
        else {
            List<InspectorFolderWrapper> registryWrapperChildren = registryDescriptorsDive(path, parentWrapper);
            if (registryWrapperChildren != null)
                wrapperChildren.addAll(registryWrapperChildren);
        }
        if (wrapperChildren == null)
            wrapperChildren = parentWrapper.getChildren();
        else {
            List<InspectorFolderWrapper> parentWrapperChildren = parentWrapper.getChildren();
            if (parentWrapperChildren != null)
                wrapperChildren.addAll(parentWrapperChildren);
        }
        if (wrapperChildren != null) {
            WeakSet<InspectorFolderWrapper> wrappersToDelete = null;
            for (InspectorFolderWrapper folder : wrapperChildren) {
                if (componentsToDelete != null && folder.getFolder().getComponentID() != null) {
                    for (DesignComponent component : componentsToDelete) {
                        if (component.getComponentID() == folder.getFolder().getComponentID()) {
                            if (wrappersToDelete == null)
                                wrappersToDelete = new WeakSet<InspectorFolderWrapper>();
                            wrappersToDelete.add(folder);
                            foldersToUpdate.add(parentWrapper);
                        }
                    }
                }
                path.add(folder.getFolder());
                dive(path, folder);
                path.remove(folder.getFolder());
            }
            if (wrappersToDelete != null) {
                wrapperChildren.removeAll(wrappersToDelete);
                for (InspectorFolderWrapper wrapperToDelete : wrappersToDelete) {
                    wrapperToDelete.terminate();
                }
            }
            parentWrapper.setChildren(wrapperChildren);
        }
    }
    
    private List<InspectorFolderWrapper> componentsChildrenDive(InspectorFolderPath path, InspectorFolderWrapper parentWrapper, DesignComponent parentComponent) {
        List<InspectorFolderWrapper> wrapperChildren = null;
        
        for (DesignComponent component : parentComponent.getComponents()) {
            if ((! componentsToAdd.isEmpty()) &&  componentsToAdd.contains(component)) {
                List<InspectorFolderWrapper> tempWrapperChildren = componentsRecursion(path, parentWrapper, component);
                if (wrapperChildren == null)
                    wrapperChildren = tempWrapperChildren;
                else if ( tempWrapperChildren != null)
                    wrapperChildren.addAll(tempWrapperChildren);
            }
            if ((! InspectorRegistry.getRegistry().isEmpty()) && InspectorRegistry.getRegistry().contains(component)) {
                if (wrapperChildren == null)
                    wrapperChildren = changedComponentsRecursion(path, parentWrapper, component);
                else {
                    List<InspectorFolderWrapper> changedComponentsRecursionChildren = changedComponentsRecursion(path, parentWrapper, component);
                    if (changedComponentsRecursionChildren != null)
                        wrapperChildren.addAll(changedComponentsRecursionChildren);
                }
            }
            if (wrapperChildren == null)
                wrapperChildren = componentsChildrenDive(path, parentWrapper, component);
            else {
                List<InspectorFolderWrapper> componentsChildren = componentsChildrenDive(path, parentWrapper, component);
                if (componentsChildren != null)
                    wrapperChildren.addAll(componentsChildren);
            }
        }
        
        return wrapperChildren;
    }
    
    private List<InspectorFolderWrapper> componentsRecursion(InspectorFolderPath path, InspectorFolderWrapper parentWrapper, DesignComponent component){
        List<InspectorFolderWrapper> wrapperChildren = null;
        
        for (DesignComponent componentToAdd : componentsToAdd) {
            if (componentToAdd.getComponentID() != component.getComponentID() )
                continue;
            for (InspectorFolderPresenter presenter : componentToAdd.getPresenters(InspectorFolderPresenter.class)) {
                if (presenter != null &&  presenter.getFolder().isInside(path, presenter.getFolder(), componentToAdd)) {
                    if (presenter.getFolder().getComponentID() != null && ( parentWrapper.getChildrenFolders() == null || (!parentWrapper.getChildrenFolders().contains(presenter.getFolder())))) {
                        if (wrapperChildren == null)
                            wrapperChildren = new ArrayList<InspectorFolderWrapper>();
                        InspectorFolderWrapper wrapper = new InspectorFolderWrapper(presenter.getFolder());
                        wrapperChildren.add(wrapper);
                        foldersToUpdate.add(parentWrapper);
                    }
                }
            }
        }
        
        return wrapperChildren;
    }
    
    private List<InspectorFolderWrapper> changedComponentsRecursion(InspectorFolderPath path, InspectorFolderWrapper parentWrapper, DesignComponent component){
        List<InspectorFolderWrapper> wrapperChildren = null;
        
        for (InspectorFolderPresenter presenter : component.getPresenters(InspectorFolderPresenter.class)) {
            if (presenter != null &&  presenter.getFolder().isInside(path, presenter.getFolder(), component)) {
                if (presenter.getFolder().getComponentID() != null) {
                    if (! parentWrapper.removeChild(presenter.getFolder()))
                        continue;
                    if (wrapperChildren == null)
                        wrapperChildren = wrapperChildren = new ArrayList<InspectorFolderWrapper>();
                    InspectorFolderWrapper wrapper = new InspectorFolderWrapper(presenter.getFolder());
                    wrapperChildren.add(wrapper);
                    path.add(wrapper.getFolder());
                    rebulidDive(path, wrapper);
                    path.remove(wrapper.getFolder());
                    foldersToUpdate.add(parentWrapper);
                } else
                    throw new IllegalArgumentException("Argument ComponentID is null: component: " + component); //NOI18N
            }
        }
        
        return wrapperChildren;
    }
    
    private List<InspectorFolderWrapper> registryDescriptorsDive(InspectorFolderPath path, InspectorFolderWrapper parentWrapper) {
        List<InspectorFolderWrapper> wrapperChildren = null;
        
        for (InspectorFolder folder : globalFolders ){
            if (folder.isInside(path, folder, null)){
                if (parentWrapper.getChildrenFolders() != null && parentWrapper.getChildrenFolders().contains(folder))
                    continue;
                if (wrapperChildren == null)
                    wrapperChildren = wrapperChildren = new ArrayList<InspectorFolderWrapper>();
                InspectorFolderWrapper wrapper = new InspectorFolderWrapper(folder);
                wrapperChildren.add(wrapper);
            }
        }
        
        return wrapperChildren;
    }
    
    //------------------Rebulid algorithm
    
    private void rebulidDive(InspectorFolderPath path, InspectorFolderWrapper parentWrapper) {
        List<InspectorFolderWrapper> wrapperChildren = null;
        
        if (parentWrapper.getFolder() instanceof RootFolder)
            wrapperChildren = rebulidComponentsRecursion(path , parentWrapper, document.getRootComponent());
        else
            wrapperChildren = rebulidComponentsChildrenDive(path, parentWrapper, document.getRootComponent());
        
        if (wrapperChildren == null)
            wrapperChildren = registryDescriptorsDive(path, parentWrapper);
        else {
            List<InspectorFolderWrapper> registryChildren = registryDescriptorsDive(path, parentWrapper);
            if (registryChildren != null)
                wrapperChildren.addAll(registryChildren);
        }
        if (wrapperChildren == null)
            wrapperChildren = parentWrapper.getChildren();
        else {
            List<InspectorFolderWrapper> parentChildren = parentWrapper.getChildren();
            if (parentChildren != null)
                wrapperChildren.addAll(parentChildren);
        }
        if (wrapperChildren != null) {
            for (InspectorFolderWrapper folder : wrapperChildren) {
                path.add(folder.getFolder());
                rebulidDive(path, folder);
                path.remove(folder.getFolder());
            }
            parentWrapper.setChildren(wrapperChildren);
        }
    }
    
    private List<InspectorFolderWrapper> rebulidComponentsChildrenDive(InspectorFolderPath path, InspectorFolderWrapper parentWrapper, DesignComponent parentComponent) {
        List<InspectorFolderWrapper> wrapperChildren = null;
        
        for (DesignComponent component : parentComponent.getComponents()) {
            List<InspectorFolderWrapper> tempWrapperChildren = rebulidComponentsRecursion(path, parentWrapper, component);
            
            if (wrapperChildren == null)
                wrapperChildren = tempWrapperChildren;
            else if ( tempWrapperChildren != null)
                wrapperChildren.addAll(tempWrapperChildren);
            
            if (wrapperChildren == null)
                wrapperChildren = rebulidComponentsChildrenDive(path, parentWrapper, component);
            else {
                List<InspectorFolderWrapper> componentsChildren = rebulidComponentsChildrenDive(path, parentWrapper, component);
                if (componentsChildren != null)
                    wrapperChildren.addAll(componentsChildren);
            }
        }
        
        return wrapperChildren;
    }
    
    private List<InspectorFolderWrapper> rebulidComponentsRecursion(InspectorFolderPath path, InspectorFolderWrapper parentWrapper, DesignComponent component){
        List<InspectorFolderWrapper> childrenWrapper = null;
        for (InspectorFolderPresenter presenter : component.getPresenters(InspectorFolderPresenter.class)) {
            if (presenter != null &&  presenter.getFolder().isInside(path, presenter.getFolder(), component)) {
                if (presenter.getFolder().getComponentID() != null && ( parentWrapper.getChildrenFolders() == null || (!parentWrapper.getChildrenFolders().contains(presenter.getFolder())))) {
                    if (childrenWrapper == null)
                        childrenWrapper = childrenWrapper = new ArrayList<InspectorFolderWrapper>();
                    InspectorFolderWrapper wrapper = new InspectorFolderWrapper(presenter.getFolder());
                    childrenWrapper.add(wrapper);
                    foldersToUpdate.add(parentWrapper);
                }
            }
        }
        
        return childrenWrapper;
    }
    
    
    // ---------- Existing tree update view
    void updateChangedDescriptors(final Collection<DesignComponent> createdComponents , final Collection<DesignComponent> affectedComponents) {
        componentsToAdd.clear();
        componentsToDelete.clear();
        
        if (createdComponents != null ) {
            for (DesignComponent component : createdComponents){
                for (InspectorFolderPresenter presenter : component.getPresenters(InspectorFolderPresenter.class)) {
                    componentsToAdd.add(component);
                }
            }
        }
        if (affectedComponents != null ) {
            for (DesignComponent component : affectedComponents){
                if (component == null || component.getParentComponent() != null)
                    continue;
                componentsToDelete.add(component);
            }
        }
        
        InspectorRegistry.removeAll(componentsToAdd);
        InspectorRegistry.removeAll(componentsToDelete);
    }
    
    private void updateViewChildren(InspectorFolderWrapper parentWrapper) {
        if (parentWrapper.getChildren() != null) {
            for(InspectorFolderWrapper wrapper : parentWrapper.getChildren() ) {
                updateViewChildren(wrapper);
                if (foldersToUpdate.contains(parentWrapper))
                    wrapper.resolveFolder(document);
            }
        }
        parentWrapper.resolveFolder(document);
    }
    
    
    private void  updateRegistredFolders() {
        if (registry == null)
            return;
        registry.readAccess(new Runnable() {
            public void run() {
                globalFolders = registry.getFolders();
            }
        });
    }
    
    InspectorFolderWrapper getRootWrapperFolder() {
        return rootFolderWrapper;
    }
    
    public void notifyRegistryContentChange() {
        updateRegistredFolders();
    }
    
    public void activeViewChanged(DataEditorView deactivatedView, DataEditorView activatedView) {
        if (registry !=null)
            registry.removeListener(this);
        if (activatedView != null) {
            DataObjectContext localContext = activatedView.getContext();
            if (localContext != null && (!contexts.contains(localContext))) {
                contexts.add(localContext);
                localContext.addDesignDocumentAwareness(this);
            }
            registry = FolderRegistry.getRegistry(activatedView.getContext().getProjectType(),activatedView.getContext().getProjectID());
            registry.addListener(this);
            updateRegistredFolders();
        }
    }
    
    void setCurrentDesignDocument(DesignDocument document) {
        this.document = document;
        if (document == null)
            return;
        rootFolderWrapper = null;
        for (DesignDocument doc : rootFolderWrapperMap.keySet()) {
            if (document == doc){
                rootFolderWrapper = rootFolderWrapperMap.get(doc);
                return;
            }
        }
        if (rootFolderWrapper == null){
            rootFolderWrapper = new InspectorFolderWrapper(new RootFolder());
            buildTree(markAllComponentsAsToAdd(), null);
            rootFolderWrapperMap.put(document, rootFolderWrapper);
        }
    }
    
    Collection<InspectorFolderWrapper> getFolderToUpdate(){
        return foldersToUpdate;
    }
    
    private Collection<DesignComponent> markAllComponentsAsToAdd(){
        final Collection<DesignComponent> componentsToAdd = new HashSet<DesignComponent>();
        
        componentsToAdd.add(document.getRootComponent());
        componentsToAdd.addAll(markAllComponentsAsToAdd(document.getRootComponent()));
        
        return componentsToAdd;
    }
    
    private Collection<DesignComponent> markAllComponentsAsToAdd(DesignComponent parentComponent){
        Collection<DesignComponent> componentsToAdd = new HashSet<DesignComponent>();
        
        for (DesignComponent component : parentComponent.getComponents()){
            componentsToAdd.addAll(markAllComponentsAsToAdd(component));
            componentsToAdd.add(component);
        }
        
        return componentsToAdd;
    }
    
    public void setDesignDocument(DesignDocument localDocument) {
        if (localDocument == null && rootFolderWrapperMap.get(this.document) != null) {
            rootFolderWrapperMap.get(this.document).terminate();
            rootFolderWrapperMap.remove(this.document);
            document = null;
       }
    }
    
    
    private class RootFolder implements InspectorFolder {
        
        public TypeID getTypeID() {
            return new TypeID(TypeID.Kind.PRIMITIVE, ""); //NOI18N
        }
        
        public Long getComponentID() {
            return null;
        }
        
        public Image getIcon() {
            return new Image() {
                public void flush() {
                }
                public Graphics getGraphics() {
                    return null;
                }
                public int getHeight(ImageObserver observer) {
                    return 0;
                }
                public Object getProperty(String name, ImageObserver observer) {
                    return null;
                }
                public ImageProducer getSource() {
                    return null;
                }
                public int getWidth(ImageObserver observer) {
                    return 0;
                }
            };
        }
        
        public String getDisplayName() {
            return "ROOT FOLDER - SHOULD BE HIDDEN"; //NOI18N
        }
        
        public Action[] getActions() {
            return null;
        }
        
        public NewType[] getNewTypes() {
            return null;
        }
        
        public boolean canRename() {
            return false;
        }
        
        public boolean isInside(InspectorFolderPath path, InspectorFolder folder, DesignComponent component) {
            return false;
        }
        
        public String getName() {
            return "ROOT FOLDER - SHOULD BE HIDDEN"; //NOI18N
        }
        
        public InspectorOrderingController[] getOrderingControllers() {
            return null;
        }
        
    }
    
}
