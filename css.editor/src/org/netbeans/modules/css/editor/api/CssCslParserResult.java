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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.css.editor.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.css.editor.Css3Utils;
import org.netbeans.modules.css.editor.csl.CssAnalyser;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.ProblemDescription;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author mfukala@netbeans.org
 */
public class CssCslParserResult extends ParserResult {

    private CssParserResult wrappedCssParserResult;
    
    private final List<Error> errors = new ArrayList<Error>();
    private AtomicBoolean analyzerErrorsComputed = new AtomicBoolean(false);

    public CssCslParserResult(Snapshot snapshot) {
        super(snapshot);
    }
    
    public CssCslParserResult(CssParserResult cssParserResult) {
        super(null);
        this.wrappedCssParserResult = cssParserResult;
    }

    public CssParserResult getWrappedCssParserResult() {
        return wrappedCssParserResult;
    }
    
    @Override
    public Snapshot getSnapshot() {
        return wrappedCssParserResult.getSnapshot();
    }
    
    public Node getParseTree() {
        return wrappedCssParserResult.getParseTree();
    }
    
    @Override
    protected void invalidate() {
        //XXX the result invalidation must be disabled since some CSL features uses the result outside 
        //the parsing task! This should be fixed in CSL
    }
    
    @Override
    public List<? extends Error> getDiagnostics() {

        if(!analyzerErrorsComputed.getAndSet(true)) {
            List<ProblemDescription> diagnostics = wrappedCssParserResult.getDiagnostics();
            
            
            errors.addAll(Css3Utils.getCslErrorForCss3ProblemDescription(
                        wrappedCssParserResult.getSnapshot().getSource().getFileObject(), diagnostics));
            
            errors.addAll(CssAnalyser.checkForErrors(getSnapshot(), getParseTree()));
        }
        
        return errors;
    }
    
    
    
//    
//     private Error createError(ProblemDescription pe) {
//         
//         pe.
//        int from = pe.getFrom();
//
//        if (!(containsGeneratedCode(pe..image) || containsGeneratedCode(errorToken.image))) {
//            if(!filterError(pe, snapshot, errorToken)) {
//                String errorMessage = buildErrorMessage(pe);
//                int documentStartOffset = LexerUtils.findNearestMappableSourcePosition(snapshot, from, false, SEARCH_LIMIT);
//                int documentEndOffset = LexerUtils.findNearestMappableSourcePosition(snapshot, from + errorToken.image.length(), true, SEARCH_LIMIT);
//
//                //lets try to filter out some of the unwanted errors on generated virtual code
//                if(root != null) { //the root can become null in case of completely unparseable file
//                    SimpleNode errorNode = SimpleNodeUtil.findDescendant(root, errorToken.offset);
//                    assert errorNode != null;
//                    SimpleNode parent = (SimpleNode)errorNode.jjtGetParent();
//                    //[Bug 183631] generated inline style is marked as an error
//                    //The code <h1 style="#{x.style}"></h1> is translated to
//                    // SELECTOR { @@@; } which is unparseable
//                    //
//                    //check if the declaration node contains generated code (@@@)
//                    //if so, just ignore the error
//                    if(parent != null) {
//                        if(parent.kind() == CssParserTreeConstants.JJTDECLARATION) {
//                            if(containsGeneratedCode(parent.image())) {
//                                return null;
//                            }
//                        }
//                    }
//                }
//
//
//                if (documentStartOffset == -1 && documentEndOffset == -1) {
//                    //the error is completely out of the mappable area, map it to the beginning of the document
//                    documentStartOffset = documentEndOffset = 0;
//                } else if (documentStartOffset == -1) {
//                    documentStartOffset = documentEndOffset;
//                } else if (documentEndOffset == -1) {
//                    documentEndOffset = documentStartOffset;
//                }
//
//                assert documentStartOffset <= documentEndOffset;
//
//                return new DefaultError(PARSE_ERROR_KEY, errorMessage, errorMessage, fo,
//                        documentStartOffset, documentEndOffset, Severity.ERROR);
//            }
//        }
//        return null;
//    }
//
//    private boolean filterError(ParseException pe, Snapshot snapshot, Token errorToken) {
//        //#182133 - filter error in css virtual source code for empty html tag class attribute
//        //<div class=""/> generates .|{} for the empty value so the css completion can work there
//        //and offer all classes
//        if (pe.currentToken.kind == CssParserConstants.DOT
//                && errorToken.kind == CssParserConstants.LBRACE
//                && snapshot.getOriginalOffset(pe.currentToken.offset) == -1) {
//            return true;
//        }
//
//        return false;
//    }
//
//    private String buildErrorMessage(ParseException pe) {
//        StringBuilder buff = new StringBuilder();
//        buff.append(ERROR_MESSAGE_PREFIX);
//
//        int maxSize = 0;
//        for (int i = 0; i < pe.expectedTokenSequences.length; i++) {
//            if (maxSize < pe.expectedTokenSequences[i].length) {
//                maxSize = pe.expectedTokenSequences[i].length;
//            }
//        }
//
//        Token tok = pe.currentToken.next;
//        buff.append('"');
//        for (int i = 0; i < maxSize; i++) {
//            buff.append(tok.image);
//            if (i < maxSize - 1) {
//                buff.append(',');
//                buff.append(' ');
//            }
//            tok = tok.next;
//        }
//        buff.append('"');
//
//        return buff.toString();
//    }
//    
}
