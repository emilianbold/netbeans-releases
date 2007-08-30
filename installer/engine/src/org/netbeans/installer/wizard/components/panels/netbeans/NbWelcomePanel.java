/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 *
 *     "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.wizard.components.panels.netbeans;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.Installer;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.RegistryNode;
import org.netbeans.installer.product.RegistryType;
import org.netbeans.installer.product.components.Group;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.filters.AndFilter;
import org.netbeans.installer.product.filters.OrFilter;
import org.netbeans.installer.product.filters.ProductFilter;
import org.netbeans.installer.product.filters.RegistryFilter;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.helper.swing.NbiButton;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;
import org.netbeans.installer.wizard.components.panels.ErrorMessagePanel;
import org.netbeans.installer.wizard.components.panels.ErrorMessagePanel.ErrorMessagePanelSwingUi;
import org.netbeans.installer.wizard.components.panels.ErrorMessagePanel.ErrorMessagePanelUi;
import org.netbeans.installer.wizard.containers.SwingContainer;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;

/**
 *
 * @author Kirill Sorokin
 */
public class NbWelcomePanel extends ErrorMessagePanel {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private Registry bundledRegistry;
    private Registry defaultRegistry;
    
    private boolean registriesFiltered;
    private static BundleType type;
    public NbWelcomePanel() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
        
        setProperty(TEXT_PANE_CONTENT_TYPE_PROPERTY,
                DEFAULT_TEXT_PANE_CONTENT_TYPE);
        type = BundleType.getType(
                System.getProperty(WELCOME_PAGE_TYPE_PROPERTY));
        
        String header = DEFAULT_WELCOME_TEXT_HEADER +
                ResourceUtils.getString(NbWelcomePanel.class,
                WELCOME_TEXT_HEADER_APPENDING_PROPERTY + "." + type );
        
        setProperty(WELCOME_TEXT_HEADER_PROPERTY, header);
        setProperty(WELCOME_TEXT_GROUP_TEMPLATE_PROPERTY,
                DEFAULT_WELCOME_TEXT_GROUP_TEMPLATE);
        setProperty(WELCOME_TEXT_PRODUCT_INSTALLED_TEMPLATE_PROPERTY,
                DEFAULT_WELCOME_TEXT_PRODUCT_INSTALLED_TEMPLATE);
        setProperty(WELCOME_TEXT_PRODUCT_NOT_INSTALLED_TEMPLATE_PROPERTY,
                DEFAULT_WELCOME_TEXT_PRODUCT_NOT_INSTALLED_TEMPLATE);
        setProperty(WELCOME_TEXT_FOOTER_PROPERTY,
                DEFAULT_WELCOME_TEXT_FOOTER);
        setProperty(CUSTOMIZE_BUTTON_TEXT_PROPERTY,
                DEFAULT_CUSTOMIZE_BUTTON_TEXT);
        setProperty(INSTALLATION_SIZE_LABEL_TEXT_PROPERTY,
                DEFAULT_INSTALLATION_SIZE_LABEL_TEXT);
        
        setProperty(CUSTOMIZE_TITLE_PROPERTY,
                DEFAULT_CUSTOMIZE_TITLE);
        
        setProperty(MESSAGE_PROPERTY,
                DEFAULT_MESSAGE);
        setProperty(MESSAGE_INSTALL_PROPERTY,
                DEFAULT_MESSAGE_INSTALL);
        setProperty(MESSAGE_UNINSTALL_PROPERTY,
                DEFAULT_MESSAGE_UNINSTALL);
        setProperty(COMPONENT_DESCRIPTION_TEXT_PROPERTY,
                DEFAULT_COMPONENT_DESCRIPTION_TEXT);
        setProperty(COMPONENT_DESCRIPTION_CONTENT_TYPE_PROPERTY,
                DEFAULT_COMPONENT_DESCRIPTION_CONTENT_TYPE);
        setProperty(SIZES_LABEL_TEXT_PROPERTY,
                DEFAULT_SIZES_LABEL_TEXT);
        setProperty(SIZES_LABEL_TEXT_NO_DOWNLOAD_PROPERTY,
                DEFAULT_SIZES_LABEL_TEXT_NO_DOWNLOAD);
        setProperty(DEFAULT_INSTALLATION_SIZE_PROPERTY,
                DEFAULT_INSTALLATION_SIZE);
        setProperty(DEFAULT_DOWNLOAD_SIZE_PROPERTY,
                DEFAULT_DOWNLOAD_SIZE);
        setProperty(OK_BUTTON_TEXT_PROPERTY,
                DEFAULT_OK_BUTTON_TEXT);
        setProperty(CANCEL_BUTTON_TEXT_PROPERTY,
                DEFAULT_CANCEL_BUTTON_TEXT);
        setProperty(DEFAULT_COMPONENT_DESCRIPTION_PROPERTY,
                DEFAULT_DEFAULT_COMPONENT_DESCRIPTION);
        
