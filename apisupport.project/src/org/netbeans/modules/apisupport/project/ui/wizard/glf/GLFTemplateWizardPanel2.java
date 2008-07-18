package org.netbeans.modules.apisupport.project.ui.wizard.glf;

import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardPanel;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;


public class GLFTemplateWizardPanel2 extends BasicWizardPanel {
    
    private GLFTemplateWizardIterator iterator;
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private GLFTemplateVisualPanel2 component;
    
    GLFTemplateWizardPanel2 (GLFTemplateWizardIterator iterator) {
        super(iterator.getWizardDescriptor());
        this.iterator = iterator;
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public GLFTemplateVisualPanel2 getComponent () {
        if (component == null) {
            component = new GLFTemplateVisualPanel2 (this);
            component.addPropertyChangeListener(this);
        }
        return component;
    }
    
    public HelpCtx getHelp () {
        // Show no Help button for this panel:
        // If you have context help:
         return new HelpCtx(GLFTemplateWizardPanel2.class);
    }
    
    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings (WizardDescriptor settings) {
        // ensures error message is set correctly when user presses Back and Next buttons
        if (component != null)
            component.update();
    }
    String getMimeType () {
        return component.getMimeType ();
    }
    
    String getExtensions () {
        return component.getExtensions ();
    }
    
    GLFTemplateWizardIterator getIterator () {
        return iterator;
    }
}

