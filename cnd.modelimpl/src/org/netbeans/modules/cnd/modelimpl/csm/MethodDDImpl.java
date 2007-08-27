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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.csm;

import java.util.*;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.api.model.deep.*;
import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;

/**
 * Method, which contains it's body right at throws POD (point of declaration)
 * @author Vladimir Kvasihn
 */
public class MethodDDImpl<T> extends MethodImpl<T> implements CsmFunctionDefinition<T> {

    private final CsmCompoundStatement body;
    
    public MethodDDImpl(AST ast, ClassImpl cls, CsmVisibility visibility) {
        this(ast, cls, visibility, true);
    }
    
    protected MethodDDImpl(AST ast, ClassImpl cls, CsmVisibility visibility, boolean register) {
        super(ast, cls, visibility, false);
        body = AstRenderer.findCompoundStatement(ast, getContainingFile(), this);
        assert body != null : "null body in function definition, line " + getStartPosition().getLine() + ":" + getContainingFile().getAbsolutePath();
        if (register) {
            registerInProject();
        }
    }

    public CsmFunction getDeclaration() {
        return this;
    }

    public CsmCompoundStatement getBody() {
        return body;
    }
    
    public Kind getKind() {
        return CsmDeclaration.Kind.FUNCTION_DEFINITION;
    }
    
    public List<CsmScopeElement> getScopeElements() {
        List<CsmScopeElement> l = super.getScopeElements();
        l.add(getBody());
        return l;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent
    
    public void write(DataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeCompoundStatement(this.body, output);
    }
    
    public MethodDDImpl(DataInput input) throws IOException {
        super(input);
        this.body = PersistentUtils.readCompoundStatement(input);
    }     
}
