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
package org.netbeans.modules.cnd.discovery.performance;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Alexander Simon
 */
@Messages({
    "statistic.action.name.text=Project Performance Statistic",
    "statistic.title.text=Project Performance Statistic"
})
public class PerformanceStatisticAction extends NodeAction {
    protected final static boolean TEST_XREF = Boolean.getBoolean("test.xref.action"); // NOI18N

    private boolean running;
    private JMenuItem presenter;
    private boolean inited = false;
    private final boolean enabledAction;    

    public PerformanceStatisticAction() {
        enabledAction = TEST_XREF;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        JPanel panel = new StatisticPanel();
        DialogDescriptor descr = new DialogDescriptor(panel, Bundle.statistic_title_text());
        NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(descr));
        if (descr.getValue() != NotifyDescriptor.OK_OPTION) {
            return;
        }            
    }
    @Override
    public JMenuItem getMenuPresenter() {
        return getPresenter();
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return getPresenter();
    }

    private JMenuItem getPresenter() {
        if (!inited) {
            presenter = new JMenuItem();
            org.openide.awt.Actions.connect(presenter, (Action) this, true);
            inited = true;
        }
        if (enabledAction) {
            presenter.setVisible(PerformanceIssueDetector.getActiveInstance()  != null);
        } else {
            presenter.setVisible(false);
        }

        return presenter;
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (!enabledAction) {
            return false;
        }
        return PerformanceIssueDetector.getActiveInstance()  != null;
    }

    @Override
    public String getName() {
        return Bundle.statistic_action_name_text();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
