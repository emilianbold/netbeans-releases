
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;
import org.netbeans.modules.web.browser.api.BrowserSupport;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.api.WebBrowsers;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectConfigurationImplementation;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectPlatformImplementation;

public class ClientProjectPlatformImpl implements ClientProjectPlatformImplementation {

    private Project p;
    private BrowserSupport bs;
    private List<ClientProjectConfigurationImpl> configs;
    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    public ClientProjectPlatformImpl(Project p) {
        this.p = p;
        WebBrowsers.getInstance().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (WebBrowsers.PROP_BROWSERS.equals(evt.getPropertyName())) {
                    configs = null;
                    support.firePropertyChange(ClientProjectPlatformImplementation.PROP_CONFIGURATIONS, null, null);
                }
            }
        });
    }
    
    @Override
    public List<? extends ClientProjectConfigurationImplementation> getConfigurations() {
        if (configs == null) {
            configs = getBrowserConfigurations();
        }
        return configs;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener lst) {
        support.addPropertyChangeListener(lst);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener lst) {
        support.removePropertyChangeListener(lst);
    }

    private List<ClientProjectConfigurationImpl> getBrowserConfigurations() {
        List<ClientProjectConfigurationImpl> l = new ArrayList<ClientProjectConfigurationImpl>();
        int chrome = 200;
        int chromium = 300;
        int others = 400;
        for (WebBrowser browser : WebBrowsers.getInstance().getAll(false)) {
            if (browser.getBrowserFamily() == BrowserFamilyId.JAVAFX_WEBVIEW) {
                l.add(new ClientProjectConfigurationImpl((ClientSideProject)p, browser, this, Boolean.TRUE, 100, false));
            } else if (browser.getBrowserFamily() == BrowserFamilyId.CHROME || browser.getId().endsWith("ChromeBrowser")) { //NOI18N
                l.add(new ClientProjectConfigurationImpl((ClientSideProject)p, browser, this, Boolean.TRUE, chrome++, false));
                l.add(new ClientProjectConfigurationImpl((ClientSideProject)p, browser, this, Boolean.FALSE, chrome++, true));
            } else if (browser.getBrowserFamily() == BrowserFamilyId.CHROMIUM || browser.getId().endsWith("ChromiumBrowser")) { //NOI18N
                l.add(new ClientProjectConfigurationImpl((ClientSideProject)p, browser, this, Boolean.TRUE, chromium++, false));
                l.add(new ClientProjectConfigurationImpl((ClientSideProject)p, browser, this, Boolean.FALSE, chromium++, true));
            } else {
                l.add(new ClientProjectConfigurationImpl((ClientSideProject)p, browser, this, null, others++, false));
            }
        }
        Collections.sort(l, new Comparator<ClientProjectConfigurationImpl>() {
            @Override
            public int compare(ClientProjectConfigurationImpl o1, ClientProjectConfigurationImpl o2) {
                return o1.getOrder() - o2.getOrder();
            }
        });
        return l;
    }

    @Override
    public List<String> getNewConfigurationTypes() {
        return Collections.emptyList();
    }

    @Override
    public String createConfiguration(String configurationType, String configurationName) {
        return null;
    }
    
}
