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
     * Loads the file in specified revision.
     *
     * <p>It's may connect over network I/O do not
     * call from the GUI thread.
     *
     * @return null if the file does not exit in given revision
     */
    public File getFileRevision(File base, String revision) throws IOException {
        try {
            SvnClientFactory.checkClientAvailable();
        } catch (SVNClientException e) {
            return null;
        }
        if (Setup.REVISION_BASE.equals(revision)) {
            try {
                File svnDir = getMetadataDir(base.getParentFile());
                if (svnDir == null) return null;
                File svnBase = new File(svnDir, "text-base/" + base.getName() + ".svn-base");
                if (!svnBase.exists()) return null;
                File expanded = new File(svnDir, "text-base/" + base.getName() + ".netbeans-base");
                if (expanded.canRead() && svnBase.isFile() && expanded.lastModified() >= svnBase.lastModified()) {
                    return expanded;
                }
                SvnClient client = Subversion.getInstance().getClient(base);
                InputStream in = client.getContent(base, SVNRevision.BASE);
                expanded = FileUtil.normalizeFile(expanded);
                expanded.deleteOnExit();
                FileUtils.copyStreamToFile(new BufferedInputStream(in), expanded);
                expanded.setLastModified(svnBase.lastModified());
                return expanded;
            } catch (SVNClientException e) {
                IOException ioe = new IOException();
                ioe.initCause(e);
                throw ioe;
            }
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
                            if(SvnClientFactory.isCLI()) {
                                // XXX why is the revision given twice ??? !!! CLI WORKAROUND?
                                // doesn't work with javahl but we won't change for cli as there might be some reason                                
                                url = url.appendPath("@" + revision);
                                in = client.getContent(url, svnrevision);
                            } else {
                                in = client.getContent(url, svnrevision, svnrevision);
                            }
                        } else {
                            in = new ByteArrayInputStream(org.openide.util.NbBundle.getMessage(VersionsCache.class, "MSG_UnknownURL").getBytes()); // NOI18N
                        }                
                    }
                } catch (SVNClientException e) {
                    if(SvnClientExceptionHandler.isFileNotFoundInRevision(e.getMessage())) {
                        in = new ByteArrayInputStream(new byte[] {});
                    } else {
                        throw e;
                    }
                }
                // keep original extension so MIME can be guessed by the extension
                File tmp = File.createTempFile("nb-svn", base.getName());  // NOI18N
                tmp = FileUtil.normalizeFile(tmp);
                tmp.deleteOnExit();  // hard to track actual lifetime
                FileUtils.copyStreamToFile(new BufferedInputStream(in), tmp);
                return tmp;
            } catch (SVNClientException ex) {
                IOException ioex = new IOException("Can not load: " + base.getAbsolutePath() + " in revision: " + revision); // NOI18N
                ioex.initCause(ex);
                throw ioex;
            }
        }
    }
    
    private File getMetadataDir(File dir) {
        File svnDir = new File(dir, SvnUtils.SVN_ADMIN_DIR);  // NOI18N
        if (!svnDir.isDirectory()) {
            return null;
        }
        return svnDir;
    }
}
