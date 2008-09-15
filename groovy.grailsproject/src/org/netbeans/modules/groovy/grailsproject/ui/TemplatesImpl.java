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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grailsproject.ui;

import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.SourceCategory;
import org.netbeans.modules.groovy.grailsproject.ui.wizards.GrailsArtifacts;

/**
 *
 * @author schmidtm
 * @author Martin Adamek
 */
public class TemplatesImpl implements PrivilegedTemplates  , RecommendedTemplates  {

    public static final String GROOVY_CLASS = "Templates/Groovy/GroovyClass.groovy";
    public static final String GROOVY_SCRIPT = "Templates/Groovy/GroovyScript.groovy";
    public static final String GSP = "Templates/Groovy/_view.gsp";
    
    public static final String DOMAIN_CLASS = "Templates/Groovy/DomainClass.groovy";
    public static final String CONTROLLER = "Templates/Groovy/Controller.groovy";
    public static final String INTEGRATION_TEST = "Templates/Groovy/IntegrationTest.groovy";
    public static final String GANT_SCRIPT = "Templates/Groovy/GantScript.groovy";
    public static final String SERVICE = "Templates/Groovy/Service.groovy";
    public static final String TAG_LIB = "Templates/Groovy/TagLib.groovy";
    public static final String UNIT_TEST = "Templates/Groovy/UnitTest.groovy";

    private static final String FOLDER = "Templates/Other/Folder";
    private static final String PROPERTIES = "Templates/Other/properties.properties";
    private static final String SIMPLE_FILES = "simple-files";
    
    private final SourceGroup sourceGroup;
    private final GrailsProject project;

    // this constructor is to keep track which Node displays
    // which SourceGroup to provide different "New File..." templates.

    public TemplatesImpl(GrailsProject project, SourceGroup sourceGroup) {
        this.project = project;
        this.sourceGroup = sourceGroup;
    }

    public String[] getPrivilegedTemplates() {
        SourceCategory sourceCategory = GrailsArtifacts.getCategoryForFolder(
                project.getProjectDirectory(), sourceGroup.getRootFolder());
        if (sourceCategory != null) {
            switch (sourceCategory) {
                case GRAILSAPP_CONF:
                    return new String[] { GROOVY_CLASS };
                case SCRIPTS:
                    return new String[] { GANT_SCRIPT, GROOVY_SCRIPT };
                case GRAILSAPP_DOMAIN:
                    return new String[] { DOMAIN_CLASS, GROOVY_CLASS };
                case GRAILSAPP_CONTROLLERS:
                    return new String[] { CONTROLLER, GROOVY_CLASS };
                case GRAILSAPP_TAGLIB:
                    return new String[] { TAG_LIB, GROOVY_CLASS };
                case TEST_INTEGRATION:
                    return new String[] { INTEGRATION_TEST, GROOVY_CLASS };
                case TEST_UNIT:
                    return new String[] { UNIT_TEST, GROOVY_CLASS };
                case GRAILSAPP_SERVICES:
                    return new String[] { SERVICE, GROOVY_CLASS };
                case GRAILSAPP_UTILS:
                case SRC_GROOVY:
                    return new String[] { GROOVY_CLASS, GROOVY_SCRIPT };
                case GRAILSAPP_VIEWS:
                case WEBAPP:
                    return new String[] { GSP, FOLDER };
                case GRAILSAPP_I18N:
                    return new String[] { PROPERTIES };
                case SRC_JAVA:
                    return new String[] {
                        "Templates/Classes/Class.java",
                        "Templates/Classes/Interface.java",
                        "Templates/Classes/Enum.java",
                        "Templates/Classes/AnnotationType.java",
                        "Templates/Classes/Exception.java",
                        "Templates/Classes/Package.java",
                    };
            }
        }
        return new String[] {};
    }

    public String[] getRecommendedTypes() {
        return new String[] { SIMPLE_FILES };
    }
    
}
