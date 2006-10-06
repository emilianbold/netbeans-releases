/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.lexer.nbbridge.test.simple;

import org.netbeans.api.lexer.TokenId;

/**
 * Token identifications of the simple plain language.
 *
 * @author mmetelka
 */
public enum SimpleCharTokenId implements TokenId {

    CHARACTER,
    DIGIT;

    SimpleCharTokenId() {
    }

    public String primaryCategory() {
        return "chars";
    }

}
