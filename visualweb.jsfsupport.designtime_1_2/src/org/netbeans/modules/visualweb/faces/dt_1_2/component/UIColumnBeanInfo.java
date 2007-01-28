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
package org.netbeans.modules.visualweb.faces.dt_1_2.component;

import org.netbeans.modules.visualweb.faces.dt.HtmlNonGeneratedBeanInfoBase;
import java.beans.*;
import com.sun.rave.designtime.*;
import com.sun.rave.designtime.faces.*;
import com.sun.rave.designtime.markup.*;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import javax.faces.component.UIColumn;

/**
 * The UIColumnBeanInfo class provides design-time meta data for the
 * UIColumn component for use in a visual design tool.
 */
public class UIColumnBeanInfo extends HtmlNonGeneratedBeanInfoBase {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(UIColumnBeanInfo.class);

    /**
     * Construct a <code>UIColumnBeanInfoBase</code> instance
     */
    public UIColumnBeanInfo() {
        beanClass = UIColumn.class;
        iconFileName_C16 = "UIColumn_C16";  //NOI18N
        iconFileName_C32 = "UIColumn_C32";  //NOI18N
        iconFileName_M16 = "UIColumn_M16";  //NOI18N
        iconFileName_M32 = "UIColumn_M32";  //NOI18N
    }

    private BeanDescriptor beanDescriptor;

    /**
     * @return The BeanDescriptor
     */
    public BeanDescriptor getBeanDescriptor() {
        if (beanDescriptor == null) {
            beanDescriptor = new BeanDescriptor(beanClass);
            beanDescriptor.setValue(Constants.BeanDescriptor.TAGLIB_URI, "http://java.sun.com/jsf/html");  //NOI18N
            beanDescriptor.setValue(Constants.BeanDescriptor.TAG_NAME, "column");  //NOI18N
            beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME, "column");  //NOI18N
            beanDescriptor.setValue(Constants.BeanDescriptor.RESIZE_CONSTRAINTS, new Integer(Constants.ResizeConstraints.NONE));
            beanDescriptor.setValue(Constants.BeanDescriptor.FACET_DESCRIPTORS, getFacetDescriptors());
        }
        return beanDescriptor;
    }

    public FacetDescriptor[] getFacetDescriptors() {
        return new FacetDescriptor[] {
            new FacetDescriptor("header"),  //NOI18N
            new FacetDescriptor("footer"),  //NOI18N
        };
    }

    PropertyDescriptor[] propertyDescriptors;

    /**
     * Returns the PropertyDescriptor array which describes
     * the property meta-data for this JavaBean
     *
     * @return An array of PropertyDescriptor objects
     */
    public PropertyDescriptor[] getPropertyDescriptors() {

        if (propertyDescriptors == null) {
            try {

                PropertyDescriptor prop_rendered = new PropertyDescriptor("rendered", beanClass, "isRendered", "setRendered");  //NOI18N
                prop_rendered.setShortDescription(bundle.getMessage("rendPropShortDesc"));  //NOI18N
                AttributeDescriptor attrib_rendered = new AttributeDescriptor("rendered");  //NOI18N
                attrib_rendered.setBindable(true);
                prop_rendered.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR, attrib_rendered);

                PropertyDescriptor prop_id = new PropertyDescriptor("id", beanClass, "getId", "setId");  //NOI18N
                prop_id.setShortDescription(bundle.getMessage("idPropShortDesc"));  //NOI18N
                prop_id.setHidden(true);
                AttributeDescriptor attrib_id = new AttributeDescriptor("id");  //NOI18N
                attrib_id.setBindable(false);
                prop_id.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR, attrib_id);

                propertyDescriptors = new PropertyDescriptor[] {
                    prop_rendered,
                    prop_id,
                };

            }
            catch (IntrospectionException ix) {
                ix.printStackTrace();
            }
        }

        return propertyDescriptors;
    }
}
