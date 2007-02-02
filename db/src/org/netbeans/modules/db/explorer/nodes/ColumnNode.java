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

package org.netbeans.modules.db.explorer.nodes;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.Map;
import org.openide.nodes.PropertySupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.ExTransferable;

import org.netbeans.lib.ddl.CommandNotSupportedException;
import org.netbeans.lib.ddl.impl.RenameColumn;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.ConnectionList;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DatabaseTypePropertySupport;
import org.netbeans.modules.db.explorer.DbMetaDataTransferProvider;
import org.netbeans.modules.db.explorer.infos.ConnectionNodeInfo;
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

    public Transferable clipboardCopy() throws IOException {
        Transferable result;
        final DbMetaDataTransferProvider dbTansferProvider = (DbMetaDataTransferProvider)Lookup.getDefault().lookup(DbMetaDataTransferProvider.class);
        if (dbTansferProvider != null) {
            ExTransferable exTransferable = ExTransferable.create(super.clipboardCopy());
            ConnectionNodeInfo cni = (ConnectionNodeInfo)getInfo().getParent(DatabaseNode.CONNECTION);
            final DatabaseConnection dbconn = ConnectionList.getDefault().getConnection(cni.getDatabaseConnection());
            exTransferable.put(new ExTransferable.Single(dbTansferProvider.getColumnDataFlavor()) {
                protected Object getData() {
                    return dbTansferProvider.createColumnData(dbconn.getDatabaseConnection(), dbconn.findJDBCDriver(), getInfo().getTable(), getInfo().getName());
                }
            });
            result = exTransferable;
        } else {
            result = super.clipboardCopy();
        }
        return result;
    }
    
    public String getShortDescription() {
        return NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ND_Column"); //NOI18N
    }

    public boolean canDestroy() {
        if (getCookie(ViewColumnNodeInfo.class) != null)
            return false;
        
        Map removeColumnProps = (Map)getInfo().getSpecification().getProperties().get(Specification.REMOVE_COLUMN);
        String supported = (String)removeColumnProps.get("Supported"); // NOI18N
        if (supported != null) {
            return Boolean.valueOf(supported).booleanValue();
        }
        return true;
    }
}
