/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

/*
 * ReferencesPanel.java
 *
 * Created on Apr 14, 2009, 4:38:39 PM
 */

package org.netbeans.modules.javadoc.search;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * UI to display javadoc references in details.
 * <p>Usage: {@code ReferencesPanel.showInWindow()}
 *
 * @see IndexBuilder
 * @see org.netbeans.modules.javadoc.search.IndexOverviewAction
 * 
 * @author Jan Pokorsky
 */
public class ReferencesPanel extends javax.swing.JPanel implements Runnable, ListSelectionListener {

    private static final String PLEASE_WAIT = NbBundle.getMessage(ReferencesPanel.class, "ReferencesPanel.wait.text");
    private static final Object LOCK = new Object();
    private static final String EMPTY_LOCATION = ""; // NOI18N
    private static final RequestProcessor RP = new RequestProcessor(ReferencesPanel.class.getName(), 1, false, false);
    private int state = 0;
    private ListModel model;
    /** Descriptions of indices that should be accessed under {@link #LOCK lock}. */
    private ItemDesc[] items;
    private final AbstractButton openBtn;

    /** Creates new form ReferencesPanel */
    public ReferencesPanel(AbstractButton openBtn) {
        initComponents();
        Mnemonics.setLocalizedText(listLabel, NbBundle.getMessage(ReferencesPanel.class, "ReferencesPanel.listLabel.text"));
        Mnemonics.setLocalizedText(locationLabel, NbBundle.getMessage(ReferencesPanel.class, "ReferencesPanel.locationLabel.text"));
        this.openBtn = openBtn;
    }

    public static FileObject showInWindow() {
        JButton openBtn = new JButton();
        Mnemonics.setLocalizedText(openBtn, NbBundle.getMessage(ReferencesPanel.class, "ReferencesPanel.ok.text"));
        openBtn.getAccessibleContext().setAccessibleDescription(openBtn.getText());
        openBtn.setEnabled(false);

        final Object[] buttons = new Object[] { openBtn, DialogDescriptor.CANCEL_OPTION };

        ReferencesPanel panel = new ReferencesPanel(openBtn);
        DialogDescriptor desc = new DialogDescriptor(
                panel,
                NbBundle.getMessage(ReferencesPanel.class, "ReferencesPanel.title"),
                true,
                buttons,
                openBtn,
                DialogDescriptor.DEFAULT_ALIGN,
                HelpCtx.DEFAULT_HELP,
                null);
        desc.setClosingOptions(buttons);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(desc);
        dialog.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ReferencesPanel.class, "AN_ReferencesDialog"));
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ReferencesPanel.class, "AD_ReferencesDialog"));

        // schedule computing indices
        RP.post(panel);
        dialog.setVisible(true);
        dialog.dispose();

        return desc.getValue() == openBtn
                ? panel.getSelectedItem()
                : null;
    }

    public void run() {
        switch (state) {
            case 0:
                runGetIndiciesTask();
                break;
            case 1:
                runListUpdateTask();
                break;
        }
    }

    private void runGetIndiciesTask() {
        final List[] data = IndexBuilder.getDefault().getIndices(true);
        final List<String> names = data[0]; // List<String>
        final List<FileObject> indices = data[1]; // List<FileObject>

        synchronized (LOCK) {

            ItemDesc[] modelItems;
            if (names.isEmpty()) {
                modelItems = new ItemDesc[] { ItemDesc.noItem() };
            } else {
                modelItems = new ItemDesc[names.size()];
                this.items = modelItems;
                int i = 0;
                for (String name : names) {
                    modelItems[i] = new ItemDesc(name, indices.get(i));
                    i++;
                }
            }
            
            model = new FixListModel(modelItems);
        }

        state = 1;
        EventQueue.invokeLater(this);
    }

    private void runListUpdateTask() {
        refList.setModel(model);
        refList.addListSelectionListener(this);
        refList.setSelectedIndex(0);
    }

    public void valueChanged(ListSelectionEvent e) {
        FileObject item = getSelectedItem();
        String s = item == null
                ? EMPTY_LOCATION
                : FileUtil.getFileDisplayName(item);
        locationField.setText(s);
        openBtn.setEnabled(item != null);
    }

    FileObject getSelectedItem() {
        int index = refList.getSelectedIndex();
        synchronized (LOCK) {
            return index < 0 || items == null || items.length == 0
                    ? null
                    : items[index].location;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        listLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        refList = new javax.swing.JList(new String[] {PLEASE_WAIT});
        locationField = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();

        listLabel.setLabelFor(refList);
        listLabel.setText(org.openide.util.NbBundle.getMessage(ReferencesPanel.class, "ReferencesPanel.listLabel.text")); // NOI18N

        refList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(refList);
        refList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ReferencesPanel.class, "ReferencesPanel.refList.AccessibleContext.accessibleName")); // NOI18N
        refList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ReferencesPanel.class, "ReferencesPanel.refList.AccessibleContext.accessibleDescription")); // NOI18N

        locationField.setEditable(false);

        locationLabel.setLabelFor(locationField);
        locationLabel.setText(org.openide.util.NbBundle.getMessage(ReferencesPanel.class, "ReferencesPanel.locationLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, listLabel)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, locationField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, locationLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(listLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(locationLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(locationField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        listLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ReferencesPanel.class, "ReferencesPanel.listLabel.AccessibleContext.accessibleName")); // NOI18N
        listLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ReferencesPanel.class, "ReferencesPanel.listLabel.AccessibleContext.accessibleDescription")); // NOI18N
        locationLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ReferencesPanel.class, "ReferencesPanel.locationLabel.AccessibleContext.accessibleName")); // NOI18N
        locationLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ReferencesPanel.class, "ReferencesPanel.locationLabel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel listLabel;
    private javax.swing.JTextField locationField;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JList refList;
    // End of variables declaration//GEN-END:variables

    private static final class ItemDesc {
        private static ItemDesc NO_ITEM;
        String name;
        String locationName;
        FileObject location;

        public ItemDesc(String name, FileObject location) {
            this.name = name;
            this.location = location;
        }

        String getLocationName() {
            if (locationName == null) {
                locationName = FileUtil.getFileDisplayName(location);
            }
            return locationName;
        }

        static ItemDesc noItem() {
            if (NO_ITEM == null) {
                NO_ITEM = new ItemDesc(NbBundle.getMessage(ReferencesPanel.class, "ReferencesPanel.noJavadoc"), null);
            }
            return NO_ITEM;
        }

    }

    private static final class FixListModel implements ListModel {

        private ItemDesc[] items;

        public FixListModel(ItemDesc[] items) {
            this.items = items;
        }

        public int getSize() {
            return items.length;
        }

        public Object getElementAt(int index) {
            return items[index].name;
        }

        public void addListDataListener(ListDataListener l) {
            // no op
        }

        public void removeListDataListener(ListDataListener l) {
            // no op
        }

    }
}
