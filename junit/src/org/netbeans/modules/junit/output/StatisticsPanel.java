/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit.output;

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
                    "org/netbeans/modules/junit/output/res/filter.png", //NOI18N
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
    void displayReports(final List/*<Report>*/ reports) {
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
