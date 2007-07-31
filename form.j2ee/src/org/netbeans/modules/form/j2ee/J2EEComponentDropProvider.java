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

package org.netbeans.modules.form.j2ee;

import java.awt.datatransfer.Transferable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.db.explorer.DatabaseMetaDataTransfer;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.NewComponentDrop;
import org.netbeans.modules.form.NewComponentDropProvider;

/**
 * Provider of <code>NewComponentDrop</code>s for DB and J2EE objects.
 *
 * @author Jan Stola
 */
public class J2EEComponentDropProvider implements NewComponentDropProvider {
    
    /**
     * Processes given <code>transferable</code> and returns the corresponding
     * <code>NewComponentDrop</code>.
     *
     * @param formModel corresponding form model.
     * @param transferable description of transferred data.
     * @return <code>NewComponentDrop</code> that corresponds to given
     * <code>transferable</code> or <code>null</code> if this provider
     * don't understand to or don't want to process this data transfer.
     */
    public NewComponentDrop processTransferable(FormModel formModel, Transferable transferable) {
        try {
            if (transferable.isDataFlavorSupported(DatabaseMetaDataTransfer.CONNECTION_FLAVOR)) {
                DatabaseMetaDataTransfer.Connection connection = (DatabaseMetaDataTransfer.Connection)transferable.getTransferData(DatabaseMetaDataTransfer.CONNECTION_FLAVOR);
                return new DBConnectionDrop(formModel, connection);
            } else if (transferable.isDataFlavorSupported(DatabaseMetaDataTransfer.COLUMN_FLAVOR)) {
                DatabaseMetaDataTransfer.Column column = (DatabaseMetaDataTransfer.Column)transferable.getTransferData(DatabaseMetaDataTransfer.COLUMN_FLAVOR);
                return new DBColumnDrop(formModel, column);
            } else if (transferable.isDataFlavorSupported(DatabaseMetaDataTransfer.TABLE_FLAVOR)) {
                DatabaseMetaDataTransfer.Table table = (DatabaseMetaDataTransfer.Table)transferable.getTransferData(DatabaseMetaDataTransfer.TABLE_FLAVOR);
                return new DBTableDrop(formModel, table);
            }
        } catch (Exception ex) {
            // should not happen
            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
        }
        return null;
    }
    
}
