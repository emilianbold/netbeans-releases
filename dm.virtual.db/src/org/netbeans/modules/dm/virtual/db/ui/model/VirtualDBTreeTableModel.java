/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.dm.virtual.db.ui.model;

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.dm.virtual.db.bootstrap.PropertyKeys;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBColumn;
import org.netbeans.modules.dm.virtual.db.model.VirtualDBTable;
import org.netbeans.modules.dm.virtual.db.model.VirtualDatabaseModel;
import org.netbeans.modules.dm.virtual.db.ui.VirtualDBNode;
import org.openide.explorer.view.NodeTreeModel;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle;

/**
 * @author Ahimanikya Satapathy
 */
public class VirtualDBTreeTableModel extends NodeTreeModel {

    private static Logger mLogger = Logger.getLogger(VirtualDBTreeTableModel.class.getName());
    private VirtualDBNode modelNode;

    public VirtualDBTreeTableModel() {
        super();
    }

    public void configureModel(VirtualDatabaseModel dbModel) {
        createNodes(dbModel);

        this.setNode(modelNode);
        this.setAsksAllowsChildren(false);
    }

    public VirtualDatabaseModel getModel() {
        updateUserObjects();
        VirtualDatabase dbModel = (VirtualDatabase) modelNode.getUserObject();
        return dbModel.getDeligate();
    }

    public VirtualDBNode getRootNode() {
        return modelNode;
    }

    @Override
    public boolean isLeaf(Object obj) {
        boolean isLeaf = false;

        if (obj instanceof VirtualDBNode) {
            isLeaf = ((VirtualDBNode) obj).isLeaf();
        }

        return isLeaf;
    }

    private void createNodes(VirtualDatabaseModel dbModel) {
        try {
            VirtualDatabase db = new VirtualDatabase(dbModel);
            modelNode = new VirtualDBNode(db);

            // Make a copy of the List so that sorting will not affect
            // the order of the source List.
            List fileList = new ArrayList(dbModel.getTables());
            Collections.sort(fileList);

            Iterator it = fileList.iterator();
            while (it.hasNext()) {
                VirtualDBTable table = (VirtualDBTable) it.next();
                VirtualTable ffTable = null;

                try {
                    String fileType = table.getProperty(PropertyKeys.LOADTYPE);
                    if (PropertyKeys.DELIMITED.equalsIgnoreCase(fileType)) {
                        ffTable = new DelimitedFlatfile(table);
                    } else if (PropertyKeys.FIXEDWIDTH.equalsIgnoreCase(fileType)) {
                        ffTable = new FixedWidthFlatfile(table);
                    } else {
                        ffTable = new VirtualTable(table) {
                        };
                    }

                    if (ffTable != null) {
                        VirtualDBNode fileNode = createVirtualDBNode(ffTable);
                        modelNode.getChildren().add(new Node[]{fileNode});
                    }
                } catch (IntrospectionException ignore) {
                    mLogger.log(Level.SEVERE, NbBundle.getMessage(VirtualDBTreeTableModel.class, "LOG_VirtualDBNode", table.getName()), ignore);
                }
            }
        } catch (IntrospectionException ignore) {
            mLogger.log(Level.SEVERE, NbBundle.getMessage(VirtualDBTreeTableModel.class, "LOG_VirtualDBNode", dbModel.getModelName()), ignore);
        }
    }

    private VirtualDBNode createVirtualDBNode(VirtualTable table) throws IntrospectionException {
        VirtualDBNode fileNode = null;

        if (table.getColumnList().size() == 0) {
            fileNode = new VirtualDBNode.Leaf(table);
        } else {
            fileNode = new VirtualDBNode(table);

            Collection fields = table.getColumnList();
            Iterator iter = fields.iterator();

            List newNodes = new ArrayList(fields.size());
            while (iter.hasNext()) {
                VirtualDBColumn column = (VirtualDBColumn) iter.next();
                VirtualColumn ffColumn = new VirtualColumn(column);
                newNodes.add(new VirtualDBNode.Leaf(ffColumn));
            }

            if (!newNodes.isEmpty()) {
                fileNode.getChildren().add((Node[]) newNodes.toArray(new Node[newNodes.size()]));
            }
        }

        return fileNode;
    }

    private void updateUserObjects() {
        if (modelNode == null) {
            return;
        }

        final Children children = modelNode.getChildren();
        Children.MUTEX.postReadRequest(new Runnable() {

            public void run() {
                Node[] nodes = children.getNodes(true);
                for (int i = 0; i < nodes.length; i++) {
                    VirtualDBNode aNode = (VirtualDBNode) nodes[i];
                    aNode.updateUserObject();
                }
            }
        });
    }
}

