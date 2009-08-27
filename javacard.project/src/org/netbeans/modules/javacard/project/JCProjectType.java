/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project;

import org.netbeans.api.project.Project;
import org.netbeans.modules.javacard.api.ProjectKind;
import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.util.Exceptions;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JCProjectType {

    public static final String JC_PROJECT_TYPE = 
            "org.netbeans.modules.javacard.JCPROJECT"; //NOI18N
    public static final String PROJECT_CONFIGURATION_NAME = "data"; //NOI18N

    public static final String PROJECT_CONFIGURATION_NAMESPACE_OLD =
            "http://www.netbeans.org/ns/javacard-project/2"; //NOI18N
    public static final String PROJECT_CONFIGURATION_NAMESPACE = 
            "http://www.netbeans.org/ns/javacard-project/3"; //NOI18N
    public static final String PRIVATE_CONFIGURATION_NAME = "data"; //NOI18N
    public static final String PRIVATE_CONFIGURATION_NAMESPACE = 
            "http://www.netbeans.org/ns/javacard-project-private/3"; //NOI18N
    public static final String MINIMUM_ANT_VERSION = "1.6"; //NOI18N

    @AntBasedProjectRegistration(type = JCProjectType.JC_PROJECT_TYPE,
    iconResource = "org/netbeans/modules/javacard/resources/extlibproject.png", // NOI18N
    sharedName = PROJECT_CONFIGURATION_NAME,
    sharedNamespace = PROJECT_CONFIGURATION_NAMESPACE,
    privateName = PRIVATE_CONFIGURATION_NAME,
    privateNamespace = PRIVATE_CONFIGURATION_NAMESPACE)
    public static Project createProjectByAnnotation(AntProjectHelper antHelper) {
        try {
            return createProject(antHelper);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    public static JCProject createProject(AntProjectHelper antHelper) throws IOException {
        ProjectKind kind = ProjectKind.kindForProject(antHelper);
        if (kind == null) {
            Logger.getLogger(JCProjectType.class.getName()).log(Level.INFO,
                    "Bad project.xml in project " + antHelper.getProjectDirectory().getPath(),
                    new NullPointerException("Null project kind")); //NOI18N
            return null;
        }
        switch (kind) {
            case WEB:
            case CLASSIC_APPLET:
            case EXTENDED_APPLET:
            case CLASSIC_LIBRARY:
            case EXTENSION_LIBRARY:
                return new JCProject(kind, antHelper);
            default:
                throw new AssertionError("Unknown kind " + kind); //NOI18N
        }
    }
}
