/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.persistence.wizard.dao;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.util.AbstractTask;
import org.netbeans.modules.j2ee.persistence.util.GenerationUtils;
import org.netbeans.modules.j2ee.persistence.action.EntityManagerGenerator;
import org.netbeans.modules.j2ee.persistence.action.GenerationOptions;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.wizard.PersistenceClientEntitySelection;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.j2ee.persistence.wizard.WizardProperties;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public final class EjbFacadeWizardIterator implements WizardDescriptor.InstantiatingIterator {
    
    private static final String WIZARD_PANEL_CONTENT_DATA = "WizardPanel_contentData"; // NOI18N
    
    private int index;
    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;
    private String[] steps;
    private int stepsStartPos;
    
    private WizardDescriptor.Panel[] getPanels() {
        return panels;
    }
    
    
    public Set instantiate() throws IOException {
        List<Entity> entities = (List<Entity>) wizard.getProperty(WizardProperties.ENTITY_CLASS);
        final FileObject targetFolder = Templates.getTargetFolder(wizard);
        final Set createdFiles = new HashSet();
        final EjbFacadeWizardPanel2 panel = (EjbFacadeWizardPanel2) panels[1];
        String pkg = panel.getPackage();
        
        PersistenceUnit persistenceUnit = (PersistenceUnit) wizard.getProperty(WizardProperties.PERSISTENCE_UNIT);
        if (persistenceUnit != null){
            try{
                ProviderUtil.addPersistenceUnit(persistenceUnit, Templates.getProject(wizard));
            } catch (InvalidPersistenceXmlException ipx){
                // just log for debugging purposes, at this point the user has
                // already been warned about an invalid persistence.xml
                Logger.getLogger(EjbFacadeWizardIterator.class.getName()).log(Level.FINE, "Invalid persistence.xml: " + ipx.getPath(), ipx); //NO18N
            }
        }
        
        for (Entity entity : entities) {
            final String entityClass = entity.getClass2();
            final String simpleClassName = Util.simpleClassName(entityClass);
            final String variableName = simpleClassName.toLowerCase().charAt(0) + simpleClassName.substring(1);
            final String facadeNameBase = pkg + "." + simpleClassName;
            //            String classBean = JMIUtils.uniqueClassName(facadeNameBase + "Facade", targetFolder);
            final String classBean = getUniqueClassName(facadeNameBase + "Facade", targetFolder);
            //            classBean = classBean.substring(classBean.lastIndexOf(".") + 1);
            
            final FileObject sourceFile = GenerationUtils.createClass(targetFolder, classBean, null);// name must be made unique
            createdFiles.add(sourceFile);
            createdFiles.addAll(generate(sourceFile, targetFolder, classBean, pkg, panel.isRemote(), panel.isLocal()));
            
        }
        return createdFiles;
    }
    
    
    Set<FileObject> generate(final FileObject sourcexFile, final FileObject targetFolder, final String classBean, String pkg, final boolean hasRemote, final boolean hasLocal) throws IOException{
        
        final Set<FileObject> createdFiles = new HashSet<FileObject>();
        final String entityClass = classBean;
        final String simpleClassName = Util.simpleClassName(entityClass);
        final String variableName = simpleClassName.toLowerCase().charAt(0) + simpleClassName.substring(1);
        final String facadeNameBase = pkg + "." + simpleClassName;
        //            String classBean = JMIUtils.uniqueClassName(facadeNameBase + "Facade", targetFolder);
        //        final String classBean = getUniqueClassName(facadeNameBase + "Facade", targetFolder);
        
        final FileObject facade = GenerationUtils.createClass(targetFolder, classBean + "Facade", null);
        createdFiles.add(facade);
        JavaSource source = JavaSource.forFileObject(facade);
        source.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                
                for (Tree typeDeclaration : cut.getTypeDecls()){
                    if (Tree.Kind.CLASS == typeDeclaration.getKind()){
                        ClassTree clazz = (ClassTree) typeDeclaration;
                        AnnotationTree annotations = make.Annotation(make.Identifier("javax.ejb.Stateless"), Collections.<ExpressionTree>emptyList());
                        ModifiersTree modifiers = make.Modifiers(clazz.getModifiers(), Collections.<AnnotationTree>singletonList(annotations));
                        ClassTree modifiedClass =
                                make.Class(modifiers, clazz.getSimpleName(), clazz.getTypeParameters(), clazz.getExtendsClause(), (List<ExpressionTree>)clazz.getImplementsClause(), Collections.<Tree>emptyList());
                        workingCopy.rewrite(clazz, modifiedClass);
                        
                        FileObject local = null;
                        FileObject remote = null;
                        if (hasLocal){
                            String classLocal = getUniqueClassName(facadeNameBase + "FacadeLocal", targetFolder);
                            classLocal = Util.simpleClassName(classLocal);
                            local = createInterface(classLocal, "javax.ejb.Local", targetFolder);
                            createdFiles.add(local);
                        }
                        if (hasRemote){
                            String classRemote = getUniqueClassName(facadeNameBase + "FacadeRemote", targetFolder);
                            classRemote = Util.simpleClassName(classRemote);
                            remote = createInterface(classRemote, "javax.ejb.Remote", targetFolder);
                            createdFiles.add(remote);
                        }
                        EntityManagerGenerator generator = new EntityManagerGenerator(facade, classBean);
                        
                        GenerationOptions createOptions = new GenerationOptions();
                        createOptions.setMethodName("create");
                        createOptions.setOperation(GenerationOptions.Operation.PERSIST);
                        createOptions.setReturnType("void");
                        createOptions.setParameterName(variableName);
                        createOptions.setParameterType(entityClass);
                        generator.generate(createOptions);
                        addMethodToInterface("create", "void", variableName, entityClass, local);
                        addMethodToInterface("create", "void", variableName, entityClass, remote);
                        
                        GenerationOptions editOptions = new GenerationOptions();
                        editOptions.setMethodName("edit");
                        editOptions.setOperation(GenerationOptions.Operation.PERSIST);
                        editOptions.setReturnType("void");
                        editOptions.setParameterName(variableName);
                        editOptions.setParameterType(entityClass);
                        generator.generate(editOptions);
                        addMethodToInterface("edit", "void", variableName, entityClass, local);
                        addMethodToInterface("edit", "void", variableName, entityClass, remote);
                        
                        GenerationOptions destroyOptions = new GenerationOptions();
                        destroyOptions.setMethodName("remove");
                        destroyOptions.setOperation(GenerationOptions.Operation.REMOVE);
                        destroyOptions.setReturnType("void");
                        destroyOptions.setParameterName(variableName);
                        destroyOptions.setParameterType(entityClass);
                        generator.generate(destroyOptions);
                        addMethodToInterface("destroy", "void", variableName, entityClass, local);
                        addMethodToInterface("destroy", "void", variableName, entityClass, remote);
                        
                        GenerationOptions findOptions = new GenerationOptions();
                        findOptions.setMethodName("find");
                        findOptions.setOperation(GenerationOptions.Operation.FIND);
                        findOptions.setReturnType("void");
                        findOptions.setParameterName(variableName);
                        findOptions.setParameterType(entityClass);
                        generator.generate(findOptions);
                        addMethodToInterface("find", "void", variableName, entityClass, local);
                        addMethodToInterface("find", "void", variableName, entityClass, remote);
                        
                        GenerationOptions findAllOptions = new GenerationOptions();
                        findAllOptions.setMethodName("findAll");
                        findAllOptions.setOperation(GenerationOptions.Operation.FIND_ALL);
                        findAllOptions.setReturnType("void");
                        findAllOptions.setParameterName(variableName);
                        findAllOptions.setParameterType(entityClass);
                        generator.generate(findAllOptions);
                        addMethodToInterface("findAll", "void", variableName, entityClass, local);
                        addMethodToInterface("findAll", "void", variableName, entityClass, remote);
                        
                    }
                }
            }
        });
        return createdFiles;
    }
    
    String getUniqueClassName(String candidateName, FileObject targetFolder){
        return candidateName; // TODO: RETOUCHE
    }
    
    
    /**
     * Creates an interface with the given <code>name</code>, annotated with an annotation
     * of the given <code>annotationType</code>. <i>Package private visibility just because of tests</i>.
     * @param
     * @param
     */
    FileObject createInterface(String name, final String annotationType, FileObject targetFolder) throws IOException{
        FileObject sourceFile = GenerationUtils.createInterface(targetFolder, name, null);
        JavaSource source = JavaSource.forFileObject(sourceFile);
        ModificationResult result = source.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws Exception {
                
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                
                for (Tree typeDeclaration : cut.getTypeDecls()){
                    if (Tree.Kind.CLASS == typeDeclaration.getKind()){
                        ClassTree clazz = (ClassTree) typeDeclaration;
                        AnnotationTree annotations = make.Annotation(make.Identifier(annotationType), Collections.<ExpressionTree>emptyList());
                        ModifiersTree modifiers = make.Modifiers(clazz.getModifiers(), Collections.<AnnotationTree>singletonList(annotations));
                        ClassTree modifiedClass =
                                make.Class(modifiers, clazz.getSimpleName(), clazz.getTypeParameters(), clazz.getExtendsClause(), Collections.<ExpressionTree>emptyList(), Collections.<Tree>emptyList());
                        workingCopy.rewrite(clazz, modifiedClass);
                    }
                }
            }
        });
        result.commit();
        return source.getFileObjects().iterator().next();
        
    }
    
    void addMethodToInterface(final String name, final String returnType, final String parameterName,
            final String parameterType, final FileObject target) throws IOException {
        
        if (target == null){
            return;
        }
        
        JavaSource source = JavaSource.forFileObject(target);
        ModificationResult result = source.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy parameter) throws Exception {
                parameter.toPhase(Phase.RESOLVED);
                TreeMaker make = parameter.getTreeMaker();
                CompilationUnitTree cut = parameter.getCompilationUnit();
                for (Tree tree : cut.getTypeDecls()){
                    if (tree.getKind() == Tree.Kind.CLASS){
                        ClassTree iface = (ClassTree) tree;
                        ModifiersTree mt = make.Modifiers(Collections.<Modifier>emptySet());
                        VariableTree vt = make.Variable(mt, parameterName, make.Identifier(parameterType), null);
                        MethodTree method = make.Method(mt,
                                name,
                                make.Identifier(returnType),
                                Collections.<TypeParameterTree>emptyList(),
                                Collections.<VariableTree>singletonList(vt),
                                Collections.<ExpressionTree>emptyList(),
                                (BlockTree) null,
                                null);
                        ClassTree modifiedClass = make.addClassMember(iface, method);
                        parameter.rewrite(iface, modifiedClass);
                    }
                }
            }
        });
        result.commit();
        
    }
    
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        wizard.putProperty("NewFileWizard_Title",
                NbBundle.getMessage(EjbFacadeWizardIterator.class, "Templates/Persistence/ejbFacade"));
        Project project = Templates.getProject(wizard);
        if (panels == null) {
            panels = new WizardDescriptor.Panel[] {
                new PersistenceClientEntitySelection(
                        NbBundle.getMessage(EjbFacadeWizardIterator.class, "LBL_EntityClasses"),
                        new HelpCtx(EjbFacadeWizardIterator.class.getName() + "$PersistenceClientEntitySelection"), wizard), // NOI18N
                        new EjbFacadeWizardPanel2(project, wizard)
            };
            if (steps == null) {
                mergeSteps(new String[] {
                    NbBundle.getMessage(EjbFacadeWizardIterator.class, "LBL_EntityClasses"),
                    NbBundle.getMessage(EjbFacadeWizardIterator.class, "LBL_GeneratedSessionBeans"),
                });
            }
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
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
    }
    
    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }
    
    public WizardDescriptor.Panel current() {
        return getPanels()[index];
    }
    
    public String name() {
        return NbBundle.getMessage(EjbFacadeWizardIterator.class, "LBL_FacadeWizardTitle");
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
    public void addChangeListener(ChangeListener l) {}
    public void removeChangeListener(ChangeListener l) {}
    
    private void mergeSteps(String[] thisSteps) {
        Object prop = wizard.getProperty(WIZARD_PANEL_CONTENT_DATA);
        String[] beforeSteps;
        
        if (prop instanceof String[]) {
            beforeSteps = (String[]) prop;
            stepsStartPos = beforeSteps.length;
            if (stepsStartPos > 0 && ("...".equals(beforeSteps[stepsStartPos - 1]))) { // NOI18N
                stepsStartPos--;
            }
        } else {
            beforeSteps = null;
            stepsStartPos = 0;
        }
        
        steps = new String[stepsStartPos + thisSteps.length];
        System.arraycopy(beforeSteps, 0, steps, 0, stepsStartPos);
        System.arraycopy(thisSteps, 0, steps, stepsStartPos, thisSteps.length);
    }
    
}
