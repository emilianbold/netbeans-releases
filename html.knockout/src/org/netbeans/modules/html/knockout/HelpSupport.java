/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.knockout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.SyntaxAnalyzer;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import static org.netbeans.modules.html.editor.lib.api.elements.ElementType.CLOSE_TAG;
import static org.netbeans.modules.html.editor.lib.api.elements.ElementType.OPEN_TAG;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
@NbBundle.Messages("cannot_load_help=Cannot load help.")
public class HelpSupport {
    
    /**
     * Finds the "content" section of the KO binding documentation.
     */
    public static String getKnockoutDocumentationContent(String content) {
        int stripFrom = 0;
        int stripTo = content.length();
        HtmlSource source = new HtmlSource(content);
        Iterator<Element> elementsIterator = SyntaxAnalyzer.create(source).elementsIterator();
        
        boolean inContent = false;
        int depth = 0;
        elements: while (elementsIterator.hasNext()) {
            Element element = elementsIterator.next();
            switch (element.type()) {
                case OPEN_TAG:
                    OpenTag ot = (OpenTag) element;
                    if (LexerUtils.equals("div", ot.name(), true, true)) { //NOI18N
                        org.netbeans.modules.html.editor.lib.api.elements.Attribute attribute = ot.getAttribute("class"); //NOI18N
                        if (attribute != null) {
                            CharSequence unquotedValue = attribute.unquotedValue();
                            if (unquotedValue != null && LexerUtils.equals("content", unquotedValue, true, true)) { //NOI18N
                                //found the page content
                                stripFrom = element.to();
                                inContent = true;
                            }
                        }
                    }
                    if(inContent) {
                        depth++;
                    }
                    break;
                case CLOSE_TAG:
                    if(inContent) {
                        depth--;
                        if(depth == 0) {
                            //end of the content
                            stripTo = element.from();
                            break elements;
                        }
                    }
                    break;
            }
        }
        
        return content.substring(stripFrom, stripTo);
    }

    public static String loadURLContent(URL url, String charset) throws IOException {
        if (url == null) {
            return null;
        }
        InputStream is = url.openStream();
        byte buffer[] = new byte[1000];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int count;
        do {
            count = is.read(buffer);
            if (count > 0) {
                baos.write(buffer, 0, count);
            }
        } while (count > 0);

        is.close();
        String content = baos.toString(charset);
        baos.close();
        return content;
    }
    
}
