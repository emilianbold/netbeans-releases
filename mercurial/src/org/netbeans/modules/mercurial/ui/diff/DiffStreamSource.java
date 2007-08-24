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

package org.netbeans.modules.mercurial.ui.diff;

import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.diff.Difference;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.VersionsCache;
import org.netbeans.modules.versioning.util.Utils;

import java.io.*;
import java.util.*;

import org.openide.util.*;
import org.openide.util.lookup.Lookups;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * Stream source for diffing CVS managed files.
 *
 * @author Maros Sandor
 */
public class DiffStreamSource extends StreamSource {

    private final File      baseFile;
    private final String    revision;
    private final String    title;
    private String          mimeType;

    /**
     * Null is a valid value if base file does not exist in this revision. 
     */ 
    private File            remoteFile;

    /**
     * Creates a new StreamSource implementation for Diff engine.
     * 
     * @param baseFile
     * @param revision file revision, may be null if the revision does not exist (ie for new files)
     * @param title title to use in diff panel
     */ 
    public DiffStreamSource(File baseFile, String revision, String title) {
        this.baseFile = baseFile;
        this.revision = revision;
        this.title = title;
    }

    /** Creates DiffStreamSource for nonexiting files. */
    public DiffStreamSource(String title) {
        this.baseFile = null;
        this.revision = null;
        this.title = title;
    }

    public String getName() {
        if (baseFile != null) {
            return baseFile.getName();
        } else {
            return NbBundle.getMessage(DiffStreamSource.class, "LBL_Diff_Anonymous"); // NOI18N
        }
    }

    public String getTitle() {
        return title;
    }

    public synchronized String getMIMEType() {
        if (baseFile.isDirectory()) {
            // http://www.rfc-editor.org/rfc/rfc2425.txt
            return "content/unknown"; // "text/directory";  //HACK no editor for directory MIME type => NPE while constructing EditorKit // NOI18N
        }

        try {
            init();
        } catch (IOException e) {
            return null; // XXX use error manager HACK null  potentionally kills DiffViewImpl, NPE while constructing EditorKit
        }
        return mimeType;
    }

    public synchronized Reader createReader() throws IOException {
        if (baseFile.isDirectory()) {
            // XXX return directory listing?
            // could be nice te return sorted directory content
            // such as vim if user "edits" directory // NOI18N
            return new StringReader(NbBundle.getMessage(DiffStreamSource.class, "LBL_Diff_NoFolderDiff")); // NOI18N
        }
        init();
        if (revision == null || remoteFile == null) return null;
        if (!mimeType.startsWith("text/")) { // NOI18N
            return null;
        } else {
            return Utils.createReader(remoteFile);  
        }
    }

    public Writer createWriter(Difference[] conflicts) throws IOException {
        throw new IOException("Operation not supported"); // NOI18N
    }

    public boolean isEditable() {
        return Setup.REVISION_CURRENT.equals(revision) && isPrimary();
    }

    private boolean isPrimary() {
        FileObject fo = FileUtil.toFileObject(baseFile);
        if (fo != null) {
            try {
                DataObject dao = DataObject.find(fo);
                return fo.equals(dao.getPrimaryFile());
            } catch (DataObjectNotFoundException e) {
                // no dataobject, never mind
            }
        }
        return true;
    }

    public synchronized Lookup getLookup() {
        try {
            init();
        } catch (IOException e) {
            return Lookups.fixed();
        }
        if (remoteFile == null || !isPrimary()) return Lookups.fixed();
        FileObject remoteFo = FileUtil.toFileObject(remoteFile);
        if (remoteFo == null) return Lookups.fixed();

        return Lookups.fixed(remoteFo);
    }
    
    /**
     * Loads data.
     */
    synchronized void init() throws IOException {
        if (baseFile.isDirectory()) {
            return;
        }
        if (remoteFile != null || revision == null) return;
        mimeType = Mercurial.getInstance().getMimeType(baseFile);
        try {
            if (isEditable()) {
                // we cannot move editable documents because that would break Document sharing
                remoteFile = VersionsCache.getInstance().getFileRevision(baseFile, revision);
            } else {
                File tempFolder = Utils.getTempFolder();
                // To correctly get content of the base file, we need to checkout all files that belong to the same
                // DataObject. One example is Form files: data loader removes //GEN:BEGIN comments from the java file but ONLY
                // if it also finds associate .form file in the same directory
                Set<File> allFiles = Utils.getAllDataObjectFiles(baseFile);
                for (File file : allFiles) {
                    boolean isBase = file.equals(baseFile);
                    try {
                        File rf = VersionsCache.getInstance().getFileRevision(file, revision);
                        if (rf == null) {
                            remoteFile = null;
                            return;
                        }
                        File newRemoteFile = new File(tempFolder, file.getName());
                        Utils.copyStreamsCloseAll(new FileOutputStream(newRemoteFile), new FileInputStream(rf));
                        newRemoteFile.deleteOnExit();
                        if (isBase) {
                            remoteFile = newRemoteFile;
                        }
                    } catch (Exception e) {
                        if (isBase) throw e;
                        // we cannot check out peer file so the dataobject will not be constructed properly
                    }
                }
            }
            if (!baseFile.exists() && remoteFile != null && remoteFile.exists()) {
                mimeType = Mercurial.getInstance().getMimeType(remoteFile);
            }
        } catch (Exception e) {
            // TODO detect interrupted IO (exception subclass), i.e. user cancel
            IOException failure = new IOException("Can not load remote file for " + baseFile); // NOI18N
            failure.initCause(e);
            throw failure;
        }
    }
}
