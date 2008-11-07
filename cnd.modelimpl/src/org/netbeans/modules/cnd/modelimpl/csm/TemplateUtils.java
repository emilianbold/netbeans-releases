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
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
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

    public static String getSpecializationSuffix(AST qIdToken, List<CsmTemplateParameter> parameters) {
	StringBuilder sb  = new StringBuilder();
	for( AST child = qIdToken.getFirstChild(); child != null; child = child.getNextSibling() ) {
	    if( child.getType() == CPPTokenTypes.LESSTHAN ) {
		addSpecializationSuffix(child, sb, parameters);
		break;
	    }
	}
	return sb.toString();
    }
    
    // in class our parser skips LESSTHAN symbols in templates...
    public static String getClassSpecializationSuffix(AST qIdToken, List<CsmTemplateParameter> parameters) {
	StringBuilder sb  = new StringBuilder();
        addSpecializationSuffix(qIdToken.getFirstChild(), sb, parameters);
	return sb.toString();
    }
    
    public static final String TYPENAME_STRING = "class"; //NOI18N
    
    public static void addSpecializationSuffix(AST firstChild, StringBuilder sb, List<CsmTemplateParameter> parameters) {
        int depth = 0;
        for (AST child = firstChild; child != null; child = child.getNextSibling()) {
            if (child.getType() == CPPTokenTypes.LESSTHAN) {
                depth++;
            }
            if (CPPTokenTypes.CSM_START <= child.getType() && child.getType() <= CPPTokenTypes.CSM_END) {
                AST grandChild = child.getFirstChild();
                if (grandChild != null) {
                    addSpecializationSuffix(grandChild, sb, parameters);
                }
            } else {
                String text = child.getText();
                if (parameters != null) {
                    for (CsmTemplateParameter param : parameters) {
                        if (param.getName().toString().equals(text)) {
                            text = TYPENAME_STRING;
                        }
                    }
                }
                assert text != null;
                assert text.length() > 0;
                if (sb.length() > 0) {
                    if (Character.isJavaIdentifierPart(sb.charAt(sb.length() - 1))) {
                        if (Character.isJavaIdentifierPart(text.charAt(0))) {
                            sb.append(' ');
                        }
                    }
                }
                sb.append(text);

                if (child.getType() == CPPTokenTypes.GREATERTHAN) {
                    depth--;
                    if (depth == 0) {
                        break;
                    }
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

    public static AST getTemplateStart(AST ast) {
        for (AST child = ast; child != null; child = child.getNextSibling()) {
            if (child.getType() == CPPTokenTypes.LITERAL_template) {
                return child;
            }
        }
        return null;
    }
    
    public static List<CsmTemplateParameter> getTemplateParameters(AST ast, CsmFile file, CsmScope scope) {
        assert (ast != null && ast.getType() == CPPTokenTypes.LITERAL_template);
        List<CsmTemplateParameter> res = new ArrayList<CsmTemplateParameter>();
        AST parameterStart = null;
        for (AST child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
            switch (child.getType()) {
                case CPPTokenTypes.LITERAL_class:
                case CPPTokenTypes.LITERAL_typename:
                    parameterStart = child;
                    break;
                case CPPTokenTypes.ID:
                    // now create parameter
                    AST fakeAST = null;
                    if (parameterStart == null) {
                        fakeAST = parameterStart = child;
                    } else {
                        // Fix for IZ#138099: unresolved identifier for functions' template parameter.
                        // The fakeAST is needed to initialize TemplateParameter with correct offsets.
                        // Without it TemplateParameter would span either "class"/"typename" keyword
                        // or parameter name, but not both.
                        fakeAST = AstUtil.createAST(parameterStart, child);
                    }
                    if (child.getNextSibling() != null) {
                        AST assign = child.getNextSibling();
                        if (assign.getType() == CPPTokenTypes.ASSIGNEQUAL) {
                            if (assign.getNextSibling() != null) {
                                AST type = assign.getNextSibling();
                                if (type.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND
                                        || type.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN) {
                                    res.add(new TemplateParameterImpl(fakeAST, child.getText(), file, scope, type));
                                    parameterStart = null;
                                    break;
                                }
                            }
                        }
                    }
                    res.add(new TemplateParameterImpl(fakeAST, child.getText(), file, scope));                    
                    parameterStart = null;
                    break;
                case CPPTokenTypes.CSM_PARAMETER_DECLARATION:
                    // now create parameter
                    parameterStart = child;
                    AST varDecl = child.getFirstChild();
                    if (varDecl != null && varDecl.getType() == CPPTokenTypes.LITERAL_enum) {
                        varDecl = varDecl.getNextSibling();
                        if(varDecl == null || varDecl.getType() != CPPTokenTypes.CSM_TYPE_COMPOUND) {
                            break;                                    
                        }                                
                    }
                    // check for existense of CSM_VARIABLE_DECLARATION branch
                    if (varDecl != null && varDecl.getNextSibling() != null &&
                            varDecl.getNextSibling().getType() == CPPTokenTypes.CSM_VARIABLE_DECLARATION) {
                        // CSM_VARIABLE_DECLARATION branch has priority
                        varDecl = varDecl.getNextSibling();
                    }
                    if (varDecl != null) {
                        switch (varDecl.getType()) {
                            case CPPTokenTypes.CSM_VARIABLE_DECLARATION:
                                AST pn = varDecl.getFirstChild();
                                if (pn != null) {
                                    res.add(new TemplateParameterImpl(parameterStart, pn.getText(), file, scope));
                                }
                                break;
                            case CPPTokenTypes.CSM_TYPE_BUILTIN:
                            case CPPTokenTypes.CSM_TYPE_COMPOUND:
                                for(AST p = varDecl.getFirstChild(); p != null; p = p.getNextSibling()){
                                    if (p.getType() == CPPTokenTypes.ID) {
                                       res.add(new TemplateParameterImpl(parameterStart, p.getText(), file, scope));
                                       break;
                                    }
                                }
                                break;
                        }
                    }
                    break;
                case CPPTokenTypes.CSM_TEMPLATE_TEMPLATE_PARAMETER:
                    parameterStart = child;
                    for (AST paramChild = child.getFirstChild(); paramChild != null; paramChild = paramChild.getNextSibling()) {
                        if (paramChild.getType() == CPPTokenTypes.ID) {
                            // IZ 141842 : If template parameter declared as a template class, its usage is unresolved
                            // Now all IDs of template template parameter are added to template parameters of template.
                            // When CsmClassifierBasedTemplateParameter will be finished, this should be replaced. 
                            res.add(new TemplateParameterImpl(parameterStart, paramChild.getText(), file, scope));
                        }
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

        if (type instanceof NestedType) {
            NestedType nestedType = (NestedType) type;
            type = new NestedType(checkTemplateType(nestedType.getParent(), scope), nestedType);
        }
        
        // Check instantiation parameters
        if (type.isInstantiation()) {
            TypeImpl typeImpl = (TypeImpl) type;
            List<CsmType> params = typeImpl.getInstantiationParams();
            for (CsmType instParam : params) {
                CsmType newType = checkTemplateType(instParam, scope);
                if (newType != instParam) {
                    params.set(params.indexOf(instParam), newType);
                }
            }
        }
        
        // first check scope and super classes if needed
        while (scope != null) {
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
            if (scope instanceof CsmScopeElement) {
                scope = ((CsmScopeElement)scope).getScope();
            } else {
                break;
            }
        }
        
        return type;
    }

    private TemplateUtils() {
    }
}
