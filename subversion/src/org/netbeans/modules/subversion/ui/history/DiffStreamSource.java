/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.subversion.ui.history;

import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.diff.Difference;
import org.netbeans.modules.subversion.*;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.versioning.util.Utils;
import java.io.*;
import java.util.logging.Level;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnClientFactory;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.subversion.util.FileUtils;
import org.openide.util.*;
import org.openide.util.lookup.Lookups;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Stream source for diffing remote SVN managed files .
 *
 * @author Maros Sandor
 */
public class DiffStreamSource extends StreamSource implements Cancellable {

    private final File      baseFile;    
    private final String    revision;
    private final String    title;
    private String          mimeType;
    private SVNUrl          url;
    private SVNUrl          repoUrl;
    private SvnClient       client;
    
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
    public DiffStreamSource(File baseFile, SVNUrl repoUrl, SVNUrl fileUrl, String revision, String title) {
        this.baseFile = baseFile;
        this.revision = revision;
        this.title = title;
        this.url = fileUrl;
        this.repoUrl = repoUrl;
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
            Subversion.LOG.log(Level.INFO, "DiffStreamSource.getMIMEType() returns null", e);
            return null; // HACK null cases the file handled as binary
        }
        return mimeType;
    }

    public synchronized Reader createReader() throws IOException {
        init();
        if (revision == null || remoteFile == null) return null;
        if (!mimeType.startsWith("text/")) {
            return null;
        } else {
            return Utils.createReader(remoteFile);
        }
    }

    public Writer createWriter(Difference[] conflicts) throws IOException {
        throw new IOException("Operation not supported"); // NOI18N
    }

    public boolean isEditable() {
        return false;
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
     * Loads data over network.
     */
    synchronized void init() throws IOException {
        if (remoteFile != null || revision == null) return;
        if (baseFile.isDirectory()) {
            mimeType = "content/unknown"; // NOI18N
            return;
        }
        mimeType = SvnUtils.getMimeType(baseFile);
        try {
            if (isEditable()) {
                // we cannot move editable documents because that would break Document sharing
                remoteFile = VersionsCache.getInstance().getFileRevision(baseFile, revision);
            } else {               
                try {                        
                    client = Subversion.getInstance().getClient(repoUrl); 
                    InputStream in = null;
                    try {
                        if(SvnClientFactory.isCLI()) {
                            // XXX why was the revision given twice ??? !!! CLI WORKAROUND?
                            // doesn't work with javahl but we won't change for cli as there might be some reason
                            in = client.getContent(url.appendPath("@" + revision), SvnUtils.toSvnRevision(revision));
                        } else {
                            in = client.getContent(url, SvnUtils.toSvnRevision(revision), SvnUtils.toSvnRevision(revision));
                        }
                    } catch (SVNClientException e) {
                        if(SvnClientExceptionHandler.isFileNotFoundInRevision(e.getMessage())    ||
                           SvnClientExceptionHandler.isPathNotFound(e.getMessage()))
                        {
                            in = new ByteArrayInputStream(new byte[] {});
                        } else {
                            throw e;
                        }
                    }
                    // keep original extension so MIME can be guessed by the extension
                    File rf = File.createTempFile("nb-svn", baseFile.getName());  // NOI18N
                    rf = FileUtil.normalizeFile(rf);
                    rf.deleteOnExit();  // hard to track actual lifetime
                    FileUtils.copyStreamToFile(new BufferedInputStream(in), rf);  
                    if(rf == null) {
                        remoteFile = null;
                        return;
                    }
                    remoteFile = rf;
                    Utils.associateEncoding(baseFile, rf);                            
                } catch (Exception e) {
                    throw e;
                }
            }
            if (!baseFile.exists() && remoteFile != null && remoteFile.exists()) {
                mimeType = SvnUtils.getMimeType(remoteFile);
            }
        } catch (Exception e) {
            // TODO detect interrupted IO (exception subclass), i.e. user cancel
            IOException failure = new IOException("Can not load remote file for " + baseFile); // NOI18N
            failure.initCause(e);
            throw failure;
        }
    }

    public boolean cancel() {
        if(client != null) {
            client.cancel();
        }
        return true;
    }

}
