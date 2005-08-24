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
import java.util.*;

import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.form.palette.*;
import org.netbeans.modules.form.actions.TestAction;
import org.netbeans.modules.form.actions.InstallBeanAction;

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

    // ctor
    public FormToolBar(FormDesigner designer) {
        formDesigner = designer;

        // the toolbar should have roll-over buttons and no handle for dragging
        setFloatable(false);
        setRollover(true);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        listener = new Listener();

        // selection button
        selectionButton = new JToggleButton(
            new ImageIcon(getClass().getResource(
                          "/org/netbeans/modules/form/resources/selection_mode.png")), // NOI18N
            false);
        selectionButton.addActionListener(listener);
        selectionButton.addMouseListener(listener);
        selectionButton.setToolTipText(
            FormUtils.getBundleString("CTL_SelectionButtonHint")); // NOI18N
        HelpCtx.setHelpIDString(selectionButton, "gui.components.palette"); // NOI18N
        selectionButton.setSelected(true);
        initButton(selectionButton);

        // connection button
        connectionButton = new JToggleButton(
            new ImageIcon(getClass().getResource(
                          "/org/netbeans/modules/form/resources/connection_mode.png")), // NOI18N
            false);
        connectionButton.addActionListener(listener);
        connectionButton.addMouseListener(listener);
        connectionButton.setToolTipText(
            FormUtils.getBundleString("CTL_ConnectionButtonHint")); // NOI18N
        HelpCtx.setHelpIDString(connectionButton, "gui.connecting.intro"); // NOI18N
        initButton(connectionButton);

        // palette button
        paletteButton = new JToggleButton(
            new ImageIcon(getClass().getResource(
                          "/org/netbeans/modules/form/resources/beansButton.gif")), // NOI18N
            false);
        paletteButton.addActionListener(listener);
        paletteButton.addMouseListener(listener);
        paletteButton.setToolTipText(
            FormUtils.getBundleString("CTL_BeansButtonHint")); // NOI18N
        HelpCtx.setHelpIDString(paletteButton, "gui.components.adding"); // NOI18N
        // Issue 46673
        ScrollPopupMenu.doNotCancelPopupHack(paletteButton);
        initButton(paletteButton);

        // status label
        addLabel = new JLabel();

        // popup menu
        addMouseListener(listener);

        // a11y
        connectionButton.getAccessibleContext().setAccessibleName(connectionButton.getToolTipText());
        selectionButton.getAccessibleContext().setAccessibleName(selectionButton.getToolTipText());
        paletteButton.getAccessibleContext().setAccessibleName(paletteButton.getToolTipText());
        connectionButton.getAccessibleContext().setAccessibleDescription(FormUtils.getBundleString("ACSD_ConnectionMode")); // NOI18N
        selectionButton.getAccessibleContext().setAccessibleDescription(FormUtils.getBundleString("ACSD_SelectionMode")); // NOI18N
        paletteButton.getAccessibleContext().setAccessibleDescription(FormUtils.getBundleString("ACSD_AddMode")); // NOI18N

        // adding the components to the toolbar
        JToolBar.Separator separator1 = new JToolBar.Separator();
        separator1.setOrientation(JSeparator.VERTICAL);
        JToolBar.Separator separator2 = new JToolBar.Separator();
        separator2.setOrientation(JSeparator.VERTICAL);

        TestAction testAction = (TestAction) SystemAction.get(TestAction.class);
        JButton testButton = (JButton) testAction.getToolbarPresenter();
        testButton.addMouseListener(listener);
        initButton(testButton);

        InstallBeanAction paletteManagerAction = (InstallBeanAction)
                                      SystemAction.get(InstallBeanAction.class);
        // Issue 46562
        JButton pmButton = add(paletteManagerAction);
        pmButton.addMouseListener(listener);
        String pmToolTip = paletteManagerAction.getName();
        pmToolTip = org.openide.awt.Actions.cutAmpersand(pmToolTip);
        pmButton.setToolTipText(pmToolTip);
        initButton(pmButton);
        Icon icon = (Icon)paletteManagerAction.getValue("hidden_icon"); // NOI18N
        if (icon == null) {
             Image i = Utilities.loadImage("org/netbeans/modules/form/resources/palette_manager.png", true); // NOI18N
             icon = new ImageIcon(i);
             paletteManagerAction.putValue("hidden_icon", icon); // NOI18N
        }
        pmButton.setIcon(icon);

        add(Box.createHorizontalStrut(4));
        add(separator1);
        add(Box.createHorizontalStrut(6));
        add(selectionButton);
        add(connectionButton);
        add(paletteButton);
        add(Box.createHorizontalStrut(6));
        add(pmButton);
        add(Box.createHorizontalStrut(6));
        add(testButton);
        add(Box.createHorizontalStrut(4));
        add(separator2);
        add(Box.createHorizontalStrut(4));

        installDesignerActions();

        // Add "addLabel" at the end of the toolbar
        add(Box.createHorizontalGlue());
        add(addLabel);

        if (!FormLoaderSettings.getInstance().isPaletteInToolBar()) {
            addLabel.setVisible(false);
            paletteButton.setVisible(false);
        }
    }

    void installDesignerActions() {
        Collection actions = formDesigner.getDesignerActions();
        Iterator iter = actions.iterator();
        while (iter.hasNext()) {
            Action action = (Action)iter.next();
            JButton button = add(action);
            initButton(button);
        }        
    }

    // --------
    
    private void initButton(AbstractButton button) {
        if (!("Windows".equals(UIManager.getLookAndFeel().getID()) // NOI18N
            && (button instanceof JToggleButton))) {
            button.setBorderPainted(false);
        }
        button.setOpaque(false);
        button.setFocusPainted(false);
        button.setMargin(new Insets(0, 0, 0, 0));
    }
    
    void updateDesignerMode(int mode) {
        selectionButton.setSelected(mode == FormDesigner.MODE_SELECT);
        connectionButton.setSelected(mode == FormDesigner.MODE_CONNECT);
        paletteButton.setSelected(mode == FormDesigner.MODE_ADD);

        if (addLabel.isVisible()) {
            PaletteItem item = PaletteUtils.getSelectedItem();
            if (item != null && mode == FormDesigner.MODE_ADD) {
                addLabel.setIcon(
                    new ImageIcon(item.getNode().getIcon(BeanInfo.ICON_COLOR_16x16)));
                addLabel.setText(item.getNode().getDisplayName());
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
            paletteMenuView = new PaletteMenuView(listener);
            paletteMenuView.getPopupMenu().addPopupMenuListener(listener);
        }

        Point p = paletteButton.getLocation();
        p.y += paletteButton.getHeight() + 2;

        paletteMenuView.getPopupMenu().show(this, p.x, p.y);
    }

    private void showVisibilityPopupMenu(Point p) {
        JPopupMenu menu = new JPopupMenu();
        final JMenuItem item = new JCheckBoxMenuItem(
                FormUtils.getBundleString("CTL_PaletteButton_MenuItem")); // NOI18N
        item.setSelected(FormLoaderSettings.getInstance().isPaletteInToolBar());
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FormLoaderSettings.getInstance().setPaletteInToolBar(
                                                         item.isSelected());
            }
        });
        menu.add(item);
        menu.show(this, p.x, p.y);
    }
    
    public String getUIClassID() {
        // For GTK and Aqua look and feels, we provide a custom toolbar UI
        if (UIManager.get("Nb.Toolbar.ui") != null) { // NOI18N
            return "Nb.Toolbar.ui"; // NOI18N
        } else {
            return super.getUIClassID();
        }
    }

    // -------

    private class Listener extends MouseAdapter
                           implements ActionListener, NodeAcceptor,
                                      PopupMenuListener
    {
        // Determines whether palette popup menu should be shown (see issue 46673)
        private boolean showMenu;
        
        /** Action to switch to selection, connection or add mode. */
        public void actionPerformed(ActionEvent ev) {
            if (ev.getSource() == selectionButton)
                formDesigner.toggleSelectionMode();
            else if (ev.getSource() == connectionButton)
                formDesigner.toggleConnectionMode();
            else if (ev.getSource() == paletteButton) {
                if (showMenu) {
                    formDesigner.toggleAddMode();
                    showPaletteViewMenu();
                } else {
                    MenuSelectionManager.defaultManager().clearSelectedPath();
                    formDesigner.toggleSelectionMode();
                }
            }
        }

        /** Acceptor for nodes in PaletteMenuView */
        public boolean acceptNodes(Node[] nodes) {
            if (nodes.length == 0)
                return false;

            PaletteItem item = (PaletteItem) nodes[0].getCookie(PaletteItem.class);
            PaletteUtils.selectItem( item );
            return true;
        }

        /** Handles closing of PaletteMenuView popup */
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            if( PaletteUtils.getSelectedItem() == null )
                formDesigner.toggleSelectionMode();
        }
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        }
        public void popupMenuCanceled(PopupMenuEvent e) {
        }
        
        public void mousePressed(MouseEvent e) {
            if (e.getSource() == paletteButton) {
                showMenu = !paletteButton.isSelected();
            }
        }

        /** Reacts on right mouse button up - showing toolbar's popup menu. */
        public void mouseReleased(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)
                  && formDesigner.getDesignerMode() == FormDesigner.MODE_SELECT)
                showVisibilityPopupMenu(e.getPoint());
        }
    }
}
