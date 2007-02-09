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
package org.netbeans.modules.xsl;

import java.beans.*;
import java.awt.Image;
import org.openide.util.Utilities;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.Exceptions;

/**
 * Loader BeanInfo adding metadata missing in org.openide.loaders.MultiFileLoaderBeanInfo.
 *
 * @author Libor Kramolis
 */
public class XSLDataLoaderBeanInfo extends SimpleBeanInfo {
    /* Icon base dir. */
    private static final String ICON_DIR_BASE = "org/netbeans/modules/xsl/resources/"; // NOI18N

    /**
     * Gets the bean's <code>BeanDescriptor</code>s.
     *
     * @return BeanDescriptor describing the editable
     * properties of this bean.  May return null if the
     * information should be obtained by automatic analysis.
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor beanDescriptor = new BeanDescriptor  ( XSLDataLoader.class , null );
        beanDescriptor.setDisplayName ( Util.THIS.getString("NAME_XSLDataLoader") );//GEN-HEADEREND:BeanDescriptor
        
        // Here you can add code for customizing the BeanDescriptor.
        
        return beanDescriptor;
    }
    
    /**
     * Gets the bean's <code>PropertyDescriptor</code>s.
     *
     * @return An array of PropertyDescriptors describing the editable
     * properties supported by this bean.  May return null if the
     * information should be obtained by automatic analysis.
     * <p>
     * If a property is indexed, then its entry in the result array will
     * belong to the IndexedPropertyDescriptor subclass of PropertyDescriptor.
     * A client of getPropertyDescriptors can use "instanceof" to check
     * if a given PropertyDescriptor is an IndexedPropertyDescriptor.
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        int PROPERTY_extensions = 0;
        PropertyDescriptor[] properties = new PropertyDescriptor[1];
    
        try {
            properties[PROPERTY_extensions] = new PropertyDescriptor ( "extensions", XSLDataLoader.class, "getExtensions", "setExtensions" );
            properties[PROPERTY_extensions].setDisplayName ( Util.THIS.getString ("PROP_XSL_Extensions") );
            properties[PROPERTY_extensions].setShortDescription ( Util.THIS.getString ("HINT_XSL_Extensions") );
        }
        catch( IntrospectionException e) {
            Exceptions.printStackTrace(e);
        }
        return properties;
    }
    
    /**
     * Gets the bean's <code>EventSetDescriptor</code>s.
     *
     * @return  An array of EventSetDescriptors describing the kinds of
     * events fired by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    public EventSetDescriptor[] getEventSetDescriptors() {
        return new EventSetDescriptor[0];
    }
    
    /**
     * Gets the bean's <code>MethodDescriptor</code>s.
     *
     * @return  An array of MethodDescriptors describing the methods
     * implemented by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    public MethodDescriptor[] getMethodDescriptors() {
        return new MethodDescriptor[0];
    }
    
    /** @param type Desired type of the icon
     * @return returns the Entity loader's icon
     */
    public Image getIcon(final int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) ||
            (type == java.beans.BeanInfo.ICON_MONO_16x16)) {

            return Utilities.loadImage (ICON_DIR_BASE + "xslObject.gif"); // NOI18N
        } else {
            return Utilities.loadImage (ICON_DIR_BASE + "xslObject32.gif"); // NOI18N
        }
    }

    public BeanInfo[] getAdditionalBeanInfo() {
        try {
            return new BeanInfo[] {
                java.beans.Introspector.getBeanInfo (MultiFileLoader.class)
            };
        } catch (IntrospectionException e) {
            Exceptions.printStackTrace(e);
        }
        return super.getAdditionalBeanInfo();
    }

}
