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
package org.netbeans.modules.sql.framework.ui.view.join;

import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.netbeans.modules.sql.framework.model.DBColumn;
import org.netbeans.modules.sql.framework.model.SQLConnectableObject;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.ui.graph.IGraphNode;
import org.netbeans.modules.sql.framework.ui.graph.IGraphPort;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.graph.impl.BasicCellArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.ColumnArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.PortArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.TableArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.TableConstants;
import org.netbeans.modules.sql.framework.ui.utils.UIUtil;
import org.netbeans.modules.sql.framework.ui.view.graph.MetaTableModel;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLBasicTableArea;
import com.nwoods.jgo.JGoLink;
import com.nwoods.jgo.JGoListPosition;
import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoText;
import com.sun.etl.exception.BaseException;
import java.util.logging.Level;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.DBTable;
import org.openide.awt.StatusDisplayer;

/**
 *
 * @author Ritesh Adval
 * @version $Revision$
 */
public class SQLTableArea extends TableArea implements TableModelListener {

    /* log4j logger category */
    private static final String LOG_CATEGORY = SQLTableArea.class.getName();
    private TableModel dataModel;
    //private static transient final Logger mLogger = Logger.getLogger(SQLTableArea.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LOG_CATEGORY);

    /**
     * Creates a new instance of SQLTableArea
     *
     * @param tType type of the table
     * @param table table
     */
    public SQLTableArea(int tType, DBTable table) {
        super(tType);
        MetaTableModel model = new MetaTableModel(table, tType);
        setModel(model);
        initialize(tType);

        if (columnAreas != null) {
            Iterator it = columnAreas.iterator();
            while (it.hasNext()) {
                ColumnArea column = (ColumnArea) it.next();
                switch (tType) {
                    case TableConstants.INPUT_TABLE:
                        column.setTextAlignment(JGoText.ALIGN_RIGHT);
                        break;
                    case TableConstants.OUTPUT_TABLE:
                        column.setTextAlignment(JGoText.ALIGN_LEFT);
                        break;
                    case TableConstants.NO_PORT_TABLE:
                        column.setTextAlignment(JGoText.ALIGN_LEFT);
                        break;
                    case TableConstants.INPUT_OUTPUT_TABLE:
                    default:
                        column.setTextAlignment(JGoText.ALIGN_CENTER);
                        break;
                }
            }
        }
    }

    /**
     * Creates a new instance of SQLTableArea
     *
     * @param myType type of the table
     * @param myDataModel associated table model
     */
    public SQLTableArea(int myType, TableModel myDataModel) {
        super(myType);
        setModel(myDataModel);
    }

    public void setTableObject(DBTable table) {
        removeAll();
        initGui();
        MetaTableModel model = new MetaTableModel(table, this.getTableType());
        setModel(model);
        initialize(this.getTableType());
    }

    /**
     * set the table model
     *
     * @param dataModel table model
     */
    public void setModel(TableModel dataModel) {
        removeAll();

        if (dataModel == null) {
            throw new IllegalArgumentException("Cannot set a null TableModel");
        }
        if (this.dataModel != dataModel) {
            TableModel old = this.dataModel;
            if (old != null) {
                old.removeTableModelListener(this);
            }
            this.dataModel = dataModel;
            dataModel.addTableModelListener(this);
        }
        initializeTable(dataModel);
    }

    /**
     * get the table model
     *
     * @return table model
     */
    public TableModel getModel() {
        return dataModel;
    }

    private void initializeTable(TableModel dataMod) {
        int columnCount = dataMod.getColumnCount();
        int rowCount = dataMod.getRowCount();

        for (int i = 0; i < columnCount; i++) {
            String columnName = dataMod.getColumnName(i);
            addColumn(columnName);

            for (int j = 0; j < rowCount; j++) {
                SQLDBColumn rowVal = (SQLDBColumn) dataMod.getValueAt(j, i);
                addItem(j, i, rowVal.getName(), UIUtil.getColumnToolTip(rowVal));
            }
        }
    }

    /**
     * This fine grain notification tells listeners the exact range of cells, rows, or
     * columns that changed.
     *
     * @param e table model event
     */
    public void tableChanged(TableModelEvent e) {
        int firstRow = e.getFirstRow();
        int lastRow = e.getLastRow();

        switch (e.getType()) {
            case TableModelEvent.INSERT:
                insertRow(firstRow, lastRow);
                break;
            case TableModelEvent.DELETE:
                deleteRow(firstRow, lastRow);
                break;
            case TableModelEvent.UPDATE:
                updateRow(firstRow, lastRow);
                break;
        }
    }

    private void insertRow(int firstRow, int lastRow) {
        int columnCount = dataModel.getColumnCount();

        for (int i = 0; i < columnCount; i++) {
            for (int j = firstRow; j <= lastRow; j++) {
                SQLDBColumn rowVal = (SQLDBColumn) dataModel.getValueAt(j, i);
                addItem(j, i, rowVal.getName(), UIUtil.getColumnToolTip(rowVal));
            }
        }
    }

    private void deleteRow(int firstRow, int lastRow) {
        int columnCount = dataModel.getColumnCount();
        deleteLinks(firstRow, lastRow);
        for (int i = 0; i < columnCount; i++) {
            for (int j = firstRow; j <= lastRow; j++) {
                this.removeItem(j, i);
            }
        }
    }

