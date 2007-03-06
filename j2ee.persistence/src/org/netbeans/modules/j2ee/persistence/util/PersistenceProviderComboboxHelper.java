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
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JSeparator;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.LibrariesCustomizer;
import org.netbeans.modules.j2ee.persistence.provider.DefaultProvider;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.spi.provider.PersistenceProviderSupplier;
import org.netbeans.modules.j2ee.persistence.wizard.library.PersistenceLibraryCustomizer;
import org.netbeans.modules.j2ee.persistence.wizard.library.PersistenceLibrarySupport;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * A helper class for populating combo box with persistence providers.
 * Providers may be provided (no pun intended) by server in a container
 * managed environment or they might come from libraries.
 *
 * @author Libor Kotouc
 * @author Erno Mononen
 */
public final class PersistenceProviderComboboxHelper {
    
    private final static String SEPARATOR = "PersistenceProviderComboboxHelper.SEPARATOR";
    private final static String EMPTY = "PersistenceProviderComboboxHelper.EMPTY";

    private final PersistenceProviderSupplier providerSupplier;

    /**
     * Creates a new PersistenceProviderComboboxHelper. 
     * @param project the current project. Must have an implementation of 
     * the PersistenceProviderSupplier in its lookup.
     * @throws IllegalArgumentException if the project did not have an implementation of 
     * the PersistenceProviderSupplier in its lookup.
     */ 
    public PersistenceProviderComboboxHelper(Project project) {
        Parameters.notNull("project", project);
        
        PersistenceProviderSupplier aProviderSupplier =project.getLookup().lookup(PersistenceProviderSupplier.class); 
        
        if (aProviderSupplier == null){
            // a java se project
            aProviderSupplier = new DefaultPersistenceProviderSupplier();
        }
        
        this.providerSupplier = aProviderSupplier;
    }
    
    /**
     * Populates the given <code>providerCombo</code> with persistence providers. Supported 
     * providers from the project's server (if it had one) are also added. If the project
     * doesn't have a server, only providers from libraries
     * are added. Items for adding and managing libraries are always included.
     * @param providerCombo the combo box to be populated.
     */
    public void connect(final JComboBox providerCombo) {
        providerCombo.setEditable(false);
        initCombo(providerCombo);
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
                        initCombo(providerCombo);
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
                        initCombo(providerCombo);
                    }
                } else {
                    currentItem = selectedItem;
                    currentIndex = providerCombo.getSelectedIndex();
                }
            }
        });
    }
    
    private void initCombo(JComboBox providerCombo) {
        
        DefaultComboBoxModel providers = new DefaultComboBoxModel();
        
        for(Provider each : providerSupplier.getSupportedProviders()){
           providers.addElement(each);
        }

        if (providers.getSize() == 0 && providerSupplier.supportsDefaultProvider()){
            providers.addElement(ProviderUtil.DEFAULT_PROVIDER);
        }
        
        addProvidersFromLibraries(providers);
        
        providerCombo.setModel(providers);
        providerCombo.addItem(SEPARATOR);
        providerCombo.addItem(new NewPersistenceLibraryItem());
        providerCombo.addItem(new ManageLibrariesItem());
        providerCombo.setRenderer(new PersistenceProviderCellRenderer((Provider)providers.getElementAt(0)));
        providerCombo.setSelectedIndex(0);
    }
    
    
    /**
     * Adds persistence providers found from libraries to the given model.
     */
    private void addProvidersFromLibraries(DefaultComboBoxModel model){
        for (Provider each : PersistenceLibrarySupport.getProvidersFromLibraries()){
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
     * An implementation of the PersistenceProviderSupplier that returns an empty list for supported
     * providers and doesn't support a default provider. Used when an implementation of 
     * the PersistenceProviderSupplier can't be found in the project lookup (as is the case
     * for instance for Java SE projects).
     */ 
    private static class DefaultPersistenceProviderSupplier implements PersistenceProviderSupplier{
        
        public List<Provider> getSupportedProviders() {
            return Collections.<Provider>emptyList();
        }

        public boolean supportsDefaultProvider() {
            return false;
        }
}
}
