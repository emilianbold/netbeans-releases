/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.csm;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.openide.util.CharSequences;

/**
 * Template function explicit specialization declaration.
 *
 * @author Nikolay Krasilnikov (nnnnnk@netbeans.org)
 */
public class MethodImplSpecialization<T> extends MethodImpl<T> {

    protected MethodImplSpecialization(AST ast, ClassImpl cls, CsmVisibility visibility, NameHolder nameHolder, boolean global) throws AstRendererException {
        super(ast, cls, visibility, nameHolder, global);
    }

    public static<T> MethodImplSpecialization<T> create(AST ast, ClassImpl cls, CsmVisibility visibility, boolean register) throws AstRendererException {
        NameHolder nameHolder = NameHolder.createFunctionName(ast);//NameHolder.createName(getFunctionName(ast));
        MethodImplSpecialization<T> methodImpl = new MethodImplSpecialization<T>(ast, cls, visibility, nameHolder, register);
        postObjectCreateRegistration(register, methodImpl);
        nameHolder.addReference(cls.getContainingFile(), methodImpl);
        return methodImpl;
    }

    private static String getFunctionName(AST ast) {
        CharSequence funName = CharSequences.create(AstUtil.findId(ast, CPPTokenTypes.RCURLY, true));
        return getFunctionNameFromFunctionSpecialicationName(funName.toString());
    }

    private static String getFunctionNameFromFunctionSpecialicationName(CharSequence functionName) {
        CharSequence[] nameParts = Utils.splitQualifiedName(functionName.toString());
        StringBuilder className = new StringBuilder("");
        if(nameParts.length > 0) {
            className.append(nameParts[nameParts.length - 1]);
        }
        return className.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent

    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
    }

    public MethodImplSpecialization(DataInput input) throws IOException {
        super(input);
    }
}
