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

package org.netbeans.modules.subversion;

import java.io.*;
import org.netbeans.modules.subversion.client.*;
import org.netbeans.modules.subversion.ui.diff.Setup;
import org.netbeans.modules.subversion.util.*;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.FileUtils;
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
     */
    public File getFileRevision(File base, String revision) throws IOException {
        if (Setup.REVISION_BASE.equals(revision)) {
            String name = base.getName();
            File dir = base.getParentFile();
            // XXX _svn
            File svnDir = new File(dir, ".svn");  // NOI18N
            if (svnDir.isDirectory()) {
                File text_base = new File(svnDir, "text-base"); // NOI18N
                File pristine = new File(text_base, name + ".svn-base"); // NOI18N
                return pristine;
            }
        } else if (Setup.REVISION_CURRENT.equals(revision)) {
            return base;
        } else if (Setup.REVISION_HEAD.equals(revision)) {
            try {
                SvnClient client = Subversion.getInstance().getClient(base);
                FileStatusCache cache = Subversion.getInstance().getStatusCache();
                InputStream in;
                if ((cache.getStatus(base).getStatus() & FileInformation.STATUS_VERSIONED) != 0)  {
                    in = client.getContent(base, SVNRevision.HEAD);
                } else {
                    SVNUrl url = SvnUtils.getRepositoryUrl(base);
                    if (url != null) {
                        in = client.getContent(url, SVNRevision.HEAD);
                    } else {
                        in = new ByteArrayInputStream("[Unknown repository URL]".getBytes());
                    }
                }
                // keep original extension so MIME can be guessed by the extension
                File tmp = File.createTempFile("nb-svn", base.getName());  // NOI19N
                tmp.deleteOnExit();  // hard to track actual lifetime
                FileUtils.copyStreamToFile(new BufferedInputStream(in), tmp);
                return tmp;
            } catch (SVNClientException ex) {
                IOException ioex = new IOException("Can not load: " + base.getAbsolutePath() + " in revision: " + revision);
                ioex.initCause(ex);
                throw ioex;
            }
        }
        // TODO how to cache locally? There are no per file revisions
        assert false: "Not implemented. Can not load revision: " + revision;
        return null;
    }
}
