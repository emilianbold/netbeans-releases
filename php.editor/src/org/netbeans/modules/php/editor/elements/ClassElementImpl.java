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
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.openide.util.Parameters;

/**
 * @author Radek Matous
 */
public final class ClassElementImpl extends TypeElementImpl implements ClassElement {
    public static final String IDX_FIELD = PHPIndexer.FIELD_CLASS;

    private final QualifiedName superClass;

    private ClassElementImpl(
            final QualifiedName qualifiedName,
            final int offset,
            final QualifiedName superClsName,
            final Set<QualifiedName> ifaceNames,
            final int flags,
            final String fileUrl,
            final ElementQuery elementQuery) {
        super(qualifiedName, offset, ifaceNames, flags, fileUrl, elementQuery);
        this.superClass = superClsName;
    }

    public static Set<ClassElement> fromSignature(final IndexQueryImpl indexScopeQuery, final IndexResult indexResult) {
        return fromSignature(NameKind.empty(), indexScopeQuery, indexResult);
    }

    public static Set<ClassElement> fromSignature(final NameKind query,
            final IndexQueryImpl indexScopeQuery, final IndexResult indexResult) {
        String[] values = indexResult.getValues(IDX_FIELD);
        Set<ClassElement> retval = values.length > 0 ?
            new HashSet<ClassElement>() : Collections.<ClassElement> emptySet();

        for (String val : values) {
            final ClassElement clz = fromSignature(query, indexScopeQuery, indexResult, Signature.get(val));
            if (clz != null) {
                retval.add(clz);
            }
        }
        return retval;
    }

    private static ClassElement fromSignature(final NameKind query,
            final IndexQueryImpl indexScopeQuery, final IndexResult indexResult, final Signature clsSignature) {
        Parameters.notNull("query", query);
        ClassSignatureParser signParser = new ClassSignatureParser(clsSignature);
        ClassElement retval = null;
        if (matchesQuery(query, signParser)) {
            retval = new ClassElementImpl(signParser.getQualifiedName(), signParser.getOffset(),
                    signParser.getSuperClassName(), signParser.getSuperInterfaces(), signParser.getFlags(),
                    indexResult.getUrl().toString(), indexScopeQuery);
        }
        return retval;
    }

    private static boolean matchesQuery(final NameKind query, ClassSignatureParser signParser) {
        Parameters.notNull("query", query);
        return (query instanceof NameKind.Empty) ||
                query.matchesName(ClassElement.KIND, signParser.getQualifiedName());
    }

    @Override
    public final PhpElementKind getPhpElementKind() {
        return KIND;
    }

    @Override
    public final QualifiedName getSuperClassName() {
        return superClass;
    }

    @Override
    public String getSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase()).append(SEPARATOR.SEMICOLON);//NOI18N
        sb.append(getName()).append(SEPARATOR.SEMICOLON);//NOI18N
        sb.append(getOffset()).append(SEPARATOR.SEMICOLON);//NOI18N
        QualifiedName superClassName = getSuperClassName();
        if (superClassName != null) {
            sb.append(superClassName.toString());
        }
        sb.append(SEPARATOR.SEMICOLON);//NOI18N
        QualifiedName namespaceName = getNamespaceName();
        sb.append(namespaceName.toString()).append(SEPARATOR.SEMICOLON);//NOI18N
        StringBuilder ifaceSb = new StringBuilder();
        for (QualifiedName ifaceName : getSuperInterfaces()) {
            if (ifaceSb.length() > 0) {
                ifaceSb.append(SEPARATOR.COMMA);//NOI18N
            }
            ifaceSb.append(ifaceName.toString());//NOI18N
        }
        sb.append(ifaceSb);
        sb.append(SEPARATOR.SEMICOLON);//NOI18N
        sb.append(getPhpModifiers().toFlags()).append(SEPARATOR.SEMICOLON);

        checkClassSignature(sb);
        return sb.toString();
    }

    private void checkClassSignature(StringBuilder sb) {
        boolean checkEnabled = false;
        assert checkEnabled = true;
        if (checkEnabled) {
            String retval = sb.toString();
            ClassSignatureParser parser = new ClassSignatureParser(Signature.get(retval));
            assert getName().equals(parser.getQualifiedName().toName().toString());
            assert getNamespaceName().equals(parser.getQualifiedName().toNamespaceName());
            assert getOffset() == parser.getOffset();
            assert getPhpModifiers().toFlags() == parser.getFlags();
            QualifiedName superClassName = getSuperClassName();
            if (superClassName != null) {
                assert superClassName.equals(parser.getSuperClassName());
            }
            assert getSuperInterfaces().size() == parser.getSuperInterfaces().size();
        }
    }

    @Override
    public boolean isFinal() {
        return getPhpModifiers().isFinal();
    }

    @Override
    public boolean isAbstract() {
        return getPhpModifiers().isAbstract();
    }

    private static class ClassSignatureParser {

        private final Signature signature;

        ClassSignatureParser(Signature signature) {
            this.signature = signature;
        }

        QualifiedName getQualifiedName() {
            return composeQualifiedName(signature.string(4), signature.string(1));
        }

        @CheckForNull
        QualifiedName getSuperClassName() {
            final String name = signature.string(3);
            return name.trim().length() == 0 ? null : QualifiedName.create(name);
        }

        public Set<QualifiedName> getSuperInterfaces() {
            Set<QualifiedName> ifaces = Collections.emptySet();
            String separatedIfaces = signature.string(5);
            if (separatedIfaces != null && separatedIfaces.length() > 0) {
                ifaces = new HashSet<QualifiedName>();
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

        int getFlags() {
            return signature.integer(6);
        }
    }
}
