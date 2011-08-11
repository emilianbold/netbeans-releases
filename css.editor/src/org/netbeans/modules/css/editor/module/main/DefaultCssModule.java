/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.editor.module.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.css.editor.Css3Utils;
import org.netbeans.modules.css.editor.csl.CssNodeElement;
import org.netbeans.modules.css.editor.module.spi.EditorFeatureContext;
import org.netbeans.modules.css.editor.module.spi.FeatureContext;
import org.netbeans.modules.css.editor.module.spi.CssModule;
import org.netbeans.modules.css.editor.module.spi.FutureParamTask;
import org.netbeans.modules.css.editor.module.spi.PropertyDescriptor;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.common.api.Pair;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 * Default implementation covering the basic CSS3 features.
 *
 * @author mfukala@netbeans.org
 */
@ServiceProvider(service = CssModule.class)
public class DefaultCssModule extends CssModule {

    private static final Pattern URI_PATTERN = Pattern.compile("url\\(\\s*(.*)\\s*\\)"); //NOI18N

    @Override
    public Collection<PropertyDescriptor> getPropertyDescriptors() {
        return DefaultProperties.properties();
    }

    @Override
    public <T extends Map<OffsetRange, Set<ColoringAttributes>>> NodeVisitor<T> getSemanticHighlightingNodeVisitor(FeatureContext context, T result) {
        final Snapshot snapshot = context.getSnapshot();
        return new NodeVisitor<T>(result) {

            @Override
            public boolean visit(Node node) {
                switch (node.type()) {
                    case elementName:
                    case cssId:
                    case cssClass:
                        int dso = snapshot.getOriginalOffset(node.from());
                        if (dso == -1) {
                            //try next offset - for virtually created class and id
                            //selectors the . an # prefix are virtual code and is not
                            //a part of the source document, try to highlight just
                            //the class or id name
                            dso = snapshot.getOriginalOffset(node.to() + 1);
                        }
                        int deo = snapshot.getOriginalOffset(node.to());
                        //filter out generated and inlined style definitions - they have just virtual selector which
                        //is mapped to empty string
                        if (dso >= 0 && deo >= 0) {
                            OffsetRange range = new OffsetRange(dso, deo);
                            getResult().put(range, ColoringAttributes.METHOD_SET);
                        }
                        break;

                    case property:
                        dso = snapshot.getOriginalOffset(node.from());
                        deo = snapshot.getOriginalOffset(node.to());
                        if (dso >= 0 && deo >= 0) { //filter virtual nodes
                            //check vendor speficic property
                            OffsetRange range = new OffsetRange(dso, deo);

                            CharSequence propertyName = node.image();
                            if (Css3Utils.containsGeneratedCode(propertyName)) {
                                return false;
                            }

                            Set<ColoringAttributes> ca;
                            if (Css3Utils.isVendorSpecificProperty(propertyName)) {
                                //special highlight for vend. spec. properties
                                ca = ColoringAttributes.CUSTOM2_SET;
                            } else {
                                //normal property
                                ca = ColoringAttributes.CUSTOM1_SET;
                            }
                            getResult().put(range, ca);

                        }
                        break;
                    case attrib_name: //attribute name in selector
                    case attrname: //attribute name in css function
                        OffsetRange range = Css3Utils.getDocumentOffsetRange(node, snapshot);
                        if (Css3Utils.isValidOffsetRange(range)) {
                            getResult().put(range, ColoringAttributes.CUSTOM1_SET);
                        
                        }

                }
                return false;
            }
        };
    }

    @Override
    public <T extends Set<OffsetRange>> NodeVisitor<T> getMarkOccurrencesNodeVisitor(EditorFeatureContext context, T result) {

        final Snapshot snapshot = context.getSnapshot();

        int astCaretOffset = snapshot.getEmbeddedOffset(context.getCaretOffset());
        if (astCaretOffset == -1) {
            return null;
        }


        Node current = NodeUtil.findNonTokenNodeAtOffset(context.getParseTreeRoot(), astCaretOffset);
        if (current == null) {
            //this may happen if the offset falls to the area outside the selectors rule node.
            //(for example when the stylesheet starts or ends with whitespaces or comment and
            //and the offset falls there).
            //In such case root node (with null parent) is returned from NodeUtil.findNodeAtOffset() 
            return null;
        }

        //process only some interesting nodes
        if (!NodeUtil.isSelectorNode(current)) {
            return null;
        }

        final NodeType nodeType = current.type();
        final CharSequence currentNodeImage = current.image();

        return new NodeVisitor<T>(result) {

            @Override
            public boolean visit(Node node) {
                if (nodeType == node.type()) {
                    boolean ignoreCase = nodeType == NodeType.hexColor;
                    if (LexerUtils.equals(currentNodeImage, node.image(), ignoreCase, false)) {

                        int[] trimmedNodeRange = NodeUtil.getTrimmedNodeRange(node);
                        int docFrom = snapshot.getOriginalOffset(trimmedNodeRange[0]);

                        //virtual class or id handling - the class and id elements inside
                        //html tag's CLASS or ID attribute has the dot or hash prefix just virtual
                        //so if we want to highlight such occurances we need to increment the
                        //start offset by one
                        if (docFrom == -1 && (node.type() == NodeType.cssClass || node.type() == NodeType.cssId)) {
                            docFrom = snapshot.getOriginalOffset(trimmedNodeRange[0] + 1); //lets try +1 offset
                        }

                        int docTo = snapshot.getOriginalOffset(trimmedNodeRange[1]);

                        if (docFrom != -1 && docTo != -1) {
                            getResult().add(new OffsetRange(docFrom, docTo));
                        }
                    }
                }

                return false;


            }
        };

    }

