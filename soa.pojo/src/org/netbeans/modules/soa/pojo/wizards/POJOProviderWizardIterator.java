/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.soa.pojo.wizards;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import javax.xml.namespace.QName;
import org.openide.WizardDescriptor;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.soa.pojo.schema.POJOProvider;
import org.netbeans.modules.soa.pojo.util.GeneratorUtil;
import org.netbeans.modules.soa.pojo.util.POJOSupportedDataTypes;
import org.netbeans.modules.soa.pojo.util.Util;
import org.netbeans.spi.project.support.GenericSources;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;

public final class POJOProviderWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {
    private int index;
    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;
    private OperationMethodChooserPanelWizardDescriptor mOpWiz = null;
    private MultiTargetChooserPanel mMultiWizPanel = null;

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            Project project = Templates.getProject(wizard);
            Sources sources = (Sources) project.getLookup().lookup(Sources.class);
            SourceGroup[] sourceGroups = SourceGroups.getJavaSourceGroups(project);

            OperationMethodChooserPanelWizardDescriptor opWiz =
                    new OperationMethodChooserPanelWizardDescriptor(
                    new OperationMethodChooserPanel());
            opWiz.setWizard(wizard);
            mOpWiz = opWiz;
            WizardDescriptor.Panel wizardDescriptorPanel =
                    new MultiTargetChooserPanel(project, sourceGroups, opWiz, true);
            mMultiWizPanel = (MultiTargetChooserPanel) wizardDescriptorPanel;


