/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import org.netbeans.modules.form.*;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

public class BindAction extends CookieAction {

    protected int mode() {
        return MODE_EXACTLY_ONE; // can be invoked on just one node
    }

    protected Class[] cookieClasses() {
        return new Class[] { RADComponentCookie.class };
    }

    public String getName() {
        return "Bind"; // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected void performAction(Node[] activatedNodes) {
    }

    @Override
    public JMenuItem getMenuPresenter() {
        return getPopupPresenter();
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu popupMenu = new JMenu(NbBundle.getMessage(BindAction.class, "ACT_Bind")); // NOI18N
        
        popupMenu.setEnabled(isEnabled());
        
        popupMenu.addMenuListener(new MenuListener() {
            public void menuSelected(MenuEvent e) {
                JMenu menu = (JMenu) e.getSource();
                createBindingsSubmenu(menu);
            }
            
            public void menuDeselected(MenuEvent e) {}
            
            public void menuCanceled(MenuEvent e) {}
        });
        return popupMenu;
    }

    private void createBindingsSubmenu(JMenu menu) {
        if (menu.getMenuComponentCount() > 0)
            menu.removeAll();

        Node[] nodes = getActivatedNodes();
        if (nodes.length != 1)
            return;

        RADComponentCookie radCookie = nodes[0].getCookie(RADComponentCookie.class);
        if (radCookie == null)
            return;

        BindingProperty[][] bindingProps = radCookie.getRADComponent().getBindingProperties();
        BindingProperty[] props = bindingProps[(bindingProps[0].length==0) ? 1 : 0];
        if (props.length > 0) {
            for (BindingProperty prop : props) {
                BindingMenuItem mi = new BindingMenuItem(prop);
                mi.addActionListener(mi);
                menu.add(mi);
            }
        } else {
            JMenuItem item = new JMenuItem(NbBundle.getMessage(BindAction.class, "MSG_NoBinding")); // NOI18N
            item.setEnabled(false);
            menu.add(item);
        }
    }

    private static class BindingMenuItem extends JMenuItem implements ActionListener {
        private BindingProperty bindingProperty;

        private BindingMenuItem(BindingProperty prop) {
            bindingProperty = prop;
            setText(prop.getDisplayName());
            updateFont();
        }

        private void updateFont() {
            java.awt.Font font = getFont();
            if (bindingProperty.getValue() != null) {
                setFont(font.deriveFont(font.getStyle() | java.awt.Font.BOLD));
            }
            else {
                setFont(font.deriveFont(font.getStyle() & ~java.awt.Font.BOLD));
            }
        }

        public void actionPerformed(ActionEvent ev) {
            MetaBinding binding = bindingProperty.getValue();
            final BindingCustomizer customizer = new BindingCustomizer(bindingProperty);
            customizer.setBinding(binding);
            customizer.getDialog(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    bindingProperty.setValue(customizer.getBinding());
                    updateFont();
                }
            }).setVisible(true);
        }
    }
}