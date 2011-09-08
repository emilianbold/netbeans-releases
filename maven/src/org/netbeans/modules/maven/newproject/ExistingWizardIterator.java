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
import java.util.Collections;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import static org.netbeans.modules.maven.newproject.Bundle.*;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 *
 *@author mkleint
 */
public class ExistingWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {
    
    private static final long serialVersionUID = 1L;
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    
    private ExistingWizardIterator() {}
    
    public static ExistingWizardIterator createIterator() {
        return new ExistingWizardIterator();
    }
    
    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new UseOpenWizardPanel()
        };
    }
    
    @Messages("LBL_UseOpenStep=Existing Project")
    private String[] createSteps() {
        return new String[] {
            LBL_UseOpenStep(),
        };
    }
    
    public Set/*<FileObject>*/ instantiate() throws IOException {
        assert false : "Cannot call this method if implements WizardDescriptor.ProgressInstantiatingIterator."; //NOI18N
        return null;
    }
    
    public Set instantiate(ProgressHandle handle) throws IOException {
        try {
            handle.start(2);
            handle.progress(1);
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    tryOpenProject();
                }
            });
            return Collections.EMPTY_SET;
        } finally {
            handle.finish();
            
        }
    }
    
    private void tryOpenProject() {
        final Action act = FileUtil.getConfigObject("Actions/Project/org-netbeans-modules-project-ui-OpenProject.instance", Action.class); //NOI18N
        if (act != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    act.actionPerformed(null);
                }
            });
        }
    }
    
    public void initialize(WizardDescriptor wiz) {
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
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); //NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); //NOI18N
            }
        }
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        panels = null;
    }
    
    @Messages("MSG_One_of_Many={0} of {1}")
    public String name() {
        return MSG_One_of_Many(index + 1, panels.length);
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
