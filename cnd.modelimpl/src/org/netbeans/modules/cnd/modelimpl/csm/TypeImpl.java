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

import java.io.DataInput;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.FakeAST;
import java.util.*;

import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.services.CsmIncludeResolver;
import org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.utils.cache.TextCache;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.Resolver.SafeClassifierProvider;
import org.netbeans.modules.cnd.modelimpl.csm.core.Resolver.SafeTemplateBasedProvider;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionStatementImpl;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.impl.services.InstantiationProviderImpl;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.CharSequences;

/**
 *
 * @author Vladimir Kvashin
 */
public class TypeImpl extends OffsetableBase implements CsmType, SafeClassifierProvider, SafeTemplateBasedProvider {
    private static final byte FLAGS_TYPE_OF_TYPEDEF = 1 << 0;
    private static final byte FLAGS_REFERENCE = 1 << 1;
    private static final byte FLAGS_CONST = 1 << 2;
    private static final byte FLAGS_TYPE_WITH_CLASSIFIER = 1 << 3;
    protected static final int LAST_USED_FLAG_INDEX = 4;

    private final byte pointerDepth;
    private final byte arrayDepth;
    private byte flags;
    CharSequence classifierText;
    /*package*/ static final CharSequence NON_INITIALIZED_CLASSIFIER_TEXT = CharSequences.empty();
    private int parseCount;

    final ArrayList<CsmSpecializationParameter> instantiationParams = new ArrayList<CsmSpecializationParameter>();

    // FIX for lazy resolver calls
    CharSequence[] qname = null;
    private CsmUID<CsmClassifier> classifierUID;

    // package-local - for facory only
    TypeImpl(CsmClassifier classifier, int pointerDepth, boolean reference, int arrayDepth, AST ast, CsmFile file, CsmOffsetable offset) {
        super(file, offset == null ? getStartOffset(ast) : offset.getStartOffset(), offset == null ? getEndOffset(ast) : offset.getEndOffset());
        this._setClassifier(classifier);
        this.pointerDepth = (byte) pointerDepth;
        setFlags(FLAGS_REFERENCE, reference);
        this.arrayDepth = (byte) arrayDepth;
        boolean _const = isTypeDefAST(ast) ? initIsConst(ast.getFirstChild()) : initIsConst(ast);
        setFlags(FLAGS_CONST, _const);
        if (classifier == null) {
            CndUtils.assertTrueInConsole(false, "why null classifier?");
            this._setClassifier(initClassifier(ast));
            this.classifierText = initClassifierText(ast);
        } else {
            setFlags(FLAGS_TYPE_WITH_CLASSIFIER, true);
            CharSequence typeName = classifier.getName();
            if (typeName == null || typeName.length()==0){
                this.classifierText = initClassifierText(ast);
            } else {
                this.classifierText = typeName;
            }
        }
        if (this.classifierText == null) {
            CndUtils.assertTrueInConsole(false, "why null classifierText?"+classifier);
            this.classifierText = NON_INITIALIZED_CLASSIFIER_TEXT;
        }
        instantiationParams.trimToSize();
    }

    // package-local - for facory only
    TypeImpl(CsmFile file, int pointerDepth, boolean reference, int arrayDepth, boolean _const, int startOffset, int endOffset) {
        super(file, startOffset, endOffset);
        this.classifierText = NON_INITIALIZED_CLASSIFIER_TEXT;
        this.pointerDepth = (byte) pointerDepth;
        setFlags(FLAGS_REFERENCE, reference);
        this.arrayDepth = (byte) arrayDepth;
        setFlags(FLAGS_CONST, _const);
    }

    // package-local - for factory only
    TypeImpl(TypeImpl type, int pointerDepth, boolean reference, int arrayDepth, boolean _const) {
        super(type.getContainingFile(), type.getStartOffset(), type.getEndOffset());

        this.pointerDepth = (byte) pointerDepth;
        setFlags(FLAGS_REFERENCE, reference);
        this.arrayDepth = (byte) arrayDepth;
        setFlags(FLAGS_CONST, _const);
        setFlags(FLAGS_TYPE_OF_TYPEDEF, type.isTypeOfTypedef());
        
        this.classifierUID = type.classifierUID;
        this.qname = type.qname;
        this.classifierText = type.classifierText;
        if (!type.instantiationParams.isEmpty()) {
            this.instantiationParams.addAll(type.instantiationParams);
        }
        instantiationParams.trimToSize();
    }

