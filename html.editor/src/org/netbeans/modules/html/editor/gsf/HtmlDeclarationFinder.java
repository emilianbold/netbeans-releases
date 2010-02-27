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
package org.netbeans.modules.html.editor.gsf;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.css.refactoring.api.CssRefactoring;
import org.netbeans.modules.css.refactoring.api.EntryHandle;
import org.netbeans.modules.css.refactoring.api.RefactoringElementType;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.html.editor.api.Utils;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension;
import org.netbeans.modules.html.editor.completion.AttrValuesCompletion;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.modules.web.common.spi.ProjectWebRootQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * just CSL to HtmlExtension bridge
 *
 * @author marekfukala
 */
public class HtmlDeclarationFinder implements DeclarationFinder {

    private static final String XHTML_MIMETYPE = "text/xhtml"; //NOI18N

    /**
     * Find the declaration for the program element that is under the caretOffset
     * Return a Set of regions that should be renamed if the element under the caret offset is
     * renamed.
     *
     * Return {@link DeclarationLocation#NONE} if the declaration can not be found, otherwise return
     *   a valid DeclarationLocation.
     */
    @Override
    public DeclarationLocation findDeclaration(ParserResult info, int caretOffset) {
        DeclarationLocation loc = findCoreHtmlDeclaration(info, caretOffset);
        if (loc != null) {
            return loc;
        }

        for (HtmlExtension ext : HtmlExtension.getRegisteredExtensions(info.getSnapshot().getSource().getMimeType())) {
            loc = ext.findDeclaration(info, caretOffset);
            if (loc != null) {
                return loc;
            }
        }
        return DeclarationLocation.NONE;
    }

    /**
     * Check the caret offset in the document and determine if it is over a span
     * of text that should be hyperlinkable ("Go To Declaration" - in other words,
     * locate the reference and return it. When the user drags the mouse with a modifier
     * key held this will be hyperlinked, and so on.
     * <p>
     * Remember that when looking up tokens in the token hierarchy, you will get the token
     * to the right of the caret offset, so check for these conditions
     * {@code (sequence.move(offset); sequence.offset() == offset)} and check both
     * sides such that placing the caret between two tokens will match either side.
     *
     * @return {@link OffsetRange#NONE} if the caret is not over a valid reference span,
     *   otherwise return the character range for the given hyperlink tokens
     */
    @Override
    public OffsetRange getReferenceSpan(Document doc, int caretOffset) {
        OffsetRange range = getCoreHtmlReferenceSpan(doc, caretOffset);
        if (range != null) {
            return range;
        }

        //html extensions
        String mimeType = NbEditorUtilities.getMimeType(doc);
        for (HtmlExtension ext : HtmlExtension.getRegisteredExtensions(mimeType)) {
            range = ext.getReferenceSpan(doc, caretOffset);
            if (range != null) {
                return range;
            }
        }
        return OffsetRange.NONE;
    }

    private OffsetRange getCoreHtmlReferenceSpan(Document doc, int caretOffset) {
        final TokenSequence<HTMLTokenId> ts = Utils.getJoinedHtmlSequence(doc, caretOffset);
        if (ts == null) {
            return null;
        }

        //tag attribute value hyperlinking
        if (ts.token().id() == HTMLTokenId.VALUE) {
            return new AttributeValueAction<OffsetRange>(ts) {

                @Override
                public OffsetRange resolve() {
                    if (tagName != null && attrName != null) {
                        AttrValuesCompletion support = AttrValuesCompletion.getSupport(tagName, attrName);
                        if (AttrValuesCompletion.FILE_NAME_SUPPORT == support) {
                            //some file to hyperlink to
                            return valueRange;
                        }
                    }
                    return null;
                }
            }.run();

        } else if (ts.token().id() == HTMLTokenId.VALUE_CSS) {
            //css class or id hyperlinking 
            int quotesDiff = WebUtils.isValueQuoted(ts.token().text().toString()) ? 1 : 0;
            OffsetRange range = new OffsetRange(ts.offset() + quotesDiff, ts.offset() + ts.token().length() - quotesDiff);
            return range;
        }

        return null;
    }

