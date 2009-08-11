/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.rest.spi;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.dd.DDHelper;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping25;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author mkuchtiak
 */
public abstract class WebRestSupport extends RestSupport {
    
    /** Creates a new instance of WebProjectRestSupport */
    public WebRestSupport(Project project) {
        super(project);
    }

    @Override
    public FileObject getPersistenceXml() {
        PersistenceScope ps = PersistenceScope.getPersistenceScope(getProject().getProjectDirectory());
        if (ps != null) {
            return ps.getPersistenceXml();
        }
        return null;
    }
    /** Get deployment descriptor (DD API root bean)
     *
     * @return WebApp bean
     * @throws java.io.IOException
     */
    public WebApp getWebApp() throws IOException {
        FileObject fo = getWebXml();
        if (fo != null) {
            return DDProvider.getDefault().getDDRoot(fo);
        }
        return null;
    }

    public FileObject getWebXml() throws IOException {
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (provider != null) {
            File dd = provider.getJ2eeModule().getDeploymentConfigurationFile("WEB-INF/web.xml"); // NOI18N
            if (dd != null && dd.exists()) {
                return FileUtil.toFileObject(dd);
            } else {
                WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
                if (wm != null) {
                    FileObject webInf = wm.getWebInf();
                    if (webInf != null) {
                        return DDHelper.createWebXml(wm.getJ2eeProfile(), webInf);
                    }
                }
            }
        }
        return null;
    }

    public ServletMapping getRestServletMapping(WebApp webApp) {
        for (ServletMapping sm : webApp.getServletMapping()) {
            if (REST_SERVLET_ADAPTOR.equals(sm.getServletName())) {
                return sm;
            }
        }
        return null;
    }

    protected boolean hasRestServletAdaptor() {
        try {
            return getRestServletAdaptor(getWebApp()) != null;
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            return false;
        }
    }

    protected Servlet getRestServletAdaptor(WebApp webApp) {
        if (webApp != null) {
            for (Servlet s : webApp.getServlet()) {
                if (REST_SERVLET_ADAPTOR.equals(s.getServletName())) {
                    return s;
                }
            }
        }
        return null;
    }
    
    protected void addResourceConfigToWebApp() throws IOException {
        FileObject ddFO = getWebXml();
        WebApp webApp = getWebApp();
        if (webApp == null) {
            return;
        }
        boolean needsSave = false;
        try {
            Servlet adaptorServlet = getRestServletAdaptor(webApp);
            if (adaptorServlet == null) {
                adaptorServlet = (Servlet) webApp.createBean("Servlet");
                adaptorServlet.setServletName(REST_SERVLET_ADAPTOR);
                adaptorServlet.setServletClass(getServletAdapterClass());
                adaptorServlet.setLoadOnStartup(BigInteger.valueOf(1));
                webApp.addServlet(adaptorServlet);
                needsSave = true;
            }

            ServletMapping sm = getRestServletMapping(webApp);
            if (sm == null) {
                sm = (ServletMapping) webApp.createBean("ServletMapping");
                sm.setServletName(adaptorServlet.getServletName());
                if (sm instanceof ServletMapping25) {
                    ((ServletMapping25)sm).addUrlPattern(REST_SERVLET_ADAPTOR_MAPPING);
                } else {
                    sm.setUrlPattern(REST_SERVLET_ADAPTOR_MAPPING);
                }
                webApp.addServletMapping(sm);
                needsSave = true;
            }
            if (needsSave) {
                webApp.write(ddFO);
            }
        } catch (IOException ioe) {
            throw ioe;
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    protected void removeResourceConfigFromWebApp() throws IOException {
        FileObject ddFO = getWebXml();
        WebApp webApp = getWebApp();
        if (webApp == null) {
            return;
        }
        boolean needsSave = false;
        Servlet restServlet = getRestServletAdaptor(webApp);
        if (restServlet != null) {
            webApp.removeServlet(restServlet);
            needsSave = true;
        }
        ServletMapping sm = getRestServletMapping(webApp);
        if (sm != null) {
            webApp.removeServletMapping(sm);
            needsSave = true;
        }
        if (needsSave) {
            webApp.write(ddFO);
        }
    }
}
