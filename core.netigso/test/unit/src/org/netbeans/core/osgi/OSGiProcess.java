/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.core.osgi;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import static junit.framework.Assert.*;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.FileResource;
import org.netbeans.SetupHid;
import org.netbeans.nbbuild.MakeOSGi;
import org.openide.util.test.TestFileUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

class OSGiProcess {

    private final File workDir;
    private final Map<String, String> sources = new HashMap<String, String>();
    private String manifest;

    public OSGiProcess(File workDir) {
        this.workDir = workDir;
    }

    public OSGiProcess sourceFile(String path, String... contents) {
        sources.put(path, join(contents));
        return this;
    }

    public OSGiProcess manifest(String... contents) {
        manifest = join(contents);
        return this;
    }

    public void run() throws Exception {
        File platformDir = new File(System.getProperty("platform.dir"));
        assertTrue(platformDir.toString(), platformDir.isDirectory());
        MakeOSGi makeosgi = new MakeOSGi();
        Project antprj = new Project();
        /* XXX does not work, why?
        DefaultLogger logger = new DefaultLogger();
        logger.setOutputPrintStream(System.out);
        logger.setErrorPrintStream(System.err);
        antprj.addBuildListener(logger);
         */
        makeosgi.setProject(antprj);
        FileSet fs = new FileSet();
        fs.setProject(antprj);
        fs.setDir(platformDir);
        fs.createInclude().setName("lib/*.jar");
        fs.createInclude().setName("core/*.jar");
        fs.createInclude().setName("modules/org-netbeans-core-netigso.jar");
        fs.createInclude().setName("modules/org-netbeans-libs-osgi.jar");
        makeosgi.add(fs);
        File extra = new File(workDir, "extra");
        File srcdir = new File(workDir, "custom");
        for (Map.Entry<String, String> entry : sources.entrySet()) {
            TestFileUtils.writeFile(new File(srcdir, entry.getKey()), entry.getValue());
        }
        if (manifest != null) {
            TestFileUtils.writeFile(new File(workDir, "custom.mf"), manifest);
        }
        List<File> cp = new ArrayList<File>();
        for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
            if (!entry.isEmpty()) {
                cp.add(new File(entry));
            }
        }
        SetupHid.createTestJAR(workDir, extra, "custom", null, cp.toArray(new File[cp.size()]));
        makeosgi.add(new FileResource(extra, "custom.jar"));
        File bundles = new File(workDir, "bundles");
        bundles.mkdir();
        makeosgi.setDestdir(bundles);
        makeosgi.execute();
        /* Would need to introspect manifestContents above:
        assertTrue(new File(bundles, "custom-1.0.0.jar").isFile());
         */
        Map<String, Object> config = new HashMap<String, Object>();
        File cache = new File(workDir, "cache");
        config.put(Constants.FRAMEWORK_STORAGE, cache.toString());
        Framework f = ServiceLoader.load(FrameworkFactory.class).iterator().next().newFramework(config);
        f.start();
        List<Bundle> installed = new ArrayList<Bundle>();
        for (File bundle : bundles.listFiles()) {
            installed.add(f.getBundleContext().installBundle(bundle.toURI().toString()));
        }
        Collections.sort(installed, new Comparator<Bundle>() {
            public @Override int compare(Bundle b1, Bundle b2) {
                return b1.getSymbolicName().compareTo(b2.getSymbolicName());
            }
        });
        for (Bundle bundle : installed) {
            bundle.start();
        }
        f.stop();
        f.waitForStop(0);
    }

    private static String join(String[] contents) {
        StringBuilder b = new StringBuilder();
        for (String line : contents) {
            b.append(line).append('\n');
        }
        return b.toString();
    }

}
