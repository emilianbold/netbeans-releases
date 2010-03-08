/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.elements;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.elements.FunctionElement;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.openide.util.Parameters;

/**
 * @author Radek Matous
 */
public final class FunctionElementImpl extends FullyQualifiedElementImpl implements FunctionElement {
    public static final String IDX_FIELD = PHPIndexer.FIELD_BASE;
    private final BaseFunctionElementSupport functionSupport;

    private FunctionElementImpl(
            final QualifiedName qualifiedName,
            final int offset,
            final String fileUrl,
            final ElementQuery elementQuery,
            final List<ParameterElement> parameters,
            final Set<TypeResolver> returnTypes) {
        super(qualifiedName.toName().toString(), qualifiedName.toNamespaceName().toString(),
                fileUrl, offset, elementQuery);
        this.functionSupport = new BaseFunctionElementSupport(parameters, returnTypes);
    }

    public static Set<FunctionElement> fromSignature(final IndexQueryImpl indexQuery, final IndexResult indexResult) {
        return fromSignature(NameKind.empty(), indexQuery, indexResult);
    }
    public static Set<FunctionElement> fromSignature(
            final NameKind query, final IndexQueryImpl indexQuery, final IndexResult indexResult) {
        String[] values = indexResult.getValues(IDX_FIELD);
        Set<FunctionElement> retval = values.length > 0 ?
            new HashSet<FunctionElement>() : Collections.<FunctionElement> emptySet();

        for (String val : values) {
            final FunctionElement fnc = fromSignature(query, indexQuery, indexResult, Signature.get(val));
            if (fnc != null) {
                retval.add(fnc);
            }
        }
        return retval;
    }

    public static FunctionElement fromSignature(final NameKind query, IndexQueryImpl indexScopeQuery, IndexResult indexResult, Signature sig) {
        Parameters.notNull("NameKind query: can't be null", query);
        FunctionSignatureParser signParser = new FunctionSignatureParser(sig);
        FunctionElement retval = null;
        if (matchesQuery(query, signParser)) {
            retval = new FunctionElementImpl(signParser.getQualifiedName(),
                    signParser.getOffset(), indexResult.getUrl().toString(),
                    indexScopeQuery, signParser.getParameters(), signParser.getReturnTypes());
        }
        return retval;
    }

    private static boolean matchesQuery(final NameKind query, FunctionSignatureParser signParser) {
        Parameters.notNull("NameKind query: can't be null", query);
        return (query instanceof NameKind.Empty) ||
                query.matchesName(FunctionElement.KIND, signParser.getQualifiedName());
    }


    @Override
    public PhpElementKind getPhpElementKind() {
        return FunctionElement.KIND;
    }

    @Override
    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase()).append(SEPARATOR.SEMICOLON);//NOI18N
        sb.append(getName()).append(SEPARATOR.SEMICOLON);//NOI18N
        sb.append(getSignatureLastPart());

        checkFunctionSignature(sb);
        return sb.toString();
    }

    private String getSignatureLastPart() {
        StringBuilder sb = new StringBuilder();
        sb.append(getOffset()).append(SEPARATOR.SEMICOLON); //NOI18N
        List<ParameterElement> parameterList = getParameters();
        for (int idx = 0; idx < parameterList.size(); idx++) {
            ParameterElementImpl parameter = (ParameterElementImpl) parameterList.get(idx);
            if (idx > 0) {
                sb.append(SEPARATOR.COMMA); //NOI18N
            }
            sb.append(parameter.getSignature());
        }
        sb.append(SEPARATOR.SEMICOLON); //NOI18N
        for (TypeResolver typeResolver : getReturnTypes()) {
            TypeResolverImpl resolverImpl = (TypeResolverImpl) typeResolver;
            sb.append(resolverImpl.getSignature());
        }
        sb.append(SEPARATOR.SEMICOLON); //NOI18N
        sb.append(getPhpModifiers().toFlags()).append(SEPARATOR.SEMICOLON);
        return sb.toString();
    }

    private void checkFunctionSignature(StringBuilder sb) {
        boolean checkEnabled = false;
        assert checkEnabled = true;
        if (checkEnabled) {
            String retval = sb.toString();
            FunctionSignatureParser parser = new FunctionSignatureParser(Signature.get(retval));
            assert getName().equals(parser.getQualifiedName().toName().toString());
            assert getNamespaceName().equals(parser.getQualifiedName().toNamespaceName());
            assert getOffset() == parser.getOffset();
            assert getParameters().size() == parser.getParameters().size();
            assert getReturnTypes().size() == parser.getReturnTypes().size();
        }
    }

    @Override
    public List<ParameterElement> getParameters() {
        return this.functionSupport.getParameters();
    }

    @Override
    public Collection<TypeResolver> getReturnTypes() {
        return this.functionSupport.getReturnTypes();
    }

    @Override
    public String asString(PrintAs as) {
        return this.functionSupport.asString(as, this);
    }

    private static class FunctionSignatureParser {
        private final Signature signature;

        FunctionSignatureParser(Signature signature) {
            this.signature = signature;
        }

        QualifiedName getQualifiedName() {
            return composeQualifiedName(signature.string(5), signature.string(1));
        }

        int getOffset() {
            return signature.integer(2);
        }

        List<ParameterElement> getParameters() {
            return ParameterElementImpl.parseParameters(signature.string(3));
        }


        Set<TypeResolver> getReturnTypes() {
            return TypeResolverImpl.parseTypes(signature.string(4));
        }
    }
}
