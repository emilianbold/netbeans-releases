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
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;

import org.openide.nodes.NodeTransfer;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;

import org.netbeans.lib.ddl.impl.*;
import org.netbeans.modules.db.explorer.infos.*;

public class IndexNode extends DatabaseNode {
    public IndexNode(DatabaseNodeInfo info) {
        super(info);
    }

    @Override
    protected void createPasteTypes(Transferable t, List s)
    {
        super.createPasteTypes(t, s);
        Node node = NodeTransfer.node(t, NodeTransfer.MOVE);
        if (node != null) {
            ColumnNodeInfo nfo = (ColumnNodeInfo)node.getCookie(ColumnNodeInfo.class);
            if (nfo != null) s.add(new IndexPasteType((ColumnNodeInfo)nfo, null));
        }
    }

    class IndexPasteType extends PasteType
    {
        /** transferred info */
        private DatabaseNodeInfo info;

        /** the node to destroy or null */
        private Node node;

        /** Constructs new TablePasteType for the specific type of operation paste.
        */
        public IndexPasteType(ColumnNodeInfo info, Node node)
        {
            this.info = info;
            this.node = node;
        }

        /* @return Human presentable name of this paste type. */
        public String getName() {
            return NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("IndexPasteTypeName"); //NOI18N
        }

        /** Performs the paste action.
        * @return Transferable which should be inserted into the clipboard after
        *         paste action. It can be null, which means that clipboard content
        *         should stay the same.
        */
        public Transferable paste() throws IOException {
            IndexNodeInfo destinfo = (IndexNodeInfo)getInfo();
            
            if (info != null) {
                Specification spec;

                try {
                    spec = (Specification)info.getSpecification();
                    DriverSpecification drvSpec = info.getDriverSpecification();
                    drvSpec.getIndexInfo(info.getTable(), false, true);
                    ResultSet rs = drvSpec.getResultSet();
                    if (rs != null) {
                        String index = destinfo.getName();
                        HashSet ixrm = new HashSet();
                        HashMap rset = new HashMap();

                        while (rs.next()) {
                            rset = drvSpec.getRow();
                            String ixname = (String) rset.get(new Integer(6));
                            String colname = (String) rset.get(new Integer(9));
                            if (ixname.equals(index))
                                ixrm.add(colname);
                            rset.clear();
                        }
                        rs.close();

                        if (ixrm.contains(info.getName())) {
                            String message = MessageFormat.format(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("EXC_IndexContainsColumn"), new String[] {index, info.getName()}); // NOI18N
                            throw new IOException(message);
                        }

                        CreateIndex icmd = spec.createCommandCreateIndex(info.getTable());
                        icmd.setIndexName(destinfo.getName());
                        Iterator enu = ixrm.iterator();
                        while (enu.hasNext()) {
                            icmd.specifyColumn((String)enu.next());
                        }

                        icmd.specifyColumn(info.getName());
                        DropIndex dicmd = spec.createCommandDropIndex(index);
                        dicmd.setObjectOwner((String)destinfo.get(DatabaseNodeInfo.SCHEMA));
                        dicmd.execute();
                        icmd.setObjectOwner((String)destinfo.get(DatabaseNodeInfo.SCHEMA));
                        icmd.execute();

                        drvSpec.getIndexInfo(destinfo.getTable(), false, true);
                        rs = drvSpec.getResultSet();
                        if (rs != null) {
                            IndexNodeInfo ixinfo;
                            while (rs.next()) {
                                rset = drvSpec.getRow();
                                String ixname = (String) rset.get(new Integer(6));
                                String colname = (String) rset.get(new Integer(9));
                                if (ixname.equals(index) && colname.equals(info.getName())) {
                                    ixinfo = (IndexNodeInfo) DatabaseNodeInfo.createNodeInfo(destinfo, DatabaseNode.INDEX, rset);
                                    if (ixinfo != null) 
                                        destinfo.addChild(ixinfo);
                                    else
                                        throw new Exception(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("EXC_UnableToCreateIndexNodeInfo")); //NOI18N
                                }
                                rset.clear();
                            }
                            rs.close();
                        }
                    }
                } catch (Exception e) {
                    throw new IOException(e.getMessage());
                }

            } else
                throw new IOException(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("EXC_CannotFindIndexOwnerInformation"));
            
            return null;
        }
    }    
}
