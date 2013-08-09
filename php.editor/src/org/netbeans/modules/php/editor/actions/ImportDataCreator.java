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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.php.editor.actions.FixUsesAction.Options;
import org.netbeans.modules.php.editor.actions.ImportData.DataItem;
import org.netbeans.modules.php.editor.actions.ImportData.ItemVariant;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.InterfaceElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
@NbBundle.Messages("DoNotUseType=Don't use type.")
public class ImportDataCreator {
    public static final String NS_SEPARATOR = "\\"; //NOI18N
    private final Map<String, List<UsedNamespaceName>> usedNames;
    private final Index phpIndex;
    private final QualifiedName currentNamespace;
    private boolean shouldShowUsesPanel = false;
    private final Options options;
    private final List<PossibleItem> possibleItems = new ArrayList<>();

    private static Collection<TypeElement> sortTypeElements(final Collection<TypeElement> filteredTypeElements) {
        final List<TypeElement> sortedTypeElements = new ArrayList<>(filteredTypeElements);
        Collections.sort(sortedTypeElements, new TypeElementsComparator());
        return sortedTypeElements;
    }

    public ImportDataCreator(final Map<String, List<UsedNamespaceName>> usedNames, final Index phpIndex, final QualifiedName currentNamespace, final Options options) {
        this.usedNames = usedNames;
        this.phpIndex = phpIndex;
        this.currentNamespace = currentNamespace;
        this.options = options;
    }

    public ImportData create() {
        for (String typeName : usedNames.keySet()) {
            processTypeName(typeName);
        }
        ImportData data = new ImportData();
        for (PossibleItem possibleItem : possibleItems) {
            possibleItem.insertData(data);
        }
        data.shouldShowUsesPanel = shouldShowUsesPanel;
        return data;
    }

    private void processTypeName(final String typeName) {
        Collection<TypeElement> possibleTypes = fetchPossibleTypes(typeName);
        Collection<TypeElement> filteredDuplicates = filterDuplicates(possibleTypes);
        Collection<TypeElement> filteredExactUnqualifiedNames = filterExactUnqualifiedName(filteredDuplicates, typeName);
        if (filteredExactUnqualifiedNames.isEmpty()) {
            possibleItems.add(new EmptyItem(typeName));
        } else {
            Collection<TypeElement> filteredTypeElements = filterTypesFromCurrentNamespace(filteredExactUnqualifiedNames);
            if (filteredTypeElements.isEmpty()) {
                possibleItems.add(new ReplaceItem(typeName, filteredExactUnqualifiedNames));
            } else {
                possibleItems.add(new ValidItem(
                        typeName,
                        filteredTypeElements,
                        filteredTypeElements.size() != filteredExactUnqualifiedNames.size()));
            }
        }
    }

    private Collection<TypeElement> fetchPossibleTypes(final String typeName) {
        Collection<ClassElement> possibleClasses = phpIndex.getClasses(NameKind.prefix(typeName));
        Collection<InterfaceElement> possibleIfaces = phpIndex.getInterfaces(NameKind.prefix(typeName));
        Collection<TypeElement> possibleTypes = new HashSet<>();
        possibleTypes.addAll(possibleClasses);
        possibleTypes.addAll(possibleIfaces);
        return possibleTypes;
    }

    private Collection<TypeElement> filterDuplicates(final Collection<TypeElement> possibleTypes) {
        Collection<TypeElement> result = new HashSet<>();
        Collection<String> filteredTypeElements = new HashSet<>();
        for (TypeElement typeElement : possibleTypes) {
            String typeElementName = typeElement.toString();
            if (!filteredTypeElements.contains(typeElementName)) {
                filteredTypeElements.add(typeElementName);
                result.add(typeElement);
            }
        }
        return result;
    }

    private Collection<TypeElement> filterExactUnqualifiedName(final Collection<TypeElement> possibleTypes, final String typeName) {
        Collection<TypeElement> result = new HashSet<>();
        for (TypeElement typeElement : possibleTypes) {
            if (typeElement.getFullyQualifiedName().toString().endsWith(typeName)) {
                result.add(typeElement);
            }
        }
        return result;
    }

    private Collection<TypeElement> filterTypesFromCurrentNamespace(final Collection<TypeElement> possibleTypes) {
        Collection<TypeElement> result = new HashSet<>();
        for (TypeElement typeElement : possibleTypes) {
            if (!typeElement.getNamespaceName().equals(currentNamespace)) {
                result.add(typeElement);
            }
        }
        return result;
    }

    private boolean hasDefaultNamespaceName(final Collection<TypeElement> possibleTypes) {
        boolean result = false;
        for (TypeElement typeElement : possibleTypes) {
            if (typeElement.getNamespaceName().isDefaultNamespace()) {
                result = true;
                break;
            }
        }
        return result;
    }

