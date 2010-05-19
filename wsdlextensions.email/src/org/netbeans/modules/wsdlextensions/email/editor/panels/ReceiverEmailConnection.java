package org.netbeans.modules.wsdlextensions.email.editor.panels;

import java.awt.Image;
import java.text.MessageFormat;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import org.netbeans.modules.wsdlextensions.email.editor.EmailError;
import org.netbeans.modules.wsdlextensions.email.editor.panels.Chooser.FolderIcon;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author skini
 */
public class ReceiverEmailConnection {

    private final String hostName;
    private final int port;
    private final String userName;
    private final String password;
    private final String folderName;
    private boolean isValidated;
    private Node rootNode;
    private final String protocolType;
    private final String hostProperty;
    private final String portProperty;
    private final String userProperty;

    public ReceiverEmailConnection(String hostName, int port, final String userName, final String password, boolean useSSL, String folderName, String protocol) {
        this.hostName = hostName;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.folderName = folderName;
        this.protocolType = protocol + (useSSL ? "s" : "");
        hostProperty = MessageFormat.format("mail.{0}.host", protocolType);
        portProperty = MessageFormat.format("mail.{0}.port", protocolType);
        userProperty = MessageFormat.format("mail.{0}.user", protocolType);
    }

    public Session getSession() {
        // get the session properties
        Properties props = new Properties();
        props.put("mail.store.protocol", protocolType);
        props.put(hostProperty, hostName);
        if (port != 0) {
            props.put(portProperty, Integer.toString(port));
        }
        if (null != userName) {
            props.put(userProperty, userName);
        }
        // get the authenticator information
        Authenticator sessionAuthenticator = new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        };

        Session session = Session.getInstance(props, sessionAuthenticator);
        return session;

    }

    public EmailError validateInbound(boolean populateFolders) {
        isValidated = false;
        Store store = null;
        Session session = getSession();
        try {
            store = session.getStore();
        } catch (NoSuchProviderException ex) {
            String message = ex.getLocalizedMessage();
            return new EmailError(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, (message == null) ? ex.getMessage() : message);
        }
        try {
            try {
                store.connect();
                if (populateFolders) {
                    try {
                        Folder defaultFolder = store.getDefaultFolder();
                        rootNode = getNode(defaultFolder);
                    } catch (MessagingException ex) {
                        String message = ex.getLocalizedMessage();
                        return new EmailError(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, (message == null) ? ex.getMessage() : message);
                    }
                }
            } catch (MessagingException ex) {
                String message = ex.getLocalizedMessage();
                return new EmailError(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, (message == null) ? ex.getMessage() : message);
            }

            if (folderName != null && folderName.trim().length() > 0) {
                Folder folder;
                try {
                    folder = store.getFolder(folderName.trim());
                    if (folder == null || !folder.exists()) {
                        return new EmailError(ExtensibilityElementConfigurationEditorComponent.PROPERTY_WARNING_EVT, NbBundle.getMessage(ReceiverEmailConnection.class, "EmailConnectionValidator.error.FolderNotExist", folderName));
                    }
                } catch (MessagingException ex) {
                    String message = ex.getLocalizedMessage();
                    return new EmailError(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, (message == null) ? ex.getMessage() : message);
                }

            }

        } finally {
            if (store != null) {
                try {
                    store.close();
                } catch (MessagingException ex1) {
                    //ignore
                }
            }
        }
        isValidated = true;
        return new EmailError(ExtensibilityElementConfigurationEditorComponent.PROPERTY_NORMAL_MESSAGE_EVT, NbBundle.getMessage(ReceiverEmailConnection.class, "EmailConnection.validationSuccess"));
    }

    public boolean isValidated() {
        return isValidated;
    }

    public Node getRootNode() {
        return rootNode;
    }

    private Node getNode(Folder folder) {
        Children children = getChildren(folder);
        AbstractNode abstractNode = new AbstractNode(children) {

            @Override
            public Image getIcon(int type) {
                return FolderIcon.getIcon(type);
            }

            @Override
            public Image getOpenedIcon(int type) {
                return FolderIcon.getOpenedIcon(type);
            }
        };
        abstractNode.setName(folder.getName());
        abstractNode.setDisplayName(folder.getName());
        return abstractNode;
    }

    private Children getChildren(Folder folder) {
        try {
            Folder[] childFolders = folder.list();
            if (childFolders.length > 0) {
                Children.Array arr = new Children.Array();
                Node[] childArr = new Node[childFolders.length];
                int i = 0;
                for (Folder child : childFolders) {
                    childArr[i++] = getNode(child);
                }
                arr.add(childArr);
                return arr;
            }
        } catch (MessagingException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Children.LEAF;
    }
}
