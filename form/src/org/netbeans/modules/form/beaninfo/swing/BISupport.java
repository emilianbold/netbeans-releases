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

package org.netbeans.modules.form.beaninfo.swing;

import java.awt.Image;
import java.beans.*;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * A class that provides some support for simplier BeanInfos in this package.
 *
 * @author petr.nejedly@sun.com
 */
abstract class BISupport extends SimpleBeanInfo {
   
    private PropertyDescriptor[] pds;
    private String icon;
    private Class beanClass;
    
    protected BISupport(String iconBaseName, Class beanClass) {
        icon = iconBaseName;
        this.beanClass = beanClass;
    }

    public BeanDescriptor getBeanDescriptor() {
        return new BeanDescriptor(beanClass);
    }

    public synchronized PropertyDescriptor[] getPropertyDescriptors() {
        if (pds == null) {
            try {
                pds = createPropertyDescriptors();
            } catch (IntrospectionException e) {
                pds = super.getPropertyDescriptors();
            }
        }
        return pds;
    }

    
    public Image getIcon(int type) {
        if (type == ICON_COLOR_32x32 || type == ICON_MONO_32x32)
            return Utilities.loadImage("org/netbeans/modules/form/beaninfo/swing/" + icon + "32.gif"); // NOI18N
        else
            return Utilities.loadImage("org/netbeans/modules/form/beaninfo/swing/" + icon + ".gif"); // NOI18N
    }

    protected PropertyDescriptor[] createPropertyDescriptors() throws IntrospectionException {
        return new PropertyDescriptor[0];
    }
    
    protected String getString(String key) {
        return NbBundle.getMessage(BISupport.class, key);
    }
    
    protected PropertyDescriptor createRW( Class beanClass, String name)
                                            throws IntrospectionException {
        String title = Character.toUpperCase(name.charAt(0)) + name.substring(1);

        PropertyDescriptor pd = new PropertyDescriptor(name, beanClass,
                "get" + title, "set" + title ); // NOI18N
        
        setProps(pd, beanClass, title);
        return pd;
    }

    protected PropertyDescriptor createRO( Class beanClass, String name)
                                            throws IntrospectionException {
        String title = Character.toUpperCase(name.charAt(0)) + name.substring(1);

        PropertyDescriptor pd = new PropertyDescriptor(name, beanClass,
                "get" + title, null); // NOI18N
        
        setProps(pd, beanClass, title);
        return pd;
    }

    private void setProps( PropertyDescriptor pd, Class beanClass, String title ) {
        String className = beanClass.getName();
        int dotIdx = className.lastIndexOf('.');
        className = dotIdx > 0 ? className.substring(dotIdx+1) : className;
        pd.setDisplayName(getString("PROP_" + className + '.' + title)); // NOI18N
        pd.setShortDescription(getString("HINT_" + className + '.' + title)); // NOI18N
    }



    static class TaggedPropertyEditor extends PropertyEditorSupport {
        private String[] tags;
        private int[] values;
        private String[] javaInitStrings;
        private String[] tagKeys;

        protected TaggedPropertyEditor( int[] values, String[] javaInitStrings, String[] tagKeys ) {
            this.values = values;
            this.javaInitStrings = javaInitStrings;
            this.tagKeys = tagKeys;
        }
        
        public String[] getTags() {
            if (tags == null) createTags();
            return tags;
        }

        public String getAsText() {
            Object valObj = getValue();
            if (valObj instanceof Integer) {
                if (tags == null) createTags();

                int value = ((Integer)valObj).intValue();
                for (int i = 0; i < values.length; i++)
                    if (value == values[i])
                        return tags[i];
            }
            return null;
        }

        public void setAsText(String str) {
            if (tags == null) createTags();

            int value = -1;

            for (int i = 0; i < tags.length; i++) {
                if (str.equals(tags[i])) {
                    value = values[i];
                    break;
                }
            }

            if (value != -1)
                setValue(new Integer(value));
        }

        public String getJavaInitializationString() {
            Object valObj = getValue();
            if (valObj instanceof Integer) {
                int value = ((Integer)valObj).intValue();
                for (int i = 0; i < values.length; i++)
                    if (value == values[i])
                        return javaInitStrings[i];
            }
            return "???"; // NOI18N
        }

        private void createTags() {
            tags = new String[tagKeys.length];
            ResourceBundle bundle = NbBundle.getBundle(BISupport.class);
            for (int i=0; i<tagKeys.length; i++) {
                tags[i] = bundle.getString(tagKeys[i]);
            }
        }
    }
}
