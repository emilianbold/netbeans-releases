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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows.view.ui.toolbars;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.openide.awt.ToolbarPool;
import org.openide.windows.WindowManager;


/**
 * ToolbarLayout is a LayoutManager2 that should be used on a toolbar panel to
 * allow placement of components in absolute positions.
 *
 * This is the place where components are setted it's bounds by actual
 * toolbar configuration.
 *
 * @author Libor Kramolis
 */
public class ToolbarLayout implements LayoutManager2, java.io.Serializable {
    /** Toolbar horizontal gap. */
    public static final int HGAP = 1;
    /** Toolbar vertical gap. */
    public static final int VGAP = 1;

    static final long serialVersionUID =7489472539255790677L;
    
    /** ToolbarConfiguration cached for getting preferred toolbar configuration width. */
    ToolbarConfiguration toolbarConfig;
    /** Map of components. */
    HashMap<Component,Object> componentMap;

    /**
     * Creates a new ToolbarLayout.
     */
    public ToolbarLayout (ToolbarConfiguration conf) {
        toolbarConfig = conf;
        componentMap = new HashMap<Component, Object>();
    }

    /** Adds the specified component with the specified name to
     * the layout. Everytime throws IllegalArgumentException.
     * @param name the component name
     * @param comp the component to be added
     */
    public void addLayoutComponent (String name, Component comp) {
        throw new IllegalArgumentException();
    }

    /**
     * Adds the specified component to the layout, using the specified
     * constraint object.
     * @param comp the component to be added
     * @param constraints  the where/how the component is added to the layout.
     * @exception <code>ClassCastException</code> if the argument is not a
     *		  <code>ToolbarConstraints</code>.
     */
    public void addLayoutComponent (Component comp, Object constr) {
        if (!(constr instanceof ToolbarConstraints))
            throw new IllegalArgumentException (ToolbarConfiguration.getBundleString("EXC_wrongConstraints"));

        componentMap.put (comp, constr);
        ToolbarConstraints tc = (ToolbarConstraints)constr;
        tc.setPreferredSize (comp.getPreferredSize());
        comp.setVisible (tc.isVisible());
    }

    /**
     * Removes the specified component from this layout.
     * @param comp the component to be removed
     */
    public void removeLayoutComponent (Component comp) {
        componentMap.remove (comp);
    }

    /**
     * Calculates the preferred dimension for the specified panel given the
     * components in the specified parent container.
     * @param parent the component to be laid out
     *
     * @see #minimumLayoutSize
     */
    public Dimension preferredLayoutSize (Container parent) {
        Insets insets = parent.getInsets();
        Dimension prefSize = new Dimension (insets.left + toolbarConfig.getPrefWidth() + insets.right,
                                            insets.top + toolbarConfig.getPrefHeight() + insets.bottom);
        return prefSize;
    }

    /**
     * Calculates the minimum dimension for the specified
     * panel given the components in the specified parent container.
     * @param parent the component to be laid out
     * @see #preferredLayoutSize
     */
    public Dimension minimumLayoutSize (Container parent) {
        return preferredLayoutSize (parent);
    }

