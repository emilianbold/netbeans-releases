/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * PropertySetModelEvent.java
 *
 * Created on December 30, 2002, 11:56 AM
 */
package org.openide.explorer.propertysheet;

import java.awt.event.*;


/** Event type that carries information about changes in the property
 *  set model, such as changes in the number of rows that should be
 *  shown in the table due to expanding or closing categories.  In
 *  particular it is used to maintain the current selection across a
 *  change which can alter the selected index.
 * @author  Tim Boudreau
 */
class PropertySetModelEvent extends java.util.EventObject {
    public static final int TYPE_INSERT = 0;
    public static final int TYPE_REMOVE = 1;
    public static final int TYPE_WHOLESALE_CHANGE = 2;
    int type = 2;
    int start = -1;
    int end = -1;
    boolean reordering = false;

    /**  Create a new model event of <code>TYPE_WHOLESALE_CHANGE</code>. */
    public PropertySetModelEvent(Object source) {
        super(source);
    }

    /** Create a new model event with the specified parameters. */
    public PropertySetModelEvent(Object source, int type, int start, int end, boolean reordering) {
        super(source);
        this.type = type;
        this.start = start;
        this.end = end;
        this.reordering = reordering;
    }

    /** Get the type of event.  This will be one of
    * TYPE_INSERT,
    * TYPE_REMOVE, or
    * TYPE_WHOLESALE_CHANGE,
    * depending on the type of change (expansion of a category,
    * de-expansion of a category, or a wholesale change like changing
    * the node displayed, which completely invalidates the displayed
    * data.  */
    public int getType() {
        return type;
    }

    /** Get the first row affected by this change.  */
    public int getStartRow() {
        return start;
    }

    /** Get the last row affected by this change.  This should be the
     *  affected row <strong>prior</strong> to the change;  that is, if
     *  a category is de-expanded, removing properties 20-30, this value
     *  should be 30. */
    public int getEndRow() {
        return end;
    }

    public boolean isReordering() {
        return reordering;
    }
}
