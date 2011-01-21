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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.regex.Pattern;
import nu.validator.htmlparser.impl.ErrorReportingTokenizer;
import nu.validator.htmlparser.impl.Tokenizer;
import nu.validator.htmlparser.io.Driver;
import org.netbeans.editor.ext.html.parser.api.AstNode;
import org.netbeans.editor.ext.html.parser.api.AstNodeFactory;
import org.netbeans.editor.ext.html.parser.api.HtmlVersion;
import org.netbeans.editor.ext.html.parser.api.ParseException;
import org.netbeans.editor.ext.html.parser.spi.DefaultHtmlParseResult;
import org.netbeans.editor.ext.html.parser.spi.HelpResolver;
import org.netbeans.editor.ext.html.parser.spi.HtmlModel;
import org.netbeans.editor.ext.html.parser.spi.HtmlParseResult;
import org.netbeans.editor.ext.html.parser.spi.HtmlParser;
import org.netbeans.editor.ext.html.parser.api.HtmlSource;
import org.netbeans.editor.ext.html.parser.api.ProblemDescription;
import org.netbeans.editor.ext.html.parser.spi.HtmlTag;
import org.netbeans.editor.ext.html.parser.spi.NamedCharRef;
import org.netbeans.modules.html.parser.model.ElementDescriptor;
import org.netbeans.modules.html.parser.model.NamedCharacterReference;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author marekfukala
 */
@ServiceProvider(service = HtmlParser.class, position = 100)
public class Html5Parser implements HtmlParser {

    private static final String PARSER_NAME = String.format("validator.nu html5 parser (%s).", Html5Parser.class); //NOI18N
    private static final HtmlModel HTML5MODEL = new Html5Model();

    private static final Pattern TEMPLATING_MARKS_PATTERN = Pattern.compile("@@@"); //NOI18N
    private static final String TEMPLATING_MARKS_MASK = "   "; //NOI18N

    public HtmlParseResult parse(HtmlSource source, HtmlVersion preferedVersion, Lookup lookup) throws ParseException {
        try {
            String code = maskTemplatingMarks(source.getSourceCode().toString());
            InputSource is = new InputSource(new StringReader(code));
            final AstNodeTreeBuilder treeBuilder = new AstNodeTreeBuilder(AstNodeFactory.shared().createRootNode(0, code.length()));
            final Tokenizer tokenizer = new ErrorReportingTokenizer(treeBuilder);

            Driver driver = new Driver(tokenizer);
            driver.setTransitionHandler(treeBuilder);
            driver.tokenize(is);
            AstNode root = treeBuilder.getRoot();

            return new Html5ParserResult(source, root, Collections.<ProblemDescription>emptyList(), preferedVersion);

        } catch (SAXException ex) {
            throw new ParseException(ex);
        } catch (IOException ex) {
            throw new ParseException(ex);
        }
    }

    public boolean canParse(HtmlVersion version) {
        return version == HtmlVersion.HTML5
                || version == HtmlVersion.XHTML5
                || version == HtmlVersion.HTML32
                || version == HtmlVersion.HTML41_STRICT
                || version == HtmlVersion.HTML41_TRANSATIONAL
                || version == HtmlVersion.HTML41_FRAMESET
                || version == HtmlVersion.HTML40_STRICT
                || version == HtmlVersion.HTML40_TRANSATIONAL
                || version == HtmlVersion.HTML40_FRAMESET
                || version == HtmlVersion.XHTML10_STICT
                || version == HtmlVersion.XHTML10_TRANSATIONAL
                || version == HtmlVersion.XHTML10_FRAMESET
                || version == HtmlVersion.XHTML11;
    }

    public HtmlModel getModel(HtmlVersion version) {
        assert version == HtmlVersion.HTML5 || version == HtmlVersion.XHTML5;
        return HTML5MODEL;
    }

    public String getName() {
        return PARSER_NAME;
    }

    static String maskTemplatingMarks(String code) {
        return TEMPLATING_MARKS_PATTERN.matcher(code).replaceAll(TEMPLATING_MARKS_MASK);
    }

    private static class Html5ParserResult extends DefaultHtmlParseResult {

        public Html5ParserResult(HtmlSource source, AstNode root, Collection<ProblemDescription> problems, HtmlVersion version) {
            super(source, root, problems, version);
        }

        public HtmlModel model() {
            return HTML5MODEL;
        }

        public Collection<HtmlTag> getPossibleTagsInContext(AstNode afterNode, boolean openTags) {
            HtmlTag tag = model().getTag(afterNode.getNameWithoutPrefix());
            if(tag == null) {
                return Collections.emptyList();
            }
            if (openTags) {
                //skip empty tags - this is mailny a workaround for bad logical context of empty nodes
                //however not easily fixable since the HtmlCompletionQuery uses the XmlSyntaxTreeBuilder
                //when the parse tree is broken and the builder has no notion of such metadata.
                while (tag != null && tag.isEmpty()) {
                    afterNode = afterNode.parent();
                    if (afterNode == null) {
                        return Collections.emptyList();
                    }
                    tag = model().getTag(afterNode.getNameWithoutPrefix());
                }
                if (tag == null) {
                    return Collections.emptyList();
                }

                Collection<HtmlTag> possibleChildren = new LinkedHashSet<HtmlTag>();
                addPossibleTags(tag, possibleChildren);
                return possibleChildren;
            } else {
                return completeEndTags(afterNode);
            }
        }

        public void addPossibleTags(HtmlTag tag, Collection<HtmlTag> possible) {
            //1.add all children of the tag
            //2.if a child has optional end, add its possible children
            //3.if a child is transparent, add its possible children
            Collection<HtmlTag> children = tag.getChildren();
            possible.addAll(children);
            for (HtmlTag child : children) {
                if (child.hasOptionalOpenTag()) {
                    addPossibleTags(child, possible);
                }
                //TODO add the transparent check
            }
        }

        private Collection<HtmlTag> completeEndTags(AstNode node) {
            Collection<HtmlTag> possible = new LinkedHashSet<HtmlTag>();
            //end tags
            do {
                if(!node.isVirtual()) {
                    HtmlTag tag = HtmlTagProvider.getTagForElement(node.getNameWithoutPrefix());
                    if (!tag.isEmpty()) {
                        possible.add(tag);
                    }
                }
            } while ((node = node.parent()) != null && !node.isRootNode());

            return possible;
        }
    }

    private static final class Html5Model implements HtmlModel {

        private static Collection<HtmlTag> ALL_TAGS;

        public synchronized Collection<HtmlTag> getAllTags() {
            if (ALL_TAGS == null) {
                ALL_TAGS = new ArrayList<HtmlTag>();
                for (ElementDescriptor element : ElementDescriptor.values()) {
                    ALL_TAGS.add(HtmlTagProvider.getTagForElement(element.getName()));
                }
            }
            return Collections.unmodifiableCollection(ALL_TAGS);
        }

        public HtmlTag getTag(String tagName) {
            return HtmlTagProvider.getTagForElement(tagName);
        }

        public Collection<? extends NamedCharRef> getNamedCharacterReferences() {
            return EnumSet.allOf(NamedCharacterReference.class);
        }

        public HelpResolver getHelpResolver() {
            return HtmlDocumentation.getDefault();
        }

        public String getModelId() {
            return "html5model";//NOI18N
        }
    }
}
