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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.elements.InterfaceElement;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.openide.util.Parameters;

/**
 * @author Radek Matous
 */
public class InterfaceElementImpl extends TypeElementImpl implements InterfaceElement {
    public static final String IDX_FIELD = PHPIndexer.FIELD_IFACE;

    private InterfaceElementImpl(
            final QualifiedName qualifiedName,
            final int offset,
            final Set<QualifiedName> ifaceNames,
            final String fileUrl,
            final ElementQuery elementQuery) {
        super(qualifiedName, offset, ifaceNames,
                PhpModifiers.noModifiers().toFlags(), fileUrl, elementQuery);
    }

    public static Set<InterfaceElement> fromSignature(IndexQueryImpl indexScopeQuery, IndexResult indexResult) {
        return fromSignature(NameKind.empty(), indexScopeQuery, indexResult);
    }

    public static Set<InterfaceElement> fromSignature(final NameKind query,
            final IndexQueryImpl indexScopeQuery, final IndexResult indexResult) {
        String[] values = indexResult.getValues(IDX_FIELD);
        Set<InterfaceElement> retval = values.length > 0 ?
            new HashSet<InterfaceElement>() : Collections.<InterfaceElement> emptySet();

        for (String val : values) {
            final InterfaceElement iface = fromSignature(query, indexScopeQuery, indexResult, Signature.get(val));
            if (iface != null) {
                retval.add(iface);
            }
        }
        return retval;
    }

    private static InterfaceElement fromSignature(final NameKind query, final IndexQueryImpl indexScopeQuery,
            final IndexResult indexResult, final Signature signature) {
        Parameters.notNull("query", query);//NOI18N
        InterfaceSignatureParser signParser = new InterfaceSignatureParser(signature);
        InterfaceElement retval = null;
        if (matchesQuery(query, signParser)) {
            retval = new InterfaceElementImpl(signParser.getQualifiedName(), signParser.getOffset(),
                    signParser.getSuperInterfaces(),
                    indexResult.getUrl().toString(), indexScopeQuery);
        }
        return retval;
    }

    private static boolean matchesQuery(final NameKind query, InterfaceSignatureParser signParser) {
        Parameters.notNull("query", query);//NOI18N
        return (query instanceof NameKind.Empty) ||
                query.matchesName(InterfaceElement.KIND, signParser.getQualifiedName());
    }

    @Override
    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase()).append(SEPARATOR.SEMICOLON);//NOI18N
        sb.append(getName()).append(SEPARATOR.SEMICOLON);//NOI18N
        sb.append(getOffset()).append(SEPARATOR.SEMICOLON);//NOI18N
        StringBuilder ifaceSb = new StringBuilder();
        for (QualifiedName ifaceName : getSuperInterfaces()) {
            if (ifaceSb.length() > 0) {
                ifaceSb.append(SEPARATOR.COMMA);//NOI18N
            }
            ifaceSb.append(ifaceName.toString());//NOI18N
        }
        sb.append(ifaceSb);
        sb.append(SEPARATOR.SEMICOLON);//NOI18N
        QualifiedName namespaceName = getNamespaceName();
        sb.append(namespaceName.toString()).append(SEPARATOR.SEMICOLON);//NOI18N
        checkInterfaceSignature(sb);
        return sb.toString();
    }

    @Override
    public final PhpElementKind getPhpElementKind() {
        return InterfaceElement.KIND;
    }

    private void checkInterfaceSignature(StringBuilder sb) {
        boolean checkEnabled = false;
        assert checkEnabled = true;
        if (checkEnabled) {
            String retval = sb.toString();
            InterfaceSignatureParser parser = new InterfaceSignatureParser(Signature.get(retval));
            assert getName().equals(parser.getQualifiedName().toName().toString());
            assert getNamespaceName().equals(parser.getQualifiedName().toNamespaceName());
            assert getOffset() == parser.getOffset();
            assert getSuperInterfaces().size() == parser.getSuperInterfaces().size();
        }
    }

    private static class InterfaceSignatureParser {

        private final Signature signature;

        InterfaceSignatureParser(Signature signature) {
            this.signature = signature;
        }

        QualifiedName getQualifiedName() {
            return composeQualifiedName(signature.string(4), signature.string(1));
        }

        public Set<QualifiedName> getSuperInterfaces() {
            Set<QualifiedName> ifaces = Collections.emptySet();
            String separatedIfaces = signature.string(3);
            if (separatedIfaces != null && separatedIfaces.length() > 0) {
                ifaces = new HashSet<QualifiedName>(); //NOI18N
                final String[] ifaceNames = separatedIfaces.split(SEPARATOR.COMMA.toString());
                for (String ifName : ifaceNames) {
                    ifaces.add(QualifiedName.create(ifName));
                }
            }
            return ifaces;
        }

        int getOffset() {
            return signature.integer(2);
        }
    }
}
