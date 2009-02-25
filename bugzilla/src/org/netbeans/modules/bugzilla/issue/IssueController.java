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

import java.text.SimpleDateFormat;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.openide.util.HelpCtx;

/**
 *
 * @author Tomas Stupka, Jan Stola
 */
public class IssueController extends BugtrackingController {
    private IssuePanel issuePanel = new IssuePanel();
    private DefaultListModel attachmentsModel;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("hh24:mmm:ss dd.mm.yyyy");
    private BugzillaIssue issue;

    public IssueController(BugzillaIssue issue) {
        this.issue = issue;
        issuePanel.setIssue(issue);
    }

    @Override
    public JComponent getComponent() {
        return issuePanel;
    }

    @Override
    public HelpCtx getHelpContext() {
        return new HelpCtx(org.netbeans.modules.bugzilla.issue.BugzillaIssue.class);
    }

    @Override
    public boolean isValid() {
        return true; // PENDING
        /*return !panel.summaryField.getText().trim().equals("") &&
               !panel.priorityField.getText().trim().equals("") &&
               !panel.summaryField.getText().trim().equals("") &&
               !panel.descTextArea.getText().trim().equals("") &&
               !panel.typeField.getText().trim().equals("");*/
    }

    @Override
    public void applyChanges() {
    }

    /*private void onResolve() {
        Set<TaskAttribute> attrs;
        try {
            BugzillaClient client = Bugzilla.getInstance().getRepositoryConnector().getClientManager().getClient(issue.getTaskRepository(), new NullProgressMonitor());
            List<String> res = client.getRepositoryConfiguration().getResolutions();
            resolvePanel.resolutionCBO.setModel(new DefaultComboBoxModel(res.toArray(new String[res.size()])));
            if (!BugzillaUtil.show(resolvePanel, "Got resolution?", "submit")) {
                return;
            }
            attrs = issue.getResolveAttributes((String) resolvePanel.resolutionCBO.getSelectedItem());
            RepositoryResponse rr = Bugzilla.getInstance().getRepositoryConnector().getTaskDataHandler().postTaskData(issue.getTaskRepository(), issue.getData(), attrs, new NullProgressMonitor());
            issue.refresh();
            refreshViewData();
        } catch (MalformedURLException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        } catch (CoreException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        }
    }*/

    void refreshViewData() {
        // PENDING
    }

}
