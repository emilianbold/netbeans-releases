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
 * PropertyDisplayer_Mutable.java
 * Refactored from PropertyDisplayer.Mutable to keep the interface private.
 * Created on December 13, 2003, 7:20 PM
 */
package org.openide.explorer.propertysheet;

import org.openide.nodes.Node.Property;


/** Basic interface for a property displayer which can have the property
 * it is displaying changed on the fly (such as a table cell renderer)
 * @author Tim Boudreau
 */
interface PropertyDisplayer_Mutable extends PropertyDisplayer {
    public void setProperty(Property prop);
}
