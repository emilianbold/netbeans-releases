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

package org.netbeans.modules.soa.ldap;

import java.awt.Dialog;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.soa.ldap.properties.AuthenticationPanel;
import org.netbeans.modules.soa.ldap.properties.AuthenticationType;
import org.netbeans.modules.soa.ldap.properties.ConnectionProperties;
import org.netbeans.modules.soa.ldap.properties.ConnectionPropertiesPanel;
import org.netbeans.modules.soa.ldap.properties.ConnectionPropertyType;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;

/**
 *
 * @author anjeleevich
 */
public final class LDAP {
    private boolean loaded = false;

    private List<LDAPConnection> connections
            = new ArrayList<LDAPConnection>();

    Preferences ldapPreferences = null;

    private final Object sync = new Object();
    private EventListenerList listenerList = new EventListenerList();

    private LDAP() {
        
    }

    public void addLDAPChangeListener(LDAPListener listener) {
        listenerList.add(LDAPListener.class, listener);
    }

    public void removeLDAPChangeListener(LDAPListener listener) {
        listenerList.remove(LDAPListener.class, listener);
    }

    public List<LDAPConnection> getConnections() {
        synchronized (sync) {
            loadConnections();
            return new ArrayList<LDAPConnection>(connections);
        }
    }

    public void createConnection() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    createConnection();
                }
            });
            return;
        }

        ConnectionPropertiesPanel panel = new ConnectionPropertiesPanel(null);

        DialogDescriptor descriptor = new DialogDescriptor(panel,
                NbBundle.getMessage(getClass(),
                "AddConnectionDialogTitle")); // NOI18N

        panel.setDialogDescriptor(descriptor);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        dialog.dispose();

        panel.setDialogDescriptor(null);

        if (descriptor.getValue() != DialogDescriptor.OK_OPTION) {
            return;
        }

        createConnection(panel.getNewConnectionProperties());
    }

    private LDAPConnection createConnection(ConnectionProperties properties) {
        IllegalThreadException.checkEDT();

        LDAPConnection result = null;
        LDAPListener[] listeners = null;

        synchronized (sync) {
            loadConnections();

            FileObject connectionsFolder = getLDAPConnectionsFolder(true);

            if (connectionsFolder != null) {
                String newChildName = generateNewChildName(connectionsFolder);

                FileObject newChild = null;
                OutputStream outputStream = null;
                boolean failed = false;

                try {
                    newChild = connectionsFolder.createData(newChildName,
                            FILE_EXT);

                    Document document = properties.toXMLDocument();

                    outputStream = newChild.getOutputStream();

                    XMLUtil.write(document, outputStream, "UTF-8");
                } catch (IOException ex) {
                    failed = true;
                    Logger.getLogger(LDAP.class.getName())
                            .log(Level.WARNING, ex.getMessage(), ex);
                } catch (ParserConfigurationException ex) {
                    failed = true;
                    Logger.getLogger(LDAP.class.getName())
                            .log(Level.WARNING, ex.getMessage(), ex);
                } catch (DOMException ex) {
                    failed = true;
                    Logger.getLogger(LDAP.class.getName())
                            .log(Level.WARNING, ex.getMessage(), ex);
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException ex) {
                            // do nothing
                        }
                    }

                    if (failed && newChild != null) {
                        try {
                            newChild.delete();
                        } catch (IOException ex) {
                            // do nothing;
                        }
                        newChild = null;
                    }
                }

                if (newChild != null) {
                    result = loadConnection(newChild);
                    if (result != null) {
                        connections.add(result);
                        listeners = listenerList
                                .getListeners(LDAPListener.class);
                    }
                }
            }
        }

        if (listeners != null && listeners.length > 0) {
            LDAPEvent event = new LDAPEvent(result);
            for (LDAPListener listener : listeners) {
                listener.ldapConnectionAdded(event);
            }
        }

        return result;
    }

    public void editConnection(final LDAPConnection connection) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    editConnection(connection);
                }
            });
            return;
        }

        ConnectionPropertiesPanel panel = new ConnectionPropertiesPanel(
                connection.getProperties());

        DialogDescriptor descriptor = new DialogDescriptor(panel,
                NbBundle.getMessage(getClass(),
                "EditConnectionDialogTitle")); // NOI18N

        panel.setDialogDescriptor(descriptor);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        dialog.dispose();

        panel.setDialogDescriptor(null);

        if (descriptor.getValue() != DialogDescriptor.OK_OPTION) {
            return;
        }

        ConnectionProperties oldConnectionProperties = panel
                .getOldConnectionProperties();
        ConnectionProperties newConnectionProperties = panel
                .getNewConnectionProperties();

        Set<ConnectionPropertyType> changes = oldConnectionProperties
                .getChangedProperties(newConnectionProperties);

        setConnectionProperties(connection, newConnectionProperties);
    }

    private void setConnectionProperties(LDAPConnection connection,
            ConnectionProperties newConnectionProperties)
    {
        LDAPListener[] listeners = null;

        ConnectionProperties oldConnectionProperties = connection
                .getProperties();

        Set<ConnectionPropertyType> changes = oldConnectionProperties
                .getChangedProperties(newConnectionProperties);

        if (changes.isEmpty()) {
            return;
        }
        
        synchronized (sync) {
            OutputStream outputStream = null;

            try {
                FileObject connectionsFolder = getLDAPConnectionsFolder(true);
                FileObject fileObject = connectionsFolder
                        .getFileObject(connection.getFileName(), "xml"); // NOI18N

                if (fileObject != null) {
                    Document document = newConnectionProperties.toXMLDocument();

                    outputStream = fileObject.getOutputStream();

                    XMLUtil.write(document, outputStream, "UTF-8"); // NOI18N

                    listeners = listenerList.getListeners(LDAPListener.class);

                    connection.setProperties(newConnectionProperties);
                }
            } catch (IOException ex) {

            } catch (DOMException ex) {

            } catch (ParserConfigurationException ex) {

            } finally {
                try {
                    outputStream.close();
                } catch (Exception ex) {
                    // do nothing
                }
            }
        }

        if (listeners != null && listeners.length > 0) {
            LDAPChangeEvent event = new LDAPChangeEvent(connection, changes);
            for (LDAPListener listener : listeners) {
                listener.ldapConnectionChanged(event);
            }
        }
    }

    private boolean isPasswordRequired(ConnectionProperties properties) {
        if (properties.getAuthenticationType() == AuthenticationType
                .NO_AUTHENTICATION)
        {
            return false;
        }

        char[] password = properties.getPassword();

        return (password == null) || (password.length == 0);
    }

    public boolean askPassword(LDAPConnection connection) {
        ConnectionProperties oldProperties = connection.getProperties();

        if (isPasswordRequired(oldProperties)) {
            AuthenticationPanel panel = new AuthenticationPanel(connection
                    .getProperties());

            DialogDescriptor dialogDescriptor = new DialogDescriptor(panel,
                    NbBundle.getMessage(LDAP.class,
                    "LDAP.AuthenticationDialogEnterPassword")); // NOI18N

            panel.setDialogDescriptor(dialogDescriptor);

            Dialog dialog = DialogDisplayer.getDefault()
                    .createDialog(dialogDescriptor);

            dialog.setVisible(true);
            dialog.dispose();

            if (dialogDescriptor.getValue() != DialogDescriptor.OK_OPTION) {
                return false;
            }

            ConnectionProperties newProperties = panel
                    .getNewConnectionProperties();

            setConnectionProperties(connection, newProperties);
        }

        return true;
    }

    public void deleteConnection(final LDAPConnection connection) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                   deleteConnection(connection); 
                }
            });
            return;
        }

        LDAPListener[] listeners = null;

        synchronized (sync) {
            FileObject connectionsFolder = getLDAPConnectionsFolder(false);

            if (connectionsFolder != null) {
                FileObject fileObject = connectionsFolder
                        .getFileObject(connection.getFileName(), FILE_EXT);

                if (fileObject != null) {
                    try {
                        fileObject.delete();
                    } catch (IOException ex) {
                        // do nothing
                    }
                }
            }

            connections.remove(connection);
            
            listeners = listenerList
                    .getListeners(LDAPListener.class);
        }

        if (listeners != null && listeners.length > 0) {
            LDAPEvent event = new LDAPEvent(connection);
            for (LDAPListener listener : listeners) {
                listener.ldapConnectionRemoved(event);
            }
        }
    }

    private void loadConnections() {
        synchronized (sync) {
            if (loaded) {
                return;
            }
            loaded = true;

            FileObject connectionsFolder = getLDAPConnectionsFolder(false);

            FileObject[] children = (connectionsFolder == null) ? null
                    : connectionsFolder.getChildren();

            if (children != null) {
                for (FileObject child : children) {
                    LDAPConnection connection = loadConnection(child);
                    if (connection != null) {
                        connections.add(connection);
                    }
                }
            }
        }
    }

    private LDAPConnection loadConnection(FileObject fileObject) {
        synchronized (sync) {
            LDAPConnection result = null;

            if (fileObject == null || fileObject.isFolder()) {
                return null;
            }
            String fileName = fileObject.getName();
            long index = LDAP.extractChildIndex(fileName);
            if (index < 1l || !"xml".equals(fileObject.getExt())) {
                return null;
            }

            InputStream inputStream = null;

            try {
                inputStream = fileObject.getInputStream();

                DocumentBuilder documentBuilder = DocumentBuilderFactory
                        .newInstance().newDocumentBuilder();
                Document document = documentBuilder.parse(inputStream);

                ConnectionProperties connectionProperties
                        = new ConnectionProperties(document);

                result = new LDAPConnection(fileName, connectionProperties);
            } catch (Exception ex) {
                // TODO write log
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException ex) {
                        // do nothing
                    }
                }
            }

            return result;
        }
    }


    private static FileObject getLDAPConnectionsFolder(boolean create) {
        FileObject result = null;
        
        try {
            result = FileUtil.getConfigFile(CONNECTIONS_PATH);
            if (result != null && !result.isFolder()) {
                return null;
            }

            if (create && result == null) {
                FileObject fileObject = FileUtil.getConfigRoot();

                StringTokenizer tokenizer
                        = new StringTokenizer(CONNECTIONS_PATH, "/");
                while (tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken();

                    FileObject nextFileObject = fileObject.getFileObject(token);

                    if (nextFileObject == null) {
                        nextFileObject = fileObject.createFolder(token);
                    } else if (!nextFileObject.isFolder()) {
                        return null;
                    }

                    fileObject = nextFileObject;
                }

                result = fileObject;
            }

//            FileSystem fileSystem = Repository.getDefault().getDefaultFileSystem();
//            result = fileSystem.findResource(CONNECTIONS_PATH);
//
//            if (result != null && !result.isFolder()) {
//                return null;
//            }
//
//            if (create && result == null) {
//                FileObject fileObject = fileSystem.getRoot();
//
//                StringTokenizer tokenizer
//                        = new StringTokenizer(CONNECTIONS_PATH, "/");
//                while (tokenizer.hasMoreTokens()) {
//                    String token = tokenizer.nextToken();
//
//                    FileObject nextFileObject = fileObject.getFileObject(token);
//
//                    if (nextFileObject == null) {
//                        nextFileObject = fileObject.createFolder(token);
//                    } else if (!nextFileObject.isFolder()) {
//                        return null;
//                    }
//
//                    fileObject = nextFileObject;
//                }
//
//                result = fileObject;
//            }
        } catch (IOException ex) {
            Logger.getLogger(LDAP.class.getName())
                    .log(Level.WARNING, ex.getMessage(), ex);
        }
        
        return result;
    }

    private static String generateNewChildName(FileObject parent) {
        FileObject[] children = parent.getChildren();

        if (children == null || children.length == 0) {
            return FILE_PREFIX + "1";
        }

        long maxChildIndex = 0;
        for (FileObject fileObject : children) {
            if (fileObject == null || fileObject.isFolder()
                    || !FILE_EXT.equals(fileObject.getExt()))
            {
                continue;
            }

            String name = fileObject.getName();

            maxChildIndex = Math.max(extractChildIndex(name),
                    maxChildIndex);
        }

        maxChildIndex++;

        return FILE_PREFIX + maxChildIndex;
    }

    private static long extractChildIndex(String childName) {
        if (childName == null) {
            return -1l;
        }

        if (!childName.startsWith(FILE_PREFIX)) {
            return -1l;
        }

        childName = childName.substring(FILE_PREFIX.length());
        
        long index = 0l;

        try {
            index = Long.parseLong(childName);
        } catch (NumberFormatException ex) {

        }

        return (index < 1l) ? -1l : index;
    }

    static final String FILE_PREFIX = "ldap";
    static final String FILE_EXT = "xml";
    static final String CONNECTIONS_PATH = "LDAP/connections"; // NOI18N
    
    public static final LDAP INSTANCE = new LDAP();
}
