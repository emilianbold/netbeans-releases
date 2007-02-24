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
 * File       : IChangeableKind.java
 * Created on : Sep 16, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.core.primitivetypes;

/**
 * @author Aztec
 */
public interface IChangeableKind
{
    // Indicates that there is no restriction on adding new values, changing a value, or removing values to an occurence of a StructuralFeature.
    public static final int CK_UNRESTRICTED = 0;

    // Indicates that adding new values, changing values, and removing values or an occurence of a StructuralFeatureare all restricted.
    public static final int CK_RESTRICTED = 1;

    // Indicates that there is no restriction on adding new values to an occurence of a StructuralFeature, but changing and removing values are restricted.
    public static final int CK_ADDONLY = 2;

    // Indicates that there is no restriction on removing values from an occurence of a StructuralFeature, but adding new values and changing values are restricted.
    public static final int CK_REMOVEONLY = 3;
}
