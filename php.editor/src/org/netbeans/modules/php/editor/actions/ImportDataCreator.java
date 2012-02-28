/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.actions;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.InterfaceElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class ImportDataCreator {
    public static final String NS_SEPARATOR = "\\"; //NOI18N
    private final Map<String, List<UsedNamespaceName>> usedNames;
    private final Index phpIndex;
    private final QualifiedName currentNamespace;
    private ImportData data;
    private boolean shouldShowUsesPanel = false;
    private final Options options;

    public ImportDataCreator(final Map<String, List<UsedNamespaceName>> usedNames, final Index phpIndex, final QualifiedName currentNamespace, final Options options) {
        this.usedNames = usedNames;
        this.phpIndex = phpIndex;
        this.currentNamespace = currentNamespace;
        this.options = options;
    }

    @NbBundle.Messages({
        "CanNotBeResolved=<html><font color='#FF0000'>&lt;cannot be resolved&gt;",
        "DoNotUseType=Don't use type."
    })
    public ImportData create() {
        data = new ImportData(usedNames.size());
        int index = 0;
        for (String typeName : usedNames.keySet()) {
            processTypeName(index, typeName);
            index++;
        }
        data.shouldShowUsesPanel = shouldShowUsesPanel;
        return data;
    }

    private void processTypeName(final int index, final String typeName) {
        data.names[index] = typeName;
        Set<TypeElement> possibleTypes = fetchPossibleTypes(typeName);
        if (possibleTypes.isEmpty()) {
            insertEmptyData(index);
        } else {
            Set<TypeElement> filteredDuplicates = filterDuplicates(possibleTypes);
            Set<TypeElement> filteredTypeElements = filterExactUnqualifiedName(filteredDuplicates, typeName);
            insertPossibleData(index, filteredTypeElements, typeName);
        }
    }

    private Set<TypeElement> fetchPossibleTypes(final String typeName) {
        Set<ClassElement> possibleClasses = phpIndex.getClasses(NameKind.prefix(typeName));
        Set<InterfaceElement> possibleIfaces = phpIndex.getInterfaces(NameKind.prefix(typeName));
        Set<TypeElement> possibleTypes = new HashSet<TypeElement>();
        possibleTypes.addAll(possibleClasses);
        possibleTypes.addAll(possibleIfaces);
        return possibleTypes;
    }

    private void insertEmptyData(final int index) {
        data.variants[index] = new String[1];
        data.variants[index][0] = Bundle.CanNotBeResolved();
        data.defaults[index] = data.variants[index][0];
        data.icons[index] = new Icon[1];
        data.icons[index][0] = IconsUtils.getErrorGlyphIcon();
    }

    private void insertPossibleData(final int index, final Set<TypeElement> filteredTypeElements, final String typeName) {
        data.variants[index] = new String[filteredTypeElements.size() + 1];
        data.icons[index] = new Icon[data.variants[index].length];
        data.usedNamespaceNames.put(index, usedNames.get(typeName));
        int i = -1;
        for (TypeElement typeElement : filteredTypeElements) {
            data.variants[index][++i] = typeElement.getFullyQualifiedName().toString();
            data.icons[index][i] = IconsUtils.getElementIcon(typeElement.getPhpElementKind());
            if (i == 0) {
                data.defaults[index] = data.variants[index][i];
            }
            shouldShowUsesPanel = true;
        }
        data.variants[index][++i] = Bundle.DoNotUseType();
        data.icons[index][i] = null;
        QualifiedName qualifiedTypeName = QualifiedName.create(typeName);
        if (qualifiedTypeName.getKind().isFullyQualified()) {
            if (options.preferFullyQualifiedNames()) {
                data.defaults[index] = data.variants[index][i];
            }
        } else {
            QualifiedName exactMatchName = createExactMatchName(qualifiedTypeName);
            if (hasDefaultNamespaceName(filteredTypeElements) || hasExactName(filteredTypeElements, exactMatchName)) {
                data.defaults[index] = data.variants[index][i];
            }
        }
    }

    private Set<TypeElement> filterDuplicates(final Set<TypeElement> possibleTypes) {
        Set<TypeElement> result = new HashSet<TypeElement>();
        Set<String> filteredTypeElements = new HashSet<String>();
        for (TypeElement typeElement : possibleTypes) {
            String typeElementName = typeElement.toString();
            if (!filteredTypeElements.contains(typeElementName)) {
                filteredTypeElements.add(typeElementName);
                result.add(typeElement);
            }
        }
        return result;
    }

    private Set<TypeElement> filterExactUnqualifiedName(final Set<TypeElement> possibleTypes, final String typeName) {
        Set<TypeElement> result = new HashSet<TypeElement>();
        for (TypeElement typeElement : possibleTypes) {
            if (typeElement.getFullyQualifiedName().toString().endsWith(typeName)) {
                result.add(typeElement);
            }
        }
        return result;
    }

    private QualifiedName createExactMatchName(final QualifiedName currentType) {
        QualifiedName result = currentType;
        if (!currentType.getKind().isFullyQualified()) {
            String namespace = currentNamespace.toString();
            if (currentType.getSegments().size() > 1) {
                if (!namespace.trim().isEmpty()) {
                    namespace += NS_SEPARATOR;
                }
                // -2
                // because of possible bug in QualifiedName:269
                // for (int i = 0; i <= numberOfSegments; i ++) {
                // ====>>>>
                // for (int i = 0; i < numberOfSegments; i ++) {
                namespace += currentType.toString(currentType.getSegments().size() - 2);
            }
            result = QualifiedName.createFullyQualified(currentType.getSegments().getLast(), namespace);
        }
        return result;
    }

    private boolean hasDefaultNamespaceName(final Set<TypeElement> possibleTypes) {
        boolean result = false;
        for (TypeElement typeElement : possibleTypes) {
            if (typeElement.getNamespaceName().isDefaultNamespace()) {
                result = true;
                break;
            }
        }
        return result;
    }

    private boolean hasExactName(final Set<TypeElement> typeElements, final QualifiedName exactName) {
        boolean result = false;
        for (TypeElement typeElement : typeElements) {
            if (typeElement.getFullyQualifiedName().equals(exactName)) {
                result = true;
                break;
            }
        }
        return result;
    }

    static class Options {

        private final boolean preferFullyQualifiedNames;

        public Options(boolean preferFullyQualifiedNames) {
            this.preferFullyQualifiedNames = preferFullyQualifiedNames;
        }

        public boolean preferFullyQualifiedNames() {
            return preferFullyQualifiedNames;
        }

    }

}
