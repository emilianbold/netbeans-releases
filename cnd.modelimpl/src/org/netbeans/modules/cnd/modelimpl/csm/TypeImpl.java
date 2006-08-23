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
import org.netbeans.modules.cnd.modelimpl.antlr2.FakeAST;
import java.util.*;

import antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.*;
import org.netbeans.modules.cnd.modelimpl.antlr2.CsmAST;
import org.netbeans.modules.cnd.modelimpl.antlr2.generated.CPPTokenTypes;

import org.netbeans.modules.cnd.modelimpl.csm.deep.*;

import org.netbeans.modules.cnd.modelimpl.platform.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

/**
 *
 * @author Vladimir Kvashin
 */
public class TypeImpl extends OffsetableBase implements CsmType {

    //private AST ast;
    private CsmFile file;
    private CsmClassifier classifier;
    //private String text = null;
    private int pointerDepth;
    private boolean reference;
    private int arrayDepth = 0;
    
    private TypeImpl(CsmClassifier classifier, int pointerDepth, boolean reference, int arrayDepth, AST ast, CsmFile file) {
        super(file, 0, 0);
        setAst(ast);
        this.classifier = classifier;
        this.pointerDepth = pointerDepth;
        this.reference = reference;
        this.arrayDepth = arrayDepth;
    }

    private TypeImpl(AST classifier, CsmFile file, int pointerDepth, boolean reference, int arrayDepth) {
        super(classifier, file);
        //setAst(classifier);
        this.file = file;
        this.pointerDepth = pointerDepth;
        this.reference = reference;
        this.arrayDepth = arrayDepth;
    }
    
    public CsmAST getEndAst() {
        AST ast = getAst();
        if( ast == null ) {
            return null;
        }
        ast = getLastNode(ast);
        if( ast instanceof CsmAST ) {
            return (CsmAST) ast;
        }
        return super.getEndAst();
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
    
    public boolean isConst() {
        if( getAst() != null ) {
            for( AST token = getAst(); token != null; token = token.getNextSibling() ) {
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

    public String getText() {
//        if( text == null ) {
            StringBuffer sb = new StringBuffer();
            if( isConst() ) {
                sb.append("const ");
            }
            sb.append(getClassifierText());
            for( int i = 0; i < getPointerDepth(); i++ ) {
                sb.append('*');
            }
            if( isReference() ) {
                sb.append('&');
            }
            for( int i = 0; i < getArrayDepth(); i++ ) {
                sb.append("[]");
            }
//            text = sb.toString();
//        }
//        return text;
            return sb.toString();
    }
    
    private String getClassifierText() {
        if( getAst() == null ) {
            return classifier == null ? "" : classifier.getName();
        }
        else {
            StringBuffer sb = new StringBuffer();
            addText(sb, AstRenderer.getFirstSiblingSkipQualifiers(getAst()));
            return sb.toString();
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
        if( classifier == null ) {
            classifier = renderClassifier();
        }
        return classifier;
    }
    
    private CsmClassifier renderClassifier() {
        AST tokType = AstRenderer.getFirstSiblingSkipQualifiers(getAst());
        if( tokType == null ||  
            (tokType.getType() != CPPTokenTypes.CSM_TYPE_BUILTIN && 
            tokType.getType() != CPPTokenTypes.CSM_TYPE_COMPOUND) &&
            tokType.getType() != CPPTokenTypes.CSM_QUALIFIED_ID ) {
            return null;
        }

        CsmClassifier result = null;
        if( tokType.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ) {
            result = BuiltinTypes.getBuiltIn(tokType);
        }
        else { // tokType.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND
            try {
                CsmAST tokFirstId = (CsmAST) tokType.getFirstChild();
                        
                int offset = tokFirstId.getOffset();
                Resolver resolver = ResolverFactory.createResolver(file, offset);
                // gather name components into string array 
                // for example, for std::vector new String[] { "std", "vector" }
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
                            StringBuffer tokenText = new StringBuffer();
                            tokenText.append('[').append(namePart.getText());
                            if (namePart.getNumberOfChildren() == 0) {
                                tokenText.append(", line=").append(namePart.getLine());
                                tokenText.append(", column=").append(namePart.getColumn());
                            }
                            tokenText.append(']');
                            System.err.println("Incorect token: expected '::', found " + tokenText.toString());
                        }
                    }
                }
                String[] qname = (String[]) l.toArray(new String[l.size()]);
                CsmObject o = resolver.resolve(qname);
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
                    result = ((ProjectBase) getContainingFile().getProject()).getDummyForUnresolved(qname, file, offset);
                }
            }
            catch( Exception e ) {
                e.printStackTrace(System.err);
            }
        }

        return result;
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
