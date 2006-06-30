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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit;

import java.awt.Dimension;
import java.awt.LayoutManager;
import javax.swing.JPanel;

/**
 * Panel whose dimension can be restricted by its preferred size.
 * It is supposed to be used in containers using <code>BoxLayout</code>.
 *
 * @see  javax.swing.BoxLayout  BoxLayout
 * @author  Marian Petras
 */
public class SizeRestrictedPanel extends JPanel {

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
    public SizeRestrictedPanel(LayoutManager layoutMgr) {
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
    public SizeRestrictedPanel(LayoutManager layoutMgr,
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
    public Dimension getMaximumSize() {
        if (widthRestriction && heightRestriction) {    //both true
            return getPreferredSize();
        }
        if (widthRestriction == heightRestriction) {    //both false
            return super.getMaximumSize();
        }
        
        Dimension maximumSize = super.getMaximumSize();
        if (widthRestriction) {
            maximumSize.width = getPreferredSize().width;
        } else {
            maximumSize.height = getPreferredSize().height;
        }
        return maximumSize;
    }
    
}
