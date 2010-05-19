/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.soa.ldap.browser;

import java.awt.BorderLayout;
import java.util.Set;
import javax.naming.InterruptedNamingException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapName;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import org.netbeans.modules.soa.ldap.IllegalThreadException;
import org.netbeans.modules.soa.ldap.LDAP;
import org.netbeans.modules.soa.ldap.LDAPChangeEvent;
import org.netbeans.modules.soa.ldap.LDAPConnection;
import org.netbeans.modules.soa.ldap.LDAPEvent;
import org.netbeans.modules.soa.ldap.LDAPListener;
import org.netbeans.modules.soa.ldap.LDAPUtils;
import org.netbeans.modules.soa.ldap.browser.FilterPanel.Event;
import org.netbeans.modules.soa.ldap.properties.ConnectionProperties;
import org.netbeans.modules.soa.ldap.properties.ConnectionPropertyType;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 *
 * @author anjeleevich
 */
public class LDAPBrowser extends TopComponent implements 
        FilterPanel.Listener, LDAPTreeView.Listener, LDAPListener
{

    private FilterPanel filterPanel;
    private LDAPConnection connection;

    private JSplitPane splitPane;

    private LDAPItemView itemView;
    private LDAPTreeView treeView;

    private JPanel connectingPanel;

    private RequestProcessor.Task connectingTask;

    public LDAPBrowser(LDAPConnection connection) {
        this.connection = connection;
        setDisplayName(connection.getDisplayName());

        setLayout(new BorderLayout());
        connectingPanel = new ConnectingPanel();
        add(connectingPanel, BorderLayout.CENTER);
    }

    @Override
    protected void componentClosed() {
        LDAP.INSTANCE.removeLDAPChangeListener(this);

        if (connectingTask != null) {
            connectingTask.cancel();
            connectingTask = null;
        }

        if (treeView != null) {
            treeView.removeLDAPTreeViewListener(this);
            treeView.abortBackgroundTasks();
        }

        if (filterPanel != null) {
            filterPanel.removeLDAPFilterListener(this);
        }

        if (itemView != null) {
            itemView.abortBackgroundTasks();
        }
        
        super.componentClosed();
    }

    @Override
    protected void componentOpened() {
        SwingUtilities.invokeLater(new ConnectRunnable());
    }

    @Override
    protected String preferredID() {
        return getPreferredID(connection);
    }

    public static String getPreferredID(LDAPConnection connection) {
        return "LDAPBrowserTopComponent-" + connection.getFileName();
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }

    public LDAPConnection getLDAPConnection() {
        return connection;
    }

    public void applyFilter() {
        treeView.setFilter(filterPanel.getFilter());
    }

    void ldapTreeSelectionChanged(LdapName ldapName) {
        itemView.setLDAPName(ldapName);
    }

    private void connect() {
        IllegalThreadException.checkEDT();

        // TODO At first check that connection was not removed

        if (!isOpened()) {
            return;
        }

        ConnectionProperties properties = connection.getProperties();

        if (!LDAP.INSTANCE.askPassword(connection)) {
            close();
            return;
        }

        connectingTask = RequestProcessor.getDefault()
                .post(new ConnectingRunnable());
    }

    private void connectionFaild(ConnectionProperties properties, String message) {
        IllegalThreadException.checkEDT();
        
        // show notify dialog
        // close top component

        if (connectingTask == null) {
            return;
        }
        
        connectingTask = null;

        if (!connection.getProperties().equals(properties)) {
            connectingTask = RequestProcessor.getDefault()
                    .post(new ConnectRunnable());
        } else {
//            ConnectionFailedPanel.showMessage(message);
            DialogDisplayer.getDefault().notify(new NotifyDescriptor
                    .Message(NbBundle.getMessage(LDAPBrowser.class,
                    "LDAPBrowser.connectionFaild", message), // NOI18N
                    NotifyDescriptor.ERROR_MESSAGE));
            close();
        }
    }

    private void connectionEstablished(ConnectionProperties properties) {
        IllegalThreadException.checkEDT();
        
        if (connectingTask == null) {
            return;
        }

        connectingTask = null;

        if (!connection.getProperties().equals(properties)) {
            connectingTask = RequestProcessor.getDefault()
                    .post(new ConnectRunnable());
            return;
        }

        remove(connectingPanel);
        connectingPanel = null;
        
        filterPanel = new FilterPanel();
        itemView = new LDAPItemView(connection);
        treeView = new LDAPTreeView(connection);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
                treeView, itemView);
        splitPane.setDividerLocation(0.4);
        splitPane.setDividerLocation(Math.max(100, getWidth() * 4 / 10));
        splitPane.setResizeWeight(0.4);
        splitPane.setBorder(null);

        filterPanel.addLDAPFilterListener(this);
        treeView.addLDAPTreeViewListener(this);

        setLayout(new BorderLayout());

        add(filterPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        revalidate();
        repaint();

        applyFilter();

        LDAP.INSTANCE.addLDAPChangeListener(this);
    }

    public void ldapFilterChanged(Event event) {
        treeView.setFilter(filterPanel.getFilter());
    }

    public void ldapTreeViewSelectionChanged(LDAPTreeView.Event event) {
        itemView.setLDAPName(treeView.getSelectedName());
    }

    public void ldapConnectionRemoved(LDAPEvent event) {
        if (event.getLDAPConnection() != connection) {
            return;
        }

        close();
    }

    public void ldapConnectionChanged(LDAPChangeEvent event) {
        if (event.getLDAPConnection() != connection) {
            return;
        }

        Set<ConnectionPropertyType> changedProperties = event
                .getChangedProperties();

        if (changedProperties.remove(ConnectionPropertyType.CONNECTION_NAME)) {
            String connectionName = connection.getProperties()
                    .getConnectionName();
            setDisplayName(connectionName);
        }
    }

    public void ldapConnectionAdded(LDAPEvent event) {
        // do nothing
    }

    private class ConnectingRunnable implements Runnable {
        private ConnectionProperties connectionProperties;

        public ConnectingRunnable() {
            this.connectionProperties = connection.getProperties();
        }

        public void run() {
            Runnable resultRunnable = null;

            DirContext dirContext = null;
            NamingEnumeration<? extends Attribute> result = null;

            try {
                dirContext = connectionProperties.createDirContext();
                Attributes attributes = dirContext.getAttributes("");

                result = attributes.getAll();
                while (result.hasMore()) {
                    Attribute attribute = result.next();
                }
            } catch (InterruptedNamingException ex) {
                return;
            } catch (NamingException ex) {
                resultRunnable = new ConnectionFaildRunnable(
                        connectionProperties,
                        LDAPUtils.exceptionToString(ex));
            } finally {
                LDAPUtils.close(result);
                LDAPUtils.close(dirContext);
                result = null;
                dirContext = null;
            }

            if (resultRunnable == null) {
                resultRunnable = new ConnectionEstablishedRunnable(
                        connectionProperties);
            }

            SwingUtilities.invokeLater(resultRunnable);
        }
    }

    private class ConnectRunnable implements Runnable {
        public void run() {
            connect();
        }
    }

    private class ConnectionFaildRunnable implements Runnable {
        private ConnectionProperties connectionProperties;
        private String message;

        ConnectionFaildRunnable(ConnectionProperties connectionProperties,
                String message)
        {
            this.connectionProperties = connectionProperties;
            this.message = message;
        }

        public void run() {
            connectionFaild(connectionProperties, message);
        }
    }

    private class ConnectionEstablishedRunnable implements Runnable {
        private ConnectionProperties connectionProperties;

        public ConnectionEstablishedRunnable(
                ConnectionProperties connectionProperties)
        {
            this.connectionProperties = connectionProperties;
        }
        
        public void run() {
            connectionEstablished(connectionProperties);
        }
    }
}
