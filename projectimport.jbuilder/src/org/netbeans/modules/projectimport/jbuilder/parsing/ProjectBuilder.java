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

package org.netbeans.modules.projectimport.jbuilder.parsing;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 * Serveral helpers for parsing, managing, loading JBuilder projects
 *
 * @author Radek Matous
 */
public abstract class ProjectBuilder {
    private static final ProjectBuilder[] SUPPORTED_TYPES = new ProjectBuilder[] {
        new JpxBuilder(".jpr"),//NOI18N;
        new JpxBuilder(".jpx")//NOI18N;
    };
    
    public static boolean isProjectFile(final File file) {
        return (getProvider(file) != null);
    }
    
    /** may return null for not supported files*/
    public static Collection/*<ProjectModel>*/ buildProjectModels(final File file) {
        ProjectBuilder provider = null;
        provider = getProvider(file);
        
        return (provider != null) ? provider.buildImpl(file) : Collections.EMPTY_LIST;
    }
    
    static ProjectBuilder getProvider(final File file) {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo != null) {
            for (int i = 0; i < SUPPORTED_TYPES.length; i++) {
                if (SUPPORTED_TYPES[i].isProjectFileImpl(file)) {
                    return SUPPORTED_TYPES[i];
                }
            }
        }
        
        return null;
    }
        
    protected abstract Collection/*<ProjectModel>*/ buildImpl(File file);
    protected abstract String getSupportedExtension();
    private final boolean isProjectFileImpl(File file) {
        return file.getName().toLowerCase().endsWith(getSupportedExtension());
    }    
}
