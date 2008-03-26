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
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
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
    
    // in class our parser skips LESSTHAN symbols in templates...
    public static String getClassSpecializationSuffix(AST qIdToken) {
	StringBuilder sb  = new StringBuilder();
        addSpecializationSuffix(qIdToken.getFirstChild(), sb);
	return sb.toString();
    }
    
    public static void addSpecializationSuffix(AST firstChild, StringBuilder sb) {
        int depth = 0;
	for( AST child = firstChild; child != null; child = child.getNextSibling() ) {
            if (child.getType() == CPPTokenTypes.LESSTHAN) depth ++;
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
		    if( Character.isJavaIdentifierPart(sb.charAt(sb.length() - 1)) ) {
			if( Character.isJavaIdentifierPart(text.charAt(0)) ) {
			    sb.append(' '); 
			}
		    }
		}
		sb.append(text);
                if (child.getType() == CPPTokenTypes.GREATERTHAN) {
                    depth--;
                    if (depth==0)
                        break;
                }
	    }
	}
    }
    
    public static boolean isPartialClassSpecialization(AST ast) {
	if( ast.getType() == CPPTokenTypes.CSM_TEMPLATE_CLASS_DECLARATION ) {
	    for( AST node = ast.getFirstChild(); node != null; node = node.getNextSibling() ) {
		if( node.getType() == CPPTokenTypes.CSM_QUALIFIED_ID ) {
		    for( AST child = node.getFirstChild(); child != null; child = child.getNextSibling() ) {
			if( child.getType() == CPPTokenTypes.LESSTHAN ) {
			    return true;
			}
		    }
		}
	    }
	}
	return false;
    }
    
    public static List<CsmTemplateParameter> getTemplateParameters(AST ast, CsmTemplate template) {
        assert (ast != null && ast.getType() == CPPTokenTypes.LITERAL_template);
        List<CsmTemplateParameter> res = new ArrayList();
        for (AST child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
            switch (child.getType()) {
                case CPPTokenTypes.LITERAL_typename:
                    break;
                case CPPTokenTypes.LITERAL_class:
                    break;
                case CPPTokenTypes.ID:
                    // now create parameter
                    res.add(new TemplateParameterImpl(child.getText()));
                    break;
                case CPPTokenTypes.CSM_PARAMETER_DECLARATION:
                    // now create parameter
                    AST type = child.getFirstChild();
                    if (type != null) {
                        AST typeName = type.getFirstChild();
                        if (typeName != null) {
                            res.add(new TemplateParameterImpl(typeName.getText()));
                        } else {
                            System.err.println("not yet supported template parameter"); // NOI18N
                        }
                    } else {
                        System.err.println("not yet supported template parameter"); // NOI18N
                    }
                    break;
            }
        }
        return res;
    }
    
    public static CsmType checkTemplateType(CsmType type, CsmScope scope) {
        if (!(type instanceof TypeImpl)) {
            return type;
        }
        
        // first check scope
        if (CsmKindUtilities.isTemplate(scope)) {
            List<CsmTemplateParameter> params = ((CsmTemplate)scope).getTemplateParameters();
            if (!params.isEmpty()) {
                String classifierText = ((TypeImpl)type).getClassifierText().toString();
                for (CsmTemplateParameter param : params) {
                    if (param.getName().toString().equals(classifierText)) {
                        return new TemplateParameterTypeImpl(type, param);
                    }
                }
            }
        }
        
        // then check class or super class
        if (scope instanceof ClassImpl) {
            return checkTemplateType(type, ((ClassImpl)scope).getScope());
        } else if (scope instanceof CsmMethod) {
            return checkTemplateType(type, ((CsmMethod)scope).getContainingClass());
        }
        
        return type;
    }
}
