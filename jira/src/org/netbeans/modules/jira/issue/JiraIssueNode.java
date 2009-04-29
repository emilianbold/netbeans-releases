/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jira.issue;

import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.JiraStatus;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.IssueNode;
import org.netbeans.modules.bugtracking.spi.IssueNode.SeenProperty;
import org.netbeans.modules.jira.issue.NbJiraIssue.IssueField;
import org.openide.nodes.Node.Property;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class JiraIssueNode extends IssueNode {
    public JiraIssueNode(Issue issue) {
        super(issue);
    }

    NbJiraIssue getNbJiraIssue() {
        return (NbJiraIssue) super.getIssue();
    }

    @Override
    protected Property<?>[] getProperties() {
        return new Property<?>[] {
            new KeyProperty(),
            new TypeProperty(),
            new PriorityProperty(),
            new StatusProperty(),
            new ResolutionProperty(),
            new SummaryProperty(),
            new RecentChangesProperty(), // XXX move to issue node
            new SeenProperty() // XXX move to issue node
        };
    };

    @Override
    public void fireDataChanged() {
        super.fireDataChanged();
    }

    private Integer getTypeSortKey(String type) {
        // XXX sorting !!!
//        BugzillaConfiguration bc = getNbJiraIssue().getRepository().getConfiguration();
//        if(bc == null) {
//            return null;
//        }
//        List<String> s = bc.getSeverities();
//        if(s == null) {
//            return null;
//        }
//        return s.indexOf(severity);
        return 1;
    }

    private Integer getPrioritySortKey(String priority) {
//        BugzillaConfiguration bc = getNbJiraIssue().getRepository().getConfiguration();
//        if(bc == null) {
//            return null;
//        }
//        List<String> p = bc.getPriorities();
//        if(p == null) {
//            return null;
//        }
//        return p.indexOf(priority);
        return 1;
    }

    private Integer getResolutionSortKey(String resolution) {
//        BugzillaConfiguration bc = getNbJiraIssue().getRepository().getConfiguration();
//        if(bc == null) {
//            return null;
//        }
//        List<String> r = bc.getResolutions();
//        if(r == null) {
//            return null;
//        }
//        return r.indexOf(resolution);
        return 1;
    }

    private class KeyProperty extends IssueNode.IssueProperty<String> {
        public KeyProperty() {
            super(NbJiraIssue.LABEL_NAME_ID,
                  String.class,
                  NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_ID_Title"), // NOI18N
                  NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_ID_Desc")); // NOI18N
        }
        public String getValue() {
            return getNbJiraIssue().getFieldValue(IssueField.KEY);
        }
        @Override
        public int compareTo(IssueProperty p) {
            if(p == null) return 1;
            Integer i1 = Integer.parseInt(getIssue().getID());
            Integer i2 = Integer.parseInt(p.getIssue().getID());
            return i1.compareTo(i2);
        }
    }

    private class TypeProperty extends IssueNode.IssueProperty<String> {
        public TypeProperty() {
            super(NbJiraIssue.LABEL_NAME_TYPE,
                  String.class,
                  NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_Type_Title"), // NOI18N
                  NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_Type_Desc")); // NOI18N
        }
        public String getValue() {
            IssueType type = getNbJiraIssue().getType();
            return type != null ? type.getName() : "";                          // NOI18N
        }
        @Override
        public Object getValue(String attributeName) {
            if("sortkey".equals(attributeName)) {                               // NOI18N
                return getTypeSortKey(getNbJiraIssue().getFieldValue(IssueField.TYPE));
            } else {
                return super.getValue(attributeName);
            }
        }
    }

    private class PriorityProperty extends IssueProperty<String> {
        public PriorityProperty() {
            super(NbJiraIssue.LABEL_NAME_PRIORITY,
                  String.class,
                  NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_Priority_Title"), // NOI18N
                  NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_Priority_Desc")); // NOI18N
        }
        public String getValue() {
            Priority priority = getNbJiraIssue().getPriority();
            return priority != null ? priority.getName() : "";                  // NOI18N
        }
        @Override
        public Object getValue(String attributeName) {
            if("sortkey".equals(attributeName)) {                               // NOI18N
                return getPrioritySortKey(getNbJiraIssue().getFieldValue(IssueField.PRIORITY));
            } else {
                return super.getValue(attributeName);
            }
        }
    }

    private class StatusProperty extends IssueProperty<String> {
        public StatusProperty() {
            super(NbJiraIssue.LABEL_NAME_STATUS,
                  String.class,
                  NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_Status_Title"), // NOI18N
                  NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_Status_Desc")); // NOI18N
        }
        public String getValue() {
            JiraStatus status = getNbJiraIssue().getStatus();
            return status != null ? status.getName() : "";                      // NOI18N
        }
        @Override
        public int compareTo(IssueProperty p) {
            if(p == null) return 1;
            String s1 = getNbJiraIssue().getFieldValue(IssueField.STATUS);
            String s2 = ((NbJiraIssue)p.getIssue()).getFieldValue(IssueField.STATUS);
            return s1.compareTo(s2);
        }
    }

    private class ResolutionProperty extends IssueProperty<String> {
        public ResolutionProperty() {
            super(NbJiraIssue.LABEL_NAME_RESOLUTION,
                  String.class,
                  NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_Resolution_Title"), // NOI18N
                  NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_ID_Desc")); // NOI18N
        }
        public String getValue() {
            Resolution resolution = getNbJiraIssue().getResolution();
            return resolution != null ? resolution.getName() : "";              // NOI18N
        }
        @Override
        public Object getValue(String attributeName) {
            if("sortkey".equals(attributeName)) {                               // NOI18N
                return getResolutionSortKey(getNbJiraIssue().getFieldValue(IssueField.RESOLUTION));
            } else {
                return super.getValue(attributeName);
            }
        }
    }

    private class SummaryProperty extends IssueProperty<String> {
        public SummaryProperty() {
            super(NbJiraIssue.LABEL_NAME_SUMMARY,
                  String.class,
                  NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_Summary_Title"), // NOI18N
                  NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_Summary_Desc")); // NOI18N
        }
        public String getValue() {
            return getNbJiraIssue().getSummary();
        }
        @Override
        public int compareTo(IssueProperty p) {
            if(p == null) return 1;
            String s1 = getIssue().getSummary();
            String s2 = p.getIssue().getSummary();
            return s1.compareTo(s2);
        }
    }
}
