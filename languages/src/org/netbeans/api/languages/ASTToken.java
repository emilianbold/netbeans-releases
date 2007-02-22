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
    
    
    public static ASTToken create (
        String  type,
        String  identifier,
        int     offset,
        int     length,
        List     children
    ) {
        return new ASTToken (
            null,
            type, 
            identifier, 
            offset, 
            length,
            children
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
        List    children
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
        List<ASTItem> children
    ) {
        super (mimeType, offset, length, children);
        this.identifier = identifier;
        this.type = type;
    }

    public String getType () {
        return type;
    }
    
    public String getIdentifier () {
        return identifier;
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
                   append (e (identifier)).
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