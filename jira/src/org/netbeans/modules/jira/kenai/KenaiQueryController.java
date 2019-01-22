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

package org.netbeans.modules.jira.kenai;

import org.netbeans.modules.jira.client.spi.FilterDefinition;
import org.netbeans.modules.jira.client.spi.JiraFilter;
import org.netbeans.modules.jira.client.spi.Project;
import org.netbeans.modules.jira.issue.NbJiraIssue;
import org.netbeans.modules.jira.issue.NbJiraIssue.IssueField;
import org.netbeans.modules.jira.query.JiraQuery;
import org.netbeans.modules.jira.query.QueryController;
import org.netbeans.modules.jira.repository.JiraRepository;
import org.netbeans.modules.jira.util.JiraUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor.Confirmation;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
public class KenaiQueryController extends QueryController
{
    private final String projectName; // XXX don't need this - already set in filterDef
    private final FilterDefinition filter;

    public KenaiQueryController(JiraRepository repository, JiraQuery query, JiraFilter jf, String projectName, boolean predefinedQuery) {
        super(repository, query, jf, !predefinedQuery);
        this.projectName = projectName;
        this.filter = (FilterDefinition) jf;
    }
    
    @Override
    protected void enableFields(boolean bl) {
        super.enableFields(bl);
        super.disableProject();
    }

    @Override
    protected void openIssue(NbJiraIssue issue) {
        if(issue != null) {
            if(!checkIssueProduct(issue)) {
                return;
            }
        }
        super.openIssue(issue);
    }

    @Override
    protected void onCloneQuery () {
        FilterDefinition fd = getFilterDefinition();
        JiraQuery q = new KenaiQuery(null, getRepository(), fd, projectName, false, true);
        JiraUtils.openQuery(q);
    }

    private boolean checkIssueProduct(NbJiraIssue issue) {
        String issueProject = issue.getFieldValue(IssueField.PROJECT);
        Project project = issue.getRepository().getConfiguration().getProjectById(issueProject);
        if (project==null || !project.getKey().equals(projectName)) {
            Confirmation dd = new DialogDescriptor.Confirmation(
                                NbBundle.getMessage(
                                    KenaiQueryController.class,
                                    "MSG_WrongProjectWarning",
                                    new Object[] {issue.getKey(), issueProject}),
                                Confirmation.YES_NO_OPTION);
            return DialogDisplayer.getDefault().notify(dd) ==  Confirmation.YES_OPTION;
        }
        return true;
    }

}
