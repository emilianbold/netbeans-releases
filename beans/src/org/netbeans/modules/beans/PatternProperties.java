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

package org.netbeans.modules.beans;

/** Names of properties of patterns.
*
*
* @author Petr Hrebejk
*/
public interface PatternProperties {
    /** Name of type property for all {@link PropertyPattern}s.
    */
    public static final String PROP_TYPE = "type"; // NOI18N

    public static final String PROP_MODE = "mode"; // NOI18N

    public static final String PROP_NAME = "name"; // NOI18N

    public static final String PROP_GETTER = "getter"; // NOI18N

    public static final String PROP_SETTER = "setter"; // NOI18N

    public static final String PROP_ESTIMATEDFIELD = "estimatedField"; // NOI18N

    public static final String PROP_INDEXEDTYPE = "indexedType"; // NOI18N

    public static final String PROP_INDEXEDGETTER = "indexedGetter"; // NOI18N

    public static final String PROP_INDEXEDSETTER = "indexedSetter"; // NOI18N

    public static final String PROP_ADDLISTENER = "addListener"; // NOI18N

    public static final String PROP_REMOVELISTENER = "removeListener"; // NOI18N

    public static final String PROP_ISUNICAST = "isUnicast"; // NOI18N
}
