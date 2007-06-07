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
package org.netbeans.modules.editor.bracesmatching;

import java.awt.event.ActionEvent;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.openide.util.NbBundle;

/**
 *
 * @author Vita Stejskal
 */
public final class BracesMatchAction extends TextAction {

    private final boolean select;
    
    public BracesMatchAction() {
        this(false);
    }

    public BracesMatchAction(boolean select) {
        super(select ? "selection-match-brace" : "match-brace"); //NOI18N
        this.select = select;
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(BracesMatchAction.class, (String) getValue(NAME)));
    }
    
    public void actionPerformed(ActionEvent e) {
        JTextComponent component = getTextComponent(e);
        Document document = component.getDocument();
        Caret caret = component.getCaret();
        
        MasterMatcher.get(component).navigate(
            document,
            caret.getDot(), 
            caret,
            select
        );
    }

}
