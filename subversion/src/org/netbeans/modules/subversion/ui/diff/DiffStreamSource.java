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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion.ui.diff;

import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.diff.Difference;
import org.netbeans.modules.subversion.*;
import org.netbeans.modules.subversion.client.PropertiesClient;
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
    private final String    propertyName;
    private final String    revision;
    private final String    title;
    private String          mimeType;

    /**
     * Null is a valid value if base file does not exist in this revision. 
     */ 
    private File            remoteFile;
    private MultiDiffPanel.Property propertyValue;

    /**
     * Creates a new StreamSource implementation for Diff engine.
     * 
     * @param baseFile
     * @param revision file revision, may be null if the revision does not exist (ie for new files)
     * @param title title to use in diff panel
     */ 
    public DiffStreamSource(File baseFile, String propertyName, String revision, String title) {
        this.baseFile = baseFile;
        this.propertyName = propertyName;
        this.revision = revision;
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
        try {
            init();
        } catch (IOException e) {
            return null; // XXX use error manager HACK null  potentionally kills DiffViewImpl, NPE while constructing EditorKit
        }
        return mimeType;
    }

    public synchronized Reader createReader() throws IOException {
        init();
        if (propertyName != null) {
            if (propertyValue != null) {
                return propertyValue.toReader();
            } else {
                return null;
            }
        } else {
            if (revision == null || remoteFile == null) return null;
            if (!mimeType.startsWith("text/")) {
                return new StringReader(NbBundle.getMessage(DiffStreamSource.class, "BK5001", getTitle())); // NOI18N
            } else {
                return Utils.createReader(remoteFile);
            }
        }
    }

    public Writer createWriter(Difference[] conflicts) throws IOException {
        throw new IOException("Operation not supported"); // NOI18N
    }

    public boolean isEditable() {
        return propertyName == null && Setup.REVISION_CURRENT.equals(revision) && isPrimary();
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
        if (propertyName != null || remoteFile == null || !isPrimary()) return Lookups.fixed();
        FileObject remoteFo = FileUtil.toFileObject(remoteFile);
        if (remoteFo == null) return Lookups.fixed();

        return Lookups.fixed(remoteFo);
    }
    
    /**
     * Loads data over network.
     */
    synchronized void init() throws IOException {
        if (propertyValue != null || remoteFile != null || revision == null) return;
        if (propertyName != null) {
            initProperty();
            return;
        }
        if (baseFile.isDirectory()) {
            mimeType = "content/unknown"; // NOI18N
            return;
        }
        mimeType = Subversion.getInstance().getMimeType(baseFile);
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
                        if(rf == null) {
                            remoteFile = null;
                            return;
                        }
                        File newRemoteFile = new File(tempFolder, file.getName());
                        newRemoteFile.deleteOnExit();
                        Utils.copyStreamsCloseAll(new FileOutputStream(newRemoteFile), new FileInputStream(rf));
                        if (isBase) {
                            remoteFile = newRemoteFile;
                            Utils.associateEncoding(file, newRemoteFile);                            
                        }
                    } catch (Exception e) {
                        if (isBase) throw e;
                        // we cannot check out peer file so the dataobject will not be constructed properly
                    }
                }
            }
            if (!baseFile.exists() && remoteFile != null && remoteFile.exists()) {
                mimeType = Subversion.getInstance().getMimeType(remoteFile);
            }
        } catch (Exception e) {
            // TODO detect interrupted IO (exception subclass), i.e. user cancel
            IOException failure = new IOException("Can not load remote file for " + baseFile); // NOI18N
            failure.initCause(e);
            throw failure;
        }
    }

    private void initProperty() throws IOException {
        PropertiesClient client = new PropertiesClient(baseFile);
        if (Setup.REVISION_BASE.equals(revision)) {
            byte [] value = client.getBaseProperties().get(propertyName);
            propertyValue = value != null ? new MultiDiffPanel.Property(value) : null;
        } else if (Setup.REVISION_CURRENT.equals(revision)) {
            byte [] value = client.getProperties().get(propertyName);
            propertyValue = value != null ? new MultiDiffPanel.Property(value) : null;
        }
        mimeType = propertyValue != null ? propertyValue.getMIME() : "content/unknown"; // NOI18N
    }
}
