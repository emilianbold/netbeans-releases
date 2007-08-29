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
package org.netbeans.modules.websvc.core.webservices.ui.panels;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.websvc.core.webservices.ui.WSHandlerDialog;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author  rico
 */
public class MessageHandlerPanel extends javax.swing.JPanel {
    
    private Project project;
    private List<String> handlerClasses;
    private boolean isJaxWS;
    private String componentName;
    private boolean isChanged;
    private int protocolIndex = -1;
    private final static String LOGICAL_TYPE = "Logical";
    private final static String PROTOCOL_TYPE = "Protocol";
    
    /** Creates new form HandlerPanel */
    public MessageHandlerPanel(Project project, List<String> handlerClasses, boolean isJaxWS,
            String componentName) {
        this.project = project;
        this.handlerClasses = handlerClasses;
        this.isJaxWS = isJaxWS;
        this.componentName = componentName;
        initComponents();
        addBtn.addActionListener(new AddButtonActionListener());
        removeBtn.addActionListener(new RemoveButtonActionListener());
        populateHandlers();
        isChanged = false;
        handlerTable.getColumnModel().getColumn(1).setCellRenderer(new TypeCellRenderer());
    }
    
    
    
    public boolean isChanged(){
        return isChanged;
    }
    
    public TableModel getHandlerTableModel(){
        return handlerTableModel;
    }
    
    private FileObject getFileObjectOfClass(String className){
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for(int i = 0; i < sourceGroups.length; i++){
            FileObject rootFolder = sourceGroups[i].getRootFolder();
            FileObject classFileObject = rootFolder.getFileObject(className.replaceAll("\\.", "/") + ".java");
            if(classFileObject != null){
                return classFileObject;
            }
        }
        return null;
    }
    
    private void populateHandlers(){
        ListIterator<String> listIterator = handlerClasses.listIterator();
        final int[] handlerType = new int[]{WSHandlerDialog.JAXWS_LOGICAL_HANDLER};
        while(listIterator.hasNext()){
            String handlerClass = listIterator.next();
            CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                    handlerType[0] = WSHandlerDialog.getHandlerType(controller, isJaxWS);
                }
                public void cancel() {}
            };
            
            FileObject classFO = getFileObjectOfClass(handlerClass);
            if(classFO != null){
                try {
                    JavaSource javaSource = JavaSource.forFileObject(classFO);
                    javaSource.runUserActionTask(task, true);
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
            if(handlerType[0] == WSHandlerDialog.JAXWS_LOGICAL_HANDLER){
                protocolIndex++;
            }
            handlerTableModel.addRow(new Object[] {handlerClass, handlerType[0]});
        }
        if(handlerTableModel.getRowCount() > 0){
            ((ListSelectionModel)handlerTable.getSelectionModel()).setSelectionInterval(0, 0);
        }
    }
    
