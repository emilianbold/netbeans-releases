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

package org.netbeans.modules.websvc.jaxrpc.actions;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.modules.websvc.api.registry.WebServicesRegistryView;

import org.openide.NotifyDescriptor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.jaxrpc.client.ui.RefreshWsdlPanel;
import org.netbeans.modules.websvc.jaxrpc.client.ui.DownloadWsdlPanel;
import org.netbeans.modules.websvc.wsdl.config.ServiceInformationImpl;

/**
 *
 * @author Peter Williams
 */
public class RefreshServiceAction extends NodeAction {

    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    public HelpCtx getHelpCtx() {
        // !PW FIXME use correct help context when known.
        return HelpCtx.DEFAULT_HELP;
    }

    public String getName() {
        return NbBundle.getMessage(RefreshServiceAction.class, "LBL_RefreshWsdl"); // NOI18N
    }

    protected void performAction(Node[] activatedNodes) {

        assert (activatedNodes != null && activatedNodes.length == 1);

        // Invoked on ClientRootNode to refresh the list of webservice clients
        // in this project.
        WebServicesClientSupport support = null;
        FileObject fo = null;

        // Find WebServicesClientSupport from activated node.
        DataObject dobj = (DataObject) activatedNodes[0].getLookup().lookup(DataObject.class);
        if(dobj != null) {
            fo = dobj.getPrimaryFile();
            support = WebServicesClientSupport.getWebServicesClientSupport(fo);
        }
        
        final WebServicesClientSupport clientSupport = support;
        final FileObject wsdlFO = fo;
        
        if(clientSupport == null) {
            String mes = NbBundle.getMessage(RefreshServiceAction.class, "ERR_NoClientSupport", activatedNodes[0]);  // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            return;
        }

        ServiceInformationImpl wsdlModel = new ServiceInformationImpl(dobj);
        String [] supportedServices = wsdlModel.getServiceNames();

        // When looking up service information in the project, use the name of the wsdl file, not the
        // name of the service.
        final String wsdlName = dobj.getName();
        final String wsdlSource = clientSupport.getWsdlSource(wsdlName);
        final RefreshWsdlPanel refreshPanel = new RefreshWsdlPanel(wsdlSource, supportedServices);
        final DialogDescriptor descriptor = new DialogDescriptor(refreshPanel, 
            NbBundle.getMessage(RefreshServiceAction.class, "LBL_RefreshWsdlForService")); // NOI18N
        refreshPanel.setDescriptor(descriptor);
        // !PW FIXME put help context here when known to get a displayed help button on the panel.
//        descriptor.setHelpCtx(new HelpCtx("HelpCtx_RefreshClientWsdlHelp"));
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                DialogDisplayer.getDefault().notify(descriptor);
                if (NotifyDescriptor.OK_OPTION.equals(descriptor.getValue())) {
                    String newWsdlSource = refreshPanel.getWsdlSource();
                    if(newWsdlSource.indexOf("://") != -1) {
                        refreshViaUrl(wsdlFO, newWsdlSource);
                    } else {
                        refreshViaFile(wsdlFO, newWsdlSource);
                    }
                    if(!newWsdlSource.equals(wsdlSource)) {
                        clientSupport.setWsdlSource(wsdlName, newWsdlSource);
                    }
                    WebServicesRegistryView registryView = (WebServicesRegistryView) Lookup.getDefault().lookup(WebServicesRegistryView.class);
                    registryView.registerService(wsdlFO, true);
                }
            }
            
        });
    }

    protected boolean asynchronous() {
        return false;
    }

    private void refreshViaUrl(FileObject wsdlFO, String url) {
        DownloadWsdlPanel downloadPanel = new DownloadWsdlPanel(url);
        DialogDescriptor descriptor = new DialogDescriptor(downloadPanel, 
            NbBundle.getMessage(RefreshServiceAction.class, "LBL_DownloadWsdl")); // NOI18N
        downloadPanel.setDescriptor(descriptor);
        DialogDisplayer.getDefault().notify(descriptor);
        if (NotifyDescriptor.OK_OPTION.equals(descriptor.getValue())) {
            // grab wsdl string and write to target file.
            byte [] wsdlBuf = downloadPanel.getWsdl();
            FileObject tmpFO = writeTempWsdl(wsdlFO.getName(), wsdlBuf);
            if(tmpFO != null) {
                updateWsdl(wsdlFO, tmpFO);
                try {
                    tmpFO.delete();
                } catch(IOException ex) {
                    String message = NbBundle.getMessage(RefreshServiceAction.class, 
                        "ERR_CannotDeleteTempWsdlFile", ex.getLocalizedMessage()); // NOI18N
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, message);
                }
            }
        }
    }

    private FileObject writeTempWsdl(String prefix, byte [] content) {
        FileObject tmpFO = null;

        try {
            if(prefix==null||prefix.length()<3) {
                prefix = "tmp";// need prefix atleast 3 characters long, fix for bug 113430
            }
            tmpFO = FileUtil.toFileObject(FileUtil.normalizeFile(File.createTempFile(prefix, "wsdl"))); // NOI18N
            FileLock lock = tmpFO.lock();
            OutputStream out = null;

            try {
                // write content to the file.
                out = tmpFO.getOutputStream(lock);
                try {
                    out.write(content);
                    out.flush();
                } finally {
                    if(out != null) {
                        out.close();
                    }
                }
            } finally {
                lock.releaseLock();
            }
        } catch(IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
        }

        return tmpFO;
    }

    private void refreshViaFile(FileObject wsdlFO, String path) {
        File normalizedFile = FileUtil.normalizeFile(new File(path));
        FileObject newWsdlFO = FileUtil.toFileObject(normalizedFile);
        if(newWsdlFO != null) {
            updateWsdl(wsdlFO, newWsdlFO);
        } else {
            String message = NbBundle.getMessage(RefreshServiceAction.class, 
                "ERR_CannotGetFileObject", normalizedFile.getPath()); // NOI18N
            ErrorManager.getDefault().log(ErrorManager.ERROR, message);
        }
    }

    private void updateWsdl(FileObject wsdlFO, FileObject newWsdlFO) {
        try {
            // Cache what we need to do the copy.
            FileObject targetDir = wsdlFO.getParent();
            String name = wsdlFO.getName();
            String ext = wsdlFO.getExt();

            // delete existing file.
            wsdlFO.delete();

            // copy new file into place.
            newWsdlFO.copy(targetDir, name, ext);
        } catch(IOException ex) {
            // !PW Should we beautify this sort of error?
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
        }
    }
}
