/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo.editors;

import java.awt.Insets;
import org.netbeans.core.UIException;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/** A property editor for Insets class.
* @author   Petr Hamernik
*/
public class InsetsEditor extends ArrayOfIntSupport implements ExPropertyEditor {
    public InsetsEditor() {
        super("java.awt.Insets", 4); // NOI18N
    }

    /** Abstract method for translating the value from getValue() method to array of int. */
    int[] getValues() {
        Insets insets = (Insets) getValue();
        if (insets != null) {
            return new int[] { insets.top, insets.left, insets.bottom, insets.right };
        } else {
            return new int[4];
        }
    }

    /** Abstract method for translating the array of int to value
    * which is set to method setValue(XXX)
    */
    void setValues(int[] val) {
        if ((val[0] < 0) || (val[1] < 0) || (val[2] < 0) || (val[3] < 0)) {
            String msg = NbBundle.getMessage(DimensionEditor.class, 
                "CTL_NegativeSize"); //NOI18N
            IllegalArgumentException iae = new IllegalArgumentException (
                "Negative value"); //NOI18N
            UIException.annotateUser(iae, iae.getMessage(), msg, null,
                                     new java.util.Date());
            throw iae;

        }
        else
            setValue(new Insets(val[0], val[1], val[2], val[3]));
    }

    public boolean supportsCustomEditor () {
        return true;
    }

    public java.awt.Component getCustomEditor () {
        return new InsetsCustomEditor (this, env);
    }

    /** @return the format of value set in property editor. */
    String getHintFormat() {
        return NbBundle.getMessage (InsetsEditor.class, "CTL_HintFormatIE"); //NOI18N
    }

    /** Provides name of XML tag to use for XML persistence of the property value */
    protected String getXMLValueTag () {
        return "Insets"; // NOI18N
    }
    
    private PropertyEnv env;
    public void attachEnv(PropertyEnv env) {
        //cache for custom editor
        this.env = env;
    }    

}
