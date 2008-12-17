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

package org.netbeans.modules.php.editor;

import java.util.List;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.model.ModelFactory;
import org.netbeans.modules.php.editor.model.ModelScope;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.VariableName;

/**
 * @author Radek Matous
 */
public final class VarTypeResolver {
    String type;
    private VarTypeResolver(final PHPCompletionItem.CompletionRequest request,
            final String varName) {
        this(varName,request.anchor,request.index, request.info, request.result);
    }

    private VarTypeResolver(final String varName,int anchor,PHPIndex index,CompilationInfo info,PHPParseResult result) {
        ModelScope modelScope = ModelFactory.getModel(info).getModelScope();
        List<? extends VariableName> variables = modelScope.getVariables(varName);
        VariableName var = ModelUtils.getFirst(variables);
        if (var != null) {
            TypeScope typeScope = ModelUtils.getFirst(var.getTypes(anchor));
            if (typeScope != null) {
                type = typeScope.getName();
            }
        }

    }

    private VarTypeResolver(final CompilationInfo info, final int offset, final String varName)  {
        this(varName,offset,PHPIndex.get(info.getIndex(PHPLanguage.PHP_MIME_TYPE)), info,
                (PHPParseResult)info.getEmbeddedResult(PHPLanguage.PHP_MIME_TYPE, offset));
    }
    public static VarTypeResolver getInstance(final PHPCompletionItem.CompletionRequest request,
            final String varName)  {

        return new VarTypeResolver(request, varName);
    }


    public String resolveType() {
        return type;
    }
}
