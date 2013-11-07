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

package org.netbeans.modules.odcs.tasks.issue;

import com.tasktop.c2c.server.tasks.domain.AbstractReferenceValue;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.netbeans.modules.bugtracking.issuetable.IssueNode;
import org.netbeans.modules.odcs.tasks.ODCS;
import org.netbeans.modules.odcs.tasks.util.ODCSUtil;
import org.openide.nodes.Node.Property;

/**
 *
 * @author Tomas Stupka
 */
public class ODCSIssueNode extends IssueNode<ODCSIssue> {
    private static final AbstractReferenceValue DUMMY_ODCS_VALUE;
    static {
        DUMMY_ODCS_VALUE = new AbstractReferenceValue();
        DUMMY_ODCS_VALUE.setValue(""); //NOI18N
        DUMMY_ODCS_VALUE.setSortkey(Short.MAX_VALUE);
    }
    
    public ODCSIssueNode(ODCSIssue issue) {
        super(issue, 
              ODCS.getInstance().getIssueProvider(),
              ODCS.getInstance().getStatusProvider(),
              ODCS.getInstance().getChangesProvider());
    }

    ODCSIssue getODCSIssue() {
        return getIssueData();
    }

    @Override
    protected Property<?>[] getProperties() {
        return new Property<?>[] {
            // XXX is this complete?
            new IDProperty(),
            new ODCSFieldProperty(IssueField.TASK_TYPE),
            new SeverityProperty(),
            new PriorityProperty(),
            new StatusProperty(),
            new ResolutionProperty(),
            new SummaryProperty(), 
            new ModificationProperty(),
            new ODCSFieldProperty(IssueField.PRODUCT), 
            new ODCSFieldProperty(IssueField.COMPONENT), 
            new IterationProperty(),
            new MilestoneProperty(), 
        };
    };

    @Override
    public void fireDataChanged() {
        super.fireDataChanged();
    }

    private class IDProperty extends IssueNode<ODCSIssue>.IssueProperty<String> {
        public IDProperty() {
            super(IssueField.ID.getKey(), String.class, IssueField.ID.getDisplayName(), IssueField.ID.getDescription()); 
        }
        @Override
        public String getValue() {
            return getODCSIssue().getID();
        }
        @Override
        public int compareTo( IssueNode<ODCSIssue>.IssueProperty<String> p) {
            if(p == null) {
                return 1;
            }
            Integer i1 = Integer.parseInt(getIssueData().getID());
            Integer i2 = Integer.parseInt(p.getIssueData().getID());
            return i1.compareTo(i2);
        }
    }

    private class SeverityProperty extends ARVProperty {
        public SeverityProperty() {
            super(IssueField.SEVERITY);
        }
        @Override
        public AbstractReferenceValue getODCSValue () {
            return getODCSIssue().getSeverity();
        }
    }

    public class PriorityProperty extends ARVProperty {
        public PriorityProperty() {
            super(IssueField.PRIORITY); 
        }
        @Override
        public AbstractReferenceValue getODCSValue () {
            return getODCSIssue().getPriority();
        }
    }

    private class ResolutionProperty extends ARVProperty {
        public ResolutionProperty() {
            super(IssueField.RESOLUTION); 
        }
        @Override
        public AbstractReferenceValue getODCSValue () {
            return getODCSIssue().getResolution();
        }
    }
    
    private class StatusProperty extends ARVProperty {
        public StatusProperty() {
            super(IssueField.STATUS); 
        }
        @Override
        public AbstractReferenceValue getODCSValue () {
            return getODCSIssue().getTaskStatus();
        }
    }
    
    private class IterationProperty extends ARVProperty {
        public IterationProperty() {
            super(IssueField.ITERATION); 
        }
        @Override
        public AbstractReferenceValue getODCSValue () {
            return getODCSIssue().getIteration();
        }
    }
    
    private class MilestoneProperty extends ARVProperty {
        public MilestoneProperty() {
            super(IssueField.MILESTONE); 
        }
        @Override
        public AbstractReferenceValue getODCSValue () {
            return getODCSIssue().getMilestone();
        }
    }
    
    private class ModificationProperty extends IssueNode<ODCSIssue>.IssueProperty<String> {
        private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        public ModificationProperty() {
            super(IssueField.MODIFIED.getKey(),
                  String.class,
                  IssueField.MODIFIED.getDisplayName(), 
                  IssueField.MODIFIED.getDescription());
        }
        @Override
        public String getValue() {
            Date date = getODCSIssue().getLastModifyDate();
            return date != null ? dateFormat.format(date) : ""; // NOI18N
        }
        @Override
        public int compareTo(IssueNode<ODCSIssue>.IssueProperty<String> p) {
            if(p == null) {
                return 1;
            }
            Date d1 = getODCSIssue().getLastModifyDate();
            if(d1 == null) {
                return 1;
            }
            Date d2 = p.getIssueData().getLastModifyDate();
            return d1.compareTo(d2);
        }
    }
    
    private abstract class ARVProperty extends IssueProperty<AbstractReferenceValue> {
        public ARVProperty(IssueField f) {
            super(f.getKey(),
                  AbstractReferenceValue.class,
                  f.getDisplayName(), 
                  f.getDescription()); 
        }

        @Override
        public final AbstractReferenceValue getValue () {
            AbstractReferenceValue value = getODCSValue();
            if (value == null) {
                value = DUMMY_ODCS_VALUE;
            }
            return value;
        }

        protected abstract AbstractReferenceValue getODCSValue ();
    
        @Override
        public int compareTo(IssueProperty<AbstractReferenceValue> p) {
            if(p == null) {
                return 1;
            }
            try {
                return getValue().compareTo(p.getValue());
            } catch (IllegalAccessException ex) {
                return 0;
            } catch (InvocationTargetException ex) {
                return 0;
            }
        }
    }

    private class ODCSFieldProperty extends IssueProperty<String> {
        private final IssueField field;
        public ODCSFieldProperty(IssueField f) {
            super(f.getKey(),
                  String.class,
                  f.getDisplayName(),
                  f.getDescription());
            this.field = f;
        }
        @Override
        public String getValue() {
            return getODCSIssue().getRepositoryFieldValue(field);
        }
        @Override
        public int compareTo(IssueNode<ODCSIssue>.IssueProperty<String> p) {
            if(p == null) {
                return 1;
            }
            String s1 = getODCSIssue().getRepositoryFieldValue(field);
            String s2 = p.getIssueData().getRepositoryFieldValue(field);
            return s1.compareTo(s2);
        }
    }
}
