/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.soa.pojo.wizards;

import java.awt.Component;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.soa.pojo.util.GeneratorUtil;
import org.netbeans.modules.soa.pojo.util.POJOMessageExchangePattern;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.netbeans.spi.project.ui.templates.support.Templates;
public class POJOProviderWizardMEPOperationTypePanel implements WizardDescriptor.Panel {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private Component component;
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new POJOProviderMEPOperationTypePanel();
        }
        return component;
    }

    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
    // If you have context help:
    // return new HelpCtx(SampleWizardPanel1.class);
    }

    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return true;
    // If it depends on some condition (form filled out...), then:
    // return someCondition();
    // and when this condition changes (last form field filled in...) then:
    // fireChangeEvent();
    // and uncomment the complicated stuff below.
    }

    public final void addChangeListener(ChangeListener l) {
    }

    public final void removeChangeListener(ChangeListener l) {
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
        POJOProviderMEPOperationTypePanel panel = ((POJOProviderMEPOperationTypePanel)this.component);
        POJOProviderAdvancedPanel adwPanel = panel.getAdvancedPanel();
        WizardDescriptor wizDesc = (WizardDescriptor)settings;
      /*  if (! adwPanel.isSaved()) {
            String className = Templates.getTargetName(wizDesc);
            FileObject folder = Templates.getTargetFolder(wizDesc);
            String pkgName = Util.getSelectedPackageName(folder);
            String defaultNS = GeneratorUtil.getNamespace(pkgName, className);
            adwPanel.setEndpointName(className);
            adwPanel.setInterfaceName(className+GeneratorUtil.POJO_INTERFACE_SUFFIX);
            adwPanel.setInterfaceNameNS(defaultNS);
            adwPanel.setOutMessageType(className+GeneratorUtil.POJO_OUT_MESSAGE_SUFFIX);
            adwPanel.setOutMessageTypeNS(defaultNS);
            adwPanel.setServiceName(className +GeneratorUtil.POJO_SERVICE_SUFFIX);
            adwPanel.setServiceNameNS(className +GeneratorUtil.POJO_SERVICE_SUFFIX);
        }*/
        
    }

    public void storeSettings(Object settings) {
        WizardDescriptor wizDesc = (WizardDescriptor)settings;
        POJOProviderMEPOperationTypePanel panel = ((POJOProviderMEPOperationTypePanel)this.component);
        WizardDescriptor wizProps = wizDesc;
        if  ( panel.isInOnly()) {
            wizProps.putProperty(GeneratorUtil.POJO_OPERATION_PATTERN, POJOMessageExchangePattern.InOnly);
        } else {
            wizProps.putProperty(GeneratorUtil.POJO_OUTPUT_TYPE, panel.getOutputType());
        }
        wizProps.putProperty(GeneratorUtil.POJO_INPUT_TYPE, panel.getInputType());

        String className = Templates.getTargetName(wizDesc);
        if ( className != null ) {
            POJOProviderAdvancedPanel adwPanel = panel.getAdvancedPanel();
            wizProps.putProperty(GeneratorUtil.POJO_ENDPOINT_NAME, adwPanel.getEndpointName());
            wizProps.putProperty(GeneratorUtil.POJO_INTERFACE_NAME,adwPanel.getInterfaceName());
            wizProps.putProperty(GeneratorUtil.POJO_SERVICE_NAME, adwPanel.getServiceName());
            wizProps.putProperty(GeneratorUtil.POJO_INTERFACE_NS,adwPanel.getInterfaceNameNS());
            wizProps.putProperty(GeneratorUtil.POJO_SERVICE_NS,adwPanel.getServiceNameNS());
            wizProps.putProperty(GeneratorUtil.POJO_OUTMSG_TYPE_NAME,adwPanel.getOutMessageType());
            wizProps.putProperty(GeneratorUtil.POJO_OUTMSG_TYPE_NS,adwPanel.getOutMessageTypeNS());
        }
    }
    
   
}



