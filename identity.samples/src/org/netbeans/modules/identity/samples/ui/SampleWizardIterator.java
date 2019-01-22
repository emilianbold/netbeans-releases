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

package org.netbeans.modules.identity.samples.ui;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ui.templates.support.Templates;

import org.netbeans.modules.identity.samples.ui.SampleWizardPanel;
import org.netbeans.modules.identity.samples.util.Log;
import org.netbeans.modules.identity.samples.util.SoaSampleProjectProperties;
import org.netbeans.modules.identity.samples.util.SoaSampleUtils;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * 
 * @version 22 January 2006
 */
public class SampleWizardIterator  implements WizardDescriptor.InstantiatingIterator {
    
    private static final long serialVersionUID = 1L;

    public static final String PROJDIR = "projdir"; // NOI18N
    public static final String NAME = "name"; // NOI18N
    private Project myProject;
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    public transient WizardDescriptor wiz;
    private String projectConfigNamespace = SoaSampleProjectProperties.WEB_PROJECT_CONFIGURATION_NAMESPACE;
 
    public SampleWizardIterator() {}
        
    public static SampleWizardIterator createIterator() {
        return new SampleWizardIterator();
    }

    protected WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new SampleWizardPanel(),
        };
    }
    
    protected String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(SampleWizardIterator.class, "MSG_SampleProject"),
        };
    }
    
    public Project getProject() {
        return myProject;
    }
    
    public void setProjectConfigNamespace(String namespace) {
        this.projectConfigNamespace = namespace;
    }
    
    public Set/*<FileObject>*/ instantiate() throws IOException {
        Set resultSet = new LinkedHashSet();
        File dirF = FileUtil.normalizeFile((File) wiz.getProperty(PROJDIR));
        dirF.mkdirs();
        
        String name = (String) wiz.getProperty(NAME);
        FileObject template = Templates.getTemplate(wiz);

        FileObject dir = FileUtil.toFileObject(dirF);
        
        // All projects associated with the sample are created within a 
        // top level directory.       
        dir = dir.createFolder(name);
        dirF = FileUtil.toFile(dir);
        
        SoaSampleUtils.unZipFile(template.getInputStream(), dir);
        
        if (projectConfigNamespace != null)
            SoaSampleUtils.setProjectName(dir, projectConfigNamespace, name);
 
        Project p = ProjectManager.getDefault().findProject(dir);
        myProject = p;
        
        String appLocation = SoaSampleUtils.getDefaultSunAppLocation();
        if (appLocation != null)
            SoaSampleUtils.setPrivateProperty(dir, SoaSampleProjectProperties.APPSERVER_RT_REF, 
                    appLocation + "\\lib\\appserv-rt.jar");
        
        Log.out("created Sample Identity Project"); // NOI18N
        // Always open top dir as a project:
        resultSet.add(dir);
        // Look for nested projects to open as well:
        Enumeration e = dir.getFolders(true);
        while (e.hasMoreElements()) {
            FileObject subfolder = (FileObject) e.nextElement();
            if (ProjectManager.getDefault().isProject(subfolder)) {
                resultSet.add(subfolder);
            }
        }

        File parent = dirF.getParentFile();
        if (parent != null && parent.exists()) {
            ProjectChooser.setProjectsFolder(parent);
        }

        return resultSet;
    }
    
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            }
        }
    }

    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty(PROJDIR,null);
        this.wiz.putProperty(NAME,null);
        this.wiz = null;
        panels = null;
    }
    
    public String name() {
        return MessageFormat.format("{0} of {1}", //NOI18N
            new Object[] {new Integer (index + 1), new Integer (panels.length)});
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
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public void addChangeListener(ChangeListener l) {}
    public void removeChangeListener(ChangeListener l) {}
    
        
}
