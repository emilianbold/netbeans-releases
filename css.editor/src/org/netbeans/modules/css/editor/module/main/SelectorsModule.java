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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.css.editor.Css3Utils;
import org.netbeans.modules.css.editor.module.spi.CompletionContext;
import org.netbeans.modules.css.editor.module.spi.CssModule;
import org.netbeans.modules.css.editor.module.spi.EditorFeatureContext;
import org.netbeans.modules.css.editor.module.spi.FeatureContext;
import org.netbeans.modules.css.editor.module.spi.Utilities;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.openide.util.lookup.ServiceProvider;

/**
 * The selectors module functionality is partially implemented in the DefaultCssModule
 * from historical reasons. Newly added features are implemented here.
 *
 * @author mfukala@netbeans.org
 */
@ServiceProvider(service = CssModule.class)
public class SelectorsModule extends CssModule {

    //NOI18N>>>
    private static final String[] PSEUDO_CLASSES = new String[]{
        "link", "visited", "hover", "active", "focus", //dynamic

        "target",
        "lang",
        "enabled", "disabled", "checked", "indeterminate", //UI

        "root", "nth-child", "nth-last-child", "nth-of-type", "nth-last-of-type",
        "first-child", "last-child", "first-of-type", "last-of-type", "only-child",
        "only-of-type", "empty" //structural
    };
    private static final String[] PSEUDO_ELEMENTS = new String[]{
        "first-line", "first-letter", "before", "after"
    };
    //<<< NOI18N
    //XXX fix CSL
    static ElementKind PSEUDO_ELEMENT_KIND = ElementKind.GLOBAL;
    static ElementKind PSEUDO_CLASS_KIND = ElementKind.GLOBAL;

    @Override
    public List<CompletionProposal> getCompletionProposals(CompletionContext context) {
        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();
        Node activeNode = context.getActiveNode();
        boolean isError = activeNode.type() == NodeType.error;
        if (isError) {
            activeNode = activeNode.parent();
        }

        switch (activeNode.type()) {
            case simpleSelectorSequence:
                //test if the previous node is typeSelector:  html:|
                Node siblingBefore = NodeUtil.getSibling(context.getActiveNode(), true);
                if (siblingBefore != null && siblingBefore.type() == NodeType.typeSelector) {
                    switch (context.getTokenSequence().token().id()) {
                        case COLON:
                            proposals.addAll(getPseudoClasses(context));
                            break;
                        case DCOLON:
                            proposals.addAll(getPseudoElements(context));
                            break;
                    }
                }
                break;

            case pseudo:
                switch (context.getTokenSequence().token().id()) {
                    case COLON:
                        proposals.addAll(getPseudoClasses(context));
                        break;
                    case DCOLON:
                        proposals.addAll(getPseudoElements(context));
                        break;
                    case IDENT:
                    if (context.getTokenSequence().movePrevious()) {
                        switch (context.getTokenSequence().token().id()) {
                            case COLON:
                                proposals.addAll(getPseudoClasses(context));
                                break;
                            case DCOLON:
                                proposals.addAll(getPseudoElements(context));
                                break;
                        }
                    }
                }
                break;
        }

        return Css3Utils.filterCompletionProposals(proposals, context.getPrefix(), true);
    }

    private static List<CompletionProposal> getPseudoClasses(CompletionContext context) {
        return Utilities.createRAWCompletionProposals(Arrays.asList(PSEUDO_CLASSES), ElementKind.FIELD, context.getAnchorOffset());
    }

    private static List<CompletionProposal> getPseudoElements(CompletionContext context) {
        return Utilities.createRAWCompletionProposals(Arrays.asList(PSEUDO_ELEMENTS), ElementKind.FIELD, context.getAnchorOffset());
    }

    @Override
    public <T extends Map<OffsetRange, Set<ColoringAttributes>>> NodeVisitor<T> getSemanticHighlightingNodeVisitor(FeatureContext context, T result) {
        return new NodeVisitor<T>(result) {

            @Override
            public boolean visit(Node node) {
                switch (node.type()) {
                    case pseudo:
                        getResult().put(Css3Utils.getOffsetRange(node), ColoringAttributes.CLASS_SET);
                        break;
                }
                return false;
            }
        };
    }

    @Override
    public <T extends Set<OffsetRange>> NodeVisitor<T> getMarkOccurrencesNodeVisitor(EditorFeatureContext context, T result) {
        return Utilities.createMarkOccurrencesNodeVisitor(context, result, NodeType.pseudo);
    }
}
