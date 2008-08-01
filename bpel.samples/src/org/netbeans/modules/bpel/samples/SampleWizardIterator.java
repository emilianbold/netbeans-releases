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
package org.netbeans.modules.bpel.samples;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.openide.filesystems.Repository;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.netbeans.spi.project.ui.templates.support.Templates;

public abstract class SampleWizardIterator implements WizardDescriptor.InstantiatingIterator {
    
    private static final long serialVersionUID = 1L;

    public static final String PROJECT_DIR = "projdir"; // NOI18N
    public static final String NAME = "name"; // NOI18N
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    private FileObject dir;
 
    protected WizardDescriptor.Panel[] createPanels() {
      return new WizardDescriptor.Panel[] { new SampleWizardPanel() };
    }
    
    protected String[] createSteps() {
      return new String[] { NbBundle.getMessage(SampleWizardIterator.class, "MSG_SampleProject")}; // NOI18N
    }

    protected FileObject getProjectDir() {
      return dir;
    }
    
    public Set<FileObject> instantiate() throws IOException {
      final Set<FileObject> resultSet = new LinkedHashSet<FileObject>();

      Repository.getDefault().getDefaultFileSystem().runAtomicAction(new org.openide.filesystems.FileSystem.AtomicAction() {
        public void run() throws IOException {
          File dirF = FileUtil.normalizeFile((File) wiz.getProperty(PROJECT_DIR));
          dirF.mkdirs();
          dir = FileUtil.toFileObject(dirF);
          FileObject template = Templates.getTemplate(wiz);
          String name = (String) wiz.getProperty(NAME);

          dir = dir.createFolder(name);
          dirF = FileUtil.toFile(dir);

          Util.unZipFile(template.getInputStream(), dir);
          Util.setProjectName(dir, Util.BPEL_PROJECT_CONFIGURATION_NAMESPACE, name, template.getName());
          resultSet.add(dir);
          FileObject dirParent = dir.getParent();

          resultSet.addAll(createCompositeApplicationProject(dirParent, name + "Application")); // NOI18N
        }});
        return resultSet;
    }
    
    protected abstract Set<FileObject> createCompositeApplicationProject(FileObject projectDir, String name) throws IOException;

    public void initialize(WizardDescriptor aWiz) {
        this.wiz = aWiz;
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

    public void uninitialize(WizardDescriptor aWiz) {
        this.wiz.putProperty(PROJECT_DIR, null);
        this.wiz.putProperty(NAME,null);
        this.wiz = null;
        panels = null;
    }
    
    public String name() {
      return MessageFormat.format("{0} of {1}", new Object[] {new Integer (index + 1), new Integer (panels.length)}); // NOI18N
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
    
    public void addChangeListener(ChangeListener l) {}
    public void removeChangeListener(ChangeListener l) {}
}
