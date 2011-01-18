/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.gsf.testrunner.api;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Panel containing the toolbar and the tree of test results.
 *
 * @author  Marian Petras
 */
final class StatisticsPanel extends JPanel {
    
    /** */
    private final ResultPanelTree treePanel;
    /** */
    private JToggleButton btnShowPassed;
    private JToggleButton btnShowFailed;
    private JToggleButton btnShowError;

    /**
     * Rerun button for running (all) tests again.
     */
    private JButton rerunButton;
    private JButton rerunFailedButton;

    private JButton nextFailure;

    private JButton previousFailure;
    
    private final ResultDisplayHandler displayHandler;

    int filterMask = Status.PASSED.getBitMask();

    private static final Icon rerunIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/gsf/testrunner/resources/rerun.png", true);
    private static final Icon rerunFailedIcon = ImageUtilities.image2Icon(ImageUtilities.mergeImages(
                            ImageUtilities.loadImage("org/netbeans/modules/gsf/testrunner/resources/rerun.png"), //NOI18N
                            ImageUtilities.loadImage("org/netbeans/modules/gsf/testrunner/resources/error-badge.gif"), //NOI18N
                            8, 8));

    /**
     */
    public StatisticsPanel(final ResultDisplayHandler displayHandler) {
        super(new BorderLayout(0, 0));
        this.displayHandler = displayHandler;
        JComponent toolbar = createToolbar();
        treePanel = new ResultPanelTree(displayHandler, this);
        treePanel.setFilterMask(filterMask);

        add(toolbar, BorderLayout.WEST);
        add(treePanel, BorderLayout.CENTER);
    }

    /**
     */
    private JComponent createToolbar() {
        createShowButtons();
        createRerunButtons();
        createNextPrevFailureButtons();

        JToolBar toolbar = new JToolBar(SwingConstants.VERTICAL);
        toolbar.add(rerunButton);
        toolbar.add(rerunFailedButton);
        toolbar.add(new JToolBar.Separator());
        toolbar.add(btnShowPassed);
        toolbar.add(btnShowFailed);
        toolbar.add(btnShowError);
        toolbar.add(new JToolBar.Separator());
        toolbar.add(previousFailure);
        toolbar.add(nextFailure);
        
        toolbar.setFocusable(false);
        toolbar.setRollover(true);
        toolbar.setFloatable(false);
        toolbar.setBorder(BorderFactory.createEtchedBorder());
        return toolbar;
    }
    
    private void createRerunButtons() {
        rerunButton = new JButton(rerunIcon);
        rerunButton.setEnabled(false);
        rerunButton.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(getClass(), "ACSN_RerunButton"));  //NOI18N
        rerunButton.setToolTipText(NbBundle.getMessage(StatisticsPanel.class, "MultiviewPanel.rerunButton.tooltip"));

