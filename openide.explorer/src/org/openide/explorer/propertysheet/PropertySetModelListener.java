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
 * PropertySetModelListener.java
 *
 * Created on December 30, 2002, 12:13 PM
 */
package org.openide.explorer.propertysheet;


/** Listener interface for PropertySetModel changes.
 *
 * @author  Tim Boudreau
 */
interface PropertySetModelListener extends java.util.EventListener {
    /* Indicates a change is about to occur, but the model data is still
     *  valid with its pre-change values.  */
    public void pendingChange(PropertySetModelEvent e);

    /** A change which has known constraints, such as the insertion or
     *  removal of rows due to expansion/de-expansion of a category in
     *  a property sheet.  The affected rows are available from the
     *  event object. */
    public void boundedChange(PropertySetModelEvent e);

    /** Called when a change occurs that is so far reaching that the
     *  entire model is invalidated.  In this case, the affected
     *  row properties of the event are irrelevant and should not
     *  be used.*/
    public void wholesaleChange(PropertySetModelEvent e);
}
