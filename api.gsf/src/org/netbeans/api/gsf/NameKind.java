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
package org.netbeans.api.gsf;


/**
 * Encodes a type of the name kind used by
 * {@link ClassIndex#getDeclaredTypes} method.
 *
 */
public enum NameKind {
    /**
     * The name parameter of the {@link ClassIndex#getDeclaredTypes}
     * is an exact simple name of the package or declared type.
     */
    EXACT_NAME,
    /**
     * The name parameter of the {@link ClassIndex#getDeclaredTypes}
     * is an case sensitive prefix of the package or declared type name.
     */
    PREFIX,
    /**
     * The name parameter of the {@link ClassIndex#getDeclaredTypes} is
     * an case insensitive prefix of the declared type name.
     */
    CASE_INSENSITIVE_PREFIX,
    /**
     * The name parameter of the {@link ClassIndex#getDeclaredTypes} is
     * an camel case of the declared type name.
     */
    CAMEL_CASE,
    /**
     * The name parameter of the {@link ClassIndex#getDeclaredTypes} is
     * an regular expression of the declared type name.
     */
    REGEXP,
    /**
     * The name parameter of the {@link ClassIndex#getDeclaredTypes} is
     * an case insensitive regular expression of the declared type name.
     */
    CASE_INSENSITIVE_REGEXP;
}
