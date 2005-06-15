/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.jmx.configwizard.generator;

import java.text.MessageFormat;
import java.io.File;
import java.util.Date;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.io.InputStream;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.WizardHelpers;
import org.netbeans.modules.jmx.configwizard.JMXConfigWizardIterator;
import org.netbeans.modules.jmx.configwizard.RMIAuthenticatedUser;

import org.netbeans.modules.jmx.runtime.ManagementDialogs;
import org.openide.NotifyDescriptor;

/**
 *
 *  Wizard Management Configuration files generator class
 */
public class ConfigGenerator 
{
    // string which represents the date and time of the creation
    private final String CURRENT_DATE = new Date().toString();
    private final static String COMMENT = new String("# ");
    private static final String FALSE = new String("false");
    private static final String TRUE = new String("true");
    private static final String FILEPATH = new String("filepath");
    private static final String INET = new String("<InetAddress>");
    private static final String CYPHER_SUITES = new String("<cypher-suites>");
    private static final String PROTOCOL_VER = 
            new String("<protocol-versions>");
    private static final String CONFIG_NAME = 
            new String("<config-name>");
    private static final String PORT_NUMBER = 
            new String("<port-number>");
    private static final String TRAP_PORT_NUMBER = 
            new String("<trap-destination-port-number>");

    
    private String rmiAccessFile;
    private String rmiPasswordFile;
    private boolean rmiSelected;
    private boolean rmiAuthenticate;
    private ArrayList rmiAuthenticatedUsers;
    private boolean rmiPortSelected;
    private int rmiPort;
    private boolean sslSelected;
    private boolean sslReqClientAuth;
    private boolean sslProtocolsSelected;
    private String sslProtocols;
    private boolean sslTlsCipherSelected;
    private String sslTlsCipher;
    private boolean snmpSelected;
    private boolean snmpPortSelected;
    private int snmpPort;
    private boolean snmpTrapPortSelected;
    private int snmpTrapPort;
    private boolean snmpInterfacesSelected;
    private String snmpInterfaces;
    private boolean snmpAclSelected;
    private boolean snmpAclFileSelected;
    private String snmpAclFile;
    private boolean threadMonitor;
    
