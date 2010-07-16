/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.soa.pojo.wizards;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.soa.pojo.util.GeneratorUtil;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class POJOConsumerCreationPanelPalleteWizardPanel implements WizardDescriptor.Panel, ChangeListener {
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private Component component;
    private WizardDescriptor mWizard;
    private List listeners = new ArrayList();    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new POJOConsumerCreationPanel();
            ((POJOConsumerCreationPanel)component).addChangeListener(this);
            
        }
        return component;
    }

    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
    // If you have context help:
    // return new HelpCtx(SampleWizardPanel1.class);
    }


    private void setErrorMessage( String key ) {
        if ( key == null ) {
            setLocalizedErrorMessage ( "" ); // NOI18N
        }
        else {
            setLocalizedErrorMessage ( NbBundle.getMessage( POJOConsumerCreationPanelPalleteWizardPanel.class, key)  ); // NOI18N
        }
    }
    
    private void setLocalizedErrorMessage (String message) {
        if ( mWizard != null) {
            mWizard.putProperty("WizardPanel_errorMessage", message);//NOI18N
        }
    }    
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        POJOConsumerCreationPanel pcp = (POJOConsumerCreationPanel)this.component;
        String intfs = pcp.getInterfaceNS();
        String intfName = pcp.getInterfaceName();
        String operationName = pcp.getOperation();
        String operationNs = pcp.getOperationNS();
        
        if (! MultiTargetChooserPanel.isValidTypeIdentifier(intfName) ) {
            this.setErrorMessage("MSG_Invalid_Interface_Name");//NOI18N
            return false;
        }
        String msg = GeneratorUtil.isValidNamespace(intfs,"MSG_Invalid_Interface_Namespace");//NOI18N
        if ( msg != null)  {
            setLocalizedErrorMessage(msg);
            return false;
        }

        if (! MultiTargetChooserPanel.isValidTypeIdentifier(operationName) ) {
            this.setErrorMessage("MSG_Invalid_Operation_Name");//NOI18N
            return false;
        }

        msg = GeneratorUtil.isValidNamespace(operationNs,"MSG_Invalid_Operation_Namespace");//NOI18N
        if ( msg != null)  {
            setLocalizedErrorMessage(msg);
            return false;
        }
        this.setErrorMessage(null);
        return true;
    // If it depends on some condition (form filled out...), then:
    // return someCondition();
    // and when this condition changes (last form field filled in...) then:
    // fireChangeEvent();
    // and uncomment the complicated stuff below.
    }

    public void addChangeListener(ChangeListener cl) {
        listeners.add(cl);
    }
    public void removeChangeListener(ChangeListener arg0) {
        listeners.remove(arg0);
    }

    private void fireChange()
    {
        ChangeEvent e = new ChangeEvent(this);
        for(Iterator it = listeners.iterator(); it.hasNext(); ((ChangeListener)it.next()).stateChanged(e));
    }
    
    /*
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0
    public final void addChangeListener(ChangeListener l) {
    synchronized (listeners) {
    listeners.add(l);
    }
    }
    public final void removeChangeListener(ChangeListener l) {
    synchronized (listeners) {
    listeners.remove(l);
    }
    }
    protected final void fireChangeEvent() {
    Iterator<ChangeListener> it;
    synchronized (listeners) {
    it = new HashSet<ChangeListener>(listeners).iterator();
    }
    ChangeEvent ev = new ChangeEvent(this);
    while (it.hasNext()) {
    it.next().stateChanged(ev);
    }
    }
     */

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {
        WizardDescriptor wizDesc = (WizardDescriptor) settings;
        this.mWizard = wizDesc;
        POJOConsumerCreationPanel pojoCreatePanel = (POJOConsumerCreationPanel)component;
        pojoCreatePanel.setWizardDescriptor(wizDesc);
    }

    public void storeSettings(Object settings) {
        WizardDescriptor wizDesc = (WizardDescriptor) settings;
        POJOConsumerCreationPanel pojoCreatePanel = (POJOConsumerCreationPanel)component;
        wizDesc.putProperty(GeneratorUtil.POJO_CONSUMER_INPUT_TYPE, pojoCreatePanel.getInputType());
        wizDesc.putProperty(GeneratorUtil.POJO_CONSUMER_OUTPUT_TYPE, pojoCreatePanel.getOutputType());
        wizDesc.putProperty(GeneratorUtil.POJO_CONSUMER_INVOKE_TYPE,Boolean.valueOf(pojoCreatePanel.isSynchronousInvoke()));
        wizDesc.putProperty(GeneratorUtil.POJO_SELECTED_METHOD, pojoCreatePanel.getInvokeFrom());
        
        Boolean bConsumerDropObj = (Boolean) wizDesc.getProperty(GeneratorUtil.POJO_CONSUMER_DROP);
        boolean bConsumerDrop = ( bConsumerDropObj != null && bConsumerDropObj == Boolean.TRUE );
        if ( !bConsumerDrop) {
            wizDesc.putProperty(GeneratorUtil.POJO_CONSUMER_INTERFACE_NAME,  pojoCreatePanel.getInterfaceName());
            wizDesc.putProperty(GeneratorUtil.POJO_CONSUMER_INTERFACE_NS, pojoCreatePanel.getInterfaceNS());
            wizDesc.putProperty(GeneratorUtil.POJO_CONSUMER_OPERATION_NAME,pojoCreatePanel.getOperation());
        }
        POJOConsumerPalleteAdvancedPanel poAdv = pojoCreatePanel.getAdvanced();
        if ( poAdv != null) {
            if ( !bConsumerDrop) {
                wizDesc.putProperty(GeneratorUtil.POJO_CONSUMER_INPUT_MESSAGE_TYPE,pojoCreatePanel.getAdvanced().getInputMessageType());
                wizDesc.putProperty(GeneratorUtil.POJO_CONSUMER_INPUT_MESSAGE_TYPE_NS,pojoCreatePanel.getAdvanced().getInputMessageTypeNS());
            }

            wizDesc.putProperty(GeneratorUtil.POJO_CONSUMER_REPLY_METHOD_NAME, pojoCreatePanel.getAdvanced().getReplyMethodName());
            wizDesc.putProperty(GeneratorUtil.POJO_CONSUMER_DONE_METHOD_NAME,pojoCreatePanel.getAdvanced().getDoneMethodName());
        }
        
    }

    void setWizard(WizardDescriptor wizard) {
        mWizard = wizard;
    }

    public void stateChanged(ChangeEvent e) {
      fireChange();
    }
}

