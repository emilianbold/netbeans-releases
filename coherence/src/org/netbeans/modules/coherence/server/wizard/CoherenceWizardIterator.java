/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence.server.wizard;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.server.properties.InstanceProperties;
import org.netbeans.api.server.properties.InstancePropertiesManager;
import org.netbeans.modules.coherence.server.CoherenceInstance;
import org.netbeans.modules.coherence.server.CoherenceInstanceProvider;
import org.netbeans.modules.coherence.server.CoherenceProperties;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.NbBundle;

/**
 * {@code WizardDescriptor.InstantiatingIterator} for registering a new server instance.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class CoherenceWizardIterator implements WizardDescriptor.InstantiatingIterator {

    /**
     * Display name from Server wizard.
     */
    private static final String PROP_DISPLAY_NAME = "ServInstWizard_displayName"; // NOI18N

    private int index;
    private WizardDescriptor wizardDescriptor;
    private WizardDescriptor.Panel[] panels;
    private ServerLocationPanel basePropertiesPanel;
    private String coherenceClasspath;
    private String coherenceLocation;

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = createPanels();
        }
        return panels;
    }

    /**
     * Creates all panels in advance.
     * @return initialized {@code WizardDescriptor.Panel[]}
     */
    protected WizardDescriptor.Panel[] createPanels() {
        basePropertiesPanel = new ServerLocationPanel(this);

        return new WizardDescriptor.Panel[] { basePropertiesPanel };
    }

    @Override
    public Set instantiate() throws IOException {
        String displayName = (String) wizardDescriptor.getProperty(PROP_DISPLAY_NAME);

        // create and store new properties
        InstanceProperties instanceProperties = InstancePropertiesManager.getInstance().
                createProperties(CoherenceInstanceProvider.COHERENCE_INSTANCES_NS);
        instanceProperties.putString(CoherenceProperties.PROP_DISPLAY_NAME, displayName);
        instanceProperties.putString(CoherenceProperties.PROP_COHERENCE_LOCATION, getCoherenceLocation());
        instanceProperties.putString(CoherenceProperties.PROP_COHERENCE_CLASSPATH, getCoherenceClasspath());

        // create new persistent server instance
        CoherenceInstance instance = CoherenceInstance.createPersistent(instanceProperties);

        return Collections.singleton(instance.getServerInstance());
    }

    @Override
    public void initialize(WizardDescriptor wizardDescriptor) {
        this.wizardDescriptor = wizardDescriptor;

        for (int i = 0; i < this.getPanels().length; i++)
        {
            Object c = panels[i].getComponent();

            if (c instanceof JComponent)
            {
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(
                    WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i)); // NOI18N

                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            }
        }
    }

    private String[] steps = new String[] {
        NbBundle.getMessage(CoherenceWizardIterator.class, "LBL_CoherenceCommonProperties"),  // NOI18N
    };

    public String getCoherenceClasspath() {
        return coherenceClasspath;
    }

    public void setCoherenceClasspath(String coherenceClasspath) {
        this.coherenceClasspath = coherenceClasspath;
    }

    public String getCoherenceLocation() {
        return coherenceLocation;
    }

    public void setCoherenceLocation(String coherenceLocation) {
        this.coherenceLocation = coherenceLocation;
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
    }

    @Override
    public Panel current() {
        return getPanels()[index];
    }

    @Override
    public String name() {
        return index + 1 + ". from " + getPanels().length;
    }

    @Override
    public boolean hasNext() {
        return index < getPanels().length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        index++;
    }

    @Override
    public void previousPanel() {
        index--;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

}
