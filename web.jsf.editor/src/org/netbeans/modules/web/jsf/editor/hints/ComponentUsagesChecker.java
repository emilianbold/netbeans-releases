/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.editor.hints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.ElementUtils;
import org.netbeans.modules.html.editor.lib.api.elements.ElementVisitor;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.jsf.editor.JsfUtils;
import org.netbeans.modules.web.jsfapi.api.Attribute;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.netbeans.modules.web.jsfapi.api.LibraryComponent;
import org.netbeans.modules.web.jsfapi.api.Tag;
import org.netbeans.modules.web.jsfapi.spi.LibraryUtils;
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
        Map<String, Library> declaredLibraries = LibraryUtils.getDeclaredLibraries(result);

        CharSequence documentContent = null;
        
        //now we have all  declared component libraries
        //lets get their parse trees and check the content
        for (final String declaredLibraryNamespace : declaredLibraries.keySet()) {
            final Library lib = declaredLibraries.get(declaredLibraryNamespace);
            Node root = result.root(declaredLibraryNamespace);
            if(root == null) {
                //no parse tree for this namespace
                continue;
            }

            //get the document snapshot content if not created yet
            if (documentContent == null) {
                documentContent = getSourceText(snapshot.getSource());
            }
            final CharSequence docText = documentContent;

            ElementUtils.visitChildren(root, new ElementVisitor() {

                @Override
                public void visit(Element node) {
                    OpenTag openTag = (OpenTag)node;
                        String tagName = openTag.unqualifiedName().toString();
                        LibraryComponent component = lib.getComponent(tagName);
                        if (component == null) {
                            //error, the component doesn't exist in the library
                            Hint hint = new Hint(ERROR_RULE_BADGING,
                                    NbBundle.getMessage(HintsProvider.class, "MSG_UNKNOWN_CC_COMPONENT", lib.getDisplayName(), tagName),
                                    context.parserResult.getSnapshot().getSource().getFileObject(),
                                    JsfUtils.createOffsetRange(snapshot, docText, node.from(), node.to()),
                                    Collections.EMPTY_LIST, DEFAULT_ERROR_HINT_PRIORITY);
                            hints.add(hint);
                        } else {
                            //check the component attributes
                            Tag tag = component.getTag();
                            if (tag != null) {
                                //Check wheter the tag has some non-generic (e.g. explicitly declared) attributes
                                if(!tag.hasNonGenenericAttributes()) {
                                    //There aren't any declared attributes so we cannot do any attributes checks
                                    //since facelets allows to not to declare the attributes in the descriptor, but
                                    //use it in the facelets page. The engine then simply sets all the found 
                                    //attributes to the component without knowing if the component knows them or not.
                                    return ;
                                }
                                
                                //1. check required attributes
                                Collection<Attribute> attrs = tag.getAttributes();
                                for (Attribute attr : attrs) {
                                    if (attr.isRequired()) {
                                        if (openTag.getAttribute(attr.getName()) == null) {
                                            //missing required attribute
                                            Hint hint = new Hint(ERROR_RULE_BADGING,
                                                    NbBundle.getMessage(HintsProvider.class, "MSG_MISSING_REQUIRED_ATTRIBUTE", attr.getName()),
                                                    context.parserResult.getSnapshot().getSource().getFileObject(),
                                                    JsfUtils.createOffsetRange(snapshot, docText, node.from(), node.to()),
                                                    Collections.EMPTY_LIST, DEFAULT_ERROR_HINT_PRIORITY);
                                            hints.add(hint);
                                        }
                                    }
                                }

                                //2. check for unknown attributes
                                for (org.netbeans.modules.html.editor.lib.api.elements.Attribute nodeAttr : openTag.attributes()) {
                                    //do not check attributes with a namespace
                                    String nodeAttrName = nodeAttr.name().toString();
                                    if (nodeAttr.namespacePrefix() == null && 
					    tag.getAttribute(nodeAttrName) == null &&
					    !"xmlns".equals(nodeAttrName.toLowerCase(Locale.ENGLISH))) {
                                        //unknown attribute
                                        Hint hint = new Hint(ERROR_RULE_BADGING,
                                                    NbBundle.getMessage(HintsProvider.class, "MSG_UNKNOWN_ATTRIBUTE", nodeAttr.name(), tag.getName()),
                                                    context.parserResult.getSnapshot().getSource().getFileObject(),
                                                    JsfUtils.createOffsetRange(snapshot, docText, nodeAttr.from(), nodeAttr.to()),
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

            }, ElementType.OPEN_TAG);
        }

    }
}
