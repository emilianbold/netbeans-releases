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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.jspparser;

import java.io.File;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.project.JavaAntLogger;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.JspParserFactory;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 * Tests that need "full" IDE can be placed here.
 * @author Tomas Mysik
 */
public class IdeEnvironmentTest extends NbTestCase {

    public IdeEnvironmentTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        clearWorkDir();

        File userdir = new File(getWorkDir(), "userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getPath());

        File platformCluster = new File(Lookup.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                .getParentFile().getParentFile();
        File ideCluster = new File(ProjectManager.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                .getParentFile().getParentFile();
        File javaCluster = new File(JavaAntLogger.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                .getParentFile().getParentFile();
        File enterCluster = new File(WebModule.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                .getParentFile().getParentFile();
        System.setProperty("netbeans.home", platformCluster.getPath());
        System.setProperty("netbeans.dirs", javaCluster.getPath() + File.pathSeparator + enterCluster.getPath()
                + File.pathSeparator + ideCluster.getPath());
        System.setProperty("org.netbeans.modules.jspparser.debug", "1");

        Logger.getLogger("org.netbeans.core.startup.ModuleList").setLevel(Level.OFF);

        // module system
        Lookup.getDefault().lookup(ModuleInfo.class);
    }

    // test for issue #70426
    public void testGetTagLibMap70426() throws Exception {
        File projectFile = new File(getDataDir(), "emptyWebProject");
        Project project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(projectFile));
        FileObject jspFo = project.getProjectDirectory().getFileObject("web/index.jsp");
        JspParserAPI.WebModule wm = TestUtil.getWebModule(jspFo);
        Map library = JspParserFactory.getJspParser().getTaglibMap(wm);
        System.out.println("map->" + library);
        Library jstlLibrary = LibraryManager.getDefault().getLibrary("jstl11");
        assertNotNull("Library has to be found", jstlLibrary);
        ProjectClassPathExtender cpExtender = project.getLookup().lookup(ProjectClassPathExtender.class);
        cpExtender.addLibrary(jstlLibrary);
        library = JspParserFactory.getJspParser().getTaglibMap(wm);
        System.out.println("map->" + library);
        assertNotNull("The JSTL/core library was not returned.", library.get("http://java.sun.com/jsp/jstl/core"));
    }
}
