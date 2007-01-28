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
package org.netbeans.modules.visualweb.faces.dt_1_2.component.html;

import com.sun.rave.designtime.*;
import org.netbeans.modules.visualweb.faces.dt.HtmlDesignInfoBase;
import javax.faces.component.html.HtmlInputSecret;

public class HtmlInputSecretDesignInfo extends HtmlDesignInfoBase /* implements MarkupDesignInfo*/ {

    public Class getBeanClass() { return HtmlInputSecret.class; }

    /*
    public MarkupMouseRegion[] annotateRender(DesignBean bean, DocumentFragment documentFragment, MarkupPosition begin, MarkupPosition end) {
        NodeList nodeList = documentFragment.getChildNodes();
        int len = nodeList.getLength();
        for (int i = 0; i < len; i++) {
            Node child = nodeList.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element)child;
                if (element.getTagName().equals("input")) { //NOI18N
                    String value = element.getAttribute("value"); //NOI18N
                    if (value == null || value.length() == 0 || "null".equals(value)) {
                        String newValue = bean.getInstanceName();
                        String valueRef = bean.getProperty("valueRef").getValueAsText();
                        if (valueRef != null && value.length() > 0) {
                            valueRef = valueRef.substring(valueRef.lastIndexOf("."));
                            newValue = valueRef;
                        }
                        else {
                            Object bindValue = bean.getDesignContext().getContextData(bean.getInstanceName() + ".databind");
                            if (bindValue != null) {
                                newValue = "" + bindValue;
                            }
                        }
                        element.setAttribute("value", newValue);
                    }
                }
            }
        }
        return null;
    }
    */

    public void propertyChanged(DesignProperty prop, Object oldValue) {
        super.propertyChanged(prop, oldValue);
    	if (prop == null) {return;}
    	//fix 5024101. if "value" property is set on HtmlInputSecret, then set redisplay property to true
    	if ("value".equals(prop.getPropertyDescriptor().getName())) { //NOI18N
			DesignBean secretBean = prop.getDesignBean();
			if (secretBean != null) {
				DesignProperty redisplayProp = secretBean.getProperty("redisplay"); //NOI18N
				if (redisplayProp != null) {
                                    String valueSource = prop.getValueSource();
                                    if (valueSource != null && valueSource.length() > 0) {
                                            redisplayProp.setValue(Boolean.TRUE);
                                    }
                                    else {
                                            redisplayProp.setValue(Boolean.FALSE);
                                    }
				}
			}
    	}
    }
}
