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

package org.netbeans.modules.lexer.demo.antlr;

import org.netbeans.api.lexer.TokenCategory;
import org.netbeans.api.lexer.TokenId;

/**
 * Various utility methods over CalcLanguage.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class CalcLanguageUtilities {

    private static final CalcLanguage language = CalcLanguage.get();
    
    private static final TokenCategory errorCategory = language.getCategory("error");
    private static final TokenCategory incompleteCategory = language.getCategory("incomplete");
    private static final TokenCategory operatorCategory = language.getCategory("operator");
    
    private CalcLanguageUtilities() {
        // no instances
    }
 
    /**
     * Does the given tokenId represent an errorneous lexical construction?
     * @param id tokenId to check. It must be part of the CalcLanguage.
     * @return true if the id is in "error" category.
     */
    public static boolean isError(TokenId id) {
        return errorCategory.isMember(id);
    }

    /**
     * Does the given tokenId represent an incomplete token?
     * @param id tokenId to check. It must be part of the CalcLanguage.
     * @return true if the id is in "incomplete" category.
     */
    public static boolean isIncomplete(TokenId id) {
        return incompleteCategory.isMember(id);
    }

   /**
     * Does the given tokenId represent an operator?
     * @param id tokenId to check. It must be part of the CalcLanguage.
     * @return true if the id is in "operator" category.
     */
    public static boolean isOperator(TokenId id) {
        return operatorCategory.isMember(id);
    }
    
}
