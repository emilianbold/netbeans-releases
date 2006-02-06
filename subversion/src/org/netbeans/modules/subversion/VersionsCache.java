/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion;

import java.io.*;

/**
 * File revisions cache. It can access pristine files.
 *
 *
 * @author Petr Kuzel
 */
public class VersionsCache {
    
    private static VersionsCache instance;
    
    /** Creates a new instance of VersionsCache */
    private VersionsCache() {
    }

    public static synchronized VersionsCache getInstance() {
        if (instance == null) {
            instance = new VersionsCache();
        }
        return instance;
    }
    
    public File getFileRevision(File base, String revision) {
        if ("BASE".equals(revision)) { // XXX
            String name = base.getName();
            File dir = base.getParentFile();
            // XXX _svn
            File svnDir = new File(dir, ".svn");  // NOI18N
            if (svnDir.isDirectory()) {
                File text_base = new File(svnDir, "text-base"); // NOI18N
                File pristine = new File(text_base, name + ".svn-base"); // NOI18N
                return pristine;
            }
        } else if ("LOCAL".equals(revision)) {
            return base;
        }
        // TODO how to cache locally? There are no per file revisions
        return null;
    }
}
