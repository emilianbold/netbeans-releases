/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.ruby.testrunner.ui;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Panel containing the toolbar and the tree of test results.
 *
 * @author  Marian Petras
 */
public final class StatisticsPanel extends JPanel implements ItemListener {
    
    /** */
    private final ResultPanelTree treePanel;
    /** */
    private JToggleButton btnFilter;
    /** */
    private String tooltipShowAll;
    /** */
    private String tooltipShowFailures;

    /**
     */
    public StatisticsPanel(final ResultDisplayHandler displayHandler) {
        super(new BorderLayout(0, 0));

        JComponent toolbar = createToolbar();
        treePanel = new ResultPanelTree(displayHandler);
        treePanel.setFiltered(btnFilter.isSelected());

        add(toolbar, BorderLayout.WEST);
        add(treePanel, BorderLayout.CENTER);
    }

    /**
     */
    private JComponent createToolbar() {
        createFilterButton();

        JToolBar toolbar = new JToolBar(SwingConstants.VERTICAL);
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));
        toolbar.add(btnFilter);
        toolbar.add(Box.createHorizontalGlue());
        
        toolbar.setFocusable(false);
        toolbar.setFloatable(false);
        toolbar.setBorderPainted(false);
        
        return toolbar;
    }
    
    /**
     */
    private void createFilterButton() {
        btnFilter = new JToggleButton(new ImageIcon(
                Utilities.loadImage(
                    "org/netbeans/modules/ruby/testrunner/ui/res/filter.png", //NOI18N
                    true)));
        btnFilter.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(getClass(), "ACSN_FilterButton"));  //NOI18N
        btnFilter.addItemListener(this);
        
        updateFilterButtonLabel();
    }
    
    /**
     */
    private void updateFilterButtonLabel() {
        if (tooltipShowAll == null) {
            tooltipShowAll = NbBundle.getMessage(
                    getClass(),
                    "MultiviewPanel.btnFilter.showAll.tooltip");        //NOI18N
            tooltipShowFailures = NbBundle.getMessage(
                    getClass(),
                    "MultiviewPanel.btnFilter.showFailures.tooltip");   //NOI18N
        }
        btnFilter.setToolTipText(btnFilter.isSelected() ? tooltipShowAll
                                                        : tooltipShowFailures);
    }
    
    /**
     */
    public void itemStateChanged(ItemEvent e) {
        /* called when the Filter button is toggled. */
        treePanel.setFiltered(btnFilter.isSelected());
        updateFilterButtonLabel();
    }
    
    /**
     */
    void displayReport(final Report report) {
        treePanel.displayReport(report);
        
        btnFilter.setEnabled(
            treePanel.getSuccessDisplayedLevel() != RootNode.ALL_PASSED_ABSENT);
    }
    
    /**
     */
    void displayReports(final List<Report> reports) {
        if (reports.isEmpty()) {
            return;
        }
        
        treePanel.displayReports(reports);
        
        btnFilter.setEnabled(
            treePanel.getSuccessDisplayedLevel() != RootNode.ALL_PASSED_ABSENT);
    }
    
    /**
     * Displays a message about a running suite.
     *
     * @param  suiteName  name of the running suite,
     *                    or {@code ANONYMOUS_SUITE} for anonymous suites
     * @see  ResultDisplayHandler#ANONYMOUS_SUITE
     */
    void displaySuiteRunning(final String suiteName) {
        treePanel.displaySuiteRunning(suiteName);
    }
    
    /**
     */
    void displayMsg(final String msg) {
        treePanel.displayMsg(msg);
    }
    
}
