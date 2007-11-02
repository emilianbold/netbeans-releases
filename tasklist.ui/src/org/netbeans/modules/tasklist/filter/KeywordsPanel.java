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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.tasklist.filter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.UIManager;
import org.openide.util.NbBundle;

/**
 * Panel which is used to create a filter for the tasklist
 * The GUI is based on the one in Mozilla's mail tool.
 * Please read comment at the beginning of initA11y before editing
 * this file using the form builder.
 *
 * @author Tor Norbye
 */
final class KeywordsPanel extends JPanel implements ActionListener, PropertyChangeListener {

    private static final long serialVersionUID = 1;

//    private FilteredTopComponent view;
    private KeywordsFilter filter;

    // head of subpanels chain
    private FilterSubpanel subpanel;

    /**
     * Creates new form FilterPanel. 
     *
     * @param view view with the given filter
     * @param filter filter to be edited. Can be null and in that case
     *               all fields are disabled.
     */
    public KeywordsPanel( KeywordsFilter filter ) {
        this.filter = filter;

        initComponents();
        initA11y();
        
        if( "Metal".equals( UIManager.getLookAndFeel().getID() ) ) //NOI18N
            setOpaque( true );
        else
            setOpaque( false );

        // it's not generated by form editor
        JPanel topAlign = new JPanel();
        topAlign.setLayout(new BorderLayout());
        topAlign.add(conditionsPanel, BorderLayout.NORTH);
        conditionsScrollPane.setViewportView(topAlign);
        // compute 80x10 chars space in scroll pane
        FontMetrics fm = getFontMetrics(getFont());
        int width = fm.charWidth('n') * 80;  // NOI18N
        int height = fm.getHeight() * 10;
        conditionsScrollPane.setPreferredSize(new java.awt.Dimension(width, height));

        Color background = (Color)UIManager.get("Table.background"); //NOI18N
        conditionsPanel.setBackground(background);
        topAlign.setBackground(background);

        moreButton.addActionListener(this);
        fewerButton.addActionListener(this);
        matchAllRadio.addActionListener(this);
        matchAnyRadio.addActionListener(this);

        showFilter(filter);
        updateSensitivity();
    }

    /**
     * Links to next filter customizer (or null).
     */
    public void initSubpanel(FilterSubpanel next) {
        assert subpanel == null;
        subpanel = next;
    }