    private void deleteLinks(int firstRow, int lastRow) {
        //before removing rows we need to find if port
        //we are removing has any links and if so we should remove them
        ArrayList<JGoLink> links = new ArrayList<JGoLink>();

        for (int k = firstRow; k <= lastRow; k++) {
            IGraphPort port = this.getLeftGraphPort(k);
            if (port != null) {
                this.addLinks(port, links);
            }

            port = this.getRightGraphPort(k);
            if (port != null) {
                this.addLinks(port, links);
            }
        }
        //now delete the links
        SQLBasicTableArea tableArea = (SQLBasicTableArea) this.getParent();
        if (tableArea != null) {
            IGraphView view = tableArea.getGraphView();
            if (view != null) {
                view.deleteLinks(links);
            }
        }
    }

    private void updateRow(int firstRow, int lastRow) {
        int columnCount = dataModel.getColumnCount();

        for (int i = 0; i < columnCount; i++) {
            ColumnArea cArea = this.getColumnArea(i);
            if (cArea == null) {
                continue;
            }

            for (int j = firstRow; j <= lastRow; j++) {
                DBColumn rowVal = (DBColumn) dataModel.getValueAt(j, i);
                BasicCellArea cell = cArea.getCellAt(j);
                if (cell != null) {
                    cell.setText(rowVal.getName());
                }
            }
        }
    }

    public boolean isColumnMapped(SQLDBColumn column) {
        int columnCount = dataModel.getColumnCount();
        int rowCount = dataModel.getRowCount();

        for (int i = 0; i < columnCount; i++) {
            for (int j = 0; j < rowCount; j++) {
                DBColumn rowVal = (DBColumn) dataModel.getValueAt(j, i);
                if (rowVal.equals(column)) {
                    PortArea p = this.rightPortArea.getPortAreaAt(j);
                    IGraphPort rightPort = p.getGraphPort();
                    if (rightPort != null) {
                        JGoPort port = (JGoPort) rightPort;
                        if (port.getNumLinks() > 0) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public void makeColumnVisible(SQLDBColumn column) {
        int columnCount = dataModel.getColumnCount();
        int rowCount = dataModel.getRowCount();

        for (int i = 0; i < columnCount; i++) {
            for (int j = 0; j < rowCount; j++) {
                DBColumn rowVal = (DBColumn) dataModel.getValueAt(j, i);
                if (rowVal.equals(column)) {
                    //add cell
                    this.addItem(j, i, column.getName(), column.toString());
                }
            }
        }
    }

    public void makeColumnInVisible(SQLDBColumn column) throws BaseException {
        try {
            //first try to remove column refs
            removeColumnReference(column);
        } catch (BaseException ex) {
            String msg = mLoc.t("EDIT193: Error making column invisible, unable to remove column references.{0}", column.getName());
            StatusDisplayer.getDefault().setStatusText(msg.substring(15) + ex.getMessage());
            logger.log(Level.SEVERE, mLoc.t("EDIT193: Error making column invisible, unable to remove column references.{0}", column.getName())+ ex);
            throw ex;
        }

        int columnCount = dataModel.getColumnCount();
        int rowCount = dataModel.getRowCount();

        for (int i = 0; i < columnCount; i++) {
            for (int j = 0; j < rowCount; j++) {
                DBColumn rowVal = (DBColumn) dataModel.getValueAt(j, i);
                if (rowVal.equals(column)) {
                    ColumnArea cArea = this.getColumnArea(i);
                    //remove cell
                    cArea.removeItem(j);

                    //remove port
                    this.rightPortArea.removePort(j);
                }
            }
        }
    }

    public void removeColumnReference(SQLDBColumn column) throws BaseException {
        int columnCount = dataModel.getColumnCount();
        int rowCount = dataModel.getRowCount();

        for (int i = 0; i < columnCount; i++) {
            for (int j = 0; j < rowCount; j++) {
                DBColumn rowVal = (DBColumn) dataModel.getValueAt(j, i);
                if (rowVal.equals(column)) {
                    //remove column reference
                    PortArea p = this.rightPortArea.getPortAreaAt(j);
                    IGraphPort rightPort = p.getGraphPort();
                    if (rightPort != null) {
                        JGoPort port = (JGoPort) rightPort;
                        removeColumnFromExpressionObject(port, column);
                        return;
                    }
                }
            }
        }
    }

    private void removeColumnFromExpressionObject(JGoPort port, SQLDBColumn column) throws BaseException {
        JGoListPosition pos = port.getFirstLinkPos();
        while (pos != null) {
            JGoLink link = port.getLinkAtPos(pos);
            pos = port.getNextLinkPos(pos);
            JGoPort toPort = link.getToPort();
            IGraphPort toGraphPort = (IGraphPort) toPort;

            IGraphNode node = toGraphPort.getDataNode();
            String argName = node.getFieldName(toGraphPort);

            Object dataObj = node.getDataObject();
            if (dataObj != null && dataObj instanceof SQLConnectableObject) {
                SQLConnectableObject expObj = (SQLConnectableObject) dataObj;
                expObj.removeInputByArgName(argName, column);
            }
        }
    }
}
