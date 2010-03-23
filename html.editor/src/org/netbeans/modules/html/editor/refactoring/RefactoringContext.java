/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.refactoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.css.refactoring.api.CssRefactoring;
import org.netbeans.modules.css.refactoring.api.EntryHandle;
import org.netbeans.modules.css.refactoring.api.RefactoringElementType;
import org.netbeans.modules.html.editor.api.Utils;
import org.netbeans.modules.html.editor.indexing.HtmlFileModel;
import org.netbeans.modules.html.editor.indexing.HtmlLinkEntry;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.FileReference;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public class RefactoringContext {

    private static final String CSS_MIME_TYPE = "text/x-css";//NOI18N
    private FileObject file;
    private Document document;
    private int from, to;
    private HtmlFileModel model;
    private List<InlinedStyleInfo> inlinedStyles;
    private List<OffsetRange> existingEmbeddedCssSections;
    private List<HtmlLinkEntry> linkedExternalStylesheets;
    private Map<InlinedStyleInfo, ResolveDeclarationItem> idSelectorsToResolve;
    private Map<InlinedStyleInfo, ResolveDeclarationItem> classSelectorsToResolve;

    public static RefactoringContext create(FileObject file, Document document, int from, int to) throws ParseException {
        //find inlined styles - just lexical
        List<InlinedStyleInfo> inlinedStyles = findInlinedStyles(document, from, to);

        HtmlFileModel model = new HtmlFileModel(Source.create(document));

        //find all references to external stylesheets
        List<HtmlLinkEntry> references = model.getReferences();
        //filter out stylesheets
        List<HtmlLinkEntry> cssOnlyLinks = new LinkedList<HtmlLinkEntry>();
        for (HtmlLinkEntry linkEntry : references) {
            FileReference ref = linkEntry.getFileReference();
            if (ref != null) {
                FileObject linkTarget = ref.target();
                if (CSS_MIME_TYPE.equals(linkTarget.getMIMEType())) {
                    cssOnlyLinks.add(linkEntry);
                }
            }
        }

        //find all css embedded sections in the file (<style>...</style>)
        List<OffsetRange> cssEmbeddedSections = model.getEmbeddedCssSections();

        Map<InlinedStyleInfo, ResolveDeclarationItem> ids2resolve = getUnresolvedSelectorDeclarations(RefactoringElementType.ID, inlinedStyles, file);
        Map<InlinedStyleInfo, ResolveDeclarationItem> classes2resolve = getUnresolvedSelectorDeclarations(RefactoringElementType.CLASS, inlinedStyles, file);

        return new RefactoringContext(file, document, from, to, model,
                inlinedStyles, cssEmbeddedSections, cssOnlyLinks, ids2resolve, classes2resolve);
    }

    private RefactoringContext(FileObject file, Document document, int from, int to,
            HtmlFileModel model,
            List<InlinedStyleInfo> inlinedStyles, /* document range */
            List<OffsetRange> existingEmbeddedCssSections, /* ast range */
            List<HtmlLinkEntry> externalStyleSheets, /* ast range */
            Map<InlinedStyleInfo, ResolveDeclarationItem> ids2resolve,
            Map<InlinedStyleInfo, ResolveDeclarationItem> classes2resolve) {
        this.file = file;
        this.document = document;
        this.from = from;
        this.to = to;
        this.model = model;
        this.inlinedStyles = inlinedStyles;
        this.existingEmbeddedCssSections = existingEmbeddedCssSections;
        this.linkedExternalStylesheets = externalStyleSheets;
        this.idSelectorsToResolve = ids2resolve;
        this.classSelectorsToResolve = classes2resolve;
    }

    public FileObject getFile() {
        return file;
    }

    public Document getDocument() {
        return document;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    /** note: document ranges */
    public List<InlinedStyleInfo> getInlinedStyles() {
        return inlinedStyles;
    }

    /** note: ast ranges */
    public List<OffsetRange> getExistingEmbeddedCssSections() {
        return existingEmbeddedCssSections == null
                ? Collections.<OffsetRange>emptyList()
                : existingEmbeddedCssSections;
    }

    public List<HtmlLinkEntry> getLinkedExternalStylesheets() {
        return linkedExternalStylesheets == null
                ? Collections.<HtmlLinkEntry>emptyList()
                : linkedExternalStylesheets;
    }

    public HtmlFileModel getModel() {
        return model;
    }

    public OffsetRange getDocumentRange(OffsetRange astRange) {
        Snapshot snap = getModel().getSnapshot();
        int dfrom = snap.getOriginalOffset(astRange.getStart());
        int dto = snap.getOriginalOffset(astRange.getEnd());
        return dfrom == -1 || dto == -1 ? null : new OffsetRange(dfrom, dto);
    }

    public Map<InlinedStyleInfo, ResolveDeclarationItem> getIdSelectorsToResolve() {
        return idSelectorsToResolve;
    }

    public Map<InlinedStyleInfo, ResolveDeclarationItem> getClassSelectorsToResolve() {
        return classSelectorsToResolve;
    }

    static List<InlinedStyleInfo> findInlinedStyles(Document doc, int from, int to) {
        List<InlinedStyleInfo> found = new LinkedList<InlinedStyleInfo>();
        TokenSequence<HTMLTokenId> ts = Utils.getJoinedHtmlSequence(doc, from);
        if (ts == null) {
            //XXX - try to search backward and forward to find some html code???
            return Collections.emptyList();
        }
        //the joined ts is moved to the from offset and a token already selected (moveNext/Previous() == true)
        //seek for all tag's attributes with css embedding representing an inlined style
        String tag = null;
        String attr = null;
        String styleAttr = null;
        int styleAttrOffset = -1;
        int classValueAppendOffset = -1;
        int attrOffset = -1;
        String tagsClass = null;
        String tagsId = null;
        String value = null;
        OffsetRange range = null;
        do {
            Token<HTMLTokenId> t = ts.token();
            if (t.id() == HTMLTokenId.TAG_OPEN) {
                tag = t.text().toString();
                attr = styleAttr = tagsClass = tagsId = null;
                attrOffset = classValueAppendOffset = styleAttrOffset = -1;
                range = null;
                value = null;
            } else if (t.id() == HTMLTokenId.TAG_CLOSE_SYMBOL) {
                //closing tag, produce the info
                if (tag != null && range != null) {
                    //some inlined code found
                    found.add(new InlinedStyleInfo(tag, tagsClass, tagsId, styleAttr, styleAttrOffset, classValueAppendOffset, range, value));
                    tag = attr = styleAttr = tagsClass = tagsId = null;
                    attrOffset = styleAttrOffset = classValueAppendOffset = -1;
                }
            } else if (t.id() == HTMLTokenId.ARGUMENT) {
                attr = t.text().toString();
                attrOffset = ts.offset();
            } else if (t.id() == HTMLTokenId.VALUE_CSS) {
                //check if this is an inlined code, not class or id representation
                String csstype = (String) t.getProperty(HTMLTokenId.VALUE_CSS_TOKEN_TYPE_PROPERTY);
                if (csstype == null) {
                    //inlined code
                    int diff = WebUtils.isValueQuoted(t.text()) ? 1 : 0;
                    range = new OffsetRange(ts.offset() + diff, ts.offset() + t.length() - diff);
                    value = WebUtils.unquotedValue(t.text().toString());
                    styleAttrOffset = attrOffset;
                    styleAttr = attr;
                } else {
                    //class or id attribute value
                    if ("class".equalsIgnoreCase(attr)) { //NOI18N
                        classValueAppendOffset = ts.offset() + t.length() - (WebUtils.isValueQuoted(t.text()) ? 1 : 0);
                        tagsClass = WebUtils.unquotedValue(t.text());
                    } else if ("id".equalsIgnoreCase(attr)) { //NOI18N
                        tagsId = WebUtils.unquotedValue(t.text());
                    }
                }
            } else if (t.id() == HTMLTokenId.VALUE_CSS) {
                //TODO use TagMetadata for getting the info a the attribute represents a css or not
            }

        } while (ts.moveNext() && ts.offset() <= to);

        return found;
    }

    //note: only the inlined infos from tags with an id selector are present in the map
    private static Map<InlinedStyleInfo, ResolveDeclarationItem> getUnresolvedSelectorDeclarations(
            RefactoringElementType type,
            Collection<InlinedStyleInfo> infos,
            FileObject file) {

        Map<InlinedStyleInfo, ResolveDeclarationItem> toResolve = new HashMap<InlinedStyleInfo, ResolveDeclarationItem>();
        for (InlinedStyleInfo si : infos) {

            String element = getElementNameByType(si, type);
            if (element != null) {
                //there's already an id selector, lets add the refactored styleinto it
                Map<FileObject, Collection<EntryHandle>> declarations =
                        CssRefactoring.findAllOccurances(element, type, file, true);

                ResolveDeclarationItem item = new ResolveDeclarationItemImpl(si, type, declarations);
                if (!item.getPossibleDeclarations().isEmpty()) {
                    toResolve.put(si, item);
                }
            }

        }

        return toResolve;
    }

    private static String getElementNameByType(InlinedStyleInfo si, RefactoringElementType type) {
        String element;
        switch (type) {
            case CLASS:
                element = si.getTagsClass();
                break;
            case ID:
                element = si.getTagsId();
                break;
            default:
                element = null;
                assert false;
        }
        return element;
    }

    private static class ResolveDeclarationItemImpl extends ResolveDeclarationItem {

        private InlinedStyleInfo si;
        private List<DeclarationItem> declarations;
        private RefactoringElementType type;

        public ResolveDeclarationItemImpl(InlinedStyleInfo si, RefactoringElementType type, Map<FileObject, Collection<EntryHandle>> declarationsMap) {
            this.type = type;
            this.si = si;
            declarations = new ArrayList<DeclarationItem>();
            //convert
            for (FileObject file : declarationsMap.keySet()) {
                for (EntryHandle handle : declarationsMap.get(file)) {
                    declarations.add(new DeclarationItem(handle, file));
                }
            }
        }

        @Override
        public String getName() {
            return getElementNameByType(si, type);
        }

        @Override
        public List<DeclarationItem> getPossibleDeclarations() {
            return declarations;
        }
    }
}
