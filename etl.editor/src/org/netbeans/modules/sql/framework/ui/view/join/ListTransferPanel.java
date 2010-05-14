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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.DefaultButtonModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.model.utils.SQLObjectUtil;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.model.CollabSQLUIModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import com.sun.etl.exception.BaseException;
import java.util.logging.Level;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.DBTable;
import org.netbeans.modules.sql.framework.model.SQLDBTable;

/**
 * A Basic implementation of list transfer panel
 * @author radval
 */
public class ListTransferPanel extends JPanel implements ActionListener, ListSelectionListener {

    private static final String LOG_CATEGORY = ListTransferPanel.class.getName();
    /** Label indicating that all elements should be moved. */
    public static final String LBL_ALL = "ALL";
    /** Indicates addition of item(s). */
    public static final String LBL_ADD = ">";
    /** Tooltip to describe addition of selected item(s). */
    public static final String TIP_ADD = "Add to selected items";
    /** Indicates removal of item(s). */
    public static final String LBL_REMOVE = "<";
    /** Tooltip to describe addition of selected item(s). */
    public static final String TIP_REMOVE = "Remove from selected items";
    /** Indicates addition of all source items. */
    public static final String LBL_ADD_ALL = LBL_ALL + " " + LBL_ADD;
    /** Tooltip to describe addition of all source items. */
    public static final String TIP_ADD_ALL = "Add all items";
    /** Indicates removal of all destination items. */
    public static final String LBL_REMOVE_ALL = LBL_REMOVE + " " + LBL_ALL;
    /** Tooltip to describe removal of all destination items. */
    public static final String TIP_REMOVE_ALL = "Remove all items";
    /** Describes source list and user task. */
    public static final String LBL_SOURCE_MSG = "Select Connection from the list:";
    /** Describes destination list */
    public static final String LBL_DEST_MSG = "Selected Connections:";
    /** Minimum number of visible items in lists */
    public static final int MINIMUM_VISIBLE = 5;
    /** Maximum number of visible items in lists */
    public static final int MAXIMUM_VISIBLE = 10;
    /* Set <ChangeListeners> */
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    private JList sourceList;
    private JList destList;
    private JButton upButton;
    private JButton downButton;
    private JLabel srcLabel;
    private JLabel destLabel;
    private String srcLabelStr;
    private String destLabelStr;
    private Collection srcCollection;
    private Collection destCollection;
    private ListTransferModel listModel;
    private JoinMainPanel jmPanel;
    private ArrayList<SourceTable> newTables = new ArrayList<SourceTable>();
    //private static transient final Logger mLogger = Logger.getLogger(ListTransferPanel.class.getName());
    private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LOG_CATEGORY);
    private static transient final Localizer mLoc = Localizer.get();

    public ListTransferPanel(JoinMainPanel jMainPanel, String sLabelStr, String dLabelStr, Collection sCollection, Collection dCollection) {

        this.jmPanel = jMainPanel;
        this.srcLabelStr = sLabelStr;
        this.destLabelStr = dLabelStr;
        this.srcCollection = sCollection;
        this.destCollection = dCollection;

        initGui();
    }

    private void initGui() {
        listModel = new ListTransferModel(srcCollection, destCollection);
        String largestString = listModel.getPrototypeCell();

        if (largestString.length() < srcLabelStr.length()) {
            largestString = srcLabelStr;
        } else if (largestString.length() < destLabelStr.length()) {
            largestString = destLabelStr;
        }

        int visibleCt = Math.min(Math.max(MINIMUM_VISIBLE, listModel.getMaximumListSize()), MAXIMUM_VISIBLE);

        sourceList = new JList(listModel.getSourceModel());
        sourceList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        sourceList.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getSource() instanceof JList) {
                    JList list = (JList) e.getSource();
                    int[] indices = list.getSelectedIndices();
                    Object[] selections = list.getSelectedValues();
                    listModel.add(selections, indices);
                }
            }
        });
        sourceList.addListSelectionListener(this);
        sourceList.setPrototypeCellValue(largestString);
        sourceList.setVisibleRowCount(visibleCt);

        destList = new JList(listModel.getDestinationModel());
        destList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        destList.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getSource() instanceof JList) {
                    JList list = (JList) e.getSource();
                    int[] indices = list.getSelectedIndices();
                    Object[] selections = list.getSelectedValues();
                    if (isRemoveAllowed(selections)) {
                        listModel.remove(selections, indices);
                    }
                }
            }
        });
        destList.addListSelectionListener(this);
        destList.setPrototypeCellValue(largestString);
        destList.setVisibleRowCount(visibleCt);

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BorderLayout());
        srcLabel = new JLabel(srcLabelStr);
        srcLabel.getAccessibleContext().setAccessibleName(srcLabelStr);
        srcLabel.setLabelFor(sourceList);

        searchPanel.add(srcLabel, BorderLayout.NORTH);

        JScrollPane sourcepane = new JScrollPane(sourceList);
        JScrollPane destpane = new JScrollPane(destList);
        JPanel buttonPanel = new JPanel();

        JButton addButton = new JButton(LBL_ADD);
        addButton.setModel(listModel.getAddButtonModel());
        addButton.setToolTipText(TIP_ADD);

        JButton removeButton = new JButton(LBL_REMOVE);
        removeButton.setModel(listModel.getRemoveButtonModel());
        removeButton.setToolTipText(TIP_REMOVE);

        JButton removeAllButton = new JButton(LBL_REMOVE_ALL);
        removeAllButton.setModel(listModel.getRemoveAllButtonModel());
        removeAllButton.setToolTipText(TIP_REMOVE_ALL);

        JButton addAllButton = new JButton(LBL_ADD_ALL);
        addAllButton.setModel(listModel.getAddAllButtonModel());
        addAllButton.setToolTipText(TIP_ADD_ALL);

        addButton.setMargin(new Insets(2, 18, 2, 18));
        removeButton.setMargin(new Insets(2, 18, 2, 18));
        removeAllButton.setMargin(new Insets(2, 15, 2, 15));
        addAllButton.setMargin(new Insets(2, 15, 2, 15));

        buttonPanel.setLayout(new GridLayout(6, 1));

        buttonPanel.add(new JPanel());
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(new JPanel());
        buttonPanel.add(removeAllButton);
        buttonPanel.add(addAllButton);

        addButton.addActionListener(this);
        removeButton.addActionListener(this);
        removeAllButton.addActionListener(this);
        addAllButton.addActionListener(this);

        JPanel sourcePanel = new JPanel();
        sourcePanel.setLayout(new BorderLayout());
        sourcePanel.add(searchPanel, BorderLayout.NORTH);
        sourcePanel.add(sourcepane, BorderLayout.CENTER);
        sourcePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

        JPanel ctrPanel = new JPanel();
        ctrPanel.add(buttonPanel);
        ctrPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        JPanel destPanel = new JPanel();
        destLabel = new JLabel(destLabelStr);
        destLabel.getAccessibleContext().setAccessibleName(destLabelStr);
        // destLabel.setDisplayedMnemonic('o');
        destLabel.setLabelFor(destList);

        destPanel.setLayout(new BorderLayout());
        destPanel.add(destLabel, BorderLayout.NORTH);
        destPanel.add(destpane, BorderLayout.CENTER);
        destPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));

        JPanel listPanel = new JPanel();

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        listPanel.setLayout(gridbag);

        // allocate half of the resized space to sourcePanel
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        listPanel.add(sourcePanel, c);

        // make ctrPanel non resizeable
        c.fill = GridBagConstraints.NONE;
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.0;
        c.weighty = 0.0;
        listPanel.add(ctrPanel, c);

        // allocate half of the resized space to destPanel
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        listPanel.add(destPanel, c);

        // add updown button panel
        // initialize up and down target list button panel
        JPanel upDownButtonPanel = new JPanel();
        upDownButtonPanel.setLayout(new GridBagLayout());
        GridBagConstraints buttonPanelC = new GridBagConstraints();

        JPanel upDownPanel = new JPanel();
        upDownPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        buttonPanelC.weightx = 1.0;
        buttonPanelC.weighty = 0.0;
        buttonPanelC.fill = GridBagConstraints.NONE;
        buttonPanelC.gridwidth = GridBagConstraints.REMAINDER;
        upDownButtonPanel.add(upDownPanel, buttonPanelC);

        upDownPanel.setLayout(new GridBagLayout());
        GridBagConstraints buttonC = new GridBagConstraints();
        buttonC.weightx = 1.0;
        buttonC.weighty = 0.2;
        buttonC.fill = GridBagConstraints.NONE;
        buttonC.gridwidth = GridBagConstraints.REMAINDER;
        buttonC.insets = new Insets(0, 0, 5, 0);

        upButton = new JButton(new ImageIcon(ListTransferPanel.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/up.png")));
        upButton.setPreferredSize(new Dimension(20, 20));

        downButton = new JButton(new ImageIcon(ListTransferPanel.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/down.png")));
        downButton.setPreferredSize(new Dimension(20, 20));

        // add button action listener
        upButton.addActionListener(this);
        downButton.addActionListener(this);

        // JPanel bPanel = new JPanel();
        upDownPanel.add(upButton, buttonC);
        upDownPanel.add(downButton, buttonC);

        // add a dummy panel
        JPanel dummpyPanel2 = new JPanel();
        buttonPanelC.weightx = 1.0;
        buttonPanelC.weighty = 1.0;
        buttonPanelC.fill = GridBagConstraints.BOTH;
        buttonPanelC.gridwidth = GridBagConstraints.REMAINDER;
        upDownButtonPanel.add(dummpyPanel2, buttonPanelC);

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 3;
        c.gridy = 0;
        c.weightx = 0.0;
        c.weighty = 0.0;
        listPanel.add(upDownButtonPanel, c);

        setLayout(new BorderLayout());
        add(listPanel, BorderLayout.CENTER);
    }

    public void reset() {
        this.setSourceList(new ArrayList());
        this.setDestinationList(new ArrayList());
        this.enableButton(true);
        newTables.clear();
    }

    /**
     * set the source list cell renderer
     *
     * @param cellRenderer list cell renderer
     */
    public void setSourceListCellRenderer(ListCellRenderer cellRenderer) {
        this.sourceList.setCellRenderer(cellRenderer);
    }

    /**
     * set the target list cell renderer
     *
     * @param cellRenderer list cell renderer
     */
    public void setDestinationListCellRenderer(ListCellRenderer cellRenderer) {
        this.destList.setCellRenderer(cellRenderer);
    }

    public ListTransferModel getListTransferModel() {
        return this.listModel;
    }

    /**
     * set the source list
     *
     * @param sList source list
     */
    public void setSourceList(Collection sList) {
        listModel.setSourceList(sList);
        this.sourceList.setModel(listModel.getSourceModel());
    }

    /**
     * set the destination list
     *
     * @param dList destination list
     */
    public void setDestinationList(Collection dList) {
        listModel.setDestinationList(dList);
        this.destList.setModel(listModel.getDestinationModel());
    }

    public JList getSourceJList() {
        return this.sourceList;
    }

    public JList getDestinationJList() {
        return this.destList;
    }

    /**
     * Gets copy of current contents of source list
     *
     * @return List of current source list contents
     */
    public List getSourceList() {
        return listModel.getSourceList();
    }

    /**
     * Gets copy of current contents of destination list
     *
     * @return List of current destination list contents
     */
    public List<DBTable> getDestinationList() {
        return listModel.getDestinationList();
    }

    public void addToDestination(Object item) {
        DefaultListModel dModel = listModel.getDestinationModel();
        dModel.addElement(item);
    }

    public void removeFromDestination(Object item) {
        DefaultListModel dModel = listModel.getDestinationModel();
        dModel.removeElement(item);
    }

    public void addToSource(Object item) {
        DefaultListModel sModel = listModel.getSourceModel();
        sModel.addElement(item);
    }

    public void removeFromSource(Object item) {
        DefaultListModel sModel = listModel.getSourceModel();
        sModel.removeElement(item);
    }

    /**
     * Add a ChangeListener to this model.
     *
     * @param l ChangeListener to add
     */
    public void addChangeListener(ChangeListener l) {
        if (l != null) {
            synchronized (listeners) {
                listeners.add(l);
            }
        }
    }

    /**
     * Remove a ChangeListener from this model.
     *
     * @param l ChangeListener to remove
     */
    public void removeChangeListener(ChangeListener l) {
        if (l != null) {
            synchronized (listeners) {
                listeners.remove(l);
            }
        }
    }

    /**
     * Fires a ChangeEvent to all interested listeners to indicate a state change in one
     * or more UI components.
     */
    public void fireChangeEvent() {
        Iterator<ChangeListener> it;

        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }

        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }

    /**
     * Invoked whenever one of the transfer buttons is clicked.
     *
     * @param e ActionEvent to handle
     */
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

        if (LBL_ADD.equals(cmd)) {
            int[] indices = sourceList.getSelectedIndices();
            Object[] selections = sourceList.getSelectedValues();
            if (isAddAllowed(selections)) {
                listModel.add(selections, indices);
            }
        } else if (LBL_ADD_ALL.equals(cmd)) {
            Object[] selections = listModel.getSourceList().toArray();
            if (isAddAllowed(selections)) {
                listModel.addAll();
            }
        } else if (LBL_REMOVE.equals(cmd)) {
            int[] indices = destList.getSelectedIndices();
            Object[] selections = destList.getSelectedValues();
            if (isRemoveAllowed(selections)) {
                listModel.remove(selections, indices);
            }
        } else if (LBL_REMOVE_ALL.equals(cmd)) {
            Object[] selections = listModel.getDestinationList().toArray();
            if (isRemoveAllowed(selections)) {
                listModel.removeAll();
            }
        } else if (e.getSource().equals(upButton)) {
            this.jmPanel.moveUp();
        } else if (e.getSource().equals(downButton)) {
            this.jmPanel.moveDown();
        } else {
            // Log this as an invalid or unknown command.
            System.err.println("Unknown cmd: " + cmd);
        }
    }

    private boolean isRemoveAllowed(Object[] selections) {
        IGraphView graphView = this.jmPanel.getMainGraphView();
        // if graph view is null then it means this is used in collaboraiton creation
        // wizard
        // so return true;
        if (graphView == null) {
            return true;
        }

        SQLJoinView joinView = this.jmPanel.getInitialSQLJoinView();
        JoinViewGraphNode joinViewNode = (JoinViewGraphNode) graphView.findGraphNode(joinView);
        CollabSQLUIModel sqlModel = (CollabSQLUIModel) graphView.getGraphModel();

        // no join view exist
        if (joinViewNode != null) {
            for (int i = 0; i < selections.length; i++) {
                SourceTable sTable = (SourceTable) selections[i];
                if (joinViewNode.isTableColumnMapped(sTable)) {
                    NotifyDescriptor d = new NotifyDescriptor.Confirmation("Table " + sTable.getName() + " has some mappings defined which will be lost. Do you really want to remove this table?", NotifyDescriptor.WARNING_MESSAGE);
                    Object response = DialogDisplayer.getDefault().notify(d);
                    if (!response.equals(NotifyDescriptor.OK_OPTION)) {
                        return false;
                    }
                }
            }
        }

        NotifyDescriptor nd = new NotifyDescriptor.Confirmation("You may lose some user defined conditions in some joins. Do you really want to remove the table?", NotifyDescriptor.WARNING_MESSAGE);

        Object response = DialogDisplayer.getDefault().notify(nd);
        if (!response.equals(NotifyDescriptor.OK_OPTION)) {
            return false;
        }

        SourceTable sTable = null;
        try {
            // We need to remove the table from definition if this table was added using
            // more table dialog.
            for (int i = 0; i < selections.length; i++) {
                sTable = (SourceTable) selections[i];
                // if its a new table selected using more table dialog then
                // we need to add this to sql model
                if (newTables.contains(sTable)) {
                    sqlModel.removeObject(sTable);
                    newTables.remove(sTable);
                }
            }
        } catch (BaseException ex) {
            String tableName = sTable != null ? sTable.getName() : "";
            logger.log(Level.SEVERE, mLoc.t("EDIT190: Error Occured while removing the table{0}which user has added using more table dialog", tableName) + ex);
            NotifyDescriptor d = new NotifyDescriptor.Message("Table " + tableName + " which was added using more table dialog, can not be deleted from the model.", NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return false;
        }

        return true;
    }

    private boolean isAddAllowed(Object[] selections) {
        IGraphView graphView = this.jmPanel.getMainGraphView();
        SQLJoinView joinView = this.jmPanel.getInitialSQLJoinView();
        // if graph view is null then it means this is used in collaboraiton creation
        // wizard
        // so return true;
        if (graphView == null) {
            return true;
        }
        CollabSQLUIModel sqlModel = (CollabSQLUIModel) graphView.getGraphModel();

        // check if tables which we are trying to add are mapped to different target
        // tables
        // than the one which are already in join view
        Iterator it = this.getDestinationList().iterator();
        TargetTable joinViewMappedTable = null;
        while (it.hasNext()) {
            SourceTable sTable = (SourceTable) it.next();
            joinViewMappedTable = SQLObjectUtil.getMappedTargetTable(sTable, sqlModel.getSQLDefinition().getTargetTables());
            if (joinViewMappedTable != null) {
                break;
            }
        }

        if (joinViewMappedTable != null) {
            for (int i = 0; i < selections.length; i++) {
                SourceTable sTable = (SourceTable) selections[i];
                TargetTable mappedTable = SQLObjectUtil.getMappedTargetTable(sTable, sqlModel.getSQLDefinition().getTargetTables());
                if (mappedTable != null && !mappedTable.equals(joinViewMappedTable)) {
                    NotifyDescriptor d = new NotifyDescriptor.Message("Table " + sTable.getName() + " has some mappings defined to a different target table, so it can not be added", NotifyDescriptor.INFORMATION_MESSAGE);
                    DialogDisplayer.getDefault().notify(d);
                    return false;
                }
            }
        }

        TargetTable previousMappedTable = null;
        SourceTable previousSourceTable = null;

        for (int i = 0; i < selections.length; i++) {
            SourceTable sTable = (SourceTable) selections[i];
            TargetTable mappedTable = SQLObjectUtil.getMappedTargetTable(sTable, sqlModel.getSQLDefinition().getTargetTables());
            if (mappedTable != null) {
                if (joinView != null) {
                    SQLJoinView mappedTableJoinView = mappedTable.getJoinView();
                    // a target table may
                    if (mappedTableJoinView != null && !joinView.equals(mappedTableJoinView)) {
                        NotifyDescriptor d = new NotifyDescriptor.Message("Table " + sTable.getName() + " has some mappings defined to a different target table, so it can not be added", NotifyDescriptor.INFORMATION_MESSAGE);
                        DialogDisplayer.getDefault().notify(d);
                        return false;
                    }
                    return true;
                }
                // check if two tables which we are trying to add are mapped to two
                // different target tables
                if (previousMappedTable != null && previousSourceTable != null && !previousMappedTable.equals(mappedTable)) {
                    NotifyDescriptor d = new NotifyDescriptor.Message("Table " + sTable.getName() + " and Table " + previousSourceTable.getName() + " are mapped to different target table, so these can not be added", NotifyDescriptor.INFORMATION_MESSAGE);
                    DialogDisplayer.getDefault().notify(d);
                    return false;
                }

                previousMappedTable = mappedTable;
                previousSourceTable = sTable;
            }
        }

        SourceTable sTable = null;
        try {

            // We need to add the table if it is not definition(meaning this table is
            // selected in more table dialog) , because if user
            // uses this table column in join condition then it will not have an id
            // and will cause parsing for that condition column
            for (int i = 0; i < selections.length; i++) {
                sTable = (SourceTable) selections[i];
                // if its a new table selected using more table dialog then
                // we need to add this to sql model
                if (!sqlModel.exists(sTable)) {
                    sTable.setUsedInJoin(true);
                    sqlModel.addObject(sTable);
                    newTables.add(sTable);
                }
            }
        } catch (BaseException ex) {
            String tableName = sTable != null ? sTable.getName() : "";
            logger.log(Level.SEVERE, mLoc.t("EDIT191: Error Occured while adding the table {0}to model, which user has added using more table dialog", tableName) + ex);
            NotifyDescriptor d = new NotifyDescriptor.Message("Table " + tableName + " which was added using more table dialog, can not be added to the model.", NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return false;
        }

        return true;
    }

    /**
     * Called whenever the value of the selection changes.
     *
     * @param e the event that characterizes the change.
     */
    public void valueChanged(ListSelectionEvent e) {
        Object src = e.getSource();

        // Enforce mutually exclusive focus between source and destination
        // lists.
        if (sourceList.equals(src)) {
            if (!destList.isSelectionEmpty()) {
                destList.clearSelection();
            }
        } else if (destList.equals(src)) {
            if (!sourceList.isSelectionEmpty()) {
                sourceList.clearSelection();
            }
        } else {
            // @todo Log unhandled ListSelectionEvent as DEBUG message.
        }
        listModel.updateButtonState();
    }

    /**
     * Container for ListModels associated with source and destination lists of a list
     * transfer panel. Holds ButtonModels for controls that indicate selected addition and
     * bulk addition to destination list and selected removal and bulk removal of items
     * from the destination list.
     *
     * @author Jonathan Giron
     * @author Sanjeeth Duvuru
     * @version $Revision$
     */
    public class ListTransferModel {

        private DefaultListModel source;
        private DefaultListModel dest;
        private ButtonModel addButtonModel;
        private ButtonModel addAllButtonModel;
        private ButtonModel removeButtonModel;
        private ButtonModel removeAllButtonModel;
        private HashSet<ChangeListener> changeListeners;
        private String listPrototype;
        private boolean enableButton = true;

        /**
         * Creates a new instance of ListTransferModel, using the data in the given
         * collections to initially populate the source and destination lists.
         *
         * @param srcColl Collection used to populate source list
         * @param dstColl Collection used to populate destination list
         */
        public ListTransferModel(Collection srcColl, Collection dstColl) {

            if (srcColl == null || dstColl == null) {
                throw new IllegalArgumentException("Must supply non-null collections for srcColl and dstColl");
            }

            listPrototype = "";

            source = new DefaultListModel();
            dest = new DefaultListModel();

            addButtonModel = new DefaultButtonModel();
            addAllButtonModel = new DefaultButtonModel();
            removeButtonModel = new DefaultButtonModel();
            removeAllButtonModel = new DefaultButtonModel();

            setSourceList(srcColl);
            setDestinationList(dstColl);

            changeListeners = new HashSet<ChangeListener>();
        }

        /**
         * @see org.openide.WizardDescriptor.Panel#addChangeListener
         */
        public void addChangeListener(ChangeListener l) {
            synchronized (changeListeners) {
                changeListeners.add(l);
            }
        }

        /**
         * @see org.openide.WizardDescriptor.Panel#removeChangeListener
         */
        public void removeChangeListener(ChangeListener l) {
            synchronized (changeListeners) {
                changeListeners.remove(l);
            }
        }

        /**
         * Gets ListModel associated with source list.
         *
         * @return source ListModel
         */
        public DefaultListModel getSourceModel() {
            return source;
        }

        /**
         * Sets source list to include contents of given list. Clears current contents
         * before adding items from newList.
         *
         * @param newList List whose contents will supplant the current contents of the
         *        source list
         */
        public void setSourceList(Collection newList) {
            if (newList == null) {
                throw new IllegalArgumentException("Must supply non-null Collection for newList");
            }

            if (source == null) {
                source = new DefaultListModel();
            }

            synchronized (source) {
                source.clear();

                Iterator it = newList.iterator();
                while (it.hasNext()) {
                    Object o = it.next();
                    source.addElement(o);
                    if (o.toString().trim().length() > listPrototype.length()) {
                        listPrototype = o.toString().trim();
                    }
                }
            }

            updateButtonState();
        }

        /**
         * Gets copy of current contents of source list
         *
         * @return List of current source list contents
         */
        public List getSourceList() {
            ArrayList<Object> srcList = new ArrayList<Object>();

            synchronized (source) {
                source.trimToSize();
                for (int i = 0; i < source.size(); i++) {
                    srcList.add(source.get(i));
                }
            }

            return srcList;
        }

        /**
         * Gets ListModel associated with destination list.
         *
         * @return source ListModel
         */
        public DefaultListModel getDestinationModel() {
            return dest;
        }

        /**
         * Sets destination list to include contents of given list. Clears current
         * contents before adding items from newList.
         *
         * @param newList List whose contents will supplant the current contents of the
         *        destination list
         */
        public void setDestinationList(Collection newList) {
            if (newList == null) {
                throw new IllegalArgumentException("Must supply non-null Collection for newList");
            }

            if (dest == null) {
                dest = new DefaultListModel();
            }

            synchronized (dest) {
                dest.clear();

                Iterator it = newList.iterator();
                while (it.hasNext()) {
                    Object o = it.next();
                    dest.addElement(o);
                    if (o.toString().trim().length() > listPrototype.length()) {
                        listPrototype = o.toString().trim();
                    }
                }
            }

            updateButtonState();
        }

        /**
         * Gets copy of current contents of destination list
         *
         * @return List of current destination list contents
         */
        public List<DBTable> getDestinationList() {
            ArrayList<DBTable> dstList = new ArrayList<DBTable>();

            synchronized (dest) {
                dest.trimToSize();
                for (int i = 0; i < dest.size(); i++) {
                    dstList.add((SQLDBTable) dest.get(i));
                }
            }

            return dstList;
        }

        /**
         * Moves indicated items from source to destination list.
         *
         * @param selections array of selected items
         * @param indices array of indices, each element corresponding to the item in
         *        selections array
         */
        public void add(Object[] selections, int[] indices) {
            synchronized (dest) {
                synchronized (source) {
                    for (int i = 0; i < indices.length; i++) {
                        Object element = selections[i];
                        dest.addElement(element);
                        int index = dest.indexOf(element);
                        source.removeElement(element);
                        fireTransferEvent(dest, element, index, TransferEvent.ADDED);
                    }

                    updateButtonState();
                }

                // fire change event so that next button can be enabled as we add new
                // rows in table
                fireChangeEvent();
            }

            updateButtonState();
        }

        /**
         * Gets ButtonModel associated with add button action.
         *
         * @return add ButtonModel
         */
        public ButtonModel getAddButtonModel() {
            return addButtonModel;
        }

        /**
         * Moves all remaining items from source to destination list.
         */
        public void addAll() {
            synchronized (dest) {
                synchronized (source) {
                    int size = source.getSize();
                    for (int i = 0; i < size; i++) {
                        Object element = source.elementAt(i);
                        dest.addElement(element);
                        int index = dest.indexOf(element);
                        fireTransferEvent(dest, element, index, TransferEvent.ADDED);
                    }
                    source.removeAllElements();
                }
            }

            updateButtonState();

            // fire change event so that next button can be enabled as we add new rows in
            // table
            fireChangeEvent();
        }

        /**
         * Gets ButtonModel associated with add all button action.
         *
         * @return add all ButtonModel
         */
        public ButtonModel getAddAllButtonModel() {
            return addAllButtonModel;
        }

        /**
         * Moves indicated items from destination to source list.
         *
         * @param selections array of selected items
         * @param indices array of indices, each element corresponding to the item in
         *        selections array
         */
        public void remove(Object[] selections, int[] indices) {
            synchronized (dest) {
                synchronized (source) {
                    for (int i = 0; i < indices.length; i++) {
                        Object element = selections[i];
                        source.addElement(element);
                        int index = dest.indexOf(element);
                        dest.removeElement(element);
                        fireTransferEvent(dest, element, index, TransferEvent.REMOVED);
                    }
                }
            }

            updateButtonState();

            // fire change event so that next button can be enabled as we remove new rows
            // in table
            fireChangeEvent();
        }

        /**
         * Gets ButtonModel associated with remove button action.
         *
         * @return remove ButtonModel
         */
        public ButtonModel getRemoveButtonModel() {
            return removeButtonModel;
        }

        /**
         * Moves all remaining items from destination to source list.
         */
        public void removeAll() {
            synchronized (dest) {
                synchronized (source) {
                    int size = dest.getSize();
                    ArrayList<Object> removed = new ArrayList<Object>();
                    for (int i = 0; i < size; i++) {
                        Object element = dest.elementAt(i);
                        source.addElement(element);
                        removed.add(element);
                    }

                    for (int i = 0; i < size; i++) {
                        Object element = removed.get(i);
                        dest.removeElement(element);
                        fireTransferEvent(dest, element, i, TransferEvent.REMOVED);
                    }
                }
            }

            updateButtonState();

            // fire change event so that next button can be enabled as we remove new rows
            // in table
            fireChangeEvent();
        }

        /**
         * Gets ButtonModel associated with remove all button action
         *
         * @return remove all ButtonModel
         */
        public ButtonModel getRemoveAllButtonModel() {
            return removeAllButtonModel;
        }

        /**
         * Updates button states
         */
        public void updateButtonState() {
            if (!enableButton) {
                return;
            }

            boolean canAddAll = !source.isEmpty();
            boolean canRemoveAll = !dest.isEmpty();

            boolean srcSelected = (sourceList != null) && !sourceList.isSelectionEmpty();
            boolean destSelected = (destList != null) && !destList.isSelectionEmpty();

            boolean canAdd = canAddAll & srcSelected;
            boolean canRemove = canRemoveAll & destSelected;

            addButtonModel.setEnabled(canAdd);
            addAllButtonModel.setEnabled(canAddAll);
            removeButtonModel.setEnabled(canRemove);
            removeAllButtonModel.setEnabled(canRemoveAll);
        }

        public void enableButton(boolean enable) {
            this.enableButton = enable;
            addButtonModel.setEnabled(enable);
            addAllButtonModel.setEnabled(enable);
            removeButtonModel.setEnabled(enable);
            removeAllButtonModel.setEnabled(enable);
        }

        /**
         * Returns index of source item matching the given string.
         *
         * @param searchStr string to search for in source list
         * @param startFrom index from which to start search
         * @return index of matching item, or -1 if no match exists
         */
        public int getSourceIndexFor(String searchStr, int startFrom) {
            if (startFrom < 0 || startFrom > source.size()) {
                startFrom = 0;
            }

            if (searchStr != null && searchStr.trim().length() != 0) {
                return source.indexOf(searchStr, startFrom);
            }

            return -1;
        }

        /**
         * Gets prototype String that has the largest width of an item in either list.
         *
         * @return String whose length is the largest among the items in either list
         */
        public String getPrototypeCell() {
            return listPrototype;
        }

        /**
         * Gets maximum number of items expected in either the source or destination list.
         *
         * @return maximum count of items in any one list
         */
        public int getMaximumListSize() {
            return source.size() + dest.size();
        }

        private void fireTransferEvent(Object src, Object item, int index, int type) {
            if (src != null && item != null) {
                TransferEvent e = new TransferEvent(src, item, index, type);
                synchronized (changeListeners) {
                    Iterator iter = changeListeners.iterator();
                    while (iter.hasNext()) {
                        ChangeListener l = (ChangeListener) iter.next();
                        l.stateChanged(e);
                    }
                }
            }
        }
    }

    /**
     * Extends ChangeEvent to convey information on an item being transferred to or from
     * the source of the event.
     *
     * @author Jonathan Giron
     * @version $Revision$
     */
    public static class TransferEvent extends ChangeEvent {

        /** Indicates addition of an item to the source of the event */
        public static final int ADDED = 0;
        /** Indicates removal of an item from the source of the event */
        public static final int REMOVED = 1;
        private Object item;
        private int type;
        private int idx;

        /**
         * Create a new TransferEvent instance with the given source, item and type.
         *
         * @param source source of this transfer event
         * @param item transferred item
         * @param type transfer type, either ADDED or REMOVED
         * @see #ADDED
         * @see #REMOVED
         */
        public TransferEvent(Object source, Object item, int type) {
            super(source);
            this.item = item;
            this.type = type;
        }

        /**
         * Create a new TransferEvent instance with the given source, item and type.
         *
         * @param source source of this transfer event
         * @param item transferred item
         * @param type transfer type, either ADDED or REMOVED
         * @see #ADDED
         * @see #REMOVED
         */
        public TransferEvent(Object source, Object item, int index, int type) {
            super(source);
            this.item = item;
            this.idx = index;
            this.type = type;
        }

        /**
         * Gets item that was transferred.
         *
         * @return transferred item
         */
        public Object getItem() {
            return item;
        }

        public int getItemIndex() {
            return idx;
        }

        /**
         * Gets type of transfer event.
         *
         * @return ADDED or REMOVED
         */
        public int getType() {
            return type;
        }
    }

    public void enableButton(boolean enable) {
        // this.listModel.enableButton(enable);
    }

    // remove tables which were added using more table dialog, when user cancel join edit
    // or first time creation
    public void removeMoreTablesOnCancel() {
        IGraphView graphView = this.jmPanel.getMainGraphView();
        if (graphView != null) {
            CollabSQLUIModel sqlModel = (CollabSQLUIModel) graphView.getGraphModel();
            SourceTable sTable = null;

            try {
                // We need to remove the table from definition if this table was added using
                // more table dialog.
                for (int i = 0; i < newTables.size(); i++) {
                    sTable = newTables.get(i);
                    // if its a new table selected using more table dialog then
                    // we need to add this to sql model
                    sqlModel.removeObject(sTable);
                }
            } catch (BaseException ex) {
                String tableName = sTable != null ? sTable.getName() : "";
                logger.log(Level.SEVERE, mLoc.t("EDIT192: Error Occured while removing the table {0}which user has added using more table dialog", tableName) + ex);
                NotifyDescriptor d = new NotifyDescriptor.Message("Table " + tableName + " which was added using more table dialog, can not be deleted from the model.", NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        }
    }
//    private boolean existsIn(Collection collection, SQLObject obj) {
//        if (collection == null || obj == null) {
//            return false;
//        }
//
//        Iterator iter = collection.iterator();
//        boolean exists = false;
//        while (iter.hasNext()) {
//            Object existing = iter.next();
//            if (obj.toString().equals(existing.toString())) {
//                exists = true;
//                break;
//            }
//        }
//
//        return exists;
//    }
}
