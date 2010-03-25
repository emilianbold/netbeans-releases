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

import java.util.Date;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.JiraStatus;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.issuetable.IssueNode;
import org.netbeans.modules.jira.issue.NbJiraIssue.IssueField;
import org.netbeans.modules.jira.repository.JiraConfiguration;
import org.netbeans.modules.jira.util.JiraUtils;
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
            new JiraFieldProperty(NbJiraIssue.LABEL_NAME_ASSIGNED_TO, IssueField.ASSIGNEE, "CTL_Issue_Assigned_Title", "CTL_Issue_Assigned_Desc"),
            new SummaryProperty(),
            new JiraFieldProperty(NbJiraIssue.LABEL_NAME_PROJECT, IssueField.PROJECT, "CTL_Issue_Project_Title", "CTL_Issue_Project_Desc",ValueReturnType.DISPLAY_VALUE),
            new DateFieldProperty(NbJiraIssue.LABEL_NAME_CREATED, IssueField.CREATION, "CTL_Issue_Created_Title", "CTL_Issue_Created_Desc"),
            new DateFieldProperty(NbJiraIssue.LABEL_NAME_UPDATED, IssueField.MODIFICATION, "CTL_Issue_Updated_Title", "CTL_Issue_Updated_Desc"),
            new DateFieldProperty(NbJiraIssue.LABEL_NAME_DUE, IssueField.DUE, "CTL_Issue_Due_Title", "CTL_Issue_Due_Desc"),
            new WorkLogFieldProperty(NbJiraIssue.LABEL_NAME_ESTIMATE, IssueField.ESTIMATE, "CTL_Issue_Estimate_Title", "CTL_Issue_Estimate_Desc"),
            new WorkLogFieldProperty(NbJiraIssue.LABEL_NAME_INITIAL_ESTIMATE, IssueField.INITIAL_ESTIMATE, "CTL_Issue_Initial_Estimate_Title", "CTL_Issue_Initial_Estimate_Desc"),
            new WorkLogFieldProperty(NbJiraIssue.LABEL_NAME_TIME_SPENT, IssueField.ACTUAL, "CTL_Issue_Time_Spent_Title", "CTL_Issue_Time_Spent_Desc"),
            new MultiValueFieldProperty(NbJiraIssue.LABEL_NAME_COMPONENTS, IssueField.COMPONENT, "CTL_Issue_Components_Title", "CTL_Issue_Components_Desc"),
            new MultiValueFieldProperty(NbJiraIssue.LABEL_NAME_AFFECTS_VERSION, IssueField.AFFECTSVERSIONS, "CTL_Issue_Affects_Version_Title", "CTL_Issue_Affects_Version_Desc"),
            new MultiValueFieldProperty(NbJiraIssue.LABEL_NAME_FIX_VERSION, IssueField.FIXVERSIONS, "CTL_Issue_Fix_Version_Title", "CTL_Issue_Fix_Version_Desc"),
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
        @Override
        public String getValue() {
            return getNbJiraIssue().getFieldValue(IssueField.KEY);
        }
        @Override
        public int compareTo(IssueProperty p) {
            if(p == null) {
                return 1;
            }
            String id = getIssue().getID();
            String pid = p.getIssue().getID();
            int idx = id.lastIndexOf("-");      // NOI18N
            int pidx = pid.lastIndexOf("-");    // NOI18N

            String projectId = idx < id.length() ?  id.substring(0, idx) : "";      // NOI18N
            String projectPid = pidx < pid.length() ? pid.substring(0, pidx) : "";  // NOI18N
            int c = projectId.compareTo(projectPid);
            if(c != 0) {
                return c;
            }
            try {
                Long lid = idx + 1 < pid.length() ? Long.parseLong(id.substring(idx + 1)) : -1;
                Long lpid = pidx + 1 < pid.length() ? Long.parseLong(pid.substring(pidx + 1)) : -1;
                if(lid != null && lpid != null) {
                    return lid.compareTo(lpid);
                }
            } catch (NumberFormatException ex) {}
            return id.compareTo(pid);
        }
    }

    public class PriorityProperty extends IssueProperty<String> {
        public PriorityProperty() {
            super(NbJiraIssue.LABEL_NAME_PRIORITY,
                  String.class,
                  NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_Priority_Title"), // NOI18N
                  NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_Priority_Desc")); // NOI18N
        }
        @Override
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
        @Override
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

    private class TypeProperty extends IssueNode.IssueProperty<String> {
        public TypeProperty() {
            super(NbJiraIssue.LABEL_NAME_TYPE,
                  String.class,
                  NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_Type_Title"), // NOI18N
                  NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_Type_Desc")); // NOI18N
        }
        @Override
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

    private class ResolutionProperty extends IssueProperty<String> {
        public ResolutionProperty() {
            super(NbJiraIssue.LABEL_NAME_RESOLUTION,
                  String.class,
                  NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_Resolution_Title"), // NOI18N
                  NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_ID_Desc")); // NOI18N
        }
        @Override
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

    private enum ValueReturnType {
        VALUE,
        DISPLAY_VALUE
    }
    private class JiraFieldProperty extends IssueProperty<String> {
        private final IssueField field;
        private final ValueReturnType returnType;
        public JiraFieldProperty(String fieldLabel, IssueField f, String titleProp, String descProp) {
            this(fieldLabel, f, titleProp, descProp, ValueReturnType.VALUE);
        }
        public JiraFieldProperty(String fieldLabel, IssueField f, String titleProp, String descProp, ValueReturnType returnType) {
            super(fieldLabel,
                  String.class,
                  NbBundle.getMessage(NbJiraIssue.class, titleProp), 
                  NbBundle.getMessage(NbJiraIssue.class, descProp)); 
            this.field = f;
            this.returnType = returnType;
        }
        @Override
        public String getValue() {
            return getValue(returnType);
        }
        public String getValue(ValueReturnType returnType) {
            switch(returnType) {
                case VALUE:
                    return getNbJiraIssue().getFieldValue(field);
                case DISPLAY_VALUE:
                    return getNbJiraIssue().getFieldDisplayValue(field);
                default:
                    throw new IllegalStateException(returnType.toString());
            }
        }
        @Override
        public int compareTo(IssueProperty p) {
            if(p == null) return 1;
            String s1 = getValue();
            JiraFieldProperty bp = (JiraFieldProperty)p;
            String s2 = bp.getValue(bp.returnType);
            return s1.compareTo(s2);
        }
    }

    private class DateFieldProperty extends IssueProperty<String> {
        private final IssueField field;
        public DateFieldProperty(String fieldLabel, IssueField f, String titleProp, String descProp) {
            super(fieldLabel,
                  String.class,
                  NbBundle.getMessage(NbJiraIssue.class, titleProp),
                  NbBundle.getMessage(NbJiraIssue.class, descProp));
            this.field = f;
        }
        @Override
        public String getValue() {
            String value = getNbJiraIssue().getFieldValue(field);
            return JiraUtils.dateByMillis(value, true);
        }
        @Override
        public int compareTo(IssueProperty p) {
            if(p == null) return 1;
            Date d1 = JiraUtils.dateByMillis(getNbJiraIssue().getFieldValue(field));
            Date d2 = JiraUtils.dateByMillis(((NbJiraIssue)p.getIssue()).getFieldValue(field));
            return d1.compareTo(d2);
        }
    }

    public class MultiValueFieldProperty extends IssueProperty<String> {
        private final IssueField field;
        public MultiValueFieldProperty(String fieldLabel, IssueField f, String titleProp, String descProp) {
            super(fieldLabel,
                  String.class,
                  NbBundle.getMessage(NbJiraIssue.class, titleProp),
                  NbBundle.getMessage(NbJiraIssue.class, descProp));
            this.field = f;
        }
        @Override
        public String getValue() {
            // XXX sorted?
            return getNbJiraIssue().getFieldDisplayValue(field);
        }
        @Override
        public int compareTo(IssueProperty p) {
            if(p == null) return 1;
            // XXX sorted?
            String s1 = getNbJiraIssue().getFieldDisplayValue(field);
            String s2 = ((NbJiraIssue)p.getIssue()).getFieldDisplayValue(field);
            return s1.compareTo(s2);
        }
    }

    private class WorkLogFieldProperty extends IssueProperty<String> {
        private final IssueField field;
        public WorkLogFieldProperty(String fieldLabel, IssueField f, String titleProp, String descProp) {
            super(fieldLabel,
                  String.class,
                  NbBundle.getMessage(NbJiraIssue.class, titleProp),
                  NbBundle.getMessage(NbJiraIssue.class, descProp));
            this.field = f;
        }
        @Override
        public String getValue() {
            final JiraConfiguration configuration = getNbJiraIssue().getRepository().getConfiguration();
            int daysPerWeek = configuration.getWorkDaysPerWeek();
            int hoursPerDay = configuration.getWorkHoursPerDay();
            String value = getNbJiraIssue().getFieldValue(field);
            return JiraUtils.getWorkLogText(toInt(value), daysPerWeek, hoursPerDay, true);
        }
        @Override
        public int compareTo(IssueProperty p) {
            if(p == null) return 1;
            Integer i1 = toInt(getNbJiraIssue().getFieldValue(field));
            Integer i2 = toInt(((NbJiraIssue)p.getIssue()).getFieldValue(field));
            return i1.compareTo(i2);
        }

        private int toInt(String text) {
            if (text.trim().length() > 0) {
                try {
                    return Integer.parseInt(text);
                } catch (NumberFormatException nfex) {
                    nfex.printStackTrace();
                }
            }
            return 0;
        }
    }

}
