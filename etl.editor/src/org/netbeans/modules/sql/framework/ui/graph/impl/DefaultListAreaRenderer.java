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
package org.netbeans.modules.sql.framework.ui.graph.impl;

import org.netbeans.modules.sql.framework.ui.graph.IHighlightConfigurator;
import org.netbeans.modules.sql.framework.ui.graph.ListAreaCellRenderer;

import com.nwoods.jgo.JGoObject;

/**
 * Extends highlightable basic cell to allow list area components to change color on mouse
 * hover.
 * 
 * @author Ritesh Adval
 * @author Jonathan Giron
 * @version $Revision$
 */
public class DefaultListAreaRenderer extends BasicCellArea.Highlightable implements ListAreaCellRenderer {

    /** Creates a new instance of DefaultListAreaRenderer */
    public DefaultListAreaRenderer() {
    }

    /**
     * Gets the list area cell renderer
     * 
     * @param list list Area
     * @param value value
     * @param index index
     * @param isSelected isSelected
     * @param cellHasFocus cellHasFocus
     * @return list renderer
     */
    public JGoObject getListAreaCellRenderer(JGoObject list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        BasicCellArea.Highlightable tCellArea = new BasicCellArea.Highlightable(BasicCellArea.LEFT_PORT_AREA, value.toString());

        tCellArea.setBrush(this.getHighlightConfigurator().getNormalBrush());
        tCellArea.setTextColor(this.getTextColor());
        tCellArea.setHighlightEnabled(this.isHighlightEnabled());

        IHighlightConfigurator tHC = tCellArea.getHighlightConfigurator();
        tHC.setHoverBrush(this.getHighlightConfigurator().getHoverBrush());

        // Draw bounding rectangle around it
        tCellArea.drawBoundingRect(true);
        return cellArea;
    }
}

