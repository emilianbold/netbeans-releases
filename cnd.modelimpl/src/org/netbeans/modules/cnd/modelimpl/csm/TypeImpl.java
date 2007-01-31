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

import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.FakeAST;
import java.util.*;

import antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.apt.utils.TextCache;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

/**
 *
 * @author Vladimir Kvashin
 */
public class TypeImpl extends OffsetableBase implements CsmType {

    private final int pointerDepth;
    private final boolean reference;
    private final int arrayDepth;
    private final boolean _const;
    private final String classifierText;
    
    // FIX for lazy resolver calls
    private String[] qname = null;
    private int firstOffset;
    private CsmClassifier classifier;
    
    private TypeImpl(CsmClassifier classifier, int pointerDepth, boolean reference, int arrayDepth, AST ast, CsmFile file) {
        super(ast, file);
        this.classifier = classifier;
        this.pointerDepth = pointerDepth;
        this.reference = reference;
        this.arrayDepth = arrayDepth;
        _const = initIsConst(ast);
        if (this.classifier == null) {
            this.classifier = initClassifier(ast);
            this.classifierText = initClassifierText(ast);
        } else {
            String typeName = classifier.getName();
            if (typeName == null || typeName.length()==0){
                this.classifierText = initClassifierText(ast);
            } else {
                this.classifierText = typeName;
            }
        }
    }

    private TypeImpl(AST classifier, CsmFile file, int pointerDepth, boolean reference, int arrayDepth) {
        super(classifier, file);
        //setAst(classifier);
        this.pointerDepth = pointerDepth;
        this.reference = reference;
        this.arrayDepth = arrayDepth;
        _const = initIsConst(classifier);
        this.classifier = initClassifier(classifier);
        this.classifierText = initClassifierText(classifier);
    }
    
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

    public String getText() {
//        if( text == null ) {
            StringBuffer sb = new StringBuffer();
            if( isConst() ) {
                sb.append("const "); // NOI18N
            }
            sb.append(classifierText);
            for( int i = 0; i < getPointerDepth(); i++ ) {
                sb.append('*');
            }
            if( isReference() ) {
                sb.append('&');
            }
            for( int i = 0; i < getArrayDepth(); i++ ) {
                sb.append("[]"); // NOI18N
            }
//            text = sb.toString();
//        }
//        return text;
            return sb.toString();
    }
    
    private String initClassifierText(AST node) {
        if( node == null ) {
            return classifier == null ? "" : classifier.getName();
        }
        else {
            StringBuffer sb = new StringBuffer();
            addText(sb, AstRenderer.getFirstSiblingSkipQualifiers(node));
            return TextCache.getString(sb.toString());
//            return sb.toString();
        }
    }
    
    private static void addText(StringBuffer sb, AST ast) {
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
        if ((classifier == null) && (qname != null)) {
            classifier = renderClassifier();
        }
        return classifier;
    }
    
    private CsmClassifier renderClassifier() {
        CsmClassifier result = null;
        Resolver resolver = ResolverFactory.createResolver(getContainingFile(), firstOffset);
        CsmObject o = resolver.resolve(qname);
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
                        if( templateDepth == 0 && namePart.getType() != CPPTokenTypes.SCOPE ) {
                            if (TraceFlags.DEBUG) {
                                StringBuffer tokenText = new StringBuffer();
                                tokenText.append('[').append(namePart.getText());
                                if (namePart.getNumberOfChildren() == 0) {
                                    tokenText.append(", line=").append(namePart.getLine()); // NOI18N
                                    tokenText.append(", column=").append(namePart.getColumn()); // NOI18N
                                }
                                tokenText.append(']');
                                System.err.println("Incorect token: expected '::', found " + tokenText.toString());
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
                e.printStackTrace(System.err);
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

    public static TypeImpl createType(AST classifier, CsmFile file,  AST ptrOperator, int arrayDepth) {
        boolean pointer = false;
        boolean refence = false;
        int pointerDepth = 0;
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
        return new TypeImpl(classifier, file, pointerDepth, refence, arrayDepth);
    }
    
    public static TypeImpl createBuiltinType(String text, AST ptrOperator, int arrayDepth, AST ast, CsmFile file) {
        CsmBuiltIn builtin = BuiltinTypes.getBuiltIn(text);
        return createType(builtin, ptrOperator, arrayDepth, ast, file);
    }

    public static TypeImpl createType(CsmClassifier classifier, AST ptrOperator, int arrayDepth, AST ast, CsmFile file) {
        boolean pointer = false;
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
        return new TypeImpl(classifier, pointerDepth, refence, arrayDepth, ast, file);
    }
    
}
