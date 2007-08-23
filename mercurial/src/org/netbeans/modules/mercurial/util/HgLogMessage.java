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
 * "Portions Copyrighted [year] [name of copyright owner]" // NOI18N
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.mercurial.util;

import java.util.*;

/**
 * A class to encapsulate the parts of the hg log message we are interested in.
 *
 * @author Padraig O'Briain
 */
public class HgLogMessage {

    private long    revision;
    private Date    date;
    private String  commitMessage;
    private String  changeSet;
    private String  timeZoneOffset;
    
    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public String getChangeSet() {
        return changeSet;
    }

    public void setChangeSet(String changeSet) {
        this.changeSet = changeSet;
    }

    public String getTimeZoneOffset() {
        return timeZoneOffset;
    }

    public void setTimeZoneOffset(String timeZoneOffset) {
        this.timeZoneOffset = timeZoneOffset;
    }

    /**
     * Returns the revision of this log message.
     */
    public Long getRevision() {
        return revision;
    }

    /**
     * Sets the revision of this log message.
     */
    public void setRevision(Long revision) {
        this.revision = revision;
    }

    /**
     * Returns the date of this log message.
     */
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