    /**
     * Show the given filter in the GUI
     */
    private void showFilter(KeywordsFilter filter) {
        if (filter != null) {
            if (filter.matchAll()) {
                matchAllRadio.setSelected(true);
            } else {
                matchAnyRadio.setSelected(true);
            }
            conditionsPanel.removeAll();
            List conditions = filter.getConditions();
            if (conditions.size() > 0) {
                Iterator it = conditions.iterator();
                while (it.hasNext()) {
                    AppliedFilterCondition cond = (AppliedFilterCondition) it.next();
                    addCondition(false, cond);
                }
            } else {
                addCondition(false, null);
            }
        } else {
            this.setEnabled(false);
            matchAnyRadio.setEnabled(false);
            matchAllRadio.setEnabled(false);
            conditionsPanel.setEnabled(false);
            moreButton.setEnabled(false);
            fewerButton.setEnabled(false);
        }
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        matchGroup.add(matchAllRadio);
        org.openide.awt.Mnemonics.setLocalizedText(matchAllRadio, NbBundle.getMessage(KeywordsPanel.class, "MatchAll")); // NOI18N(); // NOI18N
        matchAllRadio.setToolTipText(org.openide.util.NbBundle.getMessage(KeywordsPanel.class, "HINT_MatchAll")); // NOI18N
        matchAllRadio.setOpaque(false);

        matchGroup.add(matchAnyRadio);
        matchAnyRadio.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(matchAnyRadio, NbBundle.getMessage(KeywordsPanel.class, "MatchAny")); // NOI18N(); // NOI18N
        matchAnyRadio.setToolTipText(org.openide.util.NbBundle.getMessage(KeywordsPanel.class, "HINT_MatchAny")); // NOI18N
        matchAnyRadio.setOpaque(false);

        conditionsScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        conditionsScrollPane.setAlignmentX(0.0F);
        conditionsScrollPane.setAlignmentY(0.0F);
        conditionsScrollPane.setPreferredSize(new java.awt.Dimension(32767, 32767));

        conditionsPanel.setLayout(new java.awt.GridBagLayout());
        conditionsScrollPane.setViewportView(conditionsPanel);

        org.openide.awt.Mnemonics.setLocalizedText(fewerButton, NbBundle.getMessage(KeywordsPanel.class, "Fewer")); // NOI18N(); // NOI18N
        fewerButton.setToolTipText(org.openide.util.NbBundle.getMessage(KeywordsPanel.class, "HINT_Fewer")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(moreButton, NbBundle.getMessage(KeywordsPanel.class, "More")); // NOI18N(); // NOI18N
        moreButton.setToolTipText(org.openide.util.NbBundle.getMessage(KeywordsPanel.class, "HINT_More")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(moreButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(fewerButton))
                    .add(layout.createSequentialGroup()
                        .add(matchAllRadio, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(matchAnyRadio, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(278, 278, 278))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, conditionsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 584, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(matchAllRadio)
                    .add(matchAnyRadio))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(conditionsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(fewerButton)
                    .add(moreButton))
                .addContainerGap())
        );

        conditionsScrollPane.getAccessibleContext().setAccessibleName(null);
        conditionsScrollPane.getAccessibleContext().setAccessibleDescription(null);
    }// </editor-fold>//GEN-END:initComponents

    private void initA11y() {
        // accessible
        this.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(KeywordsPanel.class,
                                    "ACSD_Filter")); // NOI18N
        conditionsPanel.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(KeywordsPanel.class,
                                    "ACSN_Conditions")); // NOI18N
        conditionsPanel.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(KeywordsPanel.class,
                                    "ACSD_Conditions")); // NOI18N
    }

    /**
     * Adds a condition to the filter.
     *
     * @param revalidate true = invalidate() will be called
     * @param condition a condition or null
     */
    private ConditionPanel addCondition(boolean revalidate, AppliedFilterCondition condition) {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = new Insets(6, 0, 0, 0);
        gridBagConstraints.weightx = 1.0;
        ConditionPanel panel = new ConditionPanel(filter, condition);
        panel.addPropertyChangeListener(FilterCondition.PROP_VALUE_VALID, this);
        panel.setBackground((Color)UIManager.get("Table.background")); //NOI18N
        conditionsPanel.add(panel, gridBagConstraints);
        if (revalidate) {
            invalidate();
            getParent().validate();
            repaint();
        }
        putClientProperty(FilterCondition.PROP_VALUE_VALID, Boolean.valueOf(isValueValid()));
        return panel;
    }

    /** @return aggregated PROP_VALUE_VALID value */
    public final boolean isValueValid() {
        Component[] cps = conditionsPanel.getComponents();
        for (int i = 0; i < cps.length; i++) {
            ConditionPanel condition = (ConditionPanel) cps[i];
            if (condition != null) {
                if (condition.isValueValid() == false) {
                    return false;
                }
            }
        }
        return true;
    }

    // forward aggregated PROP_VALUE_VALID value
    public void propertyChange(PropertyChangeEvent evt) {
        putClientProperty(FilterCondition.PROP_VALUE_VALID, Boolean.valueOf(isValueValid()));
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent evt) {
        Object source = evt.getSource();
        if (source == moreButton) {
            ConditionPanel comp = addCondition(true, null);
            JScrollBar vsb = conditionsScrollPane.getVerticalScrollBar();
            vsb.setValue(vsb.getMaximum());
            comp.focusPropertyCombo();
        } else if (source == fewerButton) {
            conditionsPanel.remove(conditionsPanel.getComponentCount() - 1);
            invalidate();
            getParent().validate();
            repaint();
        }
        updateSensitivity();
        putClientProperty(FilterCondition.PROP_VALUE_VALID, Boolean.valueOf(isValueValid()));
    }

    /**
     * Updates enabled/disabled state of the "fewer" button
     */
    private void updateSensitivity() {
        int n = conditionsPanel.getComponentCount();
        fewerButton.setEnabled(n > 0);
    }

    /**
     * Return a filter corresponding to what is in the GUI
     *
     * @return filter or null
     */
    public KeywordsFilter getFilter() {
      if (filter != null) {
        // Get conditions
        Component[] cps = conditionsPanel.getComponents();
        ArrayList<AppliedFilterCondition> conditions = new ArrayList<AppliedFilterCondition>(cps.length);
        for (int i = 0; i < cps.length; i++) {
            AppliedFilterCondition condition = ((ConditionPanel)cps[i]).getCondition();
            if (condition != null) {
                conditions.add(condition);
            }
        }
        filter.setConditions(conditions);
        filter.setMatchAll(matchAllRadio.isSelected());
        if (subpanel != null) {
            subpanel.updateFilter(filter);
        }
      }
      return filter;

    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JPanel conditionsPanel = new javax.swing.JPanel();
    final javax.swing.JScrollPane conditionsScrollPane = new javax.swing.JScrollPane();
    final javax.swing.JButton fewerButton = new javax.swing.JButton();
    final javax.swing.JRadioButton matchAllRadio = new javax.swing.JRadioButton();
    final javax.swing.JRadioButton matchAnyRadio = new javax.swing.JRadioButton();
    final javax.swing.ButtonGroup matchGroup = new javax.swing.ButtonGroup();
    final javax.swing.JButton moreButton = new javax.swing.JButton();
    final javax.swing.ButtonGroup subtaskGroup = new javax.swing.ButtonGroup();
    // End of variables declaration//GEN-END:variables

}
