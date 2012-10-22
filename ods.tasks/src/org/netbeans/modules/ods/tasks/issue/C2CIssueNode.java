/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ods.tasks.issue;

import com.tasktop.c2c.server.tasks.domain.TaskSeverity;
import org.netbeans.modules.bugtracking.issuetable.IssueNode;
import org.netbeans.modules.ods.tasks.C2C;
import org.netbeans.modules.ods.tasks.spi.C2CData;
import org.netbeans.modules.ods.tasks.util.C2CUtil;
import org.openide.nodes.Node.Property;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class C2CIssueNode extends IssueNode<C2CIssue> {
    public C2CIssueNode(C2CIssue issue) {
        super(C2CUtil.getRepository(issue.getRepository()), issue);
    }

    C2CIssue getC2CIssue() {
        return getIssueData();
    }

    @Override
    protected Property<?>[] getProperties() {
        return new Property<?>[] {
            // XXX is this complete?
            new IDProperty(),
            new C2CFieldProperty(C2CIssue.LABEL_NAME_TASK_TYPE, IssueField.TASK_TYPE, "CTL_Issue_Task_Type_Title", "CTL_Issue_Task_Type_Desc"), // NOI18N
            new SeverityProperty(),
            new PriorityProperty(),
            new C2CFieldProperty(C2CIssue.LABEL_NAME_STATUS, IssueField.STATUS, "CTL_Issue_Status_Title", "CTL_Issue_Status_Desc"), // NOI18N
            new ResolutionProperty(),
            new SummaryProperty(), 
            new ModificationProperty(),
            new C2CFieldProperty(C2CIssue.LABEL_NAME_PRODUCT, IssueField.PRODUCT, "CTL_Issue_Product_Title", "CTL_Issue_Product_Desc"), // NOI18N
            new C2CFieldProperty(C2CIssue.LABEL_NAME_COMPONENT, IssueField.COMPONENT, "CTL_Issue_Component_Title", "CTL_Issue_Component_Desc"), // NOI18N
            new C2CFieldProperty(C2CIssue.LABEL_NAME_ITERATION, IssueField.ITERATION, "CTL_Issue_Iteration_Title", "CTL_Issue_Iteration_Desc"), // NOI18N
            new C2CFieldProperty(C2CIssue.LABEL_NAME_MILESTONE, IssueField.MILESTONE, "CTL_Issue_Milestone_Title", "CTL_Issue_Milestone_Desc"), // NOI18N
        };
    };

    @Override
    public void fireDataChanged() {
        super.fireDataChanged();
    }

    private Integer getSortKey(String severity, Class clazz) {
        C2CData cd = C2C.getInstance().getClientData(getC2CIssue().getRepository());
        TaskSeverity ts = cd.getValue(severity, TaskSeverity.class);
        return ts.getSortkey().intValue();
    }

    private class IDProperty extends IssueNode<C2CIssue>.IssueProperty<String> {
        public IDProperty() {
            super(C2CIssue.LABEL_NAME_ID,
                  String.class,
                  NbBundle.getMessage(C2CIssue.class, "CTL_Issue_ID_Title"), // NOI18N
                  NbBundle.getMessage(C2CIssue.class, "CTL_Issue_ID_Desc")); // NOI18N
        }
        @Override
        public String getValue() {
            return getC2CIssue().getID();
        }
        @Override
        public int compareTo(IssueProperty p) {
            if(p == null) return 1;
            Integer i1 = Integer.parseInt(getIssue().getID());
            Integer i2 = Integer.parseInt(p.getIssue().getID());
            return i1.compareTo(i2);
        }
    }

    private class SeverityProperty extends IssueNode<C2CIssue>.IssueProperty<String> {
        public SeverityProperty() {
            super(C2CIssue.LABEL_NAME_SEVERITY,
                  String.class,
                  NbBundle.getMessage(C2CIssue.class, "CTL_Issue_Severity_Title"), // NOI18N
                  NbBundle.getMessage(C2CIssue.class, "CTL_Issue_Severity_Desc")); // NOI18N
        }
        @Override
        public String getValue() {
            return getC2CIssue().getFieldValue(IssueField.SEVERITY);
        }
        @Override
        public Object getValue(String attributeName) {
            if("sortkey".equals(attributeName)) {                               // NOI18N
                return -1; //getSeveritySortKey(getC2CIssue().getFieldValue(IssueField.SEVERITY));
            } else {
                return super.getValue(attributeName);
            }
        }
    }

    public class PriorityProperty extends IssueNode<C2CIssue>.IssueProperty<String> {
        public PriorityProperty() {
            super(C2CIssue.LABEL_NAME_PRIORITY,
                  String.class,
                  NbBundle.getMessage(C2CIssue.class, "CTL_Issue_Priority_Title"), // NOI18N
                  NbBundle.getMessage(C2CIssue.class, "CTL_Issue_Priority_Desc")); // NOI18N
        }
        @Override
        public String getValue() {
            return getC2CIssue().getFieldValue(IssueField.PRIORITY);
        }
        @Override
        public Object getValue(String attributeName) {
            if("sortkey".equals(attributeName)) {                               // NOI18N
                return -1; //getPrioritySortKey(getC2CIssue().getFieldValue(IssueField.PRIORITY));
            } else {
                return super.getValue(attributeName);
            }
        }
    }

    private class ResolutionProperty extends IssueNode<C2CIssue>.IssueProperty<String> {
        public ResolutionProperty() {
            super(C2CIssue.LABEL_NAME_RESOLUTION,
                  String.class,
                  NbBundle.getMessage(C2CIssue.class, "CTL_Issue_Resolution_Title"), // NOI18N
                  NbBundle.getMessage(C2CIssue.class, "CTL_Issue_ID_Desc")); // NOI18N
        }
        @Override
        public String getValue() {
            return getC2CIssue().getFieldValue(IssueField.RESOLUTION);
        }
        @Override
        public Object getValue(String attributeName) {
            if("sortkey".equals(attributeName)) {                               // NOI18N
                return -1; //getResolutionSortKey(getC2CIssue().getFieldValue(IssueField.RESOLUTION));
            } else {
                return super.getValue(attributeName);
            }
        }
    }

    private class ModificationProperty extends IssueNode<C2CIssue>.IssueProperty<String> {
        public ModificationProperty() {
            super(C2CIssue.LABEL_NAME_MODIFIED,
                  String.class,
                  NbBundle.getMessage(C2CIssue.class, "CTL_Issue_Modification_Title"), // NOI18N
                  NbBundle.getMessage(C2CIssue.class, "CTL_Issue_Modification_Desc")); // NOI18N
        }
        @Override
        public String getValue() {
            return getC2CIssue().getFieldValue(IssueField.MODIFIED);
        }
        @Override
        public int compareTo(IssueNode<C2CIssue>.IssueProperty<String> p) {
            if(p == null) return 1;
            // XXX sort as date
            String s1 = getC2CIssue().getFieldValue(IssueField.MODIFIED);
            String s2 = p.getIssueData().getFieldValue(IssueField.MODIFIED);
            return s1.compareTo(s2);
        }
    }

    private class C2CFieldProperty extends IssueProperty<String> {
        private final IssueField field;
        public C2CFieldProperty(String fieldLabel, IssueField f, String titleProp, String descProp) {
            super(fieldLabel,
                  String.class,
                  NbBundle.getMessage(C2CIssue.class, titleProp), // NOI18N
                  NbBundle.getMessage(C2CIssue.class, descProp)); // NOI18N
            this.field = f;
        }
        @Override
        public String getValue() {
            return getC2CIssue().getFieldValue(field);
        }
        @Override
        public int compareTo(IssueNode<C2CIssue>.IssueProperty<String> p) {
            if(p == null) return 1;
            String s1 = getC2CIssue().getFieldValue(field);
            String s2 = p.getIssueData().getFieldValue(field);
            return s1.compareTo(s2);
        }
    }
}
