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

    public static String getWizardTitle(SourceCategory SourceCategory) {
        switch (SourceCategory) {
            case GRAILSAPP_CONTROLLERS: return NbBundle.getMessage(NewArtifactWizardIterator.class,"WIZARD_TITLE_CONTROLLERS");
            case GRAILSAPP_DOMAIN: return NbBundle.getMessage(NewArtifactWizardIterator.class,"WIZARD_TITLE_DOMAIN");
            case GRAILSAPP_SERVICES: return NbBundle.getMessage(NewArtifactWizardIterator.class,"WIZARD_TITLE_SERVICES");
            case GRAILSAPP_VIEWS: return NbBundle.getMessage(NewArtifactWizardIterator.class,"WIZARD_TITLE_VIEWS");
            case GRAILSAPP_TAGLIB: return NbBundle.getMessage(NewArtifactWizardIterator.class,"WIZARD_TITLE_TAGLIB");
            case SCRIPTS: return NbBundle.getMessage(NewArtifactWizardIterator.class,"WIZARD_TITLE_SCRIPTS");
        }
        return null;
    }

    public static SourceCategory getCategoryForFolder(FileObject projectRoot, FileObject fileObject) {
        String dirName = null;
        if (projectRoot != null && fileObject.isFolder()) {
            dirName = FileUtil.getRelativePath(projectRoot, fileObject);
        }
        if (dirName == null) {
            return null;
        }
        if (SourceCategory.GRAILSAPP_CONF.getRelativePath().equals(dirName)) {
            return SourceCategory.GRAILSAPP_CONF;
        } else if (SourceCategory.GRAILSAPP_CONTROLLERS.getRelativePath().equals(dirName)) {
            return SourceCategory.GRAILSAPP_CONTROLLERS;
        } else if (SourceCategory.GRAILSAPP_DOMAIN.getRelativePath().equals(dirName)) {
            return SourceCategory.GRAILSAPP_DOMAIN;
        } else if (SourceCategory.TEST_INTEGRATION.getRelativePath().equals(dirName)) {
            return SourceCategory.TEST_INTEGRATION;
        } else if (SourceCategory.LIB.getRelativePath().equals(dirName)) {
            return SourceCategory.LIB;
        } else if (SourceCategory.GRAILSAPP_I18N.getRelativePath().equals(dirName)) {
            return SourceCategory.GRAILSAPP_I18N;
        } else if (SourceCategory.SCRIPTS.getRelativePath().equals(dirName)) {
            return SourceCategory.SCRIPTS;
        } else if (SourceCategory.GRAILSAPP_SERVICES.getRelativePath().equals(dirName)) {
            return SourceCategory.GRAILSAPP_SERVICES;
        } else if (SourceCategory.SRC_GROOVY.getRelativePath().equals(dirName)) {
            return SourceCategory.SRC_GROOVY;
        } else if (SourceCategory.SRC_JAVA.getRelativePath().equals(dirName)) {
            return SourceCategory.SRC_JAVA;
        } else if (SourceCategory.GRAILSAPP_TAGLIB.getRelativePath().equals(dirName)) {
            return SourceCategory.GRAILSAPP_TAGLIB;
        } else if (SourceCategory.TEST_UNIT.getRelativePath().equals(dirName)) {
            return SourceCategory.TEST_UNIT;
        } else if (SourceCategory.GRAILSAPP_UTILS.getRelativePath().equals(dirName)) {
            return SourceCategory.GRAILSAPP_UTILS;
        } else if (SourceCategory.GRAILSAPP_VIEWS.getRelativePath().equals(dirName)) {
            return SourceCategory.GRAILSAPP_VIEWS;
        } else if (SourceCategory.WEBAPP.getRelativePath().equals(dirName)) {
            return SourceCategory.WEBAPP;
        }
        return null;
    }

    public static SourceCategory getCategoryForTemplate(FileObject template) {
        String templatePath = template.getPath();
        if (TemplatesImpl.CONTROLLER.equals(templatePath)) { // NOI18N
            return SourceCategory.GRAILSAPP_CONTROLLERS;
        } else if (TemplatesImpl.DOMAIN_CLASS.equals(templatePath)) {
            return SourceCategory.GRAILSAPP_DOMAIN;
        } else if (TemplatesImpl.GANT_SCRIPT.equals(templatePath)) {
            return SourceCategory.SCRIPTS;
        } else if (TemplatesImpl.GROOVY_CLASS.equals(templatePath)) {
            return SourceCategory.SRC_GROOVY;
        } else if (TemplatesImpl.GROOVY_SCRIPT.equals(templatePath)) {
            return SourceCategory.SCRIPTS;
        } else if (TemplatesImpl.GSP.equals(templatePath)) {
            return SourceCategory.GRAILSAPP_VIEWS;
        } else if (TemplatesImpl.INTEGRATION_TEST.equals(templatePath)) {
            return SourceCategory.TEST_INTEGRATION;
        } else if (TemplatesImpl.SERVICE.equals(templatePath)) {
            return SourceCategory.GRAILSAPP_SERVICES;
        } else if (TemplatesImpl.TAG_LIB.equals(templatePath)) {
            return SourceCategory.GRAILSAPP_TAGLIB;
        } else if (TemplatesImpl.UNIT_TEST.equals(templatePath)) {
            return SourceCategory.TEST_UNIT;
        }
        return null;
    }

}
