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

package org.netbeans.modules.cnd.completion.csm;

import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmInitializerListContainer;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.completion.impl.xref.FileReferencesContext;

/**
 * resolve file objects under offset
 * @author vv159170
 */
public class CsmOffsetResolver {
    private CsmFile file;
    
    /** Creates a new instance of CsmOffsetResolver */
    public CsmOffsetResolver () {
    }

    public CsmOffsetResolver (CsmFile file) {
        this.file = file;
    }
    
    public CsmFile getFile () {
        return file;
    }

    public void setFile (CsmFile file) {
        this.file = file;
    }

    // =================== resolve object under offset ============================
    
    public CsmObject findObject(int offset) {
        return findObject(file, offset);
    }
    
    // ==================== help methods =======================================

    public static CsmObject findObject(CsmFile file, int offset) {
        assert (file != null) : "can't be null file in findObject";
        // not interested in context, only object under offset
        CsmObject last = findObjectWithContext(file, offset, null, null);
        return last;
    }

    public static CsmObject findObject(CsmFile file, int offset, FileReferencesContext fileReferncesContext) {
        assert (file != null) : "can't be null file in findObject";
        // not interested in context, only object under offset
        CsmObject last = findObjectWithContext(file, offset, null, fileReferncesContext);
        return last;
    }
    
    private static CsmObject findObjectWithContext(CsmFile file, int offset, CsmContext context, FileReferencesContext fileReferncesContext) {
        assert (file != null) : "can't be null file in findObject";
        CsmObject last = null;
        if (context == null) {
            // create dummy context
            context = new CsmContext(offset);
        }
        CsmObject lastObj = CsmDeclarationResolver.findInnerFileObject(file, offset, context, fileReferncesContext);
        last = lastObj;
        // for functions search deeper
        if (CsmKindUtilities.isFunction(lastObj)) {
            CsmFunction fun = (CsmFunction)lastObj;
            // check if offset in return value
            CsmType retType = fun.getReturnType();
            if (CsmOffsetUtilities.isInObject(retType, offset)) {
                context.setLastObject(retType);
                return retType;
            }
            // check template parameters
            if (CsmKindUtilities.isTemplate(fun)) {
                Collection<CsmTemplateParameter> templateParams = ((CsmTemplate)fun).getTemplateParameters();
                CsmTemplateParameter templateParam = CsmOffsetUtilities.findObject(templateParams, context, offset);
                if (templateParam != null) {
                    context.setLastObject(templateParam);
                    return templateParam;                   
                }
            }
            // check if offset in parameters
            Collection<CsmParameter> params = fun.getParameters();
            CsmParameter param = CsmOffsetUtilities.findObject(params, context, offset);
            if (param != null) {
                CsmType type = param.getType();
                if (CsmOffsetUtilities.isInObject(type, offset)) {
                    context.setLastObject(type);
                    return type;
                }
                return param;
            }   
            // check for constructor initializers
            if (CsmKindUtilities.isConstructor(lastObj)) {
                CsmInitializerListContainer ctor = (CsmInitializerListContainer)lastObj;
                for (CsmExpression izer : ctor.getInitializerList()) {
                    if (CsmOffsetUtilities.isInObject(izer, offset)) {
                        context.setLastObject(izer);
                        return izer;
                    }
                }
            }
            // for function definition search deeper in body's statements
            if (CsmKindUtilities.isFunctionDefinition(lastObj)) {
                CsmFunctionDefinition funDef = (CsmFunctionDefinition)lastObj;
                if (CsmOffsetUtilities.isInObject(funDef.getBody(), offset)) {
                    last = null;
                    // offset is in body, try to find inners statement
                    if (CsmStatementResolver.findInnerObject(funDef.getBody(), offset, context)) {
                        // if found exact object => return it, otherwise return last found scope
                        lastObj = last = context.getLastObject();
                    }
                }
            }
        } 
        
        if (CsmKindUtilities.isClass(lastObj)) {
            // check if in inheritance part
            CsmClass clazz = (CsmClass)lastObj;
            Collection<CsmInheritance> inherits = clazz.getBaseClasses();
            CsmInheritance inh = CsmOffsetUtilities.findObject(inherits, context, offset);
            if (inh != null) {
                context.setLastObject(inh);
                last = inh;
            }             
        } else if (CsmKindUtilities.isVariable(lastObj)) {
            CsmType type = ((CsmVariable)lastObj).getType();
            if (CsmOffsetUtilities.isInObject(type, offset)) {
                context.setLastObject(type);
                last = type;
            }            
        } else if (CsmKindUtilities.isClassForwardDeclaration(lastObj)) {
            // check template parameters
            if (CsmKindUtilities.isTemplate(lastObj)) {
                Collection<CsmTemplateParameter> templateParams = ((CsmTemplate)lastObj).getTemplateParameters();
                CsmTemplateParameter templateParam = CsmOffsetUtilities.findObject(templateParams, context, offset);
                if (templateParam != null) {
                    context.setLastObject(templateParam);
                    return templateParam;
                }
            }
        }
        return last;
    }    
    
    public static CsmContext findContext(CsmFile file, int offset, FileReferencesContext fileReferncesContext) {
        CsmContext context = new CsmContext(offset);
        findObjectWithContext(file, offset, context, fileReferncesContext);
        return context;
    }    
}
