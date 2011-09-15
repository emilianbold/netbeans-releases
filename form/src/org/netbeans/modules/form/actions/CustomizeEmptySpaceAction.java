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

package org.netbeans.modules.form.actions;

import java.awt.*;
import javax.swing.*;
import java.util.ResourceBundle;

import org.openide.*;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;
import org.openide.nodes.*;

import org.netbeans.modules.form.*;
import org.netbeans.modules.form.layoutdesign.*;

/**
 * Customize empty space action.
 *
 * @author Jan Stola
 */
public class CustomizeEmptySpaceAction extends CookieAction {
    private static String name;
    private Dialog dialog;

    @Override
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    @Override
    protected Class[] cookieClasses() {
        return new Class[] { RADComponentCookie.class };
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    /**
     * Human presentable name of the action.
     *
     * @return the name of the action
     */
    @Override
    public String getName() {
        if (name == null)
            name = org.openide.util.NbBundle.getBundle(CustomizeEmptySpaceAction.class)
                .getString("ACT_CustomizeEmptySpace"); // NOI18N
        return name;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected String iconResource() {
        return "org/openide/resources/actions/empty.gif"; // NOI18N
    }

    /**
     * Standard perform action extended by actually activated nodes.
     *
     * @param activatedNodes gives array of actually activated nodes.
     */
    @Override
    protected void performAction(Node[] activatedNodes) {
        java.util.List comps = FormUtils.getSelectedLayoutComponents(activatedNodes);
        if ((comps == null) || (comps.size() != 1)) return;
        RADComponent metacomp = (RADComponent)comps.get(0);
        FormModel formModel = metacomp.getFormModel();
        LayoutModel model = formModel.getLayoutModel();
        final EmptySpaceCustomizer customizer = new EmptySpaceCustomizer(model, metacomp.getId());
        DialogDescriptor dd = new DialogDescriptor(
            customizer,
            NbBundle.getMessage(CustomizeEmptySpaceAction.class, "TITLE_CustomizeEmptySpace"), // NOI18N
            true,
            NotifyDescriptor.OK_CANCEL_OPTION,
            NotifyDescriptor.OK_OPTION,
            DialogDescriptor.DEFAULT_ALIGN,
            HelpCtx.DEFAULT_HELP,
            new java.awt.event.ActionListener() {
            @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    if (evt.getSource() == NotifyDescriptor.OK_OPTION) {
                        if (customizer.checkValues()) {
                            dialog.dispose();
                        }
                    }
                }
            });
        dd.setClosingOptions(new Object[] {NotifyDescriptor.CANCEL_OPTION});
        dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizeEmptySpaceAction.class, "ACSD_EmptySpace")); // NOI18N
        dialog.setVisible(true);
        dialog = null;
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            Object layoutUndoMark = model.getChangeMark();
            javax.swing.undo.UndoableEdit ue = model.getUndoableEdit();
            boolean autoUndo = true;
            try {
                customizer.applyValues();
                autoUndo = false;
            } finally {
                formModel.fireContainerLayoutChanged(((RADVisualComponent)metacomp).getParentContainer(), null, null, null);
                if (!layoutUndoMark.equals(model.getChangeMark())) {
                    formModel.addUndoableEdit(ue);
                }
                if (autoUndo) {
                    formModel.forceUndoOfCompoundEdit();
                }
            }
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (super.enable(activatedNodes)) {
            java.util.List comps = FormUtils.getSelectedLayoutComponents(activatedNodes);
            return ((comps != null) && (comps.size() == 1));
        }
        return false;
    }
    
private static class EmptySpaceCustomizer extends JPanel {
    JComboBox leftSize = new JComboBox();
    JComboBox rightSize = new JComboBox();
    JComboBox topSize = new JComboBox();
    JComboBox bottomSize = new JComboBox();
    JCheckBox leftResizable = new JCheckBox();
    JCheckBox rightResizable = new JCheckBox();
    JCheckBox topResizable = new JCheckBox();
    JCheckBox bottomResizable = new JCheckBox();
    LayoutModel model;
    String compId;

    EmptySpaceCustomizer(LayoutModel model, String compId) {
        this.model = model;
        this.compId = compId;
        initComponents();
        LayoutComponent comp = model.getLayoutComponent(compId);
        initValues(comp, LayoutConstants.HORIZONTAL, LayoutConstants.LEADING, leftSize, leftResizable);
        initValues(comp, LayoutConstants.HORIZONTAL, LayoutConstants.TRAILING, rightSize, rightResizable);
        initValues(comp, LayoutConstants.VERTICAL, LayoutConstants.LEADING, topSize, topResizable);
        initValues(comp, LayoutConstants.VERTICAL, LayoutConstants.TRAILING, bottomSize, bottomResizable);
    }

