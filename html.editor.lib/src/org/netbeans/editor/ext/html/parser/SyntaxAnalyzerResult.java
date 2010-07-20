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
package org.netbeans.editor.ext.html.parser;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.editor.ext.html.parser.SyntaxElement.Tag;
import org.netbeans.editor.ext.html.parser.api.HtmlSource;
import org.netbeans.editor.ext.html.parser.api.ParseException;
import org.netbeans.editor.ext.html.parser.api.HtmlVersion;
import org.netbeans.editor.ext.html.parser.spi.HtmlParseResult;
import org.netbeans.editor.ext.html.parser.spi.HtmlParser;
import org.netbeans.editor.ext.html.parser.spi.ParseResult;
import org.netbeans.editor.ext.html.parser.api.AstNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.editor.ext.html.parser.SyntaxElement.Declaration;
import org.netbeans.editor.ext.html.parser.api.HtmlParserFactory;
import org.netbeans.editor.ext.html.parser.api.HtmlSourceVersionQuery;
import org.netbeans.editor.ext.html.parser.api.ProblemDescription;
import org.netbeans.editor.ext.html.parser.spi.DefaultParseResult;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Html syntax analyzer result.
 *
 * @author mfukala@netbeans.org
 */
public class SyntaxAnalyzerResult {

    /** special namespace which can be used for obtaining a parse tree
     * of tags with undeclared namespace.
     */
    private SyntaxAnalyzer analyzer;
    private Declaration declaration;
    private HtmlVersion detectedHtmlVersion;

    private HtmlParseResult htmlParseResult;
    //ns URI to AstNode map
    private Map<String, ParseResult> embeddedCodeParseResults;
    //ns URI to PREFIX map
    private Map<String, List<String>> namespaces;

    private ParseResult undeclaredEmbeddedCodeParseResult;

    private Set<String> allPrefixes;

    public SyntaxAnalyzerResult(SyntaxAnalyzer source) {
        this.analyzer = source;
    }

    public CharSequence getSourceCode() {
        return analyzer.source().getSourceCode();
    }

    public SyntaxAnalyzerElements getElements() {
        return analyzer.elements();
    }

    public HtmlVersion getHtmlVersion() {
        HtmlVersion detected = getDetectedHtmlVersion();
        HtmlVersion found = HtmlSourceVersionQuery.getSourceCodeVersion(analyzer.source(), detected);
        if (found != null) {
            return found;
        }
        return detected != null ? detected : HtmlVersion.HTML41_TRANSATIONAL; //fallback if nothing can be determined
    }

    /**
     * Returns an html version for the specified parser result input.
     * The return value depends on:
     * 1) doctype declaration content
     * 2) if not present, xhtml file extension
     * 3) if not xhtml extension, present of default XHTML namespace declaration
     *
     */
    public synchronized HtmlVersion getDetectedHtmlVersion() {
        if (detectedHtmlVersion == null) {
            detectedHtmlVersion = detectHtmlVersion();
        }
        return detectedHtmlVersion;
    }

    private HtmlVersion detectHtmlVersion() {
        Declaration doctypeDeclaration = getDoctypeDeclaration();
        if (doctypeDeclaration == null) {
            //no doctype declaration at all

            //is there an xhtml namespace declared in the file?
            for (String ns : getDeclaredNamespaces().keySet()) {
                HtmlVersion found = HtmlVersion.findByNamespace(ns);
                if (found != null) {
                    return found;
                }
            }

            //no doctype, no namespace, try to dectect file type
            FileObject fo = analyzer.source().getSourceFileObject();
            if (fo != null) {
                if ("text/xhtml".equals(fo.getMIMEType())) { //NOI18N
                    return HtmlVersion.XHTML10_STICT;
                }
            }

        } else {
            //found doctype declaration => SGML syntax
            String publicId = getPublicID();
            return HtmlVersion.findByPublicId(publicId);
        }

        return null;
    }



    public synchronized HtmlParseResult parseHtml() throws ParseException {
        if(htmlParseResult == null) {
            htmlParseResult = doParseHtml();
        }
        return htmlParseResult;
    }

