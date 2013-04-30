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

import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.model.api.AtRule;
import org.netbeans.modules.css.model.api.Body;
import org.netbeans.modules.css.model.api.BodyItem;
import org.netbeans.modules.css.model.api.Declaration;
import org.netbeans.modules.css.model.api.Declarations;
import org.netbeans.modules.css.model.api.Element;
import org.netbeans.modules.css.model.api.Media;
import org.netbeans.modules.css.model.api.MediaQueryList;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelVisitor;
import org.netbeans.modules.css.model.api.PropertyValue;
import org.netbeans.modules.css.model.api.SelectorsGroup;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.inspect.CSSUtils;
import org.netbeans.modules.web.inspect.actions.Resource;
import org.netbeans.modules.web.webkit.debugging.api.css.Property;
import org.netbeans.modules.web.webkit.debugging.api.css.Rule;
import org.netbeans.modules.web.webkit.debugging.api.css.SourceRange;
import org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetBody;
import org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetOrigin;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
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
            if (property.getText() == null) {
                // longhand property that is included in the rule
                // indirectly through the corresponding shorthand property
                continue;
            }
            String propertyName = property.getName();
            properties.add(propertyName.trim());
        }
        int sourceLine = rule.getSourceLine();
        SourceRange range = rule.getSelectorRange();
        int startOffset = (range == null) ? Short.MIN_VALUE : range.getStart();
        org.netbeans.modules.css.model.api.Rule result = findRuleInStyleSheet0(
                sourceModel, styleSheet, selector, mediaQuery, properties,
                sourceLine, startOffset);
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
            if (parentStyleSheet != null && range != null) {
                String styleSheetText = parentStyleSheet.getText();
                if (styleSheetText != null) {
                    selector = styleSheetText.substring(range.getStart(), range.getEnd());
                    selector = CSSUtils.normalizeSelector(selector);
                    result = findRuleInStyleSheet0(sourceModel, styleSheet,
                            selector, mediaQuery, properties, sourceLine, startOffset);
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
                                default:
                            }
                            index--;
                        }
                        if (index != -1) {
                            selector = selector.substring(index+1);
                            selector = CSSUtils.normalizeSelector(selector);
                            result = findRuleInStyleSheet0(sourceModel,
                                    styleSheet, selector, mediaQuery,
                                    properties, sourceLine, startOffset);
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
     * @param sourceLine (the first) source line of the rule.
     * @param startOffset starting offset of the rule.
     * @return source model representation of a rule with the specified selector.
     */
    private static org.netbeans.modules.css.model.api.Rule findRuleInStyleSheet0(
            final Model sourceModel, StyleSheet styleSheet,
            final String selector, final String mediaQuery,
            final Set<String> properties, final int sourceLine,
            final int startOffset) {
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
                        org.netbeans.modules.css.model.api.Property modelProperty = declaration.getPropertyDeclaration().getProperty();
                        String modelPropertyName = modelProperty.getContent().toString().trim();
                        if (properties.contains(modelPropertyName)) {
                            value += 2;
                        }
                    }
                }
                int offset = rule.getStartOffset();
                // The CSS model never uses CR+LF line ends, but the browser
                // does. Hence, the second part of the following check.
                if ((offset == startOffset) || (offset+sourceLine == startOffset)) {
                    try {
                        int line = LexerUtils.getLineOffset(sourceModel.getModelSource(), offset);
                        if (line == sourceLine) {
                            value += 1;
                        }
                    } catch (BadLocationException blex) {}
                }
                return value;
            }
        });

        return result[0];
    }

    /**
     * Jumps into the meta-source of the specified rule. It does nothing
     * if there is no meta-source of the rule, i.e., when the rule comes
     * directly from some CSS stylesheet.
     * 
     * @param rule rule to jump to.
     * @return {@code true} when the rule comes from some meta-source
     * and the corresponding source file was opened successfully,
     * returns {@code false} otherwise.
     */
    public static boolean goToMetaSource(org.netbeans.modules.css.model.api.Rule rule) {
        Element parent = rule.getParent();
        if (parent instanceof BodyItem) {
            BodyItem bodyItem = (BodyItem)parent;
            parent = bodyItem.getParent();
            if (parent instanceof Body) {
                Body body = (Body)parent;
                List<BodyItem> bodyItems = body.getBodyItems();
                int index = bodyItems.indexOf(bodyItem);
                if (index > 0) {
                    BodyItem previousBodyItem = bodyItems.get(index-1);
                    Element element = previousBodyItem.getElement();
                    if (element instanceof AtRule) {
                        element = ((AtRule)element).getElement();
                    }
                    if (element instanceof Media) {
                        Media media = (Media)element;
                        if (isMetaSourceInfo(media)) {
                            return goToMetaSource(media);
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines whether the given media represents information
     * about the meta-source of the next rule.
     * 
     * @param media media to check.
     * @return {@code true} when the given media holds source-related
     * information, returns {@code false} otherwise.
     */
    private static boolean isMetaSourceInfo(Media media) {
        MediaQueryList queryList = media.getMediaQueryList();
        Model sourceModel = media.getModel();
        String queryListText = sourceModel.getElementSource(queryList).toString();
        return "-sass-debug-info".equals(queryListText); // NOI18N
    }

    /**
     * Jumps into the meta-source of the rule that follows the given media
     * (that holds the information about the meta-source).
     * 
     * @param media media holding the source-related information.
     * @return {@code true} when the source file was opened successfully,
     * returns {@code false} otherwise.
     */
    private static boolean goToMetaSource(Media media) {
        String originalFileName = null;
        int originalLineNumber = -1;
        for (org.netbeans.modules.css.model.api.Rule rule : media.getRules()) {
            SelectorsGroup selectorGroup = rule.getSelectorsGroup();
            Model sourceModel = media.getModel();
            CharSequence image = sourceModel.getElementSource(selectorGroup);
            String selector = image.toString();
            if ("filename".equals(selector)) { // NOI18N
                String value = propertyValue(rule, "font-family"); // NOI18N
                if (value != null) {
                    StringBuilder sb = new StringBuilder();
                    boolean slash = false;
                    for (int i=0; i<value.length(); i++) {
                        char c = value.charAt(i);
                        if (slash && (c != ':' && c != '/' && c != '.')) {
                            sb.append('\\');
                        }
                        slash = !slash && (c == '\\');
                        if (!slash) {
                            sb.append(c);
                        }
                    }
                    originalFileName = sb.toString();
                    String prefix = "file://"; // NOI18N
                    if (originalFileName.startsWith(prefix)) {
                        originalFileName = originalFileName.substring(prefix.length());
                    }
                }
            } else if ("line".equals(selector)) { // NOI18N
                String value = propertyValue(rule, "font-family"); // NOI18N
                String prefix = "\\00003"; // NOI18N
                if (value != null && value.startsWith(prefix)) {
                    String lineTxt = value.substring(prefix.length());
                    try {
                        originalLineNumber = Integer.parseInt(lineTxt);
                    } catch (NumberFormatException nfex) {
                        Logger.getLogger(Utilities.class.getName()).log(Level.INFO, null, nfex);
                    }
                }
            }
        }
        if (originalFileName != null && originalLineNumber != -1) {
            File file = new File(originalFileName);
            file = FileUtil.normalizeFile(file);
            final FileObject fob = FileUtil.toFileObject(file);
            if (fob != null) {
                final int lineNo = originalLineNumber - 1;
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        CSSUtils.openAtLine(fob, lineNo);
                    }
                });
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the value of the specified property in the given rule.
     * 
     * @param rule rule to extract the value from.
     * @param propertyName name of the property whose value should be returned.
     * @return value of the property or {@code null} when there is no
     * property with the specified name in the given rule.
     */
    private static String propertyValue(org.netbeans.modules.css.model.api.Rule rule, String propertyName) {
        String propertyValue = null;
        Declarations declarations = rule.getDeclarations();
        if (declarations != null) {
            for (Declaration declaration : declarations.getDeclarations()) {
                org.netbeans.modules.css.model.api.Property modelProperty = declaration.getPropertyDeclaration().getProperty();
                String modelPropertyName = modelProperty.getContent().toString().trim();
                if (propertyName.equals(modelPropertyName)) {
                    PropertyValue value = declaration.getPropertyDeclaration().getPropertyValue();
                    propertyValue = value.getExpression().getContent().toString();
                }
            }
        }
        return propertyValue;
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
     * Returns {@code CssParserResult}s (including the embedded ones) that
     * correspond to the given {@code ResultIterator}.
     * 
     * @param resultIterator {@code ResultIterator} to process.
     * @return {@code CssParserResult}s contained in the given {@code ResultIterator}.
     * @throws ParseException when there is a parsing problem.
     */
    public static List<CssParserResult> cssParserResults(ResultIterator resultIterator)
            throws ParseException {
        List<ResultIterator> resultIterators = new ArrayList<ResultIterator>();
        resultIterators.add(resultIterator);
        for (Embedding embedding : resultIterator.getEmbeddings()) {
            String mimeType = embedding.getMimeType();
            if ("text/css".equals(mimeType)) { // NOI18N
                resultIterators.add(resultIterator.getResultIterator(embedding));
            }
        }
        List<CssParserResult> parserResults = new ArrayList<CssParserResult>(resultIterators.size());
        for (ResultIterator iterator : resultIterators) {
            Parser.Result parserResult = iterator.getParserResult();
            if (parserResult instanceof CssParserResult) {
                parserResults.add((CssParserResult)parserResult);
            }
        }
        return parserResults;
    }

    /**
     * Returns name of the resource relative to the project directory.
     *
     * @param resourceUrl absolute name/URL of the resource.
     * @param project project owning the resource.
     * @return relative name of the resource.
     */
    public static String relativeResourceName(String resourceUrl, Project project) {
        String name = resourceUrl;
        if (project != null) {
            FileObject fob = new Resource(project, resourceUrl).toFileObject();
            if (fob != null) {
                FileObject projectDir = project.getProjectDirectory();
                String relativePath = FileUtil.getRelativePath(projectDir, fob);
                name = relativePath;
            }
        }
        return name;
    }

}
