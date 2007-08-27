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

package org.netbeans.modules.xml.jaxb.actions;

import java.io.File;
import java.net.URL;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.jaxb.util.FileSysUtil;
import org.netbeans.modules.xml.jaxb.util.JAXBWizModuleConstants;
import org.netbeans.modules.xml.jaxb.util.ProjectHelper;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author lgao
 */
public class JAXBRefreshAction extends NodeAction  {
    
    /** Creates a new instance of JAXBRefreshAction */
    public JAXBRefreshAction() {
    }

    protected void performAction(Node[] nodes) {
        Node node = nodes[ 0 ];
        FileObject fo = node.getLookup().lookup( FileObject.class );
        Project proj = node.getLookup().lookup(Project.class);
        String origLoc = (String) node.getValue(
                JAXBWizModuleConstants.ORIG_LOCATION);
        Boolean origLocIsURL = (Boolean) node.getValue(
                JAXBWizModuleConstants.ORIG_LOCATION_TYPE);
        FileObject locSchemaRoot = (FileObject) node.getValue(
                JAXBWizModuleConstants.LOC_SCHEMA_ROOT);
        
        if ( ( fo != null ) && ( origLoc != null ) ) {
            // XXX TODO run in separate non-awt thread.
             try {
                 if (fo.canWrite()){
                     if (origLocIsURL){
                        URL url = new URL(origLoc);
                         ProjectHelper.retrieveResource(locSchemaRoot, 
                                 url.toURI());                        
                     } else {
                         File projDir = FileUtil.toFile(
                                 proj.getProjectDirectory());
                         //File srcFile = new File(origLoc);
                         File srcFile = FileSysUtil.Relative2AbsolutePath(
                                 projDir, origLoc);
                         ProjectHelper.retrieveResource(fo.getParent(), 
                                 srcFile.toURI());
                     }
                 } else {
                     String msg = NbBundle.getMessage(this.getClass(),
                             "MSG_CanNotRefreshFile"); //NOI18N
                     NotifyDescriptor d = new NotifyDescriptor.Message(
                             msg, NotifyDescriptor.INFORMATION_MESSAGE);
                     d.setTitle(NbBundle.getMessage(this.getClass(), 
                             "LBL_RefreshFile")); //NOI18N
                     DialogDisplayer.getDefault().notify(d);
                 }
             } catch (Exception ex){
                 log(ex);
             } 
        }
    }
    
    private static void log(Exception ex){
        // XXX TODO log 
        ex.printStackTrace();
    }
    
    public String getName() {
        return NbBundle.getMessage(this.getClass(), "LBL_NodeRefresh");//NOI18N
    }

    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    @Override 
    protected boolean enable(Node[] node) {
        return true;
    }
}
