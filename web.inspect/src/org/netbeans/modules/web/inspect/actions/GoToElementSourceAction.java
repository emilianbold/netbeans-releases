/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.inspect.actions;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.StyledDocument;
import org.netbeans.editor.ext.html.parser.api.AstNode;
import org.netbeans.editor.ext.html.parser.api.HtmlParsingResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.UserQuestionException;
import org.openide.util.actions.NodeAction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Go to source action for DOM elements.
 *
 * @author Jan Stola
 */
public class GoToElementSourceAction extends NodeAction  {

    @Override
    protected void performAction(Node[] activatedNodes) {
        Element element = activatedNodes[0].getLookup().lookup(Element.class);
        String uriTxt = element.getOwnerDocument().getDocumentURI();
        try {
            URI uri = new URI(uriTxt);
            File file = new File(uri);
            file = FileUtil.normalizeFile(file);
            FileObject fob = FileUtil.toFileObject(file);
            Source source = Source.create(fob);
            ParserManager.parse(Collections.singleton(source), new GoToElementTask(element, fob));
        } catch (URISyntaxException ex) {
            Logger.getLogger(GoToElementSourceAction.class.getName()).log(Level.INFO, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(GoToElementSourceAction.class.getName()).log(Level.INFO, null, ex);
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 1) {
            Element element = activatedNodes[0].getLookup().lookup(Element.class);
            if (element == null) {
                return false;
            }
            String uri = element.getOwnerDocument().getDocumentURI();
            if (uri == null || !uri.startsWith("file://")) { // NOI18N
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean asynchronous() {
        return true;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(GoToElementSourceAction.class, "GoToElementSourceAction.name"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Task that jumps on the source tag of the specified element
     * in the document in the given file.
     */
    static class GoToElementTask extends UserTask {
        /** Element to jump to. */
        private Element element;
        /** File to jump into. */
        private FileObject fob;
        /**
         * Helper field storing the node that is nearest to the requested element.
         * It is used when the exact match cannot be found. This can happen
         * when the source file doesn't correspond to the owner document
         * of the element (either because the source file has been modified
         * or because the document was tweaked by JavaScript).
         */
        private AstNode fallback;

        /**
         * Creates a new {@code GoToElementTask} for the specified file and element.
         * 
         * @param element element to jump to.
         * @param fob file to jump into.
         */
        GoToElementTask(Element element, FileObject fob) {
            this.element = element;
            this.fob = fob;
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            fallback = null;
            HtmlParsingResult result = (HtmlParsingResult)resultIterator.getParserResult();
            AstNode root = result.root();
            AstNode node = findAstNode(element, root);
            if (node == null) {
                // Exact match not found, the nearest node is stored in 'fallback' field
                node = fallback;
            }
            while (node.isVirtual()) {
                node = node.parent();
            }
            final AstNode nodeToShow = node;
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    open(fob, nodeToShow.startOffset());
                }
            });
        }

        /**
         * Finds {@code AstNode} that corresponds to the specified element.
         * 
         * @param elem element for which the corresponding {@code AstNode}
         * should be found.
         * @param root root of AST tree.
         * @return {@code AstNode} that corresponds to the specified element.
         */
        private AstNode findAstNode(Element elem, AstNode root) {
            org.w3c.dom.Node node = elem.getParentNode();
            if (node != null && (node.getNodeType() == Document.ELEMENT_NODE)) {
                Element elemParent = (Element)node;
                AstNode astParent = findAstNode(elemParent, root);
                if (astParent != null) {
                    Element current = firstSubElement(elemParent);
                    String currentName = current.getTagName().toLowerCase();
                    for (AstNode child : astParent.children()) {
                        if (child.type() == AstNode.NodeType.OPEN_TAG) {
                            String astName = child.name().toLowerCase();
                            if (currentName.equals(astName)) {
                                if (current == elem) {
                                    return child;
                                } else {
                                    current = nextSiblingElement(current);
                                    currentName = current.getTagName().toLowerCase();
                                }
                            }
                        }
                    }
                    fallback = astParent;
                }
            } else {
                String elemName = elem.getTagName().toLowerCase();
                for (AstNode child : root.children()) {
                    if (child.type() == AstNode.NodeType.OPEN_TAG) {
                        String astName = child.name().toLowerCase();
                        if (elemName.equals(astName)) {
                            return child;
                        }
                    }
                }
                fallback = root;
            }
            return null;
        }

        /**
         * Returns the first sub-element of the specified element.
         * 
         * @param element element whose first sub-element should be returned.
         * @return the first sub-element of the specified element.
         */
        private Element firstSubElement(Element element) {
            NodeList list = element.getChildNodes();
            for (int i=0; i<list.getLength(); i++) {
                org.w3c.dom.Node node = list.item(i);
                if (node.getNodeType() == Document.ELEMENT_NODE) {
                    return (Element)node;
                }
            }
            return null;
        }

        /**
         * Returns the next sibling element of the specified element.
         * 
         * @param element element whose sibling should be returned.
         * @return next sibling element of the specified element.
         */
        private Element nextSiblingElement(Element element) {
            org.w3c.dom.Node current = element;
            org.w3c.dom.Node next;
            while ((next = current.getNextSibling()) != null) {
                if (next.getNodeType() == Document.ELEMENT_NODE) {
                    return (Element)next;
                }
                current = next;
            }
            return null;
        }

        /**
         * Opens the specified file at the given offset. This method has been
         * copied (with minor modifications) from UiUtils class in csl.api module.
         * 
         * @param fob file that should be opened.
         * @param offset offset where the caret should be placed.
         * @return {@code true} when the file was opened successfully,
         * returns {@code false} otherwise.
         */
        private boolean open(FileObject fob, int offset) {
            try {
                DataObject dob = DataObject.find(fob);
                Lookup dobLookup = dob.getLookup();
                EditorCookie ec = dobLookup.lookup(EditorCookie.class);
                LineCookie lc = dobLookup.lookup(LineCookie.class);
                OpenCookie oc = dobLookup.lookup(OpenCookie.class);

                if ((ec != null) && (lc != null) && (offset != -1)) {
                    StyledDocument doc;
                    try {
                        doc = ec.openDocument();
                    } catch (UserQuestionException uqe) {
                        String title = NbBundle.getMessage(
                                GoToElementSourceAction.class,
                                "GoToElementSourceAction.question"); // NOI18N
                        Object value = DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                                uqe.getLocalizedMessage(),
                                title,
                                NotifyDescriptor.YES_NO_OPTION));
                        if (value != NotifyDescriptor.YES_OPTION) {
                            return false;
                        }
                        uqe.confirmed();
                        doc = ec.openDocument();
                    }

                    if (doc != null) {
                        int line = NbDocument.findLineNumber(doc, offset);
                        int lineOffset = NbDocument.findLineOffset(doc, line);
                        int column = offset - lineOffset;
                        if (line != -1) {
                            Line l = lc.getLineSet().getCurrent(line);
                            if (l != null) {
                                l.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS, column);
                                return true;
                            }
                        }
                    }
                }

                if (oc != null) {
                    oc.open();
                    return true;
                }
            } catch (IOException ioe) {
                Logger.getLogger(GoToElementSourceAction.class.getName()).log(Level.INFO, null, ioe);
            }

            return false;
        }
        
    }

}
