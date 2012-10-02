/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.dataview.table;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.SwingUtilities;
import javax.swing.table.*;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTableHeader;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.renderer.CheckBoxProvider;
import org.jdesktop.swingx.renderer.JRendererCheckBox;
import org.jdesktop.swingx.renderer.StringValues;
import org.jdesktop.swingx.table.DatePickerCellEditor;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.output.DataView;
import org.netbeans.modules.db.dataview.table.celleditor.*;
import org.netbeans.modules.db.dataview.util.DataViewUtils;
import org.netbeans.modules.db.dataview.util.DateType;
import org.netbeans.modules.db.dataview.util.TimestampType;

/**
 * A better-looking table than JTable, implements JXTable and a decorator to draw empty rows 
 *
 * @author Ahimanikya Satapathy
 */
public class ResultSetJXTable extends JXTableDecorator {

    private String[] columnToolTips;
    private final int multiplier;
    private static final String data = "WE WILL EITHER FIND A WAY, OR MAKE ONE."; // NOI18N
    private static final Logger mLogger = Logger.getLogger(ResultSetJXTable.class.getName());
    protected DataView dView;
    private final List<Integer> columnWidthList;
    private final static int MAX_COLUMN_WIDTH = 25;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public ResultSetJXTable(final DataView dataView) {
        this.dView = dataView;

        setShowGrid(true, true);
        setGridColor(GRID_COLOR);

        getTableHeader().setReorderingAllowed(false);
        setColumnControlVisible(true);
        getColumnControl().setToolTipText(org.openide.util.NbBundle.getMessage(ResultSetJXTable.class, "ResultSetJXTable.columnControl.tooltip"));
        setHorizontalScrollEnabled(true);
        setAutoResizeMode(JXTable.AUTO_RESIZE_OFF);
        setFillsViewportHeight(true);

        setHighlighters(HighlighterFactory.createAlternateStriping(Color.WHITE, ALTERNATE_ROW_COLOR));
        addHighlighter(new ColorHighlighter(HighlightPredicate.ROLLOVER_ROW, ROLLOVER_ROW_COLOR, null));

        setDefaultCellRenderers();
        setDefaultCellEditors();

        if (dView.getDataViewDBTable() != null) {
            columnToolTips = dView.getDataViewDBTable().getColumnToolTips();
        }
        multiplier = getFontMetrics(getFont()).stringWidth(data) / data.length() + 4;
        columnWidthList = getColumnWidthList();
        putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    }

    @Override
    protected JTableHeader createDefaultTableHeader() {
        return new JTableHeaderImpl(columnModel);
    }

    @Override
    protected RowSorter<? extends TableModel> createDefaultRowSorter() {
        return new StringFallbackRowSorter(this.getModel());
    }

    public void createTableModel(List<Object[]> rows, final JXTableRowHeader rowHeader) {
        assert SwingUtilities.isEventDispatchThread() : "Must be called from AWT thread";  //NOI18N
        assert rows != null;
        final TableModel tempModel = createModelFrom(rows);
        setModel(tempModel);
        if (!columnWidthList.isEmpty()) {
            setHeader(ResultSetJXTable.this, columnWidthList);
        }
        if (rowHeader != null) {
            rowHeader.setTable(ResultSetJXTable.this);
        }
    }

    @SuppressWarnings("deprecation")
    protected void setDefaultCellRenderers() {
        setDefaultRenderer(Object.class, new ResultSetCellRenderer());
        setDefaultRenderer(String.class, new ResultSetCellRenderer());
        setDefaultRenderer(Number.class, new ResultSetCellRenderer(StringValues.NUMBER_TO_STRING, JLabel.RIGHT));
        setDefaultRenderer(Boolean.class, new ResultSetCellRenderer(new CheckBoxProvider()));
        setDefaultRenderer(java.sql.Date.class, new ResultSetCellRenderer(StringValues.DATE_TO_STRING));
        setDefaultRenderer(java.sql.Time.class, new ResultSetCellRenderer(ResultSetCellRenderer.TIME_TO_STRING));
        setDefaultRenderer(java.sql.Timestamp.class, new ResultSetCellRenderer(ResultSetCellRenderer.DATETIME_TO_STRING));
        setDefaultRenderer(java.util.Date.class, new ResultSetCellRenderer(ResultSetCellRenderer.DATETIME_TO_STRING));
    }

