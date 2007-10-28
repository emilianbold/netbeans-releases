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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.websvc.rest.projects;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.api.project.ant.AntBuildExtender.Extension;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.filesystems.FileObject;

/**
 *
 * @author nam
 */
public class AntFilesHelper {
    /**
     * IMPORTANT: bump up version when you change the dependencies list
     */
    public static final int CURRENT_DEPENDECIES_VERSION = 0;
    // dependency(from, to)
    public static Map<String,String> dependencies = new HashMap<String,String>();
    static {
        //IMPORTANT: bump up version when you change the dependencies list
        dependencies.put("-post-compile", "-rest-post-compile");
        //IMPORTANT: bump up version when you change the dependencies list
    }
    
    public static final String REST_ANT_EXT_NAME_BASE = "rest";
    public static final String REST_ANT_EXT_NAME = getExtensionVersionString(CURRENT_DEPENDECIES_VERSION);

    public static final String REST_BUILD_XSL = "org/netbeans/modules/websvc/rest/resources/rest-build.xsl";
    public static final String REST_BUILD_XML_PATH = "nbproject/rest-build.xml";

    private AntProjectHelper projectHelper;
    private Project project;
    private AntBuildExtender extender;
    
    public AntFilesHelper(RestSupport restSupport) {
        this(restSupport.getProject(), restSupport.getAntProjectHelper());
    }
    
    public AntFilesHelper(Project project, AntProjectHelper helper) {
        projectHelper = helper;
        this.project = project;
        extender = project.getLookup().lookup(AntBuildExtender.class);
        if (extender == null) {
            throw new IllegalArgumentException("Given project does not allow extension");
        }
    }
    
    public void initRestBuildExtension() throws IOException {
        boolean changed = refreshRestBuildXml();
        FileObject extensionXml = project.getProjectDirectory().getFileObject(REST_BUILD_XML_PATH);
        if (extensionXml != null) {
            Extension extension =  extender.getExtension(REST_ANT_EXT_NAME);
            if (extension == null) {
                extension = extender.addExtension(REST_ANT_EXT_NAME, extensionXml);
                for (Map.Entry<String,String> dependency : dependencies.entrySet()) {
                    extension.addDependency(dependency.getKey(), dependency.getValue());
                }
                changed = true;
            }
        }

        // check for cleanup of last version
        changed = cleanupLastExtensionVersions();
        if (changed) {
            ProjectManager.getDefault().saveProject(project);
        }
    }
    
    public boolean refreshRestBuildXml() throws IOException {
        URL xslURL = this.getClass().getClassLoader().getResource(REST_BUILD_XSL);
        GeneratedFilesHelper helper = new GeneratedFilesHelper(projectHelper);
        return helper.refreshBuildScript(REST_BUILD_XML_PATH, xslURL, true);
    }
            
    private static String getExtensionVersionString(int version) {
        return REST_ANT_EXT_NAME_BASE + "." + version;
    }
    
    public boolean cleanupLastExtensionVersions() {
        List<String> extensionNames = new ArrayList<String>();
        String lastVersion = REST_ANT_EXT_NAME_BASE;
        if (extender.getExtension(lastVersion) != null) {
            extensionNames.add(lastVersion);
        }
        
        for (int version = 0 ; version < CURRENT_DEPENDECIES_VERSION; version++) {
            lastVersion = getExtensionVersionString(version);
            if (extender.getExtension(lastVersion) != null) {
                extensionNames.add(lastVersion);
            }
        }

        for (String name : extensionNames) {
            extender.removeExtension(name);
        }
        return extensionNames.size() > 0;
    }
}