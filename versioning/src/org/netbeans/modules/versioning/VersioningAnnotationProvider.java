/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.modules.versioning;

import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.netbeans.modules.masterfs.providers.AnnotationProvider;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.filesystems.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.Presenter;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.awt.Mnemonics;

import javax.swing.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.File;
import org.openide.util.RequestProcessor;

/**
 * Plugs into IDE filesystem and delegates annotation work to registered versioning systems.
 * 
 * @author Maros Sandor
 */
public class VersioningAnnotationProvider extends AnnotationProvider {
    
    static VersioningAnnotationProvider instance;

    public VersioningAnnotationProvider() {
        instance = this;
    }
    
    private VersioningSystem getOwner(File file) {
        return file == null ? null : VersioningManager.getInstance().getOwner(file);
    }
    
    public Image annotateIcon(Image icon, int iconType, Set files) {
        FileObject fo = (FileObject) files.iterator().next();
        VersioningSystem vs = getOwner(FileUtil.toFile(fo));
        
        if (vs == null) return null;
        VCSAnnotator an = vs.getVCSAnnotator();
        if (an == null) return null;

        VCSContext context = Utils.contextForFileObjects(files);
        return an.annotateIcon(icon, context);
    }

    public String annotateNameHtml(String name, Set files) {
        FileObject fo = (FileObject) files.iterator().next();
        VersioningSystem vs = getOwner(FileUtil.toFile(fo));
        
        if (vs == null) return null;
        VCSAnnotator an = vs.getVCSAnnotator();
        if (an == null) return null;

        VCSContext context = Utils.contextForFileObjects(files);
        return an.annotateName(name, context);
    }

    public Action[] actions(Set files) {
        FileObject fo = (FileObject) files.iterator().next();
        File file = FileUtil.toFile(fo);
        if (file == null) return new Action[0];
        VersioningSystem vs = getOwner(file);
        
        List<Action> actions = new ArrayList<Action>();
        
        VCSAnnotator an = null;
        if (vs != null) {
            an = vs.getVCSAnnotator();
        }
        if (an != null) {
            VersioningSystemActions action = SystemAction.get(VersioningSystemActions.class);
            action.setVersioninSystem(vs);
            actions.add(action);
        }
        
        VersioningSystem localHistory = VersioningManager.getInstance().getLocalHistory(file);
        if(localHistory != null && localHistory.getVCSAnnotator() != null) {
            LocalHistoryActions localHistoryAction = SystemAction.get(LocalHistoryActions.class);
            localHistoryAction.setVersioninSystem(localHistory);          
            actions.add(localHistoryAction);
        } 
        return actions.toArray(new Action [actions.size()]);
    }
    
    public static class VersioningSystemActions extends AbstractVersioningSystemActions {               
    }

    public static class LocalHistoryActions extends AbstractVersioningSystemActions {
    }
    
    public abstract static class AbstractVersioningSystemActions extends SystemAction implements ContextAwareAction {
        
        private VersioningSystem system;

        public String getName() {
            return Utils.getDisplayName(system);
        }

        public HelpCtx getHelpCtx() {
            return new HelpCtx(system.getClass());
        }

        public void actionPerformed(ActionEvent ev) {
            // this item does nothing, this is not a real action
        }

        public Action createContextAwareInstance(Lookup actionContext) {
            return new RealVersioningSystemActions(system, Utils.contextForLookup(actionContext));
        }

        public void setVersioninSystem(VersioningSystem system) {
            this.system = system;
        }
    }
    
    private static class RealVersioningSystemActions extends AbstractAction implements Presenter.Popup {

        private final VersioningSystem system;
        private final VCSContext context;

        public RealVersioningSystemActions(VersioningSystem system, VCSContext context) {
            super(Utils.getDisplayName(system));
            this.system = system;
            this.context = context;
        }

        public void actionPerformed(ActionEvent e) {
            // this item does also nothing, it displays a popup ;)
        }

        public JMenuItem getPopupPresenter() {
            return new VersioningSystemMenuItem();
        }
        
        private class VersioningSystemMenuItem extends JMenu {
        
            private boolean popupContructed;

            public VersioningSystemMenuItem() {
                Mnemonics.setLocalizedText(this, Utils.getDisplayName(system));
            }

            public void setSelected(boolean selected) {
                if (selected && popupContructed == false) {
                    // lazy submenu construction
                    Action [] actions = system.getVCSAnnotator().getActions(context, VCSAnnotator.ActionDestination.PopupMenu);
                    for (int i = 0; i < actions.length; i++) {
                        Action action = actions[i];
                        if (action == null) {
                            add(new JSeparator());    // workaround openide bug
                        } else {
                            JMenuItem item = Utils.toMenuItem(action);
                            add(item);
                        }
                    }
                    popupContructed = true;
                }
                super.setSelected(selected);
            }
        }
    }

    public InterceptionListener getInterceptionListener() {
        return VersioningManager.getInstance().getInterceptionListener();
    }

    public String annotateName(String name, Set files) {
        return name;    // do not support 'plain' annotations
    }

    static void refreshAllAnnotations() {
        if (instance != null) {
            instance.refreshAnnotations(null);
        }
    }
                   
