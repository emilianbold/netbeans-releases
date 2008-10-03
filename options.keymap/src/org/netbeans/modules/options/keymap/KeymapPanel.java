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

package org.netbeans.modules.options.keymap;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.InputLine;
import org.openide.NotifyDescriptor.Message;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author  Jan Jancura
 */
public class KeymapPanel extends JPanel implements ActionListener, 
TreeSelectionListener, ListSelectionListener {
    
    
    private boolean             listen = false;
    
    /** Creates new form KeymapPanel1 */
    public KeymapPanel() {
        initComponents();
        loc (bDuplicate,        "Duplicate");
        loc (bDelete,       "Delete");
        loc (lProfile,       "Keymap_Name");
//        loc (rbAction,      "Show_Actions");
//        loc (rbShortcut,    "Show_Shortcuts");
        liShortcuts.getAccessibleContext ().setAccessibleName (loc ("AN_Shortcuts"));
        liShortcuts.getAccessibleContext ().setAccessibleDescription (loc ("AD_Shortcuts"));
        tActions.getAccessibleContext ().setAccessibleName (loc ("AN_Actions"));
        tActions.getAccessibleContext ().setAccessibleDescription (loc ("AD_Actions"));
        cbProfile.getAccessibleContext ().setAccessibleName (loc ("AN_Profiles"));
        cbProfile.getAccessibleContext ().setAccessibleDescription (loc ("AD_Profiles"));
//        bgViewAs.add        (rbAction);
//        bgViewAs.add        (rbShortcut);
        bDuplicate.addActionListener (this);
        bDelete.addActionListener (this);
//        rbAction.setSelected (true);
//        rbAction.addActionListener (this);
//        rbShortcut.addActionListener (this);
        tActions.setRootVisible (false);
        tActions.setShowsRootHandles (true);
        tActions.addTreeSelectionListener (this);
        cbProfile.addActionListener (this);
        loc (bAdd,       "Add_Shortcut");
        loc (bRemove,    "Remove_Shortcut");
        bAdd.addActionListener (this);
        bRemove.addActionListener (this);
        liShortcuts.addListSelectionListener (this);
        bAdd.setEnabled     (false);
        bRemove.setEnabled  (false);
        loc (lShortcuts, "Shortcuts"); // NOI18N
        lShortcuts.setLabelFor (liShortcuts);
        loc (lActions, "Actions");
        lActions.setLabelFor (tActions);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        lProfile = new javax.swing.JLabel();
        cbProfile = new javax.swing.JComboBox();
        bDuplicate = new javax.swing.JButton();
        bDelete = new javax.swing.JButton();
        lActions = new javax.swing.JLabel();
        spActions = new javax.swing.JScrollPane();
        tActions = new javax.swing.JTree();
        lShortcuts = new javax.swing.JLabel();
        spShortcuts = new javax.swing.JScrollPane();
        liShortcuts = new javax.swing.JList();
        bAdd = new javax.swing.JButton();
        bRemove = new javax.swing.JButton();

        lProfile.setText("Profile:");

        bDuplicate.setText("Duplicate...");

        bDelete.setText("Delete");

        lActions.setText("Actions:");

        spActions.setViewportView(tActions);

        lShortcuts.setText("Shortcuts:");

        spShortcuts.setViewportView(liShortcuts);

        bAdd.setText("Add...");

        bRemove.setText("Remove");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(lProfile)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cbProfile, 0, 139, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(bDuplicate))
                    .add(layout.createSequentialGroup()
                        .add(spShortcuts, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 175, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(bRemove)
                            .add(bAdd))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(bDelete))
            .add(layout.createSequentialGroup()
                .add(lActions)
                .addContainerGap())
            .add(spActions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(lShortcuts)
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {bAdd, bRemove}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {bDelete, bDuplicate}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(bDelete)
                    .add(bDuplicate)
                    .add(lProfile)
                    .add(cbProfile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lActions)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(spActions, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lShortcuts)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(layout.createSequentialGroup()
                        .add(bAdd)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(bRemove))
                    .add(spShortcuts, 0, 52, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bAdd;
    private javax.swing.JButton bDelete;
    private javax.swing.JButton bDuplicate;
    private javax.swing.JButton bRemove;
    private javax.swing.JComboBox cbProfile;
    private javax.swing.JLabel lActions;
    private javax.swing.JLabel lProfile;
    private javax.swing.JLabel lShortcuts;
    private javax.swing.JList liShortcuts;
    private javax.swing.JScrollPane spActions;
    private javax.swing.JScrollPane spShortcuts;
    private javax.swing.JTree tActions;
    // End of variables declaration//GEN-END:variables
    
    
    public void actionPerformed (ActionEvent e) {
        if (!listen) return;
        Object source = e.getSource ();
        if (source == bAdd) {
            Object action = tActions.getSelectionPath ().getLastPathComponent ();
            String shortcut = getModel().showShortcutsDialog();
            if (shortcut == null) return;
            getModel ().addShortcut (
                tActions.getSelectionPath (), shortcut
            );
            selectAction (action);
            if (liShortcuts.getModel ().getSize () > 0)
                liShortcuts.setSelectedIndex (0);
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    liShortcuts.requestFocus ();
                }
            });
        } else
        if (source == bRemove) {
            int index = liShortcuts.getSelectedIndex ();
            Object action = tActions.getSelectionPath ().getLastPathComponent ();
            String shortcut = (String) liShortcuts.getSelectedValue ();
            getModel ().removeShortcut (
                tActions.getSelectionPath (), shortcut
            );
            selectAction (action);
            if (liShortcuts.getModel ().getSize () > index)
                liShortcuts.setSelectedIndex (index);
            else
            if (liShortcuts.getModel ().getSize () > 0)
                liShortcuts.setSelectedIndex (0);
        } else
        if (source == bDelete) {
            deleteCurrentProfile ();
        } else
        if (source == cbProfile) {
            String profile = (String) cbProfile.getSelectedItem ();
            final TreePath tp = tActions.getSelectionPath ();
            final boolean expanded = tActions.isExpanded(tp);
            getModel ().setCurrentProfile (profile);
            
            SwingUtilities.invokeLater(new Runnable() { //rememebrer jTree state
                public void run() {
                    if (expanded) 
                        tActions.expandPath(tp);
                    tActions.setSelectionPath(tp);
                    tActions.scrollPathToVisible(tp);
                }});
                
            if (getModel ().isCustomProfile (profile))
                loc (bDelete, "Delete");                          // NOI18N
            else
                loc (bDelete, "Restore");                         // NOI18N
            refreshAction ();
        } else
        if (source == bDuplicate) {
            InputLine il = new InputLine (
                loc ("CTL_Create_New_Profile_Message"),                // NOI18N
                loc ("CTL_Create_New_Profile_Title")                   // NOI18N
            );
            il.setInputText ((String) cbProfile.
                getSelectedItem ());
            DialogDisplayer.getDefault ().notify (il);
            if (il.getValue () == NotifyDescriptor.OK_OPTION) {
                String newProfile = il.getInputText ();
                Iterator it = getModel ().getProfiles ().iterator ();
                while (it.hasNext ())
                    if (newProfile.equals (it.next ())) {
                        Message md = new Message (
                            loc ("CTL_Duplicate_Profile_Name"),        // NOI18N
                            Message.ERROR_MESSAGE
                        );
                        DialogDisplayer.getDefault ().notify (md);
                        return;
                    }
                getModel ().cloneProfile (newProfile);
                cbProfile.addItem (il.getInputText ());
                cbProfile.setSelectedItem (il.getInputText ());
            }
            return;
        }
    }
    
    public void valueChanged (TreeSelectionEvent e) {
        if (!listen) return;
        refreshAction ();
    }
    
    public void valueChanged (ListSelectionEvent e) {
        if (!listen) return;
        // selected shourtcut changed
        int i = liShortcuts.getSelectedIndex ();
        if (i < 0) {
            bRemove.setEnabled (false);
            return;
        }
        bRemove.setEnabled (true);
    }
   
    private boolean initialized = false;
    void update () {
        if (!initialized) {
            initialized = true;
            listen = false;

            tActions.setCellRenderer (new KeymapListRenderer (getModel ()));
            tActions.setModel (getModel ());

            // cbProfile
            List keymaps = getModel ().getProfiles ();
            cbProfile.removeAllItems ();
            int i, k = keymaps.size ();
            for (i = 0; i < k; i++)
                cbProfile.addItem (keymaps.get (i));
            listen = true;
        }
        //refresh -- #65199
        getModel().refreshActions();
        
        cbProfile.setSelectedItem (getModel ().getCurrentProfile ());
    }
    
    private void deleteCurrentProfile () {
        String currentProfile = (String) cbProfile.getSelectedItem ();
        getModel ().deleteProfile (currentProfile);
        if (getModel ().isCustomProfile (currentProfile)) {
            cbProfile.removeItem (currentProfile);
            cbProfile.setSelectedIndex (0);
        }
    }
    
    void applyChanges () {
        if (!initialized) return; // not initialized yet.
        getModel ().apply ();
    }
    
    void cancel () {
        if (model == null) return;
        model.cancel ();
    }
    
    boolean dataValid () {
        return true;
    }
    
    boolean isChanged () {
        return getModel ().isChanged ();
    }
    
    private KeymapViewModel     model;

    synchronized KeymapViewModel getModel () {
        if (model == null) 
            model = new KeymapViewModel ();
        return model;
    }
    
    
    // other methods ...........................................................
    
    private static String loc (String key) {
        return NbBundle.getMessage (KeymapPanel.class, key);
    }
    
    private static void loc (Component c, String key) {
        if (!(c instanceof JLabel)) {
            c.getAccessibleContext ().setAccessibleName (loc ("AN_" + key));
            c.getAccessibleContext ().setAccessibleDescription (loc ("AD_" + key));
        }
        if (c instanceof AbstractButton) {
            Mnemonics.setLocalizedText (
                (AbstractButton) c, 
                loc ("CTL_" + key)
            );
        } else {
            Mnemonics.setLocalizedText (
                (JLabel) c, 
                loc ("CTL_" + key)
            );
        }
    }
    
    void refreshAction () {
        Object action = tActions.getSelectionPath () == null ? 
            null : tActions.getSelectionPath ().getLastPathComponent ();
        selectAction (action);
        if (liShortcuts.getModel ().getSize () > 0)
            liShortcuts.setSelectedIndex (0);
    }
    
    void selectAction (Object action) {
        if (action == null || action instanceof String) {
            liShortcuts.setModel (new DefaultListModel ());
            bAdd.setEnabled (false);
            bRemove.setEnabled (false);
            return;
        }
        bAdd.setEnabled (true);
        bRemove.setEnabled (false);
        final String[] shortcuts = getModel ().getShortcuts ((ShortcutAction) action);
        liShortcuts.setModel (new AbstractListModel () {
            public int getSize () {
                return shortcuts.length;
            }
            public Object getElementAt (int i) {
                return shortcuts [i];
            }
        });        
    }
}
