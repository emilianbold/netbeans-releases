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
import org.netbeans.modules.cnd.modelimpl.parser.FakeAST;
import java.util.*;

import antlr.collections.AST;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;

/**
 * Represent pointer to function type
 * @author Vladimir Kvashin
 */
public class TypeFunPtrImpl extends TypeImpl {

    private CharSequence functionPointerParamList;
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
    
    @Override
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

	if( next == null) {
	    return null;
	}
	
        // check that it's followed by exprected token
        if (next.getType() == CPPTokenTypes.CSM_VARIABLE_DECLARATION) {
            // fine. this could be variable of function type
        } else if (next.getType() == CPPTokenTypes.CSM_QUALIFIED_ID) {
            // check function returns function
            next = next.getNextSibling();
            // skip LPAREN (let's not assume it's obligatory)
            if (next == null || next.getType() != CPPTokenTypes.LPAREN) {
                return null;
            }
            next = next.getNextSibling();
            if (next == null) {
                return null;
            }
            // skip params of fun itself
            if (next.getType() == CPPTokenTypes.CSM_PARMLIST) {
                next = next.getNextSibling();
                if (next == null) {
                    return null;
                }
            }               
            // params of fun are closed with RPAREN
            if (next.getType() != CPPTokenTypes.RPAREN) {
                return null;
            }
        }
        // last step: verify that it's followed with a closing brace
        next = next.getNextSibling();
        if (next == null || next.getType() != CPPTokenTypes.RPAREN) {
            return null;
        }

        next = next.getNextSibling();

        // skip LPAREN (let's not assume it's obligatory)
        if (next != null && next.getType() == CPPTokenTypes.LPAREN) {
            next = next.getNextSibling();
        }        
        if (next == null) {
            return null;
        }
        if (next.getType() == CPPTokenTypes.CSM_PARMLIST) {
            if (fillText) {
                pair.paramList = gatherChildrenText(next);
            }
            return pair;
        } else if (next.getType() == CPPTokenTypes.RPAREN) {
            pair.paramList = "";
            return pair;
        } else {
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
    
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
	assert functionPointerParamList != null;
	output.writeShort(functionPointerDepth);
	output.writeUTF(this.functionPointerParamList.toString());
    }

    public TypeFunPtrImpl(DataInput input) throws IOException {
        super(input);
	this.functionPointerDepth = input.readShort();
        this.functionPointerParamList = NameCache.getManager().getString(input.readUTF());
    }
    
}