    private DeclarationLocation findCoreHtmlDeclaration(final ParserResult info, final int caretOffset) {
        final FileObject file = info.getSnapshot().getSource().getFileObject();
        final TokenSequence<HTMLTokenId> ts = info.getSnapshot().getTokenHierarchy().tokenSequence(HTMLTokenId.language());
        if (ts == null) {
            return null;
        }
        int astCaretOffset = info.getSnapshot().getEmbeddedOffset(caretOffset);
        if(astCaretOffset == -1) {
            return null;
        }

        ts.move(astCaretOffset);
        if (!ts.moveNext() && !ts.movePrevious()) {
            return null;
        }

        //tag attribute value hyperlinking
        if (ts.token().id() == HTMLTokenId.VALUE) {
            return new AttributeValueAction<DeclarationLocation>(ts) {

                @Override
                public DeclarationLocation resolve() {
                    if (tagName != null && attrName != null) {
                        AttrValuesCompletion support = AttrValuesCompletion.getSupport(tagName, attrName);
                        if (AttrValuesCompletion.FILE_NAME_SUPPORT == support) {
                            //some file to hyperlink to
                            FileObject resolved = WebUtils.resolve(info.getSnapshot().getSource().getFileObject(), unquotedValue);
                            if (resolved != null) {
                                return new DeclarationLocation(resolved, 0);
                            }
                        }
                    }
                    return null;
                }
            }.run();

        } else if (ts.token().id() == HTMLTokenId.VALUE_CSS) {
            //css class or id hyperlinking

            //I need to somehow determine the type of the selector - whether it's
            //a class or an id. There are two (bad) ways to do this:
            //1. either get the original html token containing the meta info
            //2. or parse the file and get css parser result for given offset
            //
            //both may cause some offset inconsistencies because of the lack of locking
            //
            //#1 seems to be at least faster
            final Document doc = info.getSnapshot().getSource().getDocument(true);
            final AtomicReference<DeclarationLocation> ret = new AtomicReference<DeclarationLocation>();
            doc.render(new Runnable() {

                @Override
                public void run() {
                    TokenSequence ts = Utils.getJoinedHtmlSequence(doc, caretOffset);
                    if (ts != null && ts.token() != null) {
                        //seems to be valid and properly positioned
                        Token<HTMLTokenId> valueToken = ts.token();
                        if (valueToken.id() == HTMLTokenId.VALUE_CSS) {
                            //the value_css token contains a metainfo about the type of its css embedding
                            String cssTokenType = (String) valueToken.getProperty(HTMLTokenId.VALUE_CSS_TOKEN_TYPE_PROPERTY);
                            String unquotedValue = WebUtils.unquotedValue(valueToken.text().toString());
                            if (cssTokenType != null) {
                                RefactoringElementType type;
                                if (HTMLTokenId.VALUE_CSS_TOKEN_TYPE_CLASS.equals(cssTokenType)) {
                                    //class selector
                                    type = RefactoringElementType.CLASS;
                                } else if (HTMLTokenId.VALUE_CSS_TOKEN_TYPE_ID.equals(cssTokenType)) { // instances comparison is ok here!
                                    //id selector
                                    type = RefactoringElementType.ID;
                                } else {
                                    type = null;
                                    assert false; //something very bad is going on!
                                }

                                Map<FileObject, Collection<EntryHandle>> occurances = CssRefactoring.findAllOccurances(unquotedValue, type, file, true); //non virtual element only - this means only css declarations, not usages in html code
                                if(occurances == null) {
                                    return ;
                                }

                                DeclarationLocation dl = null;
                                for (FileObject f : occurances.keySet()) {
                                    Collection<EntryHandle> entries = occurances.get(f);
                                    for (EntryHandle entryHandle : entries) {
                                        //grrr, the main declarationlocation must be also added to the alternatives
                                        //if there are more than one
                                        DeclarationLocation dloc = new DeclarationLocation(f, entryHandle.entry().getDocumentRange().getStart());
                                        if (dl == null) {
                                            //ugly DeclarationLocation alternatives handling workaround - one of the
                                            //locations simply must be "main"!!!
                                            dl = dloc;
                                        }
                                        HtmlDeclarationFinder.AlternativeLocation aloc = new HtmlDeclarationFinder.AlternativeLocationImpl(dloc, entryHandle);
                                        dl.addAlternative(aloc);
                                    }
                                }

                                //and finally if there was just one entry, remove the "alternative"
                                if(dl != null && dl.getAlternativeLocations().size() == 1) {
                                    dl.getAlternativeLocations().clear();
                                }

                                ret.set(dl);
                            }

                        } else {
                            //some bad guy modified the code meanwhile so the offsets aren't matching
                        }

                    }

                }
            });

            return ret.get();

        }


        return null;
    }

