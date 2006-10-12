/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.j2ee.persistence.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JSeparator;
import org.netbeans.api.project.libraries.LibrariesCustomizer;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.persistence.provider.DefaultProvider;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.wizard.library.PersistenceLibraryCustomizer;
import org.openide.util.NbBundle;

/**
 * A helper class for populating combo box with persistence providers.
 * Providers may be provided (no pun intended) by server in a container
 * managed environment or they might come from libraries.
 *
 * @author Libor Kotouc
 * @author Erno Mononen
 */
public class PersistenceProviderComboboxHelper {
    
    private final static String SEPARATOR = "PersistenceProviderComboboxHelper.SEPARATOR";
    private final static String EMPTY = "PersistenceProviderComboboxHelper.EMPTY";
    
    private PersistenceProviderComboboxHelper() {
    }
    
    private static class PersistenceProviderCellRenderer extends DefaultListCellRenderer {
        
        Provider defaultProvider;
        
        PersistenceProviderCellRenderer(Provider defaultProvider) {
            this.defaultProvider = defaultProvider;
        }
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            if (value instanceof Provider) {
                Provider provider = (Provider)value;
                String text = provider.getDisplayName();
                if (value.equals(defaultProvider) && (!(value instanceof DefaultProvider))) {
                    text += NbBundle.getMessage(PersistenceProviderComboboxHelper.class, "LBL_DEFAULT_PROVIDER");
                }
                setText(text);
                
            } else if (SEPARATOR.equals(value)) {
                JSeparator s = new JSeparator();
                s.setPreferredSize(new Dimension(s.getWidth(), 1));
                s.setForeground(Color.BLACK);
                return s;
                
            } else if (EMPTY.equals(value)) {
                setText(" ");
                
            } else if (value instanceof LibraryItem) {
                setText(((LibraryItem) value).getText());
                
            } else {
                setText(value != null ?  value.toString() : ""); // NOI18N
            }
            
