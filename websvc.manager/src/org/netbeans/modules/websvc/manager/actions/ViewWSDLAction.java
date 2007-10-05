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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.websvc.manager.actions;

import org.netbeans.modules.websvc.manager.model.WebServiceData;
import java.awt.Cursor;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author  David Botterill, cao
 */
public class ViewWSDLAction extends NodeAction {
    
    /** Creates a new instance of ViewWSDLAction */
    public ViewWSDLAction() {
    }
    
    protected boolean enable(Node[] nodes) {
        if(nodes != null &&
        nodes.length != 0) {
            for (int i = 0; i < nodes.length; i++) {
                WebServiceData wsData = nodes[i].getLookup().lookup(WebServiceData.class);
                
                if (wsData != null && wsData.getURL() != null && wsData.getURL().length() > 0) {
                    return true;
                }
            }
        }
        
        return false;
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
            WebServiceData wsData = null;
            for(int ii = 0; ii < activatedNodes.length; ii++) {
                wsData = activatedNodes[ii].getLookup().lookup(WebServiceData.class);
                
                if (wsData == null || wsData.getURL() == null || wsData.getURL().length() == 0) {
                    continue;
                }

                File wsdlFile = new File(wsData.getURL());
                LocalFileSystem localFileSystem = null;
                try {


                    this.getMenuPresenter().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    this.getMenuPresenter().validate();

                    this.getMenuPresenter().setCursor(null);
                    if (null == wsdlFile) {
                        ErrorManager.getDefault().log(this.getClass().getName() + ".performAction: " + 
                                NbBundle.getMessage(ViewWSDLAction.class, "READING_WSDL_RETURNED_NULL"));
                        String errorMessage = NbBundle.getMessage(ViewWSDLAction.class, "WSDL_FILE_NOT_FOUND", wsData.getURL());
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
                } catch (PropertyVetoException pve) {
                    ErrorManager.getDefault().notify(pve);
                    String errorMessage = NbBundle.getMessage(ViewWSDLAction.class, "WSDL_FILE_NOT_FOUND", wsData.getURL());
                    NotifyDescriptor d = new NotifyDescriptor.Message(errorMessage);
                    Object response = DialogDisplayer.getDefault().notify(d);
                    return;
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ioe);
                    String errorMessage = NbBundle.getMessage(ViewWSDLAction.class, "WSDL_FILE_NOT_FOUND", wsData.getURL());
                    NotifyDescriptor d = new NotifyDescriptor.Message(errorMessage);
                    Object response = DialogDisplayer.getDefault().notify(d);
                    return;
                }

                /**
                 * Now we need to get just the file name without the path.
                 */
                String pathName = wsdlFile.getAbsolutePath();
                String fileName = pathName.substring(pathName.lastIndexOf(File.separator) + 1);
                FileObject wsdlFileObject = localFileSystem.findResource(fileName);
                if (null == wsdlFileObject) {
                    ErrorManager.getDefault().log(this.getClass().getName() + ".performAction: " + NbBundle.getMessage(ViewWSDLAction.class, "READ_WSDL_NOT_FOUND"));
                    String errorMessage = NbBundle.getMessage(ViewWSDLAction.class, "WSDL_FILE_NOT_FOUND", wsData.getURL());
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
                    String errorMessage = NbBundle.getMessage(ViewWSDLAction.class, "WSDL_FILE_NOT_FOUND", wsData.getURL());
                    NotifyDescriptor d = new NotifyDescriptor.Message(errorMessage);
                    Object response = DialogDisplayer.getDefault().notify(d);
                    return;
                }

                /**
                 * Get the EditorCookie for file and display it.
                 */
                EditorCookie editorCookie = (EditorCookie) wsdlDataObject.getCookie(EditorCookie.class);
                if (null != editorCookie) {
                    editorCookie.open();
                } else {
                    ErrorManager.getDefault().log(this.getClass().getName() + ".performAction: " + NbBundle.getMessage(ViewWSDLAction.class, "ERROR_GETTING_WSDL_EDITOR_COOKIE"));
                    String errorMessage = NbBundle.getMessage(ViewWSDLAction.class, "WSDL_FILE_NOT_FOUND", wsData.getURL());
                    NotifyDescriptor d = new NotifyDescriptor.Message(errorMessage);
                    Object response = DialogDisplayer.getDefault().notify(d);
                    return;
                }
            }
        }

    }
    
}
