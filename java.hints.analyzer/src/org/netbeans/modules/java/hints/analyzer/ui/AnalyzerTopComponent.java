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

package org.netbeans.modules.java.hints.analyzer.ui;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.ActionMap;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.java.hints.analyzer.Analyzer;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
//import org.openide.util.Utilities;

/**
 * Top component which displays something.
 */
public final class AnalyzerTopComponent extends TopComponent implements ExplorerManager.Provider, ChangeListener {

    private static AnalyzerTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";

    private static final String PREFERRED_ID = "AnalyzerTopComponent";

    private Lookup context;
    private Map<String, Preferences> preferencesOverlay;
    private final ExplorerManager manager = new ExplorerManager();
    private final CheckTreeView btv;
    private final List<FixDescription> fixes = new LinkedList<FixDescription>();

    private AnalyzerTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(AnalyzerTopComponent.class, "CTL_AnalyzerTopComponent"));
        setToolTipText(NbBundle.getMessage(AnalyzerTopComponent.class, "HINT_AnalyzerTopComponent"));
//        setIcon(Utilities.loadImage(ICON_PATH, true));
        
//        BeanTreeView btv = new BeanTreeView();
        btv = new CheckTreeView();
        
        btvHolder.setLayout(new BorderLayout());
        btvHolder.add(btv, BorderLayout.CENTER);
        
        btv.setRootVisible(false);
        
        prevAction = new PreviousError(this);
        nextAction = new NextError(this);
        
        AnalyzerTopComponent.PCLImpl l = new PCLImpl();
        
        prevAction.addPropertyChangeListener(l);
        nextAction.addPropertyChangeListener(l);
        
        setData(Lookup.EMPTY, Collections.<String, Preferences>emptyMap(), Collections.<ErrorDescription>emptyList());
        stateChanged(null);
        
        getActionMap().put("jumpNext", nextAction);
        getActionMap().put("jumpPrev", prevAction);
        
        associateLookup(ExplorerUtils.createLookup(manager, new ActionMap()));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btvHolder = new javax.swing.JPanel();
        refreshButton = new javax.swing.JButton();
        fixButton = new javax.swing.JButton();
        goOverFixed = new javax.swing.JCheckBox();
        fixOnNext = new javax.swing.JCheckBox();
        nextError = new javax.swing.JButton();
        previousError = new javax.swing.JButton();

        org.jdesktop.layout.GroupLayout btvHolderLayout = new org.jdesktop.layout.GroupLayout(btvHolder);
        btvHolder.setLayout(btvHolderLayout);
        btvHolderLayout.setHorizontalGroup(
            btvHolderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 572, Short.MAX_VALUE)
        );
        btvHolderLayout.setVerticalGroup(
            btvHolderLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 246, Short.MAX_VALUE)
        );

        refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/java/hints/analyzer/ui/refresh.png"))); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(fixButton, "Fix Selected");
        fixButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fixButtonActionPerformed(evt);
            }
        });

        goOverFixed.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(goOverFixed, "Go Over Fixed Problems");
        goOverFixed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                goOverFixedActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(fixOnNext, "Fix on Next");
        fixOnNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fixOnNextActionPerformed(evt);
            }
        });

        nextError.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/java/hints/analyzer/ui/nextmatch.png"))); // NOI18N
        nextError.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextErrorActionPerformed(evt);
            }
        });

        previousError.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/java/hints/analyzer/ui/prevmatch.png"))); // NOI18N
        previousError.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousErrorActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(nextError, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(previousError, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(refreshButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(fixButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 135, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(goOverFixed)
                        .add(18, 18, 18)
                        .add(fixOnNext))
                    .add(btvHolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(refreshButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(previousError)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(nextError))
                    .add(btvHolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fixButton)
                    .add(goOverFixed)
                    .add(fixOnNext))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
    refresh();
}//GEN-LAST:event_refreshButtonActionPerformed

private void fixButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fixButtonActionPerformed
    List<FixDescription> fixes = new LinkedList<FixDescription>();

    for (FixDescription fd : this.fixes) {
        if (fd.isSelected()) {
            fixes.add(fd);
        }
    }

    for (FixDescription f : fixes) {
        try {
            f.implement();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}//GEN-LAST:event_fixButtonActionPerformed

private void previousErrorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousErrorActionPerformed
prevAction.actionPerformed(null);
}//GEN-LAST:event_previousErrorActionPerformed

private void nextErrorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextErrorActionPerformed
nextAction.actionPerformed(null);
}//GEN-LAST:event_nextErrorActionPerformed

private void goOverFixedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goOverFixedActionPerformed
    prevAction.fireEnabledChanged();
    nextAction.fireEnabledChanged();
}//GEN-LAST:event_goOverFixedActionPerformed