    private void initValues(LayoutComponent comp, int dimension, int direction, JComboBox size, JCheckBox resizable) {
        LayoutInterval space = LayoutUtils.getAdjacentEmptySpace(comp, dimension, direction);
        if (space != null) {
            String[] paddings;
            ResourceBundle bundle = NbBundle.getBundle(EmptySpaceCustomizer.class);
            if (LayoutUtils.hasAdjacentComponent(comp, dimension, direction)) {
                // there are three types of default gaps between components
                paddings = new String[] {
                        bundle.getString("VALUE_PaddingRelated"), // NOI18N
                        bundle.getString("VALUE_PaddingUnrelated"), // NOI18N
                        bundle.getString("VALUE_PaddingSeparate") }; // NOI18N
            } else { // just one type of default gap
                paddings = new String[] { bundle.getString("VALUE_PaddingDefault") }; // NOI18N
            }
            size.setModel(new DefaultComboBoxModel(paddings));
            int pref = space.getPreferredSize();
            int max = space.getMaximumSize();
            if (pref == LayoutConstants.NOT_EXPLICITLY_DEFINED) {
                size.setSelectedItem(getPaddingString(paddings, space.getPaddingType()));
            } else {
                size.setSelectedItem(Integer.toString(pref));
            }
            resizable.setSelected((max != LayoutConstants.USE_PREFERRED_SIZE) && (max != pref));
        } else {
            size.setSelectedItem(NbBundle.getMessage(CustomizeEmptySpaceAction.class, "VALUE_NoEmptySpace"));
            size.setEnabled(false);
            resizable.setEnabled(false);
        }
    }

    // converts PaddingType to String
    private static String getPaddingString(String[] paddingStrings, LayoutConstants.PaddingType paddingType) {
        if (paddingType == LayoutConstants.PaddingType.UNRELATED) {
            return paddingStrings[1];
        } else if (paddingType == LayoutConstants.PaddingType.SEPARATE) {
            return paddingStrings[2];
        } else {
            return paddingStrings[0];
        }
    }

    private static LayoutConstants.PaddingType getSelectedPaddingType(JComboBox combo) {
        if (combo.getItemCount() == 3) { // configuring a gap between components
            Object selSize = combo.getSelectedItem();
            if (selSize != null) {
                if (selSize.equals(combo.getItemAt(0))) {
                    return LayoutConstants.PaddingType.RELATED;
                } else if (selSize.equals(combo.getItemAt(1))) {
                    return LayoutConstants.PaddingType.UNRELATED;
                } else if (selSize.equals(combo.getItemAt(2))) {
                    return LayoutConstants.PaddingType.SEPARATE;
                }
            }
        }
        return null;
    }

