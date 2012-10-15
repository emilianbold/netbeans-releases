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

package org.netbeans.modules.ods.tasks.issue;

import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.netbeans.modules.ods.tasks.spi.C2CData;
import org.openide.util.NbBundle;

/**
 * Information about an issue field.
 *
 * @author Tomas Stupka
 */
public class IssueField {
    
    public static final IssueField SUMMARY = new IssueField(TaskAttribute.SUMMARY, "LBL_SUMMARY"); // NOI18N
    public static final IssueField CC = new IssueField(C2CData.ATTR_CC, "LBL_CC"); // NOI18N
    public static final IssueField COMPONENT = new IssueField(TaskAttribute.COMPONENT, "LBL_COMPONENT"); // NOI18N
    public static final IssueField DESCRIPTION = new IssueField(TaskAttribute.DESCRIPTION, "LBL_DESCRIPTION"); // NOI18N
    public static final IssueField DUEDATE = new IssueField(C2CData.ATTR_DUEDATE, "LBL_DUEDATE"); // NOI18N
    public static final IssueField ESTIMATE = new IssueField(C2CData.ATTR_ESTIMATE_WITH_UNITS, "LBL_ESTIMATE"); //NOI18N
    public static final IssueField FOUNDIN = new IssueField(C2CData.ATTR_FOUND_IN_RELEASE, "LBL_FOUNDIN"); //NOI18N
    public static final IssueField ITERATION = new IssueField(C2CData.ATTR_ITERATION, "LBL_ITERATION"); // NOI18N
    public static final IssueField MILESTONE = new IssueField(C2CData.ATTR_MILESTONE, "LBL_MILESTONE"); // NOI18N
    public static final IssueField PRIORITY = new IssueField(TaskAttribute.PRIORITY, "LBL_PRIORITY"); // NOI18N
    public static final IssueField PRODUCT = new IssueField(TaskAttribute.PRODUCT, "LBL_PRODUCT"); // NOI18N
    public static final IssueField STATUS = new IssueField(TaskAttribute.STATUS, "LBL_STATUS"); // NOI18N
    public static final IssueField RESOLUTION = new IssueField(TaskAttribute.RESOLUTION, "LBL_RESOLUTION"); // NOI18N
    public static final IssueField DUPLICATE = new IssueField(C2CData.ATTR_DUPLICATE_OF, "LBL_DUPLICATE"); // NOI18N
    public static final IssueField SEVERITY = new IssueField(TaskAttribute.SEVERITY, "LBL_SEVERITY"); // NOI18N
    public static final IssueField VERSION = new IssueField(C2CData.ATTR_VERSION, "LBL_VERSION"); // NOI18N
    public static final IssueField TASK_TYPE = new IssueField(C2CData.ATTR_TASK_TYPE, "LBL_TASK_TYPE"); // NOI18N
    public static final IssueField REPORTER = new IssueField(C2CData.ATTR_REPORTER, "LBL_REPORTER"); // NOI18N
    public static final IssueField OWNER = new IssueField(C2CData.ATTR_OWNER, "LBL_OWNER"); // NOI18N
    public static final IssueField KEYWORDS = new IssueField(C2CData.ATTR_KEYWORDS, "LBL_KEYWORDS"); // NOI18N
    public static final IssueField PARENT = new IssueField(C2CData.ATTR_PARENT, "LBL_PARENT"); // NOI18N
    public static final IssueField SUBTASK = new IssueField(C2CData.ATTR_SUBTASK, "LBL_SUBTASK"); // NOI18N
    
    public static final IssueField CREATED = new IssueField(C2CData.ATTR_MODIFIED, "LBL_CREATED"); // NOI18N
    
    public static final IssueField NEWCC = new IssueField(C2CData.ATTR_NEWCC, null); // NOI18N
    public static final IssueField MODIFIED = new IssueField(C2CData.ATTR_MODIFIED, null); // NOI18N

    private final String attribute;
    private final String displayNameKey;
    private boolean singleFieldAttribute;

    IssueField(String attribute, String displayNameKey) {
        this.attribute = attribute;
        this.displayNameKey = displayNameKey;
        this.singleFieldAttribute = true;
    }

    public String getKey() {
        return attribute;
    }

    public String getDisplayName() {
        assert displayNameKey != null; // shouldn't be called for a field with a null display name
        return NbBundle.getMessage(IssueField.class, displayNameKey);
    }

    // XXX strange usage - find some other sematics how to name and evaluate a change in comments and attachments
    boolean isSingleFieldAttribute() {
        return singleFieldAttribute;
    }

}