    private HtmlParseResult doParseHtml() throws ParseException {
        HtmlVersion version = getHtmlVersion();
        HtmlParser parser = HtmlParserFactory.findParser(version);
        if (parser == null) {
            throw new IllegalStateException("Cannot find an HtmlParser implementation for "
                    + getHtmlVersion().name()); //NOI18N
        }

        final Collection<String> prefixes = version.getDefaultNamespace() != null
                ? getAllDeclaredNamespaces().get(version.getDefaultNamespace())
                : null;


        LocalSourceContext context = createLocalContext(new TagsFilter() {

            @Override
            public boolean accepts(Tag tag, String prefix) {
                if (prefix == null) {
                    return true; //default namespace, should be html in most cases
                }
                if (prefixes != null) {
                    if (prefixes.contains(prefix)) {
                        //the prefix is mapped to the html namespace
                        return true;
                    }
                }

                return false;
            }
        });

        CharSequence clearedSource = clearIgnoredAreas(getSourceCode(), context.getIgnoredAreas());
        List<SyntaxElement> filtered = context.getFiltered();

        //create a new html source with the cleared areas
        HtmlSource source = new HtmlSource(clearedSource, analyzer.source().getSnapshot() , analyzer.source().getSourceFileObject());

        //add the syntax elements to the lookup since the old html4 parser needs them
        InstanceContent content = new InstanceContent();
        content.add(filtered);
        Lookup lookup = new AbstractLookup(content);

        return parser.parse(source, getHtmlVersion(), lookup);

    }

    public synchronized ParseResult parseEmbeddedCode(String namespace) throws ParseException {
        if(embeddedCodeParseResults == null) {
            embeddedCodeParseResults = new HashMap<String, ParseResult>();
        }
        ParseResult result = embeddedCodeParseResults.get(namespace);
        if(result == null) {
            result = doParseEmbeddedCode(namespace);
            embeddedCodeParseResults.put(namespace, result);
        }
        return result;
    }

    private ParseResult doParseEmbeddedCode(String namespace) throws ParseException {
        final Collection<String> prefixes = getAllDeclaredNamespaces().get(namespace);
        if(prefixes == null || prefixes.isEmpty()) {
            return null;
        }

        LocalSourceContext context = createLocalContext(new TagsFilter() {

            @Override
            public boolean accepts(Tag tag, String prefix) {
                return prefix != null && prefixes.contains(prefix);
            }
        });

        CharSequence clearedSource = clearIgnoredAreas(getSourceCode(), context.getIgnoredAreas());

        //create a new html source with the cleared areas
        HtmlSource source = new HtmlSource(clearedSource, analyzer.source().getSnapshot() , analyzer.source().getSourceFileObject());

        AstNode root = XmlSyntaxTreeBuilder.makeUncheckedTree(source, context.getFiltered());
        
        return new DefaultParseResult(source, root, Collections.<ProblemDescription>emptyList());

    }

    public ParseResult parseUndeclaredEmbeddedCode() throws ParseException {
        if(undeclaredEmbeddedCodeParseResult == null) {
            undeclaredEmbeddedCodeParseResult = doParseUndeclaredEmbeddedCode();
        }
        return undeclaredEmbeddedCodeParseResult;
    }
    
