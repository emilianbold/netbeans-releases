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

import org.netbeans.modules.web.clientproject.api.sites.SiteHelper;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation;
import org.netbeans.modules.web.clientproject.util.FileUtilities;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;


public abstract class OnlineSites implements SiteTemplateImplementation {

    private static final Logger LOGGER = Logger.getLogger(OnlineSites.class.getName());

    private final String name;
    private final String url;
    private final File libFile;
    private final String description;
    private final String id;



    protected OnlineSites(String id, String name, String description, String url, File libFile) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.url = url;
        this.libFile = libFile;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
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
    public void configure(ProjectProperties projectProperties) {
        // noop by default
    }

    @Override
    public final void apply(FileObject projectDir, ProjectProperties projectProperties, ProgressHandle handle) throws IOException {
        assert !EventQueue.isDispatchThread();
        if (!isPrepared()) {
            // not correctly prepared, user has to know about it already
            LOGGER.info("Template not correctly prepared, nothing to be applied"); //NOI18N
            return;
        }
        SiteHelper.unzipProjectTemplate(getTargetDir(projectDir, projectProperties), libFile, handle);
    }

    protected FileObject getTargetDir(FileObject projectDir, ProjectProperties projectProperties) {
        // by default, extract template to site root
        return projectDir.getFileObject(projectProperties.getSiteRootFolder());
    }

    @Override
    public Collection<String> supportedLibraries() {
        return SiteHelper.stripRootFolder(FileUtilities.listJsFilesFromZipFile(libFile));
    }

    //~ Inner classes

    @ServiceProvider(service=SiteTemplateImplementation.class, position=150)
    public static class SiteAngularJsSeed extends OnlineSites {

        private static final String SITE_ROOT_FOLDER = "app"; // NOI18N
        private static final String TEST_FOLDER = "test"; // NOI18N
        private static final String CONFIG_FOLDER = "config"; // NOI18N


        @NbBundle.Messages({"SiteAngularJsSeed.name=AngularJS Seed",
                "SiteAngularJsSeed.description=Site template for AngularJS projects."})
        public SiteAngularJsSeed() {
            super("ANGULAR", Bundle.SiteAngularJsSeed_name(), Bundle.SiteAngularJsSeed_description(), // NOI18N
                    "https://github.com/angular/angular-seed/zipball/master", // NOI18N
                    new File(SiteHelper.getJsLibsDirectory(), "angularjs-seed.zip")); // NOI18N
        }

        @Override
        public void configure(ProjectProperties projectProperties) {
            projectProperties.setSiteRootFolder(SITE_ROOT_FOLDER)
                    .setTestFolder(TEST_FOLDER)
                    .setConfigFolder(CONFIG_FOLDER);
        }

        @Override
        protected FileObject getTargetDir(FileObject projectDir, ProjectProperties projectProperties) {
            return projectDir;
        }

    }

    @NbBundle.Messages("SiteInitializr.description=Site template from initializr.com.")
    @ServiceProvider(service=SiteTemplateImplementation.class, position=200)
    public static class BootstrapSiteInitializr extends OnlineSites {

        @NbBundle.Messages("BootstrapSiteInitializr.name=Initializr: Bootstrap")
        public BootstrapSiteInitializr() {
            super("BOOTSTRAP", Bundle.BootstrapSiteInitializr_name(), // NOI18N
                    Bundle.SiteInitializr_description(),
                    "http://www.initializr.com/builder?boot-hero&jquerydev&h5bp-iecond&h5bp-chromeframe&h5bp-analytics&h5bp-favicon&h5bp-appletouchicons&modernizrrespond&izr-emptyscript&boot-css&boot-scripts", // NOI18N
                    new File(SiteHelper.getJsLibsDirectory(), "initializr-bootstrap-latest.zip")); // NOI18N
        }

    }

    @ServiceProvider(service=SiteTemplateImplementation.class, position=210)
    public static class ClassicSiteInitializr extends OnlineSites {

        @NbBundle.Messages("ClassicSiteInitializr.name=Initializr: Classic")
        public ClassicSiteInitializr() {
            super("INIT.CLASSIC", Bundle.ClassicSiteInitializr_name(), // NOI18N
                Bundle.SiteInitializr_description(),
                "http://www.initializr.com/builder?h5bp-content&modernizr&jquerydev&h5bp-iecond&h5bp-chromeframe&h5bp-analytics&h5bp-htaccess&h5bp-favicon&h5bp-appletouchicons&h5bp-scripts&h5bp-robots&h5bp-humans&h5bp-404&h5bp-adobecrossdomain&h5bp-css&h5bp-csshelpers&h5bp-mediaqueryprint&h5bp-mediaqueries", // NOI18N
                    new File(SiteHelper.getJsLibsDirectory(), "initializr-classic-latest.zip")); // NOI18N
        }

    }

    @ServiceProvider(service=SiteTemplateImplementation.class, position=220)
    public static class ResponsiveSiteInitializr extends OnlineSites {

        @NbBundle.Messages("ResponsiveSiteInitializr.name=Initializr: Responsive")
        public ResponsiveSiteInitializr() {
            super("INIT.RESP", Bundle.ResponsiveSiteInitializr_name(), // NOI18N
                    Bundle.SiteInitializr_description(),
                    "http://www.initializr.com/builder?izr-responsive&jquerydev&h5bp-iecond&h5bp-chromeframe&h5bp-analytics&h5bp-favicon&h5bp-appletouchicons&modernizrrespond&h5bp-css&h5bp-csshelpers&h5bp-mediaqueryprint&izr-emptyscript", // NOI18N
                    new File(SiteHelper.getJsLibsDirectory(), "initializr-responsive-latest.zip")); // NOI18N
        }

    }

    @ServiceProvider(service=SiteTemplateImplementation.class, position=300)
    public static class SiteHtml5BoilerplateV4 extends OnlineSites {

        @NbBundle.Messages({"SiteHtml5BoilerplateV4.name=HTML5 Boilerplate v4.2.0",
                "SiteHtml5BoilerplateV4.description=Site template from html5boilerplate.com. Version: 4.2.0"})
        public SiteHtml5BoilerplateV4() {
            super("INIT.BOILER4", Bundle.SiteHtml5BoilerplateV4_name(), Bundle.SiteHtml5BoilerplateV4_description(), // NOI18N
                    "https://github.com/h5bp/html5-boilerplate/zipball/v4.2.0", // NOI18N
                    new File(SiteHelper.getJsLibsDirectory(), "html5-boilerplate-420.zip")); // NOI18N
        }

    }

    @ServiceProvider(service=SiteTemplateImplementation.class, position=400)
    public static class SiteTwitterBootstrap extends OnlineSites {

        @NbBundle.Messages({"SiteTwitterBootstrap.name=Twitter Bootstrap",
                "SiteTwitterBootstrap.description=Site template from twitter.github.com/bootstrap"})
        public SiteTwitterBootstrap() {
            super("TWITTER", Bundle.SiteTwitterBootstrap_name(), Bundle.SiteTwitterBootstrap_description(), // NOI18N
                    "http://twitter.github.com/bootstrap/assets/bootstrap.zip", // NOI18N
                    new File(SiteHelper.getJsLibsDirectory(), "twitter-bootstrap.zip")); // NOI18N
        }

    }

}
