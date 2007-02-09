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
package org.netbeans.modules.j2ee.sun.share.configbean;

import java.beans.*;
import org.openide.util.Exceptions;

public class EjbJarRootBeanInfo extends SimpleBeanInfo {
	
	/** Return an appropriate icon (currently, only 16x16 color is available)
	 */
	public java.awt.Image getIcon(int iconKind) {
		return loadImage("resources/EjbJarRootIcon16.gif");	// NOI18N
	}
	
	/**
	 * Gets the bean's <code>BeanDescriptor</code>s.
	 *
	 * @return BeanDescriptor describing the editable
	 * properties of this bean.  May return null if the
	 * information should be obtained by automatic analysis.
	 */
	public BeanDescriptor getBeanDescriptor() {
            BeanDescriptor beanDescriptor = new BeanDescriptor  ( org.netbeans.modules.j2ee.sun.share.configbean.EjbJarRoot.class , org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.EjbJarRootCustomizer.class );//GEN-HEADEREND:BeanDescriptor
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
            int PROPERTY_cmpResource = 0;
            int PROPERTY_identity = 1;
            int PROPERTY_messageDestination = 2;
            int PROPERTY_name = 3;
            int PROPERTY_pmDescriptors = 4;
            int PROPERTY_refIdentity = 5;
            PropertyDescriptor[] properties = new PropertyDescriptor[6];

            try {
                properties[PROPERTY_cmpResource] = new PropertyDescriptor ( "cmpResource", org.netbeans.modules.j2ee.sun.share.configbean.EjbJarRoot.class, "getCmpResource", "setCmpResource" );
                properties[PROPERTY_cmpResource].setPropertyEditorClass ( org.netbeans.modules.j2ee.sun.share.configbean.editors.DummyPropertyEditor.class );
                properties[PROPERTY_identity] = new PropertyDescriptor ( "identity", org.netbeans.modules.j2ee.sun.share.configbean.EjbJarRoot.class, "getIdentity", "setIdentity" );
                properties[PROPERTY_messageDestination] = new IndexedPropertyDescriptor ( "messageDestination", org.netbeans.modules.j2ee.sun.share.configbean.EjbJarRoot.class, "getMessageDestination", "setMessageDestination", "getMessageDestination", "setMessageDestination" );
                properties[PROPERTY_messageDestination].setPropertyEditorClass ( org.netbeans.modules.j2ee.sun.share.configbean.editors.DummyPropertyEditor.class );
                properties[PROPERTY_name] = new PropertyDescriptor ( "name", org.netbeans.modules.j2ee.sun.share.configbean.EjbJarRoot.class, "getName", "setName" );
                properties[PROPERTY_pmDescriptors] = new PropertyDescriptor ( "pmDescriptors", org.netbeans.modules.j2ee.sun.share.configbean.EjbJarRoot.class, "getPmDescriptors", "setPmDescriptors" );
                properties[PROPERTY_pmDescriptors].setPropertyEditorClass ( org.netbeans.modules.j2ee.sun.share.configbean.editors.DummyPropertyEditor.class );
                properties[PROPERTY_refIdentity] = new PropertyDescriptor ( "refIdentity", org.netbeans.modules.j2ee.sun.share.configbean.EjbJarRoot.class, "getRefIdentity", null );
            } catch( IntrospectionException e) {
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
	
}

