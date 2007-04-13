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
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;

/**
 * Common functions related with templates.
 * Typically used by CsmClass ans CsmFunction, which has to implement CsmTemplate,
 * but 
 * @author Vladimir Kvashin
 */
public class TemplateUtils {

//    public static final byte MASK_TEMPLATE = 0x01;
//    public static final byte MASK_SPECIALIZATION = 0x02;

    public static String getSpecializationSuffix(AST qIdToken) {
	StringBuilder sb  = new StringBuilder();
	for( AST child = qIdToken.getFirstChild(); child != null; child = child.getNextSibling() ) {
	    if( child.getType() == CPPTokenTypes.LESSTHAN ) {
		addSpecializationSuffix(child, sb);
		break;
	    }
	}
	return sb.toString();
    }
    
    private static void addSpecializationSuffix(AST firstChild, StringBuilder sb) {
	for( AST child = firstChild; child != null; child = child.getNextSibling() ) {
	    if( CPPTokenTypes.CSM_START <= child.getType() && child.getType() <= CPPTokenTypes.CSM_END ) {
		AST grandChild = child.getFirstChild();
		if( grandChild != null ) {
		    addSpecializationSuffix(grandChild, sb);
		}
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
    
    public static boolean isPartialClassSpecialization(AST ast) {
	assert(ast.getType() == CPPTokenTypes.CSM_TEMPLATE_CLASS_DECLARATION);
	for( AST node = ast.getFirstChild(); node != null; node = node.getNextSibling() ) {
	    if( node.getType() == CPPTokenTypes.CSM_QUALIFIED_ID ) {
		for( AST child = node.getFirstChild(); child != null; child = child.getNextSibling() ) {
		    if( child.getType() == CPPTokenTypes.LESSTHAN ) {
			return true;
		    }
		}
	    }
	}
	return false;
    }
}
