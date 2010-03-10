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
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.editor.ext.html.parser.AstNodeVisitor;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.jsf.editor.JsfSupport;
import org.netbeans.modules.web.jsf.editor.JsfUtils;
import org.netbeans.modules.web.jsf.editor.facelets.CompositeComponentLibrary.CompositeComponent;
import org.netbeans.modules.web.jsf.editor.facelets.FaceletsLibrary;
import org.netbeans.modules.web.jsf.editor.tld.TldLibrary;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
public class ComponentUsagesChecker extends HintsProvider {

    @Override
    public List<Hint> compute(RuleContext context) {
        List<Hint> hints = new ArrayList<Hint>();

        checkCCCalls(hints, context);

        return hints;
    }

    //check the component usage(call):
    // - whether the tag exists
    // - if it has all the required attributes
    // - if all used attributes are allowed
    private void checkCCCalls(final List<Hint> hints, final RuleContext context) {
        HtmlParserResult result = (HtmlParserResult) context.parserResult;
        final Snapshot snapshot = result.getSnapshot();

        //find all usages of composite components tags for this page
        Collection<String> declaredNamespaces = result.getNamespaces().keySet();
        Map<String, FaceletsLibrary> declaredLibraries = new HashMap<String, FaceletsLibrary>();
        JsfSupport jsfSupport = JsfSupport.findFor(context.doc);
        if (jsfSupport != null) {
            Map<String, FaceletsLibrary> libs = jsfSupport.getFaceletsLibraries();

            for (String namespace : declaredNamespaces) {
                FaceletsLibrary lib = libs.get(namespace);
                if (lib != null) {
    //            if(JsfUtils.isCompositeComponentLibrary(lib)) {
                    declaredLibraries.put(namespace, lib);
    //            }
                }
            }
        }

        //now we have all  declared component libraries
        //lets get their parse trees and check the content
        for (final String declaredLibraryNamespace : declaredLibraries.keySet()) {
            final FaceletsLibrary lib = declaredLibraries.get(declaredLibraryNamespace);
            AstNode root = result.root(declaredLibraryNamespace);
            if(root == null) {
                //no parse tree for this namespace
                continue;
            }

            final Document doc = snapshot.getSource().getDocument(true);
            final AtomicReference<String> docTextRef = new AtomicReference<String>();
            doc.render(new Runnable() {

                @Override
                public void run() {
                    try {
                        docTextRef.set(doc.getText(0, doc.getLength()));
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

            });
            final String docText = docTextRef.get(); //may be null if BLE happens (which is unlikely)

            AstNodeUtils.visitChildren(root, new AstNodeVisitor() {

                @Override
                public void visit(AstNode node) {
                    if (node.type() == AstNode.NodeType.OPEN_TAG) {
                        String tagName = node.getNameWithoutPrefix();
                        FaceletsLibrary.NamedComponent component = lib.getComponent(tagName);
                        if (component == null) {
                            //error, the component doesn't exist in the library
                            Hint hint = new Hint(DEFAULT_ERROR_RULE,
                                    NbBundle.getMessage(HintsProvider.class, "MSG_UNKNOWN_CC_COMPONENT", lib.getDisplayName()),
                                    context.parserResult.getSnapshot().getSource().getFileObject(),
                                    JsfUtils.createOffsetRange(snapshot, docText, node.startOffset(), node.endOffset()),
                                    Collections.EMPTY_LIST, DEFAULT_ERROR_HINT_PRIORITY);
                            hints.add(hint);
                        } else {
                            //check the component attributes
                            TldLibrary.Tag tag = component.getTag();
                            if (tag != null) {
                                //#Bug 176807 fix -  Composite component w/o interface and implementation is ignored
                                //do not do any check on a composite component w/o any interface attributes
                                if(component instanceof CompositeComponent) {
                                    if(!tag.hasNonGenenericAttributes()) {
                                        return ;
                                    }
                                }

                                //1. check required attributes
                                Collection<TldLibrary.Attribute> attrs = tag.getAttributes();
                                for (TldLibrary.Attribute attr : attrs) {
                                    if (attr.isRequired()) {
                                        if (node.getAttribute(attr.getName()) == null) {
                                            //missing required attribute
                                            Hint hint = new Hint(DEFAULT_ERROR_RULE,
                                                    NbBundle.getMessage(HintsProvider.class, "MSG_MISSING_REQUIRED_ATTRIBUTE", attr.getName()),
                                                    context.parserResult.getSnapshot().getSource().getFileObject(),
                                                    JsfUtils.createOffsetRange(snapshot, docText, node.startOffset(), node.endOffset()),
                                                    Collections.EMPTY_LIST, DEFAULT_ERROR_HINT_PRIORITY);
                                            hints.add(hint);
                                        }
                                    }
                                }

                                //2. check for unknown attributes
                                for (AstNode.Attribute nodeAttr : node.getAttributes()) {
                                    //do not check attributes with a namespace
                                    if (nodeAttr.namespacePrefix() == null && 
					    tag.getAttribute(nodeAttr.name()) == null &&
					    !"xmlns".equals(nodeAttr.name().toLowerCase(Locale.ENGLISH))) {
                                        //unknown attribute
                                        Hint hint = new Hint(DEFAULT_ERROR_RULE,
                                                    NbBundle.getMessage(HintsProvider.class, "MSG_UNKNOWN_ATTRIBUTE", nodeAttr.name()),
                                                    context.parserResult.getSnapshot().getSource().getFileObject(),
                                                    JsfUtils.createOffsetRange(snapshot, docText, nodeAttr.nameOffset(), nodeAttr.valueOffset() + nodeAttr.value().length()),
                                                    Collections.EMPTY_LIST, DEFAULT_ERROR_HINT_PRIORITY);
                                            hints.add(hint);
                                    }
                                }

                            } else {
                                //no tld, we cannot check much.
                                //btw, composite library w/o TLD simulates a TLD since can be reasonable parsed
                            }
                        }
                    }
                }
            });
        }

    }
}