    private abstract class AttributeValueAction<T> {

        private TokenSequence<HTMLTokenId> ts;
        protected String tagName, attrName, unquotedValue;
        protected OffsetRange valueRange;

        public AttributeValueAction(TokenSequence<HTMLTokenId> ts) {
            this.ts = ts;
        }

        public abstract T resolve();

        public T run() {
            parseSquence();
            return resolve();
        }

        private void parseSquence() {
            //find attribute name
            int quotesDiff = WebUtils.isValueQuoted(ts.token().text().toString()) ? 1 : 0;
            unquotedValue = WebUtils.unquotedValue(ts.token().text().toString());
            valueRange = new OffsetRange(ts.offset() + quotesDiff, ts.offset() + ts.token().length() - quotesDiff);
            while (ts.movePrevious()) {
                HTMLTokenId id = ts.token().id();
                if (id == HTMLTokenId.ARGUMENT && attrName == null) {
                    attrName = ts.token().text().toString();
                } else if (id == HTMLTokenId.TAG_OPEN) {
                    tagName = ts.token().text().toString();
                    break;
                } else if (id == HTMLTokenId.TAG_OPEN_SYMBOL || id == HTMLTokenId.TAG_CLOSE_SYMBOL || id == HTMLTokenId.TEXT) {
                    break;
                }
            }
        }
    }

    private static class AlternativeLocationImpl implements AlternativeLocation {

        private DeclarationLocation location;
        private EntryHandle entryHandle;

        public AlternativeLocationImpl(DeclarationLocation location, EntryHandle entry) {
            this.location = location;
            this.entryHandle = entry;
        }

        @Override
        public ElementHandle getElement() {
            return CSS_SELECTOR_ELEMENT_HANDLE_SINGLETON;
        }

        @Override
        public String getDisplayHtml(HtmlFormatter formatter) {
            StringBuilder b = new StringBuilder();
            //colorize the 'current line text' a bit
            //find out if there's the opening curly bracket
            assert entryHandle.entry().getLineText() != null;
            int curlyBracketIndex = entryHandle.entry().getLineText().indexOf('{'); //NOI18N
            String croppedLineText = curlyBracketIndex == -1 ? entryHandle.entry().getLineText() : entryHandle.entry().getLineText().substring(0, curlyBracketIndex);

            b.append("<b><font color=007c00>");//NOI18N
            b.append(croppedLineText);
            b.append("</font></b> in "); //NOI18N

            //add a link to the file relative to the web root
            FileObject file = location.getFileObject();
            FileObject webRoot = ProjectWebRootQuery.getWebRoot(file);
            String path = webRoot == null ? file.getPath() : FileUtil.getRelativePath(webRoot, file);

            b.append(path);
            b.append(":"); //NOI18N
            b.append(entryHandle.entry().getLineOffset() + 1); //line offsets are counted from zero, but in editor lines starts with one.
            if(!entryHandle.isRelatedEntry()) {
                b.append(" <font color=ff0000>(");
                b.append(NbBundle.getMessage(HtmlDeclarationFinder.class, "MSG_Unrelated"));
                b.append(")</font>");
            }
            return b.toString();
        }

        @Override
        public DeclarationLocation getLocation() {
            return location;
        }

        @Override
        public int compareTo(AlternativeLocation o) {
            //compare according to the file paths
            return getComparableString(this).compareTo(getComparableString(o));
        }

        private static String getComparableString(AlternativeLocation loc) {
            return new StringBuilder()
                    .append(loc.getLocation().getOffset()) //offset
                    .append(loc.getLocation().getFileObject().getPath()).toString(); //filename
        }
    }

    //useless class just because we need to put something into the AlternativeLocation to be
    //able to get some icon from it
    private static CssSelectorElementHandle CSS_SELECTOR_ELEMENT_HANDLE_SINGLETON = new CssSelectorElementHandle();

    private static class CssSelectorElementHandle implements ElementHandle {

        @Override
        public FileObject getFileObject() {
            return null;
        }

        @Override
        public String getMimeType() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getIn() {
            return null;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.RULE;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.emptySet();
        }

        @Override
        public boolean signatureEquals(ElementHandle handle) {
            return false;
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult result) {
            return OffsetRange.NONE;
        }

    }
}
