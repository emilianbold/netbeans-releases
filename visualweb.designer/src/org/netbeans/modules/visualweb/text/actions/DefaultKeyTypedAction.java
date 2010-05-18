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
package org.netbeans.modules.visualweb.text.actions;

import java.awt.event.ActionEvent;

import javax.swing.UIManager;
import org.netbeans.modules.visualweb.api.designer.Designer.DesignerEvent;
import org.netbeans.modules.visualweb.designer.InlineEditor;


import org.netbeans.modules.visualweb.designer.WebForm;
import org.netbeans.modules.visualweb.designer.WebForm.DefaultDesignerEvent;
import org.netbeans.modules.visualweb.text.DesignerPaneBase;


/**
 * Action run by default on most keyboard keys - performs a "self insert".
 * There are a couple of exceptions - an Escape gets special handling; it may
 * select parent, or get you out of flow mode editing of a gridpositioned container,
 * or if you're in a read-only region, beep.
 * Originally based on DefaultEditorKit.DefaultKeyTypedAction.
 */
public class DefaultKeyTypedAction extends TextAction {
    /**
     * Creates this object with the appropriate identifier.
     */
    public DefaultKeyTypedAction() {
        super(DesignerPaneBase.defaultKeyTypedAction);
    }

    /**
     * The operation to perform when this action is triggered.
     *
     * @param e
     *            the action event
     */
    public void actionPerformed(ActionEvent e) {
        DesignerPaneBase target = getTextComponent(e);

        if ((target != null) && (e != null)) {
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

            String content = e.getActionCommand();

            if (content == null) {
                return;
            }

            // XXX TODO This is very suspicious, revise.
            // Escape?
            // TODO - make separate action for this and have separate key binding?
            // This has advantage in that it picks up the key regardless of modifiers
            if ((content.length() == 1) && (((short)content.charAt(0)) == 27)) {
                /* This no longer seems true -- debug!
                // Already processed by separate Escape key listener in the DesignerPane;
                // special handled there since the Escape key needs to be discovered
                // during DND etc. in which case this action isn't active
                 */
//                if (!webform.getTopComponent().seenEscape(e.getWhen())) {
//                if (!webform.tcSeenEscape(e)) {
//                    webform.getManager().getMouseHandler().escape();
//                }
                target.escape(e.getWhen());

                return;
            }

            int mod = e.getModifiers();

            if ((content.length() > 0) &&
                    ((mod & ActionEvent.ALT_MASK) == (mod & ActionEvent.CTRL_MASK))) {
                char c = content.charAt(0);

                if (((c >= 0x20) && (c != 0x7F)) || (c == '\r') || (c == '\n')) {
//                    if (target.getCaret() == null) {
                    if (!target.hasCaret()) {
                        if ((c != '\r') && (c != '\n')) {
                            // If you're typing without a caret, start editing
                            // the default property of the selection
                            boolean success = webform.getSelection().focusDefaultProperty(e);

                            if (!success) {
                                UIManager.getLookAndFeel().provideErrorFeedback(target);
                            }

                            return;
                        } else if ((c == '\r') || (c == '\n')) {
                            // Enter key has same effect as double click
//                            webform.getActions().handleDoubleClick(true);
//                            webform.getManager().handleDoubleClick(true);
//                            webform.getManager().handleDoubleClick();
                            DesignerEvent evt = new DefaultDesignerEvent(webform, null);
                            webform.fireUserActionPerformed(evt);

                            return;
                        } else {
                            UIManager.getLookAndFeel().provideErrorFeedback(target);

                            return;
                        }
                    }

                    // XXX Moved from DesingerPaneBase
                    // XXX Hack, check whether to finish inline editing.
                    // TODO Fix inline editing.
                    InlineEditor editor = webform.getManager().getInlineEditor();
                    if ((content.equals("\n") || content.equals("\r\n")) // NOI18N
                    && (editor != null) && !editor.isMultiLine()) {
                        // Commit
                        // Should I look to see if the Shift key is pressed, and if so let
                        // you insert a newline?
                        webform.getManager().finishInlineEditing(false);
                        return;
                    }
                    
////                    UndoEvent undoEvent = webform.getModel().writeLock(NbBundle.getMessage(DefaultKeyTypedAction.class, "InsertChar")); // NOI18N
//                    DomProvider.WriteLock writeLock = webform.writeLock(NbBundle.getMessage(DefaultKeyTypedAction.class, "InsertChar")); // NOI18N
//                    try {
//                        doc.writeLock(NbBundle.getMessage(DefaultKeyTypedAction.class, "InsertChar")); // NOI18N
//                        target.getCaret().replaceSelection(content);
                        target.replaceSelection(content);
//                    } finally {
////                        doc.writeUnlock();
////                        webform.getModel().writeUnlock(undoEvent);
//                        webform.writeUnlock(writeLock);
//                    }
                } // 0x7f (del) will get processed by a different action
            }
        }
    }
}
