package org.netbeans.modules.web.jsf.editor.refactoring;

import javax.swing.text.BadLocationException;
import javax.swing.text.Position.Bias;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.web.jsf.editor.el.ELElement;
import org.netbeans.modules.web.jsf.editor.refactoring.JsfELRefactoringPlugin.ParserResultHolder;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

final class WhereUsedQueryElement extends SimpleRefactoringElementImplementation {

    private final FileObject file;
    private final String reference;
    private final ELElement eLElement;
    private final ParserResultHolder parserResult;

    public WhereUsedQueryElement(FileObject file, String reference, ELElement eLElement, ParserResultHolder parserResult) {
        this.file = file;
        this.reference = reference;
        this.eLElement = eLElement;
        this.parserResult = parserResult;
    }

    @Override
    public String getText() {
        return reference;
    }

    @Override
    public String getDisplayText() {
        try {
            CharSequence text = parserResult.topLevelSnapshot.getText();
            OffsetRange orig = eLElement.getOriginalOffset();
            int astLineStart = GsfUtilities.getRowStart(text, orig.getStart());
            int astLineEnd = GsfUtilities.getRowEnd(text, orig.getStart());
            // TODO: this is not accurate, need to do highlighning based on AST offsets
            return RefactoringUtil.encodeAndHighlight(reference, text.subSequence(astLineStart, astLineEnd).toString()).trim();
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            return eLElement.getExpression();
        }
    }

    @Override
    public void performChange() {
    }

    @Override
    public Lookup getLookup() {
        return Lookups.singleton(file);
    }

    @Override
    public FileObject getParentFile() {
        return file;
    }

    @Override
    public PositionBounds getPosition() {
        CloneableEditorSupport editor = GsfUtilities.findCloneableEditorSupport(file);
        PositionRef start = editor.createPositionRef(eLElement.getOriginalOffset().getStart(), Bias.Forward);
        PositionRef end = editor.createPositionRef(eLElement.getOriginalOffset().getEnd(), Bias.Backward);
        return new PositionBounds(start, end);
    }
}
