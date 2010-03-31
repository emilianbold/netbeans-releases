/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.db.sql.editor.ui.actions;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.DefaultFocusTraversalPolicy;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.netbeans.api.db.explorer.ConnectionListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.api.sql.execute.SQLExecution;
import org.openide.awt.Mnemonics;
import org.openide.cookies.EditorCookie;
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

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new ConnectionContextAwareDelegate(this, actionContext);
    }

    private static final class ConnectionContextAwareDelegate extends ContextAwareDelegate {

        private final Lookup actionContext;
        private ToolbarPresenter toolbarPresenter;

        public ConnectionContextAwareDelegate(ConnectionAction parent, Lookup actionContext) {
            super(parent, actionContext);
            this.actionContext = actionContext;
        }

        @Override
        public Component getToolbarPresenter() {
            toolbarPresenter = new ToolbarPresenter(actionContext);
            toolbarPresenter.setSQLExecution(getSQLExecution());
            return toolbarPresenter;
        }

        @Override
        public void setEnabled(boolean enabled) {
            if (toolbarPresenter != null) {
                toolbarPresenter.setEnabled(enabled);
            }
            super.setEnabled(enabled);
        }

        @Override
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

       private final Lookup actionContext;
        private JComboBox combo;
        private JLabel comboLabel;
        private DatabaseConnectionModel model;

        public ToolbarPresenter(final Lookup actionContext) {
            initComponents();
            this.actionContext = actionContext;
        }

        @Override
        public Dimension getMinimumSize() {
            Dimension dim = super.getMinimumSize();
            int minWidth = comboLabel.getWidth() * 2;
            return new Dimension(minWidth, dim.height);
        }

        public void setSQLExecution(SQLExecution sqlExecution) {
            model.setSQLExecution(sqlExecution);
        }

        private void initComponents() {
            setLayout(new BorderLayout(4, 0));
            setBorder(new EmptyBorder(0, 2, 0, 8));
            setOpaque(false);
            setFocusTraversalPolicyProvider(true);
           setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
              @Override
              public Component getDefaultComponent(Container aContainer) {
                 final EditorCookie ec = actionContext.lookup(
                       EditorCookie.class);
                 if (ec != null) {
                    JEditorPane[] panes = ec.getOpenedPanes();
                    if (panes != null) {
                       for (JEditorPane pane : panes) {
                          if (pane.isShowing()) {
                             return pane;
                          }
                       }
                    }
                 }

                 return null;
              }
           });

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
            combo.setPreferredSize (new Dimension (Math.min (combo.getPreferredSize ().width, 400), combo.getPreferredSize ().height));

            add(combo, BorderLayout.CENTER);

            comboLabel = new JLabel();
            Mnemonics.setLocalizedText(comboLabel, NbBundle.getMessage(ConnectionAction.class, "LBL_ConnectionAction"));
            comboLabel.setOpaque(false);
            comboLabel.setLabelFor(combo);
            add(comboLabel, BorderLayout.WEST);
        }

        @Override
        public void setEnabled(boolean enabled) {
            combo.setEnabled(enabled);
            super.setEnabled(enabled);
        }
    }

    private static final class DatabaseConnectionModel extends AbstractListModel implements ComboBoxModel, ConnectionListener, PropertyChangeListener {

        private ConnectionListener listener;
        private List<DatabaseConnection> connectionList; // must be ArrayList
        private SQLExecution sqlExecution;

        public DatabaseConnectionModel() {
            listener = WeakListeners.create (ConnectionListener.class, this, ConnectionManager.getDefault ());
            ConnectionManager.getDefault().addConnectionListener(listener);
            connectionList = new ArrayList<DatabaseConnection>();
            connectionList.addAll(Arrays.asList(ConnectionManager.getDefault().getConnections()));
            sortConnections();
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
                    sortConnections();

                    DatabaseConnection selectedItem = (DatabaseConnection)getSelectedItem();
                    if (selectedItem != null && !connectionList.contains(selectedItem)) {
                        setSelectedItem(null);
                    }
                    fireContentsChanged(this, 0, connectionList.size());
                }
            });
        }

        void sortConnections() {
            Collections.sort(connectionList, new Comparator<DatabaseConnection>() {
                @Override
                public int compare(DatabaseConnection o1, DatabaseConnection o2) {
                    return o1.getDisplayName().compareTo(o2.getDisplayName());
                }
            });
        }
    }

    private static final class DatabaseConnectionRenderer extends DefaultListCellRenderer {

        @Override
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
