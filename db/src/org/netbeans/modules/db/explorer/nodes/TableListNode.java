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

package org.netbeans.modules.db.explorer.nodes;

import org.openide.util.NbBundle;

public class TableListNode extends DatabaseNode {
    public TableListNode() {
        setDisplayName(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NDN_Tables")); //NOI18N
    }    
    
    public String getShortDescription() {
        return NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ND_TableList"); //NOI18N
    }
}
