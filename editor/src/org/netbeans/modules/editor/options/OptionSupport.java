/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ResourceBundle;
import java.util.HashMap;

import org.netbeans.editor.Settings;
import org.netbeans.editor.Coloring;

import org.openide.options.SystemOption;
import org.openide.util.NbBundle;

/**
* Options for the base editor kit
*
* @author Miloslav Metelka
* @version 1.00
*/
public class OptionSupport extends SystemOption {

    static final long serialVersionUID = 2002899758839584077L;

    static final String OPTIONS_PREFIX = "OPTIONS_"; // NOI18N

    private static ResourceBundle bundle;


    private Class kitClass;

    private String typeName;

    private PropertyChangeListener settingsListener;

    private static final HashMap kitClass2Type = new HashMap();

    /** Construct new option support. The pair [kitClass, typeName]
    * is put into a map so it's possible to find a typeName when kitClass is known
    * through <tt>getTypeName()</tt> static method.
    * @param kitClass class of the editorr kit for which this support is constructed.
    * @param typeName name 
    */
    public OptionSupport(Class kitClass, String typeName) {
        this.kitClass = kitClass;
        this.typeName = typeName;
        kitClass2Type.put(kitClass, typeName);
    }

    public Class getKitClass() {
        return kitClass;
    }

    public String getTypeName() {
        return typeName;
    }

    public static String getTypeName(Class kitClass) {
        return (String)kitClass2Type.get(kitClass);
    }

    public String displayName() {
        return getString(OPTIONS_PREFIX + typeName);
    }

    public void setSettingValue(String settingName, Object newValue) {
        Object oldValue = getSettingValue(settingName);
        if ((oldValue == null && newValue == null)
                || (oldValue != null && oldValue.equals(newValue))
           ) {
            return; // no change
        }

        Settings.setValue(kitClass, settingName, newValue);
        firePropertyChange(settingName, oldValue, newValue);
    }

    public Object getSettingValue(String settingName) {
        return Settings.getValue(kitClass, settingName);
    }

    Settings.KitAndValue[] getSettingValueHierarchy(String settingName) {
        return Settings.getValueHierarchy(kitClass, settingName);
    }

    boolean getSettingBoolean(String settingName) {
        Boolean val = (Boolean)getSettingValue(settingName);
        return (val != null) ? val.booleanValue() : false;
    }

    void setSettingBoolean(String settingName, boolean newValue) {
        setSettingValue(settingName, newValue ? Boolean.TRUE : Boolean.FALSE);
    }

    int getSettingInteger(String settingName) {
        Integer val = (Integer)getSettingValue(settingName);
        return (val != null) ? val.intValue() : 0;
    }

    void setSettingInteger(String settingName, int newValue) {
        setSettingValue(settingName, new Integer(newValue));
    }

    /** @return localized string */
    protected String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(OptionSupport.class);
        }
        return bundle.getString(s);
    }

    public static String[] mergeStringArrays(String[] a1, String[] a2) {
        String[] ret = new String[a1.length + a2.length];
        for (int i = 0; i < a1.length; i++) {
            ret[i] = a1[i];
        }
        for (int i = 0; i < a2.length; i++) {
            ret[a1.length + i] = a2[i];
        }
        return ret;
    }

    public boolean isGlobal() {
        return true;
    }

}

/*
 * Log
 *  13   Jaga      1.11.1.0    3/15/00  Miloslav Metelka Structural change
 *  12   Gandalf   1.11        1/15/00  Miloslav Metelka now global
 *  11   Gandalf   1.10        1/13/00  Miloslav Metelka Localization
 *  10   Gandalf   1.9         12/28/99 Miloslav Metelka 
 *  9    Gandalf   1.8         11/11/99 Miloslav Metelka SVUID explicitly 
 *       specified
 *  8    Gandalf   1.7         11/11/99 Miloslav Metelka 
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         9/15/99  Miloslav Metelka 
 *  5    Gandalf   1.4         8/27/99  Miloslav Metelka 
 *  4    Gandalf   1.3         8/17/99  Miloslav Metelka 
 *  3    Gandalf   1.2         7/21/99  Miloslav Metelka 
 *  2    Gandalf   1.1         7/21/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/20/99  Miloslav Metelka 
 * $
 */
