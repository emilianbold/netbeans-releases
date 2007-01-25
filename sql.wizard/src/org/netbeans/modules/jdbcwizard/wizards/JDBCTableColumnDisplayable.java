package org.netbeans.modules.jdbcwizard.wizards;

import java.awt.Font;
import java.util.List;
import javax.swing.JTable;

public interface JDBCTableColumnDisplayable {

    /* font selection for column data in table body */
    static final Font FONT_TABLE_COLUMNS = new Font("Dialog", Font.PLAIN, 10);

    /* font selection for column headers in table body */
    static final Font FONT_TABLE_HEADER = new Font("Dialog", Font.BOLD, 10);

    public JTable getColumnTable();

    public List getColumnTables();

    public List getSelectedColumnTables();

    public void addColumnTable(List column_table);

    public void resetColumnTable(List column_table);
}
