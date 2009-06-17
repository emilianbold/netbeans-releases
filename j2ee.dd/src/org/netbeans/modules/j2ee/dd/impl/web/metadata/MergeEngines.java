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

package org.netbeans.modules.j2ee.dd.impl.web.metadata;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.j2ee.dd.api.common.EnvEntry;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.common.SecurityRole;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WebFragment;
import org.netbeans.modules.j2ee.dd.api.web.model.ServletInfo;
import org.netbeans.modules.j2ee.dd.impl.common.annotation.CommonAnnotationHelper;
import org.netbeans.modules.j2ee.dd.impl.web.annotation.AnnotationHelpers;
import org.netbeans.modules.j2ee.dd.impl.web.annotation.SecurityRoles;
import org.netbeans.modules.j2ee.dd.impl.web.annotation.WebServlet;

/**
 * @author Petr Slechta
 */
public class MergeEngines {

    private static ServletsEngine servletsEngine = new ServletsEngine();
    private static SecurityRolesEngine securityRolesEngine = new SecurityRolesEngine();
    private static ResourceRefsEngine resourceRefsEngine = new ResourceRefsEngine();
    private static ResourceEnvRefsEngine resourceEnvRefsEngine = new ResourceEnvRefsEngine();
    private static ResourceEnvEntriesEngine resourceEnvEntriesEngine = new ResourceEnvEntriesEngine();
    private static ResourceMsgDestsEngine resourceMsgDestsEngine = new ResourceMsgDestsEngine();
    private static ResourceServicesEngine resourceServicesEngine = new ResourceServicesEngine();

    private MergeEngines() {
    }

    // -------------------------------------------------------------------------
    static MergeEngine<ServletInfo> servletsEngine() {
        return servletsEngine;
    }

    static MergeEngine<String> securityRolesEngine() {
        return securityRolesEngine;
    }

    static MergeEngine<ResourceRef> resourceRefsEngine() {
        return resourceRefsEngine;
    }

    static MergeEngine<ResourceEnvRef> resourceEnvRefsEngine() {
        return resourceEnvRefsEngine;
    }

    static MergeEngine<EnvEntry> resourceEnvEntriesEngine() {
        return resourceEnvEntriesEngine;
    }

    static MergeEngine<MessageDestinationRef> resourceMsgDestsEngine() {
        return resourceMsgDestsEngine;
    }

    static MergeEngine<ServiceRef> resourceServicesEngine() {
        return resourceServicesEngine;
    }

    // -------------------------------------------------------------------------
    private static class ServletsEngine extends MergeEngine<ServletInfo> {
        @Override
        void addItems(WebApp webXml) {
            addServlets(webXml);
        }

        @Override
        void addItems(WebFragment webXml) {
            addServlets(webXml);
        }

        @Override
        void addAnnotations(AnnotationHelpers annotationHelpers) {
            for (WebServlet ann : annotationHelpers.getWebServletPOM().getObjects()) {
                res.add(ServletInfoAccessor.getDefault().createServletInfo(
                        ann.getName(), ann.getServletClass(), ann.getUrlPatterns()));
            }
        }

        private void addServlets(WebApp xml) {
            Servlet[] servlets = xml.getServlet();
            if (servlets != null) {
                for (Servlet s : servlets) {
                    String name = s.getServletName();
                    String clazz = s.getServletClass();
                    List<String> urlMappings = findUrlMappingsForServlet(xml, name);
                    res.add(ServletInfoAccessor.getDefault().createServletInfo(name, clazz, urlMappings));
                }
            }
        }

        private List<String> findUrlMappingsForServlet(WebApp xml, String servletName) {
            List<String> mpgs = new ArrayList<String>();
            ServletMapping[] mappings = xml.getServletMapping();
            if (mappings != null) {
                for (ServletMapping sm : mappings) {
                    if (sm.getServletName().equals(servletName) && sm.getUrlPattern() != null)
                        mpgs.add(sm.getUrlPattern());
                }
            }
            return mpgs;
        }
    }

