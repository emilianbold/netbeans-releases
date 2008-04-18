/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.wizards;

import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.Utils;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 * Just as simple wrapper for the standard new file iterator as possible.
 * @author Tomas Mysik
 */
public final class NewFileWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {
    private static final long serialVersionUID = 2262026971167469147L;

    private WizardDescriptor wizard;
    private WizardDescriptor.Panel<WizardDescriptor> panel;

    public Set instantiate() throws IOException {
        DataFolder dataFolder = DataFolder.findFolder(Templates.getTargetFolder(wizard));
        DataObject dataTemplate = DataObject.find(Templates.getTemplate(wizard));
        DataObject createdFile = dataTemplate.createFromTemplate(dataFolder, Templates.getTargetName(wizard));

        return Collections.<FileObject>singleton(createdFile.getPrimaryFile());
    }

    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        if (Templates.getTargetFolder(wizard) == null) {
            Project project = Templates.getProject(wizard);
            assert project instanceof PhpProject;
            PhpProject phpProject = (PhpProject) project;
            FileObject srcDir = getFileObject(phpProject.getHelper(), PhpProjectProperties.SRC_DIR);
            if (srcDir != null) {
                Templates.setTargetFolder(wizard, srcDir);
            }
        }
    }

    public void uninitialize(WizardDescriptor wizard) {
        panel = null;
    }

    public Panel<WizardDescriptor> current() {
        return getPanel();
    }

    public String name() {
        return ""; // NOI18N
    }

    public boolean hasNext() {
        return false;
    }

    public boolean hasPrevious() {
        return false;
    }

    public void nextPanel() {
        throw new NoSuchElementException();
    }

    public void previousPanel() {
        throw new NoSuchElementException();
    }

    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }

    private WizardDescriptor.Panel<WizardDescriptor> getPanel() {
        if (panel == null) {
            Project p = Templates.getProject(wizard);
            final SourceGroup[] groups = Utils.getSourceGroups(p);
            panel = Templates.createSimpleTargetChooser(p, groups);
        }
        return panel;
    }

    private FileObject getFileObject(AntProjectHelper helper, String propname) {
        String prop = helper.getStandardPropertyEvaluator().getProperty(propname);
        if (prop != null) {
            return helper.resolveFileObject(prop);
        }
        return null;
    }
}
