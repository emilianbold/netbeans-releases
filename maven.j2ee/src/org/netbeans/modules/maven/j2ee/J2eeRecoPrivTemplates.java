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

package org.netbeans.modules.maven.j2ee;
import java.util.ArrayList;
import java.util.Arrays;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;

/**
 * j2ee specific part of RecommendedTemplates and PrivilegedTemplates,
 * @author Milos Kleint
 */
public class J2eeRecoPrivTemplates implements RecommendedTemplates, PrivilegedTemplates {
    
    private final Project project;
    
    J2eeRecoPrivTemplates(Project proj) {
        project = proj;
    }
    
    private static final String[] EAR_TYPES = new String[] {
                "ear-types",                 // NOI18N
    };
    
    private static final String[] EAR_PRIVILEGED_NAMES = new String[] {
                "Templates/J2EE/ApplicationXml", //NOI18N
                "Templates/Other/Folder" //NOI18N
    };
    
    private static final String[] EJB_TYPES_4 = new String[] {
                "ejb-types",            // NOI18N
                "ejb-types_2_1",        // NOI18N
                "j2ee-14-types",        // NOI18N
                "ejb-types-server",     // NOI18N
//#134462                "web-services",         // NOI18N
                "j2ee-types",           // NOI18N
    };
    
    private static final String[] EJB_TYPES_5 = new String[] {
                "ejb-types",            // NOI18N
                "ejb-types_3_0",        // NOI18N
                "ejb-types-server",     // NOI18N
                "web-services",         // NOI18N
                "web-service-clients",  // NOI18N
                "j2ee-types",           // NOI18N
    };

    /**
     * Supported template categories for Java EE 6 projects (full?).
     */
    private static final String[] EJB_TYPES_6 = new String[] {
        "ejb-types",            // NOI18N
        "ejb-types-server",     // NOI18N
        "ejb-types_3_1",        // NOI18N
        "web-services",         // NOI18N
        "web-service-clients",  // NOI18N
        "wsdl",                 // NOI18N
        "j2ee-types"           // NOI18N
    };

    
    private static final String[] EJB_PRIVILEGED_NAMES_4 = new String[] {
        
                "Templates/J2EE/Session", // NOI18N
                "Templates/J2EE/Entity",  // NOI18N
                "Templates/J2EE/RelatedCMP", // NOI18N
                "Templates/J2EE/Message", //NOI18N
                "Templates/Classes/Class.java", //NOI18N
                "Templates/Classes/Package", //NOI18N
    };
    
    private static final String[] EJB_PRIVILEGED_NAMES_5 = new String[] {
        
                "Templates/J2EE/Session", // NOI18N
                "Templates/J2EE/Message", //NOI18N
                "Templates/Classes/Class.java",// NOI18N
                "Templates/Classes/Package",// NOI18N
                "Templates/Persistence/Entity.java",// NOI18N
                "Templates/Persistence/RelatedCMP",// NOI18N
                "Templates/WebServices/WebService",// NOI18N
                "Templates/WebServices/WebServiceClient"// NOI18N
    };

    private static final String[] EJB_PRIVILEGED_NAMES_6 = EJB_PRIVILEGED_NAMES_5;
    
    private static final String[] WEB_TYPES = new String[] {
                "servlet-types",        // NOI18N
                "web-types",            // NOI18N
                "web-types-server"      // NOI18N
    };

    private static final String[] WEB_TYPES_5 = new String[] {
                "servlet-types",        // NOI18N
                "web-types",            // NOI18N
                "web-types-server",     // NOI18N
                "web-services",         // NOI18N
                "web-service-clients",  // NOI18N
    };

    private static final String[] WEB_TYPES_6 = WEB_TYPES_5;

    private static final String[] WEB_TYPES_EJB = new String[] {
        "ejb-types",            // NOI18N
        "ejb-types-server",     // NOI18N
        "ejb-types_3_0",        // NOI18N
        "ejb-types_3_1"         // NOI18N
    };

    private static final String[] WEB_TYPES_EJB_LITE = new String[] {
        "ejb-types",            // NOI18N
        "ejb-types_3_0",        // NOI18N
        "ejb-types_3_1"         // NOI18N
    };

    
    private static final String[] WEB_PRIVILEGED_NAMES = new String[] {
                "Templates/JSP_Servlet/JSP.jsp",            // NOI18N
                "Templates/JSP_Servlet/Html.html",          // NOI18N
                "Templates/JSP_Servlet/Servlet.java",       // NOI18N
                "Templates/Classes/Class.java",             // NOI18N
                "Templates/Classes/Package",                // NOI18N
//                "Templates/WebServices/WebService",         // NOI18N
//                "Templates/WebServices/WebServiceClient",   // NOI18N
                "Templates/Other/Folder",                   // NOI18N
    };

