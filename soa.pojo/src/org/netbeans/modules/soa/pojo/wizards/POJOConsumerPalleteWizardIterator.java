/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.soa.pojo.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.soa.pojo.util.GeneratorUtil;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

public final class POJOConsumerPalleteWizardIterator  implements WizardDescriptor.ProgressInstantiatingIterator {

    private int index;
    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;
    private POJOConsumerCreationPanelPalleteWizardPanel pCWiz = null;

    /**
//     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            pCWiz = new POJOConsumerCreationPanelPalleteWizardPanel();
            panels = new WizardDescriptor.Panel[]{
                        pCWiz
                    };
            String[] steps = new String[1];
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
    
    public POJOConsumerCreationPanelPalleteWizardPanel getConsumer() {
        return this.pCWiz;
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

    public Set instantiate(ProgressHandle arg0) throws IOException {
        File pojoFile = new File((String) wizard.getProperty(GeneratorUtil.POJO_FILE_LOCATION));
        
        FileObject fo = FileUtil.toFileObject(pojoFile);

        try {
            File tempFolder = (File) this.wizard.getProperty(GeneratorUtil.POJO_TEMP_FOLDER);

            if (tempFolder != null) {
                try {
                    tempFolder.delete();
                } catch (Exception ex) {
                }
            }
/*        try {
            GeneratorUtil.addPOJO(javaSource, methodName, outputType, Collections.singletonList(inputType), GeneratorUtil.POJO_OPERATION_ANNOTATION, annotationsProps, opnAnnotationProps);
            } catch (Throwable ex) {
            Exceptions.printStackTrace(ex);
            }
            File pojoFile = new File((String) wizard.getProperty(GeneratorUtil.POJO_FILE_LOCATION));
            FileObject fo = FileUtil.toFileObject(pojoFile);
            Boolean generateWSDL = (Boolean)wizard.getProperty(GeneratorUtil.POJO_GENERATE_WSDL);
            if (generateWSDL.booleanValue()) {
            GeneratorUtil.generateWSDL( FileUtil.toFileObject(pojoFile.getParentFile()) , wizard,!outputType.equals(GeneratorUtil.VOID_CONST));
            }
            Pojo pojo = new Pojo();
            pojo.setClassName((String)wizard.getProperty(GeneratorUtil.POJO_CLASS_NAME));
            pojo.setPackage((String)wizard.getProperty(GeneratorUtil.POJO_PACKAGE_NAME));
            Util.addPOJO2Model(project, pojo);
             */
            /////////////////
            JavaSource javaSource = (JavaSource) wizard.getProperty(GeneratorUtil.POJO_JAVA_SOURCE_INSTANCE);
            ExecutableElement method = (ExecutableElement) wizard.getProperty(GeneratorUtil.POJO_SELECTED_METHOD);
            Map<String, Object> props = wizard.getProperties();
            GeneratorUtil.addConsumer(javaSource, method, props);
            
        } catch (Throwable ex) {
            Exceptions.printStackTrace(ex);
        }
       /* String intfNm = (String) wizard.getProperty(GeneratorUtil.POJO_CONSUMER_INTERFACE_NAME);
        String intfNmNS = (String) wizard.getProperty(GeneratorUtil.POJO_CONSUMER_INTERFACE_NS);
        String opnNm = (String) wizard.getProperty(GeneratorUtil.POJO_CONSUMER_OPERATION_NAME);
        String inMsgNm = (String) wizard.getProperty(GeneratorUtil.POJO_CONSUMER_INPUT_MESSAGE_TYPE);
        String inMsgNmNs = (String) wizard.getProperty(GeneratorUtil.POJO_CONSUMER_INPUT_MESSAGE_TYPE_NS);
        Boolean bConsumerDrop = (Boolean)wizard.getProperty(GeneratorUtil.POJO_CONSUMER_DROP);
        if ( bConsumerDrop == null || bConsumerDrop == Boolean.FALSE ) {
            POJOConsumer pjs = new POJOConsumer();
            pjs.setInputMessageType(new QName(inMsgNmNs,inMsgNm));
            pjs.setInterface(new QName(intfNmNS,intfNm));
            pjs.setOperation(new QName(intfNmNS,opnNm));
        
            Boolean bSynchronous = (Boolean)wizard.getProperty(GeneratorUtil.POJO_CONSUMER_INVOKE_TYPE);
            if ( bSynchronous == Boolean.TRUE) {
                pjs.setInvokePattern(GeneratorUtil.SYNCH_CONST);    
            } else {
                pjs.setInvokePattern(GeneratorUtil.ASYNCH_CONST);    
            }

            pjs.setInvokeInputType(wizard.getProperty(GeneratorUtil.POJO_CONSUMER_INPUT_TYPE).toString());
            pjs.setInvokeReturnType(wizard.getProperty(GeneratorUtil.POJO_CONSUMER_OUTPUT_TYPE).toString());
            Project prj = (Project)wizard.getProperty(GeneratorUtil.PROJECT_INSTANCE);
            if ( !Util.pojoConsumerExist(prj, pjs) ) {
                Util.addPOJOConsumer2Model(prj,pjs);
            }
        }*/
        return Collections.singleton(fo);
    }
}
