/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.newproject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.validation.adapters.WizardDescriptorAdapter;
import org.netbeans.modules.maven.api.archetype.Archetype;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 *@author mkleint
 */
public class MavenWizardIterator implements WizardDescriptor.BackgroundInstantiatingIterator<WizardDescriptor> {
    
    private static final long serialVersionUID = 1L;
    static final String PROPERTY_CUSTOM_CREATOR = "customCreator"; //NOI18N
    private transient int index;
    private transient List<WizardDescriptor.Panel<WizardDescriptor>> panels;
    private transient WizardDescriptor wiz;
    public static final String KEY_GROUP_ID = "groupId", KEY_ARTIFACT_ID = "artifactId", KEY_VERSION = "version", KEY_REPOSITORY = "repository";
    private final Archetype archetype;
    
    private MavenWizardIterator(Archetype archetype) {
        this.archetype = archetype;
    }

    /** Wizard iterator which prompts the user to select an archetype from several lists. */
    public static WizardDescriptor.Iterator<?> pickArchetype() {
        return new MavenWizardIterator(null);
    }

    /**
     * Wizard iterator using a predetermined archetype.
     * @param params list of keys among {@link #KEY_GROUP_ID}, {@link #KEY_ARTIFACT_ID}, {@link #KEY_VERSION}, optionally {@link #KEY_REPOSITORY}
     */
    public static WizardDescriptor.Iterator<?> definedArchetype(Map<String,String> params) {
        Archetype arch = new Archetype();
        arch.setGroupId(params.get(KEY_GROUP_ID));
        arch.setArtifactId(params.get(KEY_ARTIFACT_ID));
        arch.setVersion(params.get(KEY_VERSION));
        arch.setRepository(params.get(KEY_REPOSITORY)); // null OK
        return new MavenWizardIterator(arch);
    }
    
    public @Override Set<FileObject> instantiate() throws IOException {
        return ArchetypeWizardUtils.instantiate(wiz);
    }
    
    @Override
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        ValidationGroup vg = ValidationGroup.create(new WizardDescriptorAdapter(wiz));
        panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        List<String> steps = new ArrayList<String>();
        if (archetype == null) {
            panels.add(new ChooseWizardPanel());
            steps.add(NbBundle.getMessage(MavenWizardIterator.class, "LBL_CreateProjectStep"));
        }
        panels.add(new BasicWizardPanel(vg));
        steps.add(NbBundle.getMessage(MavenWizardIterator.class, "LBL_CreateProjectStep2"));
        for (int i = 0; i < panels.size(); i++) {
            JComponent c = (JComponent) panels.get(i).getComponent();
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
            c.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps.toArray(new String[0]));
      }
        if (archetype != null) {
            wiz.putProperty(ChooseArchetypePanel.PROP_ARCHETYPE, archetype);
        }
    }
    
    @Override
    public void uninitialize(WizardDescriptor wiz) {
        wiz.putProperty("projdir",null); //NOI18N
        wiz.putProperty("name",null); //NOI18N
        this.wiz = null;
        panels = null;
    }
    
    public @Override String name() {
        return NbBundle.getMessage(MavenWizardIterator.class, "NameFormat", index + 1, panels.size());
    }
    
    @Override
    public boolean hasNext() {
        return index < panels.size() - 1;
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
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels.get(index);
    }
    
    public @Override void addChangeListener(ChangeListener l) {}
    
    public @Override void removeChangeListener(ChangeListener l) {}

}
