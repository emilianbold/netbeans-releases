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
