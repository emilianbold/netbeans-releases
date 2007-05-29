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

package org.netbeans.modules.groovy.grailsproject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.queries.SharabilityQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;


/**
 *
 * @author Martin Adamek
 */
public class GrailsSources implements Sources {
    
    private final FileObject projectDir;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    
    GrailsSources(FileObject projectDir) {
        this.projectDir = projectDir;
    }
    
    public SourceGroup[] getSourceGroups(String type) {
        List<SourceGroup> result = new ArrayList<SourceGroup>();
        
        if (Sources.TYPE_GENERIC.equals(type)) {
            result.add(new Group(projectDir, projectDir.getName(), null, null));
        } else if ("conf".equals(type)) {
            result.add(new Group(projectDir.getFileObject("grails-app/conf"), "Configuration", null, null));
        } else if ("controllers".equals(type)) {
            result.add(new Group(projectDir.getFileObject("grails-app/controllers"), "Controllers", null, null));
        } else if ("domain".equals(type)) {
            result.add(new Group(projectDir.getFileObject("grails-app/domain"), "Domain classes", null, null));
        } else if ("i18n".equals(type)) {
            result.add(new Group(projectDir.getFileObject("grails-app/i18n"), "Message bundles", null, null));
        } else if ("services".equals(type)) {
            result.add(new Group(projectDir.getFileObject("grails-app/services"), "Services", null, null));
        } else if ("taglib".equals(type)) {
            result.add(new Group(projectDir.getFileObject("grails-app/taglib"), "Tag libraries", null, null));
        } else if ("util".equals(type)) {
            result.add(new Group(projectDir.getFileObject("grails-app/util"), "Utility classes", null, null));
        } else if ("views".equals(type)) {
            result.add(new Group(projectDir.getFileObject("grails-app/views"), "Views and layouts", null, null));
        }
        return result.toArray(new SourceGroup[result.size()]);
    }
    
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }
    
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }
    
    
    private final class Group implements SourceGroup {
        
        private final FileObject loc;
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
 
        private final String displayName;
        private final Icon icon;
        private final Icon openedIcon;
        
        public Group(FileObject loc, String displayName, Icon icon, Icon openedIcon) {
            this.loc = loc;
            this.displayName = displayName;
            this.icon = icon;
            this.openedIcon = openedIcon;
        }
        
        public FileObject getRootFolder() {
            return loc;
        }
        
        public String getName() {
            String location = loc.getPath();
            return location.length() > 0 ? location : "generic"; // NOI18N
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public Icon getIcon(boolean opened) {
            return opened ? icon : openedIcon;
        }
        
        public boolean contains(FileObject file) throws IllegalArgumentException {
            if (file == loc) {
                return true;
            }
            String path = FileUtil.getRelativePath(loc, file);
            if (path == null) {
                throw new IllegalArgumentException();
            }
            if (file.isFolder()) {
                path += "/"; // NOI18N
            }
            if (file.isFolder() && file != projectDir && ProjectManager.getDefault().isProject(file)) {
                // #67450: avoid actually loading the nested project.
                return false;
            }
            File f = FileUtil.toFile(file);
            if (f != null && SharabilityQuery.getSharability(f) == SharabilityQuery.NOT_SHARABLE) {
                return false;
            } // else MIXED, UNKNOWN, or SHARABLE; or not a disk file
            return true;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener l) {
            pcs.addPropertyChangeListener(l);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener l) {
            pcs.removePropertyChangeListener(l);
        }
        
        @Override
        public String toString() {
            return "GrailsSources.Group[name=" + getName() + ",rootFolder=" + getRootFolder() + "]"; // NOI18N
        }
        
    }
    
}
