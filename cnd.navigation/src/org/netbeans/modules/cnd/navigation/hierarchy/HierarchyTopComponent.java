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

package org.netbeans.modules.cnd.navigation.hierarchy;

import java.awt.BorderLayout;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmModelListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.navigation.classhierarchy.ClassHierarchyPanel;
import org.netbeans.modules.cnd.navigation.includeview.IncludeHierarchyPanel;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays c/c++ hierarchy.
 */
final class HierarchyTopComponent extends TopComponent implements CsmModelListener {

    private static HierarchyTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/netbeans/modules/cnd/navigation/classhierarchy/resources/subtypehierarchy.gif"; // NOI18N
    private static final String PREFERRED_ID = "HierarchyTopComponent"; // NOI18N
    private JComponent last = null;

    private HierarchyTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(getClass(), "CTL_HierarchyTopComponent")); // NOI18N
        setToolTipText(NbBundle.getMessage(getClass(), "HINT_HierarchyTopComponent")); // NOI18N
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
    }

    void setClass(CsmClass decl, boolean setClose) {
        setName(decl.getName()+" - "+NbBundle.getMessage(getClass(), "CTL_HierarchyTopComponent")); // NOI18N
        setToolTipText(NbBundle.getMessage(getClass(), "HINT_TypeHierarchyTopComponent")); // NOI18N
        if (!(last instanceof ClassHierarchyPanel)) {
            removeAll();
            ClassHierarchyPanel panel = new ClassHierarchyPanel(true);
            add(panel, BorderLayout.CENTER);
            validate();
            panel.setClass(decl);
            last = panel;
        }
        if (setClose) {
            ((ClassHierarchyPanel)last).setClose();
        } else {
            ((ClassHierarchyPanel)last).clearClose();
        }
        ((ClassHierarchyPanel)last).setClass(decl);
        last.requestFocusInWindow();
    }

    void setFile(CsmFile file, boolean setClose) {
        setName(file.getName()+" - "+NbBundle.getMessage(getClass(), "CTL_HierarchyTopComponent")); // NOI18N
        setToolTipText(NbBundle.getMessage(getClass(), "HINT_IncludeHierarchyTopComponent")); // NOI18N
        if (!(last instanceof IncludeHierarchyPanel)) {
            removeAll();
            IncludeHierarchyPanel panel = new IncludeHierarchyPanel(true);
            add(panel, BorderLayout.CENTER);
            validate();
            last = panel;
        }
        if (setClose) {
            ((IncludeHierarchyPanel)last).setClose();
        } else {
            ((IncludeHierarchyPanel)last).clearClose();
        }
        ((IncludeHierarchyPanel)last).setFile(file);
        last.requestFocusInWindow();
    }

    @Override
    public void requestActive() {
        super.requestActive();
        if (last != null) {
            last.requestFocusInWindow();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jButton1.setBackground(new JTextArea().getBackground());
        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(HierarchyTopComponent.class, "NoViewAvailable")); // NOI18N
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
    public static synchronized HierarchyTopComponent getDefault() {
        if (instance == null) {
            instance = new HierarchyTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the HierarchyTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized HierarchyTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(HierarchyTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system."); // NOI18N
            return getDefault();
        }
        if (win instanceof HierarchyTopComponent) {
            return (HierarchyTopComponent)win;
        }
        Logger.getLogger(HierarchyTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID + // NOI18N
                "' ID. That is a potential source of errors and unexpected behavior."); // NOI18N
        return getDefault();
    }

    public @Override int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    public @Override void componentOpened() {
        CsmListeners.getDefault().addModelListener(this);
    }

    public @Override void componentClosed() {
        removeAll();
        initComponents();
        last = null;
        CsmListeners.getDefault().removeModelListener(this);
    }

    /** replaces this in object stream */
    public @Override Object writeReplace() {
        return new ResolvableHelper();
    }

    protected @Override String preferredID() {
        return PREFERRED_ID;
    }

    final static class ResolvableHelper implements Serializable {
        private static final long serialVersionUID = 1L;
        public Object readResolve() {
            return HierarchyTopComponent.getDefault();
        }
    }

    public void projectOpened(CsmProject project) {
    }

    public void projectClosed(CsmProject project) {
        if (CsmModelAccessor.getModel().projects().isEmpty()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    HierarchyTopComponent tc = HierarchyTopComponent.findInstance();
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
