/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.sql.editor.ui.actions;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.netbeans.api.db.explorer.ConnectionListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.api.sql.SQLExecuteCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.cookies.EditorCookie;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.actions.CookieAction;
import org.openide.util.actions.Presenter;

/**
 * Toolbar action to allow users to select a connection against which
 * to execute the SQL in the current editor window
 *
 * @author Jesse Beaumont, Andrei Badea
 */
public class ConnectionAction extends CookieAction {
    
    private static final Map/*<SQLExecuteCookie, ConnectionModel>*/ MODEL_REGISTRY = new HashMap();
    
    public ConnectionAction() {
    }
    
    /**
     * Get help context
     */
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    /**
     * Return the display name for the action.
     */
    public String getName() {
        return NbBundle.getMessage(ConnectionAction.class, "LBL_ConnectionAction");
    }

    /**
     * The action accepts only one node with an SQL and editor cookie on it.
     */
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }
    
    protected Class[] cookieClasses() {
        return new Class[] { EditorCookie.class, SQLExecuteCookie.class };
    }
    
    protected void performAction(Node[] activatedNodes) {
    }
    
    public Action createContextAwareInstance(Lookup actionContext) {
        return new DelegateAction(this, actionContext);
    }
    
    private static final class DelegateAction implements Action, Presenter.Toolbar {
        
        private Lookup actionContext;
        private Action delegate;
        
        public DelegateAction(Action delegate, Lookup actionContext) {
            this.actionContext = actionContext;
            this.delegate = delegate;
        }

        public Object getValue(String key) {
            return delegate.getValue(key);
        }

        public void putValue(String key, Object value) {
            delegate.putValue(key, value);
        }

        public void actionPerformed(ActionEvent e) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void setEnabled(boolean b) {
        }

        public boolean isEnabled() {
            return true;
        }

        public Component getToolbarPresenter() {
            Node node = (Node)actionContext.lookup(Node.class);
            SQLExecuteCookie sqlCookie = (SQLExecuteCookie)node.getCookie(SQLExecuteCookie.class);
            return new ToolbarPresenter(sqlCookie);
        }
    }
    
    private static ConnectionModel getConnectionModelForCookie(SQLExecuteCookie cookie) {
        assert cookie != null;
        synchronized (MODEL_REGISTRY) {
            ConnectionModel model = (ConnectionModel)MODEL_REGISTRY.get(cookie);
            if (model == null) {
                model = new ConnectionModel();
                MODEL_REGISTRY.put(cookie, model);
            }
            return model;
        }
    }
    
    public static DatabaseConnection getConnectionForCookie(SQLExecuteCookie cookie) {
        assert cookie != null;
        synchronized (MODEL_REGISTRY) {
            ConnectionModel model = (ConnectionModel)MODEL_REGISTRY.get(cookie);
            if (model != null) {
                return model.getSelectedConnection();
            } else {
                return null;
            }
        }
    }
    
    public static void setConnectionForCookie(SQLExecuteCookie cookie, DatabaseConnection dbconn) {
        assert cookie != null;
        synchronized (MODEL_REGISTRY) {
            ConnectionModel model = getConnectionModelForCookie(cookie);
            for (int i = 0; i < model.getSize(); i++) {
                if (((ConnectionWrapper)model.getElementAt(i)).getConnection() == dbconn) {
                    model.setSelectedItem(model.getElementAt(i));
                }
            }
        }
    }
   
    /**
     * The toolbar presenter for this action.
     */
    private static final class ToolbarPresenter extends JPanel {
        
        private SQLExecuteCookie sqlCookie;
        
        public ToolbarPresenter(SQLExecuteCookie sqlCookie) {
            this.sqlCookie = sqlCookie;
            initComponents();
        }
        
        private void initComponents() {
            JLabel comboLabel;
            JComboBox combo;
            
            setLayout(new BorderLayout(4, 0));
            setBorder(new EmptyBorder(0, 2, 0, 8));
            setOpaque(false);
            
            combo = new JComboBox();
            ConnectionModel model = ConnectionAction.getConnectionModelForCookie(sqlCookie);
            combo.setModel(model);
            add(combo, BorderLayout.CENTER);
            
            comboLabel = new JLabel();
            comboLabel.setText(NbBundle.getMessage(ConnectionAction.class, "LBL_ConnectionAction"));
            comboLabel.setDisplayedMnemonic(NbBundle.getMessage(ConnectionAction.class, "MNE_ConnectionAction").charAt(0));
            // comboLabel.setToolTipText(NbBundle.getMessage(ConnectionAction.class, "HINT_ConnectionAction"));
            comboLabel.setOpaque(false);
            comboLabel.setLabelFor(combo);
            
            add(comboLabel, BorderLayout.WEST);
        }
    }
    
    private static final class ConnectionModel extends AbstractListModel implements ComboBoxModel, ConnectionListener {

        private Object selectedItem;
        private ConnectionListener listener;
        private Map map = new HashMap();
        private int size;
        
        public ConnectionModel() {
            listener = (ConnectionListener)WeakListeners.create(ConnectionListener.class, this, ConnectionManager.getDefault());
            ConnectionManager.getDefault().addConnectionListener(listener);
        }
        
        public Object getElementAt(int index) {
            return getWrapper(ConnectionManager.getDefault().getConnections()[index]);
        }
        
        public int getSize() {
            size = ConnectionManager.getDefault().getConnections().length;
            return size;
        }
        
        public void setSelectedItem(Object object) {
            selectedItem = object;
        }
        
        public Object getSelectedItem() {
            return selectedItem;
        }
        
        public DatabaseConnection getSelectedConnection() {
            if (selectedItem != null) {
                return ((ConnectionWrapper)selectedItem).getConnection();
            } else {
                return null;
            }
        }
        
        public void connectionsChanged() {
            if (selectedItem != null && !Arrays.asList(ConnectionManager.getDefault().getConnections()).contains(((ConnectionWrapper)selectedItem).getConnection())) {
                selectedItem = null;
            }
            fireContentsChanged(this, 0, size);
        }
        
        private ConnectionWrapper getWrapper(DatabaseConnection dbconn) {
            ConnectionWrapper wrapper = (ConnectionWrapper)map.get(dbconn);
            if (wrapper == null) {
                wrapper = new ConnectionWrapper(dbconn);
                map.put(dbconn, wrapper);
            }
            return wrapper;
        }
    }
    
    private static final class ConnectionWrapper {
        
        private DatabaseConnection dbconn;
        
        private ConnectionWrapper(DatabaseConnection dbconn) {
            this.dbconn = dbconn;
        }
        
        public DatabaseConnection getConnection() {
            return dbconn;
        }
        
        public String toString() {
            return dbconn.getName();
        }
    }
}
