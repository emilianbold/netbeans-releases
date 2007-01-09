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
import org.netbeans.modules.versioning.util.FlatFolder;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
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
    
    private VCSContext createContext(Set<FileObject> files) {
        Set<File> roots = new HashSet<File>(files.size());
        if (files instanceof NonRecursiveFolder) {
            FileObject folder = ((NonRecursiveFolder) files).getFolder();
            roots.add(new FlatFolder(FileUtil.toFile(folder).getAbsolutePath()));
        } else {
            for (FileObject fo : files) {
                roots.add(FileUtil.toFile(fo));
            }
        }
        return VCSContext.forFiles(roots);
    }

    public Image annotateIcon(Image icon, int iconType, Set files) {
        FileObject fo = (FileObject) files.iterator().next();
        VersioningSystem vs = VersioningManager.getInstance().getOwner(FileUtil.toFile(fo));
        
        if (vs == null) return null;
        VCSAnnotator an = vs.getVCSAnnotator();
        if (an == null) return null;

        VCSContext context = createContext(files);
        return an.annotateIcon(icon, context);
    }

    public String annotateNameHtml(String name, Set files) {
        FileObject fo = (FileObject) files.iterator().next();
        VersioningSystem vs = VersioningManager.getInstance().getOwner(FileUtil.toFile(fo));
        
        if (vs == null) return null;
        VCSAnnotator an = vs.getVCSAnnotator();
        if (an == null) return null;

        VCSContext context = createContext(files);
        return an.annotateName(name, context);
    }

    public Action[] actions(Set files) {
        FileObject fo = (FileObject) files.iterator().next();
        VersioningSystem vs = VersioningManager.getInstance().getOwner(FileUtil.toFile(fo));
        
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
        
        VersioningSystem localHistory = VersioningManager.getInstance().getLocalHistory(FileUtil.toFile(fo));
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
            return system.getDisplayName();
        }

        public HelpCtx getHelpCtx() {
            return new HelpCtx(system.getClass());
        }

        public void actionPerformed(ActionEvent ev) {
            // this item does nothing, this is not a real action
        }

        public Action createContextAwareInstance(Lookup actionContext) {
            return new RealVersioningSystemActions(system, VCSContext.forLookup(actionContext));
        }

        public void setVersioninSystem(VersioningSystem system) {
            this.system = system;
        }
    }
    
    private static class RealVersioningSystemActions extends AbstractAction implements Presenter.Popup {

        private final VersioningSystem system;
        private final VCSContext context;

        public RealVersioningSystemActions(VersioningSystem system, VCSContext context) {
            super(system.getDisplayName());
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
                Mnemonics.setLocalizedText(this, system.getDisplayName());
            }

            public void setSelected(boolean selected) {
                if (selected && popupContructed == false) {
                    // lazy submenu construction
                    Action [] actions = system.getVCSAnnotator().getActions(context, VCSAnnotator.DEST_POPUPMENU);
                    for (int i = 0; i < actions.length; i++) {
                        Action action = actions[i];
                        if (action == null) {
                            add(new JSeparator());    // workaround openide bug
                        } else {
                            JMenuItem item = new JMenuItem(actions[i]);
                            Mnemonics.setLocalizedText(item, item.getText());
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

    public void refreshAllAnnotations(boolean icon, boolean text) {
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
            fireFileStatusChanged(new FileStatusEvent(fileSystem, icon, text));                
        }
    }
    
    /**
     * Refreshes annotations for all given files and all parent folders of those files.
     * 
     * @param filesToRefresh files to refresh
     */
    void refreshAnnotations(Set<File> filesToRefresh) {
        if (filesToRefresh == null) {
            refreshAllAnnotations(true, true);
            return;
        }
        Map<FileSystem, Set<FileObject>> folders = new HashMap<FileSystem, Set<FileObject>>();
        for (File file : filesToRefresh) {
            for (File parent = file.getParentFile(); parent != null; parent = parent.getParentFile()) {
                try {
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
                    if (fo != null) {
                        FileSystem fs = fo.getFileSystem();
                        Set<FileObject> fsFolders = folders.get(fs);
                        if (fsFolders == null) {
                            fsFolders = new HashSet<FileObject>();
                            folders.put(fs, fsFolders);
                        }
                        fsFolders.add(fo);
                    }
                } catch (FileStateInvalidException e) {
                    // ignore files in invalid filesystems
                }
            }
            FileObject fo = FileUtil.toFileObject(file);
            if (fo != null) {
                try {
                    fireFileStatusChanged(new FileStatusEvent(fo.getFileSystem(), fo, fo.isFolder(), true));
                } catch (FileStateInvalidException e) {
                    // ignore files in invalid filesystems
                }
            }
        }
        for (Iterator i = folders.keySet().iterator(); i.hasNext();) {
            FileSystem fs = (FileSystem) i.next();
            Set files = folders.get(fs);
            fireFileStatusChanged(new FileStatusEvent(fs, files, true, false));
        }
    }
}
