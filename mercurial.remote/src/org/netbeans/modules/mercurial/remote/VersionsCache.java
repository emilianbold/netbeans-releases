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

package org.netbeans.modules.mercurial.remote;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.netbeans.modules.mercurial.remote.ui.log.HgLogMessage.HgRevision;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.historystore.Storage;
import org.netbeans.modules.versioning.historystore.StorageManager;

/**
 * File revisions cache. It can access pristine files.
 *
 * XXX and what exactly is cached here?!
 * 
 * 
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
     * @return null if the file does not exist in given revision
     */
    public VCSFileProxy getFileRevision(VCSFileProxy base, HgRevision revision) throws IOException {
        return getFileRevision(base, revision, true);
    }
    
    public VCSFileProxy getFileRevision(VCSFileProxy base, HgRevision revision, boolean tryHard) throws IOException {
        String revisionNumber = revision.getRevisionNumber();
        if("-1".equals(revisionNumber)) { //NOI18N
            return null; // NOI18N
        }
        
        VCSFileProxy repository = Mercurial.getInstance().getRepositoryRoot(base);
        if (HgRevision.CURRENT.equals(revision)) {
            return base;
        } else {
            try {
                VCSFileProxy tempFile = VCSFileProxy.createFileProxy(VCSFileProxySupport.getTempFolder(repository, true), "nb-hg-" + base.getName()); //NOI18N
                if (HgRevision.BASE.equals(revision)) {
                    HgCommand.doCat(repository, base, tempFile, null);
                } else {
                    if ("false".equals(System.getProperty("versioning.mercurial.historycache.enable", "true"))) { //NOI18N
                        HgCommand.doCat(repository, base, tempFile, revisionNumber, null);
                    } else {
                        String changesetId = revision.getChangesetId();
                        Storage cachedVersions = StorageManager.getInstance().getStorage(repository.getPath());
                        String relativePath = HgUtils.getRelativePath(base);
                        File cachedFile = cachedVersions.getContent(relativePath, base.getName(), changesetId);
                        if (cachedFile.length() == 0) { // not yet cached
                            HgCommand.doCat(repository, base, tempFile, revisionNumber, null, tryHard);
                            if (VCSFileProxySupport.length(tempFile) != 0) {
                                cachedVersions.setContent(relativePath, changesetId, tempFile.getInputStream(false));
                            }
                        } else {
                            VCSFileProxySupport.copyStreamToFile(new BufferedInputStream(new FileInputStream(cachedFile)), tempFile);
                        }
                    }
                }
                if (VCSFileProxySupport.length(tempFile) == 0) {
                    VCSFileProxySupport.delete(tempFile);
                    return null;
                }
                return tempFile;
            } catch (HgException e) {
                throw new IOException(e);
            }
        }
    }
}