    private ParseResult doParseUndeclaredEmbeddedCode() throws ParseException {
        final Collection<String> prefixes = getAllDeclaredPrefixes();
        LocalSourceContext context = createLocalContext(new TagsFilter() {

            @Override
            public boolean accepts(Tag tag, String prefix) {
                return prefix != null && !prefixes.contains(prefix);
            }
        });

        CharSequence clearedSource = clearIgnoredAreas(getSourceCode(), context.getIgnoredAreas());

        //create a new html source with the cleared areas
        HtmlSource source = new HtmlSource(clearedSource, analyzer.source().getSnapshot() , analyzer.source().getSourceFileObject());

        AstNode root = XmlSyntaxTreeBuilder.makeUncheckedTree(source, context.getFiltered());

        return new DefaultParseResult(source, root, Collections.<ProblemDescription>emptyList());

    }

    
    private LocalSourceContext createLocalContext(TagsFilter filter) {
        //html5 parser:
        //since the nu.validator.htmlparser parser cannot properly handle the
        //'foreign' namespace content it needs to be filtered from the source
        //before running the parser on the input charsequence
        //
        //so following content needs to be filtere out:
        //1. xmlns non default declarations <html xmlns:f="http:/...
        //2. the prefixed tags and attributes <f:if ...
        List<IgnoredArea> ignoredAreas = new ArrayList<IgnoredArea>();

        List<SyntaxElement> filtered = new ArrayList<SyntaxElement>();

        for (SyntaxElement e : getElements().items()) {
            if (e.type() == SyntaxElement.TYPE_TAG || e.type() == SyntaxElement.TYPE_ENDTAG) {
                SyntaxElement.Tag tag = (SyntaxElement.Tag) e;
                String tagNamePrefix = getTagNamePrefix(tag);

                if (filter.accepts(tag, tagNamePrefix)) {
                    filtered.add(e);
                } else {
                    ignoredAreas.add(new IgnoredArea(e.offset(), e.offset() + e.length()));
                }
            }
        }
        return new LocalSourceContext(ignoredAreas, filtered);

    }

//    private void xxx() {
//
//        //filter the elements
//        List<SyntaxElement> filtered = new ArrayList<SyntaxElement>();
//
//        //prefixes may be null, if there's is no tag with the specified namespace,
//        //
//        //or can be a one item list with the UNDECLARED_TAGS_PREFIX which represents
//        //all undeclared tags,
//        //
//        //or can be a list of tags prefixes already mapped to the given namespace
//        List<String> prefixes = UNDECLARED_TAGS_NAMESPACE.equals(namespace)
//                ? Collections.singletonList(UNDECLARED_TAGS_PREFIX)
//                : getAllDeclaredNamespaces().get(namespace);
//
//        //html5 parser:
//        //since the nu.validator.htmlparser parser cannot properly handle the
//        //'foreign' namespace content it needs to be filtered from the source
//        //before running the parser on the input charsequence
//        //
//        //so following content needs to be filtere out:
//        //1. xmlns non default declarations <html xmlns:f="http:/...
//        //2. the prefixed tags and attributes <f:if ...
//        List<IgnoredArea> ignoredAreas = new ArrayList<IgnoredArea>();
//
//        for (SyntaxElement e : getElements()) {
//            if (e.type() == SyntaxElement.TYPE_TAG || e.type() == SyntaxElement.TYPE_ENDTAG) {
//                SyntaxElement.Tag tag = (SyntaxElement.Tag) e;
//                String tagNamePrefix = getTagNamePrefix(tag);
//
//                boolean add = false; //for better readibility of the code below
//                if (tagNamePrefix == null) {
//                    if (prefixes == null) {
//                        //tags w/o any prefix
//                        add = true;
//                    }
//                } else {
//                    //tagNamePrefix != null
//                    if (prefixes != null) {
//                        if (prefixes.contains(UNDECLARED_TAGS_PREFIX)
//                                && !getDeclaredNamespaces().containsValue(tagNamePrefix)) {
//                            //unknown prefixed tags falls to the 'undeclared tags parse tree'
//                            add = true;
//
//                        } else if (prefixes.contains(tagNamePrefix)) {
//                            //or the prefix matches
//                            add = true;
//                        }
//                    }
//                }
//
//                if (add) {
//                    filtered.add(e);
//                } else {
//                    ignoredAreas.add(new IgnoredArea(e.offset(), e.offset() + e.length()));
//                }
//
//                //TODO Add xmlns:xxx attributes filtering
//
//            } else {
//                //do not filter the other types
//                filtered.add(e);
//            }
//        }
//
//        AstNode root;
//        CharSequence source;
//
//        if (!isHtml5()) {
//            SyntaxParserContext spc = source.clone().setElements(filtered);
//            source = spc.getSourceText();
//            DTD dtd = null;
//            if (getDTDNoFallback() != null) {
//                if (defaultDTD != null) {
//                    //DTD found for this file and preferred dtd is specified
//                    //this means we are trying to parse html or xhtml tags
//                    dtd = getDTDNoFallback();
//                } else {
//                    //DTD found, but no preferred dtd specifes,
//                    //this means that one wants to parse some non-html stuff
//                    dtd = null;
//                }
//            } else {
//                if (defaultDTD != null) {
//                    dtd = defaultDTD;
//                }
//            }
//            spc.setDTD(dtd);
//            root = SyntaxTreeBuilder.makeTree(spc);
//        } else {
//            //html5
//            source = clearIgnoredAreas(source.getSourceText(), ignoredAreas);
//            try {
//                root = HtmlParserFactory.instance().parse(source);
//            } catch (ParseException ex) {
//                Exceptions.printStackTrace(ex);
//                root = null;
//            }
//        }
//
//        root.setProperty(AstNode.NAMESPACE_PROPERTY, namespace); //NOI18N
//        innerResults.put(namespace, new Result(source, root));
//        return root;
//    }

    private static String getTagNamePrefix(SyntaxElement.Tag tag) {
        String tName = tag.getName();
        int colonPrefix = tName.indexOf(':');
        if (colonPrefix == -1) {
            return null;
        } else {
            return tName.substring(0, colonPrefix);
        }
    }

    public String getPublicID() {
        return getDoctypeDeclaration() != null ? getDoctypeDeclaration().getPublicIdentifier() : null;
    }