    /**
     * Returns the maximum size of this component.
     * @see java.awt.Component#getMinimumSize()
     * @see java.awt.Component#getPreferredSize()
     * @see LayoutManager
     */
    public Dimension maximumLayoutSize (Container parent) {
        return new Dimension (Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Returns the alignment along the x axis.  This specifies how the
     * component would like to be aligned relative to other components.  The
     * value should be a number between 0 and 1 where 0 represents alignment
     * along the origin, 1 is aligned the furthest away from the origin, 0.5
     * is centered, etc.
     */
    public float getLayoutAlignmentX (Container parent) {
        return 0;
    }

    /**
     * Returns the alignment along the y axis.  This specifies how the
     * component would like to be aligned relative to other components.  The
     * value should be a number between 0 and 1 where 0 represents alignment
     * along the origin, 1 is aligned the furthest away from the origin, 0.5
     * is centered, etc.
     */
    public float getLayoutAlignmentY (Container parent) {
        return 0;
    }

    /**
     * Invalidates the layout, indicating that if the layout manager has
     * cached information it should be discarded.
     */
    public void invalidateLayout (Container parent) {
    }

    /**
     * Lays out the container in the specified panel.
     * @param parent the component which needs to be laid out
     */
    public void layoutContainer (Container parent) {
        synchronized (parent.getTreeLock()) {
            //Insets insets = parent.getInsets();
            //int maxPosition = parent.getWidth() - (insets.left + insets.right) - HGAP;
            Insets insets = WindowManager.getDefault().getMainWindow().getInsets();
            int maxPosition;
            Frame f = WindowManager.getDefault().getMainWindow();
            if ((f.getExtendedState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
                maxPosition = f.getWidth() - HGAP;
            } else {
                maxPosition = f.getWidth() - (insets.left + insets.right) - HGAP;
            }
            Component comp;
            ToolbarConstraints constr;

	    /* It is very important to update preferred sizes for each component
	       because when any component change it's size it can affect to other
	       components' size and position. */
            Iterator it = componentMap.keySet().iterator();
            while (it.hasNext()) {
                comp = (Component)it.next();
                constr = (ToolbarConstraints)componentMap.get (comp);
                constr.updatePreferredSize (comp.getPreferredSize());
            }

	    /* Setting components' bounds. */
            HashSet<ToolbarConstraints> completed = new HashSet<ToolbarConstraints>(componentMap.size()*2);
            for (int i = 0; i < toolbarConfig.getRowCount(); i++) {
                ToolbarConstraints overflownTC = processRow(toolbarConfig.getRow(i), completed, maxPosition);
                // add row members to completed
                for (Iterator<ToolbarConstraints> iter = toolbarConfig.getRow(i).iterator(); it.hasNext(); ) {
                    completed.add(iter.next());
                }
            }
            parent.repaint();
        }
    }
     
    private ToolbarConstraints processRow (ToolbarRow row, Collection completed, int maxPosition) {
        Rectangle bounds;
        Component comp;
        ToolbarConstraints constr;
        List<ToolbarConstraints> moveDownCandidates = new ArrayList<ToolbarConstraints>(5);
        for (Iterator it = row.iterator(); it.hasNext(); ) {
            constr = (ToolbarConstraints)it.next();
            // don't compute twice
            if (completed.contains(constr)) {
                continue;
            }
            comp = ToolbarPool.getDefault().findToolbar(constr.getName());

            /* ToolbarConstraints has component bounds prepared. */
            bounds = constr.getBounds();
            if ((bounds.x < maxPosition) &&                 // If component starts on visible position ...
                (bounds.x + bounds.width > maxPosition)) {  // ... but with width it is over visible area ...
                bounds.width = maxPosition - bounds.x;      // ... so width is cropped to max possible.
                comp.setBounds(bounds);
            } else {
                if (constr.getPosition() > maxPosition + HGAP) {
                    // mark toolbar as candidate for move to next row down
                    if (!constr.isAlone() && ((row.toolbarCount() - moveDownCandidates.size()) > 1)) {
                        moveDownCandidates.add(constr);
                    }
                } else {
                    comp.setBounds(bounds);
                }
            }
        }
        // move chosen toolbars to next row
        for (Iterator iter = moveDownCandidates.iterator(); iter.hasNext(); ) {
            moveToolbarDown((ToolbarConstraints)iter.next());
        }
        
        return null;
    }
    

    /** Move toolbar specified by given constraints to the next row */
    private void moveToolbarDown (ToolbarConstraints constr) {
        int pos = constr.rowIndex() + 1;
        constr.destroy();
        for (int i = pos; i < pos + constr.getRowCount(); i++) {
            toolbarConfig.getRow(i).addToolbar(constr, 0);
        }
        constr.setPosition(0);
        constr.updatePosition();
    }
    
    
}

