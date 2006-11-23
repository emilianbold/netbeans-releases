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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.mobility.editor.hints;
import org.netbeans.modules.mobility.editor.actions.RecommentAction;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.BaseDocument;
import org.openide.util.NbBundle;
import org.openide.text.NbDocument;
import org.openide.ErrorManager;

import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;

/**
 */
public class DisableHint implements Fix {
    
    final protected Document doc;
    protected int offset;
    
    public DisableHint(BaseDocument doc, int offset) {
        this.doc = doc;
        try {
            this.offset = Utilities.getRowFirstNonWhite(doc, offset);
        } catch (BadLocationException ble) {
        }
    }
    
    public String getText() {
        return NbBundle.getMessage(InlineIncludeHint.class, "HintInlineDisable"); //NOI18N
    }
    
    public synchronized ChangeInfo implement() {
        NbDocument.runAtomic((StyledDocument) doc, new Runnable() {
            public void run() {
                try {
                    doc.insertString(offset, "/", null);
                } catch (BadLocationException ble) {
                    ErrorManager.getDefault().notify(ble);
                }
                RecommentAction.actionPerformed(doc);
            }
        });
        return null;
    }
    
//    public int getType() {
//        return SUGGESTION;
//    }
}
