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

package org.netbeans.modules.subversion.ui.diff;

import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.diff.Difference;
import org.netbeans.modules.diff.EncodedReaderFactory;
import org.netbeans.modules.subversion.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import java.io.*;
import org.openide.util.*;

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

    private IOException     failure;
    /**
     * Null is a valid value if base file does not exist in this revision. 
     */ 
    private File            remoteFile;
    private boolean         binary;

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
            return "anonymous";
        }
    }

    public String getTitle() {
        return title;
    }

    public String getMIMEType() {
        if (baseFile.isDirectory()) {
            // http://www.rfc-editor.org/rfc/rfc2425.txt
            return "content/unknown"; // "text/directory";  //XXX no editor for directory MIME type => NPE // NOI18N
        }

        try {
            init();
        } catch (IOException e) {
            return null; // XXX potentionally kills DiffViewImpl
        }
        return mimeType;
    }

    public Reader createReader() throws IOException {
        if (baseFile.isDirectory()) {
            // XXX return directory listing?
            return new StringReader("[No Content, This is Folder]");
        }
        init();
        if (revision == null || remoteFile == null) return null;
        if (binary) {
            return new StringReader(NbBundle.getMessage(DiffStreamSource.class, "BK5001", getTitle()));
        } else {
            // XXX implementation dependency, we need Encoding API or rewrite to binary diff
            return EncodedReaderFactory.getDefault().getReader(remoteFile, mimeType);  
        }
    }

    public Writer createWriter(Difference[] conflicts) throws IOException {
        throw new IOException("Operation not supported"); // NOI18N
    }

    /**
     * Loads data over network.
     */
    synchronized void init() throws IOException {
        if (baseFile.isDirectory()) {
            return;
        }
        if (remoteFile != null || revision == null) return;
//        binary = !CvsVersioningSystem.getInstance().isText(baseFile);
        try {
            remoteFile = VersionsCache.getInstance().getFileRevision(baseFile, revision);
            if (!baseFile.exists() && remoteFile != null && remoteFile.exists()) {
//                binary = !CvsVersioningSystem.getInstance().isText(remoteFile);
            }
            failure = null;
        } catch (Exception e) {
            // TODO detect interrupted IO, i.e. user cancel
            failure = new IOException("Can not load remote file for " + baseFile); // NOI18N
            failure.initCause(e);
            throw failure;
        }
        FileObject fo = FileUtil.toFileObject(baseFile);
        if (fo == null && remoteFile != null) {
            fo = FileUtil.toFileObject(remoteFile);
        }
        if (fo != null) {
            mimeType = fo.getMIMEType();
        } else if (binary) {
            mimeType = "application/octet-stream"; // NOI18N
        } else {
            mimeType = "text/plain"; // NOI18N
        }
    }
}