        rerunFailedButton = new JButton(rerunFailedIcon);
        rerunFailedButton.setEnabled(false);
        rerunFailedButton.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(getClass(), "ACSN_RerunFailedButton"));  //NOI18N
        rerunFailedButton.setToolTipText(NbBundle.getMessage(StatisticsPanel.class, "MultiviewPanel.rerunFailedButton.tooltip"));

        final RerunHandler rerunHandler = displayHandler.getSession().getRerunHandler();
        if (rerunHandler != null) {
            rerunButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    rerunHandler.rerun();
                }
            });
            rerunFailedButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    rerunHandler.rerun(treePanel.getFailedTests());
                }
            });
            rerunHandler.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    updateButtons();
                }
            });
            updateButtons();
        }
    }

    void updateButtons(){
        RerunHandler rerunHandler = displayHandler.getSession().getRerunHandler();
        if (rerunHandler == null) {
            return;
        }
        rerunButton.setEnabled(displayHandler.sessionFinished &&
                               rerunHandler.enabled(RerunType.ALL));
        rerunFailedButton.setEnabled(displayHandler.sessionFinished && 
                                     rerunHandler.enabled(RerunType.CUSTOM) &&
                                     !treePanel.getFailedTests().isEmpty());
    }

    private void createShowButtons() {
        btnShowPassed = newShowButton(
               "org/netbeans/modules/gsf/testrunner/resources/ok_16.png",
               "StatisticsPanel.btnShowPassed",
               "ACSN_ShowPassedButton",
               Status.PASSED);
        btnShowFailed = newShowButton(
               "org/netbeans/modules/gsf/testrunner/resources/warning_16.png",
               "StatisticsPanel.btnShowFailed",
               "ACSN_ShowFailedButton",
               Status.FAILED);
        btnShowError = newShowButton(
               "org/netbeans/modules/gsf/testrunner/resources/error_16.png",
               "StatisticsPanel.btnShowError",
               "ACSN_ShowErrorButton",
               Status.ERROR);
    }

    private JToggleButton newShowButton(String iconId,
                                          String tooltipId,
                                          String accessibleNameId,
                                          Status status) {
        JToggleButton btn =
                new JToggleButton(ImageUtilities.loadImageIcon(iconId, true));
        btn.setToolTipText(NbBundle.getMessage(getClass(), tooltipId));
        final String acsn = NbBundle.getMessage(getClass(), accessibleNameId);
        btn.getAccessibleContext().setAccessibleName(acsn);
        btn.setSelected((filterMask & status.getBitMask()) == 0);
        btn.addItemListener(new FilterItemListener(status));
        return btn;
    }

    void copyFilterMask(StatisticsPanel sp) {
        filterMask = sp.filterMask;
        updateShowButtons();
    }

    private void updateShowButtons() {
        btnShowPassed.setSelected((filterMask & Status.PASSED.getBitMask()) == 0);
        btnShowFailed.setSelected((filterMask & Status.FAILED.getBitMask()) == 0);
        btnShowError.setSelected((filterMask & Status.ERROR.getBitMask()) == 0);
    }

    private void createNextPrevFailureButtons() {
        nextFailure = new JButton(ImageUtilities.loadImageIcon("org/netbeans/modules/gsf/testrunner/resources/nextmatch.png", true));
        nextFailure.setToolTipText(NbBundle.getMessage(StatisticsPanel.class, "MSG_NextFailure"));
        nextFailure.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                selectNextFailure();
            }
        });

        previousFailure = new JButton(ImageUtilities.loadImageIcon("org/netbeans/modules/gsf/testrunner/resources/prevmatch.png", true));

        previousFailure.setToolTipText(NbBundle.getMessage(StatisticsPanel.class, "MSG_PreviousFailure"));
        previousFailure.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                selectPreviousFailure();
            }
        });
    }

    void selectPreviousFailure() {
        treePanel.selectPreviousFailure();
    }

    void selectNextFailure() {
        treePanel.selectNextFailure();
    }
    
    /**
     */
    void displayReport(final Report report) {
        treePanel.displayReport(report);        
//        btnFilter.setEnabled(
//          treePanel.getSuccessDisplayedLevel() != RootNode.ALL_PASSED_ABSENT);
    }
    
    /**
     */
    void displayReports(final List<Report> reports) {
        if (reports.isEmpty()) {
            return;
        }
        
        treePanel.displayReports(reports);
        
//        btnFilter.setEnabled(
//          treePanel.getSuccessDisplayedLevel() != RootNode.ALL_PASSED_ABSENT);
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

    private class FilterItemListener implements ItemListener {
        private int itemMask;

        public FilterItemListener(Status status) {
            this.itemMask = status.getBitMask();
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            switch(e.getStateChange()) {
                case ItemEvent.SELECTED:
                    filterMask &= ~itemMask;
                    treePanel.setFilterMask(filterMask);
                    return;
                case ItemEvent.DESELECTED:
                    filterMask |= itemMask;
                    treePanel.setFilterMask(filterMask);
                    return;
            }
        }
    } // FilterItemListener

}
