/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.BeanInfo;

import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.form.palette.*;
import org.netbeans.modules.form.actions.TestAction;

/**
 * ToolBar in the FormDesigner - by default it holds buttons for selection and
 * connection mode and for testing the form. May contain other buttons for
 * some form editor actions.
 *
 * @author Tomas Pavek
 */

class FormToolBar extends JToolBar {

    private FormDesigner formDesigner;
    
    private JToggleButton selectionButton;
    private JToggleButton connectionButton;
    private JToggleButton paletteButton;
    private JLabel addLabel;

    private PaletteMenuView paletteMenuView;

    private Listener listener;

    public FormToolBar(FormDesigner designer) {
        formDesigner = designer;

        // the toolbar should have roll-over buttons and no handle for dragging
        setFloatable(false);
        setRollover(true);
        Border b = UIManager.getBorder("ToolBar.border"); // NOI18N
        setBorder(new CompoundBorder(b, new EmptyBorder(2, 2, 2, 2)));

        listener = new Listener();

        // selection button
        selectionButton = new JToggleButton(
            new ImageIcon(getClass().getResource(
                          "/org/netbeans/modules/form/resources/selectionMode.gif")), // NOI18N
            false);
        selectionButton.addActionListener(listener);
        selectionButton.setToolTipText(
            FormUtils.getBundleString("CTL_SelectionButtonHint")); // NOI18N
        HelpCtx.setHelpIDString(selectionButton, "gui.components.palette"); // NOI18N
        selectionButton.setSelected(true);

        // connection button
        connectionButton = new JToggleButton(
            new ImageIcon(getClass().getResource(
                          "/org/netbeans/modules/form/resources/connectionMode.gif")), // NOI18N
            false);
        connectionButton.addActionListener(listener);
        connectionButton.setToolTipText(
            FormUtils.getBundleString("CTL_ConnectionButtonHint")); // NOI18N
        HelpCtx.setHelpIDString(connectionButton, "gui.connecting.intro"); // NOI18N

        // palette button
        paletteButton = new JToggleButton(
            new ImageIcon(getClass().getResource(
                          "/org/netbeans/modules/form/resources/beansButton.gif")), // NOI18N
            false);
        paletteButton.addActionListener(listener);
        paletteButton.setToolTipText(
            FormUtils.getBundleString("CTL_BeansButtonHint")); // NOI18N
        HelpCtx.setHelpIDString(paletteButton, "gui.components.adding"); // NOI18N

        // status label
        addLabel = new JLabel();

        // a11y
        connectionButton.getAccessibleContext().setAccessibleName(connectionButton.getToolTipText());
        selectionButton.getAccessibleContext().setAccessibleName(selectionButton.getToolTipText());
        paletteButton.getAccessibleContext().setAccessibleName(paletteButton.getToolTipText());
        connectionButton.getAccessibleContext().setAccessibleDescription(FormUtils.getBundleString("ACSD_ConnectionMode")); // NOI18N
        selectionButton.getAccessibleContext().setAccessibleDescription(FormUtils.getBundleString("ACSD_SelectionMode")); // NOI18N
        paletteButton.getAccessibleContext().setAccessibleDescription(FormUtils.getBundleString("ACSD_AddMode")); // NOI18N

        // adding the components to the toolbar
        JToolBar.Separator separator = new JToolBar.Separator();
        separator.setOrientation(JSeparator.VERTICAL);

        TestAction testAction = (TestAction) SystemAction.get(TestAction.class);
        JButton testButton = (JButton) testAction.getToolbarPresenter();

        add(selectionButton);
        add(connectionButton);
        add(paletteButton);
        add(Box.createHorizontalStrut(6));
        add(separator);
        add(Box.createHorizontalStrut(6));
        add(testButton);
        add(Box.createHorizontalGlue());
        add(addLabel);

        if (!FormLoaderSettings.getInstance().isPaletteInToolBar()) {
            addLabel.setVisible(false);
            paletteButton.setVisible(false);
        }
    }

    void updateDesignerMode(int mode) {
        selectionButton.setSelected(mode == FormDesigner.MODE_SELECT);
        connectionButton.setSelected(mode == FormDesigner.MODE_CONNECT);
        paletteButton.setSelected(mode == FormDesigner.MODE_ADD);

        if (addLabel.isVisible()) {
            PaletteItem item = CPManager.getDefault().getSelectedItem();
            if (item != null && mode == FormDesigner.MODE_ADD) {
                addLabel.setIcon(new ImageIcon(item.getItemNode().getIcon(BeanInfo.ICON_COLOR_16x16)));
                addLabel.setText(item.getName());
            }
            else {
                addLabel.setText(""); // NOI18N
                addLabel.setIcon(null);
            }
        }
    }

    void showPaletteButton(boolean visible) {
        addLabel.setVisible(visible);
        paletteButton.setVisible(visible);
    }

    private void showPaletteViewMenu() {
        if (paletteMenuView == null) {
            paletteMenuView = new PaletteMenuView(PaletteNode.getPaletteNode(), listener);
            paletteMenuView.getPopupMenu().addPopupMenuListener(listener);
        }

        Point p = paletteButton.getLocation();
        p.y += paletteButton.getHeight() + 2;

        paletteMenuView.getPopupMenu().show(this, p.x, p.y);
    }

    // -------

    private class Listener implements ActionListener, NodeAcceptor, PopupMenuListener {
        public void actionPerformed(ActionEvent ev) {
            if (ev.getSource() == selectionButton)
                formDesigner.toggleSelectionMode();
            else if (ev.getSource() == connectionButton)
                formDesigner.toggleConnectionMode();
            else if (ev.getSource() == paletteButton) {
                formDesigner.toggleAddMode();
                showPaletteViewMenu();
            }
        }

        /** Acceptor for nodes in PaletteMenuView */
        public boolean acceptNodes(Node[] nodes) {
            if (nodes.length == 0)
                return false;

            PaletteItem item = CPManager.createPaletteItem(nodes[0]);
            CPManager.getDefault().setSelectedItem(item);
            return true;
        }

        /** Handles closing of PaletteMenuView popup */
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            if (CPManager.getDefault().getSelectedItem() == null)
                formDesigner.toggleSelectionMode();
        }
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        }
        public void popupMenuCanceled(PopupMenuEvent e) {
        }
    }
}
