package org.netbeans.modules.wsdlextensions.email.template;

import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardContext;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardDescriptorPanel;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.WSDLWizardExtensionIterator;
import org.netbeans.modules.wsdlextensions.email.editor.wizard.OutboundMessageStepWizardPanel;
import org.netbeans.modules.wsdlextensions.email.editor.wizard.InboundMessageStepWizardPanel;

/**
 * Wizard Iterator used to plug-in from the WSDL Wizard
 * 
 */
public class EmailWSDLWizardExtensionIterator extends WSDLWizardExtensionIterator {

    private String templateName;
    private String[] steps;
    private int currentStepIndex = -1;
    private WSDLWizardDescriptorPanel[] panels;
    private InboundMessageStepWizardPanel mInboundPanel = null;

    public EmailWSDLWizardExtensionIterator(WSDLWizardContext context) {
        super(context);
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
        currentStepIndex = -1;
        WSDLWizardContext context = getWSDLWizardContext();
        
       if (templateName.equals("IMAP")) {     //NOI18N
            panels = new WSDLWizardDescriptorPanel[]{
                        mInboundPanel = new InboundMessageStepWizardPanel(context)
                    };
            mInboundPanel.setTemplateType("IMAP");
        } else if (templateName.equals("POP3")) {     //NOI18N
            panels = new WSDLWizardDescriptorPanel[]{
                       mInboundPanel =  new InboundMessageStepWizardPanel(context)
                    };
            mInboundPanel.setTemplateType("POP3");
        } else if (templateName.equals("SMTP")) {     //NOI18N
            panels = new WSDLWizardDescriptorPanel[]{
                        new OutboundMessageStepWizardPanel(context)
                    };
        } 
        
        steps = new String[panels.length];
        int i = 0;
        for (WSDLWizardDescriptorPanel panel : panels) {
            steps[i++] = panel.getName();
        }
        
    }

    @Override
    public WSDLWizardDescriptorPanel current() {
        return panels[currentStepIndex];
    }

    @Override
    public String[] getSteps() {
        assert templateName != null : "template is not set";
        return steps;
    }

    @Override
    public boolean hasNext() {
        return currentStepIndex < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return true;
    }

    @Override
    public void nextPanel() {
        currentStepIndex++;
    }

    @Override
    public void previousPanel() {
        currentStepIndex--;
    }

    @Override
    public boolean commit() {
        boolean status = true;
        WSDLWizardContext context = getWSDLWizardContext();
        for (WSDLWizardDescriptorPanel panel : panels) {
            if (panel instanceof InboundMessageStepWizardPanel) {                                
                status = ((InboundMessageStepWizardPanel)panel).commit();                 
            } else if (panel instanceof OutboundMessageStepWizardPanel) {
                status = ((OutboundMessageStepWizardPanel)panel).commit();
            } 
            
            if (!status) {
                return status;
            }
        }
        return status;
    }

}
