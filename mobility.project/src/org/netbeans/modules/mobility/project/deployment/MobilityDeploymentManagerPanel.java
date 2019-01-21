/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.mobility.project.deployment;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.netbeans.spi.mobility.deployment.DeploymentPlugin;
import org.netbeans.spi.mobility.project.ui.customizer.support.VisualPropertySupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.awt.Mnemonics;

/**
 *
 * 
 */
public class MobilityDeploymentManagerPanel extends JPanel implements ExplorerManager.Provider, PropertyChangeListener {

    private final ExplorerManager manager = new ExplorerManager();
    private final BeanTreeView btw = new BeanTreeView();
    private final MobilityDeploymentProperties props = new MobilityDeploymentProperties(new RequestProcessor("Mobility Deployment Dialog")); //NOI18N
    private final VisualPropertySupport vps = VisualPropertySupport.getDefault(props);
    private final String initialTypeName;

    public static synchronized String manageDeployment(final String deploymentTypeDisplayName, final String instance) {
        final String instanceName[] = new String[1];
        Thread r1;
        //do not block AWT, but block the dialog until properties are loaded!
        RequestProcessor.getDefault().post(r1 = new Thread() {

            @Override
            public void run() {
                Thread r2;
                Lookup.Result deployments = Lookup.getDefault().lookup(new Lookup.Template<DeploymentPlugin>(DeploymentPlugin.class));
                DeploymentPropertiesHandler.loadDeploymentProperties(deployments.allInstances());
                SwingUtilities.invokeLater(r2 = new Thread() {
                    @Override
                    public void run() {
                        JButton closeButton = new JButton(); 
                        Mnemonics.setLocalizedText(closeButton , 
                                NbBundle.getMessage(MobilityDeploymentManagerPanel.class, 
                                "LBL_closeButton"));//NOI18N
                        closeButton.getAccessibleContext().setAccessibleName(
                                NbBundle.getMessage(
                                MobilityDeploymentManagerPanel.class,
                                "ACCESSIBLE_NAME_closeButton")); //NOI18N
                        closeButton.getAccessibleContext().setAccessibleDescription(
                                NbBundle.getMessage(
                                MobilityDeploymentManagerPanel.class,
                                "ACCESSIBLE_DESCRIPTION_closeButton")); //NOI18N
                        MobilityDeploymentManagerPanel mdmp = new MobilityDeploymentManagerPanel(deploymentTypeDisplayName, instance);
                        DialogDescriptor desc = new DialogDescriptor(mdmp,
                                NbBundle.getMessage(
                                    MobilityDeploymentManagerPanel.class,
                                    "Title_DeploymentManager"),
                                    true, new Object[]{closeButton},
                                    DialogDescriptor.CLOSED_OPTION,
                                    DialogDescriptor.DEFAULT_ALIGN,
                                    new HelpCtx(MobilityDeploymentManagerPanel.class),
                                    null);
                        desc.setClosingOptions(new Object[]{closeButton});
                        DialogDisplayer.getDefault().notify( desc );  //NOI18N
                        instanceName[0] = mdmp.getSelectedInstanceName();
                    }
                });
                try {
                    r2.join();
                } catch (InterruptedException ex) {
                }
            }
        });
        try {
            r1.join();
        } catch (InterruptedException ex) {
        }
        return instanceName[0];
    }

