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

package org.netbeans.modules.form.editors;

import java.beans.*;

/**
 *
 * @author Tran Duc Trung
 */

public class EnumEditor extends PropertyEditorSupport
{
    private Object[] enumerationValues;

    public EnumEditor(Object[] enumerationValues) {
        this.enumerationValues = enumerationValues;
    }

    public String[] getTags() {
        String[] tags = new String[enumerationValues.length / 3];
        for (int i = 0; i < enumerationValues.length / 3; i++) {
            tags[i] = (String) enumerationValues[i * 3];
        }
        return tags;
    }

    public void setAsText(String t) {
        for (int i = 0; i < enumerationValues.length / 3; i++) {
            if (t.equals(enumerationValues[i * 3].toString())) {
                setValue(enumerationValues[i * 3 + 1]);
                break;
            }
        }
    }

    public String getAsText() {
        Object value = getValue();
        for (int i = 0; i < enumerationValues.length / 3; i++) {
            if (enumerationValues[i * 3 + 1].equals(value))
                return enumerationValues[i * 3].toString();
        }
        return enumerationValues.length > 0 ? enumerationValues[0].toString() : null;
    }

    public String getJavaInitializationString() {
        String initString = null;
        
        Object value = getValue();
        for (int i = 0; i < enumerationValues.length / 3; i++) {
            if (enumerationValues[i * 3 + 1].equals(value)) {
                initString = (String) enumerationValues[i * 3 + 2];
                break;
            }
        }
        if (initString == null) {
            initString = enumerationValues.length > 2 ?
                (String) enumerationValues[2] : null;
        }
        if (initString == null)
            return null;
        for (int i = 0; i < swingclassnames.length; i++) {
            if (initString.startsWith(swingclassnames[i] + ".")) {
                initString = "javax.swing." + initString; // NOI18N
                break;
            }
        }
        return initString;
    }

    private static String[] swingclassnames = new String[] {
        "SwingConstants",       // NOI18N
        "DebugGraphics",        // NOI18N
        "JDesktopPane",         // NOI18N
        "JFileChooser",         // NOI18N
        "WindowConstants",      // NOI18N
        "ListSelectionModel",   // NOI18N
        "JScrollBar",           // NOI18N
        "JScrollPane",          // NOI18N
        "Slider",               // NOI18N
        "JSplitPane",           // NOI18N
        "JTabbedPane",          // NOI18N
        "JTable",               // NOI18N
        "JTextField",           // NOI18N
        "JViewport",            // NOI18N
        "JFrame",               // NOI18N        
    };
}
