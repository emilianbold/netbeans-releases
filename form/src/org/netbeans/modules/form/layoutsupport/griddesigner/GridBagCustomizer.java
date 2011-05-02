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
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.AbstractGridAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.GridActionPerformer;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.GridBoundsChange;
import org.openide.util.ImageUtilities;
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
        bidiCenter = true; // default to promote bidirectional anchors over absolute ones
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
    
    /**
     * bidiCenter remembers the last used anchor type;
     * this is a workaround to deal with the ambiguous meaning
     * of {@code GridBagConstraints.CENTER} which is not sufficient
     * to distinguish among ANCHOR_ABSOLUTE and ANCHOR_BIDI anchor types
     */
    private boolean bidiCenter;

    /** 
     * Parameter passing structure 
     */
    private class PaddingChange {
        PaddingChange(int xdiff, int ydiff) {
            this.xdiff = xdiff;
            this.ydiff = ydiff;
        }
        public final int xdiff; /** horizontal padding increase or decrease */
        public final int ydiff; /** vertical padding increase or decrease */
        private PaddingChange() {xdiff = ydiff = 0;}
    }

    /** 
     * Parameter passing structure 
     */
    private class FillChange {
        FillChange(int hfill, int vfill) {
            this.hfill = hfill;
            this.vfill = vfill;
        }
        public final int hfill; /** -1 = no change, 0 = NONE/VERTICAL, 1 = HORIZONTAL/BOTH */
        public final int vfill; /** -1 = no change, 0 = NONE/HORIZONTAL, 1= VERTICAL/BOTH */
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
        leftPanel = new javax.swing.JPanel();
        anchorToolGroup = new javax.swing.JPanel();
        anchorLabel = new javax.swing.JLabel();
        anchorSeparator = new javax.swing.JSeparator();
        anchorPanel = new javax.swing.JPanel();
        cAnchorButton = new javax.swing.JToggleButton();
        nAnchorButton = new javax.swing.JToggleButton();
        nwAnchorButton = new javax.swing.JToggleButton();
        eAnchorButton = new javax.swing.JToggleButton();
        neAnchorButton = new javax.swing.JToggleButton();
        sAnchorButton = new javax.swing.JToggleButton();
        seAnchorButton = new javax.swing.JToggleButton();
        wAnchorButton = new javax.swing.JToggleButton();
        swAnchorButton = new javax.swing.JToggleButton();
        anchorTypePanel = new javax.swing.JPanel();
        baselineAnchorButton = new javax.swing.JToggleButton();
        bidiAnchorButton = new javax.swing.JToggleButton();
        paddingToolGroup = new javax.swing.JPanel();
        paddingLabel = new javax.swing.JLabel();
        paddingSeparator = new javax.swing.JSeparator();
        paddingPanel = new javax.swing.JPanel();
        vminusPaddingButton = new javax.swing.JButton();
        vpadLabel = new javax.swing.JLabel();
        vplusPaddingButton = new javax.swing.JButton();
        hplusPaddingButton = new javax.swing.JButton();
        hpadLabel = new javax.swing.JLabel();
        hminusPaddingButton = new javax.swing.JButton();
        bminusPaddingButton = new javax.swing.JButton();
        bpadLabel = new javax.swing.JLabel();
        bplusPaddingButton = new javax.swing.JButton();
        gridSizeToolGroup = new javax.swing.JPanel();
        gridSizeLabel = new javax.swing.JLabel();
        gridSizeSeparator = new javax.swing.JSeparator();
        gridSizePanel = new javax.swing.JPanel();
        vgridRelativeButton = new javax.swing.JToggleButton();
        vgridRemainderButton = new javax.swing.JToggleButton();
        vgridMinusButton = new javax.swing.JButton();
        vgridPlusButton = new javax.swing.JButton();
        vgridLabel = new javax.swing.JLabel();
        hgridMinusButton = new javax.swing.JButton();
        hgridPlusButton = new javax.swing.JButton();
        hgridRelativeButton = new javax.swing.JToggleButton();
        hgridRemainderButton = new javax.swing.JToggleButton();
        hgridLabel = new javax.swing.JLabel();
        gridPositionToolGroup = new javax.swing.JPanel();
        gridPositionLabel = new javax.swing.JLabel();
        gridPositionSeparator = new javax.swing.JSeparator();
        gridPositionPanel = new javax.swing.JPanel();
        xgridMinusButton = new javax.swing.JButton();
        ygridMinusButton = new javax.swing.JButton();
        ygridPlusButton = new javax.swing.JButton();
        xgridPlusButton = new javax.swing.JButton();
        xgridRelativeButton = new javax.swing.JToggleButton();
        ygridRelativeButton = new javax.swing.JToggleButton();
        rightPanel = new javax.swing.JPanel();
        insetsToolGroup = new javax.swing.JPanel();
        insetsLabel = new javax.swing.JLabel();
        insetsSeparator = new javax.swing.JSeparator();
        insetsPanel = new javax.swing.JPanel();
        vInsetLabel = new javax.swing.JLabel();
        vminusInsetButton = new javax.swing.JButton();
        vplusInsetButton = new javax.swing.JButton();
        hInsetLabel = new javax.swing.JLabel();
        hminusInsetButton = new javax.swing.JButton();
        hplusInsetButton = new javax.swing.JButton();
        bInsetLabel = new javax.swing.JLabel();
        bminusInsetButton = new javax.swing.JButton();
        bplusInsetButton = new javax.swing.JButton();
        insetsCross = new javax.swing.JPanel();
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
        crossCenter = new javax.swing.Box.Filler(new java.awt.Dimension(20, 20), new java.awt.Dimension(20, 20), new java.awt.Dimension(20, 20));
        weightsToolGroup = new javax.swing.JPanel();
        weightsLabel = new javax.swing.JLabel();
        weightsSeparator = new javax.swing.JSeparator();
        weightsPanel = new javax.swing.JPanel();
        vweightLabel = new javax.swing.JLabel();
        vminusWeightButton = new javax.swing.JButton();
        vplusWeightButton = new javax.swing.JButton();
        hweightLabel = new javax.swing.JLabel();
        hminusWeightButton = new javax.swing.JButton();
        hplusWeightButton = new javax.swing.JButton();
        hweightEqualize = new javax.swing.JButton();
        vweightEqualize = new javax.swing.JButton();
        fillToolGroup = new javax.swing.JPanel();
        fillLabel = new javax.swing.JLabel();
        fillSeparator = new javax.swing.JSeparator();
        fillPanel = new javax.swing.JPanel();
        hFillButton = new javax.swing.JToggleButton();
        vFillButton = new javax.swing.JToggleButton();

        FormListener formListener = new FormListener();

        customizer.setPreferredSize(new java.awt.Dimension(306, 360));

        leftPanel.setPreferredSize(new java.awt.Dimension(155, 340));

        anchorToolGroup.setOpaque(false);
        anchorToolGroup.setPreferredSize(new java.awt.Dimension(145, 100));

        anchorLabel.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.anchorLabel.text")); // NOI18N

        anchorPanel.setPreferredSize(new java.awt.Dimension(125, 85));

        cAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_c.png"))); // NOI18N
        cAnchorButton.setEnabled(false);
        cAnchorButton.setFocusPainted(false);
        cAnchorButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cAnchorButton.addActionListener(formListener);

        nAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_n.png"))); // NOI18N
        nAnchorButton.setEnabled(false);
        nAnchorButton.setFocusPainted(false);
        nAnchorButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        nAnchorButton.addActionListener(formListener);

        nwAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_nw.png"))); // NOI18N
        nwAnchorButton.setEnabled(false);
        nwAnchorButton.setFocusPainted(false);
        nwAnchorButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        nwAnchorButton.addActionListener(formListener);

        eAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_e.png"))); // NOI18N
        eAnchorButton.setEnabled(false);
        eAnchorButton.setFocusPainted(false);
        eAnchorButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        eAnchorButton.addActionListener(formListener);

        neAnchorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_ne.png"))); // NOI18N
        neAnchorButton.setEnabled(false);
        neAnchorButton.setFocusPainted(false);
        neAnchorButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        neAnchorButton.addActionListener(formListener);

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

        javax.swing.GroupLayout anchorTypePanelLayout = new javax.swing.GroupLayout(anchorTypePanel);
        anchorTypePanel.setLayout(anchorTypePanelLayout);
        anchorTypePanelLayout.setHorizontalGroup(
            anchorTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(anchorTypePanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(anchorTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bidiAnchorButton)
                    .addComponent(baselineAnchorButton))
                .addGap(0, 0, 0))
        );
        anchorTypePanelLayout.setVerticalGroup(
            anchorTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(anchorTypePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bidiAnchorButton)
                .addGap(0, 0, 0)
                .addComponent(baselineAnchorButton)
                .addContainerGap())
        );

        javax.swing.GroupLayout anchorPanelLayout = new javax.swing.GroupLayout(anchorPanel);
        anchorPanel.setLayout(anchorPanelLayout);
        anchorPanelLayout.setHorizontalGroup(
            anchorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(anchorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(anchorTypePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addContainerGap())
        );
        anchorPanelLayout.setVerticalGroup(
            anchorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(anchorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(anchorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(anchorTypePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                            .addComponent(seAnchorButton))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout anchorToolGroupLayout = new javax.swing.GroupLayout(anchorToolGroup);
        anchorToolGroup.setLayout(anchorToolGroupLayout);
        anchorToolGroupLayout.setHorizontalGroup(
            anchorToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(anchorToolGroupLayout.createSequentialGroup()
                .addComponent(anchorLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(anchorSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
                .addGap(0, 0, 0))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, anchorToolGroupLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(anchorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                .addContainerGap())
        );
        anchorToolGroupLayout.setVerticalGroup(
            anchorToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(anchorToolGroupLayout.createSequentialGroup()
                .addGroup(anchorToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(anchorSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(anchorLabel))
                .addGap(0, 0, 0)
                .addComponent(anchorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        paddingToolGroup.setPreferredSize(new java.awt.Dimension(145, 80));

        paddingLabel.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.paddingLabel.text")); // NOI18N

        paddingPanel.setPreferredSize(new java.awt.Dimension(140, 65));

        vminusPaddingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        vminusPaddingButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.padding.VMinus.toolTipText")); // NOI18N
        vminusPaddingButton.setEnabled(false);
        vminusPaddingButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vminusPaddingButton.setMaximumSize(new java.awt.Dimension(20, 20));
        vminusPaddingButton.setMinimumSize(new java.awt.Dimension(20, 20));
        vminusPaddingButton.setPreferredSize(new java.awt.Dimension(20, 20));
        vminusPaddingButton.addActionListener(formListener);

        vpadLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        vpadLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/vertical.png"))); // NOI18N
        vpadLabel.setMaximumSize(new java.awt.Dimension(10, 15));
        vpadLabel.setMinimumSize(new java.awt.Dimension(10, 15));
        vpadLabel.setPreferredSize(new java.awt.Dimension(10, 15));

        vplusPaddingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        vplusPaddingButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.padding.VPlus.toolTipText")); // NOI18N
        vplusPaddingButton.setEnabled(false);
        vplusPaddingButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vplusPaddingButton.setMaximumSize(new java.awt.Dimension(20, 20));
        vplusPaddingButton.setMinimumSize(new java.awt.Dimension(20, 20));
        vplusPaddingButton.setPreferredSize(new java.awt.Dimension(20, 20));
        vplusPaddingButton.addActionListener(formListener);

        hplusPaddingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        hplusPaddingButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.padding.HPlus.toolTipText")); // NOI18N
        hplusPaddingButton.setEnabled(false);
        hplusPaddingButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hplusPaddingButton.setMaximumSize(new java.awt.Dimension(20, 20));
        hplusPaddingButton.setMinimumSize(new java.awt.Dimension(20, 20));
        hplusPaddingButton.setPreferredSize(new java.awt.Dimension(20, 20));
        hplusPaddingButton.addActionListener(formListener);

        hpadLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/horizontal.png"))); // NOI18N
        hpadLabel.setMaximumSize(new java.awt.Dimension(15, 10));
        hpadLabel.setMinimumSize(new java.awt.Dimension(15, 10));
        hpadLabel.setPreferredSize(new java.awt.Dimension(15, 10));

        hminusPaddingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        hminusPaddingButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.padding.HMinus.toolTipText")); // NOI18N
        hminusPaddingButton.setEnabled(false);
        hminusPaddingButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hminusPaddingButton.setMaximumSize(new java.awt.Dimension(20, 20));
        hminusPaddingButton.setMinimumSize(new java.awt.Dimension(20, 20));
        hminusPaddingButton.setPreferredSize(new java.awt.Dimension(20, 20));
        hminusPaddingButton.addActionListener(formListener);

        bminusPaddingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        bminusPaddingButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.padding.BMinus.toolTipText")); // NOI18N
        bminusPaddingButton.setEnabled(false);
        bminusPaddingButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        bminusPaddingButton.setMaximumSize(new java.awt.Dimension(20, 20));
        bminusPaddingButton.setMinimumSize(new java.awt.Dimension(20, 20));
        bminusPaddingButton.setPreferredSize(new java.awt.Dimension(20, 20));
        bminusPaddingButton.addActionListener(formListener);

        bpadLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        bpadLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/both.png"))); // NOI18N

        bplusPaddingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
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
                .addGap(0, 0, 0)
                .addComponent(vpadLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(paddingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(vplusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vminusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(paddingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(hpadLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(paddingPanelLayout.createSequentialGroup()
                        .addComponent(hminusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(hplusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bpadLabel)
                .addGap(0, 0, 0)
                .addGroup(paddingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(bplusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bminusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        paddingPanelLayout.setVerticalGroup(
            paddingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(paddingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(paddingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(paddingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(bpadLabel)
                        .addGroup(paddingPanelLayout.createSequentialGroup()
                            .addComponent(bplusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, 0)
                            .addComponent(bminusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(paddingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(vpadLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(paddingPanelLayout.createSequentialGroup()
                            .addComponent(vplusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, 0)
                            .addComponent(vminusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, paddingPanelLayout.createSequentialGroup()
                            .addGroup(paddingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(hplusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(hminusPaddingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(0, 0, 0)
                            .addComponent(hpadLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout paddingToolGroupLayout = new javax.swing.GroupLayout(paddingToolGroup);
        paddingToolGroup.setLayout(paddingToolGroupLayout);
        paddingToolGroupLayout.setHorizontalGroup(
            paddingToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(paddingToolGroupLayout.createSequentialGroup()
                .addComponent(paddingLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(paddingSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE))
            .addGroup(paddingToolGroupLayout.createSequentialGroup()
                .addComponent(paddingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        paddingToolGroupLayout.setVerticalGroup(
            paddingToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(paddingToolGroupLayout.createSequentialGroup()
                .addGroup(paddingToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(paddingSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(paddingLabel))
                .addGap(0, 0, 0)
                .addComponent(paddingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        gridSizeToolGroup.setPreferredSize(new java.awt.Dimension(145, 80));

        gridSizeLabel.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.gridSizeLabel.text")); // NOI18N

        gridSizePanel.setPreferredSize(new java.awt.Dimension(140, 62));

        vgridRelativeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/gridheight_relative.png"))); // NOI18N
        vgridRelativeButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vgridRelativeButton.toolTipText")); // NOI18N
        vgridRelativeButton.setEnabled(false);
        vgridRelativeButton.setMaximumSize(new java.awt.Dimension(20, 20));
        vgridRelativeButton.setMinimumSize(new java.awt.Dimension(20, 20));
        vgridRelativeButton.setPreferredSize(new java.awt.Dimension(20, 20));

        vgridRemainderButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/gridheight_remainder.png"))); // NOI18N
        vgridRemainderButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vgridRemainderButton.toolTipText")); // NOI18N
        vgridRemainderButton.setEnabled(false);
        vgridRemainderButton.setMaximumSize(new java.awt.Dimension(20, 20));
        vgridRemainderButton.setMinimumSize(new java.awt.Dimension(20, 20));
        vgridRemainderButton.setPreferredSize(new java.awt.Dimension(20, 20));

        vgridMinusButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        vgridMinusButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vgridMinusButton.toolTipText")); // NOI18N
        vgridMinusButton.setEnabled(false);
        vgridMinusButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vgridMinusButton.setMaximumSize(new java.awt.Dimension(20, 20));
        vgridMinusButton.setMinimumSize(new java.awt.Dimension(20, 20));
        vgridMinusButton.setPreferredSize(new java.awt.Dimension(20, 20));
        vgridMinusButton.addActionListener(formListener);

        vgridPlusButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        vgridPlusButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vgridPlusButton.toolTipText")); // NOI18N
        vgridPlusButton.setEnabled(false);
        vgridPlusButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vgridPlusButton.setMaximumSize(new java.awt.Dimension(20, 20));
        vgridPlusButton.setMinimumSize(new java.awt.Dimension(20, 20));
        vgridPlusButton.setPreferredSize(new java.awt.Dimension(20, 20));
        vgridPlusButton.addActionListener(formListener);

        vgridLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        vgridLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/gridheight.png"))); // NOI18N
        vgridLabel.setMaximumSize(new java.awt.Dimension(10, 15));
        vgridLabel.setMinimumSize(new java.awt.Dimension(10, 15));
        vgridLabel.setPreferredSize(new java.awt.Dimension(10, 15));

        hgridMinusButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        hgridMinusButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hgridMinusButton.toolTipText")); // NOI18N
        hgridMinusButton.setEnabled(false);
        hgridMinusButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hgridMinusButton.setMaximumSize(new java.awt.Dimension(20, 20));
        hgridMinusButton.setMinimumSize(new java.awt.Dimension(20, 20));
        hgridMinusButton.setPreferredSize(new java.awt.Dimension(20, 20));
        hgridMinusButton.addActionListener(formListener);

        hgridPlusButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        hgridPlusButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hgridPlusButton.toolTipText")); // NOI18N
        hgridPlusButton.setEnabled(false);
        hgridPlusButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hgridPlusButton.setMaximumSize(new java.awt.Dimension(20, 20));
        hgridPlusButton.setMinimumSize(new java.awt.Dimension(20, 20));
        hgridPlusButton.setPreferredSize(new java.awt.Dimension(20, 20));
        hgridPlusButton.addActionListener(formListener);

        hgridRelativeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/gridwidth_relative.png"))); // NOI18N
        hgridRelativeButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hgridRelativeButton.toolTipText")); // NOI18N
        hgridRelativeButton.setEnabled(false);
        hgridRelativeButton.setMaximumSize(new java.awt.Dimension(20, 20));
        hgridRelativeButton.setMinimumSize(new java.awt.Dimension(20, 20));
        hgridRelativeButton.setPreferredSize(new java.awt.Dimension(20, 20));

        hgridRemainderButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/gridwidth_remainder.png"))); // NOI18N
        hgridRemainderButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hgridRemainderButton.toolTipText")); // NOI18N
        hgridRemainderButton.setEnabled(false);
        hgridRemainderButton.setMaximumSize(new java.awt.Dimension(20, 20));
        hgridRemainderButton.setMinimumSize(new java.awt.Dimension(20, 20));
        hgridRemainderButton.setPreferredSize(new java.awt.Dimension(20, 20));

        hgridLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/gridwidth.png"))); // NOI18N
        hgridLabel.setPreferredSize(new java.awt.Dimension(15, 10));

        javax.swing.GroupLayout gridSizePanelLayout = new javax.swing.GroupLayout(gridSizePanel);
        gridSizePanel.setLayout(gridSizePanelLayout);
        gridSizePanelLayout.setHorizontalGroup(
            gridSizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gridSizePanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(vgridLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(gridSizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(vgridMinusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vgridPlusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(gridSizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addGroup(gridSizePanelLayout.createSequentialGroup()
                        .addComponent(hgridMinusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(hgridPlusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(hgridLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(gridSizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(vgridRelativeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hgridRelativeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(gridSizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(vgridRemainderButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hgridRemainderButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        gridSizePanelLayout.setVerticalGroup(
            gridSizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gridSizePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(gridSizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(gridSizePanelLayout.createSequentialGroup()
                        .addComponent(hgridRemainderButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(vgridRemainderButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(gridSizePanelLayout.createSequentialGroup()
                        .addComponent(hgridRelativeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(vgridRelativeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(gridSizePanelLayout.createSequentialGroup()
                        .addGroup(gridSizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(hgridPlusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(hgridMinusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, 0)
                        .addComponent(hgridLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(gridSizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(vgridLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(gridSizePanelLayout.createSequentialGroup()
                            .addComponent(vgridMinusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, 0)
                            .addComponent(vgridPlusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout gridSizeToolGroupLayout = new javax.swing.GroupLayout(gridSizeToolGroup);
        gridSizeToolGroup.setLayout(gridSizeToolGroupLayout);
        gridSizeToolGroupLayout.setHorizontalGroup(
            gridSizeToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gridSizeToolGroupLayout.createSequentialGroup()
                .addComponent(gridSizeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gridSizeSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE))
            .addGroup(gridSizeToolGroupLayout.createSequentialGroup()
                .addComponent(gridSizePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        gridSizeToolGroupLayout.setVerticalGroup(
            gridSizeToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gridSizeToolGroupLayout.createSequentialGroup()
                .addGroup(gridSizeToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(gridSizeSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gridSizeLabel))
                .addGap(0, 0, 0)
                .addComponent(gridSizePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        gridPositionToolGroup.setPreferredSize(new java.awt.Dimension(145, 80));

        gridPositionLabel.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.gridPositionLabel.text")); // NOI18N

        gridPositionPanel.setPreferredSize(new java.awt.Dimension(124, 62));

        xgridMinusButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_w.png"))); // NOI18N
        xgridMinusButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.xgridMinusButton.toolTipText")); // NOI18N
        xgridMinusButton.setEnabled(false);
        xgridMinusButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        xgridMinusButton.setMaximumSize(new java.awt.Dimension(20, 20));
        xgridMinusButton.setMinimumSize(new java.awt.Dimension(20, 20));
        xgridMinusButton.setPreferredSize(new java.awt.Dimension(20, 20));
        xgridMinusButton.addActionListener(formListener);

        ygridMinusButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_n.png"))); // NOI18N
        ygridMinusButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.ygridMinusButton.toolTipText")); // NOI18N
        ygridMinusButton.setEnabled(false);
        ygridMinusButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ygridMinusButton.setMaximumSize(new java.awt.Dimension(20, 20));
        ygridMinusButton.setMinimumSize(new java.awt.Dimension(20, 20));
        ygridMinusButton.setPreferredSize(new java.awt.Dimension(20, 20));
        ygridMinusButton.addActionListener(formListener);

        ygridPlusButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_s.png"))); // NOI18N
        ygridPlusButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.ygridPlusButton.toolTipText")); // NOI18N
        ygridPlusButton.setEnabled(false);
        ygridPlusButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ygridPlusButton.setMaximumSize(new java.awt.Dimension(20, 20));
        ygridPlusButton.setMinimumSize(new java.awt.Dimension(20, 20));
        ygridPlusButton.setPreferredSize(new java.awt.Dimension(20, 20));
        ygridPlusButton.addActionListener(formListener);

        xgridPlusButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/anchor_e.png"))); // NOI18N
        xgridPlusButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.xgridPlusButton.toolTipText")); // NOI18N
        xgridPlusButton.setEnabled(false);
        xgridPlusButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        xgridPlusButton.setMaximumSize(new java.awt.Dimension(20, 20));
        xgridPlusButton.setMinimumSize(new java.awt.Dimension(20, 20));
        xgridPlusButton.setPreferredSize(new java.awt.Dimension(20, 20));
        xgridPlusButton.addActionListener(formListener);

        xgridRelativeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/gridx_relative.png"))); // NOI18N
        xgridRelativeButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.xgridRelativeButton.toolTipText")); // NOI18N
        xgridRelativeButton.setEnabled(false);
        xgridRelativeButton.setMaximumSize(new java.awt.Dimension(20, 20));
        xgridRelativeButton.setMinimumSize(new java.awt.Dimension(20, 20));
        xgridRelativeButton.setPreferredSize(new java.awt.Dimension(20, 20));

        ygridRelativeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/gridy_relative.png"))); // NOI18N
        ygridRelativeButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.ygridRelativeButton.toolTipText")); // NOI18N
        ygridRelativeButton.setEnabled(false);
        ygridRelativeButton.setMaximumSize(new java.awt.Dimension(20, 20));
        ygridRelativeButton.setMinimumSize(new java.awt.Dimension(20, 20));
        ygridRelativeButton.setPreferredSize(new java.awt.Dimension(20, 20));

        javax.swing.GroupLayout gridPositionPanelLayout = new javax.swing.GroupLayout(gridPositionPanel);
        gridPositionPanel.setLayout(gridPositionPanelLayout);
        gridPositionPanelLayout.setHorizontalGroup(
            gridPositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gridPositionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(xgridMinusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(gridPositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(ygridMinusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ygridPlusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addComponent(xgridPlusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(gridPositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(ygridRelativeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(xgridRelativeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        gridPositionPanelLayout.setVerticalGroup(
            gridPositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gridPositionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(gridPositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(gridPositionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(xgridMinusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(xgridPlusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(gridPositionPanelLayout.createSequentialGroup()
                            .addComponent(ygridMinusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, 0)
                            .addComponent(ygridPlusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.CENTER, gridPositionPanelLayout.createSequentialGroup()
                        .addComponent(xgridRelativeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(ygridRelativeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout gridPositionToolGroupLayout = new javax.swing.GroupLayout(gridPositionToolGroup);
        gridPositionToolGroup.setLayout(gridPositionToolGroupLayout);
        gridPositionToolGroupLayout.setHorizontalGroup(
            gridPositionToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gridPositionToolGroupLayout.createSequentialGroup()
                .addComponent(gridPositionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gridPositionSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE))
            .addGroup(gridPositionToolGroupLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(gridPositionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        gridPositionToolGroupLayout.setVerticalGroup(
            gridPositionToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gridPositionToolGroupLayout.createSequentialGroup()
                .addGroup(gridPositionToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(gridPositionSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gridPositionLabel))
                .addGap(0, 0, 0)
                .addComponent(gridPositionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout leftPanelLayout = new javax.swing.GroupLayout(leftPanel);
        leftPanel.setLayout(leftPanelLayout);
        leftPanelLayout.setHorizontalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(gridPositionToolGroup, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(gridSizeToolGroup, javax.swing.GroupLayout.Alignment.LEADING, 0, 145, Short.MAX_VALUE)
                    .addComponent(paddingToolGroup, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(anchorToolGroup, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        leftPanelLayout.setVerticalGroup(
            leftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(leftPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(anchorToolGroup, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(paddingToolGroup, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(gridSizeToolGroup, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(gridPositionToolGroup, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        insetsToolGroup.setPreferredSize(new java.awt.Dimension(135, 195));

        insetsLabel.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.insetsLabel.text")); // NOI18N

        insetsPanel.setPreferredSize(new java.awt.Dimension(124, 139));

        vInsetLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        vInsetLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/inset_v.png"))); // NOI18N
        vInsetLabel.setMaximumSize(new java.awt.Dimension(10, 15));
        vInsetLabel.setMinimumSize(new java.awt.Dimension(10, 15));
        vInsetLabel.setPreferredSize(new java.awt.Dimension(10, 15));

        vminusInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        vminusInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vminusInsetButton.toolTipText")); // NOI18N
        vminusInsetButton.setEnabled(false);
        vminusInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vminusInsetButton.setMaximumSize(new java.awt.Dimension(20, 20));
        vminusInsetButton.setMinimumSize(new java.awt.Dimension(20, 20));
        vminusInsetButton.setPreferredSize(new java.awt.Dimension(20, 20));
        vminusInsetButton.addActionListener(formListener);

        vplusInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        vplusInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vplusInsetButton.toolTipText")); // NOI18N
        vplusInsetButton.setEnabled(false);
        vplusInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vplusInsetButton.setMaximumSize(new java.awt.Dimension(20, 20));
        vplusInsetButton.setMinimumSize(new java.awt.Dimension(20, 20));
        vplusInsetButton.setPreferredSize(new java.awt.Dimension(20, 20));
        vplusInsetButton.addActionListener(formListener);

        hInsetLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/inset_h.png"))); // NOI18N
        hInsetLabel.setMaximumSize(new java.awt.Dimension(15, 10));
        hInsetLabel.setMinimumSize(new java.awt.Dimension(15, 10));
        hInsetLabel.setPreferredSize(new java.awt.Dimension(15, 10));

        hminusInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        hminusInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hminusInsetButton.toolTipText")); // NOI18N
        hminusInsetButton.setEnabled(false);
        hminusInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hminusInsetButton.setMaximumSize(new java.awt.Dimension(20, 20));
        hminusInsetButton.setMinimumSize(new java.awt.Dimension(20, 20));
        hminusInsetButton.setPreferredSize(new java.awt.Dimension(20, 20));
        hminusInsetButton.addActionListener(formListener);

        hplusInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        hplusInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hplusInsetButton.toolTipText")); // NOI18N
        hplusInsetButton.setEnabled(false);
        hplusInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hplusInsetButton.setMaximumSize(new java.awt.Dimension(20, 20));
        hplusInsetButton.setMinimumSize(new java.awt.Dimension(20, 20));
        hplusInsetButton.setPreferredSize(new java.awt.Dimension(20, 20));
        hplusInsetButton.addActionListener(formListener);

        bInsetLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        bInsetLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/inset_both.png"))); // NOI18N

        bminusInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        bminusInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.bminusInsetButton.toolTipText")); // NOI18N
        bminusInsetButton.setEnabled(false);
        bminusInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        bminusInsetButton.setMaximumSize(new java.awt.Dimension(20, 20));
        bminusInsetButton.setMinimumSize(new java.awt.Dimension(20, 20));
        bminusInsetButton.setPreferredSize(new java.awt.Dimension(20, 20));
        bminusInsetButton.addActionListener(formListener);

        bplusInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
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
                        .addGap(0, 0, 0)
                        .addComponent(vminusInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(insetsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(hInsetLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(insetsPanelLayout.createSequentialGroup()
                        .addComponent(hminusInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(hplusInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bInsetLabel)
                .addGap(0, 0, 0)
                .addGroup(insetsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bplusInsetButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bminusInsetButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        insetsPanelLayout.setVerticalGroup(
            insetsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(insetsPanelLayout.createSequentialGroup()
                .addContainerGap()
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        topLeftCorner.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/corner_tl.png"))); // NOI18N

        vplusTopInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        vplusTopInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vplusTopInsetButton.toolTipText")); // NOI18N
        vplusTopInsetButton.setEnabled(false);
        vplusTopInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vplusTopInsetButton.setMaximumSize(new java.awt.Dimension(20, 20));
        vplusTopInsetButton.setMinimumSize(new java.awt.Dimension(20, 20));
        vplusTopInsetButton.setPreferredSize(new java.awt.Dimension(20, 18));
        vplusTopInsetButton.addActionListener(formListener);

        vminusTopInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        vminusTopInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vminusTopInsetButton.toolTipText")); // NOI18N
        vminusTopInsetButton.setEnabled(false);
        vminusTopInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vminusTopInsetButton.setMaximumSize(new java.awt.Dimension(20, 20));
        vminusTopInsetButton.setMinimumSize(new java.awt.Dimension(20, 20));
        vminusTopInsetButton.setPreferredSize(new java.awt.Dimension(20, 18));
        vminusTopInsetButton.addActionListener(formListener);

        topRightCorner.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/corner_tr.png"))); // NOI18N

        hplusLeftInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        hplusLeftInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hplusLeftInsetButton.toolTipText")); // NOI18N
        hplusLeftInsetButton.setEnabled(false);
        hplusLeftInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hplusLeftInsetButton.setMaximumSize(new java.awt.Dimension(20, 20));
        hplusLeftInsetButton.setMinimumSize(new java.awt.Dimension(20, 20));
        hplusLeftInsetButton.setPreferredSize(new java.awt.Dimension(18, 20));
        hplusLeftInsetButton.addActionListener(formListener);

        hminusLeftInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        hminusLeftInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hminusLeftInsetButton.toolTipText")); // NOI18N
        hminusLeftInsetButton.setEnabled(false);
        hminusLeftInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hminusLeftInsetButton.setMaximumSize(new java.awt.Dimension(18, 20));
        hminusLeftInsetButton.setMinimumSize(new java.awt.Dimension(18, 20));
        hminusLeftInsetButton.setPreferredSize(new java.awt.Dimension(18, 20));
        hminusLeftInsetButton.addActionListener(formListener);

        hminusRightInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        hminusRightInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hminusRightInsetButton.toolTipText")); // NOI18N
        hminusRightInsetButton.setEnabled(false);
        hminusRightInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hminusRightInsetButton.setMaximumSize(new java.awt.Dimension(18, 20));
        hminusRightInsetButton.setMinimumSize(new java.awt.Dimension(18, 20));
        hminusRightInsetButton.setPreferredSize(new java.awt.Dimension(18, 20));
        hminusRightInsetButton.addActionListener(formListener);

        hplusRightInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        hplusRightInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hplusRightInsetButton.toolTipText")); // NOI18N
        hplusRightInsetButton.setEnabled(false);
        hplusRightInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hplusRightInsetButton.setMaximumSize(new java.awt.Dimension(20, 20));
        hplusRightInsetButton.setMinimumSize(new java.awt.Dimension(20, 20));
        hplusRightInsetButton.setPreferredSize(new java.awt.Dimension(18, 20));
        hplusRightInsetButton.addActionListener(formListener);

        bottomLeftCorner.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/corner_bl.png"))); // NOI18N

        vminusBottomInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        vminusBottomInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vminusBottomInsetButton.toolTipText")); // NOI18N
        vminusBottomInsetButton.setEnabled(false);
        vminusBottomInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vminusBottomInsetButton.setMaximumSize(new java.awt.Dimension(20, 20));
        vminusBottomInsetButton.setMinimumSize(new java.awt.Dimension(20, 20));
        vminusBottomInsetButton.setPreferredSize(new java.awt.Dimension(20, 18));
        vminusBottomInsetButton.addActionListener(formListener);

        vplusBottomInsetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        vplusBottomInsetButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vplusBottomInsetButton.toolTipText")); // NOI18N
        vplusBottomInsetButton.setEnabled(false);
        vplusBottomInsetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vplusBottomInsetButton.setMaximumSize(new java.awt.Dimension(20, 20));
        vplusBottomInsetButton.setMinimumSize(new java.awt.Dimension(20, 20));
        vplusBottomInsetButton.setPreferredSize(new java.awt.Dimension(20, 18));
        vplusBottomInsetButton.addActionListener(formListener);

        bottomRightCorner.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/corner_br.png"))); // NOI18N

        javax.swing.GroupLayout insetsCrossLayout = new javax.swing.GroupLayout(insetsCross);
        insetsCross.setLayout(insetsCrossLayout);
        insetsCrossLayout.setHorizontalGroup(
            insetsCrossLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(insetsCrossLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(insetsCrossLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(insetsCrossLayout.createSequentialGroup()
                        .addComponent(topLeftCorner)
                        .addGap(0, 0, 0))
                    .addGroup(insetsCrossLayout.createSequentialGroup()
                        .addComponent(hplusLeftInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(hminusLeftInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(insetsCrossLayout.createSequentialGroup()
                        .addComponent(bottomLeftCorner)
                        .addGap(0, 0, 0)))
                .addGroup(insetsCrossLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(vplusTopInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vminusTopInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(crossCenter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vminusBottomInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vplusBottomInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(insetsCrossLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bottomRightCorner)
                    .addGroup(insetsCrossLayout.createSequentialGroup()
                        .addComponent(hminusRightInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(hplusRightInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(topRightCorner))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        insetsCrossLayout.setVerticalGroup(
            insetsCrossLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(insetsCrossLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(insetsCrossLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(topLeftCorner)
                    .addGroup(insetsCrossLayout.createSequentialGroup()
                        .addComponent(vplusTopInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addGroup(insetsCrossLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(vminusTopInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(topRightCorner))))
                .addGap(0, 0, 0)
                .addGroup(insetsCrossLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(hplusLeftInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hminusLeftInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hminusRightInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(crossCenter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hplusRightInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(insetsCrossLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bottomRightCorner)
                    .addGroup(insetsCrossLayout.createSequentialGroup()
                        .addComponent(vminusBottomInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(vplusBottomInsetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(bottomLeftCorner))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout insetsToolGroupLayout = new javax.swing.GroupLayout(insetsToolGroup);
        insetsToolGroup.setLayout(insetsToolGroupLayout);
        insetsToolGroupLayout.setHorizontalGroup(
            insetsToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(insetsToolGroupLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(insetsToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(insetsToolGroupLayout.createSequentialGroup()
                        .addComponent(insetsLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(insetsSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE))
                    .addGroup(insetsToolGroupLayout.createSequentialGroup()
                        .addGap(0, 0, 0)
                        .addComponent(insetsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addGroup(insetsToolGroupLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(insetsCross, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        insetsToolGroupLayout.setVerticalGroup(
            insetsToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(insetsToolGroupLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(insetsToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(insetsLabel)
                    .addComponent(insetsSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addComponent(insetsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(insetsCross, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        weightsLabel.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.weightsLabel.text")); // NOI18N

        weightsPanel.setPreferredSize(new java.awt.Dimension(140, 60));

        vweightLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        vweightLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/weight_vertical.png"))); // NOI18N

        vminusWeightButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        vminusWeightButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vminusWeightButton.toolTipText")); // NOI18N
        vminusWeightButton.setEnabled(false);
        vminusWeightButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vminusWeightButton.setMaximumSize(new java.awt.Dimension(20, 20));
        vminusWeightButton.setMinimumSize(new java.awt.Dimension(20, 20));
        vminusWeightButton.setPreferredSize(new java.awt.Dimension(20, 20));
        vminusWeightButton.addActionListener(formListener);

        vplusWeightButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        vplusWeightButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vplusWeightButton.toolTipText")); // NOI18N
        vplusWeightButton.setEnabled(false);
        vplusWeightButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vplusWeightButton.setMaximumSize(new java.awt.Dimension(20, 20));
        vplusWeightButton.setMinimumSize(new java.awt.Dimension(20, 20));
        vplusWeightButton.setPreferredSize(new java.awt.Dimension(20, 20));
        vplusWeightButton.addActionListener(formListener);

        hweightLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/weight_horizontal.png"))); // NOI18N

        hminusWeightButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/minus.png"))); // NOI18N
        hminusWeightButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hminusWeightButton.toolTipText")); // NOI18N
        hminusWeightButton.setEnabled(false);
        hminusWeightButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hminusWeightButton.setMaximumSize(new java.awt.Dimension(20, 20));
        hminusWeightButton.setMinimumSize(new java.awt.Dimension(20, 20));
        hminusWeightButton.setPreferredSize(new java.awt.Dimension(20, 20));
        hminusWeightButton.addActionListener(formListener);

        hplusWeightButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/plus.png"))); // NOI18N
        hplusWeightButton.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hplusWeightButton.toolTipText")); // NOI18N
        hplusWeightButton.setEnabled(false);
        hplusWeightButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hplusWeightButton.setMaximumSize(new java.awt.Dimension(20, 20));
        hplusWeightButton.setMinimumSize(new java.awt.Dimension(20, 20));
        hplusWeightButton.setPreferredSize(new java.awt.Dimension(20, 20));
        hplusWeightButton.addActionListener(formListener);

        hweightEqualize.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/weight_equal_horizontal.png"))); // NOI18N
        hweightEqualize.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.hweightEqualize.toolTipText")); // NOI18N
        hweightEqualize.setEnabled(false);
        hweightEqualize.setMargin(new java.awt.Insets(0, 0, 0, 0));
        hweightEqualize.setMaximumSize(new java.awt.Dimension(20, 20));
        hweightEqualize.setMinimumSize(new java.awt.Dimension(20, 20));
        hweightEqualize.setPreferredSize(new java.awt.Dimension(20, 20));
        hweightEqualize.addActionListener(formListener);

        vweightEqualize.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/layoutsupport/griddesigner/resources/weight_equal_vertical.png"))); // NOI18N
        vweightEqualize.setToolTipText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.vweightEqualize.toolTipText")); // NOI18N
        vweightEqualize.setEnabled(false);
        vweightEqualize.setMargin(new java.awt.Insets(0, 0, 0, 0));
        vweightEqualize.setMaximumSize(new java.awt.Dimension(20, 20));
        vweightEqualize.setMinimumSize(new java.awt.Dimension(20, 20));
        vweightEqualize.setPreferredSize(new java.awt.Dimension(20, 20));
        vweightEqualize.addActionListener(formListener);

        javax.swing.GroupLayout weightsPanelLayout = new javax.swing.GroupLayout(weightsPanel);
        weightsPanel.setLayout(weightsPanelLayout);
        weightsPanelLayout.setHorizontalGroup(
            weightsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(weightsPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(vweightLabel)
                .addGap(0, 0, 0)
                .addGroup(weightsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(vplusWeightButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vminusWeightButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(weightsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(hweightLabel)
                    .addGroup(weightsPanelLayout.createSequentialGroup()
                        .addComponent(hminusWeightButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(hplusWeightButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(weightsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(hweightEqualize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vweightEqualize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        weightsPanelLayout.setVerticalGroup(
            weightsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(weightsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(weightsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(vweightLabel)
                    .addGroup(weightsPanelLayout.createSequentialGroup()
                        .addComponent(vplusWeightButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(vminusWeightButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(weightsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(weightsPanelLayout.createSequentialGroup()
                            .addComponent(hweightEqualize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, 0)
                            .addComponent(vweightEqualize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(weightsPanelLayout.createSequentialGroup()
                            .addGroup(weightsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(hplusWeightButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(hminusWeightButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(0, 0, 0)
                            .addComponent(hweightLabel))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout weightsToolGroupLayout = new javax.swing.GroupLayout(weightsToolGroup);
        weightsToolGroup.setLayout(weightsToolGroupLayout);
        weightsToolGroupLayout.setHorizontalGroup(
            weightsToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(weightsToolGroupLayout.createSequentialGroup()
                .addComponent(weightsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(weightsSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE))
            .addComponent(weightsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        weightsToolGroupLayout.setVerticalGroup(
            weightsToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(weightsToolGroupLayout.createSequentialGroup()
                .addGroup(weightsToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(weightsSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(weightsLabel))
                .addGap(0, 0, 0)
                .addComponent(weightsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        fillToolGroup.setPreferredSize(new java.awt.Dimension(135, 47));

        fillLabel.setText(org.openide.util.NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.fillLabel.text")); // NOI18N

        fillPanel.setPreferredSize(new java.awt.Dimension(124, 20));

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
                .addGap(42, 42, 42)
                .addComponent(hFillButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(vFillButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(36, Short.MAX_VALUE))
        );
        fillPanelLayout.setVerticalGroup(
            fillPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(hFillButton)
            .addComponent(vFillButton)
        );

        javax.swing.GroupLayout fillToolGroupLayout = new javax.swing.GroupLayout(fillToolGroup);
        fillToolGroup.setLayout(fillToolGroupLayout);
        fillToolGroupLayout.setHorizontalGroup(
            fillToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fillToolGroupLayout.createSequentialGroup()
                .addComponent(fillLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fillSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE))
            .addComponent(fillPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        fillToolGroupLayout.setVerticalGroup(
            fillToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fillToolGroupLayout.createSequentialGroup()
                .addGroup(fillToolGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(fillSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fillLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fillPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout rightPanelLayout = new javax.swing.GroupLayout(rightPanel);
        rightPanel.setLayout(rightPanelLayout);
        rightPanelLayout.setHorizontalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(weightsToolGroup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(rightPanelLayout.createSequentialGroup()
                        .addGroup(rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(insetsToolGroup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fillToolGroup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, 0))))
        );
        rightPanelLayout.setVerticalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rightPanelLayout.createSequentialGroup()
                .addComponent(insetsToolGroup, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(fillToolGroup, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(weightsToolGroup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout customizerLayout = new javax.swing.GroupLayout(customizer);
        customizer.setLayout(customizerLayout);
        customizerLayout.setHorizontalGroup(
            customizerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(customizerLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(leftPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(rightPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        customizerLayout.setVerticalGroup(
            customizerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(customizerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(customizerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(leftPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rightPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == cAnchorButton) {
                GridBagCustomizer.this.cAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == nAnchorButton) {
                GridBagCustomizer.this.nAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == nwAnchorButton) {
                GridBagCustomizer.this.nwAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == eAnchorButton) {
                GridBagCustomizer.this.eAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == neAnchorButton) {
                GridBagCustomizer.this.neAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == sAnchorButton) {
                GridBagCustomizer.this.sAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == seAnchorButton) {
                GridBagCustomizer.this.seAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == wAnchorButton) {
                GridBagCustomizer.this.wAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == swAnchorButton) {
                GridBagCustomizer.this.swAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == baselineAnchorButton) {
                GridBagCustomizer.this.baselineAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == bidiAnchorButton) {
                GridBagCustomizer.this.bidiAnchorButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vminusPaddingButton) {
                GridBagCustomizer.this.vminusPaddingButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vplusPaddingButton) {
                GridBagCustomizer.this.vplusPaddingButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hplusPaddingButton) {
                GridBagCustomizer.this.hplusPaddingButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hminusPaddingButton) {
                GridBagCustomizer.this.hminusPaddingButtonActionPerformed(evt);
            }
            else if (evt.getSource() == bminusPaddingButton) {
                GridBagCustomizer.this.bminusPaddingButtonActionPerformed(evt);
            }
            else if (evt.getSource() == bplusPaddingButton) {
                GridBagCustomizer.this.bplusPaddingButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vgridMinusButton) {
                GridBagCustomizer.this.vgridMinusButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vgridPlusButton) {
                GridBagCustomizer.this.vgridPlusButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hgridMinusButton) {
                GridBagCustomizer.this.hgridMinusButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hgridPlusButton) {
                GridBagCustomizer.this.hgridPlusButtonActionPerformed(evt);
            }
            else if (evt.getSource() == xgridMinusButton) {
                GridBagCustomizer.this.xgridMinusButtonActionPerformed(evt);
            }
            else if (evt.getSource() == ygridMinusButton) {
                GridBagCustomizer.this.ygridMinusButtonActionPerformed(evt);
            }
            else if (evt.getSource() == ygridPlusButton) {
                GridBagCustomizer.this.ygridPlusButtonActionPerformed(evt);
            }
            else if (evt.getSource() == xgridPlusButton) {
                GridBagCustomizer.this.xgridPlusButtonActionPerformed(evt);
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
            else if (evt.getSource() == vminusWeightButton) {
                GridBagCustomizer.this.vminusWeightButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vplusWeightButton) {
                GridBagCustomizer.this.vplusWeightButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hminusWeightButton) {
                GridBagCustomizer.this.hminusWeightButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hplusWeightButton) {
                GridBagCustomizer.this.hplusWeightButtonActionPerformed(evt);
            }
            else if (evt.getSource() == hweightEqualize) {
                GridBagCustomizer.this.hweightEqualizeActionPerformed(evt);
            }
            else if (evt.getSource() == vweightEqualize) {
                GridBagCustomizer.this.vweightEqualizeActionPerformed(evt);
            }
            else if (evt.getSource() == hFillButton) {
                GridBagCustomizer.this.hFillButtonActionPerformed(evt);
            }
            else if (evt.getSource() == vFillButton) {
                GridBagCustomizer.this.vFillButtonActionPerformed(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void nwAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nwAnchorButtonActionPerformed
        int anchor = currentAnchorSpecialization(GridBagConstraints.NORTHWEST);
        selectAnchorButtons(anchor);
        update(-1, anchor, null, null, null);
    }//GEN-LAST:event_nwAnchorButtonActionPerformed

    private void nAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nAnchorButtonActionPerformed
        int anchor = currentAnchorSpecialization(GridBagConstraints.NORTH);
        selectAnchorButtons(anchor);
        update(-1, anchor, null, null, null);
    }//GEN-LAST:event_nAnchorButtonActionPerformed

    private void neAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_neAnchorButtonActionPerformed
        int anchor = currentAnchorSpecialization(GridBagConstraints.NORTHEAST);
        selectAnchorButtons(anchor);
        update(-1, anchor, null, null, null);
    }//GEN-LAST:event_neAnchorButtonActionPerformed

    private void wAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wAnchorButtonActionPerformed
        int anchor = currentAnchorSpecialization(GridBagConstraints.WEST);
        selectAnchorButtons(anchor);
        update(-1, anchor, null, null, null);
    }//GEN-LAST:event_wAnchorButtonActionPerformed

    private void cAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cAnchorButtonActionPerformed
        int anchor = currentAnchorSpecialization(GridBagConstraints.CENTER);
        selectAnchorButtons(anchor);
        update(-1, anchor, null, null, null);
    }//GEN-LAST:event_cAnchorButtonActionPerformed

    private void eAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eAnchorButtonActionPerformed
        int anchor = currentAnchorSpecialization(GridBagConstraints.EAST);
        selectAnchorButtons(anchor);
        update(-1, anchor, null, null, null);
    }//GEN-LAST:event_eAnchorButtonActionPerformed

    private void swAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_swAnchorButtonActionPerformed
        int anchor = currentAnchorSpecialization(GridBagConstraints.SOUTHWEST);
        selectAnchorButtons(anchor);
        update(-1, anchor, null, null, null);
    }//GEN-LAST:event_swAnchorButtonActionPerformed

    private void sAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sAnchorButtonActionPerformed
        int anchor = currentAnchorSpecialization(GridBagConstraints.SOUTH);
        selectAnchorButtons(anchor);
        update(-1, anchor, null, null, null);
    }//GEN-LAST:event_sAnchorButtonActionPerformed

    private void seAnchorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seAnchorButtonActionPerformed
        int anchor = currentAnchorSpecialization(GridBagConstraints.SOUTHEAST);
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
        boolean baseline = baselineAnchorButton.isSelected();
        boolean bidi;
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
        boolean bidi = bidiAnchorButton.isSelected();
        bidiCenter = bidi;
        boolean baseline;
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
        int changeby = 1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, new PaddingChange(-changeby, 0), null);
    }//GEN-LAST:event_hminusPaddingButtonActionPerformed

    private void hplusPaddingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hplusPaddingButtonActionPerformed
        int changeby = 1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, new PaddingChange(changeby, 0), null);
    }//GEN-LAST:event_hplusPaddingButtonActionPerformed

    private void bminusPaddingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bminusPaddingButtonActionPerformed
        int changeby = 1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, new PaddingChange(-changeby, -changeby), null);
    }//GEN-LAST:event_bminusPaddingButtonActionPerformed

    private void bplusPaddingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bplusPaddingButtonActionPerformed
        int changeby = 1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, new PaddingChange(changeby, changeby), null);
    }//GEN-LAST:event_bplusPaddingButtonActionPerformed

    private void bplusInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bplusInsetButtonActionPerformed
        int changeby = 1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new Insets(changeby, changeby, changeby, changeby));
    }//GEN-LAST:event_bplusInsetButtonActionPerformed

    private void bminusInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bminusInsetButtonActionPerformed
        int changeby = 1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new Insets(-changeby, -changeby, -changeby, -changeby));
    }//GEN-LAST:event_bminusInsetButtonActionPerformed

    private void hminusInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hminusInsetButtonActionPerformed
        int changeby = 1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new Insets(0, -changeby, 0, -changeby));
    }//GEN-LAST:event_hminusInsetButtonActionPerformed

    private void hplusInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hplusInsetButtonActionPerformed
        int changeby = 1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new Insets(0, changeby, 0, changeby));
    }//GEN-LAST:event_hplusInsetButtonActionPerformed

    private void vminusInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vminusInsetButtonActionPerformed
        int changeby = 1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new Insets(-changeby, 0, -changeby, 0));
    }//GEN-LAST:event_vminusInsetButtonActionPerformed

    private void vplusInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vplusInsetButtonActionPerformed
        int changeby = 1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new Insets(changeby, 0, changeby, 0));
    }//GEN-LAST:event_vplusInsetButtonActionPerformed

    private void xgridMinusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xgridMinusButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_xgridMinusButtonActionPerformed

    private void xgridPlusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xgridPlusButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_xgridPlusButtonActionPerformed

    private void ygridPlusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ygridPlusButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ygridPlusButtonActionPerformed

    private void ygridMinusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ygridMinusButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ygridMinusButtonActionPerformed

    private void hminusWeightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hminusWeightButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_hminusWeightButtonActionPerformed

    private void hplusWeightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hplusWeightButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_hplusWeightButtonActionPerformed

    private void vminusWeightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vminusWeightButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_vminusWeightButtonActionPerformed

    private void vplusWeightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vplusWeightButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_vplusWeightButtonActionPerformed

    private void vweightEqualizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vweightEqualizeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_vweightEqualizeActionPerformed

    private void hweightEqualizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hweightEqualizeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_hweightEqualizeActionPerformed

    private void hgridPlusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hgridPlusButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_hgridPlusButtonActionPerformed

    private void hgridMinusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hgridMinusButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_hgridMinusButtonActionPerformed

    private void vgridMinusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vgridMinusButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_vgridMinusButtonActionPerformed

    private void vgridPlusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vgridPlusButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_vgridPlusButtonActionPerformed

    private void vplusPaddingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vplusPaddingButtonActionPerformed
        int changeby = 1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, new PaddingChange(0, changeby), null);
}//GEN-LAST:event_vplusPaddingButtonActionPerformed

    private void vminusPaddingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vminusPaddingButtonActionPerformed
        int changeby = 1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, new PaddingChange(0, -changeby), null);
}//GEN-LAST:event_vminusPaddingButtonActionPerformed

    private void vplusTopInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vplusTopInsetButtonActionPerformed
        int changeby = 1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new Insets(changeby, 0, 0, 0));
    }//GEN-LAST:event_vplusTopInsetButtonActionPerformed

    private void vminusTopInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vminusTopInsetButtonActionPerformed
        int changeby = 1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new Insets(-changeby, 0, 0, 0));
    }//GEN-LAST:event_vminusTopInsetButtonActionPerformed

    private void hplusLeftInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hplusLeftInsetButtonActionPerformed
        int changeby = 1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new Insets(0, changeby, 0, 0));
    }//GEN-LAST:event_hplusLeftInsetButtonActionPerformed

    private void hminusLeftInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hminusLeftInsetButtonActionPerformed
        int changeby = 1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new Insets(0, -changeby, 0, 0));
    }//GEN-LAST:event_hminusLeftInsetButtonActionPerformed

    private void hminusRightInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hminusRightInsetButtonActionPerformed
        int changeby = 1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new Insets(0, 0, 0, -changeby));
    }//GEN-LAST:event_hminusRightInsetButtonActionPerformed

    private void hplusRightInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hplusRightInsetButtonActionPerformed
        int changeby = 1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new Insets(0, 0, 0, changeby));
    }//GEN-LAST:event_hplusRightInsetButtonActionPerformed

    private void vminusBottomInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vminusBottomInsetButtonActionPerformed
        int changeby = 1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new Insets(0, 0, -changeby, 0));
    }//GEN-LAST:event_vminusBottomInsetButtonActionPerformed

    private void vplusBottomInsetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vplusBottomInsetButtonActionPerformed
        int changeby = 1;
        if (( evt.getModifiers() & ActionEvent.CTRL_MASK ) != 0) changeby = ACCELERATED_SIZE_CHANGE;
        update(-1, -1, null, null, new Insets(0, 0, changeby, 0));
    }//GEN-LAST:event_vplusBottomInsetButtonActionPerformed

    private int currentAnchorSpecialization(int anchorbutton) {
        boolean baseline = baselineAnchorButton.isSelected();
        boolean bidi = bidiAnchorButton.isSelected();
        return convertAnchorType(baseline ? ANCHOR_BASELINE : (bidi ? ANCHOR_BIDI : ANCHOR_ABSOLUTE),anchorbutton);
    }
    
    private int convertAnchorType(int type, int currentanchor) {
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
    private javax.swing.JPanel anchorToolGroup;
    private javax.swing.JPanel anchorTypePanel;
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
    private javax.swing.Box.Filler crossCenter;
    private javax.swing.JPanel customizer;
    private javax.swing.JToggleButton eAnchorButton;
    private javax.swing.JLabel fillLabel;
    private javax.swing.JPanel fillPanel;
    private javax.swing.JSeparator fillSeparator;
    private javax.swing.JPanel fillToolGroup;
    private javax.swing.JLabel gridPositionLabel;
    private javax.swing.JPanel gridPositionPanel;
    private javax.swing.JSeparator gridPositionSeparator;
    private javax.swing.JPanel gridPositionToolGroup;
    private javax.swing.JLabel gridSizeLabel;
    private javax.swing.JPanel gridSizePanel;
    private javax.swing.JSeparator gridSizeSeparator;
    private javax.swing.JPanel gridSizeToolGroup;
    private javax.swing.JToggleButton hFillButton;
    private javax.swing.JLabel hInsetLabel;
    private javax.swing.JLabel hgridLabel;
    private javax.swing.JButton hgridMinusButton;
    private javax.swing.JButton hgridPlusButton;
    private javax.swing.JToggleButton hgridRelativeButton;
    private javax.swing.JToggleButton hgridRemainderButton;
    private javax.swing.JButton hminusInsetButton;
    private javax.swing.JButton hminusLeftInsetButton;
    private javax.swing.JButton hminusPaddingButton;
    private javax.swing.JButton hminusRightInsetButton;
    private javax.swing.JButton hminusWeightButton;
    private javax.swing.JLabel hpadLabel;
    private javax.swing.JButton hplusInsetButton;
    private javax.swing.JButton hplusLeftInsetButton;
    private javax.swing.JButton hplusPaddingButton;
    private javax.swing.JButton hplusRightInsetButton;
    private javax.swing.JButton hplusWeightButton;
    private javax.swing.JButton hweightEqualize;
    private javax.swing.JLabel hweightLabel;
    private javax.swing.JPanel insetsCross;
    private javax.swing.JLabel insetsLabel;
    private javax.swing.JPanel insetsPanel;
    private javax.swing.JSeparator insetsSeparator;
    private javax.swing.JPanel insetsToolGroup;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JToggleButton nAnchorButton;
    private javax.swing.JToggleButton neAnchorButton;
    private javax.swing.JToggleButton nwAnchorButton;
    private javax.swing.JLabel paddingLabel;
    private javax.swing.JPanel paddingPanel;
    private javax.swing.JSeparator paddingSeparator;
    private javax.swing.JPanel paddingToolGroup;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JToggleButton sAnchorButton;
    private javax.swing.JToggleButton seAnchorButton;
    private javax.swing.JToggleButton swAnchorButton;
    private javax.swing.JLabel topLeftCorner;
    private javax.swing.JLabel topRightCorner;
    private javax.swing.JToggleButton vFillButton;
    private javax.swing.JLabel vInsetLabel;
    private javax.swing.JLabel vgridLabel;
    private javax.swing.JButton vgridMinusButton;
    private javax.swing.JButton vgridPlusButton;
    private javax.swing.JToggleButton vgridRelativeButton;
    private javax.swing.JToggleButton vgridRemainderButton;
    private javax.swing.JButton vminusBottomInsetButton;
    private javax.swing.JButton vminusInsetButton;
    private javax.swing.JButton vminusPaddingButton;
    private javax.swing.JButton vminusTopInsetButton;
    private javax.swing.JButton vminusWeightButton;
    private javax.swing.JLabel vpadLabel;
    private javax.swing.JButton vplusBottomInsetButton;
    private javax.swing.JButton vplusInsetButton;
    private javax.swing.JButton vplusPaddingButton;
    private javax.swing.JButton vplusTopInsetButton;
    private javax.swing.JButton vplusWeightButton;
    private javax.swing.JButton vweightEqualize;
    private javax.swing.JLabel vweightLabel;
    private javax.swing.JToggleButton wAnchorButton;
    private javax.swing.JLabel weightsLabel;
    private javax.swing.JPanel weightsPanel;
    private javax.swing.JSeparator weightsSeparator;
    private javax.swing.JPanel weightsToolGroup;
    private javax.swing.JButton xgridMinusButton;
    private javax.swing.JButton xgridPlusButton;
    private javax.swing.JToggleButton xgridRelativeButton;
    private javax.swing.JButton ygridMinusButton;
    private javax.swing.JButton ygridPlusButton;
    private javax.swing.JToggleButton ygridRelativeButton;
    // End of variables declaration//GEN-END:variables

    public void selectAnchorButtons(int anchor)
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
            bidiAnchorButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/bidi.png", false)); // NOI18N
            bidiAnchorButton.setToolTipText(NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.anchor.bidiAware")); // NOI18N
            baselineAnchorButton.setSelected(false);
            baselineAnchorButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/baseline.png", false)); // NOI18N
            baselineAnchorButton.setToolTipText(NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.anchor.baselineRelated")); // NOI18N
            selectAnchorButtons(GridBagConstraints.NONE);
            hFillButton.setSelected(false);
            hFillButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/resize_h.png", false)); // NOI18N
            hFillButton.setToolTipText(NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.fill.horizontal")); // NOI18N
            vFillButton.setSelected(false);
            vFillButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/resize_v.png", false)); // NOI18N
            vFillButton.setToolTipText(NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.fill.vertical")); // NOI18N
        }
        bidiAnchorButton.setEnabled(enablebuttons);
        baselineAnchorButton.setEnabled(enablebuttons);

        nwAnchorButton.setEnabled(enablebuttons);
        nAnchorButton.setEnabled(enablebuttons);
        neAnchorButton.setEnabled(enablebuttons);
        wAnchorButton.setEnabled(enablebuttons);
        cAnchorButton.setEnabled(enablebuttons);
        eAnchorButton.setEnabled(enablebuttons);
        swAnchorButton.setEnabled(enablebuttons);
        sAnchorButton.setEnabled(enablebuttons);
        seAnchorButton.setEnabled(enablebuttons);

        hminusPaddingButton.setEnabled(enablebuttons);
        hplusPaddingButton.setEnabled(enablebuttons);
        vminusPaddingButton.setEnabled(enablebuttons);
        vplusPaddingButton.setEnabled(enablebuttons);
        bminusPaddingButton.setEnabled(enablebuttons);
        bplusPaddingButton.setEnabled(enablebuttons);

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

        hFillButton.setEnabled(enablebuttons);
        vFillButton.setEnabled(enablebuttons);

        if(enablebuttons) {
            GridBagInfoProvider info = manager.getGridInfo();

            /* set fill/bidi/base buttons as selected only if all selected components have the respective property set */
            int bidi = 0, baseline = 0, center = 0;
            boolean tlanchor = false, tcanchor = false, tranchor = false;
            boolean lanchor = false, canchor = false, ranchor = false;
            boolean blanchor = false, bcanchor = false, branchor = false;
            int hfill = 0, vfill = 0;
            for(Component component : components) {
                int fill = info.getFill(component);
                if(fill == GridBagConstraints.HORIZONTAL || fill == GridBagConstraints.BOTH) hfill++;
                if(fill == GridBagConstraints.VERTICAL || fill == GridBagConstraints.BOTH) vfill++;
                int anchor = info.getAnchor(component);
                switch(anchor) {
                    // baseline anchors
                    case GridBagConstraints.ABOVE_BASELINE_LEADING: tlanchor = true; baseline++; break;
                    case GridBagConstraints.ABOVE_BASELINE: tcanchor = true; baseline++; break;
                    case GridBagConstraints.ABOVE_BASELINE_TRAILING: tranchor = true; baseline++; break;
                    case GridBagConstraints.BASELINE_LEADING: lanchor = true; baseline++; break;
                    case GridBagConstraints.BASELINE: canchor = true; baseline++; break;
                    case GridBagConstraints.BASELINE_TRAILING: ranchor = true; baseline++; break;
                    case GridBagConstraints.BELOW_BASELINE_LEADING: blanchor = true; baseline++; break;
                    case GridBagConstraints.BELOW_BASELINE: bcanchor = true; baseline++; break;
                    case GridBagConstraints.BELOW_BASELINE_TRAILING: branchor = true; baseline++; break;
                    // bidirectional anchors
                    case GridBagConstraints.FIRST_LINE_START: tlanchor = true; bidi++; break;
                    case GridBagConstraints.PAGE_START: tcanchor = true; bidi++; break;
                    case GridBagConstraints.FIRST_LINE_END: tranchor = true; bidi++; break;
                    case GridBagConstraints.LINE_START: lanchor = true; bidi++; break;
                    case GridBagConstraints.LINE_END: ranchor = true; bidi++; break;
                    case GridBagConstraints.LAST_LINE_START: blanchor = true; bidi++; break;
                    case GridBagConstraints.PAGE_END: bcanchor = true; bidi++; break;
                    case GridBagConstraints.LAST_LINE_END: branchor = true; bidi++; break;
                    // absolute anchors
                    case GridBagConstraints.NORTHWEST: tlanchor = true; break;
                    case GridBagConstraints.NORTH: tcanchor = true; break;
                    case GridBagConstraints.NORTHEAST: tranchor = true; break;
                    case GridBagConstraints.WEST: lanchor = true; break;
                    case GridBagConstraints.CENTER: canchor = true; center++; break;
                    case GridBagConstraints.EAST: ranchor = true; break;
                    case GridBagConstraints.SOUTHWEST: blanchor = true; break;
                    case GridBagConstraints.SOUTH: bcanchor = true; break;
                    case GridBagConstraints.SOUTHEAST: branchor = true; break;
                }
            }
            if(center != components.size()) {
                if(baseline + bidi == 0) {
                    bidiCenter = false;
                } else {
                    bidiCenter = true;
                }
            }
            if(baseline + bidi > 0 && baseline + bidi + center != components.size()) {
                bidiAnchorButton.setSelected(false);
                bidiAnchorButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/bidi_warning.png", false)); // NOI18N
                bidiAnchorButton.setToolTipText(NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.anchor.bidiAwareWarning")); // NOI18N
            } else {
                bidiAnchorButton.setSelected(bidiCenter);
                bidiAnchorButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/bidi.png", false)); // NOI18N
                bidiAnchorButton.setToolTipText(NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.anchor.bidiAware")); // NOI18N
            }
            baselineAnchorButton.setSelected(baseline == components.size());
            if(baseline > 0 && baseline != components.size()) {
                baselineAnchorButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/baseline_warning.png", false)); // NOI18N
                baselineAnchorButton.setToolTipText(NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.anchor.baselineRelatedWarning")); // NOI18N
            } else {
                baselineAnchorButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/baseline.png", false)); // NOI18N
                baselineAnchorButton.setToolTipText(NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.anchor.baselineRelated")); // NOI18N
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
            hFillButton.setSelected(hfill == components.size());
            if(hfill > 0 && hfill != components.size()) {
                hFillButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/resize_h_warning.png", false)); // NOI18N
                hFillButton.setToolTipText(NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.fill.horizontalWarning")); // NOI18N
            } else {
                hFillButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/resize_h.png", false)); // NOI18N
                hFillButton.setToolTipText(NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.fill.horizontal")); // NOI18N
            }
            vFillButton.setSelected(vfill==components.size());
            if(vfill > 0 && vfill != components.size()) {
                vFillButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/resize_v_warning.png", false)); // NOI18N
                vFillButton.setToolTipText(NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.fill.verticalWarning")); // NOI18N
            } else {
                vFillButton.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/layoutsupport/griddesigner/resources/resize_v.png", false)); // NOI18N
                vFillButton.setToolTipText(NbBundle.getMessage(GridBagCustomizer.class, "GridBagCustomizer.fill.vertical")); // NOI18N
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
                GridBagManager gridBagManager = (GridBagManager) gridManager;
                GridBagInfoProvider info = gridBagManager.getGridInfo();
                int columns = info.getColumnCount();
                int rows = info.getRowCount();
                GridUtils.removePaddingComponents(gridManager);
                for(Component component : context.getSelectedComponents()) {
                    if (anchor != -1) {
                        gridBagManager.setAnchor(component, anchor);
                    }
                    if (anchortype != -1) {
                        int oldanchor = info.getAnchor(component);
                        int newanchor = convertAnchorType(anchortype,oldanchor);
                        if (newanchor != oldanchor) {
                            gridBagManager.setAnchor(component, newanchor);
                        }
                    }
                    if (fill != null) {
                        if(fill.hfill != -1) gridBagManager.setHorizontalFill(component, fill.hfill == 1);
                        if(fill.vfill != -1) gridBagManager.setVerticalFill(component, fill.vfill == 1);
                    }
                    if (ipad != null) {
                        if(ipad.xdiff != 0) gridBagManager.updateIPadX(component, ipad.xdiff);
                        if(ipad.ydiff != 0) gridBagManager.updateIPadY(component, ipad.ydiff);
                    }
                    if (insets != null) {
                        if(insets.top != 0 || insets.left != 0 || insets.bottom != 0 || insets.right != 0) {
                            gridBagManager.updateInsets(component, insets);
                        }
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
