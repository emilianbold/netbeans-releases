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

package org.netbeans.modules.form.editors2;

import javax.swing.*;
import org.netbeans.modules.form.editors.StringArrayEditor;
import org.openide.util.NbBundle;

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
    
    // NamedPropertyEditor implementation
    public String getDisplayName() {
        return NbBundle.getBundle(getClass()).getString("CTL_ComboBoxModelEditor_DisplayName"); // NOI18N
    }

}
