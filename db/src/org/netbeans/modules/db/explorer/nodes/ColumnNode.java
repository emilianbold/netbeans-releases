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

import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

import org.netbeans.lib.ddl.CommandNotSupportedException;
import org.netbeans.lib.ddl.impl.RenameColumn;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.DatabaseTypePropertySupport;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.infos.ViewColumnNodeInfo;

public class ColumnNode extends LeafNode {
    protected PropertySupport createPropertySupport(String name, Class type, String displayName, String shortDescription, DatabaseNodeInfo rep, boolean writable, boolean expert) {
        PropertySupport ps;
        if (name.equals("datatype")) //NOI18N
            ps = new DatabaseTypePropertySupport(name, type, displayName, shortDescription, rep, writable, expert);
        else
            ps = super.createPropertySupport(name, type, displayName, shortDescription, rep, writable, expert);
        
        return ps;
    }

    public void setName(String newname) {
        try {
            DatabaseNodeInfo info = getInfo();
            String table = (String) info.get(DatabaseNode.TABLE);
            Specification spec = (Specification) info.getSpecification();
            RenameColumn cmd = spec.createCommandRenameColumn(table);
            cmd.renameColumn(info.getName(), newname);
            cmd.setObjectOwner((String) info.get(DatabaseNodeInfo.SCHEMA));
            cmd.execute();
            super.setName(newname);
        } catch (CommandNotSupportedException ex) {
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public String getShortDescription() {
        return NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ND_Column"); //NOI18N
    }

    public boolean canDestroy() {
        if (getCookie(ViewColumnNodeInfo.class) != null)
            return false;
        
        //WORKAROUND: IBM DB2 doesn't support delete column command
        String drv = getInfo().getDriver().toLowerCase();
        return !drv.startsWith("com.ibm.db2.jdbc");
    }
}
