/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.editor.completion;

import java.awt.Color;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.html.editor.completion.HtmlCompletionItem;
import org.netbeans.modules.html.editor.gsf.api.HtmlParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.web.jsf.editor.tld.TldLibrary;

/**
 *
 * @author marekfukala
 */
public class JsfCompletionItem {

    //----------- Factory methods --------------
    public static JsfTag createTag(String name, int substitutionOffset, TldLibrary.Tag tag, TldLibrary library, boolean autoimport) {
        return new JsfTag(name, substitutionOffset, tag, library, autoimport);
    }

    public static JsfTagAttribute createAttribute(String name, int substitutionOffset, TldLibrary library, TldLibrary.Tag tag, TldLibrary.Attribute attr) {
        return new JsfTagAttribute(name, substitutionOffset, library, tag, attr);
    }

    public static class JsfTag extends HtmlCompletionItem.Tag {

        private static final String BOLD_OPEN_TAG = "<b>"; //NOI18N
        private static final String BOLD_END_TAG = "</b>"; //NOI18N
        private TldLibrary library;
        private TldLibrary.Tag tag;
        private boolean autoimport; //autoimport (declare) the tag namespace if set to true

        public JsfTag(String text, int substitutionOffset, TldLibrary.Tag tag, TldLibrary library, boolean autoimport) {
            super(text, substitutionOffset, null, true);
            this.library = library;
            this.tag = tag;
            this.autoimport = autoimport;
        }

        @Override
        protected String getRightHtmlText() {
            return "<font color=#" + (autoimport ? hexColorCode(Color.RED.darker().darker()) : hexColorCode(Color.GRAY)) + ">" + library.getDisplayName() + "</font>";
        }

        @Override
        public void defaultAction(JTextComponent component) {
            super.defaultAction(component);
            if (autoimport) {
                autoimportLibrary(component);
            }
        }

        //XXX document vs parser infr. locking - how to modify document from a usertask???????????
        //now I just feel lucky and do not lock the document. 
        private void autoimportLibrary(JTextComponent component) {
            try {
                final BaseDocument doc = (BaseDocument) component.getDocument();
                Source source = Source.create(doc);
                final AstNode[] htmlRootNode = new AstNode[1];
                ParserManager.parse(Collections.singleton(source), new UserTask() {

                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        //suppose we are always top level
                        Result result = resultIterator.getParserResult(substitutionOffset);
                        if (result.getSnapshot().getMimeType().equals("text/html")) {
                            htmlRootNode[0] = AstNodeUtils.query(((HtmlParserResult)result).root(), "html");
                        }
                    }
                });
                //TODO reformat
                //TODO decide whether to add a new line before or not based on other attrs - could be handled by the formatter!?!?!
                if (htmlRootNode[0] != null) {

                    final Indent indent = Indent.get(doc);
                    indent.lock();
                    try {
                        doc.runAtomic(new Runnable() {
                            public void run() {
                                try {
                                    boolean noAttributes = htmlRootNode[0].getAttributeKeys().isEmpty();
                                    //if there are no attributes, just add the new one at the end of the tag,
                                    //if there are some, add the new one on a new line and reformat the tag

                                    int insertPosition = htmlRootNode[0].endOffset() - 1; //just before the closing symbol
                                    String text = (!noAttributes ? "\n" : "") + " xmlns:" + JsfTag.this.library.getDefaultPrefix() +
                                            "=\"" + JsfTag.this.library.getURI() + "\"";

                                    doc.insertString(insertPosition, text, null);

                                    if(!noAttributes) {
                                        //reformat the tag so the new attribute gets aligned with the previous one/s
                                        int newRootNodeEndOffset = htmlRootNode[0].endOffset() + text.length();
                                        indent.reindent(insertPosition, newRootNodeEndOffset);
                                    }
                                } catch (BadLocationException ex) {
                                    Logger.global.log(Level.INFO, null, ex);
                                }
                            }
                        });
                    } finally {
                        indent.unlock();
                    }
                } else {
                    //TODO create the root node???
                }
            } catch (ParseException ex) {
                Logger.global.log(Level.INFO, null, ex);
            }
        }

        //use bold font
        @Override
        protected String getLeftHtmlText() {
            StringBuffer buff = new StringBuffer();
            buff.append(BOLD_OPEN_TAG);
            buff.append(super.getLeftHtmlText());
            buff.append(BOLD_END_TAG);
            return buff.toString();
        }

        @Override
        public int getSortPriority() {
            return DEFAULT_SORT_PRIORITY - 5;
        }

        @Override
        public String getHelp() {
            StringBuffer sb = new StringBuffer();
            sb.append(getLibraryHelpHeader(library));
            sb.append("<h1>");
            sb.append(tag.getName());
            sb.append("</h1>");
            sb.append(tag.getDescription());
            return sb.toString();
        }

        @Override
        public boolean hasHelp() {
            return this.tag.getDescription() != null;
        }
    }

    public static class JsfTagAttribute extends HtmlCompletionItem.Attribute {

        private TldLibrary library;
        private TldLibrary.Tag tag;
        private TldLibrary.Attribute attr;

        public JsfTagAttribute(String value, int offset, TldLibrary library, TldLibrary.Tag tag, TldLibrary.Attribute attr) {
            super(value, offset, attr.isRequired(), null);
            this.library = library;
            this.tag = tag;
            this.attr = attr;
        }

        @Override
        public String getHelp() {
            StringBuffer sb = new StringBuffer();
            sb.append(getLibraryHelpHeader(library));
            sb.append("<div><b>Tag:</b> ");
            sb.append(tag.getName());
            sb.append("</div>");
            sb.append("<h1>"); //NOI18N
            sb.append(attr.getName());
            sb.append("</h1>"); //NOI18N
            sb.append(attr.getDescription());
            return sb.toString();
        }

        @Override
        public boolean hasHelp() {
            return attr.getDescription() != null;
        }
    }

    private static String getLibraryHelpHeader(TldLibrary library) {
        StringBuffer sb = new StringBuffer();
        sb.append("<div><b>Library:</b> "); //NOI18N
        sb.append(library.getDisplayName());
        sb.append(" ("); //NOI18N
        sb.append(library.getURI());
        sb.append(")</div>"); //NOI18N
        return sb.toString();

    }
}
