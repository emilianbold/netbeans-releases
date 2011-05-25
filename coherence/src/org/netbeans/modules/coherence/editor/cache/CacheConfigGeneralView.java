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
package org.netbeans.modules.coherence.editor.cache;

import org.netbeans.modules.coherence.xml.cache.CacheConfig;
import org.netbeans.modules.coherence.xml.cache.Defaults;
import org.netbeans.modules.coherence.xml.cache.Serializer;
import org.netbeans.modules.coherence.xml.cache.SocketProvider;
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
import org.jdesktop.beansbinding.Binding.SyncFailure;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.BindingListener;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.Property;
import org.jdesktop.beansbinding.PropertyStateEvent;
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
public class CacheConfigGeneralView extends JPanel implements MultiViewDescription, MultiViewElement,
        ExplorerManager.Provider, DocumentListener, PropertyChangeListener, TableModelListener, ListSelectionListener, BindingListener {

    /** Creates new form CacheConfigGeneralView */
    public CacheConfigGeneralView() {
        initComponents();
        initialise();
    }

    /*
     * =========================================================================
     * START: Custom Code
     * =========================================================================
     */
    public CacheConfigGeneralView(CacheConfigEditorSupport support) {
        this();
        this.support = support;
    }
    /*
     * My Properties
     */
    private static final Logger logger = Logger.getLogger(CacheConfigGeneralView.class.getCanonicalName());
    private CacheConfigEditorSupport support = null;
    private ExplorerManager em = null;
    private BindingGroup bindingGroup = new BindingGroup();

    /*
     * My Methods
     */
    private void initialise() {
    }

    private void setModified() {
        if (!support.getDataObject().isModified()) {
            support.getDataObject().setModified(true);
        }
    }

    protected CacheConfig getCacheConfig() {
        return ((CacheConfigDataObject) support.getDataObject()).getCacheConfig();
    }

    private void reload() {
        refresh(((CacheConfigDataObject) support.getDataObject()).loadData());
    }

    private void refresh() {
        refresh(getCacheConfig());
    }

    private void refresh(CacheConfig cacheConfig) {
        if (cacheConfig != null) {
            refreshBindings();
        }
    }

    private void refreshBindings() {
        for (Binding b : bindingGroup.getBindings()) {
            bindingGroup.removeBinding(b);
        }
        setupBindings();
    }

    private void setupBindings() {
        // Set Bindings
        Property propertyTextValue = BeanProperty.create("text");
        // DefaultSerializer
        Property propertyDefaultSerializer = BeanProperty.create("defaultSerializer");
        Binding bindingDefaultSerializer = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, propertyDefaultSerializer, tfSerializer, propertyTextValue);
        bindingGroup.addBinding(bindingDefaultSerializer);
        // SocketProvider
        Property propertySocketProvider = BeanProperty.create("defaultSocketProvider");
        Binding bindingSocketProvider = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, propertySocketProvider, tfSocketProvider, propertyTextValue);
        bindingGroup.addBinding(bindingSocketProvider);
        // Bind All
        bindingGroup.addBindingListener(this);
        bindingGroup.bind();
    }
    /*
     * Binding Methods
     */

    public void setDefaultSerializer(String s) {
        CacheConfig cacheConfig = getCacheConfig();
        if (cacheConfig != null) {
            Defaults defaults = cacheConfig.getDefaults();
            if (defaults == null) {
                defaults = new Defaults();
                cacheConfig.setDefaults(defaults);
            }
            Serializer serializer = defaults.getSerializer();
            if (serializer == null) {
                serializer = new Serializer();
                defaults.setSerializer(serializer);
            }
            serializer.setSystemProperty(s);
        }

    }

    public String getDefaultSerializer() {
        CacheConfig cacheConfig = getCacheConfig();
        String s = null;

        if (cacheConfig != null) {
            Defaults defaults = cacheConfig.getDefaults();
            if (defaults != null) {
                Serializer serializer = defaults.getSerializer();
                if (serializer != null) {
                    s = serializer.getSystemProperty();
                }
            }
        }

        return s;
    }

    public void setDefaultSocketProvider(String s) {
        CacheConfig cacheConfig = getCacheConfig();
        if (cacheConfig != null) {
            Defaults defaults = cacheConfig.getDefaults();
            if (defaults == null) {
                defaults = new Defaults();
                cacheConfig.setDefaults(defaults);
            }
            SocketProvider provider = defaults.getSocketProvider();
            if (provider == null) {
                provider = new SocketProvider();
                defaults.setSocketProvider(provider);
            }
            provider.setSystemProperty(s);
        }

    }

    public String getDefaultSocketProvider() {
        CacheConfig cacheConfig = getCacheConfig();
        String s = null;

        if (cacheConfig != null) {
            Defaults defaults = cacheConfig.getDefaults();
            if (defaults != null) {
                SocketProvider provider = defaults.getSocketProvider();
                if (provider != null) {
                    s = provider.getSystemProperty();
                }
            }
        }

        return s;
    }
    /*
     * Binding Overrides
     */

    @Override
    public void bindingBecameBound(Binding binding) {
    }

    @Override
    public void bindingBecameUnbound(Binding binding) {
    }

    @Override
    public void syncFailed(Binding binding, SyncFailure failure) {
    }

    @Override
    public void synced(Binding binding) {
    }

    @Override
    public void sourceChanged(Binding binding, PropertyStateEvent event) {
        logger.log(Level.INFO, "*** APH-I1 : sourceChanged ");
    }

    @Override
    public void targetChanged(Binding binding, PropertyStateEvent event) {
        logger.log(Level.INFO, "*** APH-I1 : targetChanged ");
        setModified();
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
        return ImageUtilities.loadImage(org.openide.util.NbBundle.getMessage(CacheConfigGeneralView.class, "CacheConfig.file.icon"));
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
        reload();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        reload();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        reload();
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
        return ((CacheConfigDataObject) support.getDataObject()).getNodeDelegate().getLookup();
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
     * END: Custom Code
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

        topScrollPane = new javax.swing.JScrollPane();
        topPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        tfSerializer = new javax.swing.JTextField();
        tfSocketProvider = new javax.swing.JTextField();

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(CacheConfigGeneralView.class, "CacheConfigGeneralView.jPanel1.border.title"))); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(CacheConfigGeneralView.class, "CacheConfigGeneralView.jLabel1.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(CacheConfigGeneralView.class, "CacheConfigGeneralView.jLabel2.text")); // NOI18N

        tfSerializer.setText(org.openide.util.NbBundle.getMessage(CacheConfigGeneralView.class, "CacheConfigGeneralView.tfSerializer.text")); // NOI18N

        tfSocketProvider.setText(org.openide.util.NbBundle.getMessage(CacheConfigGeneralView.class, "CacheConfigGeneralView.tfSocketProvider.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tfSocketProvider, javax.swing.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
                    .addComponent(tfSerializer, javax.swing.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(tfSerializer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(tfSocketProvider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout topPanelLayout = new javax.swing.GroupLayout(topPanel);
        topPanel.setLayout(topPanelLayout);
        topPanelLayout.setHorizontalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        topPanelLayout.setVerticalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(227, Short.MAX_VALUE))
        );

        topScrollPane.setViewportView(topPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(topScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(topScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField tfSerializer;
    private javax.swing.JTextField tfSocketProvider;
    private javax.swing.JPanel topPanel;
    private javax.swing.JScrollPane topScrollPane;
    // End of variables declaration//GEN-END:variables
}
