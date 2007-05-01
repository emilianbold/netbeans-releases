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

import java.awt.Insets;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class TableConstants {

    /**
     * constant for inset of column
     */
    public static final Insets COLUMN_INSETS = new Insets(1, 5, 0, 10);

    /**
     * constant for insets of table header
     */
    public static final Insets TABLE_HEADER_INSETS = new Insets(1, 0, 1, 0);

    /**
     * constant for insets of header cell
     */
    public static final Insets HEADER_CELL_INSETS = new Insets(1, 2, 1, 10);

    /**
     * constant for scrollbar width
     */
    public static final int TABLE_SCROLLBAR_WIDTH = 14;

    /**
     * constant for the gap between table and its header
     */
    public static final int TABLE_HEADER_GAP = 2;

    /**
     * constant that describe a column area having left port area
     */
    public static final int LEFT_PORT_AREA = 0;

    /**
     * constant that describe a column area having right port area
     */
    public static final int RIGHT_PORT_AREA = 1;

    /**
     * constants that describes a table being both input and output so that port appears
     * on both left and right side of it
     */
    public static final int INPUT_OUTPUT_TABLE = -1;

    /**
     * constants that describes a table being input so that port appears only on right
     * side of it
     */
    public static final int INPUT_TABLE = 0;

    /**
     * constants that describes a table being output so that port appears only on left
     * side of it
     */
    public static final int OUTPUT_TABLE = 1;
    
    public static final int NO_PORT_TABLE = 2;

}

