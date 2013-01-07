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
package org.netbeans.modules.web.clientproject;

import java.util.Collection;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.clientproject.sites.SiteZip;
import org.netbeans.modules.web.clientproject.sites.SiteZipPanel;
import org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation;
import org.netbeans.modules.web.clientproject.ui.customizer.ClientSideProjectProperties;
import org.netbeans.modules.web.clientproject.util.ClientSideProjectUtilities;
import org.netbeans.spi.project.support.ant.AntBasedProjectType;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

public class ClientSideProjectTest extends NbTestCase {

    public ClientSideProjectTest() {
        super("ClientSideProjectTest");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.init();
        Collection<? extends AntBasedProjectType> all = Lookups.forPath("Services/AntBasedProjectTypes").lookupAll(AntBasedProjectType.class);
        MockLookup.setInstances(
                all.iterator().next()
                );
    }

    @Override
    protected void tearDown() throws Exception {
        MockLookup.setLookup(Lookup.EMPTY);
        super.tearDown();
    }

    public void testProjectCreation() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        wd = wd.createFolder(""+System.currentTimeMillis());
        AntProjectHelper projectHelper = ClientSideProjectUtilities.setupProject(wd, "Project1");
        ClientSideProject project = (ClientSideProject) FileOwnerQuery.getOwner(projectHelper.getProjectDirectory());
        ClientSideProjectUtilities.initializeProject(project,
                "public_html_XX",
                "test",
                "config");
        ClientSideProjectProperties projectProperties = new ClientSideProjectProperties(project);
        assertEquals("site root was created", wd.getFileObject("public_html_XX"), FileUtil.toFileObject(projectProperties.getResolvedSiteRootFolder()));
        ProjectProblemsProvider ppp = project.getLookup().lookup(ProjectProblemsProvider.class);
        assertNotNull("project does have ProjectProblemsProvider", ppp);
        assertEquals("project does not have any problems", 0, ppp.getProblems().size());
    }

    public void testProjectCreationWithProblems() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        wd = wd.createFolder(""+System.currentTimeMillis());
        AntProjectHelper projectHelper = ClientSideProjectUtilities.setupProject(wd, "Project2");
        ClientSideProject project = (ClientSideProject) FileOwnerQuery.getOwner(projectHelper.getProjectDirectory());
        ClientSideProjectProperties projectProperties = new ClientSideProjectProperties(project);
        projectProperties.setSiteRootFolder(ClientSideProjectConstants.DEFAULT_SITE_ROOT_FOLDER);
        projectProperties.setTestFolder(ClientSideProjectConstants.DEFAULT_TEST_FOLDER);
        projectProperties.setConfigFolder(ClientSideProjectConstants.DEFAULT_CONFIG_FOLDER);
        projectProperties.save();
        ProjectProblemsProvider ppp = project.getLookup().lookup(ProjectProblemsProvider.class);
        assertNotNull("project does have ProjectProblemsProvider", ppp);
        assertEquals("project does not have any problems", 3, ppp.getProblems().size());
    }

    public void testProjectCreationFromZipTemplate() throws Exception {
        FileObject wd = FileUtil.toFileObject(getWorkDir());
        wd = wd.createFolder(""+System.currentTimeMillis());
        AntProjectHelper projectHelper = ClientSideProjectUtilities.setupProject(wd, "Project3");
        ClientSideProject project = (ClientSideProject) FileOwnerQuery.getOwner(projectHelper.getProjectDirectory());
        SiteZip sz = new SiteZip();
        FileObject dd = FileUtil.toFileObject(getDataDir()).getFileObject("TestTemplate.zip");
        ((SiteZipPanel)(sz.getCustomizer().getComponent())).setTemplate(FileUtil.getFileDisplayName(dd));
        SiteTemplateImplementation.ProjectProperties pp = new SiteTemplateImplementation.ProjectProperties();
        sz.configure(pp);
        ClientSideProjectUtilities.initializeProject(project,
                pp.getSiteRootFolder(),
                pp.getTestFolder(),
                pp.getConfigFolder());
        sz.apply(projectHelper.getProjectDirectory(), pp, ProgressHandleFactory.createHandle("somename"));
        ClientSideProjectProperties projectProperties = new ClientSideProjectProperties(project);
        assertEquals("site root was created from template", wd.getFileObject("custom_siteroot"), FileUtil.toFileObject(projectProperties.getResolvedSiteRootFolder()));
        ProjectProblemsProvider ppp = project.getLookup().lookup(ProjectProblemsProvider.class);
        assertNotNull("project does have ProjectProblemsProvider", ppp);
        assertEquals("project does not have any problems", 0, ppp.getProblems().size());
    }

}
