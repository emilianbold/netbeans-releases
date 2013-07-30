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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.php.editor.actions.FixUsesAction.Options;
import org.netbeans.modules.php.editor.actions.ImportData.ItemVariant;
import org.netbeans.modules.php.editor.api.AliasedName;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.indent.CodeStyle;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.UseScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.UnusedUsesCollector;
import org.netbeans.modules.php.editor.parser.UnusedUsesCollector.UnusedOffsetRanges;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.UseStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.util.Exceptions;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class FixUsesPerformer {

    private static final String NEW_LINE = "\n"; //NOI18N
    private static final String SEMICOLON = ";"; //NOI18N
    private static final String SPACE = " "; //NOI18N
    private static final String USE_KEYWORD = "use"; //NOI18N
    private static final String USE_PREFIX = NEW_LINE + USE_KEYWORD + SPACE; //NOI18N
    private static final String AS_KEYWORD = "as"; //NOI18N
    private static final String AS_CONCAT = SPACE + AS_KEYWORD + SPACE;
    private static final String EMPTY_STRING = ""; //NOI18N
    private static final String COLON = ","; //NOI18N
    private final PHPParseResult parserResult;
    private final ImportData importData;
    private final List<ItemVariant> selections;
    private final boolean removeUnusedUses;
    private final Options options;
    private EditList editList;
    private BaseDocument baseDocument;

    public FixUsesPerformer(
            final PHPParseResult parserResult,
            final ImportData importData,
            final List<ItemVariant> selections,
            final boolean removeUnusedUses,
            final Options options) {
        this.parserResult = parserResult;
        this.importData = importData;
        this.selections = selections;
        this.removeUnusedUses = removeUnusedUses;
        this.options = options;
    }

    public void perform() {
        final Document document = parserResult.getSnapshot().getSource().getDocument(false);
        if (document instanceof BaseDocument) {
            baseDocument = (BaseDocument) document;
            editList = new EditList(baseDocument);
            processExistingUses();
            processSelections();
            editList.apply();
        }
    }

    private void processSelections() {
        resolveDuplicateSelections();
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(parserResult.getModel().getFileScope(), importData.caretPosition);
        assert namespaceScope != null;
        int startOffset = getOffset(baseDocument, namespaceScope);
        List<String> useParts = new ArrayList<>();
        Collection<? extends UseScope> declaredUses = namespaceScope.getDeclaredUses();
        for (UseScope useElement : declaredUses) {
            processUseElement(useElement, useParts);
        }
        for (int i = 0; i < selections.size(); i++) {
            ItemVariant itemVariant = selections.get(i);
            if (itemVariant.canBeUsed()) {
                SanitizedUse sanitizedUse = new SanitizedUse(modifyUseName(itemVariant.getName()), useParts, createAliasStrategy(i, useParts, selections));
                if (sanitizedUse.shouldBeUsed()) {
                    useParts.add(sanitizedUse.getSanitizedUsePart());
                }
                for (UsedNamespaceName usedNamespaceName : importData.getItems().get(i).getUsedNamespaceNames()) {
                    editList.replace(usedNamespaceName.getOffset(), usedNamespaceName.getReplaceLength(), sanitizedUse.getReplaceName(usedNamespaceName), false, 0);
                }
            }
        }
        editList.replace(startOffset, 0, createInsertString(useParts), false, 0);
    }

    private void resolveDuplicateSelections() {
        List<ImportData.DataItem> dataItems = importData.getItems();
        List<ItemVariant> selectionsCopy = new ArrayList<>(selections);
        List<Integer> itemsToRemove = new ArrayList<>();
        for (int i = 0; i < selections.size(); i++) {
            List<UsedNamespaceName> usedNamespaceNames = new ArrayList<>();
            ItemVariant baseVariant = selections.get(i);
            for (int j = i + 1; j < selectionsCopy.size(); j++) {
                ItemVariant testedVariant = selectionsCopy.get(j);
                if (baseVariant.equals(testedVariant) && !itemsToRemove.contains(j)) {
                    ImportData.DataItem duplicateItem = dataItems.get(j);
                    usedNamespaceNames.addAll(duplicateItem.getUsedNamespaceNames());
                    itemsToRemove.add(j);
                }
            }
            if (!usedNamespaceNames.isEmpty()) {
                dataItems.get(i).addUsedNamespaceNames(usedNamespaceNames);
            }
        }
        for (Integer integer : itemsToRemove) {
            dataItems.remove((int) integer);
            selections.remove((int) integer);
        }
    }

    private AliasStrategy createAliasStrategy(final int selectionIndex, final List<String> existingUseParts, final List<ItemVariant> selections) {
        AliasStrategy createAliasStrategy;
        if (options.aliasesCapitalsOfNamespaces()) {
            createAliasStrategy = new CapitalsStrategy(selectionIndex, existingUseParts, selections);
        } else {
            createAliasStrategy = new UnqualifiedNameStrategy(selectionIndex, existingUseParts, selections);
        }
        return createAliasStrategy;
    }

    private void processUseElement(final UseScope useElement, final List<String> useParts) {
        if (isUsed(useElement) || !removeUnusedUses) {
            AliasedName aliasedName = useElement.getAliasedName();
            if (aliasedName != null) {
                useParts.add(modifyUseName(aliasedName.getRealName().toString()) + AS_CONCAT + aliasedName.getAliasName());
            } else {
                useParts.add(modifyUseName(useElement.getName()));
            }
        }
    }

    private String modifyUseName(final String useName) {
        String result = useName;
        if (options.startUseWithNamespaceSeparator()) {
            result = result.startsWith(ImportDataCreator.NS_SEPARATOR) ? result : ImportDataCreator.NS_SEPARATOR + result;
        } else {
            result = result.startsWith(ImportDataCreator.NS_SEPARATOR) ? result.substring(ImportDataCreator.NS_SEPARATOR.length()) : result;
        }
        return result;
    }

    private boolean isUsed(final UseScope useElement) {
        boolean result = true;
        for (UnusedOffsetRanges unusedRange : new UnusedUsesCollector(parserResult).collect()) {
            if (unusedRange.getRangeToVisualise().containsInclusive(useElement.getOffset())) {
                result = false;
                break;
            }
        }
        return result;
    }

    private String createInsertString(final List<String> useParts) {
        StringBuilder insertString = new StringBuilder();
        Collections.sort(useParts, new UsePartsComparator());
        if (useParts.size() > 0) {
            insertString.append(NEW_LINE);
        }
        if (options.preferMultipleUseStatementsCombined()) {
            CodeStyle codeStyle = CodeStyle.get(baseDocument);
            String indentString = IndentUtils.createIndentString(codeStyle.getIndentSize(), codeStyle.expandTabToSpaces(), codeStyle.getTabSize());
            insertString.append(USE_PREFIX);
            for (int i = 0; i < useParts.size(); i++) {
                String usePart = useParts.get(i);
                if (i != 0) {
                    insertString.append(indentString);
                }
                insertString.append(usePart);
                insertString.append(i + 1 == useParts.size() ? SEMICOLON : COLON + NEW_LINE);
            }
        } else {
            for (String usePart : useParts) {
                insertString.append(USE_PREFIX).append(usePart).append(SEMICOLON);
            }
        }
        return insertString.toString();
    }

    private void processExistingUses() {
        ExistingUseStatementVisitor visitor = new ExistingUseStatementVisitor();
        Program program = parserResult.getProgram();
        if (program != null) {
            program.accept(visitor);
        }
        for (OffsetRange offsetRange : visitor.getUsedRanges()) {
            int startOffset = getOffsetWithoutLeadingWhitespaces(offsetRange.getStart());
            editList.replace(startOffset, offsetRange.getEnd() - startOffset, EMPTY_STRING, false, 0);
        }
    }

    private int getOffsetWithoutLeadingWhitespaces(final int startOffset) {
        int result = startOffset;
        baseDocument.readLock();
        try {
            TokenSequence<PHPTokenId> ts = LexUtilities.getPHPTokenSequence(baseDocument, startOffset);
            if (ts != null) {
                ts.move(startOffset);
                while (ts.movePrevious() && ts.token().id().equals(PHPTokenId.WHITESPACE)) {
                    result = ts.offset();
                }
            }
        } finally {
            baseDocument.readUnlock();
        }
        return result;
    }

    private static int getOffset(BaseDocument baseDocument, NamespaceScope namespaceScope) {
        try {
            return Utilities.getRowEnd(baseDocument, getReferenceElement(namespaceScope).getOffset());
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return 0;
    }

    private static ModelElement getReferenceElement(NamespaceScope namespaceScope) {
        ModelElement offsetElement = null;
        Collection<? extends UseScope> declaredUses = namespaceScope.getDeclaredUses();
        for (UseScope useElement : declaredUses) {
            if (offsetElement == null || offsetElement.getOffset() < useElement.getOffset()) {
                offsetElement = useElement;
            }
        }
        return (offsetElement != null) ? offsetElement : namespaceScope;
    }

    private interface AliasStrategy {
        String createAlias(final QualifiedName qualifiedName);
    }

    private abstract static class AliasStrategyImpl implements AliasStrategy {

        private final int selectionIndex;
        private final List<String> existingUseParts;
        private final List<ItemVariant> selections;

        public AliasStrategyImpl(final int selectionIndex, final List<String> existingUseParts, final List<ItemVariant> selections) {
            this.selectionIndex = selectionIndex;
            this.existingUseParts = existingUseParts;
            this.selections = selections;
        }

        @Override
        public String createAlias(final QualifiedName qualifiedName) {
            String result = "";
            final String possibleAliasedName = getPossibleAliasName(qualifiedName);
            String newAliasedName = possibleAliasedName;
            int i = 1;
            while (existSelectionWith(newAliasedName, selectionIndex) || existUseWith(newAliasedName)) {
                i++;
                newAliasedName = possibleAliasedName + i;
                result = newAliasedName;
            }
            return result.isEmpty() && mustHaveAlias(qualifiedName) ? possibleAliasedName : result;
        }

        private boolean mustHaveAlias(final QualifiedName qualifiedName) {
            final String unqualifiedName = qualifiedName.getName();
            return existSelectionWith(unqualifiedName, selectionIndex) || existUseWith(unqualifiedName);
        }

        private boolean existSelectionWith(final String name, final int selectionIndex) {
            boolean result = false;
            for (int i = selectionIndex + 1; i < selections.size(); i++) {
                if (endsWithName(selections.get(i).getName(), name)) {
                    result = true;
                }
            }
            return result;
        }

        private boolean existUseWith(final String name) {
            boolean result = false;
            for (String existingUsePart : existingUseParts) {
                if (endsWithName(existingUsePart, name) || existingUsePart.endsWith(SPACE + name)) {
                    result = true;
                }
            }
            return result;
        }

        private boolean endsWithName(final String usePart, final String name) {
            return usePart.endsWith(ImportDataCreator.NS_SEPARATOR + name);
        }

        protected abstract String getPossibleAliasName(final QualifiedName qualifiedName);

    }

    private static class CapitalsStrategy extends AliasStrategyImpl {

        public CapitalsStrategy(int selectionIndex, List<String> existingUseParts, List<ItemVariant> selections) {
            super(selectionIndex, existingUseParts, selections);
        }

        @Override
        protected String getPossibleAliasName(final QualifiedName qualifiedName) {
            final StringBuilder sb = new StringBuilder();
            for (String segment : qualifiedName.getSegments()) {
                sb.append(Character.toUpperCase(segment.charAt(0)));
            }
            return sb.toString();
        }

    }

    private static class UnqualifiedNameStrategy extends AliasStrategyImpl {

        public UnqualifiedNameStrategy(int selectionIndex, List<String> existingUseParts, List<ItemVariant> selections) {
            super(selectionIndex, existingUseParts, selections);
        }

        @Override
        protected String getPossibleAliasName(QualifiedName qualifiedName) {
            return qualifiedName.getName();
        }

    }

    private static class SanitizedUse {

        private final String use;
        private String alias;
        private final boolean shouldBeUsed;

        public SanitizedUse(final String use, final List<String> existingUseParts, final AliasStrategy createAliasStrategy) {
            this.use = use;
            QualifiedName qualifiedName = QualifiedName.create(use);
            if (!existingUseParts.contains(use)) {
                alias = createAliasStrategy.createAlias(qualifiedName);
                shouldBeUsed = true;
            } else {
                shouldBeUsed = false;
            }
        }

        public String getSanitizedUsePart() {
            return hasAlias() ? use + AS_CONCAT + alias : use;
        }

        private boolean hasAlias() {
            return alias != null && !alias.isEmpty();
        }

        public String getReplaceName(final UsedNamespaceName usedNamespaceName) {
            return hasAlias() ? alias : usedNamespaceName.getReplaceName();
        }

        public boolean shouldBeUsed() {
            return shouldBeUsed;
        }
    }

    private static class UsePartsComparator implements Comparator<String>, Serializable {

        @Override
        public int compare(String o1, String o2) {
            return o1.compareToIgnoreCase(o2);
        }
    }

    private static class ExistingUseStatementVisitor extends DefaultVisitor {

        private final List<OffsetRange> usedRanges = new LinkedList<>();

        public List<OffsetRange> getUsedRanges() {
            return Collections.unmodifiableList(usedRanges);
        }

        @Override
        public void visit(UseStatement node) {
            usedRanges.add(new OffsetRange(node.getStartOffset(), node.getEndOffset()));
        }
    }
}
