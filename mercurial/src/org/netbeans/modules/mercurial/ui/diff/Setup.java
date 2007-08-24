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

package org.netbeans.modules.mercurial.ui.diff;

import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.diff.DiffView;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.util.NbBundle;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Exception;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.text.MessageFormat;

/**
 * Represents on DIFF setup.
 *
 * @author Maros Sandor
 */
public final class Setup {

    /**
     * What was locally changed? The right pane contains local file.
     *
     * <p>Local addition, removal or change is displayed in
     * the right pane as addition, removal or change respectively
     * (i.e. not reversed as removal, addition or change).
     *
     * <pre>
     * diff from-BASE to-LOCAL
     * </pre>
     */
    public static final int DIFFTYPE_LOCAL     = 0;

    /**
     * What was remotely changed? The right pane contains remote file.
     *
     * <p>Remote addition, removal or change is displayed in
     * the right pane as addition, removal or change respectively
     * (i.e. not reversed as removal, addition or change).
     *
     * <pre>
     * diff from-BASE to-HEAD
     * </pre>
     */
    public static final int DIFFTYPE_REMOTE    = 1;

    /**
     * What was locally changed comparing to recent head?
     * The Right pane contains local file.
     *
     * <p> Local addition, removal or change is displayed in
     * the right pane as addition, removal or change respectively
     * (i.e. not reversed as removal, addition or change).
     *
     * <pre>
     * diff from-HEAD to-LOCAL
     * </pre>
     */
    public static final int DIFFTYPE_ALL       = 2;
    
    public static final String REVISION_PRISTINE = "PRISTINE"; // NOI18N
    public static final String REVISION_BASE = "BASE"; // NOI18N
    public static final String REVISION_CURRENT = "LOCAL"; // NOI18N
    public static final String REVISION_HEAD    = "HEAD"; // NOI18N
    
    private final File      baseFile;
    private final String    firstRevision;
    private final String    secondRevision;
    private String          changeSetId;

    private DiffStreamSource    firstSource;
    private DiffStreamSource    secondSource;

    private DiffView view;

    private String    title;

    public Setup(File baseFile, int type) {
        this.baseFile = baseFile;
        FileInformation info = Mercurial.getInstance().getFileStatusCache().getStatus(baseFile);
        int status = info.getStatus();
        
        ResourceBundle loc = NbBundle.getBundle(Setup.class);
        String firstTitle;
        String secondTitle;

        try {
            File repository = HgUtils.getRootFile(HgUtils.getCurrentContext(null));
            changeSetId = HgCommand.getLastChangeSetId(repository, baseFile);
            if (changeSetId == null) {
                changeSetId = org.openide.util.NbBundle.getMessage(org.netbeans.modules.mercurial.ui.diff.Setup.class, "CTL_Setup_NotCommitted"); // NOI18N
            }
        } catch (HgException ex) {
            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
            DialogDisplayer.getDefault().notifyLater(e);
        }

        // the first source

        switch (type) {
            case DIFFTYPE_LOCAL:           

                // from-BASE

                if (match(status, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY
                | FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
                    firstRevision = REVISION_BASE;

                    firstTitle = loc.getString("MSG_DiffPanel_LocalNew"); // NOI18N
                } else if (match (status, FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
                    firstRevision = null;
                    firstTitle = NbBundle.getMessage(Setup.class, "LBL_Diff_NoLocalFile"); // NOI18N
                } else if (match(status, FileInformation.STATUS_VERSIONED_DELETEDLOCALLY
                | FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
                    firstRevision = REVISION_BASE;
                    firstTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_BaseRevision"), new Object [] { changeSetId }); // NOI18N
                } else {
                    firstRevision = REVISION_BASE;
                    firstTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_BaseRevision"), new Object [] { changeSetId }); // NOI18N
                }

                break;

            default:
                throw new IllegalArgumentException("Unknow diff type: " + type); // NOI18N
        }


        // the second source

        switch (type) {
            case DIFFTYPE_LOCAL:

                // to-LOCAL

                if (match(status, FileInformation.STATUS_VERSIONED_CONFLICT)) {
                    secondRevision = REVISION_CURRENT;
                    secondTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_LocalConflict"), new Object [] { changeSetId }); // NOI18N
                } else if (match(status, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY
                | FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
                    secondRevision = REVISION_CURRENT;
                    secondTitle = loc.getString("MSG_DiffPanel_LocalNew"); // NOI18N
                } else if (match (status, FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
                    secondRevision = null;
                    secondTitle = NbBundle.getMessage(Setup.class, "LBL_Diff_NoLocalFile"); // NOI18N
                } else if (match(status, FileInformation.STATUS_VERSIONED_DELETEDLOCALLY
                | FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
                    secondRevision = null;
                    secondTitle = loc.getString("MSG_DiffPanel_LocalDeleted"); // NOI18N
                } else {
                    secondRevision = REVISION_CURRENT;
                    secondTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_LocalModified"), new Object [] { changeSetId }); // NOI18N
                }
                break;

            default:
                throw new IllegalArgumentException("Unknow diff type: " + type); // NOI18N
        }

        firstSource = new DiffStreamSource(baseFile, firstRevision, firstTitle);
        secondSource = new DiffStreamSource(baseFile, secondRevision, secondTitle);
        title = "<html>" + Mercurial.getInstance().getMercurialAnnotator().annotateNameHtml(baseFile, info); // NOI18N
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

    public File getBaseFile() {
        return baseFile;
    }

    public void setView(DiffView view) {
        this.view = view;
    }

    public DiffView getView() {
        return view;
    }

    public StreamSource getFirstSource() {
        return firstSource;
    }

    public StreamSource getSecondSource() {
        return secondSource;
    }

    public String toString() {
        return title;
    }

    /**
     * Loads data
     * @param group that carries shared state. Note that this group must not be executed later on. 
     */
    void initSources() throws IOException {
        if (firstSource != null) firstSource.init();
        if (secondSource != null) secondSource.init();
    }

    private static boolean match(int status, int mask) {
        return (status & mask) != 0;
    }
}
