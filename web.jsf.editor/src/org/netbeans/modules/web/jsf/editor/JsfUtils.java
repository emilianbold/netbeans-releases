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
package org.netbeans.modules.web.jsf.editor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.html.editor.api.Utils;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.jsf.editor.facelets.CompositeComponentLibrary;
import org.netbeans.modules.web.jsf.editor.facelets.FaceletsLibrary;

/**
 *
 * @author marekfukala
 */
public class JsfUtils {

    public static final String COMPOSITE_LIBRARY_NS = "http://java.sun.com/jsf/composite"; //NOI18N
    public static final String XHTML_NS = "http://www.w3.org/1999/xhtml"; //NOI18N

    public static boolean isCompositeComponentLibrary(FaceletsLibrary library) {
        return library instanceof CompositeComponentLibrary;
    }

    public static String importLibrary(Document document, final FaceletsLibrary library, final String prefix) {
        assert document instanceof BaseDocument;

        final String prefixToDeclare = prefix != null ? prefix : library.getDefaultPrefix();

        if (prefixToDeclare == null) {
            //cannot get prefix to declare
            return null;
        }

        final String[] declaredPrefix = new String[1];
        final BaseDocument bdoc = (BaseDocument) document;
        try {
            Source source = Source.create(bdoc);
            final HtmlParserResult[] _result = new HtmlParserResult[1];
            ParserManager.parse(Collections.singleton(source), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    ResultIterator ri = Utils.getResultIterator(resultIterator, HtmlKit.HTML_MIME_TYPE);
                    if (ri != null) {
                        _result[0] = (HtmlParserResult) ri.getParserResult();
                    }
                }
            });

            if (_result[0] == null) {
                //no html code
                return null;
            }
            //try find the html root node first
            final HtmlParserResult result = _result[0];
            AstNode root = null;
            //no html root node, we need to find a root node of some other ast tree
            //belonging to some namespace
            for (AstNode r : result.roots().values()) {
                //find first open tag node

                List<AstNode> chs = r.children(new AstNode.NodeFilter() {

                    public boolean accepts(AstNode node) {
                        return (node.type() == AstNode.NodeType.OPEN_TAG ||
                                node.type() == AstNode.NodeType.UNKNOWN_TAG) && !node.isEmpty();
                    }
                    
                });

                if (!chs.isEmpty()) {
                    AstNode top = chs.get(0);
                    if (root == null) {
                        root = top;
                    } else {
                        if (top.startOffset() < root.startOffset()) {
                            root = top;
                        }
                    }
                }
            }


            final AstNode rootNode = root;
            if (rootNode == null) {
                //TODO we may want to add a root node in such case
                return null;
            }

            //TODO decide whether to add a new line before or not based on other attrs - could be handled by the formatter!?!?!
            //first check if the library is already declared

            //XXX please note that the htmlresult.getNamespaces() returns a context free namespaces
            //declarations which is wrong. If any of the nested elements declares the namespace
            //the namespaces map will contain it and we will not add a new declaration which will
            //result into an invalid page

            //namespace to declared prefix map
            Map<String, String> declaredNamespaces = result.getNamespaces();
            String alreadyDeclaredPrefix = declaredNamespaces.get(library.getNamespace());
            if(alreadyDeclaredPrefix == null) {
                //try composite component library default prefix
                if(library instanceof CompositeComponentLibrary) {
                    String defaultNS = ((CompositeComponentLibrary)library).getDefaultNamespace();
                    alreadyDeclaredPrefix = declaredNamespaces.get(defaultNS);
                }
            }

            if(alreadyDeclaredPrefix != null) {
                //already declared
                return alreadyDeclaredPrefix;
            }

            //else add the declaration

            final Indent indent = Indent.get(bdoc);
            indent.lock();
            try {
                bdoc.runAtomic(new Runnable() {

                    public void run() {
                        try {
                            boolean noAttributes = rootNode.getAttributeKeys().isEmpty();
                            //if there are no attributes, just add the new one at the end of the tag,
                            //if there are some, add the new one on a new line and reformat the tag

                            int insertPosition = result.getSnapshot().getOriginalOffset(rootNode.endOffset() - 1); //just before the closing symbol
                            if(insertPosition == -1) {
                                //cannot be mapped to the document for some reason, just ignore
                                return ;
                            }
                            String text = (!noAttributes ? "\n" : "") + " xmlns:" + prefixToDeclare + //NOI18N
                                    "=\"" + library.getNamespace() + "\""; //NOI18N

                            bdoc.insertString(insertPosition, text, null);

                            if (!noAttributes) {
                                //reformat the tag so the new attribute gets aligned with the previous one/s
                                int newRootNodeEndOffset = rootNode.endOffset() + text.length();
                                indent.reindent(insertPosition, newRootNodeEndOffset);
                            }

                            declaredPrefix[0] = prefixToDeclare;
                        } catch (BadLocationException ex) {
                            Logger.global.log(Level.INFO, null, ex);
                        }
                    }
                });
            } finally {
                indent.unlock();
            }
        } catch (ParseException ex) {
            Logger.global.log(Level.INFO, null, ex);
        }

        return declaredPrefix[0];
    }

    /**
     * Creates an OffsetRange of source document offsets for given embedded offsets.
     */
    public static OffsetRange createOffsetRange(Snapshot snapshot, int embeddedOffsetFrom, int embeddedOffsetTo) {
        Document doc = snapshot.getSource().getDocument(false);
        assert doc != null;

        int originalFrom = 0;
        int originalTo = 0;

        //try to find nearest original offset if the embedded offsets cannot be directly recomputed
        //from - try backward
        for (int i = embeddedOffsetFrom; i >= 0; i--) {
            int originalOffset = snapshot.getOriginalOffset(i);
            if (originalOffset != -1) {
                originalFrom = originalOffset;
                break;
            }
        }

        //to - try forward
        for (int i = embeddedOffsetTo; i < snapshot.getText().length(); i++) {
            int originalOffset = snapshot.getOriginalOffset(i);
            if (originalOffset != -1) {
                originalTo = originalOffset;
                break;
            }
        }

        return new OffsetRange(originalFrom, originalTo);
    }
}
