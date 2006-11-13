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
        if (Setup.REVISION_BASE.equals(revision)) {
            try {
                File svnDir = getMetadataDir(base.getParentFile());
                if (svnDir == null) return null;
                File svnBase = new File(svnDir, "text-base/" + base.getName() + ".svn-base");
                if (!svnBase.exists()) return null;
                File expanded = new File(svnDir, "text-base/" + base.getName() + ".netbeans-base");
                if (expanded.canRead() && svnBase.isFile() && expanded.lastModified() > svnBase.lastModified()) {
                    return expanded;
                }
                SvnClient client = Subversion.getInstance().getClient(base);
                InputStream in = client.getContent(base, SVNRevision.BASE);
                expanded = FileUtil.normalizeFile(expanded);
                expanded.deleteOnExit();
                FileUtils.copyStreamToFile(new BufferedInputStream(in), expanded);
                return expanded;
            } catch (SVNClientException e) {
                return null;
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
            SVNRevision svnrevision;
            if (Setup.REVISION_HEAD.equals(revision)) {
                svnrevision = SVNRevision.HEAD;
            } else {
                svnrevision = new SVNRevision.Number(Long.parseLong(revision));
            }
            try {
                SvnClient client = Subversion.getInstance().getClient(base);
                FileStatusCache cache = Subversion.getInstance().getStatusCache();
                InputStream in;
                if ((cache.getStatus(base).getStatus() & FileInformation.STATUS_VERSIONED) != 0)  {
                    in = client.getContent(base, svnrevision);
                } else {
                    SVNUrl url = SvnUtils.getRepositoryUrl(base);
                    if (url != null) {
                        url = url.appendPath("@" + revision);
                        in = client.getContent(url, svnrevision);
                    } else {
                        in = new ByteArrayInputStream(org.openide.util.NbBundle.getMessage(VersionsCache.class, "MSG_UnknownURL").getBytes()); // NOI18N
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

        // TODO how to cache locally? In SVN there are no per file revisions
        // like in CVS, revision that comes here is repositoty revision
        //
        // Example:
        // unmodified file has many repository revisions
        // and effective caching should store just one version
        // (mapping all repository revisions to it)
        //
        // File caching is leveraged in Search History
    }

    private File getMetadataDir(File dir) {
        File svnDir = new File(dir, ".svn");  // NOI18N
        if (!svnDir.isDirectory()) {
            svnDir = new File(dir, "_svn");  // NOI18N
            if (!svnDir.isDirectory()) {
                return null;
            }
        }
        return svnDir;
    }
}