        setProperty(ERROR_NO_CHANGES_PROPERTY,
                DEFAULT_ERROR_NO_CHANGES);
        setProperty(ERROR_NO_CHANGES_INSTALL_ONLY_PROPERTY,
                DEFAULT_ERROR_NO_CHANGES_INSTALL_ONLY);
        setProperty(ERROR_NO_CHANGES_UNINSTALL_ONLY_PROPERTY,
                DEFAULT_ERROR_NO_CHANGES_UNINSTALL_ONLY);
        setProperty(ERROR_REQUIREMENT_INSTALL_PROPERTY,
                DEFAULT_ERROR_REQUIREMENT_INSTALL);
        setProperty(ERROR_CONFLICT_INSTALL_PROPERTY,
                DEFAULT_ERROR_CONFLICT_INSTALL);
        setProperty(ERROR_REQUIREMENT_UNINSTALL_PROPERTY,
                DEFAULT_ERROR_REQUIREMENT_UNINSTALL);
        setProperty(ERROR_NO_ENOUGH_SPACE_TO_DOWNLOAD_PROPERTY,
                DEFAULT_ERROR_NO_ENOUGH_SPACE_TO_DOWNLOAD);
        setProperty(ERROR_NO_ENOUGH_SPACE_TO_EXTRACT_PROPERTY,
                DEFAULT_ERROR_NO_ENOUGH_SPACE_TO_EXTRACT);
        setProperty(ERROR_EVERYTHING_IS_INSTALLED_PROPERTY,
                DEFAULT_ERROR_EVERYTHING_IS_INSTALLED);
        
