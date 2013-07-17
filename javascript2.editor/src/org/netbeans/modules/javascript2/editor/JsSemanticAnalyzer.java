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
package org.netbeans.modules.javascript2.editor;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.javascript2.editor.doc.spi.JsComment;
import org.netbeans.modules.javascript2.editor.api.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.api.lexer.LexUtilities;
import org.netbeans.modules.javascript2.editor.model.JsFunction;
import org.netbeans.modules.javascript2.editor.model.JsObject;
import org.netbeans.modules.javascript2.editor.model.Model;
import org.netbeans.modules.javascript2.editor.model.Occurrence;
import org.netbeans.modules.javascript2.editor.model.Type;
import org.netbeans.modules.javascript2.editor.model.impl.JsObjectImpl;
import org.netbeans.modules.javascript2.editor.model.impl.ModelUtils;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;

/**
 *
 * @author Petr Pisl
 */
public class JsSemanticAnalyzer extends SemanticAnalyzer<JsParserResult> {
    //public static final EnumSet<ColoringAttributes> UNUSED_VARIABLE_SET = EnumSet.of(ColoringAttributes.UNUSED, ColoringAttributes.VA);
    public static final EnumSet<ColoringAttributes> UNUSED_OBJECT_SET = EnumSet.of( ColoringAttributes.UNUSED,  ColoringAttributes.CLASS);

    private boolean cancelled;
    private Map<OffsetRange, Set<ColoringAttributes>> semanticHighlights;
    private static final List<String> GLOBAL_TYPES = Arrays.asList(Type.ARRAY, Type.STRING, Type.BOOLEAN, Type.NUMBER, Type.UNDEFINED);

    public JsSemanticAnalyzer() {
        this.cancelled = false;
        this.semanticHighlights = null;
    }

    @Override
    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights() {
        return semanticHighlights;
    }

    @Override
    public void run(JsParserResult result, SchedulerEvent event) {
        resume();

        if (isCancelled()) {
            return;
        }

        Map<OffsetRange, Set<ColoringAttributes>> highlights =
                new HashMap<OffsetRange, Set<ColoringAttributes>>(100);
        Model model = result.getModel();
        JsObject global = model.getGlobalObject();

        highlights = count(result, global, highlights);

        if (highlights != null && highlights.size() > 0) {
            semanticHighlights = highlights;
        } else {
            semanticHighlights = null;
        }
    }

