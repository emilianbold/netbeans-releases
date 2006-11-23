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

package org.netbeans.modules.diff.builtin.provider;

import java.beans.*;
import org.openide.util.*;

/**
 * @author Petr Kuzel
 */
public class BuiltInDiffProviderBeanInfo extends SimpleBeanInfo {

    private static final int PROPERTY_trimLines = 0;

    private static String iconNameC16 = "/org/netbeans/modules/diff/diffSettingsIcon.gif";//GEN-BEGIN:Icons
    
    /**
     * Gets the bean's <code>BeanDescriptor</code>s.
     *
     * @return BeanDescriptor describing the editable
     * properties of this bean.  May return null if the
     * information should be obtained by automatic analysis.
     */
    public BeanDescriptor getBeanDescriptor() {
        return new BeanDescriptor  ( BuiltInDiffProvider.class , null );
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
        PropertyDescriptor[] properties = new PropertyDescriptor[1];
    
        try {
            properties[PROPERTY_trimLines] = new PropertyDescriptor ( "trimLines", BuiltInDiffProvider.class, "isTrimLines", "setTrimLines" ); // NOI18N
            properties[PROPERTY_trimLines].setDisplayName ( NbBundle.getMessage(BuiltInDiffProvider.class,"BK0001") );
            properties[PROPERTY_trimLines].setShortDescription ( NbBundle.getMessage(BuiltInDiffProvider.class,"BK0002") );
        }
        catch(IntrospectionException e) {
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
    
    public java.awt.Image getIcon(int iconKind) {
        return Utilities.loadImage(iconNameC16);
    }
    
}

