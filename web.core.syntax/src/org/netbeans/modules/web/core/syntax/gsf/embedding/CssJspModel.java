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
package org.netbeans.modules.web.core.syntax.gsf.embedding;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.css.parser.ASCII_CharStream;
import org.netbeans.modules.css.parser.CSSParser;
import org.netbeans.modules.css.parser.CSSParserTreeConstants;
import org.netbeans.modules.css.parser.NodeVisitor;
import org.netbeans.modules.css.parser.ParseException;
import org.netbeans.modules.css.parser.SimpleNode;
import org.netbeans.modules.css.parser.SimpleNodeUtil;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.html.editor.gsf.embedding.CssModel;
import org.openide.util.Exceptions;

/**
 * Creates a CSS model from HTML source code in JSP page. 
 * 
 * @author Tor Norbye, Marek Fukala
 */
public class CssJspModel extends CssModel {

    private static CSSParser PARSER;

    private static synchronized CSSParser parser() {
        if (PARSER == null) {
            PARSER = new CSSParser();
        }
        return PARSER;

    }
    private static final Logger LOGGER = Logger.getLogger(CssJspModel.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);

    public static CssJspModel get(Document doc) {
        CssJspModel model = (CssJspModel) doc.getProperty(CssJspModel.class);
        if (model == null) {
            model = new CssJspModel(doc);
            doc.putProperty(CssJspModel.class, model);
        }

        return model;
    }

    private static final String PREFIX = "GENERATED_";
    private static final String POSTFIX = ";";
    
    private static final String EL = PREFIX + "EXPRESSION_LANGUAGE" + POSTFIX;
    private static final String JAVA = PREFIX + "GENERATED_JAVA_CODE" + POSTFIX;
    
    private static final String FIXED_SELECTOR = PREFIX + "FIXED_SELECTOR";
    
    private CssJspModel(Document doc) {
        super(doc);
    }

    @Override
    public String getCode() {
        if (documentDirty) {
            long a = System.currentTimeMillis();
            
            documentDirty = false;

            codeBlocks.clear();
            StringBuilder buffer = new StringBuilder();

            BaseDocument d = (BaseDocument) doc;
            try {
                d.readLock();
                List<OffsetRange> generated = new  ArrayList<OffsetRange>();
                extractCssFromJSP(doc, buffer, generated);

                if(generated.isEmpty()) {
                    code = buffer.toString();
                    LOGGER.log(Level.FINE, "NO REPLACED TEMPLATING!");
                    return code;
                }
                
                Boolean doSanitize = (Boolean)doc.getProperty("sanitize_source");
                
                if(doSanitize != null && !doSanitize.booleanValue()) {
                    //do not sanitize
                    code = buffer.toString();
                    
                    LOGGER.log(Level.FINE, "SANITIZING DISABLED!");
                    LOGGER.log(Level.FINE, dumpCode());
                    return code;
                }
                
                if (LOG) {
                    code = buffer.toString();
                    LOGGER.log(Level.FINE, "BEFORE SANITIZING");
                    LOGGER.log(Level.FINE, dumpCode());
                }

                long b = System.currentTimeMillis();
                sanitizeCode(buffer, generated);

                code = buffer.toString();
                
                if (LOG) {
                    
                    LOGGER.log(Level.FINE, "AFTER SANITIZING");
                    LOGGER.log(Level.FINE, dumpCode());
                }


                if(LOG) {
                    LOGGER.log(Level.FINE, "CSS source generation took " + (b - a) + " ms.");
                    LOGGER.log(Level.FINE, "CSS source validation took " + (System.currentTimeMillis() - b) + " ms.");
                }


            } finally {
                d.readUnlock();
            }

        }




        return code;
    }

