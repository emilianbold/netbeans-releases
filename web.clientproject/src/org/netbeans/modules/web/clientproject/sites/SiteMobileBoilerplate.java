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

/**
 *
 */
@ServiceProvider(service=SiteTemplateImplementation.class, position=500)
public class SiteMobileBoilerplate implements SiteTemplateImplementation {

    private static final Logger LOGGER = Logger.getLogger(SiteMobileBoilerplate.class.getName());
    private static final File LIB_FILE = new File(SiteHelper.getJsLibsDirectory(), "mobile-boilerplate-30.zip"); // NOI18N

    @Override
    public String getId() {
        return "MOBILE.BOILER"; // NOI18N
    }

    @NbBundle.Messages("SiteMobileBoilerplate.name=Mobile Boilerplate")
    @Override
    public String getName() {
        return Bundle.SiteMobileBoilerplate_name();
    }

    @NbBundle.Messages("SiteMobileBoilerplate.description=Site template from html5boilerplate.com/mobile. Version: 3.0")
    @Override
    public String getDescription() {
        return Bundle.SiteMobileBoilerplate_description();
    }

    @Override
    public boolean isPrepared() {
        return LIB_FILE.isFile();
    }

    @Override
    public void prepare() throws IOException {
        assert !EventQueue.isDispatchThread();
        assert !isPrepared();
        SiteHelper.download("https://github.com/h5bp/mobile-boilerplate/zipball/v3.0", LIB_FILE, null); // NOI18N
    }

    @Override
    public void configure(ProjectProperties projectProperties) {
        // noop
    }

    @Override
    public void apply(FileObject projectDir, ProjectProperties projectProperties, ProgressHandle handle) throws IOException {
        assert !EventQueue.isDispatchThread();
        if (!isPrepared()) {
            // not correctly prepared, user has to know about it already
            LOGGER.info("Template not correctly prepared, nothing to be applied"); //NOI18N
            return;
        }
        SiteHelper.unzipProjectTemplate(projectDir.getFileObject(projectProperties.getSiteRootFolder()), LIB_FILE, handle);
    }

    @Override
    public Collection<String> supportedLibraries() {
        return SiteHelper.stripRootFolder(FileUtilities.listJsFilesFromZipFile(LIB_FILE));
    }

}
