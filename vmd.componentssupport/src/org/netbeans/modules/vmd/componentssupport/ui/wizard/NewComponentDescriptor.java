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
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.vmd.componentssupport.ui.helpers.CustomComponentHelper;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.NbBundle;

/**
 * Wizard <em>New Component Descriptor</em> for adding new ComponentDescriptors
 * for Java ME components
 *
 * @author avk
 */
public final class NewComponentDescriptor implements WizardDescriptor.InstantiatingIterator {
    
    private static final String WIZARD_TITLE     = "LBL_ComponentWizardTitle";  // NOI18N
    private static final String LIB_STEPS_COUNT = "LBL_CompWizardStepsCount";   // NOI18N
    
    public static final String  COMPONENT_DESCR_STEP    
                                                = "LBL_PrefixCompDescrStep";    // NOI18N
    public static final String  COMPONENT_PRODUCER_STEP
                                                = "LBL_ComponentProducerStep";  // NOI18N
    public static final String  PRESENTERS_STEP = "LBL_ComponentPresentersStep";// NOI18N
    public static final String  FINAL_STEP      = "LBL_ComponentFinalStep";     // NOI18N
    
    public static final String COMPONENT_DESCRIPTOR_POSTFIX 
                                                = "CD";                         // NOI18N 
    public static final String COMPONENT_PRODUCER_POSTFIX 
                                                = "Producer";                   // NOI18N 
    public static final String COMPONENT_DECRIPTOR_DEFAULT_PARENT
        = "org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD";// NOI18N 
    public static final boolean COMPONENT_DECRIPTOR_DEFAULT_CAN_INSTANTIATE = true;
    public static final boolean COMPONENT_DECRIPTOR_DEFAULT_CAN_BE_SUPER = true;
    
    // properties - common
    public static final String HELPER           = "custCompHelper";             // NOI18N 

    public static final String CC_PREFIX        = "prefix";                     // NOI18N 
    // properties - component descriptor step
    public static final String CD_CLASS_NAME    = "compDescrClassName";         // NOI18N 
    public static final String CD_TYPE_ID       = "compDescrTypeId";            // NOI18N 
    public static final String CD_SUPER_DESCR_CLASS  
                                                = "compDescrSuperClass";        // NOI18N 
    public static final String CD_VERSION       = "compDescrVersion";           // NOI18N 
    public static final String CD_CAN_INSTANTIATE  
                                                = "compDescrCanInstantiate";    // NOI18N 
    public static final String CD_CAN_BE_SUPER  = "compDescrCanBeSuper";        // NOI18N 
    // properties - component producer step
    public static final String CP_CLASS_NAME    = "compProdClassName";          // NOI18N 
    public static final String CP_PALETTE_DISP_NAME    
                                                = "compProdPaletteDispName";    // NOI18N 
    public static final String CP_PALETTE_TIP   = "compProdPaletteTip";         // NOI18N 
    public static final String CP_PALETTE_CATEGORY    
                                                = "compProdPaletteCategory";    // NOI18N 
    public static final String CP_SMALL_ICON    = "compProdSmallIcon";          // NOI18N 
    public static final String CP_LARGE_ICON    = "compProdLargeIcon";          // NOI18N 
    public static final String CP_ADD_LIB       = "compProdAddLib";             // NOI18N 
    public static final String CP_LIB_NAME      = "compProdLibName";            // NOI18N 
    public static final String CP_VALID_ALWAYS  = "compProdValidAlways";        // NOI18N 
    public static final String CP_VALID_PLATFORM    
                                                = "compProdValidPlatform";      // NOI18N 
    public static final String CP_VALID_CUSTOM  = "compProdValidCustom";        // NOI18N 
    // properties - component presenters step
    // properties - final step

    public NewComponentDescriptor() {
    }
    
    NewComponentDescriptor(WizardDescriptor mainWizard){
        myMainWizard = mainWizard;
        assert myMainWizard != null;
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
        
        initWizardData();
        
    }

    private void initWizardData(){
        
        if (myMainWizard != null){
            // child wizard started from CustomComponentWizardIterator panels
            myCustCompHelper = new CustomComponentHelper.
                    InstantiationToWizardHelper(myMainWizard, myWizard);
        } else {
            // independent wizard started from existing project
            Project project = Templates.getProject(myWizard);
            assert project != null;
            
            myCustCompHelper = new CustomComponentHelper.
                    RealInstantiationHelper(project, myWizard);
        }
        assert myCustCompHelper != null;
        
        myWizard.putProperty( HELPER, myCustCompHelper );
    }
    
    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.InstantiatingIterator#instantiate()
     */
    public Set<?> instantiate() throws IOException {
        assert myCustCompHelper != null;
        return myCustCompHelper.instantiate();
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.InstantiatingIterator#uninitialize(org.openide.WizardDescriptor)
     */
    public void uninitialize( WizardDescriptor arg0 ) {
        myWizard.putProperty(HELPER, null);
        
        myWizard.putProperty(CC_PREFIX, null);
        myWizard.putProperty(CD_CLASS_NAME, null);
        myWizard.putProperty(CD_TYPE_ID, null);
        myWizard.putProperty(CD_SUPER_DESCR_CLASS, null);
        myWizard.putProperty(CD_VERSION, null);
        myWizard.putProperty(CD_CAN_INSTANTIATE, null);
        myWizard.putProperty(CD_CAN_BE_SUPER, null);

        myWizard.putProperty(CP_CLASS_NAME, null);
        myWizard.putProperty(CP_PALETTE_DISP_NAME, null);
        myWizard.putProperty(CP_PALETTE_TIP, null);
        myWizard.putProperty(CP_PALETTE_CATEGORY, null);
        myWizard.putProperty(CP_SMALL_ICON, null);
        myWizard.putProperty(CP_LARGE_ICON, null);
        myWizard.putProperty(CP_ADD_LIB, null);
        myWizard.putProperty(CP_LIB_NAME, null);
        myWizard.putProperty(CP_VALID_ALWAYS, null);
        myWizard.putProperty(CP_VALID_PLATFORM, null);
        myWizard.putProperty(CP_VALID_CUSTOM, null);

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
                NewComponentDescriptor.class).getString(
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
              new ComponentDescriptorWizardPanel(), 
              new ComponentProducerWizardPanel(), 
              //new ComponentPresentersWizardPanel(), 
              new ComponentFinalWizardPanel()
        };
    }
    
    protected static String createDefaultCDClass(String prefix){
        return prefix + COMPONENT_DESCRIPTOR_POSTFIX;
    }
            
    protected static String createDefaultProducerClass(String prefix){
        return prefix + COMPONENT_PRODUCER_POSTFIX;
    }
            
    private String[] createSteps() {
        return new String[] { 
                NbBundle.getMessage(NewComponentDescriptor.class, 
                        COMPONENT_DESCR_STEP) ,
                NbBundle.getMessage(NewComponentDescriptor.class, 
                        COMPONENT_PRODUCER_STEP), 
                //NbBundle.getMessage(NewComponentDescriptor.class, 
                //        PRESENTERS_STEP) ,
                NbBundle.getMessage(NewComponentDescriptor.class, 
                        FINAL_STEP) 
                        };
    }
    

    private int myCurrentIndex;
    private WizardDescriptor.Panel[] myPanels;
    private WizardDescriptor myWizard;
    private WizardDescriptor myMainWizard;
    private CustomComponentHelper myCustCompHelper;
}