    private void findInfo(TemplateWizard wiz) {
        Boolean rmiSelectedFinded = ((Boolean) 
            wiz.getProperty(WizardConstants.RMI_SELECTED)).booleanValue();
        if (rmiSelectedFinded == null) {
            rmiSelected = false;
        } else {
            rmiSelected = rmiSelectedFinded.booleanValue();
        }
        rmiAuthenticate = (Boolean) wiz.getProperty(
                WizardConstants.RMI_AUTHENTICATE);
        
        rmiAuthenticatedUsers = (ArrayList) wiz.getProperty(
                        WizardConstants.RMI_AUTHENTICATED_USERS);
        
        Integer rmiPortFinded = (Integer) wiz.getProperty(
                WizardConstants.RMI_PORT);
        if (rmiPortFinded == null) {
            rmiPortSelected = false;
            rmiPort = 0;
        } else {
            rmiPortSelected = true;
            rmiPort = rmiPortFinded;
        }
        sslSelected = (Boolean) wiz.getProperty(WizardConstants.SSL_SELECTED);
        sslReqClientAuth = (Boolean) wiz.getProperty(
                WizardConstants.RMI_SSL_CLIENT_AUTHENTICATE);
        String sslProtocolsFinded = (String) wiz.getProperty(
                WizardConstants.RMI_SSL_PROTOCOLS);
        if ((sslProtocolsFinded == null) || (sslProtocolsFinded.equals(""))) {
            sslProtocolsSelected = false;
            sslProtocols = "";
        } else {
            sslProtocolsSelected = true;
            sslProtocols = sslProtocolsFinded;
        }
        String sslTlsCipherFinded = (String) wiz.getProperty(
                WizardConstants.RMI_SSL_TLS_CIPHER);
        if ((sslTlsCipherFinded == null) || (sslTlsCipherFinded.equals(""))) {
            sslTlsCipherSelected = false;
            sslTlsCipher = "";
        } else {
            sslTlsCipherSelected = true;
            sslTlsCipher = sslTlsCipherFinded;
        }
        snmpSelected = (Boolean) wiz.getProperty(WizardConstants.SNMP_SELECTED);
        Integer snmpPortFinded = (Integer) wiz.getProperty(
                WizardConstants.SNMP_PORT);
        if (snmpPortFinded == null) {
            snmpPortSelected = false;
            snmpPort = 0;
        } else {
            snmpPortSelected = true;
            snmpPort = snmpPortFinded;
        }
        Integer snmpTrapPortFinded = (Integer) wiz.getProperty(
                WizardConstants.SNMP_TRAP_PORT);
        if (snmpTrapPortFinded == null) {
            snmpTrapPortSelected = false;
            snmpTrapPort = 0;
        } else {
            snmpTrapPortSelected = true;
            snmpTrapPort = snmpTrapPortFinded;
        }
        String snmpInterfacesFinded = (String) wiz.getProperty(
                WizardConstants.SNMP_INTERFACES);
        if ((snmpInterfacesFinded == null) 
                || (snmpInterfacesFinded.equals(""))) {
            snmpInterfacesSelected = false;
            snmpInterfaces = "";
        } else {
            snmpInterfacesSelected = true;
            snmpInterfaces = snmpInterfacesFinded;
        }
        snmpAclSelected = (Boolean) wiz.getProperty(WizardConstants.SNMP_ACL);
        String snmpAclFileFinded = (String) wiz.getProperty(
                WizardConstants.SNMP_ACL_FILE);
        if ((snmpAclFileFinded == null) 
                || (snmpAclFileFinded.equals(""))) {
            snmpAclFileSelected = false;
            snmpAclFile = "";
        } else {
            snmpAclFileSelected = true;
            snmpAclFile = snmpAclFileFinded;
        }
        threadMonitor = (Boolean) wiz.getProperty(
                WizardConstants.THREAD_CONTENTION_MONITOR);
        Integer otherPropNbFinded = (Integer) wiz.getProperty(
                WizardConstants.OTHER_PROP_NUMBER);
        if (otherPropNbFinded == null) {
            otherPropNbFinded = 0;
        }
    }
    
    // returns the management template
    private String getMgtTemplate(TemplateWizard wiz) throws Exception {
        FileObject mgtTemplate = Templates.getTemplate( wiz );
        // keep the pattern
        InputStream is = mgtTemplate.getInputStream();        
        StringBuffer configContent = new StringBuffer();
        int ch;
        while( (ch = is.read( )) != -1 )
            configContent.append( (char) ch );
        is.close();
        return configContent.toString();
    }

    // returns the access file template
    private String getAccessTemplate(TemplateWizard wiz) throws Exception {
        ResourceBundle bundle = NbBundle.getBundle(JMXConfigWizardIterator.class);   
        FileObject mgtTemplate = Templates.getTemplate( wiz );
        InputStream is = ((java.net.URL)mgtTemplate.
                getAttribute("accessTemplate")).openStream();   
        StringBuffer accessContent = new StringBuffer();
        int ch;
        while( (ch = is.read( )) != -1 )
            accessContent.append( (char) ch );
        
        if(rmiAuthenticate) {
            if(rmiAuthenticatedUsers != null) {
                for(int i = 0; i < rmiAuthenticatedUsers.size(); i++) {
                    RMIAuthenticatedUser r = 
                            (RMIAuthenticatedUser) rmiAuthenticatedUsers.get(i);
                    accessContent.append((r.getName() == null ? "" : r.getName()) + " " +
                                         (r.getAccess() == null ? "" : r.getAccess()) + "\n");
                }
            }
        }
        
        is.close();
        return accessContent.toString();
    }

