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

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.*;

/**
 * Special simple layout used in TabbedContainer. Shows component in the
 * "stack", it means that only one component is visible at any time, others are
 * always hidden "below" the visible one. Use method showComponent to select
 * visible component.
 *
 * @author Dafe Simonek
 */
class StackLayout implements LayoutManager {
    /**
     * Default size when no components are contained
     */
    private static Dimension emptySize = null;
    /**
     * Holds currently visible component or null if no comp is visible
     */
    private Component visibleComp = null;

    /**
     * Set the currently displayed component.  If passed null for the component,
     * all contained components will be made invisible (sliding windows do this)
     */
    public void showComponent(Component c, Container parent) {
        if (visibleComp != c) {
            if (!parent.isAncestorOf(c) && c != null) {
                parent.add(c);
            }
            synchronized (parent.getTreeLock()) {
                if (visibleComp != null) {
                    visibleComp.setVisible(false);
                }
                visibleComp = c;
                if (c != null) {
                    visibleComp.setVisible(true);
                }
                // trigger re-layout
                parent.validate(); //XXX revalidate should work!
            }
        }
    }
    
    /** Allows support for content policies */
    public Component getVisibleComponent() {
        return visibleComp;
    }

    /**
     * ********** Implementation of LayoutManager interface *********
     */

    public void addLayoutComponent(String name, Component comp) {
        synchronized (comp.getTreeLock()) {
            comp.setVisible(false);
            // keep consistency if showComponent was already called on this
            // component before
            if (comp == visibleComp) {
                visibleComp = null;
            }
/*System.out.println("Border dump for " + comp.getName());
borderDump((javax.swing.JComponent)comp, "");*/
        }
    }
    
/*private void borderDump (javax.swing.JComponent comp, String space) {
    javax.swing.border.Border compBorder = comp.getBorder();
    if (compBorder == null) {
        System.out.println(space + comp.getClass().getName() + " has no border.");
    } else {
        System.out.println(space + comp.getClass().getName() + ": " + compBorder.getClass().getName());
    }
    Component curComp;
    for (int i = 0; i < comp.getComponentCount(); i++) {
        curComp = comp.getComponent(i);
        if (curComp instanceof javax.swing.JComponent) {
            borderDump((javax.swing.JComponent)curComp, space + " ");
        }
    }
}*/
    
    public void removeLayoutComponent(Component comp) {
        synchronized (comp.getTreeLock()) {
            if (comp == visibleComp) {
                visibleComp = null;
            }
            // kick out removed component as visible, so that others
            // don't have problems with hidden components
            comp.setVisible(true);
        }
    }

    public void layoutContainer(Container parent) {
        if (visibleComp != null) {
            synchronized (parent.getTreeLock()) {
                Insets insets = parent.getInsets();
                visibleComp.setBounds(insets.left, insets.top, parent.getWidth()
                   - (insets.left + insets.right), parent.getHeight()
                   - (insets.top + insets.bottom));
            }
        }
    }

    public Dimension minimumLayoutSize(Container parent) {
        return getEmptySize();
    }

    public Dimension preferredLayoutSize(Container parent) {
        return getEmptySize();
    }

    /**
     * Specifies default size of empty container
     */
    private static Dimension getEmptySize() {
        return new Dimension(50, 50);
    }

}