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
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.css.refactoring.api.CssRefactoring;
import org.netbeans.modules.css.refactoring.api.RefactoringElementType;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.html.editor.api.Utils;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension;
import org.netbeans.modules.html.editor.completion.AttrValuesCompletion;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;

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
        ts.move(caretOffset);
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

                                Map<FileObject, Collection<int[]>> occurances = CssRefactoring.findAllOccurances(unquotedValue, type, file, true); //non virtual element only - this means only css declarations, not usages in html code
                                if(occurances == null) {
                                    return ;
                                }

                                DeclarationLocation dl = null;
                                for (FileObject f : occurances.keySet()) {
                                    Collection<int[]> elementsRanges = occurances.get(f);
                                    for (int[] range : elementsRanges) {
                                        int startOffset = range[0];
                                        int startOffsetLine = range[1];
                                        //ugly DeclarationLocation alternatives handling workaround - one of the
                                        //locations simply must be "main" !?!?!
                                        if (dl == null) {
                                            dl = new DeclarationLocation(f, startOffset);
                                        } else {
                                            DeclarationLocation dloc = new DeclarationLocation(f, startOffset);
                                            HtmlDeclarationFinder.AlternativeLocation aloc = new HtmlDeclarationFinder.AlternativeLocationImpl(dloc, unquotedValue, startOffsetLine);
                                            dl.addAlternative(aloc);
                                        }
                                    }
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
        private String elementName;
        private int entryLine;

        public AlternativeLocationImpl(DeclarationLocation location, String elementName, int entryLine) {
            this.location = location;
            this.elementName = elementName;
            this.entryLine = entryLine;
        }

        @Override
        public ElementHandle getElement() {
            return new HtmlElementHandle(null, location.getFileObject());
        }

        @Override
        public String getDisplayHtml(HtmlFormatter formatter) {
//            return "<b>" + elementName + "</b> at line " + entryLine + " in " + location.getFileObject().getNameExt();
            return "<b>" + elementName + "</b> at " + location.getOffset() + " in " + location.getFileObject().getNameExt();
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
}
