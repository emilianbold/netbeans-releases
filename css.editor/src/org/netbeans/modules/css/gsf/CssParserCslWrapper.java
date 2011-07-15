/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.gsf;

import javax.swing.event.ChangeListener;
import org.netbeans.modules.css.lib.api.CssParserFactory;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;

/**
 * Wraps the CssParserResult from css.lib as an instance of CSL's Parser
 *
 * @author marekfukala
 */
public class CssParserCslWrapper extends Parser {

    private final Parser CSS3_PARSER = CssParserFactory.getDefault().createParser(null);
    
    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        CSS3_PARSER.parse(snapshot, task, event);
    }

    @Override
    public Result getResult(Task task) throws ParseException {
        return new CssParserResultCslWrapper((CssParserResult)CSS3_PARSER.getResult(task));
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        CSS3_PARSER.addChangeListener(changeListener);
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        CSS3_PARSER.addChangeListener(changeListener);
    }
 
    
//      public List<Error> errors(List<ParseException> parseExceptions, Snapshot snapshot, SimpleNode root) {
//        List<Error> errors = new ArrayList<Error>(parseExceptions.size());
//        for (ParseException pe : parseExceptions) {
//            Error e = createError(pe, snapshot, root);
//            if (e != null) {
//                errors.add(e);
//            }
//        }
//        return errors;
//    }
//
//    public static  boolean containsGeneratedCode(CharSequence text) {
//        return CharSequenceUtilities.indexOf(text, Constants.LANGUAGE_SNIPPET_SEPARATOR) != -1;
//    }
//
//    private Error createError(ParseException pe, Snapshot snapshot, SimpleNode root) {
//        FileObject fo = snapshot.getSource().getFileObject();
//        Token lastSuccessToken = pe.currentToken;
//        if (lastSuccessToken == null) {
//            //The pe was created in response to a TokenManagerError
//            return new DefaultError(PARSE_ERROR_KEY, pe.getMessage(), pe.getMessage(), fo,
//                    0, 0, Severity.ERROR);
//        }
//        Token errorToken = lastSuccessToken.next;
//        int from = errorToken.offset;
//
//        if (!(containsGeneratedCode(lastSuccessToken.image) || containsGeneratedCode(errorToken.image))) {
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
    
}