    class RemoveButtonActionListener implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            int selectedRow = getSelectedRow();
            if(selectedRow == -1) return;
            String className = (String)handlerTableModel.getValueAt(selectedRow, 0);
            if(confirmDeletion(className)){
                Integer type = (Integer)handlerTableModel.getValueAt(selectedRow, 1);
                if(type == WSHandlerDialog.JAXWS_LOGICAL_HANDLER){
                    --protocolIndex;
                }
                handlerTableModel.removeRow(selectedRow);
                int newSelectedRow = selectedRow - 1;
                handlerTable.getSelectionModel().setSelectionInterval(newSelectedRow, newSelectedRow);
                isChanged = true;
            }
        }
        
        private boolean confirmDeletion(String className) {
            NotifyDescriptor.Confirmation notifyDesc =
                    new NotifyDescriptor.Confirmation(NbBundle.getMessage
                    (MessageHandlerPanel.class, "MSG_CONFIRM_DELETE", className, componentName),
                    NbBundle.getMessage(MessageHandlerPanel.class, "TTL_CONFIRM_DELETE"),
                    NotifyDescriptor.YES_NO_OPTION);
            DialogDisplayer.getDefault().notify(notifyDesc);
            return (notifyDesc.getValue() == NotifyDescriptor.YES_OPTION);
        }
    }
    
    class AddButtonActionListener implements ActionListener{
        DialogDescriptor dlgDesc = null;
        public void actionPerformed(ActionEvent evt){
            WSHandlerDialog wsHandlerDialog = new WSHandlerDialog(project, isJaxWS);
            wsHandlerDialog.show();
            if(wsHandlerDialog.okButtonPressed()){
                Map<String, Integer> selectedClasses = wsHandlerDialog.getSelectedClasses();
                if(selectedClasses.size() > 0){
                    int newSelectedRow = 0;
                    Set<String> classes = selectedClasses.keySet();
                    for(String selectedClass : classes){
                        int type = selectedClasses.get(selectedClass);
                        if(type == WSHandlerDialog.JAXWS_LOGICAL_HANDLER){
                            handlerTableModel.insertRow( ++protocolIndex, new Object[]{selectedClass, type});
                            newSelectedRow = protocolIndex ;
                        } else{
                            handlerTableModel.addRow( new Object[]{selectedClass, type});
                            newSelectedRow = handlerTableModel.getRowCount() -1;
                        }
                    }
                    handlerTable.getSelectionModel().setSelectionInterval(newSelectedRow, newSelectedRow);
                    isChanged = true;
                }
            }
        }
    }
    
    class HandlerTable extends JTable{
        public HandlerTable(){
            JTableHeader header = getTableHeader();
            header.setResizingAllowed(false);
            header.setReorderingAllowed(false);
            ListSelectionModel model = getSelectionModel();
            model.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
            model.addListSelectionListener(new HandlerListSelectionListener());
            
        }
    }
    
    class HandlerListSelectionListener implements ListSelectionListener{
        
        public void valueChanged(ListSelectionEvent e) {
            if(!e.getValueIsAdjusting()){
                int selectedRow = getSelectedRow();
                if(selectedRow == 0){
                    upBtn.setEnabled(false);
                } else{
                    if(!upBtn.isEnabled()){
                        upBtn.setEnabled(true);
                    }
                }
                if(selectedRow == handlerTableModel.getRowCount() - 1){
                    downBtn.setEnabled(false);
                } else{
                    if(!downBtn.isEnabled()){
                        downBtn.setEnabled(true);
                    }
                }
            }
        }
        
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        addBtn = new javax.swing.JButton();
        removeBtn = new javax.swing.JButton();
        upBtn = new javax.swing.JButton();
        downBtn = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        handlerTable = new HandlerTable();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/core/webservices/ui/panels/Bundle"); // NOI18N
        addBtn.setText(bundle.getString("Add_DotDotDot_label")); // NOI18N

        removeBtn.setText(bundle.getString("Remove_label")); // NOI18N

        upBtn.setText(org.openide.util.NbBundle.getMessage(MessageHandlerPanel.class, "LBL_Move_Up")); // NOI18N
        upBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpHandler(evt);
            }
        });

        downBtn.setText(org.openide.util.NbBundle.getMessage(MessageHandlerPanel.class, "LBL_Move_Down")); // NOI18N
        downBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownHandler(evt);
            }
        });

        handlerTableModel = new HandlerTableModel(new String[]{NbBundle.getMessage(MessageHandlerPanel.class, "HEADING_HANDLERS"),NbBundle.getMessage(MessageHandlerPanel.class, "HEADING_TYPE")}, 0);
        handlerTable.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        handlerTable.setModel(handlerTableModel);
        jScrollPane2.setViewportView(handlerTable);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 325, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(downBtn, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(upBtn, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(addBtn, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(removeBtn, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(36, 36, 36)
                        .add(addBtn)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeBtn)
                        .add(55, 55, 55)
                        .add(upBtn)
                        .add(18, 18, 18)
                        .add(downBtn))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 208, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(22, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
private void moveUpHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpHandler
    int selectedRow = getSelectedRow();
    if(selectedRow == -1) return;
    Integer type = (Integer)handlerTableModel.getValueAt(selectedRow, 1);
    if(type == WSHandlerDialog.JAXWS_MESSAGE_HANDLER){
        if((selectedRow -1) == protocolIndex){
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(MessageHandlerPanel.class,"TXT_CannotMoveUp",
                    NotifyDescriptor.WARNING_MESSAGE)
                    ));
            return;
        }
    }
    int newSelectedRow = selectedRow - 1;
    handlerTableModel.moveRow(selectedRow, selectedRow, newSelectedRow);
    handlerTable.getSelectionModel().setSelectionInterval(newSelectedRow, newSelectedRow);
    isChanged = true;
}//GEN-LAST:event_moveUpHandler

private void moveDownHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownHandler
    int selectedRow = getSelectedRow();
    if(selectedRow == -1) return;
    Integer type = (Integer)handlerTableModel.getValueAt(selectedRow, 1);
    if(type == WSHandlerDialog.JAXWS_LOGICAL_HANDLER){
        if(selectedRow == protocolIndex){
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(MessageHandlerPanel.class,"TXT_CannotMoveDown",
                    NotifyDescriptor.WARNING_MESSAGE)
                    ));
            return;
        }
    }
    int newSelectedRow = selectedRow + 1;
    handlerTableModel.moveRow(selectedRow, selectedRow, newSelectedRow);
    handlerTable.getSelectionModel().setSelectionInterval(newSelectedRow, newSelectedRow);
    
    isChanged = true;
    
}//GEN-LAST:event_moveDownHandler
private int getSelectedRow() {
    ListSelectionModel lsm = (ListSelectionModel)handlerTable.getSelectionModel();
    if (lsm.isSelectionEmpty()) {
        return -1;
    } else {
        return lsm.getMinSelectionIndex();
    }
}

class HandlerTableModel extends DefaultTableModel{
    public HandlerTableModel(Object[] columnNames, int rowCount){
        super(columnNames, rowCount);
    }
    public boolean isCellEditable(int row,
                              int column){
        return false;
    }    
}

class TypeCellRenderer implements TableCellRenderer{
    
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel typeLabel = new JLabel();
        if(column == 1){
            Integer type = (Integer)value;
            if(type == WSHandlerDialog.JAXWS_LOGICAL_HANDLER){
                typeLabel.setText(LOGICAL_TYPE);
            }else{
                typeLabel.setText(PROTOCOL_TYPE);
            }
        }
        return typeLabel;
    }
    
}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addBtn;
    private javax.swing.JButton downBtn;
    private javax.swing.JTable handlerTable;
    private javax.swing.table.DefaultTableModel handlerTableModel;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton removeBtn;
    private javax.swing.JButton upBtn;
    // End of variables declaration//GEN-END:variables
    
}
