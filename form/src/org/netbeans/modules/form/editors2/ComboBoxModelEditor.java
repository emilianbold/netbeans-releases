/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.editors2;

import javax.swing.*;
import org.openide.explorer.propertysheet.editors.*;
import org.netbeans.modules.form.FormDesignValue;
import org.netbeans.modules.form.RADComponent;

/** A simple property editor for ComboBoxModel.
 *
 * @author Tomas Pavek
 */
public class ComboBoxModelEditor extends StringArrayEditor {

    private NbComboBoxModel comboModel = null;

    public void setValue(Object val) {
        if (val instanceof NbComboBoxModel) {
            comboModel = (NbComboBoxModel)val;
        }
        else if (val instanceof String[]) {
            comboModel = new NbComboBoxModel((String[])val);
        }
        else return;
        super.setValue(comboModel.data);
    }

    public Object getValue() {
        return comboModel;
    }

    public void setStringArray(String[] value) {
        comboModel = new NbComboBoxModel(value);
        super.setValue(value);
    }

    public String[] getStringArray () {
        return (String[])super.getValue ();
    }

    public String getJavaInitializationString() {
        StringBuffer buf = new StringBuffer(
                "new javax.swing.DefaultComboBoxModel(new String[] { "); // NOI18N
        buf.append(getStrings(true));
        buf.append(" })"); // NOI18N

        return buf.toString();
    }


    public static class NbComboBoxModel implements FormDesignValue {
        private String[] data;
        private DefaultComboBoxModel model;

        public NbComboBoxModel(String[] data) {
            this.data = data;
            model = new DefaultComboBoxModel(data);
        }

        /** Provides a value which should be used during design-time
         * as the real value of a property on the JList instance.
         * @return the real property value to be used during design-time
         */
        public Object getDesignValue(RADComponent radComponent) {
            return model;
        }
    }
}
