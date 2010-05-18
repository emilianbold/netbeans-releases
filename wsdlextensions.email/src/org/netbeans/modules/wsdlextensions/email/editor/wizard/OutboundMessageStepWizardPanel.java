
package org.netbeans.modules.wsdlextensions.email.editor.wizard;

import java.awt.Component;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardContext;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardDescriptorPanel;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.modules.wsdlextensions.email.smtp.SMTPBinding;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class OutboundMessageStepWizardPanel extends WSDLWizardDescriptorPanel {

    QName mQName = null;

	/**
	 * Model to represent this wizard panel with
	 */
	WSDLComponent mComponent = null;

    boolean mValid = true;

	/**
	 * Controller associated with this wizard panel
	 */
    private OutboundPersistenceController mController = null;

    /**
     * Project associated with this wsdl model
     */
    Project mProject = null;
    
    /**
     * Template wizard
     */
    TemplateWizard mTemplateWizard = null;
   
    /**
     * Port type associated with this step
     */
    PortType mPortType = null;

	/**
	 * The visual component that displays this panel. If you need to access the
	 * component from this class, just use getComponent().
	 */
	private OutboundBindingConfigurationEditorForm mPanel;  
    
    public OutboundMessageStepWizardPanel(WSDLWizardContext context) {
        super(context);
    }

    public String getName() {
	        return NbBundle.getMessage(OutboundMessageStepWizardPanel.class,
	                "OutboundMessageStepWizardPanel.StepLabel");      
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (mPanel == null) {
			OutboundBindingConfigurationEditorModel outboundEditorModel = new OutboundBindingConfigurationEditorModel();
			mPanel = new OutboundBindingConfigurationEditorForm(outboundEditorModel);
        }
        mPanel.setProjectinPanel(mProject);
        if (mController == null) {
			 WSDLWizardContext myContext = getWSDLWizardContext();
			if ((myContext != null) && (myContext.getWSDLModel() != null)) {
				WSDLModel wsdlModel = myContext.getWSDLModel();
                mPanel.setWSDLModelinPanel(wsdlModel);
				Definitions defs = wsdlModel.getDefinitions();
				mComponent = getSMTPBinding(defs);
			}        
			mController = new OutboundPersistenceController(mComponent, mPanel);
		}
		return mPanel;
    }

    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    public boolean isFinishPanel() {
        return true;
    }

    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return true;
    }

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {       
        TemplateWizard templateWizard = (TemplateWizard) settings;        
        mTemplateWizard = templateWizard;
        mProject = Templates.getProject(mTemplateWizard);
        if (mPanel != null) {
            mPanel.setProject(mProject);
        }       
       WSDLWizardContext myContext = getWSDLWizardContext();
       if ((myContext != null) && (myContext.getWSDLModel() != null)) {
           WSDLModel wsdlModel = myContext.getWSDLModel();
           Definitions defs = wsdlModel.getDefinitions();
           mComponent = getSMTPBinding(defs); 
       }        
    }

    public void storeSettings(Object settings) {
        TemplateWizard templateWizard = (TemplateWizard) settings;        
        mTemplateWizard = templateWizard;
        mProject = Templates.getProject(mTemplateWizard);
        if (mPanel != null) {
            mPanel.setProject(mProject);
        }           
       WSDLWizardContext myContext = getWSDLWizardContext();
       if ((myContext != null) && (myContext.getWSDLModel() != null)) {
           WSDLModel wsdlModel = myContext.getWSDLModel();
           Definitions defs = wsdlModel.getDefinitions();
           mComponent = getSMTPBinding(defs);
       }
    }

    public void setValid(boolean mode) {
        mValid = mode;
    }
   
    public boolean commit() {
        boolean ok = true;
        if (mController != null) {
            ok = mController.commit();            
        }
        return ok;
    }

   private SMTPBinding getSMTPBinding(Definitions defs) {
		SMTPBinding smtpBinding = null;
		if (defs != null) {
			if(defs.getBindings().size() > 0){
				Binding binding = defs.getBindings().iterator().next();
				List<SMTPBinding> smtpBindings = binding.getExtensibilityElements(SMTPBinding.class);
				if(smtpBindings.size() > 0){
					return smtpBindings.iterator().next();
				}
			}
		}
		return smtpBinding;
	} 

}

