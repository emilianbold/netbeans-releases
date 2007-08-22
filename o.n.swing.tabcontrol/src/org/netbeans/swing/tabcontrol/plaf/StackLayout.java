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

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.*;
import java.lang.ref.WeakReference;
import javax.swing.JComponent;

/**
 * Special simple layout used in TabbedContainer. Shows component in the
 * "stack", it means that only one component is visible at any time, others are
 * always hidden "below" the visible one. Use method showComponent to select
 * visible component.
 *
 * @author Dafe Simonek
 */
class StackLayout implements LayoutManager {
    
    // #100486 - hold visibleComp weakly, because removeLayoutComponent may not
    // be called and then visibleComp is not freed. See StackLayoutTest for details.
    /**
     * Holds currently visible component or null if no comp is visible
     */
    private WeakReference<Component> visibleComp = null;

    /**
     * Set the currently displayed component.  If passed null for the component,
     * all contained components will be made invisible (sliding windows do this)
     * @param c Component to show
     * @param parent Parent container
     */
    public void showComponent(Component c, Container parent) {
        Component comp = getVisibleComponent();
        if (comp != c) {
            if (!parent.isAncestorOf(c) && c != null) {
                parent.add(c);
            }
            synchronized (parent.getTreeLock()) {
                if (comp != null) {
                    comp.setVisible(false);
                }
                visibleComp = new WeakReference<Component>(c);
                if (c != null) {
                    c.setVisible(true);
                }
		// trigger re-layout
		if (c instanceof JComponent) {
		    ((JComponent)c).revalidate();
		}
		else {
		    parent.validate(); //XXX revalidate should work!
		}
            }
        }
    }
    
    /** Allows support for content policies 
     * @return Currently visible component or null
     */
    public Component getVisibleComponent() {
        return visibleComp == null ? null : visibleComp.get();
    }

    /**
     * ********** Implementation of LayoutManager interface *********
     */

    public void addLayoutComponent(String name, Component comp) {
        synchronized (comp.getTreeLock()) {
            comp.setVisible(false);
            // keep consistency if showComponent was already called on this
            // component before
            if (comp == getVisibleComponent()) {
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
            if (comp == getVisibleComponent()) {
                visibleComp = null;
            }
            // kick out removed component as visible, so that others
            // don't have problems with hidden components
            comp.setVisible(true);
        }
    }

    public void layoutContainer(Container parent) {
        Component visibleComp = getVisibleComponent();
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