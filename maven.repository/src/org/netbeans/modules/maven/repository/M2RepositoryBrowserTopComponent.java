/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.repository;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.List;
import javax.swing.ActionMap;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.modules.maven.indexer.api.QueryField;
import org.netbeans.modules.maven.indexer.api.RepositoryIndexer;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.repository.register.RepositoryRegisterUI;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.awt.Toolbar;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 * @author mkleint
 */
public final class M2RepositoryBrowserTopComponent extends TopComponent implements ExplorerManager.Provider {

    private static M2RepositoryBrowserTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/netbeans/modules/maven/repository/MavenRepoBrowser.png"; //NOI18N
    private static final String PREFERRED_ID = "M2RepositoryBrowserTopComponent"; //NOI18N
    private BeanTreeView btv;
    private ExplorerManager manager;

    private M2RepositoryBrowserTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(M2RepositoryBrowserTopComponent.class, "CTL_M2RepositoryBrowserTopComponent")); //NOI18N
        setToolTipText(NbBundle.getMessage(M2RepositoryBrowserTopComponent.class, "HINT_M2RepositoryBrowserTopComponent")); //NOI18N
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        btv = new BeanTreeView();
        btv.setRootVisible(false);
        btv.getAccessibleContext().setAccessibleName(NbBundle.getMessage(M2RepositoryBrowserTopComponent.class, "A11Y_BrowserName")); //NOI18N
        btv.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(M2RepositoryBrowserTopComponent.class, "A11Y_BrowserDescription")); //NOI18N
        manager = new ExplorerManager();
        ActionMap map = getActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        map.put("delete", ExplorerUtils.actionDelete(manager, true)); //NOI18N
        associateLookup(ExplorerUtils.createLookup(manager, map));
        pnlBrowse.add(btv, BorderLayout.CENTER);

//        RepositoryUtil.getDefaultRepositoryIndexer().addIndexChangeListener(new ChangeListener() {
//
//            public void stateChanged(ChangeEvent e) {
//                manager.setRootContext(createRootNode());
//            }
//        });
        hideFind();
    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }

    private void hideFind() {
        pnlFind.removeAll();
        pnlFind.setVisible(false);
        
        jSplitPane1.setDividerLocation(1.0);
        jSplitPane1.setEnabled(false);
    }
    
    private void showFind(List<QueryField> fields, DialogDescriptor dd) {
        FindResultsPanel pnl = new FindResultsPanel(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hideFind();
            }
        }, dd);
        pnl.find(fields);
        pnlFind.add(pnl);
        pnlFind.setVisible(true);
        jSplitPane1.setEnabled(true);
        jSplitPane1.setDividerLocation(0.5);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlExplorer = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        pnlBrowse = new javax.swing.JPanel();
        pnlFind = new javax.swing.JPanel();
        jToolBar1 = new EditorToolbar();
        btnIndex = new javax.swing.JButton();
        btnAddRepo = new javax.swing.JButton();
        btnFind = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        pnlExplorer.setLayout(new java.awt.BorderLayout());

        jSplitPane1.setDividerSize(3);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        pnlBrowse.setLayout(new java.awt.BorderLayout());
        jSplitPane1.setTopComponent(pnlBrowse);

        pnlFind.setLayout(new java.awt.BorderLayout());
        jSplitPane1.setRightComponent(pnlFind);

        pnlExplorer.add(jSplitPane1, java.awt.BorderLayout.CENTER);

        add(pnlExplorer, java.awt.BorderLayout.CENTER);

        jToolBar1.setFloatable(false);

        btnIndex.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/maven/repository/refreshRepo.png"))); // NOI18N
        btnIndex.setToolTipText(org.openide.util.NbBundle.getMessage(M2RepositoryBrowserTopComponent.class, "LBL_REPO_Update_Indexes")); // NOI18N
        btnIndex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIndexActionPerformed(evt);
            }
        });
        jToolBar1.add(btnIndex);
        btnIndex.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(M2RepositoryBrowserTopComponent.class, "M2RepositoryBrowserTopComponent.btnIndex.AccessibleContext.accessibleName")); // NOI18N
        btnIndex.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(M2RepositoryBrowserTopComponent.class, "M2RepositoryBrowserTopComponent.btnIndex.AccessibleContext.accessibleDescription")); // NOI18N

        btnAddRepo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/maven/repository/AddRepo.png"))); // NOI18N
        btnAddRepo.setToolTipText(org.openide.util.NbBundle.getMessage(M2RepositoryBrowserTopComponent.class, "LBL_Add_Repo", new Object[] {})); // NOI18N
        btnAddRepo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddRepo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddRepo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddRepoActionPerformed(evt);
            }
        });
        jToolBar1.add(btnAddRepo);
        btnAddRepo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(M2RepositoryBrowserTopComponent.class, "M2RepositoryBrowserTopComponent.btnAddRepo.AccessibleContext.accessibleName")); // NOI18N
        btnAddRepo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(M2RepositoryBrowserTopComponent.class, "M2RepositoryBrowserTopComponent.btnAddRepo.AccessibleContext.accessibleDescription")); // NOI18N

        btnFind.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/maven/repository/FindInRepo.png"))); // NOI18N
        btnFind.setToolTipText(org.openide.util.NbBundle.getMessage(M2RepositoryBrowserTopComponent.class, "LBL_REPO_Find")); // NOI18N
        btnFind.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnFind.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnFind.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFindActionPerformed(evt);
            }
        });
        jToolBar1.add(btnFind);
        btnFind.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(M2RepositoryBrowserTopComponent.class, "M2RepositoryBrowserTopComponent.btnFind.AccessibleContext.accessibleName")); // NOI18N
        btnFind.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(M2RepositoryBrowserTopComponent.class, "M2RepositoryBrowserTopComponent.btnFind.AccessibleContext.accessibleDescription")); // NOI18N

        add(jToolBar1, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents
    private void btnIndexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIndexActionPerformed
        btnIndex.setEnabled(false);
        new RequestProcessor("Maven Repo Index Transfer/Scan").post(new Runnable() {  // NOI18N
            public void run() {
                List<RepositoryInfo> infos = RepositoryPreferences.getInstance().getRepositoryInfos();
                for (RepositoryInfo ri : infos) {
                    RepositoryIndexer.indexRepo(ri);
                }
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        btnIndex.setEnabled(true);
                    }
                });
            }
        });

    }//GEN-LAST:event_btnIndexActionPerformed

