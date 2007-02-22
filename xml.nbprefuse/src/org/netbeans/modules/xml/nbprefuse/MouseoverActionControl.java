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
/*
 * MouseoverActionControl.java
 *
 * Created on March 26, 2006, 7:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.nbprefuse;

import java.awt.event.MouseEvent;
import prefuse.controls.HoverActionControl;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Jeri Lockhart
 */
public class MouseoverActionControl extends HoverActionControl{
    protected String action;

    /**
     * Creates a new instance of MouseoverActionControl
     */
    public MouseoverActionControl(String action) {
        super(action);
        this.action = action;
    }

    /**
     * @see prefuse.controls.Control#itemEntered(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    public void itemEntered(VisualItem item, MouseEvent e) {
        if (item instanceof NodeItem &&
                item.canSetBoolean(AnalysisConstants.MOUSEOVER)){
            item.setBoolean(AnalysisConstants.MOUSEOVER, true);
        }
        super.itemEntered(item, e);
    }

    /**
     * @see prefuse.controls.Control#itemExited(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     *
     * NbFocusControl can also set MOUSEOVER to false
     *   if the user clicks a NodeItem while pressing the Control key
     */
    public void itemExited(VisualItem item, MouseEvent e) {
        if (item instanceof NodeItem &&
                item.canSetBoolean(AnalysisConstants.MOUSEOVER)){
            item.setBoolean(AnalysisConstants.MOUSEOVER, false);
        }
        super.itemExited(item, e);
    }



}
