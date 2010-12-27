/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.editor;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Sources;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.test.MockLookup;

/**
 *
 * @author marekfukala
 */
public class TestBaseForTestProject extends TestBase {

    private FileObject srcFo, webFo, projectFo, javaLibSrc;

    public TestBaseForTestProject(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        //disable info exceptions from j2eeserver
        Logger.getLogger("org.netbeans.modules.j2ee.deployment.impl.ServerRegistry").setLevel(Level.SEVERE);

        //so InstalledFileLocatorImpl finds the jsf "modules/ext/jsf-2_0/jsf-impl.jar"
        System.setProperty("netbeans.dirs", "/Volumes/Mercurial/web-main/nbbuild/netbeans/enterprise");

        this.projectFo = getTestFile("testWebProject");
        this.srcFo = getTestFile("testWebProject/src");
        this.webFo = getTestFile("testWebProject/web");

        this.javaLibSrc = getTestFile("testJavaJSFLibrary/src");

        //create classpath
        Map<String, ClassPath> cps = new HashMap<String, ClassPath>();
        cps.put(ClassPath.COMPILE, createServletAPIClassPath());
        cps.put(ClassPath.EXECUTE, createServletAPIClassPath());
        cps.put(ClassPath.SOURCE, ClassPathSupport.createClassPath(new FileObject[]{srcFo, webFo}));
        cps.put(ClassPath.BOOT, createBootClassPath());

        ClassPathProvider classpathProvider = new TestClassPathProvider(cps);
        Sources sources = new TestSources(srcFo, webFo);

        MockLookup.setInstances(
                new OpenProject(),
                new TestUserCatalog(),
                new TestProjectFactory(classpathProvider, sources),
                new SimpleFileOwnerQueryImplementation(),
                classpathProvider,
                new TestLanguageProvider(),
                new FakeWebModuleProvider(webFo, srcFo));

        refreshIndexAndWait();
    }

    protected void refreshIndexAndWait() throws FileStateInvalidException {
        //uff, it looks like we need to refresh the source roots separately since
        //if I use the project's folder here, then the index data are stored to
        //its index folder, but later the QuerySupport uses different cache folders
        //for webFO and srcFO so the index returns nothing.
        IndexingManager.getDefault().refreshIndexAndWait(srcFo.getURL(), null);
        IndexingManager.getDefault().refreshIndexAndWait(webFo.getURL(), null);
        IndexingManager.getDefault().refreshIndexAndWait(javaLibSrc.getURL(), null);
    }

    protected JsfSupportImpl getJsfSupportImpl() {
        JsfSupportImpl instance = JsfSupportImpl.findFor(getWebFolder());
        assertNotNull(instance);

        return instance;
    }

    protected FileObject getSourcesFolder() {
        return srcFo;
    }

    protected FileObject getWebFolder() {
        return webFo;
    }

    protected FileObject getProjectFolder() {
        return projectFo;
    }
}
