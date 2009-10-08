/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.wizards;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.wizards.Utilities;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

/**
 *
 * @author alexeybutenko
 */
public final class CompositeComponentWizardIterator implements TemplateWizard.Iterator {

    private int index;
    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;
    private String selectedText;
    private static final String RESOURCES_FOLDER = "resources";  //NOI18N
    private static final String COMPONENT_FOLDER = "ezcomp";  //NOI18N


    public Set<DataObject> instantiate(TemplateWizard wiz) throws IOException {
        DataObject result = null;
        String targetName = Templates.getTargetName(wizard);
        FileObject targetDir = Templates.getTargetFolder(wizard);
        DataFolder df = DataFolder.findFolder(targetDir);

        FileObject template = Templates.getTemplate( wizard );
        DataObject dTemplate = DataObject.find(template);
        HashMap<String, String> templateProperties = new HashMap<String, String>();
        if (selectedText != null) {
            templateProperties.put("implementation", selectedText);   //NOI18N
        }

        result  = dTemplate.createFromTemplate(df,targetName,templateProperties);
        return Collections.singleton(result);
    }



    public void initialize(TemplateWizard wizard) {
        this.wizard = wizard;
        selectedText = (String) wizard.getProperty("selectedText");

        Project project = Templates.getProject( wizard );
        Sources sources = project.getLookup().lookup(org.netbeans.api.project.Sources.class);

        SourceGroup[] sourceGroups = sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);

        WizardDescriptor.Panel folderPanel;
        if (sourceGroups == null || sourceGroups.length == 0) {
            sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        }

        FileObject targetFolder = null;
        FileObject resourceFolder = sourceGroups[0].getRootFolder().getFileObject(RESOURCES_FOLDER);
        if (resourceFolder != null) {
            FileObject componentFolder = resourceFolder.getFileObject(COMPONENT_FOLDER);
            if (componentFolder !=null) {
                targetFolder = componentFolder;
            } else {
                targetFolder = resourceFolder;
            }
        }

        if (targetFolder !=null) {
            Templates.setTargetFolder(wizard, targetFolder);
        }
        folderPanel = new CompositeComponentWizardPanel(wizard, sourceGroups, selectedText);

        panels = new WizardDescriptor.Panel[] { folderPanel };

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
}

    public void uninitialize(TemplateWizard wizard) {
        panels = null;
    }

    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    public String name() {
        return index + 1 + ". from " + panels.length;
    }

    public boolean hasNext() {
        return index < panels.length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }

    // You could safely ignore this method. Is is here to keep steps which were
    // there before this wizard was instantiated. It should be better handled
    // by NetBeans Wizard API itself rather than needed to be implemented by a
    // client code.
    private String[] createSteps() {
        String[] beforeSteps = null;
        Object prop = wizard.getProperty("WizardPanel_contentData");
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }

        if (beforeSteps == null) {
            beforeSteps = new String[0];
        }

        String[] res = new String[(beforeSteps.length - 1) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (beforeSteps.length - 1)) {
                res[i] = beforeSteps[i];
            } else {
                res[i] = panels[i - beforeSteps.length + 1].getComponent().getName();
            }
        }
        return res;
    }

}
