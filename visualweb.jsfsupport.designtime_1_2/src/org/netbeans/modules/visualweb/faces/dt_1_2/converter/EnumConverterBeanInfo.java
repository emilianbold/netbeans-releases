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
package org.netbeans.modules.visualweb.faces.dt_1_2.converter;

import org.netbeans.modules.visualweb.faces.dt.HtmlNonGeneratedBeanInfoBase;
import java.beans.*;
import com.sun.rave.designtime.*;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import javax.faces.convert.EnumConverter;

public class EnumConverterBeanInfo extends HtmlNonGeneratedBeanInfoBase {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(EnumConverterBeanInfo.class);

    /**
     * Construct a <code>EnumConverterBeanInfo</code> instance
     */
    public EnumConverterBeanInfo() {
        beanClass = EnumConverter.class;
        iconFileName_C16 = "EnumConverter_C16"; //NOI18N
        iconFileName_C32 = "EnumConverter_C32"; //NOI18N
        iconFileName_M16 = "EnumConverter_M16"; //NOI18N
        iconFileName_M32 = "EnumConverter_M32"; //NOI18N
    }

    private BeanDescriptor beanDescriptor;

    /**
     * @return The BeanDescriptor
     */
    public BeanDescriptor getBeanDescriptor() {
        if (beanDescriptor == null) {
            beanDescriptor = new BeanDescriptor(beanClass);
            beanDescriptor.setValue(Constants.BeanDescriptor.INSTANCE_NAME, "enumConverter"); //NOI18N
            beanDescriptor.setDisplayName(bundle.getMessage("enumConvert")); //NOI18N
            beanDescriptor.setShortDescription(bundle.getMessage("enumConvertShortDesc")); //NOI18N
            beanDescriptor.setValue(Constants.BeanDescriptor.HELP_KEY, "projrave_ui_elements_palette_jsf-val-conv_enum_converter");
        }
        return beanDescriptor;
    }


    private PropertyDescriptor[] propDescriptors;

    /**
     * Returns the PropertyDescriptor array which describes
     * the property meta-data for this JavaBean
     *
     * @return An array of PropertyDescriptor objects
     */
    public PropertyDescriptor[] getPropertyDescriptors() {

        if (propDescriptors == null) {
            try {
                PropertyDescriptor _transient = new PropertyDescriptor("transient", beanClass, "isTransient", "setTransient"); //NOI18N

                propDescriptors = new PropertyDescriptor[] {
                    _transient
                };
            } catch (IntrospectionException ix) {
                ix.printStackTrace();
            }
        }

        return propDescriptors;
    }
}
