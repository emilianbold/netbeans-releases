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
package org.netbeans.modules.cnd.navigation.macroview;

import java.awt.BorderLayout;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmModelListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Top component which displays macro expansion view.
 *
 * @author Nick Krasilnikov
 */
public final class MacroExpansionTopComponent extends TopComponent implements CsmModelListener {

    private static final Logger LOGGER = Logger.getLogger(MacroExpansionTopComponent.class.getName());
    private static MacroExpansionTopComponent instance;
    /** path to the icon used by the component and its open action */
    public static final String ICON_PATH = "org/netbeans/modules/cnd/navigation/macroview/resources/macroexpansion.png"; // NOI18N
    private static final String PREFERRED_ID = "MacroExpansionTopComponent"; // NOI18N
    private MacroExpansionPanel panel = null;
    private int lastDividerLocation = -1;
    private boolean lastLocalContext = true;
    private boolean lastSyncCaret = true;
    private boolean lastSyncContext = false;
    private Document lastExpandedContextDoc = null;

    private MacroExpansionTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(MacroExpansionTopComponent.class, "CTL_MacroExpansionTopComponent")); // NOI18N
        setToolTipText(NbBundle.getMessage(MacroExpansionTopComponent.class, "HINT_MacroExpansionTopComponent")); // NOI18N
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
    }

    /**
     * Initializes documents of the panel.
     */
    public void setDocuments(Document expandedContextDoc, Document expandedMacroDoc) {
        lastExpandedContextDoc = expandedContextDoc;
        if (panel != null) {
            lastDividerLocation = panel.getDividerLocation();
            lastLocalContext = panel.isLocalContext();
            lastSyncCaret = panel.isSyncCaret();
            lastSyncContext = panel.isSyncContext();
        }

        panel = new MacroExpansionPanel(true);
        panel.setContextExpansionDocument(expandedContextDoc);
        panel.setMacroExpansionDocument(expandedMacroDoc);
        if (lastDividerLocation != -1) {
            panel.setDividerLocation(lastDividerLocation);
        }
        panel.setLocalContext(lastLocalContext);
        panel.setSyncCaret(lastSyncCaret);
        panel.setSyncContext(lastSyncContext);
        if(panel.isSyncCaret()) {
            panel.updateCaretPosition();
        }
        removeAll();
        add(panel, BorderLayout.CENTER);
        validate();
    }

    /**
     * Updates cursor position.
     */
    public void updateCaretPosition() {
        if (panel != null) {
            panel.updateCaretPosition();
        }
    }

    /**
     * Indicates scope for macro expansion (local or whole file).
     *
     * @return is macro expansion local
     */
    public boolean isLocalContext() {
        if(panel != null) {
            lastLocalContext = panel.isLocalContext();
        }
        return lastLocalContext;
    }

    /**
     * Indicates is caret synchronization enabled or not.
     *
     * @return is caret synchronization enabled
     */
    public boolean isSyncCaret() {
        if(panel != null) {
            lastSyncCaret = panel.isSyncCaret();
        }
        return lastSyncCaret;
    }

    /**
     * Returns document with expanded context.
     *
     * @return document with expanded context
     */
    public Document getExpandedContextDoc() {
        return lastExpandedContextDoc;
    }

    /**
     * Sets text in status bar.
     *
     * @param s - text
     */
    public void setStatusBarText(String s) {
        if (panel != null) {
            panel.setStatusBarText(s);
        }
    }

    @Override
    public void requestActive() {
        super.requestActive();
        if (panel != null) {
            panel.requestFocusInWindow();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(MacroExpansionTopComponent.class, "NoViewAvailable")); // NOI18N
        jButton1.setBorderPainted(false);
        jButton1.setEnabled(false);
        add(jButton1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized MacroExpansionTopComponent getDefault() {
        if (instance == null) {
            instance = new MacroExpansionTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the MacroExpansionTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized MacroExpansionTopComponent findInstance() {
        //TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        TopComponent win = CsmUtilities.getTopComponentInEQ(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(MacroExpansionTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system."); // NOI18N
            return getDefault();
        }
        if (win instanceof MacroExpansionTopComponent) {
            return (MacroExpansionTopComponent) win;
        }
        Logger.getLogger(MacroExpansionTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID + // NOI18N
                "' ID. That is a potential source of errors and unexpected behavior."); // NOI18N
        return getDefault();
    }

    public 
    @Override
    int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    public 
    @Override
    void componentOpened() {
        CsmListeners.getDefault().addModelListener(this);
    }

    public 
    @Override
    void componentClosed() {
        removeAll();
        initComponents();
        if (panel != null) {
            lastDividerLocation = panel.getDividerLocation();
            lastLocalContext = panel.isLocalContext();
            lastSyncCaret = panel.isSyncCaret();
            lastSyncContext = panel.isSyncContext();
            panel = null;
        }
        CsmListeners.getDefault().removeModelListener(this);
    }

    /** replaces this in object stream */
    public 
    @Override
    Object writeReplace() {
        return new ResolvableHelper();
    }

    protected 
    @Override
    String preferredID() {
        return PREFERRED_ID;
    }

    final static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return MacroExpansionTopComponent.getDefault();
        }
    }

    public void projectOpened(CsmProject project) {
    }

    public void projectClosed(CsmProject project) {
        if (CsmModelAccessor.getModel().projects().isEmpty()) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    MacroExpansionTopComponent tc = MacroExpansionTopComponent.findInstance();
                    if (tc.isOpened()) {
                        tc.close();
                    }
                }
            });
        }
    }

    public void modelChanged(CsmChangeEvent e) {
    }
}
