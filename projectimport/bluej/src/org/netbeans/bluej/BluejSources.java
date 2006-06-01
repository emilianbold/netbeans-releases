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
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/**
 * Implementation of {@link Sources} interface for BluejProject.
 * @author Milos Kleint
 */
public class BluejSources implements Sources {
    
    private SourceGroup[] javaSources;

    private SourceGroup[] genericSources;

    BluejSources(BluejProject project) {
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
        // we never fire anything
    }

    public void removeChangeListener(ChangeListener changeListener) {
        // we never fire anything..
    }

    
    private static class TheOneSourceGroup implements SourceGroup {

        private FileObject root;
        
        private TheOneSourceGroup(FileObject root) {
            this.root = root;
        }
        
        public FileObject getRootFolder() {
            return root;
        }

        public String getName() {
            return "Sources";  // NOI18N
        }

        public String getDisplayName() {
            return NbBundle.getMessage(BluejSources.class, "Source_Group_Display_Name");
        }

        public Icon getIcon(boolean b) {
            return new ImageIcon(Utilities.loadImage("/org/netbeans/bluej/resources/bluejproject.png"));   // NOI18N
        }

        public boolean contains(FileObject fileObject) throws IllegalArgumentException {
            if ("bluej.pkg".equals(fileObject.getNameExt())) {  // NOI18N
                return false;
            }
            if ("build.xml".equals(fileObject.getNameExt())) {  // NOI18N
                return false;
            } 
            if (fileObject.isFolder() && fileObject.getFileObject("bluej.pkg") == null) {  // NOI18N
                return false;
            }
            return true;
        }

        public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        // we never fire anything
        }

        public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        // we never fire anything
        }
        
    }
}
