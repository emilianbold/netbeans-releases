/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.dbschema.jdbcimpl.wizard;

import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.*;
import org.netbeans.modules.db.explorer.actions.ConnectUsingDriverAction;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;

import org.netbeans.modules.db.explorer.actions.DatabaseAction;
import org.netbeans.modules.db.explorer.infos.ConnectionNodeInfo;


public class DBSchemaConnectionPanel extends JPanel implements ListDataListener {

    static final long serialVersionUID = 5364628520334696421L;

    private ArrayList connInfos;
    private ArrayList list;
    private DBSchemaWizardData data;
    private Node dbNode;
    private String waitNode = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("WaitNode"); //NOI18N
    private Node[] drvNodes;
    private Vector conList = new Vector();

    /** Creates new form DBSchemaConnectionpanel */
    public DBSchemaConnectionPanel(DBSchemaWizardData data, ArrayList list) {
        this.list = list;
        connInfos = new ArrayList();
        this.data = data;

        putClientProperty("WizardPanel_contentSelectedIndex", new Integer(1)); //NOI18N
        setName(bundle.getString("ConnectionChooser")); //NOI18N

        initComponents ();
        resize();
        initAccessibility();

        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource("UI/Runtime"); //NOI18N
        DataFolder df;
        try {
            df = (DataFolder) DataObject.find(fo);
        } catch (DataObjectNotFoundException exc) {
            return;
        }
        dbNode = df.getNodeDelegate().getChildren().findChild("Databases"); //NOI18N
        setConnectionCombo(getConnectionNodes());
        existingConnComboBox.setSelectedIndex(0);
        existingConnComboBox.getModel().addListDataListener(this);
    }

    
    public   javax.swing.JComboBox getComboBox(){
        return existingConnComboBox;
    }

    private Node[] getConnectionNodes() {
        Node[] n;
        n = dbNode.getChildren().getNodes();
        while (n.length == 1 && waitNode.equals(n[0].getName())) {
            try {
                Thread.sleep(60);
            } catch (InterruptedException e) {
                //PENDING
            }
            n = dbNode.getChildren().getNodes();
        }
        
        return n;
    }

