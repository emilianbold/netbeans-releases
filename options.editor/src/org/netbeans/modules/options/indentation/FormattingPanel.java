/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.options.indentation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;

/**
 *
 * @author Dusan Balek
 */
public class FormattingPanel extends JPanel implements ActionListener, PropertyChangeListener {
    
    private static final String ALL_LANGUAGES_PREVIEW_MIME_TYPE = "text/xml"; //NOI18N
    
    private FormattingPanelController fopControler;
    private OptionsPanelController simplePanelController = new IndentationPanelController();
    private Map<String, JComponent> categoryName2Components;
    private Iterable<? extends OptionsPanelController> controllers;
    
    /** Creates new form FormattingPanel */
    FormattingPanel(FormattingPanelController fopControler) {
        this.fopControler = fopControler;

        initComponents();
        
        if( "Windows".equals(UIManager.getLookAndFeel().getID()) ) //NOI18N
            setOpaque( false );

        // Don't highlight caret row 
        previewPane.putClientProperty(
            "HighlightsLayerExcludes", // NOI18N
            "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$" // NOI18N
        );
        previewPane.setDoubleBuffered(true);        

        // initialize mime types and all the controllers and their components;
        // it's important to do this here to satisfy the logic of OptionsPanelController,
        // which says that getComponent is called before anything else (specifically update)
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement(""); //NOI18N
        simplePanelController.getComponent(fopControler.getLookup(ALL_LANGUAGES_PREVIEW_MIME_TYPE, previewPane));
        for (String mimeType : fopControler.getMimeTypes()) {
            model.addElement(mimeType);
            Lookup l = fopControler.getLookup(mimeType, previewPane);
            for(OptionsPanelController opc : fopControler.getControllers(mimeType)) {
                JComponent c = opc.getComponent(l);
            }
        }
        languageCombo.setModel(model);        
        ListCellRenderer renderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof String) {
                    value = ((String)value).length() > 0
                            ? EditorSettings.getDefault().getLanguageName((String)value)
                            : org.openide.util.NbBundle.getMessage(FormattingPanel.class, "LBL_AllLanguages"); //NOI18N                                
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }            
        };
        languageCombo.setRenderer(renderer);
        languageCombo.addActionListener(this);
        categoryCombo.addActionListener(this);

        // Pre-select a language
        JTextComponent pane = EditorRegistry.lastFocusedComponent();
        String preSelectMimeType = pane != null ? (String)pane.getDocument().getProperty("mimeType") : ""; // NOI18N
        languageCombo.setSelectedItem(preSelectMimeType);
        if (preSelectMimeType != languageCombo.getSelectedItem()) {
            languageCombo.setSelectedIndex(0);
        }
    }
    
    void load() {
        simplePanelController.update();
        for (String mimeType : fopControler.getMimeTypes()) {
            for (OptionsPanelController controller : fopControler.getControllers(mimeType)) {
                controller.update();
            }
        }
    }
    
    void store() {
        simplePanelController.applyChanges();
        for (String mimeType : fopControler.getMimeTypes()) {
            for (OptionsPanelController controller : fopControler.getControllers(mimeType)) {
                controller.applyChanges();
            }
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        optionsPanel = new javax.swing.JPanel();
        languageLabel = new javax.swing.JLabel();
        languageCombo = new javax.swing.JComboBox();
        categoryLabel = new javax.swing.JLabel();
        categoryCombo = new javax.swing.JComboBox();
        categoryPanel = new javax.swing.JPanel();
        previewPanel = new javax.swing.JPanel();
        previewLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        previewPane = new javax.swing.JEditorPane();

        setLayout(new java.awt.GridBagLayout());

        optionsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 8));
        optionsPanel.setOpaque(false);

        languageLabel.setLabelFor(languageCombo);
        org.openide.awt.Mnemonics.setLocalizedText(languageLabel, org.openide.util.NbBundle.getMessage(FormattingPanel.class, "LBL_Language")); // NOI18N

        languageCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        categoryLabel.setLabelFor(categoryCombo);
        org.openide.awt.Mnemonics.setLocalizedText(categoryLabel, org.openide.util.NbBundle.getMessage(FormattingPanel.class, "LBL_Category")); // NOI18N

        categoryCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        categoryPanel.setOpaque(false);
        categoryPanel.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout optionsPanelLayout = new org.jdesktop.layout.GroupLayout(optionsPanel);
        optionsPanel.setLayout(optionsPanelLayout);
        optionsPanelLayout.setHorizontalGroup(
            optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(optionsPanelLayout.createSequentialGroup()
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, categoryPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, optionsPanelLayout.createSequentialGroup()
                        .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(categoryLabel)
                            .add(languageLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(languageCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(categoryCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        optionsPanelLayout.linkSize(new java.awt.Component[] {categoryCombo, languageCombo}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        optionsPanelLayout.setVerticalGroup(
            optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(optionsPanelLayout.createSequentialGroup()
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(languageLabel)
                    .add(languageCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(categoryLabel)
                    .add(categoryCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(categoryPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        add(optionsPanel, gridBagConstraints);

        previewPanel.setMinimumSize(new java.awt.Dimension(150, 100));
        previewPanel.setOpaque(false);
        previewPanel.setPreferredSize(new java.awt.Dimension(150, 100));
        previewPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(previewLabel, org.openide.util.NbBundle.getMessage(FormattingPanel.class, "LBL_Preview")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        previewPanel.add(previewLabel, gridBagConstraints);

        jScrollPane1.setDoubleBuffered(true);

        previewPane.setEditable(false);
        jScrollPane1.setViewportView(previewPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        previewPanel.add(jScrollPane1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(previewPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox categoryCombo;
    private javax.swing.JLabel categoryLabel;
    private javax.swing.JPanel categoryPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox languageCombo;
    private javax.swing.JLabel languageLabel;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JLabel previewLabel;
    private javax.swing.JEditorPane previewPane;
    private javax.swing.JPanel previewPanel;
    // End of variables declaration//GEN-END:variables
 
    // Change in the combos
    public void actionPerformed(ActionEvent e) {
        if (languageCombo == e.getSource()) {
            if (controllers != null) {
                for (OptionsPanelController controller : controllers) {
                    controller.removePropertyChangeListener(this);
                }
            }
            
            String mimeType = (String)languageCombo.getSelectedItem();
            DefaultComboBoxModel model;

            if (mimeType.length() == 0) {
                EditorKit kit = CloneableEditorSupport.getEditorKit(ALL_LANGUAGES_PREVIEW_MIME_TYPE);
                previewPane.setEditorKit(kit);

                controllers = Collections.singletonList(simplePanelController);
                simplePanelController.addPropertyChangeListener(this);

                categoryName2Components = new LinkedHashMap<String, JComponent>();
                JComponent component = simplePanelController.getComponent(fopControler.getLookup(ALL_LANGUAGES_PREVIEW_MIME_TYPE, previewPane));
                String categoryName = component.getName();
                categoryName2Components.put(categoryName, component);

                model = new DefaultComboBoxModel();
                model.addElement(categoryName);
            } else {
                EditorKit kit = CloneableEditorSupport.getEditorKit(mimeType);
                previewPane.setEditorKit(kit);
            
                controllers = fopControler.getControllers(mimeType);
                categoryName2Components = new LinkedHashMap<String, JComponent>();
                model = new DefaultComboBoxModel();
                for (OptionsPanelController controller : controllers) {
                    controller.addPropertyChangeListener(this);
                    JComponent component = controller.getComponent(fopControler.getLookup(mimeType, previewPane));
                    String categoryName = component.getName();
                    categoryName2Components.put(categoryName, component);
                    model.addElement(categoryName);
                }
            }
            
            categoryCombo.setModel(model);
            categoryCombo.setSelectedIndex(0);
        } else if (categoryCombo == e.getSource()) {
            categoryPanel.setVisible(false);
            categoryPanel.removeAll();
            JComponent component = categoryName2Components.get(categoryCombo.getSelectedItem());
            categoryPanel.add(component, BorderLayout.CENTER);
            categoryPanel.setVisible(true);  
        }
    }
        
    // Change in some of the subpanels
    public void propertyChange(PropertyChangeEvent evt) {
        // Notify the main controler that the page has changed
        fopControler.changed();
    }
}
