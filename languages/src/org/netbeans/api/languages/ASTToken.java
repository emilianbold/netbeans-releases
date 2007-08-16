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

package org.netbeans.api.languages;

import java.util.List;

/**
 * Represents one token in AST.
 */
public final class ASTToken extends ASTItem {
    
    private String      identifier;
    private CharSequence identifierCharSequence;
    private String      type;
    
    
    /**
     * Creates new token with given parameters.
     * 
     * @param mimeType a mime type of token 
     * @param type a type of token 
     * @param identifier token identifier
     * @param offset token offset
     * @param length token length
     * @param children a list of token children
     * @return new ASTToken
     */
    public static ASTToken create (
        String          mimeType,
        String          type,
        CharSequence    identifier,
        int             offset,
        int             length,
        List<? extends ASTItem> children
    ) {
        return new ASTToken (
            mimeType,
            type, 
            identifier, 
            offset, 
            length,
            children
        );
    }
    
    
    /**
     * Creates new token with given parameters, no children and length 
     * derived from identifier.
     * 
     * @param mimeType a mime type of token 
     * @param type a type of token 
     * @param identifier token identifier
     * @param offset token offset
     * @return new ASTToken
     */
    public static ASTToken create (
        String  mimeType,
        String  type,
        String  identifier,
        int     offset
    ) {
        return new ASTToken (
            mimeType,
            type, 
            identifier, 
            offset, 
            identifier == null ? 0 : identifier.length (),
            null
        );
    }

    
    private ASTToken (
        String                  mimeType,
        String                  type, 
        CharSequence            identifier, 
        int                     offset,
        int                     length,
        List<? extends ASTItem> children
    ) {
        super (mimeType, offset, length, children);
        if (identifier instanceof String)
            this.identifier = (String) identifier;
        else
            this.identifierCharSequence = identifier;
        this.type = type;
    }

    /**
     * Retruns type of token.
     * 
     * @return type of token
     */
    public String getType () {
        return type;
    }
    

    /**
     * Retruns token identifier.
     * 
     * @return token identifier
     */
    public String getIdentifier () {
        if (identifier != null) return identifier;
        if (identifierCharSequence != null)
            identifier = identifierCharSequence.toString ();
        return identifier;
    }
    
    private String toString;

    /**
     * Retruns string representation of this token.
     * 
     * @return string representation of this token
     */
    public String toString () {
        if (toString == null) {
            StringBuffer sb = new StringBuffer ();
            sb.append ('<').append (type);
            if (identifier != null)
                sb.append (",'").
                   append (e (identifier)).
                   append ("'");
            else
            if (identifierCharSequence != null)
                sb.append (",'").
                   append (e (identifierCharSequence)).
                   append ("'");
            sb.append ('>');
            toString = sb.toString ();
        }
        return toString;
    }
        
    private static String e (CharSequence t) {
        StringBuilder sb = new StringBuilder ();
        int i, k = t.length ();
        for (i = 0; i < k; i++) {
            if (t.charAt (i) == '\t')
                sb.append ("\\t");
            else
            if (t.charAt (i) == '\r')
                sb.append ("\\r");
            else
            if (t.charAt (i) == '\n')
                sb.append ("\\n");
            else
                sb.append (t.charAt (i));
        }
        return sb.toString ();
    }
}