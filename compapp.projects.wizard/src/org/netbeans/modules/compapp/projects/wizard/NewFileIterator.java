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

package org.netbeans.modules.compapp.projects.wizard;

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;


/** 
 * Iterator useful for NewFileWizard. Implements WD.InstantiatingIterator,
 * rest of methods delegates to WD.ArrayIterator created only with SimpleTargetChooserPanel.
 *
 * @author  Tientien Li
 */
public class NewFileIterator implements WizardDescriptor.InstantiatingIterator {

    private static final long serialVersionUID = 1L;

    public static final String SOURCE_TYPE_BIZPRO = "BIZPRO";

    private transient boolean isLibrary;
    private transient WizardDescriptor.Iterator simpleIterator;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    private transient Project currentProject;

    private transient boolean isFolder;


    /** Create a new generic iterator. */
    private NewFileIterator (boolean isFolderIterator) {
        isFolder = isFolderIterator;
    }

    public static NewFileIterator genericFileIterator () {
        return new NewFileIterator (false);
    }

    public static NewFileIterator folderIterator () {
        return new NewFileIterator (true);
    }

    public static NewFileIterator emptyFileIterator () {
        return new NewFileIterator (false);
    }

    private WizardDescriptor.Iterator getSimpleIterator () {
        if (simpleIterator == null) {
            assert panels != null && panels.length > 0;
            simpleIterator = new WizardDescriptor.ArrayIterator (panels);
        }
        return simpleIterator;
    }

    private WizardDescriptor.Panel[] getPanels (WizardDescriptor wizardDescriptor) {
        Project project = Templates.getProject( wizardDescriptor );
        assert project != null : wizardDescriptor;
        if (!project.equals (currentProject) || panels == null) {
            currentProject = project;
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] groups = sources.getSourceGroups(NewFileIterator.SOURCE_TYPE_BIZPRO /* Sources.TYPE_GENERIC */);
            if ((groups == null) || (groups.length < 1)) {
                groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            }

            if (isFolder) {
                panels = new WizardDescriptor.Panel[] {
                    new SimpleTargetChooserPanel (project, groups, null, true)
                };
            } else {
                panels = new WizardDescriptor.Panel[] {
                    new SimpleTargetChooserPanel (project, groups, null, false)
                    // Templates.createSimpleTargetChooser (project, sources.getSourceGroups (Sources.TYPE_GENERIC))
                };
            }
        }
        return panels;
    }

    private String[] createSteps (String[] before) {
        assert panels != null;

        if (before == null) {
            before = new String[0];
        }

        String[] res = new String[ (before.length - 1) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - 1)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + 1].getComponent ().getName ();
            }
        }
        return res;
    }

    public Set/*<FileObject>*/ instantiate () throws IOException {
        FileObject dir = Templates.getTargetFolder( wiz );

        DataFolder df = DataFolder.findFolder( dir );
        FileObject template = Templates.getTemplate( wiz );

        DataObject dTemplate = DataObject.find( template );
        DataObject dobj = dTemplate.createFromTemplate( df, Templates.getTargetName( wiz )  );

        return Collections.singleton (dobj.getPrimaryFile ());
    }

    public void initialize(WizardDescriptor wiz) {
        panels = getPanels (wiz);
        this.wiz = wiz;

        // Make sure list of steps is accurate.
        String[] beforeSteps = null;
        Object prop = wiz.getProperty ("WizardPanel_contentData"); // NOI18N
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = createSteps (beforeSteps);
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
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }
    }

    public void uninitialize (WizardDescriptor wiz) {
        this.simpleIterator = null;
        this.wiz = null;
        panels = null;
    }

    public String name() {
        return getSimpleIterator ().name ();
    }

    public boolean hasNext() {
        return getSimpleIterator ().hasNext ();
    }
    public boolean hasPrevious() {
        return getSimpleIterator ().hasPrevious ();
    }
    public void nextPanel() {
        getSimpleIterator ().nextPanel ();
    }
    public void previousPanel() {
        getSimpleIterator ().previousPanel ();
    }
    public WizardDescriptor.Panel current() {
        return getSimpleIterator ().current ();
    }
    public final void addChangeListener(ChangeListener l) {
        getSimpleIterator ().addChangeListener (l);
    }
    public final void removeChangeListener(ChangeListener l) {
        getSimpleIterator ().removeChangeListener (l);
    }
}
