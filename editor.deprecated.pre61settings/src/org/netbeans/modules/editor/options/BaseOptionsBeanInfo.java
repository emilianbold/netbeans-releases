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

import java.beans.*;
import java.awt.Image;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;

import org.openide.util.NbBundle;

import org.netbeans.editor.BaseCaret;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/** BeanInfo for base options
*
* @author Miloslav Metelka, Ales Novak
*/
public class BaseOptionsBeanInfo extends SimpleBeanInfo {

    /** Prefix of the icon location. */
    private String iconPrefix;

    /** Prefix for getting localized strings for property name and hint */
    private String bundlePrefix;

    /** Icons for compiler settings objects. */
    private Image icon;
    private Image icon32;

    private HashMap names2PD;

    /** Propertydescriptors */
    PropertyDescriptor[] descriptors;

    private static final String[] EXPERT_PROP_NAMES = new String[] {
                BaseOptions.CARET_BLINK_RATE_PROP,
                BaseOptions.CARET_TYPE_INSERT_MODE_PROP,
                BaseOptions.CARET_TYPE_OVERWRITE_MODE_PROP,
                BaseOptions.CARET_COLOR_INSERT_MODE_PROP,
                BaseOptions.CARET_COLOR_OVERWRITE_MODE_PROP,
                BaseOptions.HIGHLIGHT_CARET_ROW_PROP,
                BaseOptions.HIGHLIGHT_MATCHING_BRACKET_PROP,
                BaseOptions.LINE_HEIGHT_CORRECTION_PROP,
                BaseOptions.MARGIN_PROP,
                BaseOptions.SCROLL_JUMP_INSETS_PROP,
                BaseOptions.SCROLL_FIND_INSETS_PROP,
                BaseOptions.STATUS_BAR_CARET_DELAY_PROP,
                BaseOptions.STATUS_BAR_VISIBLE_PROP,
                BaseOptions.TEXT_LIMIT_LINE_COLOR_PROP,
                BaseOptions.TEXT_LIMIT_LINE_VISIBLE_PROP,
                BaseOptions.TEXT_LIMIT_WIDTH_PROP,
                BaseOptions.TEXT_ANTIALIASING_PROP
            };


    public BaseOptionsBeanInfo() {
        this("/org/netbeans/modules/editor/resources/baseOptions"); // NOI18N
    }

    public BaseOptionsBeanInfo(String iconPrefix) {
        this(iconPrefix, ""); // NOI18N
    }

    public BaseOptionsBeanInfo(String iconPrefix, String bundlePrefix) {
        this.iconPrefix = iconPrefix;
        this.bundlePrefix = bundlePrefix;
    }

    /*
    * @return Returns an array of PropertyDescriptors
    * describing the editable properties supported by this bean.
    */
    public @Override PropertyDescriptor[] getPropertyDescriptors () {
        if (descriptors == null) {
            String[] propNames = getPropNames();
            PropertyDescriptor[] pds = new PropertyDescriptor[propNames.length];

            for (int i = 0; i < propNames.length; i++) {
                pds[i] = createPropertyDescriptor(propNames[i]);
                // Set display-name and short-description
                pds[i].setDisplayName(getString("PROP_" + bundlePrefix + propNames[i])); // NOI18N
                pds[i].setShortDescription(getString("HINT_" + bundlePrefix + propNames[i])); // NOI18N
            }

            descriptors = pds; // now the array are inited

            // Now various properties of the descriptors can be updated
            updatePropertyDescriptors();
        }
        return descriptors;
    }

    /** Create property descriptor for a particular property-name. */
    protected PropertyDescriptor createPropertyDescriptor(String propName) {
        PropertyDescriptor pd;
        try {
            pd = new PropertyDescriptor(propName, getBeanClass());

        } catch (IntrospectionException e) {
            try {
                // Create property without read/write methods
                pd = new PropertyDescriptor(propName, null, null);
            } catch (IntrospectionException e2) {
                throw new IllegalStateException("Invalid property name=" + propName); // NOI18N
            }

            // Try a simple search for get/set methods - just by name
            // Successor can customize it if necessary
            String cap = capitalize(propName);
            Method m = findMethod("get" + cap); // NOI18N
            if (m != null) {
                try {
                    pd.setReadMethod(m);
                } catch (IntrospectionException e2) {
                }
            }
            m = findMethod("set" + cap); // NOI18N
            if (m != null) {
                try {
                    pd.setWriteMethod(m);
                } catch (IntrospectionException e2) {
                }
            }
        }

        return pd;
    }

