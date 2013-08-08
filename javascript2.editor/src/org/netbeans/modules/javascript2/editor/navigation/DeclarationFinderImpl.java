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
package org.netbeans.modules.javascript2.editor.navigation;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.editor.EditorExtender;
import org.netbeans.modules.javascript2.editor.index.IndexedElement;
import org.netbeans.modules.javascript2.editor.index.JsIndex;
import org.netbeans.modules.javascript2.editor.api.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.api.lexer.LexUtilities;
import org.netbeans.modules.javascript2.editor.model.JsObject;
import org.netbeans.modules.javascript2.editor.model.Model;
import org.netbeans.modules.javascript2.editor.model.Occurrence;
import org.netbeans.modules.javascript2.editor.model.OccurrencesSupport;
import org.netbeans.modules.javascript2.editor.model.Type;
import org.netbeans.modules.javascript2.editor.model.TypeUsage;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class DeclarationFinderImpl implements DeclarationFinder {

    private final Language<JsTokenId> language;

    public DeclarationFinderImpl(Language<JsTokenId> language) {
        this.language = language;
    }

    @Override
    public DeclarationLocation findDeclaration(ParserResult info, int caretOffset) {
        JsParserResult jsResult = (JsParserResult)info;
        Model model = jsResult.getModel();
        int offset = info.getSnapshot().getEmbeddedOffset(caretOffset);
        OccurrencesSupport os = model.getOccurrencesSupport();
        Occurrence occurrence = os.getOccurrence(offset);
        if (occurrence != null) {
            JsObject object = occurrence.getDeclarations().iterator().next();
            JsObject parent = object.getParent();
            Collection<? extends TypeUsage> assignments = (parent == null) ? null : parent.getAssignmentForOffset(offset);
            if (assignments != null && assignments.isEmpty()) {
                assignments = parent.getAssignments();
            }
            Snapshot snapshot = jsResult.getSnapshot();
            JsIndex jsIndex = JsIndex.get(snapshot.getSource().getFileObject());
            List<IndexResult> indexResults = new ArrayList<IndexResult>();
            if (assignments == null || assignments.isEmpty()) {
                FileObject fo = object.getFileObject();
                if (object.isDeclared()) {
                    
                    if (fo != null) {
                        if (fo.equals(snapshot.getSource().getFileObject())) {
                            int docOffset = LexUtilities.getLexerOffset(jsResult, object.getDeclarationName().getOffsetRange().getStart());
                            if (docOffset > -1) {
                                return new DeclarationLocation(fo, docOffset);
                            }
                        } else {
                            // TODO we need to solve to translating model offsets to the doc offset for other files?
                            return new DeclarationLocation(fo, object.getDeclarationName().getOffsetRange().getStart());
                        }
                        
                    }
                } else {
                    Collection<? extends IndexResult> items = JsIndex.get(fo).findByFqn(
                            object.getFullyQualifiedName(), JsIndex.TERMS_BASIC_INFO);
                    indexResults.addAll(items);
                    DeclarationLocation location = processIndexResult(indexResults);
                    if (location != null) {
                        return location;
                    }
                } 
            } else {  
                TokenSequence ts = LexUtilities.getTokenSequence(snapshot, caretOffset, language);
                if (ts != null) {
                    ts.move(offset);
                    if (ts.moveNext() && ts.token().id() == JsTokenId.IDENTIFIER) {
                        String propertyName = ts.token().text().toString();
                        for (Type type : assignments) {
                            Collection<? extends IndexResult> items = jsIndex.findByFqn(
                                    type.getType() + "." + propertyName, JsIndex.TERMS_BASIC_INFO); // NOI18N
                            if(items.isEmpty()) {
                                items = jsIndex.findByFqn(type.getType() + ".prototype." + propertyName, JsIndex.TERMS_BASIC_INFO); // NOI18N
                            }
                            indexResults.addAll(items);
                        }
                        DeclarationLocation location = processIndexResult(indexResults);
                        if (location != null) {
                            return location;
                        }
                    }
                }
            }
        }
        for (DeclarationFinder finder : EditorExtender.getDefault().getDeclarationFinders()) {
            DeclarationLocation loc = finder.findDeclaration(info, caretOffset);
            if (loc != null && loc != DeclarationLocation.NONE) {
                return loc;
            }
        }
        return DeclarationLocation.NONE;
    }

    private DeclarationLocation processIndexResult(List<IndexResult> indexResults) {
        if (!indexResults.isEmpty()) {
            IndexResult iResult = indexResults.get(0);
            String value = iResult.getValue(JsIndex.FIELD_OFFSET);
            int offset = Integer.parseInt(value);
            DeclarationLocation location = new DeclarationLocation(iResult.getFile(), offset, IndexedElement.create(iResult));
            if (indexResults.size() > 1) {
                for (int i = 0; i < indexResults.size(); i++) {
                    iResult = indexResults.get(i);
                    location.addAlternative(new AlternativeLocationImpl(iResult));
                }
            }
            return location;
        }
        return null;
    }
    
    @Override
    public OffsetRange getReferenceSpan(final Document doc, final int caretOffset) {
        final OffsetRange[] value = new OffsetRange[1];

        doc.render(new Runnable() {

            @Override
            public void run() {
                TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(doc, caretOffset, language);
                if (ts != null) {
                    ts.move(caretOffset);
                    if (ts.moveNext() && ts.token().id() == JsTokenId.IDENTIFIER) {
                        value[0] = new OffsetRange(ts.offset(), ts.offset() + ts.token().length());
                    }
                }
            }
        });
        if (value[0] != null) {
            return value[0];
        }

        OffsetRange result;
        for (DeclarationFinder finder : EditorExtender.getDefault().getDeclarationFinders()) {
            result = finder.getReferenceSpan(doc, caretOffset);
            if (result != null && result != OffsetRange.NONE) {
                return result;
            }
        }
        return OffsetRange.NONE;
    }

    // Note: this class has a natural ordering that is inconsistent with equals.
    // We have to implement AlternativeLocation
    @org.netbeans.api.annotations.common.SuppressWarnings("EQ_COMPARETO_USE_OBJECT_EQUALS")
    public static class AlternativeLocationImpl implements AlternativeLocation {

        private final IndexResult iResult;
        private final int offset;
        private final DeclarationLocation location;
        private final IndexedElement element;
        
        public AlternativeLocationImpl(IndexResult iResult) {
            this.iResult = iResult;
            String value = iResult.getValue(JsIndex.FIELD_OFFSET);
            this.offset = Integer.parseInt(value);
            this.location = new DeclarationLocation(iResult.getFile(), offset);
            this.element = IndexedElement.create(iResult);
        }
        
        @Override
        public ElementHandle getElement() {
            return element;
        }

        private  String getStringLocation() {
            return iResult.getRelativePath() + " : " + offset; //NOI18N
        }
        
        @Override
        public String getDisplayHtml(HtmlFormatter formatter) {
            formatter.appendText(getStringLocation());
            return formatter.getText();
        }

        @Override
        public DeclarationLocation getLocation() {
            return location;
        }

        @Override
        public int compareTo(AlternativeLocation o) {
            AlternativeLocationImpl ali = (AlternativeLocationImpl)o;
            return getStringLocation().compareTo(ali.getStringLocation());
        }
        
    }
    
}
