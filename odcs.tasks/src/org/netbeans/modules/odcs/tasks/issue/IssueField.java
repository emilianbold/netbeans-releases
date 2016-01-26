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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.odcs.tasks.issue;

import java.util.ArrayList;
import java.util.List;
import oracle.eclipse.tools.cloud.dev.tasks.CloudDevAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.netbeans.modules.mylyn.util.AbstractNbTaskWrapper;
import org.openide.util.NbBundle;

/**
 * Information about an issue field.
 *
 * @author Tomas Stupka
 */
public class IssueField {
    
    public static final IssueField ID = new IssueField(TaskAttribute.TASK_KEY, "CTL_Issue_ID_Title", "CTL_Issue_ID_Desc"); // NOI18N
    public static final IssueField SUMMARY = new IssueField(TaskAttribute.SUMMARY, "CTL_Issue_Summary_Title", "CTL_Issue_Summary_Desc"); // NOI18N
    public static final IssueField CC = new IssueField(CloudDevAttribute.CC.getTaskName(), "CTL_Issue_CC_Title", "CTL_Issue_CC_Desc"); // NOI18N
    public static final IssueField COMPONENT = new IssueField(TaskAttribute.COMPONENT, "CTL_Issue_Component_Title", "CTL_Issue_Component_Desc"); // NOI18N
    public static final IssueField DESCRIPTION = new IssueField(TaskAttribute.DESCRIPTION, "CTL_Issue_Description_Title", "CTL_Issue_Description_Desc"); // NOI18N
    public static final IssueField DUEDATE = new IssueField(TaskAttribute.DATE_DUE, "CTL_Issue_DueDate_Title", "CTL_Issue_DueDate_Desc"); // NOI18N
    public static final IssueField ESTIMATE = new IssueField(CloudDevAttribute.ESTIMATED_TIME.getTaskName(), "CTL_Issue_Estimate_Title", "CTL_Issue_Estimate_Desc"); //NOI18N
    public static final IssueField FOUNDIN = new IssueField(CloudDevAttribute.FOUND_IN_RELEASE.getTaskName(), "CTL_Issue_FoundIn_Title", "CTL_Issue_FoundIn_Desc"); //NOI18N
    public static final IssueField ITERATION = new IssueField(CloudDevAttribute.ITERATION.getTaskName(), "CTL_Issue_Iteration_Title", "CTL_Issue_Iteration_Desc"); // NOI18N
    public static final IssueField MILESTONE = new IssueField(CloudDevAttribute.MILESTONE.getTaskName(), "CTL_Issue_Milestone_Title", "CTL_Issue_Milestone_Desc"); // NOI18N
    public static final IssueField PRIORITY = new IssueField(TaskAttribute.PRIORITY, "CTL_Issue_Priority_Title", "CTL_Issue_Priority_Desc"); // NOI18N
    public static final IssueField PRODUCT = new IssueField(TaskAttribute.PRODUCT, "CTL_Issue_Product_Title", "CTL_Issue_Product_Desc"); // NOI18N
    public static final IssueField STATUS = new IssueField(TaskAttribute.STATUS, "CTL_Issue_Status_Title", "CTL_Issue_Status_Desc"); // NOI18N
    public static final IssueField RESOLUTION = new IssueField(TaskAttribute.RESOLUTION, "CTL_Issue_Resolution_Title", "CTL_Issue_Resolution_Desc"); // NOI18N
    public static final IssueField DUPLICATE = new IssueField(CloudDevAttribute.DUPLICATE_OF.getTaskName(), "CTL_Issue_Duplicate_Title", "CTL_Issue_Duplicate_Desc"); // NOI18N
    public static final IssueField SEVERITY = new IssueField(TaskAttribute.SEVERITY, "CTL_Issue_Severity_Title", "CTL_Issue_Severity_Desc"); // NOI18N
    public static final IssueField TASK_TYPE = new IssueField(CloudDevAttribute.TASK_TYPE.getTaskName(), "CTL_Issue_Task_Type_Title", "CTL_Issue_Task_Type_Desc"); // NOI18N
    public static final IssueField REPORTER = new IssueField(TaskAttribute.USER_REPORTER, "CTL_Issue_Reporter_Title", "CTL_Issue_Reporter_Title"); // NOI18N
    public static final IssueField OWNER = new IssueField(TaskAttribute.USER_ASSIGNED, "CTL_Issue_Owner_Title", "CTL_Issue_Owner_Desc"); // NOI18N
    public static final IssueField KEYWORDS = new IssueField(CloudDevAttribute.KEYWORDS.getTaskName(), "CTL_Issue_Keywords_Title", "CTL_Issue_Keywords_Desc"); // NOI18N
    public static final IssueField PARENT = new IssueField(CloudDevAttribute.PARENT_TASK.getTaskName(), "CTL_Issue_Parent_Title", "CTL_Issue_Parent_Desc"); // NOI18N
    public static final IssueField SUBTASK = new IssueField(CloudDevAttribute.SUBTASKS.getTaskName(), "CTL_Issue_Subtask_Title", "CTL_Issue_Subtask_Desc"); // NOI18N
    public static final IssueField MODIFIED = new IssueField(CloudDevAttribute.MODIFIED_DATE.getTaskName(), "CTL_Issue_Modification_Title", "CTL_Issue_Modification_Desc"); // NOI18N
    public static final IssueField CREATED = new IssueField(CloudDevAttribute.CREATION_DATE.getTaskName(), "CTL_Issue_Creation_Title", "CTL_Issue_Creation_Desc"); // NOI18N
    
