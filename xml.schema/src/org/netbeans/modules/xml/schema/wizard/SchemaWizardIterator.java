/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
        if(tns.length() == 0) tns = SchemaAdditionalInfoGUI.DEFAULT_TARGET_NAMESPACE;
        replaceInDocument(doc, "#TARGET_NAMESPACE", tns); //NOI18N
    }        
}