    private static final String[] WEB_PRIVILEGED_NAMES_5 = new String[] {
        "Templates/JSP_Servlet/JSP.jsp",            // NOI18N
        "Templates/JSP_Servlet/Html.html",          // NOI18N
        "Templates/JSP_Servlet/Servlet.java",       // NOI18N
        "Templates/Classes/Class.java",             // NOI18N
        "Templates/Classes/Package",                // NOI18N
        "Templates/Persistence/Entity.java", // NOI18N
        "Templates/Persistence/RelatedCMP", // NOI18N
        "Templates/Persistence/JsfFromDB", // NOI18N
        "Templates/WebServices/WebService.java",    // NOI18N
//        "Templates/WebServices/WebServiceFromWSDL.java",    // NOI18N
        "Templates/WebServices/WebServiceClient",   // NOI18N
//        "Templates/WebServices/RestServicesFromEntities", // NOI18N
        "Templates/WebServices/RestServicesFromDatabase",  //NOI18N
        "Templates/Other/Folder"                   // NOI18N
    };

    private static final String[] WEB_PRIVILEGED_NAMES_6 = WEB_PRIVILEGED_NAMES_5;
    private static final String[] WEB_PRIVILEGED_NAMES_EE6_FULL = new String[] {
        "Templates/J2EE/Session", // NOI18N
        "Templates/J2EE/Message"  // NOI18N
    };

    private static final String[] WEB_PRIVILEGED_NAMES_EE6_WEB = new String[] {
        "Templates/J2EE/Session"  // NOI18N
    };

    
    public String[] getRecommendedTypes() {
        NbMavenProject watcher = project.getLookup().lookup(NbMavenProject.class);
        String packaging = watcher.getPackagingType();
        if (packaging == null) {
            packaging = NbMavenProject.TYPE_JAR;
        }
        packaging = packaging.trim();
        if (NbMavenProject.TYPE_EJB.equals(packaging)) {
            EjbJar jar = EjbJar.getEjbJar(project.getProjectDirectory());
            if (jar != null) {
                Profile p = jar.getJ2eeProfile();
                if (Profile.JAVA_EE_5.equals(p)) {
                    return EJB_TYPES_5;
                }
                if (Profile.JAVA_EE_6_FULL.equals(p)) {
                    return EJB_TYPES_6;
                }
            }
            return EJB_TYPES_4;
        }
        if (NbMavenProject.TYPE_EAR.equals(packaging)) {
            return EAR_TYPES;
        }
        if (NbMavenProject.TYPE_WAR.equals(packaging)) {
            WebModule web = WebModule.getWebModule(project.getProjectDirectory());
            if (web != null) {
                Profile p = web.getJ2eeProfile();
                if (Profile.JAVA_EE_5.equals(p)) {
                    return WEB_TYPES_5;
                }
                if (Profile.JAVA_EE_6_WEB.equals(p) || Profile.JAVA_EE_6_FULL.equals(p)) {
                    ArrayList<String> toRet = new ArrayList<String>(Arrays.asList(WEB_TYPES_6));
                    J2eeProjectCapabilities cap = J2eeProjectCapabilities.forProject(project);
                    if (cap.isEjb31Supported()) {
                        toRet.addAll(Arrays.asList(WEB_TYPES_EJB));
                    }
                    if (cap.isEjb31LiteSupported()) {
                        toRet.addAll(Arrays.asList(WEB_TYPES_EJB_LITE));
                    }
                    return toRet.toArray(new String[0]);
                }
            }
            return WEB_TYPES;
        }
        return new String[0];
    }
    
    public String[] getPrivilegedTemplates() {
        NbMavenProject watcher = project.getLookup().lookup(NbMavenProject.class);
        String packaging = watcher.getPackagingType();
        if (packaging == null) {
            packaging = NbMavenProject.TYPE_JAR;
        }
        packaging = packaging.trim();
        if (NbMavenProject.TYPE_EJB.equals(packaging)) {
            EjbJar jar = EjbJar.getEjbJar(project.getProjectDirectory());
            if (jar != null) {
                Profile p = jar.getJ2eeProfile();
                if (Profile.JAVA_EE_5.equals(p)) {
                    return EJB_PRIVILEGED_NAMES_5;
                }
                if (Profile.JAVA_EE_6_FULL.equals(p)) {
                    return EJB_PRIVILEGED_NAMES_6;
                }
            }
            return EJB_PRIVILEGED_NAMES_4;
        }
        if (NbMavenProject.TYPE_EAR.equals(packaging)) {
            return EAR_PRIVILEGED_NAMES;
        }
        if (NbMavenProject.TYPE_WAR.equals(packaging)) {
            WebModule web = WebModule.getWebModule(project.getProjectDirectory());
            if (web != null) {
                Profile p = web.getJ2eeProfile();
                if (Profile.JAVA_EE_5.equals(p)) {
                    return WEB_PRIVILEGED_NAMES_5;
                }
                if (Profile.JAVA_EE_6_WEB.equals(p) || Profile.JAVA_EE_6_FULL.equals(p)) {
                    ArrayList<String> toRet = new ArrayList<String>(Arrays.asList(WEB_PRIVILEGED_NAMES_6));
                    J2eeProjectCapabilities cap = J2eeProjectCapabilities.forProject(project);
                    if (cap.isEjb31Supported()) {
                        toRet.addAll(Arrays.asList(WEB_PRIVILEGED_NAMES_EE6_FULL));
                    }
                    if (cap.isEjb31LiteSupported()) {
                        toRet.addAll(Arrays.asList(WEB_PRIVILEGED_NAMES_EE6_WEB));
                    }
                    return toRet.toArray(new String[0]);
                }
            }
            return WEB_PRIVILEGED_NAMES;
        }
        
        return new String[0];
    }
}
