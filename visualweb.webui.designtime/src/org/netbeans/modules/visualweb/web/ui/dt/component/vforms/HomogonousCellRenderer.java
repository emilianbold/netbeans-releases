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
package org.netbeans.modules.visualweb.web.ui.dt.component.vforms;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;


/**
 * A delegating renderer class that consistently sets the background color
 * of cells to reflect "selected" and "unselected" states.
 */
public class HomogonousCellRenderer extends DefaultTableCellRenderer {

    private static Color SELECTION_BACKGROUND =
            UIManager.getDefaults().getColor("TextField.selectionBackground");

    private static Color SELECTION_FOREGROUND =
            UIManager.getDefaults().getColor("TextField.selectionForeground");

    private static Color BACKGROUND =
            UIManager.getDefaults().getColor("TextField.background");

    private static Color FOREGROUND =
            UIManager.getDefaults().getColor("TextField.foreground");

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table,  value, isSelected, hasFocus, row, column);

        // To make the selection more visual

        //if( hasFocus )
        //    setBorder( new LineBorder(Color.WHITE) );

        if (isSelected) {
            c.setBackground(SELECTION_BACKGROUND);
            c.setForeground(SELECTION_FOREGROUND);
        }
        else {
            c.setBackground(BACKGROUND);
            c.setForeground(FOREGROUND);
        }

        return c;
    }
}
