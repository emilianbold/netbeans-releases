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
package org.netbeans.bluej.ui.window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Arrays;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.bluej.api.BluejLogicalViewProvider;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.ListView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
public final class BluejViewTopComponent extends TopComponent implements ExplorerManager.Provider {
    
    private static final long serialVersionUID = 1L;
    
    private static BluejViewTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/netbeans/bluej/ui/window/bluejview.png"; // NOI18N
    
    private static final String PREFERRED_ID = "BluejViewTopComponent"; // NOI18N
    private ListView view;
    private ExplorerManager manager;
    
    private ItemListener itemListener;
    private LookupProvider lookProvider;
    
    private BluejViewTopComponent() {
        manager = new ExplorerManager();
        ActionMap map = getActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        map.put("delete", ExplorerUtils.actionDelete(manager, true)); // NOI18N

        initComponents();
        setName(NbBundle.getMessage(BluejViewTopComponent.class, "CTL_BluejViewTopComponent"));
        setToolTipText(NbBundle.getMessage(BluejViewTopComponent.class, "HINT_BluejViewTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        view = new ListView() {
            protected JList createList() {
                JList list = super.createList();
                list.setCellRenderer(new HackedNodeRenderer());
                return list;
            }
        };
        add(view, BorderLayout.CENTER);
        lookProvider = new LookupProvider();
        associateLookup( new ProxyLookup(new Lookup[] {
            ExplorerUtils.createLookup(manager, map),
            Lookups.proxy(lookProvider)
        }));
        btnUp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Node nd = manager.getExploredContext();
                if (nd.getParentNode() != null) {
                    manager.setExploredContext(nd.getParentNode());
                }
            }
        });
        manager.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (ExplorerManager.PROP_EXPLORED_CONTEXT.equals(evt.getPropertyName())) {
                    Node cont = manager.getExploredContext();
                    btnUp.setEnabled(cont != null && cont.getParentNode() != null);
                }
            }
        });
        
        
        comProject.setEditable(false);
        comProject.setMinimumSize(new Dimension(150, 22));
        comProject.setPreferredSize(new Dimension(150, 22));
        itemListener = new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                // change main project and selected project in the BJ view
                if (e.getStateChange() == ItemEvent.SELECTED ||
                    (e.getStateChange() == ItemEvent.DESELECTED && 
                     e.getItemSelectable().getSelectedObjects() == null ||
                     e.getItemSelectable().getSelectedObjects().length == 0)) {
                    updateContent();
                }
            }
        };
    }
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    private void updateContent() {
        Project project = OpenedBluejProjects.getInstance().getSelectedProject();
        if ( project != null && Arrays.asList(OpenProjects.getDefault().getOpenProjects()).contains(project)) {
            // if it's not in the list of opened projects we probably are closing multiple projects as once (or shutting down)
            lookProvider.setLookup(Lookups.singleton(project));
            BluejLogicalViewProvider provider = (BluejLogicalViewProvider) project.getLookup().lookup(BluejLogicalViewProvider.class);
            manager.setRootContext(provider.getBigIconRootNode());
            Project np = (Project)getLookup().lookup(Project.class);
            OpenProjects.getDefault().setMainProject(np);
        } else {
            lookProvider.setLookup(Lookup.EMPTY);
            manager.setRootContext(new AbstractNode(Children.LEAF));
            getLookup().lookup(Project.class);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        pnlToolbar = new javax.swing.JPanel();
        lblProject = new javax.swing.JLabel();
        comProject = new javax.swing.JComboBox();
        btnUp = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        pnlToolbar.setMinimumSize(new java.awt.Dimension(200, 10));
        lblProject.setLabelFor(comProject);
        org.openide.awt.Mnemonics.setLocalizedText(lblProject, org.openide.util.NbBundle.getMessage(BluejViewTopComponent.class, "lblProject.text"));

        org.openide.awt.Mnemonics.setLocalizedText(btnUp, org.openide.util.NbBundle.getMessage(BluejViewTopComponent.class, "btnUp.label"));

        org.jdesktop.layout.GroupLayout pnlToolbarLayout = new org.jdesktop.layout.GroupLayout(pnlToolbar);
        pnlToolbar.setLayout(pnlToolbarLayout);
        pnlToolbarLayout.setHorizontalGroup(
            pnlToolbarLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlToolbarLayout.createSequentialGroup()
                .addContainerGap()
                .add(lblProject)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(comProject, 0, 219, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(btnUp)
                .addContainerGap())
        );
        pnlToolbarLayout.setVerticalGroup(
            pnlToolbarLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlToolbarLayout.createSequentialGroup()
                .add(6, 6, 6)
                .add(pnlToolbarLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblProject)
                    .add(btnUp)
                    .add(comProject, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );
        add(pnlToolbar, java.awt.BorderLayout.NORTH);

    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnUp;
    private javax.swing.JComboBox comProject;
    private javax.swing.JLabel lblProject;
    private javax.swing.JPanel pnlToolbar;
    // End of variables declaration//GEN-END:variables
    
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized BluejViewTopComponent getDefault() {
        if (instance == null) {
            instance = new BluejViewTopComponent();
        }
        return instance;
    }
    
    /**
     * Obtain the BluejViewTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized BluejViewTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot find BluejView component. It will not be located properly in the window system."); // NOI18N
            return getDefault();
        }
        if (win instanceof BluejViewTopComponent) {
            return (BluejViewTopComponent)win;
        }
        ErrorManager.getDefault().log(ErrorManager.WARNING, "There seem to be multiple components with the '" + PREFERRED_ID + "' ID. That is a potential source of errors and unexpected behavior."); // NOI18N
        return getDefault();
    }
    
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    public void componentOpened() {
        comProject.setModel(OpenedBluejProjects.getInstance().getComboModel());
        updateContent();
        comProject.addItemListener(itemListener);
    }
    
    public void componentClosed() {
        comProject.removeItemListener(itemListener);
        comProject.setModel(new DefaultComboBoxModel());
    }
    
    /** replaces this in object stream */
    public Object writeReplace() {
        return new ResolvableHelper();
    }
    
    protected String preferredID() {
        return PREFERRED_ID;
    }
    
    final static class ResolvableHelper implements Serializable {
        private static final long serialVersionUID = 1L;
        public Object readResolve() {
            return BluejViewTopComponent.getDefault();
        }
    }

    final static class LookupProvider implements Lookup.Provider {

        private Lookup lookup;

        public void setLookup(Lookup lkp) {
            lookup = lkp;
        }
        public Lookup getLookup() {
            return lookup == null ? Lookup.EMPTY : lookup;
        }

    }
}
