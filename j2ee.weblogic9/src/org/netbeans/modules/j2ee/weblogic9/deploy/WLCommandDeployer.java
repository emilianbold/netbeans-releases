/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2ee.weblogic9.deploy;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.swing.SwingUtilities;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.dd.api.application.Module;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.weblogic9.URLWait;
import org.netbeans.modules.j2ee.weblogic9.WLDeploymentFactory;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.config.gen.WeblogicWebApp;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;


/**
 *
 * @author Petr Hejl
 */
public final class WLCommandDeployer {

    private static final Logger LOGGER = Logger.getLogger(WLCommandDeployer.class.getName());

    private static final String WEBLOGIC_JAR_PATH = "server/lib/weblogic.jar";

    private static final int TIMEOUT = 300000;

    private static final Pattern LIST_APPS_PATTERN = Pattern.compile("\\s+(.*)");

    private static boolean showConsole = Boolean.getBoolean(WLCommandDeployer.class.getName() + ".showConsole");

    private final WLDeploymentFactory factory;

    private final InstanceProperties ip;

    public WLCommandDeployer(WLDeploymentFactory factory, InstanceProperties ip) {
        this.factory = factory;
        this.ip = ip;
    }

    public ProgressObject deploy(Target[] target, final File file, final File plan, String host, String port) {
        final TargetModuleID moduleId = createModuleId(new Target() {

            @Override
            public String getName() {
                return "default";
            }

            @Override
            public String getDescription() {
                return "server";
            }
        }, file, host, port);

        final WLProgressObject progress = new WLProgressObject(moduleId);

        progress.fireProgressEvent(null, new WLDeploymentStatus(
                ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING,
                NbBundle.getMessage(WLCommandDeployer.class, "MSG_Deploying", file.getAbsolutePath())));

        factory.getExecutorService().submit(new Runnable() {

            @Override
            public void run() {
                ExecutionService service = createService("-deploy", null, file.getAbsolutePath()); // NOI18N
                Future<Integer> result = service.run();
                try {
                    Integer value = result.get(TIMEOUT, TimeUnit.MILLISECONDS);
                    if (value.intValue() != 0) {
                        progress.fireProgressEvent(null, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                                NbBundle.getMessage(WLCommandDeployer.class, "MSG_Deployment_Failed")));
                    } else {
                        //waitForUrlReady(factory, moduleId, progress);
                        progress.fireProgressEvent(null, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.COMPLETED,
                                NbBundle.getMessage(WLCommandDeployer.class, "MSG_Deployment_Completed")));
                    }
                } catch (InterruptedException ex) {
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                            NbBundle.getMessage(WLCommandDeployer.class, "MSG_Deployment_Failed_Interrupted")));
                    result.cancel(true);
                    Thread.currentThread().interrupt();
                } catch (TimeoutException ex) {
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                            NbBundle.getMessage(WLCommandDeployer.class, "MSG_Deployment_Failed_Timeout")));
                    result.cancel(true);
                } catch (ExecutionException ex) {
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.FAILED,
                            NbBundle.getMessage(WLCommandDeployer.class, "MSG_Deployment_Failed_With_Message")));
                }
            }
        });

        return progress;
    }

    public ProgressObject undeploy(final TargetModuleID[] targetModuleID) {
        final WLProgressObject progress = new WLProgressObject(targetModuleID);

        progress.fireProgressEvent(null, new WLDeploymentStatus(
                ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.RUNNING,
                NbBundle.getMessage(WLCommandDeployer.class, "MSG_Undeployment_Started")));

        factory.getExecutorService().submit(new Runnable() {

            @Override
            public void run() {
                boolean failed = false;
                for (TargetModuleID module : targetModuleID) {
                    String name = module.getModuleID();
                    ExecutionService service = createService("-undeploy", null, "-name", name);
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.RUNNING,
                            NbBundle.getMessage(WLCommandDeployer.class, "MSG_Undeploying", name)));

                    Future<Integer> result = service.run();
                    try {
                        Integer value = result.get(TIMEOUT, TimeUnit.MILLISECONDS);
                        if (value.intValue() != 0) {
                            failed = true;
                            progress.fireProgressEvent(null, new WLDeploymentStatus(
                                    ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.FAILED,
                                    NbBundle.getMessage(WLCommandDeployer.class, "MSG_Undeployment_Failed")));
                            break;
                        } else {
                            continue;
                        }
                    } catch (InterruptedException ex) {
                        failed = true;
                        progress.fireProgressEvent(null, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.FAILED,
                                NbBundle.getMessage(WLCommandDeployer.class, "MSG_Undeployment_Failed_Interrupted")));
                        result.cancel(true);
                        Thread.currentThread().interrupt();
                        break;
                    } catch (TimeoutException ex) {
                        failed = true;
                        progress.fireProgressEvent(null, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.FAILED,
                                NbBundle.getMessage(WLCommandDeployer.class, "MSG_Undeployment_Failed_Timeout")));
                        result.cancel(true);
                        break;
                    } catch (ExecutionException ex) {
                        failed = true;
                        progress.fireProgressEvent(null, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.UNDEPLOY, StateType.FAILED,
                                NbBundle.getMessage(WLCommandDeployer.class, "MSG_Undeployment_Failed_With_Message")));
                        break;
                    }
                }
                if (!failed) {
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.START, StateType.COMPLETED,
                            NbBundle.getMessage(WLCommandDeployer.class, "MSG_Undeployment_Completed")));
                }
            }
        });

        return progress;
    }

    public ProgressObject start(final TargetModuleID[] targetModuleID) {
        final WLProgressObject progress = new WLProgressObject(targetModuleID);

        progress.fireProgressEvent(null, new WLDeploymentStatus(
                ActionType.EXECUTE, CommandType.START, StateType.RUNNING,
                NbBundle.getMessage(WLCommandDeployer.class, "MSG_Start_Started")));

        factory.getExecutorService().submit(new Runnable() {

            @Override
            public void run() {
                boolean failed = false;
                for (TargetModuleID module : targetModuleID) {
                    String name = module.getModuleID();
                    ExecutionService service = createService("-start", null, "-name", name);
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.START, StateType.RUNNING,
                            NbBundle.getMessage(WLCommandDeployer.class, "MSG_Starting", name)));

                    Future<Integer> result = service.run();
                    try {
                        Integer value = result.get(TIMEOUT, TimeUnit.MILLISECONDS);
                        if (value.intValue() != 0) {
                            failed = true;
                            progress.fireProgressEvent(null, new WLDeploymentStatus(
                                    ActionType.EXECUTE, CommandType.START, StateType.FAILED,
                                    NbBundle.getMessage(WLCommandDeployer.class, "MSG_Start_Failed")));
                            break;
                        } else {
                            waitForUrlReady(factory, module, progress);
                            continue;
                        }
                    } catch (InterruptedException ex) {
                        failed = true;
                        progress.fireProgressEvent(null, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.START, StateType.FAILED,
                                NbBundle.getMessage(WLCommandDeployer.class, "MSG_Start_Failed_Interrupted")));
                        result.cancel(true);
                        Thread.currentThread().interrupt();
                        break;
                    } catch (TimeoutException ex) {
                        failed = true;
                        progress.fireProgressEvent(null, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.START, StateType.FAILED,
                                NbBundle.getMessage(WLCommandDeployer.class, "MSG_Start_Failed_Timeout")));
                        result.cancel(true);
                        break;
                    } catch (ExecutionException ex) {
                        failed = true;
                        progress.fireProgressEvent(null, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.START, StateType.FAILED,
                                NbBundle.getMessage(WLCommandDeployer.class, "MSG_Start_Failed_With_Message")));
                        break;
                    }
                }
                if (!failed) {
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.START, StateType.COMPLETED,
                            NbBundle.getMessage(WLCommandDeployer.class, "MSG_Start_Completed")));
                }
            }
        });

        return progress;
    }

    public ProgressObject stop(final TargetModuleID[] targetModuleID) {
        final WLProgressObject progress = new WLProgressObject(targetModuleID);

        progress.fireProgressEvent(null, new WLDeploymentStatus(
                ActionType.EXECUTE, CommandType.STOP, StateType.RUNNING,
                NbBundle.getMessage(WLCommandDeployer.class, "MSG_Stop_Started")));

        factory.getExecutorService().submit(new Runnable() {

            public void run() {
                boolean failed = false;
                for (TargetModuleID module : targetModuleID) {
                    String name = module.getModuleID();
                    ExecutionService service = createService("-stop", null, "-name", name);
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.STOP, StateType.RUNNING,
                            NbBundle.getMessage(WLCommandDeployer.class, "MSG_Stopping", name)));

                    Future<Integer> result = service.run();
                    try {
                        Integer value = result.get(TIMEOUT, TimeUnit.MILLISECONDS);
                        if (value.intValue() != 0) {
                            failed = true;
                            progress.fireProgressEvent(null, new WLDeploymentStatus(
                                    ActionType.EXECUTE, CommandType.STOP, StateType.FAILED,
                                    NbBundle.getMessage(WLCommandDeployer.class, "MSG_Stop_Failed")));
                            break;
                        } else {
                            continue;
                        }
                    } catch (InterruptedException ex) {
                        failed = true;
                        progress.fireProgressEvent(null, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.STOP, StateType.FAILED,
                                NbBundle.getMessage(WLCommandDeployer.class, "MSG_Stop_Failed_Interrupted")));
                        result.cancel(true);
                        Thread.currentThread().interrupt();
                        break;
                    } catch (TimeoutException ex) {
                        failed = true;
                        progress.fireProgressEvent(null, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.STOP, StateType.FAILED,
                                NbBundle.getMessage(WLCommandDeployer.class, "MSG_Stop_Failed_Timeout")));
                        result.cancel(true);
                        break;
                    } catch (ExecutionException ex) {
                        failed = true;
                        progress.fireProgressEvent(null, new WLDeploymentStatus(
                                ActionType.EXECUTE, CommandType.STOP, StateType.FAILED,
                                NbBundle.getMessage(WLCommandDeployer.class, "MSG_Stop_Failed_With_Message")));
                        break;
                    }
                }
                if (!failed) {
                    progress.fireProgressEvent(null, new WLDeploymentStatus(
                            ActionType.EXECUTE, CommandType.STOP, StateType.COMPLETED,
                            NbBundle.getMessage(WLCommandDeployer.class, "MSG_Stop_Completed")));
                }
            }
        });

        return progress;
    }

    public TargetModuleID[] getAvailableModules(ModuleType moduleType, Target[] target) {

        assert !SwingUtilities.isEventDispatchThread() : "Should not be executed in EDT";

        ListAppLineProcessor lineProcessor = new ListAppLineProcessor();
        ExecutionService service = createService("-listapps", lineProcessor);
        Future<Integer> result = service.run();
        try {
            Integer value = result.get(TIMEOUT, TimeUnit.MILLISECONDS);
            if (value.intValue() != 0) {
                return null;
            } else {
                List<String> names = lineProcessor.getApps();
                TargetModuleID[] ret = new TargetModuleID[names.size()];
                for (int i = 0; i < names.size(); i++) {
                    ret[i] = new WLTargetModuleID(target[0], names.get(i));
                }
                return ret;
            }
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return null;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return null;
        } catch (TimeoutException ex) {
            LOGGER.log(Level.INFO, null, ex);
            return null;
        }
    }

    private ExecutionService createService(final String command,
            final LineProcessor processor, String... parameters) {

        String username = ip.getProperty(InstanceProperties.USERNAME_ATTR);
        String password = ip.getProperty(InstanceProperties.PASSWORD_ATTR);

        String uri = ip.getProperty(InstanceProperties.URL_ATTR);
        // it is guaranteed it is WL
        String[] parts = uri.substring(WLDeploymentFactory.URI_PREFIX.length()).split(":");

        String host = parts[0];
        String port = parts.length > 1 ? parts[1] : "";

        ExternalProcessBuilder builder = new ExternalProcessBuilder(getJavaBinary())
                .addArgument("-cp") // NOI18N
                .addArgument(getClassPath())
                .addArgument("weblogic.Deployer") // NOI18N
                .addArgument("-adminurl") // NOI18N
                .addArgument("t3://" + host + ":" + port) // NOI18N
                .addArgument("-username") // NOI18N
                .addArgument(username)
                .addArgument("-password") // NOI18N
                .addArgument(password)
                .addArgument(command);

        for (String param : parameters) {
            builder = builder.addArgument(param);
        }

        ExecutionDescriptor descriptor = new ExecutionDescriptor().inputVisible(true).outLineBased(true);
        if (processor != null) {
            descriptor = descriptor.outProcessorFactory(new ExecutionDescriptor.InputProcessorFactory() {

                public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                    return InputProcessors.proxy(defaultProcessor, InputProcessors.bridge(processor));
                }
            });
        }
        if (!showConsole) {
            descriptor = descriptor.inputOutput(InputOutput.NULL);
        }
        return ExecutionService.newService(builder, descriptor, "weblogic.Deployer " + command);
    }

    private String getClassPath() {
        String serverRoot = ip.getProperty(WLPluginProperties.SERVER_ROOT_ATTR);
        if (serverRoot != null) {
            File file = new File(serverRoot, WEBLOGIC_JAR_PATH);
            if (file.exists() && file.isFile()) {
                return file.getAbsolutePath();
            }
        }
        return "";
    }

    private String getJavaBinary() {
        // TODO configurable ? or use the jdk server is running on ?
        JavaPlatform platform = JavaPlatformManager.getDefault().getDefaultPlatform();
        Collection<FileObject> folders = platform.getInstallFolders();
        String javaBinary = "java"; // NOI18N
        if (folders.size() > 0) {
            FileObject folder = folders.iterator().next();
            File file = FileUtil.toFile(folder);
            if (file != null) {
                javaBinary = file.getAbsolutePath() + File.separator
                        + "bin" + File.separator + "java"; // NOI18N
            }
        }
        return javaBinary;
    }

    private static void waitForUrlReady(WLDeploymentFactory factory,
            TargetModuleID moduleID, WLProgressObject progressObject) throws InterruptedException, TimeoutException {

        String webUrl = moduleID.getWebURL();
        if (webUrl == null) {
            TargetModuleID[] ch = moduleID.getChildTargetModuleID();
            if (ch != null) {
                for (int i = 0; i < ch.length; i++) {
                    webUrl = ch[i].getWebURL();
                    if (webUrl != null) {
                        break;
                    }
                }
            }

        }
        waitForUrlReady(factory, webUrl, progressObject);
    }

    private static void waitForUrlReady(WLDeploymentFactory factory,
            String webUrl, WLProgressObject progressObject) throws InterruptedException, TimeoutException {

        if (webUrl != null) {
            try {
                URL url = new URL(webUrl);
                String waitingMsg = NbBundle.getMessage(WLCommandDeployer.class, "MSG_Waiting_For_Url", url);

                progressObject.fireProgressEvent(null,
                        new WLDeploymentStatus(ActionType.EXECUTE, CommandType.DISTRIBUTE, StateType.RUNNING, waitingMsg));
                //delay to prevent hitting the old content before reload
                for (int i = 0; i < 3; i++) {
                    Thread.sleep(1000);
                }
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() - start < TIMEOUT) {
                    if (URLWait.waitForUrlReady(factory.getExecutorService(), url, 1000)) {
                        break;
                    }
                }
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.INFO, "Malformed URL {0}", webUrl);
            }
        }
    }

    private static WLTargetModuleID createModuleId(Target target, File file,
            String host, String port) {

        WLTargetModuleID moduleId = new WLTargetModuleID(target, file.getName());

        try {
            String serverUrl = "http://" + host + ":" + port;

            // TODO in fact we should look to deployment plan for overrides
            // for now it is as good as previous solution
            if (file.getName().endsWith(".war")) { // NOI18N
                configureWarModuleId(moduleId, file, serverUrl);
            } else if (file.getName().endsWith(".ear")) { // NOI18N
                configureEarModuleId(moduleId, file, serverUrl);
            }

        } catch (Exception e) {
            LOGGER.log(Level.INFO, null, e);
        }

        return moduleId;
    }

    private static void configureWarModuleId(WLTargetModuleID moduleId, File file, String serverUrl) {
        try {
            JarFileSystem jfs = new JarFileSystem();
            jfs.setJarFile(file);
            FileObject webXml = jfs.getRoot().getFileObject("WEB-INF/weblogic.xml"); // NOI18N
            if (webXml != null) {
                InputStream is = webXml.getInputStream();
                try {
                    String[] ctx = WeblogicWebApp.createGraph(is).getContextRoot();
                    if (ctx != null && ctx.length > 0) {
                        moduleId.setContextURL(serverUrl + ctx[0]);
                    }
                } finally {
                    is.close();
                }
            } else {
                // FIXME there is some default
                System.out.println("Cannot find file WEB-INF/weblogic.xml in " + file);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
        } catch (PropertyVetoException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }

    private static void configureEarModuleId(WLTargetModuleID moduleId, File file, String serverUrl) {
        try {
            JarFileSystem jfs = new JarFileSystem();
            jfs.setJarFile(file);
            FileObject appXml = jfs.getRoot().getFileObject("META-INF/application.xml"); // NOI18N
            if (appXml != null) {
                Application ear = DDProvider.getDefault().getDDRoot(appXml);
                Module[] modules = ear.getModule();
                for (int i = 0; i < modules.length; i++) {
                    WLTargetModuleID childModuleId = new WLTargetModuleID(moduleId.getTarget());
                    if (modules[i].getWeb() != null) {
                        childModuleId.setContextURL(serverUrl + modules[i].getWeb().getContextRoot());
                    }
                    moduleId.addChild(childModuleId);
                }
            } else {
                // Java EE 5
                for (FileObject child : jfs.getRoot().getChildren()) {
                    if (child.hasExt("war") || child.hasExt("jar")) { // NOI18N
                        WLTargetModuleID childModuleId = new WLTargetModuleID(moduleId.getTarget());

                        if (child.hasExt("war")) { // NOI18N
                            configureWarInEarModuleId(childModuleId, child, serverUrl);
                        }
                        moduleId.addChild(childModuleId);
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
        } catch (PropertyVetoException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }

    private static void configureWarInEarModuleId(WLTargetModuleID moduleId,
            FileObject warFileObject, String serverUrl) {

        try {
            String contextRoot = "/" + warFileObject.getName();
            ZipInputStream zis = new ZipInputStream(warFileObject.getInputStream());
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

            moduleId.setContextURL(serverUrl + contextRoot);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }

    private static class ListAppLineProcessor implements LineProcessor {

        /* GuardedBy("this") */
        private List<String> apps = new ArrayList<String>();

        public synchronized List<String> getApps() {
            return apps;
        }

        public void processLine(String line) {
            Matcher matcher = LIST_APPS_PATTERN.matcher(line);
            if (matcher.matches()) {
                synchronized (this) {
                    apps.add(matcher.group(1));
                }
            }
        }

        public void reset() {
        }

        public void close() {
        }
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
}
