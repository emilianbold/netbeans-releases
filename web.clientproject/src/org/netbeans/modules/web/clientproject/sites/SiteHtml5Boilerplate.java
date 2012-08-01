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

@ServiceProvider(service=SiteTemplateImplementation.class, position=300)
public class SiteHtml5Boilerplate implements SiteTemplateImplementation {

    private static final Logger LOGGER = Logger.getLogger(SiteHtml5Boilerplate.class.getName());
    private static final File LIB_FILE = new File(SiteHelper.getJsLibDirectory(), "html5-boilerplate-301.zip"); // NOI18N


    @NbBundle.Messages("SiteHtml5Boilerplate.name=HTML5 Boilerplate")
    @Override
    public String getName() {
        return Bundle.SiteHtml5Boilerplate_name();
    }

    @NbBundle.Messages("SiteHtml5Boilerplate.description=Site template from html5boilerplate.com. Version: 3.0.1")
    @Override
    public String getDescription() {
        return Bundle.SiteHtml5Boilerplate_description();
    }

    @Override
    public boolean isPrepared() {
        return LIB_FILE.isFile();
    }

    @Override
    public void prepare() throws IOException {
        assert !EventQueue.isDispatchThread();
        assert !isPrepared();
        SiteHelper.download("https://github.com/h5bp/html5-boilerplate/zipball/v3.0.1", LIB_FILE, null); // NOI18N
    }

    @Override
    public void apply(FileObject p, ProgressHandle handle) throws IOException {
        assert !EventQueue.isDispatchThread();
        if (!isPrepared()) {
            // not correctly prepared, user has to know about it already
            LOGGER.info("Template not correctly prepared, nothing to be applied");
            return;
        }
        SiteHelper.unzip(LIB_FILE, FileUtil.toFile(p), handle);
    }

    @Override
    public Collection<String> supportedLibraries() {
        return Collections.emptyList();
    }

}
