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

package org.netbeans.api.java.lexer;

import org.netbeans.api.lexer.TokenId;

/**
 * Token ids for javadoc language (embedded in javadoc comments).
 *
 * @author Miloslav Metelka
 * @version 1.00
 */
public enum JavadocTokenId implements TokenId {

    IDENT("comment"),
    TAG("javadoc-tag"),
    HTML_TAG("html-tag"),
    DOT("comment"),
    HASH("comment"),
    OTHER_TEXT("comment");

    private final String primaryCategory;

    JavadocTokenId() {
        this(null);
    }

    JavadocTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }

}
