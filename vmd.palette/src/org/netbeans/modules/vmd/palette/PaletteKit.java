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

package org.netbeans.modules.vmd.palette;

import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DescriptorRegistry;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.common.DefaultDataFlavor;
import org.netbeans.modules.vmd.api.palette.PaletteProvider;
import org.netbeans.spi.palette.*;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.loaders.DataFolder;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.datatransfer.ExTransferable;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * @author David Kaspar, Anton Chechel
 */
public class PaletteKit implements Runnable {
    
    private DesignDocument activeDocument;
    private PaletteController paletteController;
    private DNDHandler dndHandler;
    
    private Map<String, PaletteItemDataNode> nodesMap;
    private boolean isValidationRunning;
    private LinkedList<Lookup> validationQueue;
    
    private DataFolder rootFolder;
    private FileSystem fs;

    public PaletteKit(final String projectType) {
        this.fs = Repository.getDefault().getDefaultFileSystem();
        
        validationQueue = new LinkedList<Lookup>();
        
        String rootFolderPath = projectType + "/palette"; // NOI18N
        nodesMap = new HashMap<String, PaletteItemDataNode>();
        try {
            FileObject rootFolderFO = fs.findResource(rootFolderPath);
            if (rootFolderFO == null) {
                FileObject projectTypeFO = fs.findResource(projectType);
                if (projectTypeFO == null)
                    projectTypeFO = fs.getRoot().createFolder(projectType);
                rootFolderFO = FileUtil.createFolder(projectTypeFO, "palette"); // NOI18N
            }
            rootFolder = DataFolder.findFolder(rootFolderFO);
            rootFolder.getPrimaryFile().setAttribute("itemWidth", "120"); // NOI18N

            dndHandler = new DNDHandler ();
            paletteController = PaletteFactory.createPalette(rootFolderPath, new Actions(), new Filter(), dndHandler);
        } catch (IOException ex) {
            throw Debug.error(ex);
        }
    }
    
    void update() {
        DesignDocument doc = activeDocument;
        if (doc == null) {
            return;
        }
        final String projectID = doc.getDocumentInterface().getProjectID();
        final String projectType = doc.getDocumentInterface().getProjectType();
        
        try {
            fs.runAtomicAction(new AtomicAction() {
                public void run() {
                    final DescriptorRegistry registry = DescriptorRegistry.getDescriptorRegistry(projectType, projectID);
                    registry.readAccess(new Runnable() {
                        public void run() {
                            updateCore(registry.getComponentProducers(), projectType);
                        }
                    });
                }
            });
        } catch (IOException e) {
            throw Debug.error(e);
        }
    }
    
    public PaletteController getPaletteController() {
        return paletteController;
    }

    public DragAndDropHandler getDndHandler () {
        return dndHandler;
    }

    void refreshPalette() {
        if (paletteController == null) {
            return;
        }
        paletteController.refresh();
    }
    
    private void updateCore(List<ComponentProducer> producers, String projectType) {
        Collection<? extends PaletteProvider> providers = Lookup.getDefault().lookupAll(PaletteProvider.class);
        for (PaletteProvider provider : providers) {
            if (provider != null) {
                provider.initPaletteCategories(projectType, rootFolder);
                initPalette(producers);
            }
        }
    }
    
