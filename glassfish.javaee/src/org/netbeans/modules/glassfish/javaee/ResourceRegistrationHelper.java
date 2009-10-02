/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.glassfish.javaee;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.GlassfishModule.OperationState;
import org.netbeans.modules.glassfish.spi.ServerCommand;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.SourceFileMap;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author vkraemer
 */
public class ResourceRegistrationHelper {
    private static final TimeUnit TIMEOUT_UNIT = TimeUnit.MILLISECONDS;
    private static final int TIMEOUT = 2000;

    private ResourceRegistrationHelper() {
    }

    public static void deployResources(File root, Hk2DeploymentManager dm) {
        Set<File> resourceDirs = getResourceDirs(root);
         deployResources(resourceDirs,dm);

    }

    private static void deployResources(Set<File> resourceDirs, Hk2DeploymentManager dm)  {
        for(File resourceDir: resourceDirs) {
            try {
                registerResourceDir(resourceDir,dm);
            } catch (ConfigurationException ex) {
                Logger.getLogger("glassfish-javaee").log(Level.INFO, "some data sources may not be deployed", ex);
            }
        }
    }

    private static Set<File> getResourceDirs(File file){
        Set<File> retVal = new TreeSet<File>();
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        SourceFileMap sourceFileMap = SourceFileMap.findSourceMap(fo);
        if (sourceFileMap != null) {
            retVal.addAll(Arrays.asList(sourceFileMap.getEnterpriseResourceDirs()));
        }
        return retVal;
    }

    private static boolean registerResourceDir(File resourceDir, Hk2DeploymentManager dm) throws ConfigurationException {
        boolean succeeded = false;
        File sunResourcesXml = new File(resourceDir, "sun-resources.xml");
        if(sunResourcesXml.exists()) {
            GlassfishModule commonSupport = dm.getCommonServerSupport();
            AddResourcesCommand cmd = new AddResourcesCommand(sunResourcesXml.getAbsolutePath());
            Future<OperationState> result = commonSupport.execute(cmd);
            try {
                if(result.get(TIMEOUT, TIMEOUT_UNIT) == OperationState.COMPLETED) {
                    succeeded = true;
                }
            } catch (TimeoutException ex) {
                Logger.getLogger("glassfish-javaee").log(Level.INFO, ex.getLocalizedMessage(), ex);
                throw new ConfigurationException(ex.getLocalizedMessage(), ex);
            } catch (InterruptedException ex) {
                Logger.getLogger("glassfish-javaee").log(Level.INFO, ex.getLocalizedMessage(), ex);
                throw new ConfigurationException(ex.getLocalizedMessage(), ex);
            } catch (ExecutionException ex) {
                Logger.getLogger("glassfish-javaee").log(Level.INFO, ex.getLocalizedMessage(), ex);
                throw new ConfigurationException(ex.getLocalizedMessage(), ex);
            }
        }
        return succeeded;
    }

    public static final class AddResourcesCommand extends ServerCommand {

        public AddResourcesCommand(String sunResourcesXmlPath) {
            super("add-resources"); // NOI18N
            query = "xml_file_name=" + sunResourcesXmlPath; // NOI18N
        }

    }
}
