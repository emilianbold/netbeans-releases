/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.csm;

import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunctionParameterList;
import org.netbeans.modules.cnd.api.model.CsmKnRName;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmParameterList;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.support.AbstractObjectFactory;

/**
 *
 * @author Vladimir Voskresensky
 */
public class FunctionParameterListImpl extends ParameterListImpl<CsmFunctionParameterList, CsmParameter> implements CsmFunctionParameterList {
    private final CsmUID<ParameterListImpl<CsmParameterList, CsmKnRName>> krListUID;

    private FunctionParameterListImpl(CsmFile file, int start, int end, Collection<CsmUID<CsmParameter>> parameters, ParameterListImpl<CsmParameterList, CsmKnRName> krList) {
        super(file, start, end, parameters);
        @SuppressWarnings("unchecked")
        CsmUID<ParameterListImpl<CsmParameterList, CsmKnRName>> uID = krList == null ? null : (CsmUID<ParameterListImpl<CsmParameterList, CsmKnRName>>)(CsmUID)krList.getUID();
        this.krListUID = krList == null ? null : uID;
    }

    public CsmParameterList<CsmParameterList, CsmKnRName> getKernighanAndRitchieParameterList() {
        return UIDCsmConverter.UIDtoCsmObject(krListUID);
    }

    /*package*/ static FunctionParameterListImpl create(CsmFile file, AST funAST, CsmScope scope) {
        AST lParen = funAST;
        AST paramList = funAST;
        return create(file, lParen, paramList, scope);
    }

    /*package*/ static FunctionParameterListImpl create(CsmFile file, AST lParen, AST ast, CsmScope scope) {
        List<CsmParameter> parameters = new ArrayList<CsmParameter>();
        if (ast != null && (ast.getType() == CPPTokenTypes.CSM_PARMLIST ||
                ast.getType() == CPPTokenTypes.CSM_KR_PARMLIST)) {
            for (AST token = ast.getFirstChild(); token != null; token = token.getNextSibling()) {
                if (token.getType() == CPPTokenTypes.CSM_PARAMETER_DECLARATION) {
                    List<ParameterImpl> params = AstRenderer.renderParameter(token, file, scope);
                    if (params != null) {
                        parameters.addAll(params);
                    }
                }
            }
        }
        return null;
    }

    /*package*/ static FunctionParameterListImpl create(CsmFunctionParameterList paramList, Collection<CsmParameter> parameters) {
        //TODO
        return null;
//        return new ParameterListImpl(paramList.getContainingFile(), paramList.getStartOffset(), paramList.getEndOffset(), parameters, null);
    }

    ////////////////////////////////////////////////////////////////////////////
    // persistent
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        UIDObjectFactory.getDefaultFactory().writeUID(this.krListUID, output);
    }

    @SuppressWarnings("unchecked")
    public FunctionParameterListImpl(DataInput input) throws IOException {
        super(input);
        this.krListUID = UIDObjectFactory.getDefaultFactory().readUID(input);
    }

    private static List<CsmKnRName> readKRList(ArrayList<CsmKnRName> arrayList, DataInput input) throws IOException {
        int collSize = input.readInt();
        if (collSize == AbstractObjectFactory.NULL_POINTER) {
            return null;
        }
        List<CsmKnRName> res = new ArrayList<CsmKnRName>();
        assert collSize >= 0;
        for (int i = 0; i < collSize; ++i) {
            CsmKnRName param = readKRName();
            assert param != null;
            res.add(param);
        }
        return res;
    }

    private static CsmKnRName readKRName() throws IOException {
        return null;
    }

    private static void writeKRName(CsmKnRName name, DataOutput output) throws IOException {
    }

    private void writeKRList(List<CsmKnRName> krList, DataOutput output) throws IOException {
        if (krList == null) {
            output.writeInt(AbstractObjectFactory.NULL_POINTER);
        } else {
            int len = krList.size();
            output.writeInt(len);
            for (int i = 0; i < len; i++) {
                assert krList.get(i) != null;
                writeKRName(krList.get(i), output);
            }
        }
    }
}
