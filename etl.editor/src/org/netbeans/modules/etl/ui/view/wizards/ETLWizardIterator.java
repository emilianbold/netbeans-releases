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
package org.netbeans.modules.etl.ui.view.wizards;

import java.awt.Component;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;

/**
 * A wizard iterator (sequence of panels). Used to create a wizard. Create one or more
 * panels from template as needed too.
 * 
 */
public abstract class ETLWizardIterator implements WizardDescriptor.InstantiatingIterator {

     /** Tracks index of current panel */
    protected transient int index = 0;

    /* Set <ChangeListener> */
    private transient Set listeners = new HashSet(1);

    /* Contains panels to be iterated */
    private transient WizardDescriptor.Panel[] panels = null;

    private transient WizardDescriptor wiz;
    
    // If something changes dynamically (besides moving between panels),
    // e.g. the number of panels changes in response to user input, then
    // implement fireChangeEvent().

    /**
     * @see org.openide.WizardDescriptor.Iterator#addChangeListener
     */
    public final void addChangeListener(ChangeListener l) {
       
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public int getIndex() {
        return index;
    }

    /**
     * @see org.openide.WizardDescriptor.Iterator#current
     */
    public WizardDescriptor.Panel current() {
         return getPanels(this.wiz)[index];
    }

    /**
     * @see org.openide.WizardDescriptor.Iterator#hasNext
     */
    public boolean hasNext() {
         return index < getPanels(this.wiz).length - 1;
    }

    /**
     * @see org.openide.WizardDescriptor.Iterator#hasPrevious
     */
    public boolean hasPrevious() {
         return index > 0;
    }

    /**
     * @see org.openide.WizardDescriptor.Iterator#name
     */
    public abstract String name();

    /**
     * @see org.openide.WizardDescriptor.Iterator#nextPanel
     */
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    /**
     * @see org.openide.WizardDescriptor.Iterator#previousPanel
     */
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    /**
     * @see org.openide.WizardDescriptor.Iterator#removeChangeListener
     */
    public final void removeChangeListener(ChangeListener l) {
         synchronized (listeners) {
            listeners.remove(l);
        }
    }

    /**
     * Creates list of panels to be displayed.
     * 
     * @return List of panels
     */
    protected abstract List createPanels(WizardDescriptor wiz);

    /**
     * Creates array of step descriptions
     * 
     * @return array of Strings representing task summaries for each panel
     */
    protected abstract String[] createSteps();

    /**
     * Gets panels to be displayed.
     * 
     * @return array of WizardDescriptor.Panel objects
     */
    protected final WizardDescriptor.Panel[] getPanels(WizardDescriptor wiz) {
        if (panels == null) {
            List myPanels = createPanels(wiz);

            WizardDescriptor.Panel[] pnlArray = new WizardDescriptor.Panel[myPanels.size()];
            panels = (WizardDescriptor.Panel[]) myPanels.toArray(pnlArray);
        }
        return panels;
    }

    /**
     * Gets list of steps corresponding to each panel
     * 
     * @return array of Strings summarizing the task in each panel
     */
    protected final String[] getSteps() {
        return createSteps();
    }

    public void initialize(WizardDescriptor wiz) {
        this.panels = getPanels(wiz);
        this.wiz = wiz;
        
        String[] steps = createSteps ();
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
        this.wiz = null;
        panels = null;
    }

    public Set instantiate() throws IOException {
        return new HashSet();
    }
    
}
