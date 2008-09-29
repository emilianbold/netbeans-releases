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
package org.netbeans.modules.html.editor.gsf.embedding;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.css.gsf.api.CssEmbeddingModelUtils;
import org.netbeans.modules.css.parser.CSSParserTreeConstants;
import org.netbeans.modules.css.parser.CssParserAccess;
import org.netbeans.modules.css.parser.NodeVisitor;
import org.netbeans.modules.css.parser.SimpleNode;
import org.netbeans.modules.css.parser.SimpleNodeUtil;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 * Creates a CSS model from HTML source code in JSP page. 
 * 
 * @author Tor Norbye, Marek Fukala
 */
public class CssTemplatedModel extends CssModel {

    private static final Logger LOGGER = Logger.getLogger(CssTemplatedModel.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);

    public static CssTemplatedModel get(Document doc) {
        CssTemplatedModel model = (CssTemplatedModel) doc.getProperty(CssTemplatedModel.class);
        if (model == null) {
            model = new CssTemplatedModel(doc);
            doc.putProperty(CssTemplatedModel.class, model);
        }

        return model;
    }

    private CssParserAccess.CssParserResult cachedParserResult = null;
    
    private CssTemplatedModel(Document doc) {
        super(doc);
    }

    public CssParserAccess.CssParserResult getCachedParserResult() {
        if(!documentDirty) {
            return cachedParserResult;
        } else {
            return null;
        }
    }
    
