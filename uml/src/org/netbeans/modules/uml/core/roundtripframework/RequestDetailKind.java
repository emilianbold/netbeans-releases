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
 * File       : RequestDetailKind.java
 * Created on : Oct 28, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

/**
 * @author Aztec
 */
public interface RequestDetailKind
{
    public static final int RDT_NONE = 0;
    public static final int RDT_DOCUMENTATION_MODIFIED = 1;
    public static final int RDT_ELEMENT_DELETED = 2;
    public static final int RDT_NAME_MODIFIED = 3;
    public static final int RDT_VISIBILITY_MODIFIED = 4;
    public static final int RDT_ELEMENT_ADDED_TO_NAMESPACE = 5;
    public static final int RDT_RELATION_VALIDATE = 6;
    public static final int RDT_RELATION_MODIFIED = 7;
    public static final int RDT_RELATION_DELETED = 8;
    public static final int RDT_ATTRIBUTE_DEFAULT_MODIFIED = 9;
    public static final int RDT_ATTRIBUTE_DEFAULT_BODY_MODIFIED = 10;
    public static final int RDT_ATTRIBUTE_DEFAULT_LANGUAGE_MODIFIED = 11;
    public static final int RDT_CONCURRENCY_MODIFIED = 12;
    public static final int RDT_SIGNAL_ADDED = 13;
    public static final int RDT_SIGNAL_REMOVED = 14;
    public static final int RDT_PARAMETER_ADDED = 15;
    public static final int RDT_PARAMETER_REMOVED = 16;
    public static final int RDT_ABSTRACT_MODIFIED = 17;
    public static final int RDT_FEATURE_ADDED = 18;
    public static final int RDT_FEATURE_REMOVED = 19;
    public static final int RDT_STATIC_MODIFIED = 20;
    public static final int RDT_CONDITION_ADDED = 21;
    public static final int RDT_CONDITION_REMOVED = 22;
    public static final int RDT_QUERY_MODIFIED = 23;
    public static final int RDT_PARAMETER_DEFAULT_MODIFIED = 24;
    public static final int RDT_PARAMETER_DEFAULT_BODY_MODIFIED = 25;
    public static final int RDT_PARAMETER_DEFAULT_LANGUAGE_MODIFIED = 26;
    public static final int RDT_PARAMETER_DIRECTION_MODIFIED = 27;
    public static final int RDT_CHANGEABILITY_MODIFIED = 28;
    public static final int RDT_MULTIPLICITY_MODIFIED = 29;
    public static final int RDT_TYPE_MODIFIED = 30;
    public static final int RDT_LOWER_MODIFIED = 31;
    public static final int RDT_UPPER_MODIFIED = 32;
    public static final int RDT_RANGE_ADDED = 33;
    public static final int RDT_RANGE_REMOVED = 34;
    public static final int RDT_ORDER_MODIFIED = 35;
    public static final int RDT_PACKAGE_NAME_MODIFIED = 36;
    public static final int RDT_TRANSIENT_MODIFIED = 37;
    public static final int RDT_NATIVE_MODIFIED = 38;
    public static final int RDT_VOLATILE_MODIFIED = 39;
    public static final int RDT_LEAF_MODIFIED = 40;
    public static final int RDT_RELATION_END_MODIFIED = 41;
    public static final int RDT_RELATION_END_ADDED = 42;
    public static final int RDT_RELATION_END_REMOVED = 43;
    public static final int RDT_DEPENDENCY_ADDED = 44;
    public static final int RDT_DEPENDENCY_REMOVED = 45;
    public static final int RDT_ASSOCIATION_END_MODIFIED = 46;
    public static final int RDT_ASSOCIATION_END_ADDED = 47;
    public static final int RDT_ASSOCIATION_END_REMOVED = 48;
    public static final int RDT_RELATION_CREATED = 49;
    public static final int RDT_FEATURE_MOVED = 50;
    public static final int RDT_FEATURE_DUPLICATED = 51;
    public static final int RDT_NAMESPACE_MODIFIED = 52;
    public static final int RDT_CHANGED_NAMESPACE = 53;
    public static final int RDT_NAMESPACE_MOVED = 54;
    public static final int RDT_FINAL_MODIFIED = 55;
    public static final int RDT_STRICTFP_MODIFIED = 56;
    public static final int RDT_MULTIPLE_PARAMETER_TYPE_MODIFIED = 57;
    public static final int RDT_TRANSFORM = 58;
    public static final int RDT_EXCEPTION_ADDED = 59;
    public static final int RDT_EXCEPTION_REMOVED = 60;
    public static final int RDT_SIGNATURE_CHANGED = 61;
    public static final int RDT_SOURCE_DIR_CHANGED = 62;
    public static final int RDT_OPERATION_PROPERTY_CHANGED = 63;
}
