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

/*
 * ExternalBindingPanel.java
 *
 * Created on March 7, 2006, 11:21 PM
 */

package org.netbeans.modules.websvc.customization.multiview;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.config.Binding;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/**
 *
 * @author  Roderico Cruz
 */
public class ExternalBindingPanel extends SaveableSectionInnerPanel {
    private ExternalBindingTablePanel panel;
    private Node node;
    private JaxWsModel jmodel;
    private boolean jaxwsIsDirty;
    
    /** Creates new form ExternalBindingPanel */
    public ExternalBindingPanel(SectionView sectionView,
            Node node, JaxWsModel jmodel) {
        super(sectionView);
        this.node = node;
        this.jmodel = jmodel;
        
        ExternalBindingTablePanel.EBTableModel model = new ExternalBindingTablePanel.EBTableModel();
        panel = new ExternalBindingTablePanel(model, node, jmodel);
        panel.populateModel();
        initComponents2();
    }
    
    public JComponent getErrorComponent(String errorId) {
        return null;
    }
    
    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }
    
    public void setValue(JComponent source, Object value) {
        
    }
    
    private void initComponents2() {
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                .add(panel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(panel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
                );
    }// </editor-fold>
    
    public boolean wsdlIsDirty() {
        return false;
    }
    
    public boolean jaxwsIsDirty(){
        return jaxwsIsDirty;
    }
    
    public void save() {
        FileObject srcRoot = (FileObject)node.getLookup().lookup(FileObject.class);
        FileObject bindingsFolder = null;
        if(isClient()){
            JAXWSClientSupport support = JAXWSClientSupport.getJaxWsClientSupport(srcRoot);
            bindingsFolder = support.getBindingsFolderForClient(node.getName(), true);
            
        } else{
            JAXWSSupport support = JAXWSSupport.getJAXWSSupport(srcRoot);
            bindingsFolder = support.getBindingsFolderForService(node.getName(), true);
        }
        assert srcRoot != null : "Cannot find srcRoot";
        
        Client client = (Client)node.getLookup().lookup(Client.class);
        Service service = (Service)node.getLookup().lookup(Service.class);
        Map<String, FileObject> addedBindings = panel.getAddedBindings();
        Set<String> removedBindings = panel.getRemovedBindings();
        
        if(addedBindings.size() > 0 || removedBindings.size() > 0){
            jaxwsIsDirty = true;
        }
        
        //add new binding files
        for(String bindingName : addedBindings.keySet()){
            FileObject bindingFO = addedBindings.get(bindingName);
            if(bindingFO != null){
                String normalizedBindingName = bindingName;
                String ext = bindingFO.getExt();
                if(!ext.equals("")){
                    int index = bindingName.indexOf(ext);
                    normalizedBindingName = bindingName.substring(0, index - 1);
                }
                try{
                    FileObject copiedBinding = FileUtil.copyFile(bindingFO, bindingsFolder,
                            normalizedBindingName, bindingFO.getExt());
                    
                    DataObject dobj = DataObject.find(copiedBinding);
                    String relativePath = panel.getRelativePathToWsdl();
                    boolean changed = org.netbeans.modules.websvc.core.jaxws.JaxWsUtils.addRelativeWsdlLocation(copiedBinding, relativePath);
                    if(changed){
                        if(dobj != null){
                            SaveCookie sc = (SaveCookie)dobj.getCookie(SaveCookie.class);
                            if(sc != null){
                                sc.save();
                            }
                        }
                    }
                    if(dobj != null){
                        EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
                        ec.open();
                    }
                }catch(IOException e){
                    ErrorManager.getDefault().notify(e);
                }
                
                
                
            }
            if(client != null){
                Binding binding = client.newBinding();
                binding.setFileName(bindingName);
                //binding.setOriginalFileUrl(bindingFileUri.toString());
                client.addBinding(binding);
            } else{
                if(service != null){
                    Binding binding = service.newBinding();
                    binding.setFileName(bindingName);
                    //binding.setOriginalFileUrl(bindingFileUri.toString());
                    service.addBinding(binding);
                }
            }
        }
        //remove deleted bindings from the metadata file
        //TODO Shd we also delete the binding file from the bindings directory?
        
        for(String removedBinding : removedBindings){
            if(client != null){
                Binding binding = client.getBindingByFileName(removedBinding);
                if(binding != null){
                    client.removeBinding(binding);
                }
            } else if(service != null){
                Binding binding = service.getBindingByFileName(removedBinding);
                if(binding != null){
                    service.removeBinding(binding);
                }
            }
        }
        
    }
    
    private boolean isClient(){
        Client client = (Client)node.getLookup().lookup(Client.class);
        if(client != null){
            return true;
        }
        return false;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}
