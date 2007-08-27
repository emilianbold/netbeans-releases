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

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import java.util.ArrayList;
import java.util.List;
import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;

/**
 * Implements both CsmFunction and CsmFunctionDefinition -
 * for those cases, when they coinside (i.e. implivit inlines)
 * @author Vladimir Kvasihn
 */
public class FunctionDDImpl<T> extends FunctionImpl<T> implements CsmFunctionDefinition<T> {
    
    private final CsmCompoundStatement body;

    public FunctionDDImpl(AST ast, CsmFile file, CsmScope scope) {
        super(ast, file, scope, false);
        body = AstRenderer.findCompoundStatement(ast, getContainingFile(), this);
        assert body != null : "null body in function definition, line " + getStartPosition().getLine() + ":" + file.getAbsolutePath();
        registerInProject();
    }

    public CsmCompoundStatement getBody() {
        return body;
    }

    public CsmFunction getDeclaration() {
        String uname = Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION) + UNIQUE_NAME_SEPARATOR + getUniqueNameWithoutPrefix();
        CsmProject prj = getContainingFile().getProject();
        CsmDeclaration decl = findDeclaration(prj, uname);
        if( decl != null && decl.getKind() == CsmDeclaration.Kind.FUNCTION ) {
            return (CsmFunction) decl;
        }
        for (CsmProject lib : prj.getLibraries()){
            CsmFunction def = findDeclaration(lib, uname);
            if (def != null) {
                return def;
            }
        }
        return this;
    }
    
    private CsmFunction findDeclaration(CsmProject prj, String uname){
        CsmDeclaration decl = prj.findDeclaration(uname);
        if( decl != null && decl.getKind() == CsmDeclaration.Kind.FUNCTION ) {
            return (CsmFunction) decl;
        }
        if (getParameters().size()!=0){
            CsmFile file = getContainingFile();
            if (!ProjectBase.isCppFile(file)){
                uname = uname.substring(0,uname.indexOf('('))+"()"; // NOI18N
                decl = prj.findDeclaration(uname);
                if( (decl instanceof FunctionImpl) &&
                        !((FunctionImpl)decl).isVoidParameterList()) {
                    return (CsmFunction) decl;
                }
            }
        }
        return null;
    }
    
    public CsmDeclaration.Kind getKind() {
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
        assert this.body != null: "null body in " + this.getQualifiedName();
        PersistentUtils.writeCompoundStatement(body, output);
    }
    
    public FunctionDDImpl(DataInput input) throws IOException {
        super(input);
        this.body = PersistentUtils.readCompoundStatement(input);
        assert this.body != null: "read null body for " + this.getName();
    }       
}

