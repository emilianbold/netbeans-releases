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
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNode.Attribute;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.html.editor.api.Utils;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.completion.AttrValuesCompletion;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

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
        if(loc != null) {
            return loc;
        }

        for(HtmlExtension ext : HtmlExtension.getRegisteredExtensions(info.getSnapshot().getSource().getMimeType())) {
            loc = ext.findDeclaration(info, caretOffset);
            if(loc != null) {
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
        if(range != null) {
            return range;
        }

        //html extensions
        String mimeType = NbEditorUtilities.getMimeType(doc);
        for(HtmlExtension ext : HtmlExtension.getRegisteredExtensions(mimeType)) {
            range = ext.getReferenceSpan(doc, caretOffset);
            if(range != null) {
                return range;
            }
        }
        return OffsetRange.NONE;
    }

    private OffsetRange getCoreHtmlReferenceSpan(Document doc, int caretOffset) {
        TokenSequence<HTMLTokenId> ts = Utils.getJoinedHtmlSequence(doc, caretOffset);
        if(ts == null) {
            return null;
        }

        //tag attribute value hyperlinking
        if(ts.token().id() == HTMLTokenId.VALUE) {
            //find attribute name
            int quotesDiff = WebUtils.isValueQuoted(ts.token().text().toString()) ? 1 : 0;
            OffsetRange range = new OffsetRange(ts.offset() + quotesDiff, ts.offset() + ts.token().length() - quotesDiff);
            String attrName = null;
            String tagName = null;
            while(ts.movePrevious()) {
                HTMLTokenId id = ts.token().id();
                if(id == HTMLTokenId.ARGUMENT && attrName == null) {
                    attrName = ts.token().text().toString();
                } else if(id == HTMLTokenId.TAG_OPEN) {
                    tagName = ts.token().text().toString();
                    break;
                } else if(id == HTMLTokenId.TAG_OPEN_SYMBOL || id == HTMLTokenId.TAG_CLOSE_SYMBOL || id == HTMLTokenId.TEXT) {
                    break;
                }
            }

            if(tagName != null && attrName != null) {
                AttrValuesCompletion support = AttrValuesCompletion.getSupport(tagName, attrName);
                if(AttrValuesCompletion.FILE_NAME_SUPPORT == support) {
                    //some file to hyperlink to
                    return range;
                }


            }
        }


        return null;
    }

    private DeclarationLocation findCoreHtmlDeclaration(ParserResult info, int caretOffset) {
        HtmlParserResult result = (HtmlParserResult)info;
        Snapshot snapshot = result.getSnapshot();
        final int astCaretOffset = snapshot.getEmbeddedOffset(caretOffset);
        if(astCaretOffset == -1) {
            return null; //cannot translate offset!
        }
        AstNode node = result.findLeaf(astCaretOffset);
        if(node == null) {
            return null; //no node!
        }
        if(node.type() == AstNode.NodeType.OPEN_TAG) {
            Collection<Attribute> attribs = node.getAttributes(new AstNode.AttributeFilter() {

                @Override
                //find the attribute at the caret position
                public boolean accepts(Attribute attribute) {
                    return attribute.valueOffset() <= astCaretOffset &&
                            (attribute.valueOffset() + attribute.value().length()) >= astCaretOffset;
                }
            });

            assert attribs.size() <= 1; //one or zero matches
            if(attribs.size() == 1) {
                Attribute attr = attribs.iterator().next();
                String value = attr.unquotedValue();
                FileObject resolved = WebUtils.resolve(info.getSnapshot().getSource().getFileObject(), value);
                if(resolved != null) {
                    return new DeclarationLocation(resolved, 0);
                }
            }

            
        }
        
        return null;
    }



}