    private static boolean isDefaultSizeSelected(JComboBox combo) {
        Object selSize = combo.getSelectedItem();
        if (selSize != null) {
            for (int i=0; i < combo.getItemCount(); i++) {
                if (selSize.equals(combo.getItemAt(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean checkValues() {
        return checkValue(leftSize) && checkValue(rightSize) && checkValue(topSize) && checkValue(bottomSize);
    }

    private boolean checkValue(JComboBox size) {
        Object selSize = size.getSelectedItem();
        if (size.isEnabled() && !isDefaultSizeSelected(size)) {
            try {
                int newPref = Integer.parseInt((String)selSize);
                if (newPref < 0) {
                    // Negative
                    notify("MSG_NegativeSpaceSize"); // NOI18N
                    return false;
                }
                if (newPref > Short.MAX_VALUE) {
                    // Too large
                    notify("MSG_TooLargeSpaceSize"); // NOI18N
                    return false;
                }
            } catch (NumberFormatException nfex) {
                // Not a nubmer
                notify("MSG_CorruptedSpaceSize"); // NOI18N
                return false;
            }            
        }
        return true;
    }

    private void notify(String messageKey) {
        NotifyDescriptor descriptor = new NotifyDescriptor.Message(
            NbBundle.getBundle(CustomizeEmptySpaceAction.class).getString(messageKey));
        DialogDisplayer.getDefault().notify(descriptor);
    }

    void applyValues() {
        LayoutComponent comp = model.getLayoutComponent(compId);
        applyValues(comp, LayoutConstants.HORIZONTAL, LayoutConstants.LEADING, leftSize, leftResizable);
        applyValues(comp, LayoutConstants.HORIZONTAL, LayoutConstants.TRAILING, rightSize, rightResizable);
        applyValues(comp, LayoutConstants.VERTICAL, LayoutConstants.LEADING, topSize, topResizable);
        applyValues(comp, LayoutConstants.VERTICAL, LayoutConstants.TRAILING, bottomSize, bottomResizable);
    }

    private void applyValues(LayoutComponent comp, int dimension, int direction, JComboBox size, JCheckBox resizable) {
        LayoutInterval space = LayoutUtils.getAdjacentEmptySpace(comp, dimension, direction);
        if (space != null) {
            int pref = space.getPreferredSize();
            boolean newResizable = resizable.isSelected();
            Object selSize = size.getSelectedItem();
            int newPref;
            LayoutConstants.PaddingType oldPadType = space.getPaddingType();
            LayoutConstants.PaddingType newPadType;
            if (isDefaultSizeSelected(size)) {
                newPref = LayoutConstants.NOT_EXPLICITLY_DEFINED;
                newPadType = getSelectedPaddingType(size);
            } else {
                try {
                    newPref = Integer.parseInt((String)selSize);
                    if (newPref < 0) {
                        newPref = pref;
                    }
                } catch (NumberFormatException nfex) {
                    newPref = pref; // Use old value instead
                }
                newPadType = null;
            }
            model.setUserIntervalSize(space, dimension, newPref, newResizable);
            if (oldPadType != null || newPadType != LayoutConstants.PaddingType.RELATED) {
                model.setPaddingType(space, newPadType);
            } // need not change null to RELATED
        }
    }

    private void initComponents() {
        ResourceBundle bundle = NbBundle.getBundle(EmptySpaceCustomizer.class);
        setLayout(new GridBagLayout());
        setBorder(new javax.swing.border.TitledBorder(bundle.getString("TITLE_EmptySpace"))); // NOI18N
        JLabel leftLabel = new JLabel(); 
        JLabel rightLabel = new JLabel();
        JLabel topLabel = new JLabel();
        JLabel bottomLabel = new JLabel();
        JLabel sizeLabel = new JLabel(bundle.getString("NAME_SpaceSize")); // NOI18N
        JLabel resizableLabel = new JLabel(bundle.getString("NAME_SpaceResizable")); // NOI18N

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(3, 6, 3, 0);
        add(leftLabel, gridBagConstraints);

        gridBagConstraints.gridy = 2;
        add(rightLabel, gridBagConstraints);

        gridBagConstraints.gridy = 3;
        add(topLabel, gridBagConstraints);

        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new Insets(3, 6, 6, 0);
        add(bottomLabel, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.insets = new Insets(0, 6, 3, 6);
        add(sizeLabel, gridBagConstraints);

        gridBagConstraints.gridx = 2;
        gridBagConstraints.insets = new Insets(0, 6, 3, 6);
        add(resizableLabel, gridBagConstraints);

        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        add(leftResizable, gridBagConstraints);

        gridBagConstraints.gridy = 2;
        add(rightResizable, gridBagConstraints);

        gridBagConstraints.gridy = 3;
        add(topResizable, gridBagConstraints);

        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new Insets(0, 0, 3, 0);
        add(bottomResizable, gridBagConstraints);

        leftLabel.setLabelFor(leftSize);
        rightLabel.setLabelFor(rightSize);
        topLabel.setLabelFor(topSize);
        bottomLabel.setLabelFor(bottomSize);

        Mnemonics.setLocalizedText(leftLabel, bundle.getString("NAME_LeftSpace")); // NOI18N
        Mnemonics.setLocalizedText(rightLabel, bundle.getString("NAME_RightSpace")); // NOI18N
        Mnemonics.setLocalizedText(topLabel, bundle.getString("NAME_TopSpace")); // NOI18N
        Mnemonics.setLocalizedText(bottomLabel, bundle.getString("NAME_BottomSpace")); // NOI18N

        leftSize.setEditable(true);
        rightSize.setEditable(true);
        topSize.setEditable(true);
        bottomSize.setEditable(true);

        leftResizable.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_LeftResizable")); // NOI18N
        rightResizable.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_RightResizable")); // NOI18N
        topResizable.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_TopResizable")); // NOI18N
        bottomResizable.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_BottomResizable")); // NOI18N

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(3, 6, 3, 6);
        add(leftSize, gridBagConstraints);

        gridBagConstraints.gridy = 2;
        add(rightSize, gridBagConstraints);
        
        gridBagConstraints.gridy = 3;
        add(topSize, gridBagConstraints);

        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new Insets(3, 6, 6, 6);
        add(bottomSize, gridBagConstraints);
    }

}
}
