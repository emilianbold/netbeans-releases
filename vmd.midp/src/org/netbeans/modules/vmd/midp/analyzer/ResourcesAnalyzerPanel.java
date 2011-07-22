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

package org.netbeans.modules.vmd.midp.analyzer;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.DeleteSupport;
import org.netbeans.modules.vmd.midp.components.resources.ResourceCD;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 *
 * @author  Anton Chechel
 */
public class ResourcesAnalyzerPanel extends javax.swing.JPanel {
    
    private DesignDocument document;
    private Map<Long, String> resourceNames;
    private Map<Long, Icon> resourceIcons;
    private List<Long> resourceIDs;
    private Icon resourceIcon = new ImageIcon(ResourceCD.ICON_PATH);

    ResourcesAnalyzerPanel() {
        initComponents();
        resourceIDs = new ArrayList<Long>();
        resourceNames = new HashMap<Long, String>();
        resourceIcons = new HashMap<Long, Icon>();
        resourcesList.setCellRenderer(new ResourcesListRenderer());
    }

    void setUnusedResources(DesignDocument document,List<DesignComponent> resources) {
        Collections.sort (resources, new Comparator<DesignComponent>() {
            public int compare (DesignComponent c1, DesignComponent c2) {
                int i = c1.getType ().toString ().compareToIgnoreCase (c2.getType ().toString ());
                if (i != 0)
                    return i;
                String s1 = InfoPresenter.getDisplayName (c1);
                String s2 = InfoPresenter.getDisplayName (c2);
                if (s1 != null) {
                    i = s1.compareToIgnoreCase (s2);
                    if (i != 0)
                        return i;
                    return s1.compareTo (s2);
                } else
                    return s2 != null ? 1 : 0;
            }
        });
        // do not change list if the resource are equal
        if (resources.size() == resourceIDs.size()) {
            for (int i = 0; i < resources.size(); i++) {
                if (resources.get(i).getComponentID() == resourceIDs.get(i)) {
                    return;
                }
            }
        }

        resourceIDs.clear ();
        resourceNames.clear ();
        resourceIcons.clear ();
        this.document = document;
        ((DefaultListModel) resourcesList.getModel()).removeAllElements();

        if (resources.isEmpty ()) {
            ((DefaultListModel) resourcesList.getModel()).addElement(NbBundle.getMessage (ResourcesAnalyzerPanel.class, "ResourcesAnalyzer.nothing-found")); // NOI18N
            resourcesList.clearSelection ();
        } else {
            for (DesignComponent resource : resources) {
                resourceIDs.add (resource.getComponentID ());

                InfoPresenter info = resource.getPresenter (InfoPresenter.class);
                String resourceName;
                Image image;
                if (info != null) {
                    resourceName = info.getDisplayName (InfoPresenter.NameType.PRIMARY);
                    image = info.getIcon (InfoPresenter.IconType.COLOR_16x16);
                } else {
                    Debug.warning ("Missing InfoPresenter for", resource); // NOI18N
                    resourceName = NbBundle.getMessage (ResourcesAnalyzerPanel.class, "ResourcesAnalyzer.no-label"); // NOI18N
                    image = null;
                }

                resourceNames.put (resource.getComponentID (), resourceName);
                resourceIcons.put (resource.getComponentID (), image != null ? new ImageIcon (image) : this.resourceIcon);

                ((DefaultListModel) resourcesList.getModel ()).addElement (resource.getComponentID ());
            }

            int size = resourcesList.getModel().getSize();
            if (size > 0) {
                resourcesList.setSelectionInterval(0, size - 1);
            }
        }

    }

    private void removeUnusedResources(final Object[] selectedElements) {
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                for (Object selected : selectedElements) {
                    if (! (selected instanceof Long))
                        continue;
                    DesignComponent resource = document.getComponentByUID((Long) selected);
                    if (resource != null)
                        DeleteSupport.invokeDirectUserDeletion (document, Collections.singleton (resource), false);
                    ((DefaultListModel) resourcesList.getModel()).removeElement(selected);
                }
            }
        });
        if (resourcesList.getModel ().getSize () == 0)
            ((DefaultListModel) resourcesList.getModel()).addElement(NbBundle.getMessage (ResourcesAnalyzerPanel.class, "ResourcesAnalyzer.nothing-found")); // NOI18N
    }
    
    private class ResourcesListRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            final JLabel renderer = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (document != null  &&  value instanceof Long) {
                renderer.setText(resourceNames.get((Long) value));
                renderer.setIcon(resourceIcons.get((Long) value));
            }
            return renderer;
        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        removeButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        resourcesList = new javax.swing.JList();

        setPreferredSize(new java.awt.Dimension(400, 150));

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(ResourcesAnalyzerPanel.class, "ResourcesAnalyzerPanel.removeButton.text")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        resourcesList.setModel(new DefaultListModel());
        jScrollPane1.setViewportView(resourcesList);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeButton))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(removeButton)
                .addContainerGap(125, Short.MAX_VALUE))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    
private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
    removeUnusedResources(resourcesList.getSelectedValues());
}//GEN-LAST:event_removeButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton removeButton;
    private javax.swing.JList resourcesList;
    // End of variables declaration//GEN-END:variables
    
}
