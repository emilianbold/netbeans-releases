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

import java.text.AttributedCharacterIterator.Attribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.editor.ext.html.parser.AstNodeVisitor;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.web.jsf.editor.JsfSupport;
import org.netbeans.modules.web.jsf.editor.JsfUtils;
import org.netbeans.modules.web.jsf.editor.facelets.CompositeComponentLibrary;
import org.netbeans.modules.web.jsf.editor.facelets.FaceletsLibrary;
import org.netbeans.modules.web.jsf.editor.tld.TldLibrary;
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

        //find all usages of composite components tags for this page
        Collection<String> declaredNamespaces = result.getNamespaces().keySet();
        Collection<FaceletsLibrary> declaredLibraries = new ArrayList<FaceletsLibrary>();
        Map<String, FaceletsLibrary> libs = JsfSupport.findFor(context.doc).getFaceletsLibraries();

        for (String namespace : declaredNamespaces) {
            FaceletsLibrary lib = libs.get(namespace);
            if(lib != null) {
//            if(JsfUtils.isCompositeComponentLibrary(lib)) {
                declaredLibraries.add(lib);
//            }
            }
        }

        //now we have all  declared component libraries
        //lets get their parse trees and check the content
        for (final FaceletsLibrary lib : declaredLibraries) {
            AstNode root = result.root(lib.getNamespace());

            AstNodeUtils.visitChildren(root, new AstNodeVisitor() {

                public void visit(AstNode node) {
                    if (node.type() == AstNode.NodeType.OPEN_TAG) {
                        String tagName = node.getNameWithoutPrefix();
                        FaceletsLibrary.NamedComponent component = lib.getComponent(tagName);
                        if (component == null) {
                            //error, the component doesn't exist in the library
                            Hint hint = new Hint(DEFAULT_ERROR_RULE,
                                    NbBundle.getMessage(HintsProvider.class, "MSG_UNKNOWN_CC_COMPONENT", lib.getDisplayName()),
                                    context.parserResult.getSnapshot().getSource().getFileObject(),
                                    new OffsetRange(node.startOffset(), node.endOffset()),
                                    Collections.EMPTY_LIST, DEFAULT_ERROR_HINT_PRIORITY);
                            hints.add(hint);
                        } else {
                            //check the component attributes
                            TldLibrary.Tag tag = component.getTag();
                            if (tag != null) {
                                Collection<TldLibrary.Attribute> attrs = tag.getAttributes();
                                for (TldLibrary.Attribute attr : attrs) {
                                    //1. check required attributes
                                    if (attr.isRequired()) {
                                        if (node.getAttribute(attr.getName()) == null) {
                                            //missing required attribute
                                            Hint hint = new Hint(DEFAULT_ERROR_RULE,
                                                    NbBundle.getMessage(HintsProvider.class, "MSG_MISSING_REQUIRED_ATTRIBUTE", attr.getName()),
                                                    context.parserResult.getSnapshot().getSource().getFileObject(),
                                                    new OffsetRange(node.startOffset(), node.endOffset()),
                                                    Collections.EMPTY_LIST, DEFAULT_ERROR_HINT_PRIORITY);
                                            hints.add(hint);
                                        }
                                    }

                                    //2. check for unknown attributes ... we need to fix AstNode
                                    //so we can get offsets of the attributes!!!!!
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
