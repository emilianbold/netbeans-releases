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
package org.netbeans.modules.web.clientproject.sites;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;


abstract class SiteInitializr implements SiteTemplateImplementation {

    private static final Logger LOGGER = Logger.getLogger(SiteInitializr.class.getName());

    private final String name;
    private final String url;
    private final File libFile;


    private SiteInitializr(String name, String url, File libFile) {
        this.name = name;
        this.url = url;
        this.libFile = libFile;
    }

    @Override
    public String getName() {
        return name;
    }

    @NbBundle.Messages("SiteInitializr.description=Site template from initializr.com. Version: 3.0")
    @Override
    public String getDescription() {
        return Bundle.SiteInitializr_description();
    }

    @Override
    public boolean isPrepared() {
        return libFile.isFile();
    }

    @Override
    public void prepare() throws IOException {
        assert !EventQueue.isDispatchThread();
        assert !isPrepared();
        SiteHelper.download(url, libFile, null);
    }

    @Override
    public void apply(FileObject p, ProgressHandle handle) throws IOException {
        assert !EventQueue.isDispatchThread();
        if (!isPrepared()) {
            // not correctly prepared, user has to know about it already
            LOGGER.info("Template not correctly prepared, nothing to be applied");
            return;
        }
        SiteHelper.unzip(libFile, FileUtil.toFile(p), handle);
    }

    @Override
    public Collection<String> supportedLibraries() {
        return Collections.emptyList();
    }

    //~ Inner classes

    @ServiceProvider(service=SiteTemplateImplementation.class, position=200)
    public static class BootstrapSiteInitializr extends SiteInitializr {

        @NbBundle.Messages("BootstrapSiteInitializr.name=Initializr: Bootstrap")
        public BootstrapSiteInitializr() {
            super(Bundle.BootstrapSiteInitializr_name(),
                    "http://www.initializr.com/builder?mode=less&boot-hero&jquerymin&h5bp-chromeframe&h5bp-analytics&h5bp-iecond&h5bp-favicon&h5bp-appletouchicons&modernizrrespond&izr-emptyscript&boot-css&boot-scripts", // NOI18N
                    new File(SiteHelper.getJsLibDirectory(), "initializr-bootstrap-30.zip")); // NOI18N
        }

    }

    @ServiceProvider(service=SiteTemplateImplementation.class, position=210)
    public static class ClassicSiteInitializr extends SiteInitializr {

        @NbBundle.Messages("ClassicSiteInitializr.name=Initializr: Classic")
        public ClassicSiteInitializr() {
            super(Bundle.ClassicSiteInitializr_name(),
                    "http://www.initializr.com/builder?h5bp-content&modernizr&h5bp-htaccess&jquerymin&h5bp-chromeframe&h5bp-analytics&h5bp-iecond&h5bp-favicon&h5bp-appletouchicons&h5bp-scripts&h5bp-robots&h5bp-humans&h5bp-404&h5bp-adobecrossdomain&h5bp-css&h5bp-csshelpers&h5bp-mediaqueryprint&h5bp-mediaqueries", // NOI18N
                    new File(SiteHelper.getJsLibDirectory(), "initializr-classic-30.zip")); // NOI18N
        }

    }

    @ServiceProvider(service=SiteTemplateImplementation.class, position=220)
    public static class ResponsiveSiteInitializr extends SiteInitializr {

        @NbBundle.Messages("ResponsiveSiteInitializr.name=Initializr: Responsive")
        public ResponsiveSiteInitializr() {
            super(Bundle.ResponsiveSiteInitializr_name(),
                    "http://www.initializr.com/builder?izr-responsive&jquerymin&h5bp-chromeframe&h5bp-analytics&h5bp-iecond&h5bp-favicon&h5bp-appletouchicons&modernizrrespond&h5bp-css&h5bp-csshelpers&h5bp-mediaqueryprint&izr-emptyscript", // NOI18N
                    new File(SiteHelper.getJsLibDirectory(), "initializr-responsive-30.zip")); // NOI18N
        }

    }

}
