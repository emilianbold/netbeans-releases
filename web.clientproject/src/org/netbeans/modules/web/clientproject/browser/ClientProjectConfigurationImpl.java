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
import org.netbeans.modules.web.browser.api.BrowserFamilyId;
import org.netbeans.modules.web.browser.api.BrowserSupport;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.ClientSideProjectConstants;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectConfigurationImplementation;
import org.netbeans.modules.web.clientproject.spi.platform.ProjectConfigurationCustomizer;
import org.netbeans.modules.web.clientproject.spi.platform.RefreshOnSaveListener;
import org.netbeans.modules.web.clientproject.spi.webserver.ServerURLMappingImplementation;
import org.netbeans.modules.web.clientproject.ui.browser.BrowserConfigurationPanel;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;

public class ClientProjectConfigurationImpl implements ClientProjectConfigurationImplementation {

    
    final private ClientSideProject project;
    final private WebBrowser browser;
    final private ClientProjectPlatformImpl platform;
    private BrowserSupport browserSupport;
    private ProjectConfigurationCustomizerImpl cust = null;
    private ServerURLMappingImplementation mapping;
    private Boolean browserIntegration;
    private int order;
    private boolean disableIntegration;

    public ClientProjectConfigurationImpl(ClientSideProject project, WebBrowser browser, 
        ClientProjectPlatformImpl platform, Boolean browserIntegration, int order, boolean disableIntegration) {
        this.project = project;
        this.browser = browser;
        this.platform = platform;
        this.browserIntegration = browserIntegration;
        this.order = order;
        this.disableIntegration = disableIntegration;
    }
    
    @Override
    public String getId() {
        return PropertyUtils.getUsablePropertyName(browser.getId()+(disableIntegration ? ".dis" : ""));
    }

    @Override
    public void save() {
        if (cust != null && getBrowserIntegration() == Boolean.TRUE) {
            EditableProperties p = project.getProjectHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            p.put(ClientSideProjectConstants.PROJECT_AUTO_REFRESH+"."+getId(), Boolean.toString(cust.panel.isAutoRefresh()));
            p.put(ClientSideProjectConstants.PROJECT_HIGHLIGHT_SELECTION+"."+getId(), Boolean.toString(cust.panel.isHighlightSelection()));
            project.getProjectHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, p);
        }
    }

    /**
     * @return null mean that browser in this configuration does not have nifty 
     *    NetBeans integration; TRUE means it has and it is in enabled; FALSE means it has 
     *    but it is disabled
     */
    public Boolean getBrowserIntegration() {
        return browserIntegration;
    }
    
    public boolean canBeDefaultConfiguration() {
        return Boolean.TRUE.equals(browserIntegration) && 
            (browser.getBrowserFamily() == BrowserFamilyId.CHROME || browser.getBrowserFamily() == BrowserFamilyId.CHROMIUM);
    }
    
    public boolean isAutoRefresh() {
        String val = project.getEvaluator().getProperty(ClientSideProjectConstants.PROJECT_AUTO_REFRESH+"."+getId());
        if (val != null) {
            return Boolean.parseBoolean(val) && getBrowserIntegration() == Boolean.TRUE;
        } else {
            // if browserIntegration is available then default is true for AutoRefresh
            return getBrowserIntegration() == Boolean.TRUE;
        }
    }

    @Override
    public boolean isHighlightSelectionEnabled() {
        String val = project.getEvaluator().getProperty(ClientSideProjectConstants.PROJECT_HIGHLIGHT_SELECTION+"."+getId());
        if (val != null) {
            return Boolean.parseBoolean(val) && getBrowserIntegration() == Boolean.TRUE;
        } else {
            // if browserIntegration is available then default is true for HighlightSelectionEnabled
            return getBrowserIntegration() == Boolean.TRUE;
        }
    }

    @Override
    public String getDisplayName() {
        String suffix = "";
        if (browserIntegration == Boolean.TRUE && browser.getBrowserFamily() != BrowserFamilyId.JAVAFX_WEBVIEW) {
            suffix = " with NetBeans Integration";
        }
        return browser.getName() + suffix;
    }

    @Override
    public RefreshOnSaveListener getRefreshOnSaveListener() {
        return new RefreshOnSaveListenerImpl(project, getBrowserSupport(), this);
    }

    @Override
    public ActionProvider getActionProvider() {
        return new BrowserActionProvider(project, getBrowserSupport(), this);
    }

    @Override
    public ProjectConfigurationCustomizer getProjectConfigurationCustomizer() {
        if (cust == null) {
            cust = new ProjectConfigurationCustomizerImpl();
        }
        return cust;
    }
    
    public BrowserSupport getBrowserSupport() {
        if (browserSupport == null) {
            if (browser.isEmbedded()) {
                browserSupport = BrowserSupport.getDefaultEmbedded();
            } else {
                browserSupport = BrowserSupport.create(browser, disableIntegration);
            }
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
            getBrowserSupport().close(false);
        }
    }

    int getOrder() {
        return order;
    }

    private class ProjectConfigurationCustomizerImpl implements ProjectConfigurationCustomizer {

        private BrowserConfigurationPanel panel;
        
        @Override
        public JPanel createPanel() {
            panel = new BrowserConfigurationPanel(project, 
                    ClientProjectConfigurationImpl.this, ClientProjectConfigurationImpl.this.browser);
            return panel;
        }

    }
    
}
