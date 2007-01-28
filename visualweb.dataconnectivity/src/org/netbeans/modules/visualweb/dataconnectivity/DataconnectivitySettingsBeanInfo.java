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
package org.netbeans.modules.visualweb.dataconnectivity;

import java.awt.Image;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/** A BeanInfo for dataconnectivity settings.
*
* @author Joel Brown
*/
public class DataconnectivitySettingsBeanInfo extends SimpleBeanInfo {
    /** Provides an explicit property info. */
    PropertyDescriptor[] desc = null ;
    public PropertyDescriptor[] getPropertyDescriptors() {
        if ( desc == null ) {
            try {
                desc =
                    new PropertyDescriptor[] {
                        new PropertyDescriptor(DataconnectivitySettings.PROP_MAKE_IN_SESSION, DataconnectivitySettings.class,
                            "getMakeInSession", "setMakeInSession"), // NOI18N
                        new PropertyDescriptor(DataconnectivitySettings.PROP_CHECK_ROWSET, DataconnectivitySettings.class,
                            "getCheckRowSetProp", "setCheckRowSetProp"), // NOI18N
                        new PropertyDescriptor(DataconnectivitySettings.PROP_DATAPROVIDER, DataconnectivitySettings.class,
                            "getDataProviderSuffixProp", "setDataProviderSuffixProp"), // NOI18N
                        new PropertyDescriptor(DataconnectivitySettings.PROP_ROWSET, DataconnectivitySettings.class,
                            "getRowSetSuffixProp", "setRowSetSuffixProp"), // NOI18N
                    };

                desc[0].setDisplayName(NbBundle.getMessage(DataconnectivitySettings.class, "PROP_MAKE_IN_SESSION"));
                desc[0].setShortDescription(NbBundle.getMessage(DataconnectivitySettings.class, "PROP_MAKE_IN_SESSION"));

                desc[1].setDisplayName(NbBundle.getMessage(DataconnectivitySettings.class, "PROP_CHECK_ROWSET"));
                desc[1].setShortDescription(NbBundle.getMessage(DataconnectivitySettings.class, "PROP_CHECK_ROWSET"));

                desc[2].setDisplayName(NbBundle.getMessage(DataconnectivitySettings.class, "PROP_DATAPROVIDER"));
                desc[2].setShortDescription(NbBundle.getMessage(DataconnectivitySettings.class, "PROP_DATAPROVIDER"));

                desc[3].setDisplayName(NbBundle.getMessage(DataconnectivitySettings.class, "PROP_ROWSET"));
                desc[3].setShortDescription(NbBundle.getMessage(DataconnectivitySettings.class, "PROP_ROWSET"));

            } catch (IntrospectionException ex) {
                ErrorManager.getDefault().notify(ex);

                desc = null ;
            }
        }
        return desc ;
    }

    /** Returns the designer icon */
    public Image getIcon(int type) {
        return Utilities.loadImage("org/netbeans/modules/visualweb/dataconnectivity/resources/datasource.png"); // NOI18N
    }
}
