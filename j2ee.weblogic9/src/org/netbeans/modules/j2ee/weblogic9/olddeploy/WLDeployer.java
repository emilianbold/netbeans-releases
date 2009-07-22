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
package org.netbeans.modules.j2ee.weblogic9.olddeploy;

import org.netbeans.modules.j2ee.weblogic9.*;
import java.net.URL;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.dd.api.application.Module;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import org.netbeans.modules.j2ee.weblogic9.config.gen.WeblogicWebApp;
import org.openide.filesystems.JarFileSystem;
import org.openide.util.RequestProcessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;

import org.openide.util.NbBundle;

/**
 *
 * @author Ivan Sidorkin
 */
public class WLDeployer implements ProgressObject, Runnable {

    private static final Logger LOGGER = Logger.getLogger(WLDeployer.class.getName());

    private static final String AUTO_DEPLOY_DIR = "/autodeploy"; //NOI18N
    /** timeout for waiting for URL connection */
    private static final int TIMEOUT = 60000;

    private final String uri;

    private File file;

    private TargetModuleID module_id;

    /** Creates a new instance of JBDeployer */
    public WLDeployer(String serverUri) {
        uri = serverUri;
    }


    public ProgressObject deploy(Target[] target, File file, File file2, String host, String port) {
        //PENDING: distribute to all targets!
        WLTargetModuleID module_id = new WLTargetModuleID(target[0], file.getName() );

        try {
            String server_url = "http://" + host + ":" + port;

            // TODO in fact we should look to deployment plan for overrides
            // for now it is as good as previous solution
            if (file.getName().endsWith(".war")) {
                JarFileSystem jfs = new JarFileSystem();
                jfs.setJarFile(file);
                FileObject webXml = jfs.getRoot().getFileObject("WEB-INF/weblogic.xml"); // NOI18N
                if (webXml != null) {
                    InputStream is = webXml.getInputStream();
                    try {
                        String[] ctx = WeblogicWebApp.createGraph(is).getContextRoot();
                        if (ctx != null && ctx.length > 0) {
                            module_id.setContextURL(server_url + ctx[0]);
                        }
                    } finally {
                        is.close();
                    }
                } else {
                    System.out.println("Cannot file WEB-INF/weblogic.xml in " + file);
                }
            } else if (file.getName().endsWith(".ear")) {
                JarFileSystem jfs = new JarFileSystem();
                jfs.setJarFile(file);
                FileObject appXml = jfs.getRoot().getFileObject("META-INF/application.xml"); // NOI18N
                if (appXml != null) {
                    Application ear = DDProvider.getDefault().getDDRoot(appXml);
                    Module[] modules = ear.getModule();
                    for (int i = 0; i < modules.length; i++) {
                        WLTargetModuleID mod_id = new WLTargetModuleID(target[0]);
                        if (modules[i].getWeb() != null) {
                            mod_id.setContextURL(server_url + modules[i].getWeb().getContextRoot());
                        }
                        module_id.addChild(mod_id);
                    }
                } else {
                    // Java EE 5
                    for (FileObject child : jfs.getRoot().getChildren()) {
                        if (child.hasExt("war") || child.hasExt("jar")) { // NOI18N
                            WLTargetModuleID mod_id = new WLTargetModuleID(target[0]);

                            if (child.hasExt("war")) { // NOI18N
                                String contextRoot = "/" + child.getName();
                                ZipInputStream zis = new ZipInputStream(child.getInputStream());
                                try {

                                    ZipEntry entry = null;
                                    while ((entry = zis.getNextEntry()) != null) {
                                        if ("WEB-INF/weblogic.xml".equals(entry.getName())) { // NOI18N
                                            String[] ddContextRoots =
                                                    WeblogicWebApp.createGraph(new ZipEntryInputStream(zis)).getContextRoot();
                                            if (ddContextRoots != null && ddContextRoots.length > 0) {
                                                contextRoot = ddContextRoots[0];
                                            }
                                            break;
                                        }
                                    }
                                } catch (IOException ex) {
                                    LOGGER.log(Level.INFO, "Error reading context-root", ex); // NOI18N
                                } finally {
                                    zis.close();
                                }

                                mod_id.setContextURL(server_url + contextRoot);
                            }
                            module_id.addChild(mod_id);
                        }
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.log(Level.INFO, null, e);
        }

        this.file = file;
        this.module_id = module_id;
        fireHandleProgressEvent(null, new WLDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, NbBundle.getMessage(WLDeployer.class, "MSG_DEPLOYING", file.getAbsolutePath())));
        RequestProcessor.getDefault().post(this, 0, Thread.NORM_PRIORITY);
        return this;
    }


    public void run() {

        String deployDir = InstanceProperties.getInstanceProperties(uri).getProperty(WLPluginProperties.DOMAIN_ROOT_ATTR) + AUTO_DEPLOY_DIR;
        FileObject foIn = FileUtil.toFileObject(file);
        FileObject foDestDir = FileUtil.toFileObject(new File(deployDir));
        String fileName = file.getName();

        File toDeploy = new File(deployDir+File.separator+fileName);
        if(toDeploy.exists())
            toDeploy.delete();

        fileName = fileName.substring(0,fileName.lastIndexOf('.'));
        String msg = NbBundle.getMessage(WLDeployer.class, "MSG_DEPLOYING", file.getAbsolutePath());
        fireHandleProgressEvent(null, new WLDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, msg));

        try{
            org.openide.filesystems.FileUtil.copyFile(foIn, foDestDir, fileName); // copy version
            System.out.println("Copying 1 file to: " + foDestDir.getPath());
            String webUrl = module_id.getWebURL();
            if (webUrl == null) {
                TargetModuleID ch [] = module_id.getChildTargetModuleID();
                if (ch != null) {
                    for (int i = 0; i < ch.length; i++) {
                        webUrl = ch [i].getWebURL();
                        if (webUrl != null) {
                            break;
                        }
                    }
                }

            }
            if (webUrl != null) {
                URL url = new URL(webUrl);
                String waitingMsg = NbBundle.getMessage(WLDeployer.class, "MSG_Waiting_For_Url", url);

                fireHandleProgressEvent(null, new WLDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, waitingMsg));
                //delay to prevent hitting the old content before reload
                for (int i = 0; i < 3; i++) {
                    Thread.sleep(1000);
                }
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() - start < TIMEOUT) {
                    if (URLWait.waitForUrlReady(url, 1000)) {
                        break;
                    }
                }
            }
        } catch(Exception e) {
            fireHandleProgressEvent(null, new WLDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED, "Failed"));
        }

        fireHandleProgressEvent(null, new WLDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.COMPLETED, "Applicaton Deployed"));
    }

    private static class ZipEntryInputStream extends InputStream {
        private final ZipInputStream zis;

        public ZipEntryInputStream(ZipInputStream zis) {
            this.zis = zis;
        }

        @Override
        public int available() throws IOException {
            return zis.available();
        }

        @Override
        public void close() throws IOException {
            zis.closeEntry();
        }

        @Override
        public int read() throws IOException {
            if (available() > 0) {
                return zis.read();
            }
            return -1;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return zis.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            return zis.skip(n);
        }
    }

    // ----------  Implementation of ProgressObject interface
    private List<ProgressListener> listeners = new CopyOnWriteArrayList<ProgressListener>();

    private DeploymentStatus deploymentStatus;

    public void addProgressListener(ProgressListener pl) {
        listeners.add(pl);
    }

    public void removeProgressListener(ProgressListener pl) {
        listeners.remove(pl);
    }

    public void stop() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("");
    }

    public boolean isStopSupported() {
        return false;
    }

    public void cancel() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("");
    }

    public boolean isCancelSupported() {
        return false;
    }

    public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
        return null;
    }

    public TargetModuleID[] getResultTargetModuleIDs() {
        return new TargetModuleID[]{ module_id };
    }

    public DeploymentStatus getDeploymentStatus() {
        return deploymentStatus;
    }

    /** Report event to any registered listeners. */
    public void fireHandleProgressEvent(TargetModuleID targetModuleID, DeploymentStatus deploymentStatus) {
        ProgressEvent evt = new ProgressEvent(this, targetModuleID, deploymentStatus);

        this.deploymentStatus = deploymentStatus;

        for (ProgressListener target : listeners) {
            target.handleProgressEvent(evt);
        }
    }


}



