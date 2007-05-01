/*
 * UMLMiscOptionsPanelForm.java
 *
 * Created on April 26, 2007, 10:40 AM
 */

package org.netbeans.modules.uml.propertysupport.options;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import org.netbeans.modules.uml.propertysupport.options.api.UMLOptionsPanel;

/**
 *
 * @author  krichard
 */
public class UMLMiscOptionsPanelForm extends JPanel {
    
    /** Creates new form UMLMiscOptionsPanelForm */
    public UMLMiscOptionsPanelForm() {
        //initComponents();

        super (new BorderLayout()) ;
        
        
    }

    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();

        jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .add(0, 0, Short.MAX_VALUE)
                    .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(0, 0, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 300, Short.MAX_VALUE)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .add(0, 0, Short.MAX_VALUE)
                    .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(0, 0, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    public void addTab (UMLOptionsPanel panel) {
        if (panel != null)
            jTabbedPane1.addTab(panel.getDisplayName(), panel.create());
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables
    
}
