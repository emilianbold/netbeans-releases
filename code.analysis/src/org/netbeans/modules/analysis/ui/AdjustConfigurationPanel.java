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
package org.netbeans.modules.analysis.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import org.netbeans.modules.analysis.Configuration;
import org.netbeans.modules.analysis.RunAnalysis;
import org.netbeans.modules.analysis.RunAnalysisPanel.ConfigurationRenderer;
import org.netbeans.modules.analysis.SPIAccessor;
import org.netbeans.modules.analysis.spi.Analyzer.AnalyzerFactory;
import org.netbeans.modules.analysis.spi.Analyzer.CustomizerContext;
import org.netbeans.modules.analysis.spi.Analyzer.CustomizerProvider;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public class AdjustConfigurationPanel extends javax.swing.JPanel {

    private final Iterable<? extends AnalyzerFactory> analyzers;
    private CustomizerContext<Object, JComponent> currentContext;
    private final Map<AnalyzerFactory, CustomizerProvider> customizers = new IdentityHashMap<AnalyzerFactory, CustomizerProvider>();
    private final Map<CustomizerProvider, Object> customizerData = new IdentityHashMap<CustomizerProvider, Object>();
    private Preferences currentPreferences;
    private ModifiedPreferences currentPreferencesOverlay;
    private final String preselected;

    public AdjustConfigurationPanel(Iterable<? extends AnalyzerFactory> analyzers, String preselected) {
        this.preselected = preselected;
        initComponents();

        if (preselected == null) {
            DefaultComboBoxModel configurationModel = new DefaultComboBoxModel();

            for (Configuration c : RunAnalysis.readConfigurations()) {
                configurationModel.addElement(c);
            }

            configurationModel.addElement("New...");
            configurationModel.addElement("Duplicate");
            configurationModel.addElement("Rename...");
            configurationModel.addElement("Delete");

            configurationCombo.setModel(configurationModel);
            configurationCombo.setRenderer(new ConfigurationRenderer(false));
            configurationCombo.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    updateConfiguration();
                }
            });
        } else {
            configurationLabel.setVisible(false);
            configurationCombo.setVisible(false);
        }

        this.analyzers = analyzers;
        DefaultComboBoxModel analyzerModel = new DefaultComboBoxModel();

        for (AnalyzerFactory a : analyzers) {
            customizers.put(a, a.getCustomizerProvider());
            analyzerModel.addElement(a);
        }

        analyzerCombo.setModel(analyzerModel);
        analyzerCombo.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                updateAnalyzer();
            }
        });
        analyzerCombo.setRenderer(new AnalyzerRenderer());

        updateConfiguration();
    }

    private void updateConfiguration() {
        if (preselected == null) {
            currentPreferences = RunAnalysis.getConfigurationSettingsRoot(((Configuration) configurationCombo.getSelectedItem()).id());
        } else {
            currentPreferences = RunAnalysis.getConfigurationSettingsRoot("internal-temporary"); //TODO: better temporary name
            try {
                for (String c : currentPreferences.childrenNames()) {
                    currentPreferences.node(c).removeNode();
                }
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        currentPreferencesOverlay = new ModifiedPreferences(null, "", currentPreferences);
        updateAnalyzer();
    }

    private void updateAnalyzer() {
        AnalyzerFactory selected = (AnalyzerFactory) analyzerCombo.getSelectedItem();
        CustomizerProvider customizer = customizers.get(selected);

        if (!customizerData.containsKey(customizer)) {
            customizerData.put(customizer, customizer.initialize());
        }

        Object data = customizerData.get(customizer);
        Preferences settings = currentPreferencesOverlay.node(SPIAccessor.ACCESSOR.getAnalyzerId(selected));

        analyzerPanel.removeAll();
        currentContext = new CustomizerContext<Object, JComponent>(settings, preselected, null, data);
        currentContext.setSelectedId(preselected);
        analyzerPanel.add(customizer.createComponent(currentContext), BorderLayout.CENTER);
        analyzerPanel.revalidate();
        analyzerPanel.repaint();
    }

    public String getIdToRun() {
        return SPIAccessor.ACCESSOR.getSelectedId(currentContext);
    }

    public void save() {
        currentPreferencesOverlay.store(currentPreferences);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        configurationLabel = new javax.swing.JLabel();
        configurationCombo = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        analyzerCombo = new javax.swing.JComboBox();
        analyzerPanel = new javax.swing.JPanel();

        configurationLabel.setText(org.openide.util.NbBundle.getMessage(AdjustConfigurationPanel.class, "AdjustConfigurationPanel.configurationLabel.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(AdjustConfigurationPanel.class, "AdjustConfigurationPanel.jLabel2.text")); // NOI18N

        analyzerPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(analyzerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(configurationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(configurationCombo, 0, 254, Short.MAX_VALUE)
                            .addComponent(analyzerCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(configurationLabel)
                    .addComponent(configurationCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(analyzerCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(analyzerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox analyzerCombo;
    private javax.swing.JPanel analyzerPanel;
    private javax.swing.JComboBox configurationCombo;
    private javax.swing.JLabel configurationLabel;
    private javax.swing.JLabel jLabel2;
    // End of variables declaration//GEN-END:variables

    private static class AnalyzerRenderer extends DefaultListCellRenderer {
        @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            AnalyzerFactory a = (AnalyzerFactory) value;
            return super.getListCellRendererComponent(list, SPIAccessor.ACCESSOR.getAnalyzerDisplayName(a), index, isSelected, cellHasFocus);
        }
    }

    //XXX: need tests for the ModifiedPreferences
    //XXX: should move MP to some generic API, copied on several places (java.hints, findbugs, etc.)
    private static class ModifiedPreferences extends AbstractPreferences {

        private final Map<String,Object> properties = new HashMap<String, Object>();
        private final Map<String,ModifiedPreferences> subNodes = new HashMap<String, ModifiedPreferences>();

        public ModifiedPreferences(ModifiedPreferences parent, String name) {
            super(parent, name);
        }

        public ModifiedPreferences(ModifiedPreferences parent, String name, Preferences node) {
            this(parent, name); // NOI18N
            try {
                for (java.lang.String key : node.keys()) {
                    put(key, node.get(key, null));
                }
                for (String child : node.childrenNames()) {
                    subNodes.put(child, new ModifiedPreferences(this, node.name(), node.node(child)));
                }
            }
            catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
        }


        public void store( Preferences target ) {

            try {
                for (String key : keys()) {
                    target.put(key, get(key, null));
                }
                for (String child : childrenNames()) {
                    ((ModifiedPreferences) node(child)).store(target.node(child));
                }
            }
            catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }

        }

        protected void putSpi(String key, String value) {
            properties.put(key, value);
        }

        protected String getSpi(String key) {
            return (String)properties.get(key);
        }

        protected void removeSpi(String key) {
            properties.remove(key);
        }

        protected void removeNodeSpi() throws BackingStoreException {
            ((ModifiedPreferences) parent()).subNodes.put(name(), new ModifiedPreferences(this, name()));
        }

        protected String[] keysSpi() throws BackingStoreException {
            String array[] = new String[properties.keySet().size()];
            return properties.keySet().toArray( array );
        }

        protected String[] childrenNamesSpi() throws BackingStoreException {
            return subNodes.keySet().toArray(new String[0]);
        }

        protected AbstractPreferences childSpi(String name) {
            ModifiedPreferences result = subNodes.get(name);

            if (result == null) {
                subNodes.put(name, result = new ModifiedPreferences(this, name));
            }

            return result;
        }

        protected void syncSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        protected void flushSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

	boolean isEmpty() {
	    return properties.isEmpty();
	}
    }
}
