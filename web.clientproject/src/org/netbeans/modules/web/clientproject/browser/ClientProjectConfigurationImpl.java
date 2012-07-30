/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.clientproject.browser;

import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.browser.api.BrowserSupport;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectConfigurationImplementation;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectPlatformImplementation;
import org.netbeans.modules.web.clientproject.spi.platform.ProjectConfigurationCustomizer;
import org.netbeans.modules.web.clientproject.spi.platform.RefreshOnSaveListener;
import org.netbeans.modules.web.clientproject.spi.webserver.ServerURLMappingImplementation;
import org.netbeans.modules.web.clientproject.ui.browser.RunConfigurationPanel;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;

public class ClientProjectConfigurationImpl implements ClientProjectConfigurationImplementation {

    private static String USE_SERVER = "use.server";
    private static String MAIN_FILE = "main.file";
    private static String WEB_ROOT = "web.context.root";
    
    final private ClientSideProject project;
    final private WebBrowser browser;
    final private ClientProjectPlatformImpl platform;
    private BrowserSupport browserSupport;
    private ProjectConfigurationCustomizerImpl cust = null;
    private ServerURLMappingImplementation mapping;

    public ClientProjectConfigurationImpl(ClientSideProject project, WebBrowser browser, ClientProjectPlatformImpl platform) {
        this.project = project;
        this.browser = browser;
        this.platform = platform;
    }
    
    @Override
    public String getId() {
        return PropertyUtils.getUsablePropertyName(browser.getId());
    }

    @Override
    public void save() {
        if (cust != null) {
            EditableProperties p = project.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            p.put(USE_SERVER+"."+getId(), Boolean.toString(cust.panel.isUseServer()));
            p.put(MAIN_FILE+"."+getId(), cust.panel.getMainFile());
            String s = cust.panel.getWebContextRoot();
            if (s.trim().length() == 0) {
                s = "/";
            }
            if (!s.startsWith("/")) {
                s = "/" + s;
            }
            p.put(WEB_ROOT+"."+getId(), s);
            project.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, p);
        }
    }

    public boolean isUseServer() {
        String val = project.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(USE_SERVER+"."+getId());
        if (val != null) {
            return Boolean.parseBoolean(val);
        } else {
            return false;
        }
    }

    public String getMainFile() {
        String val = project.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(MAIN_FILE+"."+getId());
        if (val == null) {
            return "index.html";
        } else {
            return val;
        }
    }

    public String getWebContextRoot() {
        String val = project.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(WEB_ROOT+"."+getId());
        if (val == null) {
            return "/"+project.getProjectDirectory().getName();
        } else {
            return val;
        }
    }

    @Override
    public String getDisplayName() {
        return browser.getName();
    }

    @Override
    public RefreshOnSaveListener getRefreshOnSaveListener() {
        return new RefreshOnSaveListenerImpl(project, getBrowserSupport());
    }

    @Override
    public ActionProvider getActionProvider() {
        return new BrowserActionProvider(project, getBrowserSupport(), this);
    }

    @Override
    public ProjectConfigurationCustomizer getProjectConfigurationCustomizer() {
        cust = new ProjectConfigurationCustomizerImpl();
        return cust;
    }
    
    public BrowserSupport getBrowserSupport() {
        if (browserSupport == null) {
            browserSupport = BrowserSupport.create(browser);
        }
        return browserSupport;
    }

    @Override
    public boolean canBeDeleted() {
        return false;
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException("not allowed");
    }

    @Override
    public void deactivate() {
        if (browserSupport != null) {
            getBrowserSupport().close(true);
        }
    }

    @Override
    public ServerURLMappingImplementation getServerURLMapping() {
        if (mapping == null) {
            mapping = new ServerURLMappingImpl(this);
        }
        return mapping;
    }
    
    private class ProjectConfigurationCustomizerImpl implements ProjectConfigurationCustomizer {

        private RunConfigurationPanel panel;
        
        @Override
        public JPanel createPanel() {
            panel = new RunConfigurationPanel(project, ClientProjectConfigurationImpl.this);
            return panel;
        }

    }
    
}
