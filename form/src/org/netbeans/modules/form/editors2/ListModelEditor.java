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

/** A simple property editor for ListModel.
 *
 * @author Tomas Pavek
 */

public class ListModelEditor extends StringArrayEditor {

    private ListModel listModel = null;

    public void setValue(Object val) {
        if (val instanceof ListModel) {
            listModel = (ListModel) val;
            super.setValue(getDataFromModel(listModel));
        }
        else if (val instanceof String[]) {
            listModel = getModelForData((String[])val);
            super.setValue(val);
        }
        else {
            listModel = getModelForData(new String[0]);
            super.setValue(null);
        }
    }

    public Object getValue() {
        return listModel;
    }

    public void setStringArray(String[] value) {
        listModel = getModelForData(value);
        super.setValue(value);
    }

    public String[] getStringArray () {
        return (String[])super.getValue ();
    }

    public String getJavaInitializationString() {
        if (getStrings(true).equals(""))
            return null;
        StringBuffer buf = new StringBuffer("new javax.swing.AbstractListModel() {\n"); // NOI18N
        buf.append("String[] strings = { "); // NOI18N
        buf.append(getStrings(true));
        buf.append(" };\n"); // NOI18N
        buf.append("public int getSize() { return strings.length; }\n"); // NOI18N
        buf.append("public Object getElementAt(int i) { return strings[i]; }\n"); // NOI18N
        buf.append("}"); // NOI18N

        return buf.toString();
    }

    static String[] getDataFromModel(ListModel model) {
        String[] data = new String[model.getSize()];
        for (int i=0; i < data.length; i++) {
            Object obj = model.getElementAt(i);
            data[i] = obj instanceof String ? (String) obj : ""; // NOI18N
        }
        return data;
    }

    static ListModel getModelForData(String[] data) {
        DefaultListModel model = new DefaultListModel();
        for (int i=0; i < data.length; i++)
            model.addElement(data[i]);
        return model;
    }
}
