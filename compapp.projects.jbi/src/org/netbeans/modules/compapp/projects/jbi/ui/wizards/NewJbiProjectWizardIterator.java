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

package org.netbeans.modules.compapp.projects.jbi.ui.wizards;

import java.util.LinkedHashSet;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.compapp.projects.jbi.CasaHelper;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.netbeans.modules.compapp.projects.jbi.JbiProjectGenerator;

import org.netbeans.modules.compapp.projects.jbi.ui.JbiLogicalViewProvider;
import org.netbeans.modules.compapp.projects.jbi.ui.actions.OpenEditorAction;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.WizardDescriptor;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import java.awt.Component;

import java.io.File;
import java.io.IOException;

import java.text.MessageFormat;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;


/**
 * Wizard to create a new Web project.
 *
 * @author Jesse Glick
 */
public class NewJbiProjectWizardIterator implements WizardDescriptor.InstantiatingIterator {
    private static final long serialVersionUID = 1L;
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;

    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {new PanelConfigureProject(),//need this after EA1
        ///new PanelConfigureProjectApp(),
        };
    }

    private String[] createSteps() {
        return new String[] {
            NbBundle.getBundle("org/netbeans/modules/compapp/projects/jbi/ui/wizards/Bundle").getString( // NOI18N
                "LBL_NWP1_ProjectTitleName" // NOI18N
            ), 
        //need this after EA1
        ///NbBundle.getBundle("org/netbeans/modules/compapp/projects/jbiserver/ui/wizards/Bundle").getString("LBL_NWP1_ProjectAppName") // NOI18N
        };
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public Set instantiate() throws IOException {
                
        LinkedHashSet resultSet = new LinkedHashSet();
        File dirF = (File) wiz.getProperty(WizardProperties.PROJECT_DIR);
        String name = (String) wiz.getProperty(WizardProperties.NAME);
        String j2eeLevel = (String) wiz.getProperty(WizardProperties.J2EE_LEVEL);
        AntProjectHelper antHelper = null;
        antHelper = JbiProjectGenerator.createProject(dirF, name, j2eeLevel);
        
        FileObject dir = FileUtil.toFileObject(FileUtil.normalizeFile(dirF));
        
       // resultSet.add(dir);
        
        // find casa file and add to the open list.
        Project p = ProjectManager.getDefault().findProject(antHelper.getProjectDirectory());
        JbiProject jbiPrj = null;
        jbiPrj = p.getLookup().lookup(JbiProject.class);
        if (jbiPrj == null) {
            if (p instanceof JbiProject) {
                jbiPrj = (JbiProject) p;
            }
        }
        
        FileObject prjDir = null;
        if ( jbiPrj != null ) {
            prjDir = jbiPrj.getProjectDirectory();
            resultSet.add(prjDir);  // add the project dir so that project is selected and expanded.
        } 
        
        
        FileObject casaFO = null;
        if ( jbiPrj != null ) {
            // TODO: when Issue 127437 fixed, then remove use the below instead. 
            // casaFO = CasaHelper.getCasaFileObject(jbiPrj, true);
            // resultSet.add(casaFO);
            // need to explicitly open due to issue #127437
            (new OpenEditorAction()).perform(jbiPrj);
        }

        // Returning set of FileObject of project diretory. 
        // Project will be open and set as main
        
        return resultSet;
    }    

    /**
     * DOCUMENT ME!
     *
     * @param wiz DOCUMENT ME!
     */
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
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i)); // NOI18N

                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param wiz DOCUMENT ME!
     */
    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty(WizardProperties.PROJECT_DIR, null);
        this.wiz.putProperty(WizardProperties.NAME, null);
        this.wiz = null;
        panels = null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String name() {
        return MessageFormat.format(
            NbBundle.getBundle("org/netbeans/modules/compapp/projects/jbi/ui/wizards/Bundle").getString( // NOI18N
                "LBL_WizardStepsCount" // NOI18N
            ), 
            new String[] {
                (new Integer(index + 1)).toString(), (new Integer(panels.length)).toString()
            }
        ); 
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean hasNext() {
        return index < (panels.length - 1);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean hasPrevious() {
        return index > 0;
    }

    /**
     * DOCUMENT ME!
     */
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        index++;
    }

    /**
     * DOCUMENT ME!
     */
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }

        index--;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param l DOCUMENT ME!
     */
    public final void removeChangeListener(ChangeListener l) {
    }
}
