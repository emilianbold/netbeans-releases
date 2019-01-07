/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.mercurial.ui.diff;

import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.diff.Difference;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.VersionsCache;
import org.netbeans.modules.versioning.util.Utils;

import java.io.*;
import java.util.*;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage.HgRevision;
import org.openide.cookies.EditorCookie;

import org.openide.util.*;
import org.openide.util.lookup.Lookups;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;

/**
 * Stream source for diffing CVS managed files.
 *
 * 
 */
public class DiffStreamSource extends StreamSource {

    private final File      baseFile;
    private final HgRevision    revision;
    private final String    title;
    private String          mimeType;
    private Boolean         start;

    /**
     * Null is a valid value if base file does not exist in this revision. 
     */ 
    private File            remoteFile;
    private Boolean         canWriteBaseFile;
    private final File fileInRevision;
    private File encodingHolder;

    /**
     * Creates a new StreamSource implementation for Diff engine.
     * 
     * @param baseFile
     * @param revision file revision, may be null if the revision does not exist (ie for new files)
     * @param title title to use in diff panel
     */ 
    public DiffStreamSource(File fileInRevision, File baseFile, HgRevision revision, String title) {
        this.baseFile = Utilities.isMac() || Utilities.isWindows() ? FileUtil.normalizeFile(baseFile) : baseFile;
        this.fileInRevision = fileInRevision;
        this.revision = revision;
        this.title = title;
        this.start = true;
    }

    /** Creates DiffStreamSource for nonexiting files. */
    public DiffStreamSource(String title) {
        this.baseFile = null;
        this.fileInRevision = null;
        this.revision = null;
        this.title = title;
        this.start = true;
    }

    @Override
    public String getName() {
        if (baseFile != null) {
            return baseFile.getName();
        } else {
            return NbBundle.getMessage(DiffStreamSource.class, "LBL_Diff_Anonymous"); // NOI18N
        }
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
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

    @Override
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

    @Override
    public Writer createWriter(Difference[] conflicts) throws IOException {
        throw new IOException("Operation not supported"); // NOI18N
    }

    @Override
    public boolean isEditable() {
        return HgRevision.CURRENT.equals(revision) && isPrimary() && isBaseFileWritable();
    }

    private boolean isBaseFileWritable () {
        if (canWriteBaseFile == null) {
            FileObject fo = FileUtil.toFileObject(baseFile);
            canWriteBaseFile = fo != null && fo.canWrite();
        }
        return canWriteBaseFile;
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

    @Override
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
    
    @Override
    public void close () {
        if (start) {
            // not yet initialized
            return;
        }
        EditorCookie.Observable ec = getEditableCookie(remoteFile);
        if (ec != null && ec.getOpenedPanes() == null && !ec.isModified()) {
            ec.close();
        }
        super.close();
    }
    
    /**
     * Loads data.
     */
    synchronized void init() throws IOException {
        if (baseFile.isDirectory()) {
            return;
        }
        if (start == false) return;
        start = false;
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
                Map<File, File> allFilePairs = new HashMap<File, File>(allFiles.size());
                boolean renamed = !baseFile.equals(fileInRevision);
                for (File f : allFiles) {
                    if (renamed) {
                        allFilePairs.put(renameFile(f, baseFile, fileInRevision), f);
                    } else {
                        allFilePairs.put(f, f);
                    }
                }
                for (Map.Entry<File, File> entry : allFilePairs.entrySet()) {
                    File file = entry.getKey();
                    File currentPair = entry.getValue();
                    boolean isBase = file.equals(fileInRevision);
                    try {
                        File rf = VersionsCache.getInstance().getFileRevision(file, revision);
                        if (rf == null || !rf.exists()) {
                            remoteFile = null;
                            return;
                        }
                        File newRemoteFile = new File(tempFolder, file.getName());
                        Utils.copyStreamsCloseAll(new FileOutputStream(newRemoteFile), new FileInputStream(rf));
                        newRemoteFile.deleteOnExit();
                        if (isBase) {
                            remoteFile = newRemoteFile;
                            encodingHolder = currentPair;
                            if (encodingHolder.exists()) {
                                Utils.associateEncoding(encodingHolder, newRemoteFile);
                            } else if (remoteFile != null) {
                                boolean created = false;
                                try {
                                    if (encodingHolder.getParentFile().exists()) {
                                        created = encodingHolder.createNewFile();
                                        Utils.associateEncoding(encodingHolder, newRemoteFile);
                                    }
                                } catch (IOException ex) {
                                    // not interested
                                } finally {
                                    if (created) {
                                        encodingHolder.delete();
                                    }
                                }
                            }
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
            throw new IOException("Can not load remote file for " + baseFile, e);
        }
    }
    
    private static EditorCookie.Observable getEditableCookie (File file) {
        EditorCookie.Observable editorCookie = null;
        if (file == null) {
            return null;
        }
        FileObject fileObj = FileUtil.toFileObject(file);
        if (fileObj != null) {
            try {
                DataObject dao = DataObject.find(fileObj);
                if (dao instanceof MultiDataObject) {
                    MultiDataObject mdao = (MultiDataObject) dao;
                    for (MultiDataObject.Entry entry : mdao.secondaryEntries()) {
                        if (fileObj == entry.getFile() && entry instanceof CookieSet.Factory) {
                            CookieSet.Factory factory = (CookieSet.Factory) entry;
                            EditorCookie ec = factory.createCookie(EditorCookie.class);
                            if (ec instanceof EditorCookie.Observable) {
                                editorCookie = (EditorCookie.Observable) ec;
                            }
                        }
                    }
                }
                if (editorCookie == null) {
                    EditorCookie cookie = dao.getCookie(EditorCookie.class);
                    if (cookie instanceof EditorCookie.Observable) {
                        editorCookie = (EditorCookie.Observable) cookie;
                    }
                }
            } catch (DataObjectNotFoundException ex) {
            }
        }
        return editorCookie;
    }

    private File renameFile (File toRename, File baseFile, File renamedBaseFile) {
        File parent = renamedBaseFile.getParentFile();
        String baseFileName = baseFile.getName();
        String renamedFileName = renamedBaseFile.getName();
        String toRenameFileName = toRename.getName();
        String retval = toRenameFileName;
        if (!renamedFileName.equals(baseFileName)) {
            String baseNameNoExt = getFileNameNoExt(baseFileName);
            String renamedNameNoExt = getFileNameNoExt(renamedFileName);
            if (toRenameFileName.startsWith(baseNameNoExt)) {
                retval = renamedNameNoExt;
                if (toRenameFileName.length() > baseNameNoExt.length()) {
                    retval += toRenameFileName.substring(baseNameNoExt.length());
                }
            }
        }
        return new File(parent, retval);
    }

    private String getFileNameNoExt (String fileName) {
        int pos = fileName.lastIndexOf('.');
        if (pos != -1) {
            return fileName.substring(0, pos);
        }
        return fileName;
    }
}