    public void setTypeOfTypedef() {
        setFlags(FLAGS_TYPE_OF_TYPEDEF, true);
    }

    protected boolean hasFlags(byte mask) {
        return (flags & mask) == mask;
    }

    protected void setFlags(byte mask, boolean value) {
        if (value) {
            flags |= mask;
        } else {
            flags &= ~mask;
        }
    }

    private static boolean isTypeDefAST(AST ast){
        if (ast != null ) {
            if (ast.getType() == CPPTokenTypes.CSM_FIELD ||
                ast.getType() == CPPTokenTypes.CSM_GENERIC_DECLARATION) {
                return true;
            }
        }
        return false;
    }

    // package-local - for factory only
    TypeImpl(TypeImpl type, List<CsmSpecializationParameter> instantiationParams) {
        super(type.getContainingFile(), type.getStartOffset(), type.getEndOffset());

        this.pointerDepth = (byte) type.getPointerDepth();
        setFlags(FLAGS_REFERENCE, type.isReference());
        this.arrayDepth = (byte) type.getArrayDepth();
        setFlags(FLAGS_CONST, type.isConst());
        setFlags(FLAGS_TYPE_OF_TYPEDEF, type.isTypeOfTypedef());
        setFlags(FLAGS_TYPE_WITH_CLASSIFIER, type.isTypeWithClassifier());

        this.classifierUID = type.classifierUID;
        this.qname = type.qname;
        this.classifierText = type.classifierText;
        if (!instantiationParams.isEmpty()) {
            this.instantiationParams.addAll(instantiationParams);
        }
        this.instantiationParams.trimToSize();
    }


    // package-local
    TypeImpl(CsmType type) {
        super(type.getContainingFile(), type.getStartOffset(), type.getEndOffset());

        this.pointerDepth = (byte) type.getPointerDepth();
        setFlags(FLAGS_REFERENCE, type.isReference());
        this.arrayDepth = (byte) type.getArrayDepth();
        setFlags(FLAGS_CONST, type.isConst());

        if (type instanceof TypeImpl) {
            TypeImpl ti = (TypeImpl) type;
            setFlags(FLAGS_TYPE_OF_TYPEDEF, ti.isTypeOfTypedef());
            setFlags(FLAGS_TYPE_WITH_CLASSIFIER, ti.isTypeWithClassifier());
            this.classifierUID = ti.classifierUID;
            this.qname = ti.qname;
            this.classifierText = ti.classifierText;
            if (!ti.instantiationParams.isEmpty()) {
                this.instantiationParams.addAll(ti.instantiationParams);
            }
        }
        instantiationParams.trimToSize();
    }

     /*TypeImpl(AST ast, CsmFile file, int pointerDepth, boolean reference, int arrayDepth) {
        this(null, pointerDepth, reference, arrayDepth, ast, file, null);
     }*/

    public static int getEndOffset(AST node) {
        AST ast = node;
        if( ast == null ) {
            return 0;
        }
        if (isTypeDefAST(ast)) {
            return OffsetableBase.getEndOffset(ast);
        }
        ast = getLastNode(ast);
        if( ast instanceof CsmAST ) {
            return ((CsmAST) ast).getEndOffset();
        }
        return OffsetableBase.getEndOffset(node);
    }

