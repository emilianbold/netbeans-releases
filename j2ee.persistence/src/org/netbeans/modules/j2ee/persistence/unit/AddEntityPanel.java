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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.persistence.unit;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;

/**
 * Panel for adding entities to persistence unit.
 *
 * @author  Erno Mononen
 */
public class AddEntityPanel extends javax.swing.JPanel {
    
    /** Creates new form AddClassPanel */
    public AddEntityPanel(Set<Entity> entityClasses) {
        initComponents();
        List<Entity> sortedEntityClasses = PersistenceUtils.sortEntityClasses(entityClasses);
        DefaultListModel model = new DefaultListModel();
        for (Entity each : sortedEntityClasses) {
            model.addElement(each);
        }
        this.entityList.setModel(model);
        this.entityList.setCellRenderer(new EntityListCellRenderer());
    }
    
    /**
     * @return fully qualified names of the selected entities' classes.
     */
    public List<String> getSelectedEntityClasses(){
        List<String> result = new ArrayList<String>();
        for (Object elem : entityList.getSelectedValues()) {
            result.add(((Entity) elem).getClass2());
        }
        return result;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        entityList = new javax.swing.JList();

        jScrollPane1.setViewportView(entityList);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 377, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 143, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList entityList;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
    

    private static class EntityListCellRenderer extends DefaultListCellRenderer implements ListCellRenderer{
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setText(((Entity) value).getClass2()); 
            return this;   
        }
        
    }
}
