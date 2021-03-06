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
package org.netbeans.modules.web.clientproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Collection;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.api.WebBrowsers;
import org.netbeans.modules.web.browser.spi.ProjectBrowserProvider;
import static org.netbeans.modules.web.browser.spi.ProjectBrowserProvider.PROP_BROWSER_ACTIVE;
import org.netbeans.modules.web.clientproject.env.CommonProjectHelper;
import org.netbeans.modules.web.clientproject.ui.customizer.CustomizerProviderImpl;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;

/**
 *
 */
public class ClientSideProjectBrowserProvider implements ProjectBrowserProvider {

    private ClientSideProject project;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public ClientSideProjectBrowserProvider(ClientSideProject project) {
        this.project = project;
    }

    void activeBrowserHasChanged() {
        pcs.firePropertyChange(PROP_BROWSER_ACTIVE, null, null);
    }

    @Override
    public Collection<WebBrowser> getBrowsers() {
        return WebBrowsers.getInstance().getAll(false, false, true, true);
    }

    @Override
    public WebBrowser getActiveBrowser() {
        return project.getProjectWebBrowser();
    }

    @Override
    public void setActiveBrowser(final WebBrowser browser) throws IllegalArgumentException, IOException {
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
		CommonProjectHelper helper = project.getProjectHelper();
		EditableProperties privateProps = helper.getProperties(CommonProjectHelper.PRIVATE_PROPERTIES_PATH);
                privateProps.put(ClientSideProjectConstants.PROJECT_SELECTED_BROWSER, browser.getId());
		helper.putProperties(CommonProjectHelper.PRIVATE_PROPERTIES_PATH, privateProps);
                try {
                    ProjectManager.getDefault().saveProject(project);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        pcs.firePropertyChange(PROP_BROWSER_ACTIVE, null, null);
    }

    @Override
    public boolean hasCustomizer() {
        return true;
    }

    @Override
    public void customize() {
        project.getLookup().lookup(CustomizerProviderImpl.class).showCustomizer("phonegap");
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener lst) {
        pcs.addPropertyChangeListener(lst);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener lst) {
        pcs.removePropertyChangeListener(lst);
    }

}
