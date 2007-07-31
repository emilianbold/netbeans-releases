/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.sql.editor.ui.actions;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.netbeans.api.db.explorer.ConnectionListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.api.sql.execute.SQLExecution;
import org.openide.awt.Mnemonics;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author Andrei Badea
 */
public class ConnectionAction extends SQLExecutionBaseAction {

    protected String getDisplayName(SQLExecution sqlExecution) {
        // just needed in order to satisfy issue 101775
        return NbBundle.getMessage(ConnectionAction.class, "LBL_DatabaseConnection");
    }

    protected void actionPerformed(SQLExecution sqlExecution) {
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        return new ConnectionContextAwareDelegate(this, actionContext);
    }

    private static final class ConnectionContextAwareDelegate extends ContextAwareDelegate {

        private ToolbarPresenter toolbarPresenter;

        public ConnectionContextAwareDelegate(ConnectionAction parent, Lookup actionContext) {
            super(parent, actionContext);
        }

        public Component getToolbarPresenter() {
            toolbarPresenter = new ToolbarPresenter();
            toolbarPresenter.setSQLExecution(getSQLExecution());
            return toolbarPresenter;
        }

        public void setEnabled(boolean enabled) {
            if (toolbarPresenter != null) {
                toolbarPresenter.setEnabled(enabled);
            }
            super.setEnabled(enabled);
        }

        protected void setSQLExecution(final SQLExecution sqlExecution) {
            Mutex.EVENT.readAccess(new Runnable() {
                public void run() {
                    if (toolbarPresenter != null) {
                        // test for null necessary since the sqlExecution property
                        // can change just before the toolbar presenter is created
                        toolbarPresenter.setSQLExecution(sqlExecution);
                    }
                }
            });
            super.setSQLExecution(sqlExecution);
        }
    }

    private static final class ToolbarPresenter extends JPanel {

        private JComboBox combo;
        private JLabel comboLabel;
        private DatabaseConnectionModel model;

        public ToolbarPresenter() {
            initComponents();
        }

        public Dimension getMinimumSize() {
            Dimension dim = super.getMinimumSize();
            return new Dimension(0, dim.height);
        }

        public void setSQLExecution(SQLExecution sqlExecution) {
            model.setSQLExecution(sqlExecution);
        }

        private void initComponents() {
            setLayout(new BorderLayout(4, 0));
            setBorder(new EmptyBorder(0, 2, 0, 8));
            setOpaque(false);

            combo = new JComboBox();
            combo.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    DatabaseConnection dbconn = (DatabaseConnection)combo.getSelectedItem();
                    combo.setToolTipText(dbconn != null ? dbconn.getDisplayName() : null);
                }
            });
            combo.setOpaque(false);
            model = new DatabaseConnectionModel();
            combo.setModel(model);
            combo.setRenderer(new DatabaseConnectionRenderer());
            String accessibleName = NbBundle.getMessage(ConnectionAction.class, "LBL_DatabaseConnection");
            combo.getAccessibleContext().setAccessibleName(accessibleName);
            combo.getAccessibleContext().setAccessibleDescription(accessibleName);

            add(combo, BorderLayout.CENTER);

            comboLabel = new JLabel();
            Mnemonics.setLocalizedText(comboLabel, NbBundle.getMessage(ConnectionAction.class, "LBL_ConnectionAction"));
            comboLabel.setOpaque(false);
            comboLabel.setLabelFor(combo);
            add(comboLabel, BorderLayout.WEST);
        }

        public void setEnabled(boolean enabled) {
            combo.setEnabled(enabled);
            super.setEnabled(enabled);
        }
    }

    private static final class DatabaseConnectionModel extends AbstractListModel implements ComboBoxModel, ConnectionListener, PropertyChangeListener {

        private ConnectionListener listener;
        private List connectionList; // must be ArrayList
        private SQLExecution sqlExecution;

        public DatabaseConnectionModel() {
            listener = (ConnectionListener)WeakListeners.create(ConnectionListener.class, this, ConnectionManager.getDefault());
            ConnectionManager.getDefault().addConnectionListener(listener);
            connectionList = new ArrayList();
            connectionList.addAll(Arrays.asList(ConnectionManager.getDefault().getConnections()));
        }

        public Object getElementAt(int index) {
            return connectionList.get(index);
        }

        public int getSize() {
            return connectionList.size();
        }

        public void setSelectedItem(Object object) {
            if (sqlExecution != null) {
                sqlExecution.setDatabaseConnection((DatabaseConnection)object);
            }
        }

        public Object getSelectedItem() {
            return sqlExecution != null ? sqlExecution.getDatabaseConnection() : null;
        }

        public void setSQLExecution(SQLExecution sqlExecution) {
            if (this.sqlExecution != null) {
                this.sqlExecution.removePropertyChangeListener(this);
            }
            this.sqlExecution = sqlExecution;
            if (this.sqlExecution != null) {
                this.sqlExecution.addPropertyChangeListener(this);
            }
            fireContentsChanged(this, 0, 0); // because the selected item might have changed
        }

        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            if (propertyName == null || propertyName.equals(SQLExecution.PROP_DATABASE_CONNECTION)) {
                Mutex.EVENT.readAccess(new Runnable() {
                    public void run() {
                        fireContentsChanged(this, 0, 0); // because the selected item might have changed
                    }
                });
            }
        }

        public void connectionsChanged() {
            Mutex.EVENT.readAccess(new Runnable() {
                public void run() {
                    connectionList.clear();
                    connectionList.addAll(Arrays.asList(ConnectionManager.getDefault().getConnections()));

                    DatabaseConnection selectedItem = (DatabaseConnection)getSelectedItem();
                    if (selectedItem != null && !connectionList.contains(selectedItem)) {
                        setSelectedItem(null);
                    }
                    fireContentsChanged(this, 0, connectionList.size());
                }
            });
        }
    }

    private static final class DatabaseConnectionRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Object displayName = null;
            String tooltipText = null;

            if (value instanceof DatabaseConnection) {
                DatabaseConnection dbconn = (DatabaseConnection)value;
                tooltipText = dbconn.getDisplayName();
                displayName = tooltipText;
            } else {
                displayName = value;
            }
            JLabel component = (JLabel)super.getListCellRendererComponent(list, displayName, index, isSelected, cellHasFocus);
            component.setToolTipText(tooltipText);

            return component;
        }
    }
}
