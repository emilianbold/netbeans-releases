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

import org.netbeans.modules.html.parser.model.HtmlTagProvider;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import nu.validator.htmlparser.impl.ErrorReportingTokenizer;
import nu.validator.htmlparser.impl.Tokenizer;
import nu.validator.htmlparser.io.Driver;
import org.netbeans.modules.html.editor.lib.api.*;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModel;
import org.netbeans.modules.html.editor.lib.api.elements.*;
import org.netbeans.modules.html.editor.lib.api.model.*;
import org.netbeans.modules.html.parser.model.ElementDescriptor;
import org.netbeans.modules.html.parser.model.NamedCharacterReference;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author marekfukala
 */
@ServiceProviders(value={
    @ServiceProvider(service = HtmlParser.class, position = 100),
    @ServiceProvider(service = HtmlModelProvider.class, position = 10)
})
public class Html5Parser implements HtmlParser, HtmlModelProvider {

    private static final String PARSER_NAME = String.format("validator.nu html5 parser (%s).", Html5Parser.class); //NOI18N
    private static final HtmlModel HTML5MODEL = new Html5Model();

    @Override
    public HtmlParseResult parse(HtmlSource source, HtmlVersion preferedVersion, Lookup lookup) throws ParseException {
        try {
            CharSequence sourceCode = source.getSourceCode();
            MaskedAreas maskedAreas = lookup.lookup(MaskedAreas.class);

            InputSource is;
            //backward compatibility
            if(maskedAreas == null) {
                //pre html.editor.lib/3.2
                is = new InputSource(new StringReader(sourceCode.toString()));
            } else {
                is = new InputSource(new MaskingChSReader(sourceCode, maskedAreas.positions(), maskedAreas.lens()));
            }
            
            final ParseTreeBuilder treeBuilder = new ParseTreeBuilder(sourceCode);
            final Tokenizer tokenizer = new ErrorReportingTokenizer(treeBuilder);

            Driver driver = new Driver(tokenizer);
            driver.setTransitionHandler(treeBuilder);

            final Collection<ProblemDescription> problems = new ArrayList<ProblemDescription>();
            driver.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException saxpe) throws SAXException {
                    reportProblem(saxpe.getLocalizedMessage(), ProblemDescription.WARNING);

                }

                @Override
                public void error(SAXParseException saxpe) throws SAXException {
                    reportProblem(saxpe.getLocalizedMessage(), ProblemDescription.ERROR);
                }

                @Override
                public void fatalError(SAXParseException saxpe) throws SAXException {
                    reportProblem(saxpe.getLocalizedMessage(), ProblemDescription.FATAL);
                }

                private void reportProblem(String message, int type) {
                    problems.add(ProblemDescription.create(
                            "html5.parser", //NOI18N
                            message,
                            type,
                            treeBuilder.getOffset(),
                            treeBuilder.getOffset()));
                }
            });
            driver.tokenize(is);
            Node root = treeBuilder.getRoot();

            return new Html5ParserResult(source, root, problems, preferedVersion);

        } catch (SAXException ex) {
            throw new ParseException(ex);
        } catch (IOException ex) {
            throw new ParseException(ex);
        } catch (AssertionError e) {
            //issue #194037 handling - under some circumstances the parser may throw assertion error
            //in these cases the problem is more likely in the parser itself than in netbeans
            //To minimalize the user impact on non-fcs version (assertions enabled), 
            //just log the assertion error and return fake parser result

            StringBuilder msg = new StringBuilder();
            msg.append("An internal parser error occured"); //NOI18N
            if (source.getSourceFileObject() != null) {
                msg.append(" when parsing "); //NOI18N
                msg.append(source.getSourceFileObject().getPath());
            }

            Logger.getAnonymousLogger().log(Level.INFO, msg.toString(), e);

            ElementsFactory factory = new ElementsFactory(source.getSourceCode());
            return new Html5ParserResult(source, factory.createRoot(),
                    Collections.<ProblemDescription>emptyList(), preferedVersion);
        }
    }

    @Override
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

    @Override
    public HtmlModel getModel(HtmlVersion version) {
        switch(version) {
            case HTML5:
            case XHTML5:
                return HTML5MODEL;
            default:
                return null;
        }
    }

    @Override
    public String getName() {
        return PARSER_NAME;
    }
    private static class Html5ParserResult extends DefaultHtmlParseResult {

        public Html5ParserResult(HtmlSource source, Node root, Collection<ProblemDescription> problems, HtmlVersion version) {
            super(source, root, problems, version);
        }

        @Override
        public HtmlModel model() {
            return HtmlModelFactory.getModel(version());
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

        @Override
        public Collection<HtmlTag> getPossibleOpenTags(Element afterNode) {
            if (afterNode.type() != ElementType.OPEN_TAG) {
                return Collections.emptyList();
            }

            OpenTag openTag = (OpenTag) afterNode;

            HtmlTag tag = model().getTag(openTag.unqualifiedName().toString());
            if (tag == null) {
                return Collections.emptyList();
            }

            //skip empty tags - this is mailny a workaround for bad logical context of empty nodes
            //however not easily fixable since the HtmlCompletionQuery uses the XmlSyntaxTreeBuilder
            //when the parse tree is broken and the builder has no notion of such metadata.
            while (tag != null && tag.isEmpty()) {
                afterNode = afterNode.parent();
                if (afterNode == null) {
                    return Collections.emptyList();
                }
                if (afterNode.type() != ElementType.OPEN_TAG) {
                    return Collections.emptyList();
                }
                OpenTag ot = (OpenTag) afterNode;
                tag = model().getTag(ot.unqualifiedName().toString());
            }
            if (tag == null) {
                return Collections.emptyList();
            }

            Collection<HtmlTag> possibleChildren = new LinkedHashSet<HtmlTag>();
            addPossibleTags(tag, possibleChildren);
            return possibleChildren;
        }

        @Override
        public Map<HtmlTag, OpenTag> getPossibleCloseTags(Element node) {
            //Bug 197608 - Non-html tags offered as closing tags using code completion
            //XXX define of what type can be the node argument
            if (node.type() != ElementType.OPEN_TAG) {
                node = node.parent();
                if (node == null) {
                    return Collections.emptyMap();
                }
            }
            //<<<

            OpenTag openTag = (OpenTag) node;
            String openTagName = openTag.unqualifiedName().toString();

            HtmlTag tag = model().getTag(openTagName);
            if (tag == null) {
                return Collections.emptyMap();
            }

            Map<HtmlTag, OpenTag> possible = new LinkedHashMap<HtmlTag, OpenTag>();
            //end tags
            do {
                if (!ElementUtils.isVirtualNode(node)) {
                    tag = HtmlTagProvider.getTagForElement(((OpenTag)node).unqualifiedName().toString());
                    if (!tag.isEmpty()) {
                        possible.put(tag, (OpenTag)node);
                    }
                    if (!tag.hasOptionalEndTag()) {
                        //since the end tag is required, the parent elements cannot be closed here
                        break;
                    }
                }
            } while ((node = node.parent()) != null && node.type() == ElementType.OPEN_TAG);

            return possible;


        }
    }

    private static final class Html5Model implements HtmlModel {

        private static Collection<HtmlTag> ALL_TAGS;

        @Override
        public synchronized Collection<HtmlTag> getAllTags() {
            if (ALL_TAGS == null) {
                ALL_TAGS = new ArrayList<HtmlTag>();
                for (ElementDescriptor element : ElementDescriptor.values()) {
                    ALL_TAGS.add(HtmlTagProvider.forElementDescriptor(element));
                }
            }
            return Collections.unmodifiableCollection(ALL_TAGS);
        }

        @Override
        public HtmlTag getTag(String tagName) {
            return HtmlTagProvider.getTagForElement(tagName);
        }

        @Override
        public Collection<? extends NamedCharRef> getNamedCharacterReferences() {
            return EnumSet.allOf(NamedCharacterReference.class);
        }

        public HelpResolver getHelpResolver() {
            return HtmlDocumentation.getDefault();
        }

        @Override
        public String getModelId() {
            return "html5model";//NOI18N
        }
    }
}