    private Map<OffsetRange, Set<ColoringAttributes>> count (JsParserResult result, JsObject parent, Map<OffsetRange, Set<ColoringAttributes>> highlights) {

        for (Iterator<? extends JsObject> it = parent.getProperties().values().iterator(); it.hasNext();) {
            JsObject object = it.next();
            if (object.getDeclarationName() != null) {
                switch (object.getJSKind()) {
                    case CONSTRUCTOR:
                    case METHOD:
                    case FUNCTION:
                        if(object.isDeclared() && !object.isAnonymous() && !object.getDeclarationName().getOffsetRange().isEmpty()) {
                            highlights.put(LexUtilities.getLexerOffsets(result, object.getDeclarationName().getOffsetRange()), ColoringAttributes.METHOD_SET);
                        }
                        for(JsObject param: ((JsFunction)object).getParameters()) {
                            count(result, param, highlights);
                            if (!hasSourceOccurences(result, param)) {
                                OffsetRange range = LexUtilities.getLexerOffsets(result, param.getDeclarationName().getOffsetRange());
                                if (range.getStart() < range.getEnd()) {
                                    // only for declared parameters
                                    highlights.put(range, ColoringAttributes.UNUSED_SET);
                                }
                            }
                        }
                        break;
                    case PROPERTY_GETTER:
                    case PROPERTY_SETTER:
                        int offset = LexUtilities.getLexerOffset(result, object.getDeclarationName().getOffsetRange().getStart());
                        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(result.getSnapshot(), offset);
                        if (ts != null) {
                            ts.move(offset);
                            if (ts.moveNext() && ts.movePrevious()) {
                                Token token = LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.BLOCK_COMMENT, JsTokenId.DOC_COMMENT));
                                if (token.id() == JsTokenId.IDENTIFIER && token.length() == 3) {
                                    highlights.put(new OffsetRange(ts.offset(), ts.offset() + token.length()), ColoringAttributes.METHOD_SET);
                                }
                            }
                            highlights.put(LexUtilities.getLexerOffsets(result, object.getDeclarationName().getOffsetRange()), ColoringAttributes.FIELD_SET);
                        }
                        break;
                    case OBJECT:
                    case OBJECT_LITERAL:
                        if(!"UNKNOWN".equals(object.getName())) {
                            if (parent.getParent() == null && !GLOBAL_TYPES.contains(object.getName())) {
                                highlights.put(LexUtilities.getLexerOffsets(result, object.getDeclarationName().getOffsetRange()), ColoringAttributes.GLOBAL_SET);
                                for (Occurrence occurence : object.getOccurrences()) {
                                    if (!isCommentOccurence(result, occurence)) {
                                        highlights.put(LexUtilities.getLexerOffsets(result, occurence.getOffsetRange()), ColoringAttributes.GLOBAL_SET);
                                    }
                                }
                            } else if (object.isDeclared() && !ModelUtils.PROTOTYPE.equals(object.getName()) && !object.isAnonymous()) {
                                if((object.getOccurrences().isEmpty()
                                        || (object.getOccurrences().size() == 1 && object.getOccurrences().get(0).getOffsetRange().equals(object.getDeclarationName().getOffsetRange())))
                                        && object.getModifiers().contains(Modifier.PRIVATE)) {
                                    highlights.put(LexUtilities.getLexerOffsets(result, object.getDeclarationName().getOffsetRange()), UNUSED_OBJECT_SET);
                                } else {
                                    highlights.put(LexUtilities.getLexerOffsets(result, object.getDeclarationName().getOffsetRange()), ColoringAttributes.CLASS_SET);
                                }
                            }
                        }
                        break;
                    case PROPERTY:
                        if(object.isDeclared()) {
                            highlights.put(LexUtilities.getLexerOffsets(result, object.getDeclarationName().getOffsetRange()), ColoringAttributes.FIELD_SET);
                            for(Occurrence occurence: object.getOccurrences()) {
                                if (!isCommentOccurence(result, occurence)) {
                                    highlights.put(LexUtilities.getLexerOffsets(result, occurence.getOffsetRange()), ColoringAttributes.FIELD_SET);
                                }
                            }
                        }
                        break;
                    case FIELD:
                        highlights.put(object.getDeclarationName().getOffsetRange(), ColoringAttributes.FIELD_SET);
                        for (Occurrence occurence : object.getOccurrences()) {
                            if (!isCommentOccurence(result, occurence)) {
                                highlights.put(LexUtilities.getLexerOffsets(result, occurence.getOffsetRange()), ColoringAttributes.FIELD_SET);
                            }
                        }
                        break;
                    case VARIABLE:
                        if (parent.getParent() == null && !GLOBAL_TYPES.contains(object.getName())) {
                            highlights.put(LexUtilities.getLexerOffsets(result, object.getDeclarationName().getOffsetRange()), ColoringAttributes.GLOBAL_SET);
                            for(Occurrence occurence: object.getOccurrences()) {
                                if (!isCommentOccurence(result, occurence)) {
                                    highlights.put(LexUtilities.getLexerOffsets(result, occurence.getOffsetRange()), ColoringAttributes.GLOBAL_SET);
                                }
                            }
                        } else {
                            if ((object.getOccurrences().isEmpty()
                                    || (object.getOccurrences().size() == 1 && object.getOccurrences().get(0).getOffsetRange().equals(object.getDeclarationName().getOffsetRange())))
                                    && !GLOBAL_TYPES.contains(object.getName())) {
                                OffsetRange range = object.getDeclarationName().getOffsetRange();
                                if (range.getStart() < range.getEnd()) {
                                    // some virtual variables (like arguments) doesn't have to be declared, but are in the model
                                    highlights.put(LexUtilities.getLexerOffsets(result, object.getDeclarationName().getOffsetRange()), ColoringAttributes.UNUSED_SET);
                                }
                            } else if (object instanceof JsObjectImpl && !ModelUtils.ARGUMENTS.equals(object.getName())) {   // NOI18N
                                if (object.getOccurrences().size() <= ((JsObjectImpl)object).getCountOfAssignments()) {
                                    // probably is used only on the left site => is unused
                                    if (object.getDeclarationName().getOffsetRange().getLength() > 0) {
                                        highlights.put(LexUtilities.getLexerOffsets(result, object.getDeclarationName().getOffsetRange()), ColoringAttributes.UNUSED_SET);
                                    }
                                    for(Occurrence occurence: object.getOccurrences()) {
                                        if (occurence.getOffsetRange().getLength() > 0) {
                                            highlights.put(LexUtilities.getLexerOffsets(result, occurence.getOffsetRange()), ColoringAttributes.UNUSED_SET);
                                        }
                                    }
                                }
                            }
                        }
                }
            }
            if (isCancelled()) {
                highlights = null;
                break;
            }
            highlights = count(result, object, highlights);
        }

        return highlights;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public synchronized void cancel() {
        cancelled = true;
    }

    protected final synchronized boolean isCancelled() {
        return cancelled;
    }

    protected final synchronized void resume() {
        cancelled = false;
    }

    private boolean hasSourceOccurences(JsParserResult result, JsObject param) {
        if (param.getOccurrences().isEmpty()) {
            return false;
        }
        if (param.getOccurrences().size() == 1 && param.getOccurrences().get(0).getOffsetRange().equals(param.getDeclarationName().getOffsetRange())) {
            return false;
        }

        int sourceOccurenceCount = 0;
        for (Occurrence occurrence : param.getOccurrences()) {
            if (!isCommentOccurence(result, occurrence)) {
                 sourceOccurenceCount++;
            }
            if (sourceOccurenceCount > 1) {
                return true;
            }
        }
        return false;
    }

    private static boolean isCommentOccurence(JsParserResult result, Occurrence occurence) {
        // Comment blocks are cached by holders so this is faster than calls to token sequences.
        for (JsComment comment : result.getDocumentationHolder().getCommentBlocks().values()) {
            if (comment.getOffsetRange().containsInclusive(occurence.getOffsetRange().getStart())) {
                return true;
            }
        }
        return false;
    }

}
