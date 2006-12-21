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

package org.netbeans.beaninfo;

import java.beans.*;

import org.openide.options.*;
import org.openide.util.Exceptions;

/** Empty bean info.
*
* @author Jesse Glick
*/
public class ContextSystemOptionBeanInfo extends SimpleBeanInfo {

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (SystemOption.class) };
        } catch (IntrospectionException ie) {
            Exceptions.printStackTrace(ie);
            return null;
        }
    }

    /** No properties.
    * @return array of hidden properties
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor beanContextProxy = new PropertyDescriptor ("beanContextProxy", ContextSystemOption.class, "getBeanContextProxy", null);
            beanContextProxy.setHidden (true);
            PropertyDescriptor options = new PropertyDescriptor ("options", ContextSystemOption.class, "getOptions", null);
            options.setHidden (true);
            return new PropertyDescriptor[] { beanContextProxy, options };
        } catch (IntrospectionException ie) {
            Exceptions.printStackTrace(ie);
            return null;
        }
    }
}
