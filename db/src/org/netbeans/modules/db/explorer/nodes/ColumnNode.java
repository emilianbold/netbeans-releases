/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.nodes;

import org.openide.nodes.Children;
import java.io.IOException;
import java.util.*;
import java.text.MessageFormat;
import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.impl.*;
import org.netbeans.modules.db.*;
import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.explorer.infos.*;
import org.openide.util.datatransfer.PasteType;
import java.awt.datatransfer.Transferable;
import org.openide.nodes.NodeTransfer;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.nodes.*;

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
            String table = (String)info.get(DatabaseNode.TABLE);
            Specification spec = (Specification)info.getSpecification();
            RenameColumn cmd = spec.createCommandRenameColumn(table);
            cmd.renameColumn(info.getName(), newname);
            cmd.setObjectOwner((String)info.get(DatabaseNodeInfo.SCHEMA));
            cmd.execute();
            super.setName(newname);
        } catch (CommandNotSupportedException ex) {
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
