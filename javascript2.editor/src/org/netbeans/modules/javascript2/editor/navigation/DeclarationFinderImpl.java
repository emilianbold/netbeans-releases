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
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.editor.index.IndexedElement;
import org.netbeans.modules.javascript2.editor.index.JsIndex;
import org.netbeans.modules.javascript2.editor.jquery.JQueryDeclarationFinder;
import org.netbeans.modules.javascript2.editor.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.lexer.LexUtilities;
import org.netbeans.modules.javascript2.editor.model.JsObject;
import org.netbeans.modules.javascript2.editor.model.Model;
import org.netbeans.modules.javascript2.editor.model.Occurrence;
import org.netbeans.modules.javascript2.editor.model.OccurrencesSupport;
import org.netbeans.modules.javascript2.editor.model.Type;
import org.netbeans.modules.javascript2.editor.model.TypeUsage;
import org.netbeans.modules.javascript2.editor.model.impl.ModelUtils;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;

/**
 *
 * @author Petr Pisl
 */
public class DeclarationFinderImpl implements DeclarationFinder{

    @Override
    public DeclarationLocation findDeclaration(ParserResult info, int caretOffset) {
        JsParserResult jsResult = (JsParserResult)info;
        Model model = jsResult.getModel();
        OccurrencesSupport os = model.getOccurrencesSupport();
        Occurrence occurrence = os.getOccurrence(caretOffset);
        if (occurrence != null) {

            JsObject object = occurrence.getDeclarations().iterator().next();
            JsObject parent = object.getParent();
            Collection<? extends TypeUsage> assignments = (parent == null) ? null : parent.getAssignmentForOffset(caretOffset);
            Snapshot snapshot = jsResult.getSnapshot();
            JsIndex jsIndex = JsIndex.get(snapshot.getSource().getFileObject());
            List<IndexResult> indexResults = new ArrayList<IndexResult>();
            if (assignments == null || assignments.isEmpty()) {
                if (object.isDeclared()) {
                    return new DeclarationLocation(object.getFileObject(), object.getDeclarationName().getOffsetRange().getStart());
                } else {
                    Collection<? extends IndexResult> items = jsIndex.query(
                            JsIndex.FIELD_FQ_NAME, ModelUtils.createFQN(object), QuerySupport.Kind.EXACT,
                            JsIndex.TERMS_BASIC_INFO);
                    indexResults.addAll(items);
                    DeclarationLocation location = processIndexResult(indexResults);
                    if (location != null) {
                        return location;
                    }
                } 
            } else {  
                TokenSequence ts = LexUtilities.getJsTokenSequence(snapshot, caretOffset);
                if (ts != null) {
                    ts.move(snapshot.getEmbeddedOffset(caretOffset));
                    if (ts.moveNext() && ts.token().id() == JsTokenId.IDENTIFIER) {
                        String propertyName = ts.token().text().toString();
                        for (Type type : assignments) {
                            Collection<? extends IndexResult> items = jsIndex.query(
                                    JsIndex.FIELD_FQ_NAME, type.getType() + "." + propertyName, QuerySupport.Kind.EXACT,  //NOI18N
                                    JsIndex.TERMS_BASIC_INFO);
                            if(items.isEmpty()) {
                                items = jsIndex.query(
                                    JsIndex.FIELD_FQ_NAME, type.getType() + ".prototype." + propertyName, QuerySupport.Kind.EXACT,  //NOI18N
                                    JsIndex.TERMS_BASIC_INFO);
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
        JQueryDeclarationFinder jQueryFinder = new JQueryDeclarationFinder();
        return jQueryFinder.findDeclaration(info, caretOffset);
    }

    private DeclarationLocation processIndexResult(List<IndexResult> indexResults) {
        if (!indexResults.isEmpty()) {
            IndexResult iResult = indexResults.get(0);
            String value = iResult.getValue(JsIndex.FIELD_OFFSET);
            int offset = Integer.parseInt(value);
            DeclarationLocation location = new DeclarationLocation(iResult.getFile(), offset);
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
    public OffsetRange getReferenceSpan(Document doc, int caretOffset) {
        OffsetRange result = OffsetRange.NONE;
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, caretOffset);
        if (ts != null) {
            ts.move(caretOffset);
            if (ts.moveNext() && ts.token().id() == JsTokenId.IDENTIFIER) {
                result =  new OffsetRange(ts.offset(), ts.offset() + ts.token().length());
            }
        }
        if(result == null) {
            JQueryDeclarationFinder jQueryFinder = new JQueryDeclarationFinder();
            result =  jQueryFinder.getReferenceSpan(doc, caretOffset);
        }
        return result;
    }
    
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
