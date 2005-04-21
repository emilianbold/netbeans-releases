/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.actions;

import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
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
                ErrorManager em = ErrorManager.getDefault();
                ErrorManager.Annotation[] ann = em.findAnnotations(e);

                // determine if "printStackTrace"  and  "new annotation" of this exception is needed
                boolean needToAnnotate = true;

                if ((ann != null) && (ann.length > 0)) {
                    for (int i = 0; i < ann.length; i++) {
                        String glm = ann[i].getLocalizedMessage();

                        if ((glm != null) && !glm.equals("")) { // NOI18N
                            needToAnnotate = false;
                        }
                    }
                }

                // annotate new localized message only if there is no localized message yet
                if (needToAnnotate) {
                    em.annotate(e, NbBundle.getMessage(RenameAction.class, "MSG_BadFormat", n.getName(), newname));
                }

                em.notify(e);
            }
        }
    }

    protected boolean asynchronous() {
        return false;
    }
}
