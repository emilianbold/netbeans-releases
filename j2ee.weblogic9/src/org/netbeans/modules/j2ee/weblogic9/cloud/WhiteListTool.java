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
package org.netbeans.modules.j2ee.weblogic9.cloud;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.j2ee.weblogic9.WLDeploymentFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Petr Hejl
 */
public class WhiteListTool {

    private static final String WHITELIST_TOOL_DIR = "JavaEE/WebLogic"; // NOI18N
    
    private static final Logger LOGGER = Logger.getLogger(WhiteListTool.class.getName());

    private static final ExecutionDescriptor TOOL_DESCRIPTOR =
            new ExecutionDescriptor().controllable(false).frontWindow(true);
    
    public static boolean execute(File file, File weblogicJar) {
        return execute(file, NbBundle.getMessage(WhiteListTool.class, "MSG_WhiteListOutput"), weblogicJar);
    }
    
    public static boolean execute(File file, String displayName, File weblogicJar) {
        assert weblogicJar != null;
        // the tool is not very friendly in order to be used in-jvm so far
        // it si configuring loggers etc.
        File jarFo = InstalledFileLocator.getDefault().locate(
                "modules/ext/whitelistscantool.jar", "org.netbeans.modules.libs.cloud9", false); // NOI18N
        if (jarFo == null) {
            LOGGER.log(Level.WARNING, "Could not invoke whitelist tool");
            return true;
        }

        File propertiesFile = null;
        try {
            FileObject configRoot = FileUtil.getConfigRoot();
            FileObject loggingDir = FileUtil.createFolder(
                    configRoot, WHITELIST_TOOL_DIR);
            File logging = FileUtil.toFile(loggingDir);
            if (logging != null) {
                propertiesFile = new File(logging, "whitelist.logging.properties"); // NOI18N
                prepareLoggingPropeties(propertiesFile);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            propertiesFile = null;
        }

        ExternalProcessBuilder builder = new ExternalProcessBuilder(getJavaBinary());
        if (propertiesFile != null) {
            builder = builder.addArgument("-Djava.util.logging.config.file=" + propertiesFile.getAbsolutePath()); // NOI18N
        }
        builder = builder.addArgument("-cp").addArgument(weblogicJar.getAbsolutePath()) // NOI18N
                .addArgument("-jar").addArgument(jarFo.getAbsolutePath()) // NOI18N
                .addArgument(file.getAbsolutePath())
                .redirectErrorStream(true)
                .workingDirectory(file.getParentFile());

        ExecutionService service = ExecutionService.newService(builder,
                TOOL_DESCRIPTOR, displayName);
        try {
            return service.run().get().equals(Integer.valueOf(0));
        } catch (InterruptedException ex) {
            LOGGER.log(Level.INFO, null, ex);
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        // TODO handle exceptions properly
        return false;
    }
    
    // FIXME copied from CommandBasedDeployer
    private static String getJavaBinary() {
        // TODO configurable ? or use the jdk server is running on ?
        JavaPlatform platform = JavaPlatformManager.getDefault().getDefaultPlatform();
        Collection<FileObject> folders = platform.getInstallFolders();
        String javaBinary = Utilities.isWindows() ? "java.exe" : "java"; // NOI18N
        if (folders.size() > 0) {
            FileObject folder = folders.iterator().next();
            File file = FileUtil.toFile(folder);
            if (file != null) {
                javaBinary = file.getAbsolutePath() + File.separator
                        + "bin" + File.separator
                        + (Utilities.isWindows() ? "java.exe" : "java"); // NOI18N
            }
        }
        return javaBinary;
    }
    
    private static void prepareLoggingPropeties(File dest) throws IOException {
        if (dest.exists()) {
            return;
        }
        
        OutputStream os = new BufferedOutputStream(new FileOutputStream(dest));
        try {
            InputStream is = WLDeploymentFactory.class.getResourceAsStream("resources/whitelist.logging.properties"); // NOI18N
            try {
                FileUtil.copy(is, os);
            } finally {
                is.close();
            }
        } finally {
            os.close();
        }
    }
}
