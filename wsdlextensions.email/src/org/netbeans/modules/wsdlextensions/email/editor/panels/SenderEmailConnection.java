package org.netbeans.modules.wsdlextensions.email.editor.panels;

import java.security.Security;
import java.text.MessageFormat;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import org.netbeans.modules.wsdlextensions.email.editor.EmailError;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.openide.util.NbBundle;

/**
 *
 * @author skini
 */
public class SenderEmailConnection {

    private final String hostName;
    private final int port;
    private final String userName;
    private final String password;
    private final boolean useSSL;
    private boolean isValidated;
    private final String protocolType;
    private final String hostProperty;
    private final String portProperty;
    private final String userProperty;
    private String userAuthProperty;

    public SenderEmailConnection(String hostName, int port, final String userName, final String password, boolean useSSL, String protocol) {
        this.hostName = hostName;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.useSSL = useSSL;
        this.protocolType = protocol + (useSSL ? "s" : "");
        hostProperty = MessageFormat.format("mail.{0}.host", protocolType);
        portProperty = MessageFormat.format("mail.{0}.port", protocolType);
        userProperty = MessageFormat.format("mail.{0}.user", protocolType);
        userAuthProperty = MessageFormat.format("mail.{0}.auth", protocolType);
    }

    public Session getSession() {
        // get the session properties
        Properties props = new Properties();
        props.put("mail.transport.protocol", protocolType);
        if (useSSL) {
            props.setProperty("mail.smtp.ssl.enable", String.valueOf(true));
            Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
            System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
        }
        props.put(hostProperty, hostName);
        if (port != 0) {
            props.put(portProperty, Integer.toString(port));
        }
        if (null != userName) {
            props.put(userProperty, userName);
            props.setProperty(userAuthProperty, String.valueOf(true));
        } else {
            props.setProperty(userAuthProperty, String.valueOf(false));
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

    public EmailError validateOutbound() {
        isValidated = false;
        Transport transport = null;
        try {
            transport = getSession().getTransport();
            try {
                try {
                    transport.connect();
                } catch (MessagingException ex) {
                    String message = ex.getLocalizedMessage();
                    return new EmailError(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, (message == null) ? ex.getMessage() : message);
                }
            } finally {
                try {
                    transport.close();
                } catch (MessagingException ex) {
                    String message = ex.getLocalizedMessage();
                    return new EmailError(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, (message == null) ? ex.getMessage() : message);
                }
            }
        } catch (NoSuchProviderException ex) {
            String message = ex.getLocalizedMessage();
            return new EmailError(ExtensibilityElementConfigurationEditorComponent.PROPERTY_ERROR_EVT, (message == null) ? ex.getMessage() : message);
        }

        isValidated = true;
        return new EmailError(ExtensibilityElementConfigurationEditorComponent.PROPERTY_NORMAL_MESSAGE_EVT, NbBundle.getMessage(SenderEmailConnection.class, "EmailConnection.validationSuccess"));
    }

    public boolean isValidated() {
        return isValidated;
    }
}