    @Override
    public String getCode() {
        if (documentDirty) {
            cachedParserResult = null;
            
            long a = System.currentTimeMillis();
            
            documentDirty = false;

            codeBlocks.clear();
            StringBuilder buffer = new StringBuilder();

            BaseDocument d = (BaseDocument) doc;
            try {
                d.readLock();
                List<OffsetRange> generated = new  ArrayList<OffsetRange>();
                extractCssFromTemplatedHTML(doc, buffer, generated);

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

    private void sanitizeCode(final StringBuilder buff, final List<OffsetRange> templatingBlocks) {
        
        final boolean[] cleared = new boolean[1];
            
            NodeVisitor visitor = new NodeVisitor() {

                public void visit(SimpleNode node) {
                    if (node.kind() == CSSParserTreeConstants.JJTERROR_SKIPBLOCK || node.kind() == CSSParserTreeConstants.JJTERROR_SKIPDECL) {
                        SimpleNode parent = (SimpleNode) node.jjtGetParent();

                        LOGGER.log(Level.FINE, "Tree Error  on " + node + "; parent: " + parent);

                        if (parent.kind() == CSSParserTreeConstants.JJTDECLARATION) {
                            //possibly clear the declaration even there is no generated code inside
                            //the error may be caused by previous incorrectly fixed declaration
                            boolean fixesInPreviousDeclaration = false;
                            SimpleNode siblingBefore = SimpleNodeUtil.getSibling(parent, true);
                            if(siblingBefore != null && siblingBefore.kind() == CSSParserTreeConstants.JJTDECLARATION) {
                                //force clear if there was fixes in the previous declaration
                                fixesInPreviousDeclaration = containsGeneratedCode(siblingBefore, buff);
                            }

                            if(clearNode(parent, buff, 0, 0, templatingBlocks, fixesInPreviousDeclaration, true)) {
                                cleared[0] = true;
                            }
                        }
                        if (parent.kind() == CSSParserTreeConstants.JJTSTYLERULE) {
                            SimpleNode siblingBefore = SimpleNodeUtil.getSibling(node, true);
                            if (siblingBefore.kind() == CSSParserTreeConstants.JJTREPORTERROR) {
                                siblingBefore = SimpleNodeUtil.getSibling(siblingBefore, true);
                                if (siblingBefore.kind() == CSSParserTreeConstants.JJTDECLARATION) {
                                    boolean modif = clearNode(siblingBefore, buff, 0, 0, templatingBlocks, false, false); //clear the last declaration node
                                    if (modif) {
                                        clearNode(node, buff, 0, -1, templatingBlocks, true, false); //clear the skipblock itself, exclude closing symbol
                                        cleared[0] = true;
                                    }
                                } else if(siblingBefore.kind() == CSSParserTreeConstants.JJTSELECTORLIST) {
                                    //error in selector list
                                    //repair the code from the selector list beginning to first left curly bracket
                                    int from = siblingBefore.startOffset();
                                    int curlyBracketIndex = buff.indexOf("{", from);
                                    if(curlyBracketIndex == -1) {
                                        //no curly bracket found - this likely means that we are at the end of the css
                                        //code and the bracket is simply missing
                                        curlyBracketIndex = buff.length();
                                    }
                                        
                                    //test if there is a generated virtual code
                                    String selectorListText = buff.substring(from, curlyBracketIndex);
                                    int idx = selectorListText.indexOf(CssEmbeddingModelUtils.getGeneratedCodeIdentifier());
                                    if(idx >= 0) {
                                        StringBuilder text = new StringBuilder(selectorListText);
                                        //remove all semicolons in the text - just the generated identifier(s) will be left
                                        String fixedText = text.toString().replace(';', 'E');
                                        clearAndWrite(buff, from, curlyBracketIndex, fixedText);
                                        cleared[0] = true;
                                    }

                                    
                                }
                            }
                        }


                    }
                }
            };

            long startTime = System.currentTimeMillis();
            
            //parse the buffer until the templating issue gets fixed.
            int i = 0;
            for(; i < 4; i++) {
                cleared[0] = false;
                //parse the buffer
                CssParserAccess parserAccess = CssParserAccess.getDefault();
                CssParserAccess.CssParserResult result = parserAccess.parse(new StringReader(buff.toString()));
                
                SimpleNode root = result.root();
                if(root == null) {
                    //unrecoverable parsing error
                    return;
                }
                
                if(LOG) {
                    LOGGER.fine("> SANITIZING LEVEL #" + i + " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                    LOGGER.fine(buff.toString());
                    LOGGER.fine("------------------------");
                    LOGGER.fine(root.dump(""));
                    LOGGER.fine("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
                }
                
                root.visitChildren(visitor);

                if(!cleared[0]) {
                    //cache the parser result
                    cachedParserResult = result;
                    //source checking finished without any correction => finish
                    break;
                }
            }
            
            if(cleared[0]) {
                LOGGER.warning("CSS source sanitization didn't success even after four passes!");
            }
            
            long endTime = System.currentTimeMillis();
            DataObject od = (DataObject)doc.getProperty(doc.StreamDescriptionProperty);
            FileObject fo = null;
            if(od != null) {
                fo = od.getPrimaryFile();
            }
            Logger.getLogger("TIMER").log(Level.FINE, "CSS Sanitizing [" + i + "]",
                    new Object[] {fo, endTime - startTime});                
            
        

    }
    
    private boolean clearNode(SimpleNode node, StringBuilder buff, 
            int startDelta, int endDelta, 
            List<OffsetRange> templatingBlocks,
            boolean forceClear, boolean wholeLine) {
        int from = node.startOffset();
        int to = node.endOffset();
        
        if(from >= to) {
            System.err.println("clearNode from >= to! node: " + node);
            return false;
        }
        
        if(wholeLine) {
            //find line start and end
            int linestart = from;
            while(linestart >= 0) {
                char ch = buff.charAt(linestart);
                if(ch == '\n') {
                    break;
                } else {
                    linestart--;
                }
            }
            
            int lineend = to;
            while(lineend < buff.length()) {
                char ch = buff.charAt(lineend);
                
                if(ch == '\n') {
                    break;
                } else {
                    lineend++;
                }
            }
            
            from = linestart;
            to = lineend;
        }
        
        
        from += startDelta;
        to += endDelta;

        if(from >= to) {
            System.err.println("clearNode from+startDelta >= to+endDelta! node: " + node);
            return false;
        }
        
        if(forceClear || containsGeneratedCode(node, buff)) {
            LOGGER.log(Level.FINE, "CLEARING NODE " + node + " [" + buff.substring(from, to) + "]");
            clear(buff, from, to);
            return true;
        }
        return false;
    }
    
    private boolean containsGeneratedCode(SimpleNode node, StringBuilder buff) {
        int from = node.startOffset();
        int to = node.endOffset();

        //fast hack, I should rather use the templating ranges
        return CssEmbeddingModelUtils.containsGeneratedCode(buff.substring(from, to));
    }
    
    private void clear(StringBuilder buff, int from, int to) {
        assert from < to;
        for (int i = from; i < to; i++) {
                buff.setCharAt(i, ' ');
            }
    }
    
    private void clearAndWrite(StringBuilder buff, int from, int to, String text) {
        LOGGER.log(Level.FINE, "CLEARING&&REPLACING [" + buff.substring(from, to) + "]: " + text);
        clear(buff, from, to);
        buff.replace(from, from + text.length(), text);
    }

    private LanguagePath findHtmlPath(TokenHierarchy th) {
        Set<LanguagePath> paths = th.languagePaths();
        for(LanguagePath path : paths) {
            if(path.innerLanguage() == HTMLTokenId.language()) {
                return path;
            }
//            for(int i = 0; i < path.size(); i++) {
//                if(path.language(i) == HTMLTokenId.language()) {
//                    return path;
//                }
//            }
        }
        return null;
    }
    
    /** @DocumenLock(type=READ) */
    private void extractCssFromTemplatedHTML(Document doc, StringBuilder buffer, List<OffsetRange> templatingBlocks) {
        HashMap<String, Object> state = new HashMap<String, Object>(6);
        TokenHierarchy th = TokenHierarchy.get(doc);
        LanguagePath htmlPath = findHtmlPath(th);
        if (htmlPath == null) {
            return; //no html content
        }

        List<TokenSequence> tslist = th.tokenSequenceList(htmlPath, 0, Integer.MAX_VALUE);
        TokenSequence last = null;
        for (TokenSequence ts : tslist) {
            ts.moveStart();
            
            if(!ts.moveNext()) {
                //this should not happen - a token sequence without any token
                continue;
            }
            
            if (last != null) {
                //there has been an html sequence before, we need to replace 
                //the "hole" between the html sequences by some reasonable content

                if (state.get(IN_STYLE) != null || state.get(IN_INLINED_STYLE) != null) {
                    //in css - do something to make the css parser happy

                    //expression language or java code
                    //just one token
                    int sourceStart = last.offset() + last.token().length();
                    int sourceEnd = ts.offset();

                    int generatedStart = buffer.length();
                    buffer.append(CssEmbeddingModelUtils.getGeneratedCodeIdentifier()); //NOI18N
                    int generatedEnd = buffer.length();

                    templatingBlocks.add(new OffsetRange(generatedStart, generatedEnd));

                    CodeBlockData blockData = new CodeBlockData(sourceStart, sourceEnd, generatedStart,
                            generatedEnd);
                    codeBlocks.add(blockData);
                    
                }
            }
            
            ts.moveStart(); //respsition at the beginning since the extractCssFromHTML supposes this state
            extractCssFromHTML(ts, buffer, state);
            last = ts;

        }
    }
        
}