    public synchronized Declaration getDoctypeDeclaration() {
        if (declaration == null) {
            for (SyntaxElement e : getElements().items()) {
                if (e.type() == SyntaxElement.TYPE_DECLARATION) {
                    declaration = (Declaration) e;
                    break;
                }
            }
        }
        return declaration;
    }

    /** Returns a map of namespace URI to prefix used in the document
     * Not only globaly registered namespace (root tag) are taken into account.
     */
    // URI to prefix map
    @Deprecated
    public Map<String, String> getDeclaredNamespaces() {
        Map<String, List<String>> all = getAllDeclaredNamespaces();
        Map<String, String> firstPrefixOnly = new HashMap<String, String>();
        for (String namespace : all.keySet()) {
            List<String> prefixes = all.get(namespace);
            if (prefixes != null && prefixes.size() > 0) {
                firstPrefixOnly.put(namespace, prefixes.get(0));
            }
        }
        return firstPrefixOnly;
    }

    public synchronized Set<String> getAllDeclaredPrefixes() {
        if(allPrefixes == null) {
            allPrefixes = getAllDeclaredPrefixes();
        }
        return allPrefixes;
    }
    
    private Set<String> findAllDeclaredPrefixes() {
       HashSet<String> all = new HashSet<String>();
        for(List<String> prefixes : getAllDeclaredNamespaces().values()) {
            all.addAll(prefixes);
        }
        return all;
    }

    /**
     *
     * @return map of namespace URI to a List of prefixes. The prefixes in the list
     * are sorted according to their occurrences in the document.
     */
    public synchronized Map<String, List<String>> getAllDeclaredNamespaces() {
        if (namespaces == null) {
            this.namespaces = new HashMap<String, List<String>>();
            for (SyntaxElement se : getElements().items()) {
                if (se.type() == SyntaxElement.TYPE_TAG) {
                    SyntaxElement.Tag tag = (SyntaxElement.Tag) se;
                    for (SyntaxElement.TagAttribute attr : tag.getAttributes()) {
                        String attrName = attr.getName();
                        if (attrName.startsWith("xmlns")) { //NOI18N
                            int colonIndex = attrName.indexOf(':'); //NOI18N
                            String nsPrefix = colonIndex == -1 ? null : attrName.substring(colonIndex + 1);
                            String value = attr.getValue();
                            //do not overwrite already existing entry
                            String key = dequote(value);
                            List<String> prefixes = namespaces.get(key);
                            if (prefixes == null) {
                                prefixes = new LinkedList<String>();
                                prefixes.add(nsPrefix);
                                namespaces.put(key, prefixes);
                            } else {
                                //already existing list of prefixes for the namespace
                                if (prefixes.contains(key)) {
                                    //just relax
                                } else {
                                    prefixes.add(nsPrefix);
                                }
                            }
                        }
                    }
                }
            }
        }

        return namespaces;
    }

    private static String dequote(String text) {
        if (text.length() < 2) {
            return text;
        } else {
            if ((text.charAt(0) == '\'' || text.charAt(0) == '"')
                    && (text.charAt(text.length() - 1) == '\'' || text.charAt(text.length() - 1) == '"')) {
                return text.substring(1, text.length() - 1);
            }
        }
        return text;
    }

    private static class LocalSourceContext {

        private Collection<IgnoredArea> ignoredAreas;
        private List<SyntaxElement> filtered;

        public LocalSourceContext(Collection<IgnoredArea> ignoredAreas, List<SyntaxElement> filtered) {
            this.ignoredAreas = ignoredAreas;
            this.filtered = filtered;
        }

        public List<SyntaxElement> getFiltered() {
            return filtered;
        }

        public Collection<IgnoredArea> getIgnoredAreas() {
            return ignoredAreas;
        }
    }

    private static interface TagsFilter {

        public boolean accepts(SyntaxElement.Tag tag, String prefix);
    }

    private static class IgnoredArea {

        int from, to;

        public IgnoredArea(int from, int to) {
            this.from = from;
            this.to = to;
        }
    }
    
    //it seems to be better (more memory/but much faster) to create a clone of the source
    //sequence w/ the specifed areas being ws-paced than doing this dynamically.
    private static final char REPLACE_CHAR = ' '; //ws //NOI18N

    private static CharSequence clearIgnoredAreas(CharSequence source, Collection<IgnoredArea> areas) {
        StringBuilder sb = new StringBuilder(source);
        for (IgnoredArea area : areas) {
            for (int i = area.from; i < area.to; i++) {
                sb.setCharAt(i, REPLACE_CHAR);
            }
        }
        return sb;
    }
}
