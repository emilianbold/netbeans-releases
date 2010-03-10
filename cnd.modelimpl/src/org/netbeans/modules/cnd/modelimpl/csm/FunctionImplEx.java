/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.utils.cache.APTStringManager;

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
    private static final byte FAKE_QUALIFIED_NAME = 1 << (FunctionImpl.LAST_USED_FLAG_INDEX+1);
    private final CharSequence[] classOrNspNames;   
    
    public FunctionImplEx(AST ast, CsmFile file, CsmScope scope, boolean register, boolean global) throws AstRendererException {
        super(ast, file, scope, false, global);
        classOrNspNames = CastUtils.isCast(ast) ? getClassOrNspNames(ast) : initClassOrNspNames(ast);
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


    private static CharSequence[] getClassOrNspNames(AST ast) {
	assert CastUtils.isCast(ast);
	AST child = ast.getFirstChild();
        if (child != null && child.getType() == CPPTokenTypes.LITERAL_template) {
            child = AstRenderer.skipTemplateSibling(child);
        }
        child = AstRenderer.getFirstSiblingSkipQualifiers(child);
	if( child != null && child.getType() == CPPTokenTypes.ID ) {
	    AST next = child.getNextSibling();
	    if( next != null && next.getType() == CPPTokenTypes.LESSTHAN ) {
                next = AstRenderer.skipTemplateParameters(next);
	    }
	    if( next != null && next.getType() == CPPTokenTypes.SCOPE ) {
		List<CharSequence> l = new ArrayList<CharSequence>();
                APTStringManager manager = NameCache.getManager();
		l.add(manager.getString(child.getText()));
		begin:
		for( next = next.getNextSibling(); next != null; next = next.getNextSibling() ) {
		    switch( next.getType() ) {
			case CPPTokenTypes.ID:
			    l.add(manager.getString(next.getText()));
                            break;
			case CPPTokenTypes.SCOPE:
			    break; // do nothing
			default:
			    break begin;
		    }
		}
		return  l.toArray(new CharSequence[l.size()]);
	    }
	}
	return null;
    }

    private static CharSequence[] initClassOrNspNames(AST node) {
        //qualified id
        AST qid = AstUtil.findMethodName(node);
        if( qid == null ) {
            return null;
        }
        int cnt = qid.getNumberOfChildren();
        if( cnt >= 1 ) {
            List<CharSequence> l = new ArrayList<CharSequence>();
            APTStringManager manager = NameCache.getManager();
            String id = null;
            int level = 0;
            for( AST token = qid.getFirstChild(); token != null; token = token.getNextSibling() ) {
                int type2 = token.getType();
                switch (type2) {
                    case CPPTokenTypes.ID:
                        id = token.getText();
                        break;
                    case CPPTokenTypes.GREATERTHAN:
                        level--;
                        break;
                    case CPPTokenTypes.LESSTHAN:
                        level++;
                        break;
                    case CPPTokenTypes.SCOPE:
                        if (id != null && level == 0) {
                            l.add(manager.getString(id));
                        }
                        break;
                    default:
                }
            }
            return l.toArray(new CharSequence[l.size()]);
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
        // check if owner is real or fake
        if(CsmKindUtilities.isQualified(owner)) {
            setFlags(FAKE_QUALIFIED_NAME, false);
            return ((CsmQualifiedNamedElement) owner).getQualifiedName().toString() + getScopeSuffix() + "::" + getQualifiedNamePostfix(); // NOI18N
        }
        setFlags(FAKE_QUALIFIED_NAME, true);
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

    @Override
    protected void registerInProject() {
        super.registerInProject();
        // if this funtion couldn't find it's FQN => register it as fake and
        // come back later on for registration (see fixFakeRegistration with null ast)
        if( hasFlags(FAKE_QUALIFIED_NAME) ) {
            ((FileImpl) getContainingFile()).onFakeRegisration(this, null);
        }
    }
    
    public final boolean fixFakeRegistration(boolean projectParsedMode, AST fixFakeRegistrationAst) {
        boolean fixed = false;
        if (fixFakeRegistrationAst != null) {
            CsmObject owner = findOwner(null);
            if (CsmKindUtilities.isClass(owner)) {
                CsmClass cls = (CsmClass) owner;
                for (CsmMember member : cls.getMembers()) {
                    if (member.isStatic() && member.getName().equals(getName())) {
                        FileImpl aFile = (FileImpl) getContainingFile();
                        VariableDefinitionImpl var = new VariableDefinitionImpl(fixFakeRegistrationAst, getContainingFile(), getReturnType(), getName().toString());
                        aFile.getProjectImpl(true).unregisterDeclaration(this);
                        aFile.removeDeclaration(this);
                        var.registerInProject();
                        aFile.addDeclaration(var);
                        fixFakeRegistrationAst = null;
                        return true;
                    }
                }
            } else if (CsmKindUtilities.isNamespace(owner)) {
                CsmFilter filter = CsmSelect.getFilterBuilder().createCompoundFilter(
                         CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.VARIABLE, CsmDeclaration.Kind.VARIABLE_DEFINITION),
                         CsmSelect.getFilterBuilder().createNameFilter(getName(), true, true, false));
                Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDeclarations(((CsmNamespace)owner), filter);
                while (it.hasNext()) {
                    CsmDeclaration decl = it.next();
                    if (CsmKindUtilities.isExternVariable(decl) && decl.getName().equals(getName())) {
                        FileImpl aFile = (FileImpl) getContainingFile();
                        VariableDefinitionImpl var = new VariableDefinitionImpl(fixFakeRegistrationAst, getContainingFile(), getReturnType(), getName().toString());
                        aFile.getProjectImpl(true).unregisterDeclaration(this);
                        aFile.removeDeclaration(this);
                        var.registerInProject();
                        aFile.addDeclaration(var);
                        fixFakeRegistrationAst = null;
                        return true;
                    }
                }
            }
            if (projectParsedMode) {
                try {
                    FileImpl aFile = (FileImpl) getContainingFile();
                    FunctionImpl fi = new FunctionImpl(fixFakeRegistrationAst, getContainingFile(), this.getScope(), true, true);
                    fixFakeRegistrationAst = null;
                    aFile.getProjectImpl(true).unregisterDeclaration(this);
                    aFile.removeDeclaration(this);
                    fi.registerInProject();
                    aFile.addDeclaration(fi);
                    fixed = true;
                    if (NamespaceImpl.isNamespaceScope(fi)) {
                        if (CsmKindUtilities.isNamespace(this.getScope())) {
                            ((NamespaceImpl) this.getScope()).addDeclaration(fi);
                        }
                    }
                } catch (AstRendererException e) {
                    DiagnosticExceptoins.register(e);
                }
            }
        } else {
            CharSequence newQname = QualifiedNameCache.getManager().getString(findQualifiedName());
            if (!newQname.equals(qualifiedName)) {
                ProjectBase aProject = ((FileImpl)getContainingFile()).getProjectImpl(true);
                aProject.unregisterDeclaration(this);
                this.cleanUID();
                qualifiedName = newQname;
                registerInProject();
                fixed = true;
            }
        }
        return fixed;
    }
    
    private CsmNamespaceDefinition findNamespaceDefinition() {
        CsmFilter filter = CsmSelect.getFilterBuilder().createKindFilter(Kind.NAMESPACE_DEFINITION);
        return findNamespaceDefinition(CsmSelect.getDeclarations(getContainingFile(), filter), filter);
    }
    
    private CsmNamespaceDefinition findNamespaceDefinition(Iterator<CsmOffsetableDeclaration> it, CsmFilter filter) {
        while(it.hasNext()) {
            CsmOffsetableDeclaration decl = it.next();
            if (decl.getStartOffset() > this.getStartOffset()) {
                break;
            }
            if (decl.getKind() == CsmDeclaration.Kind.NAMESPACE_DEFINITION) {
                if (this.getEndOffset() < decl.getEndOffset()) {
                    CsmNamespaceDefinition nsdef = (CsmNamespaceDefinition) decl;
                    CsmNamespaceDefinition inner = findNamespaceDefinition(CsmSelect.getDeclarations(nsdef, filter), filter);
                    return (inner == null) ? nsdef : inner;
                }
            }
        }
        return null;
    }    
    
    public static boolean isFakeFunction(CsmObject declaration) {
        if (declaration instanceof FunctionImplEx) {
            return FunctionImplEx.class.equals(declaration.getClass());
        } else {
            return false;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        // can be null
        PersistentUtils.writeUTF(this.qualifiedName, output);
        PersistentUtils.writeStrings(this.classOrNspNames, output);
    }
    
    public FunctionImplEx(DataInput input) throws IOException {
	super(input);
        // can be null
        this.qualifiedName = PersistentUtils.readUTF(input, QualifiedNameCache.getManager());
        this.classOrNspNames = PersistentUtils.readStrings(input, NameCache.getManager());
    }
}