    // returns the password file template
    private String getPasswordTemplate(TemplateWizard wiz) throws Exception {
        ResourceBundle bundle = NbBundle.getBundle(JMXConfigWizardIterator.class);   
        FileObject mgtTemplate = Templates.getTemplate( wiz );
        InputStream is = ((java.net.URL)mgtTemplate.
                getAttribute("passwordTemplate")).openStream();   
        StringBuffer passwordContent = new StringBuffer();
        int ch;
        while( (ch = is.read( )) != -1 )
            passwordContent.append( (char) ch );
        
        if(rmiAuthenticate) {
            if(rmiAuthenticatedUsers != null) {
                for(int i = 0; i < rmiAuthenticatedUsers.size(); i++) {
                    RMIAuthenticatedUser r =
                            (RMIAuthenticatedUser) rmiAuthenticatedUsers.get(i);
                    passwordContent.append((r.getName() == null ? "" : r.getName()) + " " +
                            (r.getPassword() == null ? "" : r.getPassword()) + "\n");
                }
            }
            ManagementDialogs.getDefault().notify(
                    new NotifyDescriptor.Message(NbBundle.getMessage(ConfigGenerator.class, "MSG_RestrictAccess"), NotifyDescriptor.INFORMATION_MESSAGE));
            
        }
        
        is.close();
        return passwordContent.toString();
    }
    
    // fill the management properties file
    private String fillMgtFile(TemplateWizard wiz) throws Exception {
        MessageFormat formMgtProp = new MessageFormat(getMgtTemplate(wiz));
        Object[] optionsArgs = new String[31];
        optionsArgs[0] = Templates.getTargetName(wiz);
        optionsArgs[1] = CURRENT_DATE;
        if (rmiSelected && rmiPortSelected) {
            optionsArgs[2] = "";
            optionsArgs[3] = String.valueOf(rmiPort);
        } else {
            optionsArgs[2] = COMMENT;
            optionsArgs[3] = PORT_NUMBER;
        }
        if (snmpSelected && snmpPortSelected) {
            optionsArgs[4] = "";
            optionsArgs[5] = String.valueOf(snmpPort);
        } else {
            optionsArgs[4] = COMMENT;
            optionsArgs[5] = PORT_NUMBER;
        }
        if (threadMonitor) {
            optionsArgs[6] = "";
        } else {
            optionsArgs[6] = COMMENT;
        }
        if (snmpSelected && snmpTrapPortSelected) {
            optionsArgs[7] = "";
            optionsArgs[8] = String.valueOf(snmpTrapPort);
        } else {
            optionsArgs[7] = COMMENT;
            optionsArgs[8] = TRAP_PORT_NUMBER;
        }
        if (snmpSelected && snmpInterfacesSelected) {
            optionsArgs[9] = "";
            optionsArgs[10] = snmpInterfaces;
        } else {
            optionsArgs[9] = COMMENT;
            optionsArgs[10] = INET;
        }
        if (snmpSelected) {
            optionsArgs[11] = "";
            if (snmpAclSelected) {
                optionsArgs[12] = TRUE;
            } else {
                optionsArgs[12] = FALSE;
            }
        } else {
            optionsArgs[11] = COMMENT;
            optionsArgs[12] = FALSE;
        }
        if (snmpSelected && snmpAclSelected && snmpAclFileSelected) {
            optionsArgs[13] = "";
            optionsArgs[14] = snmpAclFile;
        } else {
            optionsArgs[13] = COMMENT;
            optionsArgs[14] = FILEPATH;
        }
        if (rmiSelected) {
            optionsArgs[15] = "";
            if (sslSelected) {
                optionsArgs[16] = TRUE;
            } else {
                optionsArgs[16] = FALSE;
            }
        } else {
            optionsArgs[15] = COMMENT;
            optionsArgs[16] = FALSE;
        }
        if (rmiSelected && sslSelected && sslTlsCipherSelected) {
            optionsArgs[17] = "";
            optionsArgs[18] = sslTlsCipher;
        } else {
            optionsArgs[17] = COMMENT;
            optionsArgs[18] = CYPHER_SUITES;
        }
        if (rmiSelected && sslSelected && sslProtocolsSelected) {
            optionsArgs[19] = "";
            optionsArgs[20] = sslProtocols;
        } else {
            optionsArgs[19] = COMMENT;
            optionsArgs[20] = PROTOCOL_VER;
        }
        if (rmiSelected && sslSelected) {
            optionsArgs[21] = "";
            if (sslReqClientAuth) {
                optionsArgs[22] = TRUE;
            } else {
                optionsArgs[22] = FALSE;
            }
        } else {
            optionsArgs[21] = COMMENT;
            optionsArgs[22] = TRUE;
        }
        if (rmiSelected) {
            optionsArgs[23] = "";
            if (rmiAuthenticate) {
                optionsArgs[24] = TRUE;
            } else {
                optionsArgs[24] = FALSE;
            }
        } else {
            optionsArgs[23] = COMMENT;
            optionsArgs[24] = FALSE;
        }
        optionsArgs[25] = COMMENT;
        optionsArgs[26] = CONFIG_NAME;
        optionsArgs[27] = "";
        optionsArgs[28] = 
             FileUtil.toFile(Templates.getTargetFolder(
                wiz)).getAbsolutePath().replace(File.separatorChar, '/') + '/' +
                Templates.getTargetName(wiz) +
             "." + WizardConstants.PASSWORD_EXT;
        optionsArgs[29] = "";
        optionsArgs[30] = 
             FileUtil.toFile(Templates.getTargetFolder(
                wiz)).getAbsolutePath().replace(File.separatorChar, '/') + 
             '/' + Templates.getTargetName(wiz) +
             "." + WizardConstants.ACCESS_EXT;
        return formMgtProp.format(optionsArgs);
    }
    
