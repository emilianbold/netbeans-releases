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

package org.netbeans.modules.bpel.project;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.bpel.debugger.api.BpelSourcesRegistry;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Alexander Zgursky
 */
public class BpelSourcesRegistryHelper {
    private final BpelproProject myProject;
    private final List<String> myRegisteredSources =
            new ArrayList<String>();
    private final BpelSourcesRegistry mySourcesRegistry;
    
    /** Creates a new instance of BpelSourcesRegistryHelper */
    public BpelSourcesRegistryHelper(BpelproProject project) {
        myProject = project;
        mySourcesRegistry = (BpelSourcesRegistry)Lookup.
                getDefault().lookup(BpelSourcesRegistry.class);
    }
    
    public synchronized void register() {
        if (mySourcesRegistry == null) {
            return;
        }
        Sources sources = ProjectUtils.getSources(myProject);
        SourceGroup sgs [] = sources.getSourceGroups(ProjectConstants.SOURCES_TYPE_PROJECT);
        for (int i = 0; i < sgs.length; i++) {
            FileObject fo = sgs[i].getRootFolder();
            if (fo == null) {
                continue;
            }
            
            File file = FileUtil.toFile(fo);
            if (file == null) {
                continue;
            }
            
            String path = file.getPath();
            if (mySourcesRegistry.addSourceRoot(path)) {
                myRegisteredSources.add(path);
            }
        }
    }
    
    public synchronized void unregister() {
        if (mySourcesRegistry == null) {
            return;
        }
        for (String path : myRegisteredSources) {
            mySourcesRegistry.removeSourceRoot(path);
        }
        myRegisteredSources.clear();
    }
}
