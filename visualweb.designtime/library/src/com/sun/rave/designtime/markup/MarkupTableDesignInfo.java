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

package com.sun.rave.designtime.markup;

import com.sun.rave.designtime.DesignInfo;
import com.sun.rave.designtime.Result;

/**
 * Implement this interface to make a DesignInfo for a markup table
 * component that wishes to surface resizable row and/or column
 * functionality.
 */
public interface MarkupTableDesignInfo extends DesignInfo {

    /**
     * Tests resizing a row and provides the user with visual feedback.
     * The row and column parameters are absolute zero-based meaning
     * they ignore colspan and rowspan attribute settings.  The return
     * value is the *actual* height the table will snap to based on
     * internal constraints in the component.  This is used to provide
     * visual feedback for the user to see what their drag operation
     * will produce.  Return -1 to indicate a non-resizable row.
     */
    public int testResizeRow(
        MarkupDesignBean mdBean, int row, int column, int height);

    /**
     * Performs the resizing of a row.  This is expected to manipulate
     * some properties of the passed MarkupDesignBean (tree) to produce
     * the desired markup.  The row parameters is absolute zero-based
     * meaning it ignores rowspan attribute settings.
     */
    public Result resizeRow(
        MarkupDesignBean mdBean, int row, int height);

    /**
     * Clears the manual sizing constraints of a row.  This is expected
     * to manipulate some properties of the passed MarkupDesignBean
     * (tree) to produce the desired markup.  The row parameter is
     * absolute zero-based meaning they ignores rowspan attribute
     * settings.
     */
    public Result clearRowSize(MarkupDesignBean mdBean, int row);

    // same as above for columns...
    public int testResizeColumn(
        MarkupDesignBean mdBean, int row, int column, int width);
    public Result resizeColumn(
        MarkupDesignBean mdBean, int column, int width);
    public Result clearColumnSize(MarkupDesignBean mdBean, int column);
}


