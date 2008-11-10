/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.modelimpl.csm;

import org.netbeans.modules.cnd.api.model.*;
import java.util.*;
import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;

/**
 * A class that 
 * 1) represents function template specilaization declaration (without body)
 * 2) acts as a base class for FunctionDefinitionImpl.
 * In other words, it corresponds to function that has a double colon "::" in its name
 * @param T 
 * @author Vladimir Kvashin
 */
public class FunctionImplEx<T>  extends FunctionImpl<T> {

    private CharSequence qualifiedName;
    private boolean qualifiedNameIsFake = false;
    private final CharSequence[] classOrNspNames;   
    private AST fixFakeRegistrationAst = null; // AST for fixing fake registrations
    
    public FunctionImplEx(AST ast, CsmFile file, CsmScope scope) throws AstRendererException {
        this(ast, file, scope, true);
    }

    public FunctionImplEx(AST ast, CsmFile file, CsmScope scope, boolean register, boolean likeVariable) throws AstRendererException {
        this(ast, file, scope, register);
        if(likeVariable) {
            fixFakeRegistrationAst = ast;
        }
    }
    
    protected  FunctionImplEx(AST ast, CsmFile file, CsmScope scope, boolean register) throws AstRendererException {
        super(ast, file, scope, false);
        classOrNspNames = CastUtils.isCast(ast) ? CastUtils.getClassOrNspNames(ast) : initClassOrNspNames(ast);
        if (register) {
            registerInProject();
        }
    }
    

    /** @return either class or namespace */
    protected CsmObject findOwner(Resolver parent) {
	CharSequence[] cnn = classOrNspNames;
	if( cnn != null ) {
	    CsmObject obj = ResolverFactory.createResolver(this, parent).resolve(cnn, Resolver.CLASSIFIER | Resolver.NAMESPACE);
	    if( obj instanceof CsmClass ) {
                return (CsmClass) obj;
	    }
	    else if( obj instanceof CsmNamespace ) {
		return (CsmNamespace) obj;
	    }
	}
	return null;
    }    
    
    public static String[] initClassOrNspNames(AST node) {
        //qualified id
        AST qid = AstUtil.findMethodName(node);
        if( qid == null ) {
            return null;
        }
        int cnt = qid.getNumberOfChildren();
        if( cnt >= 1 ) {
            List<String> l = new ArrayList<String>();
            for( AST token = qid.getFirstChild(); token != null; token = token.getNextSibling() ) {
                if( token.getType() == CPPTokenTypes.ID ) {
                    if( token.getNextSibling() != null ) {
                        l.add(token.getText());
                    }
                }
            }
            return l.toArray(new String[l.size()]);
        }
        return null;
    }
    
    @Override
    public CharSequence getQualifiedName() {
	if( qualifiedName == null ) {
	    qualifiedName = QualifiedNameCache.getManager().getString(findQualifiedName());
	}
	return qualifiedName;
    }
    
    protected String findQualifiedName() {
	CsmObject owner = findOwner(null);
	if( owner instanceof CsmQualifiedNamedElement  ) {
	    qualifiedNameIsFake = false;
	    return ((CsmQualifiedNamedElement) owner).getQualifiedName().toString() + getScopeSuffix() + "::" + getQualifiedNamePostfix(); // NOI18N
	}
	else {
	    qualifiedNameIsFake = true;
	    CharSequence[] cnn = classOrNspNames;
	    CsmNamespaceDefinition nsd = findNamespaceDefinition();
	    StringBuilder sb = new StringBuilder();
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
            sb.append(getScopeSuffix());
	    sb.append("::"); // NOI18N
	    sb.append(getQualifiedNamePostfix());
	    return sb.toString();
	}
    }
    
    @Override
    protected void registerInProject() {
	super.registerInProject();
	if( qualifiedNameIsFake ) {
	    ((FileImpl) getContainingFile()).onFakeRegisration(this);
	}
    }
    
    public void fixFakeRegistration() {
        if (fixFakeRegistrationAst != null) {
            CsmObject owner = findOwner(null);
            if (CsmKindUtilities.isClass(owner)) {
                CsmClass cls = (CsmClass) owner;
                for (CsmMember member : cls.getMembers()) {
                    if (member.isStatic() && member.getName().equals(getName())) {
                        VariableDefinitionImpl var = new VariableDefinitionImpl(fixFakeRegistrationAst, getContainingFile(), getReturnType(), getName().toString());
                        ((FileImpl) getContainingFile()).getProjectImpl(true).registerDeclaration(var);
                        ((FileImpl) getContainingFile()).addDeclaration(var);
                        fixFakeRegistrationAst = null;
                        return;
                    }
                }
            } else if (CsmKindUtilities.isNamespace(owner)) {
                CsmNamespace ns = (CsmNamespace) owner;
                for (CsmDeclaration decl : ns.getDeclarations()) {
                    if (CsmKindUtilities.isExternVariable(decl) && decl.getName().equals(getName())) {
                        VariableDefinitionImpl var = new VariableDefinitionImpl(fixFakeRegistrationAst, getContainingFile(), getReturnType(), getName().toString());
                        ((FileImpl) getContainingFile()).getProjectImpl(true).registerDeclaration(var);
                        ((FileImpl) getContainingFile()).addDeclaration(var);
                        fixFakeRegistrationAst = null;
                        return;
                    }
                }
            }                        
            try {
                FunctionImpl fi = new FunctionImpl(fixFakeRegistrationAst, getContainingFile(), this.getScope());
                fixFakeRegistrationAst = null;
                ((FileImpl) getContainingFile()).addDeclaration(fi);
                if (NamespaceImpl.isNamespaceScope(fi)) {
                    if (CsmKindUtilities.isNamespace(this.getScope())) {
                        ((NamespaceImpl) this.getScope()).addDeclaration(fi);
                    }
                }
            } catch (AstRendererException e) {
                DiagnosticExceptoins.register(e);
            }
        } else {
            CharSequence newQname = QualifiedNameCache.getManager().getString(findQualifiedName());
            if (!newQname.equals(qualifiedName)) {
                ProjectBase aProject = ((FileImpl) getContainingFile()).getProjectImpl(true);
                aProject.unregisterDeclaration(this);
                this.cleanUID();
                qualifiedName = newQname;
                aProject.registerDeclaration(this);
            }
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
    
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        // can be null
        PersistentUtils.writeUTF(this.qualifiedName, output);
        output.writeBoolean(this.qualifiedNameIsFake);
        PersistentUtils.writeStrings(this.classOrNspNames, output);
    }
    
    public FunctionImplEx(DataInput input) throws IOException {
	super(input);
        // can be null
        String read = PersistentUtils.readUTF(input);
        this.qualifiedName = read == null ? null : QualifiedNameCache.getManager().getString(read);
        this.qualifiedNameIsFake = input.readBoolean();
        this.classOrNspNames = PersistentUtils.readStrings(input, NameCache.getManager());
    }
}
