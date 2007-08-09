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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.xslt.mapper.model;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.modules.xml.xam.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.Timer;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;
import org.netbeans.modules.xslt.model.Stylesheet;
import org.netbeans.modules.xslt.model.Template;
import org.netbeans.modules.xslt.model.XslModel;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexey
 */
public abstract class ModelBridge implements ComponentListener, PropertyChangeListener {

    private UpdateTimer updateTimer = new UpdateTimer();
    private XsltMapper mapper;

    public ModelBridge(XsltMapper mapper) {
        this.mapper = mapper;
    }

    public XsltMapper getMapper() {
        return this.mapper;
    }

    protected void subscribe(Model model) {
        if (model != null) {
            model.addComponentListener(this);
            model.addPropertyChangeListener(this);
        }
    }


    private class UpdateTimer {

        private Timer timer;

        public UpdateTimer() {
            timer = new Timer(100, new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    onModelChanged();
                }
            });
            timer.setRepeats(false);
        }

        public void onEvent() {
            if (timer.isRunning()) {
                timer.restart();
            } else {
                timer.start();
            }
        }
    }

    /**
     * This method is called whenever the model is change.
     */
    protected abstract void onModelChanged();

    protected boolean checkErrors() {
        String errorMessages = "";
        if (getMapper().getContext() != null) {
            XslModel xslModel = getMapper().getContext().getXSLModel();
            if (xslModel == null || xslModel.getState() != XslModel.State.VALID) {
                errorMessages += NbBundle.getMessage(XsltModelBridge.class, "MSG_Error_BadXSL"); // NOI18N
            }

            Stylesheet stylesheet = xslModel.getStylesheet();
            if (stylesheet == null) {
                errorMessages += NbBundle.getMessage(XsltModelBridge.class, "MSG_Error_NoStylesheet"); // NOI18N
            } else {
                List<Template> templates = stylesheet.getChildren(Template.class);
                boolean templateFound = false;
                for (Template t : templates) {
                    if (t.getMatch().equals("/")) {
                        templateFound = true;
                        break;
                    }
                }
                //
                if (!templateFound) {
                    errorMessages += NbBundle.getMessage(XsltModelBridge.class, "MSG_Error_NoRootTemplate"); // NOI18N
                }
            }
            //
            AXIComponent typeIn = getMapper().getContext().getSourceType();
            if (typeIn == null || typeIn.getModel() == null || typeIn.getModel().getState() != XslModel.State.VALID) {
                errorMessages += NbBundle.getMessage(XsltModelBridge.class, "MSG_Error_BadInputSchema"); // NOI18N
            }
            AXIComponent typeOut = getMapper().getContext().getTargetType();
            if (typeOut == null || typeIn.getModel() == null || typeOut.getModel().getState() != XslModel.State.VALID) {
                errorMessages += NbBundle.getMessage(XsltModelBridge.class, "MSG_Error_BadOutputSchema"); // NOI18N
            }
        } else {
            errorMessages += NbBundle.getMessage(XsltModelBridge.class, "MSG_Error_BadXSLTMAP"); // NOI18N
        }

        //         if (!errorMessages.isEmpty()){
        if (errorMessages != null && !"".equals(errorMessages)) {
            // NOI18N
            mapper.setError(NbBundle.getMessage(XsltModelBridge.class, "MSG_Error_Diagram", errorMessages)); // NOI18N);
            return false;
        } else {
            mapper.setError(null);
            return true;
        }
    }



    protected void reloadTree(JTree tree) {



        TreeExpandedState expandedState = new TreeExpandedState(tree);
        expandedState.save();

        XsltNodesTreeModel treeModel = (XsltNodesTreeModel) tree.getModel();

        treeModel.resetRoot();

        TreeNode treeRoot = (TreeNode) tree.getModel().getRoot();

        TreePath startFrom_tp = TreeNode.getTreePath(treeRoot);


        /*
         * trigger tree reload
         * TreeNode.reload() implementation shoukd try to keep old nodes as much as possible
         * to be able to restore selection state and preserve links on diagram
         */

        treeRoot.reload();


        ((XsltNodesTreeModel) tree.getModel()).fireTreeChanged(startFrom_tp);

        expandedState.restore();
    }

    public void valueChanged(ComponentEvent componentEvent) {
        updateDiagram();
    }

    public void childrenAdded(ComponentEvent componentEvent) {
        updateDiagram();
    }

    public void childrenDeleted(ComponentEvent componentEvent) {
        updateDiagram();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        updateDiagram();
    }

    public void updateDiagram() {
        updateTimer.onEvent();
    }
}
