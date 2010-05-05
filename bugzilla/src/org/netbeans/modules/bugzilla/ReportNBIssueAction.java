/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.bugzilla;

import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;

import java.awt.event.ActionEvent;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugzilla.commands.ValidateCommand;
import org.netbeans.modules.bugzilla.repository.NBRepositorySupport;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * 
 * @author Tomas Stupka
 */
public class ReportNBIssueAction extends SystemAction {

    public ReportNBIssueAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ReportNBIssueAction.class, "CTL_ReportIssueAction");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ReportNBIssueAction.class);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                final BugzillaRepository repo = NBRepositorySupport.findNbRepository();
                if(!checkLogin(repo)) {
                    return;
                }
                Issue.open(repo, null);
            }
        });
    }

    static boolean checkLogin(final BugzillaRepository repo) {
        if(repo.getUsername() != null && !repo.getUsername().equals("")) {
            return true;
        }

        String errorMsg = NbBundle.getMessage(ReportNBIssueAction.class, "MSG_MISSING_USERNAME_PASSWORD");  // NOI18N
        while(NBLoginPanel.show(repo, errorMsg)) {

            ValidateCommand cmd = new ValidateCommand(repo.getTaskRepository());
            ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(ReportNBIssueAction.class, "MSG_CONNECTING_2_NBORG")); // NOI18N
            handle.start();
            try {
                repo.getExecutor().execute(cmd, false, false);
            } finally {
                handle.finish();
            }
            if(cmd.hasFailed()) {
                errorMsg = cmd.getErrorMessage();
                continue;
            }
            return true;
        }
        repo.setCredentials(null, null, null, null); // reset
        return false;
    }
}
