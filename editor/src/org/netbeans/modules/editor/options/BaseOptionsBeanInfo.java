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

import java.beans.*;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.lang.reflect.Method;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Iterator;

import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent.KeyBinding;

import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import org.netbeans.editor.BaseCaret;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.MultiKeyBinding;

/** BeanInfo for base options
*
* @author Miloslav Metelka, Ales Novak
*/
public class BaseOptionsBeanInfo extends SimpleBeanInfo {

    private ResourceBundle bundle;

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
                BaseOptions.CARET_ITALIC_INSERT_MODE_PROP,
                BaseOptions.CARET_ITALIC_OVERWRITE_MODE_PROP,
                BaseOptions.CARET_TYPE_INSERT_MODE_PROP,
                BaseOptions.CARET_TYPE_OVERWRITE_MODE_PROP,
                BaseOptions.CARET_COLOR_INSERT_MODE_PROP,
                BaseOptions.CARET_COLOR_OVERWRITE_MODE_PROP,
                BaseOptions.HIGHLIGHT_CARET_ROW_PROP,
                BaseOptions.HIGHLIGHT_MATCHING_BRACKET_PROP,
                BaseOptions.LINE_HEIGHT_CORRECTION_PROP,
                BaseOptions.LINE_NUMBER_MARGIN_PROP,
                BaseOptions.MARGIN_PROP,
                BaseOptions.SCROLL_JUMP_INSETS_PROP,
                BaseOptions.SCROLL_FIND_INSETS_PROP,
                BaseOptions.STATUS_BAR_CARET_DELAY_PROP,
                BaseOptions.STATUS_BAR_VISIBLE_PROP,
                BaseOptions.TEXT_LIMIT_LINE_COLOR_PROP,
                BaseOptions.TEXT_LIMIT_LINE_VISIBLE_PROP,
                BaseOptions.TEXT_LIMIT_WIDTH_PROP,
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
    public PropertyDescriptor[] getPropertyDescriptors () {
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
                throw new IllegalStateException("Invalid property name=" + propName);
            }

            // Try a simple search for get/set methods - just by name
            // Successor can customize it if necessary
            String cap = capitalize(propName);
            Method m = findMethod("get" + cap);
            if (m != null) {
                try {
                    pd.setReadMethod(m);
                } catch (IntrospectionException e2) {
                }
            }
            m = findMethod("set" + cap);
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
        setPropertyEditor(BaseOptions.ABBREV_MAP_PROP, AbbrevsEditor.class);
        setPropertyEditor(BaseOptions.CARET_TYPE_INSERT_MODE_PROP, CaretTypeEditor.class);
        setPropertyEditor(BaseOptions.CARET_TYPE_OVERWRITE_MODE_PROP, CaretTypeEditor.class);
        setPropertyEditor(BaseOptions.KEY_BINDING_LIST_PROP, KeyBindingsEditor.class);
        setPropertyEditor(BaseOptions.COLORING_MAP_PROP, ColoringArrayEditor.class);
        setPropertyEditor(BaseOptions.SCROLL_JUMP_INSETS_PROP, ScrollInsetsEditor.class);
        setPropertyEditor(BaseOptions.SCROLL_FIND_INSETS_PROP, ScrollInsetsEditor.class);
        setPropertyEditor(BaseOptions.MACRO_MAP_PROP, MacrosEditor.class);

        setExpert(EXPERT_PROP_NAMES);

        setHidden(new String[] {
            BaseOptions.EXPAND_TABS_PROP,
            BaseOptions.SPACES_PER_TAB_PROP,
            BaseOptions.OPTIONS_VERSION_PROP
        });

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

    protected void setPropertyEditor(String propName, Class propEditor) {
        PropertyDescriptor pd = getPD(propName);
        if (pd != null) {
            pd.setPropertyEditorClass(propEditor);
        }
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
    public Image getIcon(final int type) {
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

    /** @return localized string */
    protected String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(BaseOptionsBeanInfo.class);
        }
        return bundle.getString(s);
    }

    // ------------------------ carets --------------------------------

    public static class CaretTypeEditor extends PropertyEditorSupport {

        private static ResourceBundle bundle;

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

        public String[] getTags() {
            return locTags;
        }

        public void setAsText(String txt) {
            for (int i = 0; i < locTags.length; i++) {
                if (locTags[i].equals(txt)) {
                    setValue(tags[i]);
                    break;
                }
            }
        }

        public String getAsText() {
            String val = (String) getValue();
            for (int i = 0; i < tags.length; i++) {
                if (tags[i].equals(val)) {
                    return locTags[i];
                }
            }
            throw new IllegalStateException();
        }

        static String getString(String s) {
            if (bundle == null) {
                bundle = NbBundle.getBundle(BaseOptionsBeanInfo.class);
            }
            return bundle.getString(s);
        }

    }
}
