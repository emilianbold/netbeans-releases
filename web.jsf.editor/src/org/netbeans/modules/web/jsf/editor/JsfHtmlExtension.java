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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.editor.ext.html.parser.AstNodeVisitor;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.html.editor.completion.HtmlCompletionItem;
import org.netbeans.modules.html.editor.gsf.api.HtmlExtension;
import org.netbeans.modules.html.editor.gsf.api.HtmlParserResult;
import org.netbeans.modules.html.editor.xhtml.XhtmlElTokenId;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.web.jsf.editor.completion.JsfCompletionItem;
import org.netbeans.modules.web.jsf.editor.tld.TldLibrary;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.lexer.MutableTextInput;

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
            HtmlExtension.register(new JsfHtmlExtension());
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

    private void checkELEnabled(HtmlParserResult result) {
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
        if(jsfs == null) {
            return;
        }
        Map<String, TldLibrary> libs = jsfs.getLibraries();

        Map<String, String> nss = result.getNamespaces();

        //1. resolve which declared libraries are available on classpath

        //2. resolve which tag prefixes are registered for libraries, either available or missing
        // add hint for missing library

        for (String namespace : nss.keySet()) {

            if (JsfSupport.isJSFLibrary(namespace)) { //a) test if this is a jsf library
                AstNode root = result.root(namespace);
                final TldLibrary tldl = libs.get(namespace);
                AstNodeUtils.visitChildren(root, new AstNodeVisitor() {

                    public void visit(AstNode node) {
                        if (node.type() == AstNode.NodeType.OPEN_TAG ||
                                node.type() == AstNode.NodeType.ENDTAG) {

                            if (node.getNamespacePrefix() != null) {
                                Set<ColoringAttributes> coloring = tldl == null ? ColoringAttributes.CLASS_SET : ColoringAttributes.METHOD_SET;
                                highlight(snapshot, node, highlights, coloring);
                            }
                        }
                    }
                });
            }
        }

    }

    private void highlight(Snapshot s, AstNode node, Map<OffsetRange, Set<ColoringAttributes>> hls, Set<ColoringAttributes> cas) {
        // "<div" id='x'> part
        int prefixLen = node.type() == AstNode.NodeType.OPEN_TAG ? 1 : 2; //"<" open; "</" close
        hls.put(getDocumentOffsetRange(s, node.startOffset(), node.startOffset() + node.name().length() + prefixLen /* tag open symbol len */),
                cas);
        // <div id='x'">" part
        hls.put(getDocumentOffsetRange(s, node.endOffset() - 1, node.endOffset()),
                cas);

    }

    private OffsetRange getDocumentOffsetRange(Snapshot s, int astFrom, int astTo) {
        return new OffsetRange(s.getOriginalOffset(astFrom), s.getOriginalOffset(astTo));
    }

    @Override
    public List<CompletionItem> completeOpenTags(CompletionContext context) {
        HtmlParserResult result = context.getResult();
        Source source = result.getSnapshot().getSource();
        JsfSupport jsfs = JsfSupport.findFor(source);
         if(jsfs == null) {
            return Collections.emptyList();
        }
        Map<String, TldLibrary> libs = jsfs.getLibraries();
        //uri to prefix map
        Map<String, String> declaredNS = result.getNamespaces();

        List<CompletionItem> items = new ArrayList<CompletionItem>();

        int colonIndex = context.getPrefix().indexOf(':');
        if (colonIndex == -1) {
            //editing namespace or tag w/o ns
            //offer all tags
            for (TldLibrary lib : libs.values()) {
                String declaredPrefix = declaredNS.get(lib.getURI());
                if (declaredPrefix == null) {
                    //undeclared prefix, try to match with default library prefix
                    if (lib.getDefaultPrefix().startsWith(context.getPrefix())) {
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
                for (TldLibrary lib : libs.values()) {
                    if (lib.getDefaultPrefix().equals(tagNamePrefix)) {
                        //match
                        items.addAll(queryLibrary(context, lib, tagNamePrefix, true));
                    }
                }

            } else {
                //query only associated lib
                TldLibrary lib = libs.get(namespace);
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
            HtmlCompletionItem hci = (HtmlCompletionItem) itr.next();
            if (!hci.getItemText().startsWith(context.getPrefix())) {
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

    private Collection<CompletionItem> queryLibrary(CompletionContext context, TldLibrary lib, String nsPrefix, boolean undeclared) {
        Collection<CompletionItem> items = new ArrayList<CompletionItem>();
        for (TldLibrary.Tag tag : lib.getTags().values()) {
            String tagName = tag.getName();
            //TODO resolve help!!!
            items.add(JsfCompletionItem.createTag(nsPrefix + ":" + tagName, context.getCCItemStartOffset(), tag, lib, undeclared));
        }

        return items;
    }

    @Override
    public List<CompletionItem> completeAttributes(CompletionContext context) {
        HtmlParserResult result = context.getResult();
        Source source = result.getSnapshot().getSource();
        JsfSupport jsfs = JsfSupport.findFor(source);
        if(jsfs == null) {
            return Collections.emptyList();
        }
        Map<String, TldLibrary> libs = jsfs.getLibraries();
        //uri to prefix map
        Map<String, String> declaredNS = result.getNamespaces();

        List<CompletionItem> items = new ArrayList<CompletionItem>();

        AstNode queriedNode = context.getCurrentNode();
        String nsPrefix = queriedNode.getNamespacePrefix();
        String tagName = queriedNode.getNameWithoutPrefix();

        String namespace = getUriForPrefix(nsPrefix, declaredNS);
        TldLibrary lib = libs.get(namespace);

        if (lib != null) {
            TldLibrary.Tag tag = lib.getTags().get(tagName);
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
                        items.add(JsfCompletionItem.createAttribute(attrName, context.getCCItemStartOffset(), lib, tag, a));
                    }
                }

            }
        }

        if (context.getPrefix().length() > 0) {
            //filter the items according to the prefix
            Iterator<CompletionItem> itr = items.iterator();
            while (itr.hasNext()) {
                HtmlCompletionItem hci = (HtmlCompletionItem) itr.next();
                if (!hci.getItemText().startsWith(context.getPrefix())) {
                    itr.remove();
                }
            }
        }

        return items;
    }

    @Override
    public List<CompletionItem> completeAttributeValue(CompletionContext context) {
        return Collections.EMPTY_LIST;
    }
}
