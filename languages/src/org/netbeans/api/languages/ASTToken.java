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

import java.util.Collections;
import java.util.List;

public class ASTToken extends ASTItem {
    
    private String      identifier;
    private String      type;
    private List         mimeTypeToTokens;
    
    
    public static ASTToken create (
        String  type,
        String  identifier,
        int     offset,
        int     length,
        List     mimeTypeToTokens
    ) {
        return new ASTToken (
            null,
            type, 
            identifier, 
            offset, 
            length,
            mimeTypeToTokens
        );
    }
    
    public static ASTToken create (
        String  type,
        String  identifier,
        int     offset
    ) {
        return new ASTToken (
            null,
            type, 
            identifier, 
            offset, 
            identifier == null ? 0 : identifier.length (),
            null
        );
    }
    
    public static ASTToken create (
        String  type,
        String  identifier
    ) {
        return new ASTToken (
            null,
            type, 
            identifier, 
            0, 
            identifier == null ? 0 : identifier.length (),
            null
        );
    }
    
    public static ASTToken create (
        String  mimeType,
        String  type,
        String  identifier,
        int     offset,
        int     length,
        List    mimeTypeToTokens
    ) {
        return new ASTToken (
            mimeType,
            type, 
            identifier, 
            offset, 
            length,
            mimeTypeToTokens
        );
    }
    
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
    
    public static ASTToken create (
        String  mimeType,
        String  type,
        String  identifier
    ) {
        return new ASTToken (
            mimeType,
            type, 
            identifier, 
            0, 
            identifier == null ? 0 : identifier.length (),
            null
        );
    }
    
    private ASTToken (
        String  mimeType,
        String  type, 
        String  identifier, 
        int     offset,
        int     length,
        List    mimeTypeToTokens
    ) {
        super(mimeType, offset, length, Collections.<ASTItem>emptyList ());
        this.identifier = identifier;
        this.type = type;
        this.mimeTypeToTokens = mimeTypeToTokens;
    }

    public String getType () {
        return type;
    }
    
    public String getIdentifier () {
        return identifier;
    }
    
    public List getEmbeddings () {
        if (mimeTypeToTokens == null) return Collections.EMPTY_LIST;
        return mimeTypeToTokens;
    }
    
    public boolean equals (Object o) {
        Thread.dumpStack();
        return super.equals (o);
    }

    public boolean isCompatible (Object obj) {
        if (!(obj instanceof ASTToken)) return false;
        if (type != null && 
            ((ASTToken) obj).type != null &&
            !type.equals (((ASTToken) obj).type)
        ) return false;
        if (identifier == null || ((ASTToken) obj).identifier == null) return true;
        return identifier.equals (((ASTToken) obj).identifier);
    }
    
    private String toString;
    public String toString () {
        if (toString == null) {
            StringBuffer sb = new StringBuffer ();
            sb.append ('<').append (type);
            if (identifier != null)
                sb.append (",'").
                   append (identifier).
                   append ("'");
            sb.append ('>');
            toString = sb.toString ();
        }
        return toString;
    }
}