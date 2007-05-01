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

package org.netbeans.modules.sql.framework.ui.graph;

import com.nwoods.jgo.JGoObject;

/**
 * @author radval
 */
public interface ListAreaCellRenderer {

    /**
     * get the list cell renderer
     * 
     * @param list list
     * @param value value
     * @param index index
     * @param isSelected isSelected
     * @param cellHasFocus cellHasFocus
     * @return list renderer
     */
    JGoObject getListAreaCellRenderer(JGoObject list, Object value, // value to display
            int index, // cell index
            boolean isSelected, // is the cell selected
            boolean cellHasFocus); // the list and the cell have the focus

}