            panels = new WizardDescriptor.Panel[]{
                        wizardDescriptorPanel};
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
        return panels;
    }

    OperationMethodChooserPanelWizardDescriptor getPOJOProvider() {
        return this.mOpWiz;
    }
    public Set instantiate() throws IOException {
        return Collections.EMPTY_SET;
    }

    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
    }

    public Set instantiate(ProgressHandle handle) throws IOException {
        try {
            if ( handle != null) {
            handle.start();
            }
            FileObject folder =  (FileObject) wizard.getProperty(GeneratorUtil.POJO_DEST_FOLDER);
            String className = (String) wizard.getProperty(GeneratorUtil.POJO_DEST_NAME);
            Project project = Templates.getProject(wizard);

            if ( className == null) {
                className = Templates.getTargetName(wizard);
                folder = Templates.getTargetFolder(wizard);
            }

            String pkgName = Util.getSelectedPackageName(folder);

            FileObject defaultFS = FileUtil.getConfigRoot();
            FileObject templateFO = defaultFS.getFileObject("Templates/ESB/POJOProvider.java");//NOI18N
            

            DataFolder targetFolder = DataFolder.findFolder(folder);
            FileUtil.toFile(targetFolder.getPrimaryFile()).mkdirs();
            DataObject templateDO = DataObject.find(templateFO);
            Map params = new HashMap();

            POJOSupportedDataTypes pojoSDT = ((POJOSupportedDataTypes) wizard.getProperty(GeneratorUtil.POJO_INPUT_TYPE));
            String inputType1 =pojoSDT.formatToString(pojoSDT, true) ;
            String inputType = inputType1;
            if (! inputType.equals("")) {
                inputType = inputType + " input"; //NOI18N
            }
            pojoSDT = ((POJOSupportedDataTypes) wizard.getProperty(GeneratorUtil.POJO_OUTPUT_TYPE));
            String outputType=pojoSDT.formatToString(pojoSDT, false) ;
            params.put("pojoreturntype",  outputType ); //NOI18N

            params.put("pojoreturn", "null");//NOI18N
            if ( inputType != null &&  outputType != null && outputType.equals(inputType1)) {
                params.put("pojoreturn", "input");//NOI18N
            }
            params.put("pojomethodname",(String) wizard.getProperty(GeneratorUtil.POJO_OPERATION_METHOD_NAME));//NOI18N
            params.put("pojoinputtype", inputType);//NOI18N
            if (! outputType.equals("void") ) {//NOI18N
                params.put(GeneratorUtil.GENERATE_OPERATION_ANNOTATIONS, "("+GeneratorUtil.OUT_MSGTYPE_QNAME_CONST+"=\""+ (new QName((String) wizard.getProperty(GeneratorUtil.POJO_OUTMSG_TYPE_NS),(String) wizard.getProperty(GeneratorUtil.POJO_OUTMSG_TYPE_NAME)).toString())+"\")");//NOI18N
            } else {
                params.put(GeneratorUtil.GENERATE_OPERATION_ANNOTATIONS, "");//NOI18N
            }
            if (wizard.getProperty(GeneratorUtil.POJO_ADVANCED_SAVED) == null) {
                params.put(GeneratorUtil.GENERATE_POJO_ANNOTATIONS, "");
            } else {
                params.put(GeneratorUtil.GENERATE_POJO_ANNOTATIONS, GeneratorUtil.generatePOJOAnnotations(wizard));
            }

            //@POJO (name="NewPOJOProvider", interfaceNS="",interfaceName="",serviceNS="",serviceName="" )
            //@Operation (name="",outMessageType="",outMessageTypeNS="")
            FileObject createdFile = templateDO.createFromTemplate(targetFolder, className, params).getPrimaryFile();
            POJOHelper.registerPOJOBuildScript(project);
            FileObject fileObject = createdFile;
            JavaSource javaSource = JavaSource.forFileObject(fileObject);
            GenerateTask<WorkingCopy> genTask = new GenerateTask<WorkingCopy> ();
            ModificationResult result = null;

            try {
                result = javaSource.runModificationTask(genTask);
            } catch (IOException ioe) {
                Exception taskException = genTask.getException();
                if (taskException != null) {
                    NotifyDescriptor d = new NotifyDescriptor.Exception(taskException);
                    DialogDisplayer.getDefault().notifyLater(d);
                } else {
                    throw ioe;
                }
            }
            result.commit();
            Boolean generateWSDL = (Boolean)wizard.getProperty(GeneratorUtil.POJO_GENERATE_WSDL);
            String wsdlFile = null;
            POJOProvider pojo = new POJOProvider();
            pojo.setClassName(className);
            pojo.setPackage(pkgName);

            if (generateWSDL.booleanValue()) {
                wsdlFile = GeneratorUtil.generateWSDL(project, folder, 
                        Collections.unmodifiableMap(wizard.getProperties()),
                        !outputType.equals(GeneratorUtil.VOID_CONST));
            }

            // Check if Binding Wizard has already generated the WSDL.
            if ( wsdlFile == null) {
                wsdlFile = (String)wizard.getProperty(GeneratorUtil.POJO_BC_WSDL_LOC);
                if ((wsdlFile != null) && (! "".equals(wsdlFile))){
                    File wsdlFl = new File(wsdlFile);
                    wsdlFile = FileUtil.getRelativePath(project.getProjectDirectory(),
                            FileUtil.toFileObject(wsdlFl));
                    pojo.setUpdateWsdlDuringBuild(false);
                }
            }

            pojo.setWsdlLocation(wsdlFile );
            Util.addPOJO2Model(project, pojo);

            return  Collections.singleton(createdFile);
        } finally {
            if ( handle != null) {
            handle.finish();
            }
        }
    }

    public Sources getSources(Project p) {
        Sources s = p.getLookup().lookup(Sources.class);
        if (s != null) {
            return s;
        } else {
            return GenericSources.genericOnly(p);
        }
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

      private static class DelegatingWizardPanel implements WizardDescriptor.Panel {
        private WizardDescriptor.Panel delegate;
        DelegatingWizardPanel(WizardDescriptor.Panel delegate,WizardDescriptor.Panel delegate2 ) {
            this.delegate = delegate;
        }

        public Component getComponent() {
               return delegate.getComponent();
        }

        public HelpCtx getHelp() {
            return new HelpCtx("org.netbeans.modules.soa.jca.base.about");
        }

        public void readSettings(Object arg0) {
            delegate.readSettings(arg0);
        }

        public void storeSettings(Object arg0) {
            delegate.storeSettings(arg0);
        }

        public boolean isValid() {
            return delegate.isValid();
        }

        public void addChangeListener(ChangeListener arg0) {
            delegate.addChangeListener(arg0);
        }

        public void removeChangeListener(ChangeListener arg0) {
            delegate.removeChangeListener(arg0);
        }

    }

    class GenerateTask<T extends WorkingCopy> implements Task<WorkingCopy> {

        private Exception myException = null;

        public Exception getException() {
            return myException;
        }

        public void run(WorkingCopy workingCopy) throws Exception {
            workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);

            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);

            String fullyQualifiedClassName = null;
            List<? extends TypeElement> elements = workingCopy.getTopLevelElements();
            if (elements.size() > 0) {
                TypeElement topElement = elements.get(0);
                fullyQualifiedClassName = topElement.getQualifiedName().toString();
            }
            if (fullyQualifiedClassName == null) {
                fullyQualifiedClassName = classTree.getSimpleName().toString();
            }
        }
    }
}
