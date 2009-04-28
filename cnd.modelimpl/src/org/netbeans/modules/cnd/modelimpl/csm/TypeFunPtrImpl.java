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

import antlr.collections.AST;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.impl.services.InstantiationProviderImpl;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 * Represent pointer to function type
 * @author Vladimir Kvashin
 */
public class TypeFunPtrImpl extends TypeImpl implements CsmFunctionPointerType {

    private Collection<CsmUID<CsmParameter>> functionParameters;
    private short functionPointerDepth;

    TypeFunPtrImpl(CsmFile file, int pointerDepth, boolean reference, int arrayDepth, boolean _const, int startOffset, int endOffset) {
        super(file, pointerDepth, reference, arrayDepth, _const, startOffset, endOffset);
    }

    // package-local - for facory only
    TypeFunPtrImpl(TypeFunPtrImpl type, int pointerDepth, boolean reference, int arrayDepth, boolean _const) {
        super(type, pointerDepth, reference, arrayDepth, _const);
        if(type.functionParameters != null) {
            functionParameters = new ArrayList<CsmUID<CsmParameter>>(type.functionParameters);
        }
        functionPointerDepth = type.functionPointerDepth;
    }

    // package-local - for facory only
    TypeFunPtrImpl(TypeFunPtrImpl type, List<CsmSpecializationParameter> instantiationParams) {
        super(type, instantiationParams);
        if(type.functionParameters != null) {
            functionParameters = new ArrayList<CsmUID<CsmParameter>>(type.functionParameters);
        }
        functionPointerDepth = type.functionPointerDepth;
    }
    
    void init(AST ast, boolean inFunctionParameters) {
        initFunctionPointerParamList(ast, this, inFunctionParameters);
    }

    public Collection<CsmParameter> getParameters() {
        if (functionParameters == null) {
            return Collections.<CsmParameter>emptyList();
        } else {
            return UIDCsmConverter.UIDsToDeclarations(functionParameters);
        }
    }

    @Override
    public StringBuilder decorateText(CharSequence classifierText, CsmType decorator, boolean canonical, CharSequence variableNameToInsert) {
        StringBuilder sb = new StringBuilder();
        if (decorator.isConst()) {
            sb.append("const "); // NOI18N
        }
        sb.append(classifierText);
        for (int i = 0; i < decorator.getPointerDepth(); i++) {
            sb.append('*');
        }
        if (decorator.isReference()) {
            sb.append('&');
        }
        for (int i = 0; i < decorator.getArrayDepth(); i++) {
            sb.append(canonical ? "*" : "[]"); // NOI18N
        }

        sb.append('(');
        for (int i = 0; i < functionPointerDepth; i++) {
            sb.append('*');
            if (variableNameToInsert != null) {
                sb.append(variableNameToInsert);
            }
        }
        sb.append(')');
        InstantiationProviderImpl.appendParametersSignature(getParameters(), sb);
        return sb;
    }

    @Override
    public boolean isPointer() {
        // function pointer is always pointer
        return true;
    }

    public static boolean isFunctionPointerParamList(AST ast, boolean inFunctionParameters) {
        return initFunctionPointerParamList(ast, null, inFunctionParameters);
    }

    private static boolean initFunctionPointerParamList(AST ast, TypeFunPtrImpl instance, boolean inFunctionParams) {
        AST next = null;
        // find opening brace
        AST brace = AstUtil.findSiblingOfType(ast, CPPTokenTypes.LPAREN);
        if (brace != null) {
            // check whether it's followed by asterisk
            next = brace.getNextSibling();
            if (next == null) {
                return false;
            }
            if (inFunctionParams && next.getType() == CPPTokenTypes.CSM_PARMLIST) {
                // this is start of function params
                next = AstUtil.findSiblingOfType(ast, CPPTokenTypes.CSM_QUALIFIED_ID);
            } else {
                if (next.getType() != CPPTokenTypes.CSM_PTR_OPERATOR) {
                    return false;
                }
                // skip adjacent asterisks
                do {
                    next = next.getNextSibling();
                    if (instance != null) {
                        ++instance.functionPointerDepth;
                    }
                } while (next != null && next.getType() == CPPTokenTypes.CSM_PTR_OPERATOR);
            }
        }

        if (inFunctionParams && next == null) {
            next = AstUtil.findSiblingOfType(ast, CPPTokenTypes.CSM_QUALIFIED_ID);
        }

        if (next == null) {
            return false;
        }

        // check that it's followed by exprected token
        if (next.getType() == CPPTokenTypes.CSM_VARIABLE_DECLARATION ||
                next.getType() == CPPTokenTypes.CSM_ARRAY_DECLARATION) {
            // fine. this could be variable of function type
        } else if (next.getType() == CPPTokenTypes.CSM_QUALIFIED_ID) {
            AST lookahead = next.getNextSibling();
            AST lookahead2 = lookahead == null ? null : lookahead.getNextSibling();
            if (lookahead != null && lookahead.getType() == CPPTokenTypes.RPAREN) {
                // OK. This could be function type in typedef - in this case we get
                // CSM_QUALIFIED_ID instead of CSM_VARIABLE_DECLARATION.
            } else if (inFunctionParams && lookahead != null && lookahead.getType() == CPPTokenTypes.LPAREN
                    && lookahead2 != null && lookahead2.getType() == CPPTokenTypes.CSM_PARMLIST) {
                // OK. This could be function as a parameter
                next = lookahead;
            } else {
                next = lookahead;
                // check function returns function
                // skip LPAREN (let's not assume it's obligatory)
                if (next == null || next.getType() != CPPTokenTypes.LPAREN) {
                    return false;
                }
                next = next.getNextSibling();
                if (next == null) {
                    return false;
                }
                // skip params of fun itself
                if (next.getType() == CPPTokenTypes.CSM_PARMLIST) {
                    next = next.getNextSibling();
                    if (next == null) {
                        return false;
                    }
                }
                // params of fun are closed with RPAREN
                if (next.getType() != CPPTokenTypes.RPAREN) {
                    return false;
                }
            }
        }
        // last step: verify that it's followed with a closing brace
        next = next.getNextSibling();
        if (next != null && next.getType() == CPPTokenTypes.RPAREN) {
            next = next.getNextSibling();
            // skip LPAREN (let's not assume it's obligatory)
            if (next != null && next.getType() == CPPTokenTypes.LPAREN) {
                next = next.getNextSibling();
            }
            if (next == null) {
                return false;
            }
            if (next.getType() == CPPTokenTypes.CSM_PARMLIST) {
                if (instance != null) {
                    instance.functionParameters = RepositoryUtils.put(
                            AstRenderer.renderParameters(next, instance.getContainingFile(), null, false));
                }
                return true;
            } else if (next.getType() == CPPTokenTypes.RPAREN) {
                return true;
            } else {
                return false;
            }
        } else if (inFunctionParams && next != null && next.getType() == CPPTokenTypes.CSM_PARMLIST) {
            if (instance != null) {
                instance.functionParameters = RepositoryUtils.put(
                        AstRenderer.renderParameters(next, instance.getContainingFile(), null, false));
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (functionParameters != null) {
            RepositoryUtils.remove(functionParameters);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        output.writeShort(functionPointerDepth);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        factory.writeUIDCollection(functionParameters, output, false);
    }

    public TypeFunPtrImpl(DataInput input) throws IOException {
        super(input);
        functionPointerDepth = input.readShort();
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        functionParameters = factory.readUIDCollection(new ArrayList<CsmUID<CsmParameter>>(), input);
    }
}
