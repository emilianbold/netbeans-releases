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

package org.netbeans.modules.apisupport.project;

import java.beans.PropertyChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * A NetBeans module project.
 * @author Jesse Glick
 */
final class NbModuleProject implements Project {
    
    private final AntProjectHelper helper;
    
    NbModuleProject(AntProjectHelper helper) {
        this.helper = helper;
    }
    
    public String getName() {
        return helper.getName();
    }
    
    public String getDisplayName() {
        // XXX look up localizing bundle
        return getName();
    }
    
    public Lookup getLookup() {
        // XXX need:
        // ClassPathProvider
        // ProjectXmlSavedHook
        // ProjectOpenedHook
        // SourceForBinaryQueryImplementation
        return Lookup.EMPTY;
    }
    
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }
    
}
