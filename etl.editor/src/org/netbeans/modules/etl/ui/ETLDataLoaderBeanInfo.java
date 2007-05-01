/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.etl.ui;

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
public class ETLDataLoaderBeanInfo extends SimpleBeanInfo {

    
        
	/**
	 * copied from Ant Module
	 */
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
				"extensions", ETLDataLoader.class, "getExtensions", null); // NOI18N
            extensions.setDisplayName(
				NbBundle.getMessage(ETLDataLoaderBeanInfo.class, "PROP_extensions"));
            extensions.setShortDescription(
				NbBundle.getMessage(ETLDataLoaderBeanInfo.class, "HINT_extensions"));
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

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            // I.e. MultiFileLoader.class or UniFileLoader.class.
            return new BeanInfo[] { Introspector.getBeanInfo(ETLDataLoader.class.getSuperclass()) };
        } catch (IntrospectionException ie) {
            org.openide.ErrorManager.getDefault ().notify (ie);
            return null;
        }
    }

    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            return Utilities.loadImage ("org/netbeans/modules/etl/ui/resources/images/ETLDefinition.png");
        } else {
            return Utilities.loadImage ("org/netbeans/modules/etl/ui/resources/images/ETLDefinition.png");
        }
    }

}
