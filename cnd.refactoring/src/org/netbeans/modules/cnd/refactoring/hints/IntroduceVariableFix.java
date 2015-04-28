/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.refactoring.hints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.refactoring.actions.InstantRenamePerformer;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 *
 * @author Alexander Simon
 */
public class IntroduceVariableFix extends IntroduceVariableBaseFix {
    final CsmStatement st;
    final FileObject fo;
    final JTextComponent comp;
    private String type;

    public IntroduceVariableFix(CsmStatement st, CsmOffsetable expression, Document doc, JTextComponent comp, FileObject fo) {
        super(expression, doc);
        this.fo = fo;
        this.st = st;
        this.comp = comp;
    }

    @Override
    public String getText() {
        return NbBundle.getMessage(SuggestionFactoryTask.class, "FIX_IntroduceVariable"); //NOI18N
    }

    @Override
    protected boolean isC() {
        return MIMENames.C_MIME_TYPE.equals(fo.getMIMEType());
    }

    @Override
    protected boolean isInstanceRename() {
        return true;
    }

    @Override
    protected List<Pair<Integer, Integer>> replaceOccurrences() {
        return Collections.emptyList();
    }

    @Override
    protected String getType() {
        return type;
    }

    @Override
    public ChangeInfo implement() throws Exception {
        type = suggestType();
        if (type == null) {
            return null;
        }
        final String aName = suggestName();
        if (aName == null) {
            return null;
        }
        final List<Pair<Integer, Integer>> replaceOccurrences = replaceOccurrences();
        final String exprText = expression.getText().toString();
        final ChangeInfo changeInfo = new ChangeInfo();
        final String typeTextPrefix = getType() + " "; //NOI18N
        final String text = typeTextPrefix + aName + " = " + expression.getText() + ";\n"; //NOI18N
        doc.runAtomicAsUser(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Pair<Position,Position>> other = new ArrayList<>();
                    for (int i = replaceOccurrences.size() - 1; i >= 0; i--) {
                        Pair<Integer, Integer> occurrence = replaceOccurrences.get(i);
                        doc.remove(occurrence.first(), occurrence.second() - occurrence.first());
                        doc.insertString(occurrence.first(), aName, null);
                        Position exprStart = NbDocument.createPosition(doc, occurrence.first(), Position.Bias.Forward);
                        Position exprEnd = NbDocument.createPosition(doc, occurrence.first() + aName.length(), Position.Bias.Backward);
                        other.add(Pair.of(exprStart, exprEnd));
                    }
                    doc.remove(expression.getStartOffset(), exprText.length());
                    doc.insertString(expression.getStartOffset(), aName, null);
                    Position exprStart = NbDocument.createPosition(doc, expression.getStartOffset(), Position.Bias.Forward);
                    Position exprEnd = NbDocument.createPosition(doc, expression.getStartOffset() + aName.length(), Position.Bias.Backward);
                    doc.insertString(st.getStartOffset(), text, null);
                    Position stmtStart = NbDocument.createPosition(doc, st.getStartOffset() + typeTextPrefix.length(), Position.Bias.Forward);
                    Position stmtEnd = NbDocument.createPosition(doc, st.getStartOffset() + typeTextPrefix.length() + aName.length(), Position.Bias.Backward);
                    changeInfo.add(fo, stmtStart, stmtEnd);
                    changeInfo.add(fo, exprStart, exprEnd);
                    for(Pair<Position,Position> pos : other) {
                        changeInfo.add(fo, pos.first(), pos.second());
                    }
                    Indent indent = Indent.get(doc);
                    indent.lock();
                    try {
                        indent.reindent(st.getStartOffset() + text.length() + 1);
                    } finally {
                        indent.unlock();
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        if (comp != null && isInstanceRename()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        comp.setCaretPosition(changeInfo.get(0).getEnd().getOffset());
                        InstantRenamePerformer.invokeInstantRename(comp, changeInfo);
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        }
        return changeInfo;
    }

}
