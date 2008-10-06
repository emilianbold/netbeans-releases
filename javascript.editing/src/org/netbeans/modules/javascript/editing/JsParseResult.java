/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.javascript.editing;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.text.Document;
import org.mozilla.nb.javascript.FunctionNode;
import org.mozilla.nb.javascript.Node;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.Index;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.annotations.NonNull;
import org.netbeans.modules.gsf.spi.GsfUtilities;
import org.netbeans.modules.javascript.editing.embedding.JsModel;
import org.netbeans.modules.javascript.editing.lexer.JsTokenId;
import org.openide.util.Exceptions;


/**
 *
 * @author Tor Norbye
 */
public class JsParseResult extends ParserResult {
    JsModel model;
    private AstTreeNode ast;
    private Node rootNode;
    private String source;
    private OffsetRange sanitizedRange = OffsetRange.NONE;
    private String sanitizedContents;
    private JsAnalyzer.AnalysisResult analysisResult;
    private JsParser.Sanitize sanitized;
    private boolean commentsAdded;
    private IncrementalParse incrementalParse;

    public JsParseResult(JsParser parser, ParserFile file, Node rootNode, AstTreeNode ast) {
        super(parser, file, JsTokenId.JAVASCRIPT_MIME_TYPE);
        this.rootNode = rootNode;
        this.ast = ast;
    }
    
//    public JsParseResult(ParserFile file, AstRootElement rootElement, AstTreeNode ast, Node root,
//        RootNode realRoot, JsParserResult jrubyResult) {
//        super(file);
//        this.rootElement = rootElement;
//        this.ast = ast;
//        this.root = root;
//        this.realRoot = realRoot;
//        this.jrubyResult = jrubyResult;
//    }

    public ParserResult.AstTreeNode getAst() {
        return ast;
    }

    public void setAst(AstTreeNode ast) {
        this.ast = ast;
    }

    /** The root node of the AST produced by the parser.
     * Later, rip out the getAst part etc.
     */
    public Node getRootNode() {
        return rootNode;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Return whether the source code for the parse result was "cleaned"
     * or "sanitized" (modified to reduce chance of parser errors) or not.
     * This method returns OffsetRange.NONE if the source was not sanitized,
     * otherwise returns the actual sanitized range.
     */
    public OffsetRange getSanitizedRange() {
        return sanitizedRange;
    }
    
    public String getSanitizedContents() {
        return sanitizedContents;
    }

    /**
     * Set the range of source that was sanitized, if any.
     */
    void setSanitized(JsParser.Sanitize sanitized, OffsetRange sanitizedRange, String sanitizedContents) {
        this.sanitized = sanitized;
        this.sanitizedRange = sanitizedRange;
        this.sanitizedContents = sanitizedContents;
    }

    public JsParser.Sanitize getSanitized() {
        return sanitized;
    }    

    public void setStructure(@NonNull JsAnalyzer.AnalysisResult result) {
        this.analysisResult = result;
    }

    @NonNull
    public JsAnalyzer.AnalysisResult getStructure() {
        if (analysisResult == null) {
            CompilationInfo info = getInfo();
            if (info == null) {
                try {
                    info = new CompilationInfo(getFile().getFileObject()) {
                        private Document doc;
                        
                        @Override
                        public Collection<? extends ParserResult> getEmbeddedResults(String mimeType) {
                            if (mimeType.equals(JsTokenId.JAVASCRIPT_MIME_TYPE)) {
                                return Collections.singleton(JsParseResult.this);
                            }
                            return null;
                        }

                        @Override
                        public ParserResult getEmbeddedResult(String mimeType, int offset) {
                            if (mimeType.equals(JsTokenId.JAVASCRIPT_MIME_TYPE)) {
                                return JsParseResult.this;
                            }
                            return null;
                        }

                        @Override
                        public String getText() {
                            return getSource();
                        }

                        @Override
                        public Index getIndex(String mimeType) {
                            return null;
                        }

                        @Override
                        public List<Error> getErrors() {
                            return Collections.emptyList();
                        }
                        
                        @Override
                        public Document getDocument() {
                            if (doc == null) {
                                doc = GsfUtilities.getDocument(getFileObject(), true);
                            }
                            
                            return doc;
                        }
                    };
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
            analysisResult = JsAnalyzer.analyze(this, info);
        }
        return analysisResult;
    }

    public boolean isCommentsAdded() {
        return commentsAdded;
    }

    public void setCommentsAdded(boolean commentsAdded) {
        this.commentsAdded = commentsAdded;
    }

    private VariableVisitor variableVisitor;

    public VariableVisitor getVariableVisitor() {
        if (variableVisitor == null) {
            if (incrementalParse != null && incrementalParse.previousResult.variableVisitor != null) {
                variableVisitor = incrementalParse.previousResult.variableVisitor;
                variableVisitor.incrementalEdits(incrementalParse);
            } else {
                Node root = getRootNode();
                assert root != null : "Attempted to get variable visitor for broken source";
                variableVisitor = new VariableVisitor();
                new ParseTreeWalker(variableVisitor).walk(root);
            }
        }

        return variableVisitor;
    }
    
    @Override
    public String toString() {
        return "JsParseResult(file=" + getFile() + ",rootnode=" + rootNode + ")";
    }

    public void setIncrementalParse(IncrementalParse incrementalParse) {
        this.incrementalParse = incrementalParse;
    }

    public IncrementalParse getIncrementalParse() {
        return incrementalParse;
    }

    public static class IncrementalParse {
        public FunctionNode oldFunction;
        public FunctionNode newFunction;
        /**
         * The offset of the beginning of the function node that was replaced
         */
        public int incrementalOffset;
        /**
         * The offset at which the function node used to end
         */
        public int incrementalOffsetLimit;
        /**
         * The new offset at which the function node ends - old end, e.g. delta to apply
         * to all offsets greated than the incremental offset limit
         */
        public int incrementalOffsetDelta;
        public JsParseResult previousResult;

        public IncrementalParse(FunctionNode oldFunction, FunctionNode newFunction, int incrementalOffset, int incrementalOffsetLimit, int incrementalOffsetDelta, JsParseResult previousResult) {
            this.oldFunction = oldFunction;
            this.newFunction = newFunction;
            this.incrementalOffset = incrementalOffset;
            this.incrementalOffsetLimit = incrementalOffsetLimit;
            this.incrementalOffsetDelta = incrementalOffsetDelta;
            this.previousResult = previousResult;
        }

        // Cached for incremental support
        //public Map<OffsetRange, Set<ColoringAttributes>> semanticHighlights;
    }
}
