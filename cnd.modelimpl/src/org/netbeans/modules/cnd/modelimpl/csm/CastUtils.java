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

import antlr.collections.AST;
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
	    case CPPTokenTypes.CSM_USER_TYPE_CAST_DECLARATION:
	    case CPPTokenTypes.CSM_USER_TYPE_CAST_DEFINITION:
	    case CPPTokenTypes.CSM_USER_TYPE_CAST_TEMPLATE_DECLARATION:
	    case CPPTokenTypes.CSM_USER_TYPE_CAST_TEMPLATE_DEFINITION:
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
                case CPPTokenTypes.LITERAL___const:    
                case CPPTokenTypes.LITERAL___const__:
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
