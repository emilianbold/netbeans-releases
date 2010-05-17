/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.BaseKit;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.deprecated.pre61settings.KitchenSink;
import org.openide.options.SystemOption;
import org.openide.util.NbBundle;

/**
* Options for the base editor kit
*
* @author Miloslav Metelka
* @version 1.00
*/
public class OptionSupport extends SystemOption {

    private static final Logger LOG = Logger.getLogger(OptionSupport.class.getName());
    
    static final long serialVersionUID = 2002899758839584077L;

    static final String OPTIONS_PREFIX = "OPTIONS_"; // NOI18N

    private final Class kitClass;
    private final String typeName;
    
    private MimePath mimePath = null;
    private Preferences prefs = null;

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

    /** Get the value of the setting from the <code>Settings</code>
     * @param settingName name of the setting to get.
     */
    public Object getSettingValue(String settingName) {
        return KitchenSink.getValueFromPrefs(settingName, getPreferences(), mimePath);
    }

    /** Get the value of the boolean setting from the <code>Settings</code>
     * @param settingName name of the setting to get.
     */
    protected final boolean getSettingBoolean(String settingName) {
        return getPreferences().getBoolean(settingName, false);
    }

    /** Get the value of the integer setting from the <code>Settings</code>
     * @param settingName name of the setting to get.
     */
    protected final int getSettingInteger(String settingName) {
        return getPreferences().getInt(settingName, 0);
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
        doSetSettingValue(settingName, newValue, propertyName);
    }

    public void doSetSettingValue(String settingName, Object newValue, String propertyName) {
        KitchenSink.setValueToPreferences(settingName, newValue, getPreferences(), mimePath);
    }
    
    /** Enables easier handling of the boolean settings.
     * @param settingName name of the setting to change
     * @param newValue new boolean value of the setting
     * @param propertyName Ignored.
     */
    protected void setSettingBoolean(String settingName, boolean newValue, String propertyName) {
        getPreferences().putBoolean(settingName, newValue);
    }

    /** Enables easier handling of the integer settings.
     * @param settingName name of the setting to change
     * @param newValue new integer value of the setting
     * @param propertyName Ignored.
     */
    protected  void setSettingInteger(String settingName, int newValue, String propertyName) {
        getPreferences().putInt(settingName, newValue);
    }

    /** @return localized string */
    protected String getString(String s) {
        return NbBundle.getMessage(OptionSupport.class, s);
    }
    
    /** Helper method for merging string arrays without searching
     * for the same strings.
     * @param a1 array that will be at the begining of the resulting array
     * @param a2 array that will be at the end of the resulting array
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
    }

    /* package */ Preferences getPreferences() {
        if (prefs == null) {
            mimePath = kitClass.equals(BaseKit.class) ? MimePath.EMPTY : MimePath.parse(getCTImpl());
            prefs = MimeLookup.getLookup(mimePath).lookup(Preferences.class);
        }
        return prefs;
    }

    // ------------------------------------------------------------------------
    // Mime type
    // ------------------------------------------------------------------------
    
    /**
     * <b>ALWAYS OVERWRITE THIS AND PROVIDE VALID MIME TYPE!</b>
     * @return The mime type for this BaseOptions instance.
     */
    protected String getContentType() {
        BaseKit kit = BaseKit.getKit(getKitClass());
        return kit.getContentType();
    }
    
    // diagnostics for #101078
    /* package */ String getCTImpl() {
        String mimeType = getContentType();
        if (mimeType == null) {
            String msg = "Can't determine mime type for " + simpleToString(this) + "; kitClass = " + getKitClass(); //NOI18N
            LOG.log(Level.WARNING, null, new Throwable(msg));
            
            mimeType="text/plain"; //NOI18N
        }
        return mimeType;
    }
    
    private static String simpleToString(Object o) {
        if (o == null) {
            return null;
        } else {
            return o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o)); //NOI18N
        }
    }
    
}
