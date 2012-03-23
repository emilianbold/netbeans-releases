/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.search;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.openide.util.ImageUtilities;

public class SearchExpandMenu {
    private static final Insets BUTTON_INSETS = new Insets(2, 1, 0, 1);
    /**
     * contains everything that is in Search bar and is possible to move to
     * expand popup
     */
    private final List<JComponent> inBar = new ArrayList<JComponent>();
    /**
     * components moved to popup
     */
    private final LinkedList<JComponent> inPopup = new LinkedList<JComponent>();
    /**
     * defines index of all components in Search bar
     */
    private final List<Component> barOrder = new ArrayList<Component>();
    private boolean isPopupShown = false;
    private final JButton expandButton;
    private final JPopupMenu expandPopup;
    private final JPanel padding;
    private int popupHeight;

    public SearchExpandMenu(int popupHeight) {
        this.popupHeight = popupHeight;
        expandButton = createExpandButton();
        expandPopup = createExpandPopup(expandButton);
        expandButton.setVisible(true);
        padding = new JPanel();
        padding.setOpaque(false);
    }

    public JButton getExpandButton() {
        return expandButton;
    }

    public JPanel getPadding() {
        return padding;
    }

    public void addToInbar(JComponent component) {
        inBar.add(component);
    }

    public void addAllToBarOrder(Collection<? extends Component> c) {
        barOrder.addAll(c);
    }

    private JButton createExpandButton() {
        JButton expButton = new JButton(ImageUtilities.loadImageIcon("org/netbeans/modules/editor/resources/find_expand.png", false)); // NOI18N
        expButton.setMargin(BUTTON_INSETS);
        expButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean state = !isPopupShown;
                isPopupShown = state;
                if (state) {
                    showExpandedMenu();
                } else {
                    hideExpandedMenu();
                }
            }
        });
        return expButton;
    }

    private JPopupMenu createExpandPopup(final JButton expButton) {
        JPopupMenu expPopup = new JPopupMenu();
        expPopup.setLayout(new GridLayout(0, 1));
        expPopup.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                // check if it was canceled by click on expand button
                if (expButton.getMousePosition() == null) {
                    expButton.setContentAreaFilled(false);
                    expButton.setBorderPainted(false);
                    isPopupShown = false;
                }
            }
        });
        return expPopup;
    }

    void computeLayout(JPanel jpanel) {
        int parentWidth = jpanel.getParent().getWidth();
        int totalWidth = 0;
        for (Component c : jpanel.getComponents()) {
            if (c != padding) {
                totalWidth += c.getPreferredSize().width;
            }
        }

        boolean change = false;
        if (totalWidth <= parentWidth) { // enough space try to clear expand popup
            while (!inPopup.isEmpty()) {
                JComponent c = inPopup.getFirst();
                totalWidth += c.getPreferredSize().width;
                if (totalWidth > parentWidth) {
                    break;
                }
                inPopup.removeFirst();
                inBar.add(c);
                expandPopup.remove(c);
                c.setOpaque(false);
                jpanel.add(c, barOrder.indexOf(c));
                change = true;
            }
        } else { // lack of space
            while (totalWidth > parentWidth && !inBar.isEmpty()) {
                JComponent c = inBar.remove(inBar.size() - 1);
                inPopup.addFirst(c);
                jpanel.remove(c);
                expandPopup.add(c, 0);
                c.setOpaque(true);
                totalWidth -= c.getPreferredSize().width;
                change = true;
            }
        }

        if (change) {
            if (inPopup.isEmpty()) {
                jpanel.remove(expandButton);
                expandButton.setVisible(false);
            } else if (getComponentIndexIn(expandButton, jpanel) < 0) {
                jpanel.add(expandButton, getComponentIndexIn(padding, jpanel));
                expandButton.setVisible(true);
            }
            jpanel.revalidate();
            expandPopup.invalidate();
            expandPopup.validate();
        }
    }

    private void showExpandedMenu() {
        if (!inPopup.isEmpty() && !expandPopup.isVisible()) {
            Insets ins = expandPopup.getInsets();
            // compute popup height since JPopupMenu.getHeight does not work
            expandPopup.show(expandButton, 0, -(popupHeight * inPopup.size() + ins.top + ins.bottom));
        }
    }

    private void hideExpandedMenu() {
        if (expandPopup.isVisible()) {
            expandPopup.setVisible(false);
        }
    }

    private int getComponentIndexIn(Component c, JPanel jpanel) {
        Component[] comps = jpanel.getComponents();
        for (int i = 0; i < comps.length; i++) {
            if (c == comps[i]) {
                return i;
            }
        }
        return -1;
    }
}
