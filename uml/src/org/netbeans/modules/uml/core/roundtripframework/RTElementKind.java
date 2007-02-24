/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

/*
 * File       : RTElementKind.java
 * Created on : Oct 28, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

/**
 * @author Aztec
 */
public interface RTElementKind
{
    public static final int RCT_CLASS = 0;
    public static final int RCT_ATTRIBUTE = 1;
    public static final int RCT_OPERATION = 2;
    public static final int RCT_PACKAGE = 3;
    public static final int RCT_RELATION = 4;
    public static final int RCT_PARAMETER = 5;
    public static final int RCT_INTERFACE = 6;
    public static final int RCT_NONE = 7;
    public static final int RCT_NAVIGABLE_END_ATTRIBUTE = 8;
    public static final int RCT_ENUMERATION = 9;
    public static final int RCT_ENUMERATION_LITERAL = 10;
    public static final int RCT_TEMPLATE_PARAMETER = 11;
}
