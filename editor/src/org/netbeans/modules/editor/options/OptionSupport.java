/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.editor.options;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Settings;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.options.SystemOption;
import org.openide.util.NbBundle;
import org.netbeans.modules.editor.NbEditorSettingsInitializer;
import org.netbeans.modules.editor.lib.KitsTracker;

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

    private String mimeType;
    
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
        
        // Hook up the settings initializer. This must not happen before
        // subclasses finish their initialization.
        Settings.update(new Runnable() {
            public void run() {
                Settings.Initializer si = getSettingsInitializer();
                Settings.removeInitializer(si.getName());
                Settings.addInitializer(si, Settings.OPTION_LEVEL);
                Settings.reset();
            }
            
            public boolean asynchronous() {
                return true;
            }
            
            public int delay() {
                return 10;
            }
        });
    }

    public Class getKitClass() {
        return kitClass;
    }

    public String getTypeName() {
        return typeName;
    }
    
    /**
     * <b>ALWAYS OVERWRITE THIS AND PROVIDE VALID MIME TYPE!</b>
     * @return The mime type for this BaseOptions instance.
     */
    protected String getContentType() {
        if (mimeType == null) {
            for(String s : KitsTracker.getInstance().getMimeTypes()) {
                if (s.toLowerCase().contains(typeName.toLowerCase())) {
                    mimeType = s;
                    break;
                }
            }
        }
        
        if (mimeType == null) {
            BaseKit kit = BaseKit.getKit(getKitClass());
            mimeType = kit.getContentType();
        }
        
        return mimeType;
    }
    
    public static String getTypeName(Class kitClass) {
        return (String)kitClass2Type.get(kitClass);
    }

    public String displayName() {
        return getString(OPTIONS_PREFIX + typeName);
    }

    Settings.KitAndValue[] getSettingValueHierarchy(String settingName) {
        boolean reset = setContextMimeType();
        try {
            return Settings.getValueHierarchy(kitClass, settingName);
        } finally {
            if (reset) {
                resetContextMimeType();
            }
        }
    }

    /** Get the value of the setting from the <code>Settings</code>
     * @param settingName name of the setting to get.
     */
    public Object getSettingValue(String settingName) {
        NbEditorSettingsInitializer.init();
        boolean reset = setContextMimeType();
        try {
            return Settings.getValue(kitClass, settingName);
        } finally {
            if (reset) {
                resetContextMimeType();
            }
        }
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
     * @param propertyName Ignored.
     */
    public void setSettingValue(String settingName, Object newValue, String propertyName) {
        
        initializerValuesMap.put(settingName, newValue);

        Object oldValue = getSettingValue(settingName);
        if ((oldValue == null && newValue == null)
                || (oldValue != null && oldValue.equals(newValue))
           ) {
            return; // no change
        }

        boolean reset = setContextMimeType();
        try {
            Settings.setValue(kitClass, settingName, newValue);
        } finally {
            if (reset) {
                resetContextMimeType();
            }
        }
    }

    
    public void doSetSettingValue(String settingName, Object newValue, String propertyName) {
        initializerValuesMap.put(settingName, newValue);
        
        boolean reset = setContextMimeType();
        try {
            Settings.setValue(kitClass, settingName, newValue);
        } finally {
            if (reset) {
                resetContextMimeType();
            }
        }
    }
    
    
    /** Enables easier handling of the boolean settings.
     * @param settingName name of the setting to change
     * @param newValue new boolean value of the setting
     * @param propertyName Ignored.
     */
    protected void setSettingBoolean(String settingName, boolean newValue, String propertyName) {
        setSettingValue(settingName, newValue ? Boolean.TRUE : Boolean.FALSE, propertyName);
    }

    /** Enables easier handling of the integer settings.
     * @param settingName name of the setting to change
     * @param newValue new integer value of the setting
     * @param propertyName Ignored.
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
        public String getName() {
            if (name == null) {
                name = getSettingsInitializerName();
            }
            
            return name;
        }
        
        public void updateSettingsMap(Class kitClass, Map settingsMap) {
            OptionSupport.this.updateSettingsMap(kitClass, settingsMap);
        }

        public @Override String toString() {
            return super.toString() + "[" + getName(); //NOI18N
        }
        
    } // End of SettingsInitializer class

    /* package */ boolean setContextMimeType() {
        String ctx = getContentType();
        if (ctx != null && ctx.length() > 0 && 
            getClass() != BaseOptions.class && getClass() != OptionSupport.class)
        {
            KitsTracker.getInstance().setContextMimeType(ctx);
            return true;
        } else {
            return false;
        }
    }

    /* package */ void resetContextMimeType() {
        KitsTracker.getInstance().setContextMimeType(null);
    }
}