    @Override
    public <T extends Map<String, List<OffsetRange>>> NodeVisitor<T> getFoldsNodeVisitor(FeatureContext context, T result) {
        final Snapshot snapshot = context.getSnapshot();

        return new NodeVisitor<T>(result) {

            @Override
            public boolean visit(Node node) {
                int from = -1, to = -1;
                if (node.type() == NodeType.ruleSet) {
                    //find the ruleSet curly brackets and create the fold between them inclusive
                    Node[] tokenNodes = NodeUtil.getChildrenByType(node, NodeType.token);
                    for (Node leafNode : tokenNodes) {
                        if (CharSequenceUtilities.equals("{", leafNode.image())) {
                            from = leafNode.from();
                        } else if (CharSequenceUtilities.equals("}", leafNode.image())) {
                            to = leafNode.to();
                        }
                    }

                    if (from != -1 && to != -1) {
                        int doc_from = snapshot.getOriginalOffset(from);
                        int doc_to = snapshot.getOriginalOffset(to);

                        try {
                            //check the boundaries a bit
                            if (doc_from >= 0 && doc_to >= 0) {
                                //do not creare one line folds
                                if (LexerUtils.getLineOffset(snapshot.getText(), from)
                                        < LexerUtils.getLineOffset(snapshot.getText(), to)) {

                                    List<OffsetRange> codeblocks = getResult().get("codeblocks"); //NOI18N
                                    if (codeblocks == null) {
                                        codeblocks = new ArrayList<OffsetRange>();
                                        getResult().put("codeblocks", codeblocks);
                                    }

                                    codeblocks.add(new OffsetRange(doc_from, doc_to));
                                }
                            }
                        } catch (BadLocationException ex) {
                            //ignore
                        }
                    }
                }
                return false;
            }
        };

    }

    @Override
    public Pair<OffsetRange, FutureParamTask<DeclarationLocation, EditorFeatureContext>> getDeclaration(Document document, int caretOffset) {
        //first try to find the reference span
        TokenSequence<CssTokenId> ts = LexerUtils.getJoinedTokenSequence(document, caretOffset, CssTokenId.language());
        if (ts == null) {
            return null;
        }

        OffsetRange foundRange = null;
        Token<CssTokenId> token = ts.token();
        int quotesDiff = WebUtils.isValueQuoted(ts.token().text().toString()) ? 1 : 0;
        OffsetRange range = new OffsetRange(ts.offset() + quotesDiff, ts.offset() + ts.token().length() - quotesDiff);
        if (token.id() == CssTokenId.STRING || token.id() == CssTokenId.URI) {
            //check if there is @import token before
            if (ts.movePrevious()) {
                //@import token expected
                if (ts.token().id() == CssTokenId.IMPORT_SYM) {
                    //gotcha!
                    foundRange = range;
                }
            }
        }

        if (foundRange == null) {
            return null;
        }

        //if span found then create the future task which will finally create the declaration location
        //possibly using also parser result
        FutureParamTask<DeclarationLocation, EditorFeatureContext> callable = new FutureParamTask<DeclarationLocation, EditorFeatureContext>() {

            @Override
            public DeclarationLocation run(EditorFeatureContext context) {
                final TokenSequence<CssTokenId> ts = LexerUtils.getJoinedTokenSequence(context.getDocument(), context.getCaretOffset(), CssTokenId.language());
                if (ts == null) {
                    return null;
                }

                Token<CssTokenId> valueToken = ts.token();
                String valueText = valueToken.text().toString();

                //adjust the value if a part of an URI
                if (valueToken.id() == CssTokenId.URI) {
                    Matcher m = URI_PATTERN.matcher(valueToken.text());
                    if (m.matches()) {
                        int groupIndex = 1;
                        valueText = m.group(groupIndex);
                    }
                }

                valueText = WebUtils.unquotedValue(valueText);

                FileObject resolved = WebUtils.resolve(context.getSource().getFileObject(), valueText);
                return resolved != null
                        ? new DeclarationLocation(resolved, 0)
                        : null;


            }
        };

        return new Pair<OffsetRange, FutureParamTask<DeclarationLocation, EditorFeatureContext>>(foundRange, callable);
    }

    @Override
    public <T extends List<StructureItem>> NodeVisitor<T> getStructureItemsNodeVisitor(FeatureContext context, T result) {

        final List<StructureItem> items = new ArrayList<StructureItem>();
        result.add(new TopLevelStructureItem.Rules(items));

        final Snapshot snapshot = context.getSnapshot();

        return new NodeVisitor<T>() {

            @Override
            public boolean visit(Node node) {
                if (node.type() == NodeType.selectorsGroup) {
                    //get parent - ruleSet to obtain the { ... } range 
                    Node ruleNode = node.parent();
                    assert ruleNode.type() == NodeType.ruleSet;

                    int so = snapshot.getOriginalOffset(ruleNode.from());
                    int eo = snapshot.getOriginalOffset(ruleNode.to());
                    if (eo > so) {
                        //todo: filter out virtual selectors
                        StructureItem item = new CssRuleStructureItem(node.image(), CssNodeElement.createElement(ruleNode), snapshot);
                        items.add(item);
                    }
                }
                return false;
            }
        };

    }
}
