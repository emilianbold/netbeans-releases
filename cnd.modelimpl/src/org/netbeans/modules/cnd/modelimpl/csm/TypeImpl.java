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

import java.io.DataInput;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.FakeAST;
import java.util.*;

import antlr.collections.AST;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.utils.cache.TextCache;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 *
 * @author Vladimir Kvashin
 */
public class TypeImpl extends OffsetableBase implements CsmType {

    private final byte pointerDepth;
    private final boolean reference;
    private final byte arrayDepth;
    private final boolean _const;
    private final CharSequence classifierText;
    
    private final List<CsmType> instantiationParams = new ArrayList();
    
    // FIX for lazy resolver calls
    private CharSequence[] qname = null;
    private int firstOffset;
    private CsmUID<CsmClassifier> classifierUID;
    
    // package-local - for facory only
    TypeImpl(CsmClassifier classifier, int pointerDepth, boolean reference, int arrayDepth, AST ast, CsmFile file) {
        super(ast, file);
        this._setClassifier(classifier);
        this.pointerDepth = (byte) pointerDepth;
        this.reference = reference;
        this.arrayDepth = (byte) arrayDepth;
        _const = initIsConst(ast);
        if (classifier == null) {
            this._setClassifier(initClassifier(ast));
            this.classifierText = initClassifierText(ast);
        } else {
            CharSequence typeName = classifier.getName();
            if (typeName == null || typeName.length()==0){
                this.classifierText = initClassifierText(ast);
            } else {
                this.classifierText = typeName;
            }
        }
    }

    // package-local - for facory only
    TypeImpl(CsmClassifier classifier, int pointerDepth, boolean reference, int arrayDepth, AST ast, CsmFile file, CsmOffsetable offset) {
        super(file, offset);
        this._setClassifier(classifier);
        this.pointerDepth = (byte) pointerDepth;
        this.reference = reference;
        this.arrayDepth = (byte) arrayDepth;
        _const = initIsConst(ast);
        if (classifier == null) {
            this._setClassifier(initClassifier(ast));
            this.classifierText = initClassifierText(ast);
        } else {
            CharSequence typeName = classifier.getName();
            if (typeName == null || typeName.length()==0){
                this.classifierText = initClassifierText(ast);
            } else {
                this.classifierText = typeName;
            }
        }
    }
    
    // package-local - for facory only
    TypeImpl(AST classifier, CsmFile file, int pointerDepth, boolean reference, int arrayDepth) {
        super(classifier, file);
        //setAst(classifier);
        this.pointerDepth = (byte) pointerDepth;
        this.reference = reference;
        this.arrayDepth = (byte) arrayDepth;
        _const = initIsConst(classifier);
        this._setClassifier(initClassifier(classifier));
        this.classifierText = initClassifierText(classifier);
    }
    
    @Override
    protected CsmAST getEndAst(AST node) {
        AST ast = node;
        if( ast == null ) {
            return null;
        }
        ast = getLastNode(ast);
        if( ast instanceof CsmAST ) {
            return (CsmAST) ast;
        }
        return super.getEndAst(node);
    }
    
