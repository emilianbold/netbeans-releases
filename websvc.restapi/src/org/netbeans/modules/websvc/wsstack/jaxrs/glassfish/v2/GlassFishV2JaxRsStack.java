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
package org.netbeans.modules.websvc.wsstack.jaxrs.glassfish.v2;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
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
 * @author rico
 */
public class GlassFishV2JaxRsStack implements WSStackImplementation<JaxRs> {

    private static final String ASM_31_JAR = "lib/asm-3.1.jar"; //NOI18N
    private static final String JERSEY_BUNDLE_10_JAR = "lib/jersey-bundle-1.0.jar"; //NOI18N
    private static final String JETTISON_10_JAR = "lib/jettison-1.0.1.jar"; //NOI18N
    private static final String JSR311_API_JAR = "lib/jsr311-api-1.0.jar";//NOI18N
    private File root;
    private JaxRs jaxRs;

    public GlassFishV2JaxRsStack(File root) {
        this.root = root;
        jaxRs = new JaxRs();
    }

    public JaxRs get() {
        return jaxRs;
    }

    public WSStackVersion getVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public WSTool getWSTool(Tool toolId) {
        if (toolId == JaxRs.Tool.JAXRS) {
            return WSStackFactory.createWSTool(new JaxRsTool(JaxRs.Tool.JAXRS));
        }
        return null;
    }

    public boolean isFeatureSupported(Feature feature) {
        if (feature == JaxRs.Feature.JAXRS) {
        WSTool wsTool = getWSTool(JaxRs.Tool.JAXRS);
        if (wsTool != null) {
            URL[] libs = wsTool.getLibraries();
            try {
                for (URL lib : libs) {
                    if (!new File(lib.toURI()).exists()) {
                        return false;
                    }
                }
            } catch (URISyntaxException e) {
                return false;
            }
        } else {
            return false;
        }
        } else {
            return false;
        }
        return true;
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
            try {
                return new URL[]{
                            new File(root, ASM_31_JAR).toURI().toURL(), // NOI18N
                            new File(root, JERSEY_BUNDLE_10_JAR).toURI().toURL(), //NOI18N
                            new File(root, JETTISON_10_JAR).toURI().toURL(), //NOI18N
                            new File(root, JSR311_API_JAR).toURI().toURL(), //NOI18N
                        };
            } catch (MalformedURLException ex) {
                return new URL[0];
            }
        }
    }
}
