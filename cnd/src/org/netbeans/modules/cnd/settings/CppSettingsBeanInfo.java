/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.settings;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import org.openide.util.Utilities;

/**
 *  Bean info for CppSettings
 */
public class CppSettingsBeanInfo extends SimpleBeanInfo {

    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bdesc = new BeanDescriptor(CppSettings.class);
        bdesc.setDisplayName(CppSettings.getString(
				    "OPTION_CPP_SETTINGS_NAME"));   //NOI18N
        bdesc.setShortDescription(CppSettings.getString(
				    "HINT_CPP_SETTINGS_NAME"));	    //NOI18N
        return bdesc;
    }

    /**
     *  Descriptor of valid properties.
     *
     *  @return array of properties
     */
    public PropertyDescriptor[] getPropertyDescriptors() {

	int i = 0;
	PropertyDescriptor[] desc = null;
	try {
            desc = new PropertyDescriptor[] {
		new PropertyDescriptor(CppSettings.PROP_REPLACEABLE_STRINGS_TABLE, CppSettings.class),
		new PropertyDescriptor(CppSettings.PROP_FREE_FORMAT_FORTRAN, CppSettings.class),
		new PropertyDescriptor(CppSettings.PROP_PARSING_DELAY, CppSettings.class),
		new PropertyDescriptor(CppSettings.PROP_FORTRAN_ENABLED, CppSettings.class)
	    };

	    desc[i].setDisplayName(CppSettings.getString( "PROP_REPLACEABLE_STRINGS"));	    //NOI18N
	    desc[i++].setShortDescription(CppSettings.getString( "HINT_REPLACEABLE_STRINGS"));	    //NOI18N
	    desc[i].setDisplayName(CppSettings.getString( "PROP_FREE_FORMAT_FORTRAN"));	    //NOI18N
 	    desc[i++].setShortDescription(CppSettings.getString( "HINT_FREE_FORMAT_FORTRAN"));	    //NOI18N
	    desc[i].setDisplayName(CppSettings.getString( "PROP_AUTO_PARSING_DELAY"));	    //NOI18N
 	    desc[i++].setShortDescription(CppSettings.getString( "HINT_AUTO_PARSING_DELAY"));	    //NOI18N
	    desc[i].setDisplayName(CppSettings.getString("PROP_FORTRAN_ENABLED")); //NOI18N
 	    desc[i++].setShortDescription(CppSettings.getString("HINT_FORTRAN_ENABLED")); //NOI18N
	} catch (IntrospectionException ex) {
	    throw new InternalError();
	}
	return desc;
    }

    /*
     *  There currently are no icons for CCF. This is just a place holder.
     */
    public Image getIcon(int type) {
	// XXX this icon is wrong
	return Utilities.loadImage("org/netbeans/modules/cnd/loaders/CCSrcIcon.gif"); //NOI18N
    }
}
