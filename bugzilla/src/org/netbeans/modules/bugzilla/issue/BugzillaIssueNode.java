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
import org.netbeans.modules.bugtracking.issuetable.IssueNode;
import org.netbeans.modules.bugtracking.issuetable.IssueNode.SeenProperty;
import org.netbeans.modules.bugzilla.repository.BugzillaConfiguration;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugzilla.repository.IssueField;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
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
            BugzillaUtil.isNbRepository((BugzillaRepository) getIssue().getRepository()) 
                ? new IssueTypeProperty()
                : new SeverityProperty(),
            new PriorityProperty(),
            new BugzillaFieldProperty(BugzillaIssue.LABEL_NAME_STATUS, IssueField.STATUS, "CTL_Issue_Status_Title", "CTL_Issue_Status_Desc"), // NOI18N
            new BugzillaFieldProperty(BugzillaIssue.LABEL_NAME_ASSIGNED_TO, IssueField.ASSIGNED_TO, "CTL_Issue_Assigned_Title", "CTL_Issue_Status_Desc"), // NOI18N
            new ResolutionProperty(),
            new SummaryProperty(), 
            new ModificationProperty(),
            new BugzillaFieldProperty(BugzillaIssue.LABEL_NAME_PRODUCT, IssueField.PRODUCT, "CTL_Issue_Product_Title", "CTL_Issue_Product_Desc"), // NOI18N
            new BugzillaFieldProperty(BugzillaIssue.LABEL_NAME_COMPONENT, IssueField.COMPONENT, "CTL_Issue_Component_Title", "CTL_Issue_Component_Desc"), // NOI18N
            new BugzillaFieldProperty(BugzillaIssue.LABEL_NAME_VERSION, IssueField.VERSION, "CTL_Issue_Version_Title", "CTL_Issue_Version_Desc"), // NOI18N
            new BugzillaFieldProperty(BugzillaIssue.LABEL_NAME_OS, IssueField.OS, "CTL_Issue_OS_Title", "CTL_Issue_OS_Desc"), // NOI18N
            new BugzillaFieldProperty(BugzillaIssue.LABEL_NAME_PLATFORM, IssueField.PLATFORM, "CTL_Issue_Platform_Title", "CTL_Issue_Platform_Desc"), // NOI18N
            new BugzillaFieldProperty(BugzillaIssue.LABEL_NAME_MILESTONE, IssueField.MILESTONE, "CTL_Issue_Milestone_Title", "CTL_Issue_Milestone_Desc"), // NOI18N
            new BugzillaFieldProperty(BugzillaIssue.LABEL_NAME_REPORTER, IssueField.REPORTER_NAME, "CTL_Issue_Reporter_Title", "CTL_Issue_Reporter_Desc"), // NOI18N
            new BugzillaFieldProperty(BugzillaIssue.LABEL_NAME_QA_CONTACT, IssueField.QA_CONTACT_NAME, "CTL_Issue_QA_Contact_Title", "CTL_Issue_QA_Contact_Desc"), // NOI18N
            new BugzillaFieldProperty(BugzillaIssue.LABEL_NAME_KEYWORDS, IssueField.KEYWORDS, "CTL_Issue_Keywords_Title", "CTL_Issue_Keywords_Desc"), // NOI18N
            new BugzillaFieldProperty(BugzillaIssue.LABEL_NAME_WHITEBOARD, IssueField.WHITEBOARD, "CTL_Issue_Whiteboard_Title", "CTL_Issue_Whiteboard_Desc"), // NOI18N
        };
    };

    @Override
    public void fireDataChanged() {
        super.fireDataChanged();
    }

    private Integer getIssueTypeSortKey(String issueType) {
        BugzillaConfiguration bc = getBugzillaIssue().getBugzillaRepository().getConfiguration();
        if(bc == null || !bc.isValid()) {
            return null;
        }
        List<String> s = bc.getIssueTypes();
        if(s == null) {
            return null;
        }
        return s.indexOf(issueType);
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
        @Override
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
        @Override
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

    private class IssueTypeProperty extends IssueNode.IssueProperty<String> {
        public IssueTypeProperty() {
            super(BugzillaIssue.LABEL_NAME_ISSUE_TYPE,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Issue_Type_Title"), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Issue_Type_Desc")); // NOI18N
        }
        @Override
        public String getValue() {
            return getBugzillaIssue().getFieldValue(IssueField.ISSUE_TYPE);
        }
        @Override
        public Object getValue(String attributeName) {
            if("sortkey".equals(attributeName)) {                               // NOI18N
                return getIssueTypeSortKey(getBugzillaIssue().getFieldValue(IssueField.ISSUE_TYPE));
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
        @Override
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

    private class ResolutionProperty extends IssueProperty<String> {
        public ResolutionProperty() {
            super(BugzillaIssue.LABEL_NAME_RESOLUTION,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Resolution_Title"), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_ID_Desc")); // NOI18N
        }
        @Override
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

    private class ModificationProperty extends IssueProperty<String> {
        public ModificationProperty() {
            super(BugzillaIssue.LABEL_NAME_MODIFICATION,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Modification_Title"), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Modification_Desc")); // NOI18N
        }
        @Override
        public String getValue() {
            return getBugzillaIssue().getFieldValue(IssueField.MODIFICATION);
        }
        @Override
        public int compareTo(IssueProperty p) {
            if(p == null) return 1;
            // XXX sort as date
            String s1 = getBugzillaIssue().getFieldValue(IssueField.MODIFICATION);
            String s2 = ((BugzillaIssue)p.getIssue()).getFieldValue(IssueField.MODIFICATION);
            return s1.compareTo(s2);
        }
    }

    private class BugzillaFieldProperty extends IssueProperty<String> {
        private final IssueField field;
        public BugzillaFieldProperty(String fieldLabel, IssueField f, String titleProp, String descProp) {
            super(fieldLabel,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, titleProp), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, descProp)); // NOI18N
            this.field = f;
        }
        @Override
        public String getValue() {
            return getBugzillaIssue().getFieldValue(field);
        }
        @Override
        public int compareTo(IssueProperty p) {
            if(p == null) return 1;
            String s1 = getBugzillaIssue().getFieldValue(field);
            String s2 = ((BugzillaIssue)p.getIssue()).getFieldValue(field);
            return s1.compareTo(s2);
        }
    }
}
