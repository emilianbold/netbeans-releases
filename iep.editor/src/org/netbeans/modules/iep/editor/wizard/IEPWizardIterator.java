/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.iep.editor.wizard;

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.iep.editor.PlanDataObject;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.ModelHelper;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

public final class IEPWizardIterator implements TemplateWizard.Iterator {

    private int index;
    private TemplateWizard  wizard;
    private WizardDescriptor.Panel[] panels;

    private IEPWizardPanel1 panel1;
//    private IEPWizardPanel2 panel2;
    private IEPWizardPanel2EmptyIEPFile panel2EmptyIEPFile;
    //private IEPWizardPanel3 panel3;
    
    private String[] wizardContentData;
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        //if panels are null first initialize each different
        //wizard paths
        if (panels == null) {
            panels = new WizardDescriptor.Panel[]{
                panel2EmptyIEPFile
//                panel2,
                //panel3
            };
        
            String[] steps = createSteps();
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                if (steps[i] == null) {
                    // Default step name to component name of panel. Mainly
                    // useful for getting the name of the target chooser to
                    // appear in the list of steps.
                    steps[i] = c.getName();
                }
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty("WizardPanel_contentData", steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
                }
            }
        }
        
        /*
        if (panels == null) {
            panels = new WizardDescriptor.Panel[]{
                new IEPWizardPanel1(),
                new IEPWizardPanel2(),
                new IEPWizardPanel3()
            };
            String[] steps = createSteps();
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                if (steps[i] == null) {
                    // Default step name to component name of panel. Mainly
                    // useful for getting the name of the target chooser to
                    // appear in the list of steps.
                    steps[i] = c.getName();
                }
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty("WizardPanel_contentData", steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
                }
            }
        }*/
        return panels;
    }

    public Set instantiate(TemplateWizard wizard) throws IOException {
        FileObject dir = Templates.getTargetFolder( wizard );        
        DataFolder df = DataFolder.findFolder( dir );
        FileObject template = Templates.getTemplate( wizard );        
        DataObject dTemplate = DataObject.find( template );                
        PlanDataObject createdObject = (PlanDataObject) dTemplate.createFromTemplate( df, Templates.getTargetName(wizard)  );
        if (createdObject == null)
            return Collections.emptySet();
        
        //if user has provided attribute list
        //use it and create a stream input.
        IEPModel model = createdObject.getPlanEditorSupport().getModel();
        IEPWizardHelper.processUsingExistingSchema(model, wizard);
        
        //set targetNamespace on iepfile
        String tns = Utility.generateTargetNamespace(createdObject);
        String packageName = ModelHelper.getPackageName(createdObject); 
        model.startTransaction();
        model.getPlanComponent().setName(createdObject.getName());
        
//        model.getPlanComponent().setTargetNamespace(tns);
        model.getPlanComponent().setPackageName(packageName);
        model.endTransaction();
        
        Set set = new HashSet(1);                
        set.add(createdObject.getPrimaryFile());
        return set;
    }

    public void initialize(TemplateWizard wizard) {
        this.wizard = wizard;
        
        //when initialize the selected value for first panel
        //option is set to 
        //WizardConstants.WIZARD_FIRST_PANEL_KEY_VALUE_CREATE_EMPTY_IEP
        this.wizard.putProperty(WizardConstants.WIZARD_FIRST_PANEL_SELECTION_KEY, WizardConstants.WIZARD_FIRST_PANEL_KEY_VALUE_CREATE_EMPTY_IEP);
        
        //initialize all panels here:
//        panel1 = new IEPWizardPanel1();
        FileObject dir = Templates.getTargetFolder( this.wizard );
        Project project = FileOwnerQuery.getOwner(dir);
//        panel2 = new IEPWizardPanel2(project);
        panel2EmptyIEPFile = new IEPWizardPanel2EmptyIEPFile(wizard);
        //panel3 = new IEPWizardPanel3(project);
        
        Object prop = wizard.getProperty("WizardPanel_contentData");
        if (prop != null && prop instanceof String[]) {
            wizardContentData = (String[]) prop;
        }
        
        //first time we call refresh panel manually so 
        //that we have initially set of panels based on 
        //initial iep file creation option.
//        refreshPanels();
//        
//        //add a listener on first panel for listening
//        //for changes in first panel iep file creation option
//        IEPVisualPanel1 panel1Visual = (IEPVisualPanel1) panel1.getComponent();
//        panel1Visual.getButtonModel().addItemListener(new IEPFileCreationOptionItemListener());
       
    }

    public void uninitialize(TemplateWizard wizard) {
        panels = null;
    }

    public WizardDescriptor.Panel current() {
        return getPanels()[index];
    }

    public String name() {
        return index + 1 + ". from " + getPanels().length;
    }

    public boolean hasNext() {
        return index < getPanels().length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

//    // If nothing unusual changes in the middle of the wizard, simply:
//    public void addChangeListener(ChangeListener l) {
//    }
//
//    public void removeChangeListener(ChangeListener l) {
//    }

    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then uncomment
    // the following and call when needed: fireChangeEvent();
    
    private Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0
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
     

    // You could safely ignore this method. Is is here to keep steps which were
    // there before this wizard was instantiated. It should be better handled
    // by NetBeans Wizard API itself rather than needed to be implemented by a
    // client code.
    private String[] createSteps() {
        String[] beforeSteps = null;
//        Object prop = wizard.getProperty("WizardPanel_contentData");
//        if (prop != null && prop instanceof String[]) {
//            beforeSteps = (String[]) prop;
//        }

        beforeSteps = wizardContentData;
        
        if (beforeSteps == null) {
            beforeSteps = new String[0];
        }

        String[] res = new String[(beforeSteps.length - 1) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (beforeSteps.length - 1)) {
                res[i] = beforeSteps[i];
            } else {
                res[i] = panels[i - beforeSteps.length + 1].getComponent().getName();
            }
        }
        return res;
    }

   
    
