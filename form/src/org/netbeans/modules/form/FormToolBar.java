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
        // Proper initialization of aqua toolbar ui, see commit dbd66075827a
        super("editorToolbar"); // NOI18N
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
        HelpCtx.setHelpIDString(selectionButton, "gui.about"); // NOI18N
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
        initButton(paletteButton);

        // status label
        addLabel = new JLabel();
        addLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));

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
        JToolBar.Separator separator3 = new JToolBar.Separator();
        separator3.setOrientation(JSeparator.VERTICAL);

        TestAction testAction = SystemAction.get(TestAction.class);
        JButton testButton = (JButton) testAction.getToolbarPresenter();
        testButton.addMouseListener(listener);
        initButton(testButton);

        add(Box.createHorizontalStrut(4));
        add(separator1);
        add(Box.createHorizontalStrut(6));
        add(selectionButton);
        add(connectionButton);
        add(paletteButton);
        add(Box.createHorizontalStrut(6));
        add(testButton);
        add(Box.createHorizontalStrut(4));
        add(separator2);
        add(Box.createHorizontalStrut(4));
        installDesignerActions();
        add(Box.createHorizontalStrut(4));
        add(separator3);
        add(Box.createHorizontalStrut(4));
        installResizabilityActions();

        // Add "addLabel" at the end of the toolbar
        add(Box.createHorizontalGlue());
        add(addLabel);

        if (!FormLoaderSettings.getInstance().isPaletteInToolBar()) {
            showPaletteButton(false);
        }
    }

    void installDesignerActions() {
        Collection actions = formDesigner.getDesignerActions(true);
        Iterator iter = actions.iterator();
        while (iter.hasNext()) {
            Action action = (Action)iter.next();
            JButton button = add(action);
            initButton(button);
        }
    }

    void installResizabilityActions() {
        Collection actions = formDesigner.getResizabilityActions();
        Iterator iter = actions.iterator();
        JToggleButton[] resButtons = new JToggleButton[2];
        for (int i=0; i < actions.size(); i++) {
            Action action = (Action)iter.next();
            JToggleButton button = new JToggleButton();
            button.setAction(action);
            initButton(button);
            resButtons[i] = button;
            add(button);
            add(Box.createHorizontalStrut(2));        
        }
        formDesigner.setResizabilityButtons(resButtons);
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
        // Hack that solves issue 147578
        if ("Nimbus".equals(UIManager.getLookAndFeel().getID())) { // NOI18N
            if (visible) {
                addLabel.setPreferredSize(null);
                paletteButton.setPreferredSize(null);
            } else {
                addLabel.setPreferredSize(new Dimension());
                paletteButton.setPreferredSize(new Dimension());
            }
        }
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
            @Override
            public void actionPerformed(ActionEvent e) {
                FormLoaderSettings.getInstance().setPaletteInToolBar(
                                                         item.isSelected());
            }
        });
        menu.add(item);
        menu.show(this, p.x, p.y);
    }
    
    @Override
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
        @Override
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
        @Override
        public boolean acceptNodes(Node[] nodes) {
            if (nodes.length == 0)
                return false;

            PaletteItem item = nodes[0].getCookie(PaletteItem.class);
            PaletteUtils.selectItem( item );
            return true;
        }

        /** Handles closing of PaletteMenuView popup */
        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            if( PaletteUtils.getSelectedItem() == null )
                formDesigner.toggleSelectionMode();
        }
        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        }
        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
        }
        
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getSource() == paletteButton) {
                showMenu = !paletteButton.isSelected();
            }
        }

        /** Reacts on right mouse button up - showing toolbar's popup menu. */
        @Override
        public void mouseReleased(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)
                  && formDesigner.getDesignerMode() == FormDesigner.MODE_SELECT)
                showVisibilityPopupMenu(e.getPoint());
        }
    }
}
