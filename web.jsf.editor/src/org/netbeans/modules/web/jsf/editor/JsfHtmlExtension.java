/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.web.jsf.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.servlet.jsp.tagext.TagData;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.editor.ext.html.parser.AstNodeVisitor;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintsProvider.HintsManager;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.web.common.taginfo.AttrValueType;
import org.netbeans.modules.web.common.taginfo.LibraryMetadata;
import org.netbeans.modules.web.common.taginfo.TagAttrMetadata;
import org.netbeans.modules.web.common.taginfo.TagMetadata;
import org.netbeans.modules.web.jsf.editor.completion.JsfCompletionItem;
import org.netbeans.modules.web.jsf.editor.facelets.CompositeComponentLibrary;
import org.netbeans.modules.web.jsf.editor.facelets.FaceletsLibrary;
import org.netbeans.modules.web.jsf.editor.facelets.FaceletsLibraryMetadata;
import org.netbeans.modules.web.jsf.editor.hints.HintsRegistry;
import org.netbeans.modules.web.jsf.editor.index.CompositeComponentModel;
import org.netbeans.modules.web.jsf.editor.tld.TldLibrary;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.lexer.MutableTextInput;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * XXX should be rather done by dynamic artificial embedding creation.
 * The support then can be implemented by CSL language mapped to the
 * language mimetype.
 *
 * @author marekfukala
 */
public class JsfHtmlExtension extends HtmlExtension {

    private static boolean activated = false;
    private static final String EL_ENABLED_KEY = "el_enabled"; //NOI18N

    static synchronized void activate() {
        if (!activated) {
            HtmlExtension.register("text/xhtml", new JsfHtmlExtension()); //NOI18N
            activated = true;
        }
    }

    @Override
    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights(HtmlParserResult result, SchedulerEvent event) {
        final Map<OffsetRange, Set<ColoringAttributes>> highlights = new HashMap<OffsetRange, Set<ColoringAttributes>>();

        //highlight JSF tags
        highlightJsfTags(result, highlights);

        //check if the EL is enabled in the file and enables it if not
        checkELEnabled(result);

        return highlights;

    }

    public void checkELEnabled(HtmlParserResult result) {
        Document doc = result.getSnapshot().getSource().getDocument(true);
        InputAttributes inputAttributes = (InputAttributes) doc.getProperty(InputAttributes.class);
        if (inputAttributes == null) {
            inputAttributes = new InputAttributes();
            doc.putProperty(InputAttributes.class, inputAttributes);
        }
        Language xhtmlLang = Language.find("text/xhtml"); //NOI18N
        if (inputAttributes.getValue(LanguagePath.get(xhtmlLang), EL_ENABLED_KEY) == null) {
            inputAttributes.setValue(LanguagePath.get(xhtmlLang), EL_ENABLED_KEY, new Object(), false);

            //refresh token hierarchy so the EL becomes lexed
            recolor(doc);
        }
    }

