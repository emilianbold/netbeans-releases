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

/*
 * SrcDocPathsPanel.java
 *
 * Created on March 31, 2004, 10:52 AM
 */
package org.netbeans.modules.mobility.cldcplatform;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileChooserBuilder;

/**
 *
 */
public class SrcDocPathsPanel extends javax.swing.JPanel implements ListSelectionListener {
    
    private static final java.awt.Dimension PREF_DIM = new java.awt.Dimension(560, 350);
    
    J2MEPlatform platform;
    DefaultListModel srcModel;
    DefaultListModel docModel;
    
    /** Creates new form SrcDocPathsPanel */
    public SrcDocPathsPanel() {
        initComponents();
        initAccessibility();
        lDocPaths.addListSelectionListener(this);
        lSrcPaths.addListSelectionListener(this);
        srcModel = new DefaultListModel();
        lSrcPaths.setModel(srcModel);
        docModel = new DefaultListModel();
        lDocPaths.setModel(docModel);
        platform = null;
    }
    
    public void readData(final WizardDescriptor object) {
        platform = (J2MEPlatform) object.getProperty(DetectPanel.PLATFORM);
        if (platform == null)
            return;
        final FileObject[] al = platform.getSourceFolders().getRoots();
        srcModel.clear();
        if (al != null) 
        	for (FileObject fo : al )
        		srcModel.addElement(new ListItem<FileObject>(fo));
        
        final List<URL> l = platform.getJavadocFolders();
        docModel.clear();
        if (l != null) 
        	for (URL url : l)
        		docModel.addElement(new ListItem<URL>(url));
        updateButtons();
    }
    
    public void storeData(@SuppressWarnings("unused")
	final WizardDescriptor object) {
        if (platform == null)
            return;
        
        Object[] os;
        os = srcModel.toArray();
        List<FileObject> l = new ArrayList<FileObject>();
        if (os != null) 
        	for (Object osa : os )
        		l.add(((ListItem<FileObject>)osa).getObject());
        platform.setSourceFolders(l);
        
        os = docModel.toArray();
        List<URL> u = new ArrayList<URL>();
        if (os != null) 
        	for (Object osa : os)
        		u.add(((ListItem<URL>)osa).getObject());
        platform.setJavadocFolders(u);
    }

