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

package org.netbeans.modules.versioning.system.cvss.ui.actions.diff;

import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.diff.Difference;
import org.netbeans.modules.versioning.system.cvss.VersionsCache;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.ExecutorGroup;
import org.netbeans.modules.diff.EncodedReaderFactory;
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

    private ExecutorGroup   group;
    private boolean         initialized;

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

    public String getName() {
        return baseFile.getName();
    }

    public String getTitle() {
        return title;
    }

    public void setGroup(ExecutorGroup group) {
        this.group = group;
    }

    public String getMIMEType() {
        try {
            init(null);
        } catch (IOException e) {
            return null;
        }
        return mimeType;
    }

    public Reader createReader() throws IOException {
        init(group);
        if (revision == null || remoteFile == null) return null;
        if (binary) {
            return new StringReader(NbBundle.getMessage(DiffStreamSource.class, "BK5001", getTitle()));
        } else {
            FileObject remoteFo = FileUtil.toFileObject(remoteFile);
            FileObject bfo = FileUtil.toFileObject(baseFile);
            if (bfo != null) {
                return EncodedReaderFactory.getDefault().getReader(remoteFo, null, bfo);
            } else {
                // locally deleted file, use a nasty workaround
                File tempFile = new File(remoteFile.getParentFile(), remoteFile.getName().substring(0, remoteFile.getName().indexOf('#')));
                if (tempFile.exists()) tempFile.delete();

                bfo = FileUtil.copyFile(remoteFo, remoteFo.getParent(), tempFile.getName(), "");
                
                Reader r = EncodedReaderFactory.getDefault().getReader(remoteFo, null, bfo);
                tempFile.delete();
                return r;
            }
        }
    }

    public Writer createWriter(Difference[] conflicts) throws IOException {
        throw new IOException("Operation not supported"); // NOI18N
    }

    /**
     * Loads data over network.
     *
     * @param group combines multiple loads or <code>null</code>
     * Note that this group must not be executed later on. 
     */
    synchronized void init(ExecutorGroup group) throws IOException {
        if (initialized) return;
        initialized = true;
        binary = !CvsVersioningSystem.getInstance().isText(baseFile);
        try {
            remoteFile = VersionsCache.getInstance().getRemoteFile(baseFile, revision, group);
            if (!baseFile.exists() && remoteFile != null && remoteFile.exists()) {
                binary = !CvsVersioningSystem.getInstance().isText(remoteFile);
            }
            failure = null;
        } catch (Exception e) {
            failure = new IOException("Cannot initialize stream source"); // NOI18N
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
