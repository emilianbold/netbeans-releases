/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.dd.impl.web.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.dd.api.web.WebFragment;
import org.netbeans.modules.j2ee.dd.api.web.WebFragmentProvider;
import org.netbeans.modules.j2ee.dd.api.web.model.ServletInfo;
import org.netbeans.modules.j2ee.dd.impl.web.annotation.AnnotationHelpers;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;

/**
 * Implementation of WebAppMetadata interface.
 * The logic for fragments and annotation scanning is here.
 * Also merging of data from deployment descriptors and annotations is here.
 * 
 * @author Petr Slechta
 */
public class WebAppMetadataImpl implements WebAppMetadata {

    private static final Logger LOG = Logger.getLogger(WebAppMetadataImpl.class.getName());
    private WebAppMetadataModelImpl modelImpl;
    private AnnotationHelpers annoHelpers;
    private MetadataUnit metadataUnit;
    private WebApp webXml;
    private long webXmlLastModification = -1L;
    private List<FragmentRec> fragmentRecs = new ArrayList<FragmentRec>();

    public WebAppMetadataImpl(MetadataUnit metadataUnit, WebAppMetadataModelImpl modelImpl) {
        this.metadataUnit = metadataUnit;
        this.modelImpl = modelImpl;
        refreshWebXml();
    }

    // -------------------------------------------------------------------------
    // INTERFACE IMPLEMENTATION
    // -------------------------------------------------------------------------
    public WebApp getRoot() {
        refreshWebXml();
        return webXml;
    }

    public List<WebFragment> getFragments() {
        refreshFragments();
        List<WebFragment> res = new ArrayList<WebFragment>();
        for (FragmentRec fr : fragmentRecs) {
            res.add(fr.fragment);
        }
        return res;
    }

    public List<ServletInfo> getServlets() {
        return doMerging(MergeEngines.servletsEngine());
    }

    public List<String> getSecurityRoles() {
        return doMerging(MergeEngines.securityRolesEngine());
    }

    // -------------------------------------------------------------------------
    // HELPER METHODS
    // -------------------------------------------------------------------------
    private <T> List<T> doMerging(MergeEngine<T> eng) {
        eng.clean();

        // from web.xml
        refreshWebXml();
        if (webXml != null) {
            eng.addItems(webXml);
            boolean complete;
            try {
                complete = webXml.isMetadataComplete();
            }
            catch (VersionNotSupportedException ex) {
                // old version of DD, let's suppose it is complete
                complete = true;
            }
            if (complete)
                return eng.getResult();
        }

        // from web-fragment.xml files
        for (WebFragment wf : getFragments()) {
            eng.addItems(wf);
        }

        // from annotations
        eng.addAnnotations(getAnnotationHelpers());

        return eng.getResult();
    }

    private void refreshWebXml() {
        FileObject dd = metadataUnit.getDeploymentDescriptor();
        if (dd == null) {
            webXml = null;
            webXmlLastModification = -1L;
            return;
        }
        dd.refresh();
        long lastModif = dd.lastModified().getTime();
        if (lastModif > webXmlLastModification) {
            try {
                webXml = DDProvider.getDefault().getDDRoot(dd, false);
                webXmlLastModification = lastModif;
            }
            catch (IOException ex) {
                LOG.log(Level.SEVERE, "Error during web.xml parsing!", ex);
                webXml = null;
            }
        }
    }

    // -------------------------------------------------------------------------
    private void refreshFragments() {
        List<FragmentRec> res = new ArrayList<FragmentRec>();
        List<FileObject> frgs = metadataUnit.getCompilePath().findAllResources("META-INF/web-fragment.xml");
        for (FileObject fo : frgs) {
            FragmentRec oldRec = findFragmentRec(fo);
            fo.refresh();
            long lastModif = fo.lastModified().getTime();
            if (oldRec == null || lastModif > oldRec.lastModification) {
                try {
                    FragmentRec newRec = new FragmentRec();
                    newRec.source = fo;
                    newRec.lastModification = lastModif;
                    newRec.fragment = WebFragmentProvider.getDefault().getWebFragmentRoot(fo);
                    res.add(newRec);
                }
                catch (Exception ex) {
                    LOG.log(Level.SEVERE, "Error during web-fragment.xml parsing! File: "+fo, ex);
                }
            }
            else {
                res.add(oldRec);
            }
        }
        fragmentRecs = res;
    }

    private FragmentRec findFragmentRec(FileObject fo) {
        for (FragmentRec fr : fragmentRecs) {
            try {
                if (fr.source.getURL().equals(fo.getURL()))
                    return fr;
            }
            catch (FileStateInvalidException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    private AnnotationHelpers getAnnotationHelpers() {
        if (annoHelpers == null) {
            annoHelpers = new AnnotationHelpers(modelImpl.getHelper());
        }
        return annoHelpers;
    }

    // -------------------------------------------------------------------------
    private void addServlets(List<ServletInfo> res, WebApp webXml) {
        Servlet[] servlets = webXml.getServlet();
        if (servlets != null) {
            for (Servlet s : servlets) {
                String name = s.getServletName();
                String clazz = s.getServletClass();
                List<String> urlMappings = findUrlMappingsForServlet(webXml, name);
                res.add(ServletInfoAccessor.getDefault().createServletInfo(name, clazz, urlMappings));
            }
        }
    }

    private List<String> findUrlMappingsForServlet(WebApp webXml, String servletName) {
        List<String> res = new ArrayList<String>();
        ServletMapping[] mappings = webXml.getServletMapping();
        if (mappings != null) {
            for (ServletMapping sm : mappings) {
                if (sm.getServletName().equals(servletName) && sm.getUrlPattern() != null)
                    res.add(sm.getUrlPattern());
            }
        }
        return res;
    }

    // -------------------------------------------------------------------------
    // INNER CLASSES
    // -------------------------------------------------------------------------
    private static class FragmentRec {
        WebFragment fragment;
        long lastModification;
        FileObject source;
    }

}
