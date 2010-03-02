/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.versioning.util.options;

import java.awt.Component;
import org.netbeans.modules.options.colors.spi.FontsColorsController;
import org.netbeans.modules.options.colors.ColorModel;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.*;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.modules.versioning.util.OptionsPanelColorProvider;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 * 
 * @author Maros Sandor, Ondra Vrabec
 */
public class AnnotationColorsPanel extends javax.swing.JPanel implements ActionListener, FontsColorsController, PropertyChangeListener {

    private boolean listen;
    private final HashMap<OptionsPanelColorProvider, VersioningSystemColors>  vcsColors;
    private final Lookup.Result<OptionsPanelColorProvider> result;
    private final LookupListener vcsListener;
    private boolean changed;
    
    public AnnotationColorsPanel() {
        initComponents ();

        setName(NbBundle.getMessage(this.getClass(), "CTL_AnnotationColorsPanel.title")); //NOI18N

        result = Lookup.getDefault().lookupResult(OptionsPanelColorProvider.class);
        vcsColors = new HashMap<OptionsPanelColorProvider, VersioningSystemColors> ();
        versioningSystemsList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                updateColorList();
            }
        });
        result.addLookupListener(WeakListeners.create(
            LookupListener.class,
            vcsListener = new LookupListener() {
                public void resultChanged(LookupEvent ev) {
                    initSystems();
                }
            }, result
        ));
        initSystems();

        ColorComboBox.init (cbBackground);
        lCategories.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
        lCategories.setVisibleRowCount (6);
        lCategories.addListSelectionListener (new ListSelectionListener() {
            public void valueChanged (ListSelectionEvent e) {
                if (!listen) return;
                refreshUI ();
            }
        });
        lCategories.setCellRenderer (new CategoryRenderer());
        cbBackground.addActionListener (this);
        btnResetToDefaults.addActionListener (this);
        ((JComponent)cbBackground.getEditor()).addPropertyChangeListener (this);
        versioningSystemsList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                setComponentOrientation(list.getComponentOrientation());
                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                }
                setText((String) ((OptionsPanelColorProvider) value).getName());

                setEnabled(list.isEnabled());
                setFont(list.getFont());
                setBorder(cellHasFocus ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder); //NOI18N
                return this;
            }
        });
        listen = true;
    }
    
    public void actionPerformed (ActionEvent evt) {
        if (!listen) return;
        if (evt.getSource() == btnResetToDefaults) {
            resetToDefaults();
            refreshUI();
        } else {
            updateData();
            changed = true;
        }
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        if (!listen) return;
        if (ColorComboBox.PROP_COLOR.equals(evt.getPropertyName())) {
            updateData ();
            changed = true;
        }
    }
    
    public void update(ColorModel colorModel) {
        if (!listen) return;
        listen = false;
        refreshUI ();	
        listen = true;
        changed = false;
    }
    
    public void cancel () {
        changed = false;
        vcsColors.clear();
    }
    
    public void applyChanges() {
        for (Map.Entry<OptionsPanelColorProvider, VersioningSystemColors> e : vcsColors.entrySet()) {
            e.getValue().saveColors();
        }
        vcsColors.clear();
    }
    
    public boolean isChanged () {
        return changed;
    }
    
    public void setCurrentProfile (String currentProfile) {
        refreshUI ();
    }

    public void deleteProfile (String profile) {
    }
    
    public JComponent getComponent() {
        return this;
    }
        
    // other methods ...........................................................
    
    private void updateData () {
        if (versioningSystemsList.getSelectedValue() == null) return;
        int index = lCategories.getSelectedIndex();
        if (index < 0) return;
        Color color = ColorComboBox.getColor(cbBackground);
        VersioningSystemColors colors = vcsColors.get((OptionsPanelColorProvider)versioningSystemsList.getSelectedValue());
        colors.setColor(index, color);
    }
    
    private void refreshUI () {
        if (versioningSystemsList.getSelectedValue() == null) {
            return;
        }
        int index = lCategories.getSelectedIndex();
        if (index < 0) {
            cbBackground.setEnabled(false);
            return;
        }
        cbBackground.setEnabled(true);
        
        List<AttributeSet> categories = getCategories();
	AttributeSet category = categories.get(lCategories.getSelectedIndex());
        
        listen = false;
        // set values
        ColorComboBox.setColor(cbBackground, (Color) category.getAttribute(StyleConstants.Background));
        listen = true;
    }
    
    private List<AttributeSet> getCategories() {
        OptionsPanelColorProvider provider = (OptionsPanelColorProvider)versioningSystemsList.getSelectedValue();
        VersioningSystemColors colors = vcsColors.get(provider);
        if (colors == null) {
            colors = new VersioningSystemColors(provider);
            vcsColors.put(provider, colors);
            updateColorList();
        }
        return colors.getColorAttributes();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        versioningSystemsList = new javax.swing.JList();
        jLabel2 = new javax.swing.JLabel();
        containerPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lCategories = new javax.swing.JList();
        jLabel3 = new javax.swing.JLabel();
        cbBackground = new javax.swing.JComboBox();
        btnResetToDefaults = new javax.swing.JButton();

        versioningSystemsList.setModel(new DefaultListModel());
        versioningSystemsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        versioningSystemsList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                versioningSystemsListValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(versioningSystemsList);

        jLabel2.setLabelFor(versioningSystemsList);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getBundle(AnnotationColorsPanel.class).getString("AnnotationColorsPanel.jLabel2.text")); // NOI18N

        jLabel1.setLabelFor(lCategories);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(AnnotationColorsPanel.class, "AnnotationColorsPanel.jLabel1.text")); // NOI18N

        lCategories.setMinimumSize(new java.awt.Dimension(100, 0));
        jScrollPane1.setViewportView(lCategories);

        jLabel3.setLabelFor(cbBackground);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(AnnotationColorsPanel.class, "AnnotationColorsPanel.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnResetToDefaults, org.openide.util.NbBundle.getMessage(AnnotationColorsPanel.class, "AnnotationColorsPanel.btnResetToDefaults.text")); // NOI18N
        btnResetToDefaults.setToolTipText(org.openide.util.NbBundle.getMessage(AnnotationColorsPanel.class, "AnnotationColorsPanel.btnResetToDefaults.TTtext")); // NOI18N

        org.jdesktop.layout.GroupLayout containerPanelLayout = new org.jdesktop.layout.GroupLayout(containerPanel);
        containerPanel.setLayout(containerPanelLayout);
        containerPanelLayout.setHorizontalGroup(
            containerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(containerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(containerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(containerPanelLayout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE)
                        .add(18, 18, 18)
                        .add(containerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(containerPanelLayout.createSequentialGroup()
                                .add(jLabel3)
                                .add(12, 12, 12)
                                .add(cbBackground, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 215, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(btnResetToDefaults))))
                .addContainerGap())
        );
        containerPanelLayout.setVerticalGroup(
            containerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(containerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(containerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(containerPanelLayout.createSequentialGroup()
                        .add(containerPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel3)
                            .add(cbBackground, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(btnResetToDefaults)
                        .addContainerGap(328, Short.MAX_VALUE))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane2, 0, 0, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(containerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(12, 12, 12)
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(containerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void versioningSystemsListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_versioningSystemsListValueChanged
        
}//GEN-LAST:event_versioningSystemsListValueChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnResetToDefaults;
    private javax.swing.JComboBox cbBackground;
    private javax.swing.JPanel containerPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList lCategories;
    private javax.swing.JList versioningSystemsList;
    // End of variables declaration//GEN-END:variables

    private void initSystems () {
        final Collection<? extends OptionsPanelColorProvider> providers = result.allInstances();
        Runnable inAWT = new Runnable() {
            public void run() {
                ((DefaultListModel) versioningSystemsList.getModel()).removeAllElements();
                for (OptionsPanelColorProvider provider : providers) {
                    ((DefaultListModel) versioningSystemsList.getModel()).addElement(provider);
                }
                if (versioningSystemsList.getModel().getSize() >= 1) {
                    versioningSystemsList.setSelectedIndex(0);
                }
            }
        };
        if (EventQueue.isDispatchThread()) {
            inAWT.run();
        } else {
            EventQueue.invokeLater(inAWT);
        }
    }

    private void updateColorList() {
        if (versioningSystemsList.getSelectedValue() == null) {
            lCategories.setEnabled(false);
            lCategories.setListData(new Object[0]);
            return;
        }
        lCategories.setEnabled(true);
        lCategories.setListData(new Vector<AttributeSet>(getCategories()));
        lCategories.setSelectedIndex(0);
    }

    private void resetToDefaults() {
        OptionsPanelColorProvider provider = (OptionsPanelColorProvider)versioningSystemsList.getSelectedValue();
        VersioningSystemColors colors = vcsColors.get(provider);
        if (colors != null) {
            colors.resetToDefaults();
            changed = true;
        }
    }

    private static class VersioningSystemColors {
        private final Map<String, Color[]> colors;
        private boolean changed;
        private ArrayList<AttributeSet> colorAttributes;
        private final OptionsPanelColorProvider provider;

        public VersioningSystemColors(OptionsPanelColorProvider provider) {
            this.colors = provider.getColors();
            if (colors == null) {
                throw new NullPointerException("Null colors for " + provider); // NOI18N
            }
            this.provider = provider;
        }

        public void saveColors () {
            if (changed) {
                Map<String, Color> colorsToSave = new HashMap<String, Color>(colors.size());
                for (Map.Entry<String, Color[]> e : colors.entrySet()) {
                    colorsToSave.put(e.getKey(), e.getValue()[0]);
                }
                provider.colorsChanged(colorsToSave);
            }
        }

        public synchronized List<AttributeSet> getColorAttributes() {
            ArrayList<AttributeSet> attrs = this.colorAttributes;
            if (attrs == null) {
                attrs = new ArrayList<AttributeSet>(colors.size());
                for (Map.Entry<String, Color[]> e : colors.entrySet()) {
                    SimpleAttributeSet sas = new SimpleAttributeSet ();
                    StyleConstants.setBackground(sas, e.getValue()[0]);
                    sas.addAttribute(StyleConstants.NameAttribute, e.getKey());
                    sas.addAttribute(EditorStyleConstants.DisplayName, e.getKey());
                    attrs.add(sas);
                }
                this.colorAttributes = attrs;
            }
            return attrs;
        }

        public void setColor(int index, Color color) {
            if (color == null) return;
            AttributeSet attr = colorAttributes.get(index);
            SimpleAttributeSet c = new SimpleAttributeSet(attr);
            if (attr != null) {
                c.addAttribute(StyleConstants.Background, color);
            } else {
                c.removeAttribute(StyleConstants.Background);
            }
            colorAttributes.set(index, c);
            Color[] savedColor = colors.get((String)c.getAttribute(StyleConstants.NameAttribute));
            savedColor[0] = color;
            changed = true;
        }

        /**
         * Resets colors to default values.
         */
        public synchronized void resetToDefaults() {
            for (Map.Entry<String, Color[]> e : colors.entrySet()) {
                e.getValue()[0] = e.getValue()[1];
            }
            colorAttributes = null;
            changed = true;
        }
        
    }
}
