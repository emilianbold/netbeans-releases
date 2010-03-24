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

import org.netbeans.modules.cnd.antlr.collections.AST;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.CsmOffsetable.Position;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableBase;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionStatementImpl;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;

/**
 *
 * @author Vladimir Kvashin
 */
public class TypeFactory {

    public static CsmType getVarArgType() {
        // for now we use null
        return null;
    }
    
    private TypeFactory() {}
    
    public static TypeImpl createBuiltinType(String text, AST ptrOperator, int arrayDepth, AST ast, CsmFile file) {
        CsmBuiltIn builtin = BuiltinTypes.getBuiltIn(text);
        return createType(builtin, ptrOperator, arrayDepth, ast, file);
    }

    public static TypeImpl createType(CsmClassifier classifier, AST ptrOperator, int arrayDepth, AST ast, CsmFile file) {
        return createType(classifier, ptrOperator, arrayDepth, ast, file, null);
    }
    
    public static TypeImpl createType(CsmClassifier classifier, AST ptrOperator, int arrayDepth, AST ast, CsmFile file, CsmOffsetable offset) {
        boolean refence = false;
        int pointerDepth = 0;
        if (ptrOperator != null &&
            (ptrOperator.getType() == CPPTokenTypes.CSM_CLASS_DECLARATION ||
            ptrOperator.getType() == CPPTokenTypes.CSM_ENUM_DECLARATION)) {
            ptrOperator = ptrOperator.getFirstChild();
            int count = 0; 
            boolean findBody = false;
            boolean findStruct = false;
            for (; ptrOperator != null; ptrOperator = ptrOperator.getNextSibling()){
                switch( ptrOperator.getType() ) {
                    case CPPTokenTypes.LITERAL_struct:
                    case CPPTokenTypes.LITERAL_class:
                    case CPPTokenTypes.LITERAL_enum:
                    case CPPTokenTypes.LITERAL_union:
                        findStruct = true;
                        continue;
                    case CPPTokenTypes.LCURLY:
                        findBody = true;
                        count++;
                        continue;
                    case CPPTokenTypes.RCURLY:
                        count--;
                        if (findStruct && count == -1){
                            count = 0;
                            findStruct = false;
                            findBody = true;
                        }
                        continue;
                    default:
                        if (findBody && count == 0) {
                            break;
                        }
                        continue;
                }
                break;
            }
        }
        while( ptrOperator != null && ptrOperator.getType() == CPPTokenTypes.CSM_PTR_OPERATOR ) {
            //for( AST token = ptrOperator.getFirstChild(); token != null; token = token.getNextSibling() ) {
                AST token = ptrOperator.getFirstChild();
                switch( token.getType() ) {
                    case CPPTokenTypes.STAR:
                        pointerDepth++;
                        break;
                    case CPPTokenTypes.AMPERSAND:
                        refence = true;
                        break;
                }
            //}
            ptrOperator = ptrOperator.getNextSibling();
        }
        return new TypeImpl(classifier, pointerDepth, refence, arrayDepth, ast, file, offset);
    }

    public static TypeImpl createType(AST ast, CsmFile file,  AST ptrOperator, int arrayDepth) {
        return createType(ast, file, ptrOperator, arrayDepth, null);
    }

    public static TypeImpl createType(AST ast, CsmFile file,  AST ptrOperator, int arrayDepth, CsmScope scope) {
        return createType(ast, file, ptrOperator, arrayDepth, null, scope);
    }
    
    private static TypeImpl createType(AST ast, CsmFile file,  AST ptrOperator, int arrayDepth, CsmType parent, CsmScope scope) {
        return createType(ast, file, ptrOperator, arrayDepth, parent, scope, false);
    }

    public static TypeImpl createType(AST ast, CsmFile file,  AST ptrOperator, int arrayDepth, CsmType parent, CsmScope scope, boolean inFunctionParameters) {
        return createType(ast, file, ptrOperator, arrayDepth, parent, scope, inFunctionParameters, false);
    }

    public static TypeImpl createType(AST ast, CsmFile file,  AST ptrOperator, int arrayDepth, CsmType parent, CsmScope scope, boolean inFunctionParameters, boolean inTypedef) {
        return createType(ast, null, file, ptrOperator, arrayDepth, parent, scope, inFunctionParameters, inTypedef);
    }