private void btnAddRepoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddRepoActionPerformed
    final RepositoryRegisterUI rrui = new RepositoryRegisterUI();
    rrui.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(M2RepositoryBrowserTopComponent.class, "LBL_Add_Repo"));
    DialogDescriptor dd = new DialogDescriptor(rrui, NbBundle.getMessage(M2RepositoryBrowserTopComponent.class, "LBL_Add_Repo"));
    dd.setClosingOptions(new Object[]{
                rrui.getButton(),
                DialogDescriptor.CANCEL_OPTION
            });
    dd.setOptions(new Object[]{
                rrui.getButton(),
                DialogDescriptor.CANCEL_OPTION
            });
    Object ret = DialogDisplayer.getDefault().notify(dd);
    if (rrui.getButton() == ret) {
        final  RepositoryInfo info = rrui.getRepositoryInfo();
        RepositoryPreferences.getInstance().addOrModifyRepositoryInfo(info);
        manager.setRootContext(createRootNode());
        RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    RepositoryIndexer.indexRepo(info);
                }
            });
    }
        
}//GEN-LAST:event_btnAddRepoActionPerformed

private void btnFindActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFindActionPerformed
    hideFind();
    final FindInRepoPanel pnl = new FindInRepoPanel();
    pnl.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(M2RepositoryBrowserTopComponent.class, "ACSD_Find_In_Repositories"));
    final DialogDescriptor dd = new DialogDescriptor(pnl, org.openide.util.NbBundle.getMessage(M2RepositoryBrowserTopComponent.class, "TIT_Find_In_Repositories"));
    pnl.attachDesc(dd);
    Object ret = DialogDisplayer.getDefault().notify(dd);
    if (ret == DialogDescriptor.OK_OPTION) {
        showFind(pnl.getQuery(), dd);
    }
    
}//GEN-LAST:event_btnFindActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddRepo;
    private javax.swing.JButton btnFind;
    private javax.swing.JButton btnIndex;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JPanel pnlBrowse;
    private javax.swing.JPanel pnlExplorer;
    private javax.swing.JPanel pnlFind;
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized M2RepositoryBrowserTopComponent getDefault() {
        if (instance == null) {
            instance = new M2RepositoryBrowserTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the M2RepositoryBrowserTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized M2RepositoryBrowserTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "Cannot find MyWindow component. It will not be located properly in the window system."); //NOI18N
            return getDefault();
        }
        if (win instanceof M2RepositoryBrowserTopComponent) {
            return (M2RepositoryBrowserTopComponent) win;
        }
        ErrorManager.getDefault().log(ErrorManager.WARNING,
                "There seem to be multiple components with the '" + PREFERRED_ID + //NOI18N
                "' ID. That is a potential source of errors and unexpected behavior."); //NOI18N
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                final Node root = createRootNode();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        manager.setRootContext(root);
                    }
                });
            }

        });
        manager.setRootContext(new AbstractNode(Children.LEAF));
    }

    @Override
    public void componentClosed() {
        manager.setRootContext(new AbstractNode(Children.LEAF));
    }

    @Override
    protected void componentActivated() {
        ExplorerUtils.activateActions(manager, true);
    }

    @Override
    protected void componentDeactivated() {
        ExplorerUtils.activateActions(manager, false);
    }



    @Override
    @SuppressWarnings("deprecation")
    public boolean requestFocusInWindow() {
        return btv.requestFocusInWindow();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void requestFocus() {
        btv.requestFocus();
    }

    @Override
    /** replaces this in object stream */
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    private Node createRootNode() {
        Children.Array array = new Children.Array();
        List<RepositoryInfo> infos = RepositoryPreferences.getInstance().getRepositoryInfos();
        for (RepositoryInfo ri : infos) {
            if (ri.isRemoteDownloadable() || ri.isLocal()) {
             array.add(new Node[]{new RepositoryNode(ri)});
            }
        }

        return new AbstractNode(array);
    }



    final static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return M2RepositoryBrowserTopComponent.getDefault();
        }
    }

    public static class EditorToolbar extends Toolbar {
        public EditorToolbar() {
            Border b = UIManager.getBorder("Nb.Editor.Toolbar.border"); //NOI18N
            setBorder(b);
            if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
                setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
            }
        }

        @Override
        public String getUIClassID() {
            if( UIManager.get("Nb.Toolbar.ui") != null ) { //NOI18N
                return "Nb.Toolbar.ui"; //NOI18N
            }
            return super.getUIClassID();
        }

        @Override
        public String getName() {
            return "editorToolbar"; //NOI18N
        }
    }
}
