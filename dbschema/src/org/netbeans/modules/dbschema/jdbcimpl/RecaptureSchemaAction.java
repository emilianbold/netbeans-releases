/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.dbschema.jdbcimpl;

import java.sql.SQLException;
import java.util.ResourceBundle;
import org.netbeans.modules.dbschema.jdbcimpl.wizard.RecaptureSchema;
import org.openide.ErrorManager;

import org.openide.loaders.*;
import org.openide.nodes.Node;
import org.openide.util.actions.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class RecaptureSchemaAction extends CookieAction {

    /** Create. new ObjectViewAction. */
    public RecaptureSchemaAction() {
    }

    /** Name of the action. */
    public String getName () {
        return NbBundle.getBundle("org.netbeans.modules.dbschema.jdbcimpl.resources.Bundle").getString("ActionNameRecap"); //NOI18N
    }

    /** No help yet. */
    public HelpCtx getHelpCtx () {
        return null; //new HelpCtx("dbschema_ctxhelp_wizard"); //NOI18N
    }

    protected String iconResource () {
        return "org/netbeans/modules/dbschema/jdbcimpl/DBschemaDataIcon.gif"; //NOI18N
    }

    protected Class[] cookieClasses() {
        return new Class[] {
                   DBschemaDataObject.class
               };
    }

    protected int mode() {
        return MODE_ONE;
    }

    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    public void performAction (Node[] activatedNodes) {
        try {
            if (activatedNodes.length == 1) {
                new RecaptureSchema(activatedNodes[0]).start();
            }
        }
        catch (ClassNotFoundException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        catch (SQLException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
}