    //TODO reuse the parser result - now the parser result is lost and reparsed again in CSSGSFParser
    private void sanitizeCode(final StringBuilder buff, final List<OffsetRange> templatingBlocks) {
        //final StringBuffer buff = new StringBuffer(code);
        try {

            parser().errors().clear();
            parser().ReInit(new ASCII_CharStream(new StringReader(buff.toString())));
            SimpleNode root = parser().styleSheet();

            for (ParseException pe : (List<ParseException>) parser().errors()) {
                org.netbeans.modules.css.parser.Token lastSuccessToken = pe.currentToken;
                org.netbeans.modules.css.parser.Token errorToken = lastSuccessToken.next;

                int from = errorToken.offset;
                int to = from + errorToken.image.length();

                LOGGER.log(Level.FINE, "Parser Error " + from + " -  " + to + ": " + pe.getMessage());
            }

            NodeVisitor visitor = new NodeVisitor() {

                public void visit(SimpleNode node) {
                    if (node.kind() == CSSParserTreeConstants.JJTERROR_SKIPBLOCK || node.kind() == CSSParserTreeConstants.JJTERROR_SKIPDECL) {
                        SimpleNode parent = (SimpleNode) node.jjtGetParent();

                        LOGGER.log(Level.FINE, "Tree Error  on " + node + "; parent: " + parent);

                        if (parent.kind() == CSSParserTreeConstants.JJTDECLARATION) {
                            char justAfterDeclarationNode = buff.charAt(parent.endOffset()); 
                            //possibly clear also the semicolon which is not part of the declaration node
                            clearNode(parent, buff, 0, justAfterDeclarationNode == ';' ? 1 : 0, templatingBlocks, false);   
                        }
                        if (parent.kind() == CSSParserTreeConstants.JJTSTYLERULE) {
                            SimpleNode siblingBefore = SimpleNodeUtil.getSibling(node, true);
                            if (siblingBefore.kind() == CSSParserTreeConstants.JJTREPORTERROR) {
                                siblingBefore = SimpleNodeUtil.getSibling(siblingBefore, true);
                                if (siblingBefore.kind() == CSSParserTreeConstants.JJTDECLARATION) {
                                    clearNode(siblingBefore, buff, 0, 0, templatingBlocks, false); //clear the last declaration node
                                    clearNode(node, buff, 0, -1, templatingBlocks, true); //clear the skipblock itself, exclude closing symbol
                                } else if(siblingBefore.kind() == CSSParserTreeConstants.JJTSELECTORLIST) {
                                    //error in selector list
                                    //repair the code from the selector list beginning to first left curly bracket
                                    int from = siblingBefore.startOffset();
                                    int curlyBracketIndex = buff.indexOf("{", from);
                                    if(curlyBracketIndex > 0) {
                                        clearAndWrite(buff, from, curlyBracketIndex, FIXED_SELECTOR);
                                    }
                                    
                                }
                            }
                        }


                    }
                }
            };

            root.visitChildren(visitor);

        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    private void clearNode(SimpleNode node, StringBuilder buff, 
            int startDelta, int endDelta, 
            List<OffsetRange> templatingBlocks,
            boolean forceClear) {
        int from = node.startOffset() + startDelta;
        int to = node.endOffset() + endDelta;

        //fast hack, I should rather use the templating ranges
        if(forceClear || buff.substring(from, to).contains(PREFIX)) {
            LOGGER.log(Level.FINE, "CLEARING NODE " + node + " [" + buff.substring(from, to) + "]");
            clear(buff, from, to);
        }
    }
    
    private void clear(StringBuilder buff, int from, int to) {
        for (int i = from; i < to; i++) {
                buff.setCharAt(i, ' ');
            }
    }
    
    private void clearAndWrite(StringBuilder buff, int from, int to, String text) {
        clear(buff, from, to);
        buff.replace(from, from + text.length(), text);
    }

    /** @DocumenLock(type=READ) */
    private void extractCssFromJSP(Document doc, StringBuilder buffer, List<OffsetRange> templatingBlocks) {
        HashMap<String, Object> state = new HashMap<String, Object>(6);
        TokenHierarchy th = TokenHierarchy.get(doc);
        TokenSequence<JspTokenId> ts = th.tokenSequence();
        ts.moveStart();
        while (ts.moveNext()) {
            Token<JspTokenId> token = ts.token();
            if (token.id() == JspTokenId.TEXT) {
                //content - suppose html :-|
                TokenSequence htmlTs = ts.embedded();
                htmlTs.moveStart();
                extractCssFromHTML(htmlTs, buffer, state);
            } else {
                //TODO hey, and what about the boundaries of css sections?????
                if (state.get(IN_STYLE) != null || state.get(IN_INLINED_STYLE) != null) {
                    //in css - do something to make the css parser happy
                    if (token.id() == JspTokenId.EL || token.id() == JspTokenId.SCRIPTLET) {
                        //expression language or java code
                        //just one token
                        int sourceStart = ts.offset();
                        int sourceEnd = ts.offset() + token.length();

                        int generatedStart = buffer.length();
                        buffer.append(token.id() == JspTokenId.EL ? EL : JAVA); //NOI18N
                        int generatedEnd = buffer.length();
                        
                        templatingBlocks.add(new OffsetRange(generatedStart, generatedEnd));
                        
                        CodeBlockData blockData = new CodeBlockData(sourceStart, sourceEnd, generatedStart,
                                generatedEnd);
                        codeBlocks.add(blockData);
                    } else if (token.id() == JspTokenId.TAG) {
                        //check if it is an open tag or singleton tag
                        String tagName = token.text().toString();
                        int sourceStart = ts.offset() - 1; //include the '<' symbol
                        StringBuilder tagBody = new StringBuilder();
                        while (ts.moveNext()) {
                            token = ts.token();
                            if (token.id() == JspTokenId.SYMBOL && "/>".equals(token.text().toString())) {
                                //singleton tag

                                //for now just ignore, do not generate anything
                                break;
                            } else if (token.id() == JspTokenId.TEXT) {
                                tagBody.append(token.text());

                            } else if (token.id() == JspTokenId.ENDTAG && token.text().toString().equals(tagName)) {
                                //body tag

                                ts.moveNext(); //jump to the closing symbol

                                //put the content to the virtual source 
                                //TODO we likely need more heuristics here since some tags may generate output, some don't
                                int sourceEnd = ts.offset() + ts.token().length();
                                int generatedStart = buffer.length();
                                buffer.append(tagBody);
                                int generatedEnd = buffer.length();
                                CodeBlockData blockData = new CodeBlockData(sourceStart, sourceEnd, generatedStart,
                                        generatedEnd);
                                codeBlocks.add(blockData);

                                break;

                            }

                        }

                    }
                }
            }
        } //end of main tokens loop



    }
}