//    private void refreshPanels() {
//        String firstPanelSelectionValue = (String) this.wizard.getProperty(WizardConstants.WIZARD_FIRST_PANEL_SELECTION_KEY);
//        
//        if(firstPanelSelectionValue.equals(WizardConstants.WIZARD_FIRST_PANEL_KEY_VALUE_CREATE_EMPTY_IEP)) {
//            panels = new WizardDescriptor.Panel[]{
//                panel1,
//                panel2EmptyIEPFile
//            };
//            
//        } else {
//            panels = new WizardDescriptor.Panel[]{
//                panel1,
//                panel2,
//                panel3
//            };
//            
//        }
//        
//        initializePanels();
//        
//    }
    
//    private void initializePanels() {
//        String[] steps = createSteps();
//            for (int i = 0; i < panels.length; i++) {
//                Component c = panels[i].getComponent();
//                if (steps[i] == null) {
//                    // Default step name to component name of panel. Mainly
//                    // useful for getting the name of the target chooser to
//                    // appear in the list of steps.
//                    steps[i] = c.getName();
//                }
//                if (c instanceof JComponent) { // assume Swing components
//                    JComponent jc = (JComponent) c;
//                    // Sets step number of a component
//                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
//                    // Sets steps names for a panel
//                    jc.putClientProperty("WizardPanel_contentData", steps);
//                    // Turn on subtitle creation on each step
//                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
//                    // Show steps on the left side with the image on the background
//                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
//                    // Turn on numbering of all steps
//                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
//                }
//            }
//    }
    
//    class IEPFileCreationOptionItemListener implements ItemListener {
//
//        public void itemStateChanged(ItemEvent e) {
//            IEPVisualPanel1 panel1Visual = (IEPVisualPanel1) panel1.getComponent();
//            String selectedCreationOption = panel1Visual.getSelectedOption();
//            IEPWizardIterator.this.wizard.putProperty(WizardConstants.WIZARD_FIRST_PANEL_SELECTION_KEY, selectedCreationOption);
//            
//            refreshPanels();
//            fireChangeEvent();
//        }
//        
//    }
}
