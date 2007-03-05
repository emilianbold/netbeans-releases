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
package com.sun.rave.web.ui.component;

/**
 * A panel component for laying out other components in flow mode, or using
 * absolute positioning.
 *
 * @author gjmurphy
 */
public class PanelLayout extends PanelLayoutBase {
    
    /**
     * Keyword that indicates flow layout.
     */
    public final static String FLOW_LAYOUT = "flow";
    
    /**
     * Keyword that indicates grid layout or absolute positioning.
     */
    public final static String GRID_LAYOUT = "grid";
    
    /**
     * Creates a new instance of PanelLayout.
     */
    public PanelLayout() {
        super();
    }
    
    /**
     * Set the layout mode for this panel, to either "grid" or "flow". The
     * default value is "grid". Setting this property to an unrecognized value
     * will cause it to reset to the default value.
     */
    public void setPanelLayout(String panelLayout) {
        if (FLOW_LAYOUT.equals(panelLayout))
            super.setPanelLayout(FLOW_LAYOUT);
        else
            super.setPanelLayout(GRID_LAYOUT);
    }
    
    public String getPanelLayout() {
        String panelLayout = super.getPanelLayout();
        if (panelLayout == null)
            return GRID_LAYOUT;
        return panelLayout;
    }

}
