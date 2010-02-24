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

package org.netbeans.modules.versioning.system.cvss.ui.actions.diff;

import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.diff.DiffController;
import org.netbeans.modules.versioning.system.cvss.*;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.openide.util.NbBundle;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.text.MessageFormat;
import org.netbeans.modules.versioning.diff.AbstractDiffSetup;

/**
 * Represents on DIFF setup.
 *
 * @author Maros Sandor
 */
public final class Setup extends AbstractDiffSetup {

    public static final int DIFFTYPE_LOCAL     = 0;
    public static final int DIFFTYPE_REMOTE    = 1;
    public static final int DIFFTYPE_ALL       = 2;
    
    public static final String REVISION_CURRENT = VersionsCache.REVISION_CURRENT;
    public static final String REVISION_HEAD    = VersionsCache.REVISION_HEAD;
    
    private final File      baseFile;
    private final String    firstRevision;
    private final String    secondRevision;
    private FileInformation info;

    private DiffStreamSource    firstSource;
    private DiffStreamSource    secondSource;

    private DiffController view;
    private DiffNode           node;

    private String    title;

    public Setup(File baseFile, int type) {
        this.baseFile = baseFile;
        info = CvsVersioningSystem.getInstance().getStatusCache().getStatus(baseFile);
        int status = info.getStatus();
        Entry entry = info.getEntry(baseFile);
        String revision = entry != null ? entry.getRevision() : null;
        if (revision != null && revision.charAt(0) == '-') revision = revision.substring(1);

        ResourceBundle loc = NbBundle.getBundle(Setup.class);
        String firstTitle;
        String secondTitle;
        if (type == DIFFTYPE_ALL && status == FileInformation.STATUS_VERSIONED_MERGE) {
            firstRevision = REVISION_HEAD;
            firstTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_RemoteModified"), new Object [] { revision });
        } else if (
                status == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY ||
                status == FileInformation.STATUS_VERSIONED_ADDEDLOCALLY ||
                status == FileInformation.STATUS_VERSIONED_NEWINREPOSITORY
        ) {
            firstRevision = null;
            firstTitle = loc.getString("MSG_DiffPanel_NoBaseRevision");
        } else {
            firstRevision = revision;
            firstTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_BaseRevision"), new Object [] { revision });
        }

        if (status == FileInformation.STATUS_VERSIONED_CONFLICT) {
            secondRevision = REVISION_CURRENT;
            secondTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_LocalConflict"), new Object [] { revision });
        } else if (status == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY || status == FileInformation.STATUS_VERSIONED_ADDEDLOCALLY) {
            secondRevision = REVISION_CURRENT;
            secondTitle = loc.getString("MSG_DiffPanel_LocalNew");
        } else if (status == FileInformation.STATUS_VERSIONED_DELETEDLOCALLY || status == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY) {
            secondRevision = null;
            secondTitle = loc.getString("MSG_DiffPanel_LocalDeleted");
        } else if (status == FileInformation.STATUS_VERSIONED_NEWINREPOSITORY) {
            secondRevision = REVISION_HEAD;
            secondTitle = loc.getString("MSG_DiffPanel_RemoteNew");
        } else if (status == FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY) {
            secondRevision = null;
            secondTitle = loc.getString("MSG_DiffPanel_RemoteDeleted");
        } else if (status == FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY) {
            secondRevision = REVISION_HEAD;
            secondTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_RemoteModified"), new Object [] { revision });
        } else if (type == DIFFTYPE_REMOTE) {
            secondRevision = REVISION_HEAD;
            secondTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_RemoteModified"), new Object [] { revision });
        } else {
            secondRevision = REVISION_CURRENT;
            secondTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_LocalModified"), new Object [] { revision });
        }

        firstSource = new DiffStreamSource(baseFile, firstRevision, firstTitle);
        secondSource = new DiffStreamSource(baseFile, secondRevision, secondTitle);
        title = "<html>" + CvsVersioningSystem.getInstance().getAnnotator().annotateNameHtml(baseFile, info); // NOI18N
    }

    /**
     * Text file setup for arbitrary revisions.
     * @param firstRevision first revision or <code>null</code> for inital.
     * @param secondRevision second revision
     */
    public Setup(File baseFile, String firstRevision, String secondRevision) {
        this.baseFile = baseFile;
        this.firstRevision = firstRevision;
        this.secondRevision = secondRevision;
        firstSource = new DiffStreamSource(baseFile, firstRevision, firstRevision);
        secondSource = new DiffStreamSource(baseFile, secondRevision, secondRevision);
    }

    public FileInformation getInfo() {
        return info;
    }

    DiffNode getNode() {
        return node;
    }

    void setNode(DiffNode node) {
        this.node = node;
    }

    public File getBaseFile() {
        return baseFile;
    }

    public void setView(DiffController view) {
        this.view = view;
    }

    public DiffController getView() {
        return view;
    }

    public StreamSource getFirstSource() {
        return firstSource;
    }

    public StreamSource getSecondSource() {
        return secondSource;
    }

    @Override
    public String toString() {
        return title;
    }

    static String getDisplayedRevision(File baseFile, String revision) {
        if (revision == REVISION_CURRENT) {
            FileInformation info = CvsVersioningSystem.getInstance().getStatusCache().getStatus(baseFile);
            return NbBundle.getMessage(Setup.class, "MSG_LocalRevision", info.getEntry(baseFile).getRevision());
        } else {
            return revision;
        }
    }

    /**
     * Loads data over network
     * @param group that carries shared state. Note that this group must not be executed later on. 
     */
    void initSources(ExecutorGroup group) throws IOException {
        if (firstSource != null) firstSource.init(group);
        if (secondSource != null) secondSource.init(group);
    }
}
