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
import java.util.ResourceBundle;

import org.openide.util.NbBundle;


/**
 * Bean info for <code>I18nOptions</code> class.
 *
 * @author  Peter Zavadsky
 */
public class I18nOptionsBeanInfo extends SimpleBeanInfo {

    /** Icon 16x16. */
    private static Image icon;
    
    /** Icon 32x32. */
    private static Image icon32;
    
    private static final ResourceBundle bundle = NbBundle.getBundle(I18nOptionsBeanInfo.class);    
    
    /** Overrides superclass method. */
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor genFieldPD =    new PropertyDescriptor(I18nOptions.PROP_GENERATE_FIELD, I18nOptions.class);
            PropertyDescriptor initCodePD =    new PropertyDescriptor(I18nOptions.PROP_INIT_JAVA_CODE, I18nOptions.class);
            PropertyDescriptor replaceCodePD = new PropertyDescriptor(I18nOptions.PROP_REPLACE_JAVA_CODE, I18nOptions.class);
            PropertyDescriptor regExpPD =      new PropertyDescriptor(I18nOptions.PROP_REGULAR_EXPRESSION, I18nOptions.class);

            // Set expert flags.
            initCodePD.setExpert(true);
            replaceCodePD.setExpert(true);
            regExpPD.setExpert(true);

            // Set display names.
            genFieldPD.setDisplayName(bundle.getString("TXT_GenerateField"));
            initCodePD.setDisplayName(bundle.getString("TXT_InitCodeFormat"));
            replaceCodePD.setDisplayName(bundle.getString("TXT_ReplaceCodeFormat"));
            regExpPD.setDisplayName(bundle.getString("TXT_RegularExpression"));
            
            // Set property editors.
            initCodePD.setPropertyEditorClass(HelpStringCustomEditor.InitCodeEditor.class);
            replaceCodePD.setPropertyEditorClass(HelpStringCustomEditor.ReplaceCodeEditor.class); 
            regExpPD.setPropertyEditorClass(HelpStringCustomEditor.RegExpEditor.class);
            
            return new PropertyDescriptor[] {
                genFieldPD, initCodePD, replaceCodePD, regExpPD
            };
        } catch(IntrospectionException ie) {
            return null;
        }
    }

    /** Overrides superclass method. */
    public Image getIcon(int type) {
        if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
            if(icon == null) {
                icon   = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/org/netbeans/modules/i18n/i18nAction.gif")); // NOI18N
            }
            
            return icon;
        } else { // 32
            if(icon32 == null) {
                icon32 = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/org/netbeans/modules/properties/propertiesKey32.gif")); // NOI18N
            }
            
            return icon32;
        }
    }
    
}
