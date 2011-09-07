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

import java.awt.Component;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.validation.adapters.WizardDescriptorAdapter;
import org.netbeans.modules.maven.api.archetype.Archetype;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 *@author Dafe Simonek
 */
public class BasicEEWizardIterator implements WizardDescriptor.BackgroundInstantiatingIterator {
    
    private int index;
    private WizardDescriptor.Panel[] panels;
    private WizardDescriptor wiz;

    private Archetype[] archs;

    private BasicEEWizardIterator(Archetype[] archs) {
        this.archs = archs;
    }

    public static BasicEEWizardIterator createWebAppIterator() {
        return new BasicEEWizardIterator(ArchetypeWizardUtils.WEB_APP_ARCHS);
    }

    public static BasicEEWizardIterator createEJBIterator() {
        return new BasicEEWizardIterator(ArchetypeWizardUtils.EJB_ARCHS);
    }
    
    public static BasicEEWizardIterator createAppClientIterator() {
        return new BasicEEWizardIterator(ArchetypeWizardUtils.APPCLIENT_ARCHS);
    }
    
    static String[] eeLevels() {
        return new String[] {
            NbBundle.getMessage(BasicEEWizardIterator.class, "LBL_JEE6"),
            NbBundle.getMessage(BasicEEWizardIterator.class, "LBL_JEE5"),
            NbBundle.getMessage(BasicEEWizardIterator.class, "LBL_J2EE14")
        };
    }

    private WizardDescriptor.Panel[] createPanels(ValidationGroup vg) {
        return new WizardDescriptor.Panel[] {
            new BasicWizardPanel(vg, eeLevels(), archs, true, false)
        };
    }
    
    private String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(BasicEEWizardIterator.class, "LBL_CreateProjectStep2"),
        };
    }
    
    public Set/*<FileObject>*/ instantiate() throws IOException {
        return ArchetypeWizardUtils.instantiate(wiz);
    }
    
    public void initialize(WizardDescriptor wiz) {
        index = 0;
        ValidationGroup vg = ValidationGroup.create(new WizardDescriptorAdapter(wiz));

        panels = createPanels(vg);
        this.wiz = wiz;
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
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); //NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); //NOI18N
            }
        }
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        panels = null;
    }
    
    public String name() {
        return MessageFormat.format(NbBundle.getMessage(BasicEEWizardIterator.class, "NameFormat"),
                new Object[] {new Integer(index + 1), new Integer(panels.length)});
    }
    
    public boolean hasNext() {
        return false;
    }
    
    public boolean hasPrevious() {
        return false;
    }
    
    public void nextPanel() {
    }
    
    public void previousPanel() {
    }
    
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
    
}
