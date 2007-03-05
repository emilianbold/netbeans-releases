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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.lexer;

/**
 * Whether {@link Token} represents a complete token
 * or just a part of a complete token.
 * <br/>
 * A complete token may consist of one start token, zero or more middle tokens
 * and zero or more end tokens (there may be incomplete token
 * at the end of input e.g. an incomplete block comment so there is just a start
 * part of a token).
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public enum PartType {

    /**
     * A given token represents a complete token.
     */
    COMPLETE,

    /**
     * A given token represents initial part of a complete token.
     */
    START,

    /**
     * A given token represents middle part of a complete token.
     */
    MIDDLE,

    /**
     * A given token represents end part of a complete token.
     */
    END;

}