    private void initPalette(final List<ComponentProducer> producers) {
        FileObject[] children = rootFolder.getPrimaryFile().getChildren();
        Map<String, FileObject> categoryFolders = new HashMap<String, FileObject>(children.length);
        for (FileObject fo : children) {
            categoryFolders.put(fo.getName(), fo);
        }
        
        // create item files
        for (ComponentProducer producer : producers) {
            if (producer.getPaletteDescriptor() == null) {
                continue;
            }
            
            String producerID = producer.getProducerID();
            String catID = producer.getPaletteDescriptor().getCategoryID();
            FileObject catFO;
            if (catID != null) {
                catFO = categoryFolders.get(catID);
            } else {
                catFO = categoryFolders.get("custom"); // NOI18N
                if (catFO == null)
                    continue;
            }
            
            if (catFO == null) { // if category folder was not initialized - create folder
                // only creation is not enough, should be set NB attributes, see MidpPaletteProvider for example
                Debug.warning(catID + " should be initialized! See MidpPaletteProvider."); // NOI18N
                try {
                    catFO = DataFolder.create(rootFolder, catID).getPrimaryFile();
                } catch (IOException ex) {
                    Debug.error("Can't create folder for palette category: " + ex); // NOI18N
                }
            }
            
            StringBuffer path = new StringBuffer();
            path.append(catFO.getPath());
            path.append('/'); // NOI18N
            path.append(producerID);
            path.append('.'); // NOI18N
            path.append(PaletteItemDataLoader.EXTENSION); // NOI18N
            if (fs.findResource(path.toString()) == null) {
                try {
                    FileObject itemFO = catFO.createData(producerID, PaletteItemDataLoader.EXTENSION);
                    
                    Properties props = new Properties();
                    props.setProperty("producerID", producerID); // NOI18N
                    String displayName = producer.getPaletteDescriptor().getDisplayName();
                    props.setProperty("displayName", displayName != null ? displayName : ""); // NOI18N
                    String toolTip = producer.getPaletteDescriptor().getToolTip();
                    props.setProperty("toolTip", toolTip != null ? toolTip : ""); // NOI18N
                    String icon = producer.getPaletteDescriptor().getSmallIcon();
                    props.setProperty("icon", icon != null ? icon : ""); // NOI18N
                    String largeIcon = producer.getPaletteDescriptor().getLargeIcon();
                    props.setProperty("bigIcon", largeIcon != null ? largeIcon : ""); // NOI18N
                    
                    FileLock lock = itemFO.lock();
                    OutputStream os = null;
                    try {
                        os = itemFO.getOutputStream(lock);
                        props.store(os, "VMD Palette Item"); // NOI18N
                    } finally {
                        if (os != null) {
                            try {
                                os.close();
                            } catch (IOException e) {
                                Debug.warning(e.toString());
                            }
                        }
                        lock.releaseLock();
                    }
                } catch (IOException e) {
                    Debug.error("Can't create file for palette item: " + e); // NOI18N
                }
            }
        }
        
        // delete empty categories
        //        for (FileObject catFolder : categoryFolders.values()) {
        //            if (catFolder.getChildren().length == 0) {
        //                FileLock lock = null;
        //                try {
        //                    lock = catFolder.lock();
        //                    catFolder.delete(lock);
        //                } catch (IOException e) {
        //                    Debug.error("Can't delete empty directory for unused palette category: " + e);
        //                } finally {
        //                    lock.releaseLock();
        //                }
        //            }
        //        }
    }
    
    void checkValidity(final Lookup lookup) {
        PaletteItemDataNode node = lookup.lookup(PaletteItemDataNode.class);
        assert node != null;
        
        final String producerID = node.getProducerID();
        if (producerID == null) {
            node.setNeedCheck(false);
            node.setValid(false);
            return;
        }
        
        if (!nodesMap.containsKey(producerID)) {
            nodesMap.put(producerID, node);
        }
        
        node.setNeedCheck(false);
        scheduleCheckValidityCore(lookup);
    }
    
    private void scheduleCheckValidityCore(Lookup lookup) {
        validationQueue.add(lookup);
        synchronized (this) {
            if (isValidationRunning) {
                return;
            }
            isValidationRunning = true;
        }
        RequestProcessor.getDefault().post(this);
    }
    
    public void run() {
        while (true) {
            synchronized (this) {
                if (validationQueue.isEmpty()) {
                    isValidationRunning = false;
                    break;
                }
            }
            checkValidityCore(validationQueue.remove());
        }
        refreshPalette();
    }
    
