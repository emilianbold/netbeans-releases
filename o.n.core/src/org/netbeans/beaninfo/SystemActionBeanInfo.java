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

/** Avoid fooling with SystemAction.icon, which is a messy property.
 * <pre>
 * java.lang.Error: PropertyDescriptor: internal error while merging PDs: type mismatch between read and write methods
 * 	at java.beans.PropertyDescriptor.<init>(PropertyDescriptor.java:343)
 * 	at java.beans.Introspector.processPropertyDescriptors(Introspector.java:610)
 * 	at java.beans.Introspector.getTargetPropertyInfo(Introspector.java:533)
 * 	at java.beans.Introspector.getBeanInfo(Introspector.java:361)
 * 	at java.beans.Introspector.getBeanInfo(Introspector.java:132)
 * 	at java.beans.Introspector.getBeanInfo(Introspector.java:193)
 * 	at java.beans.Introspector.<init>(Introspector.java:340)
 * 	at java.beans.Introspector.getBeanInfo(Introspector.java:132)
 * 	at java.beans.Introspector.getBeanInfo(Introspector.java:193)
 * 	at java.beans.Introspector.<init>(Introspector.java:340)
 * 	at java.beans.Introspector.getBeanInfo(Introspector.java:132)
 * 	at org.openide.util.Utilities.getBeanInfo(Unknown Source)
 * 	at org.openide.loaders.InstanceNode.initIcon(InstanceNode.java:217)
 * 	at org.openide.loaders.InstanceNode.getIcon(InstanceNode.java:162)
 * </pre>
 * @author Jesse Glick
 */
public class SystemActionBeanInfo extends SimpleBeanInfo {
    public PropertyDescriptor[] getPropertyDescriptors () {
        return new PropertyDescriptor[] {};
    }
}
