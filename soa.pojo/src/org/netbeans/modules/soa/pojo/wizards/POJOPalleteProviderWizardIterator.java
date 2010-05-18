/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.soa.pojo.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import javax.xml.namespace.QName;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.soa.pojo.listeners.POJOPalleteProviderFileListener;
import org.netbeans.modules.soa.pojo.util.GeneratorUtil;
import org.netbeans.modules.soa.pojo.util.POJOMessageExchangePattern;
import org.netbeans.modules.soa.pojo.util.POJOSupportedDataTypes;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

public final class POJOPalleteProviderWizardIterator  implements WizardDescriptor.ProgressInstantiatingIterator {

    private int index;
    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    public WizardDescriptor.Panel[] getPanels() {

      //  WizardDescriptor.Panel packageChooserPanel = new DelegatingWizardPanel(
       //         JavaTemplates.createPackageChooser(project, sourceGroups, null, true),new POJOProviderWizardMEPOperationTypePanel());
        Project project = (Project) this.wizard.getProperty(GeneratorUtil.PROJECT_INSTANCE);
        SourceGroup[] sg = GeneratorUtil.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        OperationMethodChooserPanelWizardDescriptor opWiz = new OperationMethodChooserPanelWizardDescriptor( new OperationMethodChooserPanel());
        OperationMethodChooserPanel opPanel = (OperationMethodChooserPanel)opWiz.getComponent();
        opPanel.setMethodName((String)this.wizard.getProperty(GeneratorUtil.POJO_METHOD_NAME) );
        WizardDescriptor.Panel wizardDescriptorPanel = new MultiTargetChooserPanel(project,sg, opWiz, true);        
        MultiTargetChooserPanelGUI gui = (MultiTargetChooserPanelGUI)wizardDescriptorPanel.getComponent();

        gui.setClassName( (String)this.wizard.getProperty(GeneratorUtil.POJO_CLASS_NAME) );
        
      //
        gui.setPackageName((String)this.wizard.getProperty(GeneratorUtil.POJO_PACKAGE_NAME) );
        gui.setEditable(false);
        gui.setEnable(false);
        if (panels == null) {
            panels = new WizardDescriptor.Panel[]{
                        /*new POJOPalleteProviderWizardFileInfoPanel(),
                        new POJOProviderWizardMEPOperationTypePanel(),
                        new POJOProviderWizardAdvancedPanel()
                         * */
                         wizardDescriptorPanel
                    };
            String[] steps = new String[panels.length];
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
        return panels;
    }

    public Set instantiate() throws IOException {
        return Collections.EMPTY_SET;
    }

    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
    }

    public void uninitialize(WizardDescriptor wizard) {
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

    // If nothing unusual changes in the middle of the wizard, simply:
    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }

    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then uncomment
    // the following and call when needed: fireChangeEvent();
    /*
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
     */    // You could safely ignore this method. Is is here to keep steps which were
    // there before this wizard was instantiated. It should be better handled
    // by NetBeans Wizard API itself rather than needed to be implemented by a
    // client code.
    private String[] createSteps() {
        String[] beforeSteps = null;
        Object prop = wizard.getProperty("WizardPanel_contentData");
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }

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
    


    public Set instantiate(ProgressHandle handle) throws IOException {
        Project project = Templates.getProject(wizard);   
        if ( project == null) {
            project= (Project) wizard.getProperty(GeneratorUtil.PROJECT_INSTANCE);            
        }
        if ( project != null) {
            POJOHelper.registerPOJOBuildScript(project);
        }
        String methodName = (String) wizard.getProperty(GeneratorUtil.POJO_OPERATION_METHOD_NAME);
        POJOSupportedDataTypes pojoSDT = ((POJOSupportedDataTypes) wizard.getProperty(GeneratorUtil.POJO_INPUT_TYPE));
        String inputType =pojoSDT.formatToString(pojoSDT, true) ;
        POJOMessageExchangePattern mep = (POJOMessageExchangePattern) wizard.getProperty(GeneratorUtil.POJO_OPERATION_PATTERN);
        String outputType = null;
        pojoSDT = ((POJOSupportedDataTypes) wizard.getProperty(GeneratorUtil.POJO_OUTPUT_TYPE));
        outputType=pojoSDT.formatToString(pojoSDT, false) ;
        JavaSource javaSource =(JavaSource) wizard.getProperty(GeneratorUtil.POJO_JAVA_SOURCE_INSTANCE);
        JTextComponent focusedComponent = Utilities.getFocusedComponent();
        Map<String,Object> annotationsProps = null;
        Map<String, Object> opnAnnotationProps = new HashMap<String,Object>();
        opnAnnotationProps.put(GeneratorUtil.OUT_MSGTYPE_QNAME_CONST,new QName((String)wizard.getProperty(GeneratorUtil.POJO_OUTMSG_TYPE_NS),(String) wizard.getProperty(GeneratorUtil.POJO_OUTMSG_TYPE_NAME)).toString());
        //opnAnnotationProps.put(GeneratorUtil.OUT_MSGTYPE_NS_CONST, wizard.getProperty(GeneratorUtil.POJO_OUTMSG_TYPE_NS));                
        
        if (wizard.getProperty(GeneratorUtil.POJO_ADVANCED_SAVED) != null) {

            String className = (String) wizard.getProperty(GeneratorUtil.POJO_CLASS_NAME);
            String endpointName = (String) wizard.getProperty(GeneratorUtil.POJO_ENDPOINT_NAME);
            String packageName = GeneratorUtil.getNamespace((String) wizard.getProperty(GeneratorUtil.POJO_PACKAGE_NAME), className);
            String interfaceNs = (String) wizard.getProperty(GeneratorUtil.POJO_INTERFACE_NS);
            String interfaceName = (String) wizard.getProperty(GeneratorUtil.POJO_INTERFACE_NAME);
            String serviceName = (String) wizard.getProperty(GeneratorUtil.POJO_SERVICE_NAME);
            String serviceNs = (String) wizard.getProperty(GeneratorUtil.POJO_SERVICE_NS);


            if (!((className.equals(endpointName)) && packageName.equals(interfaceNs) &&
                    serviceNs.equals(interfaceNs) && interfaceName.equals(className + GeneratorUtil.POJO_INTERFACE_SUFFIX) &&
                    serviceName.equals(className + GeneratorUtil.POJO_SERVICE_SUFFIX))) {
                annotationsProps = new HashMap<String,Object>();

                annotationsProps.put(GeneratorUtil.NAME_CONST, endpointName);
                annotationsProps.put(GeneratorUtil.INTF_QNAME_CONST, new QName(interfaceNs, interfaceName).toString());                
                annotationsProps.put(GeneratorUtil.SERVICE_QNAME, new QName(serviceNs, serviceName).toString());                
                        
            }

        }
       

        try {
            GeneratorUtil.addPOJO(javaSource, methodName, outputType, Collections.singletonList(inputType), GeneratorUtil.POJO_OPERATION_ANNOTATION, annotationsProps, opnAnnotationProps);
        } catch (Throwable ex) {
            Exceptions.printStackTrace(ex);
        }
        File pojoFile = new File((String) wizard.getProperty(GeneratorUtil.POJO_FILE_LOCATION));        
        FileObject fo = FileUtil.toFileObject(pojoFile);
        
        POJOPalleteProviderFileListener poFileListener = new POJOPalleteProviderFileListener(fo, Collections.unmodifiableMap(wizard.getProperties()),outputType, project);
        return  Collections.singleton(fo);
    }
    
  }
