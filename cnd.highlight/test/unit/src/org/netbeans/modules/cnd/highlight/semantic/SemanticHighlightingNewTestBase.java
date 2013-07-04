/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.highlight.semantic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import static junit.framework.Assert.assertNotNull;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.highlight.InterrupterImpl;
import org.netbeans.modules.cnd.modelimpl.test.ProjectBasedTestCase;
import org.netbeans.modules.cnd.modelutil.CsmFontColorManager;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.modelutil.FontColorProvider;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.PositionsBag;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 *
 * @author Nikolay Koldunov
 */
public class SemanticHighlightingNewTestBase extends ProjectBasedTestCase{
    public static final String MIME_TYPE = "text/x-c++";
    private static final String POSITION_BAG= "CndSemanticHighlighter";

    public SemanticHighlightingNewTestBase(String testName) {
        super(testName);
    }
    
    
    protected final void performTest(String filePath) {
       
        List<Highlight> out = getHighlights(filePath);
        assertNotNull(out);
        List<Highlight> sorted = new ArrayList<Highlight>(out);
        Collections.sort(sorted, new Comparator<Highlight>() {

            @Override
            public int compare(Highlight o1, Highlight o2) {
                return o1.getStartOffset() - o2.getStartOffset();
            }
        });
        int i = 1;
        for (Highlight b : sorted) {
            ref( "Block " + (i++) + ":\tPosition " +  // NOI18N
                    b.getStartPosition() + "-" +  // NOI18N
                    b.getEndPosition() + "\t" +  // NOI18N
                    b.getType());
        }
        compareReferenceFiles();

    }

    private List<Highlight> getHighlights(String path) {
        List<Highlight> ret = new ArrayList<Highlight>();

        CsmFile csmFile = null;
        try {
            csmFile = getCsmFile(getDataFile(path));
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(csmFile);
        Document doc = CsmUtilities.openDocument(ces);

        final SemanticHighlighter semanticHighlighter = new SemanticHighlighter(doc);
        semanticHighlighter.update((BaseDocument) doc, new InterrupterImpl());
        
        PositionsBag bag = (PositionsBag) doc.getProperty(POSITION_BAG);
        
        AttributeSet typedefAttrs =
                CsmFontColorManager.instance().getColorAttributes(MIME_TYPE,
                FontColorProvider.Entity.TYPEDEF);
        AttributeSet macroAttrs =
                CsmFontColorManager.instance().getColorAttributes(MIME_TYPE,
                FontColorProvider.Entity.DEFINED_MACRO);
        AttributeSet sysMacroAttrs =
                CsmFontColorManager.instance().getColorAttributes(MIME_TYPE,
                FontColorProvider.Entity.SYSTEM_MACRO);
        AttributeSet userMacroAttrs =
                CsmFontColorManager.instance().getColorAttributes(MIME_TYPE,
                FontColorProvider.Entity.USER_MACRO);
        AttributeSet directiveAttrs =
                CsmFontColorManager.instance().getColorAttributes(MIME_TYPE,
                FontColorProvider.Entity.PREPROCESSOR_DIRECTIVE);
        AttributeSet inactiveAttrs =
                CsmFontColorManager.instance().getColorAttributes(MIME_TYPE,
                FontColorProvider.Entity.INACTIVE_CODE);
        AttributeSet functionAttrs =
                CsmFontColorManager.instance().getColorAttributes(MIME_TYPE,
                FontColorProvider.Entity.FUNCTION);
        AttributeSet funcUsageAttrs =
                CsmFontColorManager.instance().getColorAttributes(MIME_TYPE,
                FontColorProvider.Entity.FUNCTION_USAGE);
        AttributeSet fieldAttrs =
                CsmFontColorManager.instance().getColorAttributes(MIME_TYPE,
                FontColorProvider.Entity.CLASS_FIELD);
        AttributeSet unusedAttrs =
                CsmFontColorManager.instance().getColorAttributes(MIME_TYPE,
                FontColorProvider.Entity.UNUSED_VARIABLES);
        
        HighlightsSequence hs = bag.getHighlights(0, doc.getLength());
        while (hs.moveNext()) {
            int start = hs.getStartOffset();
            int end = hs.getEndOffset();
            String type = "";
            
            AttributeSet attr = hs.getAttributes();
            if (typedefAttrs.equals(attr)) {
                type = "typedef";
            } else if (macroAttrs.equals(attr)) {
                type = "macro";
            } else if (sysMacroAttrs.equals(attr)) {
                type = "sysmacro";
            } else if (userMacroAttrs.equals(attr)) {
                type = "usermacro";
            } else if (directiveAttrs.equals(attr)) {
                type = "directive";
            } else if (inactiveAttrs.equals(attr)) {
                type = "inactive";
            } else if (functionAttrs.equals(attr)) {
                type = "function";
            } else if (funcUsageAttrs.equals(attr)) {
                type = "funcusage";
            } else if (fieldAttrs.equals(attr)) {
                type = "field";
            } else if (unusedAttrs.equals(attr)) {
                type = "unused";
            } else {
                assert false;
            }
            
            ret.add(new Highlight(doc, start, end, type));
        }
        
        return ret;
    }
    
    private static class Highlight {
        private Document doc;
        private int startOffset;
        private int endOffset;
        private String type;

        public Highlight(Document doc, int startOffset, int endOffset, String type) {
            this.doc = doc;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.type = type;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }

        public String getStartPosition() {
            return getPositionByOffset(doc, startOffset);
        }

        public String getEndPosition() {
            return getPositionByOffset(doc, endOffset);
        }

        public String getType() {
            return type;
        }
        
        private String getPositionByOffset(Document doc, int offset) {
            int lineNumber = -1;
            int lineColumn = -1;
            try {
                lineNumber = 1 + NbDocument.findLineNumber((StyledDocument)doc, offset);
                lineColumn = 1 + NbDocument.findLineColumn((StyledDocument)doc, offset);
            } catch (IndexOutOfBoundsException e) {
            }
            return lineNumber + ":" + lineColumn;
        }
    }
    
    
}
