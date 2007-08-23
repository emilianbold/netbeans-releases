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
 * "Portions Copyrighted [year] [name of copyright owner]" // NOI18N
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.mercurial;

import java.io.*;
import org.netbeans.modules.mercurial.ui.diff.Setup;
import org.netbeans.modules.mercurial.util.*;
import org.openide.filesystems.FileUtil;

/**
 * File revisions cache. It can access pristine files.
 *
 * XXX and what exactly is cached here?!
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

    /**
     * Loads the file in specified revision.
     *
     * @return null if the file does not exist in given revision
     */
    public File getFileRevision(File base, String revision) throws IOException {
        if (Setup.REVISION_BASE.equals(revision)) {
            try {
                File tempFile = File.createTempFile(base.getName(), null);
                File repository = HgUtils.getRootFile(HgUtils.getCurrentContext(null));
                HgCommand.doCat(repository, base, tempFile);
                if (tempFile.length() == 0) return null;
                return tempFile;
            } catch (HgException e) {
                IOException ioe = new IOException();
                ioe.initCause(e);
                throw ioe;
            }
        } else if (Setup.REVISION_CURRENT.equals(revision)) {
            return base;
        } else {
            try {
                File tempFile = File.createTempFile(base.getName(), null);
                File repository = HgUtils.getRootFile(HgUtils.getCurrentContext(null));
                HgCommand.doCat(repository, base, tempFile, revision);
                if (tempFile.length() == 0) return null;
                return tempFile;
            } catch (HgException e) {
                IOException ioe = new IOException();
                ioe.initCause(e);
                throw ioe;
            }
        }
    }
}
