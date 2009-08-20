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

package org.netbeans.modules.bugzilla.issue;

import java.util.List;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.IssueNode;
import org.netbeans.modules.bugtracking.spi.IssueNode.SeenProperty;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue.IssueField;
import org.netbeans.modules.bugzilla.repository.BugzillaConfiguration;
import org.openide.nodes.Node.Property;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class BugzillaIssueNode extends IssueNode {
    public BugzillaIssueNode(Issue issue) {
        super(issue);
    }

    BugzillaIssue getBugzillaIssue() {
        return (BugzillaIssue) super.getIssue();
    }

    @Override
    protected Property<?>[] getProperties() {
        return new Property<?>[] {
            new IDProperty(),
            new SeverityProperty(),
            new PriorityProperty(),
            new StatusProperty(),
            new AssignedProperty(),
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

    private Integer getSeveritySortKey(String severity) {
        BugzillaConfiguration bc = getBugzillaIssue().getBugzillaRepository().getConfiguration();
        if(bc == null || !bc.isValid()) {
            return null;
        }
        List<String> s = bc.getSeverities();
        if(s == null) {
            return null;
        }
        return s.indexOf(severity);
    }

    private Integer getPrioritySortKey(String priority) {
        BugzillaConfiguration bc = getBugzillaIssue().getBugzillaRepository().getConfiguration();
        if(bc == null || !bc.isValid()) {
            return null;
        }
        List<String> p = bc.getPriorities();
        if(p == null) {
            return null;
        }
        return p.indexOf(priority);
    }

    private Integer getResolutionSortKey(String resolution) {
        BugzillaConfiguration bc = getBugzillaIssue().getBugzillaRepository().getConfiguration();
        if(bc == null || !bc.isValid()) {
            return null;
        }
        List<String> r = bc.getResolutions();
        if(r == null) {
            return null;
        }
        return r.indexOf(resolution);
    }

    private class IDProperty extends IssueNode.IssueProperty<String> {
        public IDProperty() {
            super(BugzillaIssue.LABEL_NAME_ID,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_ID_Title"), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_ID_Desc")); // NOI18N
        }
        public String getValue() {
            return getBugzillaIssue().getID();
        }
        @Override
        public int compareTo(IssueProperty p) {
            if(p == null) return 1;
            Integer i1 = Integer.parseInt(getIssue().getID());
            Integer i2 = Integer.parseInt(p.getIssue().getID());
            return i1.compareTo(i2);
        }
    }

    private class SeverityProperty extends IssueNode.IssueProperty<String> {
        public SeverityProperty() {
            super(BugzillaIssue.LABEL_NAME_SEVERITY,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Severity_Title"), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Severity_Desc")); // NOI18N
        }
        public String getValue() {
            return getBugzillaIssue().getFieldValue(IssueField.SEVERITY);
        }
        @Override
        public Object getValue(String attributeName) {
            if("sortkey".equals(attributeName)) {                               // NOI18N
                return getSeveritySortKey(getBugzillaIssue().getFieldValue(IssueField.SEVERITY));
            } else {
                return super.getValue(attributeName);
            }
        }
    }

    public class PriorityProperty extends IssueProperty<String> {
        public PriorityProperty() {
            super(BugzillaIssue.LABEL_NAME_PRIORITY,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Priority_Title"), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Priority_Desc")); // NOI18N
        }
        public String getValue() {
            return getBugzillaIssue().getFieldValue(IssueField.PRIORITY);
        }
        @Override
        public Object getValue(String attributeName) {
            if("sortkey".equals(attributeName)) {                               // NOI18N
                return getPrioritySortKey(getBugzillaIssue().getFieldValue(IssueField.PRIORITY));
            } else {
                return super.getValue(attributeName);
            }
        }
    }

    private class StatusProperty extends IssueProperty<String> {
        public StatusProperty() {
            super(BugzillaIssue.LABEL_NAME_STATUS,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Status_Title"), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Status_Desc")); // NOI18N
        }
        public String getValue() {
            return getBugzillaIssue().getFieldValue(IssueField.STATUS);
        }
        @Override
        public int compareTo(IssueProperty p) {
            if(p == null) return 1;
            String s1 = getBugzillaIssue().getFieldValue(IssueField.STATUS);
            String s2 = ((BugzillaIssue)p.getIssue()).getFieldValue(IssueField.STATUS);
            return s1.compareTo(s2);
        }
    }

    private class ResolutionProperty extends IssueProperty<String> {
        public ResolutionProperty() {
            super(BugzillaIssue.LABEL_NAME_RESOLUTION,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Resolution_Title"), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_ID_Desc")); // NOI18N
        }
        public String getValue() {
            return getBugzillaIssue().getFieldValue(IssueField.RESOLUTION);
        }
        @Override
        public Object getValue(String attributeName) {
            if("sortkey".equals(attributeName)) {                               // NOI18N
                return getResolutionSortKey(getBugzillaIssue().getFieldValue(IssueField.RESOLUTION));
            } else {
                return super.getValue(attributeName);
            }
        }
    }

    private class SummaryProperty extends IssueProperty<String> {
        public SummaryProperty() {
            super(BugzillaIssue.LABEL_NAME_SUMMARY,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Summary_Title"), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Summary_Desc")); // NOI18N
        }
        public String getValue() {
            return getBugzillaIssue().getSummary();
        }
        @Override
        public int compareTo(IssueProperty p) {
            if(p == null) return 1;
            String s1 = getIssue().getSummary();
            String s2 = p.getIssue().getSummary();
            return s1.compareTo(s2);
        }
    }

    private class AssignedProperty extends IssueProperty<String> {
        public AssignedProperty() {
            super(BugzillaIssue.LABEL_NAME_ASSIGNED_TO,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Assigned_Title"), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Assigned_Desc")); // NOI18N
        }
        public String getValue() {
            return getBugzillaIssue().getFieldValue(IssueField.ASSIGNED_TO);
        }
        @Override
        public int compareTo(IssueProperty p) {
            if(p == null) return 1;
            String s1 = getBugzillaIssue().getFieldValue(IssueField.ASSIGNED_TO);
            String s2 = ((BugzillaIssue)p.getIssue()).getFieldValue(IssueField.ASSIGNED_TO);
            return s1.compareTo(s2);
        }
    }
}
