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

package org.netbeans.modules.hibernate.hqleditor.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.hibernate.cfg.model.HibernateConfiguration;
import org.netbeans.modules.hibernate.loaders.cfg.HibernateCfgDataNode;
import org.netbeans.modules.hibernate.service.HibernateEnvironment;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.Utilities;
import org.openide.windows.TopComponentGroup;

/**
 * HQL editor top component.
 * 
 * @author Vadiraj Deshpande (Vadiraj.Deshpande@Sun.COM)
 */
public final class HQLEditorTopComponent extends TopComponent {

    private static HQLEditorTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/netbeans/modules/hibernate/hqleditor/ui/resources/runsql.png";

    
    private HashMap<String, HibernateConfiguration> hibernateConfigMap = new HashMap<String, HibernateConfiguration>();
   
    private static final String PREFERRED_ID = "HQLEditorTopComponent";

    private HQLEditorTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(HQLEditorTopComponent.class, "CTL_HQLEditorTopComponent"));
        setToolTipText(NbBundle.getMessage(HQLEditorTopComponent.class, "HINT_HQLEditorTopComponent"));
        setIcon(Utilities.loadImage(ICON_PATH, true));
    }

    public void fillHibernateConfigurations() {
        // Lookup the global selection to see of any cfg or hbm file is selected.
        Lookup.Template lookupTemplate = new Lookup.Template(HibernateCfgDataNode.class);
        Lookup.Result res = Utilities.actionsGlobalContext().lookup (lookupTemplate);
        ArrayList<HibernateCfgDataNode> configDataNodes = new ArrayList<HibernateCfgDataNode>();
        for(Object o : res.allItems()) {
            if(o instanceof HibernateCfgDataNode) {
                configDataNodes.add((HibernateCfgDataNode)o);
            }
        }
        
        if(configDataNodes.size() != 0) {
            
        } else {
            // NO selection of cfg file found.
            Project mainProject = OpenProjects.getDefault().getMainProject();
            if(mainProject == null) {
                // No main project selected..
                return;
            }
            HibernateEnvironment env = mainProject.getLookup().lookup(HibernateEnvironment.class);
            if(env == null) {
                return;
            }
            ArrayList<HibernateConfiguration> configurations = env.getAllHibernateConfigurationsFromProject();
            for(HibernateConfiguration hibernateConfiguration : configurations) {
                hibernateConfigMap.put(hibernateConfiguration.getSessionFactory().getAttributeValue("name"),  //NOI18N
                        hibernateConfiguration);
            }
            hibernateConfigurationComboBox.setModel(new DefaultComboBoxModel(hibernateConfigMap.keySet().toArray()));
        }
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolBar = new javax.swing.JToolBar();
        hibernateConfigurationComboBox = new javax.swing.JComboBox();
        toolbarSeparator = new javax.swing.JToolBar.Separator();
        runHQLButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        hqlEditor = new javax.swing.JEditorPane();

        toolBar.setRollover(true);

        toolBar.add(hibernateConfigurationComboBox);
        toolBar.add(toolbarSeparator);

        runHQLButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/hibernate/hqleditor/ui/resources/runsql.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(runHQLButton, org.openide.util.NbBundle.getMessage(HQLEditorTopComponent.class, "HQLEditorTopComponent.runHQLButton.text")); // NOI18N
        runHQLButton.setFocusable(false);
        runHQLButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        runHQLButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(runHQLButton);

        jScrollPane1.setViewportView(hqlEditor);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(toolBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(toolBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 377, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox hibernateConfigurationComboBox;
    private javax.swing.JEditorPane hqlEditor;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton runHQLButton;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JToolBar.Separator toolbarSeparator;
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized HQLEditorTopComponent getDefault() {
        if (instance == null) {
            instance = new HQLEditorTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the HQLEditorTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized HQLEditorTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(HQLEditorTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof HQLEditorTopComponent) {
            return (HQLEditorTopComponent) win;
        }
        Logger.getLogger(HQLEditorTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID +
                "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        TopComponentGroup hqlEditorTCGroup = WindowManager.getDefault().findTopComponentGroup("HQLEditorGroup"); //NOI18N
        if(hqlEditorTCGroup != null) {
            hqlEditorTCGroup.open();
        }
    }

    @Override
    public void componentClosed() {
        TopComponentGroup hqlEditorTCGroup = WindowManager.getDefault().findTopComponentGroup("HQLEditorGroup"); //NOI18N
        if(hqlEditorTCGroup != null) {
            hqlEditorTCGroup.close();
        }
//        TopComponent mvTopComponent = WindowManager.getDefault().findTopComponent("HQLEditor-Output");
//        mvTopComponent.close();
    }

    /** replaces this in object stream */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    final static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return HQLEditorTopComponent.getDefault();
        }
    }
}
