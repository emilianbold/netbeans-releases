/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.soa.jca.base.inbound;

import org.netbeans.modules.soa.jca.base.GlobalRarRegistry;
import org.netbeans.modules.soa.jca.base.inbound.wizard.DefaultInboundConfigCustomPanel;
import org.netbeans.modules.soa.jca.base.spi.GlobalRarProvider;
import org.netbeans.modules.soa.jca.base.spi.InboundConfigCustomPanel;
import java.awt.Image;
import java.io.IOException;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author echou
 */
public class JcaMdbNode extends AbstractNode {

    private Action[] actions;
    private Project project;
    private JcaMdbModel model;

    public JcaMdbNode(Project project, JcaMdbModel model) {
        this(project, model, new InstanceContent());
    }

    private JcaMdbNode(Project project, JcaMdbModel model, InstanceContent content) {
        super(Children.LEAF, new AbstractLookup(content));
        content.add(this);
        actions = new Action[] {
            SystemAction.get(EditActivationAction.class),
            SystemAction.get(DeleteJcaMdbAction.class)
        };
        this.project = project;
        this.model = model;
    }

    @Override
    public String getDisplayName() {
        return model.getEjbName();
    }

    @Override
    public String getName() {
        return model.getEjbName();
    }

    @Override
    public Image getIcon(int type) {
        return Utilities.loadImage("org/netbeans/modules/soa/jca/base/inbound/resources/MessageNodeIcon.gif");
    }

    @Override
    public Image getOpenedIcon(int type) {
        return Utilities.loadImage("org/netbeans/modules/soa/jca/base/inbound/resources/MessageNodeIcon.gif");
    }

    @Override
    public Action[] getActions(boolean b) {
        return actions;
    }

    @Override
    public boolean canDestroy() {
        return true;
    }

    @Override
    public void destroy() throws IOException {
        super.destroy();

    }

    @Override
    public boolean hasCustomizer() {
        return false;
    }

    public Project getEnclosingProject() {
        return project;
    }

    public JcaMdbModel getJcaMdbModel() {
        return model;
    }


    public static class EditActivationAction extends NodeAction {

        public EditActivationAction() {
        }

        @Override
        public String getName() {
            return "Edit JCA Activation ...";
        }

        @Override
        protected void performAction(Node[] activatedNodes) {
            try {
                JcaMdbNode jcaMdbNode = activatedNodes[0].getLookup().lookup(JcaMdbNode.class);
                Project nodeProject = jcaMdbNode.getEnclosingProject();
                JcaMdbModel nodeModel = jcaMdbNode.getJcaMdbModel();

                GlobalRarProvider provider = GlobalRarRegistry.getInstance().getRar(nodeModel.getJcaModuleName());
                if (provider == null) {
                    return;
                }

                InboundConfigCustomPanel panel = provider.getInboundConfigCustomPanel(nodeProject, "");
                if (panel == null) {
                    panel = new DefaultInboundConfigCustomPanel();
                }

                panel.initFromInboundConfigData(nodeModel.getInboundConfigData());

                DialogDescriptor d = new DialogDescriptor(
                        panel,  // innerPane
                        java.util.ResourceBundle.getBundle("org/netbeans/modules/soa/jca/base/inbound/Bundle").getString("Edit_JCA_Activation"),  // title
                        true,  // modal
                        NotifyDescriptor.OK_CANCEL_OPTION,  // optionType
                        NotifyDescriptor.OK_OPTION,  // initialValue
                        DialogDescriptor.BOTTOM_ALIGN,  // align
                        HelpCtx.DEFAULT_HELP,  // helpctx
                        null  // actionListener
                );

                ChangeListener changeListener = new EditActivationChangeListener(panel, d);
                panel.addChangeListener(changeListener);

                DialogDisplayer.getDefault().notify(d);
                if (d.getValue() == NotifyDescriptor.OK_OPTION) {
                    panel.storeToInboundConfigData(nodeModel.getInboundConfigData());
                    EjbProjectUtil.modifyJcaMdbActivation(nodeProject.getProjectDirectory(), nodeModel);
                }

                panel.removeChangeListener(changeListener);

            } catch (Exception e) {
                Exceptions.printStackTrace(e);
                return;
            }

        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            if (activatedNodes.length == 1) {
                return true;
            }
            return false;
        }

        @Override
        protected boolean asynchronous() {
            return false;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }
    }

    public static class DeleteJcaMdbAction extends NodeAction {

        public DeleteJcaMdbAction() {

        }

        @Override
        public String getName() {
            return "Delete JCA ...";
        }

        @Override
        protected void performAction(Node[] activatedNodes) {
            try {
                Node node = activatedNodes[0];
                JcaMdbNode jcaMdbNode = node.getLookup().lookup(JcaMdbNode.class);
                Project nodeProject = jcaMdbNode.getEnclosingProject();
                JcaMdbModel nodeModel = jcaMdbNode.getJcaMdbModel();

                NotifyDescriptor d = new NotifyDescriptor.Confirmation(
                        java.util.ResourceBundle.getBundle("org/netbeans/modules/soa/jca/base/inbound/Bundle").getString("Delete_JCA_Message"),
                        java.util.ResourceBundle.getBundle("org/netbeans/modules/soa/jca/base/inbound/Bundle").getString("Delete_JCA_Title"),
                        NotifyDescriptor.OK_CANCEL_OPTION);

                DialogDisplayer.getDefault().notify(d);
                if (d.getValue() == NotifyDescriptor.OK_OPTION) {
                    EjbProjectUtil.deleteJcaMdb(nodeProject, nodeModel);
                }

                node.destroy();

            } catch (Exception e) {
                Exceptions.printStackTrace(e);
                return;
            }
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            if (activatedNodes.length == 1) {
                return true;
            }
            return false;
        }

        @Override
        protected boolean asynchronous() {
            return false;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }
    }

    public static class EditActivationChangeListener implements ChangeListener {

        private InboundConfigCustomPanel panel;
        private DialogDescriptor d;

        public EditActivationChangeListener(InboundConfigCustomPanel panel, DialogDescriptor d) {
            this.panel = panel;
            this.d = d;
        }

        public void stateChanged(ChangeEvent e) {
            if (panel.isPanelValid() == null) {
                d.setValid(true);
            } else {
                d.setValid(false);
            }
        }

    }

}
