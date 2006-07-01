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
package org.openide.actions;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;


/** Rename a node.
* @see Node#setName
*
* @author   Petr Hamernik, Dafe Simonek
*/
public class RenameAction extends NodeAction {
    protected boolean surviveFocusChange() {
        return false;
    }

    public String getName() {
        return NbBundle.getMessage(RenameAction.class, "Rename");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(RenameAction.class);
    }

    protected boolean enable(Node[] activatedNodes) {
        // exactly one node should be selected
        if ((activatedNodes == null) || (activatedNodes.length != 1)) {
            return false;
        }

        // and must support renaming
        return activatedNodes[0].canRename();
    }

    protected void performAction(Node[] activatedNodes) {
        Node n = activatedNodes[0]; // we supposed that one node is activated

        NotifyDescriptor.InputLine dlg = new NotifyDescriptor.InputLine(
                NbBundle.getMessage(RenameAction.class, "CTL_RenameLabel"),
                NbBundle.getMessage(RenameAction.class, "CTL_RenameTitle")
            );
        dlg.setInputText(n.getName());

        if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dlg))) {
            String newname = null;

            try {
                newname = dlg.getInputText();

                if (!newname.equals("")) {
                    n.setName(dlg.getInputText()); // NOI18N
                }
            } catch (IllegalArgumentException e) {
                // determine if "printStackTrace"  and  "new annotation" of this exception is needed
                boolean needToAnnotate = Exceptions.findLocalizedMessage(e) == null;

                // annotate new localized message only if there is no localized message yet
                if (needToAnnotate) {
                    Exceptions.attachLocalizedMessage(e,
                                                      NbBundle.getMessage(RenameAction.class,
                                                                          "MSG_BadFormat",
                                                                          n.getName(),
                                                                          newname));
                }

                Exceptions.printStackTrace(e);
            }
        }
    }

    protected boolean asynchronous() {
        return false;
    }
}
