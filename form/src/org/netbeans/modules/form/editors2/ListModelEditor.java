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

/** A simple property editor for ListModel.
 *
 * @author Tomas Pavek
 */
public class ListModelEditor extends StringArrayEditor {

    private NbListModel listModel = null;

    public void setValue(Object val) {
        if (val instanceof NbListModel) {
            listModel = (NbListModel)val;
            super.setValue(listModel.data);
        }
        else if (val instanceof String[]) {
            listModel = new NbListModel((String[])val);
            super.setValue(listModel.data);
        }
        else {
            listModel = new NbListModel(new String[0]);
            super.setValue(null);
        }
    }

    public Object getValue() {
        return listModel;
    }

    public void setStringArray(String[] value) {
        listModel = new NbListModel(value);
        super.setValue(value);
    }

    public String[] getStringArray () {
        return (String[])super.getValue ();
    }

    public String getJavaInitializationString() {
        StringBuffer buf = new StringBuffer("new javax.swing.AbstractListModel() {\n"); // NOI18N
        buf.append("String[] strings = { "); // NOI18N
        buf.append(getStrings(true));
        buf.append(" };\n"); // NOI18N
        buf.append("public int getSize() { return strings.length; }\n"); // NOI18N
        buf.append("public Object getElementAt(int i) { return strings[i]; }\n"); // NOI18N
        buf.append("}"); // NOI18N

        return buf.toString();
    }


    public static class NbListModel implements FormDesignValue{
        private String[] data;
        private DefaultListModel model;

        public NbListModel(String[] data) {
            this.data = data;
            model = new DefaultListModel();
            for (int i=0; i < data.length; i++)
                model.addElement(data[i]);
        }

        /** Returns description of the design value. Useful when
         * design value is not provided.
         */
        public String getDescription() {
            return "ListModel"; // NOI18N
        }
        
        /** Provides a value which should be used during design-time
         * as the real value of a property on the JList instance.
         * @return the real property value to be used during design-time
         */
        public Object getDesignValue() {
            return model;
        }
    }
}
