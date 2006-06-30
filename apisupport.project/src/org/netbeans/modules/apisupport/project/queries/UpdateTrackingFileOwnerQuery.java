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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.spi.project.FileOwnerQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Associated built module files with their owning project.
 * @author Jesse Glick
 */
public final class UpdateTrackingFileOwnerQuery implements FileOwnerQueryImplementation {
    
    /** Default constructor for lookup. */
    public UpdateTrackingFileOwnerQuery() {}

    public Project getOwner(URI file) {
        if (!ModuleList.existKnownEntries()) {
            return null; // #65700
        }
        if (file.getScheme().equals("file")) { // NOI18N
            return getOwner(new File(file));
        } else {
            return null;
        }
    }

    public Project getOwner(FileObject file) {
        if (!ModuleList.existKnownEntries()) {
            return null; // #65700
        }
        File f = FileUtil.toFile(file);
        if (f != null) {
            return getOwner(f);
        } else {
            return null;
        }
    }
    
    private Project getOwner(File file) {
        Set/*<ModuleEntry>*/ entries = ModuleList.getKnownEntries(file);
        Iterator it = entries.iterator();
        while (it.hasNext()) {
            File sourcedir = ((ModuleEntry) it.next()).getSourceLocation();
            if (sourcedir != null) {
                FileObject sourcedirFO = FileUtil.toFileObject(sourcedir);
                if (sourcedirFO != null) {
                    try {
                        Project p = ProjectManager.getDefault().findProject(sourcedirFO);
                        if (p != null) {
                            return p;
                        }
                    } catch (IOException e) {
                        Util.err.notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            }
        }
        return null;
    }
    
}
