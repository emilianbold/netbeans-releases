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

package org.netbeans.modules.ods.tasks.issue;

import java.awt.Font;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.util.UIUtils;
import org.openide.util.HelpCtx;

/**
 *
 * @author Tomas Stupka, Jan Stola
 */
public class C2CIssueController extends BugtrackingController {
    private JComponent component;
    private final IssuePanel panel;

    public C2CIssueController(C2CIssue issue) {
        panel = new IssuePanel();
        panel.setIssue(issue);
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getViewport().setBackground(panel.getBackground());
        scrollPane.setBorder(null);
        Font font = UIManager.getFont("Label.font"); // NOI18N
        if (font != null) {
            int size = (int)(font.getSize()*1.5);
            scrollPane.getHorizontalScrollBar().setUnitIncrement(size);
            scrollPane.getVerticalScrollBar().setUnitIncrement(size);
        }
        UIUtils.keepFocusedComponentVisible(panel);
        component = scrollPane;
    }

    @Override
    public JComponent getComponent() {
        return component;
    }

    @Override
    public void opened() {
        C2CIssue issue = panel.getIssue();
        if (issue != null) {
            panel.opened();
            issue.opened();
        }
    }

    @Override
    public void closed() {
        C2CIssue issue = panel.getIssue();
        if (issue != null) {
            issue.closed();
            panel.closed();
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.ods.tasks.issue.C2CIssue"); // NOI18N
    }

    @Override
    public boolean isValid() {
        return true; // PENDING
    }

    @Override
    public void applyChanges() {
    }

    void refreshViewData(boolean force) {
        panel.reloadFormInAWT(force);
    }

}