private void fixOnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fixOnNextActionPerformed
    prevAction.fireEnabledChanged();
    nextAction.fireEnabledChanged();
}//GEN-LAST:event_fixOnNextActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel btvHolder;
    private javax.swing.JButton fixButton;
    private javax.swing.JCheckBox fixOnNext;
    private javax.swing.JCheckBox goOverFixed;
    private javax.swing.JButton nextError;
    private javax.swing.JButton previousError;
    private javax.swing.JButton refreshButton;
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized AnalyzerTopComponent getDefault() {
        if (instance == null) {
            instance = new AnalyzerTopComponent();
        }
        return instance;
    }
    
    public void setData(Lookup context, Map<String, Preferences> preferencesOverlay, List<ErrorDescription> hints) {
        this.context = context;
        this.preferencesOverlay = preferencesOverlay;
        for (FixDescription f : fixes) {
            f.removeChangeListener(this);
        }
        fixes.clear();
        manager.setRootContext(Nodes.constructSemiLogicalView(sortErrors(hints), fixes));
        for (FixDescription f : fixes) {
            f.addChangeListener(this);
        }
        if (btv != null) {
            btv.expandAll();
        }
        refreshButton.setEnabled(Analyzer.normalizeLookup(context) != null);
        nodesForNext = null;
        seenNodes = null;
        fireActionEnabledChange();
    }
    
    public void refresh() {
        Analyzer.process(context, preferencesOverlay);
    }

    /**
     * Obtain the AnalyzerTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized AnalyzerTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(AnalyzerTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof AnalyzerTopComponent) {
            return (AnalyzerTopComponent) win;
        }
        Logger.getLogger(AnalyzerTopComponent.class.getName()).warning(
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
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
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
            return AnalyzerTopComponent.getDefault();
        }
    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }

    private Map<FileObject, List<ErrorDescription>> sortErrors(List<ErrorDescription> errs) {
        Map<FileObject, List<ErrorDescription>> sorted = new HashMap<FileObject, List<ErrorDescription>>();
        
        for (ErrorDescription e : errs) {
            List<ErrorDescription> fileErrs = sorted.get(e.getFile());
            
            if (fileErrs == null) {
                sorted.put(e.getFile(), fileErrs = new LinkedList<ErrorDescription>());
            }
            
            fileErrs.add(e);
        }
        
        return sorted;
    }

    public void stateChanged(ChangeEvent e) {
        boolean fixEnable = false;
        boolean overFixedEnabled = false;
        for (FixDescription f : fixes) {
            if (f.isSelected()) {
                fixEnable = true;
            }
            if (f.isFixed()) {
                overFixedEnabled = true;
            }
        }
        
        fixButton.setEnabled(fixEnable);
        goOverFixed.setEnabled(overFixedEnabled);
        fireActionEnabledChange();
    }
    
    List<Node> nodesForNext;
    List<Node> seenNodes;
    boolean lastGoOverFixed;
    final PreviousError prevAction;
    final NextError nextAction;
    
    void fireActionEnabledChange() {
        prevAction.fireEnabledChanged();
        nextAction.fireEnabledChanged();
    }
    
    boolean fixOnNext() {
        return fixOnNext.isEnabled() && fixOnNext.isSelected();
    }
    
    boolean goOverFixed() {
        return goOverFixed.isEnabled() && goOverFixed.isSelected();
    }
    
    private class PCLImpl implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            String name = evt.getPropertyName();
            if (name == null || "enabled".equals(name)) {
                previousError.setEnabled(prevAction.isEnabled());
                nextError.setEnabled(nextAction.isEnabled());
            }
        }
        
    }
}
