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
 * To change the template for this generated file go to Window&gt;Preferences&gt;Java&gt;Code
 * Generation&gt;Code and Comments
 */
package org.netbeans.modules.visualweb.text.actions;

import java.awt.event.ActionEvent;

import javax.swing.UIManager;
import org.netbeans.modules.visualweb.designer.SelectionManager;

import org.netbeans.modules.visualweb.designer.WebForm;
import org.netbeans.modules.visualweb.text.DesignerPaneBase;


/*
 * Deletes the character of content that precedes the current caret position.
 *
 */
import org.w3c.dom.Element;
public class DeleteNextCharAction extends TextAction {
    /**
     * Creates this object with the appropriate identifier.
     */
    public DeleteNextCharAction() {
        super(DesignerPaneBase.deleteNextCharAction);
    }

    /**
     * The operation to perform when this action is triggered.
     *
     * @param e
     *            the action event
     */
    public void actionPerformed(ActionEvent e) {
        DesignerPaneBase target = getTextComponent(e);
        boolean beep = true;

        if ((target != null) /* && (target.isEditable()) */) {
//            Document doc = target.getDocument();
//            WebForm webform = doc.getWebForm();
            WebForm webform = target.getWebForm();

////            if (webform.getModel().getLiveUnit() == null) {
//            // XXX This seems to be wrong. Model should handle it.
//            if (!webform.isAlive()) {
//                // Can't mutate document
//                UIManager.getLookAndFeel().provideErrorFeedback(target);
//
//                return;
//            }

//            DesignerCaret caret = target.getCaret();
//            if (caret == null) {
//            if (!target.hasCaret()) {
////                if (!webform.getSelection().isSelectionEmpty()) {
//                SelectionManager sm = webform.getSelection();
//                if (!sm.isSelectionEmpty()) {
////                    webform.getTopComponent().deleteSelection();
////                    webform.tcDeleteSelection();
//                    Element[] componentRootElements = sm.getSelectedComponentRootElements();
//                    sm.clearSelection(true);
//                    webform.getDomDocument().deleteComponents(componentRootElements);
//                    return;
//                }

            // XXX #104464 First apply the delete to the selected components.
            SelectionManager sm = webform.getSelection();
            if (!sm.isSelectionEmpty() && !webform.isInlineEditing()) {
                Element[] componentRootElements = sm.getSelectedComponentRootElements();
                sm.clearSelection(true);
                webform.getDomDocument().deleteComponents(componentRootElements);
                return;
            }
            
            if (!target.hasCaret()) {
                return;
            }

            // XXX Moved to designer/jsf/../DomDocumentImpl.
////            UndoEvent undoEvent = webform.getModel().writeLock(NbBundle.getMessage(DeleteNextCharAction.class, "DeleteText")); // NOI18N
//            DomProvider.WriteLock writeLock = webform.writeLock(NbBundle.getMessage(DeleteNextCharAction.class, "DeleteText")); // NOI18N
//            try {
//                doc.writeLock(NbBundle.getMessage(DeleteNextCharAction.class, "DeleteText")); // NOI18N
//                beep = !caret.removeNextChar();
                beep = !target.removeNextChar();
//            } finally {
////                doc.writeUnlock();
////                webform.getModel().writeUnlock(undoEvent);
//                webform.writeUnlock(writeLock);
//            }
        }

        if (beep) {
            UIManager.getLookAndFeel().provideErrorFeedback(target);
        }
    }
}
