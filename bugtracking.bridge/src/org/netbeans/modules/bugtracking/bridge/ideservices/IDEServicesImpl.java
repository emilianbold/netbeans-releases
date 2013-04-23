/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.bridge.ideservices;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.jumpto.type.TypeBrowser;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.autoupdate.ui.api.PluginManager;
import org.netbeans.modules.bugtracking.ide.spi.IDEServices;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.bugtracking.ide.spi.IDEServices.class)
public class IDEServicesImpl implements IDEServices {
    static final Logger LOG = Logger.getLogger(IDEServicesImpl.class.getName());
    
    @Override
    public boolean providesOpenDocument() {
        return true;
    }
    
    @Override
    @NbBundle.Messages({"LBL_OpenDocument=Open Document", 
                        "# {0} - to be opened documents path",  "MSG_CannotOpen=Couldn't open document for {0}",
                        "# {0} - to be found documents path",  "MSG_CannotFind=Couldn't find document for {0}"})
    public void openDocument(final String path, final int offset) {
        final FileObject fo = findFile(path);
        if ( fo != null ) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        DataObject od = DataObject.find(fo);
                        boolean ret = NbDocument.openDocument(od, offset, -1, Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                        if(!ret) {
                            notifyError(Bundle.LBL_OpenDocument(), Bundle.MSG_CannotOpen(path));
                        }
                    } catch (DataObjectNotFoundException e) {
                        IDEServicesImpl.LOG.log(Level.SEVERE, null, e);
                    }
                }
            });
        } else {
            notifyError(Bundle.LBL_OpenDocument(), Bundle.MSG_CannotFind(path));
        }
    }

    @Override
    public boolean providesFindFile() {
        return true;
    }
    
    @Override
    public FileObject findFile(String resourcePath) {
        return GlobalPathRegistry.getDefault().findResource(resourcePath);
    }    
    
    @Override
    public boolean providesJumpTo() {
        return true;
    }

    @Override
    public void jumpTo(String label, String resource) {
        TypeDescriptor td = TypeBrowser.browse(label, resource, null);
        if(td != null) {
            td.open();
        }
    }
    
    @Override
    public boolean providesDownloadPlugin() {
        return true;
    }

    @NbBundle.Messages({"LBL_Error=Error",
                        "# {0} - pluginName", "MSG_LookingForPlugin=Looking for {0} plugin",
                        "# {0} - pluginName", "MSG_PluginNotFound={0} plugin not found!",
                        "# {0} - pluginName", "MSG_AlreadyInstalled={0} plugin seems to be already installed!",
                        "# {0} - pluginName", "MSG_CannotBeInstalled=MSG_CannotBeInstalled={0} plugin cannot be installed"})
    @Override
    public void downloadPlugin(final String cnb, final String pluginName) {
        final ProgressHandle ph = ProgressHandleFactory.createHandle(Bundle.MSG_LookingForPlugin(pluginName));
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                ph.start();
                final UpdateElement[] updateElement = new UpdateElement[1];
                try {
                    List<UpdateUnit> units = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE);
                    
                    boolean installed = false;
                    
                    for (UpdateUnit u : units) {
                        if(u.getCodeName().equals(cnb)) {       
                            List<UpdateElement> elements = u.getAvailableUpdates();
                            if(elements.isEmpty()) {
                                installed = true;
                            } else {
                                updateElement[0] = elements.get(0);
                            }
                            break;
                        }
                    }
                    if(installed) {
                        notifyError(Bundle.LBL_Error(), Bundle.MSG_AlreadyInstalled(units));            
                        return;
                    } else if(updateElement[0] == null) {
                        notifyError(Bundle.LBL_Error(), Bundle.MSG_PluginNotFound(pluginName));            
                        return;
                     }
                } finally {
                    ph.finish();
                }
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        OperationContainer<InstallSupport> oc = OperationContainer.createForInstall();
                        if (oc.canBeAdded(updateElement[0].getUpdateUnit(), updateElement[0])) {
                            oc.add(updateElement[0]);
                            PluginManager.openInstallWizard(oc);
                        } else {
                            notifyError(Bundle.LBL_Error(), Bundle.MSG_CannotBeInstalled(pluginName));            
                        }
                    }
                });
            }
        });
    }

    private static void notifyError (final String title, final String message) {
        NotifyDescriptor nd = new NotifyDescriptor(message, title, NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE, new Object[] {NotifyDescriptor.OK_OPTION}, NotifyDescriptor.OK_OPTION);
        DialogDisplayer.getDefault().notifyLater(nd);
    }      
}
