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
package org.netbeans.modules.web.inspect.webkit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.css.editor.api.CssCslParserResult;
import org.netbeans.modules.css.model.api.Declaration;
import org.netbeans.modules.css.model.api.Declarations;
import org.netbeans.modules.css.model.api.Element;
import org.netbeans.modules.css.model.api.Media;
import org.netbeans.modules.css.model.api.MediaQueryList;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelVisitor;
import org.netbeans.modules.css.model.api.SelectorsGroup;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.web.inspect.CSSUtils;
import org.netbeans.modules.web.webkit.debugging.api.css.Property;
import org.netbeans.modules.web.webkit.debugging.api.css.Rule;
import org.netbeans.modules.web.webkit.debugging.api.css.SourceRange;
import org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetBody;
import org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetOrigin;
import org.openide.nodes.Node;

/**
 * WebKit-related utility methods that don't fit well anywhere else.
 *
 * @author Jan Stola
 */
public class Utilities {

    /**
     * Finds the specified WebKit rule in the source file.
     *
     * @param sourceModel source model where the rule should be found.
     * @param styleSheet style sheet where the rule should be found.
     * @param rule rule to find.
     * @return source model representation of the specified WebKit rule.
     */
    public static org.netbeans.modules.css.model.api.Rule findRuleInStyleSheet(
            final Model sourceModel, StyleSheet styleSheet, Rule rule) {
        String selector = CSSUtils.normalizeSelector(rule.getSelector());
        String mediaQuery = null;
        for (org.netbeans.modules.web.webkit.debugging.api.css.Media media : rule.getMedia()) {
            if (media.getSource() == org.netbeans.modules.web.webkit.debugging.api.css.Media.Source.MEDIA_RULE) {
                mediaQuery = media.getText();
                mediaQuery = CSSUtils.normalizeMediaQuery(mediaQuery);
            }
        }
        Set<String> properties = new HashSet<String>();
        for (Property property : rule.getStyle().getProperties()) {
            String propertyName = property.getShorthandName();
            if (propertyName == null) {
                propertyName = property.getName();
            }
            properties.add(propertyName.trim());
        }
        org.netbeans.modules.css.model.api.Rule result = findRuleInStyleSheet0(
                sourceModel, styleSheet, selector, mediaQuery, properties);
        if (result == null) {
            // rule.getSelector() sometimes returns value that differs slightly
            // from the selector in the source file. Besides whitespace changes
            // (that we attempt to handle using CSSUtils.normalizeSelector())
            // there are changes like replacement of a colon in pseudo-elements
            // by a double color (i.e. :after becomes ::after) etc. That's why
            // the rule may not be found despite being in the source file.
            // We attempt to run the search again with the real selector
            // from the source file in this case. Unfortunately, getSelectorRange()
            // method sometimes returns incorrect values. That's why we use
            // it as a fallback only.
            StyleSheetBody parentStyleSheet = rule.getParentStyleSheet();
            SourceRange range = rule.getSelectorRange();
            if (parentStyleSheet != null && range != null) {
                String styleSheetText = parentStyleSheet.getText();
                if (styleSheetText != null) {
                    selector = styleSheetText.substring(range.getStart(), range.getEnd());
                    selector = CSSUtils.normalizeSelector(selector);
                    result = findRuleInStyleSheet0(sourceModel, styleSheet,
                            selector, mediaQuery, properties);
                    if ((result == null) && !rule.getMedia().isEmpty() && (range.getStart() == 0)) {
                        // Workaround for a bug in WebKit (already fixed in the latest
                        // versions of Chrome, but still present in WebView)
                        boolean inLiteral = false;
                        int index = selector.length()-1;
                        outer: while (index >= 0) {
                            char c = selector.charAt(index);
                            switch (c) {
                                case '"':
                                    inLiteral = !inLiteral; break;
                                case '{':
                                case '}':
                                    if (inLiteral) {
                                        break;
                                    } else {
                                        break outer;
                                    }
                            }
                            index--;
                        }
                        if (index != -1) {
                            selector = selector.substring(index+1);
                            selector = CSSUtils.normalizeSelector(selector);
                            result = findRuleInStyleSheet0(sourceModel,
                                    styleSheet, selector, mediaQuery, properties);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Finds a rule with the specified selector in the source file.
     *
     * @param sourceModel source model where the rule should be found.
     * @param styleSheet style sheet where the rule should be found.
     * @param selector selector of the rule to find.
     * @param mediaQuery media query of the rule (can be {@code null}).
     * @param properties name of the properties in the rule.
     * @return source model representation of a rule with the specified selector.
     */
    private static org.netbeans.modules.css.model.api.Rule findRuleInStyleSheet0(
            final Model sourceModel, StyleSheet styleSheet,
            final String selector, final String mediaQuery,
            final Set<String> properties) {
        final org.netbeans.modules.css.model.api.Rule[] result =
                new org.netbeans.modules.css.model.api.Rule[1];

        styleSheet.accept(new ModelVisitor.Adapter() {
            /** Value of the best matching rule so far. */
            private int bestMatchValue = 0;

            @Override
            public void visitRule(org.netbeans.modules.css.model.api.Rule rule) {
                SelectorsGroup selectorGroup = rule.getSelectorsGroup();
                CharSequence image = sourceModel.getElementSource(selectorGroup);
                Element parent = rule.getParent();
                String queryListText = null;
                if (parent instanceof Media) {
                    Media media = (Media)parent;
                    MediaQueryList queryList = media.getMediaQueryList();
                    queryListText = sourceModel.getElementSource(queryList).toString();
                    queryListText = CSSUtils.normalizeMediaQuery(queryListText);
                }
                String selectorInFile = CSSUtils.normalizeSelector(image.toString());
                if (selector.equals(selectorInFile) &&
                        ((mediaQuery == null) ? (queryListText == null) : mediaQuery.equals(queryListText))) {
                    int matchValue = matchValue(rule);
                    if (matchValue >= bestMatchValue) {
                        bestMatchValue = matchValue;
                        result[0] = rule;
                    }
                }
            }

            /**
             * Determines how well the properties in the specified rule
             * match to the properties of the rule we are searching for.
             * 
             * @param rule rule to check.
             * @return value of the matching (the higher the better match).
             */
            private int matchValue(org.netbeans.modules.css.model.api.Rule rule) {
                int value = 0;
                Declarations declarations = rule.getDeclarations();
                if (declarations != null) {
                    for (Declaration declaration : declarations.getDeclarations()) {
                        org.netbeans.modules.css.model.api.Property modelProperty = declaration.getProperty();
                        String modelPropertyName = modelProperty.getContent().toString().trim();
                        if (properties.contains(modelPropertyName)) {
                            value++;
                        }
                    }
                }
                return value;
            }
        });

        return result[0];
    }

    /**
     * Determines whether the specified rule should be shown in CSS Styles view.
     * 
     * @param rule rule to check.
     * @return {@code true} when the rule should be shown in CSS Styles view,
     * returns {@code false} otherwise.
     */
    public static boolean showInCSSStyles(Rule rule) {
        return (rule.getOrigin() != StyleSheetOrigin.USER_AGENT);
    }

    /**
     * Finds a node that represents the specified rule in a tree
     * represented by the given root node.
     *
     * @param root root of a tree to search.
     * @param rule rule to find.
     * @return node that represents the rule or {@code null}.
     */
    public static Node findRule(Node root, Rule rule) {
        Rule candidate = root.getLookup().lookup(Rule.class);
        if (candidate != null &&  rule.getId().equals(candidate.getId())
                && rule.getSourceURL().equals(candidate.getSourceURL())
                && rule.getSelector().equals(candidate.getSelector())) {
            return root;
        }
        for (Node node : root.getChildren().getNodes()) {
            Node result = findRule(node, rule);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Returns {@code CssCslParserResult}s (including the embedded ones) that
     * correspond to the given {@code ResultIterator}.
     * 
     * @param resultIterator {@code ResultIterator} to process.
     * @return {@code CssCslParserResult}s contained in the given {@code ResultIterator}.
     * @throws ParseException when there is a parsing problem.
     */
    public static List<CssCslParserResult> cssParserResults(ResultIterator resultIterator)
            throws ParseException {
        List<ResultIterator> resultIterators = new ArrayList<ResultIterator>();
        resultIterators.add(resultIterator);
        for (Embedding embedding : resultIterator.getEmbeddings()) {
            String mimeType = embedding.getMimeType();
            if ("text/css".equals(mimeType)) { // NOI18N
                resultIterators.add(resultIterator.getResultIterator(embedding));
            }
        }
        List<CssCslParserResult> parserResults = new ArrayList<CssCslParserResult>(resultIterators.size());
        for (ResultIterator iterator : resultIterators) {
            Parser.Result parserResult = iterator.getParserResult();
            if (parserResult instanceof CssCslParserResult) {
                parserResults.add((CssCslParserResult)parserResult);
            }
        }
        return parserResults;
    }

}
