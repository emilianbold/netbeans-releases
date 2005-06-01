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

package org.netbeans.modules.versioning.system.cvss.ui.actions.diff;

import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.xml.parsers.DocumentInputSource;
import org.netbeans.modules.versioning.system.cvss.VersionsCache;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.cookies.EditorCookie;
import org.openide.ErrorManager;

import javax.swing.text.Document;
import java.io.*;

/**
 * Stream source for diffing CVS managed files.
 * 
 * @author Maros Sandor
 */
class DiffStreamSource extends StreamSource {
        
    private final File      baseFile;
    private final String    revision;
    private final String    title;
    private String          mimeType;

    private IOException     failure;
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

    public String getName() {
        return baseFile.getName();
    }

    public String getTitle() {
        return title;
    }

    public String getMIMEType() {
        try {
            init();
        } catch (IOException e) {
            return null;
        }
        return mimeType;
    }

    public Reader createReader() throws IOException {
        init();
        if (revision == null) return null;
        if (revision == Setup.REVISION_CURRENT) {

            // TODO make diff line.separator insensitive
            // it causes problem when diffing document content (always '\n')
            // and disk files (commonly using platform separator).
            if ("\n".equals(System.getProperty("line.separator"))) {  // NOI18N
                // take it from editor if opened
                FileObject fo = FileUtil.toFileObject(remoteFile);
                if (fo != null) {
                    try {
                        DataObject dobj = DataObject.find(fo);
                        FileObject primary = dobj.getPrimaryFile();
                        FileObject remote = FileUtil.toFileObject(remoteFile);
                        if (primary.equals(remote)) {  // here we assume that Document belongs to primary file
                            EditorCookie editorCookie = (EditorCookie) dobj.getCookie(EditorCookie.class);
                            if (editorCookie != null) {
                                Document doc = editorCookie.getDocument();
                                if (doc != null) {
                                    DocumentInputSource inputSource = new DocumentInputSource(doc);
                                    return inputSource.getCharacterStream();
                                }
                            }
                        }
                    } catch (IOException ex) {
                        // ignore missing dataobject
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                }
            }
        }
        return new FileReader(remoteFile);
    }

    public Writer createWriter(Difference[] conflicts) throws IOException {
        throw new IOException("Operation not supported");
    }

    synchronized void init() throws IOException {
        if (remoteFile != null || revision == null) return;
        try {
            remoteFile = VersionsCache.getInstance().getRemoteFile(baseFile, revision);
            failure = null;
        } catch (Exception e) {
            failure = new IOException("Cannot initialize stream source");
            failure.initCause(e);
            throw failure;
        }
        FileObject fo = FileUtil.toFileObject(remoteFile);
        mimeType = fo != null ? fo.getMIMEType() : "text/plain";        
    }
}
