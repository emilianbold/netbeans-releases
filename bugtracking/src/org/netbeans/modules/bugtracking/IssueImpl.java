/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import static java.lang.Character.isSpaceChar;
import java.util.Date;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.team.spi.OwnerInfo;
import org.netbeans.modules.bugtracking.spi.IssueController;
import org.netbeans.modules.bugtracking.spi.IssueScheduleInfo;
import org.netbeans.modules.bugtracking.spi.IssueSchedulingProvider;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.bugtracking.tasks.TaskSchedulingManager;
import org.netbeans.modules.bugtracking.ui.issue.IssueAction;

/**
 *
 * @author Tomas Stupka
 */
public final class IssueImpl<R, I> {
    /** 
     * public for testing purposes
     */
    public static final int SHORT_DISP_NAME_LENGTH = 15;
    
    public static final String EVENT_ISSUE_DATA_CHANGED = IssueProvider.EVENT_ISSUE_DATA_CHANGED;

    private Issue issue;
    private final RepositoryImpl<R, ?, I> repo;
    private final IssueProvider<I> issueProvider;
    private final I data;
    private String fakeId;

    IssueImpl(RepositoryImpl<R, ?, I> repo, IssueProvider<I> issueProvider, I data) {
        this.issueProvider = issueProvider;
        this.data = data;
        this.repo = repo;
        
        issueProvider.addPropertyChangeListener(data, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(IssueProvider.EVENT_ISSUE_DATA_CHANGED.equals(evt.getPropertyName())) {
                    handleScheduling();
                }
            }
        });
        handleScheduling();
    }

    public synchronized Issue getIssue() {
        if(issue == null) {
            issue = APIAccessor.IMPL.createIssue(this);
        }
        return issue;
    }
    
    /**
     * Opens this issue in the IDE
     */
    public void open() {
        IssueAction.openIssue(this);
    }

    /**
     * Returns a short variant of the display name. The short variant is used
     * in cases where the full display name might be too long, such as when used
     * as a title of a tab. The default implementation uses the
     * the {@linkplain #getDisplayName full display name} as a base and trims
     * it to maximum of {@value #SHORT_DISP_NAME_LENGTH} characters if
     * necessary. If it was necessary to trim the name (i.e. if the full name
     * was longer then {@value #SHORT_DISP_NAME_LENGTH}), then an ellipsis
     * is appended to the end of the trimmed display name.
     *
     * @return  short variant of the display name
     * @see #getDisplayName
     */
    public String getShortenedDisplayName() {
        String displayName = getDisplayName();

        int length = displayName.length();
        int limit = SHORT_DISP_NAME_LENGTH;

        if (length <= limit) {
            return displayName;
        }

        String trimmed = displayName.substring(0, limit).trim();

        StringBuilder buf = new StringBuilder(limit + 4);
        buf.append(trimmed);
        if ((length > (limit + 1)) && isSpaceChar(displayName.charAt(limit))) {
            buf.append(' ');
        }
        buf.append("...");                                              //NOI18N

        return buf.toString();
    }

    public RepositoryImpl getRepositoryImpl() {
        return repo;
    }

    IssueProvider getProvider() {
        return issueProvider;
    }

    public String getID() {
        String id = issueProvider.getID(data);
        if(id == null) {
            synchronized(repo) {
                if(fakeId == null) {
                    fakeId = repo.getNextFakeIssueID();
                }
                return fakeId;
            }
        }
        return id;
    }
    public String getSummary() {
        return issueProvider.getSummary(data);
    }
    public String getTooltip() {
        return issueProvider.getTooltip(data);
    }

    public void attachFile(File file, String description, boolean isPatch) {
        issueProvider.attachFile(data, file, description, isPatch);
    }

    public void addComment(String comment, boolean closeAsFixed) {
        issueProvider.addComment(data, comment, closeAsFixed);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        issueProvider.addPropertyChangeListener(data, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        issueProvider.removePropertyChangeListener(data, listener);
    }

    public boolean refresh() {
        return issueProvider.refresh(data);
    }

    public boolean isNew() {
        return issueProvider.isNew(data);
    }

    public boolean isFinished() {
        return issueProvider.isFinished(data);
    }
    
    public String getDisplayName() {
        return issueProvider.getDisplayName(data);
    }

    public void setContext(OwnerInfo info) {
        repo.setIssueContext(data, info);
    }

    public IssueController getController() {
        return issueProvider.getController(data);
    }    
    
    public boolean isData(Object obj) {
        return data == obj;
    }

    public IssueStatusProvider.Status getStatus() {
        IssueStatusProvider<R, I> sp = repo.getStatusProvider();
        if(sp == null) {
            return IssueStatusProvider.Status.SEEN;
        } 
        return sp.getStatus(data);
    }

    public void addIssueStatusListener(PropertyChangeListener l) {
        IssueStatusProvider<R, I> sp = repo.getStatusProvider();
        if(sp == null) {
            return;
        }
        sp.addPropertyChangeListener(data, l);
    }

    public void removeIssueStatusListener(PropertyChangeListener l) {
        IssueStatusProvider<R, I> sp = repo.getStatusProvider();
        if(sp == null) {
            return;
        }
        sp.removePropertyChangeListener(data, l);
    }

    public void setSeen(boolean isUptodate) {
        IssueStatusProvider<R, I> sp = repo.getStatusProvider();
        if(sp == null) {
            return;
        }
        sp.setSeenIncoming(data, isUptodate);
    }
    
    public boolean submit () {
        IssueStatusProvider<R, I> sp = repo.getStatusProvider();
        if(sp == null) {
            return false;
        }
        return sp.submit(data);
    }

    public void discardChanges() {
        IssueStatusProvider<R, I> sp = repo.getStatusProvider();
        if(sp == null) {
            return;
        }
        sp.discardOutgoing(data);
    }

    public boolean hasSchedule() {
        IssueSchedulingProvider<I> isp = repo.getSchedulingProvider();
        return isp != null;
    }
    
    
    public void setDueDate(Date date) {
        IssueSchedulingProvider<I> isp = repo.getSchedulingProvider();
        assert isp != null : "do no call .setDueDate() if .hasSchedule() is false"; // NOI18N
        if(isp != null) {
            isp.setDueDate(data, date);
        }
    }

    public void setSchedule(IssueScheduleInfo info) {
        IssueSchedulingProvider<I> isp = repo.getSchedulingProvider();
        assert isp != null : "do no call .setSchedule() if .hasSchedule() is false"; // NOI18N
        if(isp != null) {
            isp.setSchedule(data, info);
        }
    }

    public void setEstimate(int hours) {
        IssueSchedulingProvider<I> isp = repo.getSchedulingProvider();
        assert isp != null : "do no call .setEstimate() if .hasSchedule() is false"; // NOI18N
        if(isp != null) {
            isp.setEstimate(data, hours);
        }
    }

    public Date getDueDate() {
        IssueSchedulingProvider<I> isp = repo.getSchedulingProvider();
        return isp != null ? isp.getDueDate(data) : null;
    }

    public IssueScheduleInfo getSchedule() {
        IssueSchedulingProvider<I> isp = repo.getSchedulingProvider();
        return isp != null ? isp.getSchedule(data) : null;
    }

    public int getEstimate() {
        IssueSchedulingProvider<I> isp = repo.getSchedulingProvider();
        return isp != null ? isp.getEstimate(data) : null;
    }
    
    public boolean providesPriority() {
        return repo.getPriorityProvider() != null;
    }
    
    public String getPriority() {
        return repo.getPriorityName(data);
    }
    
    public Image getPriorityIcon() {
        return repo.getPriorityIcon(data);
    }

    private void handleScheduling () {
        TaskSchedulingManager.getInstance().handleTask(IssueImpl.this);
    }
    
}
