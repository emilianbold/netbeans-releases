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
package org.netbeans.modules.bpel.core;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.openide.loaders.DataLoader;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.ErrorManager;

/**
 * Loader BeanInfo adding metadata missing in org.openide.loaders.MultiFileLoaderBeanInfo.
 *
 * @author ads
 */
public class BPELDataLoaderBeanInfo extends SimpleBeanInfo {
    
    private static final String LOADER_DESC = "LBL_loader_desc";    // NOI18N
    
    private static final String PATH_TO_IMAGE = 
        "org/netbeans/modules/bpel/core/resources/bpel_file.gif";   // NOI18N

    /** {@inheritDoc} */
    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (DataLoader.class) };
        } catch (IntrospectionException ie) {
            ErrorManager.getDefault().notify(ie);
            return null;
        }
    }
    
    /**
     * Gets the bean's <code>BeanDescriptor</code>s.
     *
     * @return BeanDescriptor describing the editable
     * properties of this bean.  May return null if the
     * information should be obtained by automatic analysis.
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor beanDescriptor = 
            new BeanDescriptor ( BPELDataLoader.class , null );
        beanDescriptor.setDisplayName ( NbBundle.getMessage( getClass(), 
                BPELDataLoader.LOADER_NAME) );
        beanDescriptor.setShortDescription ( NbBundle.getMessage(getClass(), 
                LOADER_DESC) );
        
        // Here you can add code for customizing the BeanDescriptor.
        
        return beanDescriptor;
    }
    
    /** {@inheritDoc} */
    public PropertyDescriptor[] getPropertyDescriptors() {
        // Make extensions into a r/o property.
        // It will only contain the BPEL MIME type.
        // Customizations should be done on the resolver object, not on the extension list.
        // Does not work to just use additional bean info from UniFileLoader and return one extensions
        // property with no setter--Introspector cleverly (!&#$@&) keeps your display name
        // and everything and adds back in the setter from the superclass.
        // So bypass UniFileLoader in the beaninfo search.
        try {
            PropertyDescriptor extensions = new PropertyDescriptor(
                "extensions", BPELDataLoader.class, "getExtensions", null);// NOI18N
            extensions.setDisplayName(
                NbBundle.getMessage(getClass(), "PROP_extensions"));
            extensions.setShortDescription(
                NbBundle.getMessage(getClass(), "HINT_extensions"));
            extensions.setExpert(true);
            return new PropertyDescriptor[] {extensions};
        } catch (IntrospectionException ie) {
            ErrorManager.getDefault().notify(ie);
            return null;
        }
    }
    
    /** {@inheritDoc} */
    public MethodDescriptor[] getMethodDescriptors() {
        return new MethodDescriptor[0];
    }
    
    /** {@inheritDoc} */
    public EventSetDescriptor[] getEventSetDescriptors() {
        return new EventSetDescriptor[0];
    }

    /** {@inheritDoc} */
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            return Utilities.loadImage( PATH_TO_IMAGE );
        } else {
            return null;
        }
    }
}
