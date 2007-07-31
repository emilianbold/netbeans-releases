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

package org.netbeans.modules.xml.wsdl.ui.netbeans.module;

import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

import org.openide.util.Utilities;

/** Description of {@link WSDLDataLoader}.
 *
 * @author Jerry Waldorf
 */
public class WSDLDataLoaderBeanInfo extends SimpleBeanInfo {



	/**
	 * copied from Ant Module
	 */
    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        // Make extensions into a r/o property.
        // It will only contain the WSDL MIME type.
        // Customizations should be done on the resolver object, not on the extension list.
        // Does not work to just use additional bean info from UniFileLoader and return one extensions
        // property with no setter--Introspector cleverly (!&#$@&) keeps your display name
        // and everything and adds back in the setter from the superclass.
        // So bypass UniFileLoader in the beaninfo search.
        try {
            PropertyDescriptor extensions = new PropertyDescriptor(
				"extensions", WSDLDataLoader.class, "getExtensions", null); // NOI18N
            extensions.setDisplayName(
				NbBundle.getMessage(WSDLDataLoaderBeanInfo.class, "PROP_extensions"));
            extensions.setShortDescription(
				NbBundle.getMessage(WSDLDataLoaderBeanInfo.class, "HINT_extensions"));
            extensions.setExpert(true);
            return new PropertyDescriptor[] {extensions};
        } catch (IntrospectionException ie) {
            ErrorManager.getDefault().notify(ie);
            return null;
        }
    }


    // If you have additional properties:
    /*
    public PropertyDescriptor[] getPropertyDescriptors () {
	try {
            PropertyDescriptor myProp = new PropertyDescriptor ("myProp", MyDataLoader.class);
            myProp.setDisplayName (NbBundle.getMessage (MyDataLoaderBeanInfo.class, "PROP_myProp"));
            myProp.setShortDescription (NbBundle.getMessage (MyDataLoaderBeanInfo.class, "HINT_myProp"));
	    return new PropertyDescriptor[] { myProp };
	} catch (IntrospectionException ie) {
            TopManager.getDefault ().getErrorManager ().notify (ie);
	    return null;
	}
    }
    */

    @Override
    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            // I.e. MultiFileLoader.class or UniFileLoader.class.
            return new BeanInfo[] { Introspector.getBeanInfo(WSDLDataLoader.class.getSuperclass()) };
        } catch (IntrospectionException ie) {
            org.openide.ErrorManager.getDefault ().notify (ie);
            return null;
        }
    }

    @Override
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            return Utilities.loadImage ("org.netbeans.modules.xml.wsdl.ui.netbeans.module/resources/wsdl_file.png");
        }
        return null;
    }

}
