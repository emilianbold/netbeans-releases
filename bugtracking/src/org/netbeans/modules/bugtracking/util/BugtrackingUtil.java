/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.util;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.bugtracking.ui.issue.IssueTopComponent;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.ui.selectors.RepositorySelector;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.windows.TopComponent;

/**
 *
 * @author Tomas Stupka
 */
public class BugtrackingUtil {

    public static Logger LOG = Logger.getLogger("org.netbeans.modules.issues");

    public static boolean show(JPanel panel, String title, String okName) {
        JButton ok = new JButton(okName);
        JButton cancel = new JButton("Cancel");
        final DialogDescriptor dd = new DialogDescriptor(panel, title, true, new Object[]{ok, cancel}, ok, DialogDescriptor.DEFAULT_ALIGN, null, null);
        return DialogDisplayer.getDefault().notify(dd) == ok;
    }

    public static boolean showControllerComponent(final BugtrackingController bc) {
        JComponent com = bc.getComponent();
        final JButton ok = new JButton("Ok");
        JButton cancel = new JButton("Cancel");
        final DialogDescriptor dd = new DialogDescriptor(com, "Repository?", true, new Object[]{ok, cancel}, ok, DialogDescriptor.DEFAULT_ALIGN, bc.getHelpContext(), null);
        dd.setOptions(new Object[]{ok, cancel});
        dd.setModal(true);
        dd.setHelpCtx(bc.getHelpContext());
        dd.setValid(false);
        ok.setEnabled(false);
        bc.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                boolean valid = bc.isValid();
                dd.setValid(valid);
                ok.setEnabled(valid);
            }
        });
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        return dd.getValue() == ok;
    }

    public static Issue[] getOpenIssues() {
        Set<TopComponent> tcs = TopComponent.getRegistry().getOpened();
        List<Issue> issues = new ArrayList<Issue>();
        for (TopComponent tc : tcs) {
            if(tc instanceof IssueTopComponent) {
                issues.add(((IssueTopComponent)tc).getIssue());
            }
        }
        return issues.toArray(new Issue[issues.size()]);
    }

    public static Issue[] getByIdOrSummary(Issue[] issues, String criteria) {
        if(criteria == null) {
            return issues;
        }
        criteria = criteria.trim();
        if(criteria.equals("")) {
            return issues;
        }
        List<Issue> ret = new ArrayList<Issue>();
        for (Issue issue : issues) {
            if(issue.getID().equals(criteria) ||
               issue.getSummary().indexOf(criteria) > -1)
            {
                ret.add(issue);
            }  
        }
        return ret.toArray(new Issue[ret.size()]);
    }

    public static Repository createRepository() {
        RepositorySelector rs = new RepositorySelector();
        return rs.create();
    }

    public static Repository getBugtrackingOwner(File file) {
        return BugtrackingOwnerQuery.getBugtrackingOwner(file);
    }

    public static String scramble(String str) {
        return Scrambler.getInstance().scramble(str);
    }

    public static String descramble(String str) {
        return Scrambler.getInstance().descramble(str);
    }

    public static int[] getIssueSpans(String text) {
        return IssueFinder.getIssueSpans(text);
    }
}
