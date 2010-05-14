/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.soa.pojo.wizards;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.soa.pojo.model.api.PortTypeMetadata;
import org.netbeans.modules.soa.pojo.model.api.WSDLMetadata;
import org.netbeans.modules.soa.pojo.util.GeneratorUtil;
import org.netbeans.modules.soa.pojo.util.WSDLUtil;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.wizard.common.WSDLWizardConstants;
import org.netbeans.modules.xml.wsdl.ui.wizard.common.WSDLWizardIterator;
import org.netbeans.modules.xml.wsdl.ui.wsdl.util.BindingUtils;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;
/**
 *
 * @author sgenipudi
 */
public class BindingWizardIterator extends WSDLWizardIterator implements ChangeListener { 
    WizardDescriptor wizard = null;
    private Project project = null;
    private String[] mPojoSteps;
    private int mStepNo = 0;
    private Panel<WizardDescriptor> mPOJOPanel = null;
    
    private POJOProviderWizardIterator mPwiz = null;
    private boolean bNextPanelIsPOJOPanel = false;
    private boolean bPOJOPanelVisited = false;
  //  private String[] mSteps = null;
    private Map<Integer, Panel<WizardDescriptor>> mMapIndexWizard = new Hashtable<Integer, Panel<WizardDescriptor>>();
    
    
    public void initializePOJOWiz() {        
             mPwiz = new POJOProviderWizardIterator();
             mPwiz.initialize(wizard);

           List<String> steps = new ArrayList<String>();
           steps.add("Choose File Type");//NOI18N
           int wizCnt = mMapIndexWizard.size();
           for ( int ix =0; ix < mMapIndexWizard.size(); ix++) {
               steps.add(mMapIndexWizard.get(Integer.valueOf(ix)).getComponent().getName() );
           }
            mPwiz.hasNext();
            OperationMethodChooserPanelWizardDescriptor opWiz = mPwiz.getPOJOProvider();//new OperationMethodChooserPanelWizardDescriptor( new OperationMethodChooserPanel());
            opWiz.setWizard(wizard);
            WizardDescriptor.Panel wizardDescriptorPanel = mPwiz.current();//new MultiTargetChooserPanel(project, sourceGroups, opWiz, true);
            this.mPOJOPanel = wizardDescriptorPanel;
            JComponent jc = (JComponent) wizardDescriptorPanel.getComponent();
            // Sets step number of a component
            jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(4));
            // Sets steps names for a panel
            steps.add(jc.getName());
            jc.putClientProperty("WizardPanel_contentData",steps.toArray(new String[0])  );
            // Turn on subtitle creation on each step
            jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
            // Show steps on the left side with the image on the background
            jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
            // Turn on numbering of all steps
            jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
            MultiTargetChooserPanelGUI mGui = (MultiTargetChooserPanelGUI)jc;
            this.populatePOJODefaults(mPwiz);
            mGui.setPackageName((String) wizard.getProperty(GeneratorUtil.POJO_PACKAGE_NAME));
            mGui.setClassName((String) wizard.getProperty(GeneratorUtil.POJO_ENDPOINT_NAME));            
    }
    
    @Override
    public void initialize(TemplateWizard wiz){
        wizard = wiz;
        wizard.putProperty(BindingUtils.BINDING_EDITOR_MODE,BindingUtils.Type.INBOUND);
        wizard.putProperty(GeneratorUtil.HIDE_ADVANCED, Boolean.TRUE);
        this.appendStep(NbBundle.getMessage( MultiTargetChooserPanelGUI.class, "LBL_JavaTargetChooserPanelGUI_Name"  ));// NOI18N
        this.project = Templates.getProject(wizard);
        super.initialize(wiz);
/*        if ( this.mPojoSteps == null) {
           getSteps(); 
        }*/
    }
    
    
    @Override
    public boolean hasNext() {
        boolean hasNext =  super.hasNext();
        if ( !hasNext) {
            if ( !this.bNextPanelIsPOJOPanel) {
             return true;
            } else {
                if ( this.mStepNo >= this.mMapIndexWizard.size()-1) {
                    return false;
                }
            }
        } else {
            bNextPanelIsPOJOPanel =  false;
        }   
        return true;
    }

    @Override
    public boolean hasPrevious() {
       // String[] steps = getSteps();
        if ( !this.bPOJOPanelVisited ) {
            return super.hasPrevious();
        } else {
            if ( this.mStepNo >= this.mMapIndexWizard.size()-1) {
                   return true;
            }         
        }
        return false;

    }

    @Override
    public void nextPanel() {
      //  String[] steps = getSteps();
        bNextPanelIsPOJOPanel =  false;
        boolean hasNext =  super.hasNext();
        if ( hasNext) {
            super.nextPanel();
            this.mStepNo++;
        } else {
//
            if ( !hasNext) {
                if ( !this.bPOJOPanelVisited ) {
                   bNextPanelIsPOJOPanel =  true;
                } else {
                    if ( this.mStepNo >= this.mMapIndexWizard.size()-1) {
                        bNextPanelIsPOJOPanel = false;
                    }
                }
            }

            if (bNextPanelIsPOJOPanel || !hasNext && this.mStepNo == this.mMapIndexWizard.size()-2 ) {
                this.mStepNo++;
            } 
        }   

    }

    @Override
    public void previousPanel() {
        bNextPanelIsPOJOPanel = false;
        boolean hasPrev =  this.hasPrevious();
        if ( !this.bPOJOPanelVisited ) {
            super.previousPanel();
            this.mStepNo--;
        } else {
            this.mStepNo--;
        }   
    }
    
    @Override
    public Set<DataObject> instantiate(TemplateWizard wiz) throws IOException {
        //Call BC Editors instantiate.
        Set<DataObject> dt =  super.instantiate(wiz);
        POJOProviderWizardIterator pwz = mPwiz;
        if ( !this.bPOJOPanelVisited) {
            
            pwz = new POJOProviderWizardIterator();
            mPwiz = pwz;
            pwz.initialize(wizard);
            pwz.hasNext();
            this.populatePOJODefaults(pwz);
        }

        HashSet<DataObject> bcWiz = new HashSet<DataObject>();
        DataObject wsdlDObj = dt.iterator().next();
        
        wiz.putProperty(GeneratorUtil.POJO_BC_WSDL_LOC, FileUtil.toFile(wsdlDObj.getPrimaryFile()).getAbsolutePath());
        if  ( pwz != null) {
            Set<FileObject> pwzSet =  pwz.instantiate(null);
            bcWiz.add(DataObject.find(pwzSet.iterator().next()));
        }
        return bcWiz;
    }
    
    private void populatePOJODefaults(POJOProviderWizardIterator pwz ) {
            WSDLModel tempModel = (WSDLModel) wizard.getProperty(WSDLWizardConstants.TEMP_WSDLMODEL);

            WSDLUtil wsd = new WSDLUtil();        
            WSDLMetadata wm = wsd.getInterfaceNames(tempModel);

            PortTypeMetadata ptMetadata = wm.getPortTypeMetadaList().get(0);        

            String serviceName = ptMetadata.getPortType().getLocalPart()+GeneratorUtil.POJO_SERVICE_SUFFIX;

            final FileObject dir = Templates.getTargetFolder(wizard);
            String name = Templates.getTargetName(wizard);
            int wsInx = -1;
            if ((wsInx =  name.toLowerCase().lastIndexOf(".wsdl")) != -1) {//NOI18N
                name = name.substring(0, wsInx);
            }
            File newFileBaseDir = FileUtil.toFile(dir);
            File newFileDir =  new File(newFileBaseDir, serviceName);
            if (! bPOJOPanelVisited) {
                newFileDir.mkdirs();
            }
            String pojoClassName = name;
            pojoClassName =GeneratorUtil.findNewFile(newFileDir, pojoClassName);

            OperationMethodChooserPanel omc =(OperationMethodChooserPanel)pwz.getPOJOProvider().getComponent();
            omc.setWizardDescriptor(wizard);
            omc.populateAdvancedPanel(wm);
            omc.getAdvancedPanel().setEndpointName(pojoClassName);
            omc.getAdvancedPanel().disableDefault(true);
            wizard.putProperty(GeneratorUtil.POJO_PACKAGE_NAME, serviceName);

            pwz.getPOJOProvider().storeSettings( wizard);
            wizard.putProperty(GeneratorUtil.POJO_DEST_FOLDER, FileUtil.toFileObject(newFileDir));
            wizard.putProperty(GeneratorUtil.POJO_DEST_NAME,  pojoClassName );
            wizard.putProperty(GeneratorUtil.POJO_ENDPOINT_NAME, pojoClassName);
        
    }
    @Override
    public Panel<WizardDescriptor> current() {

        if (!MultiTargetChooserPanel.isValidProject(project)){
            return new InvalidProjectWizPanel(super.current());
        }
        //if there is no next panel then the next panel is POJO Panel. 
        if (mStepNo > 0 &&  bNextPanelIsPOJOPanel) {
            bPOJOPanelVisited = true;
            if  ( mPOJOPanel == null) {
                initializePOJOWiz();
            }
            return mPOJOPanel;
         }
        
        if (! super.hasPrevious()) {
             this.mStepNo = 0;
             bPOJOPanelVisited = false;
             this.mMapIndexWizard.clear();
         }
        
        if (! bPOJOPanelVisited) {
            Panel<WizardDescriptor> currWiz = super.current();
           // DelegatingWizardPanel dw = new DelegatingWizardPanel(currWiz);
            
        //    JComponent jc = (JComponent) currWiz.getComponent();
        //    String[] steps = (String[]) jc.getClientProperty("WizardPanel_contentData");
         //   JComponent jc = (JComponent) currWiz.getComponent();
            
         //   System.err.println("comp name = "+jc.getClass().getName());
         /*   if (mSteps == null) {
                    ArrayList<String> stepList = new ArrayList<String>();
                    /*
LBL_Wsdl_File_Type=Choose File Type
LBL_Name_And_Location=Name and Location
LBL_Wsdl_Abs_Config=Abstract Configuration
LBL_Wsdl_Conc_Config=Concrete Configuration
*/    /*
                    stepList.add(NbBundle.getMessage( MultiTargetChooserPanelGUI.class, "LBL_Wsdl_File_Type"  ));
                    stepList.add(NbBundle.getMessage( MultiTargetChooserPanelGUI.class, "LBL_Name_And_Location"  ));
                    stepList.add(NbBundle.getMessage( MultiTargetChooserPanelGUI.class, "LBL_Wsdl_Abs_Config"  ));
                    stepList.add(NbBundle.getMessage( MultiTargetChooserPanelGUI.class, "LBL_Wsdl_Conc_Config"  ));                    
                    stepList.add(NbBundle.getMessage( MultiTargetChooserPanelGUI.class, "LBL_JavaTargetChooserPanelGUI_Name"  ) );// NOI18N
                    mSteps = stepList.toArray(new String[0]);
            }
            
            if (! jc.getClass().getName().startsWith("org.netbeans.modules.project")) {//NOI18N
                try {
                   jc.putClientProperty("WizardPanel_contentData", mSteps);
                }catch ( Exception e) {
                }
            }*/
            Integer key = Integer.valueOf(mStepNo);
            if ( !mMapIndexWizard.containsKey(key)) {
                mMapIndexWizard.put(key, currWiz);
                //this.appendStep(NbBundle.getMessage( MultiTargetChooserPanelGUI.class, "LBL_JavaTargetChooserPanelGUI_Name"  ));// NOI18N

            }
            return currWiz;
        } else {
            return mMapIndexWizard.get(Integer.valueOf(mStepNo));
        }
    }

    public void stateChanged(ChangeEvent e) {
    }

    @Override
    public String name() {
        return NbBundle.getMessage(this.getClass(), "LBL_BindingConsumerWizName"); //NOI18N
    }
}
