/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.python.editor;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.modules.python.editor.lexer.PythonTokenId;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.Index;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.annotations.NonNull;
import org.netbeans.modules.gsf.spi.GsfUtilities;
import org.netbeans.modules.python.editor.PythonParser.Sanitize;
import org.netbeans.modules.python.editor.scopes.SymbolTable;
import org.openide.util.Exceptions;
import org.python.antlr.PythonTree;

/**
 * A ParserResult for Python. The AST Jython's AST.
 *
 * @todo Cache AstPath for caret position here!
 * @author Tor Norbye
 */
public class PythonParserResult extends ParserResult {
    private PythonTree root;
    private OffsetRange sanitizedRange = OffsetRange.NONE;
    private String source;
    private String sanitizedContents;
    private PythonParser.Sanitize sanitized;
    private PythonStructureScanner.AnalysisResult analysisResult;
    private SymbolTable symbolTable;
    private int codeTemplateOffset = -1;

    public PythonParserResult(PythonTree tree, PythonParser parser, ParserFile file, boolean isValid) {
        super(parser, file, PythonTokenId.PYTHON_MIME_TYPE, isValid);
        this.root = tree;
    }

    public PythonTree getRoot() {
        return root;
    }

    @Override
    public AstTreeNode getAst() {
        return PythonAstTreeNode.get(root);
    }

    /**
     * Set the range of source that was sanitized, if any.
     */
    void setSanitized(PythonParser.Sanitize sanitized, OffsetRange sanitizedRange, String sanitizedContents) {
        this.sanitized = sanitized;
        this.sanitizedRange = sanitizedRange;
        this.sanitizedContents = sanitizedContents;
        if (sanitizedContents == null || sanitizedRange == OffsetRange.NONE) {
            this.sanitized = Sanitize.NONE;
        }
    }

    public PythonParser.Sanitize getSanitized() {
        return sanitized;
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

    public SymbolTable getSymbolTable() {
        if (symbolTable == null) {
            symbolTable = new SymbolTable(root, file.getFileObject());
        }

        return symbolTable;
    }

    public String getSanitizedContents() {
        return sanitizedContents;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    void setStructure(@NonNull PythonStructureScanner.AnalysisResult result) {
        this.analysisResult = result;
    }

    @NonNull
    public PythonStructureScanner.AnalysisResult getStructure() {
        if (analysisResult == null) {
            CompilationInfo info = getInfo();
            if (info == null) {
                try {
                    info = new CompilationInfo(getFile().getFileObject()) {
                        private Document doc;

                        @Override
                        public Collection<? extends ParserResult> getEmbeddedResults(String mimeType) {
                            if (mimeType.equals(PythonTokenId.PYTHON_MIME_TYPE)) {
                                return Collections.singleton(PythonParserResult.this);
                            }
                            return null;
                        }

                        @Override
                        public ParserResult getEmbeddedResult(String mimeType, int offset) {
                            if (mimeType.equals(PythonTokenId.PYTHON_MIME_TYPE)) {
                                return PythonParserResult.this;
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
            analysisResult = PythonStructureScanner.analyze(info);
        }
        return analysisResult;
    }

    /**
     * @return the codeTemplateOffset
     */
    public int getCodeTemplateOffset() {
        return codeTemplateOffset;
    }

    /**
     * @param codeTemplateOffset the codeTemplateOffset to set
     */
    public void setCodeTemplateOffset(int codeTemplateOffset) {
        this.codeTemplateOffset = codeTemplateOffset;
    }
}
