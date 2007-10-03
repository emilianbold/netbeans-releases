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

package org.netbeans.modules.db.explorer.infos;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;

import org.netbeans.lib.ddl.DDLException;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.lib.ddl.impl.DropIndex;
import org.netbeans.lib.ddl.impl.Specification;

import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.explorer.DatabaseNodeChildren;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;

public class IndexNodeInfo extends TableNodeInfo {
    static final long serialVersionUID =-8633867970381524742L;
    static final Logger LOGGER = Logger.getLogger(IndexNodeInfo.class.getName());
    
    public void initChildren(Vector children) throws DatabaseException {
        try {
            String table = (String)get(DatabaseNode.TABLE);

            DriverSpecification drvSpec = getDriverSpecification();
            drvSpec.getIndexInfo(table, false, true);
            ResultSet rs = drvSpec.getResultSet();
            if (rs != null) {
                HashMap rset = new HashMap();
                DatabaseNodeInfo info;
                while (rs.next()) {
                    rset = drvSpec.getRow();
                    String ixname = (String)get("index"); //NOI18N
                    info = DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.INDEXCOLUMN, rset);
                    String newixname = (String)info.get("ixname"); //NOI18N
                    if (ixname != null && newixname != null && newixname.equals(ixname)) {
                        String way;
                        if (info.get("ord") instanceof java.lang.Boolean)  //NOI18N //HACK for PointBase
                            way = "A"; //NOI18N
                        else
                            way = (String) info.get("ord"); //NOI18N
                        if (way == null) way = "A"; //NOI18N
                        info.put(DatabaseNodeInfo.ICONBASE, info.get(DatabaseNodeInfo.ICONBASE+way));
                        if (info != null)
                            children.add(info);
                        else {
                            rs.close();
                            throw new Exception(bundle().getString("EXC_UnableToCreateIndexNodeInfo")); //NOI18N
                        }
                    }
                    rset.clear();
                }
                rs.close();
            }
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    public void refreshChildren() throws DatabaseException {
        // create list (infos)
        Vector charr = new Vector();
        put(DatabaseNodeInfo.CHILDREN, charr);
        initChildren(charr);
        
        // create sub-tree (by infos)
        try {
            Node[] subTreeNodes = new Node[charr.size()];

            // current sub-tree
            DatabaseNodeChildren children = (DatabaseNodeChildren)getNode().getChildren();

            // remove current sub-tree
            children.remove(children.getNodes());

            // build refreshed sub-tree
            for(int i=0; i<charr.size(); i++)
                subTreeNodes[i] = children.createNode((DatabaseNodeInfo)charr.elementAt(i));

            // add built sub-tree
            children.add(subTreeNodes);
        } catch (Exception ex) {
            Logger.getLogger("global").log(Level.INFO, null, ex);
        }
    }

    public void delete() throws IOException {
        try {
            String table = (String)get(DatabaseNode.TABLE);
            Specification spec = (Specification)getSpecification();
            DDLHelper.deleteIndex(spec, (String)get(DatabaseNodeInfo.SCHEMA), 
                    table, getName());

            //refresh list of columns due to the column's icons
            getParent(DatabaseNode.TABLE).refreshChildren();
        } catch (DDLException e) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, null, e);
            throw new IOException(e.toString());
        }
    }
}
