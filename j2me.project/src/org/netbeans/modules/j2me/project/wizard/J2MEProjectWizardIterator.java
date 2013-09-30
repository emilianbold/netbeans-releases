/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2me.project.wizard;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.templates.TemplateRegistration;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Roman Svitanic
 */
public class J2MEProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {
    
    public static enum WizardType { APPLICATION, EXISTING };
    
    static final String PROP_NAME_INDEX = "nameIndex"; //NOI18N
    
    private static final String MANIFEST_FILE = "manifest.mf"; //NOI18N
    private static final long serialVersionUID = 1L;
    private WizardType type;

    private J2MEProjectWizardIterator(WizardType type) {
        this.type = type;
    }
    
    @TemplateRegistration(folder="Project/JavaME", position=100, displayName="#template_app", iconBase="org/netbeans/modules/j2me/project/ui/resources/j2meProject.gif", description="../ui/resources/emptyProject.html")
    @NbBundle.Messages("template_app=Java ME Embedded Application")
    public static J2MEProjectWizardIterator application() {
        return new J2MEProjectWizardIterator(WizardType.APPLICATION);
    }

    @Override
    public Set<?> instantiate() throws IOException {
        assert false : "Cannot call this method if implements WizardDescriptor.ProgressInstantiatingIterator.";
        return null;
    }

    @Override
    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wiz = wizard;
        index = 0;
        panels = createPanels();
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
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }
        //set the default values of the sourceRoot and the testRoot properties
        this.wiz.putProperty("sourceRoot", new File[0]); //NOI18N
        this.wiz.putProperty("testRoot", new File[0]); //NOI18N
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        if (this.wiz != null) {
            this.wiz.putProperty("projdir", null); //NOI18N
            this.wiz.putProperty("name", null); //NOI18N
            this.wiz.putProperty("mainClass", null); //NOI18N
            switch (type) {
                case EXISTING:
                    this.wiz.putProperty("sourceRoot", null); //NOI18N
                    this.wiz.putProperty("testRoot", null); //NOI18N
            }
            this.wiz = null;
            panels = null;
        }
    }

    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    @Override
    public String name() {
        return NbBundle.getMessage(J2MEProjectWizardIterator.class, "LAB_IteratorName", index + 1, panels.length); //NOI18N
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public void addChangeListener(ChangeListener l) { }

    @Override
    public void removeChangeListener(ChangeListener l) { }

    private WizardDescriptor.Panel[] createPanels() {
        switch (type) {
            default:
                return new WizardDescriptor.Panel[]{
                    new PanelConfigureProject(type)
                };
        }
    }

    private String[] createSteps() {
        switch (type) {
            default:
                return new String[]{
                    NbBundle.getMessage(J2MEProjectWizardIterator.class, "LAB_ConfigureProject") // NOI18N
                };
        }
    }
}
