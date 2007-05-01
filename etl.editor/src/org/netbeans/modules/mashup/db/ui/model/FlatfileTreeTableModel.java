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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.mashup.db.ui.model;

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.mashup.db.common.PropertyKeys;
import org.netbeans.modules.mashup.db.model.FlatfileDBColumn;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;
import org.netbeans.modules.mashup.db.model.FlatfileDatabaseModel;
import org.netbeans.modules.mashup.db.ui.FlatfileNode;
import org.openide.explorer.view.NodeTreeModel;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

import com.sun.sql.framework.utils.Logger;

/**
 * @author Ritesh Adval, Jonathan Giron
 * @version $Revision$
 */
public class FlatfileTreeTableModel extends NodeTreeModel {

    /* Log4J category string */
    private static final String LOG_CATEGORY = FlatfileTreeTableModel.class.getName();

    private FlatfileNode modelNode;

    /**
     * Creates a new default instance of FlatfileTreeTableModel.
     */
    public FlatfileTreeTableModel() {
        super();
    }

    /**
     * Configures this model using the contents of the given FlatfileDatabaseModel
     * instance.
     * 
     * @param dbModel FlatfileDatabaseModel containing FlatfileDBTable instances to be
     *        represented
     */
    public void configureModel(FlatfileDatabaseModel dbModel) {
        createNodes(dbModel);

        this.setNode(modelNode);
        this.setAsksAllowsChildren(false);
    }

    /**
     * Gets FlatfileDatabaseModel with contents modified as a result of user's selection
     * of columns
     * 
     * @return model containing user-customized content
     */
    public FlatfileDatabaseModel getModel() {
        updateUserObjects();
        FlatfileDatabase dbModel = (FlatfileDatabase) modelNode.getUserObject();
        return dbModel.getDeligate();
    }

    /**
     * Gets the root node for this model
     * 
     * @return FlatfileNode - the root node.
     */
    public FlatfileNode getRootNode() {
        return modelNode;
    }

    /**
     * Returns whether the specified node is a leaf node. The way the test is performed
     * depends on the askAllowsChildren setting.
     * 
     * @param obj is the leaf object to check
     * @return boolean true if the node is a leaf node; false otherwise
     * @see javax.swing.tree.DefaultTreeModel#isLeaf
     */
    public boolean isLeaf(Object obj) {
        boolean isLeaf = false;

        if (obj instanceof FlatfileNode) {
            isLeaf = ((FlatfileNode) obj).isLeaf();
        }

        return isLeaf;
    }

    private void createNodes(FlatfileDatabaseModel dbModel) {
        try {
            FlatfileDatabase db = new FlatfileDatabase(dbModel);
            modelNode = new FlatfileNode(db);

            // Make a copy of the List so that sorting will not affect
            // the order of the source List.
            List fileList = new ArrayList(dbModel.getTables());
            Collections.sort(fileList);

            Iterator it = fileList.iterator();
            while (it.hasNext()) {
                FlatfileDBTable table = (FlatfileDBTable) it.next();
                FlatfileTable ffTable = null;

                try {
                    String fileType = table.getProperty(PropertyKeys.LOADTYPE);
                    if (PropertyKeys.DELIMITED.equalsIgnoreCase(fileType)) {
                        ffTable = new DelimitedFlatfile(table);
                    } else if (PropertyKeys.FIXEDWIDTH.equalsIgnoreCase(fileType)) {
                        ffTable = new FixedWidthFlatfile(table);
                    }

                    if (ffTable != null) {
                        FlatfileNode fileNode = createFlatfileNode(ffTable);
                        modelNode.getChildren().add(new Node[] { fileNode});
                    }
                } catch (IntrospectionException ignore) {
                    Logger.printThrowable(Logger.ERROR, LOG_CATEGORY, this, "Caught exception while building FlatfileNode for " + table.getName(),
                        ignore);
                }
            }
        } catch (IntrospectionException ignore) {
            Logger.printThrowable(Logger.ERROR, LOG_CATEGORY, this, "Caught exception while building FlatfileNode for " + dbModel.getModelName(),
                ignore);
        }
    }

    private FlatfileNode createFlatfileNode(FlatfileTable table) throws IntrospectionException {
        FlatfileNode fileNode = null;

        if (table.getColumnList().size() == 0) {
            fileNode = new FlatfileNode.Leaf(table);
        } else {
            fileNode = new FlatfileNode(table);

            Collection fields = table.getColumnList();
            Iterator iter = fields.iterator();

            List newNodes = new ArrayList(fields.size());
            while (iter.hasNext()) {
                FlatfileDBColumn column = (FlatfileDBColumn) iter.next();
                FlatfileColumn ffColumn = new FlatfileColumn(column);
                newNodes.add(new FlatfileNode.Leaf(ffColumn));
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
                    FlatfileNode aNode = (FlatfileNode) nodes[i];
                    aNode.updateUserObject();
                }
            }
        });
    }
}

