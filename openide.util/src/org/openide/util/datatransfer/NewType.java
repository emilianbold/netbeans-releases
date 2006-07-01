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
package org.openide.util.datatransfer;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import java.io.IOException;


/** Describes a type that can be created anew. Used by <a href="@org-openide-nodes/org/openide/nodes.Node#getNewTypes">Node.getNewTypes</a>.
*
* @author Jaroslav Tulach
*/
public abstract class NewType extends Object implements HelpCtx.Provider {
    /** Display name for the creation action. This should be
    * presented as an item in a menu.
    *
    * @return the name of the action
    */
    public String getName() {
        return NbBundle.getBundle(NewType.class).getString("Create");
    }

    /** Help context for the creation action.
    * @return the help context
    */
    public HelpCtx getHelpCtx() {
        return org.openide.util.HelpCtx.DEFAULT_HELP;
    }

    /** Create the object.
    * @exception IOException if something fails
    */
    public abstract void create() throws IOException;

    /* JST: Originally designed for dnd and it now uses getDropType () of a node.
    *
    * Create the object at a specific position.
    * The default implementation simply calls {@link #create()}.
    * Subclasses may
    * allow pastes to a specific index in their
    * children list (if the object has children indexed by integer).
    *
    * @param indx index to insert into, can be ignored if not supported
    * @throws IOException if something fails
    *
    public void createAt (int indx) throws IOException {
      create ();
    }
    */
}
