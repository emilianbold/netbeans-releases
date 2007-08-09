/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.ruby;

import java.util.List;
import java.util.Set;

import org.jruby.ast.Node;
import org.jruby.ast.RootNode;
import org.jruby.parser.RubyParserResult;
import org.netbeans.api.gsf.Element;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.gsf.ParserFile;
import org.netbeans.api.gsf.ParserResult;
import org.netbeans.api.gsf.annotations.NonNull;
import org.netbeans.modules.ruby.elements.AstElement;
import org.netbeans.modules.ruby.elements.AstRootElement;


/**
 *
 * @author Tor Norbye
 */
public class RubyParseResult extends ParserResult {
    private AstTreeNode ast;
    private Node root;
    private RootNode realRoot;
    private AstRootElement rootElement;
    private String source;
    private OffsetRange sanitizedRange = OffsetRange.NONE;
    private StructureAnalyzer.AnalysisResult analysisResult;
    private RubyParserResult jrubyResult;
    private boolean commentsAdded;

    /** Result used for failed compilation
     * @todo Provide errors too?
     */
    public RubyParseResult(ParserFile file) {
        super(file);
    }

    /**
     * Result used for successful compilation
     */
    public RubyParseResult(ParserFile file, AstRootElement rootElement, AstTreeNode ast, Node root,
        RootNode realRoot, RubyParserResult jrubyResult) {
        super(file);
        this.rootElement = rootElement;
        this.ast = ast;
        this.root = root;
        this.realRoot = realRoot;
        this.jrubyResult = jrubyResult;
    }

    public ParserResult.AstTreeNode getAst() {
        return ast;
    }

    public void setAst(AstTreeNode ast) {
        this.ast = ast;
    }

    @Override
    public Element getRoot() {
        return rootElement;
    }

    /** The root node of the AST produced by the parser.
     * Later, rip out the getAst part etc.
     */
    public Node getRootNode() {
        return root;
    }

    public Node getRealRoot() {
        return realRoot;
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

    /**
     * Set the range of source that was sanitized, if any.
     */
    public void setSanitizedRange(OffsetRange sanitizedRange) {
        this.sanitizedRange = sanitizedRange;
    }

    public RubyParserResult getJRubyResult() {
        return jrubyResult;
    }

    public void setStructure(@NonNull StructureAnalyzer.AnalysisResult result) {
        this.analysisResult = result;
    }

    @NonNull
    public StructureAnalyzer.AnalysisResult getStructure() {
        if (analysisResult == null) {
            analysisResult = new StructureAnalyzer().analyze(this);
        }
        return analysisResult;
    }

    public boolean isCommentsAdded() {
        return commentsAdded;
    }

    public void setCommentsAdded(boolean commentsAdded) {
        this.commentsAdded = commentsAdded;
    }
}
