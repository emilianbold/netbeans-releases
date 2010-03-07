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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position.Bias;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.editor.ext.html.parser.AstNodeVisitor;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.support.ModificationResult;
import org.netbeans.modules.csl.spi.support.ModificationResult.Difference;
import org.netbeans.modules.css.refactoring.api.CssRefactoring;
import org.netbeans.modules.css.refactoring.api.Entry;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.html.editor.refactoring.api.ExtractInlinedStyleRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
public class ExtractInlinedStyleRefactoringPlugin implements RefactoringPlugin {

    private boolean cancelled;
    private ExtractInlinedStyleRefactoring refactoring;

    public ExtractInlinedStyleRefactoringPlugin(ExtractInlinedStyleRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    //TODO implement the checks!

    @Override
    public Problem preCheck() {
        return null;
    }

    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    public Problem fastCheckParameters() {
        return null;
    }

    @Override
    public void cancelRequest() {
        cancelled = true;
    }

    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        if(cancelled) {
            return null;
        }
        ModificationResult modificationResult = new ModificationResult();
        RefactoringContext context = refactoring.getRefactoringSource().lookup(RefactoringContext.class);
        assert context != null;

        switch(refactoring.getMode()) {
            case refactorToExistingEmbeddedSection:
                int embeddedSectionEnd = refactoring.getExistingEmbeddedCssSection().getEnd();
                refactorToEmbeddedSection(modificationResult, context, embeddedSectionEnd);
                break;
            case refactorToNewEmbeddedSection:
                refactorToNewEmbeddedSection(modificationResult, context);
                break;
            case refactorToReferedExternalSheet:
                refactorToStyleSheet(modificationResult, context);
                break;
            case refactorToExistingExternalSheet:
                importStyleSheet(modificationResult, context);
                refactorToStyleSheet(modificationResult, context);
                break;
        }

        refactoringElements.registerTransaction(new RetoucheCommit(Collections.singletonList(modificationResult)));

        for (FileObject fo : modificationResult.getModifiedFileObjects()) {
            for (Difference diff : modificationResult.getDifferences(fo)) {
                refactoringElements.add(refactoring, DiffElement.create(diff, fo, modificationResult));
            }
        }

        return null;

    }

