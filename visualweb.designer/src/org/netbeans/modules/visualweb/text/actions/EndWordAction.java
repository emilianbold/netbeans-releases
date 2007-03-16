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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * Created on Mar 16, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.netbeans.modules.visualweb.text.actions;

import java.awt.event.ActionEvent;

import org.netbeans.modules.visualweb.css2.ModelViewMapper;
import org.netbeans.modules.visualweb.designer.InlineEditor;
import org.netbeans.modules.visualweb.designer.WebForm;
import org.netbeans.modules.visualweb.text.*;


/*
 * Position the caret to the end of the word.
 */
public class EndWordAction extends TextAction {
    private boolean select;

    /**
     * Create this action with the appropriate identifier.
     * @param nm  the name of the action, Action.NAME.
     * @param select whether to extend the selection when
     *  changing the caret position.
     */
    public EndWordAction(String nm, boolean select) {
        super(nm);
        this.select = select;
    }

    /** The operation to perform when this action is triggered. */
    public void actionPerformed(ActionEvent e) {
        DesignerPaneBase target = getTextComponent(e);

        if (target != null) {
            Position dot = target.getCaretPosition();

            if (dot == Position.NONE) {
                return;
            }

//            WebForm webform = target.getDocument().getWebForm();
            WebForm webform = target.getWebForm();
            
//            ModelViewMapper mapper = webform.getMapper();
            Position endPos = ModelViewMapper.getWordEnd(webform.getPane().getPageBox(), dot);

            if (endPos == Position.NONE) {
                return;
            }

            DesignerCaret caret = target.getCaret();

            if (!caret.isWithinEditableRegion(endPos)) {
                InlineEditor editor = webform.getManager().getInlineEditor();

                if (editor != null) {
                    endPos = editor.getEnd();
                }
            }

            if (select) {
                target.moveCaretPosition(endPos);
            } else {
                target.setCaretPosition(endPos);
            }
        }
    }
}
