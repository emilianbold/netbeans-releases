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

package org.netbeans.modules.ant.freeform.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JSeparator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;


/**A wrapper node for project's root node that adds CVS badges+Project/Actions.
 * 
 * This class should be moved into projectuiapi (org.netbeans.spi.project.ui.support)
 * after some cleanup and adding tests.
 *
 * The intent is to make it package private and create a factory which will wrap
 * provided node (project's logical view) with this wrapper to serve the CVS annotations.
 *
 * @author Jan Lahoda
 */
public final class ProjectNodeWrapper extends FilterNode implements Runnable, FileStatusListener, ChangeListener, PropertyChangeListener {
    
    private Set<FileObject> files;
    private Map<FileSystem,FileStatusListener> fileSystemListeners;
    private RequestProcessor.Task task;
    private final Object privateLock = new Object();
    private boolean iconChange;
    private boolean nameChange;
    private ChangeListener sourcesListener;
    private Map<SourceGroup,PropertyChangeListener> groupsListeners;
    
    public static final Action GENERIC_PROJECTS_ACTIONS_MARKER = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
        }
    };
    
    public ProjectNodeWrapper(Node toWrap) {
        super(toWrap);
        setProjectFiles();
    }
    
    public Action[] getActions(boolean context) {
        Action[] actions = super.getActions(context);
        
        List<Action> result = new ArrayList<Action>();
        
        for (int cntr = 0; cntr < actions.length; cntr++) {
            if (actions[cntr] != GENERIC_PROJECTS_ACTIONS_MARKER) {
                result.add(actions[cntr]);
            } else {
                // honor 57874 contact:
                Lookup lookup = Lookups.forPath("Projects/Actions");
                Iterator<? extends Object> it = lookup.lookupAll(Object.class).iterator();
                while (it.hasNext()) {
                    Object next = it.next();
                    if (next instanceof Action) {
                        result.add((Action) next);
                    } else if (next instanceof JSeparator) {
                        result.add(null);
                    }
                }
            }
        }
        
        return result.toArray(new Action[result.size()]);
    }
    
    
    protected final void setProjectFiles() {
        Project prj = getLookup().lookup(Project.class);
        
        if (prj != null) {
            setProjectFiles(prj);
        } else {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Node: " + getOriginal() + " wrapped with ProjectNodeWrapper, but does not contain a Project in the lookup!");
        }
    }
    
    protected final void setProjectFiles(Project project) {
        Sources sources = ProjectUtils.getSources(project);  // returns singleton
        if (sourcesListener == null) {
            sourcesListener = WeakListeners.change(this, sources);
            sources.addChangeListener(sourcesListener);
        }
        setGroups(Arrays.asList(sources.getSourceGroups(Sources.TYPE_GENERIC)));
    }
    
    private final void setGroups(Collection<SourceGroup> groups) {
        if (groupsListeners != null) {
            for (Map.Entry<SourceGroup,PropertyChangeListener> entry : groupsListeners.entrySet()) {
                entry.getKey().removePropertyChangeListener(entry.getValue());
            }
        }
        groupsListeners = new HashMap<SourceGroup,PropertyChangeListener>();
        Set<FileObject> roots = new HashSet<FileObject>();
        for (SourceGroup group : groups) {
            PropertyChangeListener pcl = WeakListeners.propertyChange(this, group);
            groupsListeners.put(group, pcl);
            group.addPropertyChangeListener(pcl);
            FileObject fo = group.getRootFolder();
            roots.add(fo);
        }
        setFiles(roots);
    }
    
    protected final void setFiles(Set<FileObject> files) {
        if (fileSystemListeners != null) {
            for (Map.Entry<FileSystem,FileStatusListener> entry : fileSystemListeners.entrySet()) {
                entry.getKey().removeFileStatusListener(entry.getValue());
            }
        }
        
        fileSystemListeners = new HashMap<FileSystem,FileStatusListener>();
        this.files = files;
        if (files == null) return;
        
        Set<FileSystem> hookedFileSystems = new HashSet<FileSystem>();
        for (FileObject fo : files) {
            try {
                FileSystem fs = fo.getFileSystem();
                if (hookedFileSystems.contains(fs)) {
                    continue;
                }
                hookedFileSystems.add(fs);
                FileStatusListener fsl = FileUtil.weakFileStatusListener(this, fs);
                fs.addFileStatusListener(fsl);
                fileSystemListeners.put(fs, fsl);
            } catch (FileStateInvalidException e) {
                Logger.getLogger(ProjectNodeWrapper.class.getName()).log(Level.INFO, "Cannot get " + fo + " filesystem, ignoring...", e); // NOI18N
            }
        }
    }
    
//    public String getDisplayName() {
//        String s = super.getDisplayName();
//        
//        if (files != null && files.iterator().hasNext()) {
//            try {
//                FileObject fo = (FileObject) files.iterator().next();
//                s = fo.getFileSystem().getStatus().annotateName(s, files);
//            } catch (FileStateInvalidException e) {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
//            }
//        }
//        
//        return s;
//    }
//    
//    public String getHtmlDisplayName() {
//        if (files != null && files.iterator().hasNext()) {
//            try {
//                FileObject fo = (FileObject) files.iterator().next();
//                FileSystem.Status stat = fo.getFileSystem().getStatus();
//                if (stat instanceof FileSystem.HtmlStatus) {
//                    FileSystem.HtmlStatus hstat = (FileSystem.HtmlStatus) stat;
//                    
//                    String result = hstat.annotateNameHtml(
//                            super.getHtmlDisplayName(), files);
//                    
//                    //Make sure the super string was really modified
//                    if (result != null && !result.equals(getDisplayName())) {
//                        return result;
//                    }
//                }
//            } catch (FileStateInvalidException e) {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
//            }
//        }
//        return super.getHtmlDisplayName();
//    }
    
    public Image getIcon(int type) {
        Image img = super.getIcon(type);
        
        if (files != null && files.iterator().hasNext()) {
            try {
                FileObject fo = files.iterator().next();
                img = fo.getFileSystem().getStatus().annotateIcon(img, type, files);
            } catch (FileStateInvalidException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        
        return img;
    }
    
    public Image getOpenedIcon(int type) {
        Image img = super.getOpenedIcon(type);
        
        if (files != null && files.iterator().hasNext()) {
            try {
                FileObject fo = files.iterator().next();
                img = fo.getFileSystem().getStatus().annotateIcon(img, type, files);
            } catch (FileStateInvalidException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        
        return img;
    }
    
    public void run() {
        boolean fireIcon;
        boolean fireName;
        synchronized (privateLock) {
            fireIcon = iconChange;
            fireName = nameChange;
            iconChange = false;
            nameChange = false;
        }
        if (fireIcon) {
            fireIconChange();
            fireOpenedIconChange();
        }
        if (fireName) {
            fireDisplayNameChange(null, null);
        }
    }
    
    public void annotationChanged(FileStatusEvent event) {
        if (task == null) {
            task = RequestProcessor.getDefault().create(this);
        }
        
        synchronized (privateLock) {
            if ((iconChange == false && event.isIconChange())  || (nameChange == false && event.isNameChange())) {
                for (FileObject fo : files) {
                    if (event.hasChanged(fo)) {
                        iconChange |= event.isIconChange();
                        nameChange |= event.isNameChange();
                    }
                }
            }
        }
        
        task.schedule(50);  // batch by 50 ms
    }
    
    // sources change
    public void stateChanged(ChangeEvent e) {
        setProjectFiles();
    }
    
    // group change
    public void propertyChange(PropertyChangeEvent evt) {
        setProjectFiles();
    }
    
}
