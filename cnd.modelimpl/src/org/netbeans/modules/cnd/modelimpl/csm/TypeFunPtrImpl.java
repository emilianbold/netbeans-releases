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

import java.io.DataInput;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.FakeAST;
import java.util.*;

import antlr.collections.AST;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.apt.utils.TextCache;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 * Represent pointer to function type
 * @author Vladimir Kvashin
 */
public class TypeFunPtrImpl extends TypeImpl {

    private String functionPointerParamList;
    private short functionPointerDepth;
    
    private static class Pair {
	public String paramList;
	public short pointerDepth;
    }
    
    public TypeFunPtrImpl(AST classifier, CsmFile file, int pointerDepth, boolean reference, int arrayDepth) {
	super(classifier, file, pointerDepth, reference, arrayDepth);
	Pair pair = getFunctionPointerParamList(classifier, true);
	assert pair != null;
	functionPointerParamList = pair.paramList;
	functionPointerDepth = pair.pointerDepth;
    }
    
    protected StringBuilder getText(boolean canonical, String variableNameToInsert) {
	StringBuilder sb = new StringBuilder();
	if( isConst() ) {
	    sb.append("const "); // NOI18N
	}
	sb.append(getClassifierText());
	for( int i = 0; i < getPointerDepth(); i++ ) {
	    sb.append('*');
	}
	if( isReference() ) {
	    sb.append('&');
	}
	for( int i = 0; i < getArrayDepth(); i++ ) {
	    sb.append(canonical ? "*" : "[]"); // NOI18N
	}
	
	sb.append('(');
	for (int i = 0; i < functionPointerDepth; i++) {
	    sb.append('*');
	    if( variableNameToInsert != null ) {
		sb.append(variableNameToInsert);
	    }
	}
	sb.append(')');
	
	sb.append('(');
	sb.append(functionPointerParamList);
	sb.append(')');
	
	return sb;
    }
    
    public static boolean isFunctionPointerParamList(AST classifier) {
	return getFunctionPointerParamList(classifier, false) != null;
    }
    
    private static Pair getFunctionPointerParamList(AST classifier, boolean fillText) {
	
	// find opening brace
	AST brace = AstUtil.findSiblingOfType(classifier, CPPTokenTypes.LPAREN);
	if( brace == null ) {
	    return null;
	}
	
	// check whether it's followed by asterisk
	AST next = brace.getNextSibling();
	if( next == null || next.getType() != CPPTokenTypes.CSM_PTR_OPERATOR) {
	    return null;
	}
	
	Pair pair = new Pair();
	
	// skip adjacent asterisks
	do {
	    next = next.getNextSibling();
	    pair.pointerDepth++;
	}
	while( next != null && next.getType() == CPPTokenTypes.CSM_PTR_OPERATOR );

	// check that it's followed by CSM_VARIABLE_DECLARATION
	if( next == null && next.getType() != CPPTokenTypes.CSM_VARIABLE_DECLARATION ) {
	    return null;
	}
	
	// last step: verify that it's followed with a closing brace
	next = next.getNextSibling();
	if( next == null || next.getType() != CPPTokenTypes.RPAREN ) {
	    return null;
	}
	
	next = next.getNextSibling();
	
	// skip LPAREN (let's not assume it's obligatory)
	if( next != null && next.getType() == CPPTokenTypes.LPAREN ) {
	    next = next.getNextSibling();
	}
	
	if( next == null ) {
	    return null;
	}
	if( next.getType() == CPPTokenTypes.CSM_PARMLIST ) {
	    if( fillText ) {
		pair.paramList = gatherChildrenText(next);
	    }
	    return pair;
	}
	else if( next.getType() == CPPTokenTypes.RPAREN ) {
	    pair.paramList = "";
	    return pair;
	}
	else {
	    return null;
	}
	
	
    }
    
    private static String gatherChildrenText(AST ast) {
	StringBuilder sb = new StringBuilder();
	for( AST next = ast.getFirstChild(); next != null; next = next.getNextSibling() ) {
	    if( sb.length() > 0 ) {
		sb.append(',');
	    }
	    StringBuilder param = new StringBuilder();
	    addText(param, next);
	    sb.append(param);
	}
	return sb.toString();
    }    

    // copy-pasted from TypeImpl. The difference us in one check: ast.getType() != CPPTokenTypes.STAR
    // it's so small that parametherising isn't better
    private static void addText(StringBuilder sb, AST ast) {
        if( ! (ast instanceof FakeAST) ) {
            if( sb.length() > 0 && ast.getType() != CPPTokenTypes.STAR ) {
                sb.append(' ');
            }
            sb.append(ast.getText());
        }
        for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
            addText(sb,  token);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    
    public void write(DataOutput output) throws IOException {
        super.write(output);
	assert functionPointerParamList != null;
	output.writeShort(functionPointerDepth);
	output.writeUTF(this.functionPointerParamList);
    }

    public TypeFunPtrImpl(DataInput input) throws IOException {
        super(input);
	this.functionPointerDepth = input.readShort();
        this.functionPointerParamList = TextCache.getString(input.readUTF());
    }
    
}
