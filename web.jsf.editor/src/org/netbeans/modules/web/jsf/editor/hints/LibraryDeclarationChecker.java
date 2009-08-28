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
package org.netbeans.modules.web.jsf.editor.hints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNode.Attribute;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.editor.ext.html.parser.AstNodeVisitor;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.web.jsf.editor.JsfSupport;
import org.netbeans.modules.web.jsf.editor.facelets.FaceletsLibrary;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
public class LibraryDeclarationChecker extends HintsProvider {

    @Override
    public List<Hint> compute(RuleContext context) {
        List<Hint> hints = new ArrayList<Hint>();

        checkLibraryDeclarations(hints, context);

        return hints;
    }

    //check the namespaces declaration:
    //1. if the declared library is available
    //2. if the declared library is used + remove unused declaration hint
    //3. if there are usages of undeclared library 
    //    + hint to add the declaration (if library available)
    //        - by default prefix
    //        - or search all the libraries for such component and offer the match/es
    //
    private void checkLibraryDeclarations(final List<Hint> hints, final RuleContext context) {
        HtmlParserResult result = (HtmlParserResult) context.parserResult;

        //find all usages of composite components tags for this page
        Collection<String> declaredNamespaces = result.getNamespaces().keySet();
        Collection<FaceletsLibrary> declaredLibraries = new ArrayList<FaceletsLibrary>();
        Map<String, FaceletsLibrary> libs = JsfSupport.findFor(context.doc).getFaceletsLibraries();

        //Find the namespaces declarations itself
        //a.take the html AST
        //b.search for nodes with xmlns attribute
        //ugly, grr, the whole namespace support needs to be fixed
        final Map<String, AstNode.Attribute> namespace2Attribute = new HashMap<String, Attribute>();
        AstNode root = result.root();
        AstNodeUtils.visitChildren(root, new AstNodeVisitor() {

            public void visit(AstNode node) {
                if (node.type() == AstNode.NodeType.OPEN_TAG) {
                    //put all NS attributes to the namespace2Attribute map for #1.
                    Collection<AstNode.Attribute> nsAttrs = node.getAttributes(new AstNode.AttributeFilter() {

                        public boolean accepts(Attribute attribute) {
                            return "xmlns".equals(attribute.namespacePrefix()); //NOI18N
                        }
                    });
                    for (AstNode.Attribute attr : nsAttrs) {
                        namespace2Attribute.put(attr.unquotedValue(), attr);
                    }
                } else if (node.type() == AstNode.NodeType.UNKNOWN_TAG && node.getNamespacePrefix() != null) {
                    //3. check for undeclared components

                    //this itself means that the node is undeclared since
                    //otherwise it wouldn't appear in the pure html parse tree
                    Hint hint = new Hint(DEFAULT_ERROR_RULE,
                            NbBundle.getMessage(HintsProvider.class, "MSG_UNDECLARED_COMPONENT"), //NOI18N
                            context.parserResult.getSnapshot().getSource().getFileObject(),
                            new OffsetRange(node.startOffset(), node.startOffset() + node.name().length() + 1 /* "<".length */),
                            Collections.EMPTY_LIST, DEFAULT_ERROR_HINT_PRIORITY);
                    hints.add(hint);
                }
            }
        });

        for (String namespace : declaredNamespaces) {
            FaceletsLibrary lib = libs.get(namespace);
            if (lib != null) {
                declaredLibraries.add(lib);
            } else {
                //1. report error - missing library for the declaration
                Attribute attr = namespace2Attribute.get(namespace);
                if (attr != null) {
                    //found the declaration, mark as error
                    Hint hint = new Hint(DEFAULT_ERROR_RULE,
                            NbBundle.getMessage(HintsProvider.class, "MSG_MISSING_LIBRARY"), //NOI18N
                            context.parserResult.getSnapshot().getSource().getFileObject(),
                            new OffsetRange(attr.nameOffset(), attr.valueOffset() + attr.value().length()),
                            Collections.EMPTY_LIST, DEFAULT_ERROR_HINT_PRIORITY);
                    hints.add(hint);
                }
            }
        }

        //2. find for unused declarations
        for (FaceletsLibrary lib : declaredLibraries) {
            AstNode rootNode = result.root(lib.getNamespace());
            final int[] usages = new int[1];
            AstNodeUtils.visitChildren(rootNode, new AstNodeVisitor() {

                public void visit(AstNode node) {
                    usages[0]++;
                }
            }, AstNode.NodeType.OPEN_TAG);

            if (usages[0] == 0) {
                //unused declaration
                Attribute declAttr = namespace2Attribute.get(lib.getNamespace());
                if (declAttr != null) {
                    Hint hint = new Hint(DEFAULT_ERROR_RULE,
                            NbBundle.getMessage(HintsProvider.class, "MSG_UNUSED_LIBRARY_DECLARATION"), //NOI18N
                            context.parserResult.getSnapshot().getSource().getFileObject(),
                            new OffsetRange(declAttr.nameOffset(), declAttr.valueOffset() + declAttr.value().length()),
                            Collections.EMPTY_LIST, DEFAULT_ERROR_HINT_PRIORITY);
                    hints.add(hint);
                }
            }
        }

    }
}
