/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.beans;

import java.awt.Image;
import java.beans.*;

import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** Description of {@link PropertyActionSettings}.
 *
 * @author Petr Suchomel
 */
public class PropertyActionSettingsBeanInfo extends SimpleBeanInfo {
    
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor descr = new BeanDescriptor(org.netbeans.modules.beans.PropertyActionSettings.class);
        descr.setDisplayName(NbBundle.getBundle(PropertyActionSettingsBeanInfo.class).getString("PROP_Option_Menu"));
        descr.setValue ("version", "1.1"); // NOI18N            
        descr.setValue("global", Boolean.FALSE); // NOI18N
        return descr;
    }

    public PropertyDescriptor[] getPropertyDescriptors () {
        try {                
            PropertyDescriptor propstyle = new PropertyDescriptor("propStyle", PropertyActionSettings.class);
            propstyle.setDisplayName(NbBundle.getMessage(PropertyActionSettingsBeanInfo.class, "PROP_Option_Prop_Style"));
            propstyle.setShortDescription(NbBundle.getMessage(PropertyActionSettingsBeanInfo.class, "HINT_Option_Prop_Style"));
            propstyle.setPropertyEditorClass (PropertyStyleEditor.class);

    	    return new PropertyDescriptor[] {propstyle};
        } catch (IntrospectionException ie) {
	        ErrorManager.getDefault().notify(ie);
            return null;
        }
    }

    public Image getIcon (int type) {
        return Utilities.loadImage("org/netbeans/modules/beans/resources/beansSetting.gif");
    }
    
    public static class PropertyStyleEditor extends PropertyEditorSupport {

        private static final String[] tags = {
            NbBundle.getMessage(PropertyActionSettingsBeanInfo.class, "MSG_Option_Gen_Undescored"),
            NbBundle.getMessage(PropertyActionSettingsBeanInfo.class, "MSG_Option_Gen_This"),
        };

        public String[] getTags () {
            return tags;
        }

        public String getAsText () {
            String type = (String) getValue ();
            
            if(type.equals(PropertyActionSettings.GENERATE_UNDERSCORED)){
                return tags[0];
            }
            else if(type.equals(PropertyActionSettings.GENERATE_WITH_THIS)){
                return tags[1];
            }
            return tags[0];
        }

        public void setAsText (String text) throws IllegalArgumentException {
            if (tags[0].equals (text))
                setValue (PropertyActionSettings.GENERATE_UNDERSCORED);
            else if (tags[1].equals (text))
                setValue (PropertyActionSettings.GENERATE_WITH_THIS);
            else
                throw new IllegalArgumentException ();
        }
    }
}
