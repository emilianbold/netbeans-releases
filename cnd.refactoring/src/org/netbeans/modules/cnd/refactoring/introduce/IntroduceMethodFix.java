/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.refactoring.introduce;

import javax.swing.JButton;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.refactoring.hints.BodyFinder;
import org.netbeans.modules.cnd.refactoring.ui.InsertPoint;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
final class IntroduceMethodFix implements Fix {
    private final Document doc;
    private final FileObject fileObject;
    private final BodyFinder.BodyResult res;

    public IntroduceMethodFix(BodyFinder.BodyResult res, Document doc, FileObject fileObject) {
        this.doc = doc;
        this.fileObject = fileObject;
        this.res = res;
    }

    @Override
    public String getText() {
        return NbBundle.getMessage(IntroduceMethodFix.class, "FIX_IntroduceMethod");
    }

    public String toDebugString() {
        return "[IntroduceMethod:" + res.getSelectionFrom() + ":" + res.getSelectionTo() + "]"; // NOI18N
    }

    @Override
    public ChangeInfo implement() throws Exception {
        JButton btnOk = new JButton(NbBundle.getMessage(IntroduceMethodFix.class, "LBL_Ok"));
        JButton btnCancel = new JButton(NbBundle.getMessage(IntroduceMethodFix.class, "LBL_Cancel"));
        IntroduceMethodPanel panel = new IntroduceMethodPanel(res, btnOk, fileObject); //NOI18N
        String caption = NbBundle.getMessage(IntroduceMethodFix.class, "CAP_IntroduceMethod");
        DialogDescriptor dd = new DialogDescriptor(panel, caption, true, new Object[]{btnOk, btnCancel}, btnOk, DialogDescriptor.DEFAULT_ALIGN, null, null);
        if (DialogDisplayer.getDefault().notify(dd) != btnOk) {
            return null; //cancel
        }
        final String methodCall = panel.getMethodCall();
        final String method = "\n"+panel.getMethodSignature()+"{\n"+doc.getText(res.getSelectionFrom(), res.getSelectionTo() - res.getSelectionFrom())+"\n}\n"; //NOI18N
        // for method declaration
        final FileObject fileObject2;
        final String methodDeclarationString;
        final int methodDeclarationInsertPoint;
        if (res.getFunctionKind() == BodyFinder.FunctionKind.MethodDefinition){
            CsmFunction functionDeclaration = res.getFunctionDeclaration();
            CsmFile containingFile = functionDeclaration.getContainingFile();
            fileObject2 = CsmUtilities.getFileObject(containingFile);
            methodDeclarationString = "\n"+panel.getMethodDeclarationString()+";\n"; //NOI18N
            InsertPoint insertPoint = panel.getInsertPoint();
            if (insertPoint.getContainerClass() == null) {
                // default
                methodDeclarationInsertPoint = functionDeclaration.getEndOffset();
            } else if (insertPoint.getIndex() == Integer.MIN_VALUE) {
                // at the beginning class
                methodDeclarationInsertPoint = insertPoint.getContainerClass().getLeftBracketOffset()+1;
            } else if (insertPoint.getIndex() == Integer.MAX_VALUE) {
                // at the end class
                methodDeclarationInsertPoint = insertPoint.getContainerClass().getEndOffset()-1;
            } else {
                methodDeclarationInsertPoint = insertPoint.getElementDeclaration().getEndOffset();
            }
        } else {
            fileObject2 = null;
            methodDeclarationString = null;
            methodDeclarationInsertPoint = 0;
        }

        final ChangeInfo changeInfo = new ChangeInfo();
        ((BaseDocument)doc).runAtomicAsUser(new Runnable() {
            @Override
            public void run() {
                try {
                    Position declStart = null;
                    Position declEnd = null;
                    if (fileObject.equals(fileObject2)) {
                        if (methodDeclarationInsertPoint > res.getInsetionOffset()) {
                            doc.insertString(methodDeclarationInsertPoint, methodDeclarationString, null);
                            declStart = NbDocument.createPosition(doc, methodDeclarationInsertPoint, Position.Bias.Forward);
                            declEnd = NbDocument.createPosition(doc, methodDeclarationInsertPoint + methodDeclarationString.length(), Position.Bias.Backward);
                        }
                    }
                    doc.remove(res.getSelectionFrom(), res.getSelectionTo() - res.getSelectionFrom());
                    doc.insertString(res.getSelectionFrom(), methodCall, null);
                    Position callStart = NbDocument.createPosition(doc, res.getSelectionFrom(), Position.Bias.Forward);
                    Position callEnd = NbDocument.createPosition(doc, res.getSelectionFrom() + methodCall.length(), Position.Bias.Backward);
                    doc.insertString(res.getInsetionOffset(), method, null);
                    Position methodStart = NbDocument.createPosition(doc, res.getInsetionOffset(), Position.Bias.Forward);
                    Position methodEnd = NbDocument.createPosition(doc, res.getInsetionOffset() + method.length(), Position.Bias.Backward);
                    if (fileObject.equals(fileObject2) && declStart == null) {
                        doc.insertString(methodDeclarationInsertPoint, methodDeclarationString, null);
                        declStart = NbDocument.createPosition(doc, methodDeclarationInsertPoint, Position.Bias.Forward);
                        declEnd = NbDocument.createPosition(doc, methodDeclarationInsertPoint + methodDeclarationString.length(), Position.Bias.Backward);
                    }
                    changeInfo.add(fileObject, methodStart, methodEnd);
                    changeInfo.add(fileObject, callStart, callEnd);
                    if (declStart != null) {
                        changeInfo.add(fileObject, declStart, declEnd);
                    }
                    Indent indent = Indent.get(doc);
                    indent.lock();
                    try {
                        indent.reindent(methodStart.getOffset(), methodEnd.getOffset());
                        indent.reindent(callStart.getOffset(), callEnd.getOffset());
                        if (declStart != null) {
                            indent.reindent(declStart.getOffset(), declEnd.getOffset());
                        }
                    } finally {
                        indent.unlock();
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        if (fileObject2 != null && !fileObject.equals(fileObject2)){
            DataObject dataObject2 = CsmUtilities.getDataObject(fileObject2);
            final StyledDocument doc2 = CsmUtilities.openDocument(dataObject2);
            ((BaseDocument)doc2).runAtomicAsUser(new Runnable() {
                @Override
                public void run() {
                    try {
                        doc2.insertString(methodDeclarationInsertPoint, methodDeclarationString, null);
                        Position declStart = NbDocument.createPosition(doc2, methodDeclarationInsertPoint, Position.Bias.Forward);
                        Position declEnd = NbDocument.createPosition(doc2, methodDeclarationInsertPoint + methodDeclarationString.length(), Position.Bias.Backward);
                        changeInfo.add(fileObject2, declStart, declEnd);
                        Indent indent = Indent.get(doc2);
                        indent.lock();
                        try {
                            indent.reindent(declStart.getOffset(), declEnd.getOffset());
                        } finally {
                            indent.unlock();
                        }
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        }
        return changeInfo;
    }
}
