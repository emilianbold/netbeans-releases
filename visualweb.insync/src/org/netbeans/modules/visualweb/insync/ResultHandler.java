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
 * Created on May 6, 2004
 *
 * @todo To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.visualweb.insync;

import java.awt.Dialog;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.ResultMessage;
import com.sun.rave.designtime.Customizer2;
import com.sun.rave.designtime.CustomizerResult;
import org.netbeans.modules.visualweb.insync.models.FacesModel;


/**
 * @author tor
 *
 * @todo To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ResultHandler {

    /**
     * Handle the result object; if necessary, pop up a dialog, etc.
     */
    public static void handleResult(Result r, FacesModel model) {
        if (r == null)  // success - do nothing
            return;

        ResultMessage[] messages = r.getMessages();
        if (messages != null) {

            // Decide if we need to show a dialog
            boolean hasDialog = false;
            DisplayAction[] options = r.getResultOptions();
            if (options != null && options.length > 0) {
                hasDialog = true;
            }
            else {
                for (int i = 0; i < messages.length; i++) {
                    if (messages[i].getMessageType() == ResultMessage.TYPE_CRITICAL) {
                        hasDialog = true;
                        break;
                    }
                }
            }

            if (!hasDialog) {
                // Status message: build up compound string
                StringBuffer sb = new StringBuffer(200);
                boolean hasWarning = false;
                for (int i = 0; i < messages.length; i++) {
                    ResultMessage m = messages[i];
                    if (m.getMessageType() == ResultMessage.TYPE_WARNING)
                        hasWarning = true;

                    sb.append(m.getDescription());
                    if (i < messages.length-1)
                        sb.append(' ').append('/').append(' ');
                }
                // This block only in place to get rid of warning that hasWarning is not read
                if (hasWarning) {
                }
                StatusDisplayer.getDefault().setStatusText(sb.toString());
            }
            else {
                // Dialog: build up component html string for option pane
                StringBuffer sb = new StringBuffer(400);
                sb.append("<html><body>");
                boolean hasCritical = false;
                boolean hasWarning = false;
                String title = r.getDialogTitle();
                if (title == null || title.length() == 0) {
                    title = NbBundle.getMessage(ResultHandler.class, "HandlerTitle");
                }
                String help = r.getDialogHelpKey();
                HelpCtx helpCtx = null;
                if ((help != null) && (help.length() > 0)) {
                    helpCtx = new HelpCtx(help);
                }
                for (int i = 0; i < messages.length; i++) {
                    ResultMessage m = messages[i];
                    if (m.getMessageType() == ResultMessage.TYPE_CRITICAL)
                        hasCritical = true;
                    else if (m.getMessageType() == ResultMessage.TYPE_WARNING)
                        hasWarning = true;

                    if (m.getDisplayName() != null) {
                        sb.append("<b>");
                        sb.append(m.getDisplayName());
                        sb.append("</b>");
                        sb.append("<br>");
                    }
                    if (m.getDescription() != null) {
                        sb.append(m.getDescription());
                        if (i < messages.length-1)
                            sb.append("<br>").append("<br>");
                    }
                    else {
                        sb.append("<br>");
                    }
                }
                sb.append("</body></html>");

                Object[] os = null;
                Object def = null;
                if (options != null && options.length > 0) {
                    os = new Object[options.length];
                    for (int i = 0; i < options.length; i++)
                        os[i] = options[i].getDisplayName();

                    if (options.length > 1)
                        def = os[0];
                } else {
                    // No options provided - so make a single OK button.
                    // Otherwise the default DialogDescriptor code will kick
                    // in and make an OK_CANCEL dialog, and there is no
                    // just-OK-button type.
                    def = new javax.swing.JButton(NbBundle.getMessage(ResultHandler.class, "OK"));
                    os = new Object[] { def };
                }
                DialogDescriptor dlg = new DialogDescriptor(
                        sb.toString(), title, true, os, def, DialogDescriptor.DEFAULT_ALIGN,
                        helpCtx, null);

                if (hasCritical)
                    dlg.setMessageType(NotifyDescriptor.ERROR_MESSAGE);
                else if (hasWarning)
                    dlg.setMessageType(NotifyDescriptor.WARNING_MESSAGE);
                else
                    dlg.setMessageType(NotifyDescriptor.INFORMATION_MESSAGE);

                Dialog dialog = DialogDisplayer.getDefault().createDialog(dlg);
                dialog.setModal(true);
                dialog.show();
                Object chosen = dlg.getValue();
                if (options != null && options.length > 0) {
                    for (int i = 0; i < options.length; i++) {
                        if (chosen == options[i].getDisplayName()) {
                            Result r2 = options[i].invoke();
                            handleResult(r2, model); // recurse
                            break;
                        }
                    }
                }
            }
        }

        if (r instanceof CustomizerResult) {
            CustomizerResult cr = (CustomizerResult)r;
            Customizer2 c = cr.getCustomizer();
            CustomizerDisplayer cd =
                new CustomizerDisplayer(cr.getCustomizeBean(), c, c.getHelpKey(), model);
            cd.show();
            return;
        }
    }

}