    protected void setDefaultCellEditors() {

        KeyListener kl = createControKeyListener();
        JTextField txtFld = new JTextField();
        txtFld.addKeyListener(kl);

        setDefaultEditor(Object.class, new StringTableCellEditor(txtFld));
        setDefaultEditor(String.class, new StringTableCellEditor(txtFld));
        setDefaultEditor(java.sql.Time.class, new StringTableCellEditor(txtFld));
        setDefaultEditor(Blob.class, new BlobFieldTableCellEditor());
        setDefaultEditor(Clob.class, new ClobFieldTableCellEditor());
        
        JTextField numFld = new JTextField();
        txtFld.addKeyListener(kl);
        setDefaultEditor(Number.class, new NumberFieldEditor(numFld));

        JRendererCheckBox b = new JRendererCheckBox();
        b.addKeyListener(kl);
        setDefaultEditor(Boolean.class, new BooleanTableCellEditor(b));

        try {
            DatePickerCellEditor dateEditor = new DatePickerCellEditor(new SimpleDateFormat (DateType.DEFAULT_FOMAT_PATTERN));
            setDefaultEditor(java.sql.Date.class, dateEditor);
        } catch (NullPointerException npe) {
            mLogger.log(Level.WARNING, "While creating DatePickerCellEditor was thrown " + npe, npe);
        }

        try{
            DateTimePickerCellEditor dateTimeEditor = new DateTimePickerCellEditor(new SimpleDateFormat (TimestampType.DEFAULT_FORMAT_PATTERN));
            dateTimeEditor.addKeyListener(kl);
            setDefaultEditor(Timestamp.class, dateTimeEditor);
            setDefaultEditor(java.util.Date.class, dateTimeEditor);
        } catch (NullPointerException npe) {
            mLogger.log(Level.WARNING, "While creating DateTimePickerCellEditor was thrown " + npe, npe);
        }
    }

    protected KeyListener createControKeyListener() {
        return new KeyListener() {

            @Override
            public void keyTyped(KeyEvent arg0) {
            }

            @Override
            public void keyPressed(KeyEvent arg0) {
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
            }
        };
    }

    private void setHeader(JTable table, List<Integer> columnWidthList) {
        try {
            TableColumnModel cModel = table.getColumnModel();
            for (int i = 0; i < columnWidthList.size(); i++) {
                TableColumn column = cModel.getColumn(i);
                column.setPreferredWidth(columnWidthList.get(i));
            }
            table.getTableHeader().setColumnModel(cModel);
            for (int i = 0, I = getRSColumnCount(); i < I; i++) {
                DBColumn col = getDBColumn(i);
                TableColumn tc = cModel.getColumn(i);
                StringBuilder sb = new StringBuilder();
                sb.append("<html>");                                    //NOI18N
                if (col.getDisplayName() != null) {
                    sb.append(DataViewUtils.escapeHTML(
                            col.getDisplayName().toString()));
                }
                sb.append("</html>");                                  // NOI18N
                tc.setHeaderValue(sb.toString());
                tc.setIdentifier(col.getDisplayName() == null
                        ? "COL_" + i : col.getDisplayName());           //NOI18N
            }
        } catch (Exception e) {
            mLogger.log(Level.INFO, "Failed to set the size of the table headers" + e, e);
        }
    }

    private List<Integer> getColumnWidthList() {
        List<Integer> colWidthList = new ArrayList<Integer>();
        try {
            for (int i = 0, I = getRSColumnCount(); i < I; i++) {
                DBColumn col = getDBColumn(i);
                int fieldWidth = col.getDisplaySize();
                int labelWidth = col.getDisplayName().length();
                int colWidth = Math.max(fieldWidth, labelWidth) * multiplier;
                if (colWidth < 5) {
                    colWidth = 15 * multiplier;
                }
                if (colWidth > MAX_COLUMN_WIDTH * multiplier) {
                    colWidth = MAX_COLUMN_WIDTH * multiplier;
                }
                colWidthList.add(colWidth);
            }
        } catch (Exception e) {
            mLogger.log(Level.INFO, "Failed to set the size of the table headers" + e, e); // NOI18N
        }
        return colWidthList;
    }

    private TableModel createModelFrom(List<Object[]> rows) {
        DefaultTableModel dtm = getDefaultTableModel();
        for (int i = 0, I = getRSColumnCount(); i < I; i++) {
            DBColumn col = getDBColumn(i);
            dtm.addColumn(col.getDisplayName() != null
                    ? col.getDisplayName() : "COL_" + i);               //NOI18N
        }

        for (Object[] row : rows) {
            dtm.addRow(row);
        }
        return dtm;
    }

    public DBColumn getDBColumn(int col) {
        DBColumn dbcol = dView.getDataViewDBTable().getColumn(col);
        return dbcol;
    }

    public int getRSColumnCount() {
        return dView.getDataViewDBTable().getColumnCount();
    }

    protected DefaultTableModel getDefaultTableModel() {
        return new ResultSetTableModel(this);
    }

    @Override
    public boolean isEditable() {
        if(dView != null && dView.isEditable()) {
            return dView.isEditable();
        }
        return false;
    }
    
    // This is mainly used for set Tooltip for column headers

    private class JTableHeaderImpl extends JXTableHeader {

        public JTableHeaderImpl(TableColumnModel cm) {
            super(cm);
        }

        @Override
        public String getToolTipText(MouseEvent e) {
            return getColumnToolTipText(e);
        }

        @Override
        protected String getColumnToolTipText(MouseEvent e) {
            java.awt.Point p = e.getPoint();
            int index = columnModel.getColumnIndexAtX(p.x);
            try {
                int realIndex = columnModel.getColumn(index).getModelIndex();
                return columnToolTips[realIndex];
            } catch (ArrayIndexOutOfBoundsException aio) {
                return null;
            }
        }
    }
}


