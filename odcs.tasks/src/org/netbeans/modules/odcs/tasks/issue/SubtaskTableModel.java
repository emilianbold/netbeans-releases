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

import com.tasktop.c2c.server.tasks.domain.TaskStatus;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.swing.table.DefaultTableModel;
import org.netbeans.modules.bugtracking.cache.IssueCache;
import org.netbeans.modules.odcs.tasks.ODCS;
import org.netbeans.modules.odcs.tasks.repository.ODCSRepository;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Stola
 */
public class SubtaskTableModel extends DefaultTableModel {

    public SubtaskTableModel(ODCSIssue issue) {
        super(data(issue), columnNames());
    }

    private static String[] columnNames() {
        ResourceBundle bundle = NbBundle.getBundle(SubtaskTableModel.class);
        String subTask = bundle.getString("SubtaskTableModel.subTask"); // NOI18N
        String summary = bundle.getString("SubtaskTableModel.summary"); // NOI18N
        String issueType = bundle.getString("SubtaskTableModel.issueType"); // NOI18N
        String status = bundle.getString("SubtaskTableModel.status"); // NOI18N
        String priority = bundle.getString("SubtaskTableModel.priority"); // NOI18N
        return new String[] {subTask, summary, issueType, status, priority};
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        Class clazz = String.class;
        switch (columnIndex) {
            case 2: clazz = String.class; break;
            case 3: clazz = TaskStatus.class; break;
            case 4: clazz = com.tasktop.c2c.server.tasks.domain.Priority.class; break;
        }
        return clazz;
    }

    private static Object[][] data(ODCSIssue issue) {
        ODCSRepository repository = issue.getRepository();
        IssueCache cache = repository.getIssueCache();
        String[] subtasks = issue.getSubtasks();
        Object[][] data = new Object[subtasks.length][];
        int count = 0;
        for (String id : subtasks) {
            ODCSIssue subTask = (ODCSIssue)cache.getIssue(id);
            if (subTask == null) {
                subTask = (ODCSIssue)repository.getIssue(id);
            }
            if(subTask != null) {
                data[count] = new Object[] {
                    id,
                    subTask.getSummary(),
                    subTask.getType(),
                    subTask.getStatus(),
                    subTask.getPriority()
                };
                count++;
            } else {
                ODCS.LOG.log(Level.WARNING, "no subtask returned for key {0}", id); // NOI18N
            }
        }
        if(count < subtasks.length) {
            Object[][] ret = new Object[count][];
            System.arraycopy(data, 0, ret, 0, count);
            return ret;
        } else {
            return data;
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

}
