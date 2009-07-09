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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import java.util.ArrayList;
import org.openide.util.RequestProcessor;

/**
 * Plugs into IDE filesystem and delegates annotation work to registered versioning systems.
 * 
 * @author Maros Sandor
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.masterfs.providers.AnnotationProvider.class)
public class VersioningAnnotationProvider extends AnnotationProvider {
    
    static VersioningAnnotationProvider instance;

    public VersioningAnnotationProvider() {
        instance = this;
    }
    
    private VersioningSystem getOwner(File file) {
        return file == null ? null : VersioningManager.getInstance().getOwner(file);
    }
    
    public Image annotateIcon(Image icon, int iconType, Set<? extends FileObject> files) {
        long ft = System.currentTimeMillis();
        VersioningManager.LOG.log(Level.FINE, "annotateIcon");
        VCSAnnotator an = null;
        long at = 0;
        try {
            if (files.isEmpty()) return icon;
            FileObject fo = (FileObject) files.iterator().next();
            VersioningSystem vs = getOwner(FileUtil.toFile(fo));

            if (vs == null) return null;
            an = vs.getVCSAnnotator();
            if (an == null) return null;

            VCSContext context = Utils.contextForFileObjects(files);
            at = System.currentTimeMillis();
            return an.annotateIcon(icon, context);
        } finally {
            if(VersioningManager.LOG.isLoggable(Level.FINE)) {
                long t = System.currentTimeMillis();
                if(an != null) {
                    VersioningManager.LOG.log(Level.FINE, " " + an.getClass().getName() + " returns in " + (t - at) + " millis");
                }
                VersioningManager.LOG.fine("annotateIcon returns in " + (t - ft) + " millis");
            }
        }
    }

    public String annotateNameHtml(String name, Set<? extends FileObject> files) {
        long ft = System.currentTimeMillis();
        VersioningManager.LOG.log(Level.FINE, "annotateNameHtml");
        VCSAnnotator an = null;
        long at = 0;

        try {
            if (files.isEmpty()) return name;
            FileObject fo = (FileObject) files.iterator().next();
            VersioningSystem vs = getOwner(FileUtil.toFile(fo));

            if (vs == null) return null;
            an = vs.getVCSAnnotator();
            if (an == null) return null;

            at = System.currentTimeMillis();
            VCSContext context = Utils.contextForFileObjects(files);
            return an.annotateName(name, context);
        } finally {
            if(VersioningManager.LOG.isLoggable(Level.FINE)) {
                long t = System.currentTimeMillis();
                if(an != null) {
                    VersioningManager.LOG.log(Level.FINE, " " + an.getClass().getName() + " returns in " + (t - at) + " millis");
                }
                VersioningManager.LOG.fine("annotateNameHtml returns in " + (t - ft) + " millis");
            }
        }
    }

    public Action[] actions(Set files) {
        if (files.isEmpty()) return new Action[0];

        List<Action> actions = new ArrayList<Action>();
        LocalHistoryActions localHistoryAction = null;

        // group all given files by owner
        Map<VersioningSystem, List<File>> owners = new HashMap<VersioningSystem, java.util.List<File>>(3);
        for (FileObject fo : (Set<FileObject>) files) {
            File file = FileUtil.toFile(fo);
            if (file != null) {

                // check if there is at least ine file managed by local hisotry
                VersioningSystem localHistory = VersioningManager.getInstance().getLocalHistory(file);
                if(localHistoryAction == null && localHistory != null && localHistory.getVCSAnnotator() != null) {
                    localHistoryAction = SystemAction.get(LocalHistoryActions.class);
                    localHistoryAction.setVersioninSystem(localHistory);
                    actions.add(localHistoryAction);
                }

                VersioningSystem owner = getOwner(file);
                if(owner != null) {
                    List<File> fileList = owners.get(owner);
                    if(fileList == null) {
                        fileList = new ArrayList<File>();
                    }
                    fileList.add(file);
                    owners.put(owner, fileList);
                }
            }
        }

        VersioningSystem vs = null;
        if(owners.keySet().size() == 1) {
            vs = owners.keySet().iterator().next();
        } else {
            return actions.toArray(new Action [actions.size()]);
        } 
        
        VCSAnnotator an = null;
        if (vs != null) {
            an = vs.getVCSAnnotator();
        }
        if (an != null) {
            VersioningSystemActions action = SystemAction.get(VersioningSystemActions.class);
            action.setVersioninSystem(vs);
            actions.add(action);
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
                            addSeparator();
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
            file = FileUtil.normalizeFile(file);
            // collect files parents and store them for the refresh
            for (File parent = file.getParentFile(); parent != null; parent = parent.getParentFile()) {
                addToMap(parentsToRefresh, FileUtil.toFileObject(parent));
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
            
            FileSystem fileSystem = Utils.getRootFilesystem();
            fireFileStatusChanged(new FileStatusEvent(fileSystem, true, true));
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
                        fileEvents.add(new FileStatusEvent(fs, files, true, true));
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
