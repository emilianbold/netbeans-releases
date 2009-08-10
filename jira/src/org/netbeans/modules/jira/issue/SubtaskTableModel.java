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

import java.util.List;
import java.util.ResourceBundle;
import javax.swing.table.DefaultTableModel;
import org.eclipse.mylyn.internal.jira.core.model.JiraStatus;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.netbeans.modules.bugtracking.spi.IssueCache;
import org.netbeans.modules.jira.repository.JiraRepository;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Stola
 */
public class SubtaskTableModel extends DefaultTableModel {

    public SubtaskTableModel(NbJiraIssue issue) {
        super(data(issue), columnNames());
    }

    private static String[] columnNames() {
        ResourceBundle bundle = NbBundle.getBundle(SubtaskTableModel.class);
        String subTask = bundle.getString("SubtaskTableModel.subTask"); // NOI18N
        String summary = bundle.getString("SubtaskTableModel.summary"); // NOI18N
        String status = bundle.getString("SubtaskTableModel.status"); // NOI18N
        String priority = bundle.getString("SubtaskTableModel.priority"); // NOI18N
        return new String[] {subTask, summary, status, priority};
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        Class clazz = String.class;
        switch (columnIndex) {
            case 2: clazz = JiraStatus.class; break;
            case 3: clazz = Priority.class; break;
        }
        return clazz;
    }

    private static Object[][] data(NbJiraIssue issue) {
        JiraRepository repository = issue.getRepository();
        IssueCache cache = repository.getIssueCache();
        List<String> keys = issue.getSubtaskKeys();
        Object[][] data = new Object[keys.size()][];
        int count = 0;
        for (String key : keys) {
            NbJiraIssue subTask = (NbJiraIssue)cache.getIssue(key);
            if (subTask == null) {
                subTask = (NbJiraIssue)repository.getIssue(key);
            }
            data[count] = new Object[] {key, subTask.getSummary(), subTask.getStatus(), subTask.getPriority()};
            count++;
        }
        return data;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

}
