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
package org.netbeans.modules.bugtracking.ui.issue;

import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;

import java.awt.event.ActionEvent;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.util.BugtrackingOwnerSupport;
import org.openide.util.NbBundle;

/**
 * 
 * @author Tomas Stupka
 */
public class IssueAction extends SystemAction {

    public IssueAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    public String getName() {
        return NbBundle.getMessage(IssueAction.class, "CTL_IssueAction");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(IssueAction.class);
    }

    public void actionPerformed(ActionEvent ev) {
        openIssue();
    }

    public static void openIssue() {
        openIssue(null);
    }

    public static void openIssue(final Repository repository) {
//        final Repository repository;
        final boolean repositoryGiven = repository != null;

//        if (repository != null) {
//            repository = givenRepository;
//            repositoryGiven = true;
//            repositoryGiven = true;
//        } else {
//            repository = BugtrackingOwnerSupport.getInstance()
//                         .getRepository(BugtrackingOwnerSupport.ContextType
//                                        .SELECTED_FILE_AND_ALL_PROJECTS);
//            repositoryGiven = false;
//        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                IssueTopComponent tc = new IssueTopComponent();
                tc.initNewIssue(repository, !repositoryGiven);
                tc.open();
                tc.requestActive();
            }
        });
    }

    public static void openIssue(final Issue issue, final Repository repository,
                                 final boolean suggestedSelectionOnly) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                IssueTopComponent tc = null;
                if(issue != null) {
                    tc = IssueTopComponent.find(issue);
                }
                if(tc == null) {
                    tc = new IssueTopComponent();
                }
                tc.initNewIssue(repository, suggestedSelectionOnly);
                if(!tc.isOpened()) {
                    tc.open();
                }
                tc.requestActive();
            }
        });
    }

    public static void closeIssue(final Issue issue) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                IssueTopComponent tc = IssueTopComponent.find(issue);
                if(tc != null) {
                    tc.close();
                }
            }
        });
    }
}
