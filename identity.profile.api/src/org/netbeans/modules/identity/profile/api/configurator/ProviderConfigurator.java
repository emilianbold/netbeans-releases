/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.identity.profile.api.configurator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import org.netbeans.modules.identity.profile.api.configurator.spi.ProviderConfig;
import org.openide.util.NbBundle;

/**
 * Configurator used to configure ProviderConfig.
 *
 * @author ptliu
 */
public class ProviderConfigurator extends Configurator {
    
    public static final String WSP_SERVICE_TYPE = "urn:wsp";        //NOI18N
    
    public enum Type {
        WSP, WSC
    };
    
    public enum Configurable {
        SECURITY_MECH,
        SECURITY_MECH_ORDERING,
        SECURITY_MECH_COLLECTION,
        SIGN_RESPONSE,
        KEY_ALIAS,
        KEY_PASSWORD,
        KEYSTORE_PASSWORD,
        KEYSTORE_LOCATION,
        PROVIDER_ID,
        SERVICE_URI,
        WSP_ENDPOINT,
        SERVER_PROPERTIES,
        USERNAME,
        PASSWORD,
        USERNAME_PASSWORD_PAIRS,
        SERVICE_TYPE,
        USE_DEFAULT_KEYSTORE,
        TRUST_AUTHORITY_CONFIG_LIST
    };
    
    private static final String DUMMY_PROVIDER_NAME = "org.netbeans.modules.identity.profile.api.DummyProviderName";  //NOI18N
    private static final String AM_CONFIG_FILE_PROP = "AM_CONFIG_FILE";   //NOI18N
    private static final String PROVIDER_ID_PROPERTY = "ProviderID";  //NOI18N
    private static final String SERVICE_URI_PROPERTY = "ServiceURI";  //NOI18N
    
    private String providerName;
    private Type type;
    private String serverID;
    private ProviderConfig providerConfig;
    private String keystoreLocation;
    private String keystorePassword;
    private String keyPassword;
    private boolean disabled = false;
    private JLabel errorComponent;
    private String errorText;
    private Collection<ChangeListener> listeners;
    private SecurityMechanismHelper secMechHelper;
    
    /** Creates a new instance of ProviderConfigurator */
    private ProviderConfigurator(String providerName, Type type, String serverID) {
        this.providerName = providerName;
        this.type = type;
        this.serverID = serverID;
        
        //
        // Force loading the SecurityMechanisms which will load
        // the amclientsdk.  If anything goes wrong, the exception
        // will be thrown from here.
        //
        secMechHelper = new SecurityMechanismHelper(serverID);
        secMechHelper.getAllSecurityMechanisms();
    }
    
    public static ProviderConfigurator getConfigurator(String providerName, Type type,
            AccessMethod accessMethod, Object accessToken, String serverID) {
        ProviderConfigurator configurator = new ProviderConfigurator(providerName, type, serverID);
        configurator.init(accessMethod, accessToken);
        
        return configurator;
    }
    
    public static Collection<ProviderConfigurator> getAllConfigurators(Type type,
            AccessMethod accessMethod, Object accessToken, String id) {
        //
        // Use a dummy ProviderConfig instance to retrieve the list of
        // all the provider names for the given type.
        //
        ProviderConfig dummyConfig = ProviderConfigFactory.newInstance(accessMethod,
                DUMMY_PROVIDER_NAME, type, accessToken);
        Collection<String> providerNames = dummyConfig.getAllProviderNames();
        ArrayList<ProviderConfigurator> result = new ArrayList<ProviderConfigurator>();
        
        for (String name : providerNames) {
            if (name.equals(DUMMY_PROVIDER_NAME))
                continue;
            
            result.add(ProviderConfigurator.getConfigurator(name, type, accessMethod,
                    accessToken, id));
        }
        
        return result;
    }
    
    protected void init(AccessMethod accessMethod, Object accessToken) {
        providerConfig = ProviderConfigFactory.newInstance(accessMethod,
                providerName, type, accessToken);
        
        //System.out.println("providerConfig = " + providerConfig);
        setConfiguration(providerConfig);
     
        // Initialize keyStoreLocation and keyStorePassword fields
        keystoreLocation = (String) getValue(Configurable.KEYSTORE_LOCATION);
        keystorePassword = (String) getValue(Configurable.KEYSTORE_PASSWORD);
        keyPassword = (String) getValue(Configurable.KEY_PASSWORD);
    }
    
    ProviderConfig getProviderConfig() {
        return providerConfig;
    }
    
    public String getProviderName() {
        return providerName;
    }
    
    public Type getType() {
        return type;
    }
    