    private void checkValidityCore(Lookup lookup) {
        PaletteItemDataNode node = lookup.lookup(PaletteItemDataNode.class);
        assert node != null;
        final String producerID = node.getProducerID();
        
        if (activeDocument == null) {
            return;
        }
        final String projectID = activeDocument.getDocumentInterface().getProjectID();
        final String projectType = activeDocument.getDocumentInterface().getProjectType();
        
        // check whether producerID is valid
        final ComponentProducer[] result = new ComponentProducer[1];
        final DescriptorRegistry registry = DescriptorRegistry.getDescriptorRegistry(projectType, projectID);
        registry.readAccess(new Runnable() {
            public void run() {
                List<ComponentProducer> producers = registry.getComponentProducers();
                ComponentProducer producer = null;
                for (ComponentProducer p : producers) {
                    if (p.getProducerID().equals(producerID)) {
                        producer = p;
                        break;
                    }
                }
                result[0] = producer;
            }
        });
        
        boolean isValid = result[0] != null;
        
        // check component's availability in classpath
        if (isValid) {
            isValid = result[0].checkValidity(activeDocument);
        }
        
        node.setValid(isValid);
    }
    
    void clearNodesStateCache() {
        for (PaletteItemDataNode node : nodesMap.values()) {
            node.setNeedCheck(true);
            node.setValid(true);
        }
    }
    
    void setActiveDocument(DesignDocument activeDocument) {
        this.activeDocument = activeDocument;
    }
    
    private class Actions extends PaletteActions {
        public Action[] getImportActions() {
            DesignDocument doc = activeDocument;
            if (doc == null)
                return null;
            final String projectType = doc.getDocumentInterface().getProjectType();
            
            Collection<? extends PaletteProvider> providers = Lookup.getDefault().lookupAll(PaletteProvider.class);
            ArrayList<Action> actions = new ArrayList<Action> ();
            for (PaletteProvider paletteProvider : providers) {
                List<? extends Action> list = paletteProvider.getActions(projectType);
                if (list != null)
                    actions.addAll(list);
            }
            return actions.toArray(new Action[actions.size()]);
        }
        
        public Action[] getCustomPaletteActions() {
            return new Action[0]; // TODO
        }
        
        public Action[] getCustomCategoryActions(Lookup category) {
            return new Action[0]; // TODO
        }
        
        public Action[] getCustomItemActions(Lookup item) {
            return new Action[0]; // TODO
        }
        
        public Action getPreferredAction(Lookup item) {
            return null; // TODO
        }
        
        @Override
        public Action getRefreshAction() {
            return new AbstractAction() {
                public void actionPerformed(ActionEvent evt) {
                    // TODO
                    //ComponentSerializationSupport.refreshDescriptorRegistry()
                    update();
//                    refreshPalette();
                }
            };
        }

        @Override
        public Action getResetAction() {
            return new AbstractAction() {
                public void actionPerformed(ActionEvent evt) {
                    // TODO --||-- + remove customComponents
                    update();
                }
            };
        }
    }
    
    private class Filter extends PaletteFilter {
        public boolean isValidCategory(Lookup lkp) {
            return true;
        }
        
        public boolean isValidItem(Lookup lkp) {
            PaletteItemDataNode node = lkp.lookup(PaletteItemDataNode.class);
            return node != null ? node.isValid() : true;
        }
    }
    
    private class DNDHandler extends DragAndDropHandler {
        public void customize(final ExTransferable t, Lookup item) {
            DesignDocument doc = activeDocument;
            if (doc == null  ||  item == null)
                return;

            String projectID = doc.getDocumentInterface().getProjectID();
            String projectType = doc.getDocumentInterface().getProjectType();
            PaletteItemDataObject itemDataObject = item.lookup (PaletteItemDataObject.class);
            if (itemDataObject == null)
                return;
            final String producerID = itemDataObject.getProducerID();
            
            final DescriptorRegistry registry = DescriptorRegistry.getDescriptorRegistry(projectType, projectID);
            registry.readAccess(new Runnable() {
                public void run() {
                    List<ComponentProducer> producers = registry.getComponentProducers();
                    ComponentProducer producer = null;
                    for (ComponentProducer p : producers) {
                        if (p.getProducerID().equals(producerID)) {
                            producer = p;
                            break;
                        }
                    }
                    if (producer == null) {
                        return;
                    }
                    
                    final ComponentProducer p = producer;
                    DefaultDataFlavor dataFlavor = new DefaultDataFlavor(p);
                    t.put(new ExTransferable.Single(dataFlavor) {
                        protected Object getData() {
                            return p.getProducerID();
                        }
                    });
                }
            });
        }
    }
}
