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

package org.netbeans.spi.jumpto.type;


/**
 * This enum describes the different kinds of searches that the Go To Type
 * dialog wants to perform on a type provider.
 * 
 * 
 * @author Tor Norbye
 */
public enum SearchType {
    /**
     * A search using an exact name of the type name
     * 
     * This is not yet used but the Go To Type dialog, but it seems plausible 
     * that it could be.
     */
    EXACT_NAME, // was: SIMPLE_NAME

    /**
     * A search using a case-sensitive prefix of the type name
     */
    PREFIX,

    /**
     * A search using a case-insensitive prefix of the type name
     */
    CASE_INSENSITIVE_PREFIX,

    /**
     * A search using a camel-case reduction of the type name
     */
    CAMEL_CASE,


    /**
     * A search using a case-sensitive
     * regular expression which should match the type name
     */
    REGEXP,

    /**
     * A search using a case-insensitive
     * regular expression which should match the type name
     */
    CASE_INSENSITIVE_REGEXP
}
