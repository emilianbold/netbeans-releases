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

package org.openide.options;

import java.beans.*;

import org.openide.options.SystemOption;
import org.openide.util.Exceptions;

/** Empty bean info
*
* @author Jaroslav Tulach
*/
public class SystemOptionBeanInfo extends SimpleBeanInfo {
    /** No properties.
    * @return array of hidden properties
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor name = new PropertyDescriptor ("name", SystemOption.class, "getName", null);
            name.setHidden (true);
            PropertyDescriptor helpCtx = new PropertyDescriptor ("helpCtx", SystemOption.class, "getHelpCtx", null);
            helpCtx.setHidden (true);
            return new PropertyDescriptor[] { name, helpCtx };
        } catch (IntrospectionException ie) {
            Exceptions.printStackTrace(ie);
            return null;
        }
    }
}
