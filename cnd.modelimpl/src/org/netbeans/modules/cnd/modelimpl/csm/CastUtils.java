/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.parser.FakeAST;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.openide.util.CharSequences;

/**
 * Utility class used for user cast operators processing.
 *
 */
public class CastUtils {
    
    public static boolean isCast(AST ast) {
	switch( ast.getType() ) {
	    case CPPTokenTypes.CSM_USER_TYPE_CAST_DECLARATION:
	    case CPPTokenTypes.CSM_USER_TYPE_CAST_DEFINITION:
	    case CPPTokenTypes.CSM_USER_TYPE_CAST_TEMPLATE_DECLARATION:
	    case CPPTokenTypes.CSM_USER_TYPE_CAST_TEMPLATE_DEFINITION:
        case CPPTokenTypes.CSM_USER_TYPE_CAST_DEFINITION_EXPLICIT_SPECIALIZATION:
		return true;
	    default:
		return false;
	}
    }
    
    // Extracts qualified id into subnode CSM_QUALIFIED_ID
    public static AST transform(AST ast) {
        // TODO: this should be done in grammar
        assert isCast(ast);
        AST nameStart = findCastOperatorNameStart(ast);
        AST nameEnd = findCastOperatorNameEnd(ast);
        
        AST transformed = new FakeAST();
        transformed.setType(ast.getType());
        transformed.setFirstChild(AstUtil.cloneAST(ast.getFirstChild(), nameStart, false));
        
        AST qualifiedName = new FakeAST();
        qualifiedName.setType(CPPTokenTypes.CSM_QUALIFIED_ID);
        qualifiedName.setFirstChild(AstUtil.cloneAST(nameStart, nameEnd));
        
        AstUtil.getLastChild(transformed).setNextSibling(qualifiedName);
        qualifiedName.setNextSibling(AstUtil.cloneAST(nameEnd.getNextSibling(), AstUtil.getLastChild(ast)));
        
        return transformed;
    }
    
    public static String getFunctionName(AST ast) {
	assert isCast(ast);
	AST operator = AstUtil.findChildOfType(ast, CPPTokenTypes.LITERAL_OPERATOR);
	if( operator == null ) {
            // error in AST
	    return "operator ???"; // NOI18N
	}
	StringBuilder sb = new StringBuilder(AstUtil.getText(operator));
	sb.append(' ');
	begin:
	for( AST next = operator.getNextSibling(); next != null; next = next.getNextSibling() ) {
	    switch( next.getType() ) {
		case CPPTokenTypes.CSM_TYPE_BUILTIN:
		case CPPTokenTypes.CSM_TYPE_COMPOUND:
                case CPPTokenTypes.CSM_TYPE_ATOMIC:
		    sb.append(' ');
		    addTypeText(next, sb);
                    break;
		case CPPTokenTypes.CSM_PTR_OPERATOR:
		    addTypeText(next, sb);
                    break;
		case CPPTokenTypes.LPAREN:
		    break begin;
		case CPPTokenTypes.AMPERSAND:
		case CPPTokenTypes.STAR:
		case CPPTokenTypes.LITERAL_const:
                case CPPTokenTypes.LITERAL___const:    
                case CPPTokenTypes.LITERAL___const__:
		    sb.append(AstUtil.getText(next));
		    break;
		default:
		    sb.append(' ');
		    sb.append(AstUtil.getText(next));
	    }
	}
	return sb.toString();
    }
    
    public static CharSequence getFunctionRawName(AST token) {
        return getFunctionRawName(token, "."); // NOI18N
    }

    public static CharSequence getFunctionRawName(AST token, String separator) {
        assert isCast(token);
        AST ast = token;
        token = token.getFirstChild();
        StringBuilder l = new StringBuilder();
        for( ; token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
                case CPPTokenTypes.IDENT:
                    if (l.length()>0) {
                        l.append(separator);
                    }
                    l.append(AstUtil.getText(token));
                    break;
                case CPPTokenTypes.SCOPE:
                    break;
                case CPPTokenTypes.LITERAL_OPERATOR:
                    if (l.length()>0) {
                        l.append(separator);
                    }
                    l.append(getFunctionName(ast));
                    return NameCache.getManager().getString(CharSequences.create(l));
                default:
                    //TODO: process templates
                    break;
            }
        }
        return NameCache.getManager().getString(CharSequences.create(l));
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
		CharSequence text = AstUtil.getText(child);
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
        while (child != null && child.getType() == CPPTokenTypes.LITERAL_template) {
            child = AstRenderer.skipTemplateSibling(child);
        }
        child = AstRenderer.getFirstSiblingSkipInline(child);
        child = AstRenderer.getFirstSiblingSkipQualifiers(child);
	if( child != null && child.getType() == CPPTokenTypes.IDENT ) {
	    child = child.getNextSibling();
	    if( child != null && child.getType() == CPPTokenTypes.LESSTHAN ) {
                child = AstRenderer.skipTemplateParameters(child);
	    }
            if( child != null && child.getType() == CPPTokenTypes.SCOPE ) {
		return true;
	    }
	}
	return false;
    }

    public static boolean isCastOperatorOnlySpecialization(AST ast) {
        assert isCast(ast);        
        AST operator = AstUtil.findChildOfType(ast, CPPTokenTypes.LITERAL_OPERATOR);
        if (operator != null) {
            AST nameStart = findCastOperatorNameStart(ast);
            while (nameStart != null && nameStart != operator) {
                if (nameStart.getType() == CPPTokenTypes.LESSTHAN) {
                    return false;
                }
                nameStart = nameStart.getNextSibling();
            }
            return true;
        }
        return false;
    }
    
    public static AST findCastOperatorNameStart(AST ast) {
        assert isCast(ast);   
        AST lastTemplateInHeader = ast.getFirstChild();
        if (lastTemplateInHeader != null && lastTemplateInHeader.getType() != CPPTokenTypes.LITERAL_template) {
            return lastTemplateInHeader;
        }
        AST current = lastTemplateInHeader;
        while (current != null && current.getNextSibling() != null && current.getNextSibling().getType() == CPPTokenTypes.LESSTHAN) {
            lastTemplateInHeader = current;
            current = AstUtil.findSiblingOfType(current.getNextSibling(), CPPTokenTypes.LITERAL_template);
        }
        return AstUtil.findSiblingOfType(lastTemplateInHeader, CPPTokenTypes.GREATERTHAN).getNextSibling();        
    }

    public static AST findCastOperatorNameEnd(AST ast) {
        assert isCast(ast);
        AST nameStart = findCastOperatorNameStart(ast);
        AST nameEnd = nameStart;
        while (nameStart != null && nameStart.getType() != CPPTokenTypes.LPAREN) {
            nameEnd = nameStart;
            nameStart = nameStart.getNextSibling();
        }
        return nameEnd;
    }    
}
