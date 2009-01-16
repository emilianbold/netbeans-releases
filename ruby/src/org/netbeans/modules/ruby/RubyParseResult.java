/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.ruby;

import java.util.ArrayList;
import java.util.List;
import org.jruby.nb.ast.Node;
import org.jruby.nb.parser.RubyParserResult;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 * @author Tor Norbye
 */
public class RubyParseResult extends ParserResult {
    
    private final RubyParser parser;
    private final Node rootNode;
    
    private String source;
    private OffsetRange sanitizedRange = OffsetRange.NONE;
    private String sanitizedContents;
    private RubyStructureAnalyzer.AnalysisResult analysisResult;
    private RubyParser.Sanitize sanitized;
    private RubyParserResult jrubyResult;
    private boolean commentsAdded;
    private List<Error> errors;

    public RubyParseResult(RubyParser parser, Snapshot snapshot, Node rootNode) {
        super(snapshot);
        this.parser = parser;
        this.rootNode = rootNode;
        this.errors = new ArrayList<Error>();
    }

    @Override
    protected void invalidate() {
        // XXX: what exactly should we do here?
    }

    @Override
    public List<? extends Error> getDiagnostics() {
        return errors;
    }

    public void setErrors(List<? extends Error> errors) {
        this.errors = new ArrayList<Error>(errors);
    }

    // XXX: CSL rewrite
//    public ParserResult.AstTreeNode getAst() {
//        return ast;
//    }
//
//    public void setAst(AstTreeNode ast) {
//        this.ast = ast;
//    }

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
    void setSanitized(RubyParser.Sanitize sanitized, OffsetRange sanitizedRange, String sanitizedContents) {
        this.sanitized = sanitized;
        this.sanitizedRange = sanitizedRange;
        this.sanitizedContents = sanitizedContents;
    }

    public RubyParser.Sanitize getSanitized() {
        return sanitized;
    }    

    public RubyParserResult getJRubyResult() {
        return jrubyResult;
    }

    public void setStructure(@NonNull RubyStructureAnalyzer.AnalysisResult result) {
        this.analysisResult = result;
    }

    @NonNull
    public RubyStructureAnalyzer.AnalysisResult getStructure() {
        if (analysisResult == null) {
            analysisResult = new RubyStructureAnalyzer().analyze(this);
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
