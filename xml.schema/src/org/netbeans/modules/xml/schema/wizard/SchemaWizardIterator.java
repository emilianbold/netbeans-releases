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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.xml.schema.wizard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.xam.ui.ProjectConstants;
import org.openide.WizardDescriptor;
import org.openide.loaders.DataFolder;
import org.openide.loaders.TemplateWizard;
import org.netbeans.spi.project.ui.templates.support.Templates;


/**
 * Schema wizard iterator. This guy is responsible for showing appropriate GUI
 * panels to the user, collecting inputs from those panels and based on those
 * collected inputs, will instantiate schema from templates.
 *
 * See layer.xml for template declaration.
 *
 * Read http://performance.netbeans.org/howto/dialogs/wizard-panels.html.
 * 
 * @author  Samaresh (Samaresh.Panda@Sun.Com)
 */
public class SchemaWizardIterator extends AbstractSchemaWizardIterator {
        
    private transient WizardDescriptor.Panel schemaPanel;
    private SchemaAdditionalInfoGUI schemaGUI;
        
    /**
     * You should define what panels you want to use here:
     */
    protected WizardDescriptor.Panel[] createPanels (Project project,
						     final TemplateWizard wizard) {
        Sources sources = ProjectUtils.getSources(project);
	List<SourceGroup> roots = new ArrayList<SourceGroup>();
	SourceGroup[] javaRoots = 
	    sources.getSourceGroups(ProjectConstants.JAVA_SOURCES_TYPE);
	roots.addAll(Arrays.asList(javaRoots));
	if (roots.isEmpty()) {
	    SourceGroup[] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
	    roots.addAll(Arrays.asList(sourceGroups));
	}
        schemaPanel = new SchemaAdditionalInfoPanel();
	schemaGUI = (SchemaAdditionalInfoGUI) schemaPanel.getComponent();
	DataFolder folder = DataFolder.findFolder(roots.get(0).getRootFolder());
	DataFolder projectFolder = 
	    DataFolder.findFolder(project.getProjectDirectory());
	try {
	    if (wizard.getTargetFolder().equals(projectFolder)) {
		wizard.setTargetFolder(folder);
	    }
	} catch (IOException ioe) {
	    wizard.setTargetFolder(folder);
	}
        WizardDescriptor.Panel panel = 
            Templates.createSimpleTargetChooser(project, 
		roots.toArray(new SourceGroup[roots.size()]), schemaPanel);
	schemaGUI.setParentPanel(panel);
	return new WizardDescriptor.Panel[] {panel};
    }
        
    protected void fixTemplate(BaseDocument doc) {
        String tns = schemaGUI.getTargetNamespace();
        if(tns.length() == 0) {
            return;
        }       
        updateDocument(doc, tns);
    }
}
