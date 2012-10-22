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
package org.netbeans.modules.javafx2.editor.css;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.css.editor.module.spi.*;
import org.netbeans.modules.css.lib.api.CssModule;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Java FX CSS editor
 *
 * @author Anton Chechel <anton.chechel@oracle.com>
 * @version 1.0
 */
@ServiceProvider(service = CssEditorModule.class)
public class JavaFXCSSModule extends CssEditorModule implements CssModule {

//    private static final String NAMESPACE_KEYWORD = "@namespace"; // NOI18N
    static ElementKind JFX_CSS_ELEMENT_KIND = ElementKind.GLOBAL;
    private static final String PROPERTIES_DEFINITION_PATH = "org/netbeans/modules/javafx2/editor/css/javafx2"; // NOI18N
    private static Map<String, PropertyDefinition> propertyDescriptors;

    @Override
    public Collection<String> getPropertyNames(FileObject file) {
        return isJavaFXContext(file) ? getJavaFXProperties().keySet() : Collections.<String>emptyList();
    }

    @Override
    public PropertyDefinition getPropertyDefinition(FileObject context, String propertyName) {
        return  isJavaFXContext(context) ? getJavaFXProperties().get(propertyName) : null;
    }

    private synchronized Map<String, PropertyDefinition> getJavaFXProperties() {
        if (propertyDescriptors == null) {
            propertyDescriptors = Utilities.parsePropertyDefinitionFile(PROPERTIES_DEFINITION_PATH, this);
        }
        return propertyDescriptors;
    }

    /**
     * TODO IMPLEMENT!!!
     * 
     * @param file file context - may be null!
     * @return
     */
    private boolean isJavaFXContext(FileObject file) {
        return true;
    }

//    @Override
//    public List<org.netbeans.modules.csl.api.CompletionProposal> getCompletionProposals(CompletionContext context) {
//        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();
//        Node activeNode = context.getActiveNode();
//        boolean isError = activeNode.type() == NodeType.error;
//        if (isError) {
//            activeNode = activeNode.parent();
//        }
//
//        switch (activeNode.type()) {
//            case namespace_prefix:
//            case elementName:
//                //already in the prefix
//
//                //todo: rewrite to use index later
//                Stylesheet model = context.getParserResult().getModel();
//                for (Namespace ns : model.getNamespaces()) {
//                    proposals.add(new JavaFXCSSCompletionItem(ns.getPrefix().toString(), ns.getResourceIdentifier().toString(), context.getAnchorOffset()));
//                }
//                break;
//
//            case root:
//            case styleSheet:
//            case bodylist:
//                CompletionProposal nsKeywordProposal =
//                        CssCompletionItem.createRAWCompletionItem(new CssElement(NAMESPACE_KEYWORD), NAMESPACE_KEYWORD, ElementKind.FIELD, context.getAnchorOffset(), false);
//                proposals.add(nsKeywordProposal);
//
//            case bodyset:
//            case media:
//            case combinator:
//            case selector:
//                proposals.addAll(getJavaFXCSSCompletionProposals(context));
//                break;
//
//            case elementSubsequent: //after element selector
//            case typeSelector: //after class or id selector
//                CssTokenId tokenNodeTokenId = context.getActiveTokenNode().type() == NodeType.token ? NodeUtil.getTokenNodeTokenId(context.getActiveTokenNode()) : null;
//                if (tokenNodeTokenId == CssTokenId.WS) {
//                    proposals.addAll(getJavaFXCSSCompletionProposals(context));
//                }
//                break;
//
//            case namespace:
//                CssTokenId tokenId = context.getTokenSequence().token().id();
//                if (tokenId == CssTokenId.NAMESPACE_SYM) {
//                    nsKeywordProposal =
//                            CssCompletionItem.createRAWCompletionItem(new CssElement(NAMESPACE_KEYWORD), NAMESPACE_KEYWORD, ElementKind.FIELD, context.getAnchorOffset(), false);
//                    proposals.add(nsKeywordProposal);
//                }
//
//            case simpleSelectorSequence:
//                if (isError) {
//                    Token<CssTokenId> token = context.getTokenSequence().token();
//                    switch (token.id()) {
//                        case IDENT:
//                            if (JavaFXEditorUtils.followsToken(context.getTokenSequence(), EnumSet.of(CssTokenId.LBRACKET, CssTokenId.COMMA), true, true, CssTokenId.WS) != null) {
//                                proposals.addAll(getJavaFXCSSCompletionProposals(context));
//                            }
//                            break;
//                        case LBRACKET:
//                        case WS:
//                            proposals.addAll(getJavaFXCSSCompletionProposals(context));
//                            break;
//
//                    }
//                }
//                break;
//
//            case attrib:
//            case attrib_name:
//            case namespace_wqname_prefix:
//                proposals.addAll(getJavaFXCSSCompletionProposals(context));
//                break;
//        }
//
//        return JavaFXEditorUtils.filterCompletionProposals(proposals, context.getPrefix(), true);
//    }
//    private static List<CompletionProposal> getJavaFXCSSCompletionProposals(CompletionContext context) {
//        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();
//        for (Namespace ns : context.getParserResult().getModel().getNamespaces()) {
//            proposals.add(new JavaFXCSSCompletionItem(ns.getPrefix().toString(), ns.getResourceIdentifier().toString(), context.getAnchorOffset()));
//        }
//        return proposals;
//    }
    @Override
    public String getName() {
        return "javafx2_css"; //NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(this.getClass(), "css-module-displayname-" + getName()); // NOI18N
    }

    @Override
    public String getSpecificationURL() {
        return "http://docs.oracle.com/javafx/2/api/javafx/scene/doc-files/cssref.html"; // NOI18N
    }
}