    private Method findMethod(String name) {
        try {
            Method[] ma = getBeanClass().getDeclaredMethods();
            for (int i = 0; i < ma.length; i++) {
                if (name.equals(ma[i].getName())) {
                    return ma[i];
                }
            }
        } catch (SecurityException e) {
        }
        return null;
    }

    private static String capitalize(String s) {
	if (s.length() == 0) {
 	    return s;
	}
	char chars[] = s.toCharArray();
	chars[0] = Character.toUpperCase(chars[0]);
	return new String(chars);
    }

    /** Update various properties of the property descriptors. */
    protected void updatePropertyDescriptors() {
        setPropertyEditor(BaseOptions.ABBREV_MAP_PROP, AbbrevsEditor.class, false);
        setPropertyEditor(BaseOptions.CARET_TYPE_INSERT_MODE_PROP, CaretTypeEditor.class);
        setPropertyEditor(BaseOptions.CARET_TYPE_OVERWRITE_MODE_PROP, CaretTypeEditor.class);
        setPropertyEditor(BaseOptions.KEY_BINDING_LIST_PROP, KeyBindingsEditor.class, false);
        setPropertyEditor(BaseOptions.COLORING_MAP_PROP, ColoringArrayEditor.class, false);
        setPropertyEditor(BaseOptions.SCROLL_JUMP_INSETS_PROP, ScrollInsetsEditor.class);
        setPropertyEditor(BaseOptions.SCROLL_FIND_INSETS_PROP, ScrollInsetsEditor.class);
        setPropertyEditor(BaseOptions.MACRO_MAP_PROP, MacrosEditor.class, false);
        
        setExpert(EXPERT_PROP_NAMES);
        boolean usesNewOptions = usesNewOptions();
        
        String hidden[] = (usesNewOptions) ?
                new String[] {
                    BaseOptions.ABBREV_MAP_PROP,
                    BaseOptions.CARET_BLINK_RATE_PROP,
                    BaseOptions.CARET_COLOR_INSERT_MODE_PROP,
                    BaseOptions.CARET_COLOR_OVERWRITE_MODE_PROP,
                    BaseOptions.CARET_ITALIC_INSERT_MODE_PROP,
                    BaseOptions.CARET_ITALIC_OVERWRITE_MODE_PROP,
                    BaseOptions.CARET_TYPE_INSERT_MODE_PROP,
                    BaseOptions.CARET_TYPE_OVERWRITE_MODE_PROP,
                    BaseOptions.CODE_FOLDING_PROPS_PROP,
                    BaseOptions.COLORING_MAP_PROP,
                    BaseOptions.EXPAND_TABS_PROP,
                    BaseOptions.FONT_SIZE_PROP,
                    BaseOptions.HIGHLIGHT_CARET_ROW_PROP,
                    BaseOptions.HIGHLIGHT_MATCHING_BRACKET_PROP,
                    BaseOptions.KEY_BINDING_LIST_PROP,
                    BaseOptions.MACRO_MAP_PROP,
                    BaseOptions.MARGIN_PROP,
                    BaseOptions.OPTIONS_VERSION_PROP,
                    BaseOptions.PAIR_CHARACTERS_COMPLETION,
                    BaseOptions.SCROLL_FIND_INSETS_PROP,
                    BaseOptions.SCROLL_JUMP_INSETS_PROP,
                    BaseOptions.SPACES_PER_TAB_PROP,
                    BaseOptions.TAB_SIZE_PROP,
                    BaseOptions.TEXT_LIMIT_LINE_COLOR_PROP,
                    BaseOptions.TEXT_LIMIT_LINE_VISIBLE_PROP,
                    BaseOptions.TEXT_LIMIT_WIDTH_PROP,
                } :
                new String[] {
                    BaseOptions.CARET_ITALIC_INSERT_MODE_PROP,
                    BaseOptions.CARET_ITALIC_OVERWRITE_MODE_PROP,
                    BaseOptions.EXPAND_TABS_PROP,
                    BaseOptions.HIGHLIGHT_CARET_ROW_PROP,
                    BaseOptions.HIGHLIGHT_MATCHING_BRACKET_PROP,
                    BaseOptions.MARGIN_PROP,
                    BaseOptions.OPTIONS_VERSION_PROP,
                    BaseOptions.SCROLL_FIND_INSETS_PROP,
                    BaseOptions.SCROLL_JUMP_INSETS_PROP,
                    BaseOptions.SPACES_PER_TAB_PROP,
                } ;
        
        setHidden(hidden);

    }

