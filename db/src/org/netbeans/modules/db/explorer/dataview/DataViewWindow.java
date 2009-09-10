/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.explorer.dataview;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ObjectStreamException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.MutableComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.node.ColumnNode;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.datatransfer.ExClipboard;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.MultiTransferObject;
import org.openide.windows.TopComponent;

public class DataViewWindow extends TopComponent {

    // TODO: remove this class, replace by the SQL editor

    private JTextArea queryarea;
    private JTable jtable;
    private DataModel dbadaptor;
    private JComboBox rcmdscombo;
    private JLabel status;
    private JPopupMenu tablePopupMenu;

    private DatabaseConnection connection;

    static final long serialVersionUID = 6855188441469780252L;

    public DataViewWindow(DatabaseConnection connection, String query) throws SQLException {
        //this.info = info;
        this.connection = connection;

        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (DataViewWindow.class, "ACS_DataViewWindowA11yDesc")); //NOI18N

        String title = connection.getDisplayName();
        int idx = title.indexOf(" ["); //NOI18N
        title = title.substring(0, idx);
        setName(title);
        setToolTipText(NbBundle.getMessage (DataViewWindow.class, "CommandEditorTitle") + " " + // NOI18N
                title);

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints con = new GridBagConstraints ();
        setLayout (layout);

        // Data model
        dbadaptor = new DataModel(connection);

        // Query area and button
        JPanel subpane = new JPanel();
        GridBagLayout sublayout = new GridBagLayout();
        GridBagConstraints subcon = new GridBagConstraints ();
        subpane.setLayout(sublayout);

