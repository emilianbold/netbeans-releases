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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.EditHistory;
import org.netbeans.modules.gsf.api.IncrementalEmbeddingModel;

/**
 * Creates a CSS model from html source code. 
 * (originally copied from JsModel from javascript.editor module written by Tor Norbye)
 * 
 * A generic implemenatation without any templating languages heuristics. 
 * Can work on top of any language embedding html code inside. It simply
 * extracts the html code using lexer API and creates a source from it.
 * 
 * @todo (marek) There is a problem how to generically resolve the templating 
 * problem - the top level(templating) language may generate parts of the code 
 * so we need to replace these by something meaningful. This impl. provides a 
 * simple mechanism how to replace this holes, but some king of plugin API 
 * is necessary here so the templating language can say what should be generated 
 * instead of the templating part.
 * 
 * @todo (tor) Preserve script includes in the index somehow so I can figure out what files are included
 * @todo (tor) Preserve DOM elements somehow
 * 
 * @author Tor Norbye, Marek Fukala
 */
public class HtmlModel {

    private static final Logger LOGGER = Logger.getLogger(HtmlModel.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    
    public static final String HTML_MIME_TYPE = "text/html"; //NOI18N
    private final Document doc;
    private final ArrayList<CodeBlockData> codeBlocks = new ArrayList<CodeBlockData>();
    private String htmlCode;
    //private String rhtmlCode; // For debugging purposes
    private boolean documentDirty = true;
    /** Caching */
    private int prevAstOffset; // Don't need to initialize: the map 0 => 0 is correct
    /** Caching */
    private int prevLexOffset;

    public static HtmlModel get(Document doc) {
        HtmlModel model = (HtmlModel) doc.getProperty(HtmlModel.class);
        if (model == null) {
            model = new HtmlModel(doc);
            doc.putProperty(HtmlModel.class, model);
        }

        return model;
    }

    HtmlModel(Document doc) {
        this.doc = doc;

        if (doc != null) { // null in some unit tests
            TokenHierarchy hi = TokenHierarchy.get(doc);
            hi.addTokenHierarchyListener(new TokenHierarchyListener() {

                public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
                    documentDirty = true;
                }
            });
        }
    }

    public String getHtmlCode() {
        if (documentDirty) {
            documentDirty = false;

            // Debugging
            //try {
            //    rhtmlCode = doc.getText(0, doc.getLength());
            //} catch (Exception e) {
            //    e.printStackTrace();
            //}
            codeBlocks.clear();
            StringBuilder buffer = new StringBuilder();

            BaseDocument d = (BaseDocument) doc;
            try {
                d.readLock();
                extractHtml(d, buffer, false);
            } finally {
                d.readUnlock();
            }
            htmlCode = buffer.toString();
        }

        if (LOG) {
            LOGGER.log(Level.FINE, "===== VIRTUAL HTML SOURCE =====");
            LOGGER.log(Level.FINE, htmlCode);
            LOGGER.log(Level.FINE, "===============================");

        }

        return htmlCode;
    }

    /**Finds first language path with HTML language at the end (e.g. text/html or text/x-jsp/text/html)
     *  
     * @DocumenLock(type=READ) */
    private LanguagePath findLanguagePath(TokenHierarchy th, String mimeType) {
        LanguagePath foundPath = null;
        Set<LanguagePath> lpaths = th.languagePaths();
        for (LanguagePath path : lpaths) {
            if (path.language(path.size() - 1).mimeType().equals(mimeType)) {
                foundPath = path;
                break;
            }
        //XXX do some testing of multiple language paths matching the criteria (e.g. html in javadoc in java in JSP).
        }
        return foundPath;
    }