            return this;
        }
        
    }
    
    /**
     * Populates given <code>providerCombo</code> with persistence providers. If
     * given <code>j2eeModuleProvider</code> is not null, supported providers from
     * respective server are also added. If it is null, only providers from libraries
     * are added. Items for adding and managing libraries are always included.
     * @param providerCombo
     * @param j2eeModuleProvider
     */
    public static void connect(final J2eeModuleProvider j2eeModuleProvider, final JComboBox providerCombo) {
        providerCombo.setEditable(false);
        initCombo(j2eeModuleProvider, providerCombo);
        // handling of <ENTER> key event
        providerCombo.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (KeyEvent.VK_ENTER == keyCode) {
                    Object selectedItem = providerCombo.getSelectedItem();
                    if (selectedItem instanceof LibraryItem) {
                        ((LibraryItem) selectedItem).performAction();
                        providerCombo.setPopupVisible(false);
                        e.consume();
                        initCombo(j2eeModuleProvider, providerCombo);
                    }
                }
            }
        });
        
        providerCombo.addActionListener(new ActionListener() {
            
            Object currentItem = providerCombo.getSelectedItem();
            int currentIndex = providerCombo.getSelectedIndex();
            
            public void actionPerformed(ActionEvent e) {
                Object selectedItem = providerCombo.getSelectedItem();
                // skipping of separator
                if (SEPARATOR.equals(selectedItem)) {
                    int selectedIndex = providerCombo.getSelectedIndex();
                    if (selectedIndex > currentIndex) {
                        currentIndex = selectedIndex + 1;
                        currentItem = providerCombo.getItemAt(currentIndex);
                    } else {
                        currentIndex = selectedIndex - 1;
                        currentItem = providerCombo.getItemAt(currentIndex);
                    }
                    providerCombo.setSelectedItem(currentItem);
                    // handling mouse click, see KeyEvent.getKeyModifiersText(e.getModifiers())
                } else if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
                    if (selectedItem instanceof LibraryItem) {
                        ((LibraryItem) selectedItem).performAction();
                        providerCombo.setPopupVisible(false);
                        initCombo(j2eeModuleProvider, providerCombo);
                    }
                } else {
                    currentItem = selectedItem;
                    currentIndex = providerCombo.getSelectedIndex();
                }
            }
        });
    }
    
    private static void initCombo(J2eeModuleProvider j2eeModuleProvider, JComboBox providerCombo) {
        
        DefaultComboBoxModel providers = new DefaultComboBoxModel();
        final Provider[] defProvider = new Provider[] { ProviderUtil.DEFAULT_PROVIDER };
        
        if (j2eeModuleProvider != null) {
            
            J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(j2eeModuleProvider.getServerInstanceID());
            addPersistenceProvider(providers, platform,
                    ProviderUtil.HIBERNATE_PROVIDER, "hibernatePersistenceProviderIsDefault", defProvider); // NOI18N
            addPersistenceProvider(providers, platform,
                    ProviderUtil.TOPLINK_PROVIDER, "toplinkPersistenceProviderIsDefault", defProvider);// NOI18N
            addPersistenceProvider(providers, platform,
                    ProviderUtil.KODO_PROVIDER, "kodoPersistenceProviderIsDefault", defProvider); // NOI18N
            
            if (defProvider[0] == ProviderUtil.DEFAULT_PROVIDER 
                    && providers.getSize() == 0
                    && supportsDefaultProvider(platform, j2eeModuleProvider)) {
                
                providers.addElement(defProvider[0]);
            }
        }
        
        addProvidersFromLibraries(providers);
        
        providerCombo.setModel(providers);
        providerCombo.addItem(SEPARATOR);
        providerCombo.addItem(new NewPersistenceLibraryItem());
        providerCombo.addItem(new ManageLibrariesItem());
        providerCombo.setRenderer(new PersistenceProviderCellRenderer(defProvider[0]));
        providerCombo.setSelectedIndex(0);
    }
    
    /**
     *@return true if the given platform supports default persistence provider 
     * for the given module provider's module type.
     */
    private static boolean supportsDefaultProvider(J2eePlatform platform, J2eeModuleProvider j2eeModuleProvider){

        if (platform == null){
            // server probably not registered, can't resolve whether default provider is supported (see #79856)
            return false;
        }
        
        Set<String> supportedVersions = 
                platform.getSupportedSpecVersions(j2eeModuleProvider.getJ2eeModule().getModuleType());
        
        return supportedVersions.contains(J2eeModule.JAVA_EE_5);
        
    }
    
    /**
     * Adds persistence providers found from libraries to the given model.
     */
    private static void addProvidersFromLibraries(DefaultComboBoxModel model){
        for (Provider each : ProviderUtil.getProvidersFromLibraries()){
            boolean found = false;
            for (int i = 0; i < model.getSize(); i++) {
                Object elem = model.getElementAt(i);
                if (elem instanceof Provider && each.equals(elem)){
                    found = true;
                    break;
                }
            }
            if (!found){
                model.addElement(each);
            }
        }
    }
    
    private static void addPersistenceProvider(
            DefaultComboBoxModel providers,
            J2eePlatform platform,
            Provider provider,
            String providerIsDefaultProperty,
            Provider[] defProvider) {
        
        if (platform != null && platform.isToolSupported(provider.getProviderClass())) {
            if (platform.isToolSupported(providerIsDefaultProperty)) {
                providers.insertElementAt(provider, 0);
                defProvider[0] = provider;
            } else {
                providers.addElement(provider);
            }
        }
    }
    
    
    public static interface LibraryItem {
        String getText();
        void performAction();
    }
    
    private static class NewPersistenceLibraryItem implements LibraryItem {
        public String getText() {
            return NbBundle.getMessage(PersistenceProviderComboboxHelper.class, "LBL_NewPersistenceLibrary");
        }
        public void performAction() {
            PersistenceLibraryCustomizer.showCustomizer();
        }
    }
    
    private static class ManageLibrariesItem implements LibraryItem {
        public String getText() {
            return NbBundle.getMessage(PersistenceProviderComboboxHelper.class, "LBL_ManageLibraries");
        }
        public void performAction() {
            LibrariesCustomizer.showCustomizer(null);
        }
    }
    
}
