/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.knockout;

import org.netbeans.modules.html.knockout.model.KOModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.html.editor.api.gsf.CustomAttribute;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.HelpItem;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.html.knockout.model.Binding;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
 * Knockout extension to the html editor.
 *
 * @author marekfukala
 */
@MimeRegistrations({
    @MimeRegistration(mimeType = "text/html", service = HtmlExtension.class),
    @MimeRegistration(mimeType = "text/xhtml", service = HtmlExtension.class),
    @MimeRegistration(mimeType = "text/x-jsp", service = HtmlExtension.class),
    @MimeRegistration(mimeType = "text/x-tag", service = HtmlExtension.class),
    @MimeRegistration(mimeType = "text/x-php5", service = HtmlExtension.class)
})
public class KOHtmlExtension extends HtmlExtension {

    @Override
    public boolean isApplicationPiece(HtmlParserResult result) {
        return KOModel.getModel(result).containsKnockout();
    }

    @Override
    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights(HtmlParserResult result, SchedulerEvent event) {
        final Map<OffsetRange, Set<ColoringAttributes>> highlights = new HashMap<>();
        KOModel model = KOModel.getModel(result);
        for (Attribute ngAttr : model.getBindings()) {
            OffsetRange dor = KOUtils.getValidDocumentOffsetRange(ngAttr.from(), ngAttr.from() + ngAttr.name().length(), result.getSnapshot());
            if (dor != null) {
                highlights.put(dor, ColoringAttributes.CONSTRUCTOR_SET);
            }
        }
        return highlights;
    }

    @Override
    public List<CompletionItem> completeAttributes(CompletionContext context) {
        KOModel model = KOModel.getModel(context.getResult());
        List<CompletionItem> items = new ArrayList<>();
        Element element = context.getCurrentNode();

        if (element != null) {
            switch (element.type()) {
                case OPEN_TAG:
                    OpenTag ot = (OpenTag) element;
                    String name = ot.unqualifiedName().toString();
                    Collection<CustomAttribute> customAttributes = getCustomAttributes(name);
                    for (CustomAttribute ca : customAttributes) {
                        items.add(new KOAttributeCompletionItem(ca, context.getCCItemStartOffset(), model.containsKnockout()));
                    }
                    break;
            }
        }

        //XXX copied - needs more elegant solution!
        if (context.getPrefix().length() > 0) {
            //filter the items according to the prefix
            Iterator<CompletionItem> itr = items.iterator();
            while (itr.hasNext()) {
                CharSequence insertPrefix = itr.next().getInsertPrefix();
                if (insertPrefix != null) {
                    if (!LexerUtils.startsWith(insertPrefix, context.getPrefix(), true, false)) {
                        itr.remove();
                    }
                }
            }
        }

        return items;
    }

    //complete keys in ko-data-bind attribute
    //<div data-bind="tex| => text:
    @Override
    public List<CompletionItem> completeAttributeValue(CompletionContext context) {
        Document document = context.getResult().getSnapshot().getSource().getDocument(true);
        TokenHierarchy tokenHierarchy = TokenHierarchy.get(document);
        TokenSequence<HTMLTokenId> ts = LexerUtils.getTokenSequence(tokenHierarchy, context.getOriginalOffset(), HTMLTokenId.language(), false);
        if (ts != null) {
            int diff = ts.move(context.getOriginalOffset());
            if (diff == 0 && ts.movePrevious() || ts.moveNext()) {
                Token<HTMLTokenId> token = ts.token();
                if (token.id() == HTMLTokenId.VALUE) {
                    TokenSequence<KODataBindTokenId> embedded = ts.embedded(KODataBindTokenId.language());
                    if (embedded != null) {
                        if (embedded.isEmpty()) {
                            //no prefix
                            return getBindingItems("", context.getOriginalOffset());
                        }
                        int ediff = embedded.move(context.getOriginalOffset());
                        if (ediff == 0 && embedded.movePrevious() || embedded.moveNext()) {
                            //we are on a token of ko-data-bind token sequence
                            Token<KODataBindTokenId> etoken = embedded.token();
                            switch (etoken.id()) {
                                case KEY:
                                    //ke|
                                    CharSequence prefix = ediff == 0 ? etoken.text() : etoken.text().subSequence(0, ediff);
                                    return getBindingItems(prefix, embedded.offset());
                                case COMMA:
                                    //key:value,|
                                    return getBindingItems("", context.getOriginalOffset());
                                case WS:
                                    //key: value, |
                                    if (embedded.movePrevious()) {
                                        switch (embedded.token().id()) {
                                            case COMMA:
                                                return getBindingItems("", context.getOriginalOffset());
                                        }
                                    } else {
                                        //just WS is before the caret, no token before
                                        //   |
                                        return getBindingItems("", context.getOriginalOffset());

                                    }
                                    break;
                            }
                        }
                    }
                }
            }
        }

        return Collections.emptyList();
    }

    private List<CompletionItem> getBindingItems(CharSequence prefix, int offset) {
        List<CompletionItem> items = new ArrayList<>();
        for (Binding b : Binding.values()) {
            String bindingName = b.getName();
            if (LexerUtils.startsWith(bindingName, prefix, true, false)) {
                items.add(new KOBindingCompletionItem(b, offset));
            }
        }
        return items;
    }

    @Override
    public boolean isCustomAttribute(Attribute attribute) {
        return KOModel.isKODataBindingAttribute(attribute);
    }

    @Override
    public Collection<CustomAttribute> getCustomAttributes(String elementName) {
        return Collections.singleton(KO_DATA_BIND_CUSTOM_ATTRIBUTE);
    }
    private static final String DOC_URL = "http://knockoutjs.com/documentation/binding-syntax.html"; //NOI18N
    static final KOHelpItem KO_DATA_BIND_HELP_ITEM = new KOHelpItem() {
        @Override
        public String getName() {
            return KOUtils.KO_DATA_BIND_ATTR_NAME;
        }

        @Override
        public String getExternalDocumentationURL() {
            return DOC_URL;
        }
    };
    private static final CustomAttribute KO_DATA_BIND_CUSTOM_ATTRIBUTE = new CustomAttribute() {
        @Override
        public String getName() {
            return KOUtils.KO_DATA_BIND_ATTR_NAME;
        }

        @Override
        public boolean isRequired() {
            return false;
        }

        @Override
        public boolean isValueRequired() {
            return true;
        }

        @Override
        public HelpItem getHelp() {
            return new HelpItemImpl(KO_DATA_BIND_HELP_ITEM);
        }
    };
}
