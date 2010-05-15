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

package org.netbeans.modules.bpel.debugger;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.bpel.debugger.spi.SourcePathSelectionProvider;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Alexander Zgursky
 */
public class DebugProjectSourcePathSelectionProvider implements SourcePathSelectionProvider {
    
    private ContextProvider myContextProvider;
    private Set<String> mySubprojectsBaseDirs;
    
    /** Creates a new instance of AttachingSourcePathSelectionProvider */
    public DebugProjectSourcePathSelectionProvider(ContextProvider contextProvider) {
        myContextProvider = contextProvider;
    }

    public boolean isSelected(String path) {
        if (mySubprojectsBaseDirs == null) {
            Map params = myContextProvider.lookupFirst(null, Map.class);
            if (params != null) {
                String projectBaseDir = (String)params.get("projectBaseDir");
                if (projectBaseDir != null) {
                    try {
                        mySubprojectsBaseDirs =
                                ProjectUtil.getSubprojectsBaseDirs(projectBaseDir);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            if (mySubprojectsBaseDirs == null) {
                mySubprojectsBaseDirs = new HashSet<String>();
            }
        }
        
        //defensive logic
        if (mySubprojectsBaseDirs.isEmpty()) {
            return true;
        }
        
        FileObject fo = FileUtil.toFileObject(new File(path));
        Project project = FileOwnerQuery.getOwner(fo);
        if (project == null) {
            return false;
        }
        String baseDirToFind = project.getProjectDirectory().getPath();
        for (String baseDir : mySubprojectsBaseDirs) {
            if (baseDirToFind.equals(baseDir)) {
                return true;
            }
        }
        return false;
    }
}
