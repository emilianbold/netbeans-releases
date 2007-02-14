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

package org.netbeans.modules.editor.options;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.editor.Settings;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.options.SystemOption;
import org.openide.util.NbBundle;
import org.netbeans.modules.editor.NbEditorSettingsInitializer;

/**
* Options for the base editor kit
*
* @author Miloslav Metelka
* @version 1.00
*/
public class OptionSupport extends SystemOption {

    static final long serialVersionUID = 2002899758839584077L;

    static final String OPTIONS_PREFIX = "OPTIONS_"; // NOI18N

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
        
        // Hook up the settings initializer
        Settings.Initializer si = getSettingsInitializer();
        Settings.removeInitializer(si.getName());
        Settings.addInitializer(si, Settings.OPTION_LEVEL);
        Settings.reset();
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
        NbEditorSettingsInitializer.init();
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

//        if (propertyName != null) {
            //firePropertyChange(propertyName, oldValue, newValue);[PENDING]
//        }
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
        return NbBundle.getMessage(OptionSupport.class, s);
    }
    
    /** Helper method for merging string arrays without searching
     * for the same strings.
     * @param a1 array that will be at the begining of the resulting array
     * @param a1 array that will be at the end of the resulting array
     */
    public static String[] mergeStringArrays(String[] a1, String[] a2) {
        return NbEditorUtilities.mergeStringArrays(a1, a2);
    }

    /** Get the name of the <code>Settings.Initializer</code> related
     * to these options.
     */
    protected String getSettingsInitializerName() {
        return getTypeName() + "-options-initalizer"; // NOI18N
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
    
    
    class SettingsInitializer implements Settings.Initializer {
        
        String name;
        private volatile boolean updating = false;
        
        public String getName() {
            if (name == null) {
                name = getSettingsInitializerName();
            }
            
            return name;
        }
        
        public void updateSettingsMap(Class kitClass, Map settingsMap) {
            if (!updating) {
                updating = true;
                try {
                    OptionSupport.this.updateSettingsMap(kitClass, settingsMap);
                } finally {
                    updating = false;
                }
            }
        }
        
    }


}
