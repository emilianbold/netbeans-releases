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
import java.util.Arrays;
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
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.ContainerManagedJTAInjectableInEJB;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategy;
import org.netbeans.modules.j2ee.persistence.wizard.PersistenceClientEntitySelection;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.j2ee.persistence.wizard.WizardProperties;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Generates EJB facades for entity classes.
 * 
 * @author Martin Adamek, Erno Mononen
 */ 
    public final class EjbFacadeWizardIterator implements WizardDescriptor.InstantiatingIterator {
    
    private static final String WIZARD_PANEL_CONTENT_DATA = "WizardPanel_contentData"; // NOI18N

    private static final String FACADE_SUFFIX = "Facade"; //NO18N
    private static final String FACADE_REMOTE_SUFFIX = FACADE_SUFFIX + "Remote"; //NO18N
    private static final String FACADE_LOCAL_SUFFIX = FACADE_SUFFIX + "Local"; //NO18N
    private static final String EJB_LOCAL = "javax.ejb.Local"; //NO18N
    private static final String EJB_REMOTE = "javax.ejb.Remote"; //NO18N
    private static final String EJB_STATELESS = "javax.ejb.Stateless"; //NO18N

    private int index;
    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;
    private String[] steps;
    private int stepsStartPos;
    
    private static final String EJB30_STATELESS_EJBCLASS = "Templates/J2EE/EJB30/StatelessEjbClass.java"; // NOI18N
    
    private WizardDescriptor.Panel[] getPanels() {
        return panels;
    }
    
    public Set instantiate() throws IOException {
        @SuppressWarnings("unchecked")
        List<String> entities = (List<String>) wizard.getProperty(WizardProperties.ENTITY_CLASS);
        final FileObject targetFolder = Templates.getTargetFolder(wizard);
        final Set<FileObject> createdFiles = new HashSet<FileObject>();
        final EjbFacadeWizardPanel2 panel = (EjbFacadeWizardPanel2) panels[1];
        String pkg = panel.getPackage();
        
        PersistenceUnit persistenceUnit = (PersistenceUnit) wizard.getProperty(WizardProperties.PERSISTENCE_UNIT);
        if (persistenceUnit != null) {
            try {
                ProviderUtil.addPersistenceUnit(persistenceUnit, Templates.getProject(wizard));
            } catch (InvalidPersistenceXmlException ipx) {
                // just log for debugging purposes, at this point the user has
                // already been warned about an invalid persistence.xml
                Logger.getLogger(EjbFacadeWizardIterator.class.getName()).log(Level.FINE, "Invalid persistence.xml: " + ipx.getPath(), ipx); //NO18N
            }
        }
        
        for (String entity : entities) {
            createdFiles.addAll(generate(targetFolder, entity, pkg, panel.isRemote(), panel.isLocal()));
        }
        return createdFiles;
    }
    
    /**
     * Generates the facade and the loca/remote interface(s) for the given
     * entity class.
     * @param targetFolder the folder where the facade and interfaces are generated.
     * @param entityClass the FQN of the entity class for which the facade is generated.
     * @param pkg the package prefix for the generated facede.
     * @param hasRemote specifies whether a remote interface is generated.
     * @param hasLocal specifies whether a local interface is generated.
     * 
     * @return a set containing the generated files.
     */ 
    private Set<FileObject> generate(final FileObject targetFolder, final String entityClass, String pkg, final boolean hasRemote, final boolean hasLocal) throws IOException {
        return generate(targetFolder, entityClass, pkg, hasRemote, hasLocal, ContainerManagedJTAInjectableInEJB.class);
    }
    
    
    /**
     * Generates the facade and the loca/remote interface(s) for thhe given
     * entity class.
     * <i>Package private visibility for tests</i>.
     * @param targetFolder the folder where the facade and interfaces are generated.
     * @param entityClass the FQN of the entity class for which the facade is generated.
     * @param pkg the package prefix for the generated facede.
     * @param hasRemote specifies whether a remote interface is generated.
     * @param hasLocal specifies whether a local interface is generated.
     * @param strategyClass the entity manager lookup strategy. 
     * 
     * @return a set containing the generated files.
     */ 
    Set<FileObject> generate(final FileObject targetFolder, final String entityFQN, 
            String pkg, final boolean hasRemote, final boolean hasLocal, final Class<? extends EntityManagerGenerationStrategy> strategyClass) throws IOException {
        
        final Set<FileObject> createdFiles = new HashSet<FileObject>();
        final String entitySimpleName = Util.simpleClassName(entityFQN);
        final String variableName = entitySimpleName.toLowerCase().charAt(0) + entitySimpleName.substring(1);
        
        // create the facade
        final FileObject facade = GenerationUtils.createClass(EJB30_STATELESS_EJBCLASS, targetFolder, entitySimpleName + FACADE_SUFFIX, null);
        createdFiles.add(facade);
        
        // generate methods for the facade
        EntityManagerGenerator generator = new EntityManagerGenerator(facade, entityFQN);
        List<GenerationOptions> methodOptions = getMethodOptions(entityFQN, variableName);
        for (GenerationOptions each : methodOptions){
            generator.generate(each, strategyClass);
        }

        // create the interfaces
        final String localInterfaceFQN = pkg + "." + getUniqueClassName(entitySimpleName + FACADE_LOCAL_SUFFIX, targetFolder);
        final String remoteInterfaceFQN = pkg + "." + getUniqueClassName(entitySimpleName + FACADE_REMOTE_SUFFIX, targetFolder);

        if (hasLocal) {
            FileObject local = createInterface(Util.simpleClassName(localInterfaceFQN), EJB_LOCAL, targetFolder);
            addMethodToInterface(methodOptions, local);
            createdFiles.add(local);
        }
        if (hasRemote) {
            FileObject remote = createInterface(Util.simpleClassName(remoteInterfaceFQN), EJB_REMOTE, targetFolder);
            addMethodToInterface(methodOptions, remote);
            createdFiles.add(remote);
        }

        // add implements clauses to the facade
        JavaSource source = JavaSource.forFileObject(facade);
        source.runModificationTask(new AbstractTask<WorkingCopy>() {

            public void run(WorkingCopy parameter) throws Exception {
                GenerationUtils genUtils = GenerationUtils.newInstance(parameter);
                ClassTree classTree = genUtils.getClassTree();
                if (hasLocal){
                    classTree = genUtils.addImplementsClause(classTree, localInterfaceFQN);
                }
                if (hasRemote){
                    classTree = genUtils.addImplementsClause(classTree, remoteInterfaceFQN);
                }
                parameter.rewrite(genUtils.getClassTree(), classTree);
            }
        }).commit();
        
        return createdFiles;
    }
    
    /**
     * @return the options representing the methods for a facade, i.e. create/edit/
     * find/remove/findAll.
     */ 
    private List<GenerationOptions> getMethodOptions(String entityFQN, String variableName){

        GenerationOptions createOptions = new GenerationOptions();
        createOptions.setMethodName("create"); //NO18N
        createOptions.setOperation(GenerationOptions.Operation.PERSIST);
        createOptions.setReturnType("void");//NO18N
        createOptions.setParameterName(variableName);
        createOptions.setParameterType(entityFQN);

        GenerationOptions editOptions = new GenerationOptions();
        editOptions.setMethodName("edit");//NO18N
        editOptions.setOperation(GenerationOptions.Operation.MERGE);
        editOptions.setReturnType("void");//NO18N
        editOptions.setParameterName(variableName);
        editOptions.setParameterType(entityFQN);

        GenerationOptions destroyOptions = new GenerationOptions();
        destroyOptions.setMethodName("remove");//NO18N
        destroyOptions.setOperation(GenerationOptions.Operation.REMOVE);
        destroyOptions.setReturnType("void");//NO18N
        destroyOptions.setParameterName(variableName);
        destroyOptions.setParameterType(entityFQN);

        GenerationOptions findOptions = new GenerationOptions();
        findOptions.setMethodName("find");//NO18N
        findOptions.setOperation(GenerationOptions.Operation.FIND);
        findOptions.setReturnType(entityFQN);//NO18N
        findOptions.setParameterName("id");//NO18N
        findOptions.setParameterType("Object");//NO18N

        GenerationOptions findAllOptions = new GenerationOptions();
        findAllOptions.setMethodName("findAll");//NO18N
        findAllOptions.setOperation(GenerationOptions.Operation.FIND_ALL);
        findAllOptions.setReturnType("java.util.List");//NO18N
        
        return Arrays.<GenerationOptions>asList(createOptions, editOptions, destroyOptions, findOptions, findAllOptions);
    }
    
    String getUniqueClassName(String candidateName, FileObject targetFolder){
        return FileUtil.findFreeFileName(targetFolder, candidateName, "java"); //NO18N
    }
    
    /**
     * Creates an interface with the given <code>name</code>, annotated with an annotation
     * of the given <code>annotationType</code>. <i>Package private visibility just because of tests</i>.
     * 
     * @param name the name for the interface
     * @param annotationType the FQN of the annotation
     * @param targetFolder the folder to which the interface is generated
     * 
     * @return the generated interface.
     */
    FileObject createInterface(String name, final String annotationType, FileObject targetFolder) throws IOException {
        FileObject sourceFile = GenerationUtils.createInterface(targetFolder, name, null);
        JavaSource source = JavaSource.forFileObject(sourceFile);
        ModificationResult result = source.runModificationTask(new AbstractTask<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws Exception {
                
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                
                for (Tree typeDeclaration : cut.getTypeDecls()) {
                    if (Tree.Kind.CLASS == typeDeclaration.getKind()) {
                        ClassTree clazz = (ClassTree) typeDeclaration;
                        AnnotationTree annotations = make.Annotation(make.Identifier(annotationType), Collections.<ExpressionTree>emptyList());
                        ModifiersTree modifiers = make.Modifiers(clazz.getModifiers(), Collections.<AnnotationTree>singletonList(annotations));
                        ClassTree modifiedClass = make.Class(modifiers, clazz.getSimpleName(), clazz.getTypeParameters(), clazz.getExtendsClause(), Collections.<ExpressionTree>emptyList(), Collections.<Tree>emptyList());
                        workingCopy.rewrite(clazz, modifiedClass);
                    }
                }
            }
        });
        result.commit();
        return source.getFileObjects().iterator().next();
    }
    
    /**
     * Adds a method to the given interface.
     * 
     * @param name the name of the method.
     * @param returnType the return type of the method.
     * @param parameterName the name of the parameter for the method.
     * @param parameterType the FQN type of the parameter.
     * @param target the target interface.
     */ 
    void addMethodToInterface(final List<GenerationOptions> options, final FileObject target) throws IOException {
        
        JavaSource source = JavaSource.forFileObject(target);
        ModificationResult result = source.runModificationTask(new AbstractTask<WorkingCopy>() {
            
            public void run(WorkingCopy parameter) throws Exception {
                parameter.toPhase(Phase.RESOLVED);
                TreeMaker make = parameter.getTreeMaker();
                CompilationUnitTree cut = parameter.getCompilationUnit();
                for (Tree tree : cut.getTypeDecls()) {
                    if (tree.getKind() == Tree.Kind.CLASS) {
                        ClassTree original = (ClassTree) tree;
                        ClassTree modifiedClass = original;
                        for (GenerationOptions each : options) {
                            MethodTree method = make.Method(make.Modifiers(Collections.<Modifier>emptySet()),
                                    each.getMethodName(), make.Identifier(each.getReturnType()), Collections.<TypeParameterTree>emptyList(),
                                    getParameterList(each, make), Collections.<ExpressionTree>emptyList(), (BlockTree) null, null);
                            modifiedClass = make.addClassMember(modifiedClass, method);
                        }
                        parameter.rewrite(original, modifiedClass);
                    }
                }
            }
        });
        result.commit();
    }
    
    private List<VariableTree> getParameterList(GenerationOptions options, TreeMaker make){
        if (options.getParameterName() == null){
            return Collections.<VariableTree>emptyList();
        }
        VariableTree vt = make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), 
                options.getParameterName(), make.Identifier(options.getParameterType()), null);
        return Collections.<VariableTree>singletonList(vt);
    }
    
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        wizard.putProperty("NewFileWizard_Title", NbBundle.getMessage(EjbFacadeWizardIterator.class, "Templates/Persistence/ejbFacade"));
        Project project = Templates.getProject(wizard);
        if (panels == null) {
            panels = new WizardDescriptor.Panel[]{new PersistenceClientEntitySelection(NbBundle.getMessage(EjbFacadeWizardIterator.class, "LBL_EntityClasses"), new HelpCtx(EjbFacadeWizardIterator.class.getName() + "$PersistenceClientEntitySelection"), wizard), new EjbFacadeWizardPanel2(project, wizard)};
            if (steps == null) {
                mergeSteps(new String[]{NbBundle.getMessage(EjbFacadeWizardIterator.class, "LBL_EntityClasses"), NbBundle.getMessage(EjbFacadeWizardIterator.class, "LBL_GeneratedSessionBeans")});
            }
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                if (c instanceof JComponent) {
                    // assume Swing components
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