    private String browse(final String title) {
        File f;
        if ((f = new FileChooserBuilder(SrcDocPathsPanel.class).
                setFileFilter(new ArchiveFilter()).
                setTitle(title).showOpenDialog()) != null) {
            return f.getAbsolutePath();
        }
        return null;
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
        jScrollPane1 = new javax.swing.JScrollPane();
        lSrcPaths = new javax.swing.JList();
        bSrcAdd = new javax.swing.JButton();
        bSrcRemove = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lDocPaths = new javax.swing.JList();
        bDocAdd = new javax.swing.JButton();
        bDocRemove = new javax.swing.JButton();

        setName(org.openide.util.NbBundle.getMessage(SrcDocPathsPanel.class, "TITLE_J2MEWizardIterator_PathsPanel")); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(lSrcPaths);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SrcDocPathsPanel.class, "LBL_SDPathsPanel_Sources")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel1, gridBagConstraints);

        lSrcPaths.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(lSrcPaths);
        lSrcPaths.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SrcDocPathsPanel.class, "ACD_SrcDocPathsPanel_Sources")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 6);
        add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bSrcAdd, org.openide.util.NbBundle.getMessage(SrcDocPathsPanel.class, "LBL_SDPathsPanel_Add")); // NOI18N
        bSrcAdd.setToolTipText(org.openide.util.NbBundle.getMessage(SrcDocPathsPanel.class, "TTT_SrcDocPathsPanel_SrcAdd")); // NOI18N
        bSrcAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bSrcAddActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 0);
        add(bSrcAdd, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bSrcRemove, org.openide.util.NbBundle.getMessage(SrcDocPathsPanel.class, "LBL_SDPathsPanel_Remove")); // NOI18N
        bSrcRemove.setToolTipText(org.openide.util.NbBundle.getMessage(SrcDocPathsPanel.class, "TTT_SrcDocPathsPanel_SrcRemove")); // NOI18N
        bSrcRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bSrcRemoveActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 0);
        add(bSrcRemove, gridBagConstraints);

        jLabel2.setLabelFor(lDocPaths);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SrcDocPathsPanel.class, "LBL_SDPathsPanel_JavaDocs")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(jLabel2, gridBagConstraints);

        lDocPaths.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(lDocPaths);
        lDocPaths.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SrcDocPathsPanel.class, "ACD_SrcDocPathsPanel_Javadoc")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 6);
        add(jScrollPane2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bDocAdd, org.openide.util.NbBundle.getMessage(SrcDocPathsPanel.class, "LBL_SDPathsPanel_Add")); // NOI18N
        bDocAdd.setToolTipText(org.openide.util.NbBundle.getMessage(SrcDocPathsPanel.class, "TTT_SrcDocPathsPanel_JavadocAdd")); // NOI18N
        bDocAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bDocAddActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 5, 0);
        add(bDocAdd, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bDocRemove, org.openide.util.NbBundle.getMessage(SrcDocPathsPanel.class, "LBL_SDPathsPanel_Remove")); // NOI18N
        bDocRemove.setToolTipText(org.openide.util.NbBundle.getMessage(SrcDocPathsPanel.class, "TTT_SrcDocPathsPanel_JavadocRemove")); // NOI18N
        bDocRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bDocRemoveActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 0, 0);
        add(bDocRemove, gridBagConstraints);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SrcDocPathsPanel.class, "ACN_SrcDocPathsPanel")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SrcDocPathsPanel.class, "ACD_SrcDocPathsPanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        //getAccessibleContext().setAccessibleName();
        //getAccessibleContext().setAccessibleDescription();
    }
    
    public java.awt.Dimension getPreferredSize() {
        return PREF_DIM;
    }
    
    private void bDocRemoveActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bDocRemoveActionPerformed
        final int selected = lDocPaths.getSelectedIndex();
        if (selected < 0)
            return;
        docModel.removeElementAt(selected);
        lDocPaths.setSelectedIndex(selected < docModel.size() ? selected : selected -1);
        updateButtons();
    }//GEN-LAST:event_bDocRemoveActionPerformed
    
    private void bSrcRemoveActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bSrcRemoveActionPerformed
        final int selected = lSrcPaths.getSelectedIndex();
        if (selected < 0)
            return;
        srcModel.removeElementAt(selected);
        lSrcPaths.setSelectedIndex(selected < srcModel.size() ? selected : selected -1);
        updateButtons();
    }//GEN-LAST:event_bSrcRemoveActionPerformed
    
    private void bDocAddActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bDocAddActionPerformed
        if (platform == null)
            return;
        final String value = browse(NbBundle.getMessage(SrcDocPathsPanel.class, "TITLE_SDPathsPanel_SelectJavaDoc")); // NOI18N
        if (value == null)
            return;
        final URL o = J2MEPlatform.localfilepath2url(value);
        if (o != null) {
            final ListItem<URL> item = new ListItem<URL>(o);
            docModel.addElement(item);
            lDocPaths.setSelectedValue(item, true);
        }
        updateButtons();
    }//GEN-LAST:event_bDocAddActionPerformed
    
    private void bSrcAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bSrcAddActionPerformed
        if (platform == null)
            return;
        final String value = browse(NbBundle.getMessage(SrcDocPathsPanel.class, "TITLE_SDPathsPanel_SelectSource")); // NOI18N
        if (value == null)
            return;
        final FileObject o = platform.resolveRelativePathToFileObject(value);
        if (o != null) {
            final ListItem<FileObject> item = new ListItem<FileObject>(o);
            srcModel.addElement(item);
            lSrcPaths.setSelectedValue(item, true);
        }
        updateButtons();
    }//GEN-LAST:event_bSrcAddActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bDocAdd;
    private javax.swing.JButton bDocRemove;
    private javax.swing.JButton bSrcAdd;
    private javax.swing.JButton bSrcRemove;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList lDocPaths;
    private javax.swing.JList lSrcPaths;
    // End of variables declaration//GEN-END:variables
    
    private void updateButtons() {
        bDocRemove.setEnabled(lDocPaths.getSelectedIndex() >= 0);
        bSrcRemove.setEnabled(lSrcPaths.getSelectedIndex() >= 0);
    }
    
    public void valueChanged(@SuppressWarnings("unused")
	final ListSelectionEvent listSelectionEvent) {
        updateButtons();
    }
    
    static class ListItem<T> {
        
        T o;
        String str;
        
        ListItem(T o) {
            this.o = o;
            if (o instanceof FileObject)
                this.str = J2MEPlatform.getFilePath((FileObject) o);
            else if (o instanceof URL)
                this.str = J2MEPlatform.getFilePath(URLMapper.findFileObject((URL) o));
            if (this.str == null)
                this.str = o != null ? o.toString() : ""; // NOI18N
        }
        
        T getObject() {
            return o;
        }
        
        public String toString() {
            return str;
        }
    }
    
    /**
     * Controller for the outer class: manages wizard panel's valid state
     * according to the user's input and detection state.
     */
    public static class WizardPanel implements WizardDescriptor.FinishablePanel {
        
        SrcDocPathsPanel component;
        WizardDescriptor wizard;
        ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();
        boolean valid = false;
        boolean finishable;
        
        public WizardPanel(boolean finishable) {
            this.finishable = finishable;
        }
        
        public void addChangeListener(final javax.swing.event.ChangeListener changeListener) {
            listeners.add(changeListener);
        }
        
        public void removeChangeListener(final javax.swing.event.ChangeListener changeListener) {
            listeners.remove(changeListener);
        }
        
        public java.awt.Component getComponent() {
            if (component == null) {
                // !!! use unified workdir
                component = new SrcDocPathsPanel();
                checkValid();
            }
            return component;
        }
        
        public org.openide.util.HelpCtx getHelp() {
            return new HelpCtx(SrcDocPathsPanel.class);
        }
        
        public boolean isFinishPanel() {
            return finishable;
        }
        
        public void showError(final String message) {
            if (wizard != null)
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message); // NOI18N
        }
        
        public boolean isValid() {
            return true;
        }
        
        public void readSettings(final Object obj) {
            wizard = (WizardDescriptor) obj;
            ((SrcDocPathsPanel) getComponent()).readData(wizard);
        }
        
        public void storeSettings(final Object obj) {
            wizard = (WizardDescriptor) obj;
            ((SrcDocPathsPanel) getComponent()).storeData(wizard);
        }
        
        void fireStateChange() {
            ChangeListener[] ll;
            synchronized (this) {
                if (listeners.isEmpty())
                    return;
                ll = listeners.toArray(new ChangeListener[listeners.size()]);
            }
            final ChangeEvent ev = new ChangeEvent(this);
            for (int i = 0; i < ll.length; i++)
                ll[i].stateChanged(ev);
        }
        
        void checkValid() {
            if (isValid() != valid) {
                valid ^= true;
                fireStateChange();
            }
        }
        
    }
    
}
