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
import org.netbeans.modules.web.jsf.editor.facelets.FaceletsLibrary;
import org.netbeans.modules.web.jsf.editor.tld.TldLibrary;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
public class JsfCompletionItem {

    //----------- Factory methods --------------
    public static JsfTag createTag(int substitutionOffset, FaceletsLibrary.NamedComponent component, String declaredPrefix, boolean autoimport) {
        return new JsfTag(substitutionOffset, component, declaredPrefix, autoimport);
    }

    public static JsfTagAttribute createAttribute(String name, int substitutionOffset, FaceletsLibrary library, TldLibrary.Tag tag, TldLibrary.Attribute attr) {
        return new JsfTagAttribute(name, substitutionOffset, library, tag, attr);
    }

    public static class JsfTag extends HtmlCompletionItem.Tag {

        private static final String BOLD_OPEN_TAG = "<b>"; //NOI18N
        private static final String BOLD_END_TAG = "</b>"; //NOI18N
        private FaceletsLibrary.NamedComponent component;
        private boolean autoimport; //autoimport (declare) the tag namespace if set to true

        public JsfTag(int substitutionOffset, FaceletsLibrary.NamedComponent component, String declaredPrefix, boolean autoimport) {
            super(generateItemText(component, declaredPrefix), substitutionOffset, null, true);
            this.component = component;
            this.autoimport = autoimport;
        }

        private static String generateItemText(FaceletsLibrary.NamedComponent component, String declaredPrefix) {
            String libraryPrefix = component.getLibrary().getDefaultPrefix();
            return (libraryPrefix != null ? libraryPrefix : declaredPrefix ) + ":" + component.getName(); //NOI18N
        }

        @Override
        protected String getRightHtmlText() {
            return "<font color=#" + (autoimport ? hexColorCode(Color.RED.darker().darker()) : hexColorCode(Color.GRAY)) + ">" + component.getLibrary().getDisplayName() + "</font>"; //NOI18N
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
                        if (result.getSnapshot().getMimeType().equals("text/html")) { //NOI18N
                            htmlRootNode[0] = AstNodeUtils.query(((HtmlParserResult)result).root(), "html"); //NOI18N
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

                                    FaceletsLibrary lib = JsfTag.this.component.getLibrary();
                                    int insertPosition = htmlRootNode[0].endOffset() - 1; //just before the closing symbol
                                    String text = (!noAttributes ? "\n" : "") + " xmlns:" + lib.getDefaultPrefix() + //NOI18N
                                            "=\"" + lib.getNamespace() + "\""; //NOI18N

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
            sb.append(getLibraryHelpHeader(component.getLibrary()));
            sb.append("<h1>"); //NOI18N
            sb.append(component.getName());
            sb.append("</h1>"); //NOI18N

            TldLibrary.Tag tag = component.getTag();
            if(tag != null) {
                //there is TLD available
                String descr = tag.getDescription();
                if(descr == null) {
                    NbBundle.getBundle(this.getClass()).getString("MSG_NO_TLD_ITEM_DESCR"); //NOI18N
                } else {
                    sb.append(descr);
                }
            } else {
                String msg = NbBundle.getBundle(this.getClass()).getString("MSG_NO_TLD"); //NOI18N
                //extract some simple info from the component
                sb.append("<table border=\"1\">"); //NOI18N
                for(String[] descr : component.getDescription()) {
                    sb.append("<tr>"); //NOI18N
                    sb.append("<td>"); //NOI18N
                    sb.append("<div style=\"font-weight: bold\">"); //NOI18N
                    sb.append(descr[0]);
                    sb.append("</div>"); //NOI18N
                    sb.append("</td>"); //NOI18N
                    sb.append("<td>"); //NOI18N
                    sb.append(descr[1]);
                    sb.append("</td>"); //NOI18N
                    sb.append("</tr>"); //NOI18N
                }                
                sb.append("</table>"); //NOI18N
                sb.append("<p style=\"color: red\">" + msg + "</p>"); //NOI18N
            }
            return sb.toString();
        }

        @Override
        public boolean hasHelp() {
            return true;
        }
    }

    public static class JsfTagAttribute extends HtmlCompletionItem.Attribute {

        private FaceletsLibrary library;
        private TldLibrary.Tag tag;
        private TldLibrary.Attribute attr;

        public JsfTagAttribute(String value, int offset, FaceletsLibrary library, TldLibrary.Tag tag, TldLibrary.Attribute attr) {
            super(value, offset, attr.isRequired(), null);
            this.library = library;
            this.tag = tag;
            this.attr = attr;
        }

        @Override
        public String getHelp() {
            StringBuffer sb = new StringBuffer();
            sb.append(getLibraryHelpHeader(library));
            sb.append("<div><b>Tag:</b> "); //NOI18N
            sb.append(tag.getName());
            sb.append("</div>"); //NOI18N
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

    private static String getLibraryHelpHeader(FaceletsLibrary library) {
        StringBuffer sb = new StringBuffer();
        sb.append("<div><b>Library:</b> "); //NOI18N
        sb.append(library.getDisplayName());
        sb.append(" ("); //NOI18N
        sb.append(library.getNamespace());
        sb.append(")</div>"); //NOI18N
        return sb.toString();

    }
}
