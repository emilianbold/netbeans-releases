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

package org.netbeans.modules.db.explorer.nodes;

import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class DriverListNode extends DatabaseNode {
    public DriverListNode() {
        setDisplayName(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NDN_Drivers")); //NOI18N
    }

    public void setInfo(DatabaseNodeInfo nodeinfo) {
        info = nodeinfo;
        processInfo();
    }
    
    public String getShortDescription() {
        return NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ND_DriverList"); //NOI18N
    }

    /** Help context where to find more about the paste type action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(DriverListNode.class);
    }

}
