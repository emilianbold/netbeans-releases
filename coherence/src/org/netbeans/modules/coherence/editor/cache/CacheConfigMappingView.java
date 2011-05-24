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
import org.netbeans.modules.coherence.xml.cache.CacheMapping;
import org.netbeans.modules.coherence.xml.cache.CachingSchemeMapping;
import org.netbeans.modules.coherence.xml.cache.InitParam;
import org.netbeans.modules.coherence.xml.cache.InitParams;
import org.netbeans.modules.coherence.xml.cache.ParamName;
import org.netbeans.modules.coherence.xml.cache.ParamType;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
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
public class CacheConfigMappingView extends JPanel implements MultiViewDescription, MultiViewElement,
        ExplorerManager.Provider, DocumentListener, PropertyChangeListener, TableModelListener, ListSelectionListener, BindingListener {

    /** Creates new form CacheConfigMappingView */
    public CacheConfigMappingView() {
        initComponents();
        initialise();
    }

    /*
     * =========================================================================
     * START: Custom Code
     * =========================================================================
     */
    public CacheConfigMappingView(CacheConfigEditorSupport support) {
        this();
        this.support = support;
    }
    /*
     * Inner Classes
     */

    public class CacheMappingsTableModel extends AbstractTableModel {

        private final String[] columnNames = {"Cache Name", "Scheme Name", "Parameters"};
        private List<Object[]> data = new ArrayList<Object[]>();
        private boolean[] edittable = {false, false, false};

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Object value = null;

            if (rowIndex < getRowCount() && columnIndex < getColumnCount()) {
                value = data.get(rowIndex)[columnIndex];
            }
            return value;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (rowIndex < getRowCount() && columnIndex < getColumnCount()) {
                data.get(rowIndex)[columnIndex] = aValue;
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (getEdittable() == null || columnIndex > getEdittable().length) {
                return false;
            } else {
                return getEdittable()[columnIndex];
            }
        }

        public boolean[] getEdittable() {
            return edittable;
        }

        public void setEdittable(boolean[] edittable) {
            this.edittable = edittable;
        }

        public void addRow(String cacheName, String schemeName, InitParams initParams, Object node) {
            Object[] row = {cacheName, schemeName, getInitParamString(initParams), node};
            data.add(row);
            fireTableDataChanged();
        }

        public void clear() {
            data.clear();
            fireTableDataChanged();
        }

        public void updateRow(int rowNum, String cacheName, String schemeName, InitParams initParams) {
            data.get(rowNum)[0] = cacheName;
            data.get(rowNum)[1] = schemeName;
            data.get(rowNum)[2] = getInitParamString(initParams);
            CacheMapping ut = (CacheMapping) data.get(rowNum)[3];
            ut.setCacheName(cacheName);
            ut.getSchemeName().setvalue(schemeName);
            ut.setInitParams(initParams);
            fireTableDataChanged();
        }

        public Object getCacheMapping(int rowIndex) {
            Object value = null;

            if (rowIndex < getRowCount()) {
                value = data.get(rowIndex)[3];
            }
            return value;
        }

        public void removeRow(int rowIndex) {
            if (rowIndex < getRowCount()) {
                data.remove(rowIndex);
            }
            fireTableDataChanged();
        }

        public String getInitParamString(InitParams initParams) {
            StringBuilder sbParams = new StringBuilder();
            Properties initParamsProp = new Properties();

            if (initParams != null) {
                Object objNameOrType = null;
                for (InitParam ip : initParams.getInitParam()) {
                    if (ip.getParamNameOrParamType() != null && ip.getParamNameOrParamType().size() > 0) {
                        objNameOrType = ip.getParamNameOrParamType().get(0);
                        if (objNameOrType instanceof ParamName) {
                            sbParams.append(((ParamName) objNameOrType).getvalue().concat(" = ").concat(ip.getParamValue().getvalue()).concat(", "));
                            initParamsProp.put(((ParamName) objNameOrType).getvalue(), ip.getParamValue().getvalue());
                        } else if (objNameOrType instanceof ParamType) {
                            sbParams.append(((ParamType) objNameOrType).getvalue().concat(" = ").concat(ip.getParamValue().getvalue()).concat(", "));
                            initParamsProp.put(((ParamType) objNameOrType).getvalue(), ip.getParamValue().getvalue());
                        }
                    }
                }
            }

            return initParamsProp.toString();
        }

        public List<Object[]> getData() {
            return data;
        }

        public void setData(List<Object[]> data) {
            this.data = data;
            fireTableDataChanged();
        }
    }

    /*
     * My Properties
     */
    private static final Logger logger = Logger.getLogger(CacheConfigMappingView.class.getCanonicalName());
    private CacheConfigEditorSupport support = null;
    private ExplorerManager em = null;
    private BindingGroup bindingGroup = new BindingGroup();
    private CacheMappingsTableModel cacheMappingsTableModel = new CacheMappingsTableModel();
    private List<String> schemeNamesList = null;
    /*
     * My Methods
     */

    private void initialise() {
        jToggleButton1ActionPerformed(null);
        tblCacheMappings.getSelectionModel().addListSelectionListener(this);
    }

    private void reload() {
        refresh(((CacheConfigDataObject) support.getDataObject()).loadData());
    }

    private void refresh() {
        refresh(getCacheConfig());
    }

    private void refresh(CacheConfig cacheConfig) {
        if (cacheConfig != null) {
            // Populate Scheme Names List
            List schemeList = cacheConfig.getCachingSchemes().getDistributedSchemeOrTransactionalSchemeOrReplicatedSchemeOrOptimisticSchemeOrLocalSchemeOrDiskSchemeOrExternalSchemeOrPagedExternalSchemeOrOverflowSchemeOrClassSchemeOrNearSchemeOrVersionedNearSchemeOrInvocationSchemeOrReadWriteBackingMapSchemeOrVersionedBackingMapSchemeOrRemoteCacheSchemeOrRemoteInvocationSchemeOrProxyScheme();
            getSchemes(schemeList);

            refreshBindings();

//            cacheMappingsTableModel.clear();
//            CachingSchemeMapping mapping = cacheConfig.getCachingSchemeMapping();
//            if (mapping != null) {
//                List<CacheMapping> mappingList = mapping.getCacheMapping();
//                for (CacheMapping cm : mappingList) {
//                    cacheMappingsTableModel.addRow(cm.getCacheName(), cm.getSchemeName().getvalue(), cm.getInitParams(), cm);
//                }
//            }
        }
    }

    private void setModified() {
        if (!support.getDataObject().isModified()) {
            support.getDataObject().setModified(true);
        }
    }

    protected CacheConfig getCacheConfig() {
        return ((CacheConfigDataObject) support.getDataObject()).getCacheConfig();
    }

    public CacheMappingsTableModel getCacheMappingsTableModel() {
        return cacheMappingsTableModel;
    }

    public List<String> getSchemes(List schemeList) {
        if (schemeNamesList == null) {
            schemeNamesList = new ArrayList<String>();
            for (Object o : schemeList) {
                schemeNamesList.add(CacheConfigSchemeView.getSchemeName(o));
            }
        }

        return schemeNamesList;
    }

    private void refreshBindings() {
        for (Binding b : bindingGroup.getBindings()) {
            bindingGroup.removeBinding(b);
        }
        setupBindings();
    }

    private void setupBindings() {
        // Set Bindings
        Property propertyData = BeanProperty.create("data");
        // Standard Types
        Property propertyCacheMapping = BeanProperty.create("cacheMappingRows");
        Binding bindingCacheMapping = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, propertyCacheMapping, cacheMappingsTableModel, propertyData);
        bindingGroup.addBinding(bindingCacheMapping);
        // Bind All
        bindingGroup.addBindingListener(this);
        bindingGroup.bind();
    }
    /*
     * Binding Methods
     */

    public void setCacheMappingRows(List<Object[]> data) {
        CacheConfig cacheConfig = getCacheConfig();
        if (data != null) {
//            for (Object[] oArray : data) {
//            }
        }
    }

    public List<Object[]> getCacheMappingRows() {
        CacheConfig cacheConfig = getCacheConfig();
        List<Object[]> objArrayList = new ArrayList<Object[]>();
        Object[] row = null;
        CachingSchemeMapping mapping = cacheConfig.getCachingSchemeMapping();
        if (mapping != null) {
            List<CacheMapping> mappingList = mapping.getCacheMapping();
            for (CacheMapping cm : mappingList) {
                row = new Object[4];
                row[0] = cm.getCacheName();
                row[1] = cm.getSchemeName().getvalue();
                row[2] = cacheMappingsTableModel.getInitParamString(cm.getInitParams());
                row[3] = cm;
                objArrayList.add(row);
            }
        }

        return objArrayList;
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
    }

    @Override
    public void targetChanged(Binding binding, PropertyStateEvent event) {
        setModified();
    }

    /*
     * Custom Overrides
     */
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }

    @Override
    public String getDisplayName() {
        return "Mappings";
    }

    @Override
    public Image getIcon() {
        return ImageUtilities.loadImage(org.openide.util.NbBundle.getMessage(CacheConfigMappingView.class, "CacheConfig.file.icon"));
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
        btnEditMapping.setEnabled(true);
        btnRemoveMapping.setEnabled(true);
        btnEditImage.setEnabled(true);
        btnRemoveImage.setEnabled(true);
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

        topPanel = new javax.swing.JPanel();
        jToggleButton1 = new javax.swing.JToggleButton();
        tablePanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        tblScrollPane = new javax.swing.JScrollPane();
        tblCacheMappings = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        btnAddImage = new javax.swing.JButton();
        btnEditImage = new javax.swing.JButton();
        btnRemoveImage = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        btnAddMapping = new javax.swing.JButton();
        btnEditMapping = new javax.swing.JButton();
        btnRemoveMapping = new javax.swing.JButton();

        jToggleButton1.setSelected(true);
        jToggleButton1.setText(org.openide.util.NbBundle.getMessage(CacheConfigMappingView.class, "CacheConfigMappingView.jToggleButton1.text")); // NOI18N
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        tblCacheMappings.setModel(getCacheMappingsTableModel());
        tblScrollPane.setViewportView(tblCacheMappings);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tblScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 385, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tblScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
        );

        btnAddImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/coherence/resources/icons/add.png"))); // NOI18N
        btnAddImage.setContentAreaFilled(false);
        btnAddImage.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btnAddImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddImageActionPerformed(evt);
            }
        });

        btnEditImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/coherence/resources/icons/edit.png"))); // NOI18N
        btnEditImage.setContentAreaFilled(false);
        btnEditImage.setEnabled(false);
        btnEditImage.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btnEditImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditImageActionPerformed(evt);
            }
        });

        btnRemoveImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/coherence/resources/icons/delete.png"))); // NOI18N
        btnRemoveImage.setContentAreaFilled(false);
        btnRemoveImage.setEnabled(false);
        btnRemoveImage.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btnRemoveImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveImageActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnAddImage)
            .addComponent(btnEditImage)
            .addComponent(btnRemoveImage)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(btnAddImage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditImage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRemoveImage)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout tablePanelLayout = new javax.swing.GroupLayout(tablePanel);
        tablePanel.setLayout(tablePanelLayout);
        tablePanelLayout.setHorizontalGroup(
            tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, tablePanelLayout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        tablePanelLayout.setVerticalGroup(
            tablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tablePanelLayout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(37, Short.MAX_VALUE))
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        btnAddMapping.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/coherence/resources/icons/add.png"))); // NOI18N
        btnAddMapping.setText(org.openide.util.NbBundle.getMessage(CacheConfigMappingView.class, "CacheConfigMappingView.btnAddMapping.text")); // NOI18N
        btnAddMapping.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddMappingActionPerformed(evt);
            }
        });

        btnEditMapping.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/coherence/resources/icons/edit.png"))); // NOI18N
        btnEditMapping.setText(org.openide.util.NbBundle.getMessage(CacheConfigMappingView.class, "CacheConfigMappingView.btnEditMapping.text")); // NOI18N
        btnEditMapping.setEnabled(false);
        btnEditMapping.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditMappingActionPerformed(evt);
            }
        });

        btnRemoveMapping.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/coherence/resources/icons/delete.png"))); // NOI18N
        btnRemoveMapping.setText(org.openide.util.NbBundle.getMessage(CacheConfigMappingView.class, "CacheConfigMappingView.btnRemoveMapping.text")); // NOI18N
        btnRemoveMapping.setEnabled(false);
        btnRemoveMapping.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveMappingActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(btnAddMapping)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditMapping)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRemoveMapping)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddMapping)
                    .addComponent(btnEditMapping)
                    .addComponent(btnRemoveMapping))
                .addContainerGap(11, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout topPanelLayout = new javax.swing.GroupLayout(topPanel);
        topPanel.setLayout(topPanelLayout);
        topPanelLayout.setHorizontalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, topPanelLayout.createSequentialGroup()
                .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, topPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(tablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, topPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, topPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jToggleButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE)))
                .addContainerGap())
        );
        topPanelLayout.setVerticalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topPanelLayout.createSequentialGroup()
                .addComponent(jToggleButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void btnEditMappingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditMappingActionPerformed
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                int rowNum = tblCacheMappings.getSelectedRow();
                CacheMapping cm = (CacheMapping) cacheMappingsTableModel.getCacheMapping(rowNum);
                EditCacheMappingDialog dialog = new EditCacheMappingDialog(null, true, cm, getSchemes(schemeNamesList));
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
                if (dialog.getReturnStatus() == dialog.RET_OK) {
                    cm = dialog.getCacheMapping();
                    CacheConfig cacheConfig = getCacheConfig();
//                    cacheConfig.getCachingSchemeMapping().getCacheMapping().add(cm);
                    cacheMappingsTableModel.updateRow(rowNum, cm.getCacheName(), cm.getSchemeName().getvalue(), cm.getInitParams());
                    setModified();
                }
            }
        });
    }//GEN-LAST:event_btnEditMappingActionPerformed

    private void btnAddMappingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddMappingActionPerformed
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                EditCacheMappingDialog dialog = new EditCacheMappingDialog(null, true, null, getSchemes(schemeNamesList));
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
                if (dialog.getReturnStatus() == dialog.RET_OK) {
                    CacheMapping cm = dialog.getCacheMapping();
                    CacheConfig cacheConfig = getCacheConfig();
                    cacheConfig.getCachingSchemeMapping().getCacheMapping().add(cm);
                    cacheMappingsTableModel.addRow(cm.getCacheName(), cm.getSchemeName().getvalue(), cm.getInitParams(), cm);
                    setModified();
                }
            }
        });
    }//GEN-LAST:event_btnAddMappingActionPerformed

    private void btnRemoveMappingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveMappingActionPerformed
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                int option = JOptionPane.showConfirmDialog(tablePanel, "Please Confirm Cache Mapping Removal", "", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    int rowNum = tblCacheMappings.getSelectedRow();
                    CacheMapping cm = (CacheMapping) cacheMappingsTableModel.getCacheMapping(rowNum);
                    cacheMappingsTableModel.removeRow(rowNum);

                    CacheConfig cacheConfig = getCacheConfig();
                    cacheConfig.getCachingSchemeMapping().getCacheMapping().remove(cm);
                    setModified();
                }
            }
        });
    }//GEN-LAST:event_btnRemoveMappingActionPerformed

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        tablePanel.setVisible(jToggleButton1.isSelected());
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void btnAddImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddImageActionPerformed
        btnAddMappingActionPerformed(evt);
    }//GEN-LAST:event_btnAddImageActionPerformed

    private void btnEditImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditImageActionPerformed
        btnEditMappingActionPerformed(evt);
    }//GEN-LAST:event_btnEditImageActionPerformed

    private void btnRemoveImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveImageActionPerformed
        btnRemoveMappingActionPerformed(evt);
    }//GEN-LAST:event_btnRemoveImageActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddImage;
    private javax.swing.JButton btnAddMapping;
    private javax.swing.JButton btnEditImage;
    private javax.swing.JButton btnEditMapping;
    private javax.swing.JButton btnRemoveImage;
    private javax.swing.JButton btnRemoveMapping;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JTable tblCacheMappings;
    private javax.swing.JScrollPane tblScrollPane;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables
}
