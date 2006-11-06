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

package org.netbeans.modules.subversion.client.parser;

import java.io.File;
import java.lang.UnsupportedOperationException;
import java.net.MalformedURLException;
import java.util.Date;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Ed Hillmann
 */
public class ParserSvnStatus implements ISVNStatus {

    private File file = null;
    private SVNUrl url = null;
    private SVNRevision.Number revision = null;
    private SVNNodeKind kind = null;
    private SVNStatusKind textStatus = null;
    private SVNStatusKind propStatus = null;
    private String lastCommitAuthor = null;
    private SVNRevision.Number lastChangedRevision = null;
    private Date lastChangedDate = null;
    private boolean isCopied = false;
    private SVNUrl urlCopiedFrom = null;
    private File conflictNew = null;
    private File conflictOld = null;
    private File conflictWorking = null;
    private Date lockCreationDate = null;
    private String lockComment = null;
    private String lockOwner = null;

    /** Creates a new instance of LocalSvnStatusImpl */
    public ParserSvnStatus(File file, String url, long revision, String kind,
            String textStatus, String propStatus,
            String lastCommitAuthor, long lastChangedRevision, Date lastChangedDate,
            boolean isCopied, String urlCopiedFrom,
            File conflictNew, File conflictOld, File conflictWorking,
            Date lockCreationDate, String lockComment, String lockOwner) {

        this.file = file;

        if (url != null) {
            try {
                this.url = new SVNUrl(url);
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        this.revision = new SVNRevision.Number(revision);
        this.kind = SVNNodeKind.fromString(kind);

        this.textStatus = SVNStatusKind.fromString(textStatus);
        this.propStatus = SVNStatusKind.fromString(propStatus);
        this.lastCommitAuthor = lastCommitAuthor;

        this.lastChangedRevision = new SVNRevision.Number(lastChangedRevision);
        this.lastChangedDate = lastChangedDate;

        this.isCopied = isCopied;
        if (urlCopiedFrom != null) {
            try {
                this.urlCopiedFrom = new SVNUrl(urlCopiedFrom);
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        this.conflictNew = conflictNew;
        this.conflictOld = conflictOld;
        this.conflictWorking = conflictWorking;
        this.lockCreationDate = lockCreationDate;
        this.lockComment  = lockComment;
        this.lockOwner = lockOwner;
    }

    public boolean isCopied() {
        return isCopied;
    }

    public SVNUrl getUrlCopiedFrom() {
        return urlCopiedFrom;
    }

    public SVNUrl getUrl() {
        return url;
    }

    public SVNStatusKind getTextStatus() {
        return textStatus;
    }

    public SVNRevision.Number getRevision() {
        return revision;
    }

    public SVNStatusKind getRepositoryTextStatus() {
        throw new UnsupportedOperationException("getRepositoryTextStatus() is not implemented"); // NOI18N
    }

    public SVNStatusKind getRepositoryPropStatus() {
        throw new UnsupportedOperationException("getRepositoryPropStatus() is not implemented"); // NOI18N
    }

    public File getConflictNew() {
        return conflictNew;
    }

    public File getConflictOld() {
        return conflictOld;
    }

    public File getConflictWorking() {
        return conflictWorking;
    }

    public File getFile() {
        return file;
    }

    public Date getLastChangedDate() {
        return lastChangedDate;
    }

    public SVNRevision.Number getLastChangedRevision() {
        return lastChangedRevision;
    }

    public String getLastCommitAuthor() {
        return lastCommitAuthor;
    }

    public String getLockComment() {
        return lockComment;
    }

    public Date getLockCreationDate() {
        return lockCreationDate;
    }

    public String getLockOwner() {
        return lockOwner;
    }

    public SVNNodeKind getNodeKind() {
        return kind;
    }

    public String getPath() {
        return file.getPath();
    }

    public SVNStatusKind getPropStatus() {
        return propStatus;
    }

    public String getUrlString() {
        return url.toString();
    }

    public boolean isWcLocked() {
        // TODO implement me
        throw new UnsupportedOperationException("not implemented yet");             // NOI18N
    }

    public boolean isSwitched() {
        // TODO implement me
        throw new UnsupportedOperationException("not implemented yet");             // NOI18N
    }

}

