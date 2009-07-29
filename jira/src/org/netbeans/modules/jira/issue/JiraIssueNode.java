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
            new AssigneeProperty(),
            new SummaryProperty(),
            new RecentChangesProperty(), // XXX move to issue node
            new SeenProperty() // XXX move to issue node
        };
    };

    @Override
    public void fireDataChanged() {
        super.fireDataChanged();
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
            return getIssue().getID().compareTo(p.getIssue().getID());
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
            return super.getValue(attributeName);
        }
        @Override
        public int compareTo(IssueProperty p) {
            if(p == null) return 1;
            String s1 = getNbJiraIssue().getType().getName();
            String s2 = ((NbJiraIssue)p.getIssue()).getType().getName();
            return s1.compareTo(s2);
        }
    }

    public class PriorityProperty extends IssueProperty<String> {
        public PriorityProperty() {
            super(NbJiraIssue.LABEL_NAME_PRIORITY,
                  String.class,
                  NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_Priority_Title"), // NOI18N
                  NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_Priority_Desc")); // NOI18N
        }
        public String getValue() {
            Priority priority = getPriority();
            return priority != null ? priority.getName() : "";                  // NOI18N
        }
        public Priority getPriority() {
            return getNbJiraIssue().getPriority();
        }
        @Override
        public Object getValue(String attributeName) {
            if("sortkey".equals(attributeName)) {                               // NOI18N
                try {
                    return Integer.parseInt(getNbJiraIssue().getFieldValue(IssueField.PRIORITY));
                } catch (NumberFormatException nfex) {
                    return null;
                }
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
            return super.getValue(attributeName);
        }
        @Override
        public int compareTo(IssueProperty p) {
            if(p == null) return 1;
            Resolution resolution = getNbJiraIssue().getResolution();
            String s1 = (resolution == null) ? "" : resolution.getName(); // NOI18N
            resolution = ((NbJiraIssue)p.getIssue()).getResolution();
            String s2 = (resolution == null) ? "" : resolution.getName(); // NOI18N
            return s1.compareTo(s2);
        }
    }

    public class SummaryProperty extends IssueProperty<String> {
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

    private class AssigneeProperty extends IssueProperty<String> {
        public AssigneeProperty() {
            super(NbJiraIssue.LABEL_NAME_ASSIGNED_TO,
                  String.class,
                  NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_Assigned_Title"), // NOI18N
                  NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_Assigned_Desc")); // NOI18N
        }
        public String getValue() {
            return getNbJiraIssue().getFieldValue(IssueField.ASSIGNEE);
        }
        @Override
        public int compareTo(IssueProperty p) {
            if(p == null) return 1;
            String s1 = getNbJiraIssue().getFieldValue(IssueField.ASSIGNEE);
            String s2 = ((NbJiraIssue)p.getIssue()).getFieldValue(IssueField.ASSIGNEE);
            return s1.compareTo(s2);
        }
    }
}
