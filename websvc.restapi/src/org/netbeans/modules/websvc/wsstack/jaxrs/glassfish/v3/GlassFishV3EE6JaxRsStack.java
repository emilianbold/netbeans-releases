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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.wsstack.jaxrs.glassfish.v3;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private static final String GFV3_MODULES_DIR_NAME = "modules"; // NOI18N

    private String gfRootStr;
    private JaxRs jaxRs;

    public GlassFishV3EE6JaxRsStack(String gfRootStr) {
        this.gfRootStr = gfRootStr;
        jaxRs = new JaxRs();
    }

    @Override
    public JaxRs get() {
        return jaxRs;
    }

    @Override
    public WSStackVersion getVersion() {
        return WSStackVersion.valueOf(1, 1, 0, 0);
    }

    @Override
    public WSTool getWSTool(Tool toolId) {
        if (toolId == JaxRs.Tool.JAXRS) {
            return WSStackFactory.createWSTool(new JaxRsTool());
        }
        return null;
    }

    @Override
    public boolean isFeatureSupported(Feature feature) {
        boolean isFeatureSupported = false;
        if (feature == JaxRs.Feature.JAXRS) {
            WSTool wsTool = getWSTool(JaxRs.Tool.JAXRS);
            if (wsTool != null) {
                URL[] libs = wsTool.getLibraries();
                if ( libs == null ){
                    return false;
                }
                for( Set<String> set: LIB_SET ){
                    if ( libs.length == set.size() ){
                        isFeatureSupported = true;
                        break;
                    }
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

        @Override
        public boolean accept(File file) {
            return file.getName().startsWith(nameprefix);
        }

    }

    protected class JaxRsTool implements WSToolImplementation {

        JaxRsTool() {
        }
        
        @Override
        public String getName() {
            return JaxRs.Tool.JAXRS.getName();
        }

        @Override
        public URL[] getLibraries() {
            List<URL> result = Collections.emptyList();
            int patternSize = 0;
            for( Set<String> set : LIB_SET ){
                List<URL> cPath = new ArrayList<URL>();
                for (String entry : set) {
                    File f = getJarName(gfRootStr, entry);
                    if ((f != null) && (f.exists())) {
                        try {
                            cPath.add(f.toURI().toURL());
                        }
                        catch (MalformedURLException ex) {

                        }
                    }
                }
                if ( patternSize< set.size() && cPath.size() >=set.size() ){
                    result = cPath;
                    patternSize = set.size();
                }
            }
            return result.toArray(new URL[result.size()]);
        }
    }
    
    private static final Set<String> JAXRS_LIBRARIES_0 = new HashSet<String>();
    private static final Set<String> JAXRS_LIBRARIES_01 = new HashSet<String>();
    private static final Set<String> JAXRS_LIBRARIES_1 = new HashSet<String>();
    private static final Collection<Set<String>> LIB_SET = new ArrayList<Set<String>>(3);
    
    static {
        JAXRS_LIBRARIES_0.add("jackson");                           //NOI18N
        JAXRS_LIBRARIES_0.add("jersey-gf-bundle");                  //NOI18N
        JAXRS_LIBRARIES_0.add("jettison");                          //NOI18N
        JAXRS_LIBRARIES_0.add("jsr311-api");                        //NOI18N
        
        JAXRS_LIBRARIES_01.add("jackson-core-asl");                  //NOI18N
        JAXRS_LIBRARIES_01.add("jersey-gf-bundle");                  //NOI18N
        JAXRS_LIBRARIES_01.add("jersey-multipart");                  //NOI18N
        JAXRS_LIBRARIES_01.add("jettison");                          //NOI18N
        JAXRS_LIBRARIES_01.add("mimepull");                          //NOI18N
        JAXRS_LIBRARIES_01.add("jsr311-api");                        //NOI18N
        
        JAXRS_LIBRARIES_1.add("jackson-core-asl");                  //NOI18N
        JAXRS_LIBRARIES_1.add("jackson-jaxrs");                     //NOI18N
        JAXRS_LIBRARIES_1.add("jackson-mapper-asl");                //NOI18N
        JAXRS_LIBRARIES_1.add("jersey-client");                     //NOI18N
        JAXRS_LIBRARIES_1.add("jersey-core");                       //NOI18N
        JAXRS_LIBRARIES_1.add("jersey-gf-server");                 //NOI18N
        JAXRS_LIBRARIES_1.add("jersey-json");                      //NOI18N
        JAXRS_LIBRARIES_1.add("jersey-multipart");                 //NOI18N
        JAXRS_LIBRARIES_1.add("jettison");                          //NOI18N
        JAXRS_LIBRARIES_1.add("mimepull");                          //NOI18N
        
        LIB_SET.add( JAXRS_LIBRARIES_0 );
        LIB_SET.add( JAXRS_LIBRARIES_01 );
        LIB_SET.add( JAXRS_LIBRARIES_1 );
    }
}