    public boolean isEnabled() {
        return !disabled;
    }
    
    public void addChangeListener(ChangeListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<ChangeListener>();
        }
        
        listeners.add(listener);
    }
    
    public void fireStateChanged() {
        if (listeners != null) {
            ChangeEvent event = new ChangeEvent(this);
            
            for (ChangeListener l : listeners) {
                l.stateChanged(event);
            }
        }
    }
    
    public void addModifier(Configurable configurable, Object source) {
        addModifier(configurable, source, null);
    }
    
    public void addModifier(Configurable configurable, Object source,
            Object initialValue) {
        // TODO: check to make sure the modifier is supported for the given
        // configurable.
        
        if (providerConfig == null)
            return;
        
        switch(configurable) {
            case SECURITY_MECH:
                if (source instanceof JComboBox) {
                    setModel((JComboBox) source, (Collection<Object>) initialValue);
                } else {
                    // throw an exception
                }
                break;
            case SECURITY_MECH_COLLECTION:
                if (source instanceof JTable) {
                    setMultiSelectTableModel((JTable) source, (Collection<Object>) initialValue,
                            "LBL_SecurityMechanisms");  //NOI18N
                } else {
                    // throw an exception
                }
                break;
            case SECURITY_MECH_ORDERING:
                setSecurityMechOrderingModel((JTable) source,
                        (Collection<SecurityMechanism>) initialValue);
                // Let the UI deal with ordering directly
                return;
            case USERNAME_PASSWORD_PAIRS:
                setDataEntryTableModel((JTable) source, new String[] {
                    "LBL_UserName", "LBL_Password"});   //NOI18N
                break;
            case SERVER_PROPERTIES:
                setModel((JComboBox) source, (Collection<Object>) initialValue);
                break;
            case SIGN_RESPONSE:
            case USE_DEFAULT_KEYSTORE:
                if (!(source instanceof JCheckBox)) {
                    // throw an exception
                }
                break;
            case KEY_ALIAS:
            case KEY_PASSWORD:
            case KEYSTORE_PASSWORD:
            case KEYSTORE_LOCATION:
            case PROVIDER_ID:
            case SERVICE_URI:
            case WSP_ENDPOINT:
            case SERVICE_TYPE:
            case USERNAME:
            case PASSWORD:
                assert source instanceof JTextField;
                break;
        }
        
        addModifierInternal(configurable, source);
    }
    
    public void addErrorComponent(JLabel errorLabel) {
        this.errorComponent = errorLabel;
        
        validate();
    }
    
    public Object getValue(Enum configurable) {
        if (providerConfig == null)
            return null;
        
        if (configurable instanceof Configurable) {
            switch ((Configurable) configurable) {
                case SECURITY_MECH:
                    return getSecurityMechanism();
                case SECURITY_MECH_COLLECTION:
                    return getSecurityMechanisms();
                case SIGN_RESPONSE:
                    //System.out.println("providerConfig.isResponseSignEnabled = " +
                    //        providerConfig.isResponseSignEnabled());
                    if (providerConfig.isResponseSignEnabled()) {
                        return Boolean.TRUE;
                    } else {
                        return Boolean.FALSE;
                    }
                case KEY_ALIAS:
                    //System.out.println("providerConfig.getKeyAlias = " + providerConfig.getKeyAlias());
                    return providerConfig.getKeyAlias();
                case KEY_PASSWORD:
                    //System.out.println("providerConfig.getKeyPassword = " + providerConfig.getKeyPassword());
                    return providerConfig.getKeyPassword();
                case KEYSTORE_PASSWORD:
                    //System.out.println("providerConfig.getKeyStorePassword = " + providerConfig.getKeyStorePassword());
                    return providerConfig.getKeyStorePassword();
                case KEYSTORE_LOCATION:
                    //System.out.println("providerConfig.getKeyStoreFile = " + providerConfig.getKeyStoreFile());
                    return providerConfig.getKeyStoreFile();
                case PROVIDER_ID:
                    return providerConfig.getProperty(PROVIDER_ID_PROPERTY);
                case SERVICE_URI:
                    return providerConfig.getProperty(SERVICE_URI_PROPERTY);
                case WSP_ENDPOINT:
                    return providerConfig.getWSPEndpoint();
                case SERVER_PROPERTIES:
                    return providerConfig.getServerProperties(serverID);
                case USERNAME:
                    return providerConfig.getUserName();
                case PASSWORD:
                    return providerConfig.getPassword();
                case USERNAME_PASSWORD_PAIRS:
                    return providerConfig.getUserNamePasswordPairs();
                case SERVICE_TYPE:
                    return providerConfig.getServiceType();
                case USE_DEFAULT_KEYSTORE:
                    return providerConfig.useDefaultKeyStore();
            }
        }
        // TODO: should throw an exception
        return null;
    }
    
    public void setValue(Enum configurable, Object value) {
        if (providerConfig == null) return;
        
        if (configurable instanceof Configurable) {
            switch((Configurable) configurable) {
                case SECURITY_MECH:
                    setSecurityMechanism((SecurityMechanism) value);
                    break;
                case SECURITY_MECH_COLLECTION:
                    setSecurityMechanisms((Collection<SecurityMechanism>) value);
                    break;
                case SIGN_RESPONSE:
                    if (Boolean.TRUE.equals(value)) {
                        //System.out.println("enable Sign Response");
                        providerConfig.setResponseSignEnabled(true);
                    } else {
                        //System.out.println("disable Sign Response");
                        providerConfig.setResponseSignEnabled(false);
                    }
                    break;
                case KEY_ALIAS:
                    //System.out.println("setting keyAlias = " + value);
                    providerConfig.setKeyAlias((String) value);
                    break;
                case KEYSTORE_PASSWORD:
                    keystorePassword = (String) value;
                    break;
                case KEYSTORE_LOCATION:
                    keystoreLocation = (String) value;
                    break;
                case KEY_PASSWORD:
                    keyPassword = (String) value;
                    break;
                case PROVIDER_ID:
                    providerConfig.setProperty(PROVIDER_ID_PROPERTY, (String) value);
                    break;
                case SERVICE_URI:
                    providerConfig.setProperty(SERVICE_URI_PROPERTY, (String) value);
                    break;
                case WSP_ENDPOINT:
                    providerConfig.setWSPEndpoint((String) value);
                    break;
                case SERVER_PROPERTIES:
                    providerConfig.setServerProperties((ServerProperties) value);
                    break;
                case USERNAME:
                    providerConfig.setUserName((String) value);
                    break;
                case PASSWORD:
                    providerConfig.setPassword((String) value);
                    break;
                case USERNAME_PASSWORD_PAIRS:
                    providerConfig.setUserNamePasswordPairs((Collection<Vector<String>>) value);
                    break;
                case SERVICE_TYPE:
                    providerConfig.setServiceType((String) value);
                    break;
                case USE_DEFAULT_KEYSTORE:
                    if (Boolean.TRUE.equals(value)) {
                        providerConfig.setDefaultKeyStore(true);
                    } else {
                        providerConfig.setDefaultKeyStore(false);
                    }
                    break;
            }
            
            validate();
            fireStateChanged();
        }
    }
    
    public SecurityMechanismHelper getSecMechHelper() {
        return secMechHelper;
    }
    
    public void setError(String errorMsg) {
        if (errorComponent != null) {
            errorComponent.setText(errorMsg);
        }
        
        this.errorText = errorMsg;
    }
    
    public void clearError() {
        if (errorComponent != null) {
            errorComponent.setText(""); //NOI18N
        }
        
        errorText = null;
    }
    
    public String getError() {
        return errorText;
    }
    
    public void save() {
        try {
            if (providerConfig != null) {
                //System.out.println("saveProvider");
                if (!disabled) {
                    providerConfig.saveProvider();
                } else {
                    providerConfig.deleteProvider();
                }
            }
        } catch (ConfiguratorException ex) {
            ex.printStackTrace();
            //TODO: Need to report error
        }
    }
    
    public String validate() {
        clearError();
        
        String errorText = validateKeyStore();
        
        if (errorText != null) {
            setError(errorText);
        }
        
        return errorText;
    }
    
    private String validateKeyStore() {
        SecurityMechanism secMech = (SecurityMechanism) getValue(Configurable.SECURITY_MECH);
        
        // If secMech is null, it is not time to do validation yet.
        if (secMech == null) return null;
        
        Boolean signResponse = (Boolean) getValue(Configurable.SIGN_RESPONSE);
        
        //
        // For UserNameToken profile, we don't care about the keystore
        // unless signResponse is true.
        //
        if (secMech.isPasswordCredentialRequired() && 
                Boolean.FALSE.equals(signResponse)) {
            return null;
        }
        
        if (Boolean.FALSE.equals(getValue(Configurable.USE_DEFAULT_KEYSTORE))) {
            //
            // Call setKeyStore() to see if we get any error first.
            // For example, the client sdk does some validation of the
            // keystore information and we want to capture them here.
            //
            setKeyStore();
            String errorText = getError();
            if (errorText != null) return errorText;
            
            // Now check for empty values
            String keyAlias = (String) getValue(Configurable.KEY_ALIAS);
            
            if (keystoreLocation == null || keystoreLocation.trim().length() == 0) {
                return NbBundle.getMessage(ProviderConfigurator.class,
                        "LBL_InvalidKeystoreLocation");
            } else if (keystorePassword == null || keystorePassword.trim().length() == 0) {
                return NbBundle.getMessage(ProviderConfigurator.class,
                        "LBL_InvalidKeystorePassword");
            } else if (keyAlias == null || keyAlias.trim().length() == 0) {
                return NbBundle.getMessage(ProviderConfigurator.class,
                        "LBL_InvalidKeyAlias");                
            } else if (keyPassword == null || keyPassword.trim().length() == 0) {
                return NbBundle.getMessage(ProviderConfigurator.class,
                        "LBL_InvalidKeyPassword");
            }
        }
        
        return null;
    }
    
    public void disable() {
        disabled = true;
    }
    
    public void enable() {
        disabled = false;
    }
    
    private void setKeyStore() {
        try {
            //System.out.println("setKeyStore() location = " + keystoreLocation +
            //       " password = " + keystorePassword + " key password = " + keyPassword);
            providerConfig.setKeyStore(convertToForwardSlash(keystoreLocation),
                    keystorePassword, keyPassword);
        } catch (ConfiguratorException ex) {
            //System.out.println("ex from keystore password = " + ex);
            setError(ex.getCause().getMessage());
        }
    }
    
    private String convertToForwardSlash(String value) {
        return (value == null) ? null : value.replace('\\', '/');
    }
    
    private Collection<SecurityMechanism> getSecurityMechanisms() {
        return secMechHelper.getSecurityMechanismsFromURIs(providerConfig.getSecurityMechanisms());
    }
    
    private SecurityMechanism getSecurityMechanism() {
        Collection<SecurityMechanism> secMechs = getSecurityMechanisms();
        
        if (secMechs.size() > 0) {
            return secMechs.iterator().next();
        }
        
        return null;
    }
    
    private void setSecurityMechanisms(Collection<SecurityMechanism> secMechs) {
        providerConfig.setSecurityMechanisms(secMechHelper.getSecurityMechanismURIs(secMechs));
    }
    
    private void setSecurityMechanism(SecurityMechanism secMech) {
        Collection<SecurityMechanism> secMechs = new ArrayList<SecurityMechanism>();
        
        if (secMechs != null) {
            secMechs.add(secMech);
        }
        
        setSecurityMechanisms(secMechs);
    }
    
    private void setModel(JComboBox comboBox, Collection<Object> values) {
        Object[] valueArray = new Object[values.size()];
        valueArray = values.toArray(valueArray);
        comboBox.setModel(new DefaultComboBoxModel(valueArray));
        comboBox.setSelectedIndex(0);
    }
    
    /**
     *  TODO: Shold support multiple columns similar to
     *  setDataEntryTableModel
     *
     */
    private void setMultiSelectTableModel(JTable table, Collection<Object> values,
            String label) {
        Object[] valueArray = new Object[values.size()];
        valueArray = values.toArray(valueArray);
        MultiSelectTableModel model = new MultiSelectTableModel();
        
        model.addColumn(NbBundle.getMessage(ProviderConfigurator.class,
                label), valueArray);
        table.setModel(model);
    }
    
    private void setDataEntryTableModel(JTable table, String[] labels) {
        DataEntryTableModel model = new DataEntryTableModel();
        
        for (String label : labels) {
            model.addColumn(NbBundle.getMessage(ProviderConfigurator.class,
                    label));
        }
        
        table.setModel(model);
    }
    
    private void setSecurityMechOrderingModel(JTable table,
            Collection<SecurityMechanism> secMechs) {
        Collection<SecurityMechanism> secMechOrdering = getSecurityMechanisms();
        SecurityMechanism[] secMechArray = new SecurityMechanism[secMechs.size()];
        secMechArray = secMechs.toArray(secMechArray);
        SecurityMechanism[] ordering = null;
        
        //
        // RESOLVE: For now, if the ordering doesn't include
        // all the security mechs, we reset it.
        //
        if (secMechOrdering == null ||
                secMechOrdering.size() != secMechArray.length) {
            ordering = secMechArray;
        } else {
            ordering = new SecurityMechanism[secMechOrdering.size()];
            ordering = secMechOrdering.toArray(ordering);
        }
        
        DefaultTableModel tableModel = new DefaultTableModel();
        
        tableModel.addColumn(NbBundle.getMessage(ProviderConfigurator.class,
                "LBL_SecurityMechanismOrdering"), //NOI18N
                (Object[]) ordering);
        table.setModel(tableModel);
        
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    
    
}
