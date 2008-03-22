/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.db.explorer.nodes;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.Map;
import org.netbeans.api.db.explorer.DatabaseMetaDataTransfer;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.ExTransferable;

import org.netbeans.lib.ddl.CommandNotSupportedException;
import org.netbeans.lib.ddl.impl.RenameColumn;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.ConnectionList;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DatabaseTypePropertySupport;
import org.netbeans.modules.db.explorer.DatabaseMetaDataTransferAccessor;
import org.netbeans.modules.db.explorer.infos.ConnectionNodeInfo;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.infos.ViewColumnNodeInfo;

public class ColumnNode extends LeafNode {
    public ColumnNode(DatabaseNodeInfo info) {
        super(info);
    }
    
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
        ExTransferable result = ExTransferable.create(super.clipboardCopy());
        ConnectionNodeInfo cni = (ConnectionNodeInfo)getInfo().getParent(DatabaseNode.CONNECTION);
        final DatabaseConnection dbconn = ConnectionList.getDefault().getConnection(cni.getDatabaseConnection());
        result.put(new ExTransferable.Single(DatabaseMetaDataTransfer.COLUMN_FLAVOR) {
            protected Object getData() {
                return DatabaseMetaDataTransferAccessor.DEFAULT.createColumnData(dbconn.getDatabaseConnection(), dbconn.findJDBCDriver(), getInfo().getTable(), getInfo().getName());
            }
        });
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