    private boolean importStyleSheet(ModificationResult modificationResult, RefactoringContext context) {
        try {
            //create a new html link to the stylesheet
            AstNode root = context.getModel().getParserResult().root();
            final AtomicInteger insertPositionRef = new AtomicInteger();
            final AtomicBoolean increaseIndent = new AtomicBoolean();
            AstNodeUtils.visitChildren(root, new AstNodeVisitor() {

                @Override
                public void visit(AstNode node) {
                    if ("head".equalsIgnoreCase(node.name())) {
                        //NOI18N
                        //append the section as first head's child if there are
                        //no existing link attribute
                        insertPositionRef.set(node.endOffset()); //end of the open tag offset
                        increaseIndent.set(true);
                    } else if ("link".equalsIgnoreCase(node.name())) {
                        //NOI18N
                        //existing link => append the new section after the last one
                        insertPositionRef.set(node.getLogicalRange()[1]); //end of the end tag offset
                        increaseIndent.set(false);
                    }
                }
            }, AstNode.NodeType.OPEN_TAG);
            int embeddedInsertOffset = insertPositionRef.get();
            if (embeddedInsertOffset == -1) {
                //TODO probably missing head tag? - generate? html tag may be missing as well
                return false;
            }
            int insertOffset = context.getModel().getSnapshot().getOriginalOffset(embeddedInsertOffset);
            if (insertOffset == -1) {
                return false; //cannot properly map back
            }
            int baseIndent = Utilities.getRowIndent((BaseDocument) context.getDocument(), insertOffset);
            if (increaseIndent.get()) {
                //add one indent level (after HEAD open tag)
                baseIndent += IndentUtils.indentLevelSize(context.getDocument());
            }

            //generate the embedded id selector section
            String baseIndentString = IndentUtils.createIndentString(context.getDocument(), baseIndent);
            String linkRelativePath = WebUtils.getRelativePath(context.getFile(), refactoring.getExternalSheet());
            String linkText = new StringBuilder().append('\n').
                    append(baseIndentString).
                    append("<link href=\"").
                    append(linkRelativePath).
                    append("\" type=\"text/css\">\n").toString(); //NOI18N

            CloneableEditorSupport editor = GsfUtilities.findCloneableEditorSupport(context.getFile());
            Difference diff = new Difference(Difference.Kind.INSERT,
                        editor.createPositionRef(insertOffset, Bias.Forward),
                        editor.createPositionRef(insertOffset, Bias.Backward),
                        null,
                        linkText,
                        NbBundle.getMessage(ExtractInlinedStyleRefactoringPlugin.class, "MSG_InsertStylesheetLink")); //NOI18N

            modificationResult.addDifferences(context.getFile(), Collections.singletonList(diff));

            return true;
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        return false;

    }

    private void refactorToStyleSheet(ModificationResult modificationResult, RefactoringContext context) {
        Document extSheetDoc = GsfUtilities.getDocument(refactoring.getExternalSheet(), true);

        int insertOffset = extSheetDoc.getLength();
        int baseIndent = getPreviousLineIndent(extSheetDoc, insertOffset);

        refactorToEmbeddedSection(modificationResult, context, refactoring.getExternalSheet(),
                insertOffset, baseIndent, null, null);
    }


    private void refactorToNewEmbeddedSection(ModificationResult modifications, RefactoringContext context) {
        try {
            //create a new embedded css section
            AstNode root = context.getModel().getParserResult().root();
            final AtomicInteger insertPositionRef = new AtomicInteger();
            final AtomicBoolean increaseIndent = new AtomicBoolean();
            AstNodeUtils.visitChildren(root, new AstNodeVisitor() {

                @Override
                public void visit(AstNode node) {
                    if ("head".equalsIgnoreCase(node.name())) {
                        //NOI18N
                        //append the section as first head's child if there are
                        //no existing style sections
                        insertPositionRef.set(node.endOffset()); //end of the open tag offset
                        increaseIndent.set(true);
                    } else if ("style".equalsIgnoreCase(node.name())) {
                        //NOI18N
                        //existing style section
                        //append the new section after the last one
                        insertPositionRef.set(node.getLogicalRange()[1]); //end of the end tag offset
                        increaseIndent.set(false);
                    }
                }
            }, AstNode.NodeType.OPEN_TAG);
            int embeddedInsertOffset = insertPositionRef.get();
            if (embeddedInsertOffset == -1) {
                //TODO probably missing head tag? - generate? html tag may be missing as well
                return;
            }
            int insertOffset = context.getModel().getSnapshot().getOriginalOffset(embeddedInsertOffset);
            if (insertOffset == -1) {
                return; //cannot properly map back
            }
            int baseIndent = Utilities.getRowIndent((BaseDocument) context.getDocument(), insertOffset);
            if(increaseIndent.get()) {
                //add one indent level (after HEAD open tag)
                baseIndent += IndentUtils.indentLevelSize(context.getDocument());
            }


            //generate the embedded id selector section
            String baseIndentString = IndentUtils.createIndentString(context.getDocument(), baseIndent);
            String prefix = new StringBuilder().append('\n').
                    append(baseIndentString).
                    append("<style type=\"text/css\">\n").toString(); //NOI18N

            String postfix = new StringBuilder().append('\n').
                    append(baseIndentString).
                    append("</style>").toString(); //NOI18N

            refactorToEmbeddedSection(modifications, context, context.getFile(), insertOffset, baseIndent, prefix, postfix);

        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    private void refactorToEmbeddedSection(ModificationResult modifications, RefactoringContext context, final int insertOffset) {
        int baseIndent = getPreviousLineIndent(context.getDocument(), insertOffset);
        refactorToEmbeddedSection(modifications, context, context.getFile(), insertOffset, baseIndent, null, null);
    }

    private void refactorToEmbeddedSection(ModificationResult modifications, RefactoringContext context, 
            FileObject targetStylesheet,
            int insertOffset, int baseIndent, String prefix, String postfix) {
        List<InlinedStyleInfo> inlinedStyles = context.getInlinedStyles();
        CloneableEditorSupport currentFileEditor = GsfUtilities.findCloneableEditorSupport(context.getFile());
        List<Difference> diffs = new LinkedList<Difference>();

        //get existing id selectors
        //XXX clarify the parsing of embedded source model - this call parses the file again!
        //should be likely done in different way
        Collection<Entry> existingIds = CssRefactoring.getAllIdSelectors(context.getFile());
        Collection<String> allIds = new LinkedList<String>();
        for(Entry e : existingIds) {
            allIds.add(e.getName());
        }

        StringBuilder generatedIdSelectorsSection = new StringBuilder();
        if(prefix != null) {
            generatedIdSelectorsSection.append(prefix);
        }

        for(InlinedStyleInfo si : inlinedStyles) {
            try {
                //TODO define some pattern for the generation in the css options!

                //find first free id selector name
                String idSelectorNameBase = si.getTag() + "id";
                String idSelectorName;
                int counter = 0;
                while(allIds.contains(idSelectorName = idSelectorNameBase + (counter++ == 0 ? "" : counter))) {
                }
                allIds.add(idSelectorName);

                //delete the inlined style - attribute name, equal sign, whitespaces and the value
                //and replace with id selector reference
                int deleteFrom = si.getAttributeStartOffset();
                int deleteTo = si.getRange().getEnd() + (si.isValueQuoted() ? 1 : 0);
                String idSelectorUsageText = "id=\""+ idSelectorName + "\""; //NOI18N

                Difference diff = new Difference(Difference.Kind.CHANGE,
                        currentFileEditor.createPositionRef(deleteFrom, Bias.Forward),
                        currentFileEditor.createPositionRef(deleteTo, Bias.Backward),
                        context.getDocument().getText(deleteFrom, deleteTo - deleteFrom),
                        idSelectorUsageText,
                        NbBundle.getMessage(ExtractInlinedStyleRefactoringPlugin.class, "MSG_ReplaceInlinedStyleWithIdSelectorReference")); //NOI18N

                diffs.add(diff);

                String[] lines = new String[]{
                    "", //empty line = will add new line
                    "#" + idSelectorName + "{",
                    "\t" + WebUtils.unquotedValue(si.getInlinedCssValue()) + ";",
                    "}"}; //NOI18N

                //if prefix is set indent the content by one level
                String idSelectorText = formatCssCode(context.getDocument(), baseIndent, prefix == null ? 0 : 1, lines);
                generatedIdSelectorsSection.append(idSelectorText);

            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        modifications.addDifferences(context.getFile(), diffs);

        if(postfix != null) {
            generatedIdSelectorsSection.append(postfix);
        }
        //generate the cumulated embedded id selector section
        CloneableEditorSupport targetStylesheetEditor = GsfUtilities.findCloneableEditorSupport(targetStylesheet);
        modifications.addDifferences(targetStylesheet, Collections.singletonList(new Difference(Difference.Kind.INSERT,
                targetStylesheetEditor.createPositionRef(insertOffset, Bias.Forward),
                targetStylesheetEditor.createPositionRef(insertOffset, Bias.Backward),
                null,
                generatedIdSelectorsSection.toString(),
                NbBundle.getMessage(ExtractInlinedStyleRefactoringPlugin.class, "MSG_GenerateIDSelectors")))); //NOI18N

    }

    //TODO there should be a generic facility allowing to reformat a piece of code
    //according to the css formatter options. I could possibly invoke the formatter
    //on an artificial document with the new code content, but since we do not have the
    //pretty printer it would not help much.
    private String formatCssCode(Document doc, int baseIndent, int additionalIndent, String... lines ) {
        StringBuilder b = new StringBuilder();

        int indentLevelSize = IndentUtils.indentLevelSize(doc);

        for(String line : lines) {
            //add base indent
            b.append(IndentUtils.createIndentString(doc, baseIndent));

            String indentString = IndentUtils.createIndentString(doc, indentLevelSize);
            //append additional indents
            for(int i = 0; i < additionalIndent; i++) {
                b.append(indentString);
            }
            
            //replace each \t by proper indentation level size
            //and copy the line to the buffer
            for(int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if(c == '\t') { //NOI18N
                    b.append(indentString);
                } else {
                    b.append(c);
                }
            }

            //and new line at the end
            b.append('\n'); //NOI18N
        }


        return b.toString();
    }

    private static int getPreviousLineIndent(final Document doc, final int insertOffset) {
        final AtomicInteger ret = new AtomicInteger(0); //default is 0 indent if something fails in the later runnable
        doc.render(new Runnable() {

            @Override
            public void run() {
                try {
                    //find last nonwhite line indent
                    int firstNonWhiteBw = Utilities.getFirstNonWhiteBwd((BaseDocument) doc, insertOffset);
                    //get the line indent
                    ret.set(Utilities.getRowIndent((BaseDocument)doc, firstNonWhiteBw));
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }

            }

        });

        return ret.get();
    }

}
