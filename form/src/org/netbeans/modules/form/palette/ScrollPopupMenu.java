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
import org.openide.awt.*;


/** Hacked JPopupMenu(Plus) - displayed in JScrollPane if too long.
 */
public class ScrollPopupMenu extends JPopupMenuPlus {

    JWindow popWin;
    JScrollPane scrollPane;
    int posX, posY;
    int maxHeight;

    ScrollPopupMenu(int maxH) {
        maxHeight = maxH;
    }

    public void setVisible(boolean visible) {
        if (visible == isVisible()) return;

        if (visible) {
            if (getInvoker() != null && !(getInvoker() instanceof JMenu)) {
		if (getSubElements().length > 0) {
		    MenuElement me[] = new MenuElement[2];
		    me[0] = (MenuElement)this;
		    me[1] = getSubElements()[0];
		    MenuSelectionManager.defaultManager().setSelectedPath(me);
		}
                else {
		    MenuElement me[] = new MenuElement[1];
		    me[0] = (MenuElement)this;
		    MenuSelectionManager.defaultManager().setSelectedPath(me);
		}
	    }

            firePopupMenuWillBecomeVisible();

            Component comp = getInvoker();
            while (comp.getParent() != null)
                comp = comp.getParent();

            popWin = comp instanceof Window ?
                        new JWindow((Window)comp) :
                        new JWindow(new JFrame());
            popWin.setLocation(posX, posY);
            
            pack();
            popWin.setVisible(true);
        }
        else {
            getSelectionModel().clearSelection();
            if (popWin != null) {
                firePopupMenuWillBecomeInvisible();
                popWin.hide();
                popWin = null;
                scrollPane = null;
            }
        }
    }

    public boolean isVisible() {
        return popWin != null ? popWin.isShowing() : false;
    }

    public void setLocation(int x, int y) {
        if (popWin != null && popWin.isShowing())
            popWin.setLocation(x, y);
        else {
            posX = x;
            posY = y;
        }
    }

    public void pack() {
        if (popWin == null)
            return;
        
        Dimension prefSize = getPreferredSize();
        if (maxHeight == 0 || prefSize.height <= maxHeight) {
            if (scrollPane != null) {
                popWin.getContentPane().remove(scrollPane);
                scrollPane = null;
            }
            popWin.getContentPane().add(this, BorderLayout.CENTER);
            popWin.pack();
        }
        else {
            if (scrollPane == null) {
                JPanel view = new JPanel(new BorderLayout());
                view.add(this, BorderLayout.CENTER);

                scrollPane = new JScrollPane(view);
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    //                scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                JScrollBar bar = scrollPane.getVerticalScrollBar();
                if (bar != null) {
                    Dimension d = bar.getPreferredSize();
                    d.width = 12;
                    bar.setPreferredSize(d);
                    bar.setUnitIncrement(21);
                }

                popWin.getContentPane().add(scrollPane, BorderLayout.CENTER);
            }
            popWin.pack();
            popWin.setSize(popWin.getSize().width+12, maxHeight);
            requestFocus();
        }
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public JScrollBar getScrollBar() {
        return scrollPane != null ? scrollPane.getVerticalScrollBar() : null;
    }
}
