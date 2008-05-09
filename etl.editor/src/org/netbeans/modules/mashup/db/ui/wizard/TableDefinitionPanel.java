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
package org.netbeans.modules.mashup.db.ui.wizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.MissingResourceException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.InputVerifier;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.netbeans.modules.mashup.db.common.PropertyKeys;
import org.netbeans.modules.mashup.db.common.SQLUtils;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;
import org.netbeans.modules.mashup.db.model.impl.FlatfileDBTableImpl;
import org.netbeans.modules.mashup.tables.wizard.MashupTableWizardIterator;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * Captures information needed to determine the record and field layout of a file to be
 * imported into an ETL process. <br>
 * <br>
 * TODO: Add Foreign Key support in flatfile database wizard, show graphical
 * representation of the relation between table.
 *
 * @author Jonathan Giron
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class TableDefinitionPanel implements ListSelectionListener,
        WizardDescriptor.FinishablePanel {
    private static transient final Logger mLogger = Logger.getLogger(TableDefinitionPanel.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    
    class BoundedIntegerVerifier extends InputVerifier {
        private int max;
        private int min;
        
        public BoundedIntegerVerifier(int minValue, int maxValue) {
            if (maxValue < minValue) {
                throw new IllegalArgumentException("maxValue < minValue!");
            }
            
            min = minValue;
            max = maxValue;
        }
        
        public void setMaximum(int newMax) {
            if (newMax < min) {
                throw new IllegalArgumentException("newMax < current minimum value!");
            }
            max = newMax;
        }
        
        public void setMinimum(int newMin) {
            if (newMin > max) {
                throw new IllegalArgumentException("newMax < current minimum value!");
            }
            min = newMin;
        }
        
        public boolean verify(JComponent input) {
            if (input instanceof JTextField) {
                String valStr = ((JTextField) input).getText();
                try {
                    int value = Integer.parseInt(valStr);
                    return (value >= min && value <= max);
                } catch (NumberFormatException ignore) {
                    return false;
                }
            }
            return false;
        }
    }
    
    static class MapperEntry implements RowEntryTableModel.RowEntry {
        /**
         * Provides a simple comparator to determine whether two unique instances of
         * MapperEntry have identical values for columnName.
         */
        static class UniqueFieldNameComparator implements Comparator {
            private static UniqueFieldNameComparator instance;
            
            public static UniqueFieldNameComparator getInstance() {
                if (instance == null) {
                    instance = new UniqueFieldNameComparator();
                }
                
                return instance;
            }
            
            private UniqueFieldNameComparator() {
            }
            
            /**
             * @see java.util.Comparator#compare
             */
            public int compare(Object o1, Object o2) {
                // Specifically return != 0 if (o1 == o2); we want to return 0
                // if and only if o1 is a distinct object with a tag ID identical
                // to that of o2.
                if (o1 == o2) {
                    return -1;
                }
                
                if (o1 instanceof MapperEntry && o2 instanceof MapperEntry) {
                    MapperEntry e1 = (MapperEntry) o1;
                    MapperEntry e2 = (MapperEntry) o2;
                    
                    // Allow for multiple instances of empty strings.
                    if (e1.columnName != null && e2.columnName != null && e1.columnName.trim().length() != 0 && e2.columnName.trim().length() != 0) {
                        return (e1.columnName.compareTo(e2.columnName));
                    }
                    // Return some placeholder value other than 0.
                    return e1.hashCode() - e2.hashCode();
                }
                throw new ClassCastException("Arguments must be MapperEntry instances.");
            }
        }
        
        /**
         * Provides a simple comparator to determine whether two unique instances of
         * MapperEntry have identical values for tagId
         */
        static class UniqueTagIDComparator implements Comparator {
            private static UniqueTagIDComparator instance;
            
            public static UniqueTagIDComparator getInstance() {
                if (instance == null) {
                    instance = new UniqueTagIDComparator();
                }
                
                return instance;
            }
            
            private UniqueTagIDComparator() {
            }
            
            /**
             * @see java.util.Comparator#compare
             */
            public int compare(Object o1, Object o2) {
                // Specifically return != 0 if (o1 == o2); we want to return 0
                // if and only if o1 is a distinct object with a tag ID identical
                // to that of o2.
                if (o1 == o2) {
                    return -1;
                }
                
                if (o1 instanceof MapperEntry && o2 instanceof MapperEntry) {
                    MapperEntry e1 = (MapperEntry) o1;
                    MapperEntry e2 = (MapperEntry) o2;
                    
                    // Allow for multiple instances of empty strings.
                    if (e1.tagId != null && e2.tagId != null && e1.tagId.trim().length() != 0 && e2.tagId.trim().length() != 0) {
                        return (e1.tagId.compareTo(e2.tagId));
                    }
                    // Return some placeholder value other than 0.
                    return e1.hashCode() - e2.hashCode();
                }
                throw new ClassCastException("Arguments must be MapperEntry instances.");
            }
        }
        
        private Integer columnLength;
        private String columnName;
        private String tagId;
        private String typeName;
        
        public MapperEntry() {
            this("", "", 0, "");
        }
        
        public MapperEntry(String id, String name, int length, String type) {
            if (id == null) {
                throw new IllegalArgumentException("Must supply non-null id");
            }
            
            if (name == null) {
                throw new IllegalArgumentException("Must supply non-null name");
            }
            
            tagId = id;
            columnName = name;
            columnLength = (length > 0) ? new Integer(length) : INTEGER_ZERO;
            
            // typeName = (isValidFieldType(type)) ? type : FIELD_TYPES[0];
            typeName = type;
        }
        
        public String getId() {
            return tagId;
        }
        
        public int getLength() {
            return columnLength.intValue();
        }
        
        public String getName() {
            return columnName;
        }
        
        public String getType() {
            return typeName;
        }
        
        public Object getValue(int column) {
            switch (column) {
                case 0:
                    return tagId;
                    
                case 1:
                    return columnLength;
                    
                case 2:
                    return columnName;
                    
                case 3:
                    return typeName;
                    
                default:
                    throw new IndexOutOfBoundsException();
            }
        }
        
        public boolean isEditable(int column) {
            if (column < 0 || column > 3) {
                throw new IndexOutOfBoundsException();
            }
            return true;
        }
        
        public boolean isValid() {
            return (columnName != null && columnName.trim().length() != 0) && (columnLength.intValue() > 0 && isValidFieldType(typeName));
        }
        
        public boolean isValidFieldType(String type) {
            boolean result = false;
            return result;
        }
        
        public void setEditable(int column, boolean newState) {
            if (column < 0 || column > 3) {
                throw new IndexOutOfBoundsException();
            }
            // Ignore...columns are always editable
        }
        
        public void setValue(int column, Object newValue) {
            switch (column) {
                case 0:
                    if (newValue instanceof String) {
                        tagId = (String) newValue;
                    }
                    
                case 1:
                    if (newValue instanceof Integer) {
                        if (!(columnLength.equals(newValue))) {
                            columnLength = (Integer) newValue;
                        }
                    } else if (newValue instanceof String) {
                        try {
                            int val = Integer.parseInt((String) newValue);
                            if (val != columnLength.intValue() && val > 0) {
                                columnLength = new Integer(val);
                            }
                        } catch (NumberFormatException e) {
                            // Do nothing
                        }
                    }
                    break;
                    
                case 2:
                    if (newValue instanceof String) {
                        columnName = (String) newValue;
                    }
                    break;
                    
                case 3:
                    if (newValue instanceof String) {
                        typeName = (String) newValue;
                    }
                    break;
                    
                default:
                    throw new IndexOutOfBoundsException();
            }
        }
        
        public String toString() {
            StringBuilder buf = new StringBuilder();
            
            buf.append("MapperEntry: tag ID = \"").append(tagId).append("\"").append(", name = \"").append(columnName).append("\"").append(
                    ", length = ").append(columnLength).append(", type = ").append(typeName);
            
            return buf.toString();
        }
    }
    
    class MapperModel extends RowEntryTableModel {
        public MapperModel(String[] headers, boolean[] editStates) {
            super(headers, editStates);
        }
        
        /**
         * Overrides default implementation to check for duplication of tag ID and field
         * name values in the model
         *
         * @param aValue value to be set
         * @param rowIndex row of model entry to receive value
         * @param columnIndex column of model entry to receive value
         * @see RowEntryTableModel#setValueAt
         */
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    if (aValue instanceof String) {
                        if (isDuplicated(getRowEntry(rowIndex), MapperEntry.UniqueTagIDComparator.getInstance())) {
                            DialogDisplayer.getDefault().notify(
                                    new NotifyDescriptor.Message(NbBundle.getMessage(TableDefinitionPanel.MapperModel.class,
                                    "ERROR_import_file_duplicatevalue", aValue)));
                            return;
                        }
                    }
                    break;
                    
                case 2:
                    if (aValue instanceof String) {
                        if (isDuplicated(getRowEntry(rowIndex), MapperEntry.UniqueFieldNameComparator.getInstance())) {
                            DialogDisplayer.getDefault().notify(
                                    new NotifyDescriptor.Message(NbBundle.getMessage(TableDefinitionPanel.MapperModel.class,
                                    "ERROR_import_file_duplicatevalue", aValue)));
                            return;
                        }
                    }
                    
                case 1:
                case 3:
                default:
                    break;
            }
            
            super.setValueAt(aValue, rowIndex, columnIndex);
        }
    }
    
    static {
        List headerList = new ArrayList(8);
        String nbBundle1 = mLoc.t("BUND230: #");
        try {
            headerList.add(nbBundle1.substring(15));
        } catch (MissingResourceException e) {
            headerList.add("Field #");
        }
        
        String nbBundle2 = mLoc.t("BUND231: Length");
        try {
            headerList.add(nbBundle2.substring(15));
        } catch (MissingResourceException e) {
            headerList.add("Length");
        }
        
        String nbBundle3 = mLoc.t("BUND232: Name");
        try {
            headerList.add(nbBundle3.substring(15));
        } catch (MissingResourceException e) {
            headerList.add("Column Name");
        }
        
        String nbBundle4 = mLoc.t("BUND233: Datatype");
        try {
            headerList.add(nbBundle4.substring(15));
        } catch (MissingResourceException e) {
            headerList.add("Datatype");
        }
        String nbBundle5 = mLoc.t("BUND234: Scale");
        try {
            headerList.add(nbBundle5.substring(15));
        } catch (MissingResourceException e) {
            headerList.add("Scale");
        }
        String nbBundle6 = mLoc.t("BUND235: Null?");
        try {
            headerList.add(nbBundle6.substring(15));
        } catch (MissingResourceException e) {
            headerList.add("Null?");
        }
        String nbBundle7 = mLoc.t("BUND236: PK?");
        try {
            headerList.add(nbBundle7.substring(15));
        } catch (MissingResourceException e) {
            headerList.add("PK?");
        }
        String nbBundle8 = mLoc.t("BUND237: Default");
        try {
            headerList.add(nbBundle8.substring(15));
        } catch (MissingResourceException e) {
            headerList.add("Default");
        }
        
        COLUMN_HEADERS = Collections.unmodifiableList(headerList);
    }
    
    private static final List COLUMN_HEADERS;
    
    private static final Integer INTEGER_ZERO = new Integer(0);
    private static final int MAX_ERRORS_TO_DISPLAY = 20;
    private static final int MAX_LENGTH = 2048;
    
    private static final int MAX_PRECISION = 38;
    private static final int MIN_LENGTH = 0;
    private static final int MIN_PRECISION = 1;
    private static final int MIN_SCALE = 0;
    
    private ColumnMetadataTable colMetaTable;
    private FlatfileDBTable currentTable;
    private JPanel layoutPanel;
    private JLabel parseErrorMessage;
    private String parserType;
    private PreviewDataPanel previewDataPanel;
    private BoundedIntegerVerifier scaleVerifier;
    private RowEntryTableModel tableModel;
    private TableDefinitionVisualPanel panel;
    private Component component;
    private boolean finish = false;
    private int currentIndex = -1;
    
    /** Creates a new default instance of TableDefinitionPanel */
    public TableDefinitionPanel() {
    }
    
    /**
     * Overrides parent implementation to allow for addition of this instance as a
     * listener for various child components.
     */
    public void addNotify() {
        if (colMetaTable != null) {
            ListSelectionModel selModel = colMetaTable.getSelectionModel();
            selModel.removeListSelectionListener(this);
            selModel.addListSelectionListener(this);
            colMetaTable.requestFocusInWindow();
        }
    }
    
    /**
     * @see org.netbeans.modules.mashup.db.ui.wizard.AbstractWizardPanel.Content#canAdvance
     */
    private boolean canAdvance() {
        boolean isValid = true;
        List errorList = getParseDataErrorMessages();
        previewDataPanel.setTable(currentTable);
        
        if (!getParseDataErrorMessages().isEmpty()) {
            Iterator iter = errorList.iterator();
            StringBuilder buf = new StringBuilder(100);
            
            String nbBundle9 = mLoc.t("BUND238: Please correct the following error(s) in the current set of field properties.");
            buf.append(nbBundle9.substring(15));
            while (iter.hasNext()) {
                buf.append(iter.next());
            }
            
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(buf.toString(), NotifyDescriptor.WARNING_MESSAGE));
            isValid = false;
        } else if (((FlatfileColumnTableModel) tableModel).hasZeroLengthColumns()) {
            String nbBundle10 = mLoc.t("BUND239: Please supply non-zero length values for each field.");
            String errMsg = nbBundle10.substring(15);
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errMsg, NotifyDescriptor.WARNING_MESSAGE));
            isValid = false;
        } else if(!previewDataPanel.showData(null)) {
            isValid = false;
        }
        
        return isValid;
    }
    
    /**
     * Gets error messages, if any, associated with current set of parse properties.
     *
     * @return List, possibly empty, of error messages describing problems with current
     *         parse properties.
     */
    public List getParseDataErrorMessages() {
        List errList = new ArrayList();
        
        if (tableModel != null) {
            int recordLength = 0;
            int sumFieldLengths = 0;
            
            try {
                recordLength = Integer.valueOf(currentTable.getProperty(PropertyKeys.WIZARDRECORDLENGTH)).intValue();
            } catch (NumberFormatException ignore) {
                // Do nothing...
            }
            
            ListIterator iter = tableModel.getRowEntries().listIterator();
            List existingColumnNames = new ArrayList(tableModel.getRowEntries().size());
            List duplicateErrorList = new ArrayList();
            
            while (iter.hasNext()) {
                FlatfileColumnTableModel.ColumnEntry entry = (FlatfileColumnTableModel.ColumnEntry) iter.next();
                
                sumFieldLengths += entry.getColumn().getPrecision();
                errList.addAll(entry.validateColumnDefinition());
                
                String columnName = entry.getName().toUpperCase();
                if (!"".equals(columnName.trim())) { // ignore empty or blank names
                    int index = existingColumnNames.indexOf(columnName);
                    if (index != -1) {
                        String nbBundle11 = mLoc.t("BUND240: Column #{1}:  {0} is already used in column #{2}.",columnName,new Integer(
                                iter.nextIndex()), new Integer(index + 1));
                        String errMsg = nbBundle11.substring(15);
                        duplicateErrorList.add(errMsg);
                    } else {
                        existingColumnNames.add(columnName);
                    }
                } else {
                    existingColumnNames.add(""); // Add empty name as placeholder.
                }
                
                // If list size exceeds MAX_ERRORS_TO_DISPLAY, don't append any more
                // errors.
                if (errList.size() >= MAX_ERRORS_TO_DISPLAY) {
                    errList.add("\n...\n");
                    break;
                }
            }
            
            String nbBundle12 = mLoc.t("BUND241: Column lengths do not sum up to the record length given inStep 2.Please adjust your column values, or go back to Step 2 and correct the record length.");
            if (PropertyKeys.FIXEDWIDTH.equalsIgnoreCase(parserType) && sumFieldLengths != recordLength) {
                errList.add(0, nbBundle12.substring(15));
            }
            
            if (errList.size() < MAX_ERRORS_TO_DISPLAY && !duplicateErrorList.isEmpty()) {
                int maximumToShow = Math.min(Math.max(0, (MAX_ERRORS_TO_DISPLAY - errList.size())), duplicateErrorList.size());
                // Show only up to the first twenty duplications.
                if (maximumToShow != 0) {
                    String nbBundle13 = mLoc.t("BUND242: Duplicate column names:");
                    errList.add(nbBundle13.substring(15));
                    
                    errList.addAll(duplicateErrorList.subList(0, maximumToShow));
                    if (maximumToShow >= MAX_ERRORS_TO_DISPLAY) {
                        errList.add("\n...\n");
                    }
                }
            }
        }
        
        return errList;
    }
    
    /**
     * Indicates whether this panel contains valid content.
     *
     * @return true if panel is valid and iterator can advance to next panel; false
     *         otherwise
     */
    public boolean hasValidData() {
        // Determine whether to enable controls in preview pane, depending on whether
        // the layout parameters are valid.
        boolean isValid = getParseDataErrorMessages().isEmpty();
        previewDataPanel.setEnabled(isValid);
        
        if (parseErrorMessage != null) {
            String nbBundle14 = mLoc.t("BUND243: Please check for invalid name, length, scale, default values.");
            parseErrorMessage.setText(isValid ? " " : nbBundle14.substring(15));
            parseErrorMessage.revalidate();
            parseErrorMessage.repaint();
        }
        
        // execute the query and see whether we could get the resultset
        if (isValid) {
            previewDataPanel.setTable(currentTable);
            isValid = previewDataPanel.showData(parseErrorMessage);
        }
        return isValid;
    }
    
    /**
     * @see org.netbeans.modules.mashup.db.ui.wizard.AbstractContentPanel#readSettings
     */
    public void readSettings(Object settings) {
        if (settings instanceof WizardDescriptor) {
            WizardDescriptor wd = (WizardDescriptor) settings;
            int count = Integer.parseInt((String)
            wd.getProperty(MashupTableWizardIterator.TABLE_INDEX));
            currentIndex = count;
            if( ((List<String>)(wd.getProperty(MashupTableWizardIterator.URL_LIST))).size() - 1 == count) {
                finish = true;
            } else {
                finish = false;
            }
            boolean shouldClear = true;
            FlatfileDBTable tempTable = (FlatfileDBTable) wd.getProperty(MashupTableWizardIterator.PROP_CURRENTTABLE);
            if(currentTable != null && tempTable.getName().equals(currentTable.getName())) {
                shouldClear = false;
            }
            currentTable = tempTable;
            parserType = currentTable.getParserType();
            
            if (currentTable != null) {
                Collection columns = currentTable.getColumnList();
                
                if (tableModel == null) {
                    tableModel = createTableModel(columns);
                    createLayoutPanel(tableModel);
                } else {
                    if(shouldClear) {
                        tableModel.clear();
                        ((FlatfileColumnTableModel) tableModel).setRowEntries(columns);
                    }
                    previewDataPanel.clearData();
                    previewDataPanel.setTable(currentTable);
                }
                
            } else {
                throw new IllegalStateException("Context must contain reference to current flat file.");
            }
        }
    }
    
    /**
     * Overrides parent implementation to allow for removal of this instance as a listener
     * for various child components.
     */
    public void removeNotify() {
        if (colMetaTable != null) {
            ListSelectionModel selModel = colMetaTable.getSelectionModel();
            selModel.removeListSelectionListener(this);
        }
        removeNotify();
    }
    
    /**
     * @see org.netbeans.modules.mashup.db.ui.wizard.AbstractContentPanel#storeSettings
     */
    public void storeSettings(Object settings) {
        if (settings instanceof WizardDescriptor) {
            WizardDescriptor wd = (WizardDescriptor) settings;
            
            // Don't commit if user didn't click next.
            if (wd.getValue() != WizardDescriptor.NEXT_OPTION) {
                return;
            }
            
            int index = Integer.parseInt((String)wd.getProperty(
                    MashupTableWizardIterator.TABLE_INDEX));
            List<String> urls = (List<String>) wd.getProperty(MashupTableWizardIterator.URL_LIST);
            
            FlatfileDBTable table = (FlatfileDBTable) wd.getProperty(MashupTableWizardIterator.PROP_CURRENTTABLE);
            if (table == null) {
                throw new IllegalStateException("Context must contain reference to current flat file.");
            }
            if(index == currentIndex) {
                ((FlatfileColumnTableModel) tableModel).updateColumns(currentTable);
                table.setProperty(PropertyKeys.WIZARDFIELDCOUNT, new Integer(table.getColumnList().size()));
                ((FlatfileDBTableImpl)table).setOrPutProperty(PropertyKeys.FILENAME, urls.get(index));
                currentIndex = -1;
            }
        }
    }
    
    /**
     * @see javax.swing.event.ListSelectionListener#valueChanged
     */
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }
    }
    
    private ColumnMetadataTable createColumnMetaTableModel(TableModel model) {
        final ColumnMetadataTable tbl = new ColumnMetadataTable(model);
        tbl.setPreferredScrollableViewportSize(new Dimension(400, 100));
        model.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                fireChangeEvent();
            }
        });
        
        TableColumnModel colModel = tbl.getColumnModel();
        
        // Center field ID label within the colMetaTable cell.
        TableColumn aColumn = colModel.getColumn(FlatfileColumnTableModel.COLUMN_ID);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setFocusable(false);
        renderer.setRequestFocusEnabled(false);
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        aColumn.setCellRenderer(renderer);
        
        // Precision/length renderer/editor.
        aColumn = colModel.getColumn(FlatfileColumnTableModel.COLUMN_PRECLENGTH);
        
        JTextField colPrecLength = new ColumnSizeTextField(5);
        
        aColumn.setCellEditor(new DefaultCellEditor(colPrecLength));
        aColumn.setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable aTable, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel renderer1 = (JLabel) super.getTableCellRendererComponent(aTable, value, isSelected, hasFocus, row, column);
                
                Object typeObj = aTable.getValueAt(row, FlatfileColumnTableModel.COLUMN_JDBCTYPE);
                Object numericType = SQLUtils.getStdSqlType(Types.NUMERIC);
                Object timeType = SQLUtils.getStdSqlType(Types.TIME);
                Object tsType = SQLUtils.getStdSqlType(Types.TIMESTAMP);
                
                String nbBundle15 = mLoc.t("BUND244: Precision of column");
                if (numericType != null && numericType.equals(typeObj)) {
                    renderer1.setToolTipText(nbBundle15.substring(15));
                    renderer1.setHorizontalAlignment(SwingConstants.RIGHT);
                } else if (timeType != null && tsType != null && (timeType.equals(typeObj) || tsType.equals(typeObj))) {
                    String nbBundle16 = mLoc.t("BUND245: Max length of {0} representation",typeObj);
                    renderer1.setToolTipText(nbBundle16.substring(15));
                    renderer1.setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    String nbBundle17 = mLoc.t("BUND246: Char length of column",typeObj);
                    renderer1.setToolTipText(nbBundle15.substring(15));
                    renderer1.setHorizontalAlignment(SwingConstants.RIGHT);
                }
                
                return renderer1;
            }
        });
        
        // Column name renderer/editor.
        final JTextField columnName = new ColumnNameTextField();
        
        aColumn = colModel.getColumn(FlatfileColumnTableModel.COLUMN_NAME);
        aColumn.setCellEditor(new DefaultCellEditor(columnName));
        aColumn.setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable aTable, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel renderer1 = (JLabel) super.getTableCellRendererComponent(aTable, value, isSelected, hasFocus, row, column);
                
                TableModel model1 = aTable.getModel();
                Object obj = model1.getValueAt(row, column);
                renderer1.setToolTipText(obj != null ? obj.toString() : null);
                renderer1.setHorizontalAlignment(SwingConstants.LEADING);
                
                return renderer1;
            }
        });
        
        // Make scale field editable only if type selected is Types.NUMERIC.
        final JTextField colScale = new ColumnSizeTextField(5);
        
        JComboBox sqlTypeBox = tbl.setComboBoxRenderer(FlatfileColumnTableModel.COLUMN_JDBCTYPE, SQLUtils.getStdSqlTypes());
        sqlTypeBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                Object o = e.getItem();
                if (ItemEvent.SELECTED == e.getStateChange() && o instanceof String) {
                    // Force a repaint to redraw scale display for affected row.
                    tbl.revalidate();
                    tbl.repaint();
                }
            }
        });
        
        aColumn = colModel.getColumn(FlatfileColumnTableModel.COLUMN_SCALE);
        aColumn.setCellEditor(new DefaultCellEditor(colScale) {
            public Component getTableCellEditorComponent(JTable aTable, Object value, boolean isSelected, int row, int column) {
                Object typeObj = aTable.getValueAt(row, FlatfileColumnTableModel.COLUMN_JDBCTYPE);
                Object numericType = SQLUtils.getStdSqlType(Types.NUMERIC);
                JComponent editor = new JLabel();
                
                boolean isNumericType = (numericType != null && numericType.equals(typeObj));
                if (isNumericType) {
                    editor = (JComponent) super.getTableCellEditorComponent(aTable, value, isSelected, row, column);
                    editor.setEnabled(true);
                } else {
                    JLabel lbl = (JLabel) editor;
                    String nbBundle18 = mLoc.t("BUND051: N/A");
                    lbl.setText(nbBundle18.substring(15));
                    String nbBundle19 = mLoc.t("BUND248: Not defined for datatype {0}",typeObj);
                    lbl.setToolTipText(nbBundle19.substring(15));
                    lbl.setHorizontalAlignment(SwingConstants.CENTER);
                }
                
                return editor;
            }
        });
        aColumn.setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable aTable, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel renderer1 = (JLabel) super.getTableCellRendererComponent(aTable, value, isSelected, hasFocus, row, column);
                
                Object typeObj = aTable.getValueAt(row, FlatfileColumnTableModel.COLUMN_JDBCTYPE);
                Object numericType = SQLUtils.getStdSqlType(Types.NUMERIC);
                if (numericType != null && numericType.equals(typeObj)) {
                    String nbBundle20 = mLoc.t("BUND249: Numeric scale (must be less than or equal to precision)");
                    renderer1.setToolTipText(nbBundle20.substring(15));
                    renderer1.setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    String nbBundle21 = mLoc.t("BUND051: N/A");
                    renderer1.setText(nbBundle21.substring(15));
                    String nbBundle22 = mLoc.t("BUND248: Not defined for datatype {0}",typeObj);
                    renderer1.setToolTipText(nbBundle22.substring(15));
                    renderer1.setHorizontalAlignment(SwingConstants.CENTER);
                }
                
                return renderer1;
            }
        });
        
        // Add checkbox to set nullability of field
        aColumn = colModel.getColumn(FlatfileColumnTableModel.COLUMN_ISNULLABLE);
        final ColumnMetadataTable.MyBooleanRenderer nullrenderer = new ColumnMetadataTable.MyBooleanRenderer();
        aColumn.setCellEditor(new DefaultCellEditor(nullrenderer) {
            public Component getTableCellEditorComponent(JTable aTable, Object value, boolean isSelected, int row, int column) {
                Object typeObj = aTable.getValueAt(row, FlatfileColumnTableModel.COLUMN_ISPK);
                JComponent editor = new JLabel();
                
                if (Boolean.TRUE.equals(typeObj)) {
                    nullrenderer.setSelected(false);
                    JLabel lbl = (JLabel) editor;
                    String nbBundle23 = mLoc.t("BUND051: N/A");
                    lbl.setText(nbBundle23.substring(15));
                    String nbBundle24 = mLoc.t("BUND248: Not defined for datatype {0}",typeObj);
                    lbl.setToolTipText(nbBundle24.substring(15));
                    lbl.setDisplayedMnemonic(nbBundle24.substring(15).charAt(0));
                    lbl.setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    editor = (JComponent) nullrenderer.getTableCellRendererComponent(aTable, value, isSelected, isSelected, row, column);
                }
                return editor;
            }
        });
        
        // Add checkbox to set nullability of field
        aColumn = colModel.getColumn(FlatfileColumnTableModel.COLUMN_ISPK);
        final ColumnMetadataTable.MyBooleanRenderer pkrenderer = new ColumnMetadataTable.MyBooleanRenderer();
        aColumn.setCellEditor(new DefaultCellEditor(pkrenderer) {
            public Component getTableCellEditorComponent(JTable aTable, Object value, boolean isSelected, int row, int column) {
                return pkrenderer.getTableCellRendererComponent(aTable, value, isSelected, isSelected, row, column);
            }
        });
        pkrenderer.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (ItemEvent.SELECTED == e.getStateChange()) {
                    // Force a repaint to redraw scale display for affected row.
                    tbl.revalidate();
                    tbl.repaint();
                }
            }
        });
        
        // Defalt value renderer/editor.
        final JTextField defaultValue = new JTextField();
        
        aColumn = colModel.getColumn(FlatfileColumnTableModel.COLUMN_DAFAULT);
        aColumn.setCellEditor(new DefaultCellEditor(defaultValue));
        aColumn.setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable aTable, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel renderer1 = (JLabel) super.getTableCellRendererComponent(aTable, value, isSelected, hasFocus, row, column);
                
                TableModel model1 = aTable.getModel();
                Object obj = model1.getValueAt(row, column);
                renderer1.setToolTipText(obj != null ? obj.toString() : null);
                renderer1.setHorizontalAlignment(SwingConstants.LEADING);
                
                return renderer1;
            }
        });
        
        return tbl;
    }
    
    /*
     * Creates layout panel using the given TableModel and display configuration info in
     * the given VisualParamGroup.
     */
    private void createLayoutPanel(TableModel model) {
        layoutPanel.removeAll();
        layoutPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        layoutPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        
        layoutPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        layoutPanel.setLayout(new GridLayout(2, 1));
        
        // Set up listeners for interaction between colMetaTable selections and buttons.
        JPanel tablePanel = new JPanel();
        String nbBundle25 = mLoc.t("BUND250: Column Definition");
        String tableLabel = nbBundle25.substring(15);
        tablePanel.setBorder(BorderFactory.createTitledBorder(tableLabel));
        
        tablePanel.setLayout(new BorderLayout());
        
        colMetaTable = createColumnMetaTableModel(model);
        JScrollPane tableViewer = new JScrollPane(colMetaTable);
        
        parseErrorMessage = new JLabel(" ");
        parseErrorMessage.setHorizontalAlignment(SwingConstants.LEADING);
        parseErrorMessage.setForeground(Color.RED);
        parseErrorMessage.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        tablePanel.add(parseErrorMessage, BorderLayout.NORTH);
        tablePanel.add(tableViewer, BorderLayout.CENTER);
        
        // Add field properties colMetaTable to panel.
        layoutPanel.add(tablePanel);
        
        previewDataPanel = new PreviewDataPanel(currentTable);
        previewDataPanel.setTableModel((FlatfileColumnTableModel) model);
        // Add preview output to panel.
        layoutPanel.add(previewDataPanel, BorderLayout.SOUTH);
        layoutPanel.invalidate();
        layoutPanel.revalidate();
        
        fireChangeEvent();
    }
    
    private RowEntryTableModel createTableModel(Collection fields) {
        RowEntryTableModel newModel = new FlatfileColumnTableModel(fields, COLUMN_HEADERS);
        
        newModel.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                TableModel model = (TableModel) e.getSource();
                int updateType = e.getType();
                int updatedColumn = e.getColumn();
                
                if (TableModelEvent.UPDATE == updateType) {
                    int firstRow = Math.max(0, e.getFirstRow());
                    int lastRow = Math.min(e.getLastRow(), model.getRowCount() - 1);
                    for (int rowIndex = firstRow; rowIndex <= lastRow; rowIndex++) {
                        boolean isNumericType = isJdbcTypeNumeric(model, rowIndex);
                        
                        switch (updatedColumn) {
                            case FlatfileColumnTableModel.COLUMN_JDBCTYPE:
                                int maxPrecLength = 0;
                                int minPrecLength = 0;
                                
                                if (isNumericType) {
                                    maxPrecLength = MAX_PRECISION;
                                    minPrecLength = MIN_PRECISION;
                                } else {
                                    maxPrecLength = MAX_LENGTH;
                                    minPrecLength = MIN_LENGTH;
                                }
                                
                                {
                                    int maxScale = enforceBounds(model, rowIndex, FlatfileColumnTableModel.COLUMN_PRECLENGTH, maxPrecLength,
                                            minPrecLength);
                                    enforceBounds(model, rowIndex, FlatfileColumnTableModel.COLUMN_SCALE, maxScale, MIN_SCALE);
                                    if (scaleVerifier != null) {
                                        scaleVerifier.setMaximum(maxScale);
                                    }
                                }
                                break;
                                
                            case FlatfileColumnTableModel.COLUMN_PRECLENGTH:
                                if (isJdbcTypeNumeric(model, rowIndex)) {
                                    int maxScale = getInt(model, rowIndex, FlatfileColumnTableModel.COLUMN_PRECLENGTH);
                                    enforceBounds(model, rowIndex, FlatfileColumnTableModel.COLUMN_SCALE, maxScale, MIN_SCALE);
                                    if (scaleVerifier != null) {
                                        scaleVerifier.setMaximum(maxScale);
                                    }
                                }
                                break;
                                
                            default:
                                break;
                        }
                    }
                }
            }
            
            private int enforceBounds(TableModel model, int row, int col, int max, int min) {
                int modelValue = getInt(model, row, col);
                
                if (modelValue < min) {
                    model.setValueAt(new Integer(min), row, col);
                    modelValue = min;
                } else if (modelValue > max) {
                    model.setValueAt(new Integer(max), row, col);
                    modelValue = max;
                }
                
                return modelValue;
            }
            
            private int getInt(TableModel model, int row, int col) {
                int modelValue = 0;
                
                Object val = model.getValueAt(row, col);
                if (val instanceof Number) {
                    modelValue = ((Number) val).intValue();
                } else {
                    try {
                        modelValue = Integer.parseInt((String) val);
                    } catch (NumberFormatException ignore) {
                        modelValue = 0;
                    }
                }
                
                return modelValue;
            }
            
            private boolean isJdbcTypeNumeric(TableModel model, int row) {
                Object numericType = SQLUtils.getStdSqlType(Types.NUMERIC);
                Object typeObj = model.getValueAt(row, FlatfileColumnTableModel.COLUMN_JDBCTYPE);
                return (typeObj != null && typeObj.equals(numericType));
            }
        });
        
        return newModel;
    }
    
    private void init() {
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setPreferredSize(new Dimension(205, 130));
        
        String nbBundle26 = mLoc.t("BUND251: Define table and column definition for this file.");
        JLabel instr = new JLabel(nbBundle26.substring(15));
        instr.setDisplayedMnemonic(nbBundle26.substring(15).charAt(0));
        instr.setAlignmentX(Component.LEFT_ALIGNMENT);
        layoutPanel = new JPanel(); // Column layout subpanel
        
        panel.add(instr);
        panel.add(layoutPanel);
    }
    
    public Component getComponent() {
        if(component == null) {
            panel = new TableDefinitionVisualPanel(this);
            init();
            component = (Component) panel;
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }
    
    public boolean isFinishPanel() {
        return finish && canAdvance();
    }
    
    public boolean isValid() {
        return canAdvance();
    }
}