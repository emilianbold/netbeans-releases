/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit;

/**
 * Panel whose dimension can be restricted by its preferred size.
 * It is supposed to be used in containers using <code>BoxLayout</code>.
 *
 * @see  javax.swing.BoxLayout  BoxLayout
 * @author  Marian Petras
 */
class SizeRestrictedPanel extends javax.swing.JPanel {
    
    /** whether the panel's width is restricted */
    private final boolean widthRestriction;
    /** whether the panel's height is restricted */
    private final boolean heightRestriction;
    
    /**
     * Creates a panel with flow layout, restricted in both directions.
     */
    public SizeRestrictedPanel() {
        this(true, true);
    }
    
    /**
     * Creates a panel with flow layout, with width and/or height restricted.
     *
     * @param  widthRestriction  whether the panel's width should be restricted
     * @param  heightRestriction whether the panel's height should be restricted
     */
    public SizeRestrictedPanel(boolean widthRestriction,
                               boolean heightRestriction) {
        super();
        this.widthRestriction = widthRestriction;
        this.heightRestriction = heightRestriction;
    }
    
    /**
     * Creates a panel with the specified layout manager and with size
     * restricted in both directions.
     *
     * @param  layoutMgr  layout manager for this panel
     */
    public SizeRestrictedPanel(java.awt.LayoutManager layoutMgr) {
        this(layoutMgr, true, true);
    }
    
    /**
     * Creates a panel with the specified layout manager and with width and/or
     * height restricted.
     *
     * @param  layoutMgr  layout manager for this panel
     * @param  widthRestriction  whether the panel's width should be restricted
     * @param  heightRestriction whether the panel's height should be restricted
     */
    public SizeRestrictedPanel(java.awt.LayoutManager layoutMgr,
                               boolean widthRestriction,
                               boolean heightRestriction) {
        super(layoutMgr);
        this.widthRestriction = widthRestriction;
        this.heightRestriction = heightRestriction;
    }
    
    /**
     * Returns maximum size of this panel.
     * The maximum size can be restricted in width, height or in both
     * directions, depending on parameters passed to the constructor.
     *
     * @return  dimension returned from original <code>getMaximumSize()</code>
     *          and then modified according to restrictions specified
     *          by the constructor's parameters
     */
    public java.awt.Dimension getMaximumSize() {
        if (widthRestriction && heightRestriction) {    //both true
            return getPreferredSize();
        }
        if (widthRestriction == heightRestriction) {    //both false
            return super.getMaximumSize();
        }
        
        java.awt.Dimension maximumSize = super.getMaximumSize();
        if (widthRestriction) {
            maximumSize.width = getPreferredSize().width;
        } else {
            maximumSize.height = getPreferredSize().height;
        }
        return maximumSize;
    }
    
}
