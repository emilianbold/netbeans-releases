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

import org.netbeans.modules.coherence.editor.cache.scheme.TransactionalSchemePanel;
import org.netbeans.modules.coherence.editor.cache.scheme.SchemePanelInterface;
import org.netbeans.modules.coherence.editor.cache.scheme.ReplicatedSchemePanel;
import org.netbeans.modules.coherence.editor.cache.scheme.ReadWriteBackingMapSchemePanel;
import org.netbeans.modules.coherence.editor.cache.scheme.ProxySchemePanel;
import org.netbeans.modules.coherence.editor.cache.scheme.OverflowSchemePanel;
import org.netbeans.modules.coherence.editor.cache.scheme.OptimisticSchemePanel;
import org.netbeans.modules.coherence.editor.cache.scheme.NearSchemePanel;
import org.netbeans.modules.coherence.editor.cache.scheme.LocalSchemePanel;
import org.netbeans.modules.coherence.editor.cache.scheme.InvocationSchemePanel;
import org.netbeans.modules.coherence.editor.cache.scheme.ExternalSchemePanel;
import org.netbeans.modules.coherence.editor.cache.scheme.DistributedSchemePanel;
import org.netbeans.modules.coherence.xml.cache.CacheConfig;
import org.netbeans.modules.coherence.xml.cache.DistributedScheme;
import org.netbeans.modules.coherence.xml.cache.ExternalScheme;
import org.netbeans.modules.coherence.xml.cache.InvocationScheme;
import org.netbeans.modules.coherence.xml.cache.LocalScheme;
import org.netbeans.modules.coherence.xml.cache.NearScheme;
import org.netbeans.modules.coherence.xml.cache.OptimisticScheme;
import org.netbeans.modules.coherence.xml.cache.OverflowScheme;
import org.netbeans.modules.coherence.xml.cache.ProxyScheme;
import org.netbeans.modules.coherence.xml.cache.ReadWriteBackingMapScheme;
import org.netbeans.modules.coherence.xml.cache.ReplicatedScheme;
import org.netbeans.modules.coherence.xml.cache.TransactionalScheme;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
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
public class CacheConfigSchemeView extends JPanel implements MultiViewDescription, MultiViewElement,
        ExplorerManager.Provider, DocumentListener, PropertyChangeListener, TableModelListener, ListSelectionListener, ActionListener, SchemeRemoveListener, SchemeModifiedListener {

    /** Creates new form CacheConfigSchemeView */
    public CacheConfigSchemeView() {
        initComponents();
        initialise();
    }

    /*
     * =========================================================================
     * START: Custom Code
     * =========================================================================
     */
    public CacheConfigSchemeView(CacheConfigEditorSupport support) {
        this();
        this.support = support;
    }
    /*
     * Inner Class
     */
    /*
     * My Properties
     */
    private static final Logger logger = Logger.getLogger(CacheConfigSchemeView.class.getCanonicalName());
    private CacheConfigEditorSupport support = null;
    private ExplorerManager em = null;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    private List<Scheme> schemeNamesList = null;

    /*
     * My Methods
     */
    private void initialise() {
        schemesPanel.setVisible(jToggleButton1.isSelected());
    }

    private void reload() {
        refresh(((CacheConfigDataObject) support.getDataObject()).loadData());
    }

    private void refresh() {
        refresh(getCacheConfig());
    }

    private void refresh(CacheConfig cacheConfig) {
        schemesPanel.removeAll();
        schemeNamesList = null;
        if (cacheConfig != null) {
            List schemeList = cacheConfig.getCachingSchemes().getDistributedSchemeOrTransactionalSchemeOrReplicatedSchemeOrOptimisticSchemeOrLocalSchemeOrDiskSchemeOrExternalSchemeOrPagedExternalSchemeOrOverflowSchemeOrClassSchemeOrNearSchemeOrVersionedNearSchemeOrInvocationSchemeOrReadWriteBackingMapSchemeOrVersionedBackingMapSchemeOrRemoteCacheSchemeOrRemoteInvocationSchemeOrProxyScheme();
            JPanel panel = null;
            GroupLayout schemesPanelLayout = (GroupLayout) schemesPanel.getLayout();
            schemesPanel.setLayout(schemesPanelLayout);
            GroupLayout.ParallelGroup horizontalParaGroup = schemesPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
            GroupLayout.ParallelGroup verticalParaGroup = schemesPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
            GroupLayout.SequentialGroup verticalSeqGroup = schemesPanelLayout.createSequentialGroup();
            verticalParaGroup.addGroup(verticalSeqGroup);
            for (Object o : schemeList) {
                panel = null;
//                logger.log(Level.INFO, "*** APH-I2 : Refresh() Processing Scheme " + o);
                if (o instanceof DistributedScheme) {
                    panel = new DistributedSchemePanel((DistributedScheme) o, getSchemes(schemeList));
                } else if (o instanceof ReplicatedScheme) {
                    panel = new ReplicatedSchemePanel((ReplicatedScheme) o, getSchemes(schemeList));
                } else if (o instanceof OptimisticScheme) {
                    panel = new OptimisticSchemePanel((OptimisticScheme) o, getSchemes(schemeList));
                } else if (o instanceof NearScheme) {
                    panel = new NearSchemePanel((NearScheme) o, getSchemes(schemeList));
                } else if (o instanceof LocalScheme) {
                    panel = new LocalSchemePanel((LocalScheme) o);
                } else if (o instanceof ReadWriteBackingMapScheme) {
                    panel = new ReadWriteBackingMapSchemePanel((ReadWriteBackingMapScheme) o, getSchemes(schemeList));
                } else if (o instanceof OverflowScheme) {
                    panel = new OverflowSchemePanel((OverflowScheme) o, getSchemes(schemeList));
                } else if (o instanceof ExternalScheme) {
                    panel = new ExternalSchemePanel((ExternalScheme) o);
                } else if (o instanceof InvocationScheme) {
                    panel = new InvocationSchemePanel((InvocationScheme) o);
                } else if (o instanceof ProxyScheme) {
                    panel = new ProxySchemePanel((ProxyScheme) o);
                } else if (o instanceof TransactionalScheme) {
                    panel = new TransactionalSchemePanel((TransactionalScheme) o);
                }
                if (panel != null) {
                    ((SchemePanelInterface) panel).addSchemeRemoveListener(this);
                    ((SchemePanelInterface) panel).addSchemeModifiedListener(this);
//                    ((SchemePanelInterface) panel).setSchemeList(getSchemes(schemeList));
                    horizontalParaGroup.addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
                    verticalSeqGroup.addGroup(schemesPanelLayout.createSequentialGroup().addComponent(panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED));
                }
            }
            schemesPanelLayout.setHorizontalGroup(horizontalParaGroup);
            schemesPanelLayout.setVerticalGroup(verticalParaGroup);
        }
    }

    private void setModified() {
        if (!support.getDataObject().isModified())
            support.getDataObject().setModified(true);
    }

    public static String getSchemeName(Object o) {
        String name = "Unknown";

        try {
            if (o != null) {
                if (o instanceof DistributedScheme) {
                    name = ((DistributedScheme) o).getSchemeName().getvalue();
                } else if (o instanceof ReplicatedScheme) {
                    name = ((ReplicatedScheme) o).getSchemeName().getvalue();
                } else if (o instanceof OptimisticScheme) {
                    name = ((OptimisticScheme) o).getSchemeName().getvalue();
                } else if (o instanceof NearScheme) {
                    name = ((NearScheme) o).getSchemeName().getvalue();
                } else if (o instanceof LocalScheme) {
                    name = ((LocalScheme) o).getSchemeName().getvalue();
                } else if (o instanceof ReadWriteBackingMapScheme) {
                    name = ((ReadWriteBackingMapScheme) o).getSchemeName().getvalue();
                } else if (o instanceof OverflowScheme) {
                    name = ((OverflowScheme) o).getSchemeName().getvalue();
                } else if (o instanceof ExternalScheme) {
                    name = ((ExternalScheme) o).getSchemeName().getvalue();
                } else if (o instanceof InvocationScheme) {
                    name = ((InvocationScheme) o).getSchemeName().getvalue();
                } else if (o instanceof ProxyScheme) {
                    name = ((ProxyScheme) o).getSchemeName().getvalue();
                } else if (o instanceof TransactionalScheme) {
                    name = ((TransactionalScheme) o).getSchemeName().getvalue();
                }
            }
        } catch (Exception e) {
            name = null;
        }

        return name;
    }

    public static String getSchemeRefName(Object o) {
        String name = "Unknown";

        try {
            if (o != null) {
                if (o instanceof DistributedScheme) {
                    name = ((DistributedScheme) o).getSchemeRef();
                } else if (o instanceof ReplicatedScheme) {
                    name = ((ReplicatedScheme) o).getSchemeRef();
                } else if (o instanceof OptimisticScheme) {
                    name = ((OptimisticScheme) o).getSchemeRef();
                } else if (o instanceof NearScheme) {
                    name = ((NearScheme) o).getSchemeRef();
                } else if (o instanceof LocalScheme) {
                    name = ((LocalScheme) o).getSchemeRef();
                } else if (o instanceof ReadWriteBackingMapScheme) {
                    name = ((ReadWriteBackingMapScheme) o).getSchemeRef();
                } else if (o instanceof OverflowScheme) {
                    name = ((OverflowScheme) o).getSchemeRef();
                } else if (o instanceof ExternalScheme) {
                    name = ((ExternalScheme) o).getSchemeRef();
                } else if (o instanceof InvocationScheme) {
                    name = ((InvocationScheme) o).getSchemeRef();
                } else if (o instanceof ProxyScheme) {
                    name = ((ProxyScheme) o).getSchemeRef();
                } else if (o instanceof TransactionalScheme) {
                    name = ((TransactionalScheme) o).getSchemeRef();
                }
            }
        } catch (Exception e) {
            name = null;
        }

        return name;
    }

    public List<Scheme> getSchemes(List schemeList) {
        if (schemeNamesList == null) {
            schemeNamesList = new ArrayList<Scheme>();
            for (Object o : schemeList) {
                schemeNamesList.add(new Scheme(o));
            }
        }

        return schemeNamesList;
    }

    protected CacheConfig getCacheConfig() {
        return ((CacheConfigDataObject) support.getDataObject()).getCacheConfig();
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
        return "Schemes";
    }

    @Override
    public Image getIcon() {
        return ImageUtilities.loadImage(org.openide.util.NbBundle.getMessage(CacheConfigSchemeView.class, "CacheConfig.file.icon"));
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

    @Override
    public void actionPerformed(ActionEvent e) {
        // Listen for Remove Action Events
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeScheme(Object o) {
        CacheConfig cacheConfig = getCacheConfig();
        List schemeList = cacheConfig.getCachingSchemes().getDistributedSchemeOrTransactionalSchemeOrReplicatedSchemeOrOptimisticSchemeOrLocalSchemeOrDiskSchemeOrExternalSchemeOrPagedExternalSchemeOrOverflowSchemeOrClassSchemeOrNearSchemeOrVersionedNearSchemeOrInvocationSchemeOrReadWriteBackingMapSchemeOrVersionedBackingMapSchemeOrRemoteCacheSchemeOrRemoteInvocationSchemeOrProxyScheme();
        if (o != null) {
            schemeList.remove(o);
        }
        refresh(cacheConfig);
        setModified();
    }

    @Override
    public void modifyScheme(Object o) {
        setModified();
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

        jScrollPane1 = new javax.swing.JScrollPane();
        topPanel = new javax.swing.JPanel();
        btnAddScheme = new javax.swing.JButton();
        jToggleButton1 = new javax.swing.JToggleButton();
        schemesPanel = new javax.swing.JPanel();
        distributedPanel = new javax.swing.JPanel();
        replicatedPanel = new javax.swing.JPanel();

        btnAddScheme.setText(org.openide.util.NbBundle.getMessage(CacheConfigSchemeView.class, "CacheConfigSchemeView.btnAddScheme.text")); // NOI18N
        btnAddScheme.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddSchemeActionPerformed(evt);
            }
        });

        jToggleButton1.setSelected(true);
        jToggleButton1.setText(org.openide.util.NbBundle.getMessage(CacheConfigSchemeView.class, "CacheConfigSchemeView.jToggleButton1.text")); // NOI18N
        jToggleButton1.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        distributedPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(CacheConfigSchemeView.class, "CacheConfigSchemeView.distributedPanel.border.title"))); // NOI18N

        javax.swing.GroupLayout distributedPanelLayout = new javax.swing.GroupLayout(distributedPanel);
        distributedPanel.setLayout(distributedPanelLayout);
        distributedPanelLayout.setHorizontalGroup(
            distributedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 407, Short.MAX_VALUE)
        );
        distributedPanelLayout.setVerticalGroup(
            distributedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );

        replicatedPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(CacheConfigSchemeView.class, "CacheConfigSchemeView.replicatedPanel.border.title"))); // NOI18N

        javax.swing.GroupLayout replicatedPanelLayout = new javax.swing.GroupLayout(replicatedPanel);
        replicatedPanel.setLayout(replicatedPanelLayout);
        replicatedPanelLayout.setHorizontalGroup(
            replicatedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 407, Short.MAX_VALUE)
        );
        replicatedPanelLayout.setVerticalGroup(
            replicatedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 27, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout schemesPanelLayout = new javax.swing.GroupLayout(schemesPanel);
        schemesPanel.setLayout(schemesPanelLayout);
        schemesPanelLayout.setHorizontalGroup(
            schemesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(distributedPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(replicatedPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        schemesPanelLayout.setVerticalGroup(
            schemesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(schemesPanelLayout.createSequentialGroup()
                .addComponent(distributedPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(replicatedPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(251, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout topPanelLayout = new javax.swing.GroupLayout(topPanel);
        topPanel.setLayout(topPanelLayout);
        topPanelLayout.setHorizontalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, topPanelLayout.createSequentialGroup()
                .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(topPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(schemesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(topPanelLayout.createSequentialGroup()
                        .addComponent(jToggleButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 332, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddScheme)))
                .addContainerGap())
        );
        topPanelLayout.setVerticalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jToggleButton1)
                    .addComponent(btnAddScheme))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(schemesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(topPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 441, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        schemesPanel.setVisible(jToggleButton1.isSelected());
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void btnAddSchemeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddSchemeActionPerformed
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                EditSchemeDialog dialog = new EditSchemeDialog(null, true, getSchemes(schemeNamesList));
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
                if (dialog.getReturnStatus() == dialog.RET_OK) {
                    Object o = dialog.getScheme();
                    CacheConfig cacheConfig = getCacheConfig();
                    List schemeList = cacheConfig.getCachingSchemes().getDistributedSchemeOrTransactionalSchemeOrReplicatedSchemeOrOptimisticSchemeOrLocalSchemeOrDiskSchemeOrExternalSchemeOrPagedExternalSchemeOrOverflowSchemeOrClassSchemeOrNearSchemeOrVersionedNearSchemeOrInvocationSchemeOrReadWriteBackingMapSchemeOrVersionedBackingMapSchemeOrRemoteCacheSchemeOrRemoteInvocationSchemeOrProxyScheme();
                    schemeList.add(o);
                    refresh(cacheConfig);
                    setModified();
                }
            }
        });
    }//GEN-LAST:event_btnAddSchemeActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddScheme;
    private javax.swing.JPanel distributedPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JPanel replicatedPanel;
    private javax.swing.JPanel schemesPanel;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables

}
