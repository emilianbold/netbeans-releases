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

package org.netbeans.modules.subversion.ui.diff;

import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.diff.Difference;
import org.netbeans.modules.diff.EncodedReaderFactory;
import org.netbeans.modules.subversion.*;

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

    public String getMIMEType() {
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

    public Reader createReader() throws IOException {
        if (baseFile.isDirectory()) {
            // XXX return directory listing?
            // could be nice te return sorted directory content
            // such as vim if user "edits" directory
            return new StringReader(NbBundle.getMessage(DiffStreamSource.class, "LBL_Diff_NoFolderDiff")); // NOI18N
        }
        init();
        if (revision == null || remoteFile == null) return null;
        if (!mimeType.startsWith("text/")) {
            return new StringReader(NbBundle.getMessage(DiffStreamSource.class, "BK5001", getTitle())); // NOI18N
        } else {
            // XXX diff implementation dependency, we need Encoding API or rewrite to binary diff
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
        mimeType = Subversion.getInstance().getMimeType(baseFile);
        try {
            remoteFile = VersionsCache.getInstance().getFileRevision(baseFile, revision);
            if (!baseFile.exists() && remoteFile != null && remoteFile.exists()) {
                mimeType = Subversion.getInstance().getMimeType(remoteFile);
            }
            failure = null;
        } catch (Exception e) {
            // TODO detect interrupted IO (exception subclass), i.e. user cancel
            failure = new IOException("Can not load remote file for " + baseFile); // NOI18N
            failure.initCause(e);
            throw failure;
        }
    }
}
