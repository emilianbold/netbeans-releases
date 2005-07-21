/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.palette;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.lang.reflect.Field;

import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.explorer.view.MenuView;

/**
 * Hacked MenuView.Menu to use ScrollablePopupMenu instead of JPopupMenu
 * as its popup, and to filter invalid palette item nodes.
 */

public class PaletteMenuView extends org.openide.awt.JMenuPlus {

    private Node menuNode;
    private NodeAcceptor menuAction;

    private boolean hacked = false;
    private boolean filled = false;
    private int level;

    private static int maxHeight = Utilities.getUsableScreenBounds().height - 25;

    public PaletteMenuView(NodeAcceptor acceptor) {
        this(PaletteUtils.getPaletteNode(), acceptor);
    }

    public PaletteMenuView(Node node, NodeAcceptor acceptor) {
        this(node, acceptor, 0);
    }

    private PaletteMenuView(Node node, NodeAcceptor acceptor, int level) {
        menuNode = node;
        menuAction = acceptor;
        this.level = level;
        setText(node.getDisplayName());
        getSubNodes(); // force subnodes creation
    }

    /** popupMenu field should be set here because getPopupMenu() is called from
     * superclass constructor.
     */
    public JPopupMenu getPopupMenu() {
        if (!hacked) {
            try {
                Field f = JMenu.class.getDeclaredField("popupMenu"); // NOI18N
                f.setAccessible(true);
                if (f.get(this) == null) {
                    ScrollPopupMenu popup = new ScrollPopupMenu(maxHeight);
                    popup.setInvoker(this);
                    f.set(this, popup);
                }
                hacked = true;
            }
            catch (Exception ex) {
                System.out.println("[WARNING] Cannot create scrollable popup menu."); // NOI18N
            }
        }

        JPopupMenu popup = super.getPopupMenu();
        fillSubMenu(popup);
        return popup;
    }

    private void fillSubMenu(JPopupMenu popup) {
        if (!filled) {
            filled = true;
            popup.addPopupMenuListener(new PopupListener(popup));
            removeAll();

            Node[] nodes = getSubNodes();
            if (nodes.length > 0) {
                for (int i=0; i < nodes.length; i++)
                    add(nodes[i].isLeaf() ?
                       (JMenuItem) new MenuView.MenuItem(nodes[i], menuAction) :
                       (JMenuItem) new PaletteMenuView(nodes[i], menuAction, level + 1));
            }
            else {
                JMenuItem empty = new JMenuItem(
                    PaletteUtils.getBundleString("CTL_EmptyPaletteMenu")); // NOI18N
                empty.setEnabled(false);
                add(empty);
            }
        }
    }

    private Node[] getSubNodes() {
        return level == 0 ? PaletteUtils.getCategoryNodes(menuNode, true) :
                            PaletteUtils.getItemNodes(menuNode, true);
    }

    private class PopupListener implements PopupMenuListener {
        private JPopupMenu popup;

        PopupListener(JPopupMenu popup) {
            this.popup = popup;
        }

        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            filled = false; // clear the status and stop listening
            popup.removePopupMenuListener(this);
        }
        public void popupMenuCanceled(PopupMenuEvent e) {}
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
    }

    protected Point getPopupMenuOrigin() {
        int x = 0;
        int y = 0;
        JPopupMenu pm = getPopupMenu();
        // Figure out the sizes needed to caclulate the menu position
        Dimension screenSize =Toolkit.getDefaultToolkit().getScreenSize();
        Dimension s = getSize();
        Dimension pmSize = pm.getSize();
        // For the first time the menu is popped up, 
        // the size has not yet been initiated
        if (pmSize.width==0) {
            pmSize = pm.getPreferredSize();
        }
        if (pmSize.height > maxHeight) {
            pmSize.height = maxHeight + 2;
            pmSize.width += 14;
        }

        Point position = getLocationOnScreen();

        Container parent = getParent();
        if (parent instanceof JPopupMenu) {
            // We are a submenu (pull-right)

            if( getComponentOrientation().isLeftToRight() ) {
                // First determine x:
                if (position.x+s.width + pmSize.width < screenSize.width) {
                    x = s.width;         // Prefer placement to the right
                } else {
                    x = 0-pmSize.width;  // Otherwise place to the left
                }
            } else {
                // First determine x:
                if (position.x < pmSize.width) {
                    x = s.width;         // Prefer placement to the right
                } else {
                    x = 0-pmSize.width;  // Otherwise place to the left
                }
            }
            // Then the y:
            if (position.y+pmSize.height < screenSize.height) {
                y = 0;                       // Prefer dropping down
            } else {
                y = s.height-pmSize.height;  // Otherwise drop 'up'
                if (y < -position.y)
                    y = -position.y + 6;
            }
        } else {
            // We are a toplevel menu (pull-down)

            if( getComponentOrientation().isLeftToRight() ) {
                // First determine the x:
                if (position.x+pmSize.width < screenSize.width) {
                    x = 0;                     // Prefer extending to right 
                } else {
                    x = s.width-pmSize.width;  // Otherwise extend to left
                }
            } else {
                // First determine the x:
                if (position.x+s.width < pmSize.width) {
                    x = 0;                     // Prefer extending to right 
                } else {
                    x = s.width-pmSize.width;  // Otherwise extend to left
                }
            }
            // Then the y:
            if (position.y+s.height+pmSize.height < screenSize.height) {
                y = s.height;          // Prefer dropping down
            } else {
                y = -pmSize.height;   // Otherwise drop 'up'
            }
        }
        return new Point(x,y);
    }
}
