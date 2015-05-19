/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.api.codemodel.completion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Collection;
import java.util.List;
import static junit.framework.Assert.assertNotNull;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.api.codemodel.CMCursor;
import org.netbeans.modules.cnd.api.codemodel.CMFile;
import org.netbeans.modules.cnd.api.codemodel.CMSourceLocation;
import org.netbeans.modules.cnd.api.codemodel.CMSourceRange;
import org.netbeans.modules.cnd.api.codemodel.CMTranslationUnit;
import org.netbeans.modules.cnd.api.codemodel.test.CMBaseTestCase;
import org.netbeans.modules.cnd.codemodel.utils.CMCompletionUtils;
import org.netbeans.modules.cnd.spi.codemodel.support.IterableFactory;
import org.netbeans.modules.cnd.spi.codemodel.support.SPIUtilities;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.util.Utilities;

/**
 *
 * @author vkvashin
 */
public class CompletionQueryTestBase extends CMBaseTestCase {

    protected CompletionQueryTestBase(String testName) {
        super(testName);
    }

    protected void performTest(String source, int lineIndex, int colIndex) throws Exception {
        performTest(source, lineIndex, colIndex, "");// NOI18N
    }

    protected void performTest(String source, int lineIndex, int colIndex, String textToInsert) throws Exception {
        performTest(source, lineIndex, colIndex, textToInsert, 0);// NOI18N
    }

    protected void performTest(String source, int lineIndex, int colIndex, String textToInsert, int offsetAfterInsertion) throws Exception {
        performTest(source, lineIndex, colIndex, textToInsert, offsetAfterInsertion, getName() + ".ref");// NOI18N
    }

    protected void performTest(String source, int lineIndex, int colIndex, String textToInsert, int offsetAfterInsertion, String goldenFileName) throws Exception {
        performTest(source, lineIndex, colIndex, textToInsert, offsetAfterInsertion, goldenFileName, null, null);
    }

    protected void performTest(String source, int lineIndex, int colIndex, String textToInsert, int offsetAfterInsertion, String goldenFileName, String toPerformItemRE, String goldenFileName2) throws Exception {
        performTest(source, lineIndex, colIndex, textToInsert, offsetAfterInsertion, goldenFileName, toPerformItemRE, goldenFileName2, false);
    }

    protected void performTest(final String source, final int lineIndex, final int colIndex,
            final String textToInsert, int offsetAfterInsertion, final String goldenFileName,
            final String toPerformItemRE, final String goldenFileName2, final boolean tooltip) throws Exception {

        performTest(new TestPerformer() {

            @Override
            public void perform(File... sourceFiles) throws Exception {
                Iterable<CMCompletionResult> completions = null;

                URI uri = Utilities.toURI(sourceFiles[0]);
                
                Collection<CMTranslationUnit> units = SPIUtilities.getTranslationUnits(uri);
                
                if (!units.isEmpty()) {
                    CMTranslationUnit tu = units.iterator().next();
                    CMFile cmFile = tu.getFile(uri);                    
                    
                    CMSourceLocation caretPosition = tu.getLocation(cmFile, lineIndex, colIndex);                   
                    CMSourceLocation completionPosition = getCompletionPoint(tu, cmFile, caretPosition);
                    
                    CMCompletionQuery.QueryFlags flags = null;
                
                    CMCompletionResultList result = CMCompletionQuery.getCompletion(units, uri, completionPosition.getLine(), completionPosition.getColumn(), flags);
                    
                    if (result != null) {
                        completions = result.getItems();
                        
                        if (!caretPosition.equals(completionPosition)) {
                            String prefix = readRange(uri, completionPosition.getOffset(), caretPosition.getOffset());
                            completions = IterableFactory.filter(completions, new CompletionFilter(prefix));
                        }
                    }
                }                

                assertNotNull("Result should not be null", completions);
                
                //Arrays.sort(array, new CompletionComparatorWrapper(CompletionItemComparator.BY_PRIORITY));

                if (completions != null) {
                    for (CMCompletionResult item : sort(completions)) {
                        ref(format(item));
                    }
                }
                
                compareReferenceFiles();
            }
        }, source);
    }

    protected Iterable<CMCompletionResult> sort(Iterable<CMCompletionResult> items) {
        List<CMCompletionResult> sorted = new ArrayList<>();
        for (CMCompletionResult item : items) {
            sorted.add(item);
        }
        Collections.sort(sorted, new Comparator<CMCompletionResult>() {
            @Override
            public int compare(CMCompletionResult o1, CMCompletionResult o2) {
                if (o1.getPriority() != o2.getPriority()) {
                    return o1.getPriority() - o2.getPriority();
                }
                return format(o1).compareTo(format(o2));
            }
        });
        return sorted;
    }

    protected String format(CMCompletionResult item) {
        StringBuilder sb = new StringBuilder();
        for (CMCompletionChunk chunk : item.getChunks()) {
            sb.append(chunk.getKind().name()).append('=').append(chunk.getText()).append(' ');
        }
        sb.append(" [cursorKind=").append(item.getCursorKind()).append(']');
        return sb.toString();
    }
    
    
    private static CMSourceLocation getCompletionPoint(CMTranslationUnit tu, CMFile cmFile, CMSourceLocation caretPosition) {        
        CMCursor cursor = tu.getCursor(caretPosition);
        
        if (!cursor.getKind().isInvalid()) {
            int caretOffset = caretPosition.getOffset();
            
            CMSourceRange range = cursor.getExtent();

            int startOffset = range.getStart().getOffset();
            int endOffset = range.getEnd().getOffset();    
            
            if (startOffset < caretOffset && endOffset > caretOffset) {
                String text = readRange(cmFile.getURI(), startOffset, endOffset);
                
                TokenHierarchy<String> tokenHierarchy = TokenHierarchy.<String>create(text, CppTokenId.languageCpp());
                
                TokenSequence<CppTokenId> ts = (TokenSequence<CppTokenId>) tokenHierarchy.tokenSequence();
                ts.moveNext();
                
                Token<CppTokenId> token;
                
                while ((token = ts.offsetToken()) != null) {
                    int tokenStartOffset = token.offset(null);
                    int tokenEndOffset = tokenStartOffset + token.length();
                    
                    if (tokenEndOffset + startOffset >= caretOffset) {
                        break;
                    }
                    
                    ts.moveNext();
                }

                if (token != null) {
                    int tokenOffset = CMCompletionUtils.isCompletionSkipToken(token.id()) ? token.offset(null) : token.offset(null) + token.length();
                    CMSourceLocation anchorPoint = tu.getLocation(cmFile, tokenOffset + startOffset);
                    return anchorPoint;
                }
            }
        }        
        
        return caretPosition;
    }

    private static String readRange(URI uri, int startOffset, int endOffset) {
        int size = endOffset - startOffset;
        char buffer[] = new char[size];

        try {
            BufferedReader reader = new BufferedReader(new FileReader(Utilities.toFile(uri)));
            reader.skip(startOffset);
            reader.read(buffer, 0, size);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex.getCause());
        }
        
        return String.valueOf(buffer);
    }
    
    
    private static class CompletionFilter implements IterableFactory.Filter<CMCompletionResult> {
        
        private final String prefix;

        public CompletionFilter(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public boolean accept(CMCompletionResult element) {
            for (CMCompletionChunk chunk : element.getChunks()) {
                if (CMCompletionChunk.Kind.TypedText.equals(chunk.getKind())) {
                    if (!CharSequenceUtils.startsWith(chunk.getText(), prefix)) {
                        return false;
                    }
                    break; // there is only one TypedText chunk
                }
            }
            return true;
        }        
    }
    
}