    public static final IssueField COMMENT = new IssueField(TaskAttribute.COMMENT_NEW, null, null, true);
    public static final IssueField COMMENT_COUNT = new IssueField(TaskAttribute.TYPE_COMMENT, null, null, false);
    public static final IssueField ATTACHEMENT_COUNT = new IssueField(TaskAttribute.TYPE_ATTACHMENT, null, null, false);
    public static final IssueField NB_NEW_ATTACHMENTS = new IssueField(AbstractNbTaskWrapper.NEW_ATTACHMENT_ATTRIBUTE_ID, "CTL_Issue_NewAttachments_Title", null, false); //NOI18N

    private final String attribute;
    private final String displayNameKey;
    private final String descriptionKey;
    private boolean singleFieldAttribute;

    IssueField(String key, String displayNameKey, String descriptionKey) {
        this(key, displayNameKey, descriptionKey, true);
    }

    IssueField(String key, String displayNameKey, String descriptionKey, boolean singleAttribute) {
        this.attribute = key;
        this.displayNameKey = displayNameKey;
        this.descriptionKey = displayNameKey;
        this.singleFieldAttribute = singleAttribute;
    }

    public String getKey() {
        return attribute;
    }

    public String getDisplayName() {
        assert displayNameKey != null; // shouldn't be called for a field with a null display name
        return NbBundle.getMessage(IssueField.class, displayNameKey);
    }
    
    public String getDescription() {
        assert descriptionKey != null; // shouldn't be called for a field with a null description
        return NbBundle.getMessage(IssueField.class, displayNameKey);
    }

    // XXX strange usage - find some other sematics how to name and evaluate a change in comments and attachments
    boolean isSingleFieldAttribute() {
        return singleFieldAttribute;
    }

    private static List<IssueField> issueFields;
    public static List<IssueField> getFields() {
        if (issueFields == null) {
            List<IssueField> fields = new ArrayList<IssueField>(40);
            fields.add(IssueField.ATTACHEMENT_COUNT);
            fields.add(IssueField.CC);
            fields.add(IssueField.COMMENT_COUNT);
            fields.add(IssueField.COMPONENT);
            fields.add(IssueField.CREATED);
            fields.add(IssueField.DESCRIPTION);
            fields.add(IssueField.DUEDATE);
            fields.add(IssueField.DUPLICATE);
            fields.add(IssueField.ESTIMATE);
            fields.add(IssueField.FOUNDIN);
            fields.add(IssueField.ITERATION);
            fields.add(IssueField.MILESTONE);
            fields.add(IssueField.MODIFIED);
            fields.add(IssueField.OWNER);
            fields.add(IssueField.PARENT);
            fields.add(IssueField.PRIORITY);
            fields.add(IssueField.PRODUCT);
            fields.add(IssueField.REPORTER);
            fields.add(IssueField.RESOLUTION);
            fields.add(IssueField.SEVERITY);
            fields.add(IssueField.STATUS);
            fields.add(IssueField.SUBTASK);
            fields.add(IssueField.SUMMARY);
            fields.add(IssueField.KEYWORDS);
            fields.add(IssueField.TASK_TYPE);
            
            issueFields = fields;
        }
        return issueFields;
    }    
}
