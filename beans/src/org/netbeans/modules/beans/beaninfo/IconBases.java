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

package org.netbeans.modules.beans.beaninfo;

/** Resource string constants for pattern node icons.
*
* @author Petr Hrebejk
*/
interface IconBases {

    // Properties for Bean Info Features. There should be added S for selected features
    // and N for non selected features at the end of the string.

    public static final String BIF_DESCRIPTOR =
        "org/netbeans/modules/beans/resources/bifDescriptor"; // NOI18N !!! MUST BE CHANGED, BAD ICON

    public static final String BIF_PROPERTY_RW =
        "org/netbeans/modules/beans/resources/bifPropertyRW_"; // NOI18N

    public static final String BIF_PROPERTY_RO =
        "org/netbeans/modules/beans/resources/bifPropertyRO_"; // NOI18N

    public static final String BIF_PROPERTY_WO =
        "org/netbeans/modules/beans/resources/bifPropertyWO_"; // NOI18N

    public static final String BIF_IDXPROPERTY_RW =
        "org/netbeans/modules/beans/resources/bifIndexedPropertyRW_"; // NOI18N

    public static final String BIF_IDXPROPERTY_RO =
        "org/netbeans/modules/beans/resources/bifIndexedPropertyRO_"; // NOI18N

    public static final String BIF_IDXPROPERTY_WO =
        "org/netbeans/modules/beans/resources/bifIndexedPropertyWO_"; // NOI18N

    public static final String BIF_EVENTSET_MULTICAST =
        "org/netbeans/modules/beans/resources/bifEventSetMC_"; // NOI18N

    public static final String BIF_EVENTSET_UNICAST =
        "org/netbeans/modules/beans/resources/bifEventSetUC_"; // NOI18N

    public static final String BIF_METHOD =
        "org/netbeans/modules/beans/resources/bifMethod_"; // NOI18N

}
