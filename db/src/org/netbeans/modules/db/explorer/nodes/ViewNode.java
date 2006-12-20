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

import java.util.*;
import java.text.MessageFormat;

import org.openide.*;
import org.openide.util.NbBundle;

import org.netbeans.lib.ddl.*;
import org.netbeans.lib.ddl.impl.*;
import org.netbeans.modules.db.*;
import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.explorer.infos.*;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.PasteType;


// Node for Table/View/Procedure things.

public class ViewNode extends DatabaseNode {
    public void setName(String newname)
    {
        try {
            DatabaseNodeInfo info = getInfo();
            Specification spec = (Specification)info.getSpecification();
            AbstractCommand cmd = spec.createCommandRenameView(info.getName(), newname);
            cmd.setObjectOwner((String)info.get(DatabaseNodeInfo.SCHEMA));
            cmd.execute();
            super.setName(newname);
            info.put(DatabaseNode.TABLE, newname);
            info.put(DatabaseNode.VIEW, newname);
        } catch (CommandNotSupportedException exc) {
            String message = MessageFormat.format(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("EXC_UnableToChangeName"), new String[] {exc.getCommand()}); // NOI18N
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getShortDescription() {
        return NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ND_View"); //NOI18N
    }

    public Transferable clipboardCopy() throws IOException {
        Transferable result;
        final DbMetaDataTransferProvider dbTansferProvider = (DbMetaDataTransferProvider)Lookup.getDefault().lookup(DbMetaDataTransferProvider.class);
        if (dbTansferProvider != null) {
            ExTransferable exTransferable = ExTransferable.create(super.clipboardCopy());
            ConnectionNodeInfo cni = (ConnectionNodeInfo)getInfo().getParent(DatabaseNode.CONNECTION);
            final DatabaseConnection dbconn = ConnectionList.getDefault().getConnection(cni.getDatabaseConnection());
            exTransferable.put(new ExTransferable.Single(dbTansferProvider.getViewDataFlavor()) {
                protected Object getData() {
                    return dbTansferProvider.createViewData(dbconn.getDatabaseConnection(), dbconn.findJDBCDriver(), getInfo().getName());
                }
            });
            result = exTransferable;
        } else {
            result = super.clipboardCopy();
        }
        return result;
    }
}
