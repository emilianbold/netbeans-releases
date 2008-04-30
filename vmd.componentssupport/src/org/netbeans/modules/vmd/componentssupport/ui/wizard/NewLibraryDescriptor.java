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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.vmd.componentssupport.ui.wizard;

import java.awt.Component;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.netbeans.api.project.libraries.Library;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;

/**
 * Wizard <em>J2ME Library Descriptor</em> for registering
 * libraries for end users.
 *
 * @author ads
 */
final class NewLibraryDescriptor implements WizardDescriptor.InstantiatingIterator {
    
    private static final String LIB_STEPS_COUNT = "LBL_LibStepsCount";      // NOI18N
    
    public static final String  LIBRARY_STEP    = "LBL_LibSelectLibrary";   // NOI18N
    public static final String  NAME_LOCATION_STEP
                                                = "LBL_LibNameAndLocation"; // NOI18N
    
    NewLibraryDescriptor(){
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.InstantiatingIterator#initialize(org.openide.WizardDescriptor)
     */
    public void initialize( WizardDescriptor wizardDescriptor ) {
        myWizard = wizardDescriptor;
        myCurrentIndex = 0;
        myPanels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < myPanels.length; i++) {
            Component c = myPanels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components

                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(CustomComponentWizardIterator.SELECTED_INDEX,
                        i);
                // Step name (actually the whole list for reference).
                jc.putClientProperty(CustomComponentWizardIterator.CONTENT_DATA, 
                        steps);
            }
        }        
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.InstantiatingIterator#instantiate()
     */
    public Set<?> instantiate() throws IOException {
        return Collections.EMPTY_SET;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.InstantiatingIterator#uninitialize(org.openide.WizardDescriptor)
     */
    public void uninitialize( WizardDescriptor arg0 ) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#current()
     */
    public Panel current() {
        return myPanels[myCurrentIndex];
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#hasNext()
     */
    public boolean hasNext() {
        return myCurrentIndex < myPanels.length - 1;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#hasPrevious()
     */
    public boolean hasPrevious() {
        return myCurrentIndex > 0;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#name()
     */
    public String name() {
        return MessageFormat.format(NbBundle.getBundle(
                NewLibraryDescriptor.class).getString(
                LIB_STEPS_COUNT), new Object[] {
                myCurrentIndex + 1 , 
                myPanels.length  });
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#nextPanel()
     */
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        myCurrentIndex++;        
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#previousPanel()
     */
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        myCurrentIndex--;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#removeChangeListener(javax.swing.event.ChangeListener)
     */
    public void removeChangeListener( ChangeListener arg0 ) {
    }
    
    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#addChangeListener(javax.swing.event.ChangeListener)
     */
    public void addChangeListener( ChangeListener arg0 ) {
    }   
    
    WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] { 
              new SelectLibraryPanel(),  
        };
    }

    private String[] createSteps() {
        return new String[] { 
                NbBundle.getMessage(
                        SelectLibraryPanel.class, LIBRARY_STEP) ,
                        };
    }
    
    private int myCurrentIndex;
    private WizardDescriptor.Panel[] myPanels;
    private WizardDescriptor myWizard;
    
}
