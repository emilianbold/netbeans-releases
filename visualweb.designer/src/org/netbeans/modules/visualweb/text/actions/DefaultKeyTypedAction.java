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
package org.netbeans.modules.visualweb.text.actions;

import org.netbeans.modules.visualweb.api.designer.HtmlDomProvider;
import java.awt.event.ActionEvent;

import javax.swing.UIManager;

import org.openide.util.NbBundle;

import org.netbeans.modules.visualweb.designer.WebForm;
import org.netbeans.modules.visualweb.text.DesignerPaneBase;
import org.netbeans.modules.visualweb.text.Document;


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

//            if (webform.getModel().getLiveUnit() == null) {
            // XXX This seems to be wrong. Model should handle it.
            if (!webform.isAlive()) {
                // Can't mutate document
                UIManager.getLookAndFeel().provideErrorFeedback(target);

                return;
            }

            String content = e.getActionCommand();

            if (content == null) {
                return;
            }

            // Escape?
            // TODO - make separate action for this and have separate key binding?
            // This has advantage in that it picks up the key regardless of modifiers
            if ((content.length() == 1) && (((short)content.charAt(0)) == 27)) {
                /* This no longer seems true -- debug!
                // Already processed by separate Escape key listener in the DesignerPane;
                // special handled there since the Escape key needs to be discovered
                // during DND etc. in which case this action isn't active
                 */
                if (!webform.getTopComponent().seenEscape(e.getWhen())) {
                    webform.getManager().getMouseHandler().escape();
                }

                return;
            }

            int mod = e.getModifiers();

            if ((content.length() > 0) &&
                    ((mod & ActionEvent.ALT_MASK) == (mod & ActionEvent.CTRL_MASK))) {
                char c = content.charAt(0);

                if (((c >= 0x20) && (c != 0x7F)) || (c == '\r') || (c == '\n')) {
                    if (target.getCaret() == null) {
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
                            webform.getManager().handleDoubleClick(true);

                            return;
                        } else {
                            UIManager.getLookAndFeel().provideErrorFeedback(target);

                            return;
                        }
                    }

//                    UndoEvent undoEvent = webform.getModel().writeLock(NbBundle.getMessage(DefaultKeyTypedAction.class, "InsertChar")); // NOI18N
                    HtmlDomProvider.WriteLock writeLock = webform.writeLock(NbBundle.getMessage(DefaultKeyTypedAction.class, "InsertChar")); // NOI18N
                    try {
//                        doc.writeLock(NbBundle.getMessage(DefaultKeyTypedAction.class, "InsertChar")); // NOI18N
                        target.getCaret().replaceSelection(content);
                    } finally {
//                        doc.writeUnlock();
//                        webform.getModel().writeUnlock(undoEvent);
                        webform.writeUnlock(writeLock);
                    }
                } // 0x7f (del) will get processed by a different action
            }
        }
    }
}
