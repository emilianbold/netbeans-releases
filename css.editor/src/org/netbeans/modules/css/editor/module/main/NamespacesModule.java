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
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.lexer.Token;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.css.editor.Css3Utils;
import org.netbeans.modules.css.editor.csl.CssElement;
import org.netbeans.modules.css.editor.module.spi.CompletionContext;
import org.netbeans.modules.css.editor.module.spi.CssCompletionItem;
import org.netbeans.modules.css.editor.module.spi.CssModule;
import org.netbeans.modules.css.editor.module.spi.EditorFeatureContext;
import org.netbeans.modules.css.editor.module.spi.FeatureContext;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.netbeans.modules.css.lib.api.model.Namespace;
import org.netbeans.modules.css.lib.api.model.Stylesheet;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mfukala@netbeans.org
 */
@ServiceProvider(service = CssModule.class)
public class NamespacesModule extends CssModule {

    private static final String NAMESPACE_KEYWORD = "@namespace";//NOI18N
    static ElementKind NAMESPACE_ELEMENT_KIND = ElementKind.GLOBAL; //XXX fix CSL

    @Override
    public List<CompletionProposal> getCompletionProposals(CompletionContext context) {
        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();
        Node activeNode = context.getActiveNode();
        boolean isError = activeNode.type() == NodeType.error;
        if (isError) {
            activeNode = activeNode.parent();
        }

        switch (activeNode.type()) {
            case namespace_prefix:
            case elementName:
                //already in the prefix

                //todo: rewrite to use index later
                Stylesheet model = context.getParserResult().getModel();
                for (Namespace ns : model.getNamespaces()) {
                    proposals.add(new NamespaceCompletionItem(ns.getPrefix().toString(), ns.getResourceIdentifier().toString(), context.getAnchorOffset()));
                }
                break;

            case root:
            case styleSheet:
            case bodylist:
                CompletionProposal nsKeywordProposal =
                        CssCompletionItem.createCompletionItem(new CssElement(NAMESPACE_KEYWORD), NAMESPACE_KEYWORD, null, context.getAnchorOffset(), false);
                proposals.add(nsKeywordProposal);

            case bodyset:
            case media:
            case combinator:
            case selector:
                proposals.addAll(getNamespaceCompletionProposals(context));
                break;

            case elementSubsequent: //after element selector
            case typeSelector: //after class or id selector
                CssTokenId tokenNodeTokenId = context.getActiveTokenNode().type() == NodeType.token ? NodeUtil.getTokenNodeTokenId(context.getActiveTokenNode()) : null;
                if (tokenNodeTokenId == CssTokenId.WS) {
                    proposals.addAll(getNamespaceCompletionProposals(context));
                }
                break;

            case namespace:
                CssTokenId tokenId = context.getTokenSequence().token().id();
                if (tokenId == CssTokenId.NAMESPACE_SYM) {
                    nsKeywordProposal =
                            CssCompletionItem.createCompletionItem(new CssElement(NAMESPACE_KEYWORD), NAMESPACE_KEYWORD, null, context.getAnchorOffset(), false);
                    proposals.add(nsKeywordProposal);
                }

            case simpleSelectorSequence:
                if (isError) {
                    Token<CssTokenId> token = context.getTokenSequence().token();
                    switch (token.id()) {
                        case IDENT:
                            if (LexerUtils.followsToken(context.getTokenSequence(), EnumSet.of(CssTokenId.LBRACKET, CssTokenId.COMMA), true, true, CssTokenId.WS) != null) {
                                proposals.addAll(getNamespaceCompletionProposals(context));
                            }
                            break;
                        case COMMA:
                        case LBRACKET:
                        case WS:
                            proposals.addAll(getNamespaceCompletionProposals(context));
                            break;

                    }
                }
                break;

            case attrib:
            case attrib_name:
            case namespace_wqname_prefix:
                proposals.addAll(getNamespaceCompletionProposals(context));
                break;
        }

        return Css3Utils.filterCompletionProposals(proposals, context.getPrefix(), true);
    }

    private static List<CompletionProposal> getNamespaceCompletionProposals(CompletionContext context) {
        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();
        for (Namespace ns : context.getParserResult().getModel().getNamespaces()) {
            proposals.add(new NamespaceCompletionItem(ns.getPrefix().toString(), ns.getResourceIdentifier().toString(), context.getAnchorOffset()));
        }
        return proposals;
    }

    @Override
    public <T extends Map<OffsetRange, Set<ColoringAttributes>>> NodeVisitor<T> getSemanticHighlightingNodeVisitor(FeatureContext context, T result) {
        return new NodeVisitor<T>(result) {

            @Override
            public boolean visit(Node node) {
                switch (node.type()) {
                    case namespace_prefix:
                    case namespace_wildcard_prefix:
                        getResult().put(Css3Utils.getOffsetRange(node), ColoringAttributes.CONSTRUCTOR_SET);
                        break;
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

        if (current.type() != NodeType.namespace_prefix) {
            return null;
        }

        final CharSequence selectedNamespacePrefixImage = current.image();

        return new NodeVisitor<T>(result) {

            @Override
            public boolean visit(Node node) {
                if (node.type() == NodeType.namespace_prefix
                        && CharSequenceUtilities.textEquals(selectedNamespacePrefixImage, node.image())) {
                    OffsetRange documentNodeRange = Css3Utils.getDocumentOffsetRange(node, snapshot);
                    getResult().add(Css3Utils.getValidOrNONEOffsetRange(documentNodeRange));
                }
                return false;
            }
        };
    }

    @Override
    public <T extends List<StructureItem>> NodeVisitor<T> getStructureItemsNodeVisitor(FeatureContext context, T result) {
        final List<StructureItem> items = new ArrayList<StructureItem>();
        result.add(new TopLevelStructureItem.Namespaces(items));

        return new NodeVisitor<T>() {

            @Override
            public boolean visit(Node node) {
                if (node.type() == NodeType.namespace) {
                    items.add(new NamespaceStructureItem(node));
                }

                return false;
            }
        };
    }
}
