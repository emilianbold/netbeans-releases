/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.websvc.wsstack.jaxrs.glassfish.v3;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.websvc.wsstack.api.WSStack.Feature;
import org.netbeans.modules.websvc.wsstack.api.WSStack.Tool;
import org.netbeans.modules.websvc.wsstack.api.WSStackVersion;
import org.netbeans.modules.websvc.wsstack.api.WSTool;
import org.netbeans.modules.websvc.wsstack.jaxrs.JaxRs;
import org.netbeans.modules.websvc.wsstack.spi.WSStackFactory;
import org.netbeans.modules.websvc.wsstack.spi.WSStackImplementation;
import org.netbeans.modules.websvc.wsstack.spi.WSToolImplementation;

/**
 *
 * @author ayubkhan
 */
public class GlassFishV3EE6JaxRsStack implements WSStackImplementation<JaxRs> {

    private static final String[] JAXRS_LIBRARIES =
        new String[] {"asm", "jackson", "jersey-gf-bundle", "jettison", "jsr311-api"}; //NOI18N
    private static final String GFV3_MODULES_DIR_NAME = "modules"; // NOI18N

    private String gfRootStr;
    private JaxRs jaxRs;

    public GlassFishV3EE6JaxRsStack(String gfRootStr) {
        this.gfRootStr = gfRootStr;
        jaxRs = new JaxRs();
    }

    public JaxRs get() {
        return jaxRs;
    }

    public WSStackVersion getVersion() {
        return WSStackVersion.valueOf(1, 1, 0, 0);
    }

    public WSTool getWSTool(Tool toolId) {
        if (toolId == JaxRs.Tool.JAXRS) {
            return WSStackFactory.createWSTool(new JaxRsTool(JaxRs.Tool.JAXRS));
        }
        return null;
    }

    public boolean isFeatureSupported(Feature feature) {
        boolean isFeatureSupported = false;
        if (feature == JaxRs.Feature.JAXRS) {
            WSTool wsTool = getWSTool(JaxRs.Tool.JAXRS);
            if (wsTool != null) {
                URL[] libs = wsTool.getLibraries();
                if(libs != null && libs.length == JAXRS_LIBRARIES.length) {
                    isFeatureSupported = true;
                }
            }
        }
        return isFeatureSupported;
    }

    private File getJarName(String glassfishInstallRoot, String jarNamePrefix) {
        File modulesDir = new File(glassfishInstallRoot + File.separatorChar + GFV3_MODULES_DIR_NAME);

        File candidates[] = modulesDir.listFiles(new VersionFilter(jarNamePrefix));

        if(candidates != null && candidates.length > 0) {
            return candidates[0]; // the first one
        } else {
            return null;
        }
    }

    private static class VersionFilter implements FileFilter {

        private String nameprefix;

        public VersionFilter(String nameprefix) {
            this.nameprefix = nameprefix;
        }

        public boolean accept(File file) {
            return file.getName().startsWith(nameprefix);
        }

    }

    private class JaxRsTool implements WSToolImplementation {

        JaxRs.Tool tool;

        JaxRsTool(JaxRs.Tool tool) {
            this.tool = tool;
        }

        public String getName() {
            return tool.getName();
        }

        public URL[] getLibraries() {
            List<URL> cPath = new ArrayList<URL>();
            for (String entry : JAXRS_LIBRARIES) {
                File f = getJarName(gfRootStr, entry);
                if ((f != null) && (f.exists())) {
                    try {
                        cPath.add(f.toURI().toURL());
                    } catch (MalformedURLException ex) {

                    }
                }
            }
            return cPath.toArray(new URL[cPath.size()]);
        }
    }
}
