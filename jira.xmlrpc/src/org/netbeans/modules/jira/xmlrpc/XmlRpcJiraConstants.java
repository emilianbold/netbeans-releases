/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jira.xmlrpc;

import com.atlassian.connector.eclipse.internal.jira.core.IJiraConstants;
import com.atlassian.connector.eclipse.internal.jira.core.JiraAttribute;
import com.atlassian.connector.eclipse.internal.jira.core.WorkLogConverter;
import org.netbeans.modules.jira.client.spi.JiraConstants;
import org.netbeans.modules.jira.client.spi.JiraVersion;

/**
 *
 * @author Tomas Stupka
 */
class XmlRpcJiraConstants implements JiraConstants {
    private final JiraVersion MIN_VERSION;

    public XmlRpcJiraConstants(JiraVersion MIN_VERSION) {
        this.MIN_VERSION = MIN_VERSION;
    }

    @Override
    public String getATTRIBUTE_LINK_PREFIX() {
        return IJiraConstants.ATTRIBUTE_LINK_PREFIX;
    }

    @Override
    public String getMETA_TYPE() {
        return IJiraConstants.META_TYPE;
    }

    @Override
    public String getJiraAttribute_ISSUE_KEY_id() {
        return JiraAttribute.ISSUE_KEY.id();
    }

    @Override
    public String getJiraAttribute_SUMMARY_id() {
        return JiraAttribute.SUMMARY.id();
    }

    @Override
    public String getJiraAttribute_DESCRIPTION_id() {
        return JiraAttribute.DESCRIPTION.id();
    }

    @Override
    public String getJiraAttribute_PRIORITY_id() {
        return JiraAttribute.PRIORITY.id();
    }

    @Override
    public String getJiraAttribute_RESOLUTION_id() {
        return JiraAttribute.RESOLUTION.id();
    }

    @Override
    public String getJiraAttribute_PROJECT_id() {
        return JiraAttribute.PROJECT.id();
    }

    @Override
    public String getJiraAttribute_COMPONENTS_id() {
        return JiraAttribute.COMPONENTS.id();
    }

    @Override
    public String getJiraAttribute_AFFECTSVERSIONS_id() {
        return JiraAttribute.AFFECTSVERSIONS.id();
    }

    @Override
    public String getJiraAttribute_FIXVERSIONS_id() {
        return JiraAttribute.FIXVERSIONS.id();
    }

    @Override
    public String getJiraAttribute_ENVIRONMENT_id() {
        return JiraAttribute.ENVIRONMENT.id();
    }

    @Override
    public String getJiraAttribute_USER_REPORTER_id() {
        return JiraAttribute.USER_REPORTER.id();
    }

    @Override
    public String getJiraAttribute_USER_ASSIGNED_id() {
        return JiraAttribute.USER_ASSIGNED.id();
    }

    @Override
    public String getJiraAttribute_TYPE_id() {
        return JiraAttribute.TYPE.id();
    }

    @Override
    public String getJiraAttribute_CREATION_DATE_id() {
        return JiraAttribute.CREATION_DATE.id();
    }

    @Override
    public String getJiraAttribute_MODIFICATION_DATE_id() {
        return JiraAttribute.MODIFICATION_DATE.id();
    }

    @Override
    public String getJiraAttribute_DUE_DATE_id() {
        return JiraAttribute.DUE_DATE.id();
    }

    @Override
    public String getJiraAttribute_ESTIMATE_id() {
        return JiraAttribute.ESTIMATE.id();
    }

    @Override
    public String getJiraAttribute_INITIAL_ESTIMATE_id() {
        return JiraAttribute.INITIAL_ESTIMATE.id();
    }

    @Override
    public String getJiraAttribute_ACTUAL_id() {
        return JiraAttribute.ACTUAL.id();
    }

    @Override
    public String getJiraAttribute_PARENT_ID_id() {
        return JiraAttribute.PARENT_ID.id();
    }

    @Override
    public String getJiraAttribute_PARENT_KEY_id() {
        return JiraAttribute.PARENT_KEY.id();
    }

    @Override
    public String getJiraAttribute_SUBTASK_IDS_id() {
        return JiraAttribute.SUBTASK_IDS.id();
    }

    @Override
    public String getJiraAttribute_SUBTASK_KEYS_id() {
        return JiraAttribute.SUBTASK_KEYS.id();
    }

    @Override
    public String getATTRIBUTE_CUSTOM_PREFIX() {
        return IJiraConstants.ATTRIBUTE_CUSTOM_PREFIX;
    }

    @Override
    public String getWorkLogConverter_ATTRIBUTE_WORKLOG_NEW() {
        return WorkLogConverter.ATTRIBUTE_WORKLOG_NEW;
    }

    @Override
    public String getWorkLogConverter_TYPE_WORKLOG() {
        return WorkLogConverter.TYPE_WORKLOG;
    }

    @Override
    public String getWorkLogConverter_ATTRIBUTE_WORKLOG_NEW_SUBMIT_FLAG() {
        return WorkLogConverter.ATTRIBUTE_WORKLOG_NEW_SUBMIT_FLAG;
    }

    @Override
    public String getWorkLogConverter_AUTOR_key() {
        return WorkLogConverter.AUTOR.key();
    }

    @Override
    public String getWorkLogConverter_START_DATE_key() {
        return WorkLogConverter.START_DATE.key();
    }

    @Override
    public String getWorkLogConverter_TIME_SPENT_key() {
        return WorkLogConverter.TIME_SPENT.key();
    }

    @Override
    public String getWorkLogConverter_COMMENT_key() {
        return WorkLogConverter.COMMENT.key();
    }

    @Override
    public String getMETA_SUB_TASK_TYPE() {
        return IJiraConstants.META_SUB_TASK_TYPE;
    }

    @Override
    public String getPARENT_ID_id() {
        return JiraAttribute.PARENT_ID.id();
    }

    @Override
    public JiraVersion getMIN_VERSION() {
        return MIN_VERSION;
    }
}
