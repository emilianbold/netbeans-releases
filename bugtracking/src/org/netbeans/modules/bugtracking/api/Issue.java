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
package org.netbeans.modules.bugtracking.api;

import java.beans.PropertyChangeListener;
import java.io.File;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import static java.lang.Character.isSpaceChar;
import org.netbeans.modules.bugtracking.ui.issue.IssueAction;
import org.openide.nodes.Node;

/**
 *
 * @author Tomas Stupka
 */
public final class Issue {
    private static final int SHORT_DISP_NAME_LENGTH = 15;
    
    private Bind<?> bind;
    private final Repository repo;

    <I> Issue(Repository repo, IssueProvider<I> issueProvider, I data) {
        this.bind = new Bind(issueProvider, data);
        this.repo = repo;
    }

    public String getID() {
        return bind.getID();
    }

    public String getSummary() {
        return bind.getSummary();
    }

    public String getTooltip() {
        return bind.getTooltip();
    }

    public void attachPatch(File file, String description) {
        bind.attachPatch(file, description);
    }

    public void addComment(String comment, boolean closeAsFixed) {
        bind.addComment(comment, closeAsFixed);
    }

    /**
     * Opens this issue in the IDE
     */
    public void open() {
        IssueAction.openIssue(this, false);
    }

    /**
     * Opens the issue with the given issueId in the IDE. In case that issueId
     * is null a new issue wil be created.
     *
     * @param repository
     * @param issueId
     */
    public static void open(final Repository repository, final String issueId) {
        if(issueId == null) {
            IssueAction.createIssue(repository);
        } else {            
            IssueAction.openIssue(repository, issueId);
        }
    }

    /**
     * Opens this issue in the IDE
     * @param refresh also refreshes the issue after opening
     *
     */
    public final void open(final boolean refresh) {
        IssueAction.openIssue(this, refresh);
    }    
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        bind.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        bind.removePropertyChangeListener(listener);
    }

    public boolean refresh() {
        return bind.refresh();
    }

    public boolean isNew() {
        return bind.isNew();
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

    public String getDisplayName() {
        return bind.getDisplayName();
    }

    public Repository getRepository() {
        return repo;
    }

    IssueProvider getProvider() {
        return bind.issueProvider;
    }

    Object getData() {
        return bind.data;
    }

    void setContext(Node[] context) {
        bind.setContext(context);
    }
    
    private final class Bind<I> {
        private final IssueProvider<I> issueProvider;
        private final I data;

        public Bind(IssueProvider<I> issueProvider, I data) {
            this.issueProvider = issueProvider;
            this.data = data;
        }
        
        public String getID() {
            return issueProvider.getID(data);
        }
        public String getSummary() {
            return issueProvider.getSummary(data);
        }
        public String getTooltip() {
            return issueProvider.getTooltip(data);
        }

        public void attachPatch(File file, String description) {
            issueProvider.attachPatch(data, file, description);
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

        public String getDisplayName() {
            return issueProvider.getDisplayName(data);
        }

        public Repository getRepository() {
            return repo;
        }

        private void setContext(Node[] context) {
            issueProvider.setContext(data, context);
        }
        
    }
    
}
