/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.subversion;

import java.io.*;
import org.netbeans.modules.subversion.client.*;
import org.netbeans.modules.subversion.ui.diff.Setup;
import org.netbeans.modules.subversion.util.*;
import org.netbeans.modules.subversion.util.FileUtils;
import org.openide.filesystems.FileUtil;
import org.tigris.subversion.svnclientadapter.*;

/**
 * File revisions cache. It can access pristine files.
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
     * Loads a content of the given file in the given revision. May communicate over network.
     * @param repoUrl target repository URL
     * @param url target file full url
     * @param revision target revision, will be used also as the peg revision
     * @param fileName basename of the file which will be used as a temporary file's name
     * @return a temporary file with given file's content
     * @throws java.io.IOException
     */
    public File getFileRevision(SVNUrl repoUrl, SVNUrl url, String revision, String fileName) throws IOException {
        return getFileRevision(repoUrl, url, revision, revision, fileName);
    }
    /**
     * Loads a content of the given file in the given revision. May communicate over network.
     * @param repoUrl target repository URL
     * @param url target file full url
     * @param revision target revision
     * @param pegRevision peg revision
     * @param fileName basename of the file which will be used as a temporary file's name
     * @return a temporary file with given file's content
     * @throws java.io.IOException
     */
    public File getFileRevision(SVNUrl repoUrl, SVNUrl url, String revision, String pegRevision, String fileName) throws IOException {
        try {
            SvnClient client = Subversion.getInstance().getClient(repoUrl);
            InputStream in = getInputStream(client, url, revision, pegRevision);
            return createContent(fileName, in);
        } catch (SVNClientException ex) {
            IOException ioex = new IOException("Can not load: " + url + " in revision: " + revision); // NOI18N
            ioex.initCause(ex);
            throw ioex;
        }
    }

    /**
     * Loads the file in specified revision.
     * For peg revision <code>revision</code> will be used for existing files, for repository files it will be the HEAD revision.
     * <p>It's may connect over network I/O do not
     * call from the GUI thread.</p>
     *
     * @return null if the file does not exit in given revision
     */
    public File getFileRevision(File base, String revision) throws IOException {
        return getFileRevision(base, revision, revision);
    }

    /**
     * Loads the file in specified revision.
     *
     * <p>It's may connect over network I/O do not
     * call from the GUI thread.
     *
     * @return null if the file does not exit in given revision
     */
    public File getFileRevision(File base, String revision, String pegRevision) throws IOException {
        try {
            SvnClientFactory.checkClientAvailable();
        } catch (SVNClientException e) {
            return null;
        }
        if (Setup.REVISION_BASE.equals(revision)) {
            return getBaseRevisionFile(base);
        } else if (Setup.REVISION_PRISTINE.equals(revision)) {
            String name = base.getName();
            File svnDir = getMetadataDir(base.getParentFile());
            if (svnDir != null) {
                File text_base = new File(svnDir, "text-base"); // NOI18N
                File pristine = new File(text_base, name + ".svn-base"); // NOI18N
                if (pristine.isFile()) {
                    return pristine;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else if (Setup.REVISION_CURRENT.equals(revision)) {
            return base;
        } else {
            SVNRevision svnrevision = SvnUtils.toSvnRevision(revision);
            try {
                SvnClient client = Subversion.getInstance().getClient(base);
                FileStatusCache cache = Subversion.getInstance().getStatusCache();
                InputStream in;
                try {
                    if ((cache.getStatus(base).getStatus() & FileInformation.STATUS_VERSIONED) != 0)  {
                        in = client.getContent(base, svnrevision);
                    } else {
                        SVNUrl url = SvnUtils.getRepositoryUrl(base);
                        if (url != null) {
                            in = getInputStream(client, url, revision, pegRevision);
                        } else {
                            in = new ByteArrayInputStream(org.openide.util.NbBundle.getMessage(VersionsCache.class, "MSG_UnknownURL").getBytes()); // NOI18N
                        }                
                    }
                } catch (SVNClientException e) {
                    if(SvnClientExceptionHandler.isFileNotFoundInRevision(e.getMessage()) ||
                            SvnClientExceptionHandler.isPathNotFound(e.getMessage())) {
                        in = new ByteArrayInputStream(new byte[] {});
                    } else {
                        throw e;
                    }
                }
                return createContent(base.getName(), in);
            } catch (SVNClientException ex) {
                IOException ioex = new IOException("Can not load: " + base.getAbsolutePath() + " in revision: " + revision); // NOI18N
                ioex.initCause(ex);
                throw ioex;
            }
        }
    }

    /**
     * Returns content of the given file at its base revision.
     * In other words, returns content of the given file without local
     * modifications.
     * The returned {@code File} may be deleted after use.
     * The returned {@code File} is <em>not</em> scheduled for automatic
     * deletion upon JVM shutdown.
     *
     * @param  referenceFile  reference file
     * @return  file holding content of the unmodified version of the given file
     * @exception  java.io.IOException  if some file handling operation failed
     */
    File getBaseRevisionFile(File referenceFile) throws IOException {
        try {
            File svnDir = getMetadataDir(referenceFile.getParentFile());
            if (svnDir == null) {
                return null;
            }
            File svnBase = new File(svnDir, "text-base/" + referenceFile.getName() + ".svn-base"); //NOI18N
            if (!svnBase.exists()) {
                return null;
            }
            File expanded = new File(svnDir, "text-base/" + referenceFile.getName() + ".netbeans-base"); //NOI18N
            if (expanded.canRead() && svnBase.isFile() && (expanded.lastModified() >= svnBase.lastModified())) {
                return expanded;
            }
            SvnClient client = Subversion.getInstance().getClient(referenceFile);
            InputStream in = client.getContent(referenceFile, SVNRevision.BASE);
            expanded = FileUtil.normalizeFile(expanded);
            FileUtils.copyStreamToFile(new BufferedInputStream(in), expanded);
            expanded.setLastModified(svnBase.lastModified());
            return expanded;
        } catch (SVNClientException e) {
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
        }
    }

    /**
     * Tries to acquire a content of the given file url in the given revision
     * @param client
     * @param url
     * @param revision
     * @param pegRevision 
     * @return content of the file in the given revision
     * @throws org.tigris.subversion.svnclientadapter.SVNClientException
     */
    private InputStream getInputStream (SvnClient client, SVNUrl url, String revision, String pegRevision) throws SVNClientException {
        InputStream in = null;
        try {
            in = client.getContent(url, SvnUtils.toSvnRevision(revision), SvnUtils.toSvnRevision(pegRevision));
        } catch (SVNClientException e) {
            if (SvnClientExceptionHandler.isFileNotFoundInRevision(e.getMessage()) ||
                    SvnClientExceptionHandler.isPathNotFound(e.getMessage())) {
                in = new ByteArrayInputStream(new byte[]{});
            } else {
                throw e;
            }
        }
        return in;
    }

    /**
     * Creates a temporary file and prints the content of <code>in</code> to it.
     * @param fileName temporary file name, will be prefixed by <code>"nb-svn"</code>
     * @param in content to be printed
     * @return created temporary file
     * @throws java.io.IOException
     */
    private File createContent (String fileName, InputStream in) throws IOException {
        // keep original extension so MIME can be guessed by the extension
        File tmp = File.createTempFile("nb-svn", fileName);  // NOI18N
        tmp = FileUtil.normalizeFile(tmp);
        tmp.deleteOnExit();  // hard to track actual lifetime
        FileUtils.copyStreamToFile(new BufferedInputStream(in), tmp);
        return tmp;
    }

    private File getMetadataDir(File dir) {
        File svnDir = new File(dir, SvnUtils.SVN_ADMIN_DIR);  // NOI18N
        if (!svnDir.isDirectory()) {
            return null;
        }
        return svnDir;
    }
}
