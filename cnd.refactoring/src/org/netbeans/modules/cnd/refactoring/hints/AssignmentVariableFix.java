/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.refactoring.hints;

import java.util.Collections;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 *
 * @author Alexander Simon
 */
public class AssignmentVariableFix extends IntroduceVariableBaseFix {
    final FileObject fo;
    private String type;

    public AssignmentVariableFix(CsmExpression expression, Document doc, FileObject fo) {
        super(expression, doc);
        this.fo = fo;
    }

    @Override
    public String getText() {
        return NbBundle.getMessage(SuggestionFactoryTask.class, "FIX_AssignResultToVariable"); //NOI18N
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
        final String text = getType() + " " + aName + " = "; //NOI18N
        doc.insertString(expression.getStartOffset(), text, null);
        Position startPosition = new Position() {
            @Override
            public int getOffset() {
                return expression.getStartOffset() + getType().length() + 1;
            }
        };
        Position endPosition = new Position() {
            @Override
            public int getOffset() {
                return expression.getStartOffset() + text.length() - 3;
            }
        };
        ChangeInfo changeInfo = new ChangeInfo(fo, startPosition, endPosition);
        return changeInfo;
    }

}
