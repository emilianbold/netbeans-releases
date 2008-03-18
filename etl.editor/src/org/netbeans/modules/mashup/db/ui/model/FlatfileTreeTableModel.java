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
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * @author Ritesh Adval, Jonathan Giron
 * @version $Revision$
 */
public class FlatfileTreeTableModel extends NodeTreeModel {

    private static transient final Logger mLogger = Logger.getLogger(FlatfileTreeTableModel.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
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
                    } else {
                        ffTable = new FlatfileTable(table) {
                        };
                    }

                    if (ffTable != null) {
                        FlatfileNode fileNode = createFlatfileNode(ffTable);
                        modelNode.getChildren().add(new Node[]{fileNode});
                    }
                } catch (IntrospectionException ignore) {
                    mLogger.errorNoloc(mLoc.t("EDIT071: Caught exception while building FlatfileNode for{0}", table.getName()), ignore);
                }
            }
        } catch (IntrospectionException ignore) {
            mLogger.errorNoloc(mLoc.t("EDIT071: Caught exception while building FlatfileNode for{0}", dbModel.getModelName()), ignore);
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