        // query label
        subcon.fill = GridBagConstraints.HORIZONTAL;
        subcon.weightx = 0.0;
        subcon.weighty = 0.0;
        subcon.gridx = 0;
        subcon.gridy = 0;
        subcon.gridwidth = 3;
        subcon.insets = new Insets (0, 0, 5, 0);
        subcon.anchor = GridBagConstraints.SOUTH;
        JLabel queryLabel = new JLabel();
        Mnemonics.setLocalizedText(queryLabel, NbBundle.getMessage (DataViewWindow.class, "QueryLabel"));
        queryLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (DataViewWindow.class, "ACS_DataViewQueryLabelA11yDesc"));
        sublayout.setConstraints(queryLabel, subcon);
        subpane.add(queryLabel);

        // query area
        subcon.fill = GridBagConstraints.BOTH;
        subcon.weightx = 1.0;
        subcon.weighty = 1.0;
        subcon.gridx = 0;
        subcon.gridwidth = 3;
        subcon.gridy = 1;
        queryarea = new JTextArea(query, 3, 70);
        queryarea.setLineWrap(true);
        queryarea.setWrapStyleWord(true);
        queryarea.setDropTarget(new DropTarget(queryarea, new ViewDropTarget()));
        queryarea.getAccessibleContext().setAccessibleName(NbBundle.getMessage (DataViewWindow.class, "ACS_DataViewTextAreaA11yName")); //NOI18N
        queryarea.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (DataViewWindow.class, "ACS_DataViewTextAreaA11yDesc")); //NOI18N
        queryarea.setToolTipText(NbBundle.getMessage (DataViewWindow.class, "ACS_DataViewTextAreaA11yDesc")); //NOI18N
        queryLabel.setLabelFor(queryarea);

        JScrollPane scrollpane = new JScrollPane(queryarea);
        subcon.insets = new Insets (0, 0, 5, 0);
        sublayout.setConstraints(scrollpane, subcon);
        subpane.add(scrollpane);

        // combo label
        subcon.fill = GridBagConstraints.HORIZONTAL;
        subcon.weightx = 0.0;
        subcon.weighty = 0.0;
        subcon.gridx = 0;
        subcon.gridy = 2;
        subcon.gridwidth = 1;
        subcon.insets = new Insets (0, 0, 5, 5);
        subcon.anchor = GridBagConstraints.CENTER;
        JLabel comboLabel = new JLabel();
        Mnemonics.setLocalizedText(comboLabel, NbBundle.getMessage (DataViewWindow.class, "HistoryLabel")); //NOI18N
        comboLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (DataViewWindow.class, "ACS_DataViewHistoryLabelA11yDesc")); //NOI18N
        sublayout.setConstraints(comboLabel, subcon);
        subpane.add(comboLabel);

        // Combo recent commands
        subcon.fill = GridBagConstraints.HORIZONTAL;
        subcon.weightx = 1.0;
        subcon.weighty = 0.0;
        subcon.gridx = 1;
        subcon.gridy = 2;
        subcon.gridwidth = 1;
        subcon.insets = new Insets (0, 0, 5, 5);
        subcon.anchor = GridBagConstraints.SOUTH;
        rcmdscombo = new JComboBox(new ComboModel());
        rcmdscombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage (DataViewWindow.class, "ACS_DataViewComboBoxA11yName")); //NOI18N
        rcmdscombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (DataViewWindow.class, "ACS_DataViewComboBoxA11yDesc")); //NOI18N
        rcmdscombo.setToolTipText(NbBundle.getMessage (DataViewWindow.class, "ACS_DataViewComboBoxA11yDesc")); //NOI18N
        comboLabel.setLabelFor(rcmdscombo);
        sublayout.setConstraints(rcmdscombo, subcon);
        subpane.add(rcmdscombo);
        rcmdscombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox source = (JComboBox)e.getSource();
                RecentCommand cmd = (RecentCommand)source.getSelectedItem();
                if (cmd != null)
                    setCommand(cmd.getCommand());
            }
        });

        // Button Execute
        subcon.gridx = 2;
        subcon.gridy = 2;
        subcon.weightx = 0.0;
        subcon.weighty = 0.0;
        subcon.insets = new Insets (0, 0, 5, 0);
        subcon.fill = GridBagConstraints.HORIZONTAL;
        subcon.anchor = GridBagConstraints.SOUTH;
        final JButton fetchbtn = new JButton();
        Mnemonics.setLocalizedText(fetchbtn, NbBundle.getMessage (DataViewWindow.class, "ExecuteButton"));
        fetchbtn.setToolTipText(NbBundle.getMessage (DataViewWindow.class, "ACS_ExecuteButtonA11yDesc")); //NOI18N
        sublayout.setConstraints(fetchbtn, subcon);
        subpane.add(fetchbtn);
        fetchbtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fetchbtn.setEnabled(false);
                Task t = RequestProcessor.getDefault().create(new Runnable() {
                    public void run () {
                        executeCommand();
                    }
                });
                t.addTaskListener(new TaskListener() {
                    public void taskFinished(Task task) {
                        fetchbtn.setEnabled(true);
                    }
                });
                RequestProcessor.getDefault().post(t, 0);
            }
        });
        // status line
        subcon.fill = GridBagConstraints.HORIZONTAL;
        subcon.weightx = 1.0;
        subcon.weighty = 0.0;
        subcon.gridx = 0;
        subcon.gridy = 3;
        subcon.gridwidth = 3;
        subcon.insets = new Insets (0, 0, 5, 0);
        subcon.anchor = GridBagConstraints.SOUTH;
        status = new JLabel(" "); //NOI18N
        status.setBorder(new javax.swing.border.LineBorder(java.awt.Color.gray));
        status.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (DataViewWindow.class, "ACS_DataViewStatusLabelA11yDesc")); //NOI18N
        sublayout.setConstraints(status, subcon);
        subpane.add(status);

        JPanel subpane2 = new JPanel();
        GridBagLayout sublayout2 = new GridBagLayout();
        GridBagConstraints subcon2 = new GridBagConstraints ();
        subpane2.setLayout(sublayout2);

        // table label
        subcon2.fill = GridBagConstraints.HORIZONTAL;
        subcon2.weightx = 0.0;
        subcon2.weighty = 0.0;
        subcon2.gridx = 0;
        subcon2.gridy = 0;
        subcon2.gridwidth = 1;
        subcon2.insets = new Insets (5, 0, 0, 0);
        subcon2.anchor = GridBagConstraints.SOUTH;
        JLabel tableLabel = new JLabel();
        Mnemonics.setLocalizedText(tableLabel, NbBundle.getMessage (DataViewWindow.class, "ResultsLabel"));
        tableLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (DataViewWindow.class, "ACS_DataViewResultsLabelA11yDesc")); //NOI18N
        sublayout2.setConstraints(tableLabel, subcon2);
        subpane2.add(tableLabel);

        // content popup menu on table with results
        tablePopupMenu = new JPopupMenu ();
        JMenuItem miCopyValue = new JMenuItem (NbBundle.getMessage (DataViewWindow.class, "CopyCellValue")); //NOI18N
        miCopyValue.addActionListener(new ActionListener () {
            public void actionPerformed (ActionEvent e) {
                try {
                    Object o = jtable.getValueAt(jtable.getSelectedRow(), jtable.getSelectedColumn());
                    String output = (o != null) ? o.toString () : ""; //NOI18N
                    ExClipboard clipboard = Lookup.getDefault ().lookup (ExClipboard.class);
                    StringSelection strSel = new StringSelection (output);
                    clipboard.setContents (strSel, strSel);
                } catch (ArrayIndexOutOfBoundsException exc) {
                }
            }
        });
        tablePopupMenu.add (miCopyValue);

        JMenuItem miCopyRowValues = new JMenuItem (NbBundle.getMessage (DataViewWindow.class, "CopyRowValues")); //NOI18N
        miCopyRowValues.addActionListener(new ActionListener () {
            public void actionPerformed (ActionEvent e) {
                try {
                    int[] rows = jtable.getSelectedRows ();
                    int[] columns;
                    if (jtable.getRowSelectionAllowed ()) {
                        columns = new int[jtable.getColumnCount ()];
                        for (int a = 0; a < columns.length; a ++)
                            columns[a] = a;
                    } else {
                        columns = jtable.getSelectedColumns ();
                    }
                    if (rows != null  &&  columns != null) {
                        StringBuffer output = new StringBuffer ();
                        for (int row = 0; row < rows.length; row ++) {
                            for (int column = 0; column < columns.length; column ++) {
                                if (column > 0)
                                    output.append ('\t'); //NOI18N
                                Object o = jtable.getValueAt(rows[row], columns[column]);
                                output.append (o != null ? o.toString () : ""); //NOI18N
                            }
                            output.append ('\n'); //NOI18N
                        }
                        ExClipboard clipboard = Lookup.getDefault ().lookup (ExClipboard.class);
                        StringSelection strSel = new StringSelection (output.toString ());
                        clipboard.setContents (strSel, strSel);
                    }
                } catch (ArrayIndexOutOfBoundsException exc) {
                }
            }
        });
        tablePopupMenu.add (miCopyRowValues);

        // Table with results
        //      TableSorter sorter = new TableSorter();
        jtable = new JTable(dbadaptor/*sorter*/);
        jtable.getAccessibleContext().setAccessibleName(NbBundle.getMessage (DataViewWindow.class, "ACS_DataViewTableA11yName")); //NOI18N
        jtable.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (DataViewWindow.class, "ACS_DataViewTableA11yDesc")); //NOI18N
        jtable.setToolTipText(NbBundle.getMessage (DataViewWindow.class, "ACS_DataViewTableA11yDesc")); //NOI18N
        jtable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        //    	sorter.addMouseListenerToHeaderInTable(table);
        jtable.addMouseListener (new MouseAdapter () {
            @Override
            public void mouseReleased (MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    int row = jtable.rowAtPoint (e.getPoint ());
                    int column = jtable.columnAtPoint (e.getPoint ());
                    boolean inSelection = false;
                    int[] rows = jtable.getSelectedRows ();
                    for (int a = 0; a < rows.length; a ++)
                        if (rows[a] == row) {
                            inSelection = true;
                            break;
                        }
                    if (!jtable.getRowSelectionAllowed ()) {
                        inSelection = false;
                        int[] columns = jtable.getSelectedColumns ();
                        for (int a = 0; a < columns.length; a ++)
                            if (columns[a] == column) {
                                inSelection = true;
                                break;
                            }
                    }
                    if (!inSelection)
                        jtable.changeSelection (row, column, false, false);
                    tablePopupMenu.show(jtable, e.getX (), e.getY ());
                }
            }
        });
        tableLabel.setLabelFor(jtable);

        scrollpane = new JScrollPane(jtable);
        subcon2.fill = GridBagConstraints.BOTH;
        subcon2.weightx = 1.0;
        subcon2.weighty = 1.0;
        subcon2.gridx = 0;
        subcon2.gridy = 1;
        subcon2.gridwidth = 1;
        sublayout2.setConstraints(scrollpane, subcon2);
        subpane2.add(scrollpane);

        // Add it into splitview
        con.weightx = 1.0;
        con.weighty = 1.0;
        con.fill = GridBagConstraints.BOTH;
        con.gridx = 0;
        con.gridwidth = 1;
        con.gridy = 1;
        con.insets = new Insets (12, 12, 11, 11);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, subpane, subpane2);
        layout.setConstraints(split, con);
        add(split);
    }

    /**Overriden to provide preferred value
     * for unique TopComponent Id returned by getID. Returned value is used as starting
     * value for creating unique TopComponent ID.
     * Value should be preferably unique, but need not be.
     * @since 4.13
     */
    @Override
    protected String preferredID() {
        return getName();
    }

    /** Overriden to explicitely set persistence type of DataViewWindow
     * to PERSISTENCE_NEVER */
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    /** Returns query used by panel.
    */
    public String getCommand() {
        return queryarea.getText();
    }

    /** Sets query used by panel.
    */
    public void setCommand(String command) {
        queryarea.setText(command);
    }

    public boolean executeCommand() {
        String command = queryarea.getText().trim();
        boolean ret;

        try {
            dbadaptor.execute(command);

            RecentCommand rcmd = new RecentCommand(command);
            ((ComboModel)rcmdscombo.getModel()).addElement(rcmd);
            ret = true;
        } catch (Exception exc) {
            ret = false;
            status.setText(NbBundle.getMessage (DataViewWindow.class, "CommandFailed")); //NOI18N
            org.openide.DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(NbBundle.getMessage (DataViewWindow.class, "DataViewFetchErrorPrefix") + exc.getMessage(), NotifyDescriptor.ERROR_MESSAGE)); //NOI18N
        }

        return ret;
    }

    class ColDef {
        private String name;
        private boolean writable;
        private boolean bric;
        int datatype;

        public ColDef(String name, boolean flag) {
            this.name = name;
            writable = flag;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getDataType() {
            return datatype;
        }

        public void setDataType(int type) {
            datatype = type;
        }

        public boolean isWritable() {
            return writable;
        }

        public void setWritable(boolean flag) {
            writable = flag;
        }

        public boolean isBestRowIdentifierColumn() {
            return bric;
        }

        public void setBestRowIdentifierColumn(boolean flag) {
            bric = flag;
        }
    }

    static int tstrg = 0;
    static int gtcmd = 0;

    class RecentCommand {
        private String command;

        /** The command with no new lines */
        private String shortCommand;

        public RecentCommand(String cmd) {
            command = cmd;
            shortCommand = getShortCommand();
        }

        @Override
        public String toString() {
            return shortCommand;
        }

        public String getCommand() {
            return command;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof RecentCommand)
                return ((RecentCommand)obj).getShortCommand().equals(shortCommand);

            return super.equals(obj);
        }

        @Override
        public int hashCode () {
            int hash = 7;
            hash = 17 * hash + (this.shortCommand != null ? this.shortCommand.hashCode () : 0);
            return hash;
        }

        /**
         * Gets the command String for display in the JComboBox without
         * new lines.
         *
         * @return the command for display in the JComboBox
         */
         private String getShortCommand()  {
            StringTokenizer tokenizer = new StringTokenizer(command);
            StringBuffer buffer = new StringBuffer();
            while (tokenizer.hasMoreElements()) {
                buffer.append(tokenizer.nextElement());
                buffer.append(" ");
            }
            return buffer.toString();
        }
    }

    class ComboModel extends AbstractListModel implements MutableComboBoxModel{
        Vector<Object> commands;
        Object selected;

        static final long serialVersionUID =-5831993904798984334L;
        public ComboModel() {
            this(new Vector<Object> (1));
        }

        public ComboModel(Vector<Object> elems) {
            commands = elems;
        }

        public Object getSelectedItem() {
            return selected;
        }

        public void setSelectedItem(Object anItem) {
            selected = anItem;
            fireContentsChanged(this,-1,-1);
        }

        public void addElement(Object obj) {
            if (!commands.contains(obj)) {
                commands.add(obj);
                fireContentsChanged(this,-1,-1);
            }
        }

        public void removeElement(Object obj) {
            commands.removeElement(obj);
            fireContentsChanged(this,-1,-1);
        }

        public void insertElementAt(Object obj, int index) {
            if (!commands.contains(obj)) {
                commands.insertElementAt(obj, index);
                fireContentsChanged(this,-1,-1);
            }
        }

        public void removeElementAt(int index) {
            commands.removeElementAt(index);
            fireContentsChanged(this,-1,-1);
        }

        public int getSize() {
            return commands.size();
        }

        public Object getElementAt(int index) {
            return commands.get(index);
        }
    }

    class ViewDropTarget implements DropTargetListener {
        /** User is starting to drag over us */
        public void dragEnter (DropTargetDragEvent dtde) {
            dtde.acceptDrag(dtde.getDropAction());
        }

        /** User drags over us */
        public void dragOver (DropTargetDragEvent dtde) {
        }

        public void dropActionChanged (DropTargetDragEvent dtde) {
        }

        /** User exits the dragging */
        public void dragExit (DropTargetEvent dte) {
        }

        private ColumnNode getNode(Transferable t) {
            Node n = NodeTransfer.node(t, NodeTransfer.MOVE);
            if (n != null)
                return (ColumnNode)n;

            n = NodeTransfer.node(t, NodeTransfer.COPY);
            if (n != null)
                return (ColumnNode)n;

            return null;
        }

        /** Performs the drop action */
        public void drop (DropTargetDropEvent dtde) {
            String query = null;
            Transferable t = dtde.getTransferable();
            StringBuffer buff = new StringBuffer();

            try {
                DataFlavor multiFlavor = new DataFlavor (
                    "application/x-java-openide-multinode;class=org.openide.util.datatransfer.MultiTransferObject", // NOI18N
                        NbBundle.getMessage (DataViewWindow.class, "transferFlavorsMultiFlavorName"),
                        MultiTransferObject.class.getClassLoader());

                if (t.isDataFlavorSupported(multiFlavor)) {
                    MultiTransferObject mobj = (MultiTransferObject)t.getTransferData(ExTransferable.multiFlavor);
                    int count = mobj.getCount();
                    int tabidx = 0;
                    Map<String, Integer> tabidxmap = new HashMap<String, Integer> ();
                    for (int i = 0; i < count; i++) {
                        ColumnNode nfo = getNode(mobj.getTransferableAt(i));
                        if (nfo != null) {
                            String tablename = nfo.getParentName();
                            Integer tableidx = tabidxmap.get (tablename);
                            if (tableidx == null) tabidxmap.put(tablename, tableidx = new Integer(tabidx++));
                            if (buff.length()>0) buff.append(", "); //NOI18N
                            buff.append("t"+tableidx+"."+nfo.getName()); //NOI18N
                        }
                    }

                    StringBuffer frombuff = new StringBuffer();
                    Iterator iter = tabidxmap.keySet().iterator();
                    while (iter.hasNext()) {
                        String tab = (String)iter.next();
                        if (frombuff.length()>0) frombuff.append(", "); //NOI18N
                        frombuff.append(tab + " t"+tabidxmap.get(tab)); //NOI18N
                    }

                    query = "select "+buff.toString()+" from "+frombuff.toString(); //NOI18N

                } else {
                    ColumnNode nfo = getNode(t);
                    if (nfo != null) query = "select "+nfo.getName()+" from "+nfo.getParentName(); //NOI18N
                }

                if (query != null)
                    setCommand(query);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class DataModel extends AbstractTableModel {
        DatabaseConnection connection;
        Vector coldef = new Vector();
        Vector data = new Vector();
        boolean editable = false;

        static final long serialVersionUID =7729426847826999963L;

        /** Constructor */
        public DataModel(DatabaseConnection conn) throws SQLException {
            connection = conn;
        }

        /** Executes command
        * @param command SQL Expression
        */
        synchronized public void execute(String command) throws Exception {
            if (command.length() == 0) {
                status.setText(" "); //NOI18N
                return;
            }

            status.setText(NbBundle.getMessage (DataViewWindow.class, "CommandRunning")); //NOI18N

            Connection con;
            Statement stat;
            try {
                con = connection.getConnection();
                stat = con.createStatement();
            } catch ( Exception exc ) {
                String message = NbBundle.getMessage (DataViewWindow.class, "EXC_ConnectionError", exc.getMessage()); // NOI18N
                throw new DatabaseException(message);
            }

            ResultSet rs;

            if (command.toLowerCase().startsWith("select")) { //NOI18N
                rs = stat.executeQuery(command);

                ResultSetMetaData mdata = rs.getMetaData();

                int cols = mdata.getColumnCount();
                // Bug : 5083676
                // Data is getting cleared and modified in a independent thread , while the swing
                // thread tries to render the table. Hence this sometimes results in a
                // ArrayIndexOutOfBoundsException
                // Creating two 'work' vectors here and populating required changes in these
                // Then replacing the model vectors with these.
                Vector<ColDef> coldefWork = new Vector<ColDef> ();
                Vector<Vector<Object>> dataWork = new Vector<Vector<Object>> ();
                for(int column = 1; column <= cols; column++) {
                    boolean writable;
                    try {
                        writable = mdata.isWritable(column);
                    } catch (SQLException exc) {
                        //patch for FireBirdSQL (isWritable has not been implemented yet)
                        writable = false;
                    }
                    ColDef cd = new ColDef(mdata.getColumnLabel(column), writable);
                    cd.setDataType(mdata.getColumnType(column));
                    coldefWork.add(cd);
                }

                // Get all rows.
                // In future implementations should be more careful
                int rcounter = 0;
//                int limit = RootNode.getOption().getFetchLimit();
//                int step = RootNode.getOption().getFetchStep();
                int limit = 100;
                int step = 200;

                String cancel = NbBundle.getMessage (DataViewWindow.class, "DataViewCancelButton"); //NOI18N
                String nextset = NbBundle.getMessage (DataViewWindow.class, "DataViewNextFetchButton"); //NOI18N
                String allset = NbBundle.getMessage (DataViewWindow.class, "DataViewAllFetchButton"); //NOI18N

                JButton fetchNext = new JButton();
                Mnemonics.setLocalizedText(fetchNext, nextset);
                fetchNext.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (DataViewWindow.class, "ACS_DataViewNextFetchButtonA11yDesc")); //NOI18N

                JButton fetchAll = new JButton();
                Mnemonics.setLocalizedText(fetchAll, allset);
                fetchAll.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (DataViewWindow.class, "ACS_DataViewAllFetchButtonA11yDesc")); //NOI18N

                JButton no = new JButton();
                Mnemonics.setLocalizedText(no, cancel);
                no.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (DataViewWindow.class, "ACS_DataViewCancelButtonA11yDesc")); //NOI18N

                String message;
                NotifyDescriptor ndesc;
                while (rs.next()) {
                    Vector<Object> row = new Vector<Object> (cols);
                    for (int column = 1; column <= cols; column++)
                        row.add(rs.getObject(column));
                    dataWork.addElement(row);

                    // Catch row count
                    if (++rcounter >= limit) {

                        message = NbBundle.getMessage (DataViewWindow.class, "DataViewMessage", //NOI18N
                                rcounter,
                                step);
                        ndesc = new NotifyDescriptor(message, NbBundle.getMessage (DataViewWindow.class, "FetchDataTitle"), NotifyDescriptor.YES_NO_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE, new Object[] {fetchNext, fetchAll, no}, NotifyDescriptor.CANCEL_OPTION); //NOI18N

                        Object ret = DialogDisplayer.getDefault().notify(ndesc);
                        if (fetchAll.equals(ret)) {
                            limit = Integer.MAX_VALUE;
                        } else {
                            if (fetchNext.equals(ret)) {
                                limit = limit + step;
                            } else {
                                // window closed by close button or Esc key
                                // or the cancel button was pressed
                                break;
                            }
                        }
                    }
                }

                // Replace model in the swing event thread
                // Alternative is to lock on the instance and assign it.
                final Vector assignData = dataWork;
                final Vector assignColdef = coldefWork;
                SwingUtilities.invokeAndWait(new Runnable(){
                    public void run(){
                        data = assignData;
                        coldef = assignColdef;
                        fireTableChanged(null);
                    }
                });
                /*
                synchronized(coldef){
                    coldef = assignColdef;
                }
                synchronized(data){
                    data = assignData;
                }
                 */
                rs.close();
                //fireTableChanged(null);
            } else {
                if (command.toLowerCase().startsWith("delete") ||
                        command.toLowerCase().startsWith("insert") ||
                        command.toLowerCase().startsWith("update")) //NOI18N
                    stat.executeUpdate(command);
                else {
                    stat.execute(command);

                    //refresh DBExplorer nodes
                    //while (!(info instanceof ConnectionNodeInfo)) {
                    //    info = info.getParent();
                    //}
                    connection.notifyChange();

                    /*
                    Vector<DatabaseNodeInfo> children = info.getChildren();
                    for (DatabaseNodeInfo child : children ) {
                        child.refreshChildren();
                    }
                    */
                }
            }
            status.setText(NbBundle.getMessage (DataViewWindow.class, "CommandExecuted")); //NOI18N
            stat.close();
        }

        /** Returns column name
        * @param column Column index
        */
        @Override
        public String getColumnName(int column) {
            synchronized (DataModel.this) {
                if (column < coldef.size()) {
                    String cname = ((ColDef)coldef.elementAt(column)).getName();
                    return cname;
                }

                return ""; //NOI18N
            }
        }

        /** Returns column renderer/editor class
        * @param column Column index
        */
        @Override
        public Class getColumnClass(int column) {
            synchronized (DataModel.this) {
                if (column < coldef.size()) {
                    int coltype = ((ColDef)coldef.elementAt(column)).getDataType();
                    switch (coltype) {
                        case Types.CHAR:
                        case Types.VARCHAR:
                        case Types.LONGVARCHAR: return String.class;
                        case Types.BIT: return Boolean.class;
                        case Types.TINYINT:
                        case Types.SMALLINT:
                        case Types.INTEGER: return Integer.class;
                        case Types.BIGINT: return Long.class;
                        case Types.FLOAT:
                        case Types.DOUBLE: return Double.class;
                        case Types.DATE: return java.sql.Date.class;
                    }
                }

                return Object.class;
            }
        }

        /** Returns true, if cell is editable
        */
        @Override
        public boolean isCellEditable(int row, int column) {
            synchronized (DataModel.this) {
                if (!editable)
                    return false;

                if (column < coldef.size())
                    return ((ColDef)coldef.elementAt(column)).isWritable();

                return false;
            }
        }

        /** Returns colun count
        */
        public int getColumnCount() {
            synchronized (DataModel.this) {
                return coldef.size();
            }
        }

        /** Returns row count
        */
        public int getRowCount() {
            synchronized (DataModel.this) {
                return data.size();
            }
        }

        /** Returns value at specified position
        */
        public Object getValueAt(int aRow, int aColumn) {
            synchronized (DataModel.this) {
                Vector row = new Vector();
                if (aRow < data.size())
                    row = (Vector) data.elementAt(aRow);
                if (row != null && aColumn < row.size())
                    return row.elementAt(aColumn);

                return null;
            }
        }

        private String format(Object value, int type) {
            if (value == null)
                return "null"; //NOI18N

            switch(type) {
                case Types.INTEGER:
                case Types.DOUBLE:
                case Types.FLOAT: return value.toString();
                case Types.BIT: return ((Boolean)value).booleanValue() ? "1" : "0"; //NOI18N
                case Types.DATE: return value.toString();
                default: return "\""+value.toString()+"\""; //NOI18N
            }
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            synchronized (DataModel.this) {
                int enucol = 0;
                StringBuffer where = new StringBuffer();
                Enumeration enu = coldef.elements();
                while (enu.hasMoreElements()) {
                    ColDef cd = (ColDef)enu.nextElement();
                    if (cd.isBestRowIdentifierColumn()) {
                        String key = cd.getName();
                        String val = format(getValueAt(row,enucol), cd.getDataType());
                        if (where.length()>0)
                            where.append(" and "); //NOI18N
                        where.append(key+" = "+val); //NOI18N
                    }
                    enucol++;
                }
            }
        }
    }

    @Override
    protected Object writeReplace() throws ObjectStreamException {
        return null;
    }
}