    protected boolean usesNewOptions() {
        Collection<? extends ModuleInfo> infos = Lookup.getDefault().lookupAll(ModuleInfo.class);
        for(ModuleInfo mi : infos) {
            if (mi.getCodeNameBase().startsWith("org.netbeans.modules.options.editor") && //NOI18N
                mi.isEnabled()
            ) {
                return true;
            }
        }
        return false;
    }

    protected Class getBeanClass() {
        return BaseOptions.class;
    }

    protected String[] getPropNames() {
        return BaseOptions.BASE_PROP_NAMES;
    }

    protected synchronized PropertyDescriptor getPD(String propName) {
        if (names2PD == null) {
            names2PD = new HashMap(37);
            PropertyDescriptor[] pds = getPropertyDescriptors();
            for (int i = pds.length - 1; i >= 0; i--) {
                names2PD.put(pds[i].getName(), pds[i]);
            }
        }
        return (PropertyDescriptor)names2PD.get(propName);
    }
    
    protected void setPropertyEditor(String propName, Class propEditor, boolean canEditAsText) {
        PropertyDescriptor pd = getPD(propName);
        if (pd != null) {
            pd.setPropertyEditorClass(propEditor);
            pd.setValue("canEditAsText", canEditAsText ? Boolean.TRUE : Boolean.FALSE); //NOI18N
        }
    }

    protected void setPropertyEditor(String propName, Class propEditor) {
        setPropertyEditor(propName, propEditor, true);
    }

    protected void setExpert(String[] propNames) {
        for (int i = 0; i < propNames.length; i++) {
            PropertyDescriptor pd = getPD(propNames[i]);
            if (pd != null) {
                pd.setExpert(true);
            }
        }
    }

    protected void setHidden(String[] propNames) {
        for (int i = 0; i < propNames.length; i++) {
            PropertyDescriptor pd = getPD(propNames[i]);
            if (pd != null) {
                pd.setHidden(true);
            }
        }
    }

    /* @param type Desired type of the icon
    * @return returns the Java loader's icon
    */
    public @Override Image getIcon(final int type) {
        if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
            if (icon == null)
                icon = loadImage(iconPrefix + ".gif"); // NOI18N
            return icon;
        }
        else {
            if (icon32 == null)
                icon32 = loadImage(iconPrefix + "32.gif"); // NOI18N
            return icon32;
        }
    }

    /**
     * Get localized string for the given key.
     *
     * @param key the key string for which the localized
     *  text is being retrieved. The localized string
     *  for the key must exist otherwise
     *  {@link java.util.MissingResourceException}
     *  gets thrown.
     * @return localized string
     */
    protected String getString(String key) {
        return NbBundle.getMessage(BaseOptionsBeanInfo.class, key);
    }

    // ------------------------ carets --------------------------------

    public static class CaretTypeEditor extends PropertyEditorSupport {

        private static String[] tags = new String[] {
                                           BaseCaret.LINE_CARET,
                                           BaseCaret.THIN_LINE_CARET,
                                           BaseCaret.BLOCK_CARET
                                       };

        private static String[] locTags = new String[] {
                                              getString("LINE_CARET"), // NOI18N
                                              getString("THIN_LINE_CARET"), // NOI18N
                                              getString("BLOCK_CARET") // NOI18N
                                          };

        public @Override String[] getTags() {
            return locTags;
        }

        public @Override void setAsText(String txt) {
            for (int i = 0; i < locTags.length; i++) {
                if (locTags[i].equals(txt)) {
                    setValue(tags[i]);
                    break;
                }
            }
        }

        public @Override String getAsText() {
            String val = (String) getValue();
            for (int i = 0; i < tags.length; i++) {
                if (tags[i].equals(val)) {
                    return locTags[i];
                }
            }
            throw new IllegalStateException();
        }

        static String getString(String s) {
            return NbBundle.getMessage(BaseOptionsBeanInfo.class, s);
        }

    }
}
