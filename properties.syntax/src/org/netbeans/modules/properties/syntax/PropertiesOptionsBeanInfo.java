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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
