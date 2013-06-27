/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.cloud.wizards;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.ComboBoxModel;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;
import org.glassfish.tools.ide.data.cloud.GlassFishCloud;
import org.glassfish.tools.ide.data.cloud.GlassFishCloudEntity;
import org.netbeans.modules.glassfish.cloud.data.GlassFishAccountInstance;
import org.netbeans.modules.glassfish.cloud.data.GlassFishAccountInstanceProvider;
import org.netbeans.modules.glassfish.cloud.data.GlassFishCloudInstance;
import org.netbeans.modules.glassfish.cloud.data.GlassFishCloudInstanceProvider;
import static org.openide.util.NbBundle.getMessage;

/**
 *
 * @author kratz
 */
public class GlassFishAccountWizardUserComponent
        extends GlassFishWizardComponent {

    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * List of registered GlassFish cloud instances used to select one for
     * user account being registered.
     */
    static class CloudComboBox implements ComboBoxModel {

        ////////////////////////////////////////////////////////////////////////
        // Instance attributes                                                //
        ////////////////////////////////////////////////////////////////////////

        /** Combo box items. */
        List<GlassFishCloudEntity> items;

        /** Item selected in combo box. */
        GlassFishCloudEntity selected;

        /** Registered data listeners. */
        Set<ListDataListener> listeners;

        ////////////////////////////////////////////////////////////////////////
        // Constructors                                                       //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Creates an instance of GlassFish cloud instances combo box items.
         * <p/>
         * @param items List of GlassFish cloud entity objects that can
         *              be modified.
         */
        CloudComboBox(List<GlassFishCloudEntity> items) {
            this.items = items;
            this.selected = null;
            this.listeners = new HashSet<ListDataListener>();
        }

        ////////////////////////////////////////////////////////////////////////
        // Implemented Interface Methods                                      //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Set selected item.
         * <p/>
         * Implementation of this method notifies all registered
         * <code>ListDataListener</code>s that the contents have changed.
         * <p/>
         * @param anItem List object to select or<code>null</code> to clear
         *               the selection
         */
        @Override
        public void setSelectedItem(Object anItem) {
            if (anItem instanceof GlassFishCloudEntity) {
                this.selected = (GlassFishCloudEntity)anItem;
            } else {
                throw new IllegalArgumentException(
                        "Item is not GlassFish cloud instance");
            }
        }

        /**
         * Returns Selected GlassFish cloud instance.
         * <p/>
         * @return Selected GlassFish cloud instance or <code>null</code>
         *         if there is no selection.
         */
        @Override
        public Object getSelectedItem() {
            return this.selected;
        }

        /**
         * Returns the length of GlassFish cloud instances list.
         * <p/>
         * @return Length of GlassFish cloud instances list
         */
        @Override
        public int getSize() {
            return items.size();
        }

        /**
         * Returns the GlassFish cloud instance at the specified index.
         * <p/>
         * @param index the requested index
         * @return the GlassFish cloud instance at <code>index</code>
         */
        @Override
        public Object getElementAt(int index) {
            return items.get(index);
        }

        /**
         * Adds a listener to the list that's notified each time a change to the
         * data model occurs.
         * <p/>
         * @param listener <code>ListDataListener</code> to be added.
         */
        @Override
        public void addListDataListener(ListDataListener listener) {
            listeners.add(listener);
        }

        /**
         * Removes a listener from the list that's notified each time a change
         * to the data model occurs.
         * <p/>
         * @param listener <code>ListDataListener</code> to be removed.
         */
        @Override
        public void removeListDataListener(ListDataListener listener) {
            listeners.remove(listener);
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Initialize GlassFish cloud GUI component instance in constructor.
     * <p/>
     * Constructor helper containing shared code. do not use this method outside
     * constructors.
     * <p/>
     * @param instance GlassFish cloud user account GUI component instance
     *                 to be initialized.
     */
    private static void initInstance(
            GlassFishAccountWizardUserComponent instance) {
        instance.cloudComboBoxItems = new CloudComboBox(
                GlassFishCloudInstanceProvider.cloneCloudInstances());
        instance.initComponents();
        instance.glassFishCloudValid = instance.glassFishCloudValid().isValid();
        instance.displayNameValid = instance.displayNameValid().isValid();
        instance.accountValid = instance.accountValid().isValid();
        instance.userNameValid = instance.userNameValid().isValid();
        instance.userPasswordValid = instance.userPasswordValid().isValid();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////
    
    /** GlassFish cloud user account instance. */
    GlassFishAccountInstance instance;

    /** Validity of <code>displayName</code> field. */
    private boolean displayNameValid;

    /** Validity of <code>account</code> field. */
    private boolean accountValid;

    /** Validity of <code>userName</code> field. */
    private boolean userNameValid;

    /** Validity of <code>userPassword</code> field. */
    private boolean userPasswordValid;

    /** Validity of GlassFish Cloud selection field. */
    private boolean glassFishCloudValid;

    private CloudComboBox cloudComboBoxItems;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates new instance of GlassFish cloud user account GUI component.
     * <p/>
     * Form field handlers are set to wizard mode.
     */
    @SuppressWarnings("LeakingThisInConstructor") // initInstance(this);
    public GlassFishAccountWizardUserComponent() {
        this.instance = null;
        initInstance(this);
        accountTextField.getDocument()
                .addDocumentListener(initAccountValidateListener());
        userNameTextField.getDocument()
                .addDocumentListener(initUserNameValidateListener());
        userPasswordTextField.getDocument()
                .addDocumentListener(initUserPasswordValidateListener());
        cloudComboBox
                .addItemListener(initCloudSelectionValidateListener());

    }

    /**
     * Creates new instance of GlassFish cloud user account GUI component.
     * <p/>
     * Form field handlers are set to edit mode and fields are initialized using
     * <code>instance</code> content.
     * <p/>
     * @param instance GlassFish cloud user account instance to be modified.
     */
    @SuppressWarnings("LeakingThisInConstructor") // initInstance(this);
    public GlassFishAccountWizardUserComponent(
            GlassFishAccountInstance instance) {
        this.instance = instance;
        initInstance(this);
        nameLabel.setVisible(false);
        nameTextField.setVisible(false);
        accountTextField.getDocument()
                .addDocumentListener(initAccountUpdateListener());
        userNameTextField.getDocument()
                .addDocumentListener(initUserNameUpdateListener());
        userPasswordTextField.getDocument()
                .addDocumentListener(initUserPasswordUpdateListener());        
        cloudComboBox
                .addItemListener(initCloudSelectionUpdateListener());
    }

    ////////////////////////////////////////////////////////////////////////////
    // GUI Getters                                                            //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get user account display name.
     * <p/>
     * @return CPAS display name.
     */
    public String getDisplayName() {
        String text = nameTextField.getText();
        return text != null ? text.trim() : null;
    }

    /**
     * Get account name.
     * <p/>
     * @return Account name.
     */
    public String getAccount() {
        String text = accountTextField.getText();
        return text != null ? text.trim() : null;
    }

    /**
     * Get user name.
     * <p/>
     * @return User name.
     */
    public String getUserName() {
        String text = userNameTextField.getText();
        return text != null ? text.trim() : null;
    }

    /**
     * Get user password.
     * <p/>
     * Password processing should not remove leading and trailing spaces.
     * <p/>
     * @return User password.
     */
    public String getUserPassword() {
        return new String(userPasswordTextField.getPassword());
    }

    /**
     * Get selected GlassFish cloud entity name (unique identifier).
     * <p/>
     * @return Selected GlassFish cloud entity name or <code>null</code>
     *         if there is no entity selected.
     */
    public String getGlassFishCloudName() {
        GlassFishCloudEntity selected
                = (GlassFishCloudEntity)cloudComboBox.getSelectedItem();
        return selected != null ? selected.getName() : null;
    }

    /**
     * Get display name for GUI initialization.
     * <p/>
     * This method is used only in generated <code>initComponents</code> method.
     * <p/>
     * @return Value of name passed from cloud user account entity object
     *         or empty <code>String</code> when cloud entity object
     *         is <code>null</code>.
     */
    private String initDisplayName() {
        return instance != null ? instance.getName() : "";
    }

    /**
     * Get account for GUI initialization.
     * <p/>
     * This method is used only in generated <code>initComponents</code> method.
     * <p/>
     * @return Value of account passed from cloud user account entity object
     *         or empty <code>String</code> when cloud entity object
     *         is <code>null</code>.
     */
    private String initAccount() {
        return instance != null ? instance.getAcount() : "";
    }

    /**
     * Get user name for GUI initialization.
     * <p/>
     * This method is used only in generated <code>initComponents</code> method.
     * <p/>
     * @return Value of user name passed from cloud user account entity object
     *         or empty <code>String</code> when cloud entity object
     *         is <code>null</code>.
     */
    private String initUserName() {
        return instance != null ? instance.getUserName() : "";
    }

    /**
     * Get user password for GUI initialization.
     * <p/>
     * This method is used only in generated <code>initComponents</code> method.
     * <p/>
     * @return Value of user password passed from cloud user account
     *         entity object or empty <code>String</code> when cloud
     *         entity object is <code>null</code>.
     */
    private String initUserPassword() {
        return instance != null ? instance.getUserPassword() : "";
    }

    /**
     * Get CPAS host for GUI initialization.
     * <p/>
     * This method is used only in generated <code>initComponents</code> method.
     * <p/>
     * Uses cloud entity object encapsulated in cloud user account entity object
     * or null when there is no such object encapsulated.
     * <p/>
     * @return Value of CPAS host passed from cloud entity object
     *         or empty <code>String</code> when cloud entity object
     *         is <code>null</code>.
     */
    private String initCloudHost() {
        GlassFishCloud cloudInstance;
        if (instance != null) {
            cloudInstance = instance.getCloudEntity();
        } else {
            cloudInstance = null;
        }
        return cloudInstance != null ? cloudInstance.getHost() : "";
    }

    /**
     * Get CPAS host for GUI initialization.
     * <p/>
     * This method is used only in generated <code>initComponents</code> method.
     * <p/>
     * Uses cloud entity object encapsulated in cloud user account entity object
     * or null when there is no such object encapsulated.
     * <p/>
     * @return Value of CPAS host passed from cloud entity object
     *         or empty <code>String</code> when cloud entity object
     *         is <code>null</code>.
     */
    private String initCloudPort() {
        GlassFishCloud cloudInstance;
        if (instance != null) {
            cloudInstance = instance.getCloudEntity();
        } else {
            cloudInstance = null;
        }
        return cloudInstance != null
                ? Integer.toString(cloudInstance.getPort()) : "";
    }

    /**
     * Get CPAS selection in
     * {@see GlassFishAccountWizardUserComponent.CloudComboBox} model
     * for GUI initialization.
     * <p/>
     * This method is used only in generated <code>initComponents</code> method.
     * <p/>
     * Updates combo box selection to point to selected GlassFish cloud (CPAS).
     * Local <code>cloudComboBoxItems</code> attribute must be initialized
     * before this method is called (and also <code>initComponents</code>
     * method is called).
     */
    private CloudComboBox initCloudComboBox() {
        GlassFishCloud cloudInstance;
        if (instance != null) {
            cloudInstance = instance.getCloudEntity();
            String name = cloudInstance != null
                    ? cloudInstance.getName() : null;
            if (name != null) {
                for (GlassFishCloudEntity item : cloudComboBoxItems.items) {
                    if (name.equals(item.getName())) {
                        cloudComboBoxItems.selected = item;
                    }
                }
            }
        }
        return cloudComboBoxItems;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented abstract methods                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Enable modification of form elements.
     */
    @Override
    void enableModifications() {
        accountTextField.setEditable(true);
        userNameTextField.setEditable(true);
        userPasswordTextField.setEditable(true);
    }

    /**
     * Disable modification of form elements.
     */
    @Override
    void disableModifications() {
        accountTextField.setEditable(false);
        userNameTextField.setEditable(false);
        userPasswordTextField.setEditable(false);
    }

    /**
     * Validate component.
     */
    @Override
    boolean valid() {
        return glassFishCloudValid && displayNameValid && accountValid
                && userNameValid && userPasswordValid; 
    }

    ////////////////////////////////////////////////////////////////////////////
    // Event helper methods                                                   //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Process account field validation event.
     * <p/>
     * This is internal event processing helper and should not be used outside
     * listeners <code>processEvent</code> method.
     */
    void processAccountValidateEvent() {
        ValidationResult result = accountValid();
        accountValid = result.isValid();
        update(result);
    }

    /**
     * Process user password field validation event.
     * <p/>
     * This is internal event processing helper and should not be used outside
     * listeners <code>processEvent</code> method.
     */
    void processUserPasswordValidateEvent() {
        ValidationResult result = userPasswordValid();
        userPasswordValid = result.isValid();
        update(result);
    }

    /**
     * Process user password field validation event.
     * <p/>
     * This is internal event processing helper and should not be used outside
     * listeners <code>processEvent</code> method.
     */
    void processUserNameValidateEvent() {
        ValidationResult result = userNameValid();
        userNameValid = result.isValid();
        update(result);
    }

    /**
     * Process cloud selection combo box change and validation event.
     * <p/>
     * This is internal event processing helper and should not be used outside
     * listeners <code>itemStateChanged</code> method.
     * <p/>
     * @param event Event related to item selection or deselection.
     */
    @SuppressWarnings("fallthrough") // case ItemEvent.SELECTED
    void processGlassFishCloudChangeEvent(ItemEvent event) {
        // Validate combo box selection.
        ValidationResult result = glassFishCloudValid();
        glassFishCloudValid = result.isValid();
        update(result);
        // Update host and port fields.
        switch (event.getStateChange()) {
            case ItemEvent.SELECTED:
                GlassFishCloudEntity selectedCloud
                        = (GlassFishCloudEntity) cloudComboBoxItems
                        .getSelectedItem();
                if (selectedCloud != null) {
                    hostTextField.setText(selectedCloud.getHost());
                    portTextField.setText(
                            Integer.toString(selectedCloud.getPort()));
                    break;
                }
            // this case takes care of selectedCloud == null in from
            // previous case.
            case ItemEvent.DESELECTED:
                hostTextField.setText("");
                portTextField.setText("");
                break;
        }
    }

    /**
     * Create event listener to validate account field on the fly.
     */
    private DocumentListener initAccountValidateListener() {
        return new ComponentFieldListener() {
            @Override
            void processEvent() {
                processAccountValidateEvent();
            }
        };
    }

    /**
     * Create event listener to validate user name field on the fly.
     */
    private DocumentListener initUserNameValidateListener() {
        return new ComponentFieldListener() {
            @Override
            void processEvent() {
                processUserNameValidateEvent();
            }
        };
    }

    /**
     * Create event listener to validate user password field on the fly.
     */
    private DocumentListener initUserPasswordValidateListener() {
        return new ComponentFieldListener() {
            @Override
            void processEvent() {
                processUserPasswordValidateEvent();
            }
        };
    }

    /**
     * Create event listener to update host and port values depending on combo
     * box selection changes.
     */
    private ItemListener initCloudSelectionValidateListener() {
        return new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent event) {
                processGlassFishCloudChangeEvent(event);
            }
        };
    }

    /**
     * Create event listener to validate account field on the fly.
     */
    private DocumentListener initAccountUpdateListener() {
        return new ComponentFieldListener() {
            @Override
            void processEvent() {
                processAccountValidateEvent();
                if (accountValid && instance != null) {
                    instance.setAcount(getAccount());
                    GlassFishAccountInstanceProvider.persist(instance);
                }
            }
        };
    }

    /**
     * Create event listener to validate user name field on the fly.
     */
    private DocumentListener initUserNameUpdateListener() {
        return new ComponentFieldListener() {
            @Override
            void processEvent() {
                processUserNameValidateEvent();
                if (userNameValid && instance != null) {
                    instance.setUserName(getUserName());
                    GlassFishAccountInstanceProvider.persist(instance);
                }
            }
        };
    }

    /**
     * Create event listener to validate user password field on the fly.
     */
    private DocumentListener initUserPasswordUpdateListener() {
        return new ComponentFieldListener() {
            @Override
            void processEvent() {
                processUserPasswordValidateEvent();
                if (userPasswordValid && instance != null) {
                    instance.setUserPassword(getUserPassword());
                    GlassFishAccountInstanceProvider.persist(instance);
                }
            }
        };
    }

    /**
     * Create event listener to update host and port values depending on combo
     * box selection changes.
     */
    private ItemListener initCloudSelectionUpdateListener() {
        return new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent event) {
                processGlassFishCloudChangeEvent(event);
                if (glassFishCloudValid && instance != null) {
                    GlassFishCloud selectedCloud
                            = (GlassFishCloud) cloudComboBoxItems
                            .getSelectedItem();
                    GlassFishCloudInstance cloudInstance
                            = GlassFishCloudInstanceProvider
                            .getCloudInstance(selectedCloud.getName());
                    instance.setCloudEntity(cloudInstance);
                    GlassFishAccountInstanceProvider.persist(instance);
                }
                
            }
        };
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Set value of display name text field.
     * <p/>
     * @param name Display name text field to be set.
     */
    ValidationResult setNameTextField(String name) {
        nameTextField.setText(name);
        ValidationResult result = displayNameValid();
        displayNameValid = result.isValid();
        return result;
    }

    /**
     * Validate display name field.
     * <p/>
     * Display name field should be non empty string value containing at least
     * one non-whitespace character. Display name must be unique among
     * all registered GlassFish cloud instances.
     * Value of <code>validationError</code> is set to inform about field
     * status.
     * <p/>
     * @return <code>true</code> when displayName field is valid
     *         or <code>false</code> otherwise.
     */
    final ValidationResult displayNameValid() {
        String displayName = getDisplayName();
        if (displayName != null && displayName.length() > 0) {
            if (GlassFishCloudInstanceProvider
                    .containsCloudInstanceWithName(displayName)) {
                return new ValidationResult(false,
                        getMessage(GlassFishCloudWizardCpasComponent.class,
                        Bundle.USER_PANEL_ERROR_DISPLAY_NAME_DUPLICATED));
            }
            return new ValidationResult(true, null);
        } else {
            return new ValidationResult(false,
                    getMessage(GlassFishCloudWizardCpasComponent.class,
                    Bundle.USER_PANEL_ERROR_DISPLAY_NAME_EMPTY));
        }
    }

    /**
     * Validate account field.
     * <p/>
     * Account field should be non empty string value containing at least one
     * non-whitespace character.
     * Value of <code>validationError</code> is set to inform about field
     * status.
     * <p/>
     * @return <code>true</code> when account field is valid
     *         or <code>false</code> otherwise.
     */
    final ValidationResult accountValid() {
        String account = getAccount();
        if (account != null && account.length() >= 0) {
            return new ValidationResult(true, null);
        } else {
            return new ValidationResult(false,
                    getMessage(GlassFishCloudWizardCpasComponent.class,
                    Bundle.USER_PANEL_ERROR_ACCOUNT_EMPTY));
        }
    }

    /**
     * Validate userName field.
     * <p/>
     * User name field should be non empty string value containing at least one
     * non-whitespace character.
     * Value of <code>validationError</code> is set to inform about field
     * status.
     * <p/>
     * @return <code>true</code> when userName field is valid
     *         or <code>false</code> otherwise.
     */
    final ValidationResult userNameValid() {
        String userName = getUserName();
        if (userName != null && userName.length() >= 0) {
            return new ValidationResult(true, null);
        } else {
            return new ValidationResult(false,
                    getMessage(GlassFishCloudWizardCpasComponent.class,
                    Bundle.USER_PANEL_ERROR_USER_NAME_EMPTY));
        }
    }

    /**
     * Validate userPassword field.
     * <p/>
     * User password  field should be non empty string value containing at least
     * one character.
     * Value of <code>validationError</code> is set to inform about field
     * status.
     * <p/>
     * @return <code>true</code> when userPassworde field is valid
     *         or <code>false</code> otherwise.
     */
    final ValidationResult userPasswordValid() {
        String userPassword = getUserPassword();
        if (userPassword != null && userPassword.length() >= 0) {
            return new ValidationResult(true, null);
        } else {
            return new ValidationResult(false,
                    getMessage(GlassFishCloudWizardCpasComponent.class,
                    Bundle.USER_PANEL_ERROR_USER_PASSWORD_EMPTY));
        }
    }

    /**
     * Validate GlassFish Cloud selection.
     * <p/>
     * GlassFish Cloud combo box should contain selected value (can't be empty).
     * <p/>
     * @return <code>true</code> when GlassFish Cloud selection contains
     *         existing GlassFish Cloud or <code>false</code> when no cloud
     *         was selected.
     */
    final ValidationResult glassFishCloudValid() {
        if (cloudComboBox.getSelectedItem() != null) {
            return new ValidationResult(true, null);
        } else {
            return new ValidationResult(false,
                    getMessage(GlassFishCloudWizardCpasComponent.class,
                    Bundle.USER_PANEL_ERROR_CLOUD_EMPTY));
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Generated GUI code                                                     //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
        accountLabel = new javax.swing.JLabel();
        userNameLabel = new javax.swing.JLabel();
        userPasswordLabel = new javax.swing.JLabel();
        userNameTextField = new javax.swing.JTextField();
        accountTextField = new javax.swing.JTextField();
        cloudComboBox = new javax.swing.JComboBox();
        cloudLabel = new javax.swing.JLabel();
        userPasswordTextField = new javax.swing.JPasswordField();
        separator = new javax.swing.JSeparator();
        cloudHeader = new javax.swing.JLabel();
        hostLabel = new javax.swing.JLabel();
        hostTextField = new javax.swing.JTextField();
        portLabel = new javax.swing.JLabel();
        portTextField = new javax.swing.JTextField();
        acocuntHeader = new javax.swing.JLabel();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();

        accountLabel.setText(org.openide.util.NbBundle.getMessage(GlassFishAccountWizardUserComponent.class, "GlassFishAccountWizardUserComponent.accountLabel.text")); // NOI18N

        userNameLabel.setText(org.openide.util.NbBundle.getMessage(GlassFishAccountWizardUserComponent.class, "GlassFishAccountWizardUserComponent.userNameLabel.text")); // NOI18N

        userPasswordLabel.setText(org.openide.util.NbBundle.getMessage(GlassFishAccountWizardUserComponent.class, "GlassFishAccountWizardUserComponent.userPasswordLabel.text")); // NOI18N

        userNameTextField.setText(initUserName());

        accountTextField.setText(initAccount());
        accountTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                accountTextFieldActionPerformed(evt);
            }
        });

        cloudComboBox.setModel(initCloudComboBox());

        cloudLabel.setText(org.openide.util.NbBundle.getMessage(GlassFishAccountWizardUserComponent.class, "GlassFishAccountWizardUserComponent.cloudLabel.text")); // NOI18N

        userPasswordTextField.setText(initUserPassword());

        separator.setForeground(new java.awt.Color(0, 0, 0));

        cloudHeader.setText(org.openide.util.NbBundle.getMessage(GlassFishAccountWizardUserComponent.class, "GlassFishAccountWizardUserComponent.cloudHeader.text")); // NOI18N

        hostLabel.setText(org.openide.util.NbBundle.getMessage(GlassFishAccountWizardUserComponent.class, "GlassFishAccountWizardUserComponent.hostLabel.text")); // NOI18N

        hostTextField.setBackground(new java.awt.Color(238, 238, 238));
        hostTextField.setEditable(false);
        hostTextField.setText(initCloudHost());

        portLabel.setText(org.openide.util.NbBundle.getMessage(GlassFishAccountWizardUserComponent.class, "GlassFishAccountWizardUserComponent.portLabel.text")); // NOI18N

        portTextField.setBackground(new java.awt.Color(238, 238, 238));
        portTextField.setEditable(false);
        portTextField.setText(initCloudPort());

        acocuntHeader.setText(org.openide.util.NbBundle.getMessage(GlassFishAccountWizardUserComponent.class, "GlassFishAccountWizardUserComponent.acocuntHeader.text")); // NOI18N

        nameLabel.setText(org.openide.util.NbBundle.getMessage(GlassFishAccountWizardUserComponent.class, "GlassFishAccountWizardUserComponent.nameLabel.text")); // NOI18N

        nameTextField.setEditable(false);
        nameTextField.setText(initDisplayName());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(separator)
            .addComponent(cloudHeader, javax.swing.GroupLayout.DEFAULT_SIZE, 457, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(userPasswordLabel)
                    .addComponent(accountLabel)
                    .addComponent(userNameLabel)
                    .addComponent(cloudLabel)
                    .addComponent(hostLabel)
                    .addComponent(portLabel)
                    .addComponent(nameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(userPasswordTextField)
                    .addComponent(portTextField)
                    .addComponent(accountTextField)
                    .addComponent(cloudComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(nameTextField)
                    .addComponent(userNameTextField)
                    .addComponent(hostTextField)))
            .addComponent(acocuntHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(cloudHeader)
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nameLabel)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cloudLabel)
                    .addComponent(cloudComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hostLabel)
                    .addComponent(hostTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(portLabel)
                    .addComponent(portTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(acocuntHeader)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(accountLabel)
                    .addComponent(accountTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userNameLabel)
                    .addComponent(userNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userPasswordLabel)
                    .addComponent(userPasswordTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void accountTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_accountTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_accountTextFieldActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel accountLabel;
    private javax.swing.JTextField accountTextField;
    private javax.swing.JLabel acocuntHeader;
    private javax.swing.JComboBox cloudComboBox;
    private javax.swing.JLabel cloudHeader;
    private javax.swing.JLabel cloudLabel;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JTextField hostTextField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JLabel portLabel;
    private javax.swing.JTextField portTextField;
    private javax.swing.JSeparator separator;
    private javax.swing.JLabel userNameLabel;
    private javax.swing.JTextField userNameTextField;
    private javax.swing.JLabel userPasswordLabel;
    private javax.swing.JPasswordField userPasswordTextField;
    // End of variables declaration//GEN-END:variables
}
