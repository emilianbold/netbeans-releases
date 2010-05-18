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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.util.logging.Level;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.netbeans.modules.sql.framework.model.SQLJoinOperator;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.view.TableColumnTreePanel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.DBTable;

/**
 * This is the main join configuration view, where user selects table to join and can see
 * the preview of the join, can define join order and can select columns to appear in join
 * view in the canvas.
 * 
 * @author radval
 */
public class JoinMainPanel extends JPanel {

    private static final String LOG_CATEGORY = JoinMainPanel.class.getName();
    //private static transient final Logger mLogger = Logger.getLogger(JoinMainPanel.class.getName());
    private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LOG_CATEGORY);
    private static transient final Localizer mLoc = Localizer.get();
    private ListTransferPanel listPanel;
    private JTabbedPane bottomTabPane;
    private JSplitPane splitPane;
    private JoinPreviewPanel previewPanel;
    private TableColumnTreePanel tableColumnPanel;
    private Collection sources = new ArrayList();
    private Collection<DBTable> targets = new ArrayList<DBTable>();
    private URL tableImgUrl = getClass().getResource("/org/netbeans/modules/sql/framework/ui/resources/images/SourceTable.png");
    private SQLJoinView initialJoinView;
    private SQLJoinView copiedJoinView;
    private IGraphView mainGraphView;
    private transient boolean refreshPreview = true;
    private transient boolean showInstruction = true;
    private boolean isReordering = false;

    /** Creates a new instance of JoinMainPanel */
    public JoinMainPanel(IGraphView aGraphView) {
        this(aGraphView, true);
    }

    /**
     * Creates a new instance of JoinMainPanel associated with the given IGraphView
     * instance, toggling whether to display the instruction label based on the value of
     * <code>displayInstruction</code>.
     * 
     * @param graphView IGraphView instance to associate with this new instance
     * @param displayInstruction true if instruction label should be displayed; false
     *        otherwise
     */
    public JoinMainPanel(IGraphView graphView, boolean displayInstruction) {
        this.mainGraphView = graphView;
        showInstruction = displayInstruction;
        initGUI();
    }

    public JoinMainPanel(Collection joinSources) {
        this.sources = joinSources;
        initGUI();

    }

    private void initGUI() {
        // initialize and layout all the components here
        this.setLayout(new BorderLayout());
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setOneTouchExpandable(true);
        this.add(splitPane, BorderLayout.CENTER);

        GridBagLayout l = new GridBagLayout();

        GridBagConstraints gc = new GridBagConstraints();
        gc.weightx = 1.0;
        gc.weighty = 1.0;
        gc.fill = GridBagConstraints.BOTH;
        gc.gridwidth = GridBagConstraints.REMAINDER;

        JPanel mainTopPanel = new JPanel();
        mainTopPanel.setLayout(l);
        splitPane.setTopComponent(mainTopPanel);

        // initialize a panel which will contain list transfer panel
        // and more table button
        JPanel topPanel = new JPanel();
        topPanel.setMinimumSize(new Dimension(150, 215));

        topPanel.setLayout(new BorderLayout());

        mainTopPanel.add(topPanel, gc);

        // Initialize the join instruction label.
        if (showInstruction) {
            JPanel labelPnl = new JPanel();
            labelPnl.setLayout(new BorderLayout());

            String nbBundle1 = mLoc.t("BUND468: Select which tables you would like to join.");
            JLabel joinLabel = new JLabel(nbBundle1.substring(15));
            joinLabel.getAccessibleContext().setAccessibleName(nbBundle1.substring(15));

            labelPnl.add(joinLabel, BorderLayout.NORTH);
            labelPnl.add(new JSeparator(), BorderLayout.SOUTH);
            labelPnl.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

            topPanel.add(labelPnl, BorderLayout.NORTH);
        }

        // initialize list transfer panel
        String nbBundle2 = mLoc.t("BUND469: Available Tables:");
        String nbBundle3 = mLoc.t("BUND075: Selected Tables:");
        listPanel = new ListTransferPanel(this, nbBundle2.substring(15),
                nbBundle3.substring(15), sources, targets);

        // set the target list to allow continuous selection for up and down purpose
        listPanel.getDestinationJList().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        // add a listener to listen for updates in list model so that
        // preview panel can be refreshed.
        listPanel.getDestinationJList().getModel().addListDataListener(new TargetListDataListener());
        // set the list cell renderer
        listPanel.setSourceListCellRenderer(new SourceListRenderer());
        listPanel.setDestinationListCellRenderer(new TargetListRenderer());

        listPanel.getListTransferModel().addChangeListener(new ListTransferChangeListener());

        topPanel.add(listPanel, BorderLayout.CENTER);

        // initialize bottom tab panel
        bottomTabPane = new JTabbedPane();
        bottomTabPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        gc.weighty = 1.0;

        splitPane.setBottomComponent(bottomTabPane);

        // initalize join preview panel which is the first tab
        previewPanel = new JoinPreviewPanel(this);
        String nbBundle4 = mLoc.t("BUND289: Preview");
        previewPanel.setName(nbBundle4.substring(15));
        previewPanel.getAccessibleContext().setAccessibleName(nbBundle4.substring(15));
        // initialize select table column panel
        tableColumnPanel = new TableColumnTreePanel(new ArrayList());
        String nbBundle5 = mLoc.t("BUND429: Select Columns");
        tableColumnPanel.setName(nbBundle5.substring(15));

        // add these above two panel as tabs to bottom tab panel
        bottomTabPane.add(previewPanel);
        bottomTabPane.add(tableColumnPanel);
    }

    private List<DBTable> getJoinSourceTables() {
        List<DBTable> tables = new ArrayList<DBTable>();

        if (this.copiedJoinView != null) {
            SQLJoinOperator op = this.copiedJoinView.getRootJoin();
            if (op != null) {
                tables = JoinUtility.getJoinSourceTables(op);
            } else {
                tables = this.copiedJoinView.getSourceTables();
            }
        }

        return tables;
    }

    public void setTargetList(Collection<DBTable> tList) {
        if (tList != null) {
            this.targets = tList;
            listPanel.enableButton(false);
            listPanel.setDestinationList(tList);
        }
    }

    public List getTargetList() {
        return listPanel.getDestinationList();
    }

    public void setSourceList(Collection sList) {
        if (sList != null) {
            this.sources = sList;
            listPanel.setSourceList(sList);
        }
    }

    public List getSourceList() {
        return listPanel.getSourceList();
    }

    public void setDividerLocation() {
        Dimension d = this.getSize();
        int divLocation = d.height * 1 / 2;
        splitPane.setDividerLocation(divLocation);
        splitPane.setResizeWeight(0.5);
    }

    public void setDividerLocation(int divLocation) {
        splitPane.setDividerLocation(divLocation);
        splitPane.setResizeWeight(0.5);
    }

    public void setPreviewModifiable(boolean b) {
        this.previewPanel.setModifiable(b);
    }

    public void reset(IGraphView mainView) {
        this.mainGraphView = mainView;

        listPanel.reset();
        previewPanel.reset(mainView);
        tableColumnPanel.setTables(listPanel.getDestinationList());
    }

    private void refreshPanels(ListDataEvent e) {
        if (!refreshPreview) {
            return;
        }
        List<DBTable> currentList = listPanel.getDestinationList();

        if (isReordering) {
            previewPanel.refresh(currentList);

        } else {

            ListModel model = listPanel.getDestinationJList().getModel();
            int startIdx = e.getIndex0();
            int endIdx = e.getIndex1();

            for (int i = startIdx; i <= endIdx; i++) {
                SourceTable sTable = (SourceTable) model.getElementAt(i);
                previewPanel.refresh(sTable);
            }
            tableColumnPanel.setTables(currentList);
        }
    }

    private void removeTable(SourceTable sTable, int index) {
        previewPanel.removeTable(sTable);
        List<DBTable> currentList = listPanel.getDestinationList();
        ArrayList<Object> sTables = new ArrayList<Object>();
        // index is in the index of already removed table
        // if index is zero then we want to recreate all the joins again
        // other wise we will preserver joins created among tables before index
        if (index != 0) {
            for (int i = index; i < currentList.size(); i++) {
                sTables.add(currentList.get(i));
            }
            previewPanel.createJoin(sTables);
        } else {
            previewPanel.refresh(currentList);
        }

        tableColumnPanel.setTables(currentList);
    }

    class SourceListRenderer extends DefaultListCellRenderer {

        /**
         * Return a component that has been configured to display the specified value.
         * That component's <code>paint</code> method is then called to "render" the
         * cell. If it is necessary to compute the dimensions of a list because the list
         * cells do not have a fixed size, this method is called to generate a component
         * on which <code>getPreferredSize</code> can be invoked.
         * 
         * @param list The JList we're painting.
         * @param value The value returned by list.getModel().getElementAt(index).
         * @param index The cells index.
         * @param isSelected True if the specified cell was selected.
         * @param cellHasFocus True if the specified cell has the focus.
         * @return A component whose paint() method will render the specified value.
         * @see JList
         * @see ListSelectionModel
         * @see ListModel
         */
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            JLabel renderer = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof SourceTable) {
                renderer.setText(((SourceTable) value).getQualifiedName());
                renderer.setIcon(new ImageIcon(tableImgUrl));
            }

            return renderer;
        }
    }

    class TargetListRenderer extends DefaultListCellRenderer {

        /**
         * Return a component that has been configured to display the specified value.
         * That component's <code>paint</code> method is then called to "render" the
         * cell. If it is necessary to compute the dimensions of a list because the list
         * cells do not have a fixed size, this method is called to generate a component
         * on which <code>getPreferredSize</code> can be invoked.
         * 
         * @param list The JList we're painting.
         * @param value The value returned by list.getModel().getElementAt(index).
         * @param index The cells index.
         * @param isSelected True if the specified cell was selected.
         * @param cellHasFocus True if the specified cell has the focus.
         * @return A component whose paint() method will render the specified value.
         * @see JList
         * @see ListSelectionModel
         * @see ListModel
         */
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            JLabel renderer = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            JPanel cellRenderer = new JPanel();
            cellRenderer.setLayout(new GridBagLayout());
            GridBagConstraints gc = new GridBagConstraints();
            gc.weightx = 0.0;
            gc.weighty = 1.0;
            gc.fill = GridBagConstraints.BOTH;
            gc.gridwidth = GridBagConstraints.RELATIVE;
            // gc.insets = new Insets(0, 5, 0, 5);

            JLabel numLabel = new JLabel(" " + (++index) + " ");
            // numLabel.setSize(16, 16);
            numLabel.setBackground(Color.blue);
            numLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.black), BorderFactory.createEmptyBorder(0, 2,
                    0, 2)));

            cellRenderer.add(numLabel, gc);

            if (value instanceof SourceTable) {
                gc.weightx = 1.0;
                gc.weighty = 1.0;
                gc.gridwidth = GridBagConstraints.REMAINDER;
                gc.insets = new Insets(0, 2, 0, 0);

                renderer.setText(((SourceTable) value).getQualifiedName());
                renderer.setIcon(new ImageIcon(tableImgUrl));
                cellRenderer.add(renderer, gc);
                return cellRenderer;
            }

            return renderer;
        }
    }

    void moveUp() {
        if (!previewPanel.checkForUserDefinedCondition()) {
            return;
        }

        JList destList = JoinMainPanel.this.listPanel.getDestinationJList();
        int[] selectedIdxs = destList.getSelectedIndices();

        // if selected index is 0 then it can not be moved up any further
        if (selectedIdxs.length == 0 || selectedIdxs[0] == 0) {
            return;
        }

        ListModel model = destList.getModel();

        ArrayList<Object> newOrderedList = new ArrayList<Object>();

        int cumulativeElmCount = 0;

        // first add all the elements except the one before the first selected element to
        // newOrderedList
        for (int i = 0; i < model.getSize(); i++) {
            // check if i is not the one before the first selected element
            if (i == selectedIdxs[0] - 1) {
                break;
            }
            newOrderedList.add(model.getElementAt(i));
            cumulativeElmCount++;
        }

        int[] newSelectedIdxs = new int[selectedIdxs.length];

        // Now add all the selected elements
        for (int i = 0; i < selectedIdxs.length; i++) {
            int idx = selectedIdxs[i];

            // if selected index is 0 then it can not be moved up any further
            newOrderedList.add(model.getElementAt(idx));
            newSelectedIdxs[i] = newOrderedList.indexOf(model.getElementAt(idx));
        }

        // Now first add the element which was one before the first selected element
        if (selectedIdxs[0] - 1 >= 0) {
            newOrderedList.add(model.getElementAt(selectedIdxs[0] - 1));
            cumulativeElmCount++;
        }

        // then add all the element after the last selection element
        for (int i = selectedIdxs[selectedIdxs.length - 1] + 1; i < model.getSize(); i++) {
            // check if i is not the one before the first selected element
            if (i == selectedIdxs[0] - 1) {
                break;
            }
            newOrderedList.add(model.getElementAt(i));
        }

        isReordering = true;
        // now set new collection in the destination list
        JoinMainPanel.this.listPanel.setDestinationList(newOrderedList);

        destList.setSelectedIndices(newSelectedIdxs);

        isReordering = false;
    }

    void moveDown() {
        if (!previewPanel.checkForUserDefinedCondition()) {
            return;
        }

        JList destList = JoinMainPanel.this.listPanel.getDestinationJList();
        ListModel model = destList.getModel();

        int[] selectedIdxs = destList.getSelectedIndices();

        // if selected index is last item then it can not be moved down any further
        if (selectedIdxs.length == 0 || selectedIdxs[selectedIdxs.length - 1] == model.getSize() - 1) {
            return;
        }

        ArrayList<Object> newOrderedList = new ArrayList<Object>();

        int cumulativeElmCount = 0;

        // first add all the elements except the one before the first selected element to
        // newOrderedList
        for (int i = 0; i < model.getSize(); i++) {
            // check if i is not the one before the first selected element
            if (i == selectedIdxs[0]) {
                break;
            }
            newOrderedList.add(model.getElementAt(i));
            cumulativeElmCount++;
        }

        // Now add the element which was one after the last selected element
        newOrderedList.add(model.getElementAt(selectedIdxs[selectedIdxs.length - 1] + 1));
        cumulativeElmCount++;

        int[] newSelectedIdxs = new int[selectedIdxs.length];

        // Now add all the selected elements
        for (int i = 0; i < selectedIdxs.length; i++) {
            int idx = selectedIdxs[i];

            // if selected index is 0 then it can not be moved up any further
            newOrderedList.add(model.getElementAt(idx));
            newSelectedIdxs[i] = newOrderedList.indexOf(model.getElementAt(idx));
        }

        // then add all the element after the last selection element
        for (int i = selectedIdxs[selectedIdxs.length - 1] + 2; i < model.getSize(); i++) {
            newOrderedList.add(model.getElementAt(i));
        }

        isReordering = true;
        // now set new collection in the destination list
        JoinMainPanel.this.listPanel.setDestinationList(newOrderedList);

        destList.setSelectedIndices(newSelectedIdxs);
        isReordering = false;
    }

    class TargetListDataListener implements ListDataListener {

        /**
         * Sent when the contents of the list has changed in a way that's too complex to
         * characterize with the previous methods. For example, this is sent when an item
         * has been replaced. Index0 and index1 bracket the change.
         * 
         * @param e a <code>ListDataEvent</code> encapsulating the event information
         */
        public void contentsChanged(ListDataEvent e) {
            // refreshPanels(e);
        }

        /**
         * Sent after the indices in the index0,index1 interval have been inserted in the
         * data model. The new interval includes both index0 and index1.
         * 
         * @param e a <code>ListDataEvent</code> encapsulating the event information
         */
        public void intervalAdded(ListDataEvent e) {
            refreshPanels(e);
        }

        /**
         * Sent after the indices in the index0,index1 interval have been removed from the
         * data model. The interval includes both index0 and index1.
         * 
         * @param e a <code>ListDataEvent</code> encapsulating the event information
         */
        public void intervalRemoved(ListDataEvent e) {
            // refreshPanels(e);
        }
    }

    class ListTransferChangeListener implements ChangeListener {

        /**
         * Invoked when the target of the listener has changed its state.
         * 
         * @param e a ChangeEvent object
         */
        public void stateChanged(ChangeEvent e) {
            if (e instanceof ListTransferPanel.TransferEvent) {
                ListTransferPanel.TransferEvent tev = (ListTransferPanel.TransferEvent) e;
                if (tev.getType() == ListTransferPanel.TransferEvent.REMOVED) {
                    SourceTable sTable = (SourceTable) tev.getItem();
                    removeTable(sTable, tev.getItemIndex());
                }
            }
        }
    }

    public SQLJoinView getSQLJoinView() {
        return this.previewPanel.getSQLJoinView();
    }

    public SQLJoinView getInitialSQLJoinView() {
        return this.initialJoinView;
    }

    public void setSQLJoinView(SQLJoinView jView) {
        try {
            this.initialJoinView = jView;
            this.copiedJoinView = (SQLJoinView) jView.cloneSQLObject();

            this.targets = getJoinSourceTables();
            this.previewPanel.setSQLJoinView(this.copiedJoinView);
            tableColumnPanel.setTables(new ArrayList<DBTable>(targets));

            // setDestinationList will again refresh preview which we don't want
            refreshPreview = false;
            this.listPanel.setDestinationList(this.targets);
            refreshPreview = true;

        } catch (CloneNotSupportedException ex) {
            String msg = mLoc.t("EDIT159: cannot clone existing SQLJoinView{0}", LOG_CATEGORY);
            logger.log(Level.SEVERE, msg.substring(15) + ex);
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message("Join View model is corrupted. " + ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE));

            return;
        }
    }

    public void setEditSQLJoinView(SourceTable sTable, SQLJoinView jView) {
        try {
            this.initialJoinView = jView;
            this.copiedJoinView = (SQLJoinView) jView.cloneSQLObject();
            this.targets = new ArrayList<DBTable>();
            List<DBTable> joinSources = getJoinSourceTables();
            this.targets.addAll(joinSources);
            this.targets.add(sTable);

            this.previewPanel.setSQLJoinView(this.copiedJoinView);
            tableColumnPanel.setTables(new ArrayList<DBTable>(targets));

            // setDestinationList will again refresh preview which we don't want for
            // the tables of exsting join view
            refreshPreview = false;
            this.listPanel.setDestinationList(joinSources);
            refreshPreview = true;

            // now we want to add the new table to destination list and also want preview
            // to be refreshed
            this.listPanel.addToDestination(sTable);

        } catch (CloneNotSupportedException ex) {            
            logger.log(Level.SEVERE, "Join View model is corrupted"+ex);            
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message("Join View model is corrupted. " + ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
            return;
        }
    }

    public List getTableColumnNodes() {
        return this.tableColumnPanel.getTableColumnNodes();
    }

    public ListTransferPanel getListTransferPanel() {
        return this.listPanel;
    }

    public IGraphView getMainGraphView() {
        return this.mainGraphView;
    }

    public void handleCancel() {
        this.listPanel.removeMoreTablesOnCancel();
    }
}