    private AST getLastNode(AST first) {
        AST last = first;
        for( AST token = last; token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
                case CPPTokenTypes.CSM_VARIABLE_DECLARATION:
                case CPPTokenTypes.CSM_QUALIFIED_ID:
                    return AstUtil.getLastChildRecursively(last);
                default:
                    last = token;
            }
        }
        return null;
    }
    
    public boolean isReference() {
        return reference;
    }

    public boolean isPointer() {
        return pointerDepth > 0;
    }

    public List<CsmType> getInstantiationParams() {
        return instantiationParams;
    }

    public boolean isInstantiation() {
        return !instantiationParams.isEmpty();
    }
    
    private static boolean initIsConst(AST node) {
        if( node != null ) {
            for( AST token = node; token != null; token = token.getNextSibling() ) {
		switch( token.getType() ) {
		    case CPPTokenTypes.LITERAL_const:
                    case CPPTokenTypes.LITERAL___const:
			return true;
		    case CPPTokenTypes.CSM_VARIABLE_DECLARATION:
		    case CPPTokenTypes.CSM_QUALIFIED_ID:
			return false;
		}
            }
        }
        return false;
    }
    
    public boolean isConst() {
        return _const;
    }

    public String getCanonicalText() {
	return decorateText(classifierText, this, true, null).toString();
    }
    
    @Override
    public CharSequence getText() {
	// TODO: resolve typedefs
	return decorateText(classifierText, this, false, null).toString();
    }
    
    protected StringBuilder getText(boolean canonical, CharSequence variableNameToInsert) {
        return decorateText(classifierText, this, canonical, variableNameToInsert);
    }
    
    public StringBuilder decorateText(CharSequence classifierText, CsmType decorator, boolean canonical, CharSequence variableNameToInsert) {
	StringBuilder sb = new StringBuilder();
	if( decorator.isConst() ) {
	    sb.append("const "); // NOI18N
	}
	sb.append(classifierText);
	for( int i = 0; i < decorator.getPointerDepth(); i++ ) {
	    sb.append('*');
	}
	if( decorator.isReference() ) {
	    sb.append('&');
	}
	for( int i = 0; i < decorator.getArrayDepth(); i++ ) {
	    sb.append(canonical ? "*" : "[]"); // NOI18N
	}
	if( variableNameToInsert != null ) {
	    sb.append(' ');
	    sb.append(variableNameToInsert);
	}
	return sb;
    }
    
    private CharSequence initClassifierText(AST node) {
        if( node == null ) {
            CsmClassifier classifier = _getClassifier();
            return classifier == null ? "" : classifier.getName();
        }
        else {
            StringBuilder sb = new StringBuilder();
            addText(sb, AstRenderer.getFirstSiblingSkipQualifiers(node));
            return TextCache.getString(sb.toString());
//            return sb.toString();
        }
    }
    
    private static void addText(StringBuilder sb, AST ast) {
        if( ! (ast instanceof FakeAST) ) {
            if( sb.length() > 0 ) {
                sb.append(' ');
            }
            sb.append(ast.getText());
        }
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            addText(sb,  token);
        }
    }

    public CsmClassifier getClassifier() {
        CsmClassifier res = getClassifier(null);
        if (isInstantiation() && CsmKindUtilities.isTemplate(res) && !((CsmTemplate)res).getTemplateParameters().isEmpty()) {
            res = (CsmClassifier)Instantiation.create((CsmTemplate)res, this);
        }
        return res;
    }

    public CharSequence getClassifierText() {
	return classifierText;
    }
    
    public CsmClassifier getClassifier(Resolver parent) {
        CsmClassifier classifier = _getClassifier();
        if (classifier != null && (!(classifier instanceof CsmValidable) || (((CsmValidable)classifier).isValid()))) {
            return classifier;
        } else {
            _setClassifier(null);
            if (qname != null) {
                _setClassifier(renderClassifier(qname, parent));
            } else if (classifierText.length() > 0) {
                _setClassifier(renderClassifier(new CharSequence[] { classifierText }, parent ));
            }
        }
        return _getClassifier();
    }
    
    private CsmClassifier renderClassifier(CharSequence[] qname, Resolver parent) {
        CsmClassifier result = null;
        Resolver resolver = ResolverFactory.createResolver(getContainingFile(), firstOffset, parent);
        CsmObject o = resolver.resolve(qname, Resolver.CLASSIFIER);
        if( CsmKindUtilities.isClassifier(o) ) {
            result = (CsmClassifier) o;
        }
        if( result == null ) {
            result = ((ProjectBase) getContainingFile().getProject()).getDummyForUnresolved(qname, getContainingFile(), firstOffset);
        }
        return result;
    }
        
    private CsmClassifier initClassifier(AST node) {
        AST tokType = AstRenderer.getFirstSiblingSkipQualifiers(node);
        if( tokType == null ||  
            (tokType.getType() != CPPTokenTypes.CSM_TYPE_BUILTIN && 
            tokType.getType() != CPPTokenTypes.CSM_TYPE_COMPOUND) &&
            tokType.getType() != CPPTokenTypes.CSM_QUALIFIED_ID ) {
            return null;
        }

        if( tokType.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ) {
            return BuiltinTypes.getBuiltIn(tokType);
        }
        else { // tokType.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND
            try {
                CsmAST tokFirstId = (CsmAST) tokType.getFirstChild();
		if( tokFirstId == null ) {
		    // this is unnormal; but we should be able to work even on incorrect AST
		    return null;
		}
                        
                firstOffset = tokFirstId.getOffset();
                //Resolver resolver = ResolverFactory.createResolver(getContainingFile(), firstOffset);
                // gather name components into string array 
                // for example, for std::vector new String[] { "std", "vector" }
                
                //TODO: we have AstRenderer.getNameTokens, it is better to use it here
                List l = new ArrayList();
		int templateDepth = 0;
                for( AST namePart = tokFirstId; namePart != null; namePart = namePart.getNextSibling() ) {
                    if( templateDepth == 0 && namePart.getType() == CPPTokenTypes.ID ) {
                        l.add(namePart.getText());
                    }
		    else if( namePart.getType() == CPPTokenTypes.LESSTHAN ) {
			// the beginning of template parameters
			templateDepth++;
		    }
		    else if( namePart.getType() == CPPTokenTypes.GREATERTHAN ) {
			// the beginning of template parameters
			templateDepth--;
		    }
                    else {
                        //assert namePart.getType() == CPPTokenTypes.SCOPE;
                        if( templateDepth == 0) {
                            if (namePart.getType() != CPPTokenTypes.SCOPE) {
                                if (TraceFlags.DEBUG) {
                                    StringBuilder tokenText = new StringBuilder();
                                    tokenText.append('[').append(namePart.getText());
                                    if (namePart.getNumberOfChildren() == 0) {
                                        tokenText.append(", line=").append(namePart.getLine()); // NOI18N
                                        tokenText.append(", column=").append(namePart.getColumn()); // NOI18N
                                    }
                                    tokenText.append(']');
                                    System.err.println("Incorect token: expected '::', found " + tokenText.toString());
                                }
                            }
                        } else {
                            // TODO: maybe we need to filter out some more tokens
                            if (namePart.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN
                                    || namePart.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND) {
                                instantiationParams.add(AstRenderer.renderType(namePart, getContainingFile()));
                            }
                        }
                    }
                }
                qname = (String[]) l.toArray(new String[l.size()]);
                /*CsmObject o = resolver.resolve(qname);
                if( CsmKindUtilities.isClassifier(o) ) {
                    result = (CsmClassifier) o;
                }
//		else if( CsmKindUtilities.isTypedef(o) ) {
//		    CsmTypedef td = (CsmTypedef) o;
//		    CsmType type = td.getType();
//		    if( type != null ) {
//			result = type.getClassifier();
//		    }
//		}
                if( result == null ) {
                    result = ((ProjectBase) getContainingFile().getProject()).getDummyForUnresolved(qname, getContainingFile(), offset);
                }*/
            }
            catch( Exception e ) {
                DiagnosticExceptoins.register(e);
            }
        }
        return null;
    }

    public int getArrayDepth() {
        return arrayDepth;
    }
    
    public int getPointerDepth() {
        return pointerDepth;
    }

    private CsmClassifier _getClassifier() {
        CsmClassifier classifier = UIDCsmConverter.UIDtoDeclaration(classifierUID);
        // can be null if cached one was removed 
        return classifier;
    }

    private void _setClassifier(CsmClassifier classifier) {
        this.classifierUID = UIDCsmConverter.declarationToUID(classifier);
        assert (classifierUID != null || classifier == null);
    }

    public boolean isBuiltInBased(boolean resolveTypeChain) {
        CsmClassifier classifier;
        if (resolveTypeChain) {
            classifier = getClassifier();
            if (CsmKindUtilities.isTypedef(classifier)) {
                return ((CsmTypedef)classifier).getType().isBuiltInBased(true);
            }
        } else {
            classifier = _getClassifier();
        }
        return CsmKindUtilities.isBuiltIn(classifier);
    }
    

    @Override
    public String toString() {
        return "TYPE " + getText()  + getOffsetString(); // NOI18N
    }    
    
    //package-local
    /**
     * Return display text for a variable of this type
     * (we actually need this for function pointers, where simple typeName+' '+variableName does not work.
     */
    String getVariableDisplayName(String variableName) {
	return decorateText(classifierText, this, false, variableName).toString();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        output.writeInt(pointerDepth);
        output.writeBoolean(reference);
        output.writeInt(arrayDepth);
        output.writeBoolean(_const);
        assert this.classifierText != null;
        output.writeUTF(classifierText.toString());
        
        PersistentUtils.writeStrings(qname, output);
        output.writeInt(firstOffset);
        PersistentUtils.writeTypes(instantiationParams, output);
        UIDObjectFactory.getDefaultFactory().writeUID(classifierUID, output);
    }

    public TypeImpl(DataInput input) throws IOException {
        super(input);
        this.pointerDepth = (byte) input.readInt();
        this.reference = input.readBoolean();
        this.arrayDepth= (byte) input.readInt();
        this._const = input.readBoolean();
        this.classifierText = NameCache.getManager().getString(input.readUTF());
        assert this.classifierText != null;
        
        this.qname = PersistentUtils.readStrings(input, NameCache.getManager());
        this.firstOffset = input.readInt();
        PersistentUtils.readTypes(this.instantiationParams, input);
        this.classifierUID = UIDObjectFactory.getDefaultFactory().readUID(input);
    }

}