    private void setConnectionCombo(Node[] n) {
        connInfos.clear();
        conList.clear();
        existingConnComboBox.removeAllItems();
        Node.Cookie cookie;
        ConnectionNodeInfo info;
        for (int i = 0; i < n.length; i++) {
            cookie = n[i].getCookie(ConnectionNodeInfo.class);
            if (cookie != null) {
                info = (ConnectionNodeInfo) cookie;
                existingConnComboBox.addItem(n[i].getDisplayName());
                conList.add(n[i].getDisplayName());
                connInfos.add(info);
            }
        }
        if (existingConnComboBox.getItemCount() == 0)
            existingConnComboBox.insertItemAt(bundle.getString("NoConnection"), 0); //NOI18N
        else
            existingConnComboBox.insertItemAt(bundle.getString("SelectFromTheList"), 0); //NOI18N
        
        existingConnComboBox.addItem(bundle.getString("NewConnectionButton"));
    }
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_ConnectionPanelA11yDesc"));  // NOI18N
        descriptionTextArea.getAccessibleContext().setAccessibleName(bundle.getString("ACS_DescriptionA11yName"));  // NOI18N
        descriptionTextArea.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_DescriptionA11yDesc"));  // NOI18N
        existingConnComboBox.getAccessibleContext().setAccessibleName(bundle.getString("ACS_ExistingConnectionA11yName"));  // NOI18N
        existingConnComboBox.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_ExistingConnectionA11yDesc"));  // NOI18N
    }
    
    private void resize() {
        int width = (new Double(descriptionTextArea.getFontMetrics(descriptionTextArea.getFont()).getStringBounds(bundle.getString("Description"), getGraphics()).getWidth() / 2)).intValue() + 160;
        //        int height = (driverLabel.getFont().getSize() * 16) + 200;
        int height = 300;
        if (width < 675)
            width = 675;
        if (height < 390)
            height = 390;
        java.awt.Dimension dim = new java.awt.Dimension(width, height);
        setMinimumSize(dim);
        setPreferredSize(dim);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        descriptionTextArea = new javax.swing.JTextArea();
        existingConnComboBox = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        descriptionTextArea.setEditable(false);
        descriptionTextArea.setFont(javax.swing.UIManager.getFont("Label.font"));
        descriptionTextArea.setText(bundle.getString("Description"));
        descriptionTextArea.setDisabledTextColor(javax.swing.UIManager.getColor("Label.foreground"));
        descriptionTextArea.setRequestFocusEnabled(false);
        descriptionTextArea.setEnabled(false);
        descriptionTextArea.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(descriptionTextArea, gridBagConstraints);

        existingConnComboBox.setToolTipText(bundle.getString("ACS_ExistingConnectionComboBoxA11yDesc"));
        existingConnComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                existingConnComboBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 5);
        add(existingConnComboBox, gridBagConstraints);

    }//GEN-END:initComponents

    private Object getNewConObject(Vector old) {
        Object ret = null;
        for (int i = 0; i < conList.size(); i++)
            if (old.contains(conList.elementAt(i)))
                continue;
            else {
                ret = conList.elementAt(i);
                break;
            }

        return ret;
    }

    private Node[] getDriverNodes() {
        if (drvNodes != null)
            return drvNodes;

        drvNodes = dbNode.getChildren().findChild("Drivers").getChildren().getNodes(); //NOI18N
        while (drvNodes.length == 1 && waitNode.equals(drvNodes[0].getName())) {
            try {
                Thread.sleep(60);
            } catch (InterruptedException e) {
                //PENDING
            }
            drvNodes = dbNode.getChildren().findChild("Drivers").getChildren().getNodes(); //NOI18N
        }
        return drvNodes;
    }
    
     private void newConnectionButtonActionPerformed(java.awt.event.ActionEvent evt) {
         Node[] n = getDriverNodes();
 
         DatabaseAction dbAction = (DatabaseAction) SystemAction.get(ConnectUsingDriverAction.class);
         dbAction.performAction (n);
 
         Vector oldConnections = (Vector) conList.clone();
         setConnectionCombo(getConnectionNodes());
         existingConnComboBox.setSelectedItem(getNewConObject(oldConnections));
     }
     
    private void existingConnComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_existingConnComboBoxActionPerformed
        if(existingConnComboBox.getSelectedIndex() > 0){
            if (existingConnComboBox.getItemCount() == existingConnComboBox.getSelectedIndex()+1 ){
                newConnectionButtonActionPerformed( evt);
            }else{
            data.setConnectionNodeInfo((ConnectionNodeInfo) connInfos.get(existingConnComboBox.getSelectedIndex() - 1));
            }
        }
    }//GEN-LAST:event_existingConnComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea descriptionTextArea;
    private javax.swing.JComboBox existingConnComboBox;
    // End of variables declaration//GEN-END:variables

    private final ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.dbschema.jdbcimpl.resources.Bundle"); //NOI18N

    public boolean isValid() {
        return (existingConnComboBox.getSelectedIndex() > 0);
    }

    public void intervalAdded(final javax.swing.event.ListDataEvent p1) {
        fireChange(this);
    }

    public void intervalRemoved(final javax.swing.event.ListDataEvent p1) {
        fireChange(this);
    }

    public void contentsChanged(final javax.swing.event.ListDataEvent p1) {
        fireChange(this);
    }

    public void initData() {
        data.setExistingConn(true);
        if(existingConnComboBox.getSelectedIndex() > 0)
            data.setConnectionNodeInfo((ConnectionNodeInfo) connInfos.get(existingConnComboBox.getSelectedIndex() - 1));
    }

    public void fireChange (Object source) {
        ArrayList lst;

        synchronized (this) {
            lst = (ArrayList) this.list.clone();
        }

        ChangeEvent event = new ChangeEvent(source);
        for (int i=0; i< lst.size(); i++){
            ChangeListener listener = (ChangeListener) lst.get(i);
            listener.stateChanged(event);
        }
    }
}
