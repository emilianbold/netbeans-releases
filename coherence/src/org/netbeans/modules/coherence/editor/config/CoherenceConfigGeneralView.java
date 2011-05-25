/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence.editor.config;

import org.netbeans.modules.coherence.xml.coherence.Coherence;
import org.netbeans.modules.coherence.xml.coherence.EditionName;
import org.netbeans.modules.coherence.xml.coherence.LicenseConfig;
import org.netbeans.modules.coherence.xml.coherence.LicenseMode;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.Property;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.awt.UndoRedo;
import org.openide.explorer.ExplorerManager;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author Andrew Hopkinson (Oracle A-Team)
 */
public class CoherenceConfigGeneralView extends JPanel implements MultiViewDescription, MultiViewElement,
        ExplorerManager.Provider, DocumentListener, PropertyChangeListener, TableModelListener, ListSelectionListener {

    /** Creates new form CoherenceConfigGeneralView */
    public CoherenceConfigGeneralView() {
        initComponents();
        initialise();
    }

    /*
     * =========================================================================
     * START: Custom Code
     * =========================================================================
     */
    public CoherenceConfigGeneralView(CoherenceConfigEditorSupport support) {
        this();
        this.support = support;
    }
    /*
     * Inner Class
     */
    /*
     * Properties
     */
    private static final Logger logger = Logger.getLogger(CoherenceConfigGeneralView.class.getCanonicalName());
    private CoherenceConfigEditorSupport support = null;
    private ExplorerManager em = null;
    private BindingGroup bindingGroup = new BindingGroup();
    /*
     * Methods
     */
    private void initialise() {
        jToggleButton1ActionPerformed(null);
    }
    private void refresh() {
        refresh(getCoherence());
    }
    private void refresh(Coherence coherence) {
        refreshBindings();
    }
    private void serialize() {

    }
    private Coherence getCoherence() {
        return ((CoherenceConfigDataObject)support.getDataObject()).getCoherence();
    }

    private void refreshBindings() {
        for (Binding b : bindingGroup.getBindings()) {
            bindingGroup.removeBinding(b);
        }
        setupBindings();
    }

    private void setupBindings() {
        Coherence coherence = getCoherence();
        // Set Bindings
        Property propertyTextValue = BeanProperty.create("text");
        Property propertySelected = BeanProperty.create("selected");
        Property propertySelectedItem = BeanProperty.create("selectedItem");
        // EditionName
        Property propertyEditionName = BeanProperty.create("editionName");
        Binding bindingEditionName = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, propertyEditionName, tfEditionName, propertyTextValue);
        bindingGroup.addBinding(bindingEditionName);
        // LicenseMode
        Property propertyLicenseMode = BeanProperty.create("licenseMode");
        Binding bindingLicenseMode = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, propertyLicenseMode, tfLicenseMode, propertyTextValue);
        bindingGroup.addBinding(bindingLicenseMode);
        // Bind All
        bindingGroup.bind();
    }
    /*
     * Binding Methods
     */
    public final static String EDITION_NAME_SYS_PROP = "tangosol.coherence.edition";
    public void setEditonName(String s) {
        Coherence coherence = getCoherence();
        if (coherence.getLicenseConfig() == null) coherence.setLicenseConfig(new LicenseConfig());
        if (coherence.getLicenseConfig().getEditionName() == null) {
            coherence.getLicenseConfig().setEditionName(new EditionName());
            coherence.getLicenseConfig().getEditionName().setSystemProperty(EDITION_NAME_SYS_PROP);
        }
        coherence.getLicenseConfig().getEditionName().setvalue(s);
    }
    public String getEditionName() {
        String s = null;
        Coherence coherence = getCoherence();

        if (coherence != null && coherence.getLicenseConfig() != null && coherence.getLicenseConfig().getEditionName() != null) s = coherence.getLicenseConfig().getEditionName().getvalue();

        return s;
    }
    public final static String LICENSE_MODE_SYS_PROP = "tangosol.coherence.mode";
    public void setLicenseMode(String s) {
        Coherence coherence = getCoherence();
        if (coherence.getLicenseConfig() == null) coherence.setLicenseConfig(new LicenseConfig());
        if (coherence.getLicenseConfig().getLicenseMode() == null) {
            coherence.getLicenseConfig().setLicenseMode(new LicenseMode());
            coherence.getLicenseConfig().getLicenseMode().setSystemProperty(LICENSE_MODE_SYS_PROP);
        }
        coherence.getLicenseConfig().getLicenseMode().setvalue(s);
    }
    public String getLicenseMode() {
        String s = null;
        Coherence coherence = getCoherence();

        if (coherence != null && coherence.getLicenseConfig() != null && coherence.getLicenseConfig().getLicenseMode() != null) s = coherence.getLicenseConfig().getLicenseMode().getvalue();

        return s;
    }
    /*
     * Overrides
     */
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }

    @Override
    public String getDisplayName() {
        return "General";
    }

    @Override
    public Image getIcon() {
        return ImageUtilities.loadImage(org.openide.util.NbBundle.getMessage(CoherenceConfigGeneralView.class, "CoherenceConfig.file.icon"));
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public String preferredID() {
        return this.getClass().getSimpleName();
    }

    @Override
    public MultiViewElement createElement() {
        em = new ExplorerManager();
        em.addPropertyChangeListener(this);
        try {
            refresh();
            support.openDocument().addDocumentListener(this);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "*** APH-I3 : Failed to Create Element ", ex);
        }
        return this;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        refresh();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        refresh();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        refresh();
    }

    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return new JToolBar();
    }

    @Override
    public Action[] getActions() {
        return support.getDataObject().getNodeDelegate().getActions(false);
    }

    @Override
    public Lookup getLookup() {
        return ((CoherenceConfigDataObject) support.getDataObject()).getNodeDelegate().getLookup();
    }

    @Override
    public UndoRedo getUndoRedo() {
        return null;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback mvec) {
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
    }

    @Override
    public void componentShowing() {
    }

    @Override
    public void componentHidden() {
    }

    @Override
    public void componentActivated() {
    }

    @Override
    public void componentDeactivated() {
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        logger.log(Level.INFO, "*** APH-I1 : propertyChanged ".concat(evt.getPropertyName()));
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        logger.log(Level.INFO, "*** APH-I1 : tableChanged ".concat(e.getColumn() + ""));
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
    }
    /*
     * =========================================================================
     * End: Custom Code
     * =========================================================================
     */

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        topPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jToggleButton1 = new javax.swing.JToggleButton();
        dataPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        tfEditionName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        tfLicenseMode = new javax.swing.JTextField();

        setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(CoherenceConfigGeneralView.class, "CoherenceConfigGeneralView.border.title"))); // NOI18N

        topPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(CoherenceConfigGeneralView.class, "CoherenceConfigGeneralView.topPanel.border.title"))); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(CoherenceConfigGeneralView.class, "CoherenceConfigGeneralView.jPanel1.border.title"))); // NOI18N

        jToggleButton1.setSelected(true);
        jToggleButton1.setText(org.openide.util.NbBundle.getMessage(CoherenceConfigGeneralView.class, "CoherenceConfigGeneralView.jToggleButton1.text")); // NOI18N
        jToggleButton1.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToggleButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToggleButton1)
        );

        dataPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(CoherenceConfigGeneralView.class, "CoherenceConfigGeneralView.dataPanel.border.title"))); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(CoherenceConfigGeneralView.class, "CoherenceConfigGeneralView.jLabel1.text")); // NOI18N

        tfEditionName.setText(org.openide.util.NbBundle.getMessage(CoherenceConfigGeneralView.class, "CoherenceConfigGeneralView.tfEditionName.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(CoherenceConfigGeneralView.class, "CoherenceConfigGeneralView.jLabel2.text")); // NOI18N

        tfLicenseMode.setText(org.openide.util.NbBundle.getMessage(CoherenceConfigGeneralView.class, "CoherenceConfigGeneralView.tfLicenseMode.text")); // NOI18N

        javax.swing.GroupLayout dataPanelLayout = new javax.swing.GroupLayout(dataPanel);
        dataPanel.setLayout(dataPanelLayout);
        dataPanelLayout.setHorizontalGroup(
            dataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dataPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tfLicenseMode, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
                    .addComponent(tfEditionName, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE))
                .addContainerGap())
        );
        dataPanelLayout.setVerticalGroup(
            dataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dataPanelLayout.createSequentialGroup()
                .addGroup(dataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(tfEditionName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(tfLicenseMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout topPanelLayout = new javax.swing.GroupLayout(topPanel);
        topPanel.setLayout(topPanelLayout);
        topPanelLayout.setHorizontalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, topPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(dataPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        topPanelLayout.setVerticalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topPanelLayout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dataPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(topPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(topPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        dataPanel.setVisible(jToggleButton1.isSelected());
    }//GEN-LAST:event_jToggleButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel dataPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JTextField tfEditionName;
    private javax.swing.JTextField tfLicenseMode;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables

}