    public static TypeImpl createType(AST ast, CsmClassifier classifier, CsmFile file,  AST ptrOperator, int arrayDepth, CsmType parent, CsmScope scope, boolean inFunctionParameters, boolean inTypedef) {
        boolean refence = false;
        int pointerDepth = 0;
        while( ptrOperator != null && ptrOperator.getType() == CPPTokenTypes.CSM_PTR_OPERATOR ) {
            //for( AST token = ptrOperator.getFirstChild(); token != null; token = token.getNextSibling() ) {
                AST token = ptrOperator.getFirstChild();
                if (token != null) {
                    switch( token.getType() ) {
                        case CPPTokenTypes.STAR:
                            ++pointerDepth;
                            break;
                        case CPPTokenTypes.AMPERSAND:
                            refence = true;
                            break;
                    }
                }
            //}
            ptrOperator = ptrOperator.getNextSibling();
        }

        int returnTypePointerDepth = pointerDepth;
        AST lookahead = ptrOperator;
        while (lookahead != null) {
            if (lookahead.getType() == CPPTokenTypes.RPAREN) {
                // ptrOperator relates to function pointer, not to return type
                returnTypePointerDepth = 0;
                break;
            } else if (lookahead.getType() == CPPTokenTypes.LPAREN) {
                // OK, no need to look further
                break;
            }
            lookahead = lookahead.getNextSibling();
        }

        TypeImpl type;

        if (parent != null) {
            type = new NestedType(parent, file, parent.getPointerDepth(), parent.isReference(), parent.getArrayDepth(), parent.isConst(), parent.getStartOffset(), parent.getEndOffset());
        } else if (TypeFunPtrImpl.isFunctionPointerParamList(ast, inFunctionParameters, inTypedef)) {
            type = new TypeFunPtrImpl(file, returnTypePointerDepth, refence, arrayDepth, TypeImpl.initIsConst(ast), OffsetableBase.getStartOffset(ast), TypeImpl.getEndOffset(ast));
            ((TypeFunPtrImpl)type).init(ast, inFunctionParameters, inTypedef);
        } else {
            type = new TypeImpl(file, pointerDepth, refence, arrayDepth, TypeImpl.initIsConst(ast), OffsetableBase.getStartOffset(ast), TypeImpl.getEndOffset(ast));
        }

        // TODO: pass extra parameters to the constructor insdead of calling methods!!!
        
        ///// INIT CLASSFIER stuff
        AST typeStart = AstRenderer.getFirstSiblingSkipQualifiers(ast);
        if( typeStart != null) {
            if (typeStart.getType() == CPPTokenTypes.LITERAL_struct ||
                    typeStart.getType() == CPPTokenTypes.LITERAL_class ||
                    typeStart.getType() == CPPTokenTypes.LITERAL_union) {
                typeStart = typeStart.getNextSibling();
            }            
        }
        if( typeStart == null)
            /*(tokType.getType() != CPPTokenTypes.CSM_TYPE_BUILTIN &&
            tokType.getType() != CPPTokenTypes.CSM_TYPE_COMPOUND) &&
            tokType.getType() != CPPTokenTypes.CSM_QUALIFIED_ID )*/ {
            //return null;
        } else {
            if(classifier != null) {
                type._setClassifier(classifier);
                type.classifierText = classifier.getName();
            } else if( typeStart.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ) {
                CsmClassifier cls = BuiltinTypes.getBuiltIn(typeStart);
                type._setClassifier(cls);
                type.classifierText = cls.getName();
            } else { // tokType.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND
                AST tokFirstId;
                try {
                    if (typeStart.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ||
                        typeStart.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND ||
                        typeStart.getType() == CPPTokenTypes.CSM_QUALIFIED_ID) {
                        tokFirstId = typeStart.getFirstChild();
                    } else {
                        tokFirstId = typeStart;
                    }
                    if( tokFirstId == null ) {
                        // this is unnormal; but we should be able to work even on incorrect AST
                        //return null;
                    } else {
                        //Check for global type
                        if (tokFirstId.getType() ==  CPPTokenTypes.SCOPE) {
                            type = new NestedType(null, file, type.getPointerDepth(), type.isReference(), type.getArrayDepth(), type.isConst(), type.getStartOffset(), type.getEndOffset());
                            tokFirstId = tokFirstId.getNextSibling();
                        }
                        //TODO: we have AstRenderer.getNameTokens, it is better to use it here
                        List<CharSequence> l = new ArrayList<CharSequence>();
                        int templateDepth = 0;
                        StringBuilder sb = new StringBuilder();
                        for( AST namePart = tokFirstId; namePart != null; namePart = namePart.getNextSibling() ) {
                            if( templateDepth == 0 && namePart.getType() == CPPTokenTypes.ID ) {
                                sb.append(namePart.getText());
                                l.add(NameCache.getManager().getString(namePart.getText()));
                                //l.add(namePart.getText());
                            } else if( namePart.getType() == CPPTokenTypes.LESSTHAN ) {
                                // the beginning of template parameters
                                templateDepth++;
                            } else if( namePart.getType() == CPPTokenTypes.GREATERTHAN ) {
                                // the beginning of template parameters
                                templateDepth--;
                            } else {
                                //assert namePart.getType() == CPPTokenTypes.SCOPE;
                                if( templateDepth == 0) {
                                    if (namePart.getType() == CPPTokenTypes.SCOPE) {
                                        // We're done here, start filling nested type
                                        type.classifierText = QualifiedNameCache.getManager().getString(sb);
                                        type.qname = l.toArray(new CharSequence[l.size()]);
                                        type = createType(namePart.getNextSibling(), file, ptrOperator, arrayDepth, TemplateUtils.checkTemplateType(type, scope), scope);
                                        break;
                                    } else {
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
                                    // Initialize instantiation params
                                    // TODO: maybe we need to filter out some more tokens
                                    if (namePart.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN
                                            || namePart.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND
                                            || namePart.getType() == CPPTokenTypes.LITERAL_struct) {
                                        CsmType t = AstRenderer.renderType(namePart, file);
                                        type.instantiationParams.add(new TypeBasedSpecializationParameterImpl(t));
                                    }
                                    if (namePart.getType() == CPPTokenTypes.CSM_EXPRESSION) {
                                        type.instantiationParams.add(new ExpressionBasedSpecializationParameterImpl(new ExpressionStatementImpl(namePart, type.getContainingFile(), scope),
                                                type.getContainingFile(), OffsetableBase.getStartOffset(namePart), OffsetableBase.getEndOffset(namePart)));
                                    }
                                }
                            }
                        }
                        if (type.classifierText == TypeImpl.NON_INITIALIZED_CLASSIFIER_TEXT) {
                            type.classifierText = QualifiedNameCache.getManager().getString(sb);
                            type.qname = l.toArray(new CharSequence[l.size()]);
                        }
                    }
                } catch( Exception e ) {
                    DiagnosticExceptoins.register(e);
                }
            }
        }
        return type;
    }

    public static CsmType createType(CsmType type, int pointerDepth, boolean reference, int arrayDepth, boolean _const) {
        if(type.getPointerDepth() == pointerDepth &&
            type.isReference() == reference &&
            type.getArrayDepth() == arrayDepth &&
            type.isConst() == _const) {
            return type;
        }
        if(type instanceof NestedType) {
            return new NestedType((NestedType)type, pointerDepth, reference, arrayDepth, _const);
        }
        if(type instanceof TypeFunPtrImpl) {
            return new TypeFunPtrImpl((TypeFunPtrImpl)type, pointerDepth, reference, arrayDepth, _const);
        }
        if(type instanceof TemplateParameterTypeImpl) {
            return new TemplateParameterTypeImpl((TemplateParameterTypeImpl)type, pointerDepth, reference, arrayDepth, _const);
        }
        if(type instanceof TypeImpl) {
            return new TypeImpl((TypeImpl)type, pointerDepth, reference, arrayDepth, _const);
        }
        return new TypeWrapper(type, pointerDepth, reference, arrayDepth, _const);
    }

    public static CsmType createType(CsmType type, List<CsmSpecializationParameter> instantiationParams) {
        if(type instanceof NestedType) {
            return new NestedType((NestedType)type, instantiationParams);
        }
        if(type instanceof TypeFunPtrImpl) {
            return new TypeFunPtrImpl((TypeFunPtrImpl)type, instantiationParams);
        }
        if(type instanceof TemplateParameterTypeImpl) {
            return new TemplateParameterTypeImpl((TemplateParameterTypeImpl)type, instantiationParams);
        }
        if(type instanceof TypeImpl) {
            return new TypeImpl((TypeImpl)type, instantiationParams);
        }
        return type;
    }

    private static class TypeWrapper implements CsmType {
        protected CsmType type;
        protected int pointerDepth;
        protected boolean reference;
        protected int arrayDepth;
        protected boolean _const;

        public TypeWrapper(CsmType type, int pointerDepth, boolean reference, int arrayDepth, boolean _const) {
            this.type = type;
            this.pointerDepth = pointerDepth;
            this.reference = reference;
            this.arrayDepth = arrayDepth;
            this._const = _const;
        }

        public CsmClassifier getClassifier() {
            return type.getClassifier();
        }

        public CharSequence getClassifierText() {
            return type.getClassifierText();
        }

        public boolean isInstantiation() {
            return type.isInstantiation();
        }

        public List<CsmSpecializationParameter> getInstantiationParams() {
            return type.getInstantiationParams();
        }

        public int getArrayDepth() {
            return arrayDepth;
        }

        public boolean isPointer() {
            return pointerDepth > 0;
        }

        public int getPointerDepth() {
            return pointerDepth;
        }

        public boolean isReference() {
            return reference;
        }

        public boolean isConst() {
            return _const;
        }

        public boolean isBuiltInBased(boolean resolveTypeChain) {
            return type.isBuiltInBased(resolveTypeChain);
        }

        public boolean isTemplateBased() {
            return type.isTemplateBased();
        }

        public CharSequence getCanonicalText() {
            return getText();
        }

        public CsmFile getContainingFile() {
            return type.getContainingFile();
        }

        public int getStartOffset() {
            return type.getStartOffset();
        }

        public int getEndOffset() {
            return type.getEndOffset();
        }

        public Position getStartPosition() {
            return type.getStartPosition();
        }

        public Position getEndPosition() {
            return type.getEndPosition();
        }

        public CharSequence getText() {
            return format();
        }

        public String format() {
            StringBuilder sb = new StringBuilder();
	        if (isConst()) {
                sb.append("const "); // NOI18N
            }
            sb.append(getClassifier().getQualifiedName());
            for (int i = 0; i < getPointerDepth(); i++) {
                sb.append('*'); // NOI18N
            }
            if (isReference()) {
                sb.append('&'); // NOI18N
            }
            for (int i = 0; i < getArrayDepth(); i++) {
                sb.append("[]"); // NOI18N
            }
            return sb.toString();
        }
    }
}
