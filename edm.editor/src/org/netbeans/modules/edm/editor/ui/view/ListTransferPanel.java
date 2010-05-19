/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.edm.editor.ui.view;

import java.awt.BorderLayout;
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
import org.openide.util.NbBundle;

/**
 * A Basic implementation of list transfer panel
 * 
 * @author Ritesh Adval
 * @version $Revision$
 */

public class ListTransferPanel extends JPanel implements ActionListener, ListSelectionListener {

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
    private class ListTransferModel {
        private ButtonModel addAllButtonModel;

        private ButtonModel addButtonModel;

        private HashSet changeListeners;
        private DefaultListModel dest;

        private boolean enableButton = true;

        private String listPrototype;
        private ButtonModel removeAllButtonModel;
        private ButtonModel removeButtonModel;
        private DefaultListModel source;

        /**
         * Creates a new instance of ListTransferModel, using the data in the given
         * collections to initially populate the source and destination lists.
         * 
         * @param srcColl Collection used to populate source list
         * @param dstColl Collection used to populate destination list
         */
        public ListTransferModel(Collection srcColl, Collection dstColl) {

            if (srcColl == null || dstColl == null) {
                throw new IllegalArgumentException(NbBundle.getMessage(ListTransferPanel.class, "MSG_null_collections_for_src/dstColl"));
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

            changeListeners = new HashSet();
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
                        source.removeElement(element);
                        fireTransferEvent(dest, element, TransferEvent.ADDED);
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
         * Moves all remaining items from source to destination list.
         */
        public void addAll() {
            synchronized (dest) {
                synchronized (source) {
                    int size = source.getSize();
                    for (int i = 0; i < size; i++) {
                        Object element = source.elementAt(i);
                        dest.addElement(element);
                        fireTransferEvent(dest, element, TransferEvent.ADDED);
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
         * @see org.openide.WizardDescriptor.Panel#addChangeListener
         */
        public void addChangeListener(ChangeListener l) {
            synchronized (listeners) {
                listeners.add(l);
            }
        }

        public void enableButton(boolean enable) {
            this.enableButton = enable;
            addButtonModel.setEnabled(enable);
            addAllButtonModel.setEnabled(enable);
            removeButtonModel.setEnabled(enable);
            removeAllButtonModel.setEnabled(enable);
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
         * Gets ButtonModel associated with add button action.
         * 
         * @return add ButtonModel
         */
        public ButtonModel getAddButtonModel() {
            return addButtonModel;
        }

        /**
         * Gets copy of current contents of destination list
         * 
         * @return List of current destination list contents
         */
        public List getDestinationList() {
            ArrayList dstList = new ArrayList();

            synchronized (dest) {
                dest.trimToSize();
                for (int i = 0; i < dest.size(); i++) {
                    dstList.add(dest.get(i));
                }
            }

            return dstList;
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
         * Gets maximum number of items expected in either the source or destination list.
         * 
         * @return maximum count of items in any one list
         */
        public int getMaximumListSize() {
            return source.size() + dest.size();
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
         * Gets ButtonModel associated with remove all button action
         * 
         * @return remove all ButtonModel
         */
        public ButtonModel getRemoveAllButtonModel() {
            return removeAllButtonModel;
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
         * Gets copy of current contents of source list
         * 
         * @return List of current source list contents
         */
        public List getSourceList() {
            ArrayList srcList = new ArrayList();

            synchronized (source) {
                source.trimToSize();
                for (int i = 0; i < source.size(); i++) {
                    srcList.add(source.get(i));
                }
            }

            return srcList;
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
                        dest.removeElement(element);
                        fireTransferEvent(dest, element, TransferEvent.REMOVED);
                    }
                }
            }

            updateButtonState();

            // fire change event so that next button can be enabled as we remove new rows
            // in table
            fireChangeEvent();
        }

        /**
         * Moves all remaining items from destination to source list.
         */
        public void removeAll() {
            synchronized (dest) {
                synchronized (source) {
                    int size = dest.getSize();
                    for (int i = 0; i < size; i++) {
                        Object element = dest.elementAt(i);
                        source.addElement(element);
                    }
                    dest.removeAllElements();
                }
            }

            updateButtonState();

            // fire change event so that next button can be enabled as we remove new rows
            // in table
            fireChangeEvent();
        }

        /**
         * @see org.openide.WizardDescriptor.Panel#removeChangeListener
         */
        public void removeChangeListener(ChangeListener l) {
            synchronized (listeners) {
                listeners.remove(l);
            }
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
                throw new IllegalArgumentException(NbBundle.getMessage(ListTransferPanel.class, "MSG_null_Collection_for_newList"));
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
         * Sets source list to include contents of given list. Clears current contents
         * before adding items from newList.
         * 
         * @param newList List whose contents will supplant the current contents of the
         *        source list
         */
        public void setSourceList(Collection newList) {
            if (newList == null) {
                throw new IllegalArgumentException(NbBundle.getMessage(ListTransferPanel.class, "MSG_null_Collection_for_newList"));
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
         * Updates button states
         */
        public void updateButtonState() {
            if (!enableButton) {
                return;
            }

            boolean canAdd = !source.isEmpty();
            boolean canRemove = !dest.isEmpty();

            addButtonModel.setEnabled(canAdd);
            addAllButtonModel.setEnabled(canAdd);
            removeButtonModel.setEnabled(canRemove);
            removeAllButtonModel.setEnabled(canRemove);
        }

        private void fireTransferEvent(Object src, Object item, int type) {
            if (src != null && item != null) {
                TransferEvent e = new TransferEvent(src, item, type);
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
    private static class TransferEvent extends ChangeEvent {
        /** Indicates addition of an item to the source of the event */
        public static final int ADDED = 0;

        /** Indicates removal of an item from the source of the event */
        public static final int REMOVED = 1;

        private Object item;
        private int type;

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
         * Gets item that was transferred.
         * 
         * @return transferred item
         */
        public Object getItem() {
            return item;
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

    /** Indicates addition of item(s). */
    public static final String LBL_ADD = ">";

    /** Label indicating that all elements should be moved. */
    public static final String LBL_ALL = NbBundle.getMessage(ListTransferPanel.class, "LBL_ALL");

    /** Indicates addition of all source items. */
    public static final String LBL_ADD_ALL = LBL_ALL + " " + LBL_ADD;

    /** Describes destination list */
    public static final String LBL_DEST_MSG = NbBundle.getMessage(ListTransferPanel.class, "LBL_Selected_Databases");

    /** Indicates removal of item(s). */
    public static final String LBL_REMOVE = "<";

    /** Indicates removal of all destination items. */
    public static final String LBL_REMOVE_ALL = LBL_REMOVE + " " + LBL_ALL;

    /** Describes source list and user task. */
    public static final String LBL_SOURCE_MSG = "Select Db's from the list:";

    /** Maximum number of visible items in lists */
    public static final int MAXIMUM_VISIBLE = 10;

    /** Minimum number of visible items in lists */
    public static final int MINIMUM_VISIBLE = 5;

    /** Tooltip to describe addition of selected item(s). */
    public static final String TIP_ADD = NbBundle.getMessage(ListTransferPanel.class, "TOOLTIP_Add_to_selected_items");

    /** Tooltip to describe addition of all source items. */
    public static final String TIP_ADD_ALL = NbBundle.getMessage(ListTransferPanel.class, "TOOLTIP_Add_all_items");

    /** Tooltip to describe addition of selected item(s). */
    public static final String TIP_REMOVE = NbBundle.getMessage(ListTransferPanel.class, "TOOLTIP_Remove_from_selected_items");

    /** Tooltip to describe removal of all destination items. */
    //public static final String TIP_REMOVE_ALL = "Remove all items";
    public static final String TIP_REMOVE_ALL = NbBundle.getMessage(ListTransferPanel.class, "TOOLTIP_Remove_all_items");
    private Collection destCollection;
    private JLabel destLabel;
    private String destLabelStr;
    private JList destList;

    /* Set <ChangeListeners> */
    private final Set listeners = new HashSet(1);

    private ListTransferModel listModel;

    private JList sourceList;

    private Collection srcCollection;

    private JLabel srcLabel;

    private String srcLabelStr;

    public ListTransferPanel(String sLabelStr, String dLabelStr, Collection sCollection, Collection dCollection) {

        this.srcLabelStr = sLabelStr;
        this.destLabelStr = dLabelStr;
        this.srcCollection = sCollection;
        this.destCollection = dCollection;

        initGui();
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
            listModel.add(selections, indices);
        } else if (LBL_ADD_ALL.equals(cmd)) {
            listModel.addAll();
        } else if (LBL_REMOVE.equals(cmd)) {
            int[] indices = destList.getSelectedIndices();
            Object[] selections = destList.getSelectedValues();
            listModel.remove(selections, indices);
        } else if (LBL_REMOVE_ALL.equals(cmd)) {
            listModel.removeAll();
        } else {
            // Log this as an invalid or unknown command.
            System.err.println(NbBundle.getMessage(ListTransferPanel.class, "MSG_Unknown_cmd") + cmd);
        }
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

    public void addToDestination(Object item) {
        DefaultListModel dModel = listModel.getDestinationModel();
        dModel.addElement(item);
    }

    public void addToSource(Object item) {
        DefaultListModel sModel = listModel.getSourceModel();
        sModel.addElement(item);
    }

    public void enableButton(boolean enable) {
        this.listModel.enableButton(enable);
    }

    /**
     * Fires a ChangeEvent to all interested listeners to indicate a state change in one
     * or more UI components.
     */
    public void fireChangeEvent() {
        Iterator it;

        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }

        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }

    public JList getDestinationJList() {
        return this.destList;
    }

    /**
     * Gets copy of current contents of destination list
     * 
     * @return List of current destination list contents
     */
    public List getDestinationList() {
        return listModel.getDestinationList();
    }

    public JList getSourceJList() {
        return this.sourceList;
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

    public void removeFromDestination(Object item) {
        DefaultListModel dModel = listModel.getDestinationModel();
        dModel.removeElement(item);
    }

    public void removeFromSource(Object item) {
        DefaultListModel sModel = listModel.getSourceModel();
        sModel.removeElement(item);
    }

    public void reset() {
        this.setSourceList(new ArrayList());
        this.setDestinationList(new ArrayList());
        this.enableButton(true);
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

    /**
     * set the target list cell renderer
     * 
     * @param cellRenderer list cell renderer
     */
    public void setDestinationListCellRenderer(ListCellRenderer cellRenderer) {
        this.destList.setCellRenderer(cellRenderer);
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
     * set the source list cell renderer
     * 
     * @param cellRenderer list cell renderer
     */
    public void setSourceListCellRenderer(ListCellRenderer cellRenderer) {
        this.sourceList.setCellRenderer(cellRenderer);
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
            // TODO: Log unhandled ListSelectionEvent as DEBUG message.
        }
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
        sourceList.getAccessibleContext().setAccessibleName("Source Table List");
        sourceList.getAccessibleContext().setAccessibleDescription("Source Table List");
        destList = new JList(listModel.getDestinationModel());
        destList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        destList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getSource() instanceof JList) {
                    JList list = (JList) e.getSource();
                    int[] indices = list.getSelectedIndices();
                    Object[] selections = list.getSelectedValues();
                    listModel.remove(selections, indices);
                }
            }
        });
        destList.addListSelectionListener(this);
        destList.setPrototypeCellValue(largestString);
        destList.setVisibleRowCount(visibleCt);
        destList.getAccessibleContext().setAccessibleName("Selected List");
        destList.getAccessibleContext().setAccessibleDescription("Selected List");
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BorderLayout());
        srcLabel = new JLabel(srcLabelStr);
        srcLabel.getAccessibleContext().setAccessibleName(srcLabelStr);
        srcLabel.getAccessibleContext().setAccessibleDescription(srcLabelStr);
        srcLabel.setDisplayedMnemonic('s');
        srcLabel.setLabelFor(sourceList);

        searchPanel.add(srcLabel, BorderLayout.NORTH);

        JScrollPane sourcepane = new JScrollPane(sourceList);
        JScrollPane destpane = new JScrollPane(destList);
        JPanel buttonPanel = new JPanel();

        JButton addButton = new JButton(LBL_ADD);
        addButton.setModel(listModel.getAddButtonModel());
        addButton.setToolTipText(TIP_ADD);
        addButton.getAccessibleContext().setAccessibleName(TIP_ADD);
        addButton.getAccessibleContext().setAccessibleDescription(TIP_ADD);
        addButton.setMnemonic('R');
        JButton removeButton = new JButton(LBL_REMOVE);
        removeButton.setModel(listModel.getRemoveButtonModel());
        removeButton.setToolTipText(TIP_REMOVE);
        removeButton.getAccessibleContext().setAccessibleName(TIP_REMOVE);
        removeButton.getAccessibleContext().setAccessibleDescription(TIP_REMOVE);
        removeButton.setMnemonic('L');
        JButton removeAllButton = new JButton(LBL_REMOVE_ALL);
        removeAllButton.setModel(listModel.getRemoveAllButtonModel());
        removeAllButton.setToolTipText(TIP_REMOVE_ALL);
        removeAllButton.getAccessibleContext().setAccessibleName(TIP_REMOVE_ALL);
        removeAllButton.getAccessibleContext().setAccessibleDescription(TIP_REMOVE_ALL);
        removeAllButton.setMnemonic(TIP_REMOVE_ALL.charAt(0));
        JButton addAllButton = new JButton(LBL_ADD_ALL);
        addAllButton.setModel(listModel.getAddAllButtonModel());
        addAllButton.setToolTipText(TIP_ADD_ALL);
        addAllButton.getAccessibleContext().setAccessibleName(TIP_ADD_ALL);
        addAllButton.getAccessibleContext().setAccessibleDescription(TIP_ADD_ALL);
        addAllButton.setMnemonic(TIP_ADD_ALL.charAt(0));
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
        sourcePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));

        JPanel ctrPanel = new JPanel();
        ctrPanel.add(buttonPanel);
        ctrPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        JPanel destPanel = new JPanel();
        destLabel = new JLabel(destLabelStr);
        destLabel.getAccessibleContext().setAccessibleName(destLabelStr);
        destLabel.getAccessibleContext().setAccessibleDescription(destLabelStr);
        destLabel.setDisplayedMnemonic('o');
        destLabel.setLabelFor(destList);

        destPanel.setLayout(new BorderLayout());
        destPanel.add(destLabel, BorderLayout.NORTH);
        destPanel.add(destpane, BorderLayout.CENTER);
        destPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));

        JPanel listPanel = new JPanel();

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        listPanel.setLayout(gridbag);

        // Allocate half of the resized space to sourcePanel
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        listPanel.add(sourcePanel, c);
        // Make ctrPanel non resizeable
        c.fill = GridBagConstraints.NONE;
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.0;
        c.weighty = 0.0;
        listPanel.add(ctrPanel, c);
        // Allocate half of the resized space to destPanel
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        listPanel.add(destPanel, c);

        setLayout(new BorderLayout());
        add(listPanel, BorderLayout.CENTER);

    }
}

