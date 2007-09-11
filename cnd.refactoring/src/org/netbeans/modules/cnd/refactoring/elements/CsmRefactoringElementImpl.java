/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.refactoring.elements;

import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CsmRefactoringElementImpl extends 
                SimpleRefactoringElementImplementation {

    private final CsmReference elem;
    private final PositionBounds bounds;
    private final FileObject fo;
    private final String displayText;
    public CsmRefactoringElementImpl(PositionBounds bounds, 
            CsmReference elem, FileObject fo, String displayText) {
        this.elem = elem;
        this.bounds = bounds;
        this.fo = fo;
        this.displayText = displayText;
    }
        
    public String getText() {
        return elem.getText();
    }

    public String getDisplayText() {
        if (displayText != null) {
            return displayText;
        }
        // returns in bold text on line
        try {
            int stOffset = elem.getStartOffset();
            int endOffset = elem.getEndOffset();
            int startLine = Utilities.getRowFirstNonWhite(getDocument(), stOffset);
            int endLine = Utilities.getRowLastNonWhite(getDocument(), endOffset);
            String lineText = getDocument().getText(startLine, endLine - startLine + 1);
            StringBuilder out = new StringBuilder(lineText);
            out.insert(endOffset-startLine, "</b>");
            out.insert(stOffset-startLine,"<b>");
            return out.toString();        
        } catch (BadLocationException ex) {
            
        }
        return "";
    }

    public void performChange() {
    }

    public Lookup getLookup() {
        return Lookups.singleton(elem);
    }

    public FileObject getParentFile() {
        return fo;
    }

    public PositionBounds getPosition() {
        return bounds;
    }

    @Override
    public void openInEditor() {
        CsmUtilities.openSource((CsmOffsetable)elem);
    }
    
    public static RefactoringElementImplementation create(CsmReference ref) {
        CsmFile csmFile = ref.getContainingFile();        
        FileObject fo = CsmUtilities.getFileObject(csmFile);  
        PositionBounds bounds = CsmUtilities.createPositionBounds(ref);
        CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(csmFile);
        BaseDocument doc = null;
        String displayText = null;
        if (ces != null && (ces.getDocument() instanceof BaseDocument)) {
            doc = (BaseDocument)ces.getDocument();
            try {            
                int stOffset = ref.getStartOffset();
                int endOffset = ref.getEndOffset();
                int startLine = Utilities.getRowFirstNonWhite(doc, stOffset);
                int endLine = Utilities.getRowLastNonWhite(doc, endOffset)+1;
                displayText = CsmRefactoringUtils.getHtml(startLine, endLine, stOffset, endOffset, doc);
            } catch (BadLocationException ex) {

            }            
        }
        return new CsmRefactoringElementImpl(bounds, ref, fo, displayText);
    }

    private BaseDocument getDocument() {
        return (BaseDocument) bounds.getBegin().getCloneableEditorSupport().getDocument();
    }
}
