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
package org.netbeans.modules.websvc.rest.nodes;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.support.LogUtils;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.modules.websvc.rest.support.Utils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;

public class TestRestServicesAction extends NodeAction  {

    public String getName() {
        return NbBundle.getMessage(TestRestServicesAction.class, "LBL_TestRestServicesAction");
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length != 1) return false;
        Project prj = activatedNodes[0].getLookup().lookup(Project.class);
        if (prj == null || prj.getLookup().lookup(RestSupport.class) == null) {
            return false;
        }
        return true;
    }

    protected void performAction(Node[] activatedNodes) {
        Project prj = activatedNodes[0].getLookup().lookup(Project.class);
        if (prj != null) {
                FileObject buildFo = Utils.findBuildXml(prj);
                if (buildFo != null) {
                    try {
                        Properties p = setupTestRestBeans(prj);
                        ActionUtils.runTarget(buildFo, new String[]{RestSupport.COMMAND_TEST_RESTBEANS}, p);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    // if there is a rest support (e.g. in Maven projects)
                    RestSupport rs = prj.getLookup().lookup(RestSupport.class);
                    if (rs != null) {
                        try {
                            File testFolder = FileUtil.toFile(prj.getProjectDirectory());
                            FileObject testFO = rs.generateTestClient(testFolder);
                            if (testFO != null) {
                                URL url = testFO.getURL();
                                if (url != null) {
                                    HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                                }
                            }
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }

                // logging usage of action
                Object[] params = new Object[2];
                params[0] = LogUtils.WS_STACK_JAXRS;
                params[1] = "TEST REST"; // NOI18N
                LogUtils.logWsAction(params);

        }
    }

    @Override
    public boolean asynchronous() {
        return true;
    }

    private Properties setupTestRestBeans(Project project) throws IOException {
        Properties p = new Properties();
        p.setProperty(RestSupport.PROP_BASE_URL_TOKEN, RestSupport.BASE_URL_TOKEN);

        String path = RestSupport.RESTBEANS_TEST_DIR;
        RestSupport rs = project.getLookup().lookup(RestSupport.class);
        if (rs != null) {
            try {
                String applicationPath = rs.getApplicationPath();
                if (applicationPath != null) {
                    if (!applicationPath.startsWith("/")) {
                        applicationPath = "/"+applicationPath;
                    }
                    p.setProperty(RestSupport.PROP_APPLICATION_PATH, applicationPath);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            AntProjectHelper helper = rs.getAntProjectHelper();
            EditableProperties projectProps = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            String path1 = projectProps.getProperty(RestSupport.PROP_RESTBEANS_TEST_DIR);
            if (path1 != null) {
                path = path1;
            }
            File testdir = helper.resolveFile(path);
            FileObject testFO = rs.generateTestClient(testdir);
            p.setProperty(RestSupport.PROP_RESTBEANS_TEST_URL, testFO.getURL().toString());
            p.setProperty(RestSupport.PROP_RESTBEANS_TEST_FILE, FileUtil.toFile(testFO).getAbsolutePath());
        }
        return p;
    }

}

