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
