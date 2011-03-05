/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.makeproject.actions;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.cnd.makeproject.MakeConfigurationSaveListener;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.xml.sax.SAXException;

/**
 * @author Vladimnir Kvbashin
 */
@ServiceProvider(service=MakeConfigurationSaveListener.class)
public class MakeConfigurationSaveListenerImpl implements MakeConfigurationSaveListener {

    private final LinkedList<MakeConfigurationDescriptor> savingProjects =
            new LinkedList<MakeConfigurationDescriptor>();
    private final Object lock = new Object();
    private static final Logger LOGGER = Logger.getLogger("cnd.remote.logger"); //NOI18N
    
    @Override
    public void configurationSaving(MakeConfigurationDescriptor mkd) {        
        Project project = mkd.getProject();
        if (project != null) {
            RemoteProject remoteProject = project.getLookup().lookup(RemoteProject.class);
            if (remoteProject != null) {
                if (remoteProject.getRemoteMode() == RemoteProject.Mode.REMOTE_SOURCES) {
                    synchronized (lock) {
                        savingProjects.add(mkd);
                    }   
                }
            }
        }
    }
    
    @Override
    public void configurationSaved(MakeConfigurationDescriptor mkd, boolean success) {
        synchronized (lock) {
            if (savingProjects.contains(mkd)) {
                savingProjects.remove(mkd);
            } else {
                mkd = null;
            }
        }
        if (mkd != null) {
            //System.err.printf("\nPROJECT SYNC:%s\n\n", mkd.getProjectDirFileObject().getPath());
            LOGGER.log(Level.FINE, "Synchronizing remote project {0}", mkd.getProjectDirFileObject().getPath());
            updateRemoteProject(mkd);
        }
    }
    
    private void updateRemoteProject(MakeConfigurationDescriptor mkd) {        
        // some paranoidal checks
        Project project = mkd.getProject();
        if (project != null) {
            RemoteProject remoteProject = project.getLookup().lookup(RemoteProject.class);
            if (remoteProject != null) {
                if (remoteProject.getRemoteMode() == RemoteProject.Mode.REMOTE_SOURCES) {
                    ExecutionEnvironment env = remoteProject.getSourceFileSystemHost();
                    if (!ConnectionManager.getInstance().isConnectedTo(env)) {
                        try {
                            ConnectionManager.getInstance().connectTo(env);
                        } catch (IOException ex) {
                           LOGGER.log(Level.SEVERE, NbBundle.getMessage(MakeConfigurationSaveListenerImpl.class, "ERR_Connection_Error"), ex);
                           return;
                        } catch (CancellationException ex) {
                            LOGGER.severe(NbBundle.getMessage(MakeConfigurationSaveListenerImpl.class, "ERR_Connection_Cancelled"));
                            return;
                        }
                    }                            
                    String remotePath = remoteProject.resolveRelativeRemotePath("."); //NOI18N
                    FileObject remoteProjectFO = FileSystemProvider.getFileObject(env, remotePath);
                    if (remoteProjectFO == null || !remoteProjectFO.isValid()) {
                        LOGGER.severe(NbBundle.getMessage(MakeConfigurationSaveListenerImpl.class, "ERR_Remote_Project_Deleted"));
                        return;                                
                    }
                    FileObject localProjectFO = mkd.getProjectDirFileObject();
                    try {
                        // TODO:FullRemote it's better not to hide IOException, but show it to user
                        ShadowProjectSynchronizer sync = new ShadowProjectSynchronizer(remotePath, localProjectFO.getPath(), env);
                        sync.updateRemoteProject();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (SAXException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }            
        }
    }    
}
