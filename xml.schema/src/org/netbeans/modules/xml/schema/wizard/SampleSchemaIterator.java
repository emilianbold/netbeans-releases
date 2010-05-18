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
 * Sample schema wizard iterator. See layer.xml for template declaration.
 *
 * @author  Samaresh (Samaresh.Panda@Sun.Com)
 */
public class SampleSchemaIterator extends AbstractSchemaWizardIterator {
    
    /**
     * You should define what panels you want to use here:
     */
    protected WizardDescriptor.Panel[] createPanels (Project project,
						     final TemplateWizard wizard) {
            DataFolder df = null;
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] folders = sources.getSourceGroups(ProjectConstants.JAVA_SOURCES_TYPE);
            if (folders == null || folders.length == 0) {
                folders = sources.getSourceGroups(Sources.TYPE_GENERIC);
            }
            try {
                df = wizard.getTargetFolder();
            } catch (IOException ex) {
                //just catch
            }
            if (df != null) {
                wizard.setTargetFolder(df);
                org.openide.WizardDescriptor.Panel panel = Templates.createSimpleTargetChooser(project, folders);
                return new org.openide.WizardDescriptor.Panel[]{panel};
            }

            //make the first one as the default target folder. IZ: 98643
            if (folders != null && folders.length != 0) {
                df = DataFolder.findFolder(folders[0].getRootFolder());
                wizard.setTargetFolder(df);
            }
            WizardDescriptor.Panel panel = Templates.createSimpleTargetChooser(project, folders);
            return new WizardDescriptor.Panel[] {panel};
    }
    
    protected void fixTemplate(BaseDocument doc) {
        //nothing to fix for samples
    }
}
