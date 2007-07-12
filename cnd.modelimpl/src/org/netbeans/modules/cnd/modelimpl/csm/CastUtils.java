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

import antlr.collections.AST;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;

/**
 * Utility class used for user cast operators processing.
 *
 * @author Vladimir Kvashin
 */
public class CastUtils {
    
    public static boolean isCast(AST ast) {
	switch( ast.getType() ) {
	    case CPPTokenTypes.CSM_USER_TYPE_CAST:
	    case CPPTokenTypes.CSM_USER_TYPE_CAST_DEFINITION:
		return true;
	    default:
		return false;
	}
    }
    
    public static String getFunctionName(AST ast) {
	assert isCast(ast);
	AST operator = AstUtil.findChildOfType(ast, CPPTokenTypes.LITERAL_OPERATOR);
	if( operator == null ) {
            // error in AST
	    return "operator ???"; // NOI18N
	}
	StringBuilder sb = new StringBuilder(operator.getText());
	sb.append(' ');
	begin:
	for( AST next = operator.getNextSibling(); next != null; next = next.getNextSibling() ) {
	    switch( next.getType() ) {
		case CPPTokenTypes.CSM_TYPE_BUILTIN:
		case CPPTokenTypes.CSM_TYPE_COMPOUND:
		    sb.append(' ');
		    addTypeText(next, sb);
		    break;
		case CPPTokenTypes.LPAREN:
		    break begin;
		case CPPTokenTypes.AMPERSAND:
		case CPPTokenTypes.STAR:
		case CPPTokenTypes.LITERAL_const:
		    sb.append(next.getText());
		    break;
		default:
		    sb.append(' ');
		    sb.append(next.getText());
	    }
	}
	return sb.toString();
    }
    
    private static void addTypeText(AST ast, StringBuilder sb) {
	if( ast == null ) {
	    return;
	}
	for( AST child = ast.getFirstChild(); child != null; child = child.getNextSibling() ) {
	    if( CPPTokenTypes.CSM_START <= child.getType() && child.getType() <= CPPTokenTypes.CSM_END ) {
		addTypeText(child, sb);
	    }
	    else {
		String text = child.getText();
		assert text != null;
		assert text.length() > 0;
		if( sb.length() > 0 ) {
		    if( Character.isLetterOrDigit(sb.charAt(sb.length() - 1)) ) {
			if( Character.isLetterOrDigit(text.charAt(0)) ) {
			    sb.append(' ');
			}
		    }
		}
		sb.append(text);
	    }
	}
    }
    
    public static String[] getClassOrNspNames(AST ast) {
	assert isCast(ast);
	AST child = ast.getFirstChild();
	if( child != null && child.getType() == CPPTokenTypes.ID ) {
	    AST next = child.getNextSibling();
	    if( next != null && next.getType() == CPPTokenTypes.SCOPE ) {
		List<String> l = new ArrayList<String>();
		l.add(child.getText());
		begin:
		for( next = next.getNextSibling(); next != null; next = next.getNextSibling() ) {
		    switch( next.getType() ) {
			case CPPTokenTypes.ID:
			    l.add(next.getText());
                            break;
			case CPPTokenTypes.SCOPE:
			    break; // do nothing
			default:
			    break begin;
		    }
		}
		return (String[]) l.toArray(new String[l.size()]);
	    }
	}
	return null;
    }
    
    public static boolean isMemberDefinition(AST ast) {
	assert isCast(ast);
	AST child = ast.getFirstChild();
	if( child != null && child.getType() == CPPTokenTypes.ID ) {
	    child = child.getNextSibling();
	    if( child != null && child.getType() == CPPTokenTypes.SCOPE ) {
		return true;
	    }
	}
	return false;
    }
    
    
}
