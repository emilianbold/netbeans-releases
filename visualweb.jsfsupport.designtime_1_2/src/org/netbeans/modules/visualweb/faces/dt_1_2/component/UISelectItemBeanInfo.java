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
import com.sun.rave.designtime.markup.*;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import javax.faces.component.UISelectItem;

public class UISelectItemBeanInfo extends HtmlNonGeneratedBeanInfoBase {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(UISelectItemBeanInfo.class);

    public UISelectItemBeanInfo() {
        super();
        beanClass = UISelectItem.class;
        iconFileName_C16 = "UISelectItem_C16.gif";  //NOI18N
        iconFileName_C32 = "UISelectItem_C32.gif";  //NOI18N
        iconFileName_M16 = "UISelectItem_M16.gif";  //NOI18N
        iconFileName_M32 = "UISelectItem_M32.gif";  //NOI18N
    }

    private BeanDescriptor beanDescriptor;

    /**
     * @return The BeanDescriptor
     */
    public BeanDescriptor getBeanDescriptor() {
        if (beanDescriptor == null) {
            beanDescriptor = new BeanDescriptor(beanClass);
            beanDescriptor.setValue(Constants.BeanDescriptor.TAGLIB_URI, "http://java.sun.com/jsf/core");  //NOI18N
            beanDescriptor.setValue(Constants.BeanDescriptor.TAGLIB_PREFIX, "f");  //NOI18N
            beanDescriptor.setValue(Constants.BeanDescriptor.TAG_NAME, "selectItem");  //NOI18N
            beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME, "selectItem");  //NOI18N
        }
        return beanDescriptor;
    }


    private PropertyDescriptor[] propertyDescriptors;

    /**
     * Returns the PropertyDescriptor array which describes
     * the property meta-data for this JavaBean
     *
     * @return An array of PropertyDescriptor objects
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        if (propertyDescriptors == null) {
            try {
                PropertyDescriptor prop_escape = new PropertyDescriptor("escape", beanClass, "getEscape", "setEscape");  //NOI18N
                AttributeDescriptor attrib_escape = new AttributeDescriptor("escape");  //NOI18N
                attrib_escape.setBindable(true);
                prop_escape.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR, attrib_escape);

                PropertyDescriptor prop_itemLabel = new PropertyDescriptor("itemLabel", beanClass, "getItemLabel", "setItemLabel");  //NOI18N
                AttributeDescriptor attrib_itemLabel = new AttributeDescriptor("itemLabel");  //NOI18N
                attrib_escape.setBindable(true);
                prop_escape.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR, attrib_escape);

                PropertyDescriptor prop_itemDescription = new PropertyDescriptor("itemDescription", beanClass, "getItemDescription", "setItemDescription");  //NOI18N
                AttributeDescriptor attrib_itemDescription = new AttributeDescriptor("itemDescription");  //NOI18N
                attrib_itemDescription.setBindable(true);
                prop_itemDescription.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR, attrib_itemDescription);

                PropertyDescriptor prop_itemValue = new PropertyDescriptor("itemValue", beanClass, "getItemValue", "setItemValue");  //NOI18N
                AttributeDescriptor attrib_itemValue = new AttributeDescriptor("itemValue");  //NOI18N
                attrib_itemValue.setBindable(true);
                prop_itemValue.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR, attrib_itemValue);

                PropertyDescriptor prop_value = new PropertyDescriptor("value", beanClass, "getValue", "setValue");  //NOI18N
                AttributeDescriptor attrib_value = new AttributeDescriptor("value");  //NOI18N
                attrib_value.setBindable(true);
                prop_value.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR, attrib_value);
                
                PropertyDescriptor prop_id = new PropertyDescriptor("id", beanClass, "getId", "setId");  //NOI18N
                prop_id.setShortDescription(bundle.getMessage("idPropShortDesc"));  //NOI18N
                prop_id.setHidden(true);
                AttributeDescriptor attrib_id = new AttributeDescriptor("id");  //NOI18N
                attrib_id.setBindable(false);
                prop_escape.setValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR, attrib_escape);
                
                propertyDescriptors = new PropertyDescriptor[] {
                    prop_escape,
                    prop_itemLabel,
                    prop_itemDescription,
                    prop_itemValue,
                    prop_value,
                    prop_id,
                };
            } catch (IntrospectionException ix) {
                ix.printStackTrace();
            }
        }
        
        return propertyDescriptors;
    }
}
