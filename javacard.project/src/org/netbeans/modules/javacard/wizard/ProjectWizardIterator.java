/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.javacard.wizard;

import com.sun.javacard.AID;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.javacard.common.Utils;
import org.netbeans.modules.javacard.constants.ProjectPropertyNames;
import org.netbeans.modules.javacard.constants.ProjectTemplateWizardKeys;
import org.netbeans.modules.javacard.constants.ProjectWizardKeys;
import org.netbeans.modules.javacard.spi.ProjectKind;
import org.netbeans.modules.projecttemplates.ProjectCreator;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 *
 * @author Tim Boudreau
 */
public class ProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor>, ChangeListener {

    private final ChangeSupport supp = new ChangeSupport(this);
    private final FileObject template;
    private final ProjectKind kind;
    private PlatformInstallerWizardPanel firstPanel;
    private ProjectDefinitionWizardPanel secondPanel;
    private ClassicPackageWizardPanel thirdPanel;
    private WizardDescriptor wiz;

    public static ProjectWizardIterator create(FileObject template) {
        return new ProjectWizardIterator(template);
    }

    private ProjectWizardIterator(FileObject template) {
        this.template = template;
        kind = ProjectKind.kindForTemplate(template);
    }

    @Override
    public Set instantiate(ProgressHandle h) throws IOException {
        Set<Object> results = new HashSet<Object>();
        String name = (String) wiz.getProperty(ProjectWizardKeys.WIZARD_PROP_PROJECT_NAME);
        String pkg = (String) wiz.getProperty(ProjectWizardKeys.WIZARD_PROP_BASE_PACKAGE_NAME);
        String mainClassName = (String) wiz.getProperty(ProjectWizardKeys.WIZARD_PROP_MAIN_CLASS_NAME);
        String appletAid = (String) wiz.getProperty(ProjectWizardKeys.WIZARD_PROP_APPLET_AID);
        String webContextPath = (String) wiz.getProperty(ProjectWizardKeys.WIZARD_PROP_WEB_CONTEXT_PATH);
        String activePlatform = (String) wiz.getProperty(ProjectPropertyNames.PROJECT_PROP_ACTIVE_PLATFORM);
        String activeDevice = (String) wiz.getProperty(ProjectPropertyNames.PROJECT_PROP_ACTIVE_DEVICE);
        String usePreprocessor = (String) wiz.getProperty(ProjectPropertyNames.PROJECT_PROP_USE_PREPROCESSOR);

        String nameSpaces = unbicapitalize(name);

        //XXX fix in ProjectDefinitionPanel
        File file = (File) wiz.getProperty("projdir"); //NOI18N
        FileObject dest = FileUtil.toFileObject(FileUtil.normalizeFile(file.getParentFile()));
        ProjectCreator gen = new ProjectCreator(dest);
        gen.add (new ProjectXmlCreator(name, ProjectKind.kindForTemplate(template)));

        Map<String, String> templateProperties = new HashMap<String, String>();

        Map<String, Object> propsFromWizard = wiz.getProperties();
        for (Map.Entry<String, Object> e : propsFromWizard.entrySet()) {
            if (e.getValue() instanceof String) {
                propsFromWizard.put(e.getKey(), e.getValue());
            }
        }

        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROJECT_NAME_SPACES, nameSpaces);
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_PACKAGE, pkg);
        String pkgSlashes = pkg.replace('.', '/'); //NOI18N
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_PACKAGE_PATH, pkgSlashes);
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_CLASSNAME, mainClassName);
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_CLASSNAME_LOWERCASE, mainClassName.toLowerCase());
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_PROJECT_NAME, name);
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_CLASSPATH, ""); //NOI18N
        templateProperties.put(ProjectWizardKeys.WIZARD_PROP_APPLET_AID, appletAid);
        if (appletAid != null) {
            String aidAsHex = Utils.getAIDStringForScript(appletAid);
            templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_APPLET_AID_HEX, aidAsHex.toUpperCase());
        }
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_FILE_SEPARATOR, File.separator);
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_APPLET_MANIFEST_TYPE, kind.getManifestApplicationType());
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_KIND, kind.name());
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_ACTIVE_DEVICE, activeDevice);
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_ACTIVE_PLATFORM, activePlatform);
        templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_USE_PREPROCESSOR, usePreprocessor);

        if (kind == ProjectKind.CLASSIC_APPLET || kind == ProjectKind.CLASSIC_LIBRARY) {
            String specifiedPkgAid = (String) wiz.getProperty(ProjectWizardKeys.WIZARD_PROP_CLASSIC_PACKAGE_AID);
            AID packageAid;
            if (specifiedPkgAid != null) {
                packageAid = AID.parse(specifiedPkgAid);
            } else if (pkg != null) {
                packageAid = Utils.generatePackageAid(pkg);
            } else {
                packageAid = Utils.generateRandomPackageAid();
            }
            templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_CLASSIC_PACKAGE_AID,
                    packageAid.toString());
        }

        if (appletAid != null) {
            templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_APPLET_AID, (String) wiz.getProperty(ProjectWizardKeys.WIZARD_PROP_APPLET_AID));
            AID instanceAid = AID.parse(appletAid).increment();
            templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_INSTANCE_AID, instanceAid.toString());
            String aidAsHexWithLength = Utils.getAIDStringWithLengthForScript(instanceAid.toString());
            templateProperties.put(ProjectTemplateWizardKeys.PROJECT_TEMPLATE_PROP_APPLET_AID_HEX_WITH_LENGTH, aidAsHexWithLength.toUpperCase());
        }
        if (webContextPath != null) {
            templateProperties.put(ProjectPropertyNames.PROJECT_PROP_WEB_CONTEXT_PATH,
                    webContextPath);
        }
        String servletMapping = (String) wiz.getProperty(ProjectWizardKeys.WIZARD_PROP_SERVLET_MAPPING);
        if (servletMapping != null) {
            templateProperties.put(ProjectWizardKeys.WIZARD_PROP_SERVLET_MAPPING, servletMapping);
        }

        org.netbeans.modules.projecttemplates.GeneratedProject proj = gen.createProject(h, name, template, templateProperties);

        results.add(proj.projectDir);
        results.addAll(proj.filesToOpen);

        return results;
    }


    public static String unbicapitalize(String s) {
        char[] chars = s.toCharArray();
        StringBuilder sb = new StringBuilder();
        boolean lastWasUpperCase = true;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            boolean isUpperCase = i == 0 || Character.isUpperCase(c);
            if (isUpperCase != lastWasUpperCase && isUpperCase) {
                sb.append(' '); //NOI18N
            }
            sb.append(c);
            lastWasUpperCase = isUpperCase;
        }
        return sb.toString();
    }

    @Override
    public Set instantiate() throws IOException {
        throw new UnsupportedOperationException("Not supported."); //NOI18N
    }

    @Override
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        firstPanel = new PlatformInstallerWizardPanel();
        firstPanel.addChangeListener(this);
        secondPanel = new ProjectDefinitionWizardPanel(kind);
        secondPanel.addChangeListener(this);
        thirdPanel = new ClassicPackageWizardPanel(kind);
        thirdPanel.addChangeListener(this);
        secondPanel.readSettings(wiz);
    }

    @Override
    public void uninitialize(WizardDescriptor wiz) {
        secondPanel.storeSettings(wiz);
        firstPanel.removeChangeListener(this);
        secondPanel.removeChangeListener(this);
        thirdPanel.removeChangeListener(this);
    }

    @Override
    public Panel<WizardDescriptor> current() {
        if (!firstPanel.isValid()){
            return firstPanel;
        }
        if (onSecondPanel) {
            return secondPanel;
        } else {
            return thirdPanel;
        }
    }

    @Override
    public String name() {
        return kind.getDisplayName();
    }

    boolean onSecondPanel = true;
    
    @Override
    public boolean hasNext() {
        return kind.isClassic() ? onSecondPanel : false;
    }

    @Override
    public boolean hasPrevious() {
        return kind.isClassic() ? !onSecondPanel : false;
    }

    @Override
    public void nextPanel() {
        if (kind.isClassic()) {
            onSecondPanel = !onSecondPanel;
        }
    }

    @Override
    public void previousPanel() {
        if (kind.isClassic()) {
            onSecondPanel = !onSecondPanel;
        }
    }

    @Override
    public void addChangeListener(ChangeListener c) {
        supp.addChangeListener(c);
    }

    @Override
    public void removeChangeListener(ChangeListener c) {
        supp.removeChangeListener(c);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        supp.fireChange();
    }
}
