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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.i18n;


import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/**
 * Bean info for <code>I18nOptions</code> class.
 *
 * @author  Peter Zavadsky
 */
public class I18nOptionsBeanInfo extends SimpleBeanInfo {
    
    /**
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor descr = new BeanDescriptor(I18nOptions.class);
        descr.setDisplayName(
                NbBundle.getMessage(I18nOptions.class,
                                    "LBL_Internationalization"));       //NOI18N
        return descr;
    }

    /** Overrides superclass method. */
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor advWizardPD    = new PropertyDescriptor(I18nOptions.PROP_ADVANCED_WIZARD, I18nOptions.class);
            PropertyDescriptor initCodePD     = new PropertyDescriptor(I18nOptions.PROP_INIT_JAVA_CODE, I18nOptions.class);
            PropertyDescriptor replaceCodePD  = new PropertyDescriptor(I18nOptions.PROP_REPLACE_JAVA_CODE, I18nOptions.class);
            PropertyDescriptor regExpPD       = new PropertyDescriptor(I18nOptions.PROP_REGULAR_EXPRESSION, I18nOptions.class);
            PropertyDescriptor i18nRegExpPD   = new PropertyDescriptor(I18nOptions.PROP_I18N_REGULAR_EXPRESSION, I18nOptions.class);
            PropertyDescriptor replaceValuePD = new PropertyDescriptor(I18nOptions.PROP_REPLACE_RESOURCE_VALUE, I18nOptions.class);
            PropertyDescriptor lastResPD      = new PropertyDescriptor(I18nOptions.PROP_LAST_RESOURCE2, I18nOptions.class);

            // Set expert flags.
            initCodePD.setExpert(true);
            replaceCodePD.setExpert(true);
            regExpPD.setExpert(true);
            i18nRegExpPD.setExpert(true);

            // Set last resource property as hidden.
            lastResPD.setHidden(true);
            
            ResourceBundle bundle = NbBundle.getBundle(I18nOptionsBeanInfo.class);
            
            // Set display names.
            advWizardPD.setDisplayName(bundle.getString("TXT_AdvancedWizard"));
            initCodePD.setDisplayName(bundle.getString("TXT_InitCodeFormat"));
            replaceCodePD.setDisplayName(bundle.getString("TXT_ReplaceCodeFormat"));
            regExpPD.setDisplayName(bundle.getString("TXT_RegularExpression"));
            replaceValuePD.setDisplayName(bundle.getString("TXT_ReplaceResourceValue"));
            i18nRegExpPD.setDisplayName(bundle.getString("TXT_I18nRegularExpression"));

            // Set short descriptions.
            advWizardPD.setShortDescription(bundle.getString("TXT_AdvancedWizardDesc"));
            initCodePD.setShortDescription(bundle.getString("TXT_InitCodeFormatDesc"));
            replaceCodePD.setShortDescription(bundle.getString("TXT_ReplaceCodeFormatDesc"));
            regExpPD.setShortDescription(bundle.getString("TXT_RegularExpressionDesc"));
            replaceValuePD.setShortDescription(bundle.getString("TXT_ReplaceResourceValueDesc"));
            i18nRegExpPD.setShortDescription(bundle.getString("TXT_I18nRegularExpressionDesc"));
            
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
