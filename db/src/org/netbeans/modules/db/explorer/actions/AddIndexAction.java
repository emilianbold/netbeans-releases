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

package org.netbeans.modules.db.explorer.actions;

import java.sql.*;
import java.util.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseException;
import org.openide.nodes.*;

import org.netbeans.lib.ddl.impl.*;
import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.explorer.dlg.*;
import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.infos.*;
import org.openide.util.RequestProcessor;

public class AddIndexAction extends DatabaseAction {
    private static final Logger LOGGER = Logger.getLogger(AddIndexAction.class .getName());

    public void performAction (Node[] activatedNodes) {
        Node node;
        if (activatedNodes != null && activatedNodes.length>0)
            node = activatedNodes[0];
        else
            return;

        try {
            DatabaseNodeInfo info = (DatabaseNodeInfo)node.getCookie(DatabaseNodeInfo.class);
            final IndexListNodeInfo nfo = (IndexListNodeInfo)info.getParent(nodename);

            String tablename = (String)nfo.get(DatabaseNode.TABLE);
            String columnname = (String)nfo.get(DatabaseNode.COLUMN);

            Specification spec = (Specification)nfo.getSpecification();
            String index = (String)nfo.get(DatabaseNode.INDEX);
            DriverSpecification drvSpec = info.getDriverSpecification();

            // List columns not present in current index
            Vector cols = new Vector(5);

            drvSpec.getColumns(tablename, "%");
            ResultSet rs = drvSpec.getResultSet();
            HashMap rset = new HashMap();
            while (rs.next()) {
                rset = drvSpec.getRow();
                cols.add((String) rset.get(new Integer(4)));
                rset.clear();
            }
            rs.close();

            if (cols.size() == 0)
                throw new Exception(bundle().getString("EXC_NoUsableColumnInPlace")); // NOI18N

            // Create and execute command
            final AddIndexDialog dlg = new AddIndexDialog(cols, spec, info);
            dlg.setIndexName(tablename + "_idx"); // NOI18N
            if (dlg.run()) {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        try {
                            nfo.addIndex(dlg.getIndexName());
                            nfo.refreshChildren();
                        } catch (DatabaseException dbe) {
                            LOGGER.log(Level.INFO, dbe.getMessage(), dbe);
                            DbUtilities.reportError(bundle().getString("ERR_UnableToAddIndex"), dbe.getMessage()); // NOI18N
                        }

                    }
                });
            }
        } catch(Exception exc) {
            LOGGER.log(Level.INFO, exc.getMessage(), exc);
            DbUtilities.reportError(bundle().getString("ERR_UnableToAddIndex"), exc.getMessage()); // NOI18N
        }
    }
}
