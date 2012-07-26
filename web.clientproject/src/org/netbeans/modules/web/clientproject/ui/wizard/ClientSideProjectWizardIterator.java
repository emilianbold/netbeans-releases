/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject.ui.wizard;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.ClientSideProjectUtilities;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

@TemplateRegistration(folder = "Project/ClientSide", displayName = "#ClientSideProject_displayName", 
        description = "ClientSideProjectDescription.html", 
        iconBase = ClientSideProject.PROJECT_ICON )
@Messages({"ClientSideProject_displayName=Client Side JavaScript Application",
            "MSG_Progress1=Creating project"})
public class ClientSideProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {

    private int index;
    private WizardDescriptor.Panel[] panels;
    private WizardDescriptor wiz;
    
    private SiteTemplateWizardPanel sitesPanel;
    private JavaScriptLibrarySelectionPanel librariesPanel;

    public ClientSideProjectWizardIterator() {
    }

    public static ClientSideProjectWizardIterator createIterator() {
        return new ClientSideProjectWizardIterator();
    }

    private WizardDescriptor.Panel[] createPanels() {
        sitesPanel = new SiteTemplateWizardPanel();
        librariesPanel = new JavaScriptLibrarySelectionPanel();
        return new WizardDescriptor.Panel[]{
                    new ClientSideProjectWizardPanel(),
                    sitesPanel,
                    librariesPanel,
        };
    }

    private String[] createSteps() {
        return new String[]{
                    NbBundle.getMessage(ClientSideProjectWizardIterator.class, "LBL_CreateProjectStep"),
                    NbBundle.getMessage(ClientSideProjectWizardIterator.class, "LBL_ChooseSiteStep"),
                    NbBundle.getMessage(ClientSideProjectWizardIterator.class, "LBL_JavaScriptLibrarySelectionStep"),
                };
    }

    public Set instantiate(ProgressHandle handle) throws IOException {
        handle.start();
        handle.progress(Bundle.MSG_Progress1());
        Set<FileObject> resultSet = new LinkedHashSet<FileObject>();
        File dirF = FileUtil.normalizeFile((File) wiz.getProperty("projdir"));
        String name = (String) wiz.getProperty("name");
        dirF.mkdirs();
        FileObject dir = FileUtil.toFileObject(dirF);
        ClientSideProjectUtilities.setupProject(dir, name);
        // Always open top dir as a project:
        resultSet.add(dir);

        if (index >= 1) {
            sitesPanel.apply(dir, handle);
        }
        if (index >= 2) {
            librariesPanel.apply(dir, handle);
        }

        File parent = dirF.getParentFile();
        if (parent != null && parent.exists()) {
            ProjectChooser.setProjectsFolder(parent);
        }

        handle.finish();
        return resultSet;
    }

    @Override
    public Set instantiate() throws IOException {
        throw new UnsupportedOperationException("never implemented - use progress one");
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
                JComponent jc = (JComponent) c;
                // Step #.
                // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_*:
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps);
            }
        }
    }

    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty("projdir", null);
        this.wiz.putProperty("name", null);
        this.wiz = null;
        panels = null;
    }

    public String name() {
        return MessageFormat.format("{0} of {1}",
                new Object[]{new Integer(index + 1), new Integer(panels.length)});
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
        if (current()==sitesPanel) {
            librariesPanel.updateDefaults(sitesPanel.getSupportedLibraries());
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
    public final void addChangeListener(ChangeListener l) {
    }

    public final void removeChangeListener(ChangeListener l) {
    }

}
