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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.openide.util.Parameters;

/**
 * @author Radek Matous
 */
public final class TypeConstantElementImpl extends PhpElementImpl implements TypeConstantElement {

    public static final String IDX_FIELD = PHPIndexer.FIELD_CLASS_CONST;
    private final TypeElement enclosingType;

    private TypeConstantElementImpl(
            final TypeElement enclosingType,
            final String constantName,
            final int offset,
            final String fileUrl,
            final ElementQuery elementQuery) {
        super(constantName, enclosingType.getName(), fileUrl, offset, elementQuery);
        this.enclosingType = enclosingType;
    }

    public static Set<TypeConstantElement> fromSignature(final TypeElement type,
            final IndexQueryImpl indexScopeQuery, final IndexResult indexResult) {
        return fromSignature(type, NameKind.empty(), indexScopeQuery, indexResult);
    }

    public static Set<TypeConstantElement> fromSignature(final TypeElement type, final NameKind query,
            final IndexQueryImpl indexScopeQuery, final IndexResult indexResult) {
        final String[] values = indexResult.getValues(IDX_FIELD);
        final Set<TypeConstantElement> retval = values.length > 0
                ? new HashSet<TypeConstantElement>() : Collections.<TypeConstantElement>emptySet();

        for (final String val : values) {
            final TypeConstantElement constant = fromSignature(type, query, indexScopeQuery, indexResult, Signature.get(val));
            if (constant != null) {
                retval.add(constant);
            }
        }
        return retval;
    }

    private static TypeConstantElement fromSignature(final TypeElement type, final NameKind query,
            final IndexQueryImpl indexScopeQuery, final IndexResult indexResult, final Signature signature) {
        final ConstantSignatureParser signParser = new ConstantSignatureParser(signature);
        TypeConstantElement retval = null;
        if (matchesQuery(query, signParser)) {
            retval = new TypeConstantElementImpl(type, signParser.getConstantName(),
                    signParser.getOffset(), indexResult.getUrl().toString(),
                    indexScopeQuery);
        }
        return retval;
    }

    private static boolean matchesQuery(final NameKind query, ConstantSignatureParser signParser) {
        Parameters.notNull("query", query);//NOI18N
        return (query instanceof NameKind.Empty)
                || query.matchesName(TypeConstantElement.KIND, signParser.getConstantName());
    }

    @Override
    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase()).append(SEPARATOR.SEMICOLON);//NOI18N
        sb.append(getName()).append(SEPARATOR.SEMICOLON);//NOI18N
        sb.append(getOffset()).append(SEPARATOR.SEMICOLON);//NOI18N
        checkSignature(sb);
        return sb.toString();
    }

    @Override
    public final PhpElementKind getPhpElementKind() {
        return TypeConstantElement.KIND;
    }

    @Override
    public final TypeElement getType() {
        return enclosingType;
    }

    private void checkSignature(StringBuilder sb) {
        boolean checkEnabled = false;
        assert checkEnabled = true;
        if (checkEnabled) {
            String retval = sb.toString();
            ConstantSignatureParser parser = new ConstantSignatureParser(Signature.get(retval));
            assert getName().equals(parser.getConstantName());
            assert getOffset() == parser.getOffset();
        }
    }

    @Override
    public String getValue() {
        //TODO: not implemented yet
        return null;
    }

    @Override
    public final boolean isStatic() {
        return getPhpModifiers().isStatic();
    }

    @Override
    public final boolean isPublic() {
        return getPhpModifiers().isPublic();
    }

    @Override
    public final boolean isProtected() {
        return getPhpModifiers().isProtected();
    }

    @Override
    public final boolean isPrivate() {
        return getPhpModifiers().isPrivate();
    }

    @Override
    public final boolean isFinal() {
        return getPhpModifiers().isFinal();
    }

    @Override
    public final boolean isAbstract() {
        return getPhpModifiers().isAbstract();
    }

    private static class ConstantSignatureParser {

        private final Signature signature;

        ConstantSignatureParser(Signature signature) {
            this.signature = signature;
        }

        String getConstantName() {
            return signature.string(1);
        }

        int getOffset() {
            return signature.integer(2);
        }
    }
}
