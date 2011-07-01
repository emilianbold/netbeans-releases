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
package org.netbeans.modules.html.editor.api.gsf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.editor.ext.html.parser.api.AstNode;
import org.netbeans.editor.ext.html.parser.api.HtmlSource;
import org.netbeans.editor.ext.html.parser.spi.UndeclaredContentResolver;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintsProvider.HintsManager;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
 *
 * @author marekfukala
 */
public class HtmlExtension {

    private static final Map<String, Collection<HtmlExtension>> EXTENSIONS = new HashMap<String, Collection<HtmlExtension>>();

    /** register a new extension to the html support. The mimeType applies to source mimetype, not embedded mimetype!
     * TODO use mimelookup
     */
    public static void register(String mimeType, HtmlExtension extension) {
        synchronized (EXTENSIONS) {
            Collection<HtmlExtension> existing = EXTENSIONS.get(mimeType);
            if (existing == null) {
                existing = new ArrayList<HtmlExtension>();
                EXTENSIONS.put(mimeType, existing);
            }
            existing.add(extension);
        }
    }

    public static Collection<HtmlExtension> getRegisteredExtensions(String mimeType) {
        Collection<HtmlExtension> exts = EXTENSIONS.get(mimeType);
        return exts != null ? exts : Collections.<HtmlExtension>emptyList();
    }

    //highlighting
    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights(HtmlParserResult result, SchedulerEvent event) {
        return Collections.emptyMap();
    }

    //completion
    public List<CompletionItem> completeOpenTags(CompletionContext context) {
        return Collections.emptyList();
    }

    public List<CompletionItem> completeAttributes(CompletionContext context) {
        return Collections.emptyList();
    }

    public List<CompletionItem> completeAttributeValue(CompletionContext context) {
        return Collections.emptyList();
    }

    //hyperlinking
    public OffsetRange getReferenceSpan(Document doc, int caretOffset) {
        return OffsetRange.NONE;
    }

    public DeclarationLocation findDeclaration(ParserResult info, int caretOffset) {
        return null;
    }

    //errors, hints
    public void computeErrors(HintsManager manager, RuleContext context, List<Hint> hints, List<Error> unhandled) {
        //no-op
    }

    public void computeSelectionHints(HintsManager manager, RuleContext context, List<Hint> hints, int start, int end) {
        //no-op
    }

    /**
     * This method allows the extension to bind some prefixed html source 
     * elements and attributes to an physically undeclared namespace.
     * 
     * @return an instance of undeclared content resolver
     */
    public UndeclaredContentResolver getUndeclaredContentResolver() {
        return null;
    }

    //--------------------
    public static class CompletionContext {

        private HtmlParserResult result;
        private int originalOffset;
        private int ccItemStartOffset;
        private int astoffset;
        private String preText;
        private String itemText;
        private AstNode currentNode;
        private String attributeName; //for attribute value completion
        private boolean valueQuoted;

        public CompletionContext(HtmlParserResult result, int originalOffset, int astoffset, int ccItemStartOffset, String preText, String itemText) {
            this(result, originalOffset, astoffset, ccItemStartOffset, preText, itemText, null);
        }

        public CompletionContext(HtmlParserResult result, int originalOffset, int astoffset, int ccItemStartOffset, String preText, String itemText, AstNode currentNode) {
            this(result, originalOffset, astoffset, ccItemStartOffset, preText, itemText, currentNode, null, false);
        }

        public CompletionContext(HtmlParserResult result, int originalOffset, int astoffset, int ccItemStartOffset, String preText, String itemText, AstNode currentNode, String attributeName, boolean valueQuoted) {
            this.result = result;
            this.originalOffset = originalOffset;
            this.astoffset = astoffset;
            this.preText = preText;
            this.ccItemStartOffset = ccItemStartOffset;
            this.currentNode = currentNode;
            this.itemText = itemText;
            this.attributeName = attributeName;
            this.valueQuoted = valueQuoted;
        }

        public String getPrefix() {
            return preText;
        }

        /** returns the whole word under cursor */
        public String getItemText() {
            return itemText;
        }

        public int getAstoffset() {
            return astoffset;
        }

        public int getOriginalOffset() {
            return originalOffset;
        }

        public int getCCItemStartOffset() {
            return ccItemStartOffset;
        }

        public HtmlParserResult getResult() {
            return result;
        }

        public AstNode getCurrentNode() {
            return currentNode;
        }

        public String getAttributeName() {
            return attributeName;
        }

        public boolean isValueQuoted() {
            return valueQuoted;
        }
    }
}