    // -------------------------------------------------------------------------
    private static class SecurityRolesEngine extends MergeEngine<String> {
        @Override
        void addItems(WebApp webXml) {
            addRole(webXml);
        }

        @Override
        void addItems(WebFragment webFragment) {
            addRole(webFragment);
        }

        @Override
        void addAnnotations(AnnotationHelpers annotationHelpers) {
            for (SecurityRoles ann : annotationHelpers.getSecurityRolesPOM().getObjects()) {
                res.addAll(ann.getRoles());
            }
        }

        private void addRole(WebApp xml) {
            for (SecurityRole r : xml.getSecurityRole()) {
                res.add(r.getRoleName());
            }
        }
    }

    // -------------------------------------------------------------------------
    private static class ResourceRefsEngine extends MergeEngine<ResourceRef> {
        @Override
        void addItems(WebApp webXml) {
            // TODO PetrS implement this
        }

        @Override
        void addItems(WebFragment webFragment) {
            // TODO PetrS implement this
        }

        @Override
        void addAnnotations(AnnotationHelpers annotationHelpers) {
            ResourceRef[] refs = CommonAnnotationHelper.getResourceRefs(annotationHelpers.getHelper());
            for (ResourceRef r : refs) {
                res.add(r);
            }
        }
    }

    // -------------------------------------------------------------------------
    private static class ResourceEnvRefsEngine extends MergeEngine<ResourceEnvRef> {
        @Override
        void addItems(WebApp webXml) {
            // TODO PetrS implement this
        }

        @Override
        void addItems(WebFragment webFragment) {
            // TODO PetrS implement this
        }

        @Override
        void addAnnotations(AnnotationHelpers annotationHelpers) {
            ResourceEnvRef[] refs = CommonAnnotationHelper.getResourceEnvRefs(annotationHelpers.getHelper());
            for (ResourceEnvRef r : refs) {
                res.add(r);
            }
        }
    }
    // -------------------------------------------------------------------------
    private static class ResourceEnvEntriesEngine extends MergeEngine<EnvEntry> {
        @Override
        void addItems(WebApp webXml) {
            // TODO PetrS implement this
        }

        @Override
        void addItems(WebFragment webFragment) {
            // TODO PetrS implement this
        }

        @Override
        void addAnnotations(AnnotationHelpers annotationHelpers) {
            EnvEntry[] refs = CommonAnnotationHelper.getEnvEntries(annotationHelpers.getHelper());
            for (EnvEntry r : refs) {
                res.add(r);
            }
        }
    }
    // -------------------------------------------------------------------------
    private static class ResourceMsgDestsEngine extends MergeEngine<MessageDestinationRef> {
        @Override
        void addItems(WebApp webXml) {
            // TODO PetrS implement this
        }

        @Override
        void addItems(WebFragment webFragment) {
            // TODO PetrS implement this
        }

        @Override
        void addAnnotations(AnnotationHelpers annotationHelpers) {
            MessageDestinationRef[] refs = CommonAnnotationHelper.getMessageDestinationRefs(annotationHelpers.getHelper());
            for (MessageDestinationRef r : refs) {
                res.add(r);
            }
        }
    }
    // -------------------------------------------------------------------------
    private static class ResourceServicesEngine extends MergeEngine<ServiceRef> {
        @Override
        void addItems(WebApp webXml) {
            // TODO PetrS implement this
        }

        @Override
        void addItems(WebFragment webFragment) {
            // TODO PetrS implement this
        }

        @Override
        void addAnnotations(AnnotationHelpers annotationHelpers) {
            ServiceRef[] refs = CommonAnnotationHelper.getServiceRefs(annotationHelpers.getHelper());
            for (ServiceRef r : refs) {
                res.add(r);
            }
        }
    }

}
