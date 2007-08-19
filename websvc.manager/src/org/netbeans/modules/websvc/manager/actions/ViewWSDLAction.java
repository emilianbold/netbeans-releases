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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.manager.actions;

import org.netbeans.modules.websvc.manager.model.WebServiceData;
import java.awt.Cursor;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import org.netbeans.modules.websvc.manager.nodes.WebServiceNode;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.nodes.FilterNode;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.netbeans.modules.websvc.manager.nodes.WebServicesPortNode;

/**
 *
 * @author  David Botterill, cao
 */
public class ViewWSDLAction extends NodeAction {
    
    /** Creates a new instance of ViewWSDLAction */
    public ViewWSDLAction() {
    }
    
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }
    
    public boolean asynchronous() {
        return true;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx("projrave_ui_elements_server_nav_websvc_view_wsdl");
    }
    
    public String getName() {
        return NbBundle.getMessage(ViewWSDLAction.class, "VIEW_WSDL");
    }
    
    protected void performAction(Node[] activatedNodes) {
        if(null != activatedNodes && activatedNodes.length > 0) {
            
            /**
             * First get the web service data
             */
            WebServiceData wsData = null;
            for(int ii = 0; ii < activatedNodes.length; ii++) {
                Node node = null;
                if(activatedNodes[ii] instanceof FilterNode){
                    node = (Node)(activatedNodes[ii]).getCookie(WebServicesPortNode.class);
                    if (node == null) {
                        node = (Node)(activatedNodes[ii]).getCookie(WebServiceNode.class);
                    }
                }else{
                    node = activatedNodes[ii];
                }
                
                if(node != null && (node instanceof WebServicesPortNode || node instanceof WebServiceNode)) {
                    
                    if (node instanceof WebServiceNode) {
                        wsData = ((WebServiceNode)node).getWebServiceData();
                    }else {
                        wsData = ((WebServicesPortNode)node).getWebServiceData();
                    }
                    File wsdlFile = new File(wsData.getURL());
                    LocalFileSystem localFileSystem = null;
                    try {
                        
                        
                        this.getMenuPresenter().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        this.getMenuPresenter().validate();
                        
                        this.getMenuPresenter().setCursor(null);
                        if(null == wsdlFile) {
                            ErrorManager.getDefault().log(this.getClass().getName() + ".performAction: " +
                            NbBundle.getMessage(ViewWSDLAction.class, "READING_WSDL_RETURNED_NULL"));
                            String errorMessage = NbBundle.getMessage(ViewWSDLAction.class, "WSDL_FILE_NOT_FOUND",wsData.getURL());
                            NotifyDescriptor d = new NotifyDescriptor.Message(errorMessage);
                            Object response = DialogDisplayer.getDefault().notify(d);
                            return;
                        }
                        /**
                         * get the local file system and then the WSDL file.
                         */
                        localFileSystem = new LocalFileSystem();
                        localFileSystem.setRootDirectory(new File(wsdlFile.getParent()));
                        /**
                         * Now set the filesystem to read-only
                         */
                        localFileSystem.setReadOnly(true);
                    } catch(PropertyVetoException pve) {
                        ErrorManager.getDefault().notify(pve);
                        String errorMessage = NbBundle.getMessage(ViewWSDLAction.class, "WSDL_FILE_NOT_FOUND",wsData.getURL());
                        NotifyDescriptor d = new NotifyDescriptor.Message(errorMessage);
                        Object response = DialogDisplayer.getDefault().notify(d);
                        return;
                        
                    } catch(IOException ioe) {
                        ErrorManager.getDefault().notify(ioe);
                        String errorMessage = NbBundle.getMessage(ViewWSDLAction.class, "WSDL_FILE_NOT_FOUND",wsData.getURL());
                        NotifyDescriptor d = new NotifyDescriptor.Message(errorMessage);
                        Object response = DialogDisplayer.getDefault().notify(d);
                        return;
                    }
                    
                    /**
                     * Now we need to get just the file name without the path.
                     */
                    String pathName = wsdlFile.getAbsolutePath();
                    String fileName = pathName.substring(pathName.lastIndexOf(File.separator)+1);
                    FileObject wsdlFileObject = localFileSystem.findResource(fileName);
                    if(null == wsdlFileObject){
                        ErrorManager.getDefault().log(this.getClass().getName() + ".performAction: " +
                        NbBundle.getMessage(ViewWSDLAction.class, "READ_WSDL_NOT_FOUND"));
                        String errorMessage = NbBundle.getMessage(ViewWSDLAction.class, "WSDL_FILE_NOT_FOUND",wsData.getURL());
                        NotifyDescriptor d = new NotifyDescriptor.Message(errorMessage);
                        Object response = DialogDisplayer.getDefault().notify(d);
                        return;
                    }
                    
                    /**
                     * Create a DataObject out of the FileObject so we can edit the file.
                     */
                    DataObject wsdlDataObject = null;
                    
                    try {
                        wsdlDataObject = DataObject.find(wsdlFileObject);
                    } catch (DataObjectNotFoundException donf) {
                        ErrorManager.getDefault().notify(donf);
                        String errorMessage = NbBundle.getMessage(ViewWSDLAction.class, "WSDL_FILE_NOT_FOUND",wsData.getURL());
                        NotifyDescriptor d = new NotifyDescriptor.Message(errorMessage);
                        Object response = DialogDisplayer.getDefault().notify(d);
                        return;
                    }
                    
                    /**
                     * Get the EditorCookie for file and display it.
                     */
                    EditorCookie editorCookie = (EditorCookie)wsdlDataObject.getCookie(EditorCookie.class);
                    if (null != editorCookie) {
                        editorCookie.open();
                    } else {
                        ErrorManager.getDefault().log(this.getClass().getName() + ".performAction: " +
                        NbBundle.getMessage(ViewWSDLAction.class, "ERROR_GETTING_WSDL_EDITOR_COOKIE"));
                        String errorMessage = NbBundle.getMessage(ViewWSDLAction.class, "WSDL_FILE_NOT_FOUND",wsData.getURL());
                        NotifyDescriptor d = new NotifyDescriptor.Message(errorMessage);
                        Object response = DialogDisplayer.getDefault().notify(d);
                        return;
                    }
                }
            }
        }
        
    }
    
}
