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

package org.netbeans.modules.cnd.modelimpl.csm;

import antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;

import org.netbeans.modules.cnd.modelimpl.csm.core.*;

/**
 * CsmEnumerator implementation
 * @author Vladimir Kvashin
 */
public class EnumeratorImpl extends OffsetableDeclarationBase implements CsmEnumerator {
    private final String name;
    private final CsmEnum enumeration;    

    public EnumeratorImpl(AST ast, EnumImpl enumeration) {
        super(ast, enumeration.getContainingFile());
        this.name = ast.getText();
        this.enumeration = enumeration;
        enumeration.addEnumerator(this);
    }
    
    public String getName() {
        return name;
    }

    public CsmExpression getExplicitValue() {
        return null;
    }

    public CsmEnum getEnumeration() {
        return enumeration;
    }
    
    public CsmScope getScope() {
        return getEnumeration();
    }

    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.ENUMERATOR;
    }

    public String getQualifiedName() {
	    return enumeration.getQualifiedName() + "::" + getName(); // NOI18N    
    }
}
