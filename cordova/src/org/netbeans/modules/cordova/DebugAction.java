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
package org.netbeans.modules.cordova;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cordova.android.AndroidDebugTransport;
import org.netbeans.modules.cordova.ios.IOSDebugTransport;
import org.netbeans.modules.cordova.ios.IOSPlatform;
import org.netbeans.modules.cordova.project.ClientProjectConfigurationImpl;
import org.netbeans.modules.web.browser.api.PageInspector;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.spi.Factory;
import org.netbeans.modules.web.webkit.debugging.spi.netbeansdebugger.NetBeansJavaScriptDebuggerFactory;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

@Messages("CTL_DebugAction=Attach Debugger")
    @ActionID(id = "org.netbeans.modules.cordova.ios.DebugAction", category = "Project")
    @ActionRegistration(displayName = "#CTL_DebugAction", lazy=false)
    @ActionReference(position = 650, path = "Projects/org.netbeans.modules.web.clientproject/Actions")
public final class DebugAction extends AbstractAction implements ContextAwareAction {

    private Project p;
    
    public DebugAction() {
        this(null, false);
    }
    
    public DebugAction(Project p) {
        this(p, true);
    }
    
    private DebugAction(Project p, boolean enabled) {
        this.p = p;
        setEnabled(enabled);
        putValue(Action.NAME, Bundle.CTL_DebugAction());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ProjectConfigurationProvider provider = p.getLookup().lookup(ProjectConfigurationProvider.class);
        ProjectConfiguration activeConfiguration = provider.getActiveConfiguration();
        final MobileDebugTransport transport;
        if (activeConfiguration instanceof ClientProjectConfigurationImpl) {
            transport = ((ClientProjectConfigurationImpl)activeConfiguration).getType().equals(IOSPlatform.TYPE)?new IOSDebugTransport():new AndroidDebugTransport();
        } else {
            return;
        }
        
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                final MobileDebugTransport safariDebugTransport = transport;
                safariDebugTransport.attach();
                WebKitDebugging webKitDebugging = Factory.createWebKitDebugging(transport);
                webKitDebugging.getDebugger().enable();
                NetBeansJavaScriptDebuggerFactory fact = Lookup.getDefault().lookup(NetBeansJavaScriptDebuggerFactory.class);
                org.netbeans.api.debugger.Session debuggerSession = fact.createDebuggingSession(webKitDebugging, Lookups.singleton(p));
                PageInspector.getDefault().inspectPage(Lookups.fixed(webKitDebugging, p));
                safariDebugTransport.setBaseUrl(webKitDebugging.getDOM().getDocument().getDocumentURL());
            }
        });
    }

    public @Override Action createContextAwareInstance(Lookup actionContext) {
        Project p = actionContext.lookup(Project.class);
        if (p == null) {
            return this;
        }
        if (p instanceof Project) {
            return new DebugAction((Project)p);
        }
        return this;
    }

}
