/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.actions;

import java.awt.*;
import javax.swing.*;
import java.util.ResourceBundle;

import org.openide.*;
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

    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    protected Class[] cookieClasses() {
        return new Class[] { RADComponentCookie.class };
    }

    protected boolean asynchronous() {
        return false;
    }

    /**
     * Human presentable name of the action.
     *
     * @return the name of the action
     */
    public String getName() {
        if (name == null)
            name = org.openide.util.NbBundle.getBundle(CustomizeEmptySpaceAction.class)
                .getString("ACT_CustomizeEmptySpace"); // NOI18N
        return name;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected String iconResource() {
        return "org/openide/resources/actions/empty.gif"; // NOI18N
    }

    /**
     * Standard perform action extended by actually activated nodes.
     *
     * @param activatedNodes gives array of actually activated nodes.
     */
    protected void performAction(Node[] activatedNodes) {
        RADComponentCookie radCookie = (RADComponentCookie)
            activatedNodes[0].getCookie(RADComponentCookie.class);
        if (radCookie != null) {
            RADComponent metacomp = radCookie.getRADComponent();
            if (metacomp instanceof RADVisualComponent) {
                FormModel formModel = metacomp.getFormModel();
                LayoutModel model = formModel.getLayoutModel();
                EmptySpaceCustomizer customizer = new EmptySpaceCustomizer(model, metacomp.getId());
                DialogDescriptor dd = new DialogDescriptor(customizer,
                    NbBundle.getMessage(CustomizeEmptySpaceAction.class, "TITLE_CustomizeEmptySpace")); // NOI18N
                Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
                dialog.show();
                if (dd.getValue() == DialogDescriptor.OK_OPTION) {
                    Object layoutUndoMark = model.getChangeMark();
                    javax.swing.undo.UndoableEdit ue = model.getUndoableEdit();
                    customizer.applyValues();
                    formModel.fireContainerLayoutChanged(((RADVisualComponent)metacomp).getParentContainer(), null, null, null);
                    if (!layoutUndoMark.equals(model.getChangeMark())) {
                        formModel.addUndoableEdit(ue);
                    }
                }
            }
        }
    }

    protected boolean enable(Node[] activatedNodes) {
        if (super.enable(activatedNodes)) {
            RADComponentCookie radCookie = (RADComponentCookie)
                activatedNodes[0].getCookie(RADComponentCookie.class);
            if (radCookie != null) {
                RADComponent metacomp = radCookie.getRADComponent();
                if (metacomp instanceof RADVisualComponent) {
                    RADVisualContainer metacont = ((RADVisualComponent)metacomp).getParentContainer();
                    return (metacont != null) && (metacont.getLayoutSupport() == null);
                }
            }
        }
        return false;
    }
    
}

class EmptySpaceCustomizer extends JPanel {
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
    String padding;

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
            int pref = space.getPreferredSize(false);
            int max = space.getMaximumSize(false);
            size.setSelectedItem((pref == LayoutConstants.NOT_EXPLICITLY_DEFINED) ? padding : ("" + pref));
            resizable.setSelected((max != LayoutConstants.USE_PREFERRED_SIZE) && (max != pref));
        } else {
            size.setSelectedItem(NbBundle.getMessage(CustomizeEmptySpaceAction.class, "VALUE_NoEmptySpace"));
            size.setEnabled(false);
            resizable.setEnabled(false);
        }
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
            int pref = space.getPreferredSize(false);
            int max = space.getMaximumSize(false);
            boolean oldResizable = (max != LayoutConstants.USE_PREFERRED_SIZE) && (max != pref);
            boolean newResizable = resizable.isSelected();
            Object selSize = size.getSelectedItem();
            int newPref;
            if (selSize.equals(padding)){
                newPref = LayoutConstants.NOT_EXPLICITLY_DEFINED;
            } else {
                try {
                    newPref = Integer.parseInt((String)selSize);
                } catch (NumberFormatException nfex) {
                    newPref = pref; // Use old value instead
                }
            }
            if (pref != newPref) {
                model.setIntervalSize(space, space.getMinimumSize(false), newPref, max);
            }
            if (oldResizable != newResizable) {
                model.setIntervalSize(space,
                    newResizable ? LayoutConstants.NOT_EXPLICITLY_DEFINED : LayoutConstants.USE_PREFERRED_SIZE,
                    newPref,
                    newResizable ? Short.MAX_VALUE : LayoutConstants.USE_PREFERRED_SIZE);
            }
        }
    }

    private void initComponents() {
        ResourceBundle bundle = NbBundle.getBundle(EmptySpaceCustomizer.class);
        setLayout(new GridBagLayout());
        setBorder(new javax.swing.border.TitledBorder(bundle.getString("TITLE_EmptySpace"))); // NOI18N
        JLabel leftLabel = new JLabel(bundle.getString("NAME_LeftSpace")); // NOI18N
        JLabel rightLabel = new JLabel(bundle.getString("NAME_RightSpace")); // NOI18N
        JLabel topLabel = new JLabel(bundle.getString("NAME_TopSpace")); // NOI18N
        JLabel bottomLabel = new JLabel(bundle.getString("NAME_BottomSpace")); // NOI18N
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

        leftSize.setEditable(true);
        rightSize.setEditable(true);
        topSize.setEditable(true);
        bottomSize.setEditable(true);
        
        padding = bundle.getString("VALUE_DefaultPadding"); // NOI18N
        leftSize.setModel(new DefaultComboBoxModel(new String[] {padding}));
        rightSize.setModel(new DefaultComboBoxModel(new String[] {padding}));
        topSize.setModel(new DefaultComboBoxModel(new String[] {padding}));
        bottomSize.setModel(new DefaultComboBoxModel(new String[] {padding}));

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