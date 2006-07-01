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

package org.netbeans.beaninfo.editors;

import java.awt.Dimension;
import org.netbeans.core.UIException;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.util.NbBundle;

/** A property editor for Dimension class.
* @author   Petr Hamernik
* @version  0.10, 21 Jul, 1998
*/
public class DimensionEditor extends ArrayOfIntSupport
implements ExPropertyEditor {
    public DimensionEditor() {
        super("java.awt.Dimension", 2); // NOI18N
    }

    /** Abstract method for translating the value from getValue() method to array of int. */
    int[] getValues() {
        Dimension d = (Dimension) getValue();
        return new int[] { d.width, d.height };
    }

    static String toArr (int[] ints) {
        StringBuffer sb = new StringBuffer();
        if ((ints != null) && (ints.length > 0)) {
            for (int i=0; i < ints.length; i++) {
                sb.append (ints[i]);
                if (i != ints.length-1) {
                    sb.append (','); //NOI18N
                }
            }
        } else {
            return NbBundle.getMessage (DimensionEditor.class,
                "MSG_NULL_OR_EMPTY"); //NOI18N
        }
        return sb.toString();
    }
    
    /** Abstract method for translating the array of int to value
    * which is set to method setValue(XXX)
    */
    void setValues(int[] val) {
        if ((val[0] < 0) || (val[1] < 0)) {
            String msg = NbBundle.getMessage(DimensionEditor.class, 
                "CTL_NegativeSize"); //NOI18N
            IllegalArgumentException iae = new IllegalArgumentException (
                "Negative value"); //NOI18N
            UIException.annotateUser(iae, iae.getMessage(), msg, null,
                                     new java.util.Date());
            throw iae;
        }
        else
            setValue(new Dimension(val[0], val[1]));
    }

    public boolean supportsCustomEditor () {
        return true;
    }

    public java.awt.Component getCustomEditor () {
        return new PointCustomEditor (this, env);
    }


    /** @return the format of value set in property editor. */
    String getHintFormat() {
        return NbBundle.getMessage(DimensionEditor.class, "CTL_HintFormat"); //NOI18N
    }

    /** Provides name of XML tag to use for XML persistence of the property value */
    protected String getXMLValueTag () {
        return "Dimension"; // NOI18N
    }

}
