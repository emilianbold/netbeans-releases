package org.netbeans.modules.uml.ui.controls.newdialog;

import java.awt.Component;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.ui.support.NewPackageKind;
import org.netbeans.modules.uml.ui.swing.drawingarea.DiagramEngine;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class AddPackageWizardPanel1 implements WizardDescriptor.Panel {
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private Component component;
    private INewDialogPackageDetails m_details;
    private AddPackageVisualPanel1 panelComponent = null;
    private boolean resultFlag = false;
    
    public AddPackageWizardPanel1(INewDialogPackageDetails details) {
        super();
        this.m_details = details;        
    }
    
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new AddPackageVisualPanel1(m_details);
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
    
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
    /*
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
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
    public void readSettings(Object settings) {}
    public void storeSettings(Object settings) {
        if (resultFlag) {
            return;
        }
        //get the user selected data from the panel
        INewDialogPackageDetails details = getResults();
        if (details != null) {            
            //set the details object
            WizardDescriptor wizDesc = (WizardDescriptor)settings;
            wizDesc.putProperty(AddPackageWizardIterator.PACKAGE_DETAILS, details);            
        }
    }
    
    private INewDialogPackageDetails getResults() {
         if (component instanceof AddPackageVisualPanel1) {
            panelComponent = (AddPackageVisualPanel1)component;
        }
         
        INewDialogPackageDetails details = new NewDialogPackageDetails();
        if (validData()) {
            details.setPackageKind(NewPackageKind.NPKGK_PACKAGE);

            // Get the name
            details.setName(panelComponent.getPackageName());
            
            // Get the scoped diagram flag
            details.setCreateScopedDiagram(panelComponent.isCheckboxSelected());
            
            // Get the scoped diagram name
            details.setScopedDiagramName((String)panelComponent.getScopedDiagramName());
            
            // Get the namespace
            INamespace pSelectedNamespace = NewDialogUtilities
                    .getNamespace((String)panelComponent.getPackageNamespace());
            details.setNamespace(pSelectedNamespace);
            
            // Get the diagram kind
            String diaType = (String)panelComponent.getScopedDiagramKind();
            if (diaType.equals(NewDialogResources
                    .getString("PSK_SEQUENCE_DIAGRAM"))) // NOI18N
            {
                details.setScopedDiagramKind(IDiagramKind.DK_SEQUENCE_DIAGRAM);
            } else if (diaType.equals(NewDialogResources
                    .getString("PSK_ACTIVITY_DIAGRAM"))) // NOI18N
            {
                details.setScopedDiagramKind(IDiagramKind.DK_ACTIVITY_DIAGRAM);
            } else if (diaType.equals(NewDialogResources
                    .getString("PSK_CLASS_DIAGRAM"))) // NOI18N
            {
                details.setScopedDiagramKind(IDiagramKind.DK_CLASS_DIAGRAM);
            } else if (diaType.equals(NewDialogResources
                    .getString("PSK_COLLABORATION_DIAGRAM"))) // NOI18N
            {
                details.setScopedDiagramKind(IDiagramKind.DK_COLLABORATION_DIAGRAM);
            } else if (diaType.equals(NewDialogResources
                    .getString("PSK_COMPONENT_DIAGRAM"))) // NOI18N
            {
                details.setScopedDiagramKind(IDiagramKind.DK_COMPONENT_DIAGRAM);
            } else if (diaType.equals(NewDialogResources
                    .getString("PSK_DEPLOYMENT_DIAGRAM"))) // NOI18N
            {
                details.setScopedDiagramKind(IDiagramKind.DK_DEPLOYMENT_DIAGRAM);
            } else if (diaType.equals(NewDialogResources
                    .getString("PSK_STATE_DIAGRAM"))) // NOI18N
            {
                details.setScopedDiagramKind(IDiagramKind.DK_STATE_DIAGRAM);
            } else if (diaType.equals(NewDialogResources
                    .getString("PSK_USE_CASE_DIAGRAM"))) // NOI18N
            {
                details.setScopedDiagramKind(IDiagramKind.DK_USECASE_DIAGRAM);
            }
            resultFlag = true;
            return details;
        }
        return null;
    }
    
    /**
     * @return
     */
    private boolean validData() {
        // TODO Auto-generated method stub
        IElementLocator pElementLocator = new ElementLocator();
        ETList<INamedElement> pFoundElements = pElementLocator.findByName(
                NewDialogUtilities.getNamespace((String)panelComponent.getPackageNamespace()),
                panelComponent.getPackageName());
        
        if (pFoundElements != null) {
            int count = pFoundElements.getCount();
            for (int i = 0 ; i < count ; i++) {
                INamedElement pFoundElement = pFoundElements.get(i);
                
                if (pFoundElement != null) {
                    if (pFoundElement.getElementType().equals("Package")) {
                        DialogDisplayer.getDefault().notify(
                                new NotifyDescriptor.Message(NbBundle.getMessage(
                                DiagramEngine.class, "IDS_NAMESPACECOLLISION")));
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
}