    private void recolor(final Document doc) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                NbEditorDocument nbdoc = (NbEditorDocument) doc;
                nbdoc.extWriteLock();
                try {
                    MutableTextInput mti = (MutableTextInput) doc.getProperty(MutableTextInput.class);
                    if (mti != null) {
                        mti.tokenHierarchyControl().rebuild();
                    }
                } finally {
                    nbdoc.extWriteUnlock();
                }
            }
        });
    }

    private void highlightJsfTags(HtmlParserResult result, final Map<OffsetRange, Set<ColoringAttributes>> highlights) {
        final Snapshot snapshot = result.getSnapshot();
        Source source = snapshot.getSource();
        JsfSupport jsfs = JsfSupport.findFor(source);
        if (jsfs == null) {
            return;
        }
        Map<String, FaceletsLibrary> libs = jsfs.getFaceletsLibraries();

        Map<String, String> nss = result.getNamespaces();

        //1. resolve which declared libraries are available on classpath

        //2. resolve which tag prefixes are registered for libraries, either available or missing
        // add hint for missing library

        for (String namespace : nss.keySet()) {

            AstNode root = result.root(namespace);
            if (root != null) {
                final FaceletsLibrary tldl = libs.get(namespace);
                AstNodeUtils.visitChildren(root, new AstNodeVisitor() {

                    public void visit(AstNode node) {
                        if (node.type() == AstNode.NodeType.OPEN_TAG ||
                                node.type() == AstNode.NodeType.ENDTAG) {

                            if (node.getNamespacePrefix() != null) {
                                Set<ColoringAttributes> coloring = tldl == null ? ColoringAttributes.CLASS_SET : ColoringAttributes.METHOD_SET;
                                try {
                                    highlight(snapshot, node, highlights, coloring);
                                } catch (BadLocationException ex) {
                                    //just ignore
                                }
                            }
                        }
                    }
                });
            }
        }

    }

    private void highlight(Snapshot s, AstNode node, Map<OffsetRange, Set<ColoringAttributes>> hls, Set<ColoringAttributes> cas) throws BadLocationException {
        // "<div" id='x'> part
        int prefixLen = node.type() == AstNode.NodeType.OPEN_TAG ? 1 : 2; //"<" open; "</" close
        hls.put(getDocumentOffsetRange(s, node.startOffset(), node.startOffset() + node.name().length() + prefixLen /* tag open symbol len */),
                cas);
        // <div id='x'">" part
        hls.put(getDocumentOffsetRange(s, node.endOffset() - 1, node.endOffset()),
                cas);

    }

    private OffsetRange getDocumentOffsetRange(Snapshot s, int astFrom, int astTo) throws BadLocationException {
        int from = s.getOriginalOffset(astFrom);
        int to = s.getOriginalOffset(astTo);

        if(from == -1 || to == -1) {
            throw new BadLocationException("Cannot convert snapshot offset to document offset", -1); //NOI18N
        }

        return new OffsetRange(from, to);
    }

    @Override
    public List<CompletionItem> completeOpenTags(CompletionContext context) {
        HtmlParserResult result = context.getResult();
        Source source = result.getSnapshot().getSource();
        JsfSupport jsfs = JsfSupport.findFor(source);
        if (jsfs == null) {
            return Collections.emptyList();
        }
        Map<String, FaceletsLibrary> libs = jsfs.getFaceletsLibraries();
        //uri to prefix map
        Map<String, String> declaredNS = result.getNamespaces();

        List<CompletionItem> items = new ArrayList<CompletionItem>();

        int colonIndex = context.getPrefix().indexOf(':');
        if (colonIndex == -1) {
            //editing namespace or tag w/o ns
            //offer all tags
            for (FaceletsLibrary lib : libs.values()) {
                String declaredPrefix = declaredNS.get(lib.getNamespace());
                if (declaredPrefix == null) {
                    //undeclared prefix, try to match with default library prefix
                    if (lib.getDefaultPrefix() != null && lib.getDefaultPrefix().startsWith(context.getPrefix())) {
                        items.addAll(queryLibrary(context, lib, lib.getDefaultPrefix(), true));
                    }
                } else {
                    items.addAll(queryLibrary(context, lib, declaredPrefix, false));
                }
            }
        } else {
            String tagNamePrefix = context.getPrefix().substring(0, colonIndex);
            //find a namespace according to the prefix
            String namespace = getUriForPrefix(tagNamePrefix, declaredNS);
            if (namespace == null) {
                //undeclared prefix, check if a taglib contains it as
                //default prefix. If so, offer it in the cc w/ tag autoimport function
                for (FaceletsLibrary lib : libs.values()) {
                    if (lib.getDefaultPrefix() != null && lib.getDefaultPrefix().equals(tagNamePrefix)) {
                        //match
                        items.addAll(queryLibrary(context, lib, tagNamePrefix, true));
                    }
                }

            } else {
                //query only associated lib
                FaceletsLibrary lib = libs.get(namespace);
                if (lib == null) {
                    //no such lib, exit
                    return Collections.emptyList();
                } else {
                    //query the library
                    items.addAll(queryLibrary(context, lib, tagNamePrefix, false));
                }
            }
        }

        //filter the items according to the prefix
        Iterator<CompletionItem> itr = items.iterator();
        while (itr.hasNext()) {
            if (!CharSequenceUtilities.startsWith(itr.next().getInsertPrefix(), context.getPrefix())) {
                itr.remove();
            }
        }

        return items;

    }

    private String getUriForPrefix(String prefix, Map<String, String> namespaces) {
        for (Entry<String, String> entry : namespaces.entrySet()) {
            if (prefix.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private Collection<CompletionItem> queryLibrary(CompletionContext context, FaceletsLibrary lib, String nsPrefix, boolean undeclared) {
        Collection<CompletionItem> items = new ArrayList<CompletionItem>();
        for (FaceletsLibrary.NamedComponent component : lib.getComponents()) {
            items.add(JsfCompletionItem.createTag(context.getCCItemStartOffset(), component, nsPrefix, undeclared));
        }

        return items;
    }

    @Override
    public List<CompletionItem> completeAttributes(CompletionContext context) {
        HtmlParserResult result = context.getResult();
        Source source = result.getSnapshot().getSource();
        JsfSupport jsfs = JsfSupport.findFor(source);
        if (jsfs == null) {
            return Collections.emptyList();
        }
        Map<String, FaceletsLibrary> libs = jsfs.getFaceletsLibraries();
        //uri to prefix map
        Map<String, String> declaredNS = result.getNamespaces();

        List<CompletionItem> items = new ArrayList<CompletionItem>();

        AstNode queriedNode = context.getCurrentNode();
        String nsPrefix = queriedNode.getNamespacePrefix();
        String tagName = queriedNode.getNameWithoutPrefix();

        String namespace = getUriForPrefix(nsPrefix, declaredNS);
        FaceletsLibrary flib = libs.get(namespace);
        if(flib == null) {
	    //The facelets library not found. This happens if one declares
	    //a namespace which is not matched to any existing library
            return Collections.emptyList();
        }
        
        TldLibrary.Tag tag = flib.getTag(tagName);
        if (tag != null) {
            Collection<TldLibrary.Attribute> attrs = tag.getAttributes();
            //TODO resolve help
            Collection<String> existingAttrNames = queriedNode.getAttributeKeys();

            for (TldLibrary.Attribute a : attrs) {
                String attrName = a.getName();
                if (!existingAttrNames.contains(attrName) ||
                        existingAttrNames.contains(context.getItemText())) {
                    //show only unused attributes except the one where the caret currently stays
                    //this is because of we need to show the item in the completion since
                    //use might want to see javadoc of already used attribute
                    items.add(JsfCompletionItem.createAttribute(attrName, context.getCCItemStartOffset(), flib, tag, a));
                }
            }

        }


        if (context.getPrefix().length() > 0) {
            //filter the items according to the prefix
            Iterator<CompletionItem> itr = items.iterator();
            while (itr.hasNext()) {
                if (!CharSequenceUtilities.startsWith(itr.next().getInsertPrefix(), context.getPrefix())) {
                    itr.remove();
                }
            }
        }

        return items;
    }

    @Override
    public List<CompletionItem> completeAttributeValue(CompletionContext context) {
        List<CompletionItem> items = new ArrayList<CompletionItem>();
        String ns = context.getCurrentNode().getNamespace();
        String attrName = context.getAttributeName();
        String tagName = context.getCurrentNode().getNameWithoutPrefix();
        LibraryMetadata lib = FaceletsLibraryMetadata.get(ns);

        if (lib != null){
            TagMetadata tag = lib.getTag(tagName);

            if (tag != null){
                TagAttrMetadata attr = tag.getAttribute(attrName);

                if (attr != null){
                    Collection<AttrValueType> valueTypes = attr.getValueTypes();

                    if (valueTypes != null){
                        for (AttrValueType valueType : valueTypes){
                            String[] possibleVals = valueType.getPossibleValues();

                            if (possibleVals != null){
                                for (String val : possibleVals){
                                    if (val.startsWith(context.getPrefix())){
                                        CompletionItem itm = HtmlCompletionItem.createAttributeValue(val,
                                                context.getCCItemStartOffset());
                                        
                                        items.add(itm);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        

        if (context.getAttributeName().toLowerCase(Locale.ENGLISH).startsWith("xmlns")) {
            //xml namespace completion for facelets namespaces
            HtmlParserResult result = context.getResult();
            Source source = result.getSnapshot().getSource();
            JsfSupport jsfs = JsfSupport.findFor(source);
            if (jsfs == null) {
                return Collections.emptyList();
            }

            Collection<String> nss = new ArrayList<String>(jsfs.getFaceletsLibraries().keySet());
            //add also xhtml ns to the completion
            nss.add(JsfUtils.XHTML_NS);
            for(String namespace : nss) {
                if(namespace.startsWith(context.getPrefix())) {
                    items.add(HtmlCompletionItem.createAttributeValue(namespace, context.getCCItemStartOffset(), !context.isValueQuoted()));
                }
            }
        }

        return items;
    }

    @Override
    public DeclarationLocation findDeclaration(ParserResult result, final int caretOffset) {
        assert result instanceof HtmlParserResult;

        HtmlParserResult htmlresult = (HtmlParserResult) result;
        Snapshot snapshot = result.getSnapshot();
        AstNode leaf = htmlresult.findLeaf(caretOffset);
        if (leaf.type() == AstNode.NodeType.OPEN_TAG) {
            String namespace = leaf.getNamespace();
            FaceletsLibrary lib = JsfSupport.findFor(result.getSnapshot().getSource()).getFaceletsLibraries().get(namespace);
            if (lib != null) {
                if (lib instanceof CompositeComponentLibrary) {
                    String tagName = leaf.getNameWithoutPrefix();
                    CompositeComponentLibrary.CompositeComponent component = (CompositeComponentLibrary.CompositeComponent) lib.getComponent(tagName);
                    if (component == null) {
                        return DeclarationLocation.NONE;
                    }
		    CompositeComponentModel model = component.getComponentModel();
                    FileObject file = model.getSourceFile();

                    //find to what exactly the user points, the AST doesn't contain attributes as nodes :-(
                    int astOffset = snapshot.getEmbeddedOffset(caretOffset);

                    int jumpOffset = 0;
                    TokenSequence htmlTs = snapshot.getTokenHierarchy().tokenSequence();
                    htmlTs.move(astOffset);
                    if (htmlTs.moveNext() || htmlTs.movePrevious()) {
                        if (htmlTs.token().id() == HTMLTokenId.TAG_OPEN) {
                            //jumpOffset = 0;
                        } else if (htmlTs.token().id() == HTMLTokenId.ARGUMENT) {
                            final String attributeName = htmlTs.token().text().toString();
                            //find the attribute in the interface

                            Source source = Source.create(file);
                            final int[] attrOffset = new int[1];
                            try {
                                ParserManager.parse(Collections.singleton(source), new UserTask() {

                                    @Override
                                    public void run(ResultIterator resultIterator) throws Exception {
                                        Result result = resultIterator.getParserResult(caretOffset);
                                        if (result instanceof HtmlParserResult) {
                                            HtmlParserResult hresult = (HtmlParserResult) result;
                                            AstNode root = hresult.root(JsfUtils.COMPOSITE_LIBRARY_NS);
                                            AstNodeUtils.visitChildren(root, new AstNodeVisitor() {

                                                public void visit(AstNode node) {
                                                    if (node.type() == AstNode.NodeType.OPEN_TAG && node.getNameWithoutPrefix().equals("interface")) {
                                                        for (AstNode child : node.children()) {
                                                            if (child.type() == AstNode.NodeType.OPEN_TAG && child.getNameWithoutPrefix().equals("attribute")) {
                                                                String nameAttrvalue = child.getUnqotedAttributeValue("name");
                                                                if (nameAttrvalue != null && nameAttrvalue.equals(attributeName)) {
                                                                    //we found it
                                                                    attrOffset[0] = child.startOffset(); //offset of the attribute tag is fine
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            } catch (ParseException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            jumpOffset = attrOffset[0];

                        }
                    }


                    if (file != null) {
                        return new DeclarationLocation(file, jumpOffset);
                    }

                } else {
                    //TODO - normal components hyperlinking - mostly nav. to java classes
                    }
            }

        }


        return DeclarationLocation.NONE;

    }

    @Override
    public OffsetRange getReferenceSpan(final Document doc, final int caretOffset) {
        TokenHierarchy th = TokenHierarchy.get(doc);
        List<TokenSequence> seqs = th.embeddedTokenSequences(caretOffset, false);
        TokenSequence ts = null;
        for (TokenSequence _ts : seqs) {
            if (_ts.language() == HTMLTokenId.language()) {
                ts = _ts;
                break;
            }
        }

        if (ts == null) {
            return OffsetRange.NONE;
        }

        ts.move(caretOffset);
        if (ts.moveNext() || ts.movePrevious()) {
            Token t = ts.token();
            if (t.id() == HTMLTokenId.TAG_OPEN) {
                if (CharSequenceUtilities.indexOf(t.text(), ':') != -1) {
                    return new OffsetRange(ts.offset(), ts.offset() + t.length());
                }
            } else if (t.id() == HTMLTokenId.ARGUMENT) {
                int from = ts.offset();
                int to = from + t.text().length();
                //try to find the tag and check if there is a prefix
                while (ts.movePrevious()) {
                    if (ts.token().id() == HTMLTokenId.TAG_OPEN) {
                        if (CharSequenceUtilities.indexOf(ts.token().text(), ':') != -1) {
                            return new OffsetRange(from, to);
                        } else {
                            break;
                        }
                    }
                }
            }
        }

        return OffsetRange.NONE;

    }

    @Override
    public void computeErrors(HintsManager manager, RuleContext context, List<Hint> hints, List<Error> unhandled) {
        //just delegate to the hints registry and add all gathered results
        hints.addAll(HintsRegistry.getDefault().gatherHints(context));
    }

    @Override
    public void computeSelectionHints(HintsManager manager, RuleContext context, List<Hint> hints, int start, int end) {
	//inject composite component support
	Hint injectCC = InjectCompositeComponent.getHint(context, start, end);
	if(injectCC != null) {
	    hints.add(injectCC);
	}
    }
}
