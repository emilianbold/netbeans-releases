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

package org.netbeans.modules.vmd.midp.components;

import java.io.IOException;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Karol Harezlak
 */
public final class MidpProjectSupport {
    
    /** Creates a new instance of MidpProjectSupport */
    private MidpProjectSupport() {
    }
    
    /**
     * Add library to the project based on the supplied names
     * @param document
     * @param libraryNames
     */
    public static void addLibraryToProject(final DesignDocument document, final String[] libraryNames) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                if (libraryNames == null || libraryNames.length <= 0)
                    return;
                final Project project = getProjectForDocument(document);
                if (project == null)
                    return;
                ProjectClassPathExtender extender = project.getLookup().lookup(ProjectClassPathExtender.class);
                final LibraryManager libraryManager = LibraryManager.getDefault();
                for (int i = 0; i < libraryNames.length; i++) {
                    final Library library = libraryManager.getLibrary(libraryNames[i]);
                    if (library != null) {
                        try {
                            extender.addLibrary(library);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }
    
    /**
     * Gets project for document
     * @param document
     * @return
     */
    private static Project getProjectForDocument(DesignDocument document) {
        if (document == null)
            return null;
        
        DataObjectContext context = ProjectUtils.getDataObjectContextForDocument(document);
        if (context == null)
            return null;
        
        return ProjectUtils.getProject(context);
    }
    
}
