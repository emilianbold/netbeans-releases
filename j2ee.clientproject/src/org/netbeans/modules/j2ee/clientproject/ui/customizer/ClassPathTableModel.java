package org.netbeans.modules.j2ee.clientproject.ui.customizer;

import java.util.Iterator;
import javax.swing.DefaultListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.j2ee.clientproject.classpath.ClassPathSupportCallbackImpl;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport;
import org.openide.util.NbBundle;

public final class ClassPathTableModel extends AbstractTableModel implements ListDataListener {

    private static final long serialVersionUID = 1L;
    private DefaultListModel model;

    public static ClassPathTableModel createTableModel( Iterator it ) {
        return new ClassPathTableModel( ClassPathUiSupport.createListModel( it ) );
    }
    
    public ClassPathTableModel(DefaultListModel model) {
        super();
        this.model = model;
        model.addListDataListener(this);
    }

    public DefaultListModel getDefaultListModel() {
        return model;
    }

    public int getColumnCount() {
        return 2;
    }

    public int getRowCount() {
        return model.getSize();
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return NbBundle.getMessage(ClassPathTableModel.class, "LBL_CustomizeLibraries_TableHeader_Library");
        } else {
            return NbBundle.getMessage(ClassPathTableModel.class, "LBL_CustomizeLibraries_TableHeader_Deploy");
        }
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return ClassPathSupport.Item.class;
        } else {
            return Boolean.class;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex != 0 && getShowItemAsIncludedInDeployment(getItem(rowIndex)) instanceof Boolean;
    }

    public Object getValueAt(int row, int column) {
        ClassPathSupport.Item item = getItem(row);
        if (column == 0) {
            return item;
        } else {
            return getShowItemAsIncludedInDeployment(item);
        }
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        if (column != 1 || !(value instanceof Boolean)) {
            return;
        }

        getItem(row).setAdditionalProperty(ClassPathSupportCallbackImpl.INCLUDE_IN_DEPLOYMENT, Boolean.toString(value == Boolean.TRUE));
        fireTableCellUpdated(row, column);
    }

    public void contentsChanged(ListDataEvent e) {
        fireTableRowsUpdated(e.getIndex0(), e.getIndex1());
    }

    public void intervalAdded(ListDataEvent e) {
        fireTableRowsInserted(e.getIndex0(), e.getIndex1());
    }

    public void intervalRemoved(ListDataEvent e) {
        fireTableRowsDeleted(e.getIndex0(), e.getIndex1());
    }

    private ClassPathSupport.Item getItem(int index) {
        return (ClassPathSupport.Item) model.get(index);
    }

    private void setItem(ClassPathSupport.Item item, int index) {
        model.set(index, item);
    }

    private Boolean getShowItemAsIncludedInDeployment(ClassPathSupport.Item item) {
        return Boolean.valueOf("true".equals(item.getAdditionalProperty(ClassPathSupportCallbackImpl.INCLUDE_IN_DEPLOYMENT)));
    }
}
