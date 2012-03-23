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
package org.netbeans.modules.html.editor.indexing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.api.completion.HtmlCompletionItem;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.completion.AttrValuesCompletion;
import org.netbeans.modules.html.editor.lib.api.tree.*;
import org.netbeans.modules.parsing.api.*;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.common.api.ValueCompletion;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author marekfukala
 */
public class HtmlFileModel {

    private static final String STYLE_TAG_NAME = "style"; //NOI18N
    
    private static final Logger LOGGER = Logger.getLogger(HtmlFileModel.class.getSimpleName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    
    private List<HtmlLinkEntry> references;
    private List<OffsetRange> embeddedCssSections;
    private HtmlParserResult parserResult;

    public HtmlFileModel(Source source) throws ParseException {
        ParserManager.parse(Collections.singletonList(source), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                ResultIterator ri = WebUtils.getResultIterator(resultIterator, HtmlKit.HTML_MIME_TYPE);
                if (ri != null) {
                    parserResult = (HtmlParserResult)ri.getParserResult();
                    init();
                }
            }
        });
    }

    public HtmlFileModel(HtmlParserResult parserResult) {
        this.parserResult = parserResult;
        init();
    }

    public HtmlParserResult getParserResult() {
        return parserResult;
    }

    public Snapshot getSnapshot() {
        return parserResult.getSnapshot();
    }

    public FileObject getFileObject() {
        return getSnapshot().getSource().getFileObject();
    }

    public List<HtmlLinkEntry> getReferences() {
        return references == null ? Collections.<HtmlLinkEntry>emptyList() : references;
    }

    public List<OffsetRange> getEmbeddedCssSections() {
        return embeddedCssSections == null ? Collections.<OffsetRange>emptyList() : embeddedCssSections;
    }

    /**
     *
     * @return true if the model is empty - nothing interesting found in the page.
     */
    public boolean isEmpty() {
        return null == references;
    }

    private List<HtmlLinkEntry> getReferencesCollectionInstance() {
        if (references == null) {
            references = new ArrayList<HtmlLinkEntry>();
        }
        return references;
    }

    private List<OffsetRange> getEmbeddedCssSectionsCollectionInstance() {
        if (embeddedCssSections == null) {
            embeddedCssSections = new ArrayList<OffsetRange>();
        }
        return embeddedCssSections;
    }

    private void init() {
        //XXX this scans only core html parse tree, what about the other namespaces????
        Node root = parserResult.root();
        if(root != null) {
            NodeUtils.visitChildren(root, new ReferencesSearch(), ElementType.OPEN_TAG);
        } else {
            //completely broken source, no parser result
        }
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(super.toString());
        buf.append(":"); //NOI18N
        for (HtmlLinkEntry c : getReferences()) {
            buf.append(" references="); //NOI18N
            buf.append(c);
            buf.append(','); //NOI18N
        }
        return buf.toString();
    }


    private HtmlLinkEntry createFileReferenceEntry(String name, OffsetRange range, String tagName, String attributeName) {
        //normalize the link so it contains just the file reference, not the possible query part
        //TODO query part handling should be moved to the HtmlLinkEntry possibly
        int qmIndex = name.indexOf("?");//NOI18N
        if(qmIndex >= 0) {
            //modify the range
            range = new OffsetRange(range.getStart(), range.getEnd() - (name.length() - qmIndex));
            //strip the name
            name = name.substring(0, qmIndex);
        }
        
        int documentFrom = getSnapshot().getOriginalOffset(range.getStart());
        int documentTo = getSnapshot().getOriginalOffset(range.getEnd());
        
        OffsetRange documentRange = null;
        if (documentFrom == -1 || documentTo == -1) {
            if(LOG) {
                LOGGER.log(Level.FINER,"Ast offset range {0}, text=''{1}" + "'', "
                        + " cannot be properly mapped to source offset range: [{2},{3}] in file {4}", new Object[]{range.toString(), getSnapshot().getText().subSequence(range.getStart(), range.getEnd()), documentFrom, documentTo, getFileObject().getPath()}); //NOI18N
            }
        } else {
            documentRange = new OffsetRange(documentFrom, documentTo);
        }
        return new HtmlLinkEntry(getFileObject(), name, range, documentRange, tagName, attributeName);
    }

    public class ReferencesSearch implements NodeVisitor {

        @Override
        public void visit(Node node) {
            Tag tnode = (Tag)node;
            //XXX This is HTML specific - USE TagMetadata!!!
            //TODO this is a funny way how to figure out if the attribute contains
            //a file reference or not. The code needs to be generified later.
            Map<String, ValueCompletion<HtmlCompletionItem>> completions = AttrValuesCompletion.getSupportsForTag(tnode.name().toString());
            if(completions != null) {
                for(Attribute attr : tnode.attributes()) {
                    ValueCompletion<HtmlCompletionItem> avc = completions.get(attr.name().toString());
                    if(AttrValuesCompletion.FILE_NAME_SUPPORT == avc) {
                        //found file reference
                        CharSequence unquotedValue = NodeUtils.unquotedValue(attr);
                        boolean isQuoted = NodeUtils.isValueQuoted(attr);
                        int offset = attr.valueOffset() + (isQuoted ? 1 : 0);
                        
                        getReferencesCollectionInstance().add(
                                createFileReferenceEntry(unquotedValue.toString(),
                                new OffsetRange(offset,
                                offset + unquotedValue.length()),
                                tnode.name().toString(),
                                attr.name().toString()));
                    }
                }
            }

            //check if the tag can contain css code
            if(LexerUtils.equals(STYLE_TAG_NAME, tnode.name(), true, true)) { //NOI18N
                //XXX maybe we should also check the type attribute for text/css mimetype
                if(!tnode.isEmpty()) {
                    int from = node.to();
                    if(from != -1) {
                        Node closeTag = node.matchingTag();
                        if(closeTag != null) {
                            int to = closeTag.from();
                            getEmbeddedCssSectionsCollectionInstance().add(new OffsetRange(from, to));
                        }
                    } else {
                        //that's odd since the end offset of the tag should be always set
                        LOGGER.log(Level.INFO, "The end offset of the node {0} is not set! Please report the exception and attach the {1} to the issue.", new Object[]{new TreePath(node).path().toString(), FileUtil.getFileDisplayName(HtmlFileModel.this.getFileObject())}); //NOI18N
                    }
                }
            }

        }

    }
}
