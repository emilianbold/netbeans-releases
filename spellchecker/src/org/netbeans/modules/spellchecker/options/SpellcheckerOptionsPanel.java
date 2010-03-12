/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.spellchecker.options;

import java.awt.Color;
import java.awt.Dialog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.spellchecker.DefaultLocaleQueryImplementation;
import org.netbeans.modules.spellchecker.DictionaryProviderImpl;
import org.netbeans.modules.spellchecker.options.DictionaryInstallerPanel.DictionaryDescription;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Jan Lahoda
 */
public class SpellcheckerOptionsPanel extends javax.swing.JPanel {
    
    private List<Locale> removedDictionaries = new ArrayList<Locale>();
    private List<DictionaryDescription> addedDictionaries = new ArrayList<DictionaryDescription>();

    private SpellcheckerOptionsPanelController c;
    private static final Icon errorIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/spellchecker/resources/error.gif"));
    
    /**
     * Creates new form SpellcheckerOptionsPanel
     */
    public SpellcheckerOptionsPanel(final SpellcheckerOptionsPanelController c) {
        initComponents();
        this.c = c;
        Color errorColor = UIManager.getColor("nb.errorForeground");
        
        if (errorColor == null) {
            errorColor = new Color(255, 0, 0);
        }
        
        errorText.setForeground(errorColor);
        
        JTextComponent editorComponent = (JTextComponent) defaultLocale.getEditor().getEditorComponent();
        final Document document = editorComponent.getDocument();
        
        document.addDocumentListener(new DocumentListener() {
            private void validate() {
                try {
                    String locale = document.getText(0, document.getLength());

                    if (locale.length() == 0) {
                        setError("Locale is empty");
                        return;
                    }

                    String[] components = locale.split("_");

                    if (components.length > 3) {
                        setError("Invalid locale");
                        return;
                    }

                    if (!Arrays.asList(Locale.getISOLanguages()).contains(components[0])) {
                        setError("Unknown language");
                        return;
                    }

                    if (components.length > 1) {
                        if (!Arrays.asList(Locale.getISOCountries()).contains(components[1])) {
                            setError("Unknown country");
                            return;
                        }

                        if (!Arrays.asList(Locale.getAvailableLocales()).contains(new Locale(components[0], components[1]))) {
                            setError("Unsupported locale");
                            return;
                        }
                    }

                    setError(null);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            public void insertUpdate(DocumentEvent e) {
                validate();
            }
            public void removeUpdate(DocumentEvent e) {
                validate();
            }
            public void changedUpdate(DocumentEvent e) {}
        });
    }

    private void setError(String error) {
        c.setValid(error == null);
        errorText.setText(error != null ? error : "");
        errorText.setIcon(error != null ? errorIcon : null);
    }
    
    public void update() {
        removedDictionaries.clear();
        addedDictionaries.clear();

        updateLocales();
        
        defaultLocale.setSelectedItem(DefaultLocaleQueryImplementation.getDefaultLocale());
    }

    private void updateLocales() {
        DefaultListModel model = new DefaultListModel();
        List<Locale> locales = new ArrayList<Locale>(Arrays.asList(DictionaryProviderImpl.getInstalledDictionariesLocales()));

        for (DictionaryDescription desc : addedDictionaries) {
            locales.add(desc.getLocale());
        }
        
        locales.removeAll(removedDictionaries);

        for (Locale l : locales) {
            model.addElement(l);
        }
        
        installedLocalesList.setModel(model);
    }
    
    public void commit() {
        //Add dictionaries:
        for (DictionaryDescription desc : addedDictionaries) {
            DictionaryInstallerPanel.doInstall(desc);
        }

        //Remove dictionaries:
        for (Locale remove : removedDictionaries) {
            DictionaryInstallerPanel.removeDictionary(remove);
        }
        
        Object selectedItem = defaultLocale.getSelectedItem();
        Locale selectedLocale = null;
        
        if (selectedItem instanceof Locale) {
            selectedLocale = (Locale) selectedItem;
        }
        
        if (selectedItem instanceof String) {
            String[] parsedComponents = ((String) selectedItem).split("_");
            String[] components = new String[] {"", "", ""};
            
            System.arraycopy(parsedComponents, 0, components, 0, parsedComponents.length);
            
            selectedLocale = new Locale(components[0], components[1], components[2]);
        }
        
        if (selectedLocale != null) {
            DefaultLocaleQueryImplementation.setDefaultLocale(selectedLocale);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        installedLocalesList = new javax.swing.JList();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        defaultLocale = new javax.swing.JComboBox();
        errorText = new javax.swing.JLabel();

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Dictionaries"));

        installedLocalesList.setModel(getInstalledDictionariesModel());
        installedLocalesList.setVisibleRowCount(4);
        jScrollPane1.setViewportView(installedLocalesList);

        org.openide.awt.Mnemonics.setLocalizedText(jButton4, "Add...");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton5, "Remove");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(118, 118, 118))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton5))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 68, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SpellcheckerOptionsPanel.class, "LBL_Default_Locale_Panel", new Object[] {}))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SpellcheckerOptionsPanel.class, "LBL_Default_Locale", new Object[] {})); // NOI18N

        defaultLocale.setEditable(true);
        defaultLocale.setModel(getLocaleModel());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(errorText)
                    .addComponent(defaultLocale, 0, 251, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(defaultLocale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(errorText)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        for (Object o : installedLocalesList.getSelectedValues()) {
            removedDictionaries.add((Locale) o);
        }
        updateLocales();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        DictionaryInstallerPanel panel = new DictionaryInstallerPanel();
        DialogDescriptor dd = new DialogDescriptor(panel, "Add Dictionary");
        Dialog d = DialogDisplayer.getDefault().createDialog(dd);

        d.setVisible(true);

        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            DictionaryDescription desc = panel.createDescription();

            addedDictionaries.add(desc);
            removedDictionaries.remove(desc.getLocale());
            updateLocales();
        }
    }//GEN-LAST:event_jButton4ActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox defaultLocale;
    private javax.swing.JLabel errorText;
    private javax.swing.JList installedLocalesList;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
    
    private ListModel getInstalledDictionariesModel() {
        DefaultListModel dlm = new DefaultListModel();

        for (Locale l : DictionaryProviderImpl.getInstalledDictionariesLocales()) {
            dlm.addElement(l);
        }

        return dlm;
    }

    private ComboBoxModel getLocaleModel() {
        DefaultComboBoxModel dlm = new DefaultComboBoxModel();
        List<Locale> locales = new ArrayList<Locale>(Arrays.asList(Locale.getAvailableLocales()));
        
        Collections.sort(locales, new LocaleComparator());
        
        for (Locale l : locales) {
            dlm.addElement(l);
        }
        
        return dlm;
    }
    
    private static class LocaleComparator implements Comparator<Locale> {
        
        public int compare(Locale o1, Locale o2) {
            return o1.toString().compareTo(o2.toString());
        }
        
    }
}
