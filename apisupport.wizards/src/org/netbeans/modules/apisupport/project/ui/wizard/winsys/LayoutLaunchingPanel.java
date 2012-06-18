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

package org.netbeans.modules.apisupport.project.ui.wizard.winsys;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.apisupport.project.ui.wizard.common.BasicWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.CheckableNode;
import org.openide.explorer.view.OutlineView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 * Shows progress of launching first, then let's user pickup changed modes.
 *
 * @author Jaroslav Tulach
 */
final class LayoutLaunchingPanel extends BasicWizardIterator.Panel 
implements TaskListener, Runnable, ExplorerManager.Provider {
    private AtomicReference<FileObject> userDir = new AtomicReference<FileObject>();
    private NewTCIterator.DataModel data;
    private Task task;
    private ProgressHandle handle;
    private ExplorerManager em;
    private OutlineView outlineView;
    
    @NbBundle.Messages({
        "CTL_FoundModes=Found modes",
        "LBL_LayoutingWizardTitle=Define Your Modes"
    })
    public LayoutLaunchingPanel(final WizardDescriptor setting, final NewTCIterator.DataModel data) {
        super(setting);
        this.data = data;
        Node root = new AbstractNode(new Children.Array());
        this.em = new ExplorerManager();
        this.em.setRootContext(root);
        initComponents();
        initAccessibility();
        putClientProperty("NewFileWizard_Title", Bundle.LBL_LayoutingWizardTitle()); // NOI18N
    }

    @Override public void addNotify() {
        super.addNotify();
        if (outlineView == null) {
            outlineView = new OutlineView(Bundle.CTL_FoundModes());
            outlineView.getOutline().setRootVisible(false);
            tree.add(outlineView);
            outlineView.setDefaultActionAllowed(false);
            outlineView.setVisible(false);
            tree.setMinimumSize(outlineView.getPreferredSize());
        }
    }
    
    @Override
    protected void storeToDataModel() {
        for (Node n : getExplorerManager().getRootContext().getChildren().getNodes()) {
            ModeNode mn = (ModeNode)n;
            if (mn.isSelected()) {
                data.defineMode(mn.getName(), mn.text);
            }
        }
    }
    
    @NbBundle.Messages({
        "MSG_LaunchingApplication=Launching your application"
    })
    @Override
    protected void readFromDataModel() {
        checkValidity();
        if (task == null) {
            try {
                task = DesignSupport.invokeDesignMode(data.getProject(), userDir, false, !data.isIgnorePreviousRun());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            handle = ProgressHandleFactory.createHandle(Bundle.MSG_LaunchingApplication());
            JComponent pc = ProgressHandleFactory.createProgressComponent(handle);
            JLabel ml = ProgressHandleFactory.createMainLabelComponent(handle);

            progress.add(ml);
            progress.add(pc);

            handle.start();
            markInvalid();
            /* XXX what was the purpose of this? cannot do it now, we are in EQ
            try {
                DesignSupport.existingModes(data);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            */
            task.addTaskListener(this);
        }
    }
    
    @Override
    @NbBundle.Messages({
        "LBL_DesignLayout=Design Window Layout"
    })
    protected String getPanelName() {
        return Bundle.LBL_DesignLayout();
    }
    
    private boolean checkValidity() {
        int cnt = 0;
        for (Node node : getExplorerManager().getRootContext().getChildren().getNodes()) {
            if (node instanceof ModeNode) {
                ModeNode mn = (ModeNode)node;
                if (mn.isSelected()) {
                    cnt++;
                }
            }
        }
        if (cnt == 0 || !outlineView.isVisible()) {
            markInvalid();
            return false;
        }
        markValid();
        return true;
    }
    
    @Override
    protected HelpCtx getHelp() {
        return new HelpCtx(LayoutLaunchingPanel.class);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        progress = new javax.swing.JPanel();
        tree = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(progress, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 11;
        add(tree, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    @NbBundle.Messages({
        "ACS_DesignPanel=Design Window Layout"
    })
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(Bundle.ACS_DesignPanel());
    }
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel progress;
    private javax.swing.JPanel tree;
    // End of variables declaration//GEN-END:variables

    @Override
    @NbBundle.Messages({
        "LBL_NoModesFound=No layout definition found",
        "MSG_NoModesFound=Is everything OK? Did your application compile and run?"
    })
    public void taskFinished(Task task) {
        handle.finish();
        FileObject modeDir = userDir.get().getFileObject("config/Windows2Local/Modes");
        boolean one = false;
        final Children ch = getExplorerManager().getRootContext().getChildren();
        if (modeDir != null) {
            try {
                FileSystem layer = DesignSupport.findLayer(data.getProject());
                if (layer == null) {
                    throw new IOException("Cannot find layer in " + data.getProject()); // NOI18N
                }
                data.setSFS(layer);
                for (FileObject m : modeDir.getChildren()) {
                    if (m.isData() && "wsmode".equals(m.getExt())) {
                        ModeNode mn = new ModeNode(m, data);
                        ch.add(new Node[] { mn });
                        one = true;
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (!one) {
            AbstractNode empty = new AbstractNode(Children.LEAF);
            empty.setName("empty"); // NOI18N
            empty.setDisplayName(Bundle.LBL_NoModesFound());
            empty.setShortDescription(Bundle.MSG_NoModesFound());
            ch.add(new Node[] { empty });
            markInvalid();
        } else {
            markValid();
        }
        
        EventQueue.invokeLater(this);
    }
    @Override
    public void run() {
        progress.setVisible(false);
        outlineView.setVisible(true);
        progress.invalidate();
        outlineView.invalidate();
        validate();
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }

    private class ModeNode extends AbstractNode
    implements CheckableNode {
        private final FileObject mode;
        private final String text;
        private boolean selected;

        public ModeNode(FileObject mode, NewTCIterator.DataModel data) throws IOException {
            super(Children.LEAF);
            this.mode = mode;
            this.text = DesignSupport.readMode(mode);
            this.selected = !data.isExistingMode(mode.getName());
            
            setName(mode.getName());
        }

        @Override
        public boolean isCheckable() {
            return true;
        }

        @Override
        public boolean isCheckEnabled() {
            return true;
        }

        @Override
        public Boolean isSelected() {
            return selected;
        }

        @Override
        public void setSelected(Boolean selected) {
            this.selected = Boolean.TRUE.equals(selected);
            checkValidity();
        }
    }
    
}
