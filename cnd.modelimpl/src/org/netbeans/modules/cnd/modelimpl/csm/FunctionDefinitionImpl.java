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
import java.util.*;
import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 * @author Vladimir Kvasihn
 */
public class FunctionDefinitionImpl<T> extends FunctionImplEx<T> implements CsmFunctionDefinition<T> {

    private CsmFunction declarationOLD;
    private CsmUID<CsmFunction> declarationUID;
    
    private final CsmCompoundStatement body;
    
    public FunctionDefinitionImpl(AST ast, CsmFile file, CsmScope scope) {
        this(ast, file, scope, true);
    }
    
    protected  FunctionDefinitionImpl(AST ast, CsmFile file, CsmScope scope, boolean register) {
        super(ast, file, scope, false);
        body = AstRenderer.findCompoundStatement(ast, getContainingFile(), this);
        assert body != null : "null body in function definition, line " + getStartPosition().getLine() + ":" + file.getAbsolutePath();
        if (register) {
            registerInProject();
        }
    }
    
    public CsmCompoundStatement getBody() {
        return body;
    }

    public CsmFunction getDeclaration() {
        return getDeclaration(null);
    }
    
    public CsmFunction getDeclaration(Resolver parent) {
        CsmFunction declaration = _getDeclaration();
	if( declaration == null ) {
            _setDeclaration(null);
	    declaration = findDeclaration(parent);
            _setDeclaration(declaration);
	}
	return declaration;
    }
    
    private CsmFunction _getDeclaration() {
        if (TraceFlags.USE_REPOSITORY) {
            CsmFunction decl = UIDCsmConverter.UIDtoDeclaration(this.declarationUID);
            // null object is OK here, because of changed cached reference
            return decl;
        } else {
            return this.declarationOLD;
        }
    }
    
    private void _setDeclaration(CsmFunction decl) {
        if (TraceFlags.USE_REPOSITORY) {
            this.declarationUID = UIDCsmConverter.declarationToUID(decl);
            assert this.declarationUID != null || decl == null;
        } else {
            this.declarationOLD = decl;
        }
    }
    
    private CsmFunction findDeclaration(Resolver parent) {
        String uname = Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION) + UNIQUE_NAME_SEPARATOR + getUniqueNameWithoutPrefix();
        CsmDeclaration def = getContainingFile().getProject().findDeclaration(uname);
	if( def == null ) {
	    CsmObject owner = findOwner(parent);
	    if( owner instanceof CsmClass ) {
		def = findByName(((CsmClass) owner).getMembers(), getName());
	    }
	    else if( owner instanceof CsmNamespace ) {
		def = findByName(((CsmNamespace) owner).getDeclarations(), getName());
	    }
	}
        return (CsmFunction) def;
    }
    
    private static CsmFunction findByName(Collection/*CsmDeclaration*/ declarations, String name) {
	for (Iterator it = declarations.iterator(); it.hasNext();) {
	    CsmDeclaration decl = (CsmDeclaration) it.next();
	    if( decl.getName().equals(name) ) {
		if( decl instanceof  CsmFunction ) { // paranoja
		    return (CsmFunction) decl;
		}
	    }	
	}
	return null;
    }
    
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.FUNCTION_DEFINITION;
    }

    protected String findQualifiedName() {
        CsmFunction declaration= _getDeclaration();
	if( declaration != null ) {
	    return declaration.getQualifiedName();
	}
	else {
	    return super.findQualifiedName();
	}
    }
    
    public CsmScope getScope() {
        return getContainingFile();
    }

    public List<CsmScopeElement> getScopeElements() {
        List<CsmScopeElement> l = super.getScopeElements();
        l.add(getBody());
        return l;
    }

    public CsmFunctionDefinition getDefinition() {
        return this;
    }
  
    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent
    
    public void write(DataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeCompoundStatement(this.body, output);
        
        // save cached declaration
        UIDObjectFactory.getDefaultFactory().writeUID(this.declarationUID, output);
    }
    
    public FunctionDefinitionImpl(DataInput input) throws IOException {
        super(input);
        this.body = PersistentUtils.readCompoundStatement(input);
        
        // read cached declaration
        this.declarationUID = UIDObjectFactory.getDefaultFactory().readUID(input);        
    }      
}
