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

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry;
import org.netbeans.spi.project.support.ant.PropertyUtils;

/**
 * For now all well-known containers (based on Europa/Ganymede) are hardcoded here.
 * Can be refactored into SPI if needed.
 * 
 */
public class ClassPathContainerResolver {

    public static final String JUNIT_CONTAINER = "org.eclipse.jdt.junit.JUNIT_CONTAINER/";
    public static final String USER_LIBRARY_CONTAINER = "org.eclipse.jdt.USER_LIBRARY/";
    public static final String WEB_CONTAINER = "org.eclipse.jst.j2ee.internal.web.container";
    public static final String J2EE_MODULE_CONTAINER = "org.eclipse.jst.j2ee.internal.module.container";
    public static final String JSF_CONTAINER = "org.eclipse.jst.jsf.core.internal.jsflibrarycontainer/";
    public static final String J2EE_SERVER_CONTAINER = "org.eclipse.jst.server.core.container/";
    
    /**
     * Converts eclipse CONTAINER claspath entry to something what can be put
     * directly to Ant based project classpath. 
     * 
     * Eg. for "org.eclipse.jdt.junit.JUNIT_CONTAINER/3.8.1" it would be "libs.junit.classpath"
     * 
     * This method is called after .classpath file was parsed.
     */
    public static boolean resolve(DotClassPathEntry entry) {
        assert entry.getKind() == DotClassPathEntry.Kind.CONTAINER : entry;
        
        String container = entry.getRawPath();
        
        if (container.startsWith(JUNIT_CONTAINER)) {
            String library = "libs.junit.classpath";
            if (container.substring(JUNIT_CONTAINER.length()).startsWith("4")) {
                library = "libs.junit_4.classpath";
            }
            entry.setContainerMapping(library);
            return true;
        }
        
        if (container.startsWith(USER_LIBRARY_CONTAINER)) {
            entry.setContainerMapping("libs."+getNetBeansLibraryName(container)+".classpath");
            return true;
        }
        
        if (container.startsWith(WEB_CONTAINER) || 
            container.startsWith(J2EE_MODULE_CONTAINER) ||
            container.startsWith(JSF_CONTAINER) ||
            container.startsWith(J2EE_SERVER_CONTAINER)) {
            // TODO: resolve these containers as empty for now.
            //       most of these are not needed anyway as they are 
            //       handled differntly directly by web project
            entry.setContainerMapping("");
            return true;
        }
        
        return false;
    }

    private static String getNetBeansLibraryName(String container) {
        return PropertyUtils.getUsablePropertyName(container.substring(USER_LIBRARY_CONTAINER.length()));
    }

    /**
     * This method is called just before ProjectTypeFactory.createProject.
     * 
     * At the moment it creates global NB libraries.
     * 
     */
    public static void setup(Workspace workspace, DotClassPathEntry entry) throws IOException {
        assert entry.getKind() == DotClassPathEntry.Kind.CONTAINER : entry;
        assert entry.getContainerMapping() != null : entry;
        
        String container = entry.getRawPath();
       
        // create eclipse user libraries in NetBeans:
        if (container.startsWith(USER_LIBRARY_CONTAINER)) {
            String library = getNetBeansLibraryName(container);
            LibraryManager lm = LibraryManager.getDefault();
            if (lm.getLibrary(library) != null) {
                return;
            }
            Map<String,List<URL>> content = new HashMap<String,List<URL>>();
            content.put("classpath", workspace.getJarsForUserLibrary(container.substring(USER_LIBRARY_CONTAINER.length())));
            lm.createLibrary("j2se", library, content);
        }
    }
        
}
