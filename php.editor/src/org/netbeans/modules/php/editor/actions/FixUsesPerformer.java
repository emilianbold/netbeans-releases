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

import java.util.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.UseElement;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.SemanticAnalysis;
import org.netbeans.modules.php.editor.parser.UnusedOffsetRanges;
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
    private final PHPParseResult parserResult;
    private final ImportData importData;
    private final String[] selections;
    private final boolean removeUnusedUses;
    private EditList editList;
    private BaseDocument baseDocument;

    public FixUsesPerformer(final PHPParseResult parserResult, final ImportData importData, final String[] selections, final boolean removeUnusedUses) {
        this.parserResult = parserResult;
        this.importData = importData;
        this.selections = selections;
        this.removeUnusedUses = removeUnusedUses;
    }

    public void perform() {
        final Document document = parserResult.getSnapshot().getSource().getDocument(false);
        if (document instanceof BaseDocument) {
            baseDocument = (BaseDocument) document;
            editList = new EditList(baseDocument);
            processSelections();
            processUnusedUses();
            editList.apply();
        }
    }

    private void processSelections() {
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(parserResult.getModel().getFileScope(), importData.caretPosition);
        int startOffset = getOffset(baseDocument, namespaceScope);
        List<String> useParts = new ArrayList<String>();
        for (int i = 0; i < selections.length; i++) {
            String use = selections[i];
            if (canBeUsed(use)) {
                SanitizedUse sanitizedUse = new SanitizedUse(use, i, selections);
                useParts.add(sanitizedUse.getSanitizedUsePart());
                List<UsedNamespaceName> namesToModify = importData.usedNamespaceNames.get(i);
                for (UsedNamespaceName usedNamespaceName : namesToModify) {
                    editList.replace(usedNamespaceName.getOffset(), usedNamespaceName.getReplaceLength(), sanitizedUse.getReplaceName(usedNamespaceName), false, 0);
                }
            }
        }
        editList.replace(startOffset, 0, createInsertString(useParts), false, 0);
    }

    private String createInsertString(final List<String> useParts) {
        StringBuilder insertString = new StringBuilder();
        Collections.sort(useParts, new UsePartsComparator());
        for (String usePart : useParts) {
            insertString.append(USE_PREFIX).append(usePart).append(SEMICOLON);
        }
        if (insertString.length() > 0) {
            insertString.append(NEW_LINE);
        }
        return insertString.toString();
    }

    private void processUnusedUses() {
        if (removeUnusedUses) {
            removeUnusedUses();
        }
    }

    private void removeUnusedUses() {
        for (UnusedOffsetRanges unusedRange : SemanticAnalysis.computeUnusedUsesOffsetRanges(parserResult)) {
            editList.replace(unusedRange.getRangeToReplace().getStart(), unusedRange.getRangeToReplace().getLength(), "", false, 0); //NOI18N
        }
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
        Collection<? extends UseElement> declaredUses = namespaceScope.getDeclaredUses();
        for (UseElement useElement : declaredUses) {
            if (offsetElement == null || offsetElement.getOffset() < useElement.getOffset()) {
                offsetElement = useElement;
            }
        }
        return (offsetElement != null) ? offsetElement : namespaceScope;
    }

    private static boolean canBeUsed(String use) {
        // Filter out "Don't use type." message.
        return use != null && !use.contains(SPACE);
    }

    private class SanitizedUse {
        private final String use;
        private String alias;

        public SanitizedUse(final String use, final int index, final String selections[]) {
            this.use = use;
            QualifiedName qualifiedName = QualifiedName.create(use);
            for (int i = index + 1; i < selections.length; i++) {
                if (selections[i].endsWith(ImportDataCreator.NS_SEPARATOR + qualifiedName.getName())) {
                    alias = qualifiedName.getName() + index;
                    break;
                }
            }
        }

        public String getSanitizedUsePart() {
            String sanitizedUsePart = hasAlias() ? use + AS_CONCAT + alias : use;
            return sanitizedUsePart.substring(ImportDataCreator.NS_SEPARATOR.length());
        }

        private boolean hasAlias() {
            return alias != null && !alias.isEmpty();
        }

        public String getReplaceName(final UsedNamespaceName usedNamespaceName) {
            return hasAlias() ? alias + ImportDataCreator.NS_SEPARATOR + usedNamespaceName.getReplaceName() : usedNamespaceName.getReplaceName();
        }

    }

    private class UsePartsComparator implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            return o1.compareToIgnoreCase(o2);
        }

    }

}
