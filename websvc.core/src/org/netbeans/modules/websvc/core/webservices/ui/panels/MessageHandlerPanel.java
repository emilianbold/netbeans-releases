/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.core.webservices.ui.panels;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.GridBagConstraints;
import javax.swing.ListSelectionModel;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.GridBagLayout;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.javacore.api.JavaModel;
import org.openide.util.NbBundle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.api.project.Project;
import org.openide.DialogDescriptor;
import java.awt.Dialog;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import javax.swing.table.DefaultTableModel;
import org.openide.filesystems.FileObject;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.loaders.DataObject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.common.Util;

/**
 * @author  rico
 */
public class MessageHandlerPanel extends JPanel{
    
    private Project project;
    private String[] handlerClasses;
    private FileObject srcRoot;
    
    /** Creates a new instance of MessageHandlerPanel */
    public MessageHandlerPanel(Project project, String[] handlerClasses, FileObject srcRoot) {
        this.project = project;
        this.handlerClasses = handlerClasses;
        this.srcRoot = srcRoot;
        initComponents();
        populateHandlers();
    }
    
    private void initComponents(){
        GridBagConstraints gridBagConstraints;
        classesScrollPane = new JScrollPane();
        messageHandlerTable = new JTable();
        tblModel = new HandlerTableModel(0,1);
        messageHandlerTable.setModel(tblModel);
        ListSelectionModel ls = messageHandlerTable.getSelectionModel();
        ls.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        formIntro = new JLabel();
        btnPanel = new JPanel();
        addBtn = new JButton();
        addBtn.addActionListener(new AddButtonActionListener());
        removeBtn = new JButton();
        addBtn.setText(NbBundle.getMessage(MessageHandlerPanel.class,
        "Add_DotDotDot_label"));
        removeBtn.setText(NbBundle.getMessage(MessageHandlerPanel.class,
        "Remove_label"));
        removeBtn.addActionListener(new RemoveButtonActionListener());
        formIntro.setText(NbBundle.getMessage(MessageHandlerPanel.class,
        "MessageHandler_Intro_msg"));
        
        setLayout(new GridBagLayout());
        
        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 12, 12)));
        classesScrollPane.setViewportView(messageHandlerTable);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(classesScrollPane, gridBagConstraints);
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(formIntro, gridBagConstraints);
        
        btnPanel.setLayout(new java.awt.GridLayout(1, 0, 6, 0));
        btnPanel.add(addBtn);
        btnPanel.add(removeBtn);
        
        gridBagConstraints.gridy = 2;
        add(btnPanel, gridBagConstraints);
        
    }
    
    private int getSelectedRow() {
        ListSelectionModel lsm = (ListSelectionModel)messageHandlerTable.getSelectionModel();
        if (lsm.isSelectionEmpty()) {
            return -1;
        }
        else {
            return lsm.getMinSelectionIndex();
        }
    }
    
    private void populateHandlers(){
        for(int i = 0; i < handlerClasses.length; i++){
            tblModel.addRow(new String[]{handlerClasses[i]});
        }
    }
    
    public DefaultTableModel getTableModel(){
        return tblModel;
    }
    
    class RemoveButtonActionListener implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            int selectedRow = getSelectedRow();
            String className = (String)tblModel.getValueAt(selectedRow, 0);
            if(confirmDeletion(className)){
                tblModel.removeRow(selectedRow);
            }
        }
        
        private boolean confirmDeletion(String className) {
            NotifyDescriptor.Confirmation notifyDesc =
            new NotifyDescriptor.Confirmation(NbBundle.getMessage
            (MessageHandlerPanel.class, "MSG_CONFIRM_DELETE", className),
            NotifyDescriptor.YES_NO_OPTION);
            DialogDisplayer.getDefault().notify(notifyDesc);
            return (notifyDesc.getValue() == NotifyDescriptor.YES_OPTION);
        }
    }
    
    class AddButtonActionListener implements ActionListener{
        DialogDescriptor dlgDesc = null;
        public void actionPerformed(ActionEvent evt){
            
            final SelectHandlerPanel sPanel = new SelectHandlerPanel(project);
            dlgDesc = new DialogDescriptor(sPanel, "Select Message Handler", true,
            new ActionListener(){
                public void actionPerformed(ActionEvent evt){
                    if(evt.getSource() == NotifyDescriptor.OK_OPTION){
                        boolean accepted = true;
                        String errMsg = null;
                        Node[] selectedNodes = sPanel.getSelectedNodes();
                        for(int i = 0; i < selectedNodes.length; i++){
                            Node node = selectedNodes[i];
                            JavaClass classElement = JMIUtils.getJavaClassFromNode(node);
                            //FIX-ME: Improve this by filtering the Tree View to only include handlers
                            if(classElement == null){
                                errMsg = NbBundle.getMessage(MessageHandlerPanel.class,
                                "NotJavaClass_msg");
                                accepted = false;
                                break;
                            }
                            
                            if(!isHandler(classElement)) {
                                errMsg = NbBundle.getMessage(MessageHandlerPanel.class,
                                "NotHandlerClass_msg",
                                classElement.getName());
                                accepted = false;
                                break;
                            }
                        }
                        if (!accepted) {
                            NotifyDescriptor.Message notifyDescr =
                            new NotifyDescriptor.Message(errMsg,
                            NotifyDescriptor.ERROR_MESSAGE );
                            DialogDisplayer.getDefault().notify(notifyDescr);
                            dlgDesc.setClosingOptions(closingOptionsWithoutOK);
                        } else {
                            // Everything was fine so allow OK
                            dlgDesc.setClosingOptions(closingOptionsWithOK);
                        }
                    }
                }
            });
            Dialog dialog = DialogDisplayer.getDefault().createDialog(dlgDesc);
            dialog.show();
            if (dlgDesc.getValue() == DialogDescriptor.OK_OPTION) {
                Node[] selectedNodes = sPanel.getSelectedNodes();
                for(int i = 0; i < selectedNodes.length; i++){
                    Node node = selectedNodes[i];
                    JavaClass classElement = JMIUtils.getJavaClassFromNode(node);
                    
                    tblModel.addRow(new String[]{classElement.getName()});
                }
            }
        }
    }
    
    private boolean isHandler(JavaClass ce) {
        if (ce != null) {
            JavaClass handlerClass = JMIUtils.findClass("javax.xml.rpc.handler.Handler");
            return ce.isSubTypeOf(handlerClass);
        }
        return false;
    }
    
    
    private Object[] closingOptionsWithoutOK = {DialogDescriptor.CANCEL_OPTION,
    DialogDescriptor.CLOSED_OPTION};
    private Object[] closingOptionsWithOK = {DialogDescriptor.CANCEL_OPTION,
    DialogDescriptor.CLOSED_OPTION, DialogDescriptor.OK_OPTION};
    
    private static class HandlerTableModel extends javax.swing.table.DefaultTableModel {
        
        public HandlerTableModel(int rows, int cols) {
            super(rows, cols);
        }
        
        public String getColumnName(int col) {
            if(col == 0){
                return NbBundle.getMessage(MessageHandlerPanel.class,
                "PathName_columnTitle");
            }
            return null;
        }
    }
    
    private JScrollPane classesScrollPane;
    private JTable messageHandlerTable;
    private JLabel formIntro;
    private JPanel btnPanel;
    private JButton addBtn;
    private JButton removeBtn;
    private HandlerTableModel tblModel;
    
}
