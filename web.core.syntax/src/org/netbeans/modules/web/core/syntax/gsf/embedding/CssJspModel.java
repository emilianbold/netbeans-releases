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

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.html.editor.gsf.embedding.CssModel;

/**
 * Creates a CSS model from HTML source code in JSP page. 
 * 
 * @author Tor Norbye, Marek Fukala
 */
public class CssJspModel extends CssModel {

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

    private CssJspModel(Document doc) {
        super(doc);
    }

    @Override
    public String getCode() {
        if (documentDirty) {
            documentDirty = false;

            codeBlocks.clear();
            StringBuilder buffer = new StringBuilder();

            BaseDocument d = (BaseDocument) doc;
            try {
                d.readLock();
                extractCssFromJSP(doc, buffer);
            } finally {
                d.readUnlock();
            }
            code = buffer.toString();
        }

        if(LOG) {
            LOGGER.log(Level.FINE, dumpCode());
            
        }

        return code;
    }

    /** @DocumenLock(type=READ) */
    private void extractCssFromJSP(Document doc, StringBuilder buffer) {
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
                        buffer.append(token.id() == JspTokenId.EL ? "EXPRESSION_LANGUAGE" : "JAVA_CODE"); //NOI18N
                        int generatedEnd = buffer.length();
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
