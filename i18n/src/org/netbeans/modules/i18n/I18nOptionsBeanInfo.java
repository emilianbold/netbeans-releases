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


package org.netbeans.modules.i18n;


import java.awt.Image;
import java.awt.Toolkit;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;

import org.openide.util.Utilities;

/**
 * Bean info for <code>I18nOptions</code> class.
 *
 * @author  Peter Zavadsky
 */
public class I18nOptionsBeanInfo extends SimpleBeanInfo {

    /** Overrides superclass method. */
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor advWizardPD    = new PropertyDescriptor(I18nOptions.PROP_ADVANCED_WIZARD, I18nOptions.class);
            PropertyDescriptor initCodePD     = new PropertyDescriptor(I18nOptions.PROP_INIT_JAVA_CODE, I18nOptions.class);
            PropertyDescriptor replaceCodePD  = new PropertyDescriptor(I18nOptions.PROP_REPLACE_JAVA_CODE, I18nOptions.class);
            PropertyDescriptor regExpPD       = new PropertyDescriptor(I18nOptions.PROP_REGULAR_EXPRESSION, I18nOptions.class);
            PropertyDescriptor i18nRegExpPD   = new PropertyDescriptor(I18nOptions.PROP_I18N_REGULAR_EXPRESSION, I18nOptions.class);
            PropertyDescriptor replaceValuePD = new PropertyDescriptor(I18nOptions.PROP_REPLACE_RESOURCE_VALUE, I18nOptions.class);
            PropertyDescriptor lastResPD      = new PropertyDescriptor(I18nOptions.PROP_LAST_RESOURCE, I18nOptions.class);

            // Set expert flags.
            initCodePD.setExpert(true);
            replaceCodePD.setExpert(true);
            regExpPD.setExpert(true);
            i18nRegExpPD.setExpert(true);

            // Set last resource property as hidden.
            lastResPD.setHidden(true);
            
            // Set display names.
            advWizardPD.setDisplayName(I18nUtil.getBundle().getString("TXT_AdvancedWizard"));
            initCodePD.setDisplayName(I18nUtil.getBundle().getString("TXT_InitCodeFormat"));
            replaceCodePD.setDisplayName(I18nUtil.getBundle().getString("TXT_ReplaceCodeFormat"));
            regExpPD.setDisplayName(I18nUtil.getBundle().getString("TXT_RegularExpression"));
            replaceValuePD.setDisplayName(I18nUtil.getBundle().getString("TXT_ReplaceResourceValue"));
            i18nRegExpPD.setDisplayName(I18nUtil.getBundle().getString("TXT_I18nRegularExpression"));
            
            // Set property editors.
            initCodePD.setPropertyEditorClass(HelpStringCustomEditor.InitCodeEditor.class);
            replaceCodePD.setPropertyEditorClass(HelpStringCustomEditor.ReplaceCodeEditor.class); 
            regExpPD.setPropertyEditorClass(HelpStringCustomEditor.RegExpEditor.class);
            i18nRegExpPD.setPropertyEditorClass(HelpStringCustomEditor.I18nRegExpEditor.class);
            
            return new PropertyDescriptor[] {
                advWizardPD, initCodePD, replaceCodePD, regExpPD, i18nRegExpPD, replaceValuePD, lastResPD
            };
        } catch(IntrospectionException ie) {
            return null;
        }
    }

    /** Overrides superclass method. */
    public Image getIcon(int type) {
        if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
	    return Utilities.loadImage("org/netbeans/modules/i18n/i18nAction.gif"); // NOI18N
        } else { // 32
            return Utilities.loadImage("org/netbeans/modules/properties/propertiesKey32.gif"); // NOI18N
        }
    }
    
}
