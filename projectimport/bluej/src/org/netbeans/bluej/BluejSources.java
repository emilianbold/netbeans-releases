/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.bluej;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.util.Utilities;


/**
 * Implementation of {@link Sources} interface for BluejProject.
 * @author Milos Kleint
 */
public class BluejSources implements Sources {
    
    private final PropertyEvaluator evaluator;
    private BluejProject project;
    /**
     * Flag to forbid multiple invocation of {@link SourcesHelper#registerExternalRoots} 
     **/
    private boolean externalRootsRegistered;    
    private final List/*<ChangeListener>*/ listeners = new ArrayList();

    private SourceGroup[] javaSources;

    private SourceGroup[] genericSources;

    BluejSources(BluejProject project, PropertyEvaluator evaluator) {
        this.evaluator = evaluator;
        this.project = project;
        javaSources = new SourceGroup[] {new TheOneSourceGroup(project.getProjectDirectory())};
        genericSources = new SourceGroup[] {new TheOneSourceGroup(project.getProjectDirectory())};
    }

    /**
     */
    public SourceGroup[] getSourceGroups(final String type) {
        if (JavaProjectConstants.SOURCES_TYPE_JAVA.equals(type)) {
            return javaSources;
        }
        if (Sources.TYPE_GENERIC.equals(type)) {
            return genericSources;
        }
        return new SourceGroup[0];
    }


    public void addChangeListener(ChangeListener changeListener) {
        synchronized (listeners) {
            listeners.add(changeListener);
        }
    }

    public void removeChangeListener(ChangeListener changeListener) {
        synchronized (listeners) {
            listeners.remove(changeListener);
        }
    }

    private void fireChange() {
        ChangeListener[] _listeners;
        synchronized (listeners) {
            if (listeners.isEmpty()) {
                return;
            }
            _listeners = (ChangeListener[])listeners.toArray(new ChangeListener[listeners.size()]);
        }
        ChangeEvent ev = new ChangeEvent(this);
        for (int i = 0; i < _listeners.length; i++) {
            _listeners[i].stateChanged(ev);
        }
    }

    
    private class TheOneSourceGroup implements SourceGroup {

        private FileObject root;
        
        private TheOneSourceGroup(FileObject root) {
            this.root = root;
        }
        
        public FileObject getRootFolder() {
            return root;
        }

        public String getName() {
            return "Sources";
        }

        public String getDisplayName() {
            return "Source Packages";
        }

        public Icon getIcon(boolean b) {
            return new ImageIcon(Utilities.loadImage("/org/netbeans/bluej/resources/bluejproject.png"));
        }

        public boolean contains(FileObject fileObject) throws IllegalArgumentException {
            if ("bluej.pkg".equals(fileObject.getNameExt())) {
                return false;
            }
            if ("build.xml".equals(fileObject.getNameExt())) {
                return false;
            } 
            if (fileObject.isFolder() && fileObject.getFileObject("bluej.pkg") == null) {
                return false;
            }
            return true;
        }

        public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        }
        
    }
}
