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

package org.netbeans.modules.db.explorer.actions;

import java.util.ResourceBundle;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

public class DatabaseAction extends CookieAction {
    
    static final long serialVersionUID =2711301279685474175L;
    
    protected static ResourceBundle bundle() {
        return NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle");
    }

    protected String aname;
    protected String nodename;

    public String getName() {
        return aname;
    }

    public void setName(String name) {
        aname = name;
    }

    public String getNode() {
        return nodename;
    }

    public void setNode(String name) {
        nodename = name;
    }

    /** Help context where to find more about the paste type action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx ("dbexpovew");
    }

    protected Class[] cookieClasses() {
        return new Class[] {
                   this.getClass()
               };
    }

    protected int mode() {
        return MODE_ONE;
    }

    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    public void performAction (Node[] activatedNodes) {
    }
    
    protected boolean asynchronous() {
        return false;
    }

}