    private boolean hasExactName(final Collection<TypeElement> typeElements, final QualifiedName exactName) {
        boolean result = false;
        for (TypeElement typeElement : typeElements) {
            if (typeElement.getFullyQualifiedName().equals(exactName)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private interface PossibleItem {

        void insertData(ImportData data);

    }

    private static class EmptyItem implements PossibleItem {
        private final String typeName;

        public EmptyItem(String typeName) {
            this.typeName = typeName;
        }

        @Override
        @NbBundle.Messages("CanNotBeResolved=<html><font color='#FF0000'>&lt;cannot be resolved&gt;")
        public void insertData(ImportData data) {
            ItemVariant itemVariant = new ItemVariant(Bundle.CanNotBeResolved(), ItemVariant.UsagePolicy.CAN_NOT_BE_USED, IconsUtils.getErrorGlyphIcon());
            data.add(new DataItem(typeName, Arrays.asList(new ItemVariant[] {itemVariant}), itemVariant));
        }

    }

    private final class ReplaceItem implements PossibleItem {
        private final String typeName;
        private final Collection<TypeElement> filteredExactUnqualifiedNames;

        public ReplaceItem(String typeName, Collection<TypeElement> filteredExactUnqualifiedNames) {
            this.typeName = typeName;
            this.filteredExactUnqualifiedNames = filteredExactUnqualifiedNames;
        }

        @Override
        public void insertData(ImportData data) {
            TypeElement typeElement = ModelUtils.getFirst(filteredExactUnqualifiedNames);
            assert typeElement != null;
            String itemVariantReplaceName = options.preferFullyQualifiedNames()
                    ? typeElement.getFullyQualifiedName().toString()
                    : typeElement.getName();
            ItemVariant replaceItemVariant = new ItemVariant(itemVariantReplaceName, ItemVariant.UsagePolicy.CAN_BE_USED);
            data.addJustToReplace(new DataItem(typeName, Collections.singletonList(replaceItemVariant), replaceItemVariant, usedNames.get(typeName)));
        }
    }

    private final class ValidItem implements PossibleItem {
        private final Collection<TypeElement> filteredTypeElements;
        private final String typeName;
        private final boolean existsTypeFromCurrentNamespace;

        private ValidItem(String typeName, Collection<TypeElement> filteredTypeElements, boolean existsTypeFromCurrentNamespace) {
            this.typeName = typeName;
            this.filteredTypeElements = filteredTypeElements;
            this.existsTypeFromCurrentNamespace = existsTypeFromCurrentNamespace;
        }

        @Override
        public void insertData(ImportData data) {
            Collection<TypeElement> sortedTypeElements = sortTypeElements(filteredTypeElements);
            List<ItemVariant> variants = new ArrayList<>();
            ItemVariant defaultValue = null;
            boolean isFirst = true;
            for (TypeElement typeElement : sortedTypeElements) {
                ItemVariant itemVariant = new ItemVariant(
                        typeElement.getFullyQualifiedName().toString(),
                        ItemVariant.UsagePolicy.CAN_BE_USED,
                        IconsUtils.getElementIcon(typeElement.getPhpElementKind()));
                variants.add(itemVariant);
                if (isFirst) {
                    defaultValue = itemVariant;
                    isFirst = false;
                }
                shouldShowUsesPanel = true;
            }
            ItemVariant dontUseItemVariant = new ItemVariant(Bundle.DoNotUseType(), ItemVariant.UsagePolicy.CAN_NOT_BE_USED);
            variants.add(dontUseItemVariant);
            QualifiedName qualifiedTypeName = QualifiedName.create(typeName);
            if (qualifiedTypeName.getKind().isFullyQualified()) {
                if (options.preferFullyQualifiedNames()) {
                    defaultValue = dontUseItemVariant;
                }
            } else {
                if ((currentNamespace.isDefaultNamespace() && hasDefaultNamespaceName(sortedTypeElements))
                        || existsTypeFromCurrentNamespace) {
                    defaultValue = dontUseItemVariant;
                }
            }
            Collections.sort(variants, new VariantsComparator());
            data.add(new DataItem(typeName, variants, defaultValue, usedNames.get(typeName)));
        }

    }

    private static class VariantsComparator implements Comparator<ItemVariant>, Serializable {

        @Override
        public int compare(ItemVariant o1, ItemVariant o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }

    private static class TypeElementsComparator implements Comparator<TypeElement>, Serializable {

        @Override
        public int compare(TypeElement o1, TypeElement o2) {
            return o1.getFullyQualifiedName().toString().compareToIgnoreCase(o2.getFullyQualifiedName().toString()) * -1;
        }

    }

}
