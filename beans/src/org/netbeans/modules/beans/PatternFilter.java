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

/** Orders and filters members in a pattern node.
*
* @author Petr Hrebejk
*/
public final class PatternFilter {

    /** Specifies a child representing a property. */
    public static final int     PROPERTY = 256;
    /** Specifies a child representing a indexed property */
    public static final int     IDXPROPERTY = 512;
    /** Specifies a child representing a event listener. */
    public static final int     EVENT_SET = 1024;
    /** Specifies a child representing a method. */

    /** Does not specify a child type. */
    public static final int     ALL = PROPERTY | IDXPROPERTY | EVENT_SET;

    /** Default order and filtering.
    * Places all fields, constructors, methods, and inner classes (interfaces) together
    * in one block.
    */
    public static final int[]   DEFAULT_ORDER = {PROPERTY | IDXPROPERTY | EVENT_SET};

}
