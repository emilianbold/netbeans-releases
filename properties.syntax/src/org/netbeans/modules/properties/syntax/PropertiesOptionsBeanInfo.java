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


package org.netbeans.modules.properties.syntax;


import java.util.MissingResourceException;

import org.openide.util.NbBundle;

import org.netbeans.modules.editor.options.BaseOptionsBeanInfo;


/** BeanInfo for properties options.
 *
 * @author Petr Jiricka, Libor Kramolis
 */
public class PropertiesOptionsBeanInfo extends BaseOptionsBeanInfo {

    /** Constucts bean info. */
    public PropertiesOptionsBeanInfo() {
        super ("/org/netbeans/modules/properties/propertiesEditorMode"); // NOI18N
    }

    
    /** Gets bean class. */
    protected Class getBeanClass() {
        return PropertiesOptions.class;
    }

    /** Gets property names. */
    protected String[] getPropNames() {
        return PropertiesOptions.PROPERTIES_PROP_NAMES;
    }

    /** Gets localized string. 
     * @return localized string */
    protected String getString(String s) {
        try {
            return NbBundle.getBundle(PropertiesOptionsBeanInfo.class).getString(s);
        } catch (MissingResourceException mre) {
            return super.getString(s);
        }
    }

}
