/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.visualweb.faces.dt_1_1.component.html;

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