    /**
     * Creates new form MobilityDeploymentManagerPanel
     */
    private MobilityDeploymentManagerPanel(String deploymentTypeDisplayName, String instanceName) {
        this.initialTypeName = deploymentTypeDisplayName;
        initComponents();
        btw.setRootVisible(false);
        btw.setPopupAllowed(false);
        btw.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage(MobilityDeploymentManagerPanel.class, "MobilityDeploymentManager.jLabel1.text")); //NOI18N
        btw.getAccessibleContext().setAccessibleName( NbBundle.getMessage(MobilityDeploymentManagerPanel.class, "MobilityDeploymentManager.jLabel1.text")); //NOI18N   
        manager.addPropertyChangeListener(this);
        jPanel1.add(btw, BorderLayout.CENTER);
        Children.Array ch = new Children.Array() {
            protected Collection<Node> initCollection() {
                Collection<Node> nodes = new ArrayList();
                for (DeploymentPlugin d : Lookup.getDefault().lookupAll(DeploymentPlugin.class)) {
                    if (d.getGlobalPropertyDefaultValues().size() > 0) {
                        nodes.add(new DeploymentTypeNode(d));
                    }
                }
                return nodes;
            }
        };
        manager.setRootContext(new AbstractNode(ch));
        Node selType = deploymentTypeDisplayName != null ? ch.findChild(deploymentTypeDisplayName) : null;
        Node selInstance = selType != null && instanceName != null ? selType.getChildren().findChild(instanceName) : null;
        if (selType != null || selInstance != null) {
            try {
                manager.setExploredContextAndSelection(selType, new Node[]{selInstance == null ? selType : selInstance});
            } catch (PropertyVetoException pve) {
            }
        }
    }

    public void addNotify() {
        super.addNotify();
        Point p = btw.getViewport().getViewPosition();
        if (p.x != 0) {
            p.setLocation(0, p.y);
            btw.getViewport().setViewPosition(p);
        }
    }

    public String getSelectedInstanceName() {
        if (initialTypeName == null) {
            return null;
        }
        Node[] n = manager.getSelectedNodes();
        return n.length != 1 || !initialTypeName.equals(n[0].getParentNode().getDisplayName()) ? null : n[0].getName();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(btw);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, NbBundle.getMessage(MobilityDeploymentManagerPanel.class, "MobilityDeploymentManager.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 0, 11);
        add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MobilityDeploymentManagerPanel.class, "ACCESSIBLE_NAME_jLabel1")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MobilityDeploymentManagerPanel.class, "ACCESSIBLE_DESCRIPTION_jLabel1")); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setMaximumSize(new java.awt.Dimension(270, 350));
        jPanel1.setOpaque(false);
        jPanel1.setPreferredSize(new java.awt.Dimension(350, 350));
        jPanel1.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 0, 0);
        add(jPanel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, NbBundle.getMessage(MobilityDeploymentManagerPanel.class, "MobilityDeploymentManager.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createInstance(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 11, 0);
        add(jButton1, gridBagConstraints);
        jButton1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MobilityDeploymentManagerPanel.class, "ACCESSIBLE_NAME_jButton1")); // NOI18N
        jButton1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MobilityDeploymentManagerPanel.class, "ACCESSIBLE_DESCRIPTION_jButton1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, NbBundle.getMessage(MobilityDeploymentManagerPanel.class, "MobilityDeploymentManager.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeInstance(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 5, 11, 11);
        add(jButton2, gridBagConstraints);
        jButton2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MobilityDeploymentManagerPanel.class, "ACCESSIBLE_NAME_jButton2")); // NOI18N
        jButton2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MobilityDeploymentManagerPanel.class, "ACCESSIBLE_DESCRIPTION_jButton2")); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 12, 1, 1));
        jPanel2.setPreferredSize(new java.awt.Dimension(500, 350));
        jPanel2.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 0, 11);
        add(jPanel2, gridBagConstraints);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MobilityDeploymentManagerPanel.class, "ACCESSIBLE_NAME_jPanel")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MobilityDeploymentManagerPanel.class, "ACCESSIBLE_DESCRIPTION_jPanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    private void removeInstance(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeInstance
        Node n[] = manager.getSelectedNodes();
        if (n.length == 1 && n[0].hasCustomizer()) {
            n[0].getCookie(InstanceNode.class).remove();
        }
    }//GEN-LAST:event_removeInstance

    private void createInstance(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createInstance
        Node n[] = manager.getSelectedNodes();
        if (n.length == 1) {
            if (n[0].hasCustomizer()) {
                n[0] = n[0].getParentNode();
            }
            createNewInstance(n[0].getCookie(DeploymentTypeNode.class).getPlugin());
        } else {
            createNewInstance(null);
        }
    }//GEN-LAST:event_createInstance

    private void createNewInstance(DeploymentPlugin d) {
        NewInstanceDialog nid = new NewInstanceDialog(props, d);
        DialogDescriptor dd = new DialogDescriptor(nid, NbBundle.getMessage(MobilityDeploymentManagerPanel.class, "TitleNewInstance"), true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(NewInstanceDialog.class), null); //NOI18N
        nid.setDialogDescriptor(dd);
        nid.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MobilityDeploymentManagerPanel.class, "TitleNewInstance"));
        nid.getAccessibleContext().setAccessibleName(NbBundle.getMessage(MobilityDeploymentManagerPanel.class, "TitleNewInstance"));
        if (DialogDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dd))) {
            DeploymentPlugin dp = nid.getDeploymentPlugin();
            if (dp != null) {
                String name = nid.getInstanceName();
                props.createInstance(dp.getDeploymentMethodName(), name);
                Node n = manager.getRootContext().getChildren().findChild(dp.getDeploymentMethodDisplayName());
                if (n != null) {
                    try {
                        DeploymentTypeNode dtn = n.getCookie(DeploymentTypeNode.class);
                        dtn.refresh();
                        Node selInstance = n.getChildren().findChild(name);
                        manager.setExploredContextAndSelection(n, new Node[]{selInstance == null ? n : selInstance});
                    } catch (PropertyVetoException pve) {
                    }
                }
            }
        }
    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        Node n[] = manager.getSelectedNodes();
        jPanel2.removeAll();
        if (n.length == 1) {
            boolean instance = n[0].hasCustomizer();
            if (instance) {
                jPanel2.add(n[0].getCustomizer(), BorderLayout.CENTER);
            }
            jButton2.setEnabled(instance);
        } else {
            jButton2.setEnabled(false);
        }
        jPanel2.validate();
        jPanel2.repaint();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    // End of variables declaration//GEN-END:variables
    private class DeploymentTypeNode extends AbstractNode implements Cookie {

        private final DeploymentPlugin d;

        public DeploymentTypeNode(DeploymentPlugin d) {
            super(createChildren(d));
            this.d = d;
            getCookieSet().add(this);
            setName(d.getDeploymentMethodDisplayName());
            setIconBaseWithExtension("org/netbeans/modules/mobility/project/ui/resources/deploy.gif");//NOI18N
        }

        public void refresh() {
            setChildren(createChildren(d));
        }

        public DeploymentPlugin getPlugin() {
            return d;
        }
    }

    private Children.Array createChildren(DeploymentPlugin d) {
        final ArrayList<Node> ch = new ArrayList();
        for (String name : props.getInstanceList(d.getDeploymentMethodName())) {
            ch.add(new InstanceNode(d, name));
        }
        return new Children.Array() {

            protected Collection<Node> initCollection() {
                return ch;
            }
        };
    }

    private class InstanceNode extends AbstractNode implements Cookie {

        private final DeploymentPlugin d;

        public InstanceNode(DeploymentPlugin d, String name) {
            super(Children.LEAF);
            getCookieSet().add(this);
            this.d = d;
            setName(name);
        }

        public DeploymentPlugin getPlugin() {
            return d;
        }

        public boolean hasCustomizer() {
            return true;
        }

        public Component getCustomizer() {
            Component c = d.createGlobalCustomizerPanel();
            registerSubcomponents(c, MobilityDeploymentProperties.DEPLOYMENT_PREFIX + d.getDeploymentMethodName() + '.' + getName() + '.', d.getGlobalPropertyDefaultValues().keySet());
            return c;
        }

        public void remove() {
            props.removeInstance(d.getDeploymentMethodName(), getName());
            ((DeploymentTypeNode) getParentNode()).refresh();
        }
    }

    private void registerSubcomponents(Component c, String prefix, Set propertyNames) {
        String prop = c.getName();
        if (prop != null && propertyNames.contains(prop)) {
            prop = prefix + prop;
            if (c instanceof JCheckBox) {
                vps.register((JCheckBox) c, prop);
            } else if (c instanceof JRadioButton) {
                vps.register((JRadioButton) c, prop);
            } else if (c instanceof JComboBox) {
                vps.register((JComboBox) c, null, prop);
            } else if (c instanceof JSlider) {
                vps.register((JSlider) c, prop);
            } else if (c instanceof JSpinner) {
                vps.register((JSpinner) c, prop);
            } else if (c instanceof JTextComponent) {
                vps.register((JTextComponent) c, prop);
            } else {
                assert false : "Unknown component type for registration";
            } //NOI18N
        }
        if (c instanceof Container) {
            for (Component sub : ((Container) c).getComponents()) {
                registerSubcomponents(sub, prefix, propertyNames);
            }
        }
    }
}
