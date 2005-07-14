/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.projectimport.j2seimport;

import java.io.File;
import java.util.Collection;
import java.util.Set;
import org.netbeans.api.java.platform.JavaPlatform;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Radek Matous
 */
public interface ProjectModel {
    String getName();
    FileObject getProjectDir();
    Collection getLibraries();
    Collection getUserLibraries();
    Collection getSourceRoots();
    Set getDependencies();
    File getJDKDirectory();
    
    Collection/**<String>*/ getErrors();
    WarningContainer getWarnings();
    
    boolean isAlreadyImported();
    
    public interface Library {
        File getArchiv();
    }
    
    public interface SourceRoot {
        String getLabel();
        File getDirectory();
    }
    
    public interface UserLibrary {
        String getName();
        Collection getLibraries();
    }
}
