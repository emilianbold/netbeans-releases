/*
 * SLanguageProvider.java
 *
 * Created on October 17, 2006, 10:10 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.languages.lexer;

import org.netbeans.api.lexer.TokenId;


/**
 *
 * @author Jan Jancura
 */
public class STokenId implements TokenId {
    
    private String  name;
    private int     ordinal;
    private String  primaryCategory;
    
    STokenId (
        String  name,
        int     ordinal, 
        String  primaryCategory
    ) {
        this.name = name;
        this.ordinal = ordinal;
        this.primaryCategory = primaryCategory;
    }
    
    public String name () {
        return name;
    }

    public int ordinal () {
        return ordinal;
    }

    public String primaryCategory () {
        return primaryCategory;
    }
}
