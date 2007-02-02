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

package org.netbeans.modules.lexer.demo.handcoded.link;

import org.netbeans.api.lexer.TokenCategory;
import org.netbeans.api.lexer.TokenId;

/**
 * Various utility methods over LinkLanguage.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class LinkLanguageUtilities {

    private static final LinkLanguage language = LinkLanguage.get();

    private static final TokenCategory linkCategory = language.getCategory("link");


    private LinkLanguageUtilities() {
        // no instances
    }

    /**
     * Does the given tokenId represent a link token?
     * @param id tokenId to check. It must be part of the LinkLanguage.
     * @return true if the id is in link category.
     */
    public static boolean isLink(TokenId id) {
        return linkCategory.isMember(id);
    }

}
