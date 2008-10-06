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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.websvc.jaxrpc.actions;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        return (activatedNodes != null && activatedNodes.length == 1);
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
        String wsdlUrl = (String)activatedNodes[0].getValue("wsdl-url");
        if (wsdlUrl != null) {
            try {
                // get WSDL File or create one if missing
                fo = getWsdlFile(new URL(wsdlUrl));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        DataObject dobj = null;
        if (fo != null) {
            support = WebServicesClientSupport.getWebServicesClientSupport(fo);
            try {
                dobj = DataObject.find(fo);
            } catch (IOException ex) {}
        }
        if (dobj == null) {
            // Find WebServicesClientSupport from activated node.
            dobj = (DataObject) activatedNodes[0].getLookup().lookup(DataObject.class);
            if(dobj != null) {
                fo = dobj.getPrimaryFile();
                support = WebServicesClientSupport.getWebServicesClientSupport(fo);
            }
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

    @Override
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
    
    private FileObject getWsdlFile(URL url) throws IOException {
        FileObject fo = null;
        try {
            File f = new File(url.toURI());
            if (f.exists()) {
                fo = FileUtil.toFileObject(f);
            } else {
                Logger.getLogger(RefreshServiceAction.class.getName()).log(Level.FINE, "Missing WSDL File");
                File parent = f.getParentFile();
                if (parent != null && parent.exists()) {
                    FileObject parentFO = FileUtil.toFileObject(parent);
                    fo = parentFO.createData(f.getName());
                }
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(RefreshServiceAction.class.getName()).log(Level.FINE, "URI Syntax Error",ex);
        }
        return fo;
    }
}
