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

import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.IssueNode;
import org.netbeans.modules.bugtracking.spi.IssueNode.SeenProperty;
import org.openide.nodes.Node.Property;
import org.openide.util.NbBundle;

/**
 *
 * @author tomas
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
            new ResolutionProperty(),
            new SummaryProperty(),
            new SeenProperty()
        };
    };

    @Override
    public void fireDataChanged() {
        super.fireDataChanged();
    }


    private class IDProperty extends IssueNode.IssueProperty {
        public IDProperty() {
            super(BugzillaIssue.LABEL_NAME_ID,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_ID_Title"), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_ID_Desc")); // NOI18N
        }
        public Object getValue() {
            return getBugzillaIssue().getID();
        }
    }

    private class SeverityProperty extends IssueNode.IssueProperty {
        public SeverityProperty() {
            super(BugzillaIssue.LABEL_NAME_SEVERITY,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Severity_Title"), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Severity_Desc")); // NOI18N
        }
        public Object getValue() {
            return getBugzillaIssue().getSeverity();
        }
    }

    private class PriorityProperty extends IssueProperty {
        public PriorityProperty() {
            super(BugzillaIssue.LABEL_NAME_PRIORITY,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Priority_Title"), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Priority_Desc")); // NOI18N
        }
        public Object getValue() {
            return getBugzillaIssue().getPriority();
        }
    }

    private class StatusProperty extends IssueProperty {
        public StatusProperty() {
            super(BugzillaIssue.LABEL_NAME_STATUS,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Status_Title"), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Status_Desc")); // NOI18N
        }
        public Object getValue() {
            return getBugzillaIssue().getStatus();
        }
    }

    private class ResolutionProperty extends IssueProperty {
        public ResolutionProperty() {
            super(BugzillaIssue.LABEL_NAME_RESOLUTION,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Resolution_Title"), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_ID_Desc")); // NOI18N
        }
        public Object getValue() {
            return getBugzillaIssue().getResolution();
        }
    }

    private class SummaryProperty extends IssueProperty {
        public SummaryProperty() {
            super(BugzillaIssue.LABEL_NAME_SUMMARY,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Summary_Title"), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Summary_Desc")); // NOI18N
        }
        public Object getValue() {
            return getBugzillaIssue().getSummary();
        }
    }
}