    private static AST getLastNode(AST first) {
        AST last = first;
        for( AST token = last; token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
                case CPPTokenTypes.CSM_VARIABLE_DECLARATION:
                case CPPTokenTypes.CSM_VARIABLE_LIKE_FUNCTION_DECLARATION:
                case CPPTokenTypes.CSM_QUALIFIED_ID:
                case CPPTokenTypes.CSM_ARRAY_DECLARATION:
                    return AstUtil.getLastChildRecursively(last);
                default:
                    last = token;
            }
        }
        return null;
    }

    public boolean isReference() {
        return hasFlags(FLAGS_REFERENCE);
    }

    public boolean isPointer() {
        return pointerDepth > 0;
    }

    private boolean isTypeOfTypedef() {
        return hasFlags(FLAGS_TYPE_OF_TYPEDEF);
    }

    private boolean isTypeWithClassifier() {
        return hasFlags(FLAGS_TYPE_WITH_CLASSIFIER);
    }

    public List<CsmSpecializationParameter> getInstantiationParams() {
        return instantiationParams;
    }

    public boolean isInstantiation() {
        return !instantiationParams.isEmpty();
    }

    /** Though it returns the same for now, it's better if its name differs */
    protected boolean isInstantiationOrSpecialization() {
        return !instantiationParams.isEmpty();
    }

    public boolean isTemplateBased() {
        return isTemplateBased(new HashSet<CsmType>());
    }

    public boolean isTemplateBased(Set<CsmType> visited) {
        CsmClassifier classifier = getClassifier();
        if (CsmKindUtilities.isTypedef(classifier)) {
            if (visited.contains(this)) {
                return false;
            }
            visited.add(this);
            CsmType type = ((CsmTypedef)classifier).getType();
            if (type instanceof SafeTemplateBasedProvider) {
                return ((SafeTemplateBasedProvider)type).isTemplateBased(visited);
            } else {
                return type.isTemplateBased();
            }
        }
        return false;
    }

    public static boolean initIsConst(AST node) {
        if( node != null ) {
            for( AST token = node; token != null; token = token.getNextSibling() ) {
                int tokenType = token.getType();
                if (AstRenderer.isConstQualifier(tokenType)) {
                    return true;
                } else if (tokenType == CPPTokenTypes.CSM_VARIABLE_DECLARATION ||
                           tokenType == CPPTokenTypes.CSM_ARRAY_DECLARATION ||
                               tokenType == CPPTokenTypes.CSM_QUALIFIED_ID) {
                    return false;
                }
            }
        }
        return false;
    }

    public boolean isConst() {
        return hasFlags(FLAGS_CONST);
    }

    public CharSequence getCanonicalText() {
        CharSequence text = getClassifierText();
        if (isInstantiationOrSpecialization()) {
            text = text.toString() + Instantiation.getInstantiationCanonicalText(this.instantiationParams);
        }
	return decorateText(text, this, true, null);
    }

    /*package*/static CharSequence getCanonicalText(CsmType type) {
        CharSequence canonicalText = null;
        if (type instanceof CsmTemplateParameterType) {
            CsmTemplateParameterType parType = (CsmTemplateParameterType) type;
            CsmTemplateParameter par = parType.getParameter();
            if (CsmKindUtilities.isClassifierBasedTemplateParameter(par)) {
                canonicalText = TemplateUtils.TYPENAME_STRING;
            }
        }
        if (canonicalText == null) {
            canonicalText = type.getCanonicalText().toString();
        }
        return canonicalText;
    }


    // package
    CharSequence getOwnText() {
        if (qname != null && qname.length>0) {
            return qname[qname.length-1];
        } else {
            return "";
        }
    }

    @Override
    public CharSequence getText() {
	// TODO: resolve typedefs
	return decorateText(getClassifierText().toString() + getInstantiationText(this), this, false, null).toString();
    }

    protected StringBuilder getText(boolean canonical, CharSequence variableNameToInsert) {
        return decorateText(getClassifierText().toString()  + getInstantiationText(this), this, canonical, variableNameToInsert);
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

    CharSequence initClassifierText(AST node) {
        if( node == null ) {
            CsmClassifier classifier = _getClassifier();
            return classifier == null ? "" : classifier.getName();
        }
        else {
            StringBuilder sb = new StringBuilder();
            addText(sb, AstRenderer.getFirstSiblingSkipQualifiers(node));
            return TextCache.getManager().getString(sb.toString());
//            return sb.toString();
        }
    }

    /*
     * Add text without instantiation params
     */
    private static void addText(StringBuilder sb, AST ast) {
        if( ! (ast instanceof FakeAST) ) {
            if( sb.length() > 0 ) {
                sb.append(' ');
            }
            sb.append(ast.getText());
        }
        int curDepth = 0;
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            if (token.getType() == CPPTokenTypes.LESSTHAN) {
                curDepth++;
                continue;
            } else if (token.getType() == CPPTokenTypes.GREATERTHAN) {
                curDepth--;
                continue;
            }
            if (curDepth == 0) {
                addText(sb,  token);
            }
        }
    }

    public CsmClassifier getClassifier() {
        return getClassifier(null);
    }

    public static CharSequence getInstantiationText(CsmType type) {
        StringBuilder sb = new StringBuilder();
        if (!type.getInstantiationParams().isEmpty()) {
            sb.append('<');
            boolean first = true;
            for (CsmSpecializationParameter param : type.getInstantiationParams()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(',');
                }
                sb.append(param.getText());
            }
            TemplateUtils.addGREATERTHAN(sb);
        }
	return sb;
    }

    public CharSequence getClassifierText() {
        return classifierText;
    }

    public CsmClassifier getClassifier(Resolver parent) {
        CsmClassifier classifier = _getClassifier();
        boolean needToRender = true;
        if (CsmBaseUtilities.isValid(classifier)) {
            // skip
            needToRender = false;
            if (!isTypeWithClassifier() && (qname != null) && (parent != null) && !CsmKindUtilities.isBuiltIn(classifier)) {
                // check visibility of classifier
                if (ForwardClass.isForwardClass(classifier) || !CsmIncludeResolver.getDefault().isObjectVisible(parent.getStartFile(), classifier)) {
                    needToRender = true;
                    classifier = null;
                }
            }
        }
        if (needToRender) {
            int newParseCount = FileImpl.getParseCount();
            if (classifier != null) {
                if (newParseCount == parseCount) {
                    return classifier;
                }
            }
            _setClassifier(null);

            if (qname != null) {
                classifier = renderClassifier(qname, parent);
            } else if (classifierText.length() > 0) {
                classifier = renderClassifier(new CharSequence[] { classifierText }, parent );
            }
            synchronized (this) {
                _setClassifier(classifier);
                parseCount = newParseCount;
            }
            classifier = _getClassifier();
        }
        if (isInstantiation() && CsmKindUtilities.isTemplate(classifier) && !((CsmTemplate)classifier).getTemplateParameters().isEmpty()) {
            CsmInstantiationProvider ip = CsmInstantiationProvider.getDefault();
            CsmObject obj;
            if (ip instanceof InstantiationProviderImpl) {
                Resolver resolver = ResolverFactory.createResolver(getContainingFile(), getStartOffset(), parent);
                if (!resolver.isRecursionOnResolving(Resolver.INFINITE_RECURSION)) {
                    obj = ((InstantiationProviderImpl) ip).instantiate((CsmTemplate) classifier, getInstantiationParams(), this, getContainingFile(), resolver, getStartOffset());
                } else {
                    return null;
                }
            } else {
                obj = ip.instantiate((CsmTemplate) classifier, getInstantiationParams(), this, getContainingFile(), getStartOffset());
            }
            if (CsmKindUtilities.isClassifier(obj)) {
                classifier = (CsmClassifier) obj;
            }
        }
        return classifier;
    }

    protected CsmClassifier renderClassifier(CharSequence[] qname, Resolver parent) {
        CsmClassifier result = null;
        Resolver resolver = ResolverFactory.createResolver(getContainingFile(), getStartOffset(), parent);
        if (isInstantiationOrSpecialization()) {
            CharSequence[] specializationQname = new CharSequence[qname.length];
            final int last = qname.length - 1;
            StringBuilder sb = new StringBuilder(qname[last]);
            sb.append(Instantiation.getInstantiationCanonicalText(this.instantiationParams));
            specializationQname[last] = sb.toString();
            for (int i = 0; i < last; i++) {
                specializationQname[i] = qname[i];
            }
            CsmObject o = resolver.resolve(specializationQname, Resolver.CLASSIFIER);
            if( CsmKindUtilities.isClassifier(o) ) {
                result = (CsmClassifier) o;
            }
            if (result == null) {
                specializationQname[last] = qname[last].toString() + "<>"; //NOI18N
                o = resolver.resolve(specializationQname, Resolver.CLASSIFIER);
                if( CsmKindUtilities.isClassifier(o) ) {
                    result = (CsmClassifier) o;
                }
            }
        }
        if (result == null) {
            CsmObject o = resolver.resolve(qname, Resolver.CLASSIFIER);
            if( CsmKindUtilities.isClassifier(o) ) {
                result = (CsmClassifier) o;
            }
        }
        if( result == null ) {
            result = ((ProjectBase) getContainingFile().getProject()).getDummyForUnresolved(qname, getContainingFile(), getStartOffset());
        }
        return result;
    }

    private CsmClassifier initClassifier(AST node) {
        AST tokType = AstRenderer.getFirstSiblingSkipQualifiers(node);
        if (tokType == null ||
                (tokType.getType() != CPPTokenTypes.CSM_TYPE_BUILTIN &&
                tokType.getType() != CPPTokenTypes.CSM_TYPE_COMPOUND) &&
                tokType.getType() != CPPTokenTypes.CSM_QUALIFIED_ID) {
            return null;
        }

        if (tokType.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN) {
            return BuiltinTypes.getBuiltIn(tokType);
        } else { // tokType.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND
            try {
                CsmAST tokFirstId = (CsmAST) tokType.getFirstChild();
                if (tokFirstId == null) {
                    // this is unnormal; but we should be able to work even on incorrect AST
                    return null;
                }

                //Resolver resolver = ResolverFactory.createResolver(getContainingFile(), firstOffset);
                // gather name components into string array
                // for example, for std::vector new CharSequence[] { "std", "vector" }

                //TODO: we have AstRenderer.getNameTokens, it is better to use it here
                List<CharSequence> l = new ArrayList<CharSequence>();
                int templateDepth = 0;
                for (AST namePart = tokFirstId; namePart != null; namePart = namePart.getNextSibling()) {
                    if (templateDepth == 0 && namePart.getType() == CPPTokenTypes.ID) {
                        l.add(NameCache.getManager().getString(namePart.getText()));
                    } else if (namePart.getType() == CPPTokenTypes.LESSTHAN) {
                        // the beginning of template parameters
                        templateDepth++;
                    } else if (namePart.getType() == CPPTokenTypes.GREATERTHAN) {
                        // the beginning of template parameters
                        templateDepth--;
                    } else {
                        //assert namePart.getType() == CPPTokenTypes.SCOPE;
                        if (templateDepth == 0) {
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
                                    || namePart.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND
                                    || namePart.getType() == CPPTokenTypes.LITERAL_struct) {
                                CsmType type = AstRenderer.renderType(namePart, getContainingFile());
                                instantiationParams.add(new TypeBasedSpecializationParameterImpl(type));
                            }
                            if (namePart.getType() == CPPTokenTypes.CSM_EXPRESSION) {
                                instantiationParams.add(new ExpressionBasedSpecializationParameterImpl(new ExpressionStatementImpl(namePart, getContainingFile(), null),
                                        getContainingFile(), OffsetableBase.getStartOffset(namePart), OffsetableBase.getEndOffset(namePart)));
                            }
                        }
                    }
                }
                qname = l.toArray(new CharSequence[l.size()]);
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
            } catch (Exception e) {
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

    protected CsmClassifier _getClassifier() {
        CsmClassifier classifier = UIDCsmConverter.UIDtoDeclaration(classifierUID);
        // can be null if cached one was removed
        return classifier;
    }

    void _setClassifier(CsmClassifier classifier) {
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
	return decorateText(getClassifierText(), this, false, variableName).toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent

    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        output.writeByte(pointerDepth);
        output.writeByte(arrayDepth);
        output.writeByte(flags);
        assert this.classifierText != null;
        PersistentUtils.writeUTF(classifierText, output);

        PersistentUtils.writeStrings(qname, output);
        PersistentUtils.writeSpecializationParameters(instantiationParams, output);
        UIDObjectFactory.getDefaultFactory().writeUID(classifierUID, output);
    }

    public TypeImpl(DataInput input) throws IOException {
        super(input);
        this.pointerDepth = input.readByte();
        this.arrayDepth= input.readByte();
        this.flags = input.readByte();
        this.classifierText = PersistentUtils.readUTF(input, NameCache.getManager());
        assert this.classifierText != null;

        this.qname = PersistentUtils.readStrings(input, NameCache.getManager());
        PersistentUtils.readSpecializationParameters(this.instantiationParams, input);
        instantiationParams.trimToSize();
        this.classifierUID = UIDObjectFactory.getDefaultFactory().readUID(input);
    }
}
