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
        if (file.getScheme().equals("file")) { // NOI18N
            return getOwner(new File(file));
        } else {
            return null;
        }
    }

    public Project getOwner(FileObject file) {
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
