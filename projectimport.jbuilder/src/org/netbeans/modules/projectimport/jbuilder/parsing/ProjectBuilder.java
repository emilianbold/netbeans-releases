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
