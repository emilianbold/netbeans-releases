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
package org.netbeans.modules.web.webkit.tooling.networkmonitor;

import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;
import org.netbeans.modules.web.webkit.debugging.api.console.Console;
import org.netbeans.modules.web.webkit.debugging.api.console.ConsoleMessage;
import org.netbeans.modules.web.webkit.debugging.api.network.Network;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 */
public class NetworkMonitor implements Network.Listener, Console.Listener {

    private NetworkMonitorTopComponent component;
    private NetworkMonitorTopComponent.Model model;
    private final BrowserFamilyId browserFamilyId;
    private final Project project;

    private NetworkMonitor(Lookup projectContext, NetworkMonitorTopComponent comp, 
            NetworkMonitorTopComponent.Model mod) {
        this.component = comp;
        this.model = mod;
        browserFamilyId = projectContext.lookup(BrowserFamilyId.class);
        project = projectContext.lookup(Project.class);
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (component == null) {
                    component = new NetworkMonitorTopComponent(NetworkMonitor.this, model);
                }
                component.open();
            }
        });
    }

    public static NetworkMonitor createNetworkMonitor(Lookup projectContext) {
        NetworkMonitorTopComponent component = null;
        NetworkMonitorTopComponent.Model model;
        for (TopComponent tc : TopComponent.getRegistry().getOpened()) {
            if (tc instanceof NetworkMonitorTopComponent) {
                component = (NetworkMonitorTopComponent)tc;
                break;
            }
        }
        if (component != null) {
            model = component.getModel();
        } else {
            model = new NetworkMonitorTopComponent.Model();
        }
        return new NetworkMonitor(projectContext, component, model);
    }

    public void close() {
        if (model.getSize() == 0 && component != null) {
            final NetworkMonitorTopComponent comp = component;
            component = null;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    comp.close();
                }
            });
        } else {
            model.close(project);
        }
    }

    @Override
    public void networkRequest(Network.Request request) {
        if (component != null) {
            model.add(request, browserFamilyId, project);
        }
        DependentFileQueryImpl.DEFAULT.networkRequest(project, request);
    }

    @Override
    public void webSocketRequest(Network.WebSocketRequest request) {
        if (component != null) {
            model.add(request, browserFamilyId, project);
        }
    }

    void componentClosed() {
        component = null;
    }

    @Override
    public void messageAdded(ConsoleMessage message) {
        if (component != null) {
            model.console(message);
        }
    }

    @Override
    public void messagesCleared() {
    }

    @Override
    public void messageRepeatCountUpdated(int count) {
    }

}
