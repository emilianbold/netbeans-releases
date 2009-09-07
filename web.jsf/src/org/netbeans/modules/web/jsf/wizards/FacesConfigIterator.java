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

package org.netbeans.modules.web.jsf.wizards;

import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.JSFFrameworkProvider;
import org.netbeans.modules.web.jsf.JSFUtils;
import org.netbeans.modules.web.wizards.Utilities;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * A template wizard operator for new faces-config.xml
 *
 * @author Alexey Butenko
 */
public class FacesConfigIterator implements TemplateWizard.Iterator {
    private int index;
    private static final String defaultName = "faces-config";   //NOI18N
    private static final String FACES_CONFIG_PARAM = "javax.faces.CONFIG_FILES";    //NOI18N
    private static final String INIT_PARAM = "InitParam";  //NOI18N
    private static String RESOURCE_FOLDER = "org/netbeans/modules/web/jsf/resources/"; //NOI18N

    private transient WizardDescriptor.Panel[] panels;

    public Set<DataObject> instantiate(TemplateWizard wizard) throws IOException {
        DataObject result = null;
        boolean isDefaultLocation = false;

        Project project = Templates.getProject( wizard );
        String targetName = Templates.getTargetName(wizard);
        FileObject targetDir = Templates.getTargetFolder(wizard);
      
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm != null) {
            FileObject dir = wm.getDocumentBase();
            if (!JSFConfigUtilities.hasJsfFramework(dir)) {
                JSFConfigUtilities.extendJsfFramework(dir, false);
            }
            FileObject dd = wm.getDeploymentDescriptor();
            assert dd != null;
            WebApp ddRoot = DDProvider.getDefault().getDDRoot(dd);

            ClassPath classpath = ClassPath.getClassPath(wm.getDocumentBase(), ClassPath.COMPILE);
            boolean isJSF20 = classpath.findResource(JSFUtils.JSF_2_0__API_SPECIFIC_CLASS.replace('.', '/')+".class")!=null; //NOI18N
            String template_file="faces-config.xml"; //NOI18N
            if (ddRoot != null) {
                Profile profile = wm.getJ2eeProfile();
                if (profile.equals(Profile.JAVA_EE_5) || profile.equals(Profile.JAVA_EE_6_FULL) || profile.equals(Profile.JAVA_EE_6_WEB)) {
                    if (isJSF20) {
                        template_file = "faces-config_2_0.xml"; //NOI18N
                    } else {
                        template_file = "faces-config_1_2.xml"; //NOI18N
                    }
                }
            }
            String content = JSFFrameworkProvider.readResource(Thread.currentThread().getContextClassLoader().getResourceAsStream(RESOURCE_FOLDER+template_file), "UTF-8"); //NOI18N
            FileObject target = FileUtil.createData(targetDir, targetName+".xml"); //NOI18N
           JSFFrameworkProvider.createFile(target, content, "UTF-8"); //NOI18N
           result = DataObject.find(target);

            FileObject webInf = wm.getWebInf();
            isDefaultLocation = defaultName.equals(targetName) && targetDir == webInf;
            if (!isDefaultLocation) {
                try {
                    //Need to specify config file in javax.faces.FACES_CONFIG property
                    //First search existing param
                    InitParam[] parameters = ddRoot.getContextParam();
                    boolean found = false;
                    int i = 0;
                    for (InitParam param : parameters) {
                        if (param.getParamName().equals(FACES_CONFIG_PARAM)) {
                            found = true;
                            String value = param.getParamValue()+",\n            /"+FileUtil.getRelativePath(wm.getDocumentBase(), targetDir)+"/"+targetName+".xml";  //NOI18N
                            ddRoot.removeContextParam(param);
                            InitParam newParameter = (InitParam) ddRoot.createBean(INIT_PARAM);
                            newParameter.setParamName(FACES_CONFIG_PARAM);
                            newParameter.setParamValue(value);  //NOI18N
                            ddRoot.addContextParam(newParameter);
                            break;
                        }
                        i++;
                    }
                    if (!found) {
                        InitParam contextParam = (InitParam) ddRoot.createBean(INIT_PARAM);
                        contextParam.setParamName(FACES_CONFIG_PARAM);
                        contextParam.setParamValue("/"+FileUtil.getRelativePath(wm.getDocumentBase(), targetDir)+"/"+targetName+".xml");  //NOI18N
                        ddRoot.addContextParam(contextParam);
                    }
                    ddRoot.write(dd);

                } catch (ClassNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        if (result != null) {
            return Collections.singleton(result);
        }
        return Collections.EMPTY_SET;
    }

    public void initialize(TemplateWizard wizard) {
        // obtaining target folder
        Project project = Templates.getProject( wizard );

        Sources sources = project.getLookup().lookup(org.netbeans.api.project.Sources.class);

        SourceGroup[] sourceGroups = sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);

        WizardDescriptor.Panel folderPanel;
        if (sourceGroups == null || sourceGroups.length == 0) {
            sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        }

        folderPanel = Templates.createSimpleTargetChooser(project, sourceGroups);

        panels = new WizardDescriptor.Panel[] { folderPanel };

        // Creating steps.
        Object prop = wizard.getProperty (WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = Utilities.createSteps(beforeSteps, panels);

        for (int i = 0; i < panels.length; i++) {
            JComponent jc = (JComponent)panels[i].getComponent ();
            if (steps[i] == null) {
                steps[i] = jc.getName ();
            }
	    jc.putClientProperty (WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer (i)); // NOI18N
	    jc.putClientProperty (WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
	}
        
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm != null) {
            FileObject webInf = wm.getWebInf();
            FileObject targetFolder = Templates.getTargetFolder(wizard);
            String relativePath = (targetFolder == null) ? null : FileUtil.getRelativePath(webInf, targetFolder);
            if (relativePath == null) {
                Templates.setTargetFolder(wizard, webInf);
            }
        }
        Templates.setTargetName(wizard, defaultName);
    }

    public void uninitialize(TemplateWizard wiz) {
        panels = null;
    }

    public Panel<WizardDescriptor> current() {
        return panels[index];
    }

    public String name() {
        return NbBundle.getMessage(FacesConfigIterator.class, "TITLE_x_of_y",
                index + 1, panels.length);
    }

    public boolean hasNext() {
        return index < panels.length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        if (! hasNext ()) throw new NoSuchElementException ();
        index++;
    }

    public void previousPanel() {
        if (! hasPrevious ()) throw new NoSuchElementException ();
        index--;
    }

    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }

}
