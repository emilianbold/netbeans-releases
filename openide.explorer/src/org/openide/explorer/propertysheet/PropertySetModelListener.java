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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
