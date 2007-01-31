/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.apt.impl.support;

import antlr.Token;
import antlr.TokenStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.utils.APTCommentsFilter;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.apt.utils.ListBasedTokenStream;

/**
 * implementation of APTMacro
 * @author Vladimir Voskresensky
 */
public class APTMacroImpl implements APTMacro {
    private final Token name;
    private final Collection params;
    private final List/*<Token>*/ body;
    private final boolean system;

    public APTMacroImpl(Token name, Collection params, List/*<Token>*/ body, boolean system) {
        assert (name != null);
        this.name = name;
        this.params = params;
        this.body = body;
        this.system = system;
    }

    public boolean isSystem() {
        return system;
    }

    public boolean isFunctionLike() {
        return params != null;
    }

    public Token getName() {
        return name;
    }

    public Collection getParams() {
        return params;
    }

    public TokenStream getBody() {
        return body != null ? new ListBasedTokenStream(body) : APTUtils.EMPTY_STREAM;
    }
    
    public boolean equals(Object obj) {
        boolean retValue;
        if (obj == null || !(obj instanceof APTMacro)) {
            retValue = false;
        } else {
            APTMacro other = (APTMacro)obj;
            retValue = APTMacroImpl.equals(this, other);
        }
        return retValue;
    }
    
    private static final boolean equals(APTMacro one, APTMacro other) {
        // compare only name
        return (one.getName().getText().compareTo(other.getName().getText()) == 0);
    }
    
    public int hashCode() {
        int retValue = 17;
        retValue = 31*retValue + getName().getText().hashCode();
        return retValue;
    }       

    public String toString() {
        StringBuffer retValue = new StringBuffer();
        retValue.append(isSystem() ? "<S>":"<U>"); // NOI18N
        retValue.append("#define '"); // NOI18N
        retValue.append(getName());
        if (params != null) {
            retValue.append("["); // NOI18N
            for (Iterator it = params.iterator();it.hasNext();) {
                Token elem = (Token) it.next();
                retValue.append(elem);
                if (it.hasNext()) {
                    retValue.append(", "); // NOI18N
                } else {
                    break;
                } 
            }
            retValue.append("]"); // NOI18N
        }
        TokenStream body = getBody();
        if (body != null) {
            retValue.append("'='"); // NOI18N
            retValue.append(APTUtils.toString(body));
        }
        return retValue.toString();
    }       
}
