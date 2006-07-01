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
package org.openide.explorer.propertysheet.editors;

import org.openide.explorer.propertysheet.PropertyEnv;

/**
* Enhances standard custom property editor with the possibility to return the customized value.
* I.e. the custom property editor does not need to fire property changes upon
* modifications; the property dialog manager
* instead sets the acquired value after the custom editor is closed.
*
* @author  Ian Formanek
* @deprecated Use {@link PropertyEnv} instead. An example of what needs to be
*    done can be found in the rewrite of
*    <a href="http://www.netbeans.org/source/browse/core/src/org/netbeans/beaninfo/editors/RectangleCustomEditor.java?r1=1.25&r2=1.26">RectangleCustomEditor</a>.
*  Another example showing the changes in property editor as well as in its
*  custom component can be found in
*  <a href="http://core.netbeans.org/source/browse/core/execution/src/org/netbeans/core/execution/beaninfo/editors/NbProcessDescriptorEditor.java?r1=1.3&r2=1.4">NbProcessDescriptorEditor</a>
*  and
*   <a href="http://core.netbeans.org/source/browse/core/execution/src/org/netbeans/core/execution/beaninfo/editors/NbProcessDescriptorCustomEditor.java?r1=1.3&r2=1.5">NbProcessDescriptorCustomEditor</a>.
*/
public @Deprecated interface EnhancedCustomPropertyEditor {
    /** Get the customized property value.
    * @return the property value
    * @exception IllegalStateException when the custom property editor does not contain a valid property value
    *            (and thus it should not be set)
    */
    public Object getPropertyValue() throws IllegalStateException;
}
