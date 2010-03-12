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


package org.netbeans.modules.quicksearch;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.modules.quicksearch.recent.RecentSearches;
import org.netbeans.modules.quicksearch.ResultsModel.ItemResult;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 * Component representing drop down for quick search
 * @author  Jan Becicka
 */
public class QuickSearchPopup extends javax.swing.JPanel 
        implements ListDataListener, ActionListener, TaskListener, Runnable {

    private AbstractQuickSearchComboBar comboBar;

    private ResultsModel rModel;

    /* Rect to store repetitive bounds computation */
    private Rectangle popupBounds = new Rectangle();

    private Timer updateTimer;
    private static final int COALESCE_TIME = 600;

    /** text to search for */
    private String searchedText;

    private int catWidth;
    private int resultWidth;
    private Task evalTask;

    public QuickSearchPopup (AbstractQuickSearchComboBar comboBar) {
        this.comboBar = comboBar;
        initComponents();
        rModel = ResultsModel.getInstance();
        jList1.setModel(rModel);
        jList1.setCellRenderer(new SearchResultRender(this));
        rModel.addListDataListener(this);

        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) //NOI18N
            jList1.setBackground(QuickSearchComboBar.getResultBackground());

        updateStatusPanel();
        setVisible(false);
    }

    void invoke() {
        ItemResult result = ((ItemResult) jList1.getModel().getElementAt(jList1.getSelectedIndex()));
        if (result != null) {
            RecentSearches.getDefault().add(result);
            result.getAction().run();
            clearModel();
        }
    }

    void selectNext() {
        int oldSel = jList1.getSelectedIndex();
        if (oldSel >= 0 && oldSel < jList1.getModel().getSize() - 1) {
            jList1.setSelectedIndex(oldSel + 1);
        }
        if (jList1.getModel().getSize() > 0) {
            setVisible(true);
        }
    }

    void selectPrev() {
        int oldSel = jList1.getSelectedIndex();
        if (oldSel > 0) {
            jList1.setSelectedIndex(oldSel - 1);
        }
        if (jList1.getModel().getSize() > 0) {
            setVisible(true);
        }
    }

    public JList getList() {
        return jList1;
    }

    public void clearModel () {
        rModel.setContent(null);
    }

    public void maybeEvaluate (String text) {
        this.searchedText = text;

        if (updateTimer == null) {
            updateTimer = new Timer(COALESCE_TIME, this);
        }

        if (!updateTimer.isRunning()) {
            // first change in possible flurry, start timer
            updateTimer.start();
        } else {
            // text change came too fast, let's wait until user calms down :)
            updateTimer.restart();
        }
    }

    /** implementation of ActionListener, called by timer,
     * actually runs search */
    public void actionPerformed(ActionEvent e) {
        updateTimer.stop();
        // search only if we are not cancelled already
        if (comboBar.getCommand().isFocusOwner()) {
            if (evalTask != null) {
                evalTask.removeTaskListener(this);
            }
            evalTask = CommandEvaluator.evaluate(searchedText, rModel);
            evalTask.addTaskListener(this);
            // start waiting on all providers execution
            RequestProcessor.getDefault().post(evalTask);
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        statusPanel = new javax.swing.JPanel();
        searchingSep = new javax.swing.JSeparator();
        searchingLabel = new javax.swing.JLabel();
        noResultsLabel = new javax.swing.JLabel();
        hintSep = new javax.swing.JSeparator();
        hintLabel = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createLineBorder(QuickSearchComboBar.getPopupBorderColor()));
        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane1.setViewportBorder(null);

        jList1.setFocusable(false);
        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jList1MouseClicked(evt);
            }
        });
        jList1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jList1MouseMoved(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        statusPanel.setBackground(QuickSearchComboBar.getResultBackground());
        statusPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        statusPanel.add(searchingSep, gridBagConstraints);

        searchingLabel.setText(org.openide.util.NbBundle.getMessage(QuickSearchPopup.class, "QuickSearchPopup.searchingLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        statusPanel.add(searchingLabel, gridBagConstraints);

        noResultsLabel.setForeground(java.awt.Color.red);
        noResultsLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        noResultsLabel.setText(org.openide.util.NbBundle.getMessage(QuickSearchPopup.class, "QuickSearchPopup.noResultsLabel.text")); // NOI18N
        noResultsLabel.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        statusPanel.add(noResultsLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        statusPanel.add(hintSep, gridBagConstraints);

        hintLabel.setBackground(QuickSearchComboBar.getResultBackground());
        hintLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        statusPanel.add(hintLabel, gridBagConstraints);

        add(statusPanel, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

private void jList1MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MouseMoved
    // selection follows mouse move
    Point loc = evt.getPoint();
    int index = jList1.locationToIndex(loc);
    if (index == -1) {
        return;
    }
    Rectangle rect = jList1.getCellBounds(index, index);
    if (rect != null && rect.contains(loc)) {
        jList1.setSelectedIndex(index);
    }

}//GEN-LAST:event_jList1MouseMoved

private void jList1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MouseClicked
    if (!SwingUtilities.isLeftMouseButton(evt)) {
        return;
    }
    // mouse left button click works the same as pressing Enter key
    comboBar.invokeSelectedItem();

}//GEN-LAST:event_jList1MouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel hintLabel;
    private javax.swing.JSeparator hintSep;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel noResultsLabel;
    private javax.swing.JLabel searchingLabel;
    private javax.swing.JSeparator searchingSep;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables


    /*** impl of reactions to results data change */

    public void intervalAdded(ListDataEvent e) {
        updatePopup();
    }

    public void intervalRemoved(ListDataEvent e) {
        updatePopup();
    }

    public void contentsChanged(ListDataEvent e) {
        updatePopup();
    }

    /**
     * Updates size and visibility of this panel according to model content
     */
    public void updatePopup () {
        int modelSize = rModel.getSize();

        // plug this popup into layered pane if needed
        JLayeredPane lPane = JLayeredPane.getLayeredPaneAbove(comboBar);
        if (lPane == null) {
            // #162075 - return when comboBar not yet seeded in AWT hierarchy
            return;
        }
        if (!isDisplayable()) {
            lPane.add(this, new Integer(JLayeredPane.POPUP_LAYER + 1) );
        }

        boolean statusVisible = updateStatusPanel();

        computePopupBounds(popupBounds, lPane, modelSize);
        setBounds(popupBounds);

        // popup visibility constraints
        if ((modelSize > 0 || statusVisible) && comboBar.getCommand().isFocusOwner()) {
            if (modelSize > 0 && !isVisible()) {
                jList1.setSelectedIndex(0);
            }
            if (jList1.getSelectedIndex() >= modelSize) {
                jList1.setSelectedIndex(modelSize - 1);
            }
            if (explicitlyInvoked || !searchedText.isEmpty()) {
                setVisible(true);
            }
        } else {
            setVisible(false);
        }
        explicitlyInvoked = false;

        // needed on JDK 1.5.x to repaint correctly
        revalidate();
    }
    private boolean explicitlyInvoked = false;
    /** User actually pressed Ctrl-I; display popup even just for Recent Searches. */
    void explicitlyInvoked() {
        explicitlyInvoked = true;
    }

    public int getCategoryWidth () {
        if (catWidth <= 0) {
            catWidth = computeWidth(jList1, 20, 30);
        }
        return catWidth;
    }

    public int getResultWidth () {
        if (resultWidth <= 0) {
            resultWidth = computeWidth(jList1, 42, 50);
        }
        return resultWidth;
    }

    /** Implementation of TaskListener, listen to when providers are finished
     * with their searching work
     */
    public void taskFinished(Task task) {
        evalTask = null;
        // update UI in ED thread
        if (SwingUtilities.isEventDispatchThread()) {
            run();
        } else {
            SwingUtilities.invokeLater(this);
        }
    }

    /** Runnable implementation, updates popup */
    public void run() {
        updatePopup();
    }

    private void computePopupBounds (Rectangle result, JLayeredPane lPane, int modelSize) {
        Dimension cSize = comboBar.getSize();
        int width = getCategoryWidth() + getResultWidth() + 3;
        Point location = new Point(cSize.width - width - 1, comboBar.getBottomLineY() - 1);
        if (SwingUtilities.getWindowAncestor(comboBar) != null) {
            location = SwingUtilities.convertPoint(comboBar, location, lPane);
        }
        result.setLocation(location);

        // hack to make jList.getpreferredSize work correctly
        // JList is listening on ResultsModel same as us and order of listeners
        // is undefined, so we have to force update of JList's layout data
        jList1.setFixedCellHeight(15);
        jList1.setFixedCellHeight(-1);
        // end of hack

        jList1.setVisibleRowCount(modelSize);
        Dimension preferredSize = jList1.getPreferredSize();

        preferredSize.width = width;
        preferredSize.height += statusPanel.getPreferredSize().height + 3;

        result.setSize(preferredSize);
    }

    /** Computes width of string up to maxCharCount, with font of given JComponent
     * and with maximum percentage of owning Window that can be taken */
    private static int computeWidth (JComponent comp, int maxCharCount, int percent) {
        FontMetrics fm = comp.getFontMetrics(comp.getFont());
        int charW = fm.charWidth('X');
        int result = charW * maxCharCount;
        // limit width to 50% of containing window
        Window w = SwingUtilities.windowForComponent(comp);
        if (w != null) {
            result = Math.min(result, w.getWidth() * percent / 100);
        }
        return result;
    }

    /** Updates visibility and content of status labels.
     *
     * @return true when update panel should be visible (some its part is visible),
     * false otherwise
     */
    private boolean updateStatusPanel () {
        boolean shouldBeVisible = false;

        boolean isInProgress = evalTask != null;
        searchingSep.setVisible(isInProgress);
        searchingLabel.setVisible(isInProgress);
        if (comboBar instanceof QuickSearchComboBar) {
            if (isInProgress) {
                ((QuickSearchComboBar) comboBar).startProgressAnimation();
            } else {
                ((QuickSearchComboBar) comboBar).stopProgressAnimation();
            }
        }
        shouldBeVisible = shouldBeVisible || isInProgress;

        boolean searchedNotEmpty = searchedText != null && searchedText.trim().length() > 0;
        boolean areNoResults = rModel.getSize() <= 0 && searchedNotEmpty && !isInProgress;
        noResultsLabel.setVisible(areNoResults);
        comboBar.setNoResults(areNoResults);
        shouldBeVisible = shouldBeVisible || areNoResults;

        hintLabel.setText(getHintText());
        boolean isNarrowed = CommandEvaluator.getEvalCat() != null && searchedNotEmpty;
        hintSep.setVisible(isNarrowed);
        hintLabel.setVisible(isNarrowed);
        shouldBeVisible = shouldBeVisible || isNarrowed;

        return shouldBeVisible;
    }

    private String getHintText () {
        ProviderModel.Category evalCat = CommandEvaluator.getEvalCat();
        if (evalCat == null) {
            return null;
        }
        return NbBundle.getMessage(QuickSearchPopup.class, "QuickSearchPopup.hintLabel.text",
                evalCat.getDisplayName(), SearchResultRender.getKeyStrokeAsText(
                comboBar.getKeyStroke()));
    }

}
