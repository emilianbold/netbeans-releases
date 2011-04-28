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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.form.layoutsupport.griddesigner;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.Insets;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Iterator;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.AbstractGridAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.GridActionPerformer;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.GridBoundsChange;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Stola, Petr Somol
 */
public class GridBagCustomizer implements GridCustomizer {

    private GridActionPerformer performer;
    private GridBagManager manager;

    public GridBagCustomizer(GridBagManager manager, GridActionPerformer performer) {
        this.manager = manager;
        this.performer = performer;
        initComponents();
    }
    
    /**
     * Step size in component size change (with Ctrl key pressed)
     */
    public static final int ACCELERATED_SIZE_CHANGE = 5;

    /**
     * Determines that anchor buttons represent absolute positioning using {@code GridBagConstraints} constants:
     * <code>CENTER</code>, <code>NORTH</code>, <code>NORTHEAST</code>,
     * <code>EAST</code>, <code>SOUTHEAST</code>, <code>SOUTH</code>,
     * <code>SOUTHWEST</code>, <code>WEST</code>, and <code>NORTHWEST</code>.
     */
    public static final int ANCHOR_ABSOLUTE = 1;

    /**
     * Determines that anchor buttons represent bi-directional-aware positioning using {@code GridBagConstraints} constants
     * <code>PAGE_END</code>,
     * <code>LINE_START</code>, <code>LINE_END</code>, 
     * <code>FIRST_LINE_START</code>, <code>FIRST_LINE_END</code>, 
     * <code>LAST_LINE_START</code> and <code>LAST_LINE_END</code>.  The
     */
    public static final int ANCHOR_BIDI = 2;

    /**
     * Determines that anchor buttons represent baseline-relative positioning using {@code GridBagConstraints} constants
     * <code>BASELINE</code>, <code>BASELINE_LEADING</code>,
     * <code>BASELINE_TRAILING</code>,
     * <code>ABOVE_BASELINE</code>, <code>ABOVE_BASELINE_LEADING</code>,
     * <code>ABOVE_BASELINE_TRAILING</code>,
     * <code>BELOW_BASELINE</code>, <code>BELOW_BASELINE_LEADING</code>,
     * and <code>BELOW_BASELINE_TRAILING</code>.
     */
    public static final int ANCHOR_BASELINE = 4;
    
    /** Parameter passing structure */
    private class PaddingChange {
        PaddingChange(final int xdifference, final int ydifference) {
            xdiff = xdifference;
            ydiff = ydifference;
        }
        final public int xdiff; /** horizontal padding increase or decrease */
        final public int ydiff; /** vertical padding increase or decrease */
        private PaddingChange() {xdiff = ydiff = 0;}
    }

    /** Parameter passing structure */
    private class FillChange {
        FillChange(final int newhfill, final int newvfill) {
            hfill = newhfill;
            vfill = newvfill;
        }
        final public int hfill; /** -1 = no change, 0 = NONE/VERTICAL, 1 = HORIZONTAL/BOTH */
        final public int vfill; /** -1 = no change, 0 = NONE/HORIZONTAL, 1= VERTICAL/BOTH */
        private FillChange() {hfill = vfill = -1;}
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        customizer = new javax.swing.JPanel();
        anchorLabel = new javax.swing.JLabel();
        anchorSeparator = new javax.swing.JSeparator();
        anchorPanel = new javax.swing.JPanel();
        nwAnchorButton = new javax.swing.JToggleButton();
        nAnchorButton = new javax.swing.JToggleButton();
        neAnchorButton = new javax.swing.JToggleButton();
        eAnchorButton = new javax.swing.JToggleButton();
        cAnchorButton = new javax.swing.JToggleButton();
        wAnchorButton = new javax.swing.JToggleButton();
        swAnchorButton = new javax.swing.JToggleButton();
        sAnchorButton = new javax.swing.JToggleButton();
        seAnchorButton = new javax.swing.JToggleButton();
        baselineAnchorButton = new javax.swing.JToggleButton();
        bidiAnchorButton = new javax.swing.JToggleButton();
        fillLabel = new javax.swing.JLabel();
        fillSeparator = new javax.swing.JSeparator();
        fillPanel = new javax.swing.JPanel();
        hFillButton = new javax.swing.JToggleButton();
        vFillButton = new javax.swing.JToggleButton();
        paddingLabel = new javax.swing.JLabel();
        paddingSeparator = new javax.swing.JSeparator();
        paddingPanel = new javax.swing.JPanel();
        hpadLabel = new javax.swing.JLabel();
        hminusPaddingButton = new javax.swing.JButton();
        hplusPaddingButton = new javax.swing.JButton();
        vpadLabel = new javax.swing.JLabel();
        vminusPaddingButton = new javax.swing.JButton();
        vplusPaddingButton = new javax.swing.JButton();
        bpadLabel = new javax.swing.JLabel();
        bminusPaddingButton = new javax.swing.JButton();
        bplusPaddingButton = new javax.swing.JButton();
        insetsLabel = new javax.swing.JLabel();
        insetsSeparator = new javax.swing.JSeparator();
        insetsPanel = new javax.swing.JPanel();
        topLeftCorner = new javax.swing.JLabel();
        vplusTopInsetButton = new javax.swing.JButton();
        vminusTopInsetButton = new javax.swing.JButton();
        topRightCorner = new javax.swing.JLabel();
        hplusLeftInsetButton = new javax.swing.JButton();
        hminusLeftInsetButton = new javax.swing.JButton();
        hminusRightInsetButton = new javax.swing.JButton();
        hplusRightInsetButton = new javax.swing.JButton();
        bottomLeftCorner = new javax.swing.JLabel();
        vminusBottomInsetButton = new javax.swing.JButton();
        vplusBottomInsetButton = new javax.swing.JButton();
        bottomRightCorner = new javax.swing.JLabel();
        vInsetLabel = new javax.swing.JLabel();
        vminusInsetButton = new javax.swing.JButton();
        vplusInsetButton = new javax.swing.JButton();
        hInsetLabel = new javax.swing.JLabel();
        hminusInsetButton = new javax.swing.JButton();
        hplusInsetButton = new javax.swing.JButton();
        bInsetLabel = new javax.swing.JLabel();
        bminusInsetButton = new javax.swing.JButton();
        bplusInsetButton = new javax.swing.JButton();
        gridSizelLabel = new javax.swing.JLabel();
        gridSizelSeparator = new javax.swing.JSeparator();

        FormListener formListener = new FormListener();

        anchorLabel.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.anchorLabel.text")); // NOI18N

        nwAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_nw.png"))); // NOI18N
        nwAnchorButton.setEnabled(false);
        nwAnchorButton.setFocusPainted(false);
        nwAnchorButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        nwAnchorButton.addActionListener(formListener);

        nAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_n.png"))); // NOI18N
        nAnchorButton.setEnabled(false);
        nAnchorButton.setFocusPainted(false);
        nAnchorButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        nAnchorButton.addActionListener(formListener);

        neAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_ne.png"))); // NOI18N
        neAnchorButton.setEnabled(false);
        neAnchorButton.setFocusPainted(false);
        neAnchorButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        neAnchorButton.addActionListener(formListener);

        eAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_e.png"))); // NOI18N
        eAnchorButton.setEnabled(false);
        eAnchorButton.setFocusPainted(false);
        eAnchorButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        eAnchorButton.addActionListener(formListener);

        cAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_c.png"))); // NOI18N
        cAnchorButton.setEnabled(false);
        cAnchorButton.setFocusPainted(false);
        cAnchorButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cAnchorButton.addActionListener(formListener);

        wAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_w.png"))); // NOI18N
        wAnchorButton.setEnabled(false);
        wAnchorButton.setFocusPainted(false);
        wAnchorButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        wAnchorButton.addActionListener(formListener);

        swAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_sw.png"))); // NOI18N
        swAnchorButton.setEnabled(false);
        swAnchorButton.setFocusPainted(false);
        swAnchorButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        swAnchorButton.addActionListener(formListener);

        sAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_s.png"))); // NOI18N
        sAnchorButton.setEnabled(false);
        sAnchorButton.setFocusPainted(false);
        sAnchorButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        sAnchorButton.addActionListener(formListener);

        seAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_se.png"))); // NOI18N
        seAnchorButton.setEnabled(false);
        seAnchorButton.setFocusPainted(false);
        seAnchorButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        seAnchorButton.addActionListener(formListener);

        baselineAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/baseline.png"))); // NOI18N
        baselineAnchorButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.anchor.baselineRelated")); // NOI18N
        baselineAnchorButton.setEnabled(false);
        baselineAnchorButton.setFocusPainted(false);
        baselineAnchorButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        baselineAnchorButton.addActionListener(formListener);

        bidiAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/bidi.png"))); // NOI18N
        bidiAnchorButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.anchor.bidiAware")); // NOI18N
        bidiAnchorButton.setEnabled(false);
        bidiAnchorButton.setFocusPainted(false);
        bidiAnchorButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        bidiAnchorButton.addActionListener(formListener);

        javax.swing.GroupLayout anchorPanelLayout = new javax.swing.GroupLayout(anchorPanel);
        anchorPanel.setLayout(anchorPanelLayout);
        anchorPanelLayout.setHorizontalGroup(
            anchorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(anchorPanelLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(anchorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bidiAnchorButton)
                    .addComponent(baselineAnchorButton))
                .addGap(18, 18, 18)
                .addGroup(anchorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addGroup(anchorPanelLayout.createSequentialGroup()
                        .addComponent(swAnchorButton)
                        .addGap(0, 0, 0)
                        .addComponent(sAnchorButton)
                        .addGap(0, 0, 0)
                        .addComponent(seAnchorButton))
                    .addGroup(anchorPanelLayout.createSequentialGroup()
                        .addComponent(wAnchorButton)
                        .addGap(0, 0, 0)
                        .addComponent(cAnchorButton)
                        .addGap(0, 0, 0)
                        .addComponent(eAnchorButton))
                    .addGroup(anchorPanelLayout.createSequentialGroup()
                        .addComponent(nwAnchorButton)
                        .addGap(0, 0, 0)
                        .addComponent(nAnchorButton)
                        .addGap(0, 0, 0)
                        .addComponent(neAnchorButton)))
                .addContainerGap(17, Short.MAX_VALUE))
        );
        anchorPanelLayout.setVerticalGroup(
            anchorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(anchorPanelLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(bidiAnchorButton)
                .addGap(1, 1, 1)
                .addComponent(baselineAnchorButton))
            .addGroup(anchorPanelLayout.createSequentialGroup()
                .addGroup(anchorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nwAnchorButton)
                    .addComponent(nAnchorButton)
                    .addComponent(neAnchorButton))
                .addGap(0, 0, 0)
                .addGroup(anchorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(wAnchorButton)
                    .addComponent(cAnchorButton)
                    .addComponent(eAnchorButton))
                .addGap(0, 0, 0)
                .addGroup(anchorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(swAnchorButton)
                    .addComponent(sAnchorButton)
                    .addComponent(seAnchorButton)))
        );

        fillLabel.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.fillLabel.text")); // NOI18N

        hFillButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/resize_h.png"))); // NOI18N
        hFillButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.fill.horizontal")); // NOI18N
        hFillButton.setEnabled(false);
        hFillButton.setFocusPainted(false);
        hFillButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hFillButton.addActionListener(formListener);

        vFillButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/resize_v.png"))); // NOI18N
        vFillButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.fill.vertical")); // NOI18N
        vFillButton.setEnabled(false);
        vFillButton.setFocusPainted(false);
        vFillButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vFillButton.addActionListener(formListener);

        javax.swing.GroupLayout fillPanelLayout = new javax.swing.GroupLayout(fillPanel);
        fillPanel.setLayout(fillPanelLayout);
        fillPanelLayout.setHorizontalGroup(
            fillPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fillPanelLayout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addComponent(hFillButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(vFillButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(40, Short.MAX_VALUE))
        );
        fillPanelLayout.setVerticalGroup(
            fillPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(hFillButton)
            .addComponent(vFillButton)
        );

        paddingLabel.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.paddingLabel.text")); // NOI18N

        paddingPanel.setPreferredSize(new java.awt.Dimension(124, 40));

        hpadLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/horizontal.png"))); // NOI18N
        hpadLabel.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.paddingHLabel.text")); // NOI18N
        hpadLabel.setPreferredSize(new java.awt.Dimension(15, 10));

        hminusPaddingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        hminusPaddingButton.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.padding.HMinus.text")); // NOI18N
        hminusPaddingButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.padding.HMinus.toolTipText")); // NOI18N
        hminusPaddingButton.setEnabled(false);
        hminusPaddingButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hminusPaddingButton.setMaximumSize(new java.awt.Dimension(20, 20));
        hminusPaddingButton.setMinimumSize(new java.awt.Dimension(20, 20));
        hminusPaddingButton.setPreferredSize(new java.awt.Dimension(20, 20));
        hminusPaddingButton.addActionListener(formListener);

        hplusPaddingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        hplusPaddingButton.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.padding.HPlus.text")); // NOI18N
        hplusPaddingButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.padding.HPlus.toolTipText")); // NOI18N
        hplusPaddingButton.setEnabled(false);
        hplusPaddingButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hplusPaddingButton.setMaximumSize(new java.awt.Dimension(20, 20));
        hplusPaddingButton.setMinimumSize(new java.awt.Dimension(20, 20));
        hplusPaddingButton.setPreferredSize(new java.awt.Dimension(20, 20));
        hplusPaddingButton.addActionListener(formListener);

        vpadLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        vpadLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/vertical.png"))); // NOI18N
        vpadLabel.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.paddingVLabel.text")); // NOI18N
        vpadLabel.setMaximumSize(new java.awt.Dimension(8, 15));
        vpadLabel.setMinimumSize(new java.awt.Dimension(8, 15));
        vpadLabel.setPreferredSize(new java.awt.Dimension(8, 15));

        vminusPaddingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        vminusPaddingButton.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.padding.VMinus.text")); // NOI18N
        vminusPaddingButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.padding.VMinus.toolTipText")); // NOI18N
        vminusPaddingButton.setEnabled(false);
        vminusPaddingButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vminusPaddingButton.setMaximumSize(new java.awt.Dimension(20, 20));
        vminusPaddingButton.setMinimumSize(new java.awt.Dimension(20, 20));
        vminusPaddingButton.setPreferredSize(new java.awt.Dimension(20, 20));
        vminusPaddingButton.addActionListener(formListener);

        vplusPaddingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        vplusPaddingButton.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.padding.VPlus.text")); // NOI18N
        vplusPaddingButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.padding.VPlus.toolTipText")); // NOI18N
        vplusPaddingButton.setEnabled(false);
        vplusPaddingButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vplusPaddingButton.setMaximumSize(new java.awt.Dimension(20, 20));
        vplusPaddingButton.setMinimumSize(new java.awt.Dimension(20, 20));
        vplusPaddingButton.setPreferredSize(new java.awt.Dimension(20, 20));
        vplusPaddingButton.addActionListener(formListener);

        bpadLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        bpadLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/both.png"))); // NOI18N
        bpadLabel.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.paddingBLabel.text")); // NOI18N

        bminusPaddingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        bminusPaddingButton.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.padding.BMinus.text")); // NOI18N
        bminusPaddingButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.padding.BMinus.toolTipText")); // NOI18N
        bminusPaddingButton.setEnabled(false);
        bminusPaddingButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        bminusPaddingButton.setMaximumSize(new java.awt.Dimension(20, 20));
        bminusPaddingButton.setMinimumSize(new java.awt.Dimension(20, 20));
        bminusPaddingButton.setPreferredSize(new java.awt.Dimension(20, 20));
        bminusPaddingButton.addActionListener(formListener);

        bplusPaddingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        bplusPaddingButton.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.padding.BPlus.text")); // NOI18N
        bplusPaddingButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.padding.BPlus.toolTipText")); // NOI18N
        bplusPaddingButton.setEnabled(false);
        bplusPaddingButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        bplusPaddingButton.setMaximumSize(new java.awt.Dimension(20, 20));
        bplusPaddingButton.setMinimumSize(new java.awt.Dimension(20, 20));
        bplusPaddingButton.setPreferredSize(new java.awt.Dimension(20, 20));
        bplusPaddingButton.addActionListener(formListener);

        javax.swing.GroupLayout paddingPanelLayout = new javax.swing.GroupLayout(paddingPanel);
        paddingPanel.setLayout(paddingPanelLayout);
        paddingPanelLayout.setHorizontalGroup(
            paddingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(paddingPanelLayout.createSequentialGroup()
                .addComponent(vpadLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addGroup(paddingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(vplusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vminusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(paddingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(hpadLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(paddingPanelLayout.createSequentialGroup()
                        .addComponent(hminusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(hplusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(8, 8, 8)
                .addComponent(bpadLabel)
                .addGap(0, 0, 0)
                .addGroup(paddingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(bplusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bminusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        paddingPanelLayout.setVerticalGroup(
            paddingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(paddingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(vpadLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(paddingPanelLayout.createSequentialGroup()
                    .addComponent(vplusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, 0)
                    .addComponent(vminusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(paddingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(paddingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(bpadLabel)
                        .addGroup(paddingPanelLayout.createSequentialGroup()
                            .addComponent(bplusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, 0)
                            .addComponent(bminusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(paddingPanelLayout.createSequentialGroup()
                        .addGroup(paddingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(hplusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(hminusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, 0)
                        .addComponent(hpadLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );

        insetsLabel.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.insetsLabel.text")); // NOI18N

        insetsPanel.setPreferredSize(new java.awt.Dimension(124, 139));

        topLeftCorner.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/corner_tl.png"))); // NOI18N
        topLeftCorner.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.topLeftCorner.text")); // NOI18N

        vplusTopInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        vplusTopInsetButton.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vplusTopInsetButton.text")); // NOI18N
        vplusTopInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vplusTopInsetButton.toolTipText")); // NOI18N
        vplusTopInsetButton.setEnabled(false);
        vplusTopInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vplusTopInsetButton.setMaximumSize(new java.awt.Dimension(20, 20));
        vplusTopInsetButton.setMinimumSize(new java.awt.Dimension(20, 20));
        vplusTopInsetButton.setPreferredSize(new java.awt.Dimension(20, 18));
        vplusTopInsetButton.addActionListener(formListener);

        vminusTopInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        vminusTopInsetButton.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vminusTopInsetButton.text")); // NOI18N
        vminusTopInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vminusTopInsetButton.toolTipText")); // NOI18N
        vminusTopInsetButton.setEnabled(false);
        vminusTopInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vminusTopInsetButton.setMaximumSize(new java.awt.Dimension(20, 20));
        vminusTopInsetButton.setMinimumSize(new java.awt.Dimension(20, 20));
        vminusTopInsetButton.setPreferredSize(new java.awt.Dimension(20, 18));
        vminusTopInsetButton.addActionListener(formListener);

        topRightCorner.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/corner_tr.png"))); // NOI18N
        topRightCorner.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.topRightCorner.text")); // NOI18N

        hplusLeftInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        hplusLeftInsetButton.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hplusLeftInsetButton.text")); // NOI18N
        hplusLeftInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hplusLeftInsetButton.toolTipText")); // NOI18N
        hplusLeftInsetButton.setEnabled(false);
        hplusLeftInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hplusLeftInsetButton.setMaximumSize(new java.awt.Dimension(20, 20));
        hplusLeftInsetButton.setMinimumSize(new java.awt.Dimension(20, 20));
        hplusLeftInsetButton.setPreferredSize(new java.awt.Dimension(18, 20));
        hplusLeftInsetButton.addActionListener(formListener);

        hminusLeftInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        hminusLeftInsetButton.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hminusLeftInsetButton.text")); // NOI18N
        hminusLeftInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hminusLeftInsetButton.toolTipText")); // NOI18N
        hminusLeftInsetButton.setEnabled(false);
        hminusLeftInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hminusLeftInsetButton.setMaximumSize(new java.awt.Dimension(20, 20));
        hminusLeftInsetButton.setMinimumSize(new java.awt.Dimension(20, 20));
        hminusLeftInsetButton.setPreferredSize(new java.awt.Dimension(18, 20));
        hminusLeftInsetButton.addActionListener(formListener);

        hminusRightInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        hminusRightInsetButton.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hminusRightInsetButton.text")); // NOI18N
        hminusRightInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hminusRightInsetButton.toolTipText")); // NOI18N
        hminusRightInsetButton.setEnabled(false);
        hminusRightInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hminusRightInsetButton.setMaximumSize(new java.awt.Dimension(20, 20));
        hminusRightInsetButton.setMinimumSize(new java.awt.Dimension(20, 20));
        hminusRightInsetButton.setPreferredSize(new java.awt.Dimension(18, 20));
        hminusRightInsetButton.addActionListener(formListener);

        hplusRightInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        hplusRightInsetButton.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hplusRightInsetButton.text")); // NOI18N
        hplusRightInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hplusRightInsetButton.toolTipText")); // NOI18N
        hplusRightInsetButton.setEnabled(false);
        hplusRightInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hplusRightInsetButton.setMaximumSize(new java.awt.Dimension(20, 20));
        hplusRightInsetButton.setMinimumSize(new java.awt.Dimension(20, 20));
        hplusRightInsetButton.setPreferredSize(new java.awt.Dimension(18, 20));
        hplusRightInsetButton.addActionListener(formListener);

        bottomLeftCorner.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/corner_bl.png"))); // NOI18N
        bottomLeftCorner.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.bottomLeftCorner.text")); // NOI18N

        vminusBottomInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        vminusBottomInsetButton.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vminusBottomInsetButton.text")); // NOI18N
        vminusBottomInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vminusBottomInsetButton.toolTipText")); // NOI18N
        vminusBottomInsetButton.setEnabled(false);
        vminusBottomInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vminusBottomInsetButton.setMaximumSize(new java.awt.Dimension(20, 20));
        vminusBottomInsetButton.setMinimumSize(new java.awt.Dimension(20, 20));
        vminusBottomInsetButton.setPreferredSize(new java.awt.Dimension(20, 18));
        vminusBottomInsetButton.addActionListener(formListener);

        vplusBottomInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        vplusBottomInsetButton.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vplusBottomInsetButton.text")); // NOI18N
        vplusBottomInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vplusBottomInsetButton.toolTipText")); // NOI18N
        vplusBottomInsetButton.setEnabled(false);
        vplusBottomInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vplusBottomInsetButton.setMaximumSize(new java.awt.Dimension(20, 20));
        vplusBottomInsetButton.setMinimumSize(new java.awt.Dimension(20, 20));
        vplusBottomInsetButton.setPreferredSize(new java.awt.Dimension(20, 18));
        vplusBottomInsetButton.addActionListener(formListener);

        bottomRightCorner.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/corner_br.png"))); // NOI18N
        bottomRightCorner.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.bottomRightCorner.text")); // NOI18N

        vInsetLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        vInsetLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/inset_v.png"))); // NOI18N
        vInsetLabel.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vInsetLabel.text")); // NOI18N
        vInsetLabel.setMaximumSize(new java.awt.Dimension(8, 15));
        vInsetLabel.setMinimumSize(new java.awt.Dimension(8, 15));
        vInsetLabel.setPreferredSize(new java.awt.Dimension(8, 15));

        vminusInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        vminusInsetButton.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vminusInsetButton.text")); // NOI18N
        vminusInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vminusInsetButton.toolTipText")); // NOI18N
        vminusInsetButton.setEnabled(false);
        vminusInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vminusInsetButton.setMaximumSize(new java.awt.Dimension(20, 20));
        vminusInsetButton.setMinimumSize(new java.awt.Dimension(20, 20));
        vminusInsetButton.setPreferredSize(new java.awt.Dimension(20, 20));
        vminusInsetButton.addActionListener(formListener);

        vplusInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        vplusInsetButton.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vplusInsetButton.text")); // NOI18N
        vplusInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vplusInsetButton.toolTipText")); // NOI18N
        vplusInsetButton.setEnabled(false);
        vplusInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vplusInsetButton.setMaximumSize(new java.awt.Dimension(20, 20));
        vplusInsetButton.setMinimumSize(new java.awt.Dimension(20, 20));
        vplusInsetButton.setPreferredSize(new java.awt.Dimension(20, 20));
        vplusInsetButton.addActionListener(formListener);

        hInsetLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/inset_h.png"))); // NOI18N
        hInsetLabel.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hInsetLabel.text")); // NOI18N
        hInsetLabel.setPreferredSize(new java.awt.Dimension(15, 10));

        hminusInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        hminusInsetButton.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hminusInsetButton.text")); // NOI18N
        hminusInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hminusInsetButton.toolTipText")); // NOI18N
        hminusInsetButton.setEnabled(false);
        hminusInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hminusInsetButton.setMaximumSize(new java.awt.Dimension(20, 20));
        hminusInsetButton.setMinimumSize(new java.awt.Dimension(20, 20));
        hminusInsetButton.setPreferredSize(new java.awt.Dimension(20, 20));
        hminusInsetButton.addActionListener(formListener);

        hplusInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        hplusInsetButton.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hplusInsetButton.text")); // NOI18N
        hplusInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hplusInsetButton.toolTipText")); // NOI18N
        hplusInsetButton.setEnabled(false);
        hplusInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hplusInsetButton.setMaximumSize(new java.awt.Dimension(20, 20));
        hplusInsetButton.setMinimumSize(new java.awt.Dimension(20, 20));
        hplusInsetButton.setPreferredSize(new java.awt.Dimension(20, 20));
        hplusInsetButton.addActionListener(formListener);

        bInsetLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        bInsetLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/inset_both.png"))); // NOI18N
        bInsetLabel.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.bInsetLabel.text")); // NOI18N

        bminusInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        bminusInsetButton.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.bminusInsetButton.text")); // NOI18N
        bminusInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.bminusInsetButton.toolTipText")); // NOI18N
        bminusInsetButton.setEnabled(false);
        bminusInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        bminusInsetButton.setMaximumSize(new java.awt.Dimension(20, 20));
        bminusInsetButton.setMinimumSize(new java.awt.Dimension(20, 20));
        bminusInsetButton.setPreferredSize(new java.awt.Dimension(20, 20));
        bminusInsetButton.addActionListener(formListener);

        bplusInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        bplusInsetButton.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.bplusInsetButton.text")); // NOI18N
        bplusInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.bplusInsetButton.toolTipText")); // NOI18N
        bplusInsetButton.setEnabled(false);
        bplusInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        bplusInsetButton.setMaximumSize(new java.awt.Dimension(20, 20));
        bplusInsetButton.setMinimumSize(new java.awt.Dimension(20, 20));
        bplusInsetButton.setPreferredSize(new java.awt.Dimension(20, 20));
        bplusInsetButton.addActionListener(formListener);

        javax.swing.GroupLayout insetsPanelLayout = new javax.swing.GroupLayout(insetsPanel);
        insetsPanel.setLayout(insetsPanelLayout);
        insetsPanelLayout.setHorizontalGroup(
            insetsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(insetsPanelLayout.createSequentialGroup()
                .addGroup(insetsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(vplusInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(insetsPanelLayout.createSequentialGroup()
                        .addComponent(vInsetLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(3, 3, 3)
                        .addComponent(vminusInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(11, 11, 11)
                .addGroup(insetsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(hInsetLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(insetsPanelLayout.createSequentialGroup()
                        .addComponent(hminusInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(hplusInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(7, 7, 7)
                .addComponent(bInsetLabel)
                .addGap(0, 0, 0)
                .addGroup(insetsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bplusInsetButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bminusInsetButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(insetsPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(hplusLeftInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(insetsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bottomLeftCorner, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(hminusLeftInsetButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(topLeftCorner, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(1, 1, 1)
                .addGroup(insetsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(insetsPanelLayout.createSequentialGroup()
                        .addGroup(insetsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(vminusBottomInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(vminusTopInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(1, 1, 1)
                        .addGroup(insetsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(bottomRightCorner)
                            .addComponent(hminusRightInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(topRightCorner))
                        .addGap(0, 0, 0)
                        .addComponent(hplusRightInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(vplusBottomInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vplusTopInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        insetsPanelLayout.setVerticalGroup(
            insetsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(insetsPanelLayout.createSequentialGroup()
                .addGroup(insetsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(insetsPanelLayout.createSequentialGroup()
                        .addGroup(insetsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(hplusInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(hminusInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, 0)
                        .addComponent(hInsetLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(insetsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(bInsetLabel)
                        .addGroup(insetsPanelLayout.createSequentialGroup()
                            .addComponent(bplusInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, 0)
                            .addComponent(bminusInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(vInsetLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(insetsPanelLayout.createSequentialGroup()
                            .addComponent(vplusInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(vminusInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(vplusTopInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(insetsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(insetsPanelLayout.createSequentialGroup()
                        .addComponent(topRightCorner)
                        .addGap(1, 1, 1)
                        .addGroup(insetsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(hplusRightInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(hminusRightInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(1, 1, 1)
                        .addComponent(bottomRightCorner))
                    .addGroup(insetsPanelLayout.createSequentialGroup()
                        .addComponent(topLeftCorner)
                        .addGap(1, 1, 1)
                        .addGroup(insetsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(hminusLeftInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(hplusLeftInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(1, 1, 1)
                        .addComponent(bottomLeftCorner))
                    .addGroup(insetsPanelLayout.createSequentialGroup()
                        .addComponent(vminusTopInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16)
                        .addComponent(vminusBottomInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(vplusBottomInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );

        gridSizelLabel.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.gridSizeLabel.text")); // NOI18N

        gridSizelSeparator.setPreferredSize(new java.awt.Dimension(10, 2));

        javax.swing.GroupLayout customizerLayout = new javax.swing.GroupLayout(customizer);
        customizer.setLayout(customizerLayout);
        customizerLayout.setHorizontalGroup(
            customizerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(customizerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(customizerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(customizerLayout.createSequentialGroup()
                        .addComponent(anchorLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(anchorSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(insetsLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(insetsSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(customizerLayout.createSequentialGroup()
                        .addGroup(customizerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(anchorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(paddingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(customizerLayout.createSequentialGroup()
                                .addComponent(paddingLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(paddingSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(customizerLayout.createSequentialGroup()
                                .addComponent(gridSizelLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(gridSizelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(customizerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(customizerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(fillPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, customizerLayout.createSequentialGroup()
                                    .addComponent(fillLabel)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(fillSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(insetsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        customizerLayout.setVerticalGroup(
            customizerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(customizerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(customizerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(anchorSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(anchorLabel)
                    .addComponent(insetsLabel)
                    .addComponent(insetsSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(customizerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(customizerLayout.createSequentialGroup()
                        .addComponent(anchorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(customizerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(paddingSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(paddingLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(paddingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addGroup(customizerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(gridSizelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(gridSizelLabel)))
                    .addComponent(insetsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(customizerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(fillSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fillLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fillPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == nwAnchorButton) {
                GridBagCustomizer.this.nwAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == nAnchorButton) {
                GridBagCustomizer.this.nAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == neAnchorButton) {
                GridBagCustomizer.this.neAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == eAnchorButton) {
                GridBagCustomizer.this.eAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == cAnchorButton) {
                GridBagCustomizer.this.cAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == wAnchorButton) {
                GridBagCustomizer.this.wAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == swAnchorButton) {
                GridBagCustomizer.this.swAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == sAnchorButton) {
                GridBagCustomizer.this.sAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == seAnchorButton) {
                GridBagCustomizer.this.seAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == baselineAnchorButton) {
                GridBagCustomizer.this.baselineAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == bidiAnchorButton) {
                GridBagCustomizer.this.bidiAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hFillButton) {
                GridBagCustomizer.this.hFillButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vFillButton) {
                GridBagCustomizer.this.vFillButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hminusPaddingButton) {
                GridBagCustomizer.this.hminusPaddingButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hplusPaddingButton) {
                GridBagCustomizer.this.hplusPaddingButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vminusPaddingButton) {
                GridBagCustomizer.this.vminusPaddingButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vplusPaddingButton) {
                GridBagCustomizer.this.vplusPaddingButtonActionPerformed(evt);
            }
            else if (evt.getSource() == bminusPaddingButton) {
                GridBagCustomizer.this.bminusPaddingButtonActionPerformed(evt);
            }
            else if (evt.getSource() == bplusPaddingButton) {
                GridBagCustomizer.this.bplusPaddingButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vplusTopInsetButton) {
                GridBagCustomizer.this.vplusTopInsetButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vminusTopInsetButton) {
                GridBagCustomizer.this.vminusTopInsetButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hplusLeftInsetButton) {
                GridBagCustomizer.this.hplusLeftInsetButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hminusLeftInsetButton) {
                GridBagCustomizer.this.hminusLeftInsetButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hminusRightInsetButton) {
                GridBagCustomizer.this.hminusRightInsetButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hplusRightInsetButton) {
                GridBagCustomizer.this.hplusRightInsetButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vminusBottomInsetButton) {
                GridBagCustomizer.this.vminusBottomInsetButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vplusBottomInsetButton) {
                GridBagCustomizer.this.vplusBottomInsetButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vminusInsetButton) {
                GridBagCustomizer.this.vminusInsetButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vplusInsetButton) {
                GridBagCustomizer.this.vplusInsetButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hminusInsetButton) {
                GridBagCustomizer.this.hminusInsetButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hplusInsetButton) {
                GridBagCustomizer.this.hplusInsetButtonActionPerformed(evt);
            }
            else if (evt.getSource() == bminusInsetButton) {
                GridBagCustomizer.this.bminusInsetButtonActionPerformed(evt);
            }
            else if (evt.getSource() == bplusInsetButton) {
                GridBagCustomizer.this.bplusInsetButtonActionPerformed(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void nwAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nwAnchorButtonActionPerformed
        final int anchor=currentAnchorSpecialization(GridBagConstraints.NORTHWEST);
        selectAnchorButtons(anchor);
        update(-1, anchor, null, null, null);
    }//GEN-LAST:event_nwAnchorButtonActionPerformed

    private void nAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nAnchorButtonActionPerformed
        final int anchor=currentAnchorSpecialization(GridBagConstraints.NORTH);
        selectAnchorButtons(anchor);
        update(-1, anchor, null, null, null);
    }//GEN-LAST:event_nAnchorButtonActionPerformed

    private void neAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_neAnchorButtonActionPerformed
        final int anchor=currentAnchorSpecialization(GridBagConstraints.NORTHEAST);
        selectAnchorButtons(anchor);
        update(-1, anchor, null, null, null);
    }//GEN-LAST:event_neAnchorButtonActionPerformed

    private void wAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wAnchorButtonActionPerformed
        final int anchor=currentAnchorSpecialization(GridBagConstraints.WEST);
        selectAnchorButtons(anchor);
        update(-1, anchor, null, null, null);
    }//GEN-LAST:event_wAnchorButtonActionPerformed

    private void cAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cAnchorButtonActionPerformed
        final int anchor=currentAnchorSpecialization(GridBagConstraints.CENTER);
        selectAnchorButtons(anchor);
        update(-1, anchor, null, null, null);
    }//GEN-LAST:event_cAnchorButtonActionPerformed

    private void eAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eAnchorButtonActionPerformed
        final int anchor=currentAnchorSpecialization(GridBagConstraints.EAST);
        selectAnchorButtons(anchor);
        update(-1, anchor, null, null, null);
    }//GEN-LAST:event_eAnchorButtonActionPerformed

    private void swAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_swAnchorButtonActionPerformed
        final int anchor=currentAnchorSpecialization(GridBagConstraints.SOUTHWEST);
        selectAnchorButtons(anchor);
        update(-1, anchor, null, null, null);
    }//GEN-LAST:event_swAnchorButtonActionPerformed

    private void sAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sAnchorButtonActionPerformed
        final int anchor=currentAnchorSpecialization(GridBagConstraints.SOUTH);
        selectAnchorButtons(anchor);
        update(-1, anchor, null, null, null);
    }//GEN-LAST:event_sAnchorButtonActionPerformed

    private void seAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seAnchorButtonActionPerformed
        final int anchor=currentAnchorSpecialization(GridBagConstraints.SOUTHEAST);
        selectAnchorButtons(anchor);
        update(-1, anchor, null, null, null);
    }//GEN-LAST:event_seAnchorButtonActionPerformed

    private void hFillButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hFillButtonActionPerformed
        boolean hFill = hFillButton.isSelected();
        update(-1, -1, new FillChange(hFill ? 1 : 0,-1), null, null);
    }//GEN-LAST:event_hFillButtonActionPerformed

    private void vFillButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vFillButtonActionPerformed
        boolean vFill = vFillButton.isSelected();
        update(-1, -1, new FillChange(-1, vFill ? 1 : 0), null, null);
    }//GEN-LAST:event_vFillButtonActionPerformed

    private void baselineAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_baselineAnchorButtonActionPerformed
        final boolean baseline = baselineAnchorButton.isSelected();
        final boolean bidi;
        if (baseline) {
            // Baseline anchors are bidi-aware
            bidiAnchorButton.setSelected(true);
            bidi = true;
        } else
            bidi = bidiAnchorButton.isSelected();
        update(baseline ? ANCHOR_BASELINE : (bidi ? ANCHOR_BIDI : ANCHOR_ABSOLUTE), -1, null, null, null);
        updateTooltips();
    }//GEN-LAST:event_baselineAnchorButtonActionPerformed

    private void bidiAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bidiAnchorButtonActionPerformed
        final boolean bidi = bidiAnchorButton.isSelected();
        final boolean baseline;
        if (!bidi) {
            // Baseline anchors are bidi-aware
            baselineAnchorButton.setSelected(false);
            baseline = false;
        } else
            baseline = baselineAnchorButton.isSelected();
        update(baseline ? ANCHOR_BASELINE : (bidi ? ANCHOR_BIDI : ANCHOR_ABSOLUTE), -1, null, null, null);
        updateTooltips();
    }//GEN-LAST:event_bidiAnchorButtonActionPerformed

    private void hminusPaddingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hminusPaddingButtonActionPerformed
        int changeby=1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby=ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, new PaddingChange(-changeby,0), null);
    }//GEN-LAST:event_hminusPaddingButtonActionPerformed

    private void hplusPaddingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hplusPaddingButtonActionPerformed
        int changeby=1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby=ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, new PaddingChange(changeby,0), null);
    }//GEN-LAST:event_hplusPaddingButtonActionPerformed

    private void vplusPaddingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vplusPaddingButtonActionPerformed
        int changeby=1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby=ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, new PaddingChange(0,changeby), null);
    }//GEN-LAST:event_vplusPaddingButtonActionPerformed

    private void vminusPaddingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vminusPaddingButtonActionPerformed
        int changeby=1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby=ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, new PaddingChange(0,-changeby), null);
    }//GEN-LAST:event_vminusPaddingButtonActionPerformed

    private void vplusTopInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vplusTopInsetButtonActionPerformed
        int changeby=1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby=ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new Insets(changeby,0,0,0));
    }//GEN-LAST:event_vplusTopInsetButtonActionPerformed

    private void vminusTopInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vminusTopInsetButtonActionPerformed
        int changeby=1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby=ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new Insets(-changeby,0,0,0));
    }//GEN-LAST:event_vminusTopInsetButtonActionPerformed

    private void hplusLeftInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hplusLeftInsetButtonActionPerformed
        int changeby=1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby=ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new Insets(0,changeby,0,0));
    }//GEN-LAST:event_hplusLeftInsetButtonActionPerformed

    private void hminusLeftInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hminusLeftInsetButtonActionPerformed
        int changeby=1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby=ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new Insets(0,-changeby,0,0));
    }//GEN-LAST:event_hminusLeftInsetButtonActionPerformed

    private void hplusRightInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hplusRightInsetButtonActionPerformed
        int changeby=1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby=ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new Insets(0,0,0,changeby));
    }//GEN-LAST:event_hplusRightInsetButtonActionPerformed

    private void hminusRightInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hminusRightInsetButtonActionPerformed
        int changeby=1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby=ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new Insets(0,0,0,-changeby));
    }//GEN-LAST:event_hminusRightInsetButtonActionPerformed

    private void bminusPaddingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bminusPaddingButtonActionPerformed
        int changeby=1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby=ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, new PaddingChange(-changeby,-changeby), null);
    }//GEN-LAST:event_bminusPaddingButtonActionPerformed

    private void bplusPaddingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bplusPaddingButtonActionPerformed
        int changeby=1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby=ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, new PaddingChange(changeby,changeby), null);
    }//GEN-LAST:event_bplusPaddingButtonActionPerformed

    private void vminusBottomInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vminusBottomInsetButtonActionPerformed
        int changeby=1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby=ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new Insets(0,0,-changeby,0));
    }//GEN-LAST:event_vminusBottomInsetButtonActionPerformed

    private void vplusBottomInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vplusBottomInsetButtonActionPerformed
        int changeby=1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby=ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new Insets(0,0,changeby,0));
    }//GEN-LAST:event_vplusBottomInsetButtonActionPerformed

    private void bplusInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bplusInsetButtonActionPerformed
        int changeby=1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby=ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new Insets(changeby,changeby,changeby,changeby));
    }//GEN-LAST:event_bplusInsetButtonActionPerformed

    private void bminusInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bminusInsetButtonActionPerformed
        int changeby=1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby=ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new Insets(-changeby,-changeby,-changeby,-changeby));
    }//GEN-LAST:event_bminusInsetButtonActionPerformed

    private void hminusInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hminusInsetButtonActionPerformed
        int changeby=1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby=ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new Insets(0,-changeby,0,-changeby));
    }//GEN-LAST:event_hminusInsetButtonActionPerformed

    private void hplusInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hplusInsetButtonActionPerformed
        int changeby=1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby=ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new Insets(0,changeby,0,changeby));
    }//GEN-LAST:event_hplusInsetButtonActionPerformed

    private void vminusInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vminusInsetButtonActionPerformed
        int changeby=1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby=ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new Insets(-changeby,0,-changeby,0));
    }//GEN-LAST:event_vminusInsetButtonActionPerformed

    private void vplusInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vplusInsetButtonActionPerformed
        int changeby=1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby=ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new Insets(changeby,0,changeby,0));
    }//GEN-LAST:event_vplusInsetButtonActionPerformed

    private int currentAnchorSpecialization(final int anchorbutton) {
        boolean baseline = baselineAnchorButton.isSelected();
        boolean bidi = bidiAnchorButton.isSelected();
        return convertAnchorType(baseline ? ANCHOR_BASELINE : (bidi ? ANCHOR_BIDI : ANCHOR_ABSOLUTE),anchorbutton);
    }
    
    private int convertAnchorType(final int type, final int currentanchor) {
        switch(type) {
            case ANCHOR_BASELINE: {
                switch(currentanchor) {
                    case GridBagConstraints.NORTHWEST: 
                    case GridBagConstraints.ABOVE_BASELINE_LEADING:
                    case GridBagConstraints.FIRST_LINE_START: return GridBagConstraints.ABOVE_BASELINE_LEADING;
                    case GridBagConstraints.NORTH:
                    case GridBagConstraints.ABOVE_BASELINE:
                    case GridBagConstraints.PAGE_START: return GridBagConstraints.ABOVE_BASELINE;
                    case GridBagConstraints.NORTHEAST:
                    case GridBagConstraints.ABOVE_BASELINE_TRAILING:
                    case GridBagConstraints.FIRST_LINE_END: return GridBagConstraints.ABOVE_BASELINE_TRAILING;
                    case GridBagConstraints.WEST:
                    case GridBagConstraints.BASELINE_LEADING:
                    case GridBagConstraints.LINE_START: return GridBagConstraints.BASELINE_LEADING;
                    case GridBagConstraints.CENTER:
                    case GridBagConstraints.BASELINE: return GridBagConstraints.BASELINE;
                    case GridBagConstraints.EAST:
                    case GridBagConstraints.BASELINE_TRAILING:
                    case GridBagConstraints.LINE_END: return GridBagConstraints.BASELINE_TRAILING;
                    case GridBagConstraints.SOUTHWEST:
                    case GridBagConstraints.BELOW_BASELINE_LEADING:
                    case GridBagConstraints.LAST_LINE_START: return GridBagConstraints.BELOW_BASELINE_LEADING;
                    case GridBagConstraints.SOUTH:
                    case GridBagConstraints.BELOW_BASELINE:
                    case GridBagConstraints.PAGE_END: return GridBagConstraints.BELOW_BASELINE;
                    case GridBagConstraints.SOUTHEAST:
                    case GridBagConstraints.BELOW_BASELINE_TRAILING:
                    case GridBagConstraints.LAST_LINE_END: return GridBagConstraints.BELOW_BASELINE_TRAILING;
                }
                break;
            }
            case ANCHOR_BIDI: {
                switch(currentanchor) {
                    case GridBagConstraints.NORTHWEST: 
                    case GridBagConstraints.ABOVE_BASELINE_LEADING:
                    case GridBagConstraints.FIRST_LINE_START: return GridBagConstraints.FIRST_LINE_START;
                    case GridBagConstraints.NORTH:
                    case GridBagConstraints.ABOVE_BASELINE:
                    case GridBagConstraints.PAGE_START: return GridBagConstraints.PAGE_START;
                    case GridBagConstraints.NORTHEAST:
                    case GridBagConstraints.ABOVE_BASELINE_TRAILING:
                    case GridBagConstraints.FIRST_LINE_END: return GridBagConstraints.FIRST_LINE_END;
                    case GridBagConstraints.WEST:
                    case GridBagConstraints.BASELINE_LEADING:
                    case GridBagConstraints.LINE_START: return GridBagConstraints.LINE_START;
                    case GridBagConstraints.CENTER:
                    case GridBagConstraints.BASELINE: return GridBagConstraints.CENTER;
                    case GridBagConstraints.EAST:
                    case GridBagConstraints.BASELINE_TRAILING:
                    case GridBagConstraints.LINE_END: return GridBagConstraints.LINE_END;
                    case GridBagConstraints.SOUTHWEST:
                    case GridBagConstraints.BELOW_BASELINE_LEADING:
                    case GridBagConstraints.LAST_LINE_START: return GridBagConstraints.LAST_LINE_START;
                    case GridBagConstraints.SOUTH:
                    case GridBagConstraints.BELOW_BASELINE:
                    case GridBagConstraints.PAGE_END: return GridBagConstraints.PAGE_END;
                    case GridBagConstraints.SOUTHEAST:
                    case GridBagConstraints.BELOW_BASELINE_TRAILING:
                    case GridBagConstraints.LAST_LINE_END: return GridBagConstraints.LAST_LINE_END;
                }
                break;
            }
            case ANCHOR_ABSOLUTE: {
                switch(currentanchor) {
                    case GridBagConstraints.NORTHWEST: 
                    case GridBagConstraints.ABOVE_BASELINE_LEADING:
                    case GridBagConstraints.FIRST_LINE_START: return GridBagConstraints.NORTHWEST;
                    case GridBagConstraints.NORTH:
                    case GridBagConstraints.ABOVE_BASELINE:
                    case GridBagConstraints.PAGE_START: return GridBagConstraints.NORTH;
                    case GridBagConstraints.NORTHEAST:
                    case GridBagConstraints.ABOVE_BASELINE_TRAILING:
                    case GridBagConstraints.FIRST_LINE_END: return GridBagConstraints.NORTHEAST;
                    case GridBagConstraints.WEST:
                    case GridBagConstraints.BASELINE_LEADING:
                    case GridBagConstraints.LINE_START: return GridBagConstraints.WEST;
                    case GridBagConstraints.CENTER:
                    case GridBagConstraints.BASELINE: return GridBagConstraints.CENTER;
                    case GridBagConstraints.EAST:
                    case GridBagConstraints.BASELINE_TRAILING:
                    case GridBagConstraints.LINE_END: return GridBagConstraints.EAST;
                    case GridBagConstraints.SOUTHWEST:
                    case GridBagConstraints.BELOW_BASELINE_LEADING:
                    case GridBagConstraints.LAST_LINE_START: return GridBagConstraints.SOUTHWEST;
                    case GridBagConstraints.SOUTH:
                    case GridBagConstraints.BELOW_BASELINE:
                    case GridBagConstraints.PAGE_END: return GridBagConstraints.SOUTH;
                    case GridBagConstraints.SOUTHEAST:
                    case GridBagConstraints.BELOW_BASELINE_TRAILING:
                    case GridBagConstraints.LAST_LINE_END: return GridBagConstraints.SOUTHEAST;
                }
                break;
            }
        }        
        return GridBagConstraints.NONE;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel anchorLabel;
    private javax.swing.JPanel anchorPanel;
    private javax.swing.JSeparator anchorSeparator;
    private javax.swing.JLabel bInsetLabel;
    private javax.swing.JToggleButton baselineAnchorButton;
    private javax.swing.JToggleButton bidiAnchorButton;
    private javax.swing.JButton bminusInsetButton;
    private javax.swing.JButton bminusPaddingButton;
    private javax.swing.JLabel bottomLeftCorner;
    private javax.swing.JLabel bottomRightCorner;
    private javax.swing.JLabel bpadLabel;
    private javax.swing.JButton bplusInsetButton;
    private javax.swing.JButton bplusPaddingButton;
    private javax.swing.JToggleButton cAnchorButton;
    private javax.swing.JPanel customizer;
    private javax.swing.JToggleButton eAnchorButton;
    private javax.swing.JLabel fillLabel;
    private javax.swing.JPanel fillPanel;
    private javax.swing.JSeparator fillSeparator;
    private javax.swing.JLabel gridSizelLabel;
    private javax.swing.JSeparator gridSizelSeparator;
    private javax.swing.JToggleButton hFillButton;
    private javax.swing.JLabel hInsetLabel;
    private javax.swing.JButton hminusInsetButton;
    private javax.swing.JButton hminusLeftInsetButton;
    private javax.swing.JButton hminusPaddingButton;
    private javax.swing.JButton hminusRightInsetButton;
    private javax.swing.JLabel hpadLabel;
    private javax.swing.JButton hplusInsetButton;
    private javax.swing.JButton hplusLeftInsetButton;
    private javax.swing.JButton hplusPaddingButton;
    private javax.swing.JButton hplusRightInsetButton;
    private javax.swing.JLabel insetsLabel;
    private javax.swing.JPanel insetsPanel;
    private javax.swing.JSeparator insetsSeparator;
    private javax.swing.JToggleButton nAnchorButton;
    private javax.swing.JToggleButton neAnchorButton;
    private javax.swing.JToggleButton nwAnchorButton;
    private javax.swing.JLabel paddingLabel;
    private javax.swing.JPanel paddingPanel;
    private javax.swing.JSeparator paddingSeparator;
    private javax.swing.JToggleButton sAnchorButton;
    private javax.swing.JToggleButton seAnchorButton;
    private javax.swing.JToggleButton swAnchorButton;
    private javax.swing.JLabel topLeftCorner;
    private javax.swing.JLabel topRightCorner;
    private javax.swing.JToggleButton vFillButton;
    private javax.swing.JLabel vInsetLabel;
    private javax.swing.JButton vminusBottomInsetButton;
    private javax.swing.JButton vminusInsetButton;
    private javax.swing.JButton vminusPaddingButton;
    private javax.swing.JButton vminusTopInsetButton;
    private javax.swing.JLabel vpadLabel;
    private javax.swing.JButton vplusBottomInsetButton;
    private javax.swing.JButton vplusInsetButton;
    private javax.swing.JButton vplusPaddingButton;
    private javax.swing.JButton vplusTopInsetButton;
    private javax.swing.JToggleButton wAnchorButton;
    // End of variables declaration//GEN-END:variables

    public void selectAnchorButtons(final int anchor)
    {
        nwAnchorButton.setSelected(anchor == GridBagConstraints.NORTHWEST
                || anchor == GridBagConstraints.FIRST_LINE_START
                || anchor == GridBagConstraints.ABOVE_BASELINE_LEADING);
        nAnchorButton.setSelected(anchor == GridBagConstraints.NORTH
                || anchor == GridBagConstraints.PAGE_START
                || anchor == GridBagConstraints.ABOVE_BASELINE);
        neAnchorButton.setSelected(anchor == GridBagConstraints.NORTHEAST
                || anchor == GridBagConstraints.FIRST_LINE_END
                || anchor == GridBagConstraints.ABOVE_BASELINE_TRAILING);
        wAnchorButton.setSelected(anchor == GridBagConstraints.WEST
                || anchor == GridBagConstraints.LINE_START
                || anchor == GridBagConstraints.BASELINE_LEADING);
        cAnchorButton.setSelected(anchor == GridBagConstraints.CENTER
                || anchor == GridBagConstraints.BASELINE);
        eAnchorButton.setSelected(anchor == GridBagConstraints.EAST
                || anchor == GridBagConstraints.LINE_END
                || anchor == GridBagConstraints.BASELINE_TRAILING);
        swAnchorButton.setSelected(anchor == GridBagConstraints.SOUTHWEST
                || anchor == GridBagConstraints.LAST_LINE_START
                || anchor == GridBagConstraints.BELOW_BASELINE_LEADING);
        sAnchorButton.setSelected(anchor == GridBagConstraints.SOUTH
                || anchor == GridBagConstraints.PAGE_END
                || anchor == GridBagConstraints.BELOW_BASELINE);
        seAnchorButton.setSelected(anchor == GridBagConstraints.SOUTHEAST
                || anchor == GridBagConstraints.LAST_LINE_END
                || anchor == GridBagConstraints.BELOW_BASELINE_TRAILING);
    }
    
    @Override
    public void setContext(DesignerContext context) {
        Set<Component> components = context.getSelectedComponents();

        boolean enablebuttons = !components.isEmpty();
        if(!enablebuttons) {
            bidiAnchorButton.setSelected(false);
            bidiAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/bidi.png"))); // NOI18N
            bidiAnchorButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.anchor.bidiAware")); // NOI18N
            baselineAnchorButton.setSelected(false);
            baselineAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/baseline.png"))); // NOI18N
            baselineAnchorButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.anchor.baselineRelated")); // NOI18N
            selectAnchorButtons(GridBagConstraints.NONE);
            hFillButton.setSelected(false);
            hFillButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/resize_h.png"))); // NOI18N
            hFillButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.fill.horizontal")); // NOI18N
            vFillButton.setSelected(false);
            vFillButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/resize_v.png"))); // NOI18N
            vFillButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.fill.vertical")); // NOI18N
        }
        bidiAnchorButton.setEnabled(enablebuttons);
        baselineAnchorButton.setEnabled(enablebuttons);
        //
        nwAnchorButton.setEnabled(enablebuttons);
        nAnchorButton.setEnabled(enablebuttons);
        neAnchorButton.setEnabled(enablebuttons);
        wAnchorButton.setEnabled(enablebuttons);
        cAnchorButton.setEnabled(enablebuttons);
        eAnchorButton.setEnabled(enablebuttons);
        swAnchorButton.setEnabled(enablebuttons);
        sAnchorButton.setEnabled(enablebuttons);
        seAnchorButton.setEnabled(enablebuttons);
        //
        hminusPaddingButton.setEnabled(enablebuttons);
        hplusPaddingButton.setEnabled(enablebuttons);
        vminusPaddingButton.setEnabled(enablebuttons);
        vplusPaddingButton.setEnabled(enablebuttons);
        bminusPaddingButton.setEnabled(enablebuttons);
        bplusPaddingButton.setEnabled(enablebuttons);
        //
        vplusInsetButton.setEnabled(enablebuttons);
        vminusInsetButton.setEnabled(enablebuttons);
        hplusInsetButton.setEnabled(enablebuttons);
        hminusInsetButton.setEnabled(enablebuttons);
        bplusInsetButton.setEnabled(enablebuttons);
        bminusInsetButton.setEnabled(enablebuttons);
        vplusTopInsetButton.setEnabled(enablebuttons);
        vminusTopInsetButton.setEnabled(enablebuttons);
        vplusBottomInsetButton.setEnabled(enablebuttons);
        vminusBottomInsetButton.setEnabled(enablebuttons);
        hplusLeftInsetButton.setEnabled(enablebuttons);
        hminusLeftInsetButton.setEnabled(enablebuttons);
        hplusRightInsetButton.setEnabled(enablebuttons);
        hminusRightInsetButton.setEnabled(enablebuttons);
        //
        hFillButton.setEnabled(enablebuttons);
        vFillButton.setEnabled(enablebuttons);

        if(enablebuttons) {
            GridBagInfoProvider info = manager.getGridInfo();

            /* set fill/bidi/base buttons as selected only if all selected components have the respective property set */
            int bidi=0, baseline=0;
            boolean tlanchor=false, tcanchor=false, tranchor=false;
            boolean lanchor=false, canchor=false, ranchor=false;
            boolean blanchor=false, bcanchor=false, branchor=false;
            int hfill=0, vfill=0;
            Iterator<Component> it = components.iterator();
            while (it.hasNext()) {
                Component component = it.next();
                int fill = info.getFill(component);
                if(fill == GridBagConstraints.HORIZONTAL || fill == GridBagConstraints.BOTH) hfill++;
                if(fill == GridBagConstraints.VERTICAL || fill == GridBagConstraints.BOTH) vfill++;
                int anchor = info.getAnchor(component);
                switch(anchor) {
                    // baseline anchors
                    case GridBagConstraints.ABOVE_BASELINE_LEADING: tlanchor=true; baseline++; break;
                    case GridBagConstraints.ABOVE_BASELINE: tcanchor=true; baseline++; break;
                    case GridBagConstraints.ABOVE_BASELINE_TRAILING: tranchor=true; baseline++; break;
                    case GridBagConstraints.BASELINE_LEADING: lanchor=true; baseline++; break;
                    case GridBagConstraints.BASELINE: canchor=true; baseline++; break;
                    case GridBagConstraints.BASELINE_TRAILING: ranchor=true; baseline++; break;
                    case GridBagConstraints.BELOW_BASELINE_LEADING: blanchor=true; baseline++; break;
                    case GridBagConstraints.BELOW_BASELINE: bcanchor=true; baseline++; break;
                    case GridBagConstraints.BELOW_BASELINE_TRAILING: branchor=true; baseline++; break;
                    // bidirectional anchors
                    case GridBagConstraints.FIRST_LINE_START: tlanchor=true; bidi++; break;
                    case GridBagConstraints.PAGE_START: tcanchor=true; bidi++; break;
                    case GridBagConstraints.FIRST_LINE_END: tranchor=true; bidi++; break;
                    case GridBagConstraints.LINE_START: lanchor=true; bidi++; break;
                    case GridBagConstraints.LINE_END: ranchor=true; bidi++; break;
                    case GridBagConstraints.LAST_LINE_START: blanchor=true; bidi++; break;
                    case GridBagConstraints.PAGE_END: bcanchor=true; bidi++; break;
                    case GridBagConstraints.LAST_LINE_END: branchor=true; bidi++; break;
                    // absolute anchors
                    case GridBagConstraints.NORTHWEST: tlanchor=true; break;
                    case GridBagConstraints.NORTH: tcanchor=true; break;
                    case GridBagConstraints.NORTHEAST: tranchor=true; break;
                    case GridBagConstraints.WEST: lanchor=true; break;
                    case GridBagConstraints.CENTER: canchor=true; break;
                    case GridBagConstraints.EAST: ranchor=true; break;
                    case GridBagConstraints.SOUTHWEST: blanchor=true; break;
                    case GridBagConstraints.SOUTH: bcanchor=true; break;
                    case GridBagConstraints.SOUTHEAST: branchor=true; break;
                }
            }
            bidiAnchorButton.setSelected(baseline+bidi==components.size());
            if(baseline+bidi>0 && baseline+bidi!=components.size()) {
                bidiAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/bidi_warning.png"))); // NOI18N
                bidiAnchorButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.anchor.bidiAwareWarning")); // NOI18N
            } else {
                bidiAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/bidi.png"))); // NOI18N
                bidiAnchorButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.anchor.bidiAware")); // NOI18N
            }
            baselineAnchorButton.setSelected(baseline==components.size());
            if(baseline>0 && baseline!=components.size()) {
                baselineAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/baseline_warning.png"))); // NOI18N
                baselineAnchorButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.anchor.baselineRelatedWarning")); // NOI18N
            } else {
                baselineAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/baseline.png"))); // NOI18N
                baselineAnchorButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.anchor.baselineRelated")); // NOI18N
            }
            nwAnchorButton.setSelected(tlanchor);
            nAnchorButton.setSelected(tcanchor);
            neAnchorButton.setSelected(tranchor);
            wAnchorButton.setSelected(lanchor);
            cAnchorButton.setSelected(canchor);
            eAnchorButton.setSelected(ranchor);
            swAnchorButton.setSelected(blanchor);
            sAnchorButton.setSelected(bcanchor);
            seAnchorButton.setSelected(branchor);
            hFillButton.setSelected(hfill==components.size());
            if(hfill>0 && hfill!=components.size()) {
                hFillButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/resize_h_warning.png"))); // NOI18N
                hFillButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.fill.horizontalWarning")); // NOI18N
            } else {
                hFillButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/resize_h.png"))); // NOI18N
                hFillButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.fill.horizontal")); // NOI18N
            }
            vFillButton.setSelected(vfill==components.size());
            if(vfill>0 && vfill!=components.size()) {
                vFillButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/resize_v_warning.png"))); // NOI18N
                vFillButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.fill.verticalWarning")); // NOI18N
            } else {
                vFillButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/resize_v.png"))); // NOI18N
                vFillButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.fill.vertical")); // NOI18N
            }
        }
        updateTooltips();
    }

    @Override
    public Component getComponent() {
        return customizer;
    }

    private void update(final int anchortype, final int anchor, final FillChange fill, final PaddingChange ipad, final Insets insets) {
        performer.performAction(new AbstractGridAction() {
            @Override
            public GridBoundsChange performAction(GridManager gridManager, DesignerContext context) {
                GridInfoProvider info = gridManager.getGridInfo();
                final int columns = info.getColumnCount();
                final int rows = info.getRowCount();
                GridUtils.removePaddingComponents(gridManager);
                Iterator<Component> it = context.getSelectedComponents().iterator();
                while (it.hasNext()) {
                    Component component = it.next();
                    if (anchor != -1) {
                        ((GridBagManager) gridManager).setAnchor(component, anchor);
                    }
                    if (anchortype != -1) {
                        final int oldanchor = ((GridBagManager) gridManager).getAnchor(component);
                        final int newanchor = convertAnchorType(anchortype,oldanchor);
                        if (newanchor != oldanchor)
                            ((GridBagManager) gridManager).setAnchor(component, newanchor);
                    }
                    if (fill != null) {
                        if(fill.hfill!=-1) ((GridBagManager) gridManager).setHorizontalFill(component, fill.hfill==1 ? true : false);
                        if(fill.vfill!=-1) ((GridBagManager) gridManager).setVerticalFill(component, fill.vfill==1 ? true : false);
                    }
                    if (ipad != null) {
                        if(ipad.xdiff!=0) ((GridBagManager) gridManager).updateIPadX(component, ipad.xdiff);
                        if(ipad.ydiff!=0) ((GridBagManager) gridManager).updateIPadY(component, ipad.ydiff);
                    }
                    if (insets != null) {
                        if(insets.top!=0 || insets.left!=0 || insets.bottom!=0 || insets.right!=0) 
                            ((GridBagManager) gridManager).updateInsets(component, insets);
                    }
                }
                gridManager.updateLayout(false);
                GridUtils.revalidateGrid(gridManager);
                GridUtils.addPaddingComponents(gridManager, columns, rows);
                GridUtils.revalidateGrid(gridManager);
                return null;
            }
        });
    }
    
    private void updateTooltips() {
        boolean baseline = baselineAnchorButton.isSelected();
        boolean bidi = bidiAnchorButton.isSelected();
        ResourceBundle bundle = NbBundle.getBundle(GridBagCustomizer.class);
        String key = (baseline ? "GridBagCustomizer.anchor.aboveBaselineLeading" // NOI18N
                : (bidi ? "GridBagCustomizer.anchor.firstLineStart" // NOI18N
                    : "GridBagCustomizer.anchor.northWest")); // NOI18N
        nwAnchorButton.setToolTipText(bundle.getString(key));
        key = (baseline ? "GridBagCustomizer.anchor.aboveBaseline" // NOI18N
                : (bidi ? "GridBagCustomizer.anchor.pageStart" // NOI18N
                    : "GridBagCustomizer.anchor.north")); // NOI18N
        nAnchorButton.setToolTipText(bundle.getString(key));
        key = (baseline ? "GridBagCustomizer.anchor.aboveBaselineTrailing" // NOI18N
                : (bidi ? "GridBagCustomizer.anchor.firstLineEnd" // NOI18N
                    : "GridBagCustomizer.anchor.northEast")); // NOI18N
        neAnchorButton.setToolTipText(bundle.getString(key));
        key = (baseline ? "GridBagCustomizer.anchor.baselineLeading" // NOI18N
                : (bidi ? "GridBagCustomizer.anchor.lineStart" // NOI18N
                    : "GridBagCustomizer.anchor.west")); // NOI18N
        wAnchorButton.setToolTipText(bundle.getString(key));
        key = (baseline ? "GridBagCustomizer.anchor.baseline" // NOI18N
                : "GridBagCustomizer.anchor.center"); // NOI18N
        cAnchorButton.setToolTipText(bundle.getString(key));
        key = (baseline ? "GridBagCustomizer.anchor.baselineTrailing" // NOI18N
                : (bidi ? "GridBagCustomizer.anchor.lineEnd" // NOI18N
                    : "GridBagCustomizer.anchor.east")); // NOI18N
        eAnchorButton.setToolTipText(bundle.getString(key));
        key = (baseline ? "GridBagCustomizer.anchor.belowBaselineLeading" // NOI18N
                : (bidi ? "GridBagCustomizer.anchor.lastLineStart" // NOI18N
                    : "GridBagCustomizer.anchor.southWest")); // NOI18N
        swAnchorButton.setToolTipText(bundle.getString(key));
        key = (baseline ? "GridBagCustomizer.anchor.belowBaseline" // NOI18N
                : (bidi ? "GridBagCustomizer.anchor.pageEnd" // NOI18N
                    : "GridBagCustomizer.anchor.south")); // NOI18N
        sAnchorButton.setToolTipText(bundle.getString(key));
        key = (baseline ? "GridBagCustomizer.anchor.belowBaselineTrailing" // NOI18N
                : (bidi ? "GridBagCustomizer.anchor.lastLineEnd" // NOI18N
                    : "GridBagCustomizer.anchor.southEast")); // NOI18N
        seAnchorButton.setToolTipText(bundle.getString(key));
    }

}
