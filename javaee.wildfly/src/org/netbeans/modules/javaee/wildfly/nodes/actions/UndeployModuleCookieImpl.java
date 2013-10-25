/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.javaee.wildfly.nodes.actions;

import java.io.File;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.javaee.wildfly.WildFlyDeploymentManager;
import org.netbeans.modules.javaee.wildfly.ide.ui.JBPluginProperties;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Michal Mocnak
 */
public class UndeployModuleCookieImpl implements UndeployModuleCookie {

    private static final int TIMEOUT = 30000;

    private static final int POLLING_INTERVAL = 2000;

    private static final RequestProcessor PROCESSOR = new RequestProcessor ("JBoss undeploy", 1); // NOI18N

    private final String fileName;

    private final Lookup lookup;

    private final ModuleType type;

    private boolean isRunning;

    public UndeployModuleCookieImpl(String fileName, Lookup lookup) {
        this(fileName, ModuleType.EJB, lookup);
    }

    public UndeployModuleCookieImpl(String fileName, ModuleType type, Lookup lookup) {
        this.lookup = lookup;
        this.fileName = fileName;
        this.type = type;
        this.isRunning = false;
    }

    public Task undeploy() {
        final WildFlyDeploymentManager dm = (WildFlyDeploymentManager) lookup.lookup(WildFlyDeploymentManager.class);
        final String nameWoExt = fileName.substring(0, fileName.lastIndexOf('.'));
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(UndeployModuleCookieImpl.class,
                "LBL_UndeployProgress", nameWoExt));

        Runnable r = new Runnable() {
            public void run() {
                isRunning = true;
                String deployDir = dm.getInstanceProperties().getProperty(JBPluginProperties.PROPERTY_DEPLOY_DIR);
                File file = new File(deployDir, fileName);

                if (file.exists() && file.canWrite()) {
                    // FIXME we can use JMX to check/udeploy deployed apps
                    // jboss.as:deployment=WarName.war
                    long start = System.currentTimeMillis();
                    file.delete();

                    File statusFile = new File(file.getAbsolutePath() + ".undeployed");
                    int time = 0;
                    do {
                        try {
                            Thread.sleep(POLLING_INTERVAL);
                            time += POLLING_INTERVAL;
                        } catch (InterruptedException ex) {
                            // Nothing to do
                        }
                    } while ((!statusFile.exists() || statusFile.lastModified() < start) && time < TIMEOUT);

                    boolean wait = true;
                    while (wait && time < TIMEOUT) {
                        try {
                            wait = false;
                            TargetModuleID[] ids = dm.getAvailableModules(type, dm.getTargets());
                            if (ids != null) {
                                for (TargetModuleID id : ids) {
                                    if (fileName.equals(id.getModuleID())) {
                                        wait = true;
                                        break;
                                    }
                                }
                            }
                        } catch (TargetException ex) {
                        } catch (IllegalStateException ex) {
                            // Nothing to do
                        }
                        if (wait) {
                            try {
                                Thread.sleep(POLLING_INTERVAL);
                                time += POLLING_INTERVAL;
                            } catch (InterruptedException ex) {
                                // Nothing to do
                            }
                        }
                    }
                }

                handle.finish();
                isRunning = false;
            }
        };

        handle.start();
        return PROCESSOR.post(r);
    }

    public boolean isRunning() {
        return isRunning;
    }

}
