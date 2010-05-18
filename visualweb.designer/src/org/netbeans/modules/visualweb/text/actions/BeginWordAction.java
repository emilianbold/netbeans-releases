/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
/*
 * Created on Mar 16, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.netbeans.modules.visualweb.text.actions;

import java.awt.event.ActionEvent;

import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition;
import org.netbeans.modules.visualweb.css2.ModelViewMapper;
import org.netbeans.modules.visualweb.designer.InlineEditor;
import org.netbeans.modules.visualweb.designer.WebForm;
import org.netbeans.modules.visualweb.text.DesignerPaneBase;


/*
 * Position the caret to the beginning of the word.
 */
public class BeginWordAction extends TextAction {
    private boolean select;

    /**
     * Create this action with the appropriate identifier.
     * @param nm  the name of the action, Action.NAME.
     * @param select whether to extend the selection when
     *  changing the caret position.
     */
    public BeginWordAction(String nm, boolean select) {
        super(nm);
        this.select = select;
    }

    /** The operation to perform when this action is triggered. */
    public void actionPerformed(ActionEvent e) {
        DesignerPaneBase target = getTextComponent(e);

        if (target != null) {
//            Position dot = target.getCaretPosition();
            DomPosition dot = target.getCaretPosition();

//            if (dot == Position.NONE) {
            if (dot == DomPosition.NONE) {
                return;
            }

//            WebForm webform = target.getDocument().getWebForm();
            WebForm webform = target.getWebForm();
            
//            ModelViewMapper mapper = webform.getMapper();
//            Position begPos = ModelViewMapper.getWordStart(webform.getPane().getPageBox(), dot);
            DomPosition begPos = ModelViewMapper.getWordStart(webform.getPane().getPageBox(), dot);

//            if (begPos == Position.NONE) {
            if (begPos == DomPosition.NONE) {
                return;
            }

//            DesignerCaret caret = target.getCaret();
//            if (!caret.isWithinEditableRegion(begPos)) {
//            if (!target.isCaretWithinEditableRegion(begPos)) {
            if (!webform.isInsideEditableRegion(begPos)) {
                InlineEditor editor = webform.getManager().getInlineEditor();

                if (editor != null) {
                    begPos = editor.getBegin();
                }
            }

            if (select) {
//                target.moveCaretPosition(begPos);
                target.moveCaretDot(begPos);
            } else {
//                target.setCaretPosition(begPos);
                target.setCaretDot(begPos);
            }
        }
    }
}
