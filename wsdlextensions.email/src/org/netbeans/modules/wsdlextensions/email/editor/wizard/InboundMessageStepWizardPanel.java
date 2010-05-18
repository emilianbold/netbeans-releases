
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
import org.netbeans.modules.wsdlextensions.email.pop3.POP3Binding;
import org.netbeans.modules.wsdlextensions.email.imap.IMAPBinding;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class InboundMessageStepWizardPanel extends WSDLWizardDescriptorPanel {

    QName mQName = null;

	/**
	 * Model to represent this wizard panel with
	 */
	WSDLComponent mComponent = null;

    boolean mValid = true;

	/**
	 * Controller associated with this wizard panel
	 */
    private InboundPersistenceController mController = null;

    /**
     * Project associated with this wsdl model
     */
//    Project mProject = null;
    
    /**
     * Template wizard
     */
    TemplateWizard mTemplateWizard = null;
   
    /**
     * Port type associated with this step
     */
    PortType mPortType = null;

    /**
     *  Whether the template is IMAP or POP3
     */
    String mTemplateType = "";

	/**
	 * The visual component that displays this panel. If you need to access the
	 * component from this class, just use getComponent().
	 */
	private InboundBindingConfigurationEditorForm mPanel;  
    
    public InboundMessageStepWizardPanel(WSDLWizardContext context) {
        super(context);
    }

    @Override
    public String getName() {
		String label;
		if(this.mTemplateType.equalsIgnoreCase("IMAP")){
			label = NbBundle.getMessage(InboundMessageStepWizardPanel.class,
	                "InboundMessageStepWizardPanel.StepLabel_IMAP");
		} else {
			label = NbBundle.getMessage(InboundMessageStepWizardPanel.class,
	                "InboundMessageStepWizardPanel.StepLabel_POP3");
		}
		return label;
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (mPanel == null) {
			InboundBindingConfigurationEditorModel inboundEditorModel = new InboundBindingConfigurationEditorModel();
			mPanel = new InboundBindingConfigurationEditorForm(inboundEditorModel);
        }
        mPanel.setTemplateType(mTemplateType);
        if (mController == null) {
			 WSDLWizardContext myContext = getWSDLWizardContext();
			if ((myContext != null) && (myContext.getWSDLModel() != null)) {
				WSDLModel wsdlModel = myContext.getWSDLModel();
				Definitions defs = wsdlModel.getDefinitions();
                if(this.mTemplateType.equalsIgnoreCase("IMAP")){
                    mComponent = getIMAPBinding(defs);
                } else {
                    mComponent = getPOP3Binding(defs);
                }
            }
			mController = new InboundPersistenceController(mComponent, mPanel);

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
//        mProject = Templates.getProject(mTemplateWizard);
//        if (mPanel != null) {
//            mPanel.setProject(mProject);
//        }
       WSDLWizardContext myContext = getWSDLWizardContext();
       if ((myContext != null) && (myContext.getWSDLModel() != null)) {
           WSDLModel wsdlModel = myContext.getWSDLModel();
           Definitions defs = wsdlModel.getDefinitions();
            if(this.mTemplateType.equalsIgnoreCase("IMAP")){
                mComponent = getIMAPBinding(defs);
            } else {
                mComponent = getPOP3Binding(defs);
            }
       }        
    }

    public void storeSettings(Object settings) {
        TemplateWizard templateWizard = (TemplateWizard) settings;        
        mTemplateWizard = templateWizard;
//        mProject = Templates.getProject(mTemplateWizard);
//        if (mPanel != null) {
//            mPanel.setProject(mProject);
//        }
       WSDLWizardContext myContext = getWSDLWizardContext();
       if ((myContext != null) && (myContext.getWSDLModel() != null)) {
            WSDLModel wsdlModel = myContext.getWSDLModel();
            Definitions defs = wsdlModel.getDefinitions();
            if(this.mTemplateType.equalsIgnoreCase("IMAP")){
                mComponent = getIMAPBinding(defs);
            } else {
                mComponent = getPOP3Binding(defs);
            }
       }
    }

    public void setValid(boolean mode) {
        mValid = mode;
    }
   
    public boolean commit() {
        boolean ok = true;
        if (mController != null) {
            mController.setTemplateType(mTemplateType);
            ok = mController.commit();            
        }
        return ok;
    }

   private POP3Binding getPOP3Binding(Definitions defs) {
		POP3Binding pop3Binding = null;
		if (defs != null) {
			if(defs.getBindings().size() > 0){
				Binding binding = defs.getBindings().iterator().next();
				List<POP3Binding> pop3Bindings = binding.getExtensibilityElements(POP3Binding.class);
				if(pop3Bindings.size() > 0){
					return pop3Bindings.iterator().next();
				}
			}

		}
		return pop3Binding;
	}

   private IMAPBinding getIMAPBinding(Definitions defs) {
		IMAPBinding imapBinding = null;
		if (defs != null) {
			if(defs.getBindings().size() > 0){
				Binding binding = defs.getBindings().iterator().next();
				List<IMAPBinding> imapBindings = binding.getExtensibilityElements(IMAPBinding.class);
				if(imapBindings.size() > 0){
					return imapBindings.iterator().next();
				}
			}

		}
		return imapBinding;
	}

   public void setTemplateType(String val){
       this.mTemplateType = val;
   }
   
}

