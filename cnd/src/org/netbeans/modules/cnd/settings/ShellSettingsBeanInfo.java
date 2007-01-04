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
 *  Bean info for ShellSettings
 */
public class ShellSettingsBeanInfo extends SimpleBeanInfo {

    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bdesc = new BeanDescriptor(ShellSettings.class);
        bdesc.setDisplayName(ShellSettings.getString("OPTION_SHELL_SETTINGS_NAME"));   //NOI18N
        bdesc.setShortDescription(ShellSettings.getString("HINT_SHELL_SETTINGS_NAME"));	    //NOI18N
        return bdesc;
    }

    /**
     *  Descriptor of valid properties.
     *
     *  @return array of properties
     */
    public PropertyDescriptor[] getPropertyDescriptors() {

	PropertyDescriptor[] desc = null;
	try {
	    desc = new PropertyDescriptor[] {
		new PropertyDescriptor(ShellSettings.PROP_DEFSHELLCOMMAND, ShellSettings.class),
		new PropertyDescriptor(ShellSettings.PROP_SAVE_ALL, ShellSettings.class)
	    };

	    desc[0].setDisplayName(ShellSettings.getString( "PROP_DEFSHELLCOMMAND"));//NOI18N
	    desc[0].setShortDescription(ShellSettings.getString( "HINT_DEFSHELLCOMMAND"));//NOI18N
	    desc[1].setDisplayName(ShellSettings.getString( "PROP_SAVE_ALL")); // NOI18N
	    desc[1].setShortDescription(ShellSettings.getString( "HINT_SAVE_ALL")); // NOI18N
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
	return Utilities.loadImage("org/netbeans/modules/cnd/loaders/CCSrcIcon.gif"); //NOI18N // FIXUP
    }
}
