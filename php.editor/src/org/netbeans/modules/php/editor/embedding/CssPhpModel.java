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
package org.netbeans.modules.php.editor.embedding;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.html.editor.gsf.embedding.CssModel;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;

/**
 * Creates a CSS model from HTML source code in PHP page. 
 * 
 * @author Tor Norbye, Marek Fukala
 */
public class CssPhpModel extends CssModel {

    private static final Logger LOGGER = Logger.getLogger(CssPhpModel.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);

    public static CssPhpModel get(Document doc) {
        CssPhpModel model = (CssPhpModel) doc.getProperty(CssPhpModel.class);
        if (model == null) {
            model = new CssPhpModel(doc);
            doc.putProperty(CssPhpModel.class, model);
        }

        return model;
    }

    private CssPhpModel(Document doc) {
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
                extractCssFromPHP(doc, buffer);
            } finally {
                d.readUnlock();
            }
            code = buffer.toString();
        }

        if (LOG) {
            LOGGER.log(Level.FINE, dumpCode());
        }
        
        dumpCode();

        return code;
    }

    /** @DocumenLock(type=READ) */
    private void extractCssFromPHP(Document doc, StringBuilder buffer) {
        HashMap<String, Object> state = new HashMap<String, Object>(6);
        TokenHierarchy<?> th = TokenHierarchy.get(doc);
        TokenSequence<PHPTokenId> ts = th.tokenSequence(PHPTokenId.language());
        if(ts == null) {
            //token hierarchy inactive???
            return ;
        }
        ts.moveStart();
        while (ts.moveNext()) {
            Token<PHPTokenId> token = ts.token();
            if (token.id() == PHPTokenId.T_INLINE_HTML) {
                TokenSequence<HTMLTokenId> htmlTs = ts.embedded(HTMLTokenId.language());
                assert htmlTs != null;
                htmlTs.moveStart();
                extractCssFromHTML(htmlTs, buffer, state);
            } else {
                //TODO hey, and what about the boundaries of css sections?????
                if (state.get(IN_STYLE) != null || state.get(IN_INLINED_STYLE) != null) {
                    //in css - do something to make the css parser happy

                    //replace the php content by some meaningful css code

                    //simple version:
                    //1. find the end of php section
                    //2. put PHP_CODE into the css source for the block of php code 
                    int sourceStart = ts.offset();
                    while (ts.moveNext()) {
                        token = ts.token();

                        if (token.id() == PHPTokenId.T_INLINE_HTML) {
                            //end of the php block
                            ts.movePrevious();

                            int sourceEnd = ts.offset() + ts.token().length();
                            int generatedStart = buffer.length();
                            buffer.append("PHP_CODE");
                            int generatedEnd = buffer.length();
                            CodeBlockData blockData = new CodeBlockData(sourceStart, sourceEnd, generatedStart,
                                    generatedEnd);
                            codeBlocks.add(blockData);

                            break;

                        }
                    }
                }


            }
        } //end of main tokens loop



    }
}
