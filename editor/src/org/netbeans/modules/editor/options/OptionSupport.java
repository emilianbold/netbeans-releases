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

package org.netbeans.modules.editor.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ResourceBundle;
import java.util.HashMap;
import java.util.Map;

import org.netbeans.editor.Settings;
import org.netbeans.editor.Coloring;
import org.netbeans.modules.editor.NbEditorUtilities;

import org.openide.options.SystemOption;
import org.openide.util.NbBundle;
import java.io.IOException;
import java.io.ObjectOutput;
import org.netbeans.editor.SettingsNames;
import java.util.List;

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

    
    private HashMap initializerValuesMap;
    
    private transient SettingsInitializer settingsInitializer;
    
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
        initializerValuesMap = new HashMap();
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

    Settings.KitAndValue[] getSettingValueHierarchy(String settingName) {
        return Settings.getValueHierarchy(kitClass, settingName);
    }

    /** Get the value of the setting from the <code>Settings</code>
     * @param settingName name of the setting to get.
     */
    public Object getSettingValue(String settingName) {
        return Settings.getValue(kitClass, settingName);
    }

    /** Get the value of the boolean setting from the <code>Settings</code>
     * @param settingName name of the setting to get.
     */
    protected final boolean getSettingBoolean(String settingName) {
        Boolean val = (Boolean)getSettingValue(settingName);
        return (val != null) ? val.booleanValue() : false;
    }

    /** Get the value of the integer setting from the <code>Settings</code>
     * @param settingName name of the setting to get.
     */
    protected final int getSettingInteger(String settingName) {
        Integer val = (Integer)getSettingValue(settingName);
        return (val != null) ? val.intValue() : 0;
    }

    /** Can be used when the settingName is the same as the propertyName */
    public void setSettingValue(String settingName, Object newValue) {
        setSettingValue(settingName, newValue, settingName);
    }
    
    /** Set the value into the <code>Settings</code> and optionally 
     * fire the property change.
     * @param settingName name of the setting to change
     * @param newValue new value of the setting
     * @param propertyName if non-null it means that the property change
     *  should be fired if the newValue is differernt from the old one.
     *  Firing is performed using the given property name. Nothing is fired
     *  when it's set to null.
     */
    public void setSettingValue(String settingName, Object newValue,
    String propertyName) {
        
        initializerValuesMap.put(settingName, newValue);

        Object oldValue = getSettingValue(settingName);
        if ((oldValue == null && newValue == null)
                || (oldValue != null && oldValue.equals(newValue))
           ) {
            return; // no change
        }

        Settings.setValue(kitClass, settingName, newValue);

        if (propertyName != null) {
            //firePropertyChange(propertyName, oldValue, newValue);[PENDING]
        }
    }

    
    
    public void doSetSettingValue(String settingName, Object newValue,
    String propertyName) {
        initializerValuesMap.put(settingName, newValue);
        Settings.setValue(kitClass, settingName, newValue);
    }
    
    
    /** Enables easier handling of the boolean settings.
     * @param settingName name of the setting to change
     * @param newValue new boolean value of the setting
     * @param propertyName if non-null it means that the property change
     *  should be fired if the newValue is differernt from the old one.
     *  Firing is performed using the given property name. Nothing is fired
     *  when it's set to null.
     */
    protected void setSettingBoolean(String settingName, boolean newValue, String propertyName) {
        setSettingValue(settingName, newValue ? Boolean.TRUE : Boolean.FALSE, propertyName);
    }

    /** Enables easier handling of the integer settings.
     * @param settingName name of the setting to change
     * @param newValue new integer value of the setting
     * @param propertyName if non-null it means that the property change
     *  should be fired if the newValue is differernt from the old one.
     *  Firing is performed using the given property name. Nothing is fired
     *  when it's set to null.
     */
    protected  void setSettingInteger(String settingName, int newValue, String propertyName) {
        setSettingValue(settingName, new Integer(newValue));
    }

    /** @return localized string */
    protected String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(OptionSupport.class);
        }
        return bundle.getString(s);
    }
    
    /** Helper method for merging string arrays without searching
     * for the same strings.
     * @param a1 array that will be at the begining of the resulting array
     * @param a1 array that will be at the end of the resulting array
     */
    public static String[] mergeStringArrays(String[] a1, String[] a2) {
        return NbEditorUtilities.mergeStringArrays(a1, a2);
    }

    /** Editor options are global therefore they return true
     * from this method.
     */
    public boolean isGlobal() {
        return true;
    }
    
    /** Get the name of the <code>Settings.Initializer</code> related
     * to these options.
     */
    protected String getSettingsInitializerName() {
        return getTypeName() + "-options-initalizer";
    }

    protected void updateSettingsMap(Class kitClass, Map settingsMap) {
        if (kitClass == getKitClass()) {
            settingsMap.putAll(initializerValuesMap);
        }
    }
    
    Settings.Initializer getSettingsInitializer() {
        if (settingsInitializer == null) {
            settingsInitializer = new SettingsInitializer();
        }
        return settingsInitializer;
    }
    
    public void writeExternal() throws IOException{
    }
    
    public void writeExternal(ObjectOutput out) throws IOException{
    }

    
    class SettingsInitializer implements Settings.Initializer {
        
        String name;
        
        public String getName() {
            if (name == null) {
                name = getSettingsInitializerName();
            }
            
            return name;
        }
        
        public void updateSettingsMap(Class kitClass, Map settingsMap) {
            OptionSupport.this.updateSettingsMap(kitClass, settingsMap);
        }
        
    }


}
