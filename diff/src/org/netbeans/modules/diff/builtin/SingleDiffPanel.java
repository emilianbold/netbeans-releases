/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.diff.builtin;

import java.awt.Container;
import org.netbeans.api.diff.DiffController;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.LifecycleManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Reader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.netbeans.modules.diff.options.DiffOptionsController;

/**
 *
 * @author Maros Sandor
 */
public class SingleDiffPanel extends javax.swing.JPanel implements PropertyChangeListener {
    
    private FileObject          base;
    private FileObject          modified;
    private final FileObject    type;

    private DiffController      controller;
    private Action              nextAction;
    private Action              prevAction;

    /** Creates new form SingleDiffPanel */
    public SingleDiffPanel(FileObject left, FileObject right, FileObject type) throws IOException {
        this.base = left;
        this.modified = right;
        this.type = type;
        initComponents();
        initMyComponents();
        refreshComponents();
    }

    private void initMyComponents() throws IOException {
        // centers components on the toolbar
        actionsToolbar.add(Box.createHorizontalGlue(), 0);
        actionsToolbar.add(Box.createHorizontalGlue());
        
        nextAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                onNext();
            }
        };
        nextAction.putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/diff/builtin/visualizer/editable/diff-next.png", false)); // NOI18N
        bNext.setAction(nextAction);
            
        prevAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                onPrev();
            }
        };
        prevAction.putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/diff/builtin/visualizer/editable/diff-prev.png", false)); // NOI18N
        bPrevious.setAction(prevAction);
        
        getActionMap().put("jumpNext", nextAction);  // NOI18N
        getActionMap().put("jumpPrev", prevAction); // NOI18N

        refreshController();
    }

    private void refreshController() throws IOException {
        if (controller != null) {
            controller.removePropertyChangeListener(this);
            addPropertyChangeListener(this);
        }
        
        StreamSource ss1 = new DiffStreamSource(base, type, false);
        StreamSource ss2 = new DiffStreamSource(modified, type, true);
        controller = DiffController.createEnhanced(ss1, ss2);
        controller.addPropertyChangeListener(this);
        
        controllerPanel.removeAll();
        JComponent innerPanel = controller.getJComponent();
        controllerPanel.add(innerPanel);
        setName(innerPanel.getName());
        revalidate();
        repaint();
    }

    public void requestActive () {
        if (controllerPanel != null) {
            controllerPanel.requestFocusInWindow();
        }
    }
    
    private void onPrev() {
        int idx = controller.getDifferenceIndex();
        if (idx > 0) {
            controller.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, idx - 1);
        }
    }

    private void onNext() {
        int idx = controller.getDifferenceIndex();
        if (idx < controller.getDifferenceCount() - 1) {
            controller.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, idx + 1);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        refreshComponents();
    }

    private void refreshComponents() {
        nextAction.setEnabled(controller.getDifferenceIndex() < controller.getDifferenceCount() - 1);
        prevAction.setEnabled(controller.getDifferenceIndex() > 0);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        actionsToolbar = new javax.swing.JToolBar();
        bNext = new javax.swing.JButton();
        bPrevious = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        bRefresh = new javax.swing.JButton();
        bSwap = new javax.swing.JButton();
        bExport = new javax.swing.JButton();
        bOptions = new javax.swing.JButton();
        controllerPanel = new javax.swing.JPanel();

        actionsToolbar.setFloatable(false);
        actionsToolbar.setRollover(true);

        bNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/diff/builtin/visualizer/editable/diff-next.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(bNext, org.openide.util.NbBundle.getMessage(SingleDiffPanel.class, "SingleDiffPanel.bNext.text")); // NOI18N
        bNext.setToolTipText(org.openide.util.NbBundle.getMessage(SingleDiffPanel.class, "SingleDiffPanel.bNext.toolTipText")); // NOI18N
        bNext.setFocusable(false);
        bNext.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bNext.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        actionsToolbar.add(bNext);

        bPrevious.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/diff/builtin/visualizer/editable/diff-prev.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(bPrevious, org.openide.util.NbBundle.getMessage(SingleDiffPanel.class, "SingleDiffPanel.bPrevious.text")); // NOI18N
        bPrevious.setToolTipText(org.openide.util.NbBundle.getMessage(SingleDiffPanel.class, "SingleDiffPanel.bPrevious.toolTipText")); // NOI18N
        bPrevious.setFocusable(false);
        bPrevious.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bPrevious.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        actionsToolbar.add(bPrevious);
        actionsToolbar.add(jSeparator1);

        bRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/diff/builtin/visualizer/editable/diff-refresh.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(bRefresh, org.openide.util.NbBundle.getMessage(SingleDiffPanel.class, "SingleDiffPanel.bRefresh.text")); // NOI18N
        bRefresh.setToolTipText(org.openide.util.NbBundle.getMessage(SingleDiffPanel.class, "SingleDiffPanel.bRefresh.toolTipText")); // NOI18N
        bRefresh.setFocusable(false);
        bRefresh.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bRefresh.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bRefreshActionPerformed(evt);
            }
        });
        actionsToolbar.add(bRefresh);

        org.openide.awt.Mnemonics.setLocalizedText(bSwap, org.openide.util.NbBundle.getMessage(SingleDiffPanel.class, "SingleDiffPanel.bSwap.text")); // NOI18N
        bSwap.setFocusable(false);
        bSwap.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bSwap.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bSwap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bSwapActionPerformed(evt);
            }
        });
        actionsToolbar.add(bSwap);

        org.openide.awt.Mnemonics.setLocalizedText(bExport, org.openide.util.NbBundle.getMessage(SingleDiffPanel.class, "SingleDiffPanel.bExport.text")); // NOI18N
        bExport.setFocusable(false);
        bExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bExport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bExportActionPerformed(evt);
            }
        });
        actionsToolbar.add(bExport);

        org.openide.awt.Mnemonics.setLocalizedText(bOptions, org.openide.util.NbBundle.getMessage(SingleDiffPanel.class, "SingleDiffPanel.bOptions.text")); // NOI18N
        bOptions.setFocusable(false);
        bOptions.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bOptions.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bOptionsActionPerformed(evt);
            }
        });
        actionsToolbar.add(bOptions);

        controllerPanel.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(controllerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
            .add(actionsToolbar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(actionsToolbar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(controllerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void bRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bRefreshActionPerformed
        LifecycleManager.getDefault().saveAll();
        try {
            refreshController();
        } catch (IOException e) {
            Logger.getLogger(SingleDiffPanel.class.getName()).log(Level.SEVERE, "", e); // elegant, nice and simple exception logging
        }
    }//GEN-LAST:event_bRefreshActionPerformed
    
    private void bSwapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bSwapActionPerformed
        LifecycleManager.getDefault().saveAll();
        FileObject temp = base;
        base = modified;
        modified = temp;
        try {
            refreshController();
            Container c = getParent();
            if (c != null) {
                c.setName(getName());
            }
        } catch (IOException e) {
            Logger.getLogger(SingleDiffPanel.class.getName()).log(Level.SEVERE, "", e); // elegant, nice and simple exception logging
        }
    }//GEN-LAST:event_bSwapActionPerformed

    private void bExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bExportActionPerformed
        StreamSource ss1 = new DiffStreamSource(base, type, false);
        StreamSource ss2 = new DiffStreamSource(modified, type, true);
        ExportPatch.exportPatch(new StreamSource[] { ss1 }, new StreamSource[] { ss2 });
    }//GEN-LAST:event_bExportActionPerformed
    
    private void bOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bOptionsActionPerformed
        OptionsDisplayer.getDefault().open("Advanced/" + DiffOptionsController.OPTIONS_SUBPATH);
    }//GEN-LAST:event_bOptionsActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar actionsToolbar;
    private javax.swing.JButton bExport;
    private javax.swing.JButton bNext;
    private javax.swing.JButton bOptions;
    private javax.swing.JButton bPrevious;
    private javax.swing.JButton bRefresh;
    private javax.swing.JButton bSwap;
    private javax.swing.JPanel controllerPanel;
    private javax.swing.JToolBar.Separator jSeparator1;
    // End of variables declaration//GEN-END:variables


    private static class DiffStreamSource extends StreamSource {
        
        private final FileObject    fileObject;
        private final FileObject    type;
        private final boolean       isRight;

        public DiffStreamSource(FileObject fileObject, FileObject type, boolean isRight) {
            this.fileObject = fileObject;
            this.type = type;
            this.isRight = isRight;
        }

        public boolean isEditable() {
            return isRight && fileObject.canWrite();
        }

        public Lookup getLookup() {
            return Lookups.fixed(fileObject);
        }

        public String getName() {
            return fileObject.getName();
        }

        public String getTitle() {
            return FileUtil.getFileDisplayName(fileObject);
        }

        public String getMIMEType() {
            if (type != null) {
                return type.getMIMEType();
            } else {
                return fileObject.getMIMEType();
            }
        }

        public Reader createReader() throws IOException {
            if (type != null) {
                return new InputStreamReader(fileObject.getInputStream(), FileEncodingQuery.getEncoding(type));
            } else {
                return new InputStreamReader(fileObject.getInputStream(), FileEncodingQuery.getEncoding(fileObject));
            }
        }

        public Writer createWriter(Difference[] conflicts) throws IOException {
            return null;
        }
    }
}