    /** @return True iff we're still in the middle of an embedded token */
    //XXX this should probably depend on HTML parser result and not just on lexical analysis
    //XXX the input source should be translated source - either directly created from pure html
    // or indirectly from JSP, PHP etc..
    /** @DocumenLock(type=READ) */
    void extractHtml(Document doc, StringBuilder buffer, boolean inCss) {
        TokenHierarchy th = TokenHierarchy.get(doc);
        LanguagePath lpath = findLanguagePath(th, HTML_MIME_TYPE);
        
        //issue #127778
        if(!th.isActive() || lpath == null) {
            return;
        }
        
        List<TokenSequence> tsl = th.tokenSequenceList(lpath, 0, doc.getLength());
        Token<HTMLTokenId> htmlToken = null;
        for (TokenSequence ts : tsl) {
            ts.moveStart();

            if (ts.moveNext()) {
                if (htmlToken != null) {
                    //second or subsequent token sequence
                    int endOfLastSequence = htmlToken.offset(th) + htmlToken.length();
                    if (endOfLastSequence < ts.offset()) {
                        //if the token sequence are not continous (probably they never are, just for sure)
                        //replace the hole by some reasonable text
                        //TODO - identify the html tokens before and after the gap 
                        //and try to generate appropriate fixing code inside
                        //
                        //for example: <div id=${"myid"}/> needs to be fixed otherwise the lexing and thus
                        //parsing will faild here. In contrast to that cases like
                        // <div id="${myid}"/> or <p>${"blabla"}<p>blabla2 are ok
                    }
                }

                //beginning of the html code
                int sourceStart = ts.offset();
                int generatedStart = buffer.length();

                //reset ts to the beginning
                ts.moveStart();

                //copy the content
                while (ts.moveNext()) {
                    htmlToken = ts.token();
                    buffer.append(htmlToken.text());
                }

                int sourceEnd = htmlToken.offset(th) + htmlToken.length();
                int generatedEnd = buffer.length();

                CodeBlockData blockData = new CodeBlockData(sourceStart, sourceEnd, generatedStart,
                        generatedEnd);
                codeBlocks.add(blockData);

            }

        }

    }

    public int sourceToGeneratedPos(int sourceOffset) {
        // Caching
        if (prevLexOffset == sourceOffset) {
            return prevAstOffset;
        }
        prevLexOffset = sourceOffset;

        // TODO - second level of caching on the code block to catch
        // nearby searches

        // Not checking dirty flag here; sourceToGeneratedPos() should apply
        // to the positions as they were when we generated the ruby code

        CodeBlockData codeBlock = getCodeBlockAtSourceOffset(sourceOffset);

        if (codeBlock == null) {
            return -1; // no embedded java code at the offset
        }

        int offsetWithinBlock = sourceOffset - codeBlock.sourceStart;
        int generatedOffset = codeBlock.generatedStart + offsetWithinBlock;
        if (generatedOffset <= codeBlock.generatedEnd) {
            prevAstOffset = generatedOffset;
        } else {
            prevAstOffset = codeBlock.generatedEnd;
        }

        return prevAstOffset;
    }

    public int generatedToSourcePos(int generatedOffset) {
        // Caching
        if (prevAstOffset == generatedOffset) {
            return prevLexOffset;
        }
        prevAstOffset = generatedOffset;
        // TODO - second level of caching on the code block to catch
        // nearby searches


        // Not checking dirty flag here; generatedToSourcePos() should apply
        // to the positions as they were when we generated the ruby code

        CodeBlockData codeBlock = getCodeBlockAtGeneratedOffset(generatedOffset);

        if (codeBlock == null) {
            return -1; // no embedded java code at the offset
        }

        int offsetWithinBlock = generatedOffset - codeBlock.generatedStart;
        int sourceOffset = codeBlock.sourceStart + offsetWithinBlock;
        if (sourceOffset <= codeBlock.sourceEnd) {
            prevLexOffset = sourceOffset;
        } else {
            prevLexOffset = codeBlock.sourceEnd;
        }

        return prevLexOffset;
    }

    private CodeBlockData getCodeBlockAtSourceOffset(int offset) {
            for(int i = 0; i < codeBlocks.size(); i++) {
            CodeBlockData codeBlock = codeBlocks.get(i);
            if (codeBlock.sourceStart <= offset && codeBlock.sourceEnd > offset) {
                return codeBlock;
            } else if(codeBlock.sourceEnd == offset) {
                //test if there the following code blocks starts with the same offset
                if(i < codeBlocks.size() - 1) {
                    CodeBlockData next = codeBlocks.get(i+1);
                    if(next.sourceStart == offset) {
                        return next;
                    } else {
                        return codeBlock;
                    }
                } else {
                    //the code block is last element, return it
                    return codeBlock;
                }
            }
        }

        
        return null;
    }