        // initialize the registries used on the panel - see the initialize() and
        // canExecute() method
        try {
            defaultRegistry = Registry.getInstance();
            bundledRegistry = new Registry();
            
            final String bundledRegistryUri = System.getProperty(
                    Registry.BUNDLED_PRODUCT_REGISTRY_URI_PROPERTY);
            if (bundledRegistryUri != null) {
                bundledRegistry.loadProductRegistry(bundledRegistryUri);
            } else {
                bundledRegistry.loadProductRegistry(
                        Registry.DEFAULT_BUNDLED_PRODUCT_REGISTRY_URI);
            }
        } catch (InitializationException e) {
            ErrorManager.notifyError("Cannot load bundled registry", e);
        }
    }
    
    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new NbWelcomePanelUi(this);
        }
        
        return wizardUi;
    }
    
    @Override
    public boolean canExecuteForward() {
        return canExecute();
    }
    
    @Override
    public boolean canExecuteBackward() {
        return canExecute();
    }
    
    @Override
    public void initialize() {
        super.initialize();
        
        if (registriesFiltered) {
            return;
        }
        
        // we need to apply additional filters to the components tree - filter out
        // the components which are not present in the bundled registry; if the
        // bundled registry contains only one element - registry root, this means
        // that we're running without any bundle, hence not filtering is required;
        // additionally, we should not be suggesting to install tomcat by default,
        // thus we should correct it's initial status
        if (bundledRegistry.getNodes().size() > 1) {
            for (Product product: defaultRegistry.getProducts()) {
                if (bundledRegistry.getProduct(
                        product.getUid(),
                        product.getVersion()) == null) {
                    product.setVisible(false);
                    
                    if (product.getStatus() == Status.TO_BE_INSTALLED) {
                        product.setStatus(Status.NOT_INSTALLED);
                    }
                } else if (product.getUid().equals("tomcat") &&
                        (product.getStatus() == Status.TO_BE_INSTALLED)) {
                    product.setStatus(Status.NOT_INSTALLED);
                }
            }
        }
        
        registriesFiltered = true;
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private boolean canExecute() {
        return bundledRegistry.getNodes().size() > 1;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static class NbWelcomePanelUi extends ErrorMessagePanelUi {
        protected NbWelcomePanel component;
        
        public NbWelcomePanelUi(NbWelcomePanel component) {
            super(component);
            
            this.component = component;
        }
        
        @Override
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new NbWelcomePanelSwingUi(component, container);
            }
            
            return super.getSwingUi(container);
        }
    }
    
    public static class NbWelcomePanelSwingUi extends ErrorMessagePanelSwingUi {
        protected NbWelcomePanel panel;
        
        private NbiTextPane textPane;
        private NbiButton customizeButton;
        private NbiLabel installationSizeLabel;
        
        private NbCustomizeSelectionDialog customizeDialog;
        
        private List<RegistryNode> registryNodes;
        
        private boolean everythingIsInstalled;
        
        ValidatingThread validatingThread;
        
        public NbWelcomePanelSwingUi(
                final NbWelcomePanel component,
                final SwingContainer container) {
            super(component, container);
            
            this.panel = component;
            
            registryNodes = new LinkedList<RegistryNode>();
            populateList(
                    registryNodes,
                    Registry.getInstance().getRegistryRoot());
            
            initComponents();
        }
        
        @Override
        public String getTitle() {
            return null; // the welcome page does not have a title
        }
        
        // protected ////////////////////////////////////////////////////////////////
        @Override
        protected void initializeContainer() {
            super.initializeContainer();
            
            container.getBackButton().setVisible(false);
        }
        
        @Override
        protected void initialize() {
            final StringBuilder welcomeText = new StringBuilder();
            welcomeText.append(panel.getProperty(WELCOME_TEXT_HEADER_PROPERTY));
            
            everythingIsInstalled = true;
            for (RegistryNode node: registryNodes) {
                if (node instanceof Product) {
                    final Product product = (Product) node;
                    
                    if (product.getStatus() == Status.INSTALLED) {
                        if(type.equals(BundleType.JAVAEE) ||
                                type.equals(BundleType.CUSTOMIZE)) {
                            welcomeText.append(StringUtils.format(
                                    panel.getProperty(WELCOME_TEXT_PRODUCT_INSTALLED_TEMPLATE_PROPERTY),
                                    node.getDisplayName()));
                        }
                    } else if (product.getStatus() == Status.TO_BE_INSTALLED) {
                        if(type.equals(BundleType.JAVAEE) ||
                                type.equals(BundleType.CUSTOMIZE)) {
                            welcomeText.append(StringUtils.format(
                                    panel.getProperty(WELCOME_TEXT_PRODUCT_NOT_INSTALLED_TEMPLATE_PROPERTY),
                                    node.getDisplayName()));
                        }
                        everythingIsInstalled = false;
                    } else if ((product.getStatus() == Status.NOT_INSTALLED)) {
                        everythingIsInstalled = false;
                    } else {
                        continue;
                    }
                } else if (node instanceof Group) {
                    final RegistryFilter filter = new AndFilter(
                            new ProductFilter(true),
                            new OrFilter(
                            new ProductFilter(Status.TO_BE_INSTALLED),
                            new ProductFilter(Status.INSTALLED)));
                    
                    if (node.hasChildren(filter)) {
                        if(type.equals(BundleType.JAVAEE) ||
                                type.equals(BundleType.CUSTOMIZE)) {
                            welcomeText.append(StringUtils.format(
                                    panel.getProperty(WELCOME_TEXT_GROUP_TEMPLATE_PROPERTY),
                                    node.getDisplayName()));
                        }
                    }
                }
            }
            
            welcomeText.append(panel.getProperty(WELCOME_TEXT_FOOTER_PROPERTY));
            
            textPane.setContentType(
                    panel.getProperty(TEXT_PANE_CONTENT_TYPE_PROPERTY));
            textPane.setText(welcomeText);
            
            customizeButton.setText(
                    panel.getProperty(CUSTOMIZE_BUTTON_TEXT_PROPERTY));
            
            long installationSize = 0;
            for (Product product: Registry.getInstance().getProductsToInstall()) {
                installationSize += product.getRequiredDiskSpace();
            }
            
            installationSizeLabel.setText(StringUtils.format(
                    panel.getProperty(INSTALLATION_SIZE_LABEL_TEXT_PROPERTY),
                    StringUtils.formatSize(installationSize)));
            
            super.initialize();
        }
        
        @Override
        protected String validateInput() {
            if (everythingIsInstalled) {
                customizeButton.setEnabled(false);
                installationSizeLabel.setVisible(false);
                
                return panel.getProperty(ERROR_EVERYTHING_IS_INSTALLED_PROPERTY);
            } else {
                customizeButton.setEnabled(true);
                installationSizeLabel.setVisible(true);
            }
            
            final List<Product> products =
                    Registry.getInstance().getProductsToInstall();
            
            if (products.size() == 0) {
                return panel.getProperty(ERROR_NO_CHANGES_INSTALL_ONLY_PROPERTY);
            }
            
            String template = panel.getProperty(
                    ERROR_NO_ENOUGH_SPACE_TO_EXTRACT_PROPERTY);
            for (Product product: products) {
                if (product.getRegistryType() == RegistryType.REMOTE) {
                    template = panel.getProperty(
                            ERROR_NO_ENOUGH_SPACE_TO_DOWNLOAD_PROPERTY);
                    break;
                }
            }
            
            try {
                final long availableSize = SystemUtils.getFreeSpace(
                        Installer.getInstance().getLocalDirectory());
                
                long requiredSize = 0;
                for (Product product: products) {
                    requiredSize += product.getDownloadSize();
                }
                requiredSize += REQUIRED_SPACE_ADDITION;
                
                if (availableSize < requiredSize) {
                    return StringUtils.format(
                            template,
                            Installer.getInstance().getLocalDirectory(),
                            StringUtils.formatSize(requiredSize - availableSize));
                }
            } catch (NativeException e) {
                ErrorManager.notifyError(
                        "Cannot check the free disk space",
                        e);
            }
            
            return null;
        }
        
        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            // textPane /////////////////////////////////////////////////////////////
            textPane = new NbiTextPane();
            
            // customizeButton //////////////////////////////////////////////////////
            customizeButton = new NbiButton();
            customizeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    customizeButtonPressed();
                }
            });
            
            // installationSizeLabel ////////////////////////////////////////////////
            installationSizeLabel = new NbiLabel();
            
            // this /////////////////////////////////////////////////////////////////
            add(textPane, new GridBagConstraints(
                    0, 0,                             // x, y
                    2, 1,                             // width, height
                    1.0, 1.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.BOTH,          // fill
                    new Insets(11, 11, 0, 11),        // padding
                    0, 0));                           // padx, pady - ???
            add(customizeButton, new GridBagConstraints(
                    0, 1,                             // x, y
                    1, 1,                             // width, height
                    1.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.LINE_START,    // anchor
                    GridBagConstraints.NONE,          // fill
                    new Insets(7, 11, 11, 0),         // padding
                    0, 0));                           // padx, pady - ???
            add(installationSizeLabel, new GridBagConstraints(
                    1, 1,                             // x, y
                    1, 1,                             // width, height
                    0.0, 0.0,                         // weight-x, weight-y
                    GridBagConstraints.CENTER,        // anchor
                    GridBagConstraints.HORIZONTAL,    // fill
                    new Insets(7, 11, 0, 11),         // padding
                    0, 0));                           // padx, pady - ???
            
            // platform-specific tweak //////////////////////////////////////////////
            if (SystemUtils.isMacOS()) {
                customizeButton.setOpaque(false);
            }
            BundleType type = BundleType.getType(
                    System.getProperty(WELCOME_PAGE_TYPE_PROPERTY));
            
            if(type.equals(BundleType.JAVAEE) ||
                    type.equals(BundleType.CUSTOMIZE)) {
                customizeButton.setVisible(true);
            } else {
                customizeButton.setVisible(false);
            }
        }
        
        private void customizeButtonPressed() {
            if (customizeDialog == null) {
                final Runnable callback = new Runnable() {
                    public void run() {
                        initialize();
                    }
                };
                
                customizeDialog = new NbCustomizeSelectionDialog(
                        panel,
                        callback,
                        registryNodes);
            }
            
            customizeDialog.setVisible(true);
            customizeDialog.requestFocus();
        }
        
        private void populateList(
                final List<RegistryNode> list,
                final RegistryNode parent) {
            final List<RegistryNode> groups = new LinkedList<RegistryNode>();
            
            for (RegistryNode node: parent.getChildren()) {
                if (!node.isVisible()) {
                    continue;
                }
                
                if (node instanceof Product) {
                    if (!SystemUtils.getCurrentPlatform().isCompatibleWith(
                            ((Product) node).getPlatforms())) {
                        continue;
                    }
                    
                    list.add(node);
                }
                
                if (node instanceof Group) {
                    if (node.hasChildren(new ProductFilter(true))) {
                        groups.add(node);
                    }
                }
            }
            
            for (RegistryNode node: groups) {
                list.add(node);
                populateList(list, node);
            }
        }
    }
    
    public Registry getBundledRegistry() {
        return bundledRegistry;
    }
    
    private enum BundleType {
        JAVASE("javase"),
        JAVAEE("javaee"),
        JAVAME("javame"),
        RUBY("ruby"),
        CND("cnd"),
        CUSTOMIZE("customize");
        
        private String name;
        private BundleType(String s) {
            this.name = s;
        }
        public static BundleType getType(String s) {
            if(s!=null) {
                for(BundleType type : BundleType.values())
                    if(type.toString().equals(s)) {
                    return type;
                    }
            }
            return CUSTOMIZE;
        }
        public String toString() {
            return name;
        }
    }
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.title");
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.description"); // NOI18N
    
    public static final String TEXT_PANE_CONTENT_TYPE_PROPERTY =
            "text.pane.content.type"; // NOI18N
    public static final String WELCOME_TEXT_HEADER_PROPERTY =
            "welcome.text.header"; // NOI18N
    public static final String WELCOME_TEXT_GROUP_TEMPLATE_PROPERTY =
            "welcome.text.group.template"; // NOI18N
    public static final String WELCOME_TEXT_PRODUCT_INSTALLED_TEMPLATE_PROPERTY =
            "welcome.text.product.installed.template"; // NOI18N
    public static final String WELCOME_TEXT_PRODUCT_NOT_INSTALLED_TEMPLATE_PROPERTY =
            "welcome.text.product.not.installed.template"; // NOI18N
    public static final String WELCOME_TEXT_FOOTER_PROPERTY =
            "welcome.text.footer"; // NOI18N
    public static final String CUSTOMIZE_BUTTON_TEXT_PROPERTY =
            "customize.button.text"; // NOI18N
    public static final String INSTALLATION_SIZE_LABEL_TEXT_PROPERTY =
            "installation.size.label.text"; // NOI18N
    
    public static final String DEFAULT_TEXT_PANE_CONTENT_TYPE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.text.pane.content.type"); // NOI18N
    public static final String DEFAULT_WELCOME_TEXT_HEADER =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.welcome.text.header"); // NOI18N
    public static final String WELCOME_TEXT_HEADER_APPENDING_PROPERTY =
            "NWP.welcome.text.header"; // NOI18N
    
    public static final String WELCOME_PAGE_TYPE_PROPERTY =
            "NWP.welcome.page.type";
    
    public static final String DEFAULT_WELCOME_TEXT_GROUP_TEMPLATE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.welcome.text.group.template"); // NOI18N
    public static final String DEFAULT_WELCOME_TEXT_PRODUCT_INSTALLED_TEMPLATE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.welcome.text.product.installed.template"); // NOI18N
    public static final String DEFAULT_WELCOME_TEXT_PRODUCT_NOT_INSTALLED_TEMPLATE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.welcome.text.product.not.installed.template"); // NOI18N
    public static final String DEFAULT_WELCOME_TEXT_FOOTER =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.welcome.text.footer"); // NOI18N
    public static final String DEFAULT_CUSTOMIZE_BUTTON_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.customize.button.text"); // NOI18N
    public static final String DEFAULT_INSTALLATION_SIZE_LABEL_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.installation.size.label.text"); // NOI18N
    
    public static final String CUSTOMIZE_TITLE_PROPERTY =
            "customize.title"; // NOI18N
    public static final String MESSAGE_PROPERTY =
            "message"; // NOI18N
    public static final String MESSAGE_INSTALL_PROPERTY =
            "message.install"; // NOI18N
    public static final String MESSAGE_UNINSTALL_PROPERTY =
            "message.uninstall"; // NOI18N
    public static final String COMPONENT_DESCRIPTION_TEXT_PROPERTY =
            "component.description.text"; // NOI18N
    public static final String COMPONENT_DESCRIPTION_CONTENT_TYPE_PROPERTY =
            "component.description.content.type"; // NOI18N
    public static final String SIZES_LABEL_TEXT_PROPERTY =
            "sizes.label.text"; // NOI18N
    public static final String SIZES_LABEL_TEXT_NO_DOWNLOAD_PROPERTY =
            "sizes.label.text.no.download"; // NOI18N
    public static final String DEFAULT_INSTALLATION_SIZE_PROPERTY =
            "default.installation.size"; // NOI18N
    public static final String DEFAULT_DOWNLOAD_SIZE_PROPERTY =
            "default.download.size"; // NOI18N
    public static final String OK_BUTTON_TEXT_PROPERTY =
            "ok.button.text"; // NOI18N
    public static final String CANCEL_BUTTON_TEXT_PROPERTY =
            "cancel.button.text"; // NOI18N
    public static final String DEFAULT_COMPONENT_DESCRIPTION_PROPERTY =
            "default.component.description";
    
    public static final String DEFAULT_CUSTOMIZE_TITLE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.customize.title"); // NOI18N
    public static final String DEFAULT_MESSAGE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.message.both"); // NOI18N
    public static final String DEFAULT_MESSAGE_INSTALL =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.message.install"); // NOI18N
    public static final String DEFAULT_MESSAGE_UNINSTALL =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.message.uninstall"); // NOI18N
    public static final String DEFAULT_COMPONENT_DESCRIPTION_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.component.description.text"); // NOI18N
    public static final String DEFAULT_COMPONENT_DESCRIPTION_CONTENT_TYPE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.component.description.content.type"); // NOI18N
    public static final String DEFAULT_SIZES_LABEL_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.sizes.label.text"); // NOI18N
    public static final String DEFAULT_SIZES_LABEL_TEXT_NO_DOWNLOAD =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.sizes.label.text.no.download"); // NOI18N
    public static final String DEFAULT_INSTALLATION_SIZE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.default.installation.size"); // NOI18N
    public static final String DEFAULT_DOWNLOAD_SIZE =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.default.download.size"); // NOI18N
    public static final String DEFAULT_OK_BUTTON_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.ok.button.text"); // NOI18N
    public static final String DEFAULT_CANCEL_BUTTON_TEXT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.cancel.button.text"); // NOI18N
    public static final String DEFAULT_DEFAULT_COMPONENT_DESCRIPTION =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.default.component.description"); // NOI18N
    
    public static final String ERROR_NO_CHANGES_PROPERTY =
            "error.no.changes.both"; // NOI18N
    public static final String ERROR_NO_CHANGES_INSTALL_ONLY_PROPERTY =
            "error.no.changes.install"; // NOI18N
    public static final String ERROR_NO_CHANGES_UNINSTALL_ONLY_PROPERTY =
            "error.no.changes.uninstall"; // NOI18N
    public static final String ERROR_REQUIREMENT_INSTALL_PROPERTY =
            "error.requirement.install"; // NOI18N
    public static final String ERROR_CONFLICT_INSTALL_PROPERTY =
            "error.conflict.install"; // NOI18N
    public static final String ERROR_REQUIREMENT_UNINSTALL_PROPERTY =
            "error.requirement.uninstall"; // NOI18N
    public static final String ERROR_NO_ENOUGH_SPACE_TO_DOWNLOAD_PROPERTY =
            "error.not.enough.space.to.download"; // NOI18N
    public static final String ERROR_NO_ENOUGH_SPACE_TO_EXTRACT_PROPERTY =
            "error.not.enough.space.to.extract"; // NOI18N
    public static final String ERROR_EVERYTHING_IS_INSTALLED_PROPERTY =
            "error.everything.is.installed"; // NOI18N
    
    public static final String DEFAULT_ERROR_NO_CHANGES =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.no.changes.both"); // NOI18N
    public static final String DEFAULT_ERROR_NO_CHANGES_INSTALL_ONLY =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.no.changes.install"); // NOI18N
    public static final String DEFAULT_ERROR_NO_CHANGES_UNINSTALL_ONLY =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.no.changes.uninstall"); // NOI18N
    public static final String DEFAULT_ERROR_REQUIREMENT_INSTALL =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.requirement.install"); // NOI18N
    public static final String DEFAULT_ERROR_CONFLICT_INSTALL =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.conflict.install"); // NOI18N
    public static final String DEFAULT_ERROR_REQUIREMENT_UNINSTALL =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.requirement.uninstall"); // NOI18N
    public static final String DEFAULT_ERROR_NO_ENOUGH_SPACE_TO_DOWNLOAD =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.not.enough.space.to.download"); // NOI18N
    public static final String DEFAULT_ERROR_NO_ENOUGH_SPACE_TO_EXTRACT =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.not.enough.space.to.extract"); // NOI18N
    public static final String DEFAULT_ERROR_EVERYTHING_IS_INSTALLED =
            ResourceUtils.getString(NbWelcomePanel.class,
            "NWP.error.everything.is.installed"); // NOI18N
    
    public static final long REQUIRED_SPACE_ADDITION =
            10L * 1024L * 1024L; // 10MB
}