    /**
     * Refreshes annotations for all given files and all parent folders of those files.
     * 
     * @param filesToRefresh files to refresh
     */
    void refreshAnnotations(Set<File> files) {
        if (files == null) {            
            refreshAllAnnotationsTask.schedule(2000);
            return;
        }
        
        for (File file : files) {             
            // collect files parents and store them for the refresh
            for (File parent = file.getParentFile(); parent != null; parent = parent.getParentFile()) {
                FileObject fo;
                // TODO: #73233 diagnostics: remove afterwards 
                try {
                    fo = FileUtil.toFileObject(parent);                        
                } catch (IllegalArgumentException e) {
                    Logger.getLogger(VersioningAnnotationProvider.class.getName()).log(Level.INFO, "Issue #73233 log begins:");
                    Logger.getLogger(VersioningAnnotationProvider.class.getName()).log(Level.INFO, "Original File: " + file.getAbsolutePath());
                    Logger.getLogger(VersioningAnnotationProvider.class.getName()).log(Level.INFO, "Illegal file: " + parent.getAbsolutePath());
                    RuntimeException ex = new RuntimeException("Please report this and append your messages.log file to issue http://www.netbeans.org/issues/show_bug.cgi?id=73233");
                    ex.initCause(e);
                    throw ex;
                }
                addToMap(parentsToRefresh, fo);                
            }   
            
            // store file for the refresh
            FileObject fo = FileUtil.toFileObject(file);            
            addToMap(filesToRefresh, fo);                                                    
        }
        
        fireFileStatusChangedTask.schedule(2000);
    }    
    
    /**
     * Stores all files which have to be refreshed 
     */
    private Map<FileSystem, Set<FileObject>> filesToRefresh = new HashMap<FileSystem, Set<FileObject>>();
    
    /**
     * Stores all parents from files which have to be refreshed 
     */
    private Map<FileSystem, Set<FileObject>> parentsToRefresh = new HashMap<FileSystem, Set<FileObject>>();        
    
    private RequestProcessor rp = new RequestProcessor("Versioning fire FileStatusChanged", 1, true);
    
    /**
     * Refreshes all annotations and clears the maps holding all files and their parents which have to be refreshed
     */
    private RequestProcessor.Task refreshAllAnnotationsTask = rp.create(new Runnable() {        
        public void run() {            
            clearMap(filesToRefresh);
            clearMap(parentsToRefresh);            
            
            Set<FileSystem> filesystems = new HashSet<FileSystem>(1);
            File[] allRoots = File.listRoots();
            for (int i = 0; i < allRoots.length; i++) {
                File root = allRoots[i];
                FileObject fo = FileUtil.toFileObject(root);
                if (fo != null) {
                    try {
                        filesystems.add(fo.getFileSystem());
                    } catch (FileStateInvalidException e) {
                        // ignore invalid filesystems
                    }
                }
            }
            for (Iterator<FileSystem> i = filesystems.iterator(); i.hasNext();) {
                FileSystem fileSystem = i.next();
                fireFileStatusChanged(new FileStatusEvent(fileSystem, true, true));                
            }            
        }
    });    
    
    /**
     * Refreshes all files stored in filesToRefresh and parentsToRefresh
     */ 
    private RequestProcessor.Task fireFileStatusChangedTask = rp.create(new Runnable() {        
        public void run() {
            
            // create and fire for all files which have to be refreshed
            List<FileStatusEvent> fileEvents = new ArrayList<FileStatusEvent>(); 
            List<FileStatusEvent> folderEvents = new ArrayList<FileStatusEvent>(); 

            synchronized(filesToRefresh) {
                Set<FileSystem> fileSystems = filesToRefresh.keySet();                
                if(fileSystems == null || fileSystems.size() == 0) {
                    return;
                }
                for (FileSystem fs : fileSystems) {
                    Set<FileObject> files = new HashSet<FileObject>();
                    Set<FileObject> folders = new HashSet<FileObject>();
                    Set<FileObject> set = filesToRefresh.get(fs);
                    for(FileObject fo : set) {
                        if(fo.isFolder()) {
                            folders.add(fo);
                        } else {
                            files.add(fo);
                        }
                    }        
                    set.clear();
                    if(files.size() > 0) {
                        fileEvents.add(new FileStatusEvent(fs, files, false, true));
                    }
                    if(folders.size() > 0) {
                        folderEvents.add(new FileStatusEvent(fs, folders, true,  true));
                    }
                }        
            }    

            fireFileStatusEvents(fileEvents);
            fireFileStatusEvents(folderEvents);

            // create and fire events for all parent from each file which has to be refreshed
            List<FileStatusEvent> parentEvents = new ArrayList<FileStatusEvent>(); 
            synchronized(parentsToRefresh) {
                Set<FileSystem> fileSystems = parentsToRefresh.keySet();
                for (FileSystem fs : fileSystems) {            
                    Set<FileObject> set = parentsToRefresh.get(fs);
                    Set<FileObject> files = new HashSet<FileObject>(set);
                    parentEvents.add(new FileStatusEvent(fs, files, true, false));                                        
                    set.clear();                    
                }                                
            }       
            fireFileStatusEvents(parentEvents);            
        }    
        
        private void fireFileStatusEvents(Collection<FileStatusEvent> events) {
            for(FileStatusEvent event : events) {
                fireFileStatusChanged(event);
            }
        }          
    });    
    
    private void clearMap(Map<FileSystem, Set<FileObject>> map)  {
        synchronized(map) {
            if(map.size() > 0) {                
                map.clear();
            }
        }                    
    }
    
    private void addToMap(Map<FileSystem, Set<FileObject>> map, FileObject fo) {
        if(fo == null) {
            return;
        }
        FileSystem fs;
        try {
            fs = fo.getFileSystem();
        } catch (FileStateInvalidException e) {
            // ignore files in invalid filesystems
            return;
        }        
        synchronized (map) {                        
            Set<FileObject> set = map.get(fs);
            if(set == null) {
                set = new HashSet<FileObject>();
                map.put(fs, set);
            }
            set.add(fo);
        }
    }               
    
}
