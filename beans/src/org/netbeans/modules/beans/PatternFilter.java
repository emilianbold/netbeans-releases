/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
