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
import org.netbeans.modules.cnd.apt.utils.TextCache;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 * A class that 
 * 1) represents function template specilaization declaration (without body)
 * 2) acts as a base class for FunctionDefinitionImpl.
 * In other words, it corresponds to function that has a double colon "::" in its name
 * @author Vladimir Kvashin
 */
public class FunctionImplEx<T>  extends FunctionImpl<T> {

    private String qualifiedName;
    private boolean qualifiedNameIsFake = false;
    private final String[] classOrNspNames;
    
    public FunctionImplEx(AST ast, CsmFile file, CsmScope scope) {
        this(ast, file, scope, true);
    }
    
    protected  FunctionImplEx(AST ast, CsmFile file, CsmScope scope, boolean register) {
        super(ast, file, scope, false);
        classOrNspNames = initClassOrNspNames(ast);
        if (register) {
            registerInProject();
        }
    }
    

    /** @return either class or namespace */
    protected CsmObject findOwner() {
	String[] cnn = classOrNspNames;
	if( cnn != null ) {
	    CsmObject obj = ResolverFactory.createResolver(this).resolve(cnn);
	    if( obj instanceof CsmClass ) {
		if( !( obj instanceof Unresolved.UnresolvedClass) ) {
		    return (CsmClass) obj;
		}
	    }
	    else if( obj instanceof CsmNamespace ) {
		return (CsmNamespace) obj;
	    }
	}
	return null;
    }    
    
    private static String[] initClassOrNspNames(AST node) {
        //qualified id
        AST qid = AstUtil.findMethodName(node);
        if( qid == null ) {
            return null;
        }
        int cnt = qid.getNumberOfChildren();
        if( cnt >= 1 ) {
            List/*<String>*/ l = new ArrayList/*<String>*/();
            for( AST token = qid.getFirstChild(); token != null; token = token.getNextSibling() ) {
                if( token.getType() == CPPTokenTypes.ID ) {
                    if( token.getNextSibling() != null ) {
                        l.add(token.getText());
                    }
                }
            }
            return (String[]) l.toArray(new String[l.size()]);
        }
        return null;
    }
    
    public String getQualifiedName() {
	if( qualifiedName == null ) {
	    qualifiedName = findQualifiedName();
	}
	return qualifiedName;
    }
    
    protected String findQualifiedName() {
	CsmObject owner = findOwner();
	if( owner instanceof CsmQualifiedNamedElement  ) {
	    qualifiedNameIsFake = false;
	    return ((CsmQualifiedNamedElement) owner).getQualifiedName() + "::" + getQualifiedNamePostfix(); // NOI18N
	}
	else {
	    qualifiedNameIsFake = true;
	    String[] cnn = classOrNspNames;
	    CsmNamespaceDefinition nsd = findNamespaceDefinition();
	    StringBuffer sb = new StringBuffer();
	    if( nsd != null ) {
		sb.append(nsd.getQualifiedName());
	    }
	    if( cnn != null ) {
		for (int i = 0; i < cnn.length; i++) {
		    if( sb.length() > 0 ) {
			sb.append("::"); // NOI18N
		    }
		    sb.append(cnn[i]);
		}
	    }
	    if( sb.length() == 0 ) {
		sb.append("unknown>"); // NOI18N
	    }
	    sb.append("::"); // NOI18N
	    sb.append(getQualifiedNamePostfix());
	    return sb.toString();
	}
    }
    
    protected void registerInProject() {
	super.registerInProject();
	if( qualifiedNameIsFake ) {
	    ((FileImpl) getContainingFile()).onFakeRegisration(this);
	}
    }
    
    public void fixFakeRegistration() {
	String newQname = findQualifiedName();
	if( ! newQname.equals(qualifiedName) ) {
	    ((FileImpl) getContainingFile()).getProjectImpl().unregisterDeclaration(this);
            this.cleanUID();
	    qualifiedName = newQname;
	    ((FileImpl) getContainingFile()).getProjectImpl().registerDeclaration(this);
	}
    }
    
    private CsmNamespaceDefinition findNamespaceDefinition() {
	return findNamespaceDefinition(getContainingFile().getDeclarations());
    }
    
    private CsmNamespaceDefinition findNamespaceDefinition(Collection/*<CsmOffsetableDeclaration>*/ declarations) {
	for (Iterator it = declarations.iterator(); it.hasNext();) {
	    CsmOffsetableDeclaration decl = (CsmOffsetableDeclaration) it.next();
	    if( decl.getStartOffset() > this.getStartOffset() ) {
		break;
	    }
	    if( decl.getKind() == CsmDeclaration.Kind.NAMESPACE_DEFINITION ) {
		if( this.getEndOffset() < decl.getEndOffset() ) {
		    CsmNamespaceDefinition nsdef = (CsmNamespaceDefinition) decl;
		    CsmNamespaceDefinition inner = findNamespaceDefinition(nsdef.getDeclarations());
		    return (inner == null) ? nsdef : inner;
		}
	    }
	}
	return null;
    }    
    
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    public void write(DataOutput output) throws IOException {
        super.write(output);
        assert this.qualifiedName != null;
        output.writeUTF(this.qualifiedName);
        output.writeBoolean(this.qualifiedNameIsFake);
        PersistentUtils.writeStrings(this.classOrNspNames, output);
    }
    
    public FunctionImplEx(DataInput input) throws IOException {
	super(input);
        this.qualifiedName = input.readUTF();
        assert this.qualifiedName != null;
        this.qualifiedNameIsFake = input.readBoolean();
        this.classOrNspNames = PersistentUtils.readStrings(input, TextCache.getManager());
    }
}
