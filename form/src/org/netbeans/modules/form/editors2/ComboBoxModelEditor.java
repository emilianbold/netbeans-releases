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
import org.netbeans.modules.form.editors.StringArrayEditor;

/** A simple property editor for ComboBoxModel.
 *
 * @author Tomas Pavek
 */

public class ComboBoxModelEditor extends StringArrayEditor {

    private ComboBoxModel comboModel = null;

    public void setValue(Object val) {
        if (val instanceof ComboBoxModel) {
            comboModel = (ComboBoxModel) val;
            super.setValue(getDataFromModel(comboModel));
        }
        else if (val instanceof String[]) {
            comboModel = getModelForData((String[])val);
            super.setValue(val);
        }
        else {
            comboModel = getModelForData(new String[0]);
            super.setValue(null);
        }
    }

    public Object getValue() {
        return comboModel;
    }

    public void setStringArray(String[] value) {
        comboModel = getModelForData(value);
        super.setValue(value);
    }

    public String[] getStringArray () {
        return (String[])super.getValue ();
    }

    public String getJavaInitializationString() {
        if (getStrings(true).equals(""))
            return null;
        StringBuffer buf = new StringBuffer(
                "new javax.swing.DefaultComboBoxModel(new String[] { "); // NOI18N
        buf.append(getStrings(true));
        buf.append(" })"); // NOI18N

        return buf.toString();
    }

    static String[] getDataFromModel(ComboBoxModel model) {
        return ListModelEditor.getDataFromModel(model);
    }

    static ComboBoxModel getModelForData(String[] data) {
        return new DefaultComboBoxModel(data);
    }
}