    /**
     * Generate the management configuration files.
     * It is the entry point to generate Management config files.
     * @param wiz <CODE>WizardDescriptor</CODE> a wizard
     * @throws java.io.IOException <CODE>IOException</CODE>
     * @throws java.lang.Exception <CODE>Exception</CODE>
     * @return <CODE>FileObject</CODE> the generated management properties file
     */
    public FileObject generateConfig(TemplateWizard wiz)
           throws java.io.IOException, Exception {
        findInfo(wiz);
        // add options tag
        String mgtContent = fillMgtFile(wiz);
        String accessContent = getAccessTemplate(wiz);
        String passwordContent = getPasswordTemplate(wiz);
        return createGeneratedFiles(wiz,
                              mgtContent,
                              accessContent,
                              passwordContent);
    }
    
    /**
     * Create the Management Configuration files.
     * @return <CODE>FileObject</CODE> the generated management properties file
     * @param mgtContent <CODE>String</CODE> expected management properties file content
     * @param accessContent <CODE>String</CODE> expected access file content
     * @param passwordContent <CODE>String</CODE> expected password file content
     * @param wiz <CODE>WizardDescriptor</CODE> a wizard
     * @throws java.io.IOException <CODE>IOException</CODE>
     */
    protected FileObject createGeneratedFiles(WizardDescriptor wiz,
                                             String mgtContent,
                                             String accessContent,
                                             String passwordContent) 
                                             throws java.io.IOException {
        // create the access configuration file
        FileObject accessFile = 
                WizardHelpers.createFile(Templates.getTargetName(wiz),
                                 Templates.getTargetFolder(wiz),
                                 WizardConstants.ACCESS_EXT,
                                 accessContent);

        // create the password configuration file
        FileObject passwordFile = 
                WizardHelpers.createFile(Templates.getTargetName(wiz),
                                 Templates.getTargetFolder(wiz),
                                 WizardConstants.PASSWORD_EXT,
                                 passwordContent);

        // create the mbean implementation file
        return WizardHelpers.createFile(Templates.getTargetName(wiz),
                                 Templates.getTargetFolder(wiz),
                                 WizardConstants.PROPERTIES_EXT,
                                 mgtContent);
    }
   
}
