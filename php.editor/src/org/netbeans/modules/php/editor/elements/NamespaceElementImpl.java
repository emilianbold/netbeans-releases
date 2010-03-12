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

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.elements.NamespaceElement;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.openide.util.Parameters;

/**
 * @author Radek Matous
 */
public class NamespaceElementImpl extends FullyQualifiedElementImpl implements NamespaceElement {
    public static final String IDX_FIELD = PHPIndexer.FIELD_NAMESPACE;

    NamespaceElementImpl(
            final QualifiedName qualifiedName,
            final int offset,
            final String fileUrl,
            final ElementQuery elementQuery) {
        super(qualifiedName.toName().toString(), qualifiedName.toNamespaceName().toString(), fileUrl, offset, elementQuery);
    }

    public static Set<NamespaceElement> fromSignature(final IndexQueryImpl indexQuery, final IndexResult indexResult) {
        return fromSignature(NameKind.empty(), indexQuery, indexResult);
    }
    
    public static Set<NamespaceElement> fromSignature(final NameKind query, final IndexQueryImpl indexQuery, final IndexResult indexResult) {
        final String[] values = indexResult.getValues(IDX_FIELD);
        final Set<NamespaceElement> retval = new HashSet<NamespaceElement>();
        for (final String val : values) {
            final NamespaceElement namespace = fromSignature(query, indexQuery, indexResult, Signature.get(val));
            if (namespace != null) {
                retval.add(namespace);
            }
        }
        return retval;
    }

    public static NamespaceElement fromSignature(final NameKind query, IndexQueryImpl indexScopeQuery, IndexResult indexResult, Signature sig) {
        final NamespaceSignatureParser signParser = new NamespaceSignatureParser(sig);
        NamespaceElement retval = null;
        if (matchesQuery(query, signParser)) {
                retval = new NamespaceElementImpl(signParser.getQualifiedName(),
                0, indexResult.getUrl().toString(),
                indexScopeQuery);
        }
        return retval;
    }

    private static boolean matchesQuery(final NameKind query, NamespaceSignatureParser signParser) {
        Parameters.notNull("NameKind query: can't be null", query);
        final QualifiedName qualifiedName = signParser.getQualifiedName();
        return (query instanceof NameKind.Empty) || 
                (!qualifiedName.isDefaultNamespace() && query.matchesName(NamespaceElement.KIND, qualifiedName));
    }

    @Override
    public String getSignature() {
        final StringBuilder sb = new StringBuilder();
        final QualifiedName qualifiedName = getFullyQualifiedName();
        final String name = qualifiedName.toName().toString();
        final String namespaceName = qualifiedName.toNamespaceName().toString();
        sb.append(name.toLowerCase()).append(SEPARATOR.SEMICOLON);//NOI18N
        sb.append(name).append(SEPARATOR.SEMICOLON);//NOI18N
        sb.append(namespaceName).append(SEPARATOR.SEMICOLON);//NOI18N
        checkSignature(sb);
        return sb.toString();
    }

    @Override
    public final PhpElementKind getPhpElementKind() {
        return NamespaceElement.KIND;
    }

    private void checkSignature(StringBuilder sb) {
        boolean checkEnabled = false;
        assert checkEnabled = true;
        if (checkEnabled) {
            String retval = sb.toString();
            NamespaceSignatureParser parser = new NamespaceSignatureParser(Signature.get(retval));
            assert getName().equals(parser.getQualifiedName().toName().toString());
            assert getNamespaceName().equals(parser.getQualifiedName().toNamespaceName());
        }
    }

    private static class NamespaceSignatureParser {

        private final Signature signature;

        NamespaceSignatureParser(Signature signature) {
            this.signature = signature;
        }

        QualifiedName getQualifiedName() {
            return composeQualifiedName(signature.string(2), signature.string(1));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NamespaceElementImpl other = (NamespaceElementImpl) obj;
        if ((this.getName() == null) ? (other.getName() != null) : !this.getName().equals(other.getName())) {
            return false;
        }
        String thisNamespaceName = this.getNamespaceName().toString();
        String otherNamespaceName = other.getNamespaceName().toString();
        if ((thisNamespaceName == null) ? (otherNamespaceName != null) : !thisNamespaceName.equals(otherNamespaceName)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        String thisNamespaceName = this.getNamespaceName().toString();
        hash = 71 * hash + (this.getName() != null ? this.getName().hashCode() : 0);
        hash = 71 * hash + (thisNamespaceName != null ? thisNamespaceName.hashCode() : 0);
        return hash;
    }


    
}
