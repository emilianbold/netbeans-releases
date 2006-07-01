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


import java.io.IOException;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.NodeAction;

/** Instantiate a template.
* Enabled only when there is one selected node and
* it represents a data object satisfying {@link DataObject#isTemplate}.
*
* @author   Jaroslav Tulach
*
* @deprecated Deprecated since 3.42. The use of this action should be avoided.
*/
public class InstantiateAction extends NodeAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 1482795804240508824L;

    protected boolean enable (Node[] activatedNodes) {
        if (activatedNodes.length != 1) return false;
        DataObject obj = (DataObject)activatedNodes[0].getCookie (DataObject.class);
        return obj != null && obj.isTemplate ();
    }

    protected void performAction (Node[] activatedNodes) {
        DataObject obj = (DataObject)activatedNodes[0].getCookie (DataObject.class);
        if (obj != null && obj.isTemplate ()) {
            try {
                instantiateTemplate (obj);
            } catch (UserCancelException ex) {
                // canceled by user
                // do not notify the exception
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /* @return the name of the action
    */
    public String getName() {
        return NbBundle.getMessage(org.openide.loaders.DataObject.class, "Instantiate");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (InstantiateAction.class);
    }

    /** Instantiate a template object.
    * Asks user for the target file's folder and creates the file.
    * Then runs the node delegate's {@link org.openide.nodes.NodeOperation#customize customizer} (if there is one).
    * Also the node's {@link Node#getDefaultAction default action}, if any, is run.
    * @param obj the template to use
    * @return set of created objects or null if user canceled the action
    * @exception IOException on I/O error
    * @see DataObject#createFromTemplate
    */
    public static java.util.Set instantiateTemplate (org.openide.loaders.DataObject obj)
    throws IOException {
        // Create component for for file name input
        return NewTemplateAction.getWizard (null).instantiate (obj);
    }
}
