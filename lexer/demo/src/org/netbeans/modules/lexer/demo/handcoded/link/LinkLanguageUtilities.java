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
