/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.vmd.componentssupport.ui.helpers.BaseHelper;
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
    
    private static final String WIZARD_TITLE    = "LBL_LibraryWizardTitle";   // NOI18N
    private static final String LIB_STEPS_COUNT = "LBL_LibWizardStepsCount";    // NOI18N
    
    public static final String  LIBRARY_STEP    = "LBL_LibSelectLibraryStep";   // NOI18N
    public static final String  NAME_LOCATION_STEP
                                                = "LBL_LibNameAndLocationStep"; // NOI18N
    
    public static final String LIBRARY          = "library";                    // NOI18N
    public static final String DISPLAY_NAME     = "displayName";                // NOI18N
    public static final String LIB_NAME         = "libName";                    // NOI18N

    public static final String EXISTING_LIBRARIES    
                                                = "existLibrary";               // NOI18N
    public static final String EXISTING_LIB_NAMES    
                                                = "existLibName";               // NOI18N
    
    public static final String LIBRARY_TYPE_J2SE    
                                                = "j2se";                       // NOI18N
    
    
    
    NewLibraryDescriptor( WizardDescriptor mainDesc ){
        myMainWizard = mainDesc;
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
        
        wizardDescriptor.setTitle(
                NbBundle.getMessage(NewLibraryDescriptor.class, WIZARD_TITLE));
        
        // put properties useful for current wizard into wizardDescriptor
        wizardDescriptor.putProperty( 
                CustomComponentWizardIterator.PROJECT_NAME, 
                myMainWizard.getProperty(
                        CustomComponentWizardIterator.PROJECT_NAME) );
        wizardDescriptor.putProperty(
                CustomComponentWizardIterator.CODE_BASE_NAME, 
                getCodeNameBase() );
        wizardDescriptor.putProperty(
                EXISTING_LIB_NAMES, 
                myMainWizard.getProperty( 
                        CustomComponentWizardIterator.LIB_NAMES) );
        wizardDescriptor.putProperty(
                EXISTING_LIBRARIES, 
                myMainWizard.getProperty( 
                        CustomComponentWizardIterator.LIBRARIES) );
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.InstantiatingIterator#instantiate()
     */
    public Set<?> instantiate() throws IOException {
        List libs = (List)myMainWizard.getProperty( 
                CustomComponentWizardIterator.LIBRARIES);
        if ( libs == null ){
            libs = new LinkedList<Library>();
            myMainWizard.putProperty( CustomComponentWizardIterator.LIBRARIES,
                    libs);
        }
        List libNames = (List)myMainWizard.getProperty( 
                CustomComponentWizardIterator.LIB_NAMES);
        if ( libNames == null ) {
            libNames = new LinkedList<String>();
            myMainWizard.putProperty( CustomComponentWizardIterator.LIB_NAMES, 
                    libNames );
        }
        List names = (List)myMainWizard.getProperty( 
                CustomComponentWizardIterator.LIB_DISPLAY_NAMES);
        if ( names == null ){
            names = new LinkedList<String>();
            myMainWizard.putProperty( CustomComponentWizardIterator.LIB_DISPLAY_NAMES, 
                    names  );
        }
        
        libs.add( myWizard.getProperty(LIBRARY));
        libNames.add(myWizard.getProperty(LIB_NAME));
        names.add(myWizard.getProperty(DISPLAY_NAME));
        
        // TODO : notify JList model in main wizard second UI panel
        // about changes.
        
        return Collections.EMPTY_SET;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.InstantiatingIterator#uninitialize(org.openide.WizardDescriptor)
     */
    public void uninitialize( WizardDescriptor arg0 ) {
        myWizard.putProperty(LIBRARY,null);
        myWizard.putProperty(LIB_NAME,null);
        myWizard.putProperty(DISPLAY_NAME,null);
        myWizard.putProperty( CustomComponentWizardIterator.PROJECT_NAME, null);
        myWizard.putProperty(CustomComponentWizardIterator.CODE_BASE_NAME, null);
        myWizard.putProperty(EXISTING_LIB_NAMES, null);
        myWizard.putProperty(EXISTING_LIBRARIES, null);
        myWizard = null;
        myPanels = null;
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
              new SelectLibraryWizardPanel(), 
              new NameAndLocationWizardPanel()
        };
    }

    /**
     * creates code name base to be used for layer.xml and Bundle.properties
     * in "Created and Modified" files preview.
     * @return codeNameBase string from main wizard if it is already created. Or 
     * the same default value that will be suggested to user in 
     * main project creation wizard.
     */
    private String getCodeNameBase(){
        String codeNameBase = (String)myMainWizard.getProperty( 
                CustomComponentWizardIterator.CODE_BASE_NAME);
        String projectName = (String)myMainWizard.getProperty( 
                CustomComponentWizardIterator.PROJECT_NAME);
        if ( codeNameBase == null ){
            codeNameBase = BaseHelper.getDefaultCodeNameBase(projectName);
        }
        return codeNameBase;
    }
    
    private String[] createSteps() {
        return new String[] { 
                NbBundle.getMessage(NewLibraryDescriptor.class, LIBRARY_STEP) ,
                NbBundle.getMessage(NewLibraryDescriptor.class, NAME_LOCATION_STEP) 
                        };
    }
    
    private int myCurrentIndex;
    private WizardDescriptor.Panel[] myPanels;
    private WizardDescriptor myWizard;
    private WizardDescriptor myMainWizard;
    
}
