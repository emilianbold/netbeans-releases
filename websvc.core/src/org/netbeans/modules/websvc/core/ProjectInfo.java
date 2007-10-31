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

package org.netbeans.modules.websvc.core;

import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;

/**
 *
 * @author radko, mkuchtiak
 */
public class ProjectInfo {
    
    private Project project;
    private int projectType;
    
    public static final int JSE_PROJECT_TYPE = 0;
    public static final int WEB_PROJECT_TYPE = 1;
    public static final int EJB_PROJECT_TYPE = 2;
    public static final int CAR_PROJECT_TYPE = 3;
    
    
    private boolean jwsdpSupported = false;
    private boolean jsr109Supported = false;
    private boolean jsr109oldSupported = false;
    private boolean jaxWsInJ2ee14Supported = false;
    
    /** Creates a new instance of ProjectInfo */
    
    public ProjectInfo(Project project) {
        this.project=project;
        JAXWSSupport wss = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
        if (wss != null) {
            Map properties = wss.getAntProjectHelper().getStandardPropertyEvaluator().getProperties();
            String serverInstance = (String)properties.get("j2ee.server.instance"); //NOI18N
            if (serverInstance != null) {
                J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstance);
                if (j2eePlatform != null) {
                    jwsdpSupported = j2eePlatform.isToolSupported(J2eePlatform.TOOL_JWSDP);
                    jsr109Supported = j2eePlatform.isToolSupported(J2eePlatform.TOOL_JSR109);
                    jsr109oldSupported = j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSCOMPILE);
                    jaxWsInJ2ee14Supported = j2eePlatform.isToolSupported("JaxWs-in-j2ee14-supported");
                }
            }
        }
        
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        EjbJar em = EjbJar.getEjbJar(project.getProjectDirectory());
        Car car = Car.getCar(project.getProjectDirectory());
        if (em != null)
            projectType = EJB_PROJECT_TYPE;
        else if (wm != null)
            projectType = WEB_PROJECT_TYPE;
        else if (car != null)
            projectType = CAR_PROJECT_TYPE;
        else
            projectType = JSE_PROJECT_TYPE;
    }
    
    public int getProjectType() {
        return projectType;
    }
    
    public Project getProject() {
        return project;
    }
    
    public boolean isJwsdpSupported() {
        return jwsdpSupported;
    }
    public boolean isJsr109Supported() {
        return jsr109Supported;
    }
    public boolean isJsr109oldSupported() {
        return jsr109oldSupported;
    }
    public boolean isJaxWsInJ2ee14Supported() {
        return jaxWsInJ2ee14Supported;
    }
}