    private CodeBlockData getCodeBlockAtGeneratedOffset(int offset) {
        for(int i = 0; i < codeBlocks.size(); i++) {
            CodeBlockData codeBlock = codeBlocks.get(i);
            if (codeBlock.generatedStart <= offset && codeBlock.generatedEnd > offset) {
                return codeBlock;
            } else if(codeBlock.generatedEnd == offset) {
                //test if there the following code blocks starts with the same offset
                if(i < codeBlocks.size() - 1) {
                    CodeBlockData next = codeBlocks.get(i+1);
                    if(next.generatedStart == offset) {
                        return next;
                    } else {
                        return codeBlock;
                    }
                } else {
                    //the code block is last element, return it
                    return codeBlock;
                }
            }
        }
        
        return null;
    }

    IncrementalEmbeddingModel.UpdateState incrementalUpdate(EditHistory history) {
        // Clear cache
        // prevLexOffset = prevAstOffset = 0;
        prevLexOffset = history.convertOriginalToEdited(prevLexOffset);

        int offset = history.getStart();
        int limit = history.getOriginalEnd();
        int delta = history.getSizeDelta();

        boolean codeOverlaps = false;
        for (CodeBlockData codeBlock : codeBlocks) {
            // Block not affected by move
            if (codeBlock.sourceEnd < offset) {
                continue;
            }
            if (codeBlock.sourceStart > limit) {
                codeBlock.sourceStart += delta;
                codeBlock.sourceEnd += delta;
                continue;
            }
            if (codeBlock.sourceStart <= offset && codeBlock.sourceEnd >= limit) {
                codeBlock.sourceEnd += delta;
                codeOverlaps = true;
                continue;
            }
            return IncrementalEmbeddingModel.UpdateState.FAILED;
        }

        return codeOverlaps ? IncrementalEmbeddingModel.UpdateState.UPDATED : IncrementalEmbeddingModel.UpdateState.COMPLETED;

        // TODO - do something about token hierarchy changes?
        // Not sure if anything needs to be done here -- check extractHtml and figure
        // out which tokens it depends on
    }

    private class CodeBlockData {

        /** Start of section in RHTML file */
        private int sourceStart;
        /** End of section in RHTML file */
        private int sourceEnd;
        /** Start of section in generated Js */
        private int generatedStart;
        /** End of section in generated Js */
        private int generatedEnd;

        public CodeBlockData(int sourceStart, int sourceEnd, int generatedStart, int generatedEnd) {
            this.sourceStart = sourceStart;
            this.generatedStart = generatedStart;
            this.sourceEnd = sourceEnd;
            this.generatedEnd = generatedEnd;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("CodeBlockData[");
            sb.append("\n  SOURCE(" + sourceStart + "," + sourceEnd + ")");
            //sb.append("=\"");
            //sb.append(rhtmlCode.substring(sourceStart, sourceEnd));
            //sb.append("\"");
            sb.append(",\n  HTML(" + generatedStart + "," + generatedEnd + ")");
            //sb.append("=\"");
            //sb.append(rubyCode.substring(generatedStart,generatedEnd));
            //sb.append("\"");
            sb.append("]");

            return sb.toString();
        }
    }

    // For debugging only; pass in "rubyCode" or "rhtmlCode" in JsModel to print
    //private String debugPos(String code, int pos) {
    //    if (pos == -1) {
    //        return "<-1:notfound>";
    //    }
    //
    //    int start = pos-15;
    //    if (start < 0) {
    //        start = 0;
    //    }
    //    int end = pos+15;
    //    if (end > code.length()) {
    //        end = code.length();
    //    } 
    //
    //    return code.substring(start, pos) + "^" + code.substring(pos, end);
    //}
}
