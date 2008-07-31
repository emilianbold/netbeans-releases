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

package org.netbeans.modules.groovy.grailsproject.ui.wizards;

import org.netbeans.modules.groovy.grailsproject.SourceCategory;
import org.netbeans.modules.groovy.grailsproject.ui.TemplatesImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Adamek
 */
public class GrailsArtifacts {

    private static final String CONFIGURATION = "grails-app/conf";
    private static final String CONTROLLERS = "grails-app/controllers";
    private static final String DOMAIN = "grails-app/domain";
    private static final String MESSAGES = "grails-app/i18n";
    private static final String SERVICES = "grails-app/services";
    private static final String TAGLIB = "grails-app/taglib";
    private static final String UTIL = "grails-app/utils";
    private static final String VIEWS = "grails-app/views";
    private static final String INTEGRATION_TESTS = "test/integration";
    private static final String UNIT_TESTS = "test/unit";
    private static final String SCRIPTS = "scripts";
    private static final String SRC_JAVA = "src/java";
    private static final String SRC_GROOVY = "src/groovy";
    private static final String WEBAPP = "web-app";
    private static final String LIB = "lib";

    public static String getWizardTitle(SourceCategory sourceCategory) {
        switch (sourceCategory) {
            case CONTROLLERS: return NbBundle.getMessage(NewArtifactWizardIterator.class,"WIZARD_TITLE_CONTROLLERS");
            case DOMAIN: return NbBundle.getMessage(NewArtifactWizardIterator.class,"WIZARD_TITLE_DOMAIN");
            case SERVICES: return NbBundle.getMessage(NewArtifactWizardIterator.class,"WIZARD_TITLE_SERVICES");
            case VIEWS: return NbBundle.getMessage(NewArtifactWizardIterator.class,"WIZARD_TITLE_VIEWS");
            case TAGLIB: return NbBundle.getMessage(NewArtifactWizardIterator.class,"WIZARD_TITLE_TAGLIB");
            case SCRIPTS: return NbBundle.getMessage(NewArtifactWizardIterator.class,"WIZARD_TITLE_SCRIPTS");
        }
        return null;
    }

    public static SourceCategory getCategoryForFolder(FileObject projectRoot, FileObject fileObject) {
        String dirName = null; // NOI18N
        if (projectRoot != null && fileObject.isFolder()) {
            dirName = FileUtil.getRelativePath(projectRoot, fileObject);
        }
        if (dirName == null) {
            return SourceCategory.NONE;
        }
        if (CONFIGURATION.equals(dirName)) {
            return SourceCategory.CONFIGURATION;
        } else if (CONTROLLERS.equals(dirName)) {
            return SourceCategory.CONTROLLERS;
        } else if (DOMAIN.equals(dirName)) {
            return SourceCategory.DOMAIN;
        } else if (INTEGRATION_TESTS.equals(dirName)) {
            return SourceCategory.INTEGRATION_TESTS;
        } else if (LIB.equals(dirName)) {
            return SourceCategory.LIB;
        } else if (MESSAGES.equals(dirName)) {
            return SourceCategory.MESSAGES;
        } else if (SCRIPTS.equals(dirName)) {
            return SourceCategory.SCRIPTS;
        } else if (SERVICES.equals(dirName)) {
            return SourceCategory.SERVICES;
        } else if (SRC_GROOVY.equals(dirName)) {
            return SourceCategory.SRC_GROOVY;
        } else if (SRC_JAVA.equals(dirName)) {
            return SourceCategory.SRC_JAVA;
        } else if (TAGLIB.equals(dirName)) {
            return SourceCategory.TAGLIB;
        } else if (UNIT_TESTS.equals(dirName)) {
            return SourceCategory.UNIT_TESTS;
        } else if (UTIL.equals(dirName)) {
            return SourceCategory.UTIL;
        } else if (VIEWS.equals(dirName)) {
            return SourceCategory.VIEWS;
        } else if (WEBAPP.equals(dirName)) {
            return SourceCategory.WEBAPP;
        } else {
            return SourceCategory.NONE;
        }
    }

    public static SourceCategory getCategoryForTemplate(FileObject template) {
        String templatePath = template.getPath();
        if (TemplatesImpl.CONTROLLER.equals(templatePath)) { // NOI18N
            return SourceCategory.CONTROLLERS;
        } else if (TemplatesImpl.DOMAIN_CLASS.equals(templatePath)) {
            return SourceCategory.DOMAIN;
        } else if (TemplatesImpl.GANT_SCRIPT.equals(templatePath)) {
            return SourceCategory.SCRIPTS;
        } else if (TemplatesImpl.GROOVY_CLASS.equals(templatePath)) {
            return SourceCategory.SRC_GROOVY;
        } else if (TemplatesImpl.GROOVY_SCRIPT.equals(templatePath)) {
            return SourceCategory.SCRIPTS;
        } else if (TemplatesImpl.GSP.equals(templatePath)) {
            return SourceCategory.VIEWS;
        } else if (TemplatesImpl.INTEGRATION_TEST.equals(templatePath)) {
            return SourceCategory.INTEGRATION_TESTS;
        } else if (TemplatesImpl.SERVICE.equals(templatePath)) {
            return SourceCategory.SERVICES;
        } else if (TemplatesImpl.TAG_LIB.equals(templatePath)) {
            return SourceCategory.TAGLIB;
        } else if (TemplatesImpl.UNIT_TEST.equals(templatePath)) {
            return SourceCategory.UNIT_TESTS;
        }
        return SourceCategory.NONE;
    }

}
