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

import org.openide.ServiceType;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/** BeanInfo for ServiceType. Has name property.
* @author Jesse Glick
*/
public class ServiceTypeBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor name = new PropertyDescriptor ("name", ServiceType.class); // NOI18N
            name.setDisplayName (NbBundle.getBundle (ServiceTypeBeanInfo.class).getString ("PROP_ServiceType_name"));
            name.setShortDescription (NbBundle.getBundle (ServiceTypeBeanInfo.class).getString ("HINT_ServiceType_name"));
            // Is there an easier way to prevent this from appearing??
            PropertyDescriptor helpCtx = new PropertyDescriptor ("helpCtx", ServiceType.class, "getHelpCtx", null); // NOI18N
            helpCtx.setHidden (true);
            return new PropertyDescriptor[] { name, helpCtx };
        } catch (IntrospectionException ie) {
            Exceptions.printStackTrace(ie);
            return null;
        }
    }

}
