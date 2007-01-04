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

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * Bean info for MakeSettings
 *
 */
public class MakeSettingsBeanInfo extends SimpleBeanInfo {

    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor desc = new BeanDescriptor(MakeSettings.class);
        desc.setDisplayName(MakeSettings.getString(
			    "OPTION_MAKE_SETTINGS_NAME"));	//NOI18N
        desc.setShortDescription(MakeSettings.getString(
			    "HINT_MAKE_SETTINGS_NAME"));	//NOI18N
        return desc;
    }

    /** Descriptor of valid properties
     * @return array of properties
     */
    public PropertyDescriptor[] getPropertyDescriptors() {

	/** Array of property descriptors. */
	PropertyDescriptor[] desc = null;

	try {
	    // MakeCompilerType default properties
	    desc = new PropertyDescriptor[] {
		new PropertyDescriptor(MakeSettings.PROP_DEFAULT_BUILD_DIR,
			    MakeSettings.class),
		new PropertyDescriptor(MakeSettings.PROP_DEFAULT_MAKE_COMMAND,
			    MakeSettings.class),
		new PropertyDescriptor(MakeSettings.PROP_REUSE_OUTPUT,
			    MakeSettings.class),
		new PropertyDescriptor(MakeSettings.PROP_SAVE_ALL,
			    MakeSettings.class)
	    };
	  
	    int i = 0;
	    desc[i].setDisplayName(MakeSettings.getString(
			    "PROP_DEFAULT_BUILD_DIR")); // NOI18N
	    desc[i++].setShortDescription(MakeSettings.getString(
			    "HINT_DEFAULT_BUILD_DIR")); // NOI18N
	    desc[i].setDisplayName(MakeSettings.getString(
			    "PROP_DEFAULT_MAKE_COMMAND")); // NOI18N
	    desc[i++].setShortDescription(MakeSettings.getString(
			    "HINT_DEFAULT_MAKE_COMMAND")); // NOI18N
	    desc[i].setDisplayName(MakeSettings.getString(
			    "PROP_REUSE_OUTPUT")); // NOI18N
	    desc[i++].setShortDescription(MakeSettings.getString(
			    "HINT_REUSE_OUTPUT")); // NOI18N
	    desc[i].setDisplayName(MakeSettings.getString(
			    "PROP_SAVE_ALL")); // NOI18N
	    desc[i++].setShortDescription(MakeSettings.getString(
			    "HINT_SAVE_ALL")); // NOI18N
	} catch (IntrospectionException ex) {
	    throw new InternalError();
	}
	return desc;
    }
}
